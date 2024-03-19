package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IFilter;
import gregtech.api.capability.IFilteredFluidContainer;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MetaTileEntityFluidHatch extends MetaTileEntityMultiblockNotifiablePart
                                      implements IMultiblockAbilityPart<IFluidTank>, IControllable,
                                      IGhostSlotConfigurable {

    public static final int INITIAL_INVENTORY_SIZE = 8000;

    // only holding this for convenience
    protected final HatchFluidTank fluidTank;
    protected boolean workingEnabled;
    private GhostCircuitItemStackHandler circuitInventory;

    // export hatch-only fields
    protected boolean locked;
    @Nullable
    protected FluidStack lockedFluid;

    public MetaTileEntityFluidHatch(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
        this.fluidTank = new HatchFluidTank(getInventorySize(), this, isExportHatch);
        this.workingEnabled = true;
        initializeInventory();
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        if (!this.hasGhostCircuitInventory()) return;

        this.circuitInventory = new GhostCircuitItemStackHandler(this);
        this.circuitInventory.addNotifiableMetaTileEntity(this);
    }

    @Override
    public boolean hasGhostCircuitInventory() {
        return !isExportHatch;
    }

    @Override
    public void setGhostCircuitConfig(int config) {
        if (this.circuitInventory == null || this.circuitInventory.getCircuitValue() == config) {
            return;
        }
        this.circuitInventory.setCircuitValue(config);
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFluidHatch(metaTileEntityId, getTier(), isExportHatch);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("workingEnabled", workingEnabled);
        if (isExportHatch) {
            data.setBoolean("IsLocked", locked);
            if (locked && lockedFluid != null) {
                data.setTag("LockedFluid", lockedFluid.writeToNBT(new NBTTagCompound()));
            }
        } else {
            this.circuitInventory.write(data);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("workingEnabled")) {
            this.workingEnabled = data.getBoolean("workingEnabled");
        }
        if (data.hasKey("ContainerInventory")) {
            MetaTileEntityQuantumTank.legacyTankItemHandlerNBTReading(this, data.getCompoundTag("ContainerInventory"),
                    0, 1);
        }
        if (isExportHatch) {
            this.locked = data.getBoolean("IsLocked");
            this.lockedFluid = this.locked ? FluidStack.loadFluidStackFromNBT(data.getCompoundTag("LockedFluid")) :
                    null;
        } else {
            this.circuitInventory.read(data);
        }
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (workingEnabled) {
                fillContainerFromInternalTank(fluidTank);
                if (isExportHatch) {
                    pushFluidsIntoNearbyHandlers(getFrontFacing());
                } else {
                    fillInternalTankFromFluidContainer(fluidTank);
                    pullFluidsFromNearbyHandlers(getFrontFacing());
                }
            }
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(workingEnabled);
        if (isExportHatch) {
            buf.writeBoolean(locked);
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
        if (isExportHatch) {
            this.locked = buf.readBoolean();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
        } else if (dataId == GregtechDataCodes.LOCK_FILL) {
            this.lockedFluid = NetworkUtils.readFluidStack(buf);
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = isExportHatch ? Textures.PIPE_OUT_OVERLAY : Textures.PIPE_IN_OVERLAY;
            renderer.renderSided(getFrontFacing(), renderState, translation, pipeline);
            SimpleOverlayRenderer overlay = isExportHatch ? Textures.FLUID_HATCH_OUTPUT_OVERLAY :
                    Textures.FLUID_HATCH_INPUT_OVERLAY;
            overlay.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    protected int getInventorySize() {
        return INITIAL_INVENTORY_SIZE * Math.min(Integer.MAX_VALUE, 1 << getTier());
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return isExportHatch ? new FluidTankList(false) : new FluidTankList(false, fluidTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return isExportHatch ? new FluidTankList(false, fluidTank) : new FluidTankList(false);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new FilteredItemHandler(this, 1).setFillPredicate(
                FilteredItemHandler.getCapabilityFilter(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, 1);
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return isExportHatch ? MultiblockAbility.EXPORT_FLUIDS : MultiblockAbility.IMPORT_FLUIDS;
    }

    @Override
    public @NotNull List<MultiblockAbility<?>> getAbilities() {
        return isExportHatch ?
                Collections.singletonList(MultiblockAbility.EXPORT_FLUIDS) :
                Arrays.asList(MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.IMPORT_ITEMS);
    }

    @Override
    public @NotNull List<?> registerAbilities(@NotNull MultiblockAbility<Object> key) {
        if (key.equals(MultiblockAbility.EXPORT_FLUIDS) || key.equals(MultiblockAbility.IMPORT_FLUIDS)) {
            return Collections.singletonList(fluidTank);
        } else if (key.equals(MultiblockAbility.IMPORT_ITEMS)) {
            return Collections.singletonList(circuitInventory);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        var fluidSyncHandler = GTFluidSlot.sync(fluidTank)
                .showAmount(false)
                .accessibility(true, !isExportHatch)
                .handleLocking(() -> this.lockedFluid, fluidStack -> {
                    setLocked(fluidStack != null);
                    this.lockedFluid = fluidStack;
                    this.fluidTank.onContentsChanged();
                }, this::setLocked);

        return GTGuis.createPanel(this, 176, 166)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(6, 6))

                // export specific
                .childIf(isExportHatch, new ItemSlot()
                        .pos(90, 44)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.OUT_SLOT_OVERLAY)
                        .slot(new ModularSlot(exportItems, 0)
                                .accessibility(false, true)))
                .childIf(isExportHatch, new ToggleButton()
                        .pos(7, 63)
                        .overlay(GTGuiTextures.BUTTON_LOCK)
                        // todo doing things this way causes flickering if it fails
                        // due to sync value cache
                        .value(new BooleanSyncValue(this::isLocked, b -> fluidSyncHandler.lockFluid(b, false)))
                        .addTooltip(true, IKey.lang("gregtech.gui.fluid_lock.tooltip.enabled"))
                        .addTooltip(false, IKey.lang("gregtech.gui.fluid_lock.tooltip.disabled")))
//        if (this.circuitInventory != null) {
//            SlotWidget circuitSlot = new GhostCircuitSlotWidget(circuitInventory, 0, 124, 62)
//                    .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY);
//            builder.widget(circuitSlot.setConsumer(slotWidget -> {
//                String configString;
//                if (circuitInventory == null ||
//                        circuitInventory.getCircuitValue() == GhostCircuitItemStackHandler.NO_CONFIG) {
//                    configString = new TextComponentTranslation("gregtech.gui.configurator_slot.no_value")
//                            .getFormattedText();
//                } else {
//                    configString = String.valueOf(circuitInventory.getCircuitValue());
//                }
//
//                slotWidget.setTooltipText("gregtech.gui.configurator_slot.tooltip", configString);
//            }));
//        }
                // import specific
                .childIf(!isExportHatch, GTGuiTextures.TANK_ICON.asWidget()
                        .pos(91, 36)
                        .size(14, 15))
                .childIf(!isExportHatch, new ItemSlot()
                        .pos(90, 53)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.OUT_SLOT_OVERLAY)
                        .slot(new ModularSlot(exportItems, 0)
                                .accessibility(false, true)))

                // common ui
                .child(new RichTextWidget()
                        .size(81 - 6, (isExportHatch ? 46 : 55) - 8)
                        // .padding(3, 4)
                        .background(GTGuiTextures.DISPLAY.asIcon().size(81, isExportHatch ? 46 : 55))
                        .pos(7 + 3, 16 + 4)
                        .textColor(Color.WHITE.main)
                        .alignment(Alignment.TopLeft)
                        .autoUpdate(true)
                        .textBuilder(richText -> {
                            richText.addLine(IKey.lang("gregtech.gui.fluid_amount"));
                            String name = fluidSyncHandler.getFluidLocalizedName();
                            if (name == null) return;
                            if (name.length() > 25) name = name.substring(0, 25) + "...";

                            richText.addLine(IKey.str(name));
                            richText.addLine(IKey.str(fluidSyncHandler.getFormattedFluidAmount()));
                        }))
                .child(new GTFluidSlot()
                        .disableBackground()
                        .pos(69, isExportHatch ? 43 : 52)
                        .size(18)
                        .syncHandler(fluidSyncHandler))
                .child(new ItemSlot()
                        .pos(90, 16)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
                        .slot(new ModularSlot(importItems, 0)
                                .singletonSlotGroup()
                                .filter(stack -> {
                                    if (!isExportHatch) return true;
                                    var h = FluidUtil.getFluidHandler(stack);
                                    if (h == null) return false;
                                    return h.getTankProperties()[0].getContents() == null;
                                })
                                .accessibility(true, true)))
                .bindPlayerInventory();
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        this.circuitInventory.addNotifiableMetaTileEntity(controllerBase);
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        this.circuitInventory.removeNotifiableMetaTileEntity(controllerBase);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        if (this.isExportHatch)
            tooltip.add(I18n.format("gregtech.machine.fluid_hatch.export.tooltip"));
        else
            tooltip.add(I18n.format("gregtech.machine.fluid_hatch.import.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", getInventorySize()));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    private boolean isLocked() {
        return this.locked;
    }

    private void setLocked(boolean locked) {
        if (!isExportHatch || this.locked == locked) return;
        this.locked = locked;

        if (!getWorld().isRemote) markDirty();
        if (locked && fluidTank.getFluid() != null) {
            this.lockedFluid = fluidTank.getFluid().copy();
            this.lockedFluid.amount = 1;
            fluidTank.onContentsChanged();
            return;
        }
        this.lockedFluid = null;
        fluidTank.onContentsChanged();
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        if (this == MetaTileEntities.FLUID_IMPORT_HATCH[0]) {
            for (var hatch : MetaTileEntities.FLUID_IMPORT_HATCH) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (var hatch : MetaTileEntities.FLUID_EXPORT_HATCH) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
        } else if (this.getClass() != MetaTileEntityFluidHatch.class) {
            // let subclasses fall through this override
            super.getSubItems(creativeTab, subItems);
        }
    }

    protected class HatchFluidTank extends NotifiableFluidTank implements IFilteredFluidContainer, IFilter<FluidStack> {

        public HatchFluidTank(int capacity, MetaTileEntity entityToNotify, boolean isExport) {
            super(capacity, entityToNotify, isExport);
        }

        @Override
        public int fillInternal(FluidStack resource, boolean doFill) {
            int accepted = super.fillInternal(resource, doFill);
            if (!isExportHatch) return accepted;
            if (doFill && locked && lockedFluid == null) {
                lockedFluid = resource.copy();
                lockedFluid.amount = 1;
                writeCustomData(GregtechDataCodes.LOCK_FILL,
                        buffer -> NetworkUtils.writeFluidStack(buffer, lockedFluid));
            }
            return accepted;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return test(fluid);
        }

        // override for visibility
        @Override
        public void onContentsChanged() {
            super.onContentsChanged();
        }

        @Nullable
        @Override
        public IFilter<FluidStack> getFilter() {
            return this;
        }

        @Override
        public boolean test(@NotNull FluidStack fluidStack) {
            if (!isExportHatch) return true;
            return !locked || lockedFluid == null || fluidStack.isFluidEqual(lockedFluid);
        }

        @Override
        public int getPriority() {
            if (!isExportHatch) return IFilter.noPriority();
            return !locked || lockedFluid == null ? IFilter.noPriority() : IFilter.whitelistPriority(1);
        }
    }
}
