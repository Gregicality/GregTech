package gregtech.api.capability;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.ItemStackHashStrategy;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DualHandler implements IItemHandlerModifiable, IMultipleTankHandler, INotifiableHandler {

    @NotNull
    private static final ItemStackHashStrategy strategy = ItemStackHashStrategy.comparingAll();

    @NotNull
    protected IItemHandlerModifiable itemDelegate;
    @NotNull
    protected IMultipleTankHandler fluidDelegate;

    private final List<ITankEntry> unwrapped;

    List<MetaTileEntity> notifiableEntities = new ArrayList<>();
    private final boolean isExport;

    public DualHandler(@NotNull IItemHandlerModifiable itemDelegate,
                       @NotNull IMultipleTankHandler fluidDelegate,
                       boolean isExport) {
        this.itemDelegate = itemDelegate;
        this.fluidDelegate = fluidDelegate;
        this.isExport = isExport;

        List<ITankEntry> list = new ArrayList<>();
        for (ITankEntry tank : this.fluidDelegate) {
            list.add(wrap(tank));
        }
        this.unwrapped = list;
    }

    public DualHandler(@NotNull IItemHandlerModifiable itemDelegate,
                       @NotNull IFluidTank fluidTank,
                       boolean isExport) {
        this(itemDelegate, new FluidTankList(false, fluidTank), isExport);
    }

    private DualEntry wrap(ITankEntry entry) {
        return entry instanceof DualEntry ? (DualEntry) entry : new DualEntry(this, entry);
    }

    public boolean isExport() {
        return this.isExport;
    }

    @Override
    public int getSlots() {
        return itemDelegate.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return itemDelegate.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        var remainder = itemDelegate.insertItem(slot, stack, simulate);
        if (!simulate && !strategy.equals(remainder, stack))
            onContentsChanged();
        return remainder;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
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
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
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
    public @NotNull List<ITankEntry> getFluidTanks() {
        return this.unwrapped;
    }

    @Override
    public int getTanks() {
        return this.fluidDelegate.getTanks();
    }

    @Override
    public @NotNull ITankEntry getTankAt(int index) {
        return this.unwrapped.get(index);
    }

    @Override
    public boolean allowSameFluidFill() {
        return this.fluidDelegate.allowSameFluidFill();
    }

    public void onContentsChanged(Object handler) {
        for (MetaTileEntity metaTileEntity : notifiableEntities) {
            addToNotifiedList(metaTileEntity, handler, isExport);
        }
    }

    public void onContentsChanged() {
        onContentsChanged(this);
    }

    @Override
    public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        if (metaTileEntity == null || this.notifiableEntities.contains(metaTileEntity))
            return;
        this.notifiableEntities.add(metaTileEntity);
    }

    @Override
    public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.remove(metaTileEntity);
    }

    public @NotNull IItemHandlerModifiable getItemDelegate() {
        return itemDelegate;
    }

    public @NotNull IMultipleTankHandler getFluidDelegate() {
        return fluidDelegate;
    }

    public static class DualEntry implements ITankEntry, INotifiableHandler {

        @NotNull
        private final DualHandler tank;

        @NotNull
        private final ITankEntry delegate;

        public DualEntry(@NotNull DualHandler tank, @NotNull ITankEntry delegate) {
            this.delegate = delegate;
            this.tank = tank;
        }

        @Override
        public @NotNull IMultipleTankHandler getParent() {
            return this.tank;
        }

        @Override
        public @NotNull ITankEntry getDelegate() {
            return this.delegate;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return this.getDelegate().getTankProperties();
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            int filled = getDelegate().fill(resource, doFill);
            if (doFill && filled > 0)
                tank.onContentsChanged(this);
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            var drained = getDelegate().drain(resource, doDrain);
            if (doDrain && drained != null)
                tank.onContentsChanged(this);
            return drained;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            var drained = getDelegate().drain(maxDrain, doDrain);
            if (doDrain && drained != null)
                tank.onContentsChanged(this);
            return drained;
        }

        @Override
        public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
            this.tank.addNotifiableMetaTileEntity(metaTileEntity);
        }

        @Override
        public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
            this.tank.removeNotifiableMetaTileEntity(metaTileEntity);
        }
    }
}
