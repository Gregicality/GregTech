package gregtech.api.recipes.logic;

import gregtech.api.capability.MultipleTankHandler;
import gregtech.api.metatileentity.IVoidable;
import gregtech.api.recipes.FluidKey;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.GTHashMaps;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.api.util.OverlayedItemHandler;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ParallelLogic {

    /**
     * @param recipe         The recipe
     * @param inputs         The item inputs
     * @param fluidInputs    the fluid inputs
     * @param parallelAmount hard cap on the amount returned
     * @return returns the amount of possible time a recipe can be made from a given input inventory
     */

    public static int getMaxRecipeMultiplier(@NotNull Recipe recipe, @NotNull IItemHandlerModifiable inputs,
                                             @NotNull MultipleTankHandler fluidInputs, int parallelAmount) {
        // Find all the items in the combined Item Input inventories and create oversized ItemStacks
        Object2IntMap<ItemStack> ingredientStacks = GTHashMaps.fromItemHandler(inputs);

        // Find all the fluids in the combined Fluid Input inventories and create oversized FluidStacks
        Map<FluidKey, Integer> fluidStacks = GTHashMaps.fromFluidHandler(fluidInputs);

        // Find the maximum number of recipes that can be performed from the items in the item input inventories
        int itemMultiplier = getMaxRatioItem(ingredientStacks, recipe, parallelAmount);
        // Find the maximum number of recipes that can be performed from the fluids in the fluid input inventories
        int fluidMultiplier = getMaxRatioFluid(fluidStacks, recipe, parallelAmount);

        if (itemMultiplier == Integer.MAX_VALUE && fluidMultiplier == Integer.MAX_VALUE) {
            return 0;
        }

        // Find the maximum number of recipes that can be performed from all available inputs
        return Math.min(itemMultiplier, fluidMultiplier);
    }

    /**
     * @param recipe         The recipe
     * @param outputs        the item output inventory
     * @param fluidOutputs   the fluid output tanks
     * @param parallelAmount the maximum expected amount
     * @param voidItems      If the result of the item parallel limiting should be ignored
     * @param voidFluids     If the result of the fluid parallel limiting should be ignored
     * @return returns the amount of recipes that can be merged successfully into a given output inventory
     */
    public static int limitByOutputMerging(@NotNull Recipe recipe, @NotNull IItemHandlerModifiable outputs,
                                           @NotNull MultipleTankHandler fluidOutputs, int parallelAmount,
                                           boolean voidItems, boolean voidFluids) {
        int modifiedItemParallelAmount = Integer.MAX_VALUE;
        int modifiedFluidParallelAmount = Integer.MAX_VALUE;

        // If we are voiding both items and fluids, return the maximum number of parallels that can be performed from
        // the inputs
        if (voidItems && voidFluids) {
            return parallelAmount;
        }

        // Check both normal item outputs and chanced item outputs
        if (!recipe.getOutputs().isEmpty() || !recipe.getChancedOutputs().getChancedEntries().isEmpty()) {
            // If we are voiding items, reset the item limit to the maximum number of parallels
            if (voidItems) {
                modifiedItemParallelAmount = parallelAmount;
            } else {
                modifiedItemParallelAmount = limitParallelByItems(recipe, new OverlayedItemHandler(outputs),
                        parallelAmount);
            }

            // If we are not voiding, and cannot fit any items, return 0
            if (modifiedItemParallelAmount == 0 && !voidItems) {
                return 0;
            }
        }

        if (!recipe.getFluidOutputs().isEmpty() || !recipe.getChancedFluidOutputs().getChancedEntries().isEmpty()) {
            // If we are voiding fluids, reset the fluid limit to the maximum number of parallels
            if (voidFluids) {
                modifiedFluidParallelAmount = parallelAmount;
            } else {
                modifiedFluidParallelAmount = limitParallelByFluids(recipe, fluidOutputs,
                        modifiedItemParallelAmount);
            }

            // If we are not voiding, and cannot fit any fluids, return 0
            if (modifiedFluidParallelAmount == 0 && !voidFluids) {
                return 0;
            }
        }

        return Math.min(modifiedFluidParallelAmount, modifiedItemParallelAmount);
    }

    /**
     * @param recipe     the recipe from which we get the input to product ratio
     * @param multiplier the maximum possible multiplied we can get from the input inventory
     *                   see
     *                   {@link ParallelLogic#getMaxRecipeMultiplier(Recipe, IItemHandlerModifiable, MultipleTankHandler, int)}
     * @return the amount of times a {@link Recipe} outputs can be merged into an inventory without
     *         voiding products.
     */
    public static int limitParallelByItems(@NotNull Recipe recipe, @NotNull OverlayedItemHandler overlayedItemHandler,
                                           int multiplier) {
        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        Object2IntMap<ItemStack> recipeOutputs = GTHashMaps.fromItemStackCollection(recipe.getAllItemOutputs());

        while (minMultiplier != maxMultiplier) {
            overlayedItemHandler.reset();

            int returnedAmount = 0;
            int amountToInsert;

            for (Object2IntMap.Entry<ItemStack> entry : recipeOutputs.object2IntEntrySet()) {
                // Since multiplier starts at Int.MAX, check here for integer overflow
                if (entry.getIntValue() != 0 && multiplier > Integer.MAX_VALUE / entry.getIntValue()) {
                    amountToInsert = Integer.MAX_VALUE;
                } else {
                    amountToInsert = entry.getIntValue() * multiplier;
                }
                returnedAmount = overlayedItemHandler.insertStackedItemStack(entry.getKey(), amountToInsert);
                if (returnedAmount > 0) {
                    break;
                }
            }

            int[] bin = adjustMultiplier(returnedAmount == 0, minMultiplier, multiplier, maxMultiplier);
            minMultiplier = bin[0];
            multiplier = bin[1];
            maxMultiplier = bin[2];

        }
        return multiplier;
    }

    /**
     * Used by the Multi Smelter and some parallellizable steam multiblocks
     *
     * @param recipeOutputList the recipe outputs from the recipe we are building up to its maximum parallel limit
     * @param outputsToAppend  the recipe outputs from the recipe we want to append to the recipe we are building
     * @param multiplier       the maximum possible multiplied we can get from the input inventory
     *                         see
     *                         {@link ParallelLogic#getMaxRecipeMultiplier(Recipe, IItemHandlerModifiable, MultipleTankHandler, int)}
     * @return the amount of times a {@link Recipe} outputs can be merged into an inventory without
     *         voiding products.
     */
    public static int limitParallelByItemsIncremental(@NotNull List<ItemStack> recipeOutputList,
                                                      @NotNull List<ItemStack> outputsToAppend,
                                                      @NotNull OverlayedItemHandler overlayedItemHandler,
                                                      final int multiplier) {
        int minMultiplier = 0;
        int currentMultiplier = multiplier;
        int maxMultiplier = multiplier;
        int previousMultiplier = multiplier;

        Object2IntMap<ItemStack> recipeOutputs = GTHashMaps.fromItemStackCollection(recipeOutputList);
        Object2IntMap<ItemStack> recipeOutputsToAppend = GTHashMaps.fromItemStackCollection(outputsToAppend);

        Object2IntMap<ItemStack> appendedResultMap = new Object2IntLinkedOpenCustomHashMap<>(recipeOutputs,
                ItemStackHashStrategy.comparingAllButCount());
        recipeOutputsToAppend
                .forEach((stackKey, amt) -> appendedResultMap.merge(stackKey, amt * multiplier, Integer::sum));

        while (minMultiplier != maxMultiplier) {
            overlayedItemHandler.reset();

            if (currentMultiplier != previousMultiplier) {
                int diff = currentMultiplier - previousMultiplier;
                recipeOutputsToAppend.forEach((sk, amt) -> {
                    appendedResultMap.put(sk, appendedResultMap.get(sk) + (amt * diff));
                });
                previousMultiplier = currentMultiplier;
            }

            int returnedAmount = 0;

            for (Object2IntMap.Entry<ItemStack> entry : appendedResultMap.object2IntEntrySet()) {
                int amountToInsert = entry.getIntValue();
                returnedAmount = overlayedItemHandler.insertStackedItemStack(entry.getKey(), amountToInsert);
                if (returnedAmount > 0) {
                    break;
                }
            }

            int[] bin = adjustMultiplier(returnedAmount == 0, minMultiplier, currentMultiplier, maxMultiplier);
            minMultiplier = bin[0];
            currentMultiplier = bin[1];
            maxMultiplier = bin[2];

        }
        return currentMultiplier;
    }

    /**
     * Binary-search-like approach to find the maximum amount that can be inserted
     *
     * @param mergedAll     if the merge was successful.
     *                      If true sets {@code minMultiplier} to the as the current multiplier
     *                      then sets {@code multiplier} to the sum of the mean difference between
     *                      {@code multiplier} and {@code maxMultiplier} plus the remainder of the division, if any,
     *                      and itself
     *                      If false, sets {@code maxMultiplier} as the current multiplier, then sets {@code multiplier}
     *                      to half of its value limited it to no less or than the value of {@code minMultiplier}
     * @param minMultiplier the last known multiplier what was fully merged
     * @param multiplier    the current multiplier
     * @param maxMultiplier the last know multiplier that resulted in simulation failure
     * @return an array consisting of the last known multiplier, new multiplier to be attempted and
     *         the last know multiplier that resulted in failure
     */
    public static int @NotNull [] adjustMultiplier(boolean mergedAll, int minMultiplier, int multiplier,
                                                   int maxMultiplier) {
        if (mergedAll) {
            minMultiplier = multiplier;
            int remainder = (maxMultiplier - multiplier) % 2;
            multiplier = multiplier + remainder + (maxMultiplier - multiplier) / 2;
        } else {
            maxMultiplier = multiplier;
            multiplier = (multiplier + minMultiplier) / 2;
        }
        if (maxMultiplier - minMultiplier <= 1) {
            multiplier = maxMultiplier = minMultiplier;
        }
        return new int[] { minMultiplier, multiplier, maxMultiplier };
    }

    /**
     * @param recipe     the recipe from which we get the fluid input to product ratio
     * @param multiplier the maximum possible multiplied we can get from the input tanks
     *                   see
     *                   {@link ParallelLogic#getMaxRecipeMultiplier(Recipe, IItemHandlerModifiable, MultipleTankHandler, int)}
     * @return the amount of times a {@link Recipe} outputs can be merged into a fluid handler without
     *         voiding products.
     */
    public static int limitParallelByFluids(@NotNull Recipe recipe,
                                            @NotNull MultipleTankHandler tankHandler, int multiplier) {
        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        while (minMultiplier != maxMultiplier) {

            int amountLeft = 0;

            for (FluidStack fluidStack : recipe.getFluidOutputs()) {
                if (fluidStack.amount <= 0) continue;
                // Since multiplier starts at Int.MAX, check here for integer overflow
                if (multiplier > Integer.MAX_VALUE / fluidStack.amount) {
                    amountLeft = Integer.MAX_VALUE;
                } else {
                    amountLeft = fluidStack.amount * multiplier;
                }
                int inserted = tankHandler.simulateFill(fluidStack, amountLeft);
                if (inserted > 0) {
                    amountLeft -= inserted;
                }
                if (amountLeft > 0) {
                    break;
                }
            }

            int[] bin = adjustMultiplier(amountLeft == 0, minMultiplier, multiplier, maxMultiplier);
            minMultiplier = bin[0];
            multiplier = bin[1];
            maxMultiplier = bin[2];

        }
        return multiplier;
    }

    /**
     * Finds the maximum number of Recipes that can be performed at the same time based on the items in the item input
     * inventory
     *
     * @param countIngredients a {@link Map} of {@link ItemStack}s that is the result of calling
     *                         {@link GTHashMaps#fromItemHandler(IItemHandler)}
     * @param recipe           The {@link Recipe} for which to find the maximum that can be run simultaneously
     * @param parallelAmount   The limit on the amount of recipes that can be performed at one time
     * @return The Maximum number of Recipes that can be performed at a single time based on the available Items
     */
    protected static int getMaxRatioItem(@NotNull Object2IntMap<ItemStack> countIngredients, @NotNull Recipe recipe,
                                         int parallelAmount) {
        int minMultiplier = Integer.MAX_VALUE;
        // map the recipe ingredients to account for duplicated and notConsumable ingredients.
        // notConsumable ingredients are not counted towards the max ratio
        Object2IntOpenHashMap<GTRecipeInput> notConsumableMap = new Object2IntOpenHashMap<>();
        Object2IntOpenHashMap<GTRecipeInput> countableMap = new Object2IntOpenHashMap<>();
        for (GTRecipeInput recipeIngredient : recipe.getInputs()) {

            int ingredientCount = recipeIngredient.getAmount();
            if (recipeIngredient.isNonConsumable()) {
                notConsumableMap.computeIfPresent(recipeIngredient, (k, v) -> v + ingredientCount);
                notConsumableMap.putIfAbsent(recipeIngredient, ingredientCount);
            } else {
                countableMap.computeIfPresent(recipeIngredient, (k, v) -> v + ingredientCount);
                countableMap.putIfAbsent(recipeIngredient, ingredientCount);
            }
        }

        // Iterate through the recipe inputs, excluding the not consumable ingredients from the inventory map
        for (Object2IntMap.Entry<GTRecipeInput> recipeInputEntry : notConsumableMap.object2IntEntrySet()) {
            int needed = recipeInputEntry.getIntValue();
            int available = 0;
            // For every stack in the ingredients gathered from the input bus.
            for (Object2IntMap.Entry<ItemStack> inventoryEntry : countIngredients.object2IntEntrySet()) {
                if (recipeInputEntry.getKey().acceptsStack(inventoryEntry.getKey())) {
                    available = inventoryEntry.getIntValue();
                    if (available > needed) {
                        inventoryEntry.setValue(available - needed);
                        needed -= available;
                        break;
                    } else {
                        inventoryEntry.setValue(0);
                        recipeInputEntry.setValue(needed - available);
                        needed -= available;
                    }
                }
            }
            // We need to check >= available here because of Non-Consumable inputs with stack size. If there is a NC
            // input
            // with size 2, and only 1 in the input, needed will be equal to available, but this situation should still
            // fail
            // as not all inputs are present
            if (needed >= available) {
                return 0;
            }
        }

        // Return the maximum parallel limit here if there are only non-consumed inputs, which are all found in the
        // input bus
        // At this point, we would have already returned 0 if we were missing any non-consumable inputs, so we can omit
        // that check
        if (countableMap.isEmpty() && !notConsumableMap.isEmpty()) {
            return parallelAmount;
        }

        // Iterate through the recipe inputs
        for (Object2IntMap.Entry<GTRecipeInput> recipeInputEntry : countableMap.object2IntEntrySet()) {
            int needed = recipeInputEntry.getIntValue();
            int available = 0;
            // For every stack in the ingredients gathered from the input bus.
            for (Object2IntMap.Entry<ItemStack> inventoryEntry : countIngredients.object2IntEntrySet()) {
                if (recipeInputEntry.getKey().acceptsStack(inventoryEntry.getKey())) {
                    available += inventoryEntry.getIntValue();
                }
            }
            if (available >= needed) {
                int ratio = Math.min(parallelAmount, available / needed);
                if (ratio < minMultiplier) {
                    minMultiplier = ratio;
                }
            } else {
                return 0;
            }
        }
        return minMultiplier;
    }

    /**
     * Finds the maximum number of a specific recipe that can be performed based upon the fluids in the fluid inputs
     *
     * @param countFluid     a {@link Set} of {@link FluidStack}s that is the result of calling
     *                       {@link GTHashMaps#fromFluidHandler(IFluidHandler)}
     * @param recipe         The {@link Recipe} for which to find the maximum that can be run simultaneously
     * @param parallelAmount The limit on the amount of recipes that can be performed at one time
     * @return The Maximum number of Recipes that can be performed at a single time based on the available Fluids
     */
    protected static int getMaxRatioFluid(@NotNull Map<FluidKey, Integer> countFluid, @NotNull Recipe recipe,
                                          int parallelAmount) {
        int minMultiplier = Integer.MAX_VALUE;
        // map the recipe input fluids to account for duplicated fluids,
        // so their sum is counted against the total of fluids available in the input
        Map<FluidKey, Integer> fluidCountMap = new HashMap<>();
        Map<FluidKey, Integer> notConsumableMap = new HashMap<>();
        for (GTRecipeInput fluidInput : recipe.getFluidInputs()) {
            int fluidAmount = fluidInput.getAmount();
            if (fluidInput.isNonConsumable()) {
                notConsumableMap.computeIfPresent(new FluidKey(fluidInput.getInputFluidStack()),
                        (k, v) -> v + fluidAmount);
                notConsumableMap.putIfAbsent(new FluidKey(fluidInput.getInputFluidStack()), fluidAmount);
            } else {
                fluidCountMap.computeIfPresent(new FluidKey(fluidInput.getInputFluidStack()),
                        (k, v) -> v + fluidAmount);
                fluidCountMap.putIfAbsent(new FluidKey(fluidInput.getInputFluidStack()), fluidAmount);
            }
        }

        // Iterate through the recipe inputs, excluding the not consumable fluids from the fluid inventory map
        for (Map.Entry<FluidKey, Integer> notConsumableFluid : notConsumableMap.entrySet()) {
            int needed = notConsumableFluid.getValue();
            int available = 0;
            // For every fluid gathered from the fluid inputs.
            for (Map.Entry<FluidKey, Integer> inputFluid : countFluid.entrySet()) {
                // Strip the Non-consumable tags here, as FluidKey compares the tags, which causes finding matching
                // fluids
                // in the input tanks to fail, because there is nothing in those hatches with a non-consumable tag
                if (notConsumableFluid.getKey().equals(inputFluid.getKey())) {
                    available = inputFluid.getValue();
                    if (available > needed) {
                        inputFluid.setValue(available - needed);
                        needed -= available;
                        break;
                    } else {
                        inputFluid.setValue(0);
                        notConsumableFluid.setValue(needed - available);
                        needed -= available;
                    }
                }
            }
            // We need to check >= available here because of Non-Consumable inputs with stack size. If there is a NC
            // input
            // with size 1000, and only 500 in the input, needed will be equal to available, but this situation should
            // still fail
            // as not all inputs are present
            if (needed >= available) {
                return 0;
            }
        }

        // Return the maximum parallel limit here if there are only non-consumed inputs, which are all found in the
        // input bus
        // At this point, we would have already returned 0 if we were missing any non-consumable inputs, so we can omit
        // that check
        if (fluidCountMap.isEmpty() && !notConsumableMap.isEmpty()) {
            return parallelAmount;
        }

        // Iterate through the fluid inputs in the recipe
        for (Map.Entry<FluidKey, Integer> fs : fluidCountMap.entrySet()) {
            int needed = fs.getValue();
            int available = 0;
            // For every fluid gathered from the fluid inputs.
            for (Map.Entry<FluidKey, Integer> inputFluid : countFluid.entrySet()) {
                if (fs.getKey().equals(inputFluid.getKey())) {
                    available += inputFluid.getValue();
                }
            }
            if (available >= needed) {
                int ratio = Math.min(parallelAmount, available / needed);
                if (ratio < minMultiplier) {
                    minMultiplier = ratio;
                }
            } else {
                return 0;
            }
        }
        return minMultiplier;
    }

    // At this point, the recipe is already trimmed according to the item and fluid output limit, so we just need to
    // take care of voiding
    public static RecipeBuilder<?> doParallelRecipes(@NotNull Recipe currentRecipe, @NotNull RecipeMap<?> recipeMap,
                                                     @NotNull IItemHandlerModifiable importInventory,
                                                     @NotNull MultipleTankHandler importFluids,
                                                     @NotNull IItemHandlerModifiable exportInventory,
                                                     @NotNull MultipleTankHandler exportFluids, int parallelAmount,
                                                     long maxVoltage, @NotNull IVoidable voidable) {
        // First check if we are limited by recipe inputs. This can short circuit a lot of consecutive checking
        int multiplierByInputs = getMaxRecipeMultiplier(currentRecipe, importInventory, importFluids, parallelAmount);
        if (multiplierByInputs == 0) {
            return null;
        }
        // Make a copy of the recipe builder and zero the EUt, since we append
        // the total multiplied EUt, and not doing so may add an extra multiple
        // for the EUt (for example, x2 recipes but x3 EUt) if the original
        // recipe builder already has a cost applied. Don't also zero the
        // duration as it doesn't get multiplied.
        RecipeBuilder<?> recipeBuilder = recipeMap.recipeBuilder().EUt(0);

        boolean voidItems = voidable.canVoidRecipeItemOutputs();
        boolean voidFluids = voidable.canVoidRecipeFluidOutputs();

        // Simulate the merging of the maximum amount of recipes that can be run with these items
        // and limit by the amount we can successfully merge
        int limitByOutput;
        limitByOutput = ParallelLogic.limitByOutputMerging(currentRecipe, exportInventory, exportFluids,
                multiplierByInputs, voidItems, voidFluids);

        long recipeEUt = currentRecipe.getEUt();
        if (recipeEUt != 0) {
            int limitByVoltage = GTUtility.safeCastLongToInt(Math.abs(maxVoltage / recipeEUt));
            int parallelizable = Math.min(limitByVoltage, limitByOutput);
            if (parallelizable != 0)
                // Use the minimum between the amount of recipes we can run with available inputs and amount of recipe
                // outputs that can fit
                recipeBuilder.append(currentRecipe, Math.min(parallelizable, multiplierByInputs), false);
        } else if (limitByOutput > 0) {
            recipeBuilder.append(currentRecipe, limitByOutput, false);
        }

        return recipeBuilder;
    }

    /**
     * Constructs a {@link RecipeBuilder} containing the recipes from the ItemStacks available in the
     * {@code importInventory}
     * Does NOT take fluids into account whatsoever
     *
     * @param recipeMap       The {@link RecipeMap} to search for recipes
     * @param importInventory The {@link IItemHandlerModifiable} that contains the items to be used as inputs
     * @param exportInventory The {@link IItemHandlerModifiable} that contains the items to be used as outputs
     * @param parallelAmount  The maximum amount of recipes that can be performed at one time
     * @param maxVoltage      The maximum voltage of the machine
     * @param voidable        The MetaTileEntity performing the parallel recipe
     * @return A {@link RecipeBuilder} containing the recipes that can be performed in parallel, limited by the
     *         ingredients available, and the output space available.
     */
    public static RecipeBuilder<?> appendItemRecipes(@NotNull RecipeMap<?> recipeMap,
                                                     @NotNull IItemHandlerModifiable importInventory,
                                                     @NotNull IItemHandlerModifiable exportInventory,
                                                     int parallelAmount, long maxVoltage, IVoidable voidable) {
        RecipeBuilder<?> recipeBuilder = null;

        OverlayedItemHandler overlayedItemHandler = new OverlayedItemHandler(exportInventory);

        // Iterate over the input items looking for more things to add until we run either out of input items
        // or we have exceeded the number of items permissible from the smelting bonus
        int engagedItems = 0;

        for (int index = 0; index < importInventory.getSlots(); index++) {
            // Skip this slot if it is empty.
            final ItemStack currentInputItem = importInventory.getStackInSlot(index);
            if (currentInputItem.isEmpty())
                continue;

            // Determine if there is a valid recipe for this item. If not, skip it.
            Recipe matchingRecipe = recipeMap.findRecipe(maxVoltage,
                    Collections.singletonList(currentInputItem),
                    Collections.emptyList());

            GTRecipeInput inputIngredient;
            if (matchingRecipe != null) {
                inputIngredient = matchingRecipe.getInputs().get(0);
                if (recipeBuilder == null) {
                    // here we make a copy of the recipe builder of the current recipe map, while zeroing
                    // the recipe builder EUt, since we're going to add to the total EUt of the recipes appended.
                    // not zeroing means there is a base cost of 1 recipe EUt while doing parallel recipes
                    // for example running 2 parallel recipes would cost the EUt of doing 3 recipes.
                    // same should apply for the recipe map duration
                    recipeBuilder = recipeMap.recipeBuilder().EUt(0).duration(0);

                }
            } else continue;

            // There's something not right with this recipe if the ingredient is null.
            if (inputIngredient == null)
                throw new IllegalStateException(
                        String.format("Got recipe with null ingredient %s", matchingRecipe));

            // Trim the recipe outputs here if required
            matchingRecipe = Recipe.trimRecipeOutputs(matchingRecipe, recipeMap, voidable.getItemOutputLimit(),
                    voidable.getFluidOutputLimit());

            // equivalent of getting the max ratio from the inputs from Parallel logic
            int ingredientRatio = Math.min(parallelAmount - engagedItems,
                    currentInputItem.getCount() / Math.max(matchingRecipe.getInputs().get(0).getAmount(), 1));

            // how much we can add to the output inventory
            int limitByOutput = Integer.MAX_VALUE;
            if (!voidable.canVoidRecipeItemOutputs()) {
                // Limit by the number of recipe outputs and chanced outputs, to simulate cases where 100% chanced
                // outputs were obtained
                limitByOutput = limitParallelByItemsIncremental(recipeBuilder.getAllItemOutputs(),
                        matchingRecipe.getOutputs(), overlayedItemHandler, ingredientRatio);
            }

            // amount to actually multiply the recipe by
            int multiplierRecipeAmount = Math.min(ingredientRatio, limitByOutput);

            if (multiplierRecipeAmount > 0) {
                recipeBuilder.append(matchingRecipe, multiplierRecipeAmount, true);
                engagedItems += multiplierRecipeAmount;
            }

            if (engagedItems == parallelAmount) {
                break;
            }
        }
        return recipeBuilder;
    }
}
