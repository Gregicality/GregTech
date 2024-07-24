package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;

import org.jetbrains.annotations.NotNull;

import static gregtech.api.recipes.logic.OverclockingLogic.standardOverclockingLogic;

/**
 * Recipe Logic for a Multiblock that does not require power.
 */
public class PrimitiveRecipeLogic extends MultiblockRecipeLogic {

    public PrimitiveRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity);
    }

    @Override
    protected long getEnergyInputPerSecond() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected long getEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected long getEnergyCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        return true; // spoof energy being drawn
    }

    @Override
    protected boolean hasEnoughPower(int @NotNull [] resultOverclock) {
        return true;
    }

    @Override
    public long getMaxVoltage() {
        return GTValues.LV;
    }

    @Override
    protected int @NotNull [] runOverclockingLogic(@NotNull IRecipePropertyStorage propertyStorage, int recipeEUt,
                                                   long maxVoltage, int recipeDuration, int amountOC) {
        return standardOverclockingLogic(
                1,
                getMaxVoltage(),
                recipeDuration,
                amountOC,
                getOverclockingDurationDivisor(),
                getOverclockingVoltageMultiplier()

        );
    }

    @Override
    public long getMaximumOverclockVoltage() {
        return GTValues.V[GTValues.LV];
    }

    /**
     * Used to reset cached values in the Recipe Logic on structure deform
     */
    public void invalidate() {
        previousRecipe = null;
        progressTime = 0;
        maxProgressTime = 0;
        recipeEUt = 0;
        fluidOutputs = null;
        itemOutputs = null;
        setActive(false); // this marks dirty for us
    }
}
