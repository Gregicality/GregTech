package gregtech.common.covers;

import gregtech.api.capability.impl.ItemHandlerDelegate;
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
import gregtech.common.covers.filter.ItemFilterContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

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

public class CoverItemFilter extends CoverBase implements CoverWithUI {

    protected final String titleLocale;
    protected final SimpleOverlayRenderer texture;
    protected final ItemFilterContainer itemFilterContainer;
    protected ItemFilterMode filterMode = ItemFilterMode.FILTER_INSERT;
    protected boolean allowFlow = false;
    protected ItemHandlerDelegate itemHandler;

    public CoverItemFilter(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                           @NotNull EnumFacing attachedSide, String titleLocale, SimpleOverlayRenderer texture) {
        super(definition, coverableView, attachedSide);
        this.titleLocale = titleLocale;
        this.texture = texture;
        this.itemFilterContainer = new ItemFilterContainer(this);
    }

    @Override
    public void onAttachment(@NotNull CoverableView coverableView, @NotNull EnumFacing side,
                             @Nullable EntityPlayer player, @NotNull ItemStack itemStack) {
        super.onAttachment(coverableView, side, player, itemStack);
        var dropStack = GTUtility.copy(1, itemStack);
        this.itemFilterContainer.setFilterStack(dropStack);
    }

    @Override
    public @NotNull ItemStack getPickItem() {
        return this.itemFilterContainer.getFilterStack();
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer packetBuffer) {
        packetBuffer.writeBoolean(itemFilterContainer.hasFilter());
        if (itemFilterContainer.hasFilter()) {
            packetBuffer.writeByte(this.filterMode.ordinal());
            packetBuffer.writeItemStack(this.itemFilterContainer.getFilterStack());
        }
    }

    @Override
    public void readInitialSyncData(@NotNull AdvancedPacketBuffer packetBuffer) {
        if (!packetBuffer.readBoolean()) return;
        this.filterMode = ItemFilterMode.VALUES[packetBuffer.readByte()];
        try {
            this.itemFilterContainer.setFilterStack(packetBuffer.readItemStack());
        } catch (IOException e) {
            GTLog.logger.error("Failed to read filter for CoverItemFilter! %s", getPos().toString());
        }
    }

    public void setFilterMode(ItemFilterMode filterMode) {
        this.filterMode = filterMode;
        getCoverableView().markDirty();
    }

    public ItemFilterMode getFilterMode() {
        return filterMode;
    }

    public @NotNull BaseFilter getFilter() {
        var filter = getFilterContainer().getFilter();
        if (filter == null) return BaseFilter.ERROR_FILTER;

        return filter;
    }

    public @NotNull BaseFilterContainer getFilterContainer() {
        return this.itemFilterContainer;
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getAttachedSide()) != null;
    }

    @Override
    public boolean canPipePassThrough() {
        return true;
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    public boolean testItemStack(ItemStack stack) {
        return itemFilterContainer.test(stack);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager) {
        var filteringMode = new EnumSyncValue<>(ItemFilterMode.class, this::getFilterMode, this::setFilterMode);

        guiSyncManager.syncValue("filtering_mode", filteringMode);

        return getFilter().createPanel(guiSyncManager)
                .size(176, 212).padding(7)
                .child(CoverWithUI.createTitleRow(getFilterContainer().getFilterStack()).left(4))
                .child(Flow.column().widthRel(1f).align(Alignment.TopLeft).top(22).coverChildrenHeight()
                        .child(new EnumRowBuilder<>(ItemFilterMode.class)
                                .value(filteringMode)
                                .lang("cover.filter.mode.title")
                                .overlay(16, GTGuiTextures.FILTER_MODE_OVERLAY)
                                .build())
                        .child(Flow.row()
                                .marginBottom(2)
                                .widthRel(1f)
                                .coverChildrenHeight()
                                .setEnabledIf(b -> getFilterMode() != ItemFilterMode.FILTER_BOTH)
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
                        .child(getFilter().createWidgets(guiSyncManager).left(0)))
                .child(SlotGroupWidget.playerInventory().bottom(7).left(7));
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                            Cuboid6 plateBox, BlockRenderLayer layer) {
        this.texture.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("FilterMode", filterMode.ordinal());
        tagCompound.setTag("Filter", this.itemFilterContainer.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.filterMode = ItemFilterMode.VALUES[tagCompound.getInteger("FilterMode")];
        if (tagCompound.hasKey("IsBlacklist")) {
            this.itemFilterContainer.setFilterStack(getDefinition().getDropItemStack());
            this.itemFilterContainer.handleLegacyNBT(tagCompound);
            this.itemFilterContainer.setBlacklistFilter(tagCompound.getBoolean("IsBlacklist"));
        } else {
            this.itemFilterContainer.deserializeNBT(tagCompound.getCompoundTag("Filter"));
        }
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, T defaultValue) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (defaultValue == null) {
                return null;
            }
            IItemHandler delegate = (IItemHandler) defaultValue;
            if (itemHandler == null || itemHandler.delegate != delegate) {
                this.itemHandler = new ItemHandlerFiltered(delegate);
            }
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
        }
        return defaultValue;
    }

    private class ItemHandlerFiltered extends ItemHandlerDelegate {

        public ItemHandlerFiltered(IItemHandler delegate) {
            super(delegate);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            // set to extract, but insertion is allowed
            if (getFilterMode() == ItemFilterMode.FILTER_EXTRACT && allowFlow)
                return super.insertItem(slot, stack, simulate);

            // if set to insert or both, test the stack
            if (getFilterMode() != ItemFilterMode.FILTER_EXTRACT && itemFilterContainer.test(stack))
                return super.insertItem(slot, stack, simulate);

            // otherwise fail
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            // set to insert, but extraction is allowed
            if (getFilterMode() == ItemFilterMode.FILTER_INSERT && allowFlow)
                return super.extractItem(slot, amount, simulate);

            // if set to extract or both, test stack
            if (getFilterMode() != ItemFilterMode.FILTER_INSERT && itemFilterContainer.test(getStackInSlot(slot)))
                return super.extractItem(slot, amount, simulate);

            // otherwise fail
            return ItemStack.EMPTY;
        }
    }
}
