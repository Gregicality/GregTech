package gregtech.api.capability;

import gregtech.api.capability.impl.LockableFluidTank;
import gregtech.api.nuclear.fission.ICoolantStats;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICoolantHandler extends ILockableHandler<Fluid> {

    @Nullable
    ICoolantStats getCoolant();

    void setCoolant(@Nullable ICoolantStats prop);

    @NotNull
    LockableFluidTank getFluidTank();

    @NotNull
    EnumFacing getFrontFacing();
}
