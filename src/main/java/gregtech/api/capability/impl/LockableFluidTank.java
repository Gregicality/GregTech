package gregtech.api.capability.impl;

import gregtech.api.capability.ILockableHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class LockableFluidTank extends NotifiableFluidTank implements ILockableHandler<Fluid> {

    private boolean locked;
    private Fluid lockedFluid;

    public LockableFluidTank(int capacity, MetaTileEntity entityToNotify, boolean isExport) {
        super(capacity, entityToNotify, isExport);
    }

    @Override
    public Fluid getLockedObject() {
        return lockedFluid;
    }

    @Override
    public void setLock(boolean isLocked) {
        locked = isLocked;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        int filled = super.fill(resource, doFill);
        if (doFill && this.fluid != null && this.fluid.amount != 0) {
            this.lockedFluid = this.fluid.getFluid();
        }
        return filled;
    }

    @Override
    public void setFluid(FluidStack fluid) {
        super.setFluid(fluid);
        if (this.fluid != null && this.fluid.amount != 0) {
            this.lockedFluid = this.fluid.getFluid();
        }
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        if (locked && fluid.getFluid() != lockedFluid) {
            return false;
        }
        return super.canFillFluidType(fluid);
    }

    @Override
    public FluidTank readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("LockedFluid")) {
            this.lockedFluid = FluidRegistry.getFluid(nbt.getString("LockedFluid"));
        } else if (this.fluid != null && this.fluid.amount != 0) {
            this.lockedFluid = this.fluid.getFluid();
        }
        return this;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (lockedFluid != null) {
            nbt.setString("LockedFluid", lockedFluid.getName());
        }
        return nbt;
    }
}
