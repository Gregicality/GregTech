package gregtech.api.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for multi-tank fluid handlers. Handles insertion logic, along with other standard
 * {@link IFluidHandler} functionalities.
 *
 * @see gregtech.api.capability.impl.FluidTankList FluidTankList
 */
public interface IMultipleTankHandler extends IFluidHandler, Iterable<IMultipleTankHandler.ITankEntry> {

    /**
     * Comparator for entries that can be used in insertion logic
     */
    Comparator<ITankEntry> ENTRY_COMPARATOR = (o1, o2) -> {
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
     * @return unmodifiable view of {@code MultiFluidTankEntry}s. Note that it's still possible to access
     *         and modify inner contents of the tanks.
     */
    @NotNull
    List<ITankEntry> getFluidTanks();

    /**
     * @return Number of tanks in this tank handler
     */
    int getTanks();

    @NotNull
    ITankEntry getTankAt(int index);

    /**
     * @return {@code false} if insertion to this fluid handler enforces input to be
     *         filled in one slot at max. {@code true} if it bypasses the rule.
     */
    boolean allowSameFluidFill();

    /**
     * Tries to search tank with contents equal to {@code fluidStack}. If {@code fluidStack} is
     * {@code null}, an empty tank is searched instead.
     *
     * @param fluidStack Fluid stack to search index
     * @return Index corresponding to tank at {@link #getFluidTanks()} with matching
     */
    default int getIndexOfFluid(@Nullable FluidStack fluidStack) {
        List<ITankEntry> fluidTanks = getFluidTanks();
        for (int i = 0; i < fluidTanks.size(); i++) {
            FluidStack tankStack = fluidTanks.get(i).getFluid();
            if (fluidStack == tankStack || tankStack != null && tankStack.isFluidEqual(fluidStack)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    default Iterator<ITankEntry> iterator() {
        return getFluidTanks().iterator();
    }

    /**
     * Entry of multi fluid tanks. Retains reference to original {@link IMultipleTankHandler} for accessing
     * information such as {@link IMultipleTankHandler#allowSameFluidFill()}.
     */
    interface ITankEntry extends IFluidTank, IFluidHandler, IFilteredFluidContainer, INBTSerializable<NBTTagCompound> {

        @NotNull
        IMultipleTankHandler getParent();

        @NotNull
        IFluidTank getDelegate();

        default boolean allowSameFluidFill() {
            return getParent().allowSameFluidFill();
        }

        default IFilter<FluidStack> getFilter() {
            return getDelegate() instanceof IFilteredFluidContainer filtered ? filtered.getFilter() : null;
        }

        @Override
        default NBTTagCompound serializeNBT() {
            if (getDelegate() instanceof FluidTank fluidTank) {
                return fluidTank.writeToNBT(new NBTTagCompound());
            } else if (getDelegate() instanceof INBTSerializable<?>serializable) {
                if (serializable.serializeNBT() instanceof NBTTagCompound compound) {
                    return compound;
                }
            }
            return new NBTTagCompound();
        }

        @Override
        @SuppressWarnings("unchecked")
        default void deserializeNBT(NBTTagCompound nbt) {
            if (getDelegate() instanceof FluidTank fluidTank) {
                fluidTank.readFromNBT(nbt);
            } else if (getDelegate() instanceof INBTSerializable<?>serializable) {
                try {
                    ((INBTSerializable<NBTBase>) serializable).deserializeNBT(nbt);
                } catch (ClassCastException ignored) {}
            }
        }

        @Nullable
        @Override
        default FluidStack getFluid() {
            return getDelegate().getFluid();
        }

        @Override
        default int getFluidAmount() {
            return getDelegate().getFluidAmount();
        }

        @Override
        default int getCapacity() {
            return getDelegate().getCapacity();
        }

        @Override
        default FluidTankInfo getInfo() {
            return getDelegate().getInfo();
        }
    }
}
