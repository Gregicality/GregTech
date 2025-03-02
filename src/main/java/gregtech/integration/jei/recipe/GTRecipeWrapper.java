package gregtech.integration.jei.recipe;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IDataItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.chance.boost.BoostableChanceEntry;
import gregtech.api.recipes.chance.output.ChancedOutputLogic;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.recipes.machines.IScannerRecipeMap;
import gregtech.api.recipes.properties.RecipeProperty;
import gregtech.api.recipes.properties.impl.ComputationProperty;
import gregtech.api.recipes.properties.impl.ScanProperty;
import gregtech.api.recipes.properties.impl.TotalComputationProperty;
import gregtech.api.util.AssemblyLineManager;
import gregtech.api.util.ClipboardUtil;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.TooltipHelper;
import gregtech.integration.RecipeCompatUtil;
import gregtech.integration.jei.utils.AdvancedRecipeWrapper;
import gregtech.integration.jei.utils.JeiButton;
import gregtech.integration.jei.utils.JeiInteractableText;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class GTRecipeWrapper extends AdvancedRecipeWrapper {

    private static final int LINE_HEIGHT = 10;

    private final RecipeMap<?> recipeMap;
    private final Recipe recipe;
    private final List<GTRecipeInput> sortedInputs;
    private final List<GTRecipeInput> sortedFluidInputs;

    public GTRecipeWrapper(RecipeMap<?> recipeMap, Recipe recipe) {
        this.recipeMap = recipeMap;
        this.recipe = recipe;

        this.sortedInputs = new ArrayList<>(recipe.getInputs());
        this.sortedInputs.sort(GTRecipeInput.RECIPE_INPUT_COMPARATOR);
        this.sortedFluidInputs = new ArrayList<>(recipe.getFluidInputs());
        this.sortedFluidInputs.sort(GTRecipeInput.RECIPE_INPUT_COMPARATOR);

        initExtras();
    }

    public Recipe getRecipe() {
        return recipe;
    }

    @Override
    public void getIngredients(@NotNull IIngredients ingredients) {
        // Inputs
        if (!sortedInputs.isEmpty()) {
            List<List<ItemStack>> list = new ArrayList<>();
            for (GTRecipeInput input : sortedInputs) {
                List<ItemStack> stacks = new ArrayList<>();
                for (ItemStack stack : input.getInputStacks()) {
                    stacks.add(stack.copy());
                }
                list.add(stacks);
            }
            ingredients.setInputLists(VanillaTypes.ITEM, list);
        }

        // Fluid Inputs
        if (!sortedFluidInputs.isEmpty()) {
            List<FluidStack> list = new ArrayList<>();
            for (GTRecipeInput input : sortedFluidInputs) {
                list.add(input.getInputFluidStack());
            }
            ingredients.setInputs(VanillaTypes.FLUID, list);
        }

        // Outputs
        if (!recipe.getOutputs().isEmpty() || !recipe.getChancedOutputs().getChancedEntries().isEmpty()) {
            List<ItemStack> recipeOutputs = recipe.getOutputs()
                    .stream().map(ItemStack::copy)
                    .collect(Collectors.toList());

            List<ItemStack> scannerPossibilities = null;
            if (this.recipeMap instanceof IScannerRecipeMap) {
                scannerPossibilities = new ArrayList<>();
                // Scanner Output replacing, used for cycling research outputs
                String researchId = null;
                for (ItemStack stack : recipe.getOutputs()) {
                    researchId = AssemblyLineManager.readResearchId(stack);
                    if (researchId != null) break;
                }
                if (researchId != null) {
                    Collection<Recipe> possibleRecipes = ((IResearchRecipeMap) RecipeMaps.ASSEMBLY_LINE_RECIPES)
                            .getDataStickEntry(researchId);
                    if (possibleRecipes != null) {
                        for (Recipe r : possibleRecipes) {
                            ItemStack researchItem = r.getOutputs().get(0);
                            researchItem = researchItem.copy();
                            researchItem.setCount(1);
                            boolean didMatch = false;
                            for (ItemStack stack : scannerPossibilities) {
                                if (ItemStack.areItemStacksEqual(stack, researchItem)) {
                                    didMatch = true;
                                    break;
                                }
                            }
                            if (!didMatch) scannerPossibilities.add(researchItem);
                        }
                    }
                    scannerPossibilities.add(recipeOutputs.get(0));
                }
            }

            List<ChancedItemOutput> chancedOutputs = new ArrayList<>(recipe.getChancedOutputs().getChancedEntries());
            for (ChancedItemOutput chancedEntry : chancedOutputs) {
                recipeOutputs.add(chancedEntry.getIngredient());
            }

            if (scannerPossibilities == null || scannerPossibilities.isEmpty()) {
                ingredients.setOutputs(VanillaTypes.ITEM, recipeOutputs);
            } else {
                ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(scannerPossibilities));
            }
        }

        // Fluid Outputs
        if (!recipe.getFluidOutputs().isEmpty() || !recipe.getChancedFluidOutputs().getChancedEntries().isEmpty()) {
            List<FluidStack> recipeOutputs = recipe.getFluidOutputs().stream()
                    .map(FluidStack::copy)
                    .collect(Collectors.toList());

            List<ChancedFluidOutput> chancedOutputs = new ArrayList<>(
                    recipe.getChancedFluidOutputs().getChancedEntries());
            for (ChancedFluidOutput chancedEntry : chancedOutputs) {
                recipeOutputs.add(chancedEntry.getIngredient());
            }

            ingredients.setOutputs(VanillaTypes.FLUID, recipeOutputs);
        }
    }

    public void addItemTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip) {
        boolean notConsumed = input && isNotConsumedItem(slotIndex);

        BoostableChanceEntry<?> entry = null;
        if (!input) {
            if (!recipe.getChancedOutputs().getChancedEntries().isEmpty()) {
                int outputIndex = slotIndex - recipeMap.getMaxInputs();
                if (outputIndex >= recipe.getOutputs().size()) {
                    entry = recipe.getChancedOutputs().getChancedEntries()
                            .get(outputIndex - recipe.getOutputs().size());
                }
            }
        }

        addIngredientTooltips(tooltip, notConsumed, input, entry, recipe.getChancedOutputs().getChancedOutputLogic());
        addIngredientTooltips(tooltip, notConsumed, input, ingredient, null);
    }

    public void addFluidTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip) {
        FluidStack fluidStack = (FluidStack) ingredient;
        TankWidget.addIngotMolFluidTooltip(fluidStack, tooltip);

        boolean notConsumed = input && isNotConsumedFluid(slotIndex);

        BoostableChanceEntry<?> entry = null;
        if (!recipe.getChancedFluidOutputs().getChancedEntries().isEmpty()) {
            int outputIndex = slotIndex - recipeMap.getMaxFluidInputs();
            if (outputIndex >= recipe.getFluidOutputs().size()) {
                entry = recipe.getChancedFluidOutputs().getChancedEntries()
                        .get(outputIndex - recipe.getFluidOutputs().size());
            }
        }

        addIngredientTooltips(tooltip, notConsumed, input, entry,
                recipe.getChancedFluidOutputs().getChancedOutputLogic());
        addIngredientTooltips(tooltip, notConsumed, input, ingredient, null);
    }

    public void addIngredientTooltips(@NotNull Collection<String> tooltip, boolean notConsumed, boolean input,
                                      @Nullable Object ingredient, @Nullable Object ingredient2) {
        if (ingredient2 instanceof ChancedOutputLogic logic) {
            if (ingredient instanceof BoostableChanceEntry<?>entry) {
                double chance = entry.getChance() / 100.0;
                double boost = entry.getChanceBoost() / 100.0;
                if (logic != ChancedOutputLogic.NONE && logic != ChancedOutputLogic.OR) {
                    tooltip.add(TooltipHelper.BLINKING_CYAN + I18n.format("gregtech.recipe.chance_logic",
                            chance, boost, I18n.format(logic.getTranslationKey())));
                } else {
                    tooltip.add(TooltipHelper.BLINKING_CYAN + I18n.format("gregtech.recipe.chance",
                            chance, boost));
                }

                // Add the total chance to the tooltip
                if (recipeMap.jeiOverclockButtonEnabled()) {
                    int tier = jeiTexts.get(0).getState();
                    int recipeTier = Math.max(GTValues.LV, GTUtility.getTierByVoltage(recipe.getEUt()));
                    int tierDifference = tier - recipeTier;

                    // The total chance may or may not max out at 100%.
                    // TODO possibly change in the future.
                    double totalChance = Math.min(chance + boost * tierDifference, 100);
                    tooltip.add(I18n.format("gregtech.recipe.chance_total", GTValues.VOCNF[tier], totalChance));
                }
            }
        } else if (notConsumed) {
            tooltip.add(TooltipHelper.BLINKING_CYAN + I18n.format("gregtech.recipe.not_consumed"));
        }

        if (!input && this.recipeMap instanceof IScannerRecipeMap && ingredient instanceof ItemStack stack &&
                !stack.isEmpty()) {
            // check for "normal" data items
            if (stack.getItem() instanceof IDataItem) return;
            // check for metaitem data items
            if (stack.getItem() instanceof MetaItem<?>metaItem) {
                for (IItemBehaviour behaviour : metaItem.getBehaviours(stack)) {
                    if (behaviour instanceof IDataItem) {
                        return;
                    }
                }
            }
            // If we are here, we know this is not the data item, so add the tooltip
            if (recipe.hasProperty(ScanProperty.getInstance())) {
                tooltip.add(TooltipHelper.BLINKING_CYAN + I18n.format("gregtech.recipe.research_result"));
            }
        }
    }

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
        var storage = recipe.propertyStorage();
        var properties = storage.values();
        boolean drawTotalEU = properties.isEmpty() || properties.stream().noneMatch(RecipeProperty::hideTotalEU);
        boolean drawEUt = properties.isEmpty() || properties.stream().noneMatch(RecipeProperty::hideEUt);
        boolean drawDuration = properties.isEmpty() || properties.stream().noneMatch(RecipeProperty::hideDuration);

        int defaultLines = 0;
        if (drawTotalEU) defaultLines++;
        if (drawEUt) defaultLines++;
        if (drawDuration) defaultLines++;

        int unhiddenCount = (int) storage.entrySet().stream()
                .filter((property) -> !property.getKey().isHidden())
                .count();
        int yPosition = recipeHeight - ((unhiddenCount + defaultLines) * 10 - 3);

        // [EUt, duration, color]
        long[] overclockResult = calculateJeiOverclock();

        // Default entries
        if (drawTotalEU) {
            // sadly we still need a custom override here, since computation uses duration and EU/t very differently
            if (recipe.hasProperty(TotalComputationProperty.getInstance()) &&
                    recipe.hasProperty(ComputationProperty.getInstance())) {
                long eu = Math.abs(recipe.getEUt()) * recipe.getDuration();
                int minimumCWUt = recipe.getProperty(ComputationProperty.getInstance(), 1);
                minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.max_eu", eu / minimumCWUt), 0, yPosition,
                        0x111111);
            } else {
                minecraft.fontRenderer.drawString(
                        I18n.format("gregtech.recipe.total", overclockResult[0] * overclockResult[1]), 0, yPosition,
                        (int) overclockResult[2]);
            }
        }
        if (drawEUt) {
            // scuffed way of dealing with 2 eu/t recipes, just recomputing instead of checking if eu/t <= 2
            minecraft.fontRenderer.drawString(
                    I18n.format(recipe.getEUt() >= 0 ? "gregtech.recipe.eu" : "gregtech.recipe.eu_inverted",
                            overclockResult[0],
                            GTValues.VOCNF[GTUtility.getOCTierByVoltage(overclockResult[0])]),
                    0, yPosition += LINE_HEIGHT, (int) overclockResult[2]);
        }
        if (drawDuration) {
            minecraft.fontRenderer.drawString(
                    I18n.format("gregtech.recipe.duration",
                            TextFormattingUtil.formatNumbers(overclockResult[1] / 20D)),
                    0, yPosition += LINE_HEIGHT, (int) overclockResult[2]);
        }
        // Property custom entries
        for (var propertyEntry : storage.entrySet()) {
            if (!propertyEntry.getKey().isHidden()) {
                RecipeProperty<?> property = propertyEntry.getKey();
                var value = propertyEntry.getValue();
                property.drawInfo(minecraft, 0, yPosition += property.getInfoHeight(value), 0x111111, value, mouseX,
                        mouseY);
            }
        }
    }

    @NotNull
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        List<String> tooltips = new ArrayList<>();
        for (var entry : recipe.propertyStorage().entrySet()) {
            if (!entry.getKey().isHidden()) {
                RecipeProperty<?> property = entry.getKey();
                Object value = entry.getValue();
                property.getTooltipStrings(tooltips, mouseX, mouseY, value);
            }
        }
        return tooltips;
    }

    @Override
    public void initExtras() {
        // do not add the info or X button if no tweaker mod is present
        if (!RecipeCompatUtil.isTweakerLoaded()) return;

        BooleanSupplier creativePlayerPredicate = () -> Minecraft.getMinecraft().player != null &&
                Minecraft.getMinecraft().player.isCreative();
        BooleanSupplier creativeTweaker = () -> creativePlayerPredicate.getAsBoolean() &&
                (recipe.getIsCTRecipe() || recipe.isGroovyRecipe());
        BooleanSupplier creativeDefault = () -> creativePlayerPredicate.getAsBoolean() && !recipe.getIsCTRecipe() &&
                !recipe.isGroovyRecipe();

        // X Button
        buttons.add(new JeiButton(166, 2, 10, 10)
                .setTextures(GuiTextures.BUTTON_CLEAR_GRID)
                .setTooltipBuilder(lines -> lines.add(
                        LocalizationUtils.format("gregtech.jei.remove_recipe.tooltip",
                                RecipeCompatUtil.getTweakerName())))
                .setClickAction((minecraft, mouseX, mouseY, mouseButton) -> {
                    String recipeLine = RecipeCompatUtil.getRecipeRemoveLine(recipeMap, recipe);
                    String output = RecipeCompatUtil.getFirstOutputString(recipe);
                    if (!output.isEmpty()) {
                        output = "// " + output + "\n";
                    }
                    String copyString = output + recipeLine + "\n";
                    ClipboardUtil.copyToClipboard(copyString);
                    Minecraft.getMinecraft().player.sendMessage(
                            new TextComponentString("Copied [\u00A76" + recipeLine + "\u00A7r] to the clipboard"));
                    return true;
                })
                .setActiveSupplier(creativeDefault));

        // CT/GS Info
        buttons.add(new JeiButton(166, 2, 10, 10)
                .setTextures(GuiTextures.INFO_ICON)
                .setTooltipBuilder(lines -> lines.add(recipe.isGroovyRecipe() ?
                        LocalizationUtils.format("gregtech.jei.gs_recipe.tooltip") :
                        LocalizationUtils.format("gregtech.jei.ct_recipe.tooltip")))
                .setClickAction((mc, x, y, button) -> false)
                .setActiveSupplier(creativeTweaker));

        if (recipeMap != null && recipeMap.jeiOverclockButtonEnabled()) {
            int recipeTier = Math.max(GTValues.LV, GTUtility.getTierByVoltage(recipe.getEUt()));
            // just here because if highTier is disabled, if a recipe is (incorrectly) registering
            // UIV+ recipes, this allows it to go up to the recipe tier for that recipe only
            int maxTier = Math.max(recipeTier, GregTechAPI.isHighTier() ? GTValues.UIV : GTValues.MAX_TRUE);
            int minTier = Math.max(GTValues.LV, GTUtility.getTierByVoltage(recipe.getEUt()));
            // scuffed positioning because we can't have good ui(until mui soontm)
            jeiTexts.add(
                    new JeiInteractableText(0, 90 - LINE_HEIGHT, GTValues.VOCNF[recipeTier], 0x111111, recipeTier, true)
                            .setTooltipBuilder((state, tooltip) -> {
                                tooltip.add(I18n.format("gregtech.jei.overclock_button", GTValues.VOCNF[state]));
                                tooltip.add(TooltipHelper.BLINKING_CYAN + I18n.format("gregtech.jei.overclock_warn"));
                            })
                            .setClickAction((minecraft, text, mouseX, mouseY, mouseButton) -> {
                                int state = text.getState();
                                if (mouseButton == 0) {
                                    // increment tier if left click
                                    if (++state > maxTier) state = minTier;
                                } else if (mouseButton == 1) {
                                    // decrement tier if right click
                                    if (--state < minTier) state = maxTier;
                                } else if (mouseButton == 2) {
                                    // reset tier if middle click
                                    state = minTier;
                                } else return false;
                                text.setCurrentText(GTValues.VOCNF[state]);
                                text.setState(state);
                                return true;
                            }));
        }
    }

    public long[] calculateJeiOverclock() {
        // simple case
        if (!recipeMap.jeiOverclockButtonEnabled())
            return new long[] { recipe.getEUt(), recipe.getDuration(), 0x111111 };

        // ULV doesn't overclock to LV, so treat ULV recipes as LV
        int recipeTier = Math.max(GTValues.LV, GTUtility.getTierByVoltage(recipe.getEUt()));
        // tier difference *should* not be negative here since at least displayOCTier() == recipeTier
        int tierDifference = jeiTexts.get(0).getState() - recipeTier;
        // there isn't any overclocking
        if (tierDifference == 0) return new long[] { recipe.getEUt(), recipe.getDuration(), 0x111111 };

        long[] result = new long[3];
        // if duration is less than 0.5, that means even with one less overclock, the recipe would still 1 tick
        // so add the yellow warning
        // LCR and fusion get manual overrides for now
        double duration = Math.floor(recipe.getDuration() /
                Math.pow(recipeMap == RecipeMaps.LARGE_CHEMICAL_RECIPES ? 4 : 2, tierDifference));
        result[2] = duration <= 0.5 ? 0xFFFF55 : 0x111111;
        result[0] = Math.abs(recipe.getEUt()) *
                (int) Math.pow(recipeMap == RecipeMaps.FUSION_RECIPES ? 2 : 4, tierDifference);
        result[1] = Math.max(1, (int) duration);

        return result;
    }

    public ChancedItemOutput getOutputChance(int slot) {
        if (slot >= recipe.getChancedOutputs().getChancedEntries().size() || slot < 0) return null;
        return recipe.getChancedOutputs().getChancedEntries().get(slot);
    }

    public ChancedOutputLogic getChancedOutputLogic() {
        return recipe.getChancedOutputs().getChancedOutputLogic();
    }

    public ChancedFluidOutput getFluidOutputChance(int slot) {
        if (slot >= recipe.getChancedFluidOutputs().getChancedEntries().size() || slot < 0) return null;
        return recipe.getChancedFluidOutputs().getChancedEntries().get(slot);
    }

    public ChancedOutputLogic getChancedFluidOutputLogic() {
        return recipe.getChancedFluidOutputs().getChancedOutputLogic();
    }

    public boolean isNotConsumedItem(int slot) {
        return slot < this.sortedInputs.size() && this.sortedInputs.get(slot).isNonConsumable();
    }

    public boolean isNotConsumedFluid(int slot) {
        return slot < this.sortedFluidInputs.size() && this.sortedFluidInputs.get(slot).isNonConsumable();
    }
}
