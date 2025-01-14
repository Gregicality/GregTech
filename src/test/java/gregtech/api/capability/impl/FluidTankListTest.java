package gregtech.api.capability.impl;

import gregtech.Bootstrap;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.OverlayedFluidHandler;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static net.minecraftforge.fluids.FluidRegistry.LAVA;
import static net.minecraftforge.fluids.FluidRegistry.WATER;

public class FluidTankListTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void testSimpleFills() {
        new FluidHandlerTester(false,
                new FluidTank(1000))
                        .beginSimulation()
                        .fill(WATER, 1000)
                        .expectContents(new FluidStack(WATER, 1000));

        new FluidHandlerTester(false,
                new FluidTank(1000))
                        .beginSimulation()
                        .fill(WATER, 333)
                        .expectContents(new FluidStack(WATER, 333))
                        .fill(WATER, 333)
                        .expectContents(new FluidStack(WATER, 666))
                        .fill(WATER, 333)
                        .expectContents(new FluidStack(WATER, 999))
                        .fill(WATER, 333)
                        .expectContents(new FluidStack(WATER, 1000));

        new FluidHandlerTester(false,
                new FluidTank(1000),
                new FluidTank(1000))
                        .beginSimulation()
                        .fill(WATER, 333)
                        .expectContents(new FluidStack(WATER, 333), null)
                        .fill(WATER, 333)
                        .expectContents(new FluidStack(WATER, 666), null)
                        .fill(WATER, 333)
                        .expectContents(new FluidStack(WATER, 999), null)
                        .fill(WATER, 333)
                        .expectContents(new FluidStack(WATER, 1000), null);

        new FluidHandlerTester(true,
                new FluidTank(1000),
                new FluidTank(1000))
                        .beginSimulation()
                        .fill(WATER, 333)
                        .expectContents(new FluidStack(WATER, 333), null)
                        .fill(WATER, 333)
                        .expectContents(new FluidStack(WATER, 666), null)
                        .fill(WATER, 333)
                        .expectContents(new FluidStack(WATER, 999), null)
                        .fill(WATER, 333)
                        .expectContents(new FluidStack(WATER, 1000), new FluidStack(WATER, 332));

        new FluidHandlerTester(false,
                new FluidTank(1000),
                new FluidTank(1000))
                        .beginSimulation()
                        .fill(WATER, 1500)
                        .expectContents(new FluidStack(WATER, 1000), null);

        new FluidHandlerTester(true,
                new FluidTank(1000),
                new FluidTank(1000))
                        .beginSimulation()
                        .fill(WATER, 1500)
                        .expectContents(new FluidStack(WATER, 1000), new FluidStack(WATER, 500));
    }

    @Test
    public void testMultipleFluidFills() {
        new FluidHandlerTester(false,
                new FluidTank(1000),
                new FluidTank(1000),
                new FluidTank(1000))
                        .fill(WATER, 800)
                        .fill(WATER, 800)
                        .fill(LAVA, 800)
                        .expectContents(
                                new FluidStack(WATER, 1000),
                                new FluidStack(LAVA, 800),
                                null)
                        .drain(WATER, 1000)
                        .expectContents(
                                null,
                                new FluidStack(LAVA, 800),
                                null)
                        .fill(LAVA, 800)
                        .expectContents(
                                null,
                                new FluidStack(LAVA, 1000),
                                null)
                        .fill(LAVA, 800)
                        .expectContents(
                                null,
                                new FluidStack(LAVA, 1000),
                                null);

        new FluidHandlerTester(true,
                new FluidTank(1000),
                new FluidTank(1000),
                new FluidTank(1000))
                        .fill(WATER, 800)
                        .fill(WATER, 800)
                        .fill(LAVA, 800)
                        .expectContents(
                                new FluidStack(WATER, 1000),
                                new FluidStack(WATER, 600),
                                new FluidStack(LAVA, 800))
                        .drain(WATER, 1600)
                        .expectContents(
                                null,
                                null,
                                new FluidStack(LAVA, 800))
                        .fill(LAVA, 800)
                        .expectContents(
                                new FluidStack(LAVA, 600),
                                null,
                                new FluidStack(LAVA, 1000))
                        .fill(LAVA, 800)
                        .expectContents(
                                new FluidStack(LAVA, 1000),
                                new FluidStack(LAVA, 400),
                                new FluidStack(LAVA, 1000))
                        .fill(WATER, 69420)
                        .expectContents(
                                new FluidStack(LAVA, 1000),
                                new FluidStack(LAVA, 400),
                                new FluidStack(LAVA, 1000));
    }

    @Test
    public void testMixedSameFluidFill() {
        new FluidHandlerTester(new FluidTankList(true,
                new FluidTankList(false,
                        new FluidTank(1000),
                        new FluidTank(1000)),
                new FluidTank(1000),
                new FluidTank(1000))) // distinct slots first
                        .beginSimulation()
                        .fill(WATER, 800)
                        .fill(WATER, 800)
                        .fill(WATER, 800)
                        .expectContents(
                                new FluidStack(WATER, 1000),
                                null,
                                new FluidStack(WATER, 1000),
                                new FluidStack(WATER, 400));

        new FluidHandlerTester(new FluidTankList(false,
                new FluidTankList(true,
                        new FluidTank(1000),
                        new FluidTank(1000)),
                new FluidTank(1000),
                new FluidTank(1000))) // non-distinct slots first
                        .beginSimulation()
                        .fill(WATER, 800)
                        .fill(WATER, 800)
                        .fill(WATER, 800)
                        .expectContents(
                                new FluidStack(WATER, 1000),
                                new FluidStack(WATER, 1000),
                                new FluidStack(WATER, 400),
                                null);
    }

    @Test
    public void testDrain() {
        new FluidHandlerTester(true,
                new FluidTank(1000),
                new FluidTank(1000),
                new FluidTank(1000))
                        .fill(WATER, 1500)
                        .fill(LAVA, 500)
                        .expectContents(
                                new FluidStack(WATER, 1000),
                                new FluidStack(WATER, 500),
                                new FluidStack(LAVA, 500))
                        .drain(1000)
                        .expectContents(
                                null,
                                new FluidStack(WATER, 500),
                                new FluidStack(LAVA, 500))
                        .drain(1000)
                        .expectContents(
                                null,
                                null,
                                new FluidStack(LAVA, 500))
                        .drain(1000)
                        .expectContents(
                                null,
                                null,
                                null)
                        .fill(LAVA, 500)
                        .fill(WATER, 1500)
                        .expectContents(
                                new FluidStack(LAVA, 500),
                                new FluidStack(WATER, 1000),
                                new FluidStack(WATER, 500))
                        .drain(1000)
                        .expectContents(
                                null,
                                new FluidStack(WATER, 1000),
                                new FluidStack(WATER, 500))
                        .drain(500)
                        .expectContents(
                                null,
                                new FluidStack(WATER, 500),
                                new FluidStack(WATER, 500))
                        .drain(1000)
                        .expectContents(
                                null,
                                null,
                                null);
    }

    @Test
    public void testFilterOrdering() {
        SingleFluidFilter waterFilter = new SingleFluidFilter(new FluidStack(WATER, 1), false);
        SingleFluidFilter lavaFilter = new SingleFluidFilter(new FluidStack(LAVA, 1), false);
        SingleFluidFilter creosoteFilter = new SingleFluidFilter(new FluidStack(Materials.Creosote.getFluid(), 1),
                false);

        new FluidHandlerTester(false,
                new FilteredFluidHandler(1000).setFilter(waterFilter),
                new FilteredFluidHandler(1000).setFilter(lavaFilter),
                new FilteredFluidHandler(1000).setFilter(creosoteFilter))
                        .beginSimulation()
                        .fill(WATER, 800)
                        .fill(LAVA, 800)
                        .fill(WATER, 800)
                        .expectContents(
                                new FluidStack(WATER, 1000),
                                new FluidStack(LAVA, 800),
                                null);

        new FluidHandlerTester(true,
                new FilteredFluidHandler(1000).setFilter(waterFilter),
                new FilteredFluidHandler(1000).setFilter(lavaFilter),
                new FilteredFluidHandler(1000).setFilter(creosoteFilter))
                        .beginSimulation()
                        .fill(WATER, 800)
                        .fill(LAVA, 800)
                        .fill(WATER, 800)
                        .expectContents(
                                new FluidStack(WATER, 1000),
                                new FluidStack(LAVA, 800),
                                null);

        new FluidHandlerTester(true,
                new FilteredFluidHandler(1000).setFilter(waterFilter),
                new FluidTank(1000))
                        .beginSimulation()
                        .fill(WATER, 800)
                        .fill(LAVA, 800)
                        .expectContents(
                                new FluidStack(WATER, 800),
                                new FluidStack(LAVA, 800));

        new FluidHandlerTester(true,
                new FilteredFluidHandler(1000).setFilter(waterFilter),
                new FluidTank(1000))
                        .beginSimulation()
                        .fill(LAVA, 800)
                        .fill(WATER, 800)
                        .expectContents(
                                new FluidStack(WATER, 800),
                                new FluidStack(LAVA, 800));

        new FluidHandlerTester(true,
                new FluidTank(1000),
                new FilteredFluidHandler(1000).setFilter(waterFilter))
                        .beginSimulation()
                        .fill(WATER, 800)
                        .fill(LAVA, 800)
                        .expectContents(
                                new FluidStack(LAVA, 800),
                                new FluidStack(WATER, 800));

        new FluidHandlerTester(true,
                new FluidTank(1000),
                new FilteredFluidHandler(1000).setFilter(waterFilter))
                        .beginSimulation()
                        .fill(LAVA, 800)
                        .fill(WATER, 800)
                        .expectContents(
                                new FluidStack(LAVA, 800),
                                new FluidStack(WATER, 800));
    }

    private static final class FluidHandlerTester {

        private final FluidTankList tank;

        @Nullable
        private OverlayedFluidHandler overlayedFluidHandler;

        FluidHandlerTester(FluidTankList tank) {
            this.tank = tank;
        }

        FluidHandlerTester(boolean allowSameFluidFill, IFluidTank... tanks) {
            this(new FluidTankList(allowSameFluidFill, tanks));
        }

        FluidHandlerTester fill(Fluid fluid, int amount) {
            return fill(new FluidStack(fluid, amount));
        }

        FluidHandlerTester fill(FluidStack fluidStack) {
            // make string representation before modifying the state, to produce better error message
            String tankString = this.tank.toString(true);

            int tankFillSim = this.tank.fill(fluidStack, false);

            if (this.overlayedFluidHandler != null) {
                String overlayString = this.overlayedFluidHandler.toString(true);
                int ofhSim = this.overlayedFluidHandler.insertFluid(fluidStack, fluidStack.amount);

                if (tankFillSim != ofhSim) {
                    throw new AssertionError("Result of simulation fill from tank and OFH differ.\n" +
                            "Tank Simulation: " + tankFillSim + ", OFH simulation: " + ofhSim + "\n" +
                            "Tank: " + tankString + "\n" +
                            "OFH: " + overlayString);
                }
            }
            int actualFill = this.tank.fill(fluidStack, true);
            if (tankFillSim != actualFill) {
                throw new AssertionError("Simulation fill to tank and actual fill differ.\n" +
                        "Simulated Fill: " + tankFillSim + ", Actual Fill: " + actualFill + "\n" +
                        "Tank: " + tankString);
            }
            return this;
        }

        FluidHandlerTester drain(Fluid fluid, int amount) {
            return drain(new FluidStack(fluid, amount));
        }

        FluidHandlerTester drain(FluidStack fluidStack) {
            if (this.overlayedFluidHandler != null) {
                throw new IllegalStateException("Cannot drain stuff in simulation");
            }
            // make string representation before modifying the state, to produce better error message
            String tankString = this.tank.toString(true);

            FluidStack drainSim = this.tank.drain(fluidStack, false);
            FluidStack actualDrain = this.tank.drain(fluidStack, true);

            if (!eq(drainSim, actualDrain)) {
                throw new AssertionError("Simulation drain from tank and actual drain differ.\n" +
                        "Simulated Drain: " + ftos(drainSim) + ", Actual Drain: " + ftos(actualDrain) + "\n" +
                        "Tank: " + tankString);
            }
            return this;
        }

        FluidHandlerTester drain(int amount) {
            if (this.overlayedFluidHandler != null) {
                throw new IllegalStateException("Cannot drain stuff in simulation");
            }
            // make string representation before modifying the state, to produce better error message
            String tankString = this.tank.toString(true);

            FluidStack drainSim = this.tank.drain(amount, false);
            FluidStack actualDrain = this.tank.drain(amount, true);

            if (!eq(drainSim, actualDrain)) {
                throw new AssertionError("Simulation drain from tank and actual drain differ.\n" +
                        "Simulated Drain: " + ftos(drainSim) + ", Actual Drain: " + ftos(actualDrain) + "\n" +
                        "Tank: " + tankString);
            }
            return this;
        }

        FluidHandlerTester beginSimulation() {
            if (this.overlayedFluidHandler != null) {
                throw new IllegalStateException("Simulation already begun");
            }
            this.overlayedFluidHandler = new OverlayedFluidHandler(this.tank);
            return this;
        }

        FluidHandlerTester expectContents(@NotNull FluidStack... optionalFluidStacks) {
            if (optionalFluidStacks.length != this.tank.getTanks()) {
                throw new IllegalArgumentException("Wrong number of fluids to compare; " +
                        "expected: " + this.tank.getTanks() + ", provided: " + optionalFluidStacks.length);
            }
            for (int i = 0; i < optionalFluidStacks.length; i++) {
                var tank = this.tank.getTankAt(i);
                if (!eq(tank.getFluid(), optionalFluidStacks[i])) {
                    throw new AssertionError("Contents of the tank don't match expected state.\n" +
                            "Expected: [\n  " + Arrays.stream(optionalFluidStacks)
                                    .map(FluidHandlerTester::ftos)
                                    .collect(Collectors.joining(",\n  ")) +
                            "\n]\n" +
                            "Tank: " + this.tank.toString(true));
                }
            }
            return this;
        }

        static boolean eq(@Nullable FluidStack fluid1, @Nullable FluidStack fluid2) {
            if (fluid1 == null || fluid1.amount <= 0) {
                return fluid2 == null || fluid2.amount <= 0;
            } else {
                return fluid1.isFluidEqual(fluid2);
            }
        }

        static String ftos(@Nullable FluidStack fluid) {
            return fluid == null ? "Empty" : fluid.getFluid().getName() + " / " + fluid.amount;
        }
    }
}
