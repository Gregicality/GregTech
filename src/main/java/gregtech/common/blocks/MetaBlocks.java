package gregtech.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.registry.MTERegistry;
import gregtech.api.pipenet.longdist.BlockLongDistancePipe;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.util.BlockUtility;
import gregtech.api.util.GTUtility;
import gregtech.api.util.function.TriConsumer;
import gregtech.client.model.SimpleStateMapper;
import gregtech.client.model.modelfactories.BakedModelHandler;
import gregtech.client.renderer.handler.MetaTileEntityRenderer;
import gregtech.client.renderer.handler.MetaTileEntityTESR;
import gregtech.client.renderer.pipe.CableRenderer;
import gregtech.client.renderer.pipe.FluidPipeRenderer;
import gregtech.client.renderer.pipe.ItemPipeRenderer;
import gregtech.client.renderer.pipe.LaserPipeRenderer;
import gregtech.client.renderer.pipe.OpticalPipeRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.explosive.BlockITNT;
import gregtech.common.blocks.explosive.BlockPowderbarrel;
import gregtech.common.blocks.foam.BlockFoam;
import gregtech.common.blocks.foam.BlockPetrifiedFoam;
import gregtech.common.blocks.wood.BlockGregFence;
import gregtech.common.blocks.wood.BlockGregFenceGate;
import gregtech.common.blocks.wood.BlockGregPlanks;
import gregtech.common.blocks.wood.BlockGregWoodSlab;
import gregtech.common.blocks.wood.BlockRubberDoor;
import gregtech.common.blocks.wood.BlockRubberLeaves;
import gregtech.common.blocks.wood.BlockRubberLog;
import gregtech.common.blocks.wood.BlockRubberSapling;
import gregtech.common.blocks.wood.BlockWoodenDoor;
import gregtech.common.items.MetaItems;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import gregtech.common.pipelike.cable.tile.TileEntityCableTickable;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.longdistance.LDFluidPipeType;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
import gregtech.common.pipelike.itempipe.BlockItemPipe;
import gregtech.common.pipelike.itempipe.ItemPipeType;
import gregtech.common.pipelike.itempipe.longdistance.LDItemPipeType;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipeTickable;
import gregtech.common.pipelike.laser.BlockLaserPipe;
import gregtech.common.pipelike.laser.LaserPipeType;
import gregtech.common.pipelike.laser.tile.TileEntityLaserPipe;
import gregtech.common.pipelike.optical.BlockOpticalPipe;
import gregtech.common.pipelike.optical.OpticalPipeType;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static gregtech.api.unification.material.info.MaterialFlags.FORCE_GENERATE_BLOCK;
import static gregtech.api.unification.material.info.MaterialFlags.GENERATE_FRAME;
import static gregtech.api.util.GTUtility.gregtechId;

public class MetaBlocks {

    private MetaBlocks() {}

    public static final Map<String, BlockCable[]> CABLES = new Object2ObjectOpenHashMap<>();
    public static final Map<String, BlockFluidPipe[]> FLUID_PIPES = new Object2ObjectOpenHashMap<>();
    public static final Map<String, BlockItemPipe[]> ITEM_PIPES = new Object2ObjectOpenHashMap<>();
    public static final BlockOpticalPipe[] OPTICAL_PIPES = new BlockOpticalPipe[OpticalPipeType.values().length];
    public static final BlockLaserPipe[] LASER_PIPES = new BlockLaserPipe[LaserPipeType.values().length];
    public static BlockLongDistancePipe LD_ITEM_PIPE;
    public static BlockLongDistancePipe LD_FLUID_PIPE;

    public static BlockBoilerCasing BOILER_CASING;
    public static BlockFireboxCasing BOILER_FIREBOX_CASING;
    public static BlockMetalCasing METAL_CASING;
    public static BlockTurbineCasing TURBINE_CASING;
    public static BlockMachineCasing MACHINE_CASING;
    public static BlockSteamCasing STEAM_CASING;
    public static BlockMultiblockCasing MULTIBLOCK_CASING;
    public static BlockGlassCasing TRANSPARENT_CASING;
    public static BlockWireCoil WIRE_COIL;
    public static BlockFusionCasing FUSION_CASING;
    public static BlockWarningSign WARNING_SIGN;
    public static BlockWarningSign1 WARNING_SIGN_1;
    public static BlockHermeticCasing HERMETIC_CASING;
    public static BlockCleanroomCasing CLEANROOM_CASING;
    public static BlockComputerCasing COMPUTER_CASING;
    public static BlockBatteryPart BATTERY_BLOCK;

    public static final EnumMap<EnumDyeColor, BlockLamp> LAMPS = new EnumMap<>(EnumDyeColor.class);
    public static final EnumMap<EnumDyeColor, BlockLamp> BORDERLESS_LAMPS = new EnumMap<>(EnumDyeColor.class);

    public static BlockAsphalt ASPHALT;

    public static final EnumMap<StoneVariantBlock.StoneVariant, StoneVariantBlock> STONE_BLOCKS = new EnumMap<>(
            StoneVariantBlock.StoneVariant.class);

    public static BlockFoam FOAM;
    public static BlockFoam REINFORCED_FOAM;
    public static BlockPetrifiedFoam PETRIFIED_FOAM;
    public static BlockPetrifiedFoam REINFORCED_PETRIFIED_FOAM;

    public static BlockRubberLog RUBBER_LOG;
    public static BlockRubberLeaves RUBBER_LEAVES;
    public static BlockRubberSapling RUBBER_SAPLING;
    public static BlockGregPlanks PLANKS;
    public static BlockGregWoodSlab WOOD_SLAB;
    public static BlockGregWoodSlab DOUBLE_WOOD_SLAB;
    public static BlockStairs RUBBER_WOOD_STAIRS;
    public static BlockStairs TREATED_WOOD_STAIRS;
    public static BlockFence RUBBER_WOOD_FENCE;
    public static BlockFence TREATED_WOOD_FENCE;
    public static BlockFenceGate RUBBER_WOOD_FENCE_GATE;
    public static BlockFenceGate TREATED_WOOD_FENCE_GATE;
    public static BlockWoodenDoor RUBBER_WOOD_DOOR;
    public static BlockWoodenDoor TREATED_WOOD_DOOR;
    public static BlockPowderbarrel POWDERBARREL;
    public static BlockITNT ITNT;

    public static BlockBrittleCharcoal BRITTLE_CHARCOAL;

    public static BlockColored METAL_SHEET;
    public static BlockColored LARGE_METAL_SHEET;
    public static BlockColored STUDS;

    public static final Map<Material, BlockCompressed> COMPRESSED = new Object2ObjectOpenHashMap<>();
    public static final Map<Material, BlockFrame> FRAMES = new Object2ObjectOpenHashMap<>();
    public static final Map<Material, BlockSurfaceRock> SURFACE_ROCK = new Object2ObjectOpenHashMap<>();

    public static final List<BlockCompressed> COMPRESSED_BLOCKS = new ArrayList<>();
    public static final List<BlockFrame> FRAME_BLOCKS = new ArrayList<>();
    public static final List<BlockSurfaceRock> SURFACE_ROCK_BLOCKS = new ArrayList<>();

    public static final List<BlockOre> ORES = new ArrayList<>();
    public static final List<BlockFluidBase> FLUID_BLOCKS = new ArrayList<>();

    public static void init() {
        for (MTERegistry registry : GregTechAPI.mteManager.getRegistries()) {
            BlockMachine machine = new BlockMachine();
            machine.setRegistryName(registry.getModid(), "mte");
            registry.setBlock(machine);
        }

        for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
            String modid = registry.getModid();
            BlockCable[] cables = new BlockCable[Insulation.VALUES.length];
            for (Insulation ins : Insulation.VALUES) {
                cables[ins.ordinal()] = new BlockCable(ins, registry);
                cables[ins.ordinal()].setRegistryName(modid, ins.getName());
            }
            CABLES.put(modid, cables);

            BlockFluidPipe[] fluidPipes = new BlockFluidPipe[FluidPipeType.VALUES.length];
            for (FluidPipeType type : FluidPipeType.VALUES) {
                fluidPipes[type.ordinal()] = new BlockFluidPipe(type, registry);
                fluidPipes[type.ordinal()].setRegistryName(modid, String.format("fluid_pipe_%s", type.name));
            }
            FLUID_PIPES.put(modid, fluidPipes);

            BlockItemPipe[] itemPipes = new BlockItemPipe[ItemPipeType.VALUES.length];
            for (ItemPipeType type : ItemPipeType.VALUES) {
                itemPipes[type.ordinal()] = new BlockItemPipe(type, registry);
                itemPipes[type.ordinal()].setRegistryName(modid, String.format("item_pipe_%s", type.name));
            }
            ITEM_PIPES.put(modid, itemPipes);
        }
        for (OpticalPipeType type : OpticalPipeType.values()) {
            OPTICAL_PIPES[type.ordinal()] = new BlockOpticalPipe(type);
            OPTICAL_PIPES[type.ordinal()].setRegistryName(String.format("optical_pipe_%s", type.getName()));
            OPTICAL_PIPES[type.ordinal()].setTranslationKey(String.format("optical_pipe_%s", type.getName()));
        }
        for (LaserPipeType type : LaserPipeType.values()) {
            LASER_PIPES[type.ordinal()] = new BlockLaserPipe(type);
            LASER_PIPES[type.ordinal()].setRegistryName(String.format("laser_pipe_%s", type.getName()));
            LASER_PIPES[type.ordinal()].setTranslationKey(String.format("laser_pipe_%s", type.getName()));
        }

        LD_ITEM_PIPE = new BlockLongDistancePipe(LDItemPipeType.INSTANCE);
        LD_ITEM_PIPE.setRegistryName("ld_item_pipe");
        LD_FLUID_PIPE = new BlockLongDistancePipe(LDFluidPipeType.INSTANCE);
        LD_FLUID_PIPE.setRegistryName("ld_fluid_pipe");
        BOILER_CASING = new BlockBoilerCasing();
        BOILER_CASING.setRegistryName("boiler_casing");
        BOILER_FIREBOX_CASING = new BlockFireboxCasing();
        BOILER_FIREBOX_CASING.setRegistryName("boiler_firebox_casing");
        METAL_CASING = new BlockMetalCasing();
        METAL_CASING.setRegistryName("metal_casing");
        TURBINE_CASING = new BlockTurbineCasing();
        TURBINE_CASING.setRegistryName("turbine_casing");
        MACHINE_CASING = new BlockMachineCasing();
        MACHINE_CASING.setRegistryName("machine_casing");
        STEAM_CASING = new BlockSteamCasing();
        STEAM_CASING.setRegistryName("steam_casing");
        MULTIBLOCK_CASING = new BlockMultiblockCasing();
        MULTIBLOCK_CASING.setRegistryName("multiblock_casing");
        TRANSPARENT_CASING = new BlockGlassCasing();
        TRANSPARENT_CASING.setRegistryName("transparent_casing");
        WIRE_COIL = new BlockWireCoil();
        WIRE_COIL.setRegistryName("wire_coil");
        FUSION_CASING = new BlockFusionCasing();
        FUSION_CASING.setRegistryName("fusion_casing");
        WARNING_SIGN = new BlockWarningSign();
        WARNING_SIGN.setRegistryName("warning_sign");
        WARNING_SIGN_1 = new BlockWarningSign1();
        WARNING_SIGN_1.setRegistryName("warning_sign_1");
        HERMETIC_CASING = new BlockHermeticCasing();
        HERMETIC_CASING.setRegistryName("hermetic_casing");
        CLEANROOM_CASING = new BlockCleanroomCasing();
        CLEANROOM_CASING.setRegistryName("cleanroom_casing");
        COMPUTER_CASING = new BlockComputerCasing();
        COMPUTER_CASING.setRegistryName("computer_casing");
        BATTERY_BLOCK = new BlockBatteryPart();
        BATTERY_BLOCK.setRegistryName("battery_block");

        for (EnumDyeColor color : EnumDyeColor.values()) {
            BlockLamp block = new BlockLamp(color);
            block.setRegistryName(color.getName() + "_lamp");
            block.setTranslationKey("gregtech_lamp." + color.getName());
            LAMPS.put(color, block);
            block = new BlockLampBorderless(color);
            block.setRegistryName("borderless_" + color.getName() + "_lamp");
            block.setTranslationKey("gregtech_lamp_borderless." + color.getName());
            BORDERLESS_LAMPS.put(color, block);
        }

        ASPHALT = new BlockAsphalt();
        ASPHALT.setRegistryName("asphalt");

        for (StoneVariantBlock.StoneVariant shape : StoneVariantBlock.StoneVariant.values()) {
            STONE_BLOCKS.put(shape, new StoneVariantBlock(shape));
        }

        FOAM = new BlockFoam(false);
        FOAM.setRegistryName("foam");
        REINFORCED_FOAM = new BlockFoam(true);
        REINFORCED_FOAM.setRegistryName("reinforced_foam");
        PETRIFIED_FOAM = new BlockPetrifiedFoam(false);
        PETRIFIED_FOAM.setRegistryName("petrified_foam");
        REINFORCED_PETRIFIED_FOAM = new BlockPetrifiedFoam(true);
        REINFORCED_PETRIFIED_FOAM.setRegistryName("reinforced_petrified_foam");

        RUBBER_LOG = new BlockRubberLog();
        RUBBER_LOG.setRegistryName("rubber_log");
        RUBBER_LEAVES = new BlockRubberLeaves();
        RUBBER_LEAVES.setRegistryName("rubber_leaves");
        RUBBER_SAPLING = new BlockRubberSapling();
        RUBBER_SAPLING.setRegistryName("rubber_sapling");
        PLANKS = new BlockGregPlanks();
        PLANKS.setRegistryName("planks");
        WOOD_SLAB = new BlockGregWoodSlab.Half();
        WOOD_SLAB.setRegistryName("wood_slab");
        DOUBLE_WOOD_SLAB = new BlockGregWoodSlab.Double();
        DOUBLE_WOOD_SLAB.setRegistryName("double_wood_slab");
        RUBBER_WOOD_STAIRS = new BlockGregStairs(PLANKS.getState(BlockGregPlanks.BlockType.RUBBER_PLANK));
        RUBBER_WOOD_STAIRS.setRegistryName("rubber_wood_stairs").setTranslationKey("rubber_wood_stairs");
        TREATED_WOOD_STAIRS = new BlockGregStairs(PLANKS.getState(BlockGregPlanks.BlockType.TREATED_PLANK));
        TREATED_WOOD_STAIRS.setRegistryName("treated_wood_stairs").setTranslationKey("treated_wood_stairs");
        RUBBER_WOOD_FENCE = new BlockGregFence();
        RUBBER_WOOD_FENCE.setRegistryName("rubber_wood_fence").setTranslationKey("rubber_wood_fence");
        TREATED_WOOD_FENCE = new BlockGregFence();
        TREATED_WOOD_FENCE.setRegistryName("treated_wood_fence").setTranslationKey("treated_wood_fence");
        RUBBER_WOOD_FENCE_GATE = new BlockGregFenceGate();
        RUBBER_WOOD_FENCE_GATE.setRegistryName("rubber_wood_fence_gate").setTranslationKey("rubber_wood_fence_gate");
        TREATED_WOOD_FENCE_GATE = new BlockGregFenceGate();
        TREATED_WOOD_FENCE_GATE.setRegistryName("treated_wood_fence_gate").setTranslationKey("treated_wood_fence_gate");
        RUBBER_WOOD_DOOR = new BlockRubberDoor(() -> MetaItems.RUBBER_WOOD_DOOR.getStackForm());
        RUBBER_WOOD_DOOR.setRegistryName("rubber_wood_door").setTranslationKey("rubber_wood_door");
        TREATED_WOOD_DOOR = new BlockWoodenDoor(() -> MetaItems.TREATED_WOOD_DOOR.getStackForm());
        TREATED_WOOD_DOOR.setRegistryName("treated_wood_door").setTranslationKey("treated_wood_door");
        POWDERBARREL = new BlockPowderbarrel();
        POWDERBARREL.setRegistryName("powderbarrel").setTranslationKey("powderbarrel");
        ITNT = new BlockITNT();
        ITNT.setRegistryName("itnt").setTranslationKey("itnt");

        BRITTLE_CHARCOAL = new BlockBrittleCharcoal();
        BRITTLE_CHARCOAL.setRegistryName("brittle_charcoal");

        METAL_SHEET = new BlockColored(net.minecraft.block.material.Material.IRON, "metal_sheet", 2.0f, 5.0f,
                SoundType.METAL, EnumDyeColor.WHITE);
        METAL_SHEET.setRegistryName("metal_sheet");
        LARGE_METAL_SHEET = new BlockColored(net.minecraft.block.material.Material.IRON, "large_metal_sheet", 2.0f,
                5.0f, SoundType.METAL, EnumDyeColor.WHITE);
        LARGE_METAL_SHEET.setRegistryName("large_metal_sheet");
        STUDS = new BlockColored(net.minecraft.block.material.Material.CARPET, "studs", 1.5f, 2.5f, SoundType.CLOTH,
                EnumDyeColor.BLACK);
        STUDS.setRegistryName("studs");

        createGeneratedBlock(m -> m.hasProperty(PropertyKey.DUST) && m.hasFlag(GENERATE_FRAME),
                MetaBlocks::createFrameBlock);
        createGeneratedBlock(m -> m.hasProperty(PropertyKey.ORE) && m.hasProperty(PropertyKey.DUST),
                MetaBlocks::createSurfaceRockBlock);

        createGeneratedBlock(
                material -> (material.hasProperty(PropertyKey.INGOT) || material.hasProperty(PropertyKey.GEM) ||
                        material.hasFlag(FORCE_GENERATE_BLOCK)) && !OrePrefix.block.isIgnored(material),
                MetaBlocks::createCompressedBlock);

        registerTileEntity();

        // not sure if that's a good place for that, but i don't want to make a dedicated method for that
        // could possibly override block methods, but since these props don't depend on state why not just use nice and
        // simple vanilla method
        Blocks.FIRE.setFireInfo(RUBBER_LOG, 5, 5);
        Blocks.FIRE.setFireInfo(RUBBER_LEAVES, 30, 60);
        Blocks.FIRE.setFireInfo(PLANKS, 5, 20);
        Blocks.FIRE.setFireInfo(WOOD_SLAB, 5, 20);
        Blocks.FIRE.setFireInfo(DOUBLE_WOOD_SLAB, 5, 20);
        Blocks.FIRE.setFireInfo(RUBBER_WOOD_STAIRS, 5, 20);
        Blocks.FIRE.setFireInfo(TREATED_WOOD_STAIRS, 5, 20);
        Blocks.FIRE.setFireInfo(RUBBER_WOOD_FENCE, 5, 20);
        Blocks.FIRE.setFireInfo(TREATED_WOOD_FENCE, 5, 20);
        Blocks.FIRE.setFireInfo(RUBBER_WOOD_FENCE_GATE, 5, 20);
        Blocks.FIRE.setFireInfo(TREATED_WOOD_FENCE_GATE, 5, 20);
        Blocks.FIRE.setFireInfo(RUBBER_WOOD_DOOR, 5, 20);
        Blocks.FIRE.setFireInfo(TREATED_WOOD_DOOR, 5, 20);
        Blocks.FIRE.setFireInfo(BRITTLE_CHARCOAL, 5, 5);
    }

    /**
     * Deterministically populates a category of MetaBlocks based on the unique registry ID of each qualifying
     * Material.
     *
     * @param materialPredicate a filter for determining if a Material qualifies for generation in the category.
     * @param blockGenerator    a function which accepts a Materials set to pack into a MetaBlock, and the ordinal this
     *                          MetaBlock should have within its category.
     */
    protected static void createGeneratedBlock(Predicate<Material> materialPredicate,
                                               TriConsumer<String, Material[], Integer> blockGenerator) {
        for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
            Int2ObjectMap<Material[]> blocksToGenerate = new Int2ObjectAVLTreeMap<>();
            for (Material material : registry) {
                if (materialPredicate.test(material)) {
                    int id = material.getId();
                    int metaBlockID = id / 16;
                    int subBlockID = id % 16;

                    if (!blocksToGenerate.containsKey(metaBlockID)) {
                        Material[] materials = new Material[16];
                        Arrays.fill(materials, Materials.NULL);
                        blocksToGenerate.put(metaBlockID, materials);
                    }

                    blocksToGenerate.get(metaBlockID)[subBlockID] = material;
                }
            }
            blocksToGenerate.forEach((key, value) -> blockGenerator.accept(registry.getModid(), value, key));
        }
    }

    private static void createCompressedBlock(String modid, Material[] materials, int index) {
        BlockCompressed block = BlockCompressed.create(materials);
        block.setRegistryName(modid, "meta_block_compressed_" + index);
        for (Material m : materials) {
            COMPRESSED.put(m, block);
        }
        COMPRESSED_BLOCKS.add(block);
    }

    private static void createFrameBlock(String modid, Material[] materials, int index) {
        BlockFrame block = BlockFrame.create(materials);
        block.setRegistryName(modid, "meta_block_frame_" + index);
        for (Material m : materials) {
            FRAMES.put(m, block);
        }
        FRAME_BLOCKS.add(block);
    }

    private static void createSurfaceRockBlock(String modid, Material[] materials, int index) {
        BlockSurfaceRock block = BlockSurfaceRock.create(materials);
        block.setRegistryName(modid, "meta_block_surface_rock_" + index);
        for (Material m : materials) {
            SURFACE_ROCK.put(m, block);
        }
        SURFACE_ROCK_BLOCKS.add(block);
    }

    public static void registerTileEntity() {
        GameRegistry.registerTileEntity(MetaTileEntityHolder.class, gregtechId("machine"));
        GameRegistry.registerTileEntity(TileEntityCable.class, gregtechId("cable"));
        GameRegistry.registerTileEntity(TileEntityCableTickable.class, gregtechId("cable_tickable"));
        GameRegistry.registerTileEntity(TileEntityFluidPipe.class, gregtechId("fluid_pipe"));
        GameRegistry.registerTileEntity(TileEntityItemPipe.class, gregtechId("item_pipe"));
        GameRegistry.registerTileEntity(TileEntityOpticalPipe.class, gregtechId("optical_pipe"));
        GameRegistry.registerTileEntity(TileEntityLaserPipe.class, gregtechId("laser_pipe"));
        GameRegistry.registerTileEntity(TileEntityFluidPipeTickable.class, gregtechId("fluid_pipe_active"));
        GameRegistry.registerTileEntity(TileEntityItemPipeTickable.class, gregtechId("item_pipe_active"));
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        for (MTERegistry registry : GregTechAPI.mteManager.getRegistries()) {
            ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(registry.getBlock()),
                    stack -> MetaTileEntityRenderer.MODEL_LOCATION);
        }

        for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
            for (BlockCable cable : CABLES.get(registry.getModid())) cable.onModelRegister();
            for (BlockFluidPipe pipe : FLUID_PIPES.get(registry.getModid())) pipe.onModelRegister();
            for (BlockItemPipe pipe : ITEM_PIPES.get(registry.getModid())) pipe.onModelRegister();
        }
        for (BlockOpticalPipe pipe : OPTICAL_PIPES)
            ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(pipe),
                    stack -> OpticalPipeRenderer.INSTANCE.getModelLocation());
        for (BlockLaserPipe pipe : LASER_PIPES)
            ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(pipe),
                    stack -> LaserPipeRenderer.INSTANCE.getModelLocation());

        registerItemModel(BOILER_CASING);
        registerItemModel(METAL_CASING);
        registerItemModel(TURBINE_CASING);
        registerItemModel(MACHINE_CASING);
        registerItemModel(STEAM_CASING);
        registerItemModel(WARNING_SIGN);
        registerItemModel(WARNING_SIGN_1);
        registerItemModel(HERMETIC_CASING);
        registerItemModel(CLEANROOM_CASING);
        registerItemModel(COMPUTER_CASING);
        registerItemModel(BATTERY_BLOCK);
        registerItemModel(ASPHALT);
        for (StoneVariantBlock block : STONE_BLOCKS.values())
            registerItemModel(block);
        registerItemModelWithOverride(RUBBER_LOG, ImmutableMap.of(BlockLog.LOG_AXIS, EnumAxis.Y));
        registerItemModel(RUBBER_LEAVES);
        registerItemModel(RUBBER_SAPLING);
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RUBBER_SAPLING), 0,
                new ModelResourceLocation(Objects.requireNonNull(RUBBER_SAPLING.getRegistryName()), "inventory"));
        registerItemModel(PLANKS);
        registerItemModel(LD_ITEM_PIPE);
        registerItemModel(LD_FLUID_PIPE);
        registerItemModelWithOverride(WOOD_SLAB, ImmutableMap.of(BlockSlab.HALF, EnumBlockHalf.BOTTOM));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RUBBER_WOOD_STAIRS), 0,
                new ModelResourceLocation(Objects.requireNonNull(RUBBER_WOOD_STAIRS.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(TREATED_WOOD_STAIRS), 0,
                new ModelResourceLocation(Objects.requireNonNull(TREATED_WOOD_STAIRS.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RUBBER_WOOD_FENCE), 0,
                new ModelResourceLocation(Objects.requireNonNull(RUBBER_WOOD_FENCE.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(TREATED_WOOD_FENCE), 0,
                new ModelResourceLocation(Objects.requireNonNull(TREATED_WOOD_FENCE.getRegistryName()), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RUBBER_WOOD_FENCE_GATE), 0,
                new ModelResourceLocation(Objects.requireNonNull(RUBBER_WOOD_FENCE_GATE.getRegistryName()),
                        "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(TREATED_WOOD_FENCE_GATE), 0,
                new ModelResourceLocation(Objects.requireNonNull(TREATED_WOOD_FENCE_GATE.getRegistryName()),
                        "inventory"));
        registerItemModel(BRITTLE_CHARCOAL);
        registerItemModel(POWDERBARREL);
        registerItemModel(ITNT);

        registerItemModel(METAL_SHEET);
        registerItemModel(LARGE_METAL_SHEET);
        registerItemModel(STUDS);

        BOILER_FIREBOX_CASING.onModelRegister();
        WIRE_COIL.onModelRegister();
        FUSION_CASING.onModelRegister();
        MULTIBLOCK_CASING.onModelRegister();
        TRANSPARENT_CASING.onModelRegister();

        for (BlockLamp lamp : LAMPS.values()) lamp.onModelRegister();
        for (BlockLamp lamp : BORDERLESS_LAMPS.values()) lamp.onModelRegister();

        for (BlockCompressed block : COMPRESSED_BLOCKS) block.onModelRegister();
        for (BlockFrame block : FRAME_BLOCKS) block.onModelRegister();
        for (BlockOre block : ORES) block.onModelRegister();
    }

    @SideOnly(Side.CLIENT)
    private static void registerItemModel(Block block) {
        for (IBlockState state : block.getBlockState().getValidStates()) {
            // noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            statePropertiesToString(state.getProperties())));
        }
    }

    @SideOnly(Side.CLIENT)
    private static void registerItemModelWithOverride(Block block, Map<IProperty<?>, Comparable<?>> stateOverrides) {
        for (IBlockState state : block.getBlockState().getValidStates()) {
            Map<IProperty<?>, Comparable<?>> stringProperties = new Object2ObjectOpenHashMap<>(state.getProperties());
            stringProperties.putAll(stateOverrides);
            // noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            statePropertiesToString(stringProperties)));
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerStateMappers() {
        for (MTERegistry registry : GregTechAPI.mteManager.getRegistries()) {
            ModelLoader.setCustomStateMapper(registry.getBlock(),
                    new SimpleStateMapper(MetaTileEntityRenderer.MODEL_LOCATION));
        }

        IStateMapper normalStateMapper;
        for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
            normalStateMapper = new SimpleStateMapper(CableRenderer.INSTANCE.getModelLocation());
            for (BlockCable cable : CABLES.get(registry.getModid())) {
                ModelLoader.setCustomStateMapper(cable, normalStateMapper);
            }
            normalStateMapper = new SimpleStateMapper(FluidPipeRenderer.INSTANCE.getModelLocation());
            for (BlockFluidPipe pipe : FLUID_PIPES.get(registry.getModid())) {
                ModelLoader.setCustomStateMapper(pipe, normalStateMapper);
            }
            normalStateMapper = new SimpleStateMapper(ItemPipeRenderer.INSTANCE.getModelLocation());
            for (BlockItemPipe pipe : ITEM_PIPES.get(registry.getModid())) {
                ModelLoader.setCustomStateMapper(pipe, normalStateMapper);
            }
        }
        normalStateMapper = new SimpleStateMapper(OpticalPipeRenderer.INSTANCE.getModelLocation());
        for (BlockOpticalPipe pipe : OPTICAL_PIPES) {
            ModelLoader.setCustomStateMapper(pipe, normalStateMapper);
        }
        normalStateMapper = new SimpleStateMapper(LaserPipeRenderer.INSTANCE.getModelLocation());
        for (BlockLaserPipe pipe : LASER_PIPES) {
            ModelLoader.setCustomStateMapper(pipe, normalStateMapper);
        }

        normalStateMapper = new SimpleStateMapper(BlockSurfaceRock.MODEL_LOCATION);
        for (BlockSurfaceRock surfaceRock : SURFACE_ROCK_BLOCKS) {
            ModelLoader.setCustomStateMapper(surfaceRock, normalStateMapper);
        }

        normalStateMapper = new StateMapperBase() {

            @NotNull
            @Override
            protected ModelResourceLocation getModelResourceLocation(@NotNull IBlockState state) {
                return new ModelResourceLocation(Block.REGISTRY.getNameForObject(state.getBlock()), "normal");
            }
        };

        ModelLoader.setCustomStateMapper(FOAM, normalStateMapper);
        ModelLoader.setCustomStateMapper(REINFORCED_FOAM, normalStateMapper);
        ModelLoader.setCustomStateMapper(PETRIFIED_FOAM, normalStateMapper);
        ModelLoader.setCustomStateMapper(REINFORCED_PETRIFIED_FOAM, normalStateMapper);

        BakedModelHandler modelHandler = new BakedModelHandler();
        MinecraftForge.EVENT_BUS.register(modelHandler);
        FLUID_BLOCKS.forEach(modelHandler::addFluidBlock);

        ClientRegistry.bindTileEntitySpecialRenderer(MetaTileEntityHolder.class, new MetaTileEntityTESR());
    }

    @SideOnly(Side.CLIENT)
    public static void registerColors() {
        BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
        ItemColors itemColors = Minecraft.getMinecraft().getItemColors();

        blockColors.registerBlockColorHandler(
                (s, w, p, i) -> s.getValue(net.minecraft.block.BlockColored.COLOR).colorValue,
                FOAM, REINFORCED_FOAM, PETRIFIED_FOAM, REINFORCED_PETRIFIED_FOAM);

        final int rubberLeavesColor = 0x98de4b;

        blockColors.registerBlockColorHandler((s, w, p, i) -> rubberLeavesColor, RUBBER_LEAVES);
        itemColors.registerItemColorHandler((s, i) -> rubberLeavesColor, RUBBER_LEAVES);

        for (BlockCompressed block : COMPRESSED_BLOCKS) {
            blockColors.registerBlockColorHandler((s, w, p, i) -> block.getGtMaterial(s).getMaterialRGB(), block);
            itemColors.registerItemColorHandler((s, i) -> block.getGtMaterial(s).getMaterialRGB(), block);
        }

        for (BlockFrame block : FRAME_BLOCKS) {
            blockColors.registerBlockColorHandler((s, w, p, i) -> block.getGtMaterial(s).getMaterialRGB(), block);
            itemColors.registerItemColorHandler((s, i) -> block.getGtMaterial(s).getMaterialRGB(), block);
        }

        for (BlockSurfaceRock block : SURFACE_ROCK_BLOCKS) {
            blockColors.registerBlockColorHandler((s, w, p, i) -> i == 1 ? block.getGtMaterial(s).getMaterialRGB() : -1,
                    block);
        }

        for (BlockOre block : ORES) {
            blockColors.registerBlockColorHandler((s, w, p, i) -> i == 1 ? block.material.getMaterialRGB() : 0xFFFFFF,
                    block);
            itemColors.registerItemColorHandler((s, i) -> i == 1 ? block.material.getMaterialRGB() : 0xFFFFFF, block);
        }

        blockColors.registerBlockColorHandler(
                (s, w, p, i) -> MACHINE_CASING.getState(s) == BlockMachineCasing.MachineCasingType.ULV ?
                        0xFFFFFF : ConfigHolder.client.defaultPaintingColor,
                MACHINE_CASING);
        itemColors.registerItemColorHandler(
                (s, i) -> MACHINE_CASING.getState(s) == BlockMachineCasing.MachineCasingType.ULV ?
                        0xFFFFFF : ConfigHolder.client.defaultPaintingColor,
                MACHINE_CASING);

        blockColors.registerBlockColorHandler((s, w, p, i) -> ConfigHolder.client.defaultPaintingColor,
                HERMETIC_CASING);
        itemColors.registerItemColorHandler((s, i) -> ConfigHolder.client.defaultPaintingColor, HERMETIC_CASING);
    }

    public static void registerWalkingSpeedBonus() {
        for (IBlockState state : ASPHALT.getBlockState().getValidStates()) {
            BlockUtility.setWalkingSpeedBonus(state, BlockUtility.ASPHALT_WALKING_SPEED_BONUS);
        }
        for (IBlockState state : STUDS.getBlockState().getValidStates()) {
            BlockUtility.setWalkingSpeedBonus(state, BlockUtility.STUDS_WALKING_SPEED_BONUS);
        }
        for (StoneVariantBlock block : STONE_BLOCKS.values()) {
            BlockUtility.setWalkingSpeedBonus(block.getState(StoneVariantBlock.StoneType.CONCRETE_DARK),
                    BlockUtility.ASPHALT_WALKING_SPEED_BONUS);
            BlockUtility.setWalkingSpeedBonus(block.getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT),
                    BlockUtility.ASPHALT_WALKING_SPEED_BONUS);
        }
    }

    public static void registerOreDict() {
        OreDictUnifier.registerOre(new ItemStack(RUBBER_LEAVES), "treeLeaves");
        OreDictUnifier.registerOre(new ItemStack(RUBBER_SAPLING), "treeSapling");

        for (BlockLamp block : LAMPS.values()) {
            block.registerOreDict();
        }
        for (BlockLamp block : BORDERLESS_LAMPS.values()) {
            block.registerOreDict();
        }

        for (Entry<Material, BlockCompressed> entry : COMPRESSED.entrySet()) {
            Material material = entry.getKey();
            BlockCompressed block = entry.getValue();
            ItemStack itemStack = block.getItem(material);
            OreDictUnifier.registerOre(itemStack, OrePrefix.block, material);
        }

        for (Entry<Material, BlockFrame> entry : FRAMES.entrySet()) {
            Material material = entry.getKey();
            BlockFrame block = entry.getValue();
            ItemStack itemStack = block.getItem(material);
            OreDictUnifier.registerOre(itemStack, OrePrefix.frameGt, material);
        }

        for (BlockOre blockOre : ORES) {
            Material material = blockOre.material;
            for (StoneType stoneType : blockOre.STONE_TYPE.getAllowedValues()) {
                if (stoneType == null) continue;
                ItemStack normalStack = GTUtility.toItem(blockOre.getDefaultState()
                        .withProperty(blockOre.STONE_TYPE, stoneType));
                OreDictUnifier.registerOre(normalStack, stoneType.processingPrefix, material);
            }
        }
        for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
            for (BlockCable cable : CABLES.get(registry.getModid())) {
                for (Material pipeMaterial : cable.getEnabledMaterials()) {
                    ItemStack itemStack = cable.getItem(pipeMaterial);
                    OreDictUnifier.registerOre(itemStack, cable.getPrefix(), pipeMaterial);
                }
            }
            for (BlockFluidPipe pipe : FLUID_PIPES.get(registry.getModid())) {
                for (Material pipeMaterial : pipe.getEnabledMaterials()) {
                    ItemStack itemStack = pipe.getItem(pipeMaterial);
                    OreDictUnifier.registerOre(itemStack, pipe.getPrefix(), pipeMaterial);
                }
            }
            for (BlockItemPipe pipe : ITEM_PIPES.get(registry.getModid())) {
                for (Material pipeMaterial : pipe.getEnabledMaterials()) {
                    ItemStack itemStack = pipe.getItem(pipeMaterial);
                    OreDictUnifier.registerOre(itemStack, pipe.getPrefix(), pipeMaterial);
                }
            }
        }
    }

    public static String statePropertiesToString(Map<IProperty<?>, Comparable<?>> properties) {
        StringBuilder stringbuilder = new StringBuilder();

        List<Entry<IProperty<?>, Comparable<?>>> entries = properties.entrySet().stream()
                .sorted(Comparator.comparing(c -> c.getKey().getName()))
                .collect(Collectors.toList());

        for (Map.Entry<IProperty<?>, Comparable<?>> entry : entries) {
            if (stringbuilder.length() != 0) {
                stringbuilder.append(",");
            }

            IProperty<?> property = entry.getKey();
            stringbuilder.append(property.getName());
            stringbuilder.append("=");
            stringbuilder.append(getPropertyName(property, entry.getValue()));
        }

        if (stringbuilder.length() == 0) {
            stringbuilder.append("normal");
        }

        return stringbuilder.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }
}
