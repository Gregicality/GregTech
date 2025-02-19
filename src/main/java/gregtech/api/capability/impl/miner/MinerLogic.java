package gregtech.api.capability.impl.miner;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IMiner;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class MinerLogic {

    private static final short MAX_SPEED = Short.MAX_VALUE;
    private static final byte POWER = 5;
    private static final byte TICK_TOLERANCE = 20;
    private static final double DIVIDEND = MAX_SPEED * Math.pow(TICK_TOLERANCE, POWER);

    protected final MetaTileEntity metaTileEntity;
    protected final IMiner miner;

    private final int fortune;
    private final int speed;
    private final int maximumRadius;

    private final ICubeRenderer PIPE_TEXTURE;

    private final LinkedList<BlockPos> blocksToMine = new LinkedList<>();

    private final AtomicInteger x = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger y = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger z = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger startX = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger startZ = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger startY = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger pipeY = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger mineX = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger mineZ = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger mineY = new AtomicInteger(Integer.MAX_VALUE);

    private int pipeLength = 0;
    private int currentRadius;
    private boolean isDone;
    private boolean isActive = false;
    private boolean isWorkingEnabled = true;
    protected boolean wasActiveAndNeedsUpdate;

    private final IBlockState oreReplacementBlock = findMiningReplacementBlock();

    /**
     * Creates the general logic for all in-world ore block miners
     *
     * @param metaTileEntity the {@link MetaTileEntity} this logic belongs to
     * @param fortune        the fortune amount to apply when mining ores
     * @param speed          the speed in ticks per block mined
     * @param maximumRadius  the maximum radius (square shaped) the miner can mine in
     */
    public MinerLogic(@NotNull MetaTileEntity metaTileEntity, int fortune, int speed, int maximumRadius,
                      ICubeRenderer pipeTexture) {
        this.metaTileEntity = metaTileEntity;
        this.miner = (IMiner) metaTileEntity;
        this.fortune = fortune;
        this.speed = speed;
        this.currentRadius = maximumRadius;
        this.maximumRadius = maximumRadius;
        this.isDone = false;
        this.PIPE_TEXTURE = pipeTexture;
    }

    private static IBlockState findMiningReplacementBlock() {
        String[] blockDescription = StringUtils.split(ConfigHolder.machines.replaceMinedBlocksWith, ":");
        Block replacementBlock;

        if (blockDescription.length == 2) {
            replacementBlock = Block.getBlockFromName(ConfigHolder.machines.replaceMinedBlocksWith);
        } else {
            replacementBlock = Block.getBlockFromName(String.format("%s:%s", blockDescription[0], blockDescription[1]));
        }
        if (replacementBlock == null) {
            GTLog.logger.error("Miner Config Replacement block was null, replacing with Cobblestone");
            return Blocks.COBBLESTONE.getDefaultState();
        }

        // check for meta
        if (blockDescription.length > 2 && !blockDescription[2].isEmpty()) {
            return replacementBlock.getDefaultState().getBlock()
                    .getStateFromMeta(Integer.parseInt(blockDescription[2]));
        }

        return replacementBlock.getDefaultState();
    }

    /**
     * Performs the actual mining in world
     * Call this method every tick in update
     */
    public void performMining() {
        // Needs to be server side
        if (metaTileEntity.getWorld().isRemote)
            return;

        // Inactive miners do nothing
        if (!this.isWorkingEnabled)
            return;

        // check if mining is possible
        if (!checkCanMine())
            return;

        // if the inventory is not full, drain energy etc. from the miner
        // the storages have already been checked earlier
        if (!miner.isInventoryFull()) {
            // always drain storages when working, even if blocksToMine ends up being empty
            drainStorages(false);

            // since energy is being consumed the miner is now active
            if (!this.isActive)
                setActive(true);
        } else {
            // the miner cannot drain, therefore it is inactive
            if (this.isActive)
                setActive(false);
        }

        // drill a hole beneath the miner and extend the pipe downwards by one
        WorldServer world = (WorldServer) metaTileEntity.getWorld();
        if (mineY.get() < pipeY.get()) {
            var pipePos = new BlockPos(metaTileEntity.getPos().getX(), pipeY.get(), metaTileEntity.getPos().getZ());
            if (world.getTileEntity(pipePos) == null &&
                    world.getBlockState(pipePos).getBlockHardness(world, pipePos) >= 0)
                world.destroyBlock(pipePos, false);
            pipeY.decrementAndGet();
            incrementPipeLength();
        }

        // check if the miner needs new blocks to mine and get them if needed
        checkBlocksToMine();

        // if there are blocks to mine and the correct amount of time has passed, do the mining
        if (metaTileEntity.getOffsetTimer() % this.speed == 0 && !blocksToMine.isEmpty()) {
            NonNullList<ItemStack> blockDrops = NonNullList.create();
            IBlockState blockState = metaTileEntity.getWorld().getBlockState(blocksToMine.getFirst());

            // check to make sure the ore is still there,
            while (!GTUtility.isOre(GTUtility.toItem(blockState))) {
                blocksToMine.removeFirst();
                if (blocksToMine.isEmpty()) break;
                blockState = metaTileEntity.getWorld().getBlockState(blocksToMine.getFirst());
            }
            // When we are here we have an ore to mine! I'm glad we aren't threaded
            if (!blocksToMine.isEmpty() && GTUtility.isOre(GTUtility.toItem(blockState))) {
                // get the small ore drops, if a small ore
                getSmallOreBlockDrops(blockDrops, world, blocksToMine.getFirst(), blockState);
                // get the block's drops.
                getRegularBlockDrops(blockDrops, world, blocksToMine.getFirst(), blockState);
                // try to insert them
                mineAndInsertItems(blockDrops, world);
            }

        }

        if (blocksToMine.isEmpty()) {
            // there were no blocks to mine, so the current position is the previous position
            x.set(mineX.get());
            y.set(mineY.get());
            z.set(mineZ.get());

            // attempt to get more blocks to mine, if there are none, the miner is done mining
            blocksToMine.addAll(getBlocksToMine());
            if (blocksToMine.isEmpty()) {
                this.isDone = true;
                this.wasActiveAndNeedsUpdate = true;
                this.setActive(false);
            }
        }
    }

    /**
     * @return true if the miner is able to mine, else false
     */
    protected boolean checkCanMine() {
        // if the miner is finished, the target coordinates are invalid, or it cannot drain storages, stop
        if (checkShouldStop()) {
            // if the miner is not finished and has invalid coordinates, get new and valid starting coordinates
            if (!isDone && checkCoordinatesInvalid(x, y, z))
                initPos(metaTileEntity.getPos(), currentRadius);

            // don't do anything else this time
            return false;
        }
        return true;
    }

    protected boolean checkShouldStop() {
        return isDone || checkCoordinatesInvalid(x, y, z) || !drainStorages(true);
    }

    /**
     * Called after each block is mined, used to perform additional actions afterwards
     */
    protected void onMineOperation() {}

    /**
     * called in order to drain anything the miner needs to drain in order to run
     * only drains energy by default
     */
    protected boolean drainStorages(boolean simulate) {
        return miner.drainEnergy(simulate);
    }

    /**
     * called to handle mining small ores
     *
     * @param blockDrops  the List of items to fill after the operation
     * @param world       the {@link WorldServer} the miner is in
     * @param blockToMine the {@link BlockPos} of the block being mined
     * @param blockState  the {@link IBlockState} of the block being mined
     */
    protected void getSmallOreBlockDrops(NonNullList<ItemStack> blockDrops, WorldServer world, BlockPos blockToMine,
                                         IBlockState blockState) {
        /*
         * small ores
         * if orePrefix of block in blockPos is small
         * applyTieredHammerNoRandomDrops...
         * else
         * current code...
         */
    }

    /**
     * called to handle mining regular ores and blocks
     *
     * @param blockDrops  the List of items to fill after the operation
     * @param world       the {@link WorldServer} the miner is in
     * @param blockToMine the {@link BlockPos} of the block being mined
     * @param blockState  the {@link IBlockState} of the block being mined
     */
    protected void getRegularBlockDrops(NonNullList<ItemStack> blockDrops, WorldServer world, BlockPos blockToMine,
                                        @NotNull IBlockState blockState) {
        blockState.getBlock().getDrops(blockDrops, world, blockToMine, blockState, 0); // regular ores do not get
                                                                                       // fortune applied
    }

    /**
     * called in order to insert the mined items into the inventory and actually remove the block in world
     * marks the inventory as full if the items cannot fit, and not full if it previously was full and items could fit
     *
     * @param blockDrops the List of items to insert
     * @param world      the {@link WorldServer} the miner is in
     */
    private void mineAndInsertItems(List<ItemStack> blockDrops, WorldServer world) {
        // If the block's drops can fit in the inventory, move the previously mined position to the block
        // replace the ore block with cobblestone instead of breaking it to prevent mob spawning
        // remove the ore block's position from the mining queue
        if (GTTransferUtils.addItemsToItemHandler(metaTileEntity.getExportItems(), true, blockDrops) ||
                metaTileEntity.getExportItems().getSlotLimit(0) == Integer.MAX_VALUE - 1) { // Check for ME Output
            GTTransferUtils.addItemsToItemHandler(metaTileEntity.getExportItems(), false, blockDrops);
            world.setBlockState(blocksToMine.getFirst(), oreReplacementBlock);
            mineX.set(blocksToMine.getFirst().getX());
            mineZ.set(blocksToMine.getFirst().getZ());
            mineY.set(blocksToMine.getFirst().getY());
            blocksToMine.removeFirst();
            onMineOperation();

            // if the inventory was previously considered full, mark it as not since an item was able to fit
            if (miner.isInventoryFull())
                miner.setInventoryFull(false);
        } else {
            // the ore block was not able to fit, so the inventory is considered full
            miner.setInventoryFull(true);
        }
    }

    /**
     * This method designates the starting position for mining blocks
     *
     * @param pos           the {@link BlockPos} of the miner itself
     * @param currentRadius the currently set mining radius
     */
    public void initPos(@NotNull BlockPos pos, int currentRadius) {
        x.set(pos.getX() - currentRadius);
        z.set(pos.getZ() - currentRadius);
        y.set(pos.getY() - 1);
        startX.set(pos.getX() - currentRadius);
        startZ.set(pos.getZ() - currentRadius);
        startY.set(pos.getY());
        pipeY.set(pos.getY() - 1);
        mineX.set(pos.getX() - currentRadius);
        mineZ.set(pos.getZ() - currentRadius);
        mineY.set(pos.getY() - 1);
    }

    /**
     * Checks if the current coordinates are invalid
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return {@code true} if the coordinates are invalid, else false
     */
    private static boolean checkCoordinatesInvalid(@NotNull AtomicInteger x, @NotNull AtomicInteger y,
                                                   @NotNull AtomicInteger z) {
        return x.get() == Integer.MAX_VALUE && y.get() == Integer.MAX_VALUE && z.get() == Integer.MAX_VALUE;
    }

    /**
     * Checks whether there are any more blocks to mine, if there are currently none queued
     */
    public void checkBlocksToMine() {
        if (blocksToMine.isEmpty())
            blocksToMine.addAll(getBlocksToMine());
    }

    /**
     * Recalculates the mining area, refills the block list and restarts the miner, if it was done
     */
    public void resetArea() {
        initPos(metaTileEntity.getPos(), currentRadius);
        if (this.isDone) this.setWorkingEnabled(false);
        this.isDone = false;
        blocksToMine.clear();
        checkBlocksToMine();
        resetPipeLength();
    }

    /**
     * Gets the blocks to mine
     *
     * @return a {@link LinkedList} of {@link BlockPos} for each ore to mine
     */
    private LinkedList<BlockPos> getBlocksToMine() {
        LinkedList<BlockPos> blocks = new LinkedList<>();

        // determine how many blocks to retrieve this time
        double quotient = getQuotient(getMeanTickTime(metaTileEntity.getWorld()));
        int calcAmount = quotient < 1 ? 1 : (int) (Math.min(quotient, Short.MAX_VALUE));
        int calculated = 0;

        // keep getting blocks until the target amount is reached
        while (calculated < calcAmount) {
            // moving down the y-axis
            if (y.get() > 0) {
                // moving across the z-axis
                if (z.get() <= startZ.get() + currentRadius * 2) {
                    // check every block along the x-axis
                    if (x.get() <= startX.get() + currentRadius * 2) {
                        BlockPos blockPos = new BlockPos(x.get(), y.get(), z.get());
                        IBlockState state = metaTileEntity.getWorld().getBlockState(blockPos);
                        if (state.getBlock().blockHardness >= 0 &&
                                metaTileEntity.getWorld().getTileEntity(blockPos) == null &&
                                GTUtility.isOre(GTUtility.toItem(state))) {
                            blocks.addLast(blockPos);
                        }
                        // move to the next x position
                        x.incrementAndGet();
                    } else {
                        // reset x and move to the next z layer
                        x.set(startX.get());
                        z.incrementAndGet();
                    }
                } else {
                    // reset z and move to the next y layer
                    z.set(startZ.get());
                    y.decrementAndGet();
                }
            } else
                return blocks;

            // only count iterations where blocks were found
            if (!blocks.isEmpty())
                calculated++;
        }
        return blocks;
    }

    /**
     * @param values to find the mean of
     * @return the mean value
     */
    private static long mean(@NotNull long[] values) {
        if (values.length == 0L)
            return 0L;

        long sum = 0L;
        for (long v : values)
            sum += v;
        return sum / values.length;
    }

    /**
     * @param world the {@link World} to get the average tick time of
     * @return the mean tick time
     */
    private static double getMeanTickTime(@NotNull World world) {
        return mean(Objects.requireNonNull(world.getMinecraftServer()).tickTimeArray) * 1.0E-6D;
    }

    /**
     * gets the quotient for determining the amount of blocks to mine
     *
     * @param base is a value used for calculation, intended to be the mean tick time of the world the miner is in
     * @return the quotient
     */
    private static double getQuotient(double base) {
        return DIVIDEND / Math.pow(base, POWER);
    }

    /**
     * Applies a fortune hammer to block drops based on a tier value, intended for small ores
     *
     * @param blockState   the block being mined
     * @param drops        where the drops are stored to
     * @param fortuneLevel the level of fortune used
     * @param map          the recipemap from which to get the drops
     * @param tier         the tier at which the operation is performed, used for calculating the chanced output boost
     */
    protected static void applyTieredHammerNoRandomDrops(@NotNull IBlockState blockState, List<ItemStack> drops,
                                                         int fortuneLevel, @NotNull RecipeMap<?> map, int tier) {
        ItemStack itemStack = GTUtility.toItem(blockState);
        Recipe recipe = map.findRecipe(Long.MAX_VALUE, Collections.singletonList(itemStack), Collections.emptyList());
        if (recipe != null && !recipe.getOutputs().isEmpty()) {
            drops.clear();
            for (ItemStack outputStack : recipe.getResultItemOutputs(GTUtility.getTierByVoltage(recipe.getEUt()), tier,
                    map)) {
                outputStack = outputStack.copy();
                if (OreDictUnifier.getPrefix(outputStack) == OrePrefix.crushed) {
                    if (fortuneLevel > 0) {
                        outputStack.grow(outputStack.getCount() * fortuneLevel);
                    }
                }
                drops.add(outputStack);
            }
        }
    }

    /**
     * Increments the pipe rendering length by one, signaling that the miner's y level has moved down by one
     */
    private void incrementPipeLength() {
        this.pipeLength++;
        this.metaTileEntity.writeCustomData(GregtechDataCodes.PUMP_HEAD_LEVEL, b -> b.writeInt(pipeLength));
        this.metaTileEntity.markDirty();
    }

    /**
     * Resets the pipe length to zero
     */
    private void resetPipeLength() {
        this.pipeLength = 0;
        this.metaTileEntity.writeCustomData(GregtechDataCodes.PUMP_HEAD_LEVEL, b -> b.writeInt(pipeLength));
        this.metaTileEntity.markDirty();
    }

    /**
     * renders the pipe beneath the miner
     */
    @SideOnly(Side.CLIENT)
    public void renderPipe(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.PIPE_IN_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        for (int i = 0; i < this.pipeLength; i++) {
            translation.translate(0.0, -1.0, 0.0);
            getPipeTexture().render(renderState, translation, pipeline, IMiner.PIPE_CUBOID);
        }
    }

    @SideOnly(Side.CLIENT)
    protected ICubeRenderer getPipeTexture() {
        return PIPE_TEXTURE;
    }

    /**
     * writes all needed values to NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#writeToNBT(NBTTagCompound)} method
     */
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        data.setTag("xPos", new NBTTagInt(x.get()));
        data.setTag("yPos", new NBTTagInt(y.get()));
        data.setTag("zPos", new NBTTagInt(z.get()));
        data.setTag("mxPos", new NBTTagInt(mineX.get()));
        data.setTag("myPos", new NBTTagInt(mineY.get()));
        data.setTag("mzPos", new NBTTagInt(mineZ.get()));
        data.setTag("sxPos", new NBTTagInt(startX.get()));
        data.setTag("syPos", new NBTTagInt(startY.get()));
        data.setTag("szPos", new NBTTagInt(startZ.get()));
        data.setTag("tempY", new NBTTagInt(pipeY.get()));
        data.setTag("isActive", new NBTTagInt(this.isActive ? 1 : 0));
        data.setTag("isWorkingEnabled", new NBTTagInt(this.isWorkingEnabled ? 1 : 0));
        data.setTag("wasActiveAndNeedsUpdate", new NBTTagInt(this.wasActiveAndNeedsUpdate ? 1 : 0));
        data.setTag("pipeLength", new NBTTagInt(pipeLength));
        data.setTag("currentRadius", new NBTTagInt(currentRadius));
        data.setTag("isDone", new NBTTagInt(isDone ? 1 : 0));
        return data;
    }

    /**
     * reads all needed values from NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#readFromNBT(NBTTagCompound)}
     * method
     */
    public void readFromNBT(@NotNull NBTTagCompound data) {
        x.set(data.getInteger("xPos"));
        y.set(data.getInteger("yPos"));
        z.set(data.getInteger("zPos"));
        mineX.set(data.getInteger("mxPos"));
        mineY.set(data.getInteger("myPos"));
        mineZ.set(data.getInteger("mzPos"));
        startX.set(data.getInteger("sxPos"));
        startY.set(data.getInteger("syPos"));
        startZ.set(data.getInteger("szPos"));
        pipeY.set(data.getInteger("tempY"));
        setActive(data.getInteger("isActive") != 0);
        setWorkingEnabled(data.getInteger("isWorkingEnabled") != 0);
        setWasActiveAndNeedsUpdate(data.getInteger("wasActiveAndNeedsUpdate") != 0);
        pipeLength = data.getInteger("pipeLength");
        this.currentRadius = data.getInteger("currentRadius");
        this.isDone = data.getInteger("isDone") != 0;
    }

    /**
     * writes all needed values to InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's
     * {@link MetaTileEntity#writeInitialSyncData(PacketBuffer)} method
     */
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        buf.writeInt(pipeLength);
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.isWorkingEnabled);
        buf.writeBoolean(this.wasActiveAndNeedsUpdate);
    }

    /**
     * reads all needed values from InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's
     * {@link MetaTileEntity#receiveInitialSyncData(PacketBuffer)} method
     */
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        this.pipeLength = buf.readInt();
        setActive(buf.readBoolean());
        setWorkingEnabled(buf.readBoolean());
        setWasActiveAndNeedsUpdate(buf.readBoolean());
    }

    /**
     * reads all needed values from CustomData
     * This MUST be called and returned in the MetaTileEntity's
     * {@link MetaTileEntity#receiveCustomData(int, PacketBuffer)} method
     */
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.PUMP_HEAD_LEVEL) {
            this.pipeLength = buf.readInt();
            metaTileEntity.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.isActive = buf.readBoolean();
            metaTileEntity.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.isWorkingEnabled = buf.readBoolean();
            metaTileEntity.scheduleRenderUpdate();
        }
    }

    /**
     * @return the current x value
     */
    public AtomicInteger getX() {
        return x;
    }

    /**
     * @return the current y value
     */
    public AtomicInteger getY() {
        return y;
    }

    /**
     * @return the current z value
     */
    public AtomicInteger getZ() {
        return z;
    }

    /**
     * @return the previously mined x value
     */
    public AtomicInteger getMineX() {
        return mineX;
    }

    /**
     * @return the previously mined y value
     */
    public AtomicInteger getMineY() {
        return mineY;
    }

    /**
     * @return the previously mined z value
     */
    public AtomicInteger getMineZ() {
        return mineZ;
    }

    /**
     * @return the starting x value
     */
    public AtomicInteger getStartX() {
        return startX;
    }

    /**
     * @return the starting y value
     */
    public AtomicInteger getStartY() {
        return startY;
    }

    /**
     * @return the starting z value
     */
    public AtomicInteger getStartZ() {
        return startZ;
    }

    /**
     * @return the pipe y value
     */
    public AtomicInteger getPipeY() {
        return pipeY;
    }

    /**
     * @return the miner's maximum radius
     */
    public int getMaximumRadius() {
        return this.maximumRadius;
    }

    /**
     * @return the miner's current radius
     */
    public int getCurrentRadius() {
        return this.currentRadius;
    }

    /**
     * @param currentRadius the radius to set the miner to use
     */
    public void setCurrentRadius(int currentRadius) {
        this.currentRadius = currentRadius;
    }

    /**
     * @return true if the miner is finished working
     */
    public boolean isDone() {
        return this.isDone;
    }

    /**
     * @return true if the miner is active
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     * @param isActive the new state of the miner's activity: true to change to active, else false
     */
    public void setActive(boolean isActive) {
        if (this.isActive != isActive) {
            this.isActive = isActive;
            this.metaTileEntity.markDirty();
            if (metaTileEntity.getWorld() != null && !metaTileEntity.getWorld().isRemote) {
                this.metaTileEntity.writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE,
                        buf -> buf.writeBoolean(isActive));
            }
        }
    }

    /**
     * @param isWorkingEnabled the new state of the miner's ability to work: true to change to enabled, else false
     */
    public void setWorkingEnabled(boolean isWorkingEnabled) {
        if (this.isWorkingEnabled != isWorkingEnabled) {
            this.isWorkingEnabled = isWorkingEnabled;
            metaTileEntity.markDirty();
            if (metaTileEntity.getWorld() != null && !metaTileEntity.getWorld().isRemote) {
                if (!isWorkingEnabled) resetArea();

                this.metaTileEntity.writeCustomData(GregtechDataCodes.WORKING_ENABLED,
                        buf -> buf.writeBoolean(isWorkingEnabled));
            }
        }
    }

    /**
     * @return whether working is enabled for the logic
     */
    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    /**
     * @return whether the miner is currently working
     */
    public boolean isWorking() {
        return isActive && isWorkingEnabled;
    }

    /**
     * @return whether the miner was active and needs an update
     */
    public boolean wasActiveAndNeedsUpdate() {
        return this.wasActiveAndNeedsUpdate;
    }

    /**
     * set whether the miner was active and needs an update
     *
     * @param wasActiveAndNeedsUpdate the state to set
     */
    public void setWasActiveAndNeedsUpdate(boolean wasActiveAndNeedsUpdate) {
        this.wasActiveAndNeedsUpdate = wasActiveAndNeedsUpdate;
    }

    /**
     * @return the miner's fortune level
     */
    public int getFortune() {
        return this.fortune;
    }

    /**
     * @return the miner's speed in ticks
     */
    public int getSpeed() {
        return this.speed;
    }
}
