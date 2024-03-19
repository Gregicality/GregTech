package gregtech.api;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class DualHandler implements IItemHandlerModifiable, IFluidHandler, INotifiableHandler {

    @Nullable
    IItemHandlerModifiable itemDelegate;
    @Nullable
    IFluidHandler fluidDelegate;
    @Nullable
    IDirtyNotifiable dirtyNotifiable;
    private final boolean isExport;

    private final List<MetaTileEntity> notifiables = new ArrayList<>();

    public DualHandler(IItemHandlerModifiable itemDelegate, IFluidHandler fluidDelegate, IDirtyNotifiable notifiable,
                       boolean isExport) {
        this.itemDelegate = itemDelegate;
        this.fluidDelegate = fluidDelegate;
        this.dirtyNotifiable = notifiable;
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

    public void onContentsCahgned() {
        if (this.dirtyNotifiable != null) {
            this.dirtyNotifiable.markAsDirty();
        }
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

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return fluidDelegate == null ?
                new IFluidTankProperties[0] :
                fluidDelegate.getTankProperties();
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        IItemHandlerModifiable itemHandler;
        IFluidHandler fluidHandler;
        IDirtyNotifiable notifiable;

        public Builder itemHandler(IItemHandlerModifiable itemHandler) {
            this.itemHandler = itemHandler;
            return this;
        }

        public Builder fluidTank(IFluidHandler fluidTank) {
            this.fluidHandler = fluidTank;
            return this;
        }

        public Builder notifiable(IDirtyNotifiable notifiable) {
            this.notifiable = notifiable;
            return this;
        }

        public DualHandler build(boolean isExport) {
            return new DualHandler(this.itemHandler, this.fluidHandler, this.notifiable, isExport);
        }
    }
}
