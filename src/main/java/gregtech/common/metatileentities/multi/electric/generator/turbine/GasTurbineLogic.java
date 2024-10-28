package gregtech.common.metatileentities.multi.electric.generator.turbine;

import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;

public class GasTurbineLogic extends LargeTurbineRecipeLogic {

    public GasTurbineLogic(AbstractLargeTurbine tileEntity) {
        super(tileEntity);
    }

    @Override
    protected long calculateOutputEUt(long energyDensity) {
        return totalFluidConsumed * energyDensity;
    }

    @Override
    protected int getRotorDamage(@NotNull RotorFit rotorFitting) {
        return 1;
    }

    @Override
    protected boolean isFuelValid(@NotNull Fluid fuel) {
        return true;
    }

    @Override
    protected void applyRotorFitting(@NotNull RotorFit rotorFit, @NotNull FittingAdjustmentValues values) {
        // TODO impl
    }

    @Override
    protected float overflowMultiplier() {
        return 1.5F;
    }

    @Override
    protected float flowFloor() {
        return 0;
    }

    @Override
    protected float unoptimalEfficiency(int overflowEfficiency) {
        int delta = Math.abs(totalFluidConsumed - optimalFuelRate);
        float divisor = optimalFuelRate;
        if (totalFluidConsumed > optimalFuelRate) {
            divisor *= overflowEfficiency * 3 - 1;
        }

        return 1.0F - delta / divisor;
    }
}
