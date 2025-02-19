package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;

import org.jetbrains.annotations.NotNull;

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
    protected boolean drawEnergy(long recipeEUt, boolean simulate) {
        return true; // spoof energy being drawn
    }

    @Override
    protected boolean hasEnoughPower(long eut, int duration) {
        return true;
    }

    @Override
    public long getMaxVoltage() {
        return GTValues.LV;
    }

    @Override
    protected void runOverclockingLogic(@NotNull OCParams ocParams, @NotNull OCResult ocResult,
                                        @NotNull RecipePropertyStorage propertyStorage, long maxVoltage) {
        ocParams.setEut(1L);
        super.runOverclockingLogic(ocParams, ocResult, propertyStorage, maxVoltage);
    }

    @Override
    public long getMaximumOverclockVoltage() {
        return GTValues.V[GTValues.LV];
    }
}
