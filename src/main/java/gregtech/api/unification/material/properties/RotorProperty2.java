package gregtech.api.unification.material.properties;

import com.google.common.base.Preconditions;

import gregtech.common.metatileentities.multi.electric.generator.turbine.TurbineType;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class RotorProperty2 implements IMaterialProperty {

    private final float[] flowMultipliers;
    private int durability;
    private int baseEfficiency;
    private int optimalFlow;
    private int overflowMultiplier;

    /**
     * @param durability         the total durability of the rotor
     * @param baseEfficiency     the base fuel efficiency of the rotor, 100% = 10_000
     * @param optimalFlow        the base optimal flow rate in mB/t
     * @param overflowMultiplier the multiplier for the maximum amount of flow rate above the optimal flow rate
     * @param flowMultipliers    the flow multipliers for each turbine type, determines actual optimal flow rate
     */
    public RotorProperty2(int durability, int baseEfficiency, int optimalFlow, int overflowMultiplier,
                          float @NotNull [] flowMultipliers) {
        Preconditions.checkArgument(flowMultipliers.length == TurbineType.size());
        this.durability = durability;
        this.baseEfficiency = baseEfficiency;
        this.optimalFlow = optimalFlow;
        this.overflowMultiplier = overflowMultiplier;
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

    public int optimalFlow() {
        return optimalFlow;
    }

    public void setOptimalFlow(int optimalFlow) {
        this.optimalFlow = optimalFlow;
    }

    public int overflowMultiplier() {
        return overflowMultiplier;
    }

    public void setOverflowMultiplier(int overflowMultiplier) {
        this.overflowMultiplier = overflowMultiplier;
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
        private int overflowMultiplier;

        public Builder() {
            Arrays.fill(flowMultipliers, 1.0F);
        }

        public @NotNull Builder durability(int durability) {
            this.durability = durability;
            return this;
        }

        public @NotNull Builder baseEfficiency(int baseEfficiency) {
            this.baseEfficiency = baseEfficiency;
            return this;
        }

        public @NotNull Builder optimalFlow(int optimalFlow) {
            this.optimalFlow = optimalFlow;
            return this;
        }

        public @NotNull Builder overflowMultiplier(int overflowMultiplier) {
            this.overflowMultiplier = overflowMultiplier;
            return this;
        }

        public @NotNull Builder flowMultiplier(@NotNull TurbineType type, float flowMultiplier) {
            flowMultipliers[type.id()] = flowMultiplier;
            return this;
        }

        public @NotNull Builder legacyStats(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
            this.optimalFlow = (int) (harvestSpeed * 50);
            this.baseEfficiency = (int) (attackDamage * 1000 + 5000);
            this.durability = durability;
            this.overflowMultiplier = computeOverflowMultiplier(harvestLevel);
            return this;
        }

        public @NotNull RotorProperty2 build() {
            return new RotorProperty2(optimalFlow, baseEfficiency, durability, overflowMultiplier, flowMultipliers);
        }

        private static int computeOverflowMultiplier(int harvestLevel) {
            if (harvestLevel >= 6) {
                return 3;
            }
            if (harvestLevel >= 3) {
                return 2;
            }
            return 1;
        }
    }
}
