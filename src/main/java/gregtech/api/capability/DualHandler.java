package gregtech.api.capability;

import gregtech.api.capability.impl.FluidTankList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DualHandler implements IItemHandlerModifiable, IMultipleTankHandler {

    @NotNull
    IItemHandlerModifiable itemDelegate;
    @NotNull
    IMultipleTankHandler fluidDelegate;
    private final boolean isExport;

    public DualHandler(@NotNull IItemHandlerModifiable itemDelegate,
                       @NotNull IMultipleTankHandler fluidDelegate,
                       boolean isExport) {
        this.itemDelegate = itemDelegate;
        this.fluidDelegate = fluidDelegate;
        this.isExport = isExport;
    }

    public DualHandler(@NotNull IItemHandlerModifiable itemDelegate,
                       @NotNull IFluidTank fluidTank,
                       boolean isExport) {
        this.itemDelegate = itemDelegate;
        this.fluidDelegate = new FluidTankList(false, fluidTank);
        this.isExport = isExport;
    }

    public IItemHandlerModifiable getItemDelegate() {
        return this.itemDelegate;
    }

    public IMultipleTankHandler getFluidDelegate() {
        return this.fluidDelegate;
    }

    public boolean isExport() {
        return this.isExport;
    }

    @Override
    public int getSlots() {
        return itemDelegate.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return itemDelegate.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return itemDelegate.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return itemDelegate.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return itemDelegate.getSlotLimit(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        itemDelegate.setStackInSlot(slot, stack);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return this.fluidDelegate.getTankProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return fluidDelegate.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return fluidDelegate.drain(resource, doDrain);
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return fluidDelegate.drain(maxDrain, doDrain);
    }

    @Override
    public @NotNull List<MultiFluidTankEntry> getFluidTanks() {
        return this.fluidDelegate.getFluidTanks();
    }

    @Override
    public int getTanks() {
        return this.fluidDelegate.getTanks();
    }

    @Override
    public @NotNull MultiFluidTankEntry getTankAt(int index) {
        return this.fluidDelegate.getTankAt(index);
    }

    @Override
    public boolean allowSameFluidFill() {
        return this.fluidDelegate.allowSameFluidFill();
    }

    public List<DualEntry> unwrap() {
        int items = itemDelegate.getSlots();
        int fluids = fluidDelegate.getTanks();
        int max = Math.max(items, fluids);

        List<DualEntry> list = new ArrayList<>(max);
        for (int i = 0; i < max; i++) {
            int itemIndex = -1;
            if (i < items)
                itemIndex = i;

            int fluidIndex = -1;
            if (i < fluids)
                fluidIndex = i;

            list.add(new DualEntry(this, itemIndex, fluidIndex));
        }

        return list;
    }

    public static class DualEntry implements IItemHandlerModifiable, IFluidTank, IFluidHandler {

        private static final FluidTankInfo NULL = new FluidTankInfo(null, 0);

        private final DualHandler delegate;
        private final int itemIndex;
        private final int fluidIndex;
        private final IFluidTankProperties[] props;

        public DualEntry(DualHandler delegate, int itemIndex, int fluidIndex) {
            this.delegate = delegate;
            this.itemIndex = itemIndex;
            this.fluidIndex = fluidIndex;
            this.props = this.fluidIndex == -1 ?
                    new IFluidTankProperties[0] :
                    this.delegate.getTankAt(this.fluidIndex).getTankProperties();
        }

        public DualHandler getDelegate() {
            return this.delegate;
        }

        @Override
        public FluidStack getFluid() {
            if (fluidIndex == -1) return null;
            return this.delegate.getTankAt(this.fluidIndex).getFluid();
        }

        @Override
        public int getFluidAmount() {
            if (fluidIndex == -1) return 0;
            return this.delegate.getTankAt(this.fluidIndex).getFluidAmount();
        }

        @Override
        public int getCapacity() {
            if (fluidIndex == -1) return 0;
            return this.delegate.getTankAt(this.fluidIndex).getCapacity();
        }

        @Override
        public FluidTankInfo getInfo() {
            if (fluidIndex == -1) return NULL;
            return this.delegate.getTankAt(this.fluidIndex).getInfo();
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return this.props;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (fluidIndex == -1) return 0;
            return this.delegate.getTankAt(this.fluidIndex).fill(resource, doFill);
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (fluidIndex == -1) return null;
            return this.delegate.getTankAt(this.fluidIndex).drain(resource, doDrain);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (fluidIndex == -1) return null;
            return this.delegate.getTankAt(this.fluidIndex).drain(maxDrain, doDrain);
        }

        @Override
        public int getSlots() {
            return itemIndex == -1 ? 0 : 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (itemIndex == -1) return ItemStack.EMPTY;
            return this.delegate.getStackInSlot(this.itemIndex);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (itemIndex == -1) return stack;
            return this.delegate.insertItem(this.itemIndex, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (itemIndex == -1) return ItemStack.EMPTY;
            return this.delegate.extractItem(this.itemIndex, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            if (itemIndex == -1) return 0;
            return this.delegate.getSlotLimit(this.itemIndex);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (itemIndex == -1) return;
            this.delegate.setStackInSlot(this.itemIndex, stack);
        }
    }
}
