package gregtech.api.recipes;

import gregtech.api.recipes.map.AbstractMapIngredient;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class RecipeIterator implements Iterator<Recipe> {

    int index;
    List<List<AbstractMapIngredient>> ingredients;
    @NotNull
    RecipeMap<?> recipeMap;
    @NotNull
    Predicate<Recipe> canHandle;
    Recipe cachedNext;

    RecipeIterator(@NotNull RecipeMap<?> recipeMap, List<List<AbstractMapIngredient>> ingredients,
                   @NotNull Predicate<Recipe> canHandle) {
        this.ingredients = ingredients;
        this.recipeMap = recipeMap;
        this.canHandle = canHandle;
    }

    @Override
    public boolean hasNext() {
        if (cachedNext != null) return true;
        if (ingredients == null || this.index > this.ingredients.size()) return false;

        while (index < ingredients.size()) {
            Recipe r = recipeMap.recurseIngredientTreeFindRecipe(ingredients, recipeMap.getLookup(), canHandle, index,
                    0,
                    (1L << index));
            ++index;
            if (r != null) {
                cachedNext = r;
                return true;
            }
        }
        return false;
    }

    @Override
    public Recipe next() {
        if (cachedNext != null) {
            Recipe r = cachedNext.copy();
            cachedNext = null;
            return r;
        }
        // couldn't build any inputs to use for search, so no recipe could be found
        if (ingredients == null) return null;
        // Try each ingredient as a starting point, save current index
        Recipe r = null;
        while (index < ingredients.size()) {
            r = recipeMap.recurseIngredientTreeFindRecipe(ingredients, recipeMap.getLookup(), canHandle, index, 0,
                    (1L << index));
            ++index;
            if (r != null) break;
        }
        return r;
    }
}
