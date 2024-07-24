package gregtech.api.pattern;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.registry.MTERegistry;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockPattern {

    static EnumFacing[] FACINGS = { EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.UP,
            EnumFacing.DOWN };
    public final int[][] aisleRepetitions;
    public final RelativeDirection[] structureDir;
    protected final TraceabilityPredicate[][][] blockMatches; // [z][y][x]
    protected final int fingerLength; // z size
    protected final int thumbLength; // y size
    protected final int palmLength; // x size
    protected final BlockWorldState worldState = new BlockWorldState();
    protected final PatternMatchContext matchContext = new PatternMatchContext();
    protected final Map<TraceabilityPredicate.SimplePredicate, Integer> globalCount;
    protected final Map<TraceabilityPredicate.SimplePredicate, Integer> layerCount;

    public Long2ObjectMap<BlockInfo> cache = new Long2ObjectOpenHashMap<>();
    // x, y, z, minZ, maxZ
    private int[] centerOffset = null;

    /**
     * The repetitions per aisle along the axis of repetition
     */
    public int[] formedRepetitionCount;

    public BlockPattern(@NotNull TraceabilityPredicate[][][] predicatesIn, @NotNull RelativeDirection[] structureDir,
                        @NotNull int[][] aisleRepetitions) {
        this.blockMatches = predicatesIn;
        this.globalCount = new HashMap<>();
        this.layerCount = new HashMap<>();
        this.fingerLength = predicatesIn.length;
        this.structureDir = structureDir;
        this.aisleRepetitions = aisleRepetitions;
        this.formedRepetitionCount = new int[aisleRepetitions.length];

        if (this.fingerLength > 0) {
            this.thumbLength = predicatesIn[0].length;

            if (this.thumbLength > 0) {
                this.palmLength = predicatesIn[0][0].length;
            } else {
                this.palmLength = 0;
            }
        } else {
            this.thumbLength = 0;
            this.palmLength = 0;
        }

        initializeCenterOffsets();
    }

    private void initializeCenterOffsets() {
        loop:
        for (int x = 0; x < this.palmLength; x++) {
            for (int y = 0; y < this.thumbLength; y++) {
                for (int z = 0, minZ = 0, maxZ = 0; z <
                        this.fingerLength; minZ += aisleRepetitions[z][0], maxZ += aisleRepetitions[z][1], z++) {
                    TraceabilityPredicate predicate = this.blockMatches[z][y][x];
                    if (predicate.isCenter) {
                        centerOffset = new int[] { x, y, z, minZ, maxZ };
                        break loop;
                    }
                }
            }
        }
        if (centerOffset == null) {
            throw new IllegalArgumentException("Didn't find center predicate");
        }
    }

    public PatternError getError() {
        return worldState.error;
    }

    public PatternMatchContext checkPatternFastAt(World world, BlockPos centerPos, EnumFacing frontFacing,
                                                  EnumFacing upwardsFacing, boolean allowsFlip) {
        if (!cache.isEmpty()) {
            boolean pass = true;
            for (Map.Entry<Long, BlockInfo> entry : cache.entrySet()) {
                BlockPos pos = BlockPos.fromLong(entry.getKey());
                IBlockState blockState = world.getBlockState(pos);
                if (blockState != entry.getValue().getBlockState()) {
                    pass = false;
                    break;
                }
                TileEntity cachedTileEntity = entry.getValue().getTileEntity();
                if (cachedTileEntity != null) {
                    TileEntity tileEntity = world.getTileEntity(pos);
                    if (tileEntity != cachedTileEntity) {
                        pass = false;
                        break;
                    }
                }
            }
            if (pass) return worldState.hasError() ? null : matchContext;
        }

        // First try normal pattern, and if it fails, try flipped (if allowed).
        PatternMatchContext pmc = checkPatternAt(world, centerPos, frontFacing, upwardsFacing, false);
        if (allowsFlip) {
            if (pmc != null) {
                return pmc;
            }
            pmc = checkPatternAt(world, centerPos, frontFacing, upwardsFacing, true);
        }
        if (pmc == null) clearCache(); // we don't want a random cache of a partially formed multi
        return pmc;
    }

    public void clearCache() {
        cache.clear();
    }

    private PatternMatchContext checkPatternAt(World world, BlockPos centerPos, EnumFacing frontFacing,
                                               EnumFacing upwardsFacing, boolean isFlipped) {
        boolean findFirstAisle = false;
        int minZ = -centerOffset[4];

        this.matchContext.reset();
        this.globalCount.clear();
        this.layerCount.clear();
        cache.clear();
        // Checking aisles
        for (int c = 0, z = minZ++, r; c < this.fingerLength; c++) {
            // Checking repeatable slices
            int validRepetitions = 0;
            loop:
            for (r = 0; (findFirstAisle ? r < aisleRepetitions[c][1] : z <= -centerOffset[3]); r++) {
                // Checking single slice
                this.layerCount.clear();

                for (int b = 0, y = -centerOffset[1]; b < this.thumbLength; b++, y++) {
                    for (int a = 0, x = -centerOffset[0]; a < this.palmLength; a++, x++) {
                        TraceabilityPredicate predicate = this.blockMatches[c][b][a];
                        BlockPos pos = RelativeDirection.setActualRelativeOffset(x, y, z, frontFacing, upwardsFacing,
                                isFlipped, structureDir)
                                .add(centerPos.getX(), centerPos.getY(), centerPos.getZ());
                        worldState.update(world, pos, matchContext, globalCount, layerCount, predicate);
                        TileEntity tileEntity = worldState.getTileEntity();
                        if (predicate != TraceabilityPredicate.ANY) {
                            if (tileEntity instanceof IGregTechTileEntity) {
                                if (((IGregTechTileEntity) tileEntity).isValid()) {
                                    cache.put(pos.toLong(),
                                            new BlockInfo(worldState.getBlockState(), tileEntity, predicate));
                                } else {
                                    cache.put(pos.toLong(), new BlockInfo(worldState.getBlockState(), null, predicate));
                                }
                            } else {
                                cache.put(pos.toLong(),
                                        new BlockInfo(worldState.getBlockState(), tileEntity, predicate));
                            }
                        }
                        if (!predicate.test(worldState)) {
                            if (findFirstAisle) {
                                if (r < aisleRepetitions[c][0]) {// retreat to see if the first aisle can start later
                                    r = c = 0;
                                    z = minZ++;
                                    matchContext.reset();
                                    findFirstAisle = false;
                                }
                            } else {
                                z++;// continue searching for the first aisle
                            }
                            continue loop;
                        }
                    }
                }
                findFirstAisle = true;
                z++;

                // Check layer-local matcher predicate
                for (Map.Entry<TraceabilityPredicate.SimplePredicate, Integer> entry : layerCount.entrySet()) {
                    if (entry.getValue() < entry.getKey().minLayerCount) {
                        worldState.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 3));
                        return null;
                    }
                }
                validRepetitions++;
            }
            // Repetitions out of range
            if (r < aisleRepetitions[c][0]) {
                if (!worldState.hasError()) {
                    worldState.setError(new PatternError());
                }
                return null;
            }

            // finished checking the aisle, so store the repetitions
            formedRepetitionCount[c] = validRepetitions;
        }

        // Check count matches amount
        for (Map.Entry<TraceabilityPredicate.SimplePredicate, Integer> entry : globalCount.entrySet()) {
            if (entry.getValue() < entry.getKey().minGlobalCount) {
                worldState.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 1));
                return null;
            }
        }

        worldState.setError(null);
        matchContext.setNeededFlip(isFlipped);
        return matchContext;
    }

    public void autoBuild(EntityPlayer player, MultiblockControllerBase controllerBase) {
        World world = player.world;
        BlockWorldState worldState = new BlockWorldState();
        int minZ = -centerOffset[4];
        EnumFacing facing = controllerBase.getFrontFacing().getOpposite();
        BlockPos centerPos = controllerBase.getPos();
        Map<TraceabilityPredicate.SimplePredicate, BlockInfo[]> cacheInfos = new HashMap<>();
        Map<TraceabilityPredicate.SimplePredicate, Integer> cacheGlobal = new HashMap<>();
        Map<BlockPos, Object> blocks = new HashMap<>();
        blocks.put(controllerBase.getPos(), controllerBase);
        for (int c = 0, z = minZ++, r; c < this.fingerLength; c++) {
            for (r = 0; r < aisleRepetitions[c][0]; r++) {
                Map<TraceabilityPredicate.SimplePredicate, Integer> cacheLayer = new HashMap<>();
                for (int b = 0, y = -centerOffset[1]; b < this.thumbLength; b++, y++) {
                    for (int a = 0, x = -centerOffset[0]; a < this.palmLength; a++, x++) {
                        TraceabilityPredicate predicate = this.blockMatches[c][b][a];
                        BlockPos pos = RelativeDirection.setActualRelativeOffset(x, y, z, facing,
                                controllerBase.getUpwardsFacing(),
                                controllerBase.isFlipped(), structureDir)
                                .add(centerPos.getX(), centerPos.getY(), centerPos.getZ());
                        worldState.update(world, pos, matchContext, globalCount, layerCount, predicate);
                        if (!world.getBlockState(pos).getMaterial().isReplaceable()) {
                            blocks.put(pos, world.getBlockState(pos));
                            for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
                                limit.testLimited(worldState);
                            }
                        } else {
                            boolean find = false;
                            BlockInfo[] infos = new BlockInfo[0];
                            for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
                                if (limit.minLayerCount > 0) {
                                    if (!cacheLayer.containsKey(limit)) {
                                        cacheLayer.put(limit, 1);
                                    } else if (cacheLayer.get(limit) < limit.minLayerCount &&
                                            (limit.maxLayerCount == -1 ||
                                                    cacheLayer.get(limit) < limit.maxLayerCount)) {
                                                        cacheLayer.put(limit, cacheLayer.get(limit) + 1);
                                                    } else {
                                                        continue;
                                                    }
                                } else {
                                    continue;
                                }
                                if (!cacheInfos.containsKey(limit)) {
                                    cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                                }
                                infos = cacheInfos.get(limit);
                                find = true;
                                break;
                            }
                            if (!find) {
                                for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
                                    if (limit.minGlobalCount > 0) {
                                        if (!cacheGlobal.containsKey(limit)) {
                                            cacheGlobal.put(limit, 1);
                                        } else if (cacheGlobal.get(limit) < limit.minGlobalCount &&
                                                (limit.maxGlobalCount == -1 ||
                                                        cacheGlobal.get(limit) < limit.maxGlobalCount)) {
                                                            cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                                        } else {
                                                            continue;
                                                        }
                                    } else {
                                        continue;
                                    }
                                    if (!cacheInfos.containsKey(limit)) {
                                        cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                                    }
                                    infos = cacheInfos.get(limit);
                                    find = true;
                                    break;
                                }
                            }
                            if (!find) { // no limited
                                for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
                                    if (limit.maxLayerCount != -1 &&
                                            cacheLayer.getOrDefault(limit, Integer.MAX_VALUE) == limit.maxLayerCount)
                                        continue;
                                    if (limit.maxGlobalCount != -1 &&
                                            cacheGlobal.getOrDefault(limit, Integer.MAX_VALUE) == limit.maxGlobalCount)
                                        continue;
                                    if (!cacheInfos.containsKey(limit)) {
                                        cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                                    }
                                    if (cacheLayer.containsKey(limit)) {
                                        cacheLayer.put(limit, cacheLayer.get(limit) + 1);
                                    } else {
                                        cacheLayer.put(limit, 1);
                                    }
                                    if (cacheGlobal.containsKey(limit)) {
                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                    } else {
                                        cacheGlobal.put(limit, 1);
                                    }
                                    infos = ArrayUtils.addAll(infos, cacheInfos.get(limit));
                                }
                                for (TraceabilityPredicate.SimplePredicate common : predicate.common) {
                                    if (!cacheInfos.containsKey(common)) {
                                        cacheInfos.put(common,
                                                common.candidates == null ? null : common.candidates.get());
                                    }
                                    infos = ArrayUtils.addAll(infos, cacheInfos.get(common));
                                }
                            }

                            List<ItemStack> candidates = Arrays.stream(infos)
                                    .filter(info -> info.getBlockState().getBlock() != Blocks.AIR).map(info -> {
                                        IBlockState blockState = info.getBlockState();
                                        MetaTileEntity metaTileEntity = info
                                                .getTileEntity() instanceof IGregTechTileEntity ?
                                                        ((IGregTechTileEntity) info.getTileEntity())
                                                                .getMetaTileEntity() :
                                                        null;
                                        if (metaTileEntity != null) {
                                            return metaTileEntity.getStackForm();
                                        } else {
                                            return new ItemStack(Item.getItemFromBlock(blockState.getBlock()), 1,
                                                    blockState.getBlock().damageDropped(blockState));
                                        }
                                    }).collect(Collectors.toList());
                            if (candidates.isEmpty()) continue;
                            // check inventory
                            ItemStack found = null;
                            if (!player.isCreative()) {
                                for (ItemStack itemStack : player.inventory.mainInventory) {
                                    if (candidates.stream().anyMatch(candidate -> candidate.isItemEqual(itemStack)) &&
                                            !itemStack.isEmpty() && itemStack.getItem() instanceof ItemBlock) {
                                        found = itemStack.copy();
                                        itemStack.setCount(itemStack.getCount() - 1);
                                        break;
                                    }
                                }
                                if (found == null) continue;
                            } else {
                                for (int i = candidates.size() - 1; i >= 0; i--) {
                                    found = candidates.get(i).copy();
                                    if (!found.isEmpty() && found.getItem() instanceof ItemBlock) {
                                        break;
                                    }
                                    found = null;
                                }
                                if (found == null) continue;
                            }
                            ItemBlock itemBlock = (ItemBlock) found.getItem();
                            IBlockState state = itemBlock.getBlock()
                                    .getStateFromMeta(itemBlock.getMetadata(found.getMetadata()));
                            blocks.put(pos, state);
                            world.setBlockState(pos, state);
                            TileEntity holder = world.getTileEntity(pos);
                            if (holder instanceof IGregTechTileEntity igtte) {
                                MTERegistry registry = GregTechAPI.mteManager
                                        .getRegistry(found.getItem().getRegistryName().getNamespace());
                                MetaTileEntity sampleMetaTileEntity = registry.getObjectById(found.getItemDamage());
                                if (sampleMetaTileEntity != null) {
                                    MetaTileEntity metaTileEntity = igtte.setMetaTileEntity(sampleMetaTileEntity);
                                    metaTileEntity.onPlacement();
                                    blocks.put(pos, metaTileEntity);
                                    if (found.getTagCompound() != null) {
                                        metaTileEntity.initFromItemStackData(found.getTagCompound());
                                    }
                                }
                            }
                        }
                    }
                }
                z++;
            }
        }
        EnumFacing[] facings = ArrayUtils.addAll(new EnumFacing[] { controllerBase.getFrontFacing() },
                FACINGS); // follow
        // controller
        // first
        blocks.forEach((pos, block) -> { // adjust facing
            if (block instanceof MetaTileEntity) {
                MetaTileEntity metaTileEntity = (MetaTileEntity) block;
                boolean find = false;
                for (EnumFacing enumFacing : facings) {
                    if (metaTileEntity.isValidFrontFacing(enumFacing)) {
                        if (!blocks.containsKey(pos.offset(enumFacing))) {
                            metaTileEntity.setFrontFacing(enumFacing);
                            find = true;
                            break;
                        }
                    }
                }
                if (!find) {
                    for (EnumFacing enumFacing : FACINGS) {
                        if (world.isAirBlock(pos.offset(enumFacing)) && metaTileEntity.isValidFrontFacing(enumFacing)) {
                            metaTileEntity.setFrontFacing(enumFacing);
                            break;
                        }
                    }
                }
            }
        });
    }

    public BlockInfo[][][] getPreview(int[] repetition) {
        Map<TraceabilityPredicate.SimplePredicate, BlockInfo[]> cacheInfos = new HashMap<>();
        Map<TraceabilityPredicate.SimplePredicate, Integer> cacheGlobal = new HashMap<>();
        Map<BlockPos, BlockInfo> blocks = new HashMap<>();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (int l = 0, x = 0; l < this.fingerLength; l++) {
            for (int r = 0; r < repetition[l]; r++) {
                // Checking single slice
                Map<TraceabilityPredicate.SimplePredicate, Integer> cacheLayer = new HashMap<>();
                for (int y = 0; y < this.thumbLength; y++) {
                    for (int z = 0; z < this.palmLength; z++) {
                        TraceabilityPredicate predicate = this.blockMatches[l][y][z];
                        boolean find = false;
                        BlockInfo[] infos = null;
                        for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) { // check layer and
                            // previewCount
                            if (limit.minLayerCount > 0) {
                                if (!cacheLayer.containsKey(limit)) {
                                    cacheLayer.put(limit, 1);
                                } else if (cacheLayer.get(limit) < limit.minLayerCount) {
                                    cacheLayer.put(limit, cacheLayer.get(limit) + 1);
                                } else {
                                    continue;
                                }
                                if (cacheGlobal.getOrDefault(limit, 0) < limit.previewCount) {
                                    if (!cacheGlobal.containsKey(limit)) {
                                        cacheGlobal.put(limit, 1);
                                    } else if (cacheGlobal.get(limit) < limit.previewCount) {
                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                    } else {
                                        continue;
                                    }
                                }
                            } else {
                                continue;
                            }
                            if (!cacheInfos.containsKey(limit)) {
                                cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                            }
                            infos = cacheInfos.get(limit);
                            find = true;
                            break;
                        }
                        if (!find) { // check global and previewCount
                            for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
                                if (limit.minGlobalCount == -1 && limit.previewCount == -1) continue;
                                if (cacheGlobal.getOrDefault(limit, 0) < limit.previewCount) {
                                    if (!cacheGlobal.containsKey(limit)) {
                                        cacheGlobal.put(limit, 1);
                                    } else if (cacheGlobal.get(limit) < limit.previewCount) {
                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                    } else {
                                        continue;
                                    }
                                } else if (limit.minGlobalCount > 0) {
                                    if (!cacheGlobal.containsKey(limit)) {
                                        cacheGlobal.put(limit, 1);
                                    } else if (cacheGlobal.get(limit) < limit.minGlobalCount) {
                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                    } else {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                                if (!cacheInfos.containsKey(limit)) {
                                    cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                                }
                                infos = cacheInfos.get(limit);
                                find = true;
                                break;
                            }
                        }
                        if (!find) { // check common with previewCount
                            for (TraceabilityPredicate.SimplePredicate common : predicate.common) {
                                if (common.previewCount > 0) {
                                    if (!cacheGlobal.containsKey(common)) {
                                        cacheGlobal.put(common, 1);
                                    } else if (cacheGlobal.get(common) < common.previewCount) {
                                        cacheGlobal.put(common, cacheGlobal.get(common) + 1);
                                    } else {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                                if (!cacheInfos.containsKey(common)) {
                                    cacheInfos.put(common, common.candidates == null ? null : common.candidates.get());
                                }
                                infos = cacheInfos.get(common);
                                find = true;
                                break;
                            }
                        }
                        if (!find) { // check without previewCount
                            for (TraceabilityPredicate.SimplePredicate common : predicate.common) {
                                if (common.previewCount == -1) {
                                    if (!cacheInfos.containsKey(common)) {
                                        cacheInfos.put(common,
                                                common.candidates == null ? null : common.candidates.get());
                                    }
                                    infos = cacheInfos.get(common);
                                    find = true;
                                    break;
                                }
                            }
                        }
                        if (!find) { // check max
                            for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
                                if (limit.previewCount != -1) {
                                    continue;
                                } else if (limit.maxGlobalCount != -1 || limit.maxLayerCount != -1) {
                                    if (cacheGlobal.getOrDefault(limit, 0) < limit.maxGlobalCount) {
                                        if (!cacheGlobal.containsKey(limit)) {
                                            cacheGlobal.put(limit, 1);
                                        } else {
                                            cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                        }
                                    } else if (cacheLayer.getOrDefault(limit, 0) < limit.maxLayerCount) {
                                        if (!cacheLayer.containsKey(limit)) {
                                            cacheLayer.put(limit, 1);
                                        } else {
                                            cacheLayer.put(limit, cacheLayer.get(limit) + 1);
                                        }
                                    } else {
                                        continue;
                                    }
                                }

                                if (!cacheInfos.containsKey(limit)) {
                                    cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                                }
                                infos = cacheInfos.get(limit);
                                break;
                            }
                        }
                        BlockInfo info = infos == null || infos.length == 0 ? BlockInfo.EMPTY : infos[0];
                        BlockPos pos = RelativeDirection.setActualRelativeOffset(z, y, x, EnumFacing.NORTH,
                                EnumFacing.UP, false, structureDir);
                        // TODO
                        if (info.getTileEntity() instanceof MetaTileEntityHolder) {
                            MetaTileEntityHolder holder = new MetaTileEntityHolder();
                            holder.setMetaTileEntity(((MetaTileEntityHolder) info.getTileEntity()).getMetaTileEntity());
                            holder.getMetaTileEntity().onPlacement();
                            info = new BlockInfo(holder.getMetaTileEntity().getBlock().getDefaultState(), holder);
                        }
                        blocks.put(pos, info);
                        minX = Math.min(pos.getX(), minX);
                        minY = Math.min(pos.getY(), minY);
                        minZ = Math.min(pos.getZ(), minZ);
                        maxX = Math.max(pos.getX(), maxX);
                        maxY = Math.max(pos.getY(), maxY);
                        maxZ = Math.max(pos.getZ(), maxZ);
                    }
                }
                x++;
            }
        }
        BlockInfo[][][] result = (BlockInfo[][][]) Array.newInstance(BlockInfo.class, maxX - minX + 1, maxY - minY + 1,
                maxZ - minZ + 1);
        int finalMinX = minX;
        int finalMinY = minY;
        int finalMinZ = minZ;
        blocks.forEach((pos, info) -> {
            if (info.getTileEntity() instanceof MetaTileEntityHolder) {
                MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) info.getTileEntity()).getMetaTileEntity();
                boolean find = false;
                for (EnumFacing enumFacing : FACINGS) {
                    if (metaTileEntity.isValidFrontFacing(enumFacing)) {
                        if (!blocks.containsKey(pos.offset(enumFacing))) {
                            metaTileEntity.setFrontFacing(enumFacing);
                            find = true;
                            break;
                        }
                    }
                }
                if (!find) {
                    for (EnumFacing enumFacing : FACINGS) {
                        BlockInfo blockInfo = blocks.get(pos.offset(enumFacing));
                        if (blockInfo != null && blockInfo.getBlockState().getBlock() == Blocks.AIR &&
                                metaTileEntity.isValidFrontFacing(enumFacing)) {
                            metaTileEntity.setFrontFacing(enumFacing);
                            break;
                        }
                    }
                }
            }
            result[pos.getX() - finalMinX][pos.getY() - finalMinY][pos.getZ() - finalMinZ] = info;
        });
        return result;
    }
}
