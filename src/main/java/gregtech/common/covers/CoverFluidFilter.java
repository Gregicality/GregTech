package gregtech.common.covers;

import gregtech.api.capability.impl.FluidHandlerDelegate;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.covers.filter.BaseFilter;
import gregtech.common.covers.filter.BaseFilterContainer;
import gregtech.common.covers.filter.FluidFilterContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class CoverFluidFilter extends CoverBase implements CoverWithUI {

    protected final String titleLocale;
    protected final SimpleOverlayRenderer texture;
    protected final FluidFilterContainer fluidFilterContainer;
    protected FluidFilterMode filterMode;
    protected boolean allowFlow = false;
    protected FluidHandlerDelegate fluidHandler;

    public CoverFluidFilter(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                            @NotNull EnumFacing attachedSide, String titleLocale, SimpleOverlayRenderer texture) {
        super(definition, coverableView, attachedSide);
        this.fluidFilterContainer = new FluidFilterContainer(this);
        this.filterMode = FluidFilterMode.FILTER_FILL;
        this.titleLocale = titleLocale;
        this.texture = texture;
    }

    public void setFilterMode(FluidFilterMode filterMode) {
        this.filterMode = filterMode;
        this.getCoverableView().markDirty();
    }

    @Override
    public void onAttachment(@NotNull CoverableView coverableView, @NotNull EnumFacing side,
                             @Nullable EntityPlayer player, @NotNull ItemStack itemStack) {
        super.onAttachment(coverableView, side, player, itemStack);
        var dropStack = GTUtility.copy(1, itemStack);
        this.fluidFilterContainer.setFilterStack(dropStack);
    }

    @Override
    public @NotNull ItemStack getPickItem() {
        return this.fluidFilterContainer.getFilterStack();
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer packetBuffer) {
        packetBuffer.writeByte(this.filterMode.ordinal());
        packetBuffer.writeBoolean(this.fluidFilterContainer.hasFilter());
        if (this.fluidFilterContainer.hasFilter()) {
            packetBuffer.writeItemStack(this.fluidFilterContainer.getFilterStack());
        }
    }

    @Override
    public void readInitialSyncData(@NotNull AdvancedPacketBuffer packetBuffer) {
        this.filterMode = FluidFilterMode.VALUES[packetBuffer.readByte()];
        if (!packetBuffer.readBoolean()) return;
        try {
            this.fluidFilterContainer.setFilterStack(packetBuffer.readItemStack());
        } catch (IOException e) {
            GTLog.logger.error("Failed to read filter for CoverFluidFilter! %s", getPos().toString());
        }
    }

    public FluidFilterMode getFilterMode() {
        return filterMode;
    }

    public @NotNull BaseFilter getFilter() {
        var filter = getFilterContainer().getFilter();
        if (filter == null) return BaseFilter.ERROR_FILTER;

        return filter;
    }

    public @NotNull BaseFilterContainer getFilterContainer() {
        return this.fluidFilterContainer;
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getAttachedSide()) != null;
    }

    @Override
    public boolean canPipePassThrough() {
        return true;
    }

    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote) {
            this.openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager) {
        var filteringMode = new EnumSyncValue<>(FluidFilterMode.class, this::getFilterMode, this::setFilterMode);

        guiSyncManager.syncValue("filtering_mode", filteringMode);
        this.fluidFilterContainer.setMaxTransferSize(1);

        return getFilter().createPanel(guiSyncManager)
                .size(176, 212).padding(7)
                .child(CoverWithUI.createTitleRow(getFilterContainer().getFilterStack()))
                .child(Flow.column().widthRel(1f).align(Alignment.TopLeft).top(22).coverChildrenHeight()
                        .child(new EnumRowBuilder<>(FluidFilterMode.class)
                                .value(filteringMode)
                                .lang("cover.filter.mode.title")
                                .overlay(16, GTGuiTextures.FILTER_MODE_OVERLAY)
                                .build())
                        .child(Flow.row()
                                .marginBottom(2)
                                .widthRel(1f)
                                .coverChildrenHeight()
                                .setEnabledIf(b -> getFilterMode() != FluidFilterMode.FILTER_BOTH)
                                .child(new ToggleButton()
                                        .overlay(IKey.dynamic(() -> IKey.lang(allowFlow ?
                                                "cover.generic.enabled" :
                                                "cover.generic.disabled").get())
                                                .color(Color.WHITE.main).shadow(false))
                                        .tooltip(tooltip -> tooltip
                                                .addLine(IKey.lang("cover.filter.allow_flow.tooltip")))
                                        .size(72, 18)
                                        .value(new BooleanSyncValue(() -> allowFlow, b -> allowFlow = b)))
                                .child(IKey.lang("cover.filter.allow_flow.label")
                                        .asWidget()
                                        .height(18)
                                        .alignX(1f)))
                        .child(new Rectangle().setColor(UI_TEXT_COLOR).asWidget()
                                .height(1).widthRel(0.95f).margin(0, 4))
                        .child(getFilter().createWidgets(guiSyncManager)))
                .child(SlotGroupWidget.playerInventory().bottom(7).left(7));
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        this.texture.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable T defaultValue) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (defaultValue instanceof IFluidHandler delegate) {
                if (fluidHandler == null || fluidHandler.delegate != delegate) {
                    this.fluidHandler = new FluidHandlerFiltered(delegate);
                }
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
            }
            return null;
        }
        return defaultValue;
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("FilterMode", this.filterMode.ordinal());
        tagCompound.setTag("Filter", this.fluidFilterContainer.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.filterMode = FluidFilterMode.values()[tagCompound.getInteger("FilterMode")];
        if (tagCompound.hasKey("IsBlacklist")) {
            this.fluidFilterContainer.setFilterStack(getDefinition().getDropItemStack());
            this.fluidFilterContainer.handleLegacyNBT(tagCompound);
            this.fluidFilterContainer.setBlacklistFilter(tagCompound.getBoolean("IsBlacklist"));
        } else {
            this.fluidFilterContainer.deserializeNBT(tagCompound.getCompoundTag("Filter"));
        }
    }

    private class FluidHandlerFiltered extends FluidHandlerDelegate {

        public FluidHandlerFiltered(@NotNull IFluidHandler delegate) {
            super(delegate);
        }

        public int fill(FluidStack resource, boolean doFill) {
            // set to drain, but filling is allowed
            if (getFilterMode() == FluidFilterMode.FILTER_DRAIN && allowFlow)
                return super.fill(resource, doFill);

            // if set to insert or both, test the stack
            if (getFilterMode() != FluidFilterMode.FILTER_DRAIN && fluidFilterContainer.test(resource))
                return super.fill(resource, doFill);

            // otherwise fail
            return 0;
        }

        @Nullable
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            // set to fill, draining is allowed
            if (getFilterMode() == FluidFilterMode.FILTER_FILL && allowFlow)
                return super.drain(resource, doDrain);

            // if set to extract or both, test stack
            if (getFilterMode() != FluidFilterMode.FILTER_FILL && fluidFilterContainer.test(resource))
                return super.drain(resource, doDrain);

            // otherwise fail
            return null;
        }

        @Nullable
        public FluidStack drain(int maxDrain, boolean doDrain) {
            var f = super.drain(maxDrain, false);
            return drain(f, doDrain);
        }
    }
}
