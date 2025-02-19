package gregtech.api.recipes.machines;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.ui.RecipeMapUIFunction;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@ApiStatus.Internal
public class RecipeMapFormingPress extends RecipeMap<SimpleRecipeBuilder> {

    private static ItemStack NAME_MOLD = ItemStack.EMPTY;

    public RecipeMapFormingPress(@NotNull String unlocalizedName, @NotNull SimpleRecipeBuilder defaultRecipeBuilder,
                                 @NotNull RecipeMapUIFunction recipeMapUI) {
        super(unlocalizedName, defaultRecipeBuilder, recipeMapUI, 6, 1, 0, 0);
        setSound(GTSoundEvents.COMPRESSOR);
    }

    @Override
    @Nullable
    public Recipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, boolean exactVoltage) {
        Recipe recipe = super.findRecipe(voltage, inputs, fluidInputs, exactVoltage);

        // Item Mold renaming - min of 2 inputs required
        if (recipe == null && inputs.size() > 1) {
            // cache name mold target comparison stack so a new one is not made every lookup
            // cannot statically initialize as RecipeMaps are registered before items, throwing a NullPointer
            if (NAME_MOLD.isEmpty()) {
                NAME_MOLD = MetaItems.SHAPE_MOLD_NAME.getStackForm();
            }

            // find the mold and the stack to rename
            ItemStack moldStack = ItemStack.EMPTY;
            ItemStack itemStack = ItemStack.EMPTY;
            for (ItemStack inputStack : inputs) {
                // early exit
                if (!moldStack.isEmpty() && !itemStack.isEmpty()) break;

                if (moldStack.isEmpty() && inputStack.isItemEqual(NAME_MOLD)) {
                    // only valid if the name mold has a name, which is stored in the "display" sub-compound
                    if (inputStack.getTagCompound() != null &&
                            inputStack.getTagCompound().hasKey("display", Constants.NBT.TAG_COMPOUND)) {
                        moldStack = inputStack;
                    }
                } else if (itemStack.isEmpty()) {
                    itemStack = inputStack;
                }
            }

            // make the mold recipe if the two required inputs were found
            if (!moldStack.isEmpty() && moldStack.getTagCompound() != null && !itemStack.isEmpty()) {
                ItemStack output = GTUtility.copy(1, itemStack);
                output.setStackDisplayName(moldStack.getDisplayName());
                return this.recipeBuilder()
                        .notConsumable(new GTRecipeItemInput(moldStack)) // recipe is reusable as long as mold stack
                                                                         // matches
                        .inputs(GTUtility.copy(1, itemStack))
                        .outputs(output)
                        .duration(40).EUt(4)
                        .build().getResult();
            }
            return null;
        }
        return recipe;
    }

    @Override
    @NotNull
    public Iterator<Recipe> getRecipeIterator(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs,
                                              boolean exactVoltage) {
        return Collections.singleton(this.findRecipe(voltage, inputs, fluidInputs, exactVoltage)).iterator();
    }
}
