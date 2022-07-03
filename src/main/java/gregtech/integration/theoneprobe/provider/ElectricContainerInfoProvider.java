package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.apiimpl.elements.ElementProgress;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import org.jetbrains.annotations.NotNull;

public class ElectricContainerInfoProvider extends CapabilityInfoProvider<IEnergyContainer> {

    @Override
    public String getID() {
        return GTValues.MODID + ":energy_container_provider";
    }

    @NotNull
    @Override
    protected Capability<IEnergyContainer> getCapability() {
        return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER;
    }

    @Override
    protected boolean allowDisplaying(@NotNull IEnergyContainer capability) {
        return !capability.isOneProbeHidden();
    }

    @Override
    protected void addProbeInfo(@NotNull IEnergyContainer capability, @NotNull IProbeInfo probeInfo,
                                EntityPlayer player, @NotNull TileEntity tileEntity, @NotNull IProbeHitData data) {
        long maxStorage = capability.getEnergyCapacity();
        if (maxStorage == 0) return; // do not add empty max storage progress bar
        probeInfo.progress(capability.getEnergyStored(), maxStorage, probeInfo.defaultProgressStyle()
                .numberFormat(player.isSneaking() ? NumberFormat.FULL : NumberFormat.COMPACT)
                .suffix(" / " + (player.isSneaking() ? maxStorage + " EU" : ElementProgress.format(maxStorage, NumberFormat.COMPACT, " EU")))
                .filledColor(0xFFEEE600)
                .alternateFilledColor(0xFFEEE600)
                .borderColor(0xFF555555).numberFormat(mcjty.theoneprobe.api.NumberFormat.COMMAS));
    }
}
