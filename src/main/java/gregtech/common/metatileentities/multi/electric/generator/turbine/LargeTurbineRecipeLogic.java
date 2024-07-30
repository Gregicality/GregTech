package gregtech.common.metatileentities.multi.electric.generator.turbine;

import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.RotorHolder;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.items.metaitem.stats.TurbineRotor;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.GTUtility;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public abstract class LargeTurbineRecipeLogic extends MultiblockFuelRecipeLogic {

    protected final AbstractLargeTurbine turbine;

    protected int currentOptimalFlow;
    protected int storedFluid;
    protected int totalFluidConsumed;
    protected long lastEUt;

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
    protected void trySearchNewRecipe() {
        if (!checkMaintenance()) {
            return;
        }

        RotorHolder rotorHolder = turbine.getAbilities(MultiblockAbility.ROTOR_HOLDER_2).get(0);
        assert rotorHolder != null;
        TurbineRotor rotor = rotorHolder.rotor();
        if (rotor == null) {
            invalidate();
            this.currentOptimalFlow = 0;
            this.totalFluidConsumed = 0;
            this.lastEUt = 0;
            return;
        }

        int baseEfficiency = rotor.baseEfficiency();
        if (baseEfficiency <= 0) {
            return;
        }

        int optimalFlow = rotor.optimalFlow();
        if (optimalFlow <= 0) {
            return;
        }

        long recipePower;
        Recipe recipe = findRecipe(GTValues.V[GTValues.MAX], new ItemStackHandler(0), getInputTank());
        if (recipe == null) {
            recipePower = 0;
            itemOutputs = NonNullList.create();
            fluidOutputs = Collections.emptyList();
        } else {
            float flowMultiplier = rotor.flowMultiplier(turbine.turbineType());
            if (flowMultiplier == 0.0F) {
                return;
            }

            recipePower = getRecipePower(recipe, rotorHolder.rotorFitting(), optimalFlow, baseEfficiency, rotor.overflowMultiplier(), flowMultiplier);
        }

        long eut = computePowerOutput(recipePower);

        if (eut <= 0) {
            rotorHolder.setSpinning(false);
            return;
        }

        if (turbine.getOffsetTimer() % 20 == 0) {
            rotorHolder.damageRotor(getRotorDamage(rotorHolder.rotorFitting()));
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

    protected long getRecipePower(@NotNull Recipe recipe, @NotNull RotorFit rotorFit, int optimalFlow,
                                  int baseEfficiency, int overflowMultiplier, float flowMultiplier) {
        var inputs = recipe.getFluidInputs();
        if (inputs.isEmpty()) {
            return 0;
        }

        FluidStack fuel = inputs.get(0).getInputFluidStack();
        assert fuel.amount != 0;
        if (fuel.getFluid() == null || !isFuelValid(fuel.getFluid())) {
            return 0;
        }

        int[] values = {optimalFlow, baseEfficiency};
        adjustPower(rotorFit, values);
        optimalFlow = values[0];
        baseEfficiency = values[1];

        IMultipleTankHandler inputTank = getInputTank();

        long recipeEU;
        if (usesRecipeEnergy()) {
            recipeEU = recipe.getDuration() * recipe.getEUt() / fuel.amount;
            if (optimalFlow < recipeEU) {
                // turbine is too weak and/or fuel is too energy dense so will consume 1mB
                this.currentOptimalFlow = 1;

                // consume the fuel and output the optimal flow
                FluidStack drained = inputTank.drain(new FluidStack(fuel, 1), true);
                if (drained == null) {
                    return 0;
                }

                this.storedFluid += drained.amount;
                return optimalFlow;
            }
        } else {
            recipeEU = 1;
        }

        this.currentOptimalFlow = GTUtility.safeCastLongToInt((long) (optimalFlow * flowMultiplier / recipeEU));

        // Allowed to use beyond the base optimal flow rate, depending on the value of overflowMultiplier.
        // The maximum EU/t possible depends on the overflowMultiplier, and the formula used makes the flow rate for
        // that maximum per value of overflowMultiplier into a percentage of optimal flow rate
        int remainingFlow = GTUtility.safeCastLongToInt((long) (currentOptimalFlow * (optimalFlowMultiplier() * overflowMultiplier + flowFloor())));
        this.totalFluidConsumed = 0;
        this.storedFluid = 0;

        if (inputTank.getTanks() == 0) {
            return 0;
        }

        if (!consumeRecipeInputs(fuel, remainingFlow)) {
            return 0;
        }

        assert totalFluidConsumed > 0;
        setRecipeOutputs(recipe);
        long totalEU = calculateOutputEUt(recipeEU);
        if (totalFluidConsumed != currentOptimalFlow) {
            totalEU = (long) (totalEU * overflowEfficiency(totalFluidConsumed, currentOptimalFlow, overflowMultiplier));
        }

        totalEU = Math.max(1, totalEU * baseEfficiency / 10_000);

        return Math.min(totalEU, getMaxVoltage());
    }

    /**
     * @return if energy generated by the recipe should be considered
     */
    protected boolean usesRecipeEnergy() {
        return true;
    }

    private long computePowerOutput(long recipePower) {
        long delta = recipePower - this.lastEUt;
        // how much power can change by each tick: max(10, 1% total power)
        long maxDelta = Math.max(10L, Math.abs(delta) / 100L);

        long eut;
        if (Math.abs(delta) > maxDelta) {
            eut = this.lastEUt + (maxDelta * (delta > 0 ? 1 : -1));
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
     * @param fuel          the fuel to consume
     * @param remainingFlow the remaining flow to consume
     * @return              if consumption was successful
     */
    protected boolean consumeRecipeInputs(@NotNull FluidStack fuel, int remainingFlow) {
        var inputTank = getInputTank();
        FluidStack drainStack = new FluidStack(fuel, 1); //shared fluid stack object to reduce allocations
        for (IMultipleTankHandler.MultiFluidTankEntry handler : inputTank) {
            FluidStack stack = handler.getFluid();
            if (stack == null || !fuel.isFluidEqual(stack)) {
                continue;
            }

            // consume up to the maximum amount of flow
            int flow = Math.min(stack.amount, remainingFlow);
            drainStack.amount = flow;
            FluidStack drained = inputTank.drain(drainStack, true);
            if (drained == null) {
                continue;
            }
            this.storedFluid += drained.amount;

            // remaining flow yet to be consumed
            remainingFlow -= flow;

            // total flow consumed
            totalFluidConsumed += flow;
        }
        return true;
    }

    /**
     * @param recipe the recipe to use
     */
    protected abstract void setRecipeOutputs(@NotNull Recipe recipe);

    /**
     * @param rotorFit the fitting of the rotor
     * @param values   an array of [optimalFlow, baseEfficiency] to adjust
     */
    protected abstract void adjustPower(@NotNull RotorFit rotorFit, int @NotNull [] values);

    /**
     * @return the multiplier for rotor optimal flow
     */
    protected abstract float optimalFlowMultiplier();

    /**
     * @return the floor value for remaining flow
     */
    protected abstract float flowFloor();

    /**
     * Changes how quickly the turbine loses efficiency after flow goes beyond the optimal value
     * At the default value of 1, any flow will generate less EU/t than optimal flow, regardless of the amount of fuel used
     * The bigger this number is, the slower efficiency loss happens as flow moves beyond the optimal value
     *
     * @param totalFluidConsumed the total amount of flow
     * @param currentOptimalFlow the currently used optimal flow
     * @param overflowMultiplier the multiplier for overflow
     * @return the efficiency of the overflow on [0, 1]
     */
    protected abstract float overflowEfficiency(int totalFluidConsumed, int currentOptimalFlow, int overflowMultiplier);

    /**
     * @param totalRecipeEU the total EU the base recipe is worth
     * @return the output EU/t
     */
    protected abstract long calculateOutputEUt(long totalRecipeEU);

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
        super.setWorkingEnabled(workingEnabled);
    }

    @Override
    public @NotNull NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setInteger("currentOptimalFlow", currentOptimalFlow);
        tag.setInteger("storedFluid", storedFluid);
        tag.setInteger("totalFluidConsumed", totalFluidConsumed);
        tag.setLong("lastEUt", lastEUt);
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        currentOptimalFlow = compound.getInteger("currentOptimalFlow");
        storedFluid = compound.getInteger("storedFluid");
        totalFluidConsumed = compound.getInteger("totalFluidConsumed");
        lastEUt = compound.getLong("lastEUt");
    }
}
