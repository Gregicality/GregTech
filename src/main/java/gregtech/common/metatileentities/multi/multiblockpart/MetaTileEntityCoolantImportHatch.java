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
import gregtech.common.blocks.BlockFissionCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.FISSION_LOCK_UPDATE;

public class MetaTileEntityCoolantImportHatch extends MetaTileEntityMultiblockNotifiablePart
                                              implements IMultiblockAbilityPart<ICoolantHandler>, ICoolantHandler,
                                              IControllable, IFissionReactorHatch {

    private boolean workingEnabled;
    private LockableFluidTank fluidTank;
    private ICoolantStats coolant;

    public MetaTileEntityCoolantImportHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 4, false);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        this.fluidTank = new LockableFluidTank(16000, this, false);
        return new FluidTankList(false, fluidTank);
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
        return new MetaTileEntityCoolantImportHatch(metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createTankUI(fluidTank, getMetaFullName(), entityPlayer).build(getHolder(), entityPlayer);
    }

    public ModularUI.Builder createTankUI(IFluidTank fluidTank, String title, EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.defaultBuilder();
        builder.image(7, 16, 131, 55, GuiTextures.DISPLAY);
        TankWidget tankWidget = new TankWidget(fluidTank, 119, 52, 18, 18)
                .setHideTooltip(true).setAlwaysShowFull(true);
        builder.widget(tankWidget);
        builder.label(11, 20, "gregtech.gui.fluid_amount", 0xFFFFFF);
        builder.dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 0xFFFFFF);
        builder.dynamicLabel(11, 40, () -> {
            if (isLocked()) {
                return tankWidget.getFluidLocalizedName() + " " + I18n.format("gregtech.gui.locked");
            } else {
                return tankWidget.getFluidLocalizedName();
            }
        }, 0xFFFFFF);
        return builder.label(6, 6, title)
                .widget(new FluidContainerSlotWidget(importItems, 0, 140, 16, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
                .widget(new ImageWidget(141, 36, 14, 15, GuiTextures.TANK_ICON))
                .widget(new SlotWidget(exportItems, 0, 140, 53, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY))
                .bindPlayerInventory(entityPlayer.inventory);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.FLUID_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public boolean checkValidity(int depth) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        for (int i = 1; i < depth; i++) {
            if (getWorld().getBlockState(pos.move(this.frontFacing.getOpposite())) !=
                    MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.COOLANT_CHANNEL)) {
                return false;
            }
        }
        if (getWorld()
                .getTileEntity(pos.move(this.frontFacing.getOpposite())) instanceof IGregTechTileEntity gtTe) {
            return gtTe.getMetaTileEntity().metaTileEntityId.equals(MetaTileEntities.COOLANT_OUTPUT.metaTileEntityId);
        }
        return false;
    }

    @Override
    public MultiblockAbility<ICoolantHandler> getAbility() {
        return MultiblockAbility.IMPORT_COOLANT;
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
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            fillContainerFromInternalTank(fluidTank);
            fillInternalTankFromFluidContainer(fluidTank);
            pullFluidsFromNearbyHandlers(getFrontFacing());
        }
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
            fluidTank.setLock(buf.readBoolean());
        }
    }

    @Override
    public boolean isLocked() {
        return fluidTank.isLocked();
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
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
    public Fluid getLockedObject() {
        return fluidTank.getLockedObject();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.nuclear.locking.fluid"));
    }
}
