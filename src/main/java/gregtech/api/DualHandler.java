package gregtech.api;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DualHandler implements IItemHandlerModifiable, IFluidTank, IMultipleTankHandler, INotifiableHandler {

    @NotNull
    IItemHandlerModifiable itemDelegate;
    @NotNull
    IMultipleTankHandler fluidDelegate;
    private final boolean isExport;

    private final List<MetaTileEntity> notifiables = new ArrayList<>();

    public DualHandler(@NotNull IItemHandlerModifiable itemDelegate,
                       @NotNull IMultipleTankHandler fluidDelegate,
                       boolean isExport) {
        this.itemDelegate = itemDelegate;
        this.fluidDelegate = fluidDelegate;
        this.isExport = isExport;
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
        var inserted = itemDelegate.insertItem(slot, stack, simulate);

        if (!simulate && inserted.getCount() != stack.getCount())
            onContentsChanged();

        return inserted;
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

    public void onContentsChanged() {
        for (var mte : this.notifiables) {
            if (isExport) {
                mte.addNotifiedOutput(this);
            } else {
                mte.addNotifiedInput(this);
            }
        }
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        itemDelegate.setStackInSlot(slot, stack);
    }

    @NotNull
    private IFluidTank getFirstTank() {
        var tanks = this.fluidDelegate.getFluidTanks();
        tanks.sort(ENTRY_COMPARATOR);
        return tanks.get(0);
    }

    @Override
    public FluidStack getFluid() {
        return getFirstTank().getFluid();
    }

    @Override
    public int getFluidAmount() {
        return getFirstTank().getFluidAmount();
    }

    @Override
    public int getCapacity() {
        return getFirstTank().getCapacity();
    }

    @Override
    public FluidTankInfo getInfo() {
        return getFirstTank().getInfo();
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return this.fluidDelegate.getTankProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        int filled = fluidDelegate.fill(resource, doFill);

        if (doFill && filled > 0)
            onContentsChanged();

        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        var drained = fluidDelegate.drain(resource, doDrain);

        if (doDrain && drained != null && drained.amount > 0)
            onContentsChanged();

        return drained;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        var drained = fluidDelegate.drain(maxDrain, doDrain);

        if (doDrain && drained != null && drained.amount > 0)
            onContentsChanged();

        return drained;
    }

    @Override
    public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiables.add(metaTileEntity);
    }

    @Override
    public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiables.remove(metaTileEntity);
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
}
