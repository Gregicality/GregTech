package gregtech.api.util;

import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.Hash;

import java.util.Objects;

public interface FluidStackHashStrategy extends Hash.Strategy<FluidStack> {

    static FluidStackHashStrategyBuilder builder() {
        return new FluidStackHashStrategyBuilder();
    }

    FluidStackHashStrategy comparingAll = builder()
            .compareFluid()
            .compareAmount()
            .compareNBT()
            .build();

    FluidStackHashStrategy comparingAllButAmount = builder()
            .compareFluid()
            .compareNBT()
            .build();

    class FluidStackHashStrategyBuilder {

        private boolean fluid, amount, nbt = false;

        public FluidStackHashStrategyBuilder compareFluid() {
            this.fluid = true;
            return this;
        }

        public FluidStackHashStrategyBuilder compareAmount() {
            this.amount = true;
            return this;
        }

        public FluidStackHashStrategyBuilder compareNBT() {
            this.nbt = true;
            return this;
        }

        public FluidStackHashStrategy build() {
            return new FluidStackHashStrategy() {

                @Override
                public int hashCode(FluidStack other) {
                    return other == null ? 0 : Objects.hash(
                            fluid ? other.getFluid() : null,
                            amount ? other.amount : null,
                            nbt ? other.tag : null);
                }

                @Override
                public boolean equals(FluidStack a, FluidStack b) {
                    if (a == null) return b == null;
                    if (b == null) return false;

                    return (!fluid || a.getFluid() == b.getFluid()) &&
                            (!amount || a.amount == b.amount) &&
                            (!nbt || Objects.equals(a.tag, b.tag));
                }
            };
        }
    }
}
