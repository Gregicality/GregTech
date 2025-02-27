package gregtech.integration.exnihilo.recipes;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.FileUtility;
import gregtech.common.blocks.MetaBlocks;
import gregtech.integration.IntegrationModule;
import gregtech.integration.exnihilo.ExNihiloConfig;
import gregtech.integration.exnihilo.ExNihiloModule;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import exnihilocreatio.ModBlocks;
import exnihilocreatio.blocks.BlockSieve;
import exnihilocreatio.registries.manager.ExNihiloRegistryManager;
import exnihilocreatio.registries.types.Siftable;
import exnihilocreatio.util.ItemInfo;

import java.io.File;
import java.util.Map;

import static gregtech.integration.exnihilo.ExNihiloModule.*;

public class SieveDrops {

    private static boolean validateDrops(String material, int meshlevel, float chance) {
        if (GregTechAPI.materialManager.getMaterial(material) == null) {
            IntegrationModule.logger.error("Material {} does not exist!", material);
            return false;
        } else if (!GregTechAPI.materialManager.getMaterial(material).hasProperty(PropertyKey.ORE)) {
            IntegrationModule.logger.error("Material {} does not have Ore property!", material);
            return false;
        }
        if (chance > 1F) {
            IntegrationModule.logger.error("Chance for {} can't be higher than 1!", material);
            return false;
        }
        if (meshlevel > 4) {
            IntegrationModule.logger.error("Mesh Level for {} out of range!", material);
            return false;
        }
        return true;
    }

    private static void processDrops(JsonElement element) {
        if (!element.isJsonObject()) {
            IntegrationModule.logger.error("Parsed JSONElement is not an JSON Object!");
            return;
        }
        JsonObject object = element.getAsJsonObject();
        object.entrySet().forEach(set -> {
            String oreDict;
            Block block;
            if (set.getKey().startsWith("ore:")) {
                block = null;
                oreDict = set.getKey().substring(4);
                if (!OreDictionary.doesOreNameExist(oreDict)) {
                    IntegrationModule.logger.error(String.format("OreDict %s does not exist!", oreDict));
                    return;
                }
            } else {
                oreDict = null;
                block = Block.getBlockFromName(set.getKey());
                if (block == null) {
                    IntegrationModule.logger.error(String.format("Block with ID %s does not exist!", set.getKey()));
                    return;
                }
            }

            NonNullList<Siftable> drops = NonNullList.create();
            JsonObject m = set.getValue().getAsJsonObject();
            for (Map.Entry<String, JsonElement> material : m.entrySet()) {
                JsonObject values = material.getValue().getAsJsonObject();
                ItemStack stack;
                String key = material.getKey().toLowerCase();
                if (!validateDrops(key, values.get("meshlevel").getAsInt(),
                        values.get("chance").getAsFloat())) {
                    continue;
                }
                if (oreDict != null || !(block == ModBlocks.netherrackCrushed || block == ModBlocks.endstoneCrushed)) {
                    stack = OreDictUnifier.get(oreChunk, GregTechAPI.materialManager.getMaterial(key));
                } else {
                    stack = block == ModBlocks.netherrackCrushed ?
                            OreDictUnifier.get(oreNetherChunk,
                                    GregTechAPI.materialManager.getMaterial(key)) :
                            OreDictUnifier.get(oreEnderChunk,
                                    GregTechAPI.materialManager.getMaterial(key));
                }
                drops.add(new Siftable(new ItemInfo(stack.getItem(), stack.getMetadata()),
                        values.get("chance").getAsFloat(), values.get("meshlevel").getAsInt()));
            }
            if (oreDict != null) {
                for (ItemStack stack : OreDictionary.getOres(oreDict)) {
                    ExNihiloRegistryManager.SIEVE_REGISTRY.getRegistry().put(Ingredient.fromStacks(stack), drops);
                }
            } else {
                ExNihiloRegistryManager.SIEVE_REGISTRY.getRegistry().put(Ingredient.fromStacks(new ItemStack(block)),
                        drops);
            }
        });
    }

    public static void registerRecipes() {
        NonNullList<Siftable> siftablesDirt = NonNullList.create();
        if (ExNihiloConfig.overrideAllSiftDrops) {
            siftablesDirt.addAll(ExNihiloRegistryManager.SIEVE_REGISTRY.getDrops(OreDictUnifier.get("dirt")));
            ExNihiloRegistryManager.SIEVE_REGISTRY.getRegistry().clear();
        }
        processDrops(FileUtility.loadJson(new File(Loader.instance().getConfigDir(), "/gregtech/sieve_drops.json")));
        siftablesDirt.add(new Siftable(new ItemInfo(ExNihiloModule.GTPebbles, 3), 0.1f,
                BlockSieve.MeshType.STRING.getID()));
        siftablesDirt.add(new Siftable(new ItemInfo(ExNihiloModule.GTPebbles), 0.5f,
                BlockSieve.MeshType.STRING.getID()));
        siftablesDirt.add(new Siftable(new ItemInfo(ExNihiloModule.GTPebbles), 0.1f,
                BlockSieve.MeshType.STRING.getID()));
        siftablesDirt.add(new Siftable(new ItemInfo(ExNihiloModule.GTPebbles, 1), 0.5f,
                BlockSieve.MeshType.STRING.getID()));
        siftablesDirt.add(new Siftable(new ItemInfo(ExNihiloModule.GTPebbles, 1), 0.1f,
                BlockSieve.MeshType.STRING.getID()));
        siftablesDirt.add(new Siftable(new ItemInfo(ExNihiloModule.GTPebbles, 2), 0.5f,
                BlockSieve.MeshType.STRING.getID()));
        siftablesDirt.add(new Siftable(new ItemInfo(ExNihiloModule.GTPebbles, 2), 0.1f,
                BlockSieve.MeshType.STRING.getID()));
        siftablesDirt.add(new Siftable(new ItemInfo(ExNihiloModule.GTPebbles, 3), 0.5f,
                BlockSieve.MeshType.STRING.getID()));
        siftablesDirt.add(new Siftable(new ItemInfo(MetaBlocks.RUBBER_SAPLING.getBlockState().getBlock()), 0.1f,
                BlockSieve.MeshType.STRING.getID()));
        ExNihiloRegistryManager.SIEVE_REGISTRY.getRegistry().put(Ingredient.fromStacks(OreDictUnifier.get("dirt")),
                siftablesDirt);
    }
}
