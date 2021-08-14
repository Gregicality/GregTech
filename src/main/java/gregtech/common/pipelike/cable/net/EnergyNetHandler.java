package gregtech.common.pipelike.cable.net;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.util.GTLog;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

import java.util.List;
import java.util.Objects;

public class EnergyNetHandler implements IEnergyContainer {

    private final EnergyNet net;
    private final TileEntityCable cable;
    private final EnumFacing facing;

    public EnergyNetHandler(EnergyNet net, TileEntityCable cable, EnumFacing facing) {
        this.net = Objects.requireNonNull(net);
        this.cable = Objects.requireNonNull(cable);
        this.facing = facing;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        long amperesUsed = 0L;
        List<RoutePath> paths = net.getNetData(cable.getPos());
        for (RoutePath path : paths) {
            if (path.getMaxLoss() >= voltage)
                continue;
            IEnergyContainer dest = path.getHandler(cable.getWorld());
            EnumFacing facing = path.getFaceToHandler().getOpposite();
            if (dest == null || !dest.inputsEnergy(facing) || dest.getEnergyCanBeInserted() <= 0) continue;
            long amps = dest.acceptEnergyFromNetwork(facing, voltage, amperage - amperesUsed);
            amperesUsed += amps;
            for (TileEntityCable cable : path.getPath()) {
                if (cable.getMaxVoltage() < voltage || !cable.incrementAmperage(amps, voltage)) {
                    cable.getWorld().setBlockState(cable.getPipePos(), Blocks.AIR.getDefaultState());
                    break;
                }
            }
            if (amperage == amperesUsed)
                break;
        }

        return amperesUsed;
    }

    @Override
    public long getInputAmperage() {
        return cable.getNodeData().amperage;
    }

    @Override
    public long getInputVoltage() {
        return cable.getNodeData().voltage;
    }

    @Override
    public long getEnergyCapacity() {
        return getInputVoltage() * getInputAmperage();
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        GTLog.logger.fatal("Do not use changeEnergy() for cables! Use acceptEnergyFromNetwork()");
        return acceptEnergyFromNetwork(facing == null ? EnumFacing.UP : facing,
                energyToAdd / getInputAmperage(),
                energyToAdd / getInputVoltage()) * getInputVoltage();
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        return true;
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return true;
    }

    @Override
    public long getEnergyStored() {
        return 0;
    }
}
