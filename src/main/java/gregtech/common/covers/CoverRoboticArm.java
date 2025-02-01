package gregtech.common.covers;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuiTextures;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.filter.SmartItemFilter;
import gregtech.common.pipelike.itempipe.net.ItemNetHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

public class CoverRoboticArm extends CoverConveyor {

    protected TransferMode transferMode;
    protected int itemsTransferBuffered;

    public CoverRoboticArm(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                           @NotNull EnumFacing attachedSide, int tier, int itemsPerSecond) {
        super(definition, coverableView, attachedSide, tier, itemsPerSecond);
        this.transferMode = TransferMode.TRANSFER_ANY;
        this.itemFilterContainer.setMaxTransferSize(1);
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                            Cuboid6 plateBox, BlockRenderLayer layer) {
        if (conveyorMode == ConveyorMode.EXPORT) {
            Textures.ARM_OVERLAY.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
        } else {
            Textures.ARM_OVERLAY_INVERTED.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
        }
    }

    @Override
    protected int doTransferItems(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount) {
        if (conveyorMode == ConveyorMode.EXPORT && itemHandler instanceof ItemNetHandler &&
                transferMode == TransferMode.KEEP_EXACT) {
            return 0;
        }
        if (conveyorMode == ConveyorMode.IMPORT && myItemHandler instanceof ItemNetHandler &&
                transferMode == TransferMode.KEEP_EXACT) {
            return 0;
        }
        return switch (transferMode) {
            case TRANSFER_ANY -> doTransferItemsAny(itemHandler, myItemHandler, maxTransferAmount);
            case TRANSFER_EXACT -> doTransferExact(itemHandler, myItemHandler, maxTransferAmount);
            case KEEP_EXACT -> doKeepExact(itemHandler, myItemHandler, maxTransferAmount);
        };
    }

    protected int doTransferExact(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount) {
        Map<ItemStack, TypeItemInfo> sourceItemAmount = doCountSourceInventoryItemsByType(itemHandler, myItemHandler);
        Iterator<ItemStack> iterator = sourceItemAmount.keySet().iterator();
        while (iterator.hasNext()) {
            TypeItemInfo sourceInfo = sourceItemAmount.get(iterator.next());
            int itemAmount = sourceInfo.totalCount;
            int itemToMoveAmount = itemFilterContainer.getTransferLimit(sourceInfo.itemStack);

            // if smart item filter and whitelist
            if (itemFilterContainer.getFilter() instanceof SmartItemFilter &&
                    !itemFilterContainer.isBlacklistFilter()) {
                if (itemFilterContainer.getTransferSize() > 1 && itemToMoveAmount * 2 <= itemAmount) {
                    // get the max we can extract from the item filter variable
                    int maxMultiplier = Math.floorDiv(maxTransferAmount, itemToMoveAmount);

                    // multiply up to the total count of all the items
                    itemToMoveAmount *= Math.min(itemFilterContainer.getTransferSize(), maxMultiplier);
                }
            }

            if (itemAmount >= itemToMoveAmount) {
                sourceInfo.totalCount = itemToMoveAmount;
            } else {
                iterator.remove();
            }
        }

        int itemsTransferred = 0;
        int maxTotalTransferAmount = maxTransferAmount + itemsTransferBuffered;
        boolean notEnoughTransferRate = false;
        for (TypeItemInfo itemInfo : sourceItemAmount.values()) {
            if (maxTotalTransferAmount >= itemInfo.totalCount) {
                boolean result = doTransferItemsExact(itemHandler, myItemHandler, itemInfo);
                itemsTransferred += result ? itemInfo.totalCount : 0;
                maxTotalTransferAmount -= result ? itemInfo.totalCount : 0;
            } else {
                notEnoughTransferRate = true;
            }
        }
        // if we didn't transfer anything because of too small transfer rate, buffer it
        if (itemsTransferred == 0 && notEnoughTransferRate) {
            itemsTransferBuffered += maxTransferAmount;
        } else {
            // otherwise, if transfer succeed, empty transfer buffer value
            itemsTransferBuffered = 0;
        }
        return Math.min(itemsTransferred, maxTransferAmount);
    }

    protected int doKeepExact(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount) {
        Map<Integer, GroupItemInfo> currentItemAmount = doCountDestinationInventoryItemsByMatchIndex(itemHandler,
                myItemHandler);
        Map<Integer, GroupItemInfo> sourceItemAmounts = doCountDestinationInventoryItemsByMatchIndex(myItemHandler,
                itemHandler);
        Iterator<Integer> iterator = sourceItemAmounts.keySet().iterator();
        while (iterator.hasNext()) {
            int filterSlotIndex = iterator.next();
            GroupItemInfo sourceInfo = sourceItemAmounts.get(filterSlotIndex);
            int itemToKeepAmount = itemFilterContainer.getTransferLimit(sourceInfo.filterSlot);

            // only run multiplier for smart item
            if (itemFilterContainer.getFilter() instanceof SmartItemFilter) {
                if (itemFilterContainer.getTransferSize() > 1 && itemToKeepAmount * 2 <= sourceInfo.totalCount) {
                    // get the max we can keep from the item filter variable
                    int maxMultiplier = Math.floorDiv(sourceInfo.totalCount, itemToKeepAmount);

                    // multiply up to the total count of all the items
                    itemToKeepAmount *= Math.min(itemFilterContainer.getTransferSize(), maxMultiplier);
                }
            }

            int itemAmount = 0;
            if (currentItemAmount.containsKey(filterSlotIndex)) {
                GroupItemInfo destItemInfo = currentItemAmount.get(filterSlotIndex);
                itemAmount = destItemInfo.totalCount;
            }
            if (itemAmount < itemToKeepAmount) {
                sourceInfo.totalCount = itemToKeepAmount - itemAmount;
            } else {
                iterator.remove();
            }
        }
        return doTransferItemsByGroup(itemHandler, myItemHandler, sourceItemAmounts, maxTransferAmount);
    }

    public int getBuffer() {
        return itemsTransferBuffered;
    }

    public void buffer(int amount) {
        itemsTransferBuffered += amount;
    }

    public void clearBuffer() {
        itemsTransferBuffered = 0;
    }

    public void setTransferMode(TransferMode transferMode) {
        if (this.transferMode != transferMode) {
            this.transferMode = transferMode;
            this.getCoverableView().markDirty();
            this.itemFilterContainer.setMaxTransferSize(transferMode.maxStackSize);
            writeCustomData(GregtechDataCodes.UPDATE_TRANSFER_MODE,
                    buffer -> buffer.writeByte(this.transferMode.ordinal()));
        }
    }

    public TransferMode getTransferMode() {
        return transferMode;
    }

    private boolean shouldDisplayAmountSlider() {
        if (transferMode == TransferMode.TRANSFER_ANY) {
            return false;
        }
        return itemFilterContainer.showGlobalTransferLimitSlider();
    }

    @Override
    public ModularPanel confgurePanel(ModularPanel panel, boolean isSmallGui) {
        return super.confgurePanel(panel, isSmallGui)
                .height(248);
    }

    @Override
    public @NotNull ParentWidget<?> createUI(SidedPosGuiData data, PanelSyncManager guiSyncManager) {
        EnumSyncValue<TransferMode> transferMode = new EnumSyncValue<>(TransferMode.class, this::getTransferMode,
                this::setTransferMode);
        guiSyncManager.syncValue("transfer_mode", transferMode);

        var filterTransferSize = new StringSyncValue(
                () -> String.valueOf(this.itemFilterContainer.getTransferSize()),
                s -> this.itemFilterContainer.setTransferSize(Integer.parseInt(s)));
        filterTransferSize.updateCacheFromSource(true);

        return super.createUI(data, guiSyncManager)
                .child(new EnumRowBuilder<>(TransferMode.class)
                        .value(transferMode)
                        .lang("cover.generic.transfer_mode")
                        .overlay(GTGuiTextures.TRANSFER_MODE_OVERLAY)
                        .build())
                .child(Flow.row().right(0).coverChildrenHeight()
                        .child(new TextFieldWidget().widthRel(0.5f).right(0)
                                .setEnabledIf(w -> shouldDisplayAmountSlider())
                                .setNumbers(0, Integer.MAX_VALUE)
                                .value(filterTransferSize)
                                .setTextColor(Color.WHITE.darker(1))));
    }

    @Override
    protected int getMaxStackSize() {
        return getTransferMode().maxStackSize;
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeByte(this.transferMode.ordinal());
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.transferMode = TransferMode.VALUES[packetBuffer.readByte()];
        this.itemFilterContainer.setMaxTransferSize(this.transferMode.maxStackSize);
    }

    @Override
    public void readCustomData(int discriminator, @NotNull PacketBuffer buf) {
        super.readCustomData(discriminator, buf);
        if (discriminator == GregtechDataCodes.UPDATE_TRANSFER_MODE) {
            this.transferMode = TransferMode.VALUES[buf.readByte()];
            this.itemFilterContainer.setMaxTransferSize(this.transferMode.maxStackSize);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferMode", transferMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        this.transferMode = TransferMode.VALUES[tagCompound.getInteger("TransferMode")];
        this.itemFilterContainer.setMaxTransferSize(this.transferMode.maxStackSize);
        super.readFromNBT(tagCompound);
    }
}
