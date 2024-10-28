package gregtech.api.capability;

import gregtech.api.items.metaitem.stats.TurbineRotor;
import gregtech.common.metatileentities.multi.electric.generator.turbine.RotorFit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RotorHolder {

    /**
     * @return the turbine rotor contained
     */
    @Nullable
    TurbineRotor rotor();

    /**
     * @return if the rotor is obstructed
     */
    boolean isObstructed();

    /**
     * Call Server-Side.
     *
     * @param spinning if the rotor should be spinning or not
     */
    void setSpinning(boolean spinning);

    @NotNull
    RotorFit rotorFitting();

    /**
     * @param amount the amount to damage by
     * @return if the rotor was destroyed
     */
    boolean damageRotor(int amount);
}
