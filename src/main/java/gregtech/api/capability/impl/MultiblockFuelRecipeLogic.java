package gregtech.api.capability.impl;

import gregtech.api.capability.IRotorHolder;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;

import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MultiblockFuelRecipeLogic extends MultiblockRecipeLogic {

    protected long totalContinuousRunningTime;
    private int previousDuration = 0;

    public MultiblockFuelRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity);
    }

    @Override
    protected void modifyOverclockPre(@NotNull OCParams ocParams, @NotNull RecipePropertyStorage storage) {
        // apply maintenance bonuses
        Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

        // duration bonus
        if (maintenanceValues.getSecond() != 1.0) {
            ocParams.setDuration((int) Math.round(ocParams.duration() / maintenanceValues.getSecond()));
        }
    }

    @Override
    protected void modifyOverclockPost(@NotNull OCResult ocResult, @NotNull RecipePropertyStorage storage) {
        // apply maintenance penalties
        Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

        // duration penalty
        if (maintenanceValues.getFirst() > 0) {
            ocResult.setDuration((int) (ocResult.duration() * (1 - 0.1 * maintenanceValues.getFirst())));
        }
    }

    @NotNull
    @Override
    public ParallelLogicType getParallelLogicType() {
        return ParallelLogicType.MULTIPLY; // TODO APPEND_FLUIDS
    }

    @Override
    protected boolean hasEnoughPower(long eut, int duration) {
        // generators always have enough power to run recipes
        return true;
    }

    @Override
    public void update() {
        super.update();
        if (workingEnabled && isActive && progressTime > 0) {
            totalContinuousRunningTime++;
        } else {
            totalContinuousRunningTime = 0;
        }
    }

    @Override
    public int getParallelLimit() {
        // parallel is limited by voltage
        return Integer.MAX_VALUE;
    }

    @Override
    protected long getMaxParallelVoltage() {
        return getMaxVoltage();
    }

    /**
     * Boost the energy production.
     * Should not change the state of the workable logic. Only read current values.
     *
     * @param production the energy amount to boost
     * @return the boosted energy amount
     */
    protected long boostProduction(long production) {
        return production;
    }

    @Override
    protected void setupRecipe(@NotNull Recipe recipe) {
        super.setupRecipe(recipe);
        this.recipeEUt = boostProduction(this.recipeEUt);
    }

    @Override
    public boolean consumesEnergy() {
        return false;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        totalContinuousRunningTime = 0;
    }

    public String getRecipeFluidInputInfo() {
        IRotorHolder rotorHolder = null;

        if (metaTileEntity instanceof MultiblockWithDisplayBase multiblockWithDisplayBase) {
            List<IRotorHolder> abilities = multiblockWithDisplayBase.getAbilities(MultiblockAbility.ROTOR_HOLDER);
            rotorHolder = abilities.size() > 0 ? abilities.get(0) : null;
        }

        // Previous Recipe is always null on first world load, so try to acquire a new recipe
        Recipe recipe;
        if (previousRecipe == null) {
            recipe = findRecipe(Integer.MAX_VALUE, getInputInventory(), getInputTank());
            if (recipe == null) return null;
        } else {
            recipe = previousRecipe;
        }
        previousDuration = recipe.getDuration();
        FluidStack requiredFluidInput = recipe.getFluidInputs().get(0).getInputFluidStack();

        int ocAmount = GTUtility.safeCastLongToInt(getMaxVoltage() / recipe.getEUt());
        int neededAmount = ocAmount * requiredFluidInput.amount;
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            neededAmount /= (int) (rotorHolder.getTotalEfficiency() / 100.0);
        } else if (rotorHolder != null && !rotorHolder.hasRotor()) {
            return null;
        }
        return TextFormatting.RED + TextFormattingUtil.formatNumbers(neededAmount) + "L";
    }

    @Override
    public int getPreviousRecipeDuration() {
        return previousDuration;
    }

    public FluidStack getInputFluidStack() {
        // Previous Recipe is always null on first world load, so try to acquire a new recipe
        if (previousRecipe == null) {
            Recipe recipe = findRecipe(Integer.MAX_VALUE, getInputInventory(), getInputTank());

            return recipe == null ? null : getInputTank().drain(
                    new FluidStack(recipe.getFluidInputs().get(0).getInputFluidStack().getFluid(), Integer.MAX_VALUE),
                    false);
        }
        FluidStack fuelStack = previousRecipe.getFluidInputs().get(0).getInputFluidStack();
        return getInputTank().drain(new FluidStack(fuelStack.getFluid(), Integer.MAX_VALUE), false);
    }

    @Override
    public boolean isAllowOverclocking() {
        return false;
    }
}
