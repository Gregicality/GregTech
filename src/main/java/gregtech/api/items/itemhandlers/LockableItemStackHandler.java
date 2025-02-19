package gregtech.api.items.itemhandlers;

import gregtech.api.capability.ILockableHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

public class LockableItemStackHandler extends ItemStackHandler implements ILockableHandler<ItemStack> {

    protected boolean locked;
    protected ItemStack lockedItemStack;

    public LockableItemStackHandler() {
        super(1);
    }

    @Override
    public void setLock(boolean isLocked) {
        this.locked = isLocked;
        if (isLocked && !this.getStackInSlot(0).isEmpty()) {
            lockedItemStack = this.getStackInSlot(0).copy();
        }
    }

    public boolean isLocked() {
        return this.locked;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (this.locked && !this.lockedItemStack.isItemEqual(stack)) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack getLockedObject() {
        return lockedItemStack;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        if (lockedItemStack != null && !lockedItemStack.isEmpty()) {
            nbt.setTag("LockedItemStack", lockedItemStack.serializeNBT());
        }
        nbt.setBoolean("Locked", locked);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        if (nbt.hasKey("LockedItemStack")) {
            lockedItemStack = new ItemStack(nbt.getCompoundTag("LockedItemStack"));
        }
        locked = nbt.getBoolean("Locked");
    }
}
