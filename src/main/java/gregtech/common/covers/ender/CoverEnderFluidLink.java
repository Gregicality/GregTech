package gregtech.common.covers.ender;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.FluidTankSwitchShim;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.virtualregistry.EntryTypes;
import gregtech.api.util.virtualregistry.VirtualEnderRegistry;
import gregtech.api.util.virtualregistry.entries.VirtualTank;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.filter.FluidFilterContainer;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CoverEnderFluidLink extends CoverAbstractEnderLink<VirtualTank>
                                 implements CoverWithUI, ITickable, IControllable {

    public static final int TRANSFER_RATE = 8000; // mB/t

    protected CoverPump.PumpMode pumpMode = CoverPump.PumpMode.IMPORT;
    private final FluidTankSwitchShim linkedTank;
    protected final FluidFilterContainer fluidFilter;

    public CoverEnderFluidLink(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                               @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
        this.linkedTank = new FluidTankSwitchShim(this.activeEntry);
        this.fluidFilter = new FluidFilterContainer(this);
    }

    @Override
    protected void updateLink() {
        super.updateLink();
        if (this.linkedTank != null)
            this.linkedTank.changeTank(this.activeEntry);
    }

    @Override
    protected EntryTypes<VirtualTank> getType() {
        return EntryTypes.ENDER_FLUID;
    }

    @Override
    protected String identifier() {
        return "EFLink#";
    }

    public FluidFilterContainer getFluidFilterContainer() {
        return this.fluidFilter;
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.ENDER_FLUID_LINK.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public void onRemoval() {
        dropInventoryContents(fluidFilter);
    }

    @Override
    public void update() {
        if (isWorkingEnabled() && isIoEnabled()) {
            transferFluids();
        }
    }

    protected void transferFluids() {
        IFluidHandler fluidHandler = getCoverableView().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                getAttachedSide());
        if (fluidHandler == null) return;
        if (pumpMode == CoverPump.PumpMode.IMPORT) {
            GTTransferUtils.transferFluids(fluidHandler, activeEntry, TRANSFER_RATE, fluidFilter::test);
        } else if (pumpMode == CoverPump.PumpMode.EXPORT) {
            GTTransferUtils.transferFluids(activeEntry, fluidHandler, TRANSFER_RATE, fluidFilter::test);
        }
    }

    public void setPumpMode(CoverPump.PumpMode pumpMode) {
        this.pumpMode = pumpMode;
        markDirty();
    }

    public CoverPump.PumpMode getPumpMode() {
        return pumpMode;
    }

    @Override
    protected IWidget createSlotWidget(VirtualTank entry) {
        var fluidTank = GTFluidSlot.sync(entry)
                .accessibility(false, false);

        return new GTFluidSlot()
                .size(18)
                .background(GTGuiTextures.FLUID_SLOT)
                .syncHandler(fluidTank)
                .marginRight(2);
    }

    @Override
    protected void deleteEntry(UUID uuid, String name) {
        VirtualEnderRegistry.deleteEntry(uuid, getType(), name, tank -> tank.getFluidAmount() == 0);
    }

    @Override
    protected IWidget createEntrySlot() {
        return new GTFluidSlot()
                .size(18)
                .background(GTGuiTextures.FLUID_SLOT)
                .syncHandler(this.linkedTank)
                .marginRight(2);
    }

    @Override
    public @NotNull ParentWidget<?> createUI(SidedPosGuiData data, PanelSyncManager manager) {
        getFluidFilterContainer().setMaxTransferSize(1);

        var pumpMode = new EnumSyncValue<>(CoverPump.PumpMode.class, this::getPumpMode, this::setPumpMode);
        manager.syncValue("pump_mode", pumpMode);
        pumpMode.updateCacheFromSource(true);

        return super.createUI(data, manager)
                .child(getFluidFilterContainer().initUI(data, manager))
                .child(new EnumRowBuilder<>(CoverPump.PumpMode.class)
                        .value(pumpMode)
                        .overlay(GTGuiTextures.CONVEYOR_MODE_OVERLAY)
                        .lang("cover.pump.mode")
                        .build());
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("PumpMode", pumpMode.ordinal());
        tagCompound.setTag("Filter", fluidFilter.serializeNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.pumpMode = CoverPump.PumpMode.values()[tagCompound.getInteger("PumpMode")];
        this.fluidFilter.deserializeNBT(tagCompound.getCompoundTag("Filter"));
    }

    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.activeEntry);
        }
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return defaultValue;
    }
}
