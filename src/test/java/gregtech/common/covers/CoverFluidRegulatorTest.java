package gregtech.common.covers;

import gregtech.Bootstrap;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class CoverFluidRegulatorTest {

    public static final Predicate<FluidStack> isWater = fs -> fs.getFluid() == FluidRegistry.WATER;

    /**
     * Required. Without this all item-related operations will fail because registries haven't been initialized.
     */
    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void doKeepExact_does_nothing_if_no_destination_tank_exists() {
        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        FluidStack water = new FluidStack(FluidRegistry.WATER, 1234);

        // Source consists of only an output tank containing a bit of water
        IFluidHandler source = new FluidHandlerProxy(new FluidTankList(false),
                new FluidTankList(false, new FluidTank(water.copy(), 64000)));

        // Tell it to keep exact from a machine with an empty fluid tank and null target fluid tank
        int amountTransferred = cfr.doKeepExact(1000, source, null, isWater, 1000, true);

        MatcherAssert.assertThat("Unexpectedly moved fluids, nothing is supposed to happen", amountTransferred, is(0));
    }

    @Test
    public void doKeepExact_moves_one_fluid_into_an_empty_tank() {
        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        FluidStack water = new FluidStack(FluidRegistry.WATER, 1234);

        IFluidHandler source = new FluidHandlerProxy(new FluidTankList(false),
                new FluidTankList(false, new FluidTank(water.copy(), 64000)));

        // Dest consists of one empty input tank
        IFluidHandler dest = new FluidHandlerProxy(new FluidTankList(false, new FluidTank(64000)),
                new FluidTankList(false));

        // Tell it to keep exact from a machine with an empty fluid tank and no target fluid tank
        int amountTransferred = cfr.doKeepExact(1000, source, dest, isWater, 1000, true);

        MatcherAssert.assertThat("Wrong fluid amount moved", amountTransferred, is(1000));
    }

    @Test
    public void doKeepExact_moves_only_as_much_fluid_as_exists_in_the_source() {
        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        IFluidHandler source = new FluidHandlerProxy(new FluidTankList(false),
                new FluidTankList(false,
                        new FluidTank(new FluidStack(FluidRegistry.WATER, 1234), 64000)));

        IFluidHandler dest = new FluidHandlerProxy(new FluidTankList(false, new FluidTank(64000)),
                new FluidTankList(false));

        int amountTransferred = cfr.doKeepExact(10000, source, dest, isWater, 10000, true);

        MatcherAssert.assertThat("Wrong fluid amount moved", amountTransferred, is(1234));
    }

    @Test
    public void doKeepExact_moves_only_the_fluid_required_if_more_could_be_moved() {
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        IFluidHandler source = new FluidHandlerProxy(
                new FluidTankList(false),
                new FluidTankList(false,
                        new FluidTank(new FluidStack(FluidRegistry.WATER, 64000), 64000)));

        IFluidHandler dest = new FluidHandlerProxy(
                new FluidTankList(false,
                        new FluidTank(new FluidStack(FluidRegistry.WATER, 100), 64000)),
                new FluidTankList(false));

        int amountTransferred = cfr.doKeepExact(10000, source, dest, isWater, 144, true);

        MatcherAssert.assertThat("Wrong fluid amount moved", amountTransferred, is(44));
    }

    @Test
    public void doKeepExact_moves_multiple_valid_fluids() {
        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        IFluidHandler source = new FluidHandlerProxy(
                new FluidTankList(false),
                new FluidTankList(false,
                        new FluidTank(new FluidStack(FluidRegistry.WATER, 64000), 64000),
                        new FluidTank(new FluidStack(FluidRegistry.LAVA, 64000), 64000)));

        // One tank with 100mB water, another with nothing
        IFluidHandler dest = new FluidHandlerProxy(
                new FluidTankList(false,
                        new FluidTank(new FluidStack(FluidRegistry.WATER, 100), 64000),
                        new FluidTank(64000)),
                new FluidTankList(false));

        // accept any fluid this time
        int amountTransferred = cfr.doKeepExact(10000, source, dest, fs -> true, 144, true);

        // expect that 44mB of water and 144mB of lava will be moved
        MatcherAssert.assertThat("Wrong fluid amount moved", amountTransferred, is(44 + 144));

        // verify final fluid quantities
        MatcherAssert.assertThat(dest.getTankProperties().length, is(2));
        IFluidTankProperties tank1 = dest.getTankProperties()[0];
        IFluidTankProperties tank2 = dest.getTankProperties()[1];
        MatcherAssert.assertThat(tank1.getContents(), notNullValue());
        MatcherAssert.assertThat(tank2.getContents(), notNullValue());
        MatcherAssert.assertThat(tank1.getContents().isFluidStackIdentical(new FluidStack(FluidRegistry.WATER, 144)),
                is(true));
        MatcherAssert.assertThat(tank2.getContents().isFluidStackIdentical(new FluidStack(FluidRegistry.LAVA, 144)),
                is(true));
    }

    @Test
    public void doKeepExact_respects_transfer_limit_with_one_fluid() {
        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        // One output tank full of water
        IFluidHandler source = new FluidHandlerProxy(
                new FluidTankList(false),
                new FluidTankList(false,
                        new FluidTank(new FluidStack(FluidRegistry.WATER, 64000), 64000)));

        // One input tank with nothing in it
        IFluidHandler dest = new FluidHandlerProxy(
                new FluidTankList(false, new FluidTank(64000)),
                new FluidTankList(false));

        // accept any fluid this time
        int amountTransferred = cfr.doKeepExact(100, source, dest, fs -> true, 144, true);

        // expect that at most 100mB of fluids total will be moved this tick, as if possible it would do 144mB
        MatcherAssert.assertThat("Wrong fluid amount moved", amountTransferred, is(100));
    }

    @Test
    public void doKeepExact_respects_transfer_limit_with_multiple_fluids() {
        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        IFluidHandler source = new FluidHandlerProxy(
                new FluidTankList(false),
                new FluidTankList(false,
                        new FluidTank(new FluidStack(FluidRegistry.WATER, 64000), 64000),
                        new FluidTank(new FluidStack(FluidRegistry.LAVA, 64000), 64000)));

        // One tank with 100mB water, another with nothing
        IFluidHandler dest = new FluidHandlerProxy(
                new FluidTankList(false,
                        new FluidTank(new FluidStack(FluidRegistry.WATER, 100), 64000),
                        new FluidTank(64000)),
                new FluidTankList(false));

        // accept any fluid this time
        int amountTransferred = cfr.doKeepExact(100, source, dest, fs -> true, 144, true);

        // expect that at most 100mB of fluids total will be moved this tick, as if possible it would do 188mB
        MatcherAssert.assertThat("Wrong fluid amount moved", amountTransferred, is(100));
    }

    @Test
    public void doKeepExact_does_nothing_if_levels_are_already_correct_in_dest() {
        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        IFluidHandler source = new FluidHandlerProxy(
                new FluidTankList(false),
                new FluidTankList(false,
                        new FluidTank(new FluidStack(FluidRegistry.WATER, 64000), 64000),
                        new FluidTank(new FluidStack(FluidRegistry.LAVA, 64000), 64000)));

        // One tank with 144mB water, another with 144mB lava
        IFluidHandler dest = new FluidHandlerProxy(
                new FluidTankList(false,
                        new FluidTank(new FluidStack(FluidRegistry.WATER, 144), 64000),
                        new FluidTank(new FluidStack(FluidRegistry.LAVA, 144), 64000)),
                new FluidTankList(false));

        // accept any fluid this time
        int amountTransferred = cfr.doKeepExact(10000, source, dest, fs -> true, 144, true);

        // expect that no fluids are moved because Keep Exact levels are already met
        MatcherAssert.assertThat("Wrong fluid amount moved", amountTransferred, is(0));
    }

    @Test
    public void doKeepExact_ignores_fluids_not_in_filter() {
        // Create a regulator for testing with, and set it to "Keep Exact" mode
        CoverFluidRegulator cfr = new CoverFluidRegulator(null, null, EnumFacing.UP, 0, 1000);
        cfr.transferMode = TransferMode.KEEP_EXACT;

        IFluidHandler source = new FluidHandlerProxy(
                new FluidTankList(false),
                new FluidTankList(false,
                        new FluidTank(new FluidStack(FluidRegistry.WATER, 64000), 64000),
                        new FluidTank(new FluidStack(FluidRegistry.LAVA, 64000), 64000)));

        // One tank with 144mB water, another with 100mB lava
        IFluidHandler dest = new FluidHandlerProxy(
                new FluidTankList(false,
                        new FluidTank(new FluidStack(FluidRegistry.WATER, 144), 64000),
                        new FluidTank(new FluidStack(FluidRegistry.LAVA, 100), 64000)),
                new FluidTankList(false));

        // accept any fluid this time
        int amountTransferred = cfr.doKeepExact(10000, source, dest, isWater, 144, true);

        // expect that no fluids are moved because already have enough water and lava isn't in the filter
        MatcherAssert.assertThat("Wrong fluid amount moved", amountTransferred, is(0));
    }
}
