package gregtech.api.recipes.logic;

import gregtech.api.capability.MultipleTankHandler;
import gregtech.api.metatileentity.IVoidable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IParallelableRecipeLogic {

    /**
     * Method which applies bonuses or penalties to the recipe based on the parallelization factor,
     * such as EU consumption or processing speed.
     *
     * @param builder the recipe builder
     */
    default void applyParallelBonus(@NotNull RecipeBuilder<?> builder) {}

    /**
     * Method which finds a recipe which can be parallelized, works by multiplying the recipe by the parallelization
     * factor,
     * and shrinking the recipe till its outputs can fit
     *
     * @param recipeMap     the recipe map
     * @param currentRecipe recipe to be parallelized
     * @param inputs        input item handler
     * @param fluidInputs   input fluid handler
     * @param outputs       output item handler
     * @param fluidOutputs  output fluid handler
     * @param parallelLimit the maximum number of parallel recipes to be performed
     * @param maxVoltage    the voltage limit on the number of parallel recipes to be performed
     * @param voidable      the voidable performing the parallel recipe
     * @return the recipe builder with the parallelized recipe. returns null the recipe can't fit
     */
    default RecipeBuilder<?> findMultipliedParallelRecipe(@NotNull RecipeMap<?> recipeMap,
                                                          @NotNull Recipe currentRecipe,
                                                          @NotNull IItemHandlerModifiable inputs,
                                                          @NotNull MultipleTankHandler fluidInputs,
                                                          @NotNull IItemHandlerModifiable outputs,
                                                          @NotNull MultipleTankHandler fluidOutputs, int parallelLimit,
                                                          long maxVoltage, @NotNull IVoidable voidable) {
        return ParallelLogic.doParallelRecipes(
                currentRecipe,
                recipeMap,
                inputs,
                fluidInputs,
                outputs,
                fluidOutputs,
                parallelLimit,
                maxVoltage,
                voidable);
    }

    /**
     * Method which finds a recipe then multiplies it, then appends it to the builds up to the parallelization factor,
     * or filling the output
     *
     * @param recipeMap     the recipe map
     * @param inputs        input item handler
     * @param outputs       output item handler
     * @param parallelLimit the maximum number of parallel recipes to be performed
     * @param maxVoltage    the voltage limit on the number of parallel recipes to be performed
     * @param voidable      the voidable performing the parallel recipe
     * @return the recipe builder with the parallelized recipe. returns null the recipe can't fit
     */
    default RecipeBuilder<?> findAppendedParallelItemRecipe(@NotNull RecipeMap<?> recipeMap,
                                                            @NotNull IItemHandlerModifiable inputs,
                                                            @NotNull IItemHandlerModifiable outputs, int parallelLimit,
                                                            long maxVoltage, @NotNull IVoidable voidable) {
        return ParallelLogic.appendItemRecipes(
                recipeMap,
                inputs,
                outputs,
                parallelLimit,
                maxVoltage,
                voidable);
    }

    // Recipes passed in here should be already trimmed, if desired
    default Recipe findParallelRecipe(@NotNull Recipe currentRecipe, @NotNull IItemHandlerModifiable inputs,
                                      @NotNull MultipleTankHandler fluidInputs,
                                      @NotNull IItemHandlerModifiable outputs,
                                      @NotNull MultipleTankHandler fluidOutputs, long maxVoltage, int parallelLimit) {
        if (parallelLimit > 1 && getRecipeMap() != null) {
            RecipeBuilder<?> parallelBuilder = switch (getParallelLogicType()) {
                case MULTIPLY -> findMultipliedParallelRecipe(getRecipeMap(), currentRecipe, inputs, fluidInputs,
                        outputs, fluidOutputs, parallelLimit, maxVoltage, getMetaTileEntity());
                case APPEND_ITEMS -> findAppendedParallelItemRecipe(getRecipeMap(), inputs, outputs, parallelLimit,
                        maxVoltage, getMetaTileEntity());
            };

            // if the builder returned is null, no recipe was found.
            if (parallelBuilder == null) {
                invalidateInputs();
                return null;
            } else {
                // if the builder returned does not parallel, its outputs are full
                if (parallelBuilder.getParallel() == 0) {
                    invalidateOutputs();
                    return null;
                } else {
                    setParallelRecipesPerformed(parallelBuilder.getParallel());
                    // apply any parallel bonus
                    applyParallelBonus(parallelBuilder);
                    return parallelBuilder.build().getResult();
                }
            }
        }
        return currentRecipe;
    }

    @NotNull
    MetaTileEntity getMetaTileEntity();

    @Nullable
    RecipeMap<?> getRecipeMap();

    @NotNull
    ParallelLogicType getParallelLogicType();

    /**
     * Set the amount of parallel recipes currently being performed
     *
     * @param amount the amount to set
     */
    void setParallelRecipesPerformed(int amount);

    /**
     * Invalidate the current state of input inventory contents
     */
    void invalidateInputs();

    /**
     * Invalidate the current state of output inventory contents
     */
    void invalidateOutputs();
}
