package gregtech.api.capability.impl;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Efficiently delegates calls into multiple item handlers
 */
public class ItemHandlerList implements IItemHandlerModifiable {

    private final Int2ObjectMap<IItemHandler> handlerBySlotIndex = new Int2ObjectOpenHashMap<>();
    private final Map<IItemHandler, Integer> baseIndexOffset = new IdentityHashMap<>();

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

    @Override
    public int getSlots() {
        return handlerBySlotIndex.size();
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        if (!(itemHandler instanceof IItemHandlerModifiable))
            throw new UnsupportedOperationException("Handler " + itemHandler + " does not support this method");
        ((IItemHandlerModifiable) itemHandler).setStackInSlot(slot - baseIndexOffset.get(itemHandler), stack);
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        int realSlot = slot - baseIndexOffset.get(itemHandler);
        return itemHandler.getStackInSlot(slot - baseIndexOffset.get(itemHandler));
    }

    @Override
    public int getSlotLimit(int slot) {
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        return itemHandler.getSlotLimit(slot - baseIndexOffset.get(itemHandler));
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        return itemHandler.insertItem(slot - baseIndexOffset.get(itemHandler), stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        IItemHandler itemHandler = handlerBySlotIndex.get(slot);
        return itemHandler.extractItem(slot - baseIndexOffset.get(itemHandler), amount, simulate);
    }

    @NotNull
    public Collection<IItemHandler> getBackingHandlers() {
        return Collections.unmodifiableCollection(baseIndexOffset.keySet());
    }
}
