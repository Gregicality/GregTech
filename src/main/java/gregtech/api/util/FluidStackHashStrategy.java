package gregtech.api.util;

import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.Hash;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A configurable generator of hashing strategies, allowing for consideration of select properties of FluidStacks when
 * considering equality.
 */
public interface FluidStackHashStrategy extends Hash.Strategy<FluidStack> {

    /**
     * @return a builder object for producing a custom FluidStackHashStrategy.
     */
    static FluidStackHashStrategyBuilder builder() {
        return new FluidStackHashStrategyBuilder();
    }

    /**
     * Generates an FluidStackHash configured to compare every aspect of FluidStacks.
     *
     * @return the FluidStackHashStrategy as described above.
     */
    static FluidStackHashStrategy comparingAll() {
        return builder().compareFluid(true)
                .compareAmount(true)
                .compareTag(true)
                .build();
    }

    /**
     * Generates a FluidStackHash configured to compare every aspect of FluidStacks except the amount
     * of fluid in the stack.
     *
     * @return the FluidStackHashStrategy as described above.
     */
    static FluidStackHashStrategy comparingAllButAmount() {
        return builder().compareFluid(true)
                .compareTag(true)
                .build();
    }

    static FluidStackHashStrategy comparingFluidAndAmount() {
        return builder().compareFluid(true)
                .compareAmount(true)
                .build();
    }

    /**
     * Builder pattern class for generating customized FluidStackHashStrategy
     */
    class FluidStackHashStrategyBuilder {

        private boolean fluid, amount, tag;

        /**
         * Defines whether the Fluid type should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public FluidStackHashStrategyBuilder compareFluid(boolean choice) {
            fluid = choice;
            return this;
        }

        /**
         * Defines whether fluid amount should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public FluidStackHashStrategyBuilder compareAmount(boolean choice) {
            amount = choice;
            return this;
        }

        /**
         * Defines whether NBT Tags should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public FluidStackHashStrategyBuilder compareTag(boolean choice) {
            tag = choice;
            return this;
        }

        /**
         * @return the FluidStackHashStrategy as configured by "compare" methods.
         */
        public FluidStackHashStrategy build() {
            return new FluidStackHashStrategy() {

                @Override
                public int hashCode(@Nullable FluidStack o) {
                    return o == null || o.amount == 0 ? 0 : Objects.hash(
                            fluid ? o.getFluid() : null,
                            amount ? o.amount : null,
                            tag ? o.tag : null);
                }

                @Override
                public boolean equals(@Nullable FluidStack a, @Nullable FluidStack b) {
                    if (a == null || a.amount == 0) return b == null || b.amount == 0;
                    if (b == null || b.amount == 0) return false;

                    return (!fluid || a.getFluid() == b.getFluid()) &&
                            (!amount || a.amount == b.amount) &&
                            (!tag || Objects.equals(a.tag, b.tag));
                }
            };
        }
    }
}
