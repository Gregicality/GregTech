package gregtech.common.pipelike.itempipe.net;

import gregtech.api.cover.Cover;
import gregtech.api.pipenet.AbstractEdgePredicate;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.pipenet.WorldPipeNetG;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.properties.ItemPipeProperties;

import gregtech.common.covers.CoverItemFilter;
import gregtech.common.covers.CoverShutter;
import gregtech.common.covers.ItemFilterMode;
import gregtech.common.pipelike.itempipe.ItemPipeType;

import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class WorldItemPipeNet extends WorldPipeNetG<ItemPipeProperties, ItemPipeType> {

    private static final String DATA_ID = "gregtech.item_pipe_net";

    public static WorldItemPipeNet getWorldPipeNet(World world) {
        WorldItemPipeNet netWorldData = (WorldItemPipeNet) world.loadData(WorldItemPipeNet.class, DATA_ID);
        if (netWorldData == null) {
            netWorldData = new WorldItemPipeNet(DATA_ID);
            world.setData(DATA_ID, netWorldData);
        }
        netWorldData.setWorldAndInit(world);
        return netWorldData;
    }

    public WorldItemPipeNet(String name) {
        super(name);
    }

    @Override
    protected boolean isDirected() {
        return true;
    }

    @Override
    protected Class<? extends IPipeTile<ItemPipeType, ItemPipeProperties>> getBasePipeClass() {
        return TileEntityItemPipe.class;
    }

    @Override
    protected AbstractEdgePredicate<?> getPredicate(Cover thisCover, Cover neighbourCover) {
        ItemPredicate predicate = new ItemPredicate();
        if (thisCover instanceof CoverItemFilter filter &&
                filter.getFilterMode() != ItemFilterMode.FILTER_INSERT) {
            predicate.setSourceFilter(filter.getItemFilter());
        }
        if (neighbourCover instanceof CoverItemFilter filter &&
                filter.getFilterMode() != ItemFilterMode.FILTER_EXTRACT) {
            predicate.setTargetFilter(filter.getItemFilter());
        }
        return shutterify(predicate, thisCover, neighbourCover);
    }

    @Override
    protected void writeNodeData(ItemPipeProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("Priority", nodeData.getPriority());
        tagCompound.setFloat("Rate", nodeData.getTransferRate());
    }

    @Override
    protected ItemPipeProperties readNodeData(NBTTagCompound tagCompound) {
        return new ItemPipeProperties(tagCompound.getInteger("Priority"), tagCompound.getFloat("Rate"));
    }
}
