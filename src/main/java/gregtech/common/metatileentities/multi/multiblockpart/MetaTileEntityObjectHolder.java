package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IObjectHolder;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IDataItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MetaTileEntityObjectHolder extends MetaTileEntityMultiblockNotifiablePart
                                        implements IMultiblockAbilityPart<IObjectHolder>, IObjectHolder {

    // purposefully not exposed to automation or capabilities
    private final ObjectHolderHandler heldItems;

    private boolean isLocked;

    public MetaTileEntityObjectHolder(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.ZPM, false);
        heldItems = new ObjectHolderHandler(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityObjectHolder(metaTileEntityId);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        guiSyncManager.registerSlotGroup("item_inv", 2);

        // TODO: Change the position of the name when it's standardized.
        return GTGuis.createPanel(this, 176, 166)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory().left(7).bottom(7))
                .child(GTGuiTextures.PROGRESS_BAR_RESEARCH_STATION_BASE.asWidget()
                        .left(46).top(18)
                        .size(84, 60))
                .child(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(heldItems, 0)
                                .slotGroup("item_inv")
                                .filter(itemStack -> !isSlotBlocked()))
                        .background(GTGuiTextures.SLOT, GTGuiTextures.RESEARCH_STATION_OVERLAY)
                        .overlay((context, x, y, width, height, widgetTheme) -> {
                            if (isSlotBlocked()) {
                                GuiDraw.drawRect(x, y, width, height, 0x80404040);
                            }
                        })
                        .left(79).top(39))
                .child(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(heldItems, 1)
                                .slotGroup("item_inv")
                                .filter(itemStack -> !isSlotBlocked()))
                        .background(GTGuiTextures.SLOT, GTGuiTextures.DATA_ORB_OVERLAY)
                        .overlay((context, x, y, width, height, widgetTheme) -> {
                            if (isSlotBlocked()) {
                                GuiDraw.drawRect(x, y, width, height, 0x80404040);
                            }
                        })
                        .left(15).top(39));
    }

    private boolean isSlotBlocked() {
        return isLocked;
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        clearInventory(itemBuffer, heldItems);
    }

    @Override
    public MultiblockAbility<IObjectHolder> getAbility() {
        return MultiblockAbility.OBJECT_HOLDER;
    }

    @Override
    public @NotNull List<? extends IObjectHolder> registerAbilities(
            @NotNull MultiblockAbility<? extends IObjectHolder> multiblockAbility) {
        return Collections.singletonList(this);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        SimpleOverlayRenderer renderer = Textures.OBJECT_HOLDER_OVERLAY;
        var controller = getController();
        if (controller != null && controller.isActive()) {
            renderer = Textures.OBJECT_HOLDER_ACTIVE_OVERLAY;
        }
        renderer.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        var controller = getController();
        if (controller != null && controller.isStructureFormed()) {
            controller.checkStructurePattern();
        }
    }

    @NotNull
    @Override
    public ItemStack getHeldItem(boolean remove) {
        return getHeldItem(0, remove);
    }

    @Override
    public void setHeldItem(@NotNull ItemStack heldItem) {
        heldItems.setStackInSlot(0, heldItem);
    }

    @NotNull
    @Override
    public ItemStack getDataItem(boolean remove) {
        return getHeldItem(1, remove);
    }

    @Override
    public void setDataItem(@NotNull ItemStack dataItem) {
        heldItems.setStackInSlot(1, dataItem);
    }

    @Override
    public void setLocked(boolean locked) {
        if (isLocked != locked) {
            isLocked = locked;
            markDirty();
            if (getWorld() != null && !getWorld().isRemote) {
                writeCustomData(GregtechDataCodes.LOCK_OBJECT_HOLDER, buf -> buf.writeBoolean(isLocked));
            }
        }
    }

    @Override
    public @NotNull IItemHandler getAsHandler() {
        // noinspection ReturnOfInnerClass
        return this.heldItems;
    }

    @NotNull
    private ItemStack getHeldItem(int slot, boolean remove) {
        ItemStack stackInSlot = heldItems.getStackInSlot(slot);
        if (remove && stackInSlot != ItemStack.EMPTY) {
            heldItems.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return stackInSlot;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("IsLocked", isLocked);
        data.setTag("Inventory", heldItems.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isLocked = data.getBoolean("IsLocked");
        this.heldItems.deserializeNBT(data.getCompoundTag("Inventory"));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isLocked);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isLocked = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.LOCK_OBJECT_HOLDER) {
            this.isLocked = buf.readBoolean();
        }
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        heldItems.addNotifiableMetaTileEntity(controllerBase);
        heldItems.addToNotifiedList(this, heldItems, false);
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        heldItems.removeNotifiableMetaTileEntity(controllerBase);
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.disabled"));
    }

    private class ObjectHolderHandler extends NotifiableItemStackHandler {

        public ObjectHolderHandler(MetaTileEntity metaTileEntity) {
            super(metaTileEntity, 2, null, false);
        }

        // only allow a single item, no stack size
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        // prevent extracting the item while running
        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!isSlotBlocked()) {
                return super.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        // only allow data items in the second slot
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (stack.isEmpty()) {
                return true;
            }

            boolean isDataItem = false;
            if (stack.getItem() instanceof MetaItem<?>metaItem) {
                for (IItemBehaviour behaviour : metaItem.getBehaviours(stack)) {
                    if (behaviour instanceof IDataItem) {
                        isDataItem = true;
                        break;
                    }
                }
            }

            if (slot == 0 && !isDataItem) {
                return true;
            } else return slot == 1 && isDataItem;
        }
    }
}
