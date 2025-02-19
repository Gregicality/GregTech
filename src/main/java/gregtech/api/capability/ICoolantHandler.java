package gregtech.api.capability;

import gregtech.api.fluids.fluidhandlers.LockableFluidTank;
import gregtech.api.nuclear.fission.ICoolantStats;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;

public interface ICoolantHandler extends ILockableHandler<Fluid> {

    @NotNull
    ICoolantStats getCoolant();

    void setCoolant(@NotNull ICoolantStats prop);

    @NotNull
    LockableFluidTank getFluidTank();

    @NotNull
    EnumFacing getFrontFacing();
}
