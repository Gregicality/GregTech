package gregtech.common;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.items.armoritem.IGTArmor;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.metatileentity.registry.MTERegistry;
import gregtech.api.recipes.GTRecipeInputCache;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.ingredients.GTRecipeOreInput;
import gregtech.api.recipes.properties.impl.FusionEUToStartProperty;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.util.AssemblyLineManager;
import gregtech.api.util.GTLog;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.BlockLamp;
import gregtech.common.blocks.BlockOre;
import gregtech.common.blocks.BlockSurfaceRock;
import gregtech.common.blocks.LampItemBlock;
import gregtech.common.blocks.MaterialItemBlock;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.OreItemBlock;
import gregtech.common.blocks.StoneVariantBlock;
import gregtech.common.items.ArmorItems;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.cable.ItemBlockCable;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.fluidpipe.ItemBlockFluidPipe;
import gregtech.common.pipelike.itempipe.BlockItemPipe;
import gregtech.common.pipelike.itempipe.ItemBlockItemPipe;
import gregtech.common.pipelike.laser.BlockLaserPipe;
import gregtech.common.pipelike.laser.ItemBlockLaserPipe;
import gregtech.common.pipelike.optical.BlockOpticalPipe;
import gregtech.common.pipelike.optical.ItemBlockOpticalPipe;
import gregtech.datafix.GTDataFixers;
import gregtech.integration.groovy.GroovyScriptModule;
import gregtech.loaders.MaterialInfoLoader;
import gregtech.loaders.OreDictionaryLoader;
import gregtech.loaders.recipe.CraftingComponent;
import gregtech.loaders.recipe.GTRecipeManager;
import gregtech.modules.GregTechModules;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

import static gregtech.common.blocks.MetaBlocks.*;

@Mod.EventBusSubscriber(modid = GTValues.MODID)
public class CommonProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        GTLog.logger.info("Registering Blocks...");
        IForgeRegistry<Block> registry = event.getRegistry();

        for (MTERegistry r : GregTechAPI.mteManager.getRegistries()) {
            registry.register(r.getBlock());
        }

        StoneType.init();

        for (MaterialRegistry materialRegistry : GregTechAPI.materialManager.getRegistries()) {
            for (Material material : materialRegistry) {
                if (material.hasProperty(PropertyKey.ORE) && !material.hasFlag(MaterialFlags.DISABLE_ORE_BLOCK)) {
                    createOreBlock(material);
                }

                if (material.hasProperty(PropertyKey.WIRE)) {
                    for (BlockCable cable : CABLES.get(materialRegistry.getModid())) {
                        if (!cable.getItemPipeType(null).isCable() ||
                                !material.getProperty(PropertyKey.WIRE).isSuperconductor())
                            cable.addCableMaterial(material, material.getProperty(PropertyKey.WIRE));
                    }
                }
                if (material.hasProperty(PropertyKey.FLUID_PIPE)) {
                    for (BlockFluidPipe pipe : FLUID_PIPES.get(materialRegistry.getModid())) {
                        if (!pipe.getItemPipeType(pipe.getItem(material)).getOrePrefix().isIgnored(material)) {
                            pipe.addPipeMaterial(material, material.getProperty(PropertyKey.FLUID_PIPE));
                        }
                    }
                }
                if (material.hasProperty(PropertyKey.ITEM_PIPE)) {
                    for (BlockItemPipe pipe : ITEM_PIPES.get(materialRegistry.getModid())) {
                        if (!pipe.getItemPipeType(pipe.getItem(material)).getOrePrefix().isIgnored(material)) {
                            pipe.addPipeMaterial(material, material.getProperty(PropertyKey.ITEM_PIPE));
                        }
                    }
                }
            }

            for (BlockCable cable : CABLES.get(materialRegistry.getModid())) registry.register(cable);
            for (BlockFluidPipe pipe : FLUID_PIPES.get(materialRegistry.getModid())) registry.register(pipe);
            for (BlockItemPipe pipe : ITEM_PIPES.get(materialRegistry.getModid())) registry.register(pipe);
        }
        for (BlockOpticalPipe pipe : OPTICAL_PIPES) registry.register(pipe);
        for (BlockLaserPipe pipe : LASER_PIPES) registry.register(pipe);

        registry.register(LD_ITEM_PIPE);
        registry.register(LD_FLUID_PIPE);
        registry.register(HERMETIC_CASING);
        registry.register(CLEANROOM_CASING);
        registry.register(COMPUTER_CASING);
        registry.register(BATTERY_BLOCK);
        registry.register(FOAM);
        registry.register(REINFORCED_FOAM);
        registry.register(PETRIFIED_FOAM);
        registry.register(REINFORCED_PETRIFIED_FOAM);
        registry.register(BOILER_CASING);
        registry.register(BOILER_FIREBOX_CASING);
        registry.register(METAL_CASING);
        registry.register(TURBINE_CASING);
        registry.register(MACHINE_CASING);
        registry.register(STEAM_CASING);
        registry.register(MULTIBLOCK_CASING);
        registry.register(TRANSPARENT_CASING);
        registry.register(WIRE_COIL);
        registry.register(FUSION_CASING);
        registry.register(WARNING_SIGN);
        registry.register(WARNING_SIGN_1);
        registry.register(ASPHALT);
        for (StoneVariantBlock block : STONE_BLOCKS.values()) registry.register(block);
        registry.register(RUBBER_LOG);
        registry.register(RUBBER_LEAVES);
        registry.register(RUBBER_SAPLING);
        registry.register(PLANKS);
        registry.register(WOOD_SLAB);
        registry.register(DOUBLE_WOOD_SLAB);
        registry.register(RUBBER_WOOD_STAIRS);
        registry.register(TREATED_WOOD_STAIRS);
        registry.register(RUBBER_WOOD_FENCE);
        registry.register(TREATED_WOOD_FENCE);
        registry.register(RUBBER_WOOD_FENCE_GATE);
        registry.register(TREATED_WOOD_FENCE_GATE);
        registry.register(RUBBER_WOOD_DOOR);
        registry.register(TREATED_WOOD_DOOR);
        registry.register(BRITTLE_CHARCOAL);
        registry.register(POWDERBARREL);
        registry.register(ITNT);
        registry.register(METAL_SHEET);
        registry.register(LARGE_METAL_SHEET);
        registry.register(STUDS);

        for (BlockLamp block : LAMPS.values()) registry.register(block);
        for (BlockLamp block : BORDERLESS_LAMPS.values()) registry.register(block);

        for (BlockCompressed block : COMPRESSED_BLOCKS) registry.register(block);
        for (BlockFrame block : FRAME_BLOCKS) registry.register(block);
        for (BlockSurfaceRock block : SURFACE_ROCK_BLOCKS) registry.register(block);
        for (BlockOre block : ORES) registry.register(block);
    }

    private static void createOreBlock(Material material) {
        StoneType[] stoneTypeBuffer = new StoneType[16];
        int generationIndex = 0;
        for (StoneType stoneType : StoneType.STONE_TYPE_REGISTRY) {
            int id = StoneType.STONE_TYPE_REGISTRY.getIDForObject(stoneType), index = id / 16;
            if (index > generationIndex) {
                createOreBlock(material, copyNotNull(stoneTypeBuffer), generationIndex);
                Arrays.fill(stoneTypeBuffer, null);
            }
            stoneTypeBuffer[id % 16] = stoneType;
            generationIndex = index;
        }
        createOreBlock(material, copyNotNull(stoneTypeBuffer), generationIndex);
    }

    private static <T> T[] copyNotNull(T[] src) {
        int nullIndex = ArrayUtils.indexOf(src, null);
        return Arrays.copyOfRange(src, 0, nullIndex == -1 ? src.length : nullIndex);
    }

    private static void createOreBlock(Material material, StoneType[] stoneTypes, int index) {
        BlockOre block = new BlockOre(material, stoneTypes);
        block.setRegistryName("ore_" + material + "_" + index);
        for (StoneType stoneType : stoneTypes) {
            GregTechAPI.oreBlockTable.computeIfAbsent(material, m -> new HashMap<>()).put(stoneType, block);
        }
        ORES.add(block);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerBlocksLast(RegistryEvent.Register<Block> event) {
        // last chance for mods to register their potion types is here
        FLUID_BLOCKS.forEach(event.getRegistry()::register);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        GTLog.logger.info("Registering Items...");
        IForgeRegistry<Item> registry = event.getRegistry();

        for (MetaItem<?> item : MetaItems.ITEMS) {
            registry.register(item);
            item.registerSubItems();
        }

        for (IGTTool tool : ToolItems.getAllTools()) {
            registry.register(tool.get());
        }
        for (IGTArmor armor : ArmorItems.getAllArmors()) {
            registry.register(armor.get());
        }

        GTRecipeManager.preLoad();

        for (MTERegistry r : GregTechAPI.mteManager.getRegistries()) {
            registry.register(createItemBlock(r.getBlock(), MachineItemBlock::new));
        }

        for (MaterialRegistry materialRegistry : GregTechAPI.materialManager.getRegistries()) {
            for (BlockCable cable : CABLES.get(materialRegistry.getModid()))
                registry.register(createItemBlock(cable, ItemBlockCable::new));
            for (BlockFluidPipe pipe : FLUID_PIPES.get(materialRegistry.getModid()))
                registry.register(createItemBlock(pipe, ItemBlockFluidPipe::new));
            for (BlockItemPipe pipe : ITEM_PIPES.get(materialRegistry.getModid()))
                registry.register(createItemBlock(pipe, ItemBlockItemPipe::new));
        }
        for (BlockOpticalPipe pipe : OPTICAL_PIPES) registry.register(createItemBlock(pipe, ItemBlockOpticalPipe::new));
        for (BlockLaserPipe pipe : LASER_PIPES) registry.register(createItemBlock(pipe, ItemBlockLaserPipe::new));

        registry.register(createItemBlock(LD_ITEM_PIPE, ItemBlock::new));
        registry.register(createItemBlock(LD_FLUID_PIPE, ItemBlock::new));
        registry.register(createItemBlock(HERMETIC_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(CLEANROOM_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(COMPUTER_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(BATTERY_BLOCK, VariantItemBlock::new));
        registry.register(createItemBlock(BOILER_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(BOILER_FIREBOX_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(METAL_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(TURBINE_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(MACHINE_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(STEAM_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(MULTIBLOCK_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(TRANSPARENT_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(WIRE_COIL, VariantItemBlock::new));
        registry.register(createItemBlock(FUSION_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(WARNING_SIGN, VariantItemBlock::new));
        registry.register(createItemBlock(WARNING_SIGN_1, VariantItemBlock::new));
        registry.register(createItemBlock(METAL_SHEET, VariantItemBlock::new));
        registry.register(createItemBlock(LARGE_METAL_SHEET, VariantItemBlock::new));
        registry.register(createItemBlock(STUDS, VariantItemBlock::new));
        for (BlockLamp block : LAMPS.values()) {
            registry.register(createItemBlock(block, LampItemBlock::new));
        }
        for (BlockLamp block : BORDERLESS_LAMPS.values()) {
            registry.register(createItemBlock(block, LampItemBlock::new));
        }
        registry.register(createItemBlock(ASPHALT, VariantItemBlock::new));
        for (StoneVariantBlock block : STONE_BLOCKS.values()) {
            registry.register(createItemBlock(block, VariantItemBlock::new));
        }
        registry.register(createItemBlock(PLANKS, VariantItemBlock::new));
        registry.register(createItemBlock(WOOD_SLAB, b -> new ItemSlab(b, b, DOUBLE_WOOD_SLAB)));
        registry.register(createItemBlock(RUBBER_WOOD_STAIRS, ItemBlock::new));
        registry.register(createItemBlock(TREATED_WOOD_STAIRS, ItemBlock::new));
        registry.register(createItemBlock(RUBBER_WOOD_FENCE, ItemBlock::new));
        registry.register(createItemBlock(TREATED_WOOD_FENCE, ItemBlock::new));
        registry.register(createItemBlock(RUBBER_WOOD_FENCE_GATE, ItemBlock::new));
        registry.register(createItemBlock(TREATED_WOOD_FENCE_GATE, ItemBlock::new));
        registry.register(createItemBlock(BRITTLE_CHARCOAL, ItemBlock::new));
        registry.register(createItemBlock(RUBBER_LOG, ItemBlock::new));
        registry.register(createItemBlock(RUBBER_LEAVES, ItemBlock::new));
        registry.register(createItemBlock(RUBBER_SAPLING, ItemBlock::new));
        registry.register(createItemBlock(POWDERBARREL, ItemBlock::new));
        registry.register(createItemBlock(ITNT, ItemBlock::new));

        for (BlockCompressed block : COMPRESSED_BLOCKS) {
            registry.register(createItemBlock(block, b -> new MaterialItemBlock(b, OrePrefix.block)));
        }
        for (BlockFrame block : FRAME_BLOCKS) {
            registry.register(createItemBlock(block, b -> new MaterialItemBlock(b, OrePrefix.frameGt)));
        }
        for (BlockOre block : ORES) {
            registry.register(createItemBlock(block, OreItemBlock::new));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void initComponents(RegistryEvent.Register<IRecipe> event) {
        GTRecipeInputCache.enableCache();
        CraftingComponent.initializeComponents();
        MinecraftForge.EVENT_BUS.post(new GregTechAPI.RegisterEvent<>(null, CraftingComponent.class));
    }

    // this is called with normal priority, so most mods working with
    // ore dictionary and recipes will get recipes accessible in time
    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        // Registers Fusion tiers for the FusionEUToStartProperty
        FusionEUToStartProperty.registerFusionTier(GTValues.LuV, "(MK1)");
        FusionEUToStartProperty.registerFusionTier(GTValues.ZPM, "(MK2)");
        FusionEUToStartProperty.registerFusionTier(GTValues.UV, "(MK3)");

        // Register data stick copying custom scanner logic
        AssemblyLineManager.registerScannerLogic();

        GTLog.logger.info("Registering ore dictionary...");

        MetaItems.registerOreDict();
        ToolItems.registerOreDict();
        MetaBlocks.registerOreDict();
        OreDictionaryLoader.init();
        MaterialInfoLoader.init();
        ArmorItems.registerLegacyConversionRecipes();

        // post an event for addons to modify unification data before base GT registers recycling recipes
        MinecraftForge.EVENT_BUS.post(new GregTechAPI.RegisterEvent<>(null, ItemMaterialInfo.class));

        GTLog.logger.info("Registering recipes...");

        GTRecipeManager.load();
    }

    // this is called almost last, to make sure all mods registered their ore dictionary
    // items and blocks for running first phase of material handlers
    // it will also clear generated materials
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void runEarlyMaterialHandlers(RegistryEvent.Register<IRecipe> event) {
        GTLog.logger.info("Running early material handlers...");
        OrePrefix.runMaterialHandlers();
    }

    // this is called last, so all mods finished registering their stuff, as example, CraftTweaker
    // if it registered some kind of ore dictionary entry, late processing will hook it and generate recipes
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerRecipesLowest(RegistryEvent.Register<IRecipe> event) {
        GTLog.logger.info("Running late material handlers...");
        OrePrefix.runMaterialHandlers();
        GTRecipeManager.loadLatest();

        // On initial load we need to postpone cache flushing until FMLPostInitializationEvent
        // to account for post-init recipe registration
        if (Loader.instance().hasReachedState(LoaderState.AVAILABLE))
            GTRecipeInputCache.disableCache();
    }

    @SubscribeEvent
    public static void syncConfigValues(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(GTValues.MODID)) {
            ConfigManager.sync(GTValues.MODID, Type.INSTANCE);
        }
    }

    @SubscribeEvent
    public static void modifyFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();
        Block block = Block.getBlockFromItem(stack.getItem());
        // handle sapling and log burn rates
        if (block == RUBBER_SAPLING) {
            event.setBurnTime(100);
        } else if (block == WOOD_SLAB) {
            event.setBurnTime(150);
        } else if (block instanceof BlockCompressed) {
            // handle material blocks burn value
            Material material = ((BlockCompressed) block).getGtMaterial(stack);
            DustProperty property = material.getProperty(PropertyKey.DUST);
            if (property != null && property.getBurnTime() > 0) {
                // compute burn value for block prefix, taking amount of material in block into account
                double materialUnitsInBlock = OrePrefix.block.getMaterialAmount(material) / (GTValues.M * 1.0);
                event.setBurnTime((int) (materialUnitsInBlock * property.getBurnTime()));
            }
        }
    }

    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        ResourceLocation registryName = block.getRegistryName();
        if (registryName == null) {
            throw new IllegalArgumentException("Block " + block.getTranslationKey() + " has no registry name.");
        }
        itemBlock.setRegistryName(registryName);
        return itemBlock;
    }

    public void onPreLoad() {}

    public void onLoad() {
        GTDataFixers.init();
    }

    public void onPostLoad() {
        if (ConfigHolder.compat.removeSmeltingForEBFMetals) {
            ModHandler.removeSmeltingEBFMetals();
        }
    }

    public void onLoadComplete() {
        GTRecipeInputCache.disableCache();

        // If JEI and GS is not loaded, refresh ore dict ingredients
        // Not needed if JEI is loaded, as done in the JEI plugin (and this runs after that)
        // Not needed if GS is loaded, as done after script loads (and this runs after that)
        if (!GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_JEI) &&
                !GroovyScriptModule.isCurrentlyRunning())
            GTRecipeOreInput.refreshStackCache();
    }

    public boolean isFancyGraphics() {
        return true;
    }
}
