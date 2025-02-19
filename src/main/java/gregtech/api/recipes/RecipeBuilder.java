package gregtech.api.recipes;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.api.recipes.chance.output.ChancedOutputList;
import gregtech.api.recipes.chance.output.ChancedOutputLogic;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.ingredients.GTRecipeOreInput;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.api.recipes.properties.RecipeProperty;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.recipes.properties.RecipePropertyStorageImpl;
import gregtech.api.recipes.properties.impl.CleanroomProperty;
import gregtech.api.recipes.properties.impl.DimensionProperty;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;
import gregtech.api.util.ValidationResult;
import gregtech.common.ConfigHolder;
import gregtech.integration.groovy.GroovyScriptModule;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.helper.ingredient.OreDictIngredient;
import crafttweaker.CraftTweakerAPI;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @see Recipe
 */

@SuppressWarnings("unchecked")
public class RecipeBuilder<R extends RecipeBuilder<R>> {

    protected RecipeMap<R> recipeMap;

    protected final List<GTRecipeInput> inputs;
    protected final List<ItemStack> outputs;
    protected final List<ChancedItemOutput> chancedOutputs;

    protected final List<GTRecipeInput> fluidInputs;
    protected final List<FluidStack> fluidOutputs;
    protected final List<ChancedFluidOutput> chancedFluidOutputs;

    protected ChancedOutputLogic chancedOutputLogic = ChancedOutputLogic.OR;
    protected ChancedOutputLogic chancedFluidOutputLogic = ChancedOutputLogic.OR;

    protected int duration;
    protected long EUt;
    protected boolean hidden = false;
    protected GTRecipeCategory category;
    protected boolean isCTRecipe = false;
    protected int parallel = 0;
    protected EnumValidationResult recipeStatus = EnumValidationResult.VALID;
    protected RecipePropertyStorage recipePropertyStorage = RecipePropertyStorage.EMPTY;
    protected boolean recipePropertyStorageErrored = false;

    protected boolean ignoreAllBuildActions = false;
    protected Map<ResourceLocation, RecipeBuildAction<R>> ignoredBuildActions;

    protected RecipeBuilder() {
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.chancedOutputs = new ArrayList<>();
        this.fluidInputs = new ArrayList<>();
        this.fluidOutputs = new ArrayList<>();
        this.chancedFluidOutputs = new ArrayList<>();
    }

    public RecipeBuilder(Recipe recipe, RecipeMap<R> recipeMap) {
        this.recipeMap = recipeMap;
        this.inputs = new ArrayList<>(recipe.getInputs());
        this.outputs = new ArrayList<>(recipe.getOutputs());
        this.chancedOutputs = new ArrayList<>(recipe.getChancedOutputs().getChancedEntries());
        this.fluidInputs = new ArrayList<>(recipe.getFluidInputs());
        this.fluidOutputs = GTUtility.copyFluidList(recipe.getFluidOutputs());
        this.chancedFluidOutputs = new ArrayList<>(recipe.getChancedFluidOutputs().getChancedEntries());
        this.duration = recipe.getDuration();
        this.EUt = recipe.getEUt();
        this.hidden = recipe.isHidden();
        this.category = recipe.getRecipeCategory();
        this.recipePropertyStorage = recipe.propertyStorage().copy();
    }

    @SuppressWarnings("CopyConstructorMissesField")
    protected RecipeBuilder(RecipeBuilder<R> recipeBuilder) {
        this.recipeMap = recipeBuilder.recipeMap;
        this.inputs = new ArrayList<>(recipeBuilder.getInputs());
        this.outputs = new ArrayList<>(recipeBuilder.getOutputs());
        this.chancedOutputs = new ArrayList<>(recipeBuilder.chancedOutputs);
        this.fluidInputs = new ArrayList<>(recipeBuilder.getFluidInputs());
        this.fluidOutputs = GTUtility.copyFluidList(recipeBuilder.getFluidOutputs());
        this.chancedFluidOutputs = new ArrayList<>(recipeBuilder.chancedFluidOutputs);
        this.chancedOutputLogic = recipeBuilder.chancedOutputLogic;
        this.chancedFluidOutputLogic = recipeBuilder.chancedFluidOutputLogic;
        this.duration = recipeBuilder.duration;
        this.EUt = recipeBuilder.EUt;
        this.hidden = recipeBuilder.hidden;
        this.category = recipeBuilder.category;
        this.recipePropertyStorage = recipeBuilder.recipePropertyStorage.copy();
        this.ignoreAllBuildActions = recipeBuilder.ignoreAllBuildActions;
        if (recipeBuilder.ignoredBuildActions != null) {
            this.ignoredBuildActions = new Object2ObjectOpenHashMap<>(recipeBuilder.ignoredBuildActions);
        }
    }

    public R cleanroom(@Nullable CleanroomType cleanroom) {
        if (ConfigHolder.machines.enableCleanroom && cleanroom != null) {
            this.applyProperty(CleanroomProperty.getInstance(), cleanroom);
        }
        return (R) this;
    }

    public R dimension(int dimensionID) {
        return dimension(dimensionID, false);
    }

    public R dimension(int dimensionID, boolean toBlackList) {
        DimensionProperty.DimensionPropertyList dimensionIDs = getCompleteDimensionIDs();
        if (dimensionIDs == null) {
            dimensionIDs = new DimensionProperty.DimensionPropertyList();
            this.applyProperty(DimensionProperty.getInstance(), dimensionIDs);
        }
        dimensionIDs.add(dimensionID, toBlackList);
        return (R) this;
    }

    public @Nullable DimensionProperty.DimensionPropertyList getCompleteDimensionIDs() {
        return this.recipePropertyStorage.get(DimensionProperty.getInstance(), null);
    }

    public @NotNull IntList getDimensionIDs() {
        return this.recipePropertyStorage.get(DimensionProperty.getInstance(),
                DimensionProperty.DimensionPropertyList.EMPTY_LIST).whiteListDimensions;
    }

    public @NotNull IntList getBlockedDimensionIDs() {
        return this.recipePropertyStorage.get(DimensionProperty.getInstance(),
                DimensionProperty.DimensionPropertyList.EMPTY_LIST).blackListDimensions;
    }

    @MustBeInvokedByOverriders
    public boolean applyPropertyCT(@NotNull String key, @NotNull Object value) {
        if (key.equals(DimensionProperty.KEY)) {
            if (value instanceof DimensionProperty.DimensionPropertyList list) {
                DimensionProperty.DimensionPropertyList dimensionIDs = getCompleteDimensionIDs();
                if (dimensionIDs == null) {
                    dimensionIDs = new DimensionProperty.DimensionPropertyList();
                    this.applyProperty(DimensionProperty.getInstance(), dimensionIDs);
                }
                dimensionIDs.merge(list);
                return true;
            }
            return false;
        } else if (key.equals(CleanroomProperty.KEY)) {
            if (value instanceof CleanroomType) {
                this.cleanroom((CleanroomType) value);
            } else if (value instanceof String) {
                this.cleanroom(CleanroomType.getByName((String) value));
            } else {
                return false;
            }
            return true;
        }
        return false;
    }

    public final boolean applyProperty(@NotNull RecipeProperty<?> property, @NotNull Object value) {
        if (this.recipePropertyStorage == RecipePropertyStorage.EMPTY) {
            this.recipePropertyStorage = new RecipePropertyStorageImpl();
        }

        boolean stored = this.recipePropertyStorage.store(property, value);
        if (!stored) {
            this.recipePropertyStorageErrored = true;
        }
        return stored;
    }

    public R input(GTRecipeInput input) {
        if (input.getAmount() < 0) {
            GTLog.logger.error("Count cannot be less than 0. Actual: {}.", input.getAmount(), new Throwable());
        } else {
            this.inputs.add(input);
        }
        return (R) this;
    }

    public R input(String oredict) {
        return input(new GTRecipeOreInput(oredict));
    }

    public R input(String oredict, int count) {
        return input(new GTRecipeOreInput(oredict, count));
    }

    public R input(OrePrefix orePrefix, Material material) {
        return input(new GTRecipeOreInput(orePrefix, material));
    }

    public R input(OrePrefix orePrefix, Material material, int count) {
        return input(new GTRecipeOreInput(orePrefix, material, count));
    }

    public R input(Item item) {
        return input(new GTRecipeItemInput(new ItemStack(item)));
    }

    public R input(Item item, int count) {
        return input(new GTRecipeItemInput(new ItemStack(item), count));
    }

    public R input(Item item, int count, int meta) {
        return input(new GTRecipeItemInput(new ItemStack(item, count, meta)));
    }

    public R input(Item item, int count, @SuppressWarnings("unused") boolean wild) {
        return input(new GTRecipeItemInput(new ItemStack(item, count, GTValues.W)));
    }

    public R input(Block block) {
        return input(block, 1);
    }

    public R input(Block block, int count) {
        return input(new GTRecipeItemInput(new ItemStack(block, count)));
    }

    public R input(Block block, int count, @SuppressWarnings("unused") boolean wild) {
        return input(new GTRecipeItemInput(new ItemStack(block, count, GTValues.W)));
    }

    public R input(MetaItem<?>.MetaValueItem item, int count) {
        return input(new GTRecipeItemInput(item.getStackForm(count)));
    }

    public R input(MetaItem<?>.MetaValueItem item) {
        return input(new GTRecipeItemInput(item.getStackForm()));
    }

    public R input(MetaTileEntity mte) {
        return input(new GTRecipeItemInput(mte.getStackForm()));
    }

    public R input(MetaTileEntity mte, int amount) {
        return input(new GTRecipeItemInput(mte.getStackForm(amount)));
    }

    public R inputNBT(GTRecipeInput input, NBTMatcher matcher, NBTCondition condition) {
        if (input.getAmount() < 0) {
            GTLog.logger.error("Count cannot be less than 0. Actual: {}.", input.getAmount(), new Throwable());
            return (R) this;
        }
        if (matcher == null) {
            GTLog.logger.error("NBTMatcher must not be null", new Throwable());
            return (R) this;
        }
        if (condition == null) {
            GTLog.logger.error("NBTCondition must not be null", new Throwable());
            return (R) this;
        }
        this.inputs.add(input.setNBTMatchingCondition(matcher, condition));
        return (R) this;
    }

    public R inputNBT(String oredict, NBTMatcher matcher, NBTCondition condition) {
        return inputNBT(new GTRecipeOreInput(oredict), matcher, condition);
    }

    public R inputNBT(String oredict, int count, NBTMatcher matcher, NBTCondition condition) {
        return inputNBT(new GTRecipeOreInput(oredict, count), matcher, condition);
    }

    public R inputNBT(OrePrefix orePrefix, Material material, NBTMatcher matcher, NBTCondition condition) {
        return inputNBT(new GTRecipeOreInput(orePrefix, material), matcher, condition);
    }

    public R inputNBT(OrePrefix orePrefix, Material material, int count, NBTMatcher matcher, NBTCondition condition) {
        return inputNBT(new GTRecipeOreInput(orePrefix, material, count), matcher, condition);
    }

    public R inputNBT(Item item, NBTMatcher matcher, NBTCondition condition) {
        return inputNBT(new ItemStack(item), matcher, condition);
    }

    public R inputNBT(Item item, int count, NBTMatcher matcher, NBTCondition condition) {
        return inputNBT(new ItemStack(item, count), matcher, condition);
    }

    public R inputNBT(Item item, int count, int meta, NBTMatcher matcher, NBTCondition condition) {
        return inputNBT(new ItemStack(item, count, meta), matcher, condition);
    }

    public R inputNBT(Item item, int count, @SuppressWarnings("unused") boolean wild, NBTMatcher matcher,
                      NBTCondition condition) {
        return inputNBT(new ItemStack(item, count, GTValues.W), matcher, condition);
    }

    public R inputNBT(Block block, NBTMatcher matcher, NBTCondition condition) {
        return inputNBT(block, 1, matcher, condition);
    }

    public R inputNBT(Block block, int count, NBTMatcher matcher, NBTCondition condition) {
        return inputNBT(new ItemStack(block, count), matcher, condition);
    }

    public R inputNBT(Block block, int count, @SuppressWarnings("unused") boolean wild, NBTMatcher matcher,
                      NBTCondition condition) {
        return inputNBT(new ItemStack(block, count, GTValues.W), matcher, condition);
    }

    public R inputNBT(MetaItem<?>.MetaValueItem item, int count, NBTMatcher matcher, NBTCondition condition) {
        return inputNBT(item.getStackForm(count), matcher, condition);
    }

    public R inputNBT(MetaItem<?>.MetaValueItem item, NBTMatcher matcher, NBTCondition condition) {
        return inputNBT(item.getStackForm(), matcher, condition);
    }

    public R inputNBT(MetaTileEntity mte, NBTMatcher matcher, NBTCondition condition) {
        return inputNBT(mte.getStackForm(), matcher, condition);
    }

    public R inputNBT(MetaTileEntity mte, int amount, NBTMatcher matcher, NBTCondition condition) {
        return inputNBT(mte.getStackForm(amount), matcher, condition);
    }

    /**
     * NBT tags are stripped from the input stack and are not automatically checked.
     *
     * @param stack     the itemstack to input.
     * @param matcher   the matcher for the stack's nbt
     * @param condition the condition for the stack's nbt
     * @return this
     */
    public R inputNBT(@NotNull ItemStack stack, NBTMatcher matcher, NBTCondition condition) {
        return inputNBT(new GTRecipeItemInput(stack), matcher, condition);
    }

    public R inputs(ItemStack input) {
        if (input == null || input.isEmpty()) {
            GTLog.logger.error("Input cannot be null or empty. Input: {}", input, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
        } else {
            this.inputs.add(new GTRecipeItemInput(input));
        }
        return (R) this;
    }

    public R inputs(ItemStack... inputs) {
        for (ItemStack input : inputs) {
            if (input == null || input.isEmpty()) {
                GTLog.logger.error("Inputs cannot contain null or empty ItemStacks. Inputs: {}", input,
                        new Throwable());
                recipeStatus = EnumValidationResult.INVALID;
                continue;
            }
            this.inputs.add(new GTRecipeItemInput(input));
        }
        return (R) this;
    }

    public R inputStacks(Collection<ItemStack> inputs) {
        for (ItemStack input : inputs) {
            if (input == null || input.isEmpty()) {
                GTLog.logger.error("Input cannot contain null or empty ItemStacks. Inputs: {}", input, new Throwable());
                recipeStatus = EnumValidationResult.INVALID;
                continue;
            }
            this.inputs.add(new GTRecipeItemInput(input));
        }
        return (R) this;
    }

    public R inputs(GTRecipeInput input) {
        if (input.getAmount() < 0) {
            GTLog.logger.error("Input count cannot be less than 0. Actual: {}.", input.getAmount(), new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
        } else {
            this.inputs.add(input);
        }
        return (R) this;
    }

    public R inputs(GTRecipeInput... inputs) {
        for (GTRecipeInput input : inputs) {
            if (input.getAmount() < 0) {
                GTLog.logger.error("Input count cannot be less than 0. Actual: {}.", input.getAmount(),
                        new Throwable());
                recipeStatus = EnumValidationResult.INVALID;
                continue;
            }
            this.inputs.add(input);
        }
        return (R) this;
    }

    public R inputIngredients(Collection<GTRecipeInput> inputs) {
        for (GTRecipeInput input : inputs) {
            if (input.getAmount() < 0) {
                GTLog.logger.error("Count cannot be less than 0. Actual: {}.", input.getAmount(), new Throwable());
                recipeStatus = EnumValidationResult.INVALID;
                continue;
            }
            this.inputs.add(input);
        }
        return (R) this;
    }

    public R clearInputs() {
        this.inputs.clear();
        return (R) this;
    }

    public R notConsumable(GTRecipeInput gtRecipeIngredient) {
        return input(gtRecipeIngredient.setNonConsumable());
    }

    public R notConsumable(ItemStack itemStack) {
        return input(new GTRecipeItemInput(itemStack).setNonConsumable());
    }

    public R notConsumable(OrePrefix prefix, Material material, int amount) {
        return input(new GTRecipeOreInput(prefix, material, amount).setNonConsumable());
    }

    public R notConsumable(OrePrefix prefix, Material material) {
        return notConsumable(prefix, material, 1);
    }

    public R notConsumable(MetaItem<?>.MetaValueItem item) {
        return input(new GTRecipeItemInput(item.getStackForm(), 1).setNonConsumable());
    }

    public R notConsumable(Fluid fluid, int amount) {
        return fluidInputs(new GTRecipeFluidInput(fluid, amount).setNonConsumable());
    }

    public R notConsumable(Fluid fluid) {
        return fluidInputs(new GTRecipeFluidInput(fluid, 1).setNonConsumable());
    }

    public R notConsumable(FluidStack fluidStack) {
        return fluidInputs(new GTRecipeFluidInput(fluidStack).setNonConsumable());
    }

    public R circuitMeta(int circuitNumber) {
        if (IntCircuitIngredient.CIRCUIT_MIN > circuitNumber || circuitNumber > IntCircuitIngredient.CIRCUIT_MAX) {
            GTLog.logger.error("Integrated Circuit Number cannot be less than {} and more than {}",
                    IntCircuitIngredient.CIRCUIT_MIN, IntCircuitIngredient.CIRCUIT_MAX, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return (R) this;
        }
        return input(IntCircuitIngredient.circuitInput(circuitNumber));
    }

    public R output(OrePrefix orePrefix, Material material) {
        return outputs(OreDictUnifier.get(orePrefix, material, 1));
    }

    public R output(OrePrefix orePrefix, Material material, int count) {
        return outputs(OreDictUnifier.get(orePrefix, material, count));
    }

    public R output(Item item) {
        return output(item, 1);
    }

    public R output(Item item, int count) {
        return outputs(new ItemStack(item, count));
    }

    public R output(Item item, int count, int meta) {
        return outputs(new ItemStack(item, count, meta));
    }

    public R output(Block item) {
        return output(item, 1);
    }

    public R output(Block item, int count) {
        return outputs(new ItemStack(item, count));
    }

    public R output(MetaItem<?>.MetaValueItem item, int count) {
        return outputs(item.getStackForm(count));
    }

    public R output(MetaItem<?>.MetaValueItem item) {
        return output(item, 1);
    }

    public R output(MetaTileEntity mte) {
        return output(mte, 1);
    }

    public R output(MetaTileEntity mte, int amount) {
        return outputs(mte.getStackForm(amount));
    }

    public R outputs(ItemStack output) {
        if (output != null && !output.isEmpty()) {
            this.outputs.add(output);
        }
        return (R) this;
    }

    public R outputs(ItemStack... outputs) {
        return outputs(Arrays.asList(outputs));
    }

    public R outputs(Collection<ItemStack> outputs) {
        outputs = new ArrayList<>(outputs);
        outputs.removeIf(stack -> stack == null || stack.isEmpty());
        this.outputs.addAll(outputs);
        return (R) this;
    }

    public R clearOutputs() {
        this.outputs.clear();
        return (R) this;
    }

    public R fluidInputs(Collection<GTRecipeInput> fluidIngredients) {
        this.fluidInputs.addAll(fluidIngredients);
        return (R) this;
    }

    public R fluidInputs(GTRecipeInput fluidIngredient) {
        this.fluidInputs.add(fluidIngredient);
        return (R) this;
    }

    public R fluidInputs(FluidStack input) {
        if (input != null && input.amount > 0) {
            this.fluidInputs.add(new GTRecipeFluidInput(input));
        } else if (input != null) {
            GTLog.logger.error("Fluid Input count cannot be less than 0. Actual: {}.", input.amount, new Throwable());
        } else {
            GTLog.logger.error("FluidStack cannot be null.");
        }
        return (R) this;
    }

    public R fluidInputs(FluidStack... fluidStacks) {
        ArrayList<GTRecipeInput> fluidIngredients = new ArrayList<>();
        for (FluidStack fluidStack : fluidStacks) {
            if (fluidStack != null && fluidStack.amount > 0) {
                fluidIngredients.add(new GTRecipeFluidInput(fluidStack));
            } else if (fluidStack != null) {
                GTLog.logger.error("Fluid Input count cannot be less than 0. Actual: {}.", fluidStack.amount,
                        new Throwable());
            } else {
                GTLog.logger.error("FluidStack cannot be null.");
            }
        }
        this.fluidInputs.addAll(fluidIngredients);
        return (R) this;
    }

    public R clearFluidInputs() {
        this.fluidInputs.clear();
        return (R) this;
    }

    public R fluidOutputs(FluidStack output) {
        if (output != null && output.amount > 0) {
            this.fluidOutputs.add(output);
        }
        return (R) this;
    }

    public R fluidOutputs(FluidStack... outputs) {
        return fluidOutputs(Arrays.asList(outputs));
    }

    public R fluidOutputs(Collection<FluidStack> outputs) {
        outputs = new ArrayList<>(outputs);
        outputs.removeIf(o -> o == null || o.amount <= 0);
        this.fluidOutputs.addAll(outputs);
        return (R) this;
    }

    public R clearFluidOutputs() {
        this.fluidOutputs.clear();
        return (R) this;
    }

    public R chancedOutput(ItemStack stack, int chance, int tierChanceBoost) {
        if (stack == null || stack.isEmpty()) {
            return (R) this;
        }
        if (0 >= chance || chance > ChancedOutputLogic.getMaxChancedValue()) {
            GTLog.logger.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                    ChancedOutputLogic.getMaxChancedValue(), chance, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return (R) this;
        }
        this.chancedOutputs.add(new ChancedItemOutput(stack.copy(), chance, tierChanceBoost));
        return (R) this;
    }

    public R chancedOutput(OrePrefix prefix, Material material, int count, int chance, int tierChanceBoost) {
        return chancedOutput(OreDictUnifier.get(prefix, material, count), chance, tierChanceBoost);
    }

    public R chancedOutput(OrePrefix prefix, Material material, int chance, int tierChanceBoost) {
        return chancedOutput(prefix, material, 1, chance, tierChanceBoost);
    }

    public R chancedOutput(MetaItem<?>.MetaValueItem item, int count, int chance, int tierChanceBoost) {
        return chancedOutput(item.getStackForm(count), chance, tierChanceBoost);
    }

    public R chancedOutput(MetaItem<?>.MetaValueItem item, int chance, int tierChanceBoost) {
        return chancedOutput(item, 1, chance, tierChanceBoost);
    }

    public R chancedOutput(ItemStack stack, String fraction, int tierChanceBoost) {
        if (stack == null || stack.isEmpty()) {
            return (R) this;
        }

        String[] split = fraction.split("/");
        if (split.length != 2) {
            GTLog.logger.error("Fraction was not parsed correctly! Expected format is \"1/3\". Actual: \"{}\".",
                    fraction, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return (R) this;
        }

        int chance;
        int maxChance;
        try {
            chance = Integer.parseInt(split[0]);
            maxChance = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            GTLog.logger.error("Fraction was not parsed correctly! Expected format is \"1/3\". Actual: \"{}\".",
                    fraction, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return (R) this;
        }

        if (0 >= chance || chance > ChancedOutputLogic.getMaxChancedValue()) {
            GTLog.logger.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                    ChancedOutputLogic.getMaxChancedValue(), chance, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return (R) this;
        }
        if (chance >= maxChance || maxChance > ChancedOutputLogic.getMaxChancedValue()) {
            GTLog.logger.error("Max Chance cannot be less or equal to Chance or more than {}. Actual: {}.",
                    ChancedOutputLogic.getMaxChancedValue(), maxChance, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return (R) this;
        }

        int scalar = Math.floorDiv(ChancedOutputLogic.getMaxChancedValue(), maxChance);
        chance *= scalar;
        maxChance *= scalar;

        this.chancedOutputs.add(new ChancedItemOutput(stack.copy(), chance, maxChance, tierChanceBoost));
        return (R) this;
    }

    public R chancedOutput(OrePrefix prefix, Material material, int count, String fraction, int tierChanceBoost) {
        return chancedOutput(OreDictUnifier.get(prefix, material, count), fraction, tierChanceBoost);
    }

    public R chancedOutput(OrePrefix prefix, Material material, String fraction, int tierChanceBoost) {
        return chancedOutput(prefix, material, 1, fraction, tierChanceBoost);
    }

    public R chancedOutput(MetaItem<?>.MetaValueItem item, int count, String fraction, int tierChanceBoost) {
        return chancedOutput(item.getStackForm(count), fraction, tierChanceBoost);
    }

    public R chancedOutput(MetaItem<?>.MetaValueItem item, String fraction, int tierChanceBoost) {
        return chancedOutput(item, 1, fraction, tierChanceBoost);
    }

    public R chancedOutputs(List<ChancedItemOutput> chancedOutputs) {
        for (ChancedItemOutput output : chancedOutputs) {
            this.chancedOutputs.add(output.copy());
        }
        return (R) this;
    }

    public R clearChancedOutput() {
        this.chancedOutputs.clear();
        return (R) this;
    }

    public R chancedOutputLogic(@NotNull ChancedOutputLogic logic) {
        this.chancedOutputLogic = logic;
        return (R) this;
    }

    public R chancedFluidOutput(FluidStack stack, int chance, int tierChanceBoost) {
        if (stack == null || stack.amount == 0) {
            return (R) this;
        }
        if (0 >= chance || chance > ChancedOutputLogic.getMaxChancedValue()) {
            GTLog.logger.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                    ChancedOutputLogic.getMaxChancedValue(), chance, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return (R) this;
        }
        this.chancedFluidOutputs.add(new ChancedFluidOutput(stack.copy(), chance, tierChanceBoost));
        return (R) this;
    }

    public R chancedFluidOutput(FluidStack stack, String fraction, int tierChanceBoost) {
        if (stack == null || stack.amount == 0) {
            return (R) this;
        }

        String[] split = fraction.split("/");
        if (split.length != 2) {
            GTLog.logger.error("Fraction was not parsed correctly! Expected format is \"1/3\". Actual: \"{}\".",
                    fraction, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return (R) this;
        }

        int chance;
        int maxChance;
        try {
            chance = Integer.parseInt(split[0]);
            maxChance = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            GTLog.logger.error("Fraction was not parsed correctly! Expected format is \"1/3\". Actual: \"{}\".",
                    fraction, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return (R) this;
        }

        if (0 >= chance || chance > ChancedOutputLogic.getMaxChancedValue()) {
            GTLog.logger.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                    ChancedOutputLogic.getMaxChancedValue(), chance, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return (R) this;
        }
        if (chance >= maxChance || maxChance > ChancedOutputLogic.getMaxChancedValue()) {
            GTLog.logger.error("Max Chance cannot be less or equal to Chance or more than {}. Actual: {}.",
                    ChancedOutputLogic.getMaxChancedValue(), maxChance, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return (R) this;
        }

        int scalar = Math.floorDiv(ChancedOutputLogic.getMaxChancedValue(), maxChance);
        chance *= scalar;
        maxChance *= scalar;

        this.chancedFluidOutputs.add(new ChancedFluidOutput(stack.copy(), chance, maxChance, tierChanceBoost));
        return (R) this;
    }

    public R chancedFluidOutputs(List<ChancedFluidOutput> chancedOutputs) {
        for (ChancedFluidOutput output : chancedOutputs) {
            this.chancedFluidOutputs.add(output.copy());
        }
        return (R) this;
    }

    public R clearChancedFluidOutputs() {
        this.chancedFluidOutputs.clear();
        return (R) this;
    }

    public R chancedFluidOutputLogic(@NotNull ChancedOutputLogic logic) {
        this.chancedFluidOutputLogic = logic;
        return (R) this;
    }

    @Optional.Method(modid = Mods.Names.GROOVY_SCRIPT)
    public R inputs(IIngredient ingredient) {
        return input(ofGroovyIngredient(ingredient));
    }

    @Optional.Method(modid = Mods.Names.GROOVY_SCRIPT)
    public R inputs(IIngredient... ingredients) {
        for (IIngredient ingredient : ingredients) {
            inputs(ingredient);
        }
        return (R) this;
    }

    @Optional.Method(modid = Mods.Names.GROOVY_SCRIPT)
    public R inputs(Collection<IIngredient> ingredients) {
        for (IIngredient ingredient : ingredients) {
            inputs(ingredient);
        }
        return (R) this;
    }

    @Optional.Method(modid = Mods.Names.GROOVY_SCRIPT)
    public R notConsumable(IIngredient ingredient) {
        return notConsumable(ofGroovyIngredient(ingredient));
    }

    @Optional.Method(modid = Mods.Names.GROOVY_SCRIPT)
    private static GTRecipeInput ofGroovyIngredient(IIngredient ingredient) {
        if (ingredient instanceof OreDictIngredient) {
            return new GTRecipeOreInput(((OreDictIngredient) ingredient).getOreDict(), ingredient.getAmount());
        }
        Object oIngredient = ingredient;
        if (oIngredient instanceof ItemStack) {
            return new GTRecipeItemInput((ItemStack) oIngredient);
        }
        if (ingredient instanceof FluidStack) {
            return new GTRecipeFluidInput((FluidStack) ingredient);
        }
        throw new IllegalArgumentException("Could not add groovy ingredient " + ingredient + " to recipe!");
    }

    /**
     * Copies the chanced outputs of a Recipe numberOfOperations times, so every chanced output
     * gets an individual roll, instead of an all or nothing situation
     *
     * @param chancedOutputsFrom The original recipe before any parallel multiplication
     * @param numberOfOperations The number of parallel operations that have been performed
     */

    public void chancedOutputsMultiply(Recipe chancedOutputsFrom, int numberOfOperations) {
        for (ChancedItemOutput entry : chancedOutputsFrom.getChancedOutputs().getChancedEntries()) {
            String fraction = String.format("%d/%d", entry.getChance(), entry.getMaxChance());
            int boost = entry.getChanceBoost();

            // Add individual chanced outputs per number of parallel operations performed, to mimic regular recipes.
            // This is done instead of simply batching the chanced outputs by the number of parallel operations
            // performed
            for (int i = 0; i < numberOfOperations; i++) {
                this.chancedOutput(entry.getIngredient().copy(), fraction, boost);
            }
        }
        for (ChancedFluidOutput entry : chancedOutputsFrom.getChancedFluidOutputs().getChancedEntries()) {
            String fraction = String.format("%d/%d", entry.getChance(), entry.getMaxChance());
            int boost = entry.getChanceBoost();

            // Add individual chanced outputs per number of parallel operations performed, to mimic regular recipes.
            // This is done instead of simply batching the chanced outputs by the number of parallel operations
            // performed
            for (int i = 0; i < numberOfOperations; i++) {
                this.chancedFluidOutput(entry.getIngredient().copy(), fraction, boost);
            }
        }
    }

    /**
     * Appends the passed {@link Recipe} onto the inputs and outputs, multiplied by the amount specified by multiplier
     * The duration of the multiplied {@link Recipe} is also added to the current duration
     *
     * @param recipe           The Recipe to be multiplied
     * @param multiplier       Amount to multiply the recipe by
     * @param multiplyDuration Whether duration should be multiplied instead of EUt
     * @return the builder holding the multiplied recipe
     */

    public R append(Recipe recipe, int multiplier, boolean multiplyDuration) {
        for (Map.Entry<RecipeProperty<?>, Object> property : recipe.propertyStorage().entrySet()) {
            this.applyPropertyCT(property.getKey().getKey(), property.getValue());
        }

        // Create holders for the various parts of the new multiplied Recipe
        List<GTRecipeInput> newRecipeInputs = new ArrayList<>();
        List<GTRecipeInput> newFluidInputs = new ArrayList<>();
        List<ItemStack> outputItems = new ArrayList<>();
        List<FluidStack> outputFluids = new ArrayList<>();

        // Populate the various holders of the multiplied Recipe
        multiplyInputsAndOutputs(newRecipeInputs, newFluidInputs, outputItems, outputFluids, recipe, multiplier);

        // Build the new Recipe with multiplied components
        this.inputIngredients(newRecipeInputs);
        this.fluidInputs(newFluidInputs);

        this.outputs(outputItems);
        this.fluidOutputs(outputFluids);

        chancedOutputsMultiply(recipe, multiplier);

        this.EUt(multiplyDuration ? recipe.getEUt() : this.EUt + recipe.getEUt() * multiplier);
        this.duration(multiplyDuration ? this.duration + recipe.getDuration() * multiplier : recipe.getDuration());
        if (this.parallel == 0) {
            this.parallel = multiplier;
        } else if (multiplyDuration) {
            this.parallel += multiplier;
        } else {
            this.parallel *= multiplier;
        }

        return (R) this;
    }

    protected static void multiplyInputsAndOutputs(List<GTRecipeInput> newRecipeInputs,
                                                   List<GTRecipeInput> newFluidInputs,
                                                   List<ItemStack> outputItems,
                                                   List<FluidStack> outputFluids,
                                                   Recipe recipe,
                                                   int numberOfOperations) {
        recipe.getInputs().forEach(ri -> {
            if (ri.isNonConsumable()) {
                newRecipeInputs.add(ri);
            } else {
                newRecipeInputs.add(ri.copyWithAmount(ri.getAmount() * numberOfOperations));
            }
        });

        recipe.getFluidInputs().forEach(fi -> {
            if (fi.isNonConsumable()) {
                newFluidInputs.add(fi);
            } else {
                newFluidInputs.add(fi.copyWithAmount(fi.getAmount() * numberOfOperations));
            }
        });

        recipe.getOutputs().forEach(itemStack -> outputItems.add(copyItemStackWithCount(itemStack,
                itemStack.getCount() * numberOfOperations)));

        recipe.getFluidOutputs().forEach(fluidStack -> outputFluids.add(copyFluidStackWithAmount(fluidStack,
                fluidStack.amount * numberOfOperations)));
    }

    public int getParallel() {
        return parallel;
    }

    protected static ItemStack copyItemStackWithCount(ItemStack itemStack, int count) {
        ItemStack itemCopy = itemStack.copy();
        itemCopy.setCount(count);
        return itemCopy;
    }

    protected static FluidStack copyFluidStackWithAmount(FluidStack fluidStack, int count) {
        FluidStack fluidCopy = fluidStack.copy();
        fluidCopy.amount = count;
        return fluidCopy;
    }

    public R duration(int duration) {
        this.duration = duration;
        return (R) this;
    }

    public R EUt(long EUt) {
        this.EUt = EUt;
        return (R) this;
    }

    public R hidden() {
        this.hidden = true;
        return (R) this;
    }

    public R category(@NotNull GTRecipeCategory category) {
        this.category = category;
        return (R) this;
    }

    public R isCTRecipe() {
        this.isCTRecipe = true;
        return (R) this;
    }

    public R setRecipeMap(RecipeMap<R> recipeMap) {
        this.recipeMap = recipeMap;
        return (R) this;
    }

    public R copy() {
        return (R) new RecipeBuilder<>(this);
    }

    /**
     * Only use if you absolutely don't want the recipe to be run through any build actions.
     * Instead, you should blacklist specific actions with {@link #ignoreBuildAction(ResourceLocation)}
     */
    public R ignoreAllBuildActions() {
        this.ignoreAllBuildActions = true;
        return (R) this;
    }

    public R ignoreBuildAction(ResourceLocation buildActionName) {
        if (ignoredBuildActions == null) {
            ignoredBuildActions = new Object2ObjectOpenHashMap<>();
        } else if (!recipeMap.getBuildActions().containsKey(buildActionName)) {
            GTLog.logger.error("Recipe map {} does not contain build action {}!", recipeMap, buildActionName,
                    new Throwable());
            return (R) this;
        } else if (ignoredBuildActions.containsKey(buildActionName)) {
            return (R) this;
        }

        ignoredBuildActions.put(buildActionName, recipeMap.getBuildActions().get(buildActionName));

        return (R) this;
    }

    public ValidationResult<Recipe> build() {
        EnumValidationResult result = recipePropertyStorageErrored ? EnumValidationResult.INVALID : validate();
        return ValidationResult.newResult(result, new Recipe(inputs, outputs,
                new ChancedOutputList<>(this.chancedOutputLogic, chancedOutputs),
                fluidInputs, fluidOutputs,
                new ChancedOutputList<>(this.chancedFluidOutputLogic, chancedFluidOutputs),
                duration, EUt, hidden, isCTRecipe, recipePropertyStorage, category));
    }

    protected EnumValidationResult validate() {
        if (GroovyScriptModule.isCurrentlyRunning()) {
            GroovyLog.Msg msg = GroovyLog.msg("Error adding GregTech " + recipeMap.unlocalizedName + " recipe").error();
            validateGroovy(msg);
            return msg.postIfNotEmpty() ? EnumValidationResult.SKIP : EnumValidationResult.VALID;
        }
        if (EUt == 0) {
            GTLog.logger.error("EU/t cannot be equal to 0", new Throwable());
            if (isCTRecipe) {
                CraftTweakerAPI.logError("EU/t cannot be equal to 0", new Throwable());
            }
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (duration <= 0) {
            GTLog.logger.error("Duration cannot be less or equal to 0", new Throwable());
            if (isCTRecipe) {
                CraftTweakerAPI.logError("Duration cannot be less or equal to 0", new Throwable());
            }
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (recipeMap != null) { // recipeMap can be null in tests
            if (category == null) {
                GTLog.logger.error("Recipes must have a category", new Throwable());
                if (isCTRecipe) {
                    CraftTweakerAPI.logError("Recipes must have a category", new Throwable());
                }
                recipeStatus = EnumValidationResult.INVALID;
            } else if (category.getRecipeMap() != this.recipeMap) {
                GTLog.logger.error("Cannot apply Category with incompatible RecipeMap", new Throwable());
                if (isCTRecipe) {
                    CraftTweakerAPI.logError("Cannot apply Category with incompatible RecipeMap",
                            new Throwable());
                }
                recipeStatus = EnumValidationResult.INVALID;
            }
        }
        if (recipeStatus == EnumValidationResult.INVALID) {
            GTLog.logger.error("Invalid recipe, read the errors above: {}", this);
        }
        return recipeStatus;
    }

    @Optional.Method(modid = Mods.Names.GROOVY_SCRIPT)
    protected void validateGroovy(GroovyLog.Msg errorMsg) {
        errorMsg.add(EUt == 0, () -> "EU/t must not be to 0");
        errorMsg.add(duration <= 0, () -> "Duration must not be less or equal to 0");
        int maxInput = recipeMap.getMaxInputs();
        int maxOutput = recipeMap.getMaxOutputs();
        int maxFluidInput = recipeMap.getMaxFluidInputs();
        int maxFluidOutput = recipeMap.getMaxFluidOutputs();
        errorMsg.add(inputs.size() > maxInput, () -> getRequiredString(maxInput, inputs.size(), "item input"));
        errorMsg.add(outputs.size() > maxOutput, () -> getRequiredString(maxOutput, outputs.size(), "item output"));
        errorMsg.add(fluidInputs.size() > maxFluidInput,
                () -> getRequiredString(maxFluidInput, fluidInputs.size(), "fluid input"));
        errorMsg.add(fluidOutputs.size() > maxFluidOutput,
                () -> getRequiredString(maxFluidOutput, fluidOutputs.size(), "fluid output"));
    }

    @NotNull
    protected static String getRequiredString(int max, int actual, @NotNull String type) {
        if (max <= 0) {
            return "No " + type + "s allowed, but found " + actual;
        }
        String out = "Must have at most " + max + " " + type;
        if (max != 1) {
            out += "s";
        }
        out += ", but found " + actual;
        return out;
    }

    /**
     * @deprecated Obsolete. Does not need calling.
     */
    @ApiStatus.Obsolete
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    @Deprecated
    protected R invalidateOnBuildAction() {
        return (R) this;
    }

    /**
     * Build and register the recipe, if valid.
     * <strong>Do not call outside of the
     * {@link net.minecraftforge.event.RegistryEvent.Register<net.minecraft.item.crafting.IRecipe>} event for recipes.
     * </strong>
     */
    @MustBeInvokedByOverriders
    public void buildAndRegister() {
        if (!ignoreAllBuildActions) {
            for (Map.Entry<ResourceLocation, RecipeBuildAction<R>> buildAction : recipeMap.getBuildActions()
                    .entrySet()) {
                if (ignoredBuildActions != null && ignoredBuildActions.containsKey(buildAction.getKey())) {
                    continue;
                }
                buildAction.getValue().accept((R) this);
            }
        }
        ValidationResult<Recipe> validationResult = build();
        recipeMap.addRecipe(validationResult);
    }

    ///////////////////
    // Getters //
    ///////////////////

    public List<GTRecipeInput> getInputs() {
        return inputs;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    public List<ChancedItemOutput> getChancedOutputs() {
        return chancedOutputs;
    }

    public List<ChancedFluidOutput> getChancedFluidOutputs() {
        return chancedFluidOutputs;
    }

    /**
     * Similar to {@link Recipe#getAllItemOutputs()}, returns the recipe outputs and all chanced outputs
     *
     * @return A List of ItemStacks composed of the recipe outputs and chanced outputs
     */
    public List<ItemStack> getAllItemOutputs() {
        List<ItemStack> stacks = new ArrayList<>(getOutputs());

        for (ChancedItemOutput entry : this.chancedOutputs) {
            stacks.add(entry.getIngredient().copy());
        }

        return stacks;
    }

    public List<GTRecipeInput> getFluidInputs() {
        return fluidInputs;
    }

    public List<FluidStack> getFluidOutputs() {
        return fluidOutputs;
    }

    public long getEUt() {
        return EUt;
    }

    public int getDuration() {
        return duration;
    }

    public @Nullable CleanroomType getCleanroom() {
        return this.recipePropertyStorage.get(CleanroomProperty.getInstance(), null);
    }

    public boolean ignoresAllBuildActions() {
        return ignoreAllBuildActions;
    }

    /**
     * Get all ignored build actions for the recipe map.
     * 
     * @return A map of ignored build actions.
     */
    public @NotNull Map<ResourceLocation, RecipeBuildAction<R>> getIgnoredBuildActions() {
        if (ignoreAllBuildActions) {
            return recipeMap.getBuildActions();
        }

        if (ignoredBuildActions == null) {
            return Collections.emptyMap();
        }

        return ignoredBuildActions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("recipeMap", recipeMap)
                .append("inputs", inputs)
                .append("outputs", outputs)
                .append("chancedOutputs", chancedOutputs)
                .append("chancedFluidOutputs", chancedFluidOutputs)
                .append("fluidInputs", fluidInputs)
                .append("fluidOutputs", fluidOutputs)
                .append("duration", duration)
                .append("EUt", EUt)
                .append("hidden", hidden)
                .append("cleanroom", getCleanroom())
                .append("dimensions", getDimensionIDs().toString())
                .append("dimensions_blocked", getBlockedDimensionIDs().toString())
                .append("recipeStatus", recipeStatus)
                .append("ignoresBuildActions", ignoresAllBuildActions())
                .append("ignoredBuildActions", getIgnoredBuildActions())
                .toString();
    }
}
