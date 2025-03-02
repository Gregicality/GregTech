package gregtech.api.recipes.machines;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.ui.RecipeMapUIFunction;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@ApiStatus.Internal
public class RecipeMapScanner extends RecipeMap<SimpleRecipeBuilder> implements IScannerRecipeMap {

    private static final List<ICustomScannerLogic> CUSTOM_SCANNER_LOGICS = new ArrayList<>();

    public RecipeMapScanner(@NotNull String unlocalizedName, @NotNull SimpleRecipeBuilder defaultRecipeBuilder,
                            @NotNull RecipeMapUIFunction recipeMapUI) {
        super(unlocalizedName, defaultRecipeBuilder, recipeMapUI, 2, 1, 1, 0);
        setSound(GTSoundEvents.ELECTROLYZER);
    }

    @Override
    public @NotNull List<Recipe> getRepresentativeRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        for (ICustomScannerLogic logic : CUSTOM_SCANNER_LOGICS) {
            List<Recipe> logicRecipes = logic.getRepresentativeRecipes();
            if (logicRecipes != null && !logicRecipes.isEmpty()) {
                recipes.addAll(logicRecipes);
            }
        }
        return recipes;
    }

    /**
     *
     * @param logic A function which is passed the normal findRecipe() result. Returns null if no valid recipe for
     *              the custom logic is found,
     */
    public static void registerCustomScannerLogic(ICustomScannerLogic logic) {
        CUSTOM_SCANNER_LOGICS.add(logic);
    }

    @Override
    @Nullable
    public Recipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, boolean exactVoltage) {
        Recipe recipe = super.findRecipe(voltage, inputs, fluidInputs, exactVoltage);
        if (recipe != null) return recipe;

        for (ICustomScannerLogic logic : CUSTOM_SCANNER_LOGICS) {
            recipe = logic.createCustomRecipe(voltage, inputs, fluidInputs, exactVoltage);
            if (recipe != null) return recipe;
        }
        return null;
    }

    @Override
    @NotNull
    public Iterator<Recipe> getRecipeIterator(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs,
                                              boolean exactVoltage) {
        return Collections.singleton(this.findRecipe(voltage, inputs, fluidInputs, exactVoltage)).iterator();
    }
}
