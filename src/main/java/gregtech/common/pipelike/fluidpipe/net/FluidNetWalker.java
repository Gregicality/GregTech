package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.covers.CoverFluidFilter;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FluidNetWalker extends PipeNetWalker {

    public static FluidNetWalker countFluid(World world, BlockPos pos, FluidStack fluid, boolean ignoreFilter) {
        FluidNetWalker walker = new FluidNetWalker(Mode.COUNT, world, pos, 1, fluid);
        walker.ignoreFilter = ignoreFilter;
        walker.traversePipeNet();
        return walker;
    }

    public static List<TileEntityFluidPipe> getPipesForFluid(World world, BlockPos pos, FluidStack fluid) {
        FluidNetWalker walker = new FluidNetWalker(Mode.GET_PIPES, world, pos, 1, fluid);
        walker.traversePipeNet();
        return walker.pipes;
    }

    public static int getTotalCapacity(World world, BlockPos pos) {
        FluidNetWalker walker = new FluidNetWalker(Mode.COUNT_CAPACITY, world, pos, 1, null);
        walker.ignoreFilter = true;
        walker.traversePipeNet();
        return walker.count;
    }

    private final Mode mode;
    private List<TileEntityFluidPipe> pipes = new ArrayList<>();
    private final FluidStack fluid;
    private int count;
    private boolean ignoreFilter = false;

    protected FluidNetWalker(Mode mode, World world, BlockPos sourcePipe, int walkedBlocks, FluidStack fluid) {
        super(world, sourcePipe, walkedBlocks);
        this.fluid = fluid;
        this.mode = mode;
    }

    @Override
    protected PipeNetWalker createSubWalker(World world, BlockPos nextPos, int walkedBlocks) {
        FluidNetWalker walker = new FluidNetWalker(mode, world, nextPos, walkedBlocks, fluid);
        walker.pipes = pipes;
        walker.ignoreFilter = ignoreFilter;
        return walker;
    }

    @Override
    protected void onRemoveSubWalker(PipeNetWalker subWalker) {
        count += ((FluidNetWalker) subWalker).count;
    }

    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
        TileEntityFluidPipe pipe = (TileEntityFluidPipe) pipeTile;
        if (mode == Mode.COUNT_CAPACITY) {
            count += pipe.getCapacityPerTank();
        } else if (mode == Mode.GET_PIPES)
            pipes.add(pipe);
        else {
            FluidStack stack = pipe.findFluid(fluid);
            if (stack != null && stack.amount > 0) {
                count += stack.amount;
                pipes.add(pipe);
            }
        }
    }

    @Override
    protected void checkNeighbour(IPipeTile<?, ?> pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour, @Nullable TileEntity neighbourTile) {
    }

    @Override
    protected boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, EnumFacing faceToNeighbour) {
        if (!(neighbourPipe instanceof TileEntityFluidPipe) || ((TileEntityFluidPipe) neighbourPipe).isInvalid())
            return false;
        if (ignoreFilter)
            return true;
        ICoverable coverable = currentPipe.getCoverableImplementation();
        CoverBehavior cover = coverable.getCoverAtSide(faceToNeighbour);
        if (cover instanceof CoverFluidFilter) {
            if (!((CoverFluidFilter) cover).testFluidStack(fluid))
                return false;
        }
        coverable = neighbourPipe.getCoverableImplementation();
        cover = coverable.getCoverAtSide(faceToNeighbour.getOpposite());
        if (cover instanceof CoverFluidFilter) {
            return ((CoverFluidFilter) cover).testFluidStack(fluid);
        }
        return true;
    }

    public int getCount() {
        return count;
    }

    public List<TileEntityFluidPipe> getPipes() {
        return pipes;
    }

    enum Mode {
        COUNT, GET_PIPES, COUNT_CAPACITY
    }
}
