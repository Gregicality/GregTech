package gregtech.common.metatileentities.multi.electric.generator.turbine;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;

import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class PlasmaTurbineLogic extends LargeTurbineRecipeLogic {

    public PlasmaTurbineLogic(AbstractLargeTurbine tileEntity) {
        super(tileEntity);
    }

    @Override
    protected long calculateOutputEUt(long totalRecipeEU) {
        return totalFluidConsumed * totalRecipeEU / 20;
    }

    @Override
    protected int getRotorDamage(@NotNull RotorFit rotorFitting) {
        return 1;
    }

    @Override
    protected boolean isFuelValid(@NotNull Fluid fuel) {
        return true;
    }

    @Override
    protected void setRecipeOutputs(@NotNull Recipe recipe) {
        RecipeMap<?> recipeMap = getRecipeMap();
        assert recipeMap != null;
        if (recipe.getFluidOutputs().isEmpty() && recipe.getChancedFluidOutputs().getChancedEntries().isEmpty()) {
            this.fluidOutputs = Collections.emptyList();
        } else {
            this.fluidOutputs = recipe.getResultFluidOutputs(GTUtility.getTierByVoltage(recipe.getEUt()),
                    getOverclockTier(), recipeMap);
            for (FluidStack stack : fluidOutputs) {
                stack.amount = totalFluidConsumed; //TODO figure out what to do about this
            }
        }
        this.itemOutputs = NonNullList.create();
    }

    @Override
    protected void adjustPower(@NotNull RotorFit rotorFit, int @NotNull [] values) {
        values[0] *= 40;
    }

    @Override
    protected float optimalFlowMultiplier() {
        return 1.5F;
    }

    @Override
    protected float flowFloor() {
        return 1;
    }

    @Override
    protected float overflowEfficiency(int totalFluidConsumed, int currentOptimalFlow, int overflowMultiplier) {
        int delta = Math.abs(totalFluidConsumed - currentOptimalFlow);
        float divisor = currentOptimalFlow;
        if (totalFluidConsumed > currentOptimalFlow) {
            divisor *= overflowMultiplier * 3 + 1;
        }

        return 1.0F - delta / divisor;
    }
}
