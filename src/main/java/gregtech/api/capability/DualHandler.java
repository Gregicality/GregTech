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

    private static final ItemStackHashStrategy strategy = ItemStackHashStrategy.builder()
            .compareItem(true)
            .compareDamage(true)
            .compareTag(true)
            .build();
    @NotNull
    IItemHandlerModifiable itemDelegate;
    @NotNull
    IMultipleTankHandler fluidDelegate;

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
        for (var tank : this.fluidDelegate) {
            list.add(new DualEntry(this, tank));
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

    public static class DualEntry implements ITankEntry, INotifiableHandler {

        private final DualHandler delegate;

        @NotNull
        private final ITankEntry tank;

        public DualEntry(DualHandler delegate, ITankEntry tank) {
            this.delegate = delegate;
            this.tank = tank;
        }

        @Override
        public @NotNull IMultipleTankHandler getParent() {
            return this.delegate;
        }

        @Override
        public @NotNull IFluidTank getDelegate() {
            return this.tank;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return this.getTank().getTankProperties();
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            int filled = getTank().fill(resource, doFill);
            if (doFill && filled > 0)
                delegate.onContentsChanged(this);
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            var drained = getTank().drain(resource, doDrain);
            if (doDrain && drained != null)
                delegate.onContentsChanged(this);
            return drained;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            var drained = getTank().drain(maxDrain, doDrain);
            if (doDrain && drained != null)
                delegate.onContentsChanged(this);
            return drained;
        }

        private @NotNull ITankEntry getTank() {
            return this.tank;
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
