package gregtech.common.metatileentities.multi.electric.generator.turbine;

import gregtech.api.capability.RotorHolder;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.items.metaitem.stats.TurbineRotor;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public abstract class LargeTurbineRecipeLogic extends MultiblockFuelRecipeLogic {

    protected final AbstractLargeTurbine turbine;

    private final FittingAdjustmentValues fittingAdjustmentValues = new FittingAdjustmentValues();

    /**
     * Optimal fuel flow rate in mB/t
     */
    protected int optimalFuelRate;
    protected int totalFluidConsumed;
    protected long lastEUt;

    private @Nullable FluidStack storedFluid;
    private boolean isSpinningDown;

    public LargeTurbineRecipeLogic(AbstractLargeTurbine tileEntity) {
        super(tileEntity);
        this.turbine = tileEntity;
    }

    @Override
    protected void updateRecipeProgress() {
        if (canRecipeProgress) {
            // turbines can void energy
            drawEnergy(recipeEUt, false);
            // as recipe starts with progress on 1 this has to be > only, not => to compensate for it
            if (++progressTime > maxProgressTime) {
                completeRecipe();
            }
        }
    }

    @Override
    protected boolean shouldSearchForRecipes() {
        // always keep going as long as the turbine is spinning down
        if (isSpinningDown) {
            return true;
        }
        return super.shouldSearchForRecipes();
    }

    @Override
    protected void completeRecipe() {
        super.completeRecipe();
        this.optimalFuelRate = 0;
        this.totalFluidConsumed = 0;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.optimalFuelRate = 0;
        this.totalFluidConsumed = 0;
        this.lastEUt = 0;
        this.isSpinningDown = false;
        this.storedFluid = null;
    }

    @Override
    protected void trySearchNewRecipe() {
        if (!checkMaintenance()) {
            return;
        }

        RotorHolder rotorHolder = turbine.getAbilities(MultiblockAbility.ROTOR_HOLDER_2).get(0);
        assert rotorHolder != null;
        TurbineRotor rotor = rotorHolder.rotor();
        if (rotor == null) {
            invalidate();
            return;
        }

        int baseEfficiency = rotor.baseEfficiency();
        assert rotor.baseEfficiency() > 0;

        long optimalEnergyFlow = rotor.optimalFlow();
        assert rotor.optimalFlow() > 0;

        long recipePower;
        Recipe recipe = findRecipe(Long.MAX_VALUE, getInputInventory(), getInputTank());
        if (recipe == null) {
            recipePower = 0;
            setEmptyOutputs();
            this.invalidInputsForRecipes = true;
        } else {
            float flowMultiplier = rotor.flowMultiplier(turbine.turbineType());
            assert flowMultiplier > 0;
            optimalEnergyFlow = (int) (optimalEnergyFlow * flowMultiplier);

            recipePower = computeRecipePower(recipe, rotorHolder.rotorFitting(), optimalEnergyFlow, baseEfficiency,
                    rotor.overflowEfficiency());
        }

        if (recipePower <= 0) {
            setEmptyOutputs();
        }

        long eut = computePowerOutput(recipePower);
        if (eut <= 0) {
            this.isSpinningDown = false;
            rotorHolder.setSpinning(false);
            return;
        }

        if (turbine.getOffsetTimer() % 20 == 0) {
            rotorHolder.damageRotor(getRotorDamage(rotorHolder.rotorFitting()));
            if (rotorHolder.rotor() == null) {
                invalidate();
                return;
            }
        }

        this.progressTime = 1;
        setMaxProgress(1);
        this.recipeEUt = -eut;
        this.lastEUt = eut;
        rotorHolder.setSpinning(true);
        if (wasActiveAndNeedsUpdate) {
            wasActiveAndNeedsUpdate = false;
        } else {
            setActive(true);
        }
    }

    /**
     * @param recipe             the recipe to run
     * @param rotorFit           the fitting of the rotor
     * @param optimalEnergyFlow  the optimal flow of the rotor in EU/t
     * @param baseEfficiency     the efficiency of the rotor
     * @param overflowEfficiency the rotor's overflow efficiency
     * @return the power in EU/t for the recipe
     */
    private long computeRecipePower(@NotNull Recipe recipe, @NotNull RotorFit rotorFit, long optimalEnergyFlow,
                                    int baseEfficiency, int overflowEfficiency) {
        var inputs = recipe.getFluidInputs();
        if (inputs.isEmpty()) {
            return 0;
        }

        FluidStack fuel = inputs.get(0).getInputFluidStack();
        if (fuel == null) {
            return 0;
        }

        assert fuel.amount != 0;
        if (fuel.getFluid() == null || !isFuelValid(fuel.getFluid())) {
            return 0;
        }

        if (storedFluid == null || !storedFluid.isFluidEqual(fuel)) {
            storedFluid = new FluidStack(fuel, 0);
        }
        assert storedFluid != null;

        fittingAdjustmentValues.optimalEnergyFlow = optimalEnergyFlow;
        fittingAdjustmentValues.baseEfficiency = baseEfficiency;
        applyRotorFitting(rotorFit, fittingAdjustmentValues);
        optimalEnergyFlow = fittingAdjustmentValues.optimalEnergyFlow;
        baseEfficiency = fittingAdjustmentValues.baseEfficiency;

        // EU/mB
        long energyDensity = computeEnergyDensity(recipe, fuel, optimalEnergyFlow);

        // EU/t / EU/mB = mB/t
        this.optimalFuelRate = GTUtility.safeCastLongToInt(optimalEnergyFlow / energyDensity);
        assert optimalFuelRate > 0;

        // Allowed to use beyond the base optimal flow rate depending on overflowEfficiency
        float overflowFactor = overflowMultiplier() * overflowEfficiency + flowFloor();
        int maxFuel = GTUtility.safeCastLongToInt((long) (optimalFuelRate * overflowFactor));
        if (maxFuel <= 0) {
            return 0;
        }

        if (!consumeRecipeInputs(fuel, maxFuel)) {
            return 0;
        }
        assert totalFluidConsumed > 0;

        setRecipeOutputs(recipe);
        assert itemOutputs != null;
        assert fluidOutputs != null;

        long totalEU = calculateOutputEUt(energyDensity);
        if (totalFluidConsumed != optimalFuelRate) {
            totalEU = (long) (totalEU * unoptimalEfficiency(overflowEfficiency));
        }

        totalEU = Math.max(1, totalEU * baseEfficiency / 10_000);
        return Math.min(totalEU, getMaxVoltage()); // voids extra power
    }

    /**
     * Computes the energy density of the fuel
     *
     * @param recipe            the recipe being run
     * @param fuel              the fuel
     * @param optimalEnergyFlow the optimal energy flow rate in EU/t
     * @return the energy density of the fuel in EU/mB
     */
    private long computeEnergyDensity(@NotNull Recipe recipe, @NotNull FluidStack fuel, long optimalEnergyFlow) {
        if (!usesRecipeEnergy()) {
            return 1;
        }

        // t * EU/t / mB = EU/mB
        long energyDensity = recipe.getDuration() * recipe.getEUt() / fuel.amount;
        if (optimalEnergyFlow >= energyDensity) {
            return energyDensity;
        }

        // turbine is too weak and/or fuel is too energy dense so will just use 1mB/t
        this.optimalFuelRate = 1;

        return optimalEnergyFlow;
    }

    /**
     * @return if energy generated by the recipe should be considered
     */
    protected boolean usesRecipeEnergy() {
        return true;
    }

    private long computePowerOutput(long recipePower) {
        long delta = recipePower - this.lastEUt;
        this.isSpinningDown = delta < 0;

        long absDelta = Math.abs(delta);
        // how much power can change by each tick: max(10, 1% total power)
        long maxDelta = Math.max(10L, absDelta / 100L);

        long eut;
        if (absDelta > maxDelta) {
            if (isSpinningDown) {
                eut = this.lastEUt - maxDelta;
            } else {
                eut = this.lastEUt + maxDelta;
            }
        } else {
            eut = recipePower;
        }

        // 10% EU/t reduction per maintenance problem
        eut = (long) (eut * (1 - (turbine.getNumMaintenanceProblems() * 0.1F)));
        return eut;
    }

    /**
     * @param fuel the fuel to test
     * @return if the fuel is allowed to be used in the turbine
     */
    protected abstract boolean isFuelValid(@NotNull Fluid fuel);

    /**
     * @param fuel   the fuel to consume
     * @param amount the amount to consume
     * @return if consumption was successful
     */
    protected boolean consumeRecipeInputs(@NotNull FluidStack fuel, int amount) {
        assert storedFluid != null;
        int toDrain = Math.min(amount, storedFluid.amount);
        this.storedFluid.amount -= toDrain;
        amount -= toDrain;
        this.totalFluidConsumed += toDrain;

        assert storedFluid.amount >= 0;
        assert amount >= 0;
        if (amount == 0) {
            return true;
        }

        FluidStack drained = getInputTank().drain(new FluidStack(fuel, amount), true);
        if (drained == null || drained.amount == 0) {
            return false;
        }

        assert drained.amount <= amount;
        this.totalFluidConsumed += drained.amount;

        return true;
    }

    /**
     * @param recipe the recipe to use
     */
    protected void setRecipeOutputs(@NotNull Recipe recipe) {
        RecipeMap<?> recipeMap = getRecipeMap();
        assert recipeMap != null;
        if (recipe.getFluidOutputs().isEmpty() && recipe.getChancedFluidOutputs().getChancedEntries().isEmpty()) {
            this.fluidOutputs = Collections.emptyList();
        } else {
            this.fluidOutputs = recipe.getResultFluidOutputs(GTUtility.getTierByVoltage(recipe.getEUt()),
                    getOverclockTier(), recipeMap);
            assert !recipe.getFluidInputs().isEmpty();
            int inputAmount = recipe.getFluidInputs().get(0).getAmount();
            for (FluidStack stack : fluidOutputs) {
                stack.amount = stack.amount * totalFluidConsumed / inputAmount;
            }
        }
        this.itemOutputs = Collections.emptyList();
    }

    /**
     * Set the outputs to empty
     */
    protected void setEmptyOutputs() {
        this.itemOutputs = Collections.emptyList();
        this.fluidOutputs = Collections.emptyList();
    }

    /**
     * @param rotorFit the fitting of the rotor
     * @param values   an array of [optimalFlow, baseEfficiency] to adjust
     */
    protected abstract void applyRotorFitting(@NotNull RotorFit rotorFit, @NotNull FittingAdjustmentValues values);

    /**
     * @return the multiplier for fuel overflow
     */
    protected abstract float overflowMultiplier();

    /**
     * @return the floor value for remaining flow
     */
    protected abstract float flowFloor();

    /**
     * Changes the energy efficiency when the fuel consumed is not optimal
     * <p>
     *
     * @param overflowEfficiency the efficiency for overflow
     * @return the efficiency of the overflow on [0, 1]
     */
    protected abstract float unoptimalEfficiency(int overflowEfficiency);

    /**
     * @param energyDensity the EU per mB of the base recipe
     * @return the output EU/t
     */
    protected abstract long calculateOutputEUt(long energyDensity);

    /**
     * @return the amount of damage to apply to the rotor
     */
    protected abstract int getRotorDamage(@NotNull RotorFit rotorFitting);

    @Override
    protected void setActive(boolean active) {
        if (this.isActive != active) {
            World world = metaTileEntity.getWorld();
            if (world != null && !world.isRemote) {
                for (RotorHolder holder : turbine.getAbilities(MultiblockAbility.ROTOR_HOLDER_2)) {
                    holder.setSpinning(active);
                }
            }
        }
        super.setActive(active);
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        if (this.workingEnabled != workingEnabled) {
            World world = metaTileEntity.getWorld();
            if (world != null && !world.isRemote) {
                for (RotorHolder holder : turbine.getAbilities(MultiblockAbility.ROTOR_HOLDER_2)) {
                    holder.setSpinning(workingEnabled);
                }
            }
        }

        // pausing stops the rotor, so energy generation should reset
        if (!workingEnabled) {
            this.lastEUt = 0;
            this.recipeEUt = 0;
        }
        super.setWorkingEnabled(workingEnabled);
    }

    @Override
    public @NotNull NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setInteger("optimalFuelRate", optimalFuelRate);
        tag.setInteger("totalFluidConsumed", totalFluidConsumed);
        tag.setLong("lastEUt", lastEUt);
        if (storedFluid != null) {
            tag.setTag("storedFluid", storedFluid.writeToNBT(new NBTTagCompound()));
        }

        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.optimalFuelRate = compound.getInteger("optimalFuelRate");
        this.totalFluidConsumed = compound.getInteger("totalFluidConsumed");
        this.lastEUt = compound.getLong("lastEUt");
        this.storedFluid = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("storedFluid"));
    }

    protected static class FittingAdjustmentValues {

        public long optimalEnergyFlow;
        public int baseEfficiency;
    }
}
