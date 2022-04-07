package gregtech.common.pipelike.itempipe.net;

import gregtech.api.cover.CoverBehavior;
import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.api.util.GTUtility;
import gregtech.common.covers.CoverItemFilter;
import gregtech.common.covers.CoverShutter;
import gregtech.common.covers.ItemFilterMode;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ItemNetWalker extends PipeNetWalker {

    public static List<ItemPipeNet.Inventory> createNetData(World world, BlockPos sourcePipe, EnumFacing faceToSourceHandler) {
        ItemNetWalker walker = new ItemNetWalker(world, sourcePipe, 1, new ArrayList<>(), null);
        walker.sourcePipe = sourcePipe;
        walker.facingToHandler = faceToSourceHandler;
        walker.traversePipeNet();
        return walker.inventories;
    }

    private ItemPipeProperties minProperties;
    private final List<ItemPipeNet.Inventory> inventories;
    private final List<Predicate<ItemStack>> filters = new ArrayList<>();
    private BlockPos sourcePipe;
    private EnumFacing facingToHandler;

    protected ItemNetWalker(World world, BlockPos sourcePipe, int distance, List<ItemPipeNet.Inventory> inventories, ItemPipeProperties properties) {
        super(world, sourcePipe, distance);
        this.inventories = inventories;
        this.minProperties = properties;
    }

    @Override
    protected PipeNetWalker createSubWalker(World world, BlockPos nextPos, int walkedBlocks) {
        ItemNetWalker walker = new ItemNetWalker(world, nextPos, walkedBlocks, inventories, minProperties);
        walker.facingToHandler = facingToHandler;
        walker.sourcePipe = sourcePipe;
        walker.filters.addAll(filters);
        return walker;
    }

    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
        ItemPipeProperties pipeProperties = ((TileEntityItemPipe) pipeTile).getNodeData();
        if (minProperties == null) {
            minProperties = pipeProperties;
        } else {
            minProperties = new ItemPipeProperties(minProperties.getPriority() + pipeProperties.getPriority(), Math.min(minProperties.getTransferRate(), pipeProperties.getTransferRate()));
        }
    }

    @Override
    protected void checkNeighbour(IPipeTile<?, ?> pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour, @Nullable TileEntity neighbourTile) {
        if (neighbourTile == null || (GTUtility.arePosEqual(pipePos, sourcePipe) && faceToNeighbour == facingToHandler)) {
            return;
        }
        IItemHandler handler = neighbourTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, faceToNeighbour.getOpposite());
        if (handler != null) {
            inventories.add(new ItemPipeNet.Inventory(new BlockPos(pipePos), faceToNeighbour, getWalkedBlocks(), minProperties, new ArrayList<>(filters)));
        }
    }

    @Override
    protected boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, EnumFacing faceToNeighbour) {
        if (!(neighbourPipe instanceof TileEntityItemPipe) || neighbourPipe.isFaceBlocked(faceToNeighbour.getOpposite())) {
            return false;
        }
        CoverBehavior thisCover = currentPipe.getCoverableImplementation().getCoverAtSide(faceToNeighbour);
        CoverBehavior neighbourCover = neighbourPipe.getCoverableImplementation().getCoverAtSide(faceToNeighbour.getOpposite());
        if (thisCover instanceof CoverShutter) {
            filters.add(stack -> !thisCover.isValid() || !((CoverShutter) thisCover).isWorkingEnabled());
        } else if (thisCover instanceof CoverItemFilter && ((CoverItemFilter) thisCover).getFilterMode() != ItemFilterMode.FILTER_INSERT) {
            filters.add(((CoverItemFilter) thisCover)::testItemStack);
        }
        if (neighbourCover instanceof CoverShutter) {
            filters.add(stack -> !neighbourCover.isValid() || !((CoverShutter) neighbourCover).isWorkingEnabled());
        } else if (neighbourCover instanceof CoverItemFilter && ((CoverItemFilter) neighbourCover).getFilterMode() != ItemFilterMode.FILTER_EXTRACT) {
            filters.add(((CoverItemFilter) neighbourCover)::testItemStack);
        }
        return true;
    }
}
