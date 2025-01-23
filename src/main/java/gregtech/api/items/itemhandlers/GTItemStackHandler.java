package gregtech.api.items.itemhandlers;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.IFilteredItemContainer;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTItemStackHandler extends ItemStackHandler implements IFilteredItemContainer {

    private final MetaTileEntity metaTileEntity;
    private IFilter<ItemStack> filter;

    public GTItemStackHandler(MetaTileEntity metaTileEntity) {
        super();
        this.metaTileEntity = metaTileEntity;
    }

    public GTItemStackHandler(MetaTileEntity metaTileEntity, int size) {
        super(size);
        this.metaTileEntity = metaTileEntity;
    }

    public GTItemStackHandler(MetaTileEntity metaTileEntity, NonNullList<ItemStack> stacks) {
        super(stacks);
        this.metaTileEntity = metaTileEntity;
    }

    @Override
    public void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        metaTileEntity.markDirty();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!isItemValid(slot, stack)) return stack;
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return filter == null || filter.test(stack);
    }

    public GTItemStackHandler setFilter(IFilter<ItemStack> filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public @Nullable IFilter<ItemStack> getFilter() {
        return null;
    }
}
