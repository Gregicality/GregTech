package gregtech.common.metatileentities.multi.electric.generator.turbine;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidConstants;
import gregtech.api.recipes.Recipe;
import gregtech.api.unification.material.Materials;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class SteamTurbineLogic extends LargeTurbineRecipeLogic {

    private static final Fluid STEAM = Materials.Steam.getFluid();
    private static final Fluid SUPERHEATED_STEAM = Materials.SuperheatedSteam.getFluid();

    private final boolean isHighPressure;

    private int excessWater;

    public SteamTurbineLogic(AbstractLargeTurbine tileEntity, boolean isHighPressure) {
        super(tileEntity);
        this.isHighPressure = isHighPressure;
    }

    @Override
    protected long calculateOutputEUt(long energyDensity) {
        return isHighPressure ? totalFluidConsumed : totalFluidConsumed / 2;
    }

    @Override
    protected int getRotorDamage(@NotNull RotorFit rotorFitting) {
        return switch (rotorFitting) {
            case TIGHT -> 1;
            case LOOSE -> GTValues.RNG.nextInt(4) == 0 ? 0 : 1;
        };
    }

    @Override
    protected boolean isFuelValid(@NotNull Fluid fuel) {
        if (isHighPressure) {
            return fuel == SUPERHEATED_STEAM;
        }
        return fuel == STEAM;
    }

    @Override
    protected boolean usesRecipeEnergy() {
        return false;
    }

    @Override
    protected void setRecipeOutputs(@NotNull Recipe recipe) {
        if (isHighPressure) {
            this.fluidOutputs = Collections.singletonList(Materials.Steam.getFluid(totalFluidConsumed));
        } else {
            excessWater += totalFluidConsumed;
            int water = excessWater / FluidConstants.STEAM_PER_WATER;
            excessWater %= FluidConstants.STEAM_PER_WATER;
            this.fluidOutputs = Collections.singletonList(Materials.DistilledWater.getFluid(water));
        }
        this.itemOutputs = Collections.emptyList();
    }

    @Override
    protected void applyRotorFitting(@NotNull RotorFit rotorFit, @NotNull FittingAdjustmentValues values) {
        switch (rotorFit) {
            case LOOSE -> {}
            case TIGHT -> {
                long optimalFlow = values.optimalEnergyFlow * 4;
                int baseEfficiency = values.baseEfficiency;
                if (baseEfficiency >= 26000.0F) {
                    values.optimalEnergyFlow = (int) (optimalFlow *
                            Math.pow(1.1F, (baseEfficiency - 8000.0F) / 500.0F));
                    values.baseEfficiency = (int) (baseEfficiency * 0.6F);
                } else if (baseEfficiency >= 22000.0F) {
                    values.optimalEnergyFlow = (int) (optimalFlow *
                            Math.pow(1.1F, (baseEfficiency - 7000.0F) / 500.0F));
                    values.baseEfficiency = (int) (baseEfficiency * 0.65F);
                } else if (baseEfficiency >= 18000.0F) {
                    values.optimalEnergyFlow = (int) (optimalFlow *
                            Math.pow(1.1F, (baseEfficiency - 6000.0F) / 500.0F));
                    values.baseEfficiency = (int) (baseEfficiency * 0.7F);
                } else if (baseEfficiency >= 14000.0F) {
                    values.optimalEnergyFlow = (int) (optimalFlow *
                            Math.pow(1.1F, (baseEfficiency - 5000.0F) / 500.0F));
                    values.baseEfficiency = (int) (baseEfficiency * 0.75F);
                } else if (baseEfficiency >= 10000.0F) {
                    values.optimalEnergyFlow = (int) (optimalFlow *
                            Math.pow(1.1F, (baseEfficiency - 4000.0F) / 500.0F));
                    values.baseEfficiency = (int) (baseEfficiency * 0.80F);
                } else if (baseEfficiency >= 6000.0F) {
                    values.optimalEnergyFlow = (int) (optimalFlow *
                            Math.pow(1.1F, (baseEfficiency - 3000.0F) / 500.0F));
                    values.baseEfficiency = (int) (baseEfficiency * 0.85F);
                } else {
                    values.optimalEnergyFlow = optimalFlow;
                    values.baseEfficiency = (int) (baseEfficiency * 0.9F);
                }

                if (values.baseEfficiency % 100 != 0) {
                    values.baseEfficiency -= baseEfficiency % 100;
                }
            }
        }
    }

    @Override
    protected float overflowMultiplier() {
        return 0.5F;
    }

    @Override
    protected float flowFloor() {
        return isHighPressure ? 1.5F : 1.0F;
    }

    @Override
    protected float unoptimalEfficiency(int overflowEfficiency) {
        int delta = Math.abs(totalFluidConsumed - optimalFuelRate);
        float divisor = optimalFuelRate;
        if (totalFluidConsumed > optimalFuelRate) {
            divisor *= overflowEfficiency + (isHighPressure ? 2 : 1);
        }
        return 1.0F - delta / divisor;
    }

    @Override
    public @NotNull NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        if (!isHighPressure) {
            compound.setInteger("excessWater", excessWater);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        if (!isHighPressure) {
            this.excessWater = compound.getInteger("excessWater");
        }
    }
}
