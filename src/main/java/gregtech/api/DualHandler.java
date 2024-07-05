package gregtech.api;

import gregtech.api.capability.IMultipleTankHandler;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DualHandler implements IItemHandlerModifiable, IFluidTank, IMultipleTankHandler {

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
}
