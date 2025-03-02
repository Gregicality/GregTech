package gregtech.api.recipes.chance.output;

import gregtech.api.recipes.chance.ChanceEntry;

import org.jetbrains.annotations.NotNull;

/**
 * And output which has a chance to be produced
 *
 * @param <T> the type of ingredient contained by the output
 */
public abstract class ChancedOutput<T> implements ChanceEntry<T> {

    private final T ingredient;
    private final int chance;
    private final int maxChance;

    public ChancedOutput(@NotNull T ingredient, int chance) {
        this(ingredient, chance, ChancedOutputLogic.getMaxChancedValue());
    }

    public ChancedOutput(@NotNull T ingredient, int chance, int maxChance) {
        this.ingredient = ingredient;
        this.chance = chance;
        this.maxChance = maxChance;
    }

    @Override
    public @NotNull T getIngredient() {
        return ingredient;
    }

    @Override
    public int getChance() {
        return chance;
    }

    @Override
    public int getMaxChance() {
        return maxChance;
    }
}
