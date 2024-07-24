package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.IControllable;
import gregtech.api.capability.ICoolantHandler;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.LockableFluidTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.FluidContainerSlotWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IFissionReactorHatch;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.nuclear.fission.ICoolantStats;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.FISSION_LOCK_UPDATE;

public class MetaTileEntityCoolantExportHatch extends MetaTileEntityMultiblockNotifiablePart
                                              implements IMultiblockAbilityPart<ICoolantHandler>, ICoolantHandler,
                                              IControllable, IFissionReactorHatch {

    private boolean workingEnabled;
    private LockableFluidTank fluidTank;
    private ICoolantStats coolant;

    public MetaTileEntityCoolantExportHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 4, true);
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.workingEnabled = isWorkingAllowed;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCoolantExportHatch(metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createTankUI(fluidTank, getMetaFullName(), entityPlayer).build(getHolder(), entityPlayer);
    }

    public ModularUI.Builder createTankUI(IFluidTank fluidTank, String title, EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.defaultBuilder();
        builder.image(7, 16, 81, 55, GuiTextures.DISPLAY);
        TankWidget tankWidget = new TankWidget(fluidTank, 69, 52, 18, 18)
                .setHideTooltip(true).setAlwaysShowFull(true);
        builder.widget(tankWidget);
        builder.label(11, 20, "gregtech.gui.fluid_amount", 0xFFFFFF);
        builder.dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 0xFFFFFF);
        builder.dynamicLabel(11, 40, tankWidget::getFluidLocalizedName, 0xFFFFFF);
        return builder.label(6, 6, title)
                .widget(new FluidContainerSlotWidget(importItems, 0, 90, 16, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
                .widget(new ImageWidget(91, 36, 14, 15, GuiTextures.TANK_ICON))
                .widget(new SlotWidget(exportItems, 0, 90, 53, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY))
                .bindPlayerInventory(entityPlayer.inventory);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.FLUID_HATCH_OUTPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public boolean checkValidity(int depth) {
        // Export ports are always considered valid
        return true;
    }

    @Override
    public void setLock(boolean isLocked) {
        fluidTank.setLock(isLocked);
        writeCustomData(FISSION_LOCK_UPDATE, (packetBuffer -> packetBuffer.writeBoolean(isLocked)));
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == FISSION_LOCK_UPDATE) {
            this.fluidTank.setLock(buf.readBoolean());
        }
    }

    @Override
    public boolean isLocked() {
        return fluidTank.isLocked();
    }

    @Override
    public Fluid getLockedObject() {
        return fluidTank.getLockedObject();
    }

    @Override
    public ICoolantStats getCoolant() {
        return this.coolant;
    }

    @Override
    public void setCoolant(ICoolantStats prop) {
        this.coolant = prop;
    }

    @Override
    public @NotNull LockableFluidTank getFluidTank() {
        return this.fluidTank;
    }

    @Override
    public MultiblockAbility<ICoolantHandler> getAbility() {
        return MultiblockAbility.EXPORT_COOLANT;
    }

    @Override
    public void registerAbilities(List<ICoolantHandler> abilityList) {
        abilityList.add(this);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new FilteredItemHandler(this).setFillPredicate(
                FilteredItemHandler.getCapabilityFilter(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        this.fluidTank = new LockableFluidTank(16000, this, true);
        return new FluidTankList(false, fluidTank);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            fillContainerFromInternalTank(fluidTank);
            pushFluidsIntoNearbyHandlers(getFrontFacing());
        }
    }
}
