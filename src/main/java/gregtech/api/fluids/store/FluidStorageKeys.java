package gregtech.api.fluids.store;

import gregtech.api.fluids.FluidState;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.PropertyKey;

import net.minecraftforge.fluids.FluidRegistry;

import java.util.function.UnaryOperator;

import static gregtech.api.util.GTUtility.gregtechId;

public final class FluidStorageKeys {

    public static final FluidStorageKey LIQUID = new FluidStorageKey(gregtechId("liquid"),
            MaterialIconType.liquid,
            UnaryOperator.identity(),
            m -> m.hasProperty(PropertyKey.DUST) ? "gregtech.fluid.liquid_generic" : "gregtech.fluid.generic",
            FluidState.LIQUID);

    public static final FluidStorageKey GAS = new FluidStorageKey(gregtechId("gas"),
            MaterialIconType.gas,
            s -> FluidRegistry.getFluid(s) == null ? s : "gas." + s,
            m -> {
                if (m.hasProperty(PropertyKey.DUST)) {
                    return "gregtech.fluid.gas_vapor";
                }
                if (m.isElement() || (m.hasProperty(PropertyKey.FLUID) &&
                        m.getProperty(PropertyKey.FLUID).getStorage().getQueuedBuilder(FluidStorageKeys.LIQUID) !=
                                null)) {
                    return "gregtech.fluid.gas_generic";
                }
                return "gregtech.fluid.generic";
            },
            FluidState.GAS);

    public static final FluidStorageKey PLASMA = new FluidStorageKey(gregtechId("plasma"),
            MaterialIconType.plasma,
            s -> "plasma." + s, m -> "gregtech.fluid.plasma",
            FluidState.PLASMA, -1);

    private FluidStorageKeys() {}
}
