package gregtech.integration.exnihilo.metatileentities;

import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;
import gregtech.integration.exnihilo.ExNihiloModule;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

public class MetaTileEntitySteamSieve extends SteamMetaTileEntity {

    public MetaTileEntitySteamSieve(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, ExNihiloModule.SIEVE_RECIPES, Textures.SIFTER_OVERLAY, isHighPressure);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntitySteamSieve(metaTileEntityId, isHighPressure);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 2, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 30, this, true);
    }

    @Override
    protected ModularUI createUI(@NotNull EntityPlayer player) {
        ModularUI.Builder builder = new ModularUI.Builder(GuiTextures.BACKGROUND_STEAM.get(this.isHighPressure), 176,
                192)
                        .label(6, 6, this.getMetaFullName()).shouldColor(false)
                        .slot(this.importItems, 0, 17, 43, GuiTextures.SLOT_STEAM.get(isHighPressure))
                        .slot(this.importItems, 1, 35, 43, GuiTextures.SLOT_STEAM.get(isHighPressure))
                        .progressBar(workableHandler::getProgressPercent, 25, 68, 20, 20,
                                ExNihiloModule.PROGRESS_BAR_SIFTER_STEAM.get(isHighPressure),
                                ProgressWidget.MoveType.VERTICAL_INVERTED, workableHandler.getRecipeMap())
                        .widget((new ImageWidget(79, 77, 18, 18,
                                GuiTextures.INDICATOR_NO_STEAM.get(this.isHighPressure)))
                                        .setPredicate(() -> this.workableHandler.isHasNotEnoughEnergy()))
                        .bindPlayerInventory(player.inventory, GuiTextures.SLOT_STEAM.get(this.isHighPressure), 7, 109);

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 6; x++) {
                builder.slot(this.exportItems, y * 6 + x, 61 + x * 18, 15 + y * 18, true, false,
                        GuiTextures.SLOT_STEAM.get(isHighPressure));
            }
        }

        return builder.build(getHolder(), player);
    }
}
