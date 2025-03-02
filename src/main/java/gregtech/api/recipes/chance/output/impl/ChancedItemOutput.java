package gregtech.api.recipes.chance.output.impl;

import gregtech.api.recipes.chance.output.BoostableChanceOutput;
import gregtech.api.util.GTStringUtils;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

/**
 * Implementation for a chanced item output
 */
public class ChancedItemOutput extends BoostableChanceOutput<ItemStack> {

    public ChancedItemOutput(@NotNull ItemStack ingredient, int chance, int chanceBoost) {
        super(ingredient, chance, chanceBoost);
    }

    public ChancedItemOutput(@NotNull ItemStack ingredient, int chance, int maxChance, int chanceBoost) {
        super(ingredient, chance, maxChance, chanceBoost);
    }

    @Override
    public @NotNull ChancedItemOutput copy() {
        return new ChancedItemOutput(getIngredient().copy(), getChance(), getMaxChance(), getChanceBoost());
    }

    @Override
    public String toString() {
        return "ChancedItemOutput{" +
                "ingredient=" + GTStringUtils.prettyPrintItemStack(getIngredient()) +
                ", chance=" + getChance() +
                ", chanceBoost=" + getChanceBoost() +
                '}';
    }
}
