package gregtech.api.util;

import gregtech.api.capability.IMultipleTankHandler;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simulates consecutive fills to {@link IMultipleTankHandler} instance.
 */
public class OverlayedFluidHandler {

    private final List<OverlayedTank> overlayedTanks;

    public OverlayedFluidHandler(@NotNull IMultipleTankHandler tank) {
        this.overlayedTanks = new ArrayList<>();
        var entries = tank.getFluidTanks().toArray(new IMultipleTankHandler.ITankEntry[0]);
        Arrays.sort(entries, IMultipleTankHandler.ENTRY_COMPARATOR);
        for (var fluidTank : entries) {
            for (IFluidTankProperties property : fluidTank.getTankProperties()) {
                this.overlayedTanks.add(new OverlayedTank(property, fluidTank.allowSameFluidFill()));
            }
        }
    }

    /**
     * Resets the internal state back to the state when the handler was
     * first mirrored.
     */
    public void reset() {
        for (OverlayedTank overlayedTank : this.overlayedTanks) {
            overlayedTank.reset();
        }
    }

    /**
     * Simulate fluid insertion to the fluid tanks.
     *
     * @param fluid          Fluid
     * @param amountToInsert Amount of the fluid to insert
     * @return Amount of fluid inserted into tanks
     */
    public int insertFluid(@NotNull FluidStack fluid, int amountToInsert) {
        if (amountToInsert <= 0) {
            return 0;
        }
        int totalInserted = 0;
        // flag value indicating whether the fluid was stored in 'distinct' slot at least once
        boolean distinctFillPerformed = false;

        // search for tanks with same fluid type first
        for (OverlayedTank overlayedTank : this.overlayedTanks) {
            // if the fluid to insert matches the tank, insert the fluid
            if (fluid.isFluidEqual(overlayedTank.fluid)) {
                int inserted = overlayedTank.tryInsert(fluid, amountToInsert);
                if (inserted > 0) {
                    totalInserted += inserted;
                    amountToInsert -= inserted;
                    if (amountToInsert <= 0) {
                        return totalInserted;
                    }
                }
                // regardless of whether the insertion succeeded, presence of identical fluid in
                // a slot prevents distinct fill to other slots
                if (!overlayedTank.allowSameFluidFill) {
                    distinctFillPerformed = true;
                }
            }
        }
        // if we still have fluid to insert, loop through empty tanks until we find one that can accept the fluid
        for (OverlayedTank overlayedTank : this.overlayedTanks) {
            // if the tank uses distinct fluid fill (allowSameFluidFill disabled) and another distinct tank had
            // received the fluid, skip this tank
            if ((!distinctFillPerformed || overlayedTank.allowSameFluidFill) &&
                    overlayedTank.isEmpty() &&
                    overlayedTank.property.canFillFluidType(fluid)) {
                int inserted = overlayedTank.tryInsert(fluid, amountToInsert);
                if (inserted > 0) {
                    totalInserted += inserted;
                    amountToInsert -= inserted;
                    if (amountToInsert <= 0) {
                        return totalInserted;
                    }
                    if (!overlayedTank.allowSameFluidFill) {
                        distinctFillPerformed = true;
                    }
                }
            }
        }
        // return the amount of fluid that was inserted
        return totalInserted;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean lineBreak) {
        StringBuilder stb = new StringBuilder("OverlayedFluidHandler[").append(this.overlayedTanks.size()).append(";");
        if (lineBreak) stb.append("\n  ");
        for (int i = 0; i < this.overlayedTanks.size(); i++) {
            if (i != 0) stb.append(',');
            if (lineBreak) stb.append("\n  ");

            OverlayedTank overlayedTank = this.overlayedTanks.get(i);
            FluidStack fluid = overlayedTank.fluid;
            if (fluid == null || fluid.amount == 0) {
                stb.append("None 0 / ").append(overlayedTank.property.getCapacity());
            } else {
                stb.append(fluid.getFluid().getName()).append(' ').append(fluid.amount)
                        .append(" / ").append(overlayedTank.property.getCapacity());
            }
        }
        if (lineBreak) stb.append('\n');
        return stb.append(']').toString();
    }

    private static class OverlayedTank {

        private final IFluidTankProperties property;
        private final boolean allowSameFluidFill;

        @Nullable
        private FluidStack fluid;

        OverlayedTank(@NotNull IFluidTankProperties property, boolean allowSameFluidFill) {
            this.property = property;
            this.allowSameFluidFill = allowSameFluidFill;
            reset();
        }

        public boolean isEmpty() {
            return fluid == null || fluid.amount <= 0;
        }

        /**
         * Tries to insert set amount of fluid into this tank. If operation succeeds,
         * the content of this tank will be updated.
         * <b>
         * Note that this method does not check preexisting fluids for insertion.
         *
         * @param fluid  Fluid
         * @param amount Amount of the fluid to insert
         * @return Amount of fluid inserted into this tank
         */
        public int tryInsert(@NotNull FluidStack fluid, int amount) {
            if (this.fluid == null) {
                this.fluid = fluid.copy();
                return this.fluid.amount = Math.min(this.property.getCapacity(), amount);
            } else {
                int maxInsert = Math.min(this.property.getCapacity() - this.fluid.amount, amount);
                if (maxInsert > 0) {
                    this.fluid.amount += maxInsert;
                    return maxInsert;
                } else return 0;
            }
        }

        public void reset() {
            FluidStack fluid = this.property.getContents();
            this.fluid = fluid != null ? fluid.copy() : null;
        }
    }
}
