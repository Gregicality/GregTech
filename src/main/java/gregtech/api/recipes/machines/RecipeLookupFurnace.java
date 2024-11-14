package gregtech.api.recipes.machines;

import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.ingredients.GTItemIngredient;
import gregtech.api.recipes.ingredients.nbt.NBTMatcher;
import gregtech.api.recipes.lookup.RecipeLookup;
import gregtech.api.recipes.lookup.flag.ItemStackApplicatorMap;
import gregtech.api.recipes.lookup.flag.ItemStackMatchingContext;
import gregtech.api.recipes.lookup.flag.SingleFlagApplicator;

import gregtech.api.recipes.ui.RecipeMapUIFunction;
import gregtech.api.unification.OreDictUnifier;

import gregtech.core.sound.GTSoundEvents;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class RecipeLookupFurnace extends RecipeLookup {

    public static final int STANDARD_VOLTAGE = 8;
    public static final int STANDARD_DURATION = 64;

    private final RecipeBuilder<?> furnaceBuilder;

    private final ObjectArrayList<Recipe> recipesFurnace = new ObjectArrayList<>();

    private @Nullable List<Recipe> combined;

    public static <T extends RecipeBuilder<T>> RecipeMap<T> createMap(@NotNull String unlocalizedName,
                                                                      @NotNull T defaultRecipeBuilder,
                                                                      @NotNull RecipeMapUIFunction recipeMapUI) {
        return new RecipeMap<>(unlocalizedName, defaultRecipeBuilder, recipeMapUI, 1, 1, 0, 0,
                new RecipeLookupFurnace(defaultRecipeBuilder)).setSound(GTSoundEvents.FURNACE);
    }

    public RecipeLookupFurnace(RecipeBuilder<?> furnaceBuilder) {
        this.furnaceBuilder = furnaceBuilder;
    }

    @Override
    public @NotNull @UnmodifiableView List<Recipe> getAllRecipes() {
        if (combined == null) combined = new ObjectArrayList<>(recipesFurnace.size() + recipes.size());
        combined.addAll(recipesFurnace);
        combined.addAll(recipes);
        return combined;
    }

    @Override
    protected void invalidate() {
        super.invalidate();
        recipesFurnace.clear();
        combined = null;
    }

    @Override
    public void rebuild() {
        super.rebuild();
        int i = recipes.size();
        for (Map.Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
            boolean wildcard = entry.getKey().getItemDamage() == GTValues.W;
            Recipe recipe = furnaceBuilder.copy()
                    .ingredient(new FurnaceRecipeIngredient(entry.getKey(), 1,
                            wildcard ? ItemStackMatchingContext.ITEM : ItemStackMatchingContext.ITEM_DAMAGE))
                    .outputs(OreDictUnifier.getUnificated(entry.getValue()))
                    .duration(RecipeLookupFurnace.STANDARD_DURATION).volts(RecipeLookupFurnace.STANDARD_VOLTAGE)
                    .build().getResult();
            ItemStackApplicatorMap map = wildcard ? getItem() : getItemDamage();
            recipesFurnace.add(recipe);
            assert recipe.ingredientFlags == 1;
            map.getOrCreate(entry.getKey()).insertApplicator(i, new SingleFlagApplicator<>((byte) 0));
            i++;
        }
    }

    protected static final class FurnaceRecipeIngredient implements GTItemIngredient {

        private final ItemStack stack;
        private final int count;
        private final ItemStackMatchingContext context;

        public FurnaceRecipeIngredient(ItemStack stack, int count, ItemStackMatchingContext context) {
            this.stack = stack;
            this.count = count;
            this.context = context;
        }

        @Override
        public boolean matches(ItemStack stack) {
            return context.matches(this.stack, stack);
        }

        @Override
        public @Range(from = 1, to = Long.MAX_VALUE) long getRequiredCount() {
            return count;
        }

        @Override
        public @NotNull Collection<ItemStack> getMatchingStacksWithinContext(
                @NotNull ItemStackMatchingContext context) {
            if (context == this.context) return Collections.singletonList(stack);
            return Collections.emptyList();
        }

        @Override
        public @Nullable NBTMatcher getMatcher() {
            return null;
        }
    }
}
