package gregtech.api;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DualHandler implements IItemHandlerModifiable, IFluidTank, IMultipleTankHandler, INotifiableHandler {

    private static final IFluidTank NULL_TANK = new FluidTank(0);
    private static final IMultipleTankHandler NULL_HANDLER = new FluidTankList(false, NULL_TANK);

    @Nullable
    IItemHandlerModifiable itemDelegate;
    @Nullable
    IMultipleTankHandler fluidDelegate;
    private final boolean isExport;

    private final List<MetaTileEntity> notifiables = new ArrayList<>();

    public DualHandler(IItemHandlerModifiable itemDelegate, IMultipleTankHandler fluidDelegate,
                       boolean isExport) {
        this.itemDelegate = itemDelegate;
        this.fluidDelegate = fluidDelegate;
        this.isExport = isExport;
    }

    @Override
    public int getSlots() {
        return itemDelegate == null ? 0 : itemDelegate.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (itemDelegate == null) return ItemStack.EMPTY;
        return itemDelegate.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (itemDelegate == null) return stack;
        return itemDelegate.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (itemDelegate == null) return ItemStack.EMPTY;
        return itemDelegate.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return itemDelegate.getSlotLimit(slot);
    }

    // todo actually use this
    public void onContentsChanged() {
        notifiables.forEach(mte -> {
            if (isExport) {
                mte.addNotifiedOutput(this);
            } else {
                mte.addNotifiedInput(this);
            }
        });
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (itemDelegate == null) return;
        itemDelegate.setStackInSlot(slot, stack);
    }

    @NotNull
    private IFluidTank getFirstTank() {
        if (fluidDelegate == null)
            return NULL_TANK;

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
        return new IFluidTankProperties[0];
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (fluidDelegate == null) return 0;
        return fluidDelegate.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (fluidDelegate == null) return null;
        return fluidDelegate.drain(resource, doDrain);
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (fluidDelegate == null) return null;
        return fluidDelegate.drain(maxDrain, doDrain);
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
        return this.fluidDelegate == null ?
                NULL_HANDLER.getFluidTanks() :
                this.fluidDelegate.getFluidTanks();
    }

    @Override
    public int getTanks() {
        return this.fluidDelegate == null ?
                NULL_HANDLER.getTanks() :
                this.fluidDelegate.getTanks();
    }

    @Override
    public @NotNull MultiFluidTankEntry getTankAt(int index) {
        return this.fluidDelegate == null ?
                NULL_HANDLER.getTankAt(0) :
                this.fluidDelegate.getTankAt(index);
    }

    @Override
    public boolean allowSameFluidFill() {
        return this.fluidDelegate == null ?
                NULL_HANDLER.allowSameFluidFill() :
                this.fluidDelegate.allowSameFluidFill();
    }
}
