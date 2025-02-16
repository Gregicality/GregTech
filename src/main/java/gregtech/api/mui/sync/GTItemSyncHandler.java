package gregtech.api.mui.sync;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemHandlerHelper;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import java.io.IOException;

public class GTItemSyncHandler extends SyncHandler {

    private final ModularSlot slot;
    private ItemStack lastStoredItem;
    private ItemStack lastStoredPhantomItem = ItemStack.EMPTY;

    public GTItemSyncHandler(ModularSlot slot) {
        this.slot = slot;
    }

    @Override
    public void init(String key, PanelSyncManager syncManager) {
        super.init(key, syncManager);
        // noinspection UnstableApiUsage
        // getSyncManager().getContainer().registerSlot(getSyncManager().getPanelName(), this.slot);
        this.lastStoredItem = getSlot().getStack().copy();
        // if (isPhantom() && !getSlot().getStack().isEmpty()) {
        // this.lastStoredPhantomItem = getSlot().getStack().copy();
        // this.lastStoredPhantomItem.setCount(1);
        // }
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        ItemStack itemStack = getSlot().getStack();
        if (itemStack.isEmpty() && this.lastStoredItem.isEmpty()) return;
        boolean onlyAmountChanged = false;
        if (init ||
                !ItemHandlerHelper.canItemStacksStack(this.lastStoredItem, itemStack) ||
                (onlyAmountChanged = itemStack.getCount() != this.lastStoredItem.getCount())) {
            getSlot().onSlotChangedReal(itemStack, onlyAmountChanged, false, init);
            if (onlyAmountChanged) {
                this.lastStoredItem.setCount(itemStack.getCount());
            } else {
                this.lastStoredItem = itemStack.isEmpty() ? ItemStack.EMPTY : itemStack.copy();
            }
            final boolean finalOnlyAmountChanged = onlyAmountChanged;
            syncToClient(1, buffer -> {
                buffer.writeBoolean(finalOnlyAmountChanged);
                buffer.writeItemStack(itemStack);
                buffer.writeBoolean(init);
            });
        }
    }

    public ModularSlot getSlot() {
        return slot;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {}

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {}
}
