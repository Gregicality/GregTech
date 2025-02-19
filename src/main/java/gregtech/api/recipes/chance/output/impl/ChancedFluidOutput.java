package gregtech.api.recipes.chance.output.impl;

import gregtech.api.recipes.chance.output.BoostableChanceOutput;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

/**
 * Implementation for a chanced fluid output
 */
public class ChancedFluidOutput extends BoostableChanceOutput<FluidStack> {

    public ChancedFluidOutput(@NotNull FluidStack ingredient, int chance, int chanceBoost) {
        super(ingredient, chance, chanceBoost);
    }

    public ChancedFluidOutput(@NotNull FluidStack ingredient, int chance, int maxChance, int chanceBoost) {
        super(ingredient, chance, maxChance, chanceBoost);
    }

    @Override
    public @NotNull ChancedFluidOutput copy() {
        return new ChancedFluidOutput(getIngredient().copy(), getChance(), getMaxChance(), getChanceBoost());
    }

    @Override
    public String toString() {
        return "ChancedFluidOutput{" +
                "ingredient=FluidStack{" + getIngredient().getUnlocalizedName() +
                ", amount=" + getIngredient().amount +
                "}, chance=" + getChance() +
                ", chanceBoost=" + getChanceBoost() +
                '}';
    }
}
