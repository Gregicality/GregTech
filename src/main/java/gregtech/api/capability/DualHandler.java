package gregtech.api.capability;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.ItemStackHashStrategy;

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

public class DualHandler implements IItemHandlerModifiable, IMultipleTankHandler, INotifiableHandler {

    private static final ItemStackHashStrategy strategy = ItemStackHashStrategy.builder()
            .compareItem(true)
            .compareDamage(true)
            .compareTag(true)
            .build();
    @NotNull
    IItemHandlerModifiable itemDelegate;
    @NotNull
    IMultipleTankHandler fluidDelegate;

    private final List<DualEntry> unwrapped;

    List<MetaTileEntity> notifiableEntities = new ArrayList<>();
    private final boolean isExport;

    public DualHandler(@NotNull IItemHandlerModifiable itemDelegate,
                       @NotNull IMultipleTankHandler fluidDelegate,
                       boolean isExport) {
        this.itemDelegate = itemDelegate;
        this.fluidDelegate = fluidDelegate;
        this.isExport = isExport;
        int items = itemDelegate.getSlots();
        int fluids = fluidDelegate.getTanks();
        int max = Math.max(items, fluids);

        List<DualEntry> list = new ArrayList<>(max);
        for (int i = 0; i < max; i++) {
            list.add(new DualEntry(this,
                    i < items ? i : -1,
                    i < fluids ? i : -1));
        }
        this.unwrapped = list;
    }

    public DualHandler(@NotNull IItemHandlerModifiable itemDelegate,
                       @NotNull IFluidTank fluidTank,
                       boolean isExport) {
        this(itemDelegate, new FluidTankList(false, fluidTank), isExport);
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
        var remainder = itemDelegate.insertItem(slot, stack, simulate);
        if (!simulate && !strategy.equals(remainder, stack))
            onContentsChanged();
        return remainder;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        var extracted = itemDelegate.extractItem(slot, amount, simulate);
        if (!simulate && !extracted.isEmpty())
            onContentsChanged();
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return itemDelegate.getSlotLimit(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        var oldStack = itemDelegate.getStackInSlot(slot);
        itemDelegate.setStackInSlot(slot, stack);
        if (!strategy.equals(oldStack, stack))
            onContentsChanged();
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return this.fluidDelegate.getTankProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        int filled = fluidDelegate.fill(resource, doFill);
        if (doFill && filled > 0) onContentsChanged();
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        var drained = fluidDelegate.drain(resource, doDrain);
        if (doDrain && drained != null) onContentsChanged();
        return drained;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        var drained = fluidDelegate.drain(maxDrain, doDrain);
        if (doDrain && drained != null) onContentsChanged();
        return drained;
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
        return this.unwrapped;
    }

    public void onContentsChanged() {
        for (MetaTileEntity metaTileEntity : notifiableEntities) {
            if (metaTileEntity != null && metaTileEntity.isValid()) {
                addToNotifiedList(metaTileEntity, this, isExport);
            }
        }
    }

    @Override
    public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        if (metaTileEntity == null) return;
        this.notifiableEntities.add(metaTileEntity);
    }

    @Override
    public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.remove(metaTileEntity);
    }

    public static class DualEntry implements IItemHandlerModifiable, IFluidTank, IFluidHandler, INotifiableHandler {

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
                    getTank().getTankProperties();
        }

        public DualHandler getDelegate() {
            return this.delegate;
        }

        @Override
        public FluidStack getFluid() {
            if (fluidIndex == -1) return null;
            return getTank().getFluid();
        }

        @Override
        public int getFluidAmount() {
            if (fluidIndex == -1) return 0;
            return getTank().getFluidAmount();
        }

        @Override
        public int getCapacity() {
            if (fluidIndex == -1) return 0;
            return getTank().getCapacity();
        }

        @Override
        public FluidTankInfo getInfo() {
            if (fluidIndex == -1) return NULL;
            return getTank().getInfo();
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return this.props;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (fluidIndex == -1) return 0;
            int filled = getTank().fill(resource, doFill);
            if (doFill && filled > 0)
                delegate.onContentsChanged();
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (fluidIndex == -1) return null;
            var drained = getTank().drain(resource, doDrain);
            if (doDrain && drained != null)
                delegate.onContentsChanged();
            return drained;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (fluidIndex == -1) return null;
            var drained = getTank().drain(maxDrain, doDrain);
            if (doDrain && drained != null)
                delegate.onContentsChanged();
            return drained;
        }

        private MultiFluidTankEntry getTank() {
            return this.delegate.getTankAt(this.fluidIndex);
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

        @Override
        public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
            this.delegate.addNotifiableMetaTileEntity(metaTileEntity);
        }

        @Override
        public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
            this.delegate.removeNotifiableMetaTileEntity(metaTileEntity);
        }
    }
}
