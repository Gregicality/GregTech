package gregtech.api.items.metaitem.stats;

import gregtech.common.metatileentities.multi.electric.generator.turbine.TurbineType;

import org.jetbrains.annotations.NotNull;

public interface TurbineRotor {

    int color();

    /**
     * @return the energy efficiency of the turbine
     */
    int baseEfficiency();

    /**
     * @return the optimal energy flow of the turbine in EU/t
     */
    long optimalFlow();

    /**
     * @return the overflow efficiency of the turbine
     */
    int overflowEfficiency();

    /**
     * @return the flow multiplier for the turbine
     */
    float flowMultiplier(@NotNull TurbineType type);
}
