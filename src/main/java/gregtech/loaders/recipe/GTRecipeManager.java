package gregtech.loaders.recipe;

import gregtech.api.event.MaterialInfoEvent;
import gregtech.loaders.recipe.handlers.DecompositionRecipeHandler;
import gregtech.loaders.recipe.handlers.FluidRecipeHandler;
import gregtech.loaders.recipe.handlers.RecipeHandlerList;
import gregtech.loaders.recipe.handlers.ToolRecipeHandler;

import net.minecraftforge.common.MinecraftForge;

public final class GTRecipeManager {

    private GTRecipeManager() {/**/}

    public static void preLoad() {
        ToolRecipeHandler.initializeMetaItems();
    }

    public static void load() {
        MachineRecipeLoader.init();
        CraftingRecipeLoader.init();
        MetaTileEntityLoader.init();
        MetaTileEntityMachineRecipeLoader.init();
        RecipeHandlerList.register();
    }

    public static void loadLatest() {
        MinecraftForge.EVENT_BUS.post(new MaterialInfoEvent());
        DecompositionRecipeHandler.runRecipeGeneration();
        FluidRecipeHandler.runRecipeGeneration();
        RecyclingRecipes.init();
    }
}
