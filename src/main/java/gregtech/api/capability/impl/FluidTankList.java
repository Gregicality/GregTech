package gregtech.api.capability.impl;

import gregtech.api.capability.MultipleTankHandler;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FluidTankList extends MultipleTankHandler {

    private final boolean allowSameFluidFill;
    private Entry[] tanks = new Entry[0];

    public FluidTankList(boolean allowSameFluidFill, IFluidTank... fluidTanks) {
        if (!ArrayUtils.isEmpty(fluidTanks)) {
            tanks = new Entry[fluidTanks.length];
            Arrays.setAll(tanks, value -> wrap(fluidTanks[value]));
        }
        this.allowSameFluidFill = allowSameFluidFill;
    }

    public FluidTankList(boolean allowSameFluidFill, @NotNull List<? extends IFluidTank> fluidTanks) {
        this(allowSameFluidFill, fluidTanks.toArray(new IFluidTank[0]));
    }

    public FluidTankList(boolean allowSameFluidFill, @NotNull MultipleTankHandler parent,
                         IFluidTank... additionalTanks) {
        int tanks = parent.size();
        int additional = ArrayUtils.getLength(additionalTanks);

        this.tanks = new Entry[tanks + additional];

        Arrays.setAll(this.tanks, value -> {
            if (value < tanks) return parent.getTankAt(value);
            else return wrap(additionalTanks[value - tanks]);
        });

        this.allowSameFluidFill = allowSameFluidFill;
    }

    @Override
    public @NotNull List<Entry> getFluidTanks() {
        return Collections.unmodifiableList(Arrays.asList(this.tanks));
    }

    @Override
    public int size() {
        return tanks.length;
    }

    @Override
    public @NotNull Entry getTankAt(int index) {
        return tanks[index];
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return this.tanks;
    }

    @Override
    public boolean allowSameFluidFill() {
        return allowSameFluidFill;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0)
            return 0;

        FluidStack copy = resource.copy();

        int totalInserted = 0;

        Entry[] fluidTanks = this.tanks.clone();
        Arrays.sort(fluidTanks, ENTRY_COMPARATOR);

        boolean overflow = false;

        // search for tanks with same fluid type first
        for (Entry tank : fluidTanks) {
            boolean empty = tank.getFluidAmount() == 0;

            // if we still have fluid to insert, loop through empty tanks until we find one that can accept the fluid
            if (empty) {
                // if the tank uses distinct fluid fill (allowSameFluidFill disabled) and another distinct tank had
                // received the fluid, skip this tank
                if (overflow && !tank.allowSameFluidFill()) continue;
                // if simulating, pass in the original resource since nothing was actually filled,
                // otherwise pass in the mutated copy
                if (!tank.canFillFluidType(doFill ? copy : resource)) continue;

                // if not empty fluid doesn't match, skip
            } else if (!resource.isFluidEqual(tank.getFluid()))
                continue;

            // if the fluid to insert matches the tank, insert the fluid
            int inserted = tank.fill(copy, doFill);
            if (inserted <= 0) continue;

            totalInserted += inserted;
            copy.amount -= inserted;
            if (copy.amount <= 0) return totalInserted;
            else if (empty) overflow = true;
        }

        // return the amount of fluid that was inserted
        return totalInserted;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0)
            return null;

        int amountLeft = resource.amount;
        FluidStack totalDrained = null;
        for (IFluidTank handler : tanks) {
            if (!resource.isFluidEqual(handler.getFluid())) {
                continue;
            }
            FluidStack drain = handler.drain(amountLeft, doDrain);
            if (drain != null) {
                if (totalDrained == null) {
                    totalDrained = drain;
                } else {
                    totalDrained.amount += drain.amount;
                }
                amountLeft -= drain.amount;
                if (amountLeft <= 0) {
                    return totalDrained;
                }
            }
        }
        return totalDrained;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (maxDrain <= 0) return null;

        FluidStack totalDrained = null;
        for (IFluidTank handler : tanks) {
            if (totalDrained == null) {
                var drained = handler.drain(maxDrain, doDrain);
                if (drained == null) continue;

                totalDrained = drained.copy();
                maxDrain -= totalDrained.amount;

            } else {
                if (!totalDrained.isFluidEqual(handler.getFluid()))
                    continue;

                FluidStack drain = handler.drain(maxDrain, doDrain);
                if (drain == null) continue;

                totalDrained.amount += drain.amount;
                maxDrain -= drain.amount;
            }
            if (maxDrain <= 0) return totalDrained;
        }
        return totalDrained;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound fluidInventory = new NBTTagCompound();
        NBTTagList tanks = new NBTTagList();
        for (Entry tank : this.tanks) {
            tanks.appendTag(tank.serializeNBT());
        }
        fluidInventory.setTag("Tanks", tanks);
        return fluidInventory;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList tanks = nbt.getTagList("Tanks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tanks.tagCount(); i++) {
            this.tanks[i].deserializeNBT(tanks.getCompoundTagAt(i));
        }
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean lineBreak) {
        StringBuilder stb = new StringBuilder("FluidTankList[").append(this.tanks.length).append(";");
        for (int i = 0; i < this.tanks.length; i++) {
            if (i != 0) stb.append(',');
            stb.append(lineBreak ? "\n  " : " ");

            FluidStack fluid = this.tanks[i].getFluid();
            if (fluid == null || fluid.amount == 0) {
                stb.append("None 0 / ").append(this.tanks[i].getCapacity());
            } else {
                stb.append(fluid.getFluid().getName()).append(' ').append(fluid.amount)
                        .append(" / ").append(this.tanks[i].getCapacity());
            }
        }
        if (lineBreak) stb.append('\n');
        return stb.append(']').toString();
    }
}
