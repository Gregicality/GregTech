package gregtech.common.pipelikeold.laser.tile;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserRelay;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.pipenetold.tile.IPipeTile;
import gregtech.api.graphnet.pipenetold.tile.TileEntityPipeBase;
import gregtech.api.util.TaskScheduler;
import gregtech.common.pipelikeold.laser.LaserPipeProperties;
import gregtech.common.pipelikeold.laser.LaserPipeType;
import gregtech.common.pipelikeold.laser.net.LaserNetHandler;
import gregtech.common.pipelikeold.laser.net.WorldLaserPipeNet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class TileEntityLaserPipe extends TileEntityPipeBase<LaserPipeType, LaserPipeProperties, NetEdge> {

    private final EnumMap<EnumFacing, LaserNetHandler> handlers = new EnumMap<>(EnumFacing.class);
    // the LaserNetHandler can only be created on the server, so we have an empty placeholder for the client
    private final ILaserRelay clientCapability = new DefaultLaserContainer();
    private LaserNetHandler defaultHandler;

    private int ticksActive = 0;
    private int activeDuration = 0;
    private boolean isActive = false;

    @Override
    public Class<LaserPipeType> getPipeTypeClass() {
        return LaserPipeType.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    private void initHandlers() {
        WorldLaserPipeNet net = WorldLaserPipeNet.getWorldPipeNet(getPipeWorld());
        for (EnumFacing facing : EnumFacing.VALUES) {
            handlers.put(facing, new LaserNetHandler(net, this, facing));
        }
        defaultHandler = new LaserNetHandler(net, this, null);
    }

    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechTileCapabilities.CAPABILITY_LASER) {
            if (world.isRemote) {
                return GregtechTileCapabilities.CAPABILITY_LASER.cast(clientCapability);
            }

            if (handlers.isEmpty()) {
                initHandlers();
            }

            return GregtechTileCapabilities.CAPABILITY_LASER.cast(handlers.getOrDefault(facing, defaultHandler));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    @Override
    public void transferDataFrom(IPipeTile<LaserPipeType, LaserPipeProperties, NetEdge> tileEntity) {
        super.transferDataFrom(tileEntity);
        if (getPipeBlock().getWorldPipeNet(getPipeWorld()) == null) {
            return;
        }

        TileEntityLaserPipe pipe = (TileEntityLaserPipe) tileEntity;
        if (!pipe.handlers.isEmpty() && pipe.defaultHandler != null) {
            // take handlers from old pipe
            handlers.clear();
            handlers.putAll(pipe.handlers);
            defaultHandler = pipe.defaultHandler;
        } else {
            // create new handlers
            initHandlers();
        }
    }

    @Override
    public void setConnection(EnumFacing side, boolean connected, boolean fromNeighbor) {
        if (!getWorld().isRemote && connected && !fromNeighbor) {
            int connections = getConnections();
            // block connection if any side other than the requested side and its opposite side are already connected.
            connections &= ~(1 << side.getIndex());
            connections &= ~(1 << side.getOpposite().getIndex());
            if (connections != 0) return;

            // check the same for the targeted pipe
            TileEntity tile = getWorld().getTileEntity(getPos().offset(side));
            if (tile instanceof IPipeTile<?, ?, ?>pipeTile &&
                    pipeTile.getPipeType().getClass() == this.getPipeType().getClass()) {
                connections = pipeTile.getConnections();
                connections &= ~(1 << side.getIndex());
                connections &= ~(1 << side.getOpposite().getIndex());
                if (connections != 0) return;
            }
        }
        super.setConnection(side, connected, fromNeighbor);
    }

    public boolean isActive() {
        return this.isActive;
    }

    /**
     * @param active   if the pipe should become active
     * @param duration how long the pipe should be active for
     */
    public void setActive(boolean active, int duration) {
        if (this.isActive != active) {
            this.isActive = active;
            notifyBlockUpdate();
            markDirty();
            writeCustomData(GregtechDataCodes.PIPE_LASER_ACTIVE, buf -> buf.writeBoolean(this.isActive));
            if (active && duration != this.activeDuration) {
                TaskScheduler.scheduleTask(getWorld(), this::queueDisconnect);
            }
        }

        this.activeDuration = duration;
        if (duration > 0 && active) {
            this.ticksActive = 0;
        }
    }

    public boolean queueDisconnect() {
        if (++this.ticksActive % activeDuration == 0) {
            this.ticksActive = 0;
            setActive(false, -1);
            return false;
        }
        return true;
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        super.receiveCustomData(discriminator, buf);
        if (discriminator == GregtechDataCodes.PIPE_LASER_ACTIVE) {
            this.isActive = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isActive);

        // schedule a disconnect on world load, gotta set the duration to something
        if (isActive) {
            activeDuration = 100;
            TaskScheduler.scheduleTask(getWorld(), this::queueDisconnect);
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
        scheduleRenderUpdate();
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        compound.setBoolean("Active", isActive);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("Active", Constants.NBT.TAG_BYTE)) {
            isActive = compound.getBoolean("Active");
        }
    }

    private static class DefaultLaserContainer implements ILaserRelay {

        @Override
        public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage, boolean simulate) {
            return 0;
        }

        @Override
        public boolean inputsEnergy(EnumFacing side) {
            return false;
        }

        @Override
        public long changeEnergy(long differenceAmount) {
            return 0;
        }

        @Override
        public long getEnergyStored() {
            return 0;
        }

        @Override
        public long getEnergyCapacity() {
            return 0;
        }

        @Override
        public long getInputAmperage() {
            return 0;
        }

        @Override
        public long getInputVoltage() {
            return 0;
        }
    }
}
