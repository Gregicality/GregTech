package gregtech.integration.jei.recipe;

import gregtech.api.GTValues;
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
                double chance = 100 * (double) entry.getChance() / entry.getMaxChance();
                String percent = TextFormattingUtil.formatPercent(chance);
                double boost = entry.getChanceBoost() / 100.0;
                if (logic != ChancedOutputLogic.NONE && logic != ChancedOutputLogic.OR) {
                    tooltip.add(TooltipHelper.BLINKING_CYAN + I18n.format("gregtech.recipe.chance_logic",
                            percent, boost, I18n.format(logic.getTranslationKey())));
                } else {
                    tooltip.add(TooltipHelper.BLINKING_CYAN + I18n.format("gregtech.recipe.chance",
                            percent, boost));
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

        // Default entries
        if (drawTotalEU) {
            long eu = recipe.getEUt() * recipe.getDuration();
            // sadly we still need a custom override here, since computation uses duration and EU/t very differently
            if (storage.contains(TotalComputationProperty.getInstance()) &&
                    storage.contains(ComputationProperty.getInstance())) {
                int minimumCWUt = storage.get(ComputationProperty.getInstance(), 1);
                minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.max_eu", eu / minimumCWUt), 0, yPosition,
                        0x111111);
            } else {
                minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.total", eu), 0, yPosition, 0x111111);
            }
        }
        if (drawEUt) {
            minecraft.fontRenderer.drawString(
                    I18n.format(
                            recipeMap.getRecipeMapUI().isGenerator() ? "gregtech.recipe.eu_inverted" :
                                    "gregtech.recipe.eu",
                            recipe.getEUt(), GTValues.VN[GTUtility.getTierByVoltage(recipe.getEUt())]),
                    0, yPosition += LINE_HEIGHT, 0x111111);
        }
        if (drawDuration) {
            minecraft.fontRenderer.drawString(
                    I18n.format("gregtech.recipe.duration",
                            TextFormattingUtil.formatNumbers(recipe.getDuration() / 20.0)),
                    0, yPosition += LINE_HEIGHT, 0x111111);
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
