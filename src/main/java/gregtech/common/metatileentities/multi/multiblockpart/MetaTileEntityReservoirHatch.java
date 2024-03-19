package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.sync.GTFluidSyncHandler;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MetaTileEntityReservoirHatch extends MetaTileEntityMultiblockNotifiablePart
                                          implements IMultiblockAbilityPart<IFluidTank> {

    private static final int FLUID_AMOUNT = 2_000_000_000;
    private final InfiniteWaterTank fluidTank;

    public MetaTileEntityReservoirHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.EV, false);
        this.fluidTank = new InfiniteWaterTank(getInventorySize(), this);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityReservoirHatch(metaTileEntityId);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            fillContainerFromInternalTank(fluidTank);
            if (getOffsetTimer() % 20 == 0) {
                fluidTank.refillWater();
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            Textures.WATER_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            // allow both importing and exporting from the tank
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidTank);
        }
        return super.getCapability(capability, side);
    }

    private int getInventorySize() {
        return FLUID_AMOUNT;
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, fluidTank);
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
    public MultiblockAbility<IFluidTank> getAbility() {
        return MultiblockAbility.IMPORT_FLUIDS;
    }

    @Override
    public @NotNull List<?> registerAbilities(@NotNull MultiblockAbility<Object> multiblockAbility) {
        return Collections.singletonList(fluidTank);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        guiSyncManager.registerSlotGroup("item_inv", 2);

        GTFluidSyncHandler tankSyncHandler = GTFluidSlot.sync(this.fluidTank)
                .showAmount(false)
                .accessibility(true, false);

        // TODO: Change the position of the name when it's standardized.
        return GTGuis.createPanel(this, 176, 166)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory().left(7).bottom(7))
                .child(GTGuiTextures.DISPLAY.asWidget()
                        .left(7).top(16)
                        .size(81, 55))
                .child(GTGuiTextures.TANK_ICON.asWidget()
                        .left(92).top(36)
                        .size(14, 15))
                .child(new RichTextWidget()
                        .size(75, 47)
                        .pos(10, 20)
                        .textColor(Color.WHITE.main)
                        .alignment(Alignment.TopLeft)
                        .autoUpdate(true)
                        .textBuilder(richText -> {
                            richText.addLine(IKey.lang("gregtech.gui.fluid_amount"));
                            String name = tankSyncHandler.getFluidLocalizedName();
                            if (name == null) return;

                            richText.addLine(IKey.str(name));
                            richText.addLine(IKey.str(tankSyncHandler.getFormattedFluidAmount()));
                        }))
                .child(new GTFluidSlot().syncHandler(tankSyncHandler)
                        .pos(69, 52)
                        .disableBackground())
                .child(new ItemSlot().slot(SyncHandlers.itemSlot(this.importItems, 0)
                        .slotGroup("item_inv")
                        .filter(itemStack -> FluidUtil.getFluidHandler(itemStack) != null))
                        .background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
                        .pos(90, 16))
                .child(new ItemSlot().slot(SyncHandlers.itemSlot(this.exportItems, 0)
                        .slotGroup("item_inv")
                        .accessibility(false, true))
                        .background(GTGuiTextures.SLOT, GTGuiTextures.OUT_SLOT_OVERLAY)
                        .pos(90, 53));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", getInventorySize()));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    private static class InfiniteWaterTank extends NotifiableFluidTank {

        public InfiniteWaterTank(int capacity, MetaTileEntity entityToNotify) {
            super(capacity, entityToNotify, false);
            // start with the full amount
            setFluid(new FluidStack(FluidRegistry.WATER, FLUID_AMOUNT));
            // don't allow external callers to fill this tank
            setCanFill(false);
        }

        private void refillWater() {
            int fillAmount = Math.max(0, FLUID_AMOUNT - getFluidAmount());
            if (fillAmount > 0) {
                // call super since our overrides don't allow any kind of filling
                super.fillInternal(new FluidStack(FluidRegistry.WATER, fillAmount), true);
            }
        }

        @Override
        public boolean canDrainFluidType(@Nullable FluidStack fluid) {
            return fluid != null && fluid.getFluid() == FluidRegistry.WATER;
        }

        // don't allow external filling
        @Override
        public int fillInternal(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return false;
        }

        // serialization is unnecessary here, we can always recreate it completely full since it would refill anyway
        @Override
        public FluidTank readFromNBT(NBTTagCompound nbt) {
            return this;
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            return nbt;
        }
    }
}
