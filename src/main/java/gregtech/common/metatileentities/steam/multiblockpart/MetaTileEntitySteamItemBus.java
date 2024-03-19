package gregtech.common.metatileentities.steam.multiblockpart;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.GTGuis;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.layout.Grid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetaTileEntitySteamItemBus extends MetaTileEntityItemBus {

    private static final boolean IS_STEEL = ConfigHolder.machines.steelSteamMultiblocks;

    public MetaTileEntitySteamItemBus(ResourceLocation metaTileEntityId, boolean isExportHatch) {
        super(metaTileEntityId, 1, isExportHatch);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySteamItemBus(metaTileEntityId, isExportHatch);
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return isExportHatch ? MultiblockAbility.STEAM_EXPORT_ITEMS : MultiblockAbility.STEAM_IMPORT_ITEMS;
    }

    @Override
    public @NotNull List<?> registerAbilities(@NotNull MultiblockAbility<Object> multiblockAbility) {
        return Collections.singletonList(isExportHatch ? this.exportItems : this.importItems);
    }

    // Override base texture to have a bus with 4 slots, but ULV textures
    @Override
    public ICubeRenderer getBaseTexture() {
        MultiblockControllerBase controller = getController();
        if (controller == null)
            return IS_STEEL ? Textures.STEAM_CASING_STEEL : Textures.STEAM_CASING_BRONZE;
        return controller.getBaseTexture(this);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(TooltipHelper.BLINKING_ORANGE + I18n.format("gregtech.machine.steam_bus.tooltip"));
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = this.isExportHatch ? Textures.PIPE_OUT_OVERLAY : Textures.PIPE_IN_OVERLAY;
            renderer.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        guiSyncManager.registerSlotGroup("item_inv", 2);

        List<List<IWidget>> widgets = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            widgets.add(new ArrayList<>());
            for (int j = 0; j < 2; j++) {
                widgets.get(i)
                        .add(new ItemSlot()
                                .slot(SyncHandlers.itemSlot(isExportHatch ? exportItems : importItems, i * 2 + j)
                                        .slotGroup("item_inv")
                                        .accessibility(!isExportHatch, true)));
            }
        }

        return GTGuis.createPanel(this, 176, 166)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .bindPlayerInventory()
                .child(new Grid()
                        .top(29).height(36)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .alignX(0.5f)
                        .matrix(widgets));
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public GTGuiTheme getUITheme() {
        return IS_STEEL ? GTGuiTheme.STEEL : GTGuiTheme.BRONZE;
    }

    @Override
    public boolean hasGhostCircuitInventory() {
        return false;
    }
}
