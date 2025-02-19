package gregtech.integration.exnihilo.recipes.ui;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.RecipeProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ui.RecipeMapUI;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

public class SieveUI<R extends RecipeMap<?>> extends RecipeMapUI<R> {

    public SieveUI(@NotNull R recipeMap) {
        super(recipeMap, false, true, false, false, false);
        setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, ProgressWidget.MoveType.VERTICAL_INVERTED);
    }

    @Override
    @NotNull
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems,
                                                 FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = new ModularUI.Builder(GuiTextures.BACKGROUND, 176, 192 + yOffset);
        builder.widget(new RecipeProgressWidget(200, 25, 50 + yOffset, 20, 20, this.progressBarTexture(),
                this.progressBarMoveType(), this.recipeMap()));
        this.addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        this.addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (this.specialTexture() != null && this.specialTexturePosition() != null) {
            this.addSpecialTexture(builder);
        }

        return builder;
    }

    @Override
    protected void addInventorySlotGroup(ModularUI.@NotNull Builder builder,
                                         @NotNull IItemHandlerModifiable itemHandler,
                                         @NotNull FluidTankList fluidHandler, boolean isOutputs, int yOffset) {
        if (isOutputs) {
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 6; x++) {
                    addSlot(builder, 61 + x * 18, y * 18, y * 6 + x, itemHandler, fluidHandler, false, true);
                }
            }
        } else {
            addSlot(builder, 17, 26, 0, itemHandler, fluidHandler, false, false);
            addSlot(builder, 35, 26, 1, itemHandler, fluidHandler, false, false);
        }
    }
}
