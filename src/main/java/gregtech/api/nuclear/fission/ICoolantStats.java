package gregtech.api.nuclear.fission;

import net.minecraftforge.fluids.Fluid;

public interface ICoolantStats {

    /**
     * @return The heated coolant fluid.
     */
    Fluid getHotCoolant();

    /**
     * @return The specific heat capacity of the fluid in J/(kg*K).
     */
    double getSpecificHeatCapacity();

    /**
     * @return A factor relating to the neutron moderation properties; the higher the factor, the fewer neutrons pass
     *         through.
     */
    double getModeratorFactor();

    /**
     * @return A rough heat transfer coefficient for the fluid.
     */
    double getCoolingFactor();

    /**
     * @return The boiling point of the fluid in Kelvin.
     */
    double getBoilingPoint();

    /**
     * @return The heat of vaporization of the fluid in J/(kg*K).
     */
    double getHeatOfVaporization();

    /**
     * @return If the coolant reacts with zirconium cladding at high temperatures. This is true for mostly water-based
     *         coolants.
     */
    boolean accumulatesHydrogen();

    /**
     * @return The mass of the coolant per liter in kg
     */
    double getMass();
}
