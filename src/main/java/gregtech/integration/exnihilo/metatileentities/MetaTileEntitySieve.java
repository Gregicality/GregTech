package gregtech.integration.exnihilo.metatileentities;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;
import gregtech.integration.exnihilo.ExNihiloModule;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public class MetaTileEntitySieve extends SimpleMachineMetaTileEntity {

    public MetaTileEntitySieve(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, ExNihiloModule.SIEVE_RECIPES, Textures.SIFTER_OVERLAY, tier, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntitySieve(metaTileEntityId, getTier());
    }

    @Override
    protected ModularUI.Builder createGuiTemplate(@NotNull EntityPlayer player) {
        ModularUI.Builder builder = new ModularUI.Builder(GuiTextures.BACKGROUND, 176, 192)
                .label(5, 5, this.getMetaFullName())
                .slot(this.importItems, 0, 17, 25, GuiTextures.SLOT)
                .slot(this.importItems, 1, 35, 25, GuiTextures.SLOT)
                .progressBar(workable::getProgressPercent, 25, 50, 20, 20,
                        GuiTextures.PROGRESS_BAR_SIFT, ProgressWidget.MoveType.VERTICAL_INVERTED,
                        workable.getRecipeMap())
                .widget(new ImageWidget(25, 69, 18, 18, GuiTextures.INDICATOR_NO_ENERGY).setIgnoreColor(true)
                        .setPredicate(workable::isHasNotEnoughEnergy))
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 109);

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 6; x++) {
                builder.slot(this.exportItems, y * 6 + x, 61 + x * 18, 15 + y * 18, true, false, GuiTextures.SLOT);
            }
        }

        builder.widget(new ToggleButtonWidget(7, 87, 18, 18,
                GuiTextures.BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setAutoOutputItems)
                        .setTooltipText("gregtech.gui.item_auto_output.tooltip")
                        .shouldUseBaseBackground());

        builder.widget(new CycleButtonWidget(25, 87, 18, 18,
                workable.getAvailableOverclockingTiers(), workable::getOverclockTier, workable::setOverclockTier)
                        .setTooltipHoverString("gregtech.gui.overclock.description")
                        .setButtonTexture(GuiTextures.BUTTON_OVERCLOCK));

        return builder;
    }
}
