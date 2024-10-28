package gregtech.api.unification.material.properties;

import gregtech.common.metatileentities.multi.electric.generator.turbine.TurbineType;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class RotorProperty2 implements IMaterialProperty {

    private final float[] flowMultipliers;
    private int durability;
    private int baseEfficiency;
    private long optimalFlow;
    private int overflowEfficiency;

    /**
     * @param durability         the total durability of the rotor
     * @param baseEfficiency     the base fuel efficiency of the rotor, 100% = 10_000
     * @param optimalFlow        the base optimal flow rate in EU/t
     * @param overflowEfficiency the multiplier for the maximum amount of flow rate above the optimal flow rate
     * @param flowMultipliers    the flow multipliers for each turbine type, determines actual optimal flow rate
     */
    public RotorProperty2(int durability, int baseEfficiency, long optimalFlow, int overflowEfficiency,
                          float @NotNull [] flowMultipliers) {
        Preconditions.checkArgument(flowMultipliers.length == TurbineType.size());
        this.durability = durability;
        this.baseEfficiency = baseEfficiency;
        this.optimalFlow = optimalFlow;
        this.overflowEfficiency = overflowEfficiency;
        this.flowMultipliers = flowMultipliers;
    }

    public float baseEfficiencyDisplay() {
        return baseEfficiency / 100.0F;
    }

    public float flowMultiplier(@NotNull TurbineType type) {
        return flowMultipliers[type.id()];
    }

    public void setFlowMultiplier(@NotNull TurbineType type, float flowMultiplier) {
        flowMultipliers[type.id()] = flowMultiplier;
    }

    public int durability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public int baseEfficiency() {
        return baseEfficiency;
    }

    public void setBaseEfficiency(int baseEfficiency) {
        this.baseEfficiency = baseEfficiency;
    }

    public long optimalFlow() {
        return optimalFlow;
    }

    public void setOptimalFlow(long optimalFlow) {
        this.optimalFlow = optimalFlow;
    }

    public int overflowEfficiency() {
        return overflowEfficiency;
    }

    public void setOverflowEfficiency(int overflowEfficiency) {
        this.overflowEfficiency = overflowEfficiency;
    }

    @Override
    public void verifyProperty(@NotNull MaterialProperties properties) {
        properties.ensureSet(PropertyKey.INGOT, true);
    }

    public static class Builder {

        private final float[] flowMultipliers = new float[TurbineType.size()];

        private int durability;
        private int baseEfficiency;
        private int optimalFlow;
        private int overflowEfficiency;

        public Builder() {
            Arrays.fill(flowMultipliers, 1.0F);
        }

        /**
         * @param durability the base durability of the rotor
         * @return this
         */
        public @NotNull Builder durability(int durability) {
            this.durability = durability;
            return this;
        }

        /**
         * @param baseEfficiency the base energy efficiency of the rotor
         * @return this
         */
        public @NotNull Builder baseEfficiency(int baseEfficiency) {
            this.baseEfficiency = baseEfficiency;
            return this;
        }

        /**
         * @param optimalFlow the optimal EU/t rate
         * @return this
         */
        public @NotNull Builder optimalFlow(int optimalFlow) {
            this.optimalFlow = optimalFlow;
            return this;
        }

        /**
         * @param overflowEfficiency the efficiency of fuel beyond the optimal rate
         * @return this
         */
        public @NotNull Builder overflowEfficiency(int overflowEfficiency) {
            this.overflowEfficiency = overflowEfficiency;
            return this;
        }

        /**
         * Specify the optimal flow for a specific turbine based on a multiplier
         *
         * @param type           the type of turbine to change optimal flow for
         * @param flowMultiplier the multiplier
         * @return this
         */
        public @NotNull Builder flowMultiplier(@NotNull TurbineType type, float flowMultiplier) {
            flowMultipliers[type.id()] = flowMultiplier;
            return this;
        }

        /**
         * Convert legacy stats into modern stats
         *
         * @return this
         */
        @Deprecated
        public @NotNull Builder legacyStats(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
            this.optimalFlow = (int) (harvestSpeed * 50);
            this.baseEfficiency = (int) (attackDamage * 1000 + 5000);
            this.durability = durability;
            this.overflowEfficiency = harvestLevel >= 6 ? 3 : harvestLevel >= 3 ? 2 : 1;
            return this;
        }

        public @NotNull RotorProperty2 build() {
            return new RotorProperty2(optimalFlow, baseEfficiency, durability, overflowEfficiency, flowMultipliers);
        }
    }
}
