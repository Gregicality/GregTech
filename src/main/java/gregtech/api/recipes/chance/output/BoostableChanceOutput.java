package gregtech.api.recipes.chance.output;

import gregtech.api.recipes.chance.boost.BoostableChanceEntry;

import org.jetbrains.annotations.NotNull;

/**
 * A chanced output which can be boosted
 *
 * @param <T> the type of ingredient contained by the output
 */
public abstract class BoostableChanceOutput<T> extends ChancedOutput<T> implements BoostableChanceEntry<T> {

    private final int chanceBoost;

    public BoostableChanceOutput(@NotNull T ingredient, int chance, int chanceBoost) {
        this(ingredient, chance, ChancedOutputLogic.getMaxChancedValue(), chanceBoost);
    }

    public BoostableChanceOutput(@NotNull T ingredient, int chance, int maxChance, int chanceBoost) {
        super(ingredient, chance, maxChance);
        this.chanceBoost = fixBoost(chanceBoost);
    }

    @Override
    public int getChanceBoost() {
        return this.chanceBoost;
    }

    /**
     * Attempts to fix and round the given chance boost due to potential differences
     * between the max chance and {@link ChancedOutputLogic#getMaxChancedValue()}.
     * <br />
     * The worst case would be {@code 5,001 / 10,000} , meaning the boost would
     * have to be halved to have the intended effect.
     * 
     * @param chanceBoost the chance boost to be fixed
     * @return the fixed chance boost
     */
    private int fixBoost(int chanceBoost) {
        float error = (float) ChancedOutputLogic.getMaxChancedValue() / getMaxChance();
        return Math.round(chanceBoost / error);
    }

    @Override
    public String toString() {
        return "BoostableChanceOutput{" +
                "ingredient=" + getIngredient() +
                ", chance=" + getChance() +
                ", maxChance=" + getMaxChance() +
                ", chanceBoost=" + getChanceBoost() +
                '}';
    }
}
