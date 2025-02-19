package gregtech.api.pipenet.tile;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.metatileentity.NeighborCacheTileEntityBase;
import gregtech.api.metatileentity.SyncedTileEntityBase;
import gregtech.api.metatileentity.interfaces.ISyncedTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.unification.material.Material;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static gregtech.api.capability.GregtechDataCodes.*;

public abstract class TileEntityPipeBase<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType> extends NeighborCacheTileEntityBase implements IPipeTile<PipeType, NodeDataType> {

    protected final PipeCoverableImplementation coverableImplementation = new PipeCoverableImplementation(this);
    protected int paintingColor = -1;
    private int connections = 0;
    private int blockedConnections = 0;
    private NodeDataType cachedNodeData;
    private BlockPipe<PipeType, NodeDataType, ?> pipeBlock;
    private PipeType pipeType = getPipeTypeClass().getEnumConstants()[0];
    @Nullable
    private Material frameMaterial;
    // set when this pipe is replaced with a ticking variant to redirect sync packets
    private TileEntityPipeBase<PipeType, NodeDataType> tickingPipe;

    public TileEntityPipeBase() {}

    public void setPipeData(BlockPipe<PipeType, NodeDataType, ?> pipeBlock, PipeType pipeType) {
        this.pipeBlock = pipeBlock;
        this.pipeType = pipeType;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_PIPE_TYPE, this::writePipeProperties);
        }
    }

    @Override
    public void transferDataFrom(IPipeTile<PipeType, NodeDataType> tileEntity) {
        this.pipeType = tileEntity.getPipeType();
        this.paintingColor = tileEntity.getPaintingColor();
        this.connections = tileEntity.getConnections();
        if (tileEntity instanceof SyncedTileEntityBase pipeBase) {
            addPacketsFrom(pipeBase);
        }
        tileEntity.getCoverableImplementation().transferDataTo(coverableImplementation);
        setFrameMaterial(tileEntity.getFrameMaterial());
    }

    public abstract Class<PipeType> getPipeTypeClass();

    @Nullable
    @Override
    public Material getFrameMaterial() {
        return frameMaterial;
    }

    public void setFrameMaterial(@Nullable Material frameMaterial) {
        this.frameMaterial = frameMaterial;
        if (world != null && world.isRemote) {
            writeCustomData(UPDATE_FRAME_MATERIAL, buf -> {
                buf.writeVarInt(frameMaterial == null ? -1 : frameMaterial.getRegistry().getNetworkId());
                buf.writeVarInt(frameMaterial == null ? -1 : frameMaterial.getId());
            });
        }
    }

    @Override
    public boolean supportsTicking() {
        return this instanceof ITickable;
    }

    @Override
    public World getPipeWorld() {
        return getWorld();
    }

    @Override
    public BlockPos getPipePos() {
        return getPos();
    }

    @SuppressWarnings("ConstantConditions") // yes this CAN actually be null
    @Override
    public void markDirty() {
        if (getWorld() != null && getPos() != null) {
            getWorld().markChunkDirty(getPos(), this);
        }
    }

    @Override
    public PipeCoverableImplementation getCoverableImplementation() {
        return coverableImplementation;
    }

    @Override
    public boolean canPlaceCoverOnSide(EnumFacing side) {
        return true;
    }

    @Override
    public IPipeTile<PipeType, NodeDataType> setSupportsTicking() {
        if (supportsTicking()) {
            return this;
        }
        if (this.tickingPipe != null) {
            // pipe was already set to tick before
            // reuse ticking pipe
            return this.tickingPipe;
        }
        // create new tickable tile entity, transfer data, and replace it
        TileEntityPipeBase<PipeType, NodeDataType> newTile = getPipeBlock().createNewTileEntity(true);
        if (!newTile.supportsTicking()) throw new IllegalStateException("Expected pipe to be ticking, but isn't!");
        newTile.transferDataFrom(this);
        getWorld().setTileEntity(getPos(), newTile);
        this.tickingPipe = newTile;
        return newTile;
    }

    @Override
    public BlockPipe<PipeType, NodeDataType, ?> getPipeBlock() {
        if (pipeBlock == null) {
            Block block = getBlockState().getBlock();
            // noinspection unchecked
            this.pipeBlock = block instanceof BlockPipe blockPipe ? blockPipe : null;
        }
        return pipeBlock;
    }

    @Override
    public int getConnections() {
        return connections;
    }

    @Override
    public int getNumConnections() {
        int count = 0;
        int connections = getConnections();
        while (connections > 0) {
            count++;
            connections = connections & (connections - 1);
        }
        return count;
    }

    @Override
    public int getBlockedConnections() {
        return canHaveBlockedFaces() ? blockedConnections : 0;
    }

    @Override
    public int getPaintingColor() {
        return isPainted() ? paintingColor : getDefaultPaintingColor();
    }

    @Override
    public void setPaintingColor(int paintingColor) {
        this.paintingColor = paintingColor;
        if (!getWorld().isRemote) {
            getPipeBlock().getWorldPipeNet(getWorld()).updateMark(getPos(), getCableMark());
            writeCustomData(UPDATE_INSULATION_COLOR, buffer -> buffer.writeInt(paintingColor));
            markDirty();
        }
    }

    @Override
    public boolean isPainted() {
        return this.paintingColor != -1;
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public boolean isConnected(EnumFacing side) {
        return isConnected(connections, side);
    }

    public static boolean isConnected(int connections, EnumFacing side) {
        return (connections & 1 << side.getIndex()) > 0;
    }

    @Override
    public void setConnection(EnumFacing side, boolean connected, boolean fromNeighbor) {
        // fix desync between two connections. Can happen if a pipe side is blocked, and a new pipe is placed next to
        // it.
        if (!getWorld().isRemote) {
            if (isConnected(side) == connected) {
                return;
            }
            TileEntity tile = getNeighbor(side);
            // block connections if Pipe Types do not match
            if (connected &&
                    tile instanceof IPipeTile pipeTile &&
                    pipeTile.getPipeType().getClass() != this.getPipeType().getClass()) {
                return;
            }
            connections = withSideConnection(connections, side, connected);

            updateNetworkConnection(side, connected);
            writeCustomData(UPDATE_CONNECTIONS, buffer -> {
                buffer.writeVarInt(connections);
            });
            markDirty();

            if (!fromNeighbor && tile instanceof IPipeTile pipeTile) {
                syncPipeConnections(side, pipeTile);
            }
        }
    }

    private void syncPipeConnections(EnumFacing side, IPipeTile<?, ?> pipe) {
        EnumFacing oppositeSide = side.getOpposite();
        boolean neighbourOpen = pipe.isConnected(oppositeSide);
        if (isConnected(side) == neighbourOpen) {
            return;
        }
        if (!neighbourOpen || pipe.getCoverableImplementation().getCoverAtSide(oppositeSide) == null) {
            pipe.setConnection(oppositeSide, !neighbourOpen, true);
        }
    }

    private void updateNetworkConnection(EnumFacing side, boolean connected) {
        WorldPipeNet<?, ?> worldPipeNet = getPipeBlock().getWorldPipeNet(getWorld());
        worldPipeNet.updateBlockedConnections(getPos(), side, !connected);
    }

    protected int withSideConnection(int blockedConnections, EnumFacing side, boolean connected) {
        int index = 1 << side.getIndex();
        if (connected) {
            return blockedConnections | index;
        } else {
            return blockedConnections & ~index;
        }
    }

    @Override
    public void setFaceBlocked(EnumFacing side, boolean blocked) {
        if (!world.isRemote && canHaveBlockedFaces()) {
            blockedConnections = withSideConnection(blockedConnections, side, blocked);
            writeCustomData(UPDATE_BLOCKED_CONNECTIONS, buf -> {
                buf.writeVarInt(blockedConnections);
            });
            markDirty();
            WorldPipeNet<?, ?> worldPipeNet = getPipeBlock().getWorldPipeNet(getWorld());
            PipeNet<?> net = worldPipeNet.getNetFromPos(pos);
            if (net != null) {
                net.onPipeConnectionsUpdate();
            }
        }
    }

    @Override
    public boolean isFaceBlocked(EnumFacing side) {
        return isFaceBlocked(blockedConnections, side);
    }

    public static boolean isFaceBlocked(int blockedConnections, EnumFacing side) {
        return (blockedConnections & (1 << side.getIndex())) > 0;
    }

    @Override
    public PipeType getPipeType() {
        return pipeType;
    }

    @Override
    public NodeDataType getNodeData() {
        if (cachedNodeData == null) {
            this.cachedNodeData = getPipeBlock().createProperties(this);
        }
        return cachedNodeData;
    }

    private int getCableMark() {
        return paintingColor == -1 ? 0 : paintingColor;
    }

    /**
     * This returns open connections purely for rendering
     *
     * @return open connections
     */
    public int getVisualConnections() {
        int connections = getConnections();
        float selfThickness = getPipeType().getThickness();
        for (EnumFacing facing : EnumFacing.values()) {
            if (isConnected(facing)) {
                if (world.getTileEntity(pos.offset(facing)) instanceof IPipeTile<?, ?>pipeTile &&
                        pipeTile.isConnected(facing.getOpposite()) &&
                        pipeTile.getPipeType().getThickness() < selfThickness) {
                    connections |= 1 << (facing.getIndex() + 6);
                }
                if (getCoverableImplementation().getCoverAtSide(facing) != null) {
                    connections |= 1 << (facing.getIndex() + 12);
                }
            }
        }
        return connections;
    }

    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechTileCapabilities.CAPABILITY_COVER_HOLDER) {
            return GregtechTileCapabilities.CAPABILITY_COVER_HOLDER.cast(getCoverableImplementation());
        }
        return super.getCapability(capability, facing);
    }

    @Nullable
    @Override
    public final <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        T pipeCapability = getPipeBlock() == null ? null : getCapabilityInternal(capability, facing);

        if (capability == GregtechTileCapabilities.CAPABILITY_COVER_HOLDER) {
            return pipeCapability;
        }

        Cover cover = facing == null ? null : coverableImplementation.getCoverAtSide(facing);
        if (cover == null) {
            if (facing == null || isConnected(facing)) {
                return pipeCapability;
            }
            return null;
        }

        T coverCapability = cover.getCapability(capability, pipeCapability);
        if (coverCapability == pipeCapability) {
            if (isConnected(facing)) {
                return pipeCapability;
            }
            return null;
        }
        return coverCapability;
    }

    @Override
    public final boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        BlockPipe<PipeType, NodeDataType, ?> pipeBlock = getPipeBlock();
        if (pipeBlock != null) {
            // noinspection ConstantConditions
            compound.setString("PipeBlock", pipeBlock.getRegistryName().toString());
        }
        compound.setInteger("PipeType", pipeType.ordinal());
        compound.setInteger("Connections", connections);
        compound.setInteger("BlockedConnections", blockedConnections);
        if (isPainted()) {
            compound.setInteger("InsulationColor", paintingColor);
        }
        compound.setString("FrameMaterial", frameMaterial == null ? "" : frameMaterial.getRegistryName());
        this.coverableImplementation.writeToNBT(compound);
        return compound;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        if (this.tickingPipe != null) {
            this.tickingPipe.readFromNBT(compound);
            return;
        }
        super.readFromNBT(compound);
        if (compound.hasKey("PipeBlock", NBT.TAG_STRING)) {
            Block block = Block.REGISTRY.getObject(new ResourceLocation(compound.getString("PipeBlock")));
            // noinspection unchecked
            this.pipeBlock = block instanceof BlockPipe blockPipe ? blockPipe : null;
        }
        this.pipeType = getPipeTypeClass().getEnumConstants()[compound.getInteger("PipeType")];

        if (compound.hasKey("Connections")) {
            connections = compound.getInteger("Connections");
        } else if (compound.hasKey("BlockedConnectionsMap")) {
            connections = 0;
            NBTTagCompound blockedConnectionsTag = compound.getCompoundTag("BlockedConnectionsMap");
            for (String attachmentTypeKey : blockedConnectionsTag.getKeySet()) {
                int blockedConnections = blockedConnectionsTag.getInteger(attachmentTypeKey);
                connections |= blockedConnections;
            }
        }
        blockedConnections = compound.getInteger("BlockedConnections");

        if (compound.hasKey("InsulationColor")) {
            this.paintingColor = compound.getInteger("InsulationColor");
        }
        String frameMaterialName = compound.getString("FrameMaterial");
        if (!frameMaterialName.isEmpty()) {
            this.frameMaterial = GregTechAPI.materialManager.getMaterial(frameMaterialName);
        }

        this.coverableImplementation.readFromNBT(compound);
        if (this.tickingPipe != null && this.coverableImplementation.hasAnyCover()) {
            // one of the covers set the pipe to ticking, and we need to send over the rest of the covers
            this.coverableImplementation.transferDataTo(this.tickingPipe.coverableImplementation);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.coverableImplementation.onLoad();
    }

    protected void writePipeProperties(PacketBuffer buf) {
        buf.writeVarInt(pipeType.ordinal());
    }

    protected void readPipeProperties(PacketBuffer buf) {
        this.pipeType = getPipeTypeClass().getEnumConstants()[buf.readVarInt()];
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        writePipeProperties(buf);
        buf.writeVarInt(connections);
        buf.writeVarInt(blockedConnections);
        buf.writeInt(paintingColor);
        buf.writeVarInt(frameMaterial == null ? -1 : frameMaterial.getRegistry().getNetworkId());
        buf.writeVarInt(frameMaterial == null ? -1 : frameMaterial.getId());
        this.coverableImplementation.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        if (this.tickingPipe != null) {
            this.tickingPipe.receiveInitialSyncData(buf);
            return;
        }
        readPipeProperties(buf);
        this.connections = buf.readVarInt();
        this.blockedConnections = buf.readVarInt();
        this.paintingColor = buf.readInt();
        int registryId = buf.readVarInt();
        int frameMaterialId = buf.readVarInt();
        if (registryId >= 0 && frameMaterialId >= 0) {
            this.frameMaterial = GregTechAPI.materialManager.getRegistry(registryId).getObjectById(frameMaterialId);
        } else {
            this.frameMaterial = null;
        }
        this.coverableImplementation.readInitialSyncData(buf);
        if (this.tickingPipe != null && this.coverableImplementation.hasAnyCover()) {
            // one of the covers set the pipe to ticking, and we need to send over the rest of the covers
            this.coverableImplementation.transferDataTo(this.tickingPipe.coverableImplementation);
        }
    }

    @Override
    public void receiveCustomData(int discriminator, @NotNull AdvancedPacketBuffer buf) {
        if (this.tickingPipe != null) {
            this.tickingPipe.receiveCustomData(discriminator, buf);
            return;
        }
        if (discriminator == UPDATE_INSULATION_COLOR) {
            this.paintingColor = buf.readInt();
            scheduleChunkForRenderUpdate();
        } else if (discriminator == UPDATE_CONNECTIONS) {
            this.connections = buf.readVarInt();
            scheduleChunkForRenderUpdate();
        } else if (discriminator == SYNC_COVER_IMPLEMENTATION) {
            int internalID = buf.readVarInt();
            AdvancedPacketBuffer b = buf.readSubBuffer(internalID);
            this.coverableImplementation.readCustomData(internalID, b);
            ISyncedTileEntity.checkData(b, this.coverableImplementation);
            buf.closeSubBuffer();
        } else if (discriminator == UPDATE_PIPE_TYPE) {
            readPipeProperties(buf);
            scheduleChunkForRenderUpdate();
        } else if (discriminator == UPDATE_BLOCKED_CONNECTIONS) {
            this.blockedConnections = buf.readVarInt();
            scheduleChunkForRenderUpdate();
        } else if (discriminator == UPDATE_FRAME_MATERIAL) {
            int registryId = buf.readVarInt();
            int frameMaterialId = buf.readVarInt();
            if (registryId >= 0 && frameMaterialId >= 0) {
                this.frameMaterial = GregTechAPI.materialManager.getRegistry(registryId).getObjectById(frameMaterialId);
            } else {
                this.frameMaterial = null;
            }
            scheduleChunkForRenderUpdate();
        }
    }

    @Override
    public void writeCoverCustomData(int id, Consumer<AdvancedPacketBuffer> writer) {
        writeCustomData(SYNC_COVER_IMPLEMENTATION, buffer -> {
            buffer.writeVarInt(id);
            writer.accept(buffer);
        });
    }

    @Override
    public void scheduleChunkForRenderUpdate() {
        BlockPos pos = getPos();
        getWorld().markBlockRangeForRenderUpdate(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    @Override
    public void notifyBlockUpdate() {
        getWorld().notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
        getPipeBlock().updateActiveNodeStatus(getWorld(), getPos(), this);
    }

    @Override
    public void markAsDirty() {
        markDirty();
    }

    @Override
    public boolean isValidTile() {
        return !isInvalid();
    }

    @Override
    public boolean shouldRefresh(@NotNull World world, @NotNull BlockPos pos, IBlockState oldState,
                                 IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    @MustBeInvokedByOverriders
    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (!world.isRemote) {
            WorldPipeNet<?, ?> worldPipeNet = getPipeBlock().getWorldPipeNet(getWorld());
            PipeNet<?> net = worldPipeNet.getNetFromPos(pos);
            if (net != null) {
                net.onChunkUnload();
            }
        }
    }

    public void doExplosion(float explosionPower) {
        getWorld().setBlockToAir(getPos());
        if (!getWorld().isRemote) {
            ((WorldServer) getWorld()).spawnParticle(EnumParticleTypes.SMOKE_LARGE, getPos().getX() + 0.5,
                    getPos().getY() + 0.5, getPos().getZ() + 0.5,
                    10, 0.2, 0.2, 0.2, 0.0);
        }
        getWorld().createExplosion(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                explosionPower, false);
    }
}
