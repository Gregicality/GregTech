package gregtech.api.worldgen.bedrockFluids;

import gregtech.api.net.NetworkHandler;
import gregtech.api.util.GTUtility;
import gregtech.api.worldgen.config.BedrockFluidDepositDefinition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BedrockFluidVeinHandler {

    public final static LinkedHashMap<BedrockFluidDepositDefinition, Integer> veinList = new LinkedHashMap<>();
    private final static Map<Integer, HashMap<Integer, Integer>> totalWeightMap = new HashMap<>();
    public static HashMap<ChunkPosDimension, FluidVeinWorldEntry> veinCache = new HashMap<>();

    private static final int veinChunkSize = 8; // veins are 8x8 chunk squares

    /**
     * Gets the FluidVeinWorldInfo object associated with the given chunk
     *
     * @param world  The world to retrieve
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return The FluidVeinWorldInfo corresponding with the given chunk
     */
    public static FluidVeinWorldEntry getFluidVeinWorldEntry(World world, int chunkX, int chunkZ) {
        if (world.isRemote)
            return null;

        ChunkPosDimension coords = new ChunkPosDimension(world.provider.getDimension(), chunkX / veinChunkSize, chunkZ / veinChunkSize);

        FluidVeinWorldEntry worldEntry = veinCache.get(coords);
        if (worldEntry == null) {
            BedrockFluidDepositDefinition definition = null;

            int query = world.getChunk(chunkX / veinChunkSize, chunkZ / veinChunkSize).getRandomWithSeed(90210).nextInt();

            Biome biome = world.getBiomeForCoordsBody(new BlockPos(chunkX << 4, 64, chunkZ << 4));
            int totalWeight = getTotalWeight(world.provider, biome);
            if (totalWeight != 0) {
                int weight = Math.abs(query % totalWeight);
                for (Map.Entry<BedrockFluidDepositDefinition, Integer> entry : veinList.entrySet()) {
                    int veinWeight = entry.getValue() + entry.getKey().getBiomeWeightModifier().apply(biome);
                    if (veinWeight != 0 && entry.getKey().getDimensionFilter().test(world.provider) &&
                            entry.getKey().getBiomeWeightModifier().apply(biome) != 0) {
                        weight -= veinWeight;
                        if (weight < 0) {
                            definition = entry.getKey();
                            break;
                        }
                    }
                }
            }

            int capacity = 0;

            if (definition != null) { //todo scale capacity to be not 100% random
                capacity = GTUtility.getRandomIntXSTR(definition.getMaximumProductionRate()) + definition.getMinimumProductionRate();
            }

            worldEntry = new FluidVeinWorldEntry();
            worldEntry.capacity = capacity;
            worldEntry.current = capacity;
            worldEntry.vein = definition;
            veinCache.put(coords, worldEntry);
        }
        return worldEntry;
    }

    /**
     * Gets production rate of fluid in a specific chunk's vein
     *
     * @param world  The world to test
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return rate of fluid in the given vein
     */
    public static int getFluidRateInChunk(World world, int chunkX, int chunkZ) {
        if (world.isRemote)
            return 0;

        FluidVeinWorldEntry info = getFluidVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null)
            return 0;
        return info.current;
    }

    /**
     * Gets the Fluid in a specific chunk's vein
     *
     * @param world  The world to test
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return Fluid in given vein
     */
    public static Fluid getFluid(World world, int chunkX, int chunkZ) {
        if (world.isRemote)
            return null;

        FluidVeinWorldEntry info = getFluidVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null)
            return null;
        return info.getVein().getStoredFluid();
    }

    /**
     * Gets the rate of fluid that in the chunk after depletion
     *
     * @param world  The world to test
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return rate of fluid produced post depletion
     */
    public static int getDepletedFluidRate(World world, int chunkX, int chunkZ) {
        if (world.isRemote)
            return 0;

        FluidVeinWorldEntry info = getFluidVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null)
            return 0;
        return info.getVein().getDepletedProductionRate();
    }

    /**
     * Depletes fluid from a given chunk
     *
     * @param world  World whose chunk to drain
     * @param chunkX Chunk x
     * @param chunkZ Chunk z
     */
    public static void depleteVein(World world, int chunkX, int chunkZ) {
        if (world.isRemote)
            return;

        FluidVeinWorldEntry info = getFluidVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null)
            return;

        BedrockFluidDepositDefinition definition = info.getVein();

        // attempt to deplete the vein
        // ciel(chance) - chance * 100 = % to get depleted
        // if random value < % chance, deplete by 1
//        int rawChance = definition.getDepletionChance();
//        int chanceToDecrease = (int) (100 * (Math.ceil(rawChance) - rawChance));
//        if (GTUtility.getRandomIntXSTR(100) < chanceToDecrease)

        // alternative depletion algorithm: 1 in vein's chance to deplete by vein's depletion amount
        if (GTUtility.getRandomIntXSTR(definition.getDepletionChance()) == 1)
            info.current = Math.max(0, info.current - definition.getDepletionAmount());

        BedrockFluidVeinSaveData.setDirty();
    }

    /**
     * Gets the total weight of all veins for the given dimension ID and biome type
     *
     * @param provider The WorldProvider whose dimension to check
     * @param biome The biome type to check
     * @return The total weight associated with the dimension/biome pair
     */
    public static int getTotalWeight(WorldProvider provider, Biome biome) {
        int dim = provider.getDimension();
        if (!totalWeightMap.containsKey(dim)) {
            totalWeightMap.put(dim, new HashMap<>());
        }

        Map<Integer, Integer> dimMap = totalWeightMap.get(dim);
        int biomeID = Biome.getIdForBiome(biome);

        if (dimMap.containsKey(biomeID)) {
            return dimMap.get(biomeID);
        }

        int totalWeight = 0;
        for (Map.Entry<BedrockFluidDepositDefinition, Integer> entry : veinList.entrySet()) {
            if (entry.getKey().getDimensionFilter().test(provider))
                totalWeight += entry.getKey().getBiomeWeightModifier().apply(biome);
            totalWeight += entry.getKey().getWeight();
        }

        // make sure the vein can generate if no biome weighting is added
        if (totalWeight == 0)
            totalWeight = 1;

        dimMap.put(biomeID, totalWeight);
        return totalWeight;
    }

    /**
     * Adds a vein to the pool of veins
     *
     * @param definition the vein to add
     */
    public static void addFluidDeposit(BedrockFluidDepositDefinition definition) {
        veinList.put(definition, definition.getWeight());
    }

    public static void recalculateChances(boolean mutePackets) {
        totalWeightMap.clear();
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && !mutePackets) {
            HashMap<FluidVeinWorldEntry, Integer> packetMap = new HashMap<>();
            for (Map.Entry<ChunkPosDimension, FluidVeinWorldEntry> entry : BedrockFluidVeinHandler.veinCache.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null)
                    packetMap.put(entry.getValue(), entry.getValue().getVein().getWeight());
            }
            NetworkHandler.INSTANCE.sendToAll(new MessageBedrockFluidVeinListSync(packetMap));
        }
    }

    public static class FluidVeinWorldEntry {
        public BedrockFluidDepositDefinition vein;
        public BedrockFluidDepositDefinition defaultVein;
        public int capacity;
        public int current;

        public BedrockFluidDepositDefinition getVein() {
            return (defaultVein == null) ? vein : defaultVein;
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("capacity", capacity);
            tag.setInteger("fluid", current);
            if (vein != null) {
                tag.setString("vein", vein.getStoredFluid().getName());
            }
            if (defaultVein != null) {
                tag.setString("defaultVein", defaultVein.getStoredFluid().getName());
            }
            return tag;
        }

        public static FluidVeinWorldEntry readFromNBT(NBTTagCompound tag) {
            FluidVeinWorldEntry info = new FluidVeinWorldEntry();
            info.capacity = tag.getInteger("capacity");
            info.current = tag.getInteger("fluid");

            if (tag.hasKey("vein")) {
                String s = tag.getString("vein");
                for (BedrockFluidDepositDefinition definition : veinList.keySet()) {
                    if (s.equalsIgnoreCase(definition.getStoredFluid().getName()))
                        info.vein = definition;
                }
            } else if (info.current > 0) {
                for (BedrockFluidDepositDefinition definition : veinList.keySet()) {
                    if (definition.getStoredFluid().getName().equalsIgnoreCase("fluid"))
                        info.vein = definition;
                }

                if (info.vein == null) {
                    return null;
                }
            }

            if (tag.hasKey("defaultVein")) {
                String s = tag.getString("defaultVein");
                for (BedrockFluidDepositDefinition definition : veinList.keySet()) {
                    if (s.equalsIgnoreCase(definition.getStoredFluid().getName()))
                        info.defaultVein = definition;
                }
            }

            return info;
        }
    }
}
