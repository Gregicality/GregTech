package gregtech.api.recipes.chance;

import org.jetbrains.annotations.NotNull;

/**
 * An entry for an ingredient and associated chance of operation.
 *
 * @param <T> the type of ingredient contained by the chanced entry
 */
public interface ChanceEntry<T> {

    /**
     * @return the ingredient
     */
    @NotNull
    T getIngredient();

    /**
     * @return the chance of operating with the ingredient
     */
    int getChance();

    int getMaxChance();

    /**
     * @return a copy of the chance entry
     */
    @NotNull
    ChanceEntry<T> copy();
}
