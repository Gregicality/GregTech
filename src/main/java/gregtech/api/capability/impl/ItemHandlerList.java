package gregtech.api.capability.impl;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Efficiently delegates calls into multiple item handlers
 */
public class ItemHandlerList implements IItemHandlerModifiable {

    private final Int2ObjectMap<IItemHandler> handlerBySlotIndex = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<IItemHandler> baseIndexOffset = new Object2IntArrayMap<>();

    public ItemHandlerList(List<? extends IItemHandler> itemHandlerList) {
        int currentSlotIndex = 0;
        for (IItemHandler itemHandler : itemHandlerList) {
            if (baseIndexOffset.containsKey(itemHandler)) {
                throw new IllegalArgumentException("Attempted to add item handler " + itemHandler + " twice");
            }
            baseIndexOffset.put(itemHandler, currentSlotIndex);
            int slotsCount = itemHandler.getSlots();
            for (int slotIndex = 0; slotIndex < slotsCount; slotIndex++) {
                handlerBySlotIndex.put(currentSlotIndex + slotIndex, itemHandler);
            }
            currentSlotIndex += slotsCount;
        }
    }

    public int getIndexOffset(IItemHandler handler) {
        return baseIndexOffset.getOrDefault(handler, -1);
    }

    @Override
    public int getSlots() {
        return handlerBySlotIndex.size();
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (invalidSlot(slot)) return;
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        int actualSlot = slot - baseIndexOffset.get(itemHandler);
        if (itemHandler instanceof IItemHandlerModifiable modifiable) {
            modifiable.setStackInSlot(actualSlot, stack);
        } else {
            itemHandler.extractItem(actualSlot, Integer.MAX_VALUE, false);
            itemHandler.insertItem(actualSlot, stack, false);
        }
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (invalidSlot(slot)) return ItemStack.EMPTY;
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        return itemHandler.getStackInSlot(slot - baseIndexOffset.get(itemHandler));
    }

    @Override
    public int getSlotLimit(int slot) {
        if (invalidSlot(slot)) return 0;
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        return itemHandler.getSlotLimit(slot - baseIndexOffset.get(itemHandler));
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (invalidSlot(slot)) return stack;
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        return itemHandler.insertItem(slot - baseIndexOffset.get(itemHandler), stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (invalidSlot(slot)) return ItemStack.EMPTY;
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        return itemHandler.extractItem(slot - baseIndexOffset.get(itemHandler), amount, simulate);
    }

    @NotNull
    public Collection<IItemHandler> getBackingHandlers() {
        return Collections.unmodifiableCollection(baseIndexOffset.keySet());
    }

    private boolean invalidSlot(int slot) {
        return slot < 0 && slot >= this.getSlots();
    }
}
