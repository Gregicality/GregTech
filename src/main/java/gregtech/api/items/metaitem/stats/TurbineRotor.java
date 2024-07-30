package gregtech.api.items.metaitem.stats;

import gregtech.common.metatileentities.multi.electric.generator.turbine.TurbineType;

import org.jetbrains.annotations.NotNull;

public interface TurbineRotor {

    int color();

    /**
     * @return the efficiency of the turbine
     */
    int baseEfficiency();

    /**
     * @return the optimal flow of the turbine
     */
    int optimalFlow();

    /**
     * @return the overflow multiplier of the turbine
     */
    int overflowMultiplier();

    /**
     * @return the flow multiplier for the turbine
     */
    float flowMultiplier(@NotNull TurbineType type);
}
