package gregtech.api.capability;

import gregtech.api.capability.impl.FluidTankList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for multi-tank fluid handlers. Handles insertion logic, along with other standard
 * {@link IFluidHandler} functionalities.
 *
 * @see FluidTankList FluidTankList
 */
public abstract class MultipleTankHandler implements IFluidHandler, Iterable<MultipleTankHandler.Entry>,
                                          INBTSerializable<NBTTagCompound> {

    /**
     * Comparator for entries that can be used in insertion logic
     */
    public static final Comparator<Entry> ENTRY_COMPARATOR = (o1, o2) -> {
        // #1: non-empty tank first
        boolean empty1 = o1.getFluidAmount() <= 0;
        boolean empty2 = o2.getFluidAmount() <= 0;
        if (empty1 != empty2) return empty1 ? 1 : -1;

        // #2: filter priority
        IFilter<FluidStack> filter1 = o1.getFilter();
        IFilter<FluidStack> filter2 = o2.getFilter();
        if (filter1 == null) return filter2 == null ? 0 : 1;
        if (filter2 == null) return -1;
        return IFilter.FILTER_COMPARATOR.compare(filter1, filter2);
    };

    /**
     * @return unmodifiable view of {@code Entry}s. Note that it's still possible to access
     *         and modify inner contents of the tanks.
     */
    @NotNull
    public abstract List<Entry> getFluidTanks();

    /**
     * @return Number of tanks in this tank handler
     */
    public abstract int size();

    @NotNull
    public abstract Entry getTankAt(int index);

    /**
     * @return {@code false} if insertion to this fluid handler enforces input to be
     *         filled in one slot at max. {@code true} if it bypasses the rule.
     */
    public abstract boolean allowSameFluidFill();

    /**
     * Tries to search tank with contents equal to {@code fluidStack}. If {@code fluidStack} is
     * {@code null}, an empty tank is searched instead.
     *
     * @param fluidStack Fluid stack to search index
     * @return Index corresponding to tank at {@link #getFluidTanks()} with matching
     */
    public final int getIndexOfFluid(@Nullable FluidStack fluidStack) {
        List<Entry> fluidTanks = getFluidTanks();
        for (int i = 0; i < fluidTanks.size(); i++) {
            FluidStack tankStack = fluidTanks.get(i).getFluid();
            if (fluidStack == tankStack || tankStack != null && tankStack.isFluidEqual(fluidStack)) {
                return i;
            }
        }
        return -1;
    }

    public int simulateFill(FluidStack stack, int amount) {
        var insertable = stack.copy();
        insertable.amount = amount;
        return fill(insertable, false);
    }

    @Override
    public final @NotNull Iterator<Entry> iterator() {
        return getFluidTanks().iterator();
    }

    protected final Entry wrap(IFluidTank tank) {
        return tank instanceof Entry ? (Entry) tank : new Entry(tank, this);
    }

    public MultipleTankHandler addTanks(IFluidTank... tanks) {
        if (ArrayUtils.isEmpty(tanks)) return this;
        return new FluidTankList(allowSameFluidFill(), this, tanks);
    }

    /**
     * Entry of multi fluid tanks. Retains reference to original {@link MultipleTankHandler} for accessing
     * information such as {@link MultipleTankHandler#allowSameFluidFill()}.
     */
    public static class Entry implements IFluidTank, IFilteredHandler<FluidStack>, INBTSerializable<NBTTagCompound>,
                              IFluidTankProperties {

        private final IFluidTank tank;
        private final MultipleTankHandler parent;

        private Entry(IFluidTank tank, MultipleTankHandler parent) {
            this.tank = tank;
            this.parent = parent;
        }

        public @NotNull MultipleTankHandler getParentHandler() {
            return parent;
        }

        public @NotNull IFluidTank getDelegate() {
            return tank;
        }

        public boolean allowSameFluidFill() {
            return getParentHandler().allowSameFluidFill();
        }

        @Nullable
        @Override
        public IFilter<FluidStack> getFilter() {
            return getDelegate() instanceof IFilteredFluidContainer filter ? filter.getFilter() : null;
        }

        @Nullable
        @Override
        public FluidStack getFluid() {
            return getDelegate().getFluid();
        }

        @Override
        public int getFluidAmount() {
            return getDelegate().getFluidAmount();
        }

        @Override
        public int getCapacity() {
            return getDelegate().getCapacity();
        }

        @Override
        public FluidTankInfo getInfo() {
            return getDelegate().getInfo();
        }

        @Override
        public FluidStack getContents() {
            return getFluid() == null ? null : getFluid().copy();
        }

        @Override
        public boolean canFill() {
            return true;
        }

        @Override
        public boolean canDrain() {
            return true;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack) {
            if (allowSameFluidFill() || fluidStack == null) return true;
            for (Entry tank : getParentHandler()) {
                // only consider other tanks that do not allow same fluid fill
                if (tank.allowSameFluidFill() || this == tank) continue;
                if (fluidStack.isFluidEqual(tank.getFluid())) {
                    return tank.getFluidAmount() + fluidStack.amount <= tank.getCapacity();
                }
            }
            return true;
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return getDelegate().fill(resource, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return getDelegate().drain(maxDrain, doDrain);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public NBTTagCompound serializeNBT() {
            if (getDelegate() instanceof FluidTank fluidTank) {
                return fluidTank.writeToNBT(new NBTTagCompound());
            } else if (getDelegate() instanceof INBTSerializable serializable) {
                return (NBTTagCompound) serializable.serializeNBT();
            }
            return new NBTTagCompound();
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            if (getDelegate() instanceof FluidTank fluidTank) {
                fluidTank.readFromNBT(nbt);
            } else if (getDelegate() instanceof INBTSerializable serializable) {
                serializable.deserializeNBT(nbt);
            }
        }
    }
}
