package gregtech.api.pipenet.tile;

import gregtech.api.metatileentity.interfaces.INeighborCache;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.unification.material.Material;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface IPipeTile<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType>
                          extends INeighborCache {

    World getPipeWorld();

    BlockPos getPipePos();

    @Override
    default World world() {
        return getPipeWorld();
    }

    @Override
    default BlockPos pos() {
        return getPipePos();
    }

    default long getTickTimer() {
        return getPipeWorld().getTotalWorldTime();
    }

    BlockPipe<PipeType, NodeDataType, ?> getPipeBlock();

    void transferDataFrom(IPipeTile<PipeType, NodeDataType> sourceTile);

    int getPaintingColor();

    void setPaintingColor(int paintingColor);

    boolean isPainted();

    int getDefaultPaintingColor();

    int getConnections();

    int getNumConnections();

    boolean isConnected(EnumFacing side);

    void setConnection(EnumFacing side, boolean connected, boolean fromNeighbor);

    // if a face is blocked it will still render as connected, but it won't be able to receive stuff from that direction
    default boolean canHaveBlockedFaces() {
        return true;
    }

    int getBlockedConnections();

    boolean isFaceBlocked(EnumFacing side);

    void setFaceBlocked(EnumFacing side, boolean blocked);

    int getVisualConnections();

    PipeType getPipeType();

    NodeDataType getNodeData();

    PipeCoverableImplementation getCoverableImplementation();

    @Nullable
    Material getFrameMaterial();

    boolean supportsTicking();

    IPipeTile<PipeType, NodeDataType> setSupportsTicking();

    boolean canPlaceCoverOnSide(EnumFacing side);

    <T> T getCapability(Capability<T> capability, EnumFacing side);

    <T> T getCapabilityInternal(Capability<T> capability, EnumFacing side);

    void notifyBlockUpdate();

    void writeCoverCustomData(int id, Consumer<AdvancedPacketBuffer> writer);

    void markAsDirty();

    boolean isValidTile();

    void scheduleChunkForRenderUpdate();
}
