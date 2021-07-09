package gregtech.common.pipelike.itempipe.net;

import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.pipenet.block.simple.EmptyNodeData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemPipeNet extends PipeNet<EmptyNodeData> {

    private final Map<BlockPos, List<Inventory>> NET_DATA = new HashMap<>();

    public ItemPipeNet(WorldPipeNet<EmptyNodeData, ? extends PipeNet<EmptyNodeData>> world) {
        super(world);
    }

    public List<Inventory> getNetData(BlockPos pipePos) {
        List<Inventory> data = NET_DATA.get(pipePos);
        if (data == null) {
            data = ItemNetWalker.createNetData(this, getWorldData(), pipePos);
            data.sort(Comparator.comparingInt(inv -> inv.distance));
            NET_DATA.put(pipePos, data);
        }
        return data;
    }

    public void nodeNeighbourChanged(BlockPos pos) {
        NET_DATA.clear();
    }

    @Override
    protected void updateBlockedConnections(BlockPos nodePos, EnumFacing facing, boolean isBlocked) {
        super.updateBlockedConnections(nodePos, facing, isBlocked);
        NET_DATA.clear();
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<EmptyNodeData>> transferredNodes, PipeNet<EmptyNodeData> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        NET_DATA.clear();
        ((ItemPipeNet) parentNet).NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(EmptyNodeData nodeData, NBTTagCompound tagCompound) {
    }

    @Override
    protected EmptyNodeData readNodeData(NBTTagCompound tagCompound) {
        return EmptyNodeData.INSTANCE;
    }

    public static class Inventory {
        private final BlockPos pipePos;
        private final EnumFacing faceToHandler;
        private final int distance;

        public Inventory(BlockPos pipePos, EnumFacing facing, int distance) {
            this.pipePos = pipePos;
            this.faceToHandler = facing;
            this.distance = distance;
        }

        public BlockPos getPipePos() {
            return pipePos;
        }

        public EnumFacing getFaceToHandler() {
            return faceToHandler;
        }

        public int getDistance() {
            return distance;
        }

        public BlockPos getHandlerPos() {
            return pipePos.offset(faceToHandler);
        }

        public IItemHandler getHandler(World world) {
            TileEntity tile = world.getTileEntity(getHandlerPos());
            if (tile != null)
                return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, faceToHandler);
            return null;
        }
    }
}
