package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.items.OreDictNames;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.Mods;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.*;
import gregtech.common.blocks.BlockMachineCasing.MachineCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.StoneVariantBlock.StoneVariant;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;
import gregtech.loaders.recipe.chemistry.AssemblerRecipeLoader;
import gregtech.loaders.recipe.chemistry.ChemistryRecipes;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.BRONZE_BRICKS;
import static gregtech.common.blocks.MetaBlocks.METAL_CASING;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.metatileentities.MetaTileEntities.*;
import static gregtech.loaders.OreDictionaryLoader.OREDICT_BLOCK_FUEL_COKE;
import static gregtech.loaders.OreDictionaryLoader.OREDICT_FUEL_COKE;

public class MachineRecipeLoader {

    private MachineRecipeLoader() {}

    public static void init() {
        ChemistryRecipes.init();
        FuelRecipes.registerFuels();
        AssemblyLineLoader.init();
        FusionLoader.init();
        AssemblerRecipeLoader.init();
        ComponentRecipes.register();
        MiscRecipeLoader.init();
        BatteryRecipes.init();
        CircuitRecipes.init();
        ComputerRecipes.init();
        DecorationRecipes.init();
        WoodRecipeLoader.registerRecipes();

        registerDecompositionRecipes();
        registerBlastFurnaceRecipes();
        registerAssemblerRecipes();
        registerAlloyRecipes();
        registerBendingCompressingRecipes();
        registerCokeOvenRecipes();
        registerFluidRecipes();
        registerMixingCrystallizationRecipes();
        registerPrimitiveBlastFurnaceRecipes();
        registerRecyclingRecipes();
        registerStoneBricksRecipes();
        registerNBTRemoval();
        ConvertHatchToHatch();
    }

    private static void registerBendingCompressingRecipes() {
        COMPRESSOR_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.Fireclay)
                .outputs(MetaItems.COMPRESSED_FIRECLAY.getStackForm())
                .duration(80).EUt(4)
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder()
                .duration(100).EUt(16)
                .notConsumable(MetaItems.SHAPE_MOLD_CREDIT.getStackForm())
                .input(OrePrefix.plate, Materials.Cupronickel, 1)
                .outputs(MetaItems.CREDIT_CUPRONICKEL.getStackForm(4))
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder()
                .duration(100).EUt(16)
                .notConsumable(MetaItems.SHAPE_MOLD_CREDIT.getStackForm())
                .input(OrePrefix.plate, Materials.Brass, 1)
                .outputs(MetaItems.COIN_DOGE.getStackForm(4))
                .buildAndRegister();

        for (MetaItem<?>.MetaValueItem shapeMold : SHAPE_MOLDS) {
            FORMING_PRESS_RECIPES.recipeBuilder()
                    .duration(120).EUt(22)
                    .notConsumable(shapeMold.getStackForm())
                    .inputs(MetaItems.SHAPE_EMPTY.getStackForm())
                    .outputs(shapeMold.getStackForm())
                    .buildAndRegister();
        }

        for (MetaItem<?>.MetaValueItem shapeExtruder : SHAPE_EXTRUDERS) {
            if (shapeExtruder == null) continue;
            FORMING_PRESS_RECIPES.recipeBuilder()
                    .duration(120).EUt(22)
                    .notConsumable(shapeExtruder.getStackForm())
                    .inputs(MetaItems.SHAPE_EMPTY.getStackForm())
                    .outputs(shapeExtruder.getStackForm())
                    .buildAndRegister();
        }

        BENDER_RECIPES.recipeBuilder()
                .circuitMeta(4)
                .input(OrePrefix.plate, Materials.Steel, 4)
                .outputs(MetaItems.SHAPE_EMPTY.getStackForm())
                .duration(180).EUt(12)
                .buildAndRegister();

        BENDER_RECIPES.recipeBuilder()
                .circuitMeta(12)
                .input(OrePrefix.plate, Materials.Tin, 2)
                .outputs(MetaItems.FLUID_CELL.getStackForm())
                .duration(200).EUt(VA[ULV])
                .buildAndRegister();

        BENDER_RECIPES.recipeBuilder()
                .circuitMeta(12)
                .input(OrePrefix.plate, Materials.Steel)
                .outputs(MetaItems.FLUID_CELL.getStackForm())
                .duration(100).EUt(VA[ULV])
                .buildAndRegister();

        BENDER_RECIPES.recipeBuilder()
                .circuitMeta(12)
                .input(OrePrefix.plate, Polytetrafluoroethylene)
                .outputs(MetaItems.FLUID_CELL.getStackForm(4))
                .duration(100).EUt(VA[ULV])
                .buildAndRegister();

        BENDER_RECIPES.recipeBuilder()
                .circuitMeta(12)
                .input(OrePrefix.plate, Polybenzimidazole)
                .outputs(MetaItems.FLUID_CELL.getStackForm(16))
                .duration(100).EUt(VA[ULV])
                .buildAndRegister();

        EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, Materials.Tin, 2)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_CELL)
                .outputs(MetaItems.FLUID_CELL.getStackForm())
                .duration(128).EUt(VA[LV])
                .buildAndRegister();

        EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, Materials.Steel)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_CELL)
                .outputs(MetaItems.FLUID_CELL.getStackForm())
                .duration(128).EUt(VA[LV])
                .buildAndRegister();

        EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, Polytetrafluoroethylene)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_CELL)
                .outputs(MetaItems.FLUID_CELL.getStackForm(4))
                .duration(128).EUt(VA[LV])
                .buildAndRegister();

        EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.ingot, Polybenzimidazole)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_CELL)
                .outputs(MetaItems.FLUID_CELL.getStackForm(16))
                .duration(128).EUt(VA[LV])
                .buildAndRegister();

        EXTRUDER_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Glass)
                .notConsumable(MetaItems.SHAPE_EXTRUDER_CELL)
                .outputs(MetaItems.FLUID_CELL_GLASS_VIAL.getStackForm(4))
                .duration(128).EUt(VA[LV])
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.NetherQuartz)
                .output(OrePrefix.plate, Materials.NetherQuartz)
                .duration(400).EUt(2).buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.CertusQuartz)
                .output(OrePrefix.plate, Materials.CertusQuartz)
                .duration(400).EUt(2).buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.Quartzite)
                .output(OrePrefix.plate, Materials.Quartzite)
                .duration(400).EUt(2).buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder()
                .input(COKE_OVEN_BRICK, 4)
                .outputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.COKE_BRICKS))
                .duration(300).EUt(2).buildAndRegister();
    }

    private static void registerPrimitiveBlastFurnaceRecipes() {
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(ingot, Iron).input(gem, Coal, 2).output(ingot, Steel)
                .output(dustTiny, DarkAsh, 2).duration(1800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(ingot, Iron).input(dust, Coal, 2).output(ingot, Steel)
                .output(dustTiny, DarkAsh, 2).duration(1800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(ingot, Iron).input(gem, Charcoal, 2).output(ingot, Steel)
                .output(dustTiny, DarkAsh, 2).duration(1800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(ingot, Iron).input(dust, Charcoal, 2).output(ingot, Steel)
                .output(dustTiny, DarkAsh, 2).duration(1800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(ingot, Iron).input(OREDICT_FUEL_COKE).output(ingot, Steel)
                .output(dustTiny, Ash).duration(1500).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(ingot, Iron).input(dust, Coke).output(ingot, Steel)
                .output(dustTiny, Ash).duration(1500).buildAndRegister();

        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(block, Iron).input(block, Coal, 2).output(block, Steel)
                .output(dust, DarkAsh, 2).duration(16200).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(block, Iron).input(block, Charcoal, 2)
                .output(block, Steel).output(dust, DarkAsh, 2).duration(16200).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(block, Iron).input(OREDICT_BLOCK_FUEL_COKE)
                .output(block, Steel).output(dust, Ash).duration(13500).buildAndRegister();

        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(ingot, WroughtIron).input(gem, Coal, 2)
                .output(ingot, Steel).output(dustTiny, DarkAsh, 2).duration(800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(ingot, WroughtIron).input(dust, Coal, 2)
                .output(ingot, Steel).output(dustTiny, DarkAsh, 2).duration(800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(ingot, WroughtIron).input(gem, Charcoal, 2)
                .output(ingot, Steel).output(dustTiny, DarkAsh, 2).duration(800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(ingot, WroughtIron).input(dust, Charcoal, 2)
                .output(ingot, Steel).output(dustTiny, DarkAsh, 2).duration(800).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(ingot, WroughtIron).input(OREDICT_FUEL_COKE)
                .output(ingot, Steel).output(dustTiny, Ash).duration(600).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(ingot, WroughtIron).input(dust, Coke).output(ingot, Steel)
                .output(dustTiny, Ash).duration(600).buildAndRegister();

        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(block, WroughtIron).input(block, Coal, 2)
                .output(block, Steel).output(dust, DarkAsh, 2).duration(7200).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(block, WroughtIron).input(block, Charcoal, 2)
                .output(block, Steel).output(dust, DarkAsh, 2).duration(7200).buildAndRegister();
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().input(block, WroughtIron).input(OREDICT_BLOCK_FUEL_COKE)
                .output(block, Steel).output(dust, Ash).duration(5400).buildAndRegister();
    }

    private static void registerCokeOvenRecipes() {
        COKE_OVEN_RECIPES.recipeBuilder().input(log, Wood).output(gem, Charcoal).fluidOutputs(Creosote.getFluid(250))
                .duration(900).buildAndRegister();
        COKE_OVEN_RECIPES.recipeBuilder().input(gem, Coal).output(gem, Coke).fluidOutputs(Creosote.getFluid(500))
                .duration(900).buildAndRegister();
        COKE_OVEN_RECIPES.recipeBuilder().input(block, Coal).output(block, Coke).fluidOutputs(Creosote.getFluid(4500))
                .duration(8100).buildAndRegister();
    }

    private static void registerStoneBricksRecipes() {
        // normal variant -> cobble variant
        EnumMap<StoneVariant, List<ItemStack>> variantListMap = new EnumMap<>(StoneVariant.class);
        for (StoneVariant shape : StoneVariant.values()) {
            StoneVariantBlock block = MetaBlocks.STONE_BLOCKS.get(shape);
            variantListMap.put(shape,
                    Arrays.stream(StoneVariantBlock.StoneType.values())
                            .map(block::getItemVariant)
                            .collect(Collectors.toList()));
        }
        List<ItemStack> cobbles = variantListMap.get(StoneVariant.COBBLE);
        List<ItemStack> mossCobbles = variantListMap.get(StoneVariant.COBBLE_MOSSY);
        List<ItemStack> smooths = variantListMap.get(StoneVariant.SMOOTH);
        List<ItemStack> polisheds = variantListMap.get(StoneVariant.POLISHED);
        List<ItemStack> bricks = variantListMap.get(StoneVariant.BRICKS);
        List<ItemStack> crackedBricks = variantListMap.get(StoneVariant.BRICKS_CRACKED);
        List<ItemStack> mossBricks = variantListMap.get(StoneVariant.BRICKS_MOSSY);
        List<ItemStack> chiseledBricks = variantListMap.get(StoneVariant.CHISELED);
        List<ItemStack> tiledBricks = variantListMap.get(StoneVariant.TILED);
        List<ItemStack> smallTiledBricks = variantListMap.get(StoneVariant.TILED_SMALL);
        List<ItemStack> windmillA = variantListMap.get(StoneVariant.WINDMILL_A);
        List<ItemStack> windmillB = variantListMap.get(StoneVariant.WINDMILL_B);
        List<ItemStack> squareBricks = variantListMap.get(StoneVariant.BRICKS_SQUARE);
        List<ItemStack> smallBricks = variantListMap.get(StoneVariant.BRICKS_SMALL);

        registerSmoothRecipe(cobbles, smooths);
        registerCobbleRecipe(smooths, cobbles);
        registerMossRecipe(cobbles, mossCobbles);
        registerSmoothRecipe(smooths, polisheds);
        registerBricksRecipe(polisheds, bricks, MarkerMaterials.Color.LightBlue);
        registerCobbleRecipe(bricks, crackedBricks);
        registerMossRecipe(bricks, mossBricks);
        registerBricksRecipe(polisheds, chiseledBricks, MarkerMaterials.Color.White);
        registerBricksRecipe(polisheds, tiledBricks, MarkerMaterials.Color.Red);
        registerBricksRecipe(tiledBricks, smallTiledBricks, MarkerMaterials.Color.Red);
        registerBricksRecipe(polisheds, windmillA, MarkerMaterials.Color.Blue);
        registerBricksRecipe(polisheds, windmillB, MarkerMaterials.Color.Yellow);
        registerBricksRecipe(polisheds, squareBricks, MarkerMaterials.Color.Green);
        registerBricksRecipe(polisheds, smallBricks, MarkerMaterials.Color.Pink);

        for (int i = 0; i < smooths.size(); i++) {
            EXTRUDER_RECIPES.recipeBuilder()
                    .inputs(smooths.get(i))
                    .notConsumable(SHAPE_EXTRUDER_INGOT.getStackForm())
                    .outputs(bricks.get(i))
                    .duration(24).EUt(8).buildAndRegister();
        }
    }

    private static void registerMixingCrystallizationRecipes() {
        RecipeMaps.AUTOCLAVE_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.SiliconDioxide)
                .fluidInputs(Materials.DistilledWater.getFluid(250))
                .chancedOutput(OreDictUnifier.get(OrePrefix.gem, Materials.Quartzite), 1000, 1000)
                .duration(1200).EUt(24).buildAndRegister();

        // todo find UU-Matter replacement
        // RecipeMaps.AUTOCLAVE_RECIPES.recipeBuilder()
        // .input(OrePrefix.dust, Materials.NetherStar)
        // .fluidInputs(Materials.UUMatter.getFluid(576))
        // .chancedOutput(new ItemStack(Items.NETHER_STAR), 3333, 3333)
        // .duration(72000).EUt(VA[HV]).buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
                .input(OrePrefix.crushedPurified, Materials.Sphalerite)
                .input(OrePrefix.crushedPurified, Materials.Galena)
                .fluidInputs(Materials.SulfuricAcid.getFluid(4000))
                .fluidOutputs(Materials.IndiumConcentrate.getFluid(1000))
                .duration(60).EUt(150).buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
                .input(dust, Coal)
                .fluidInputs(Concrete.getFluid(L))
                .outputs(MetaBlocks.ASPHALT.getItemVariant(BlockAsphalt.BlockType.ASPHALT))
                .duration(60).EUt(16).buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
                .input(dust, Charcoal)
                .fluidInputs(Concrete.getFluid(L))
                .outputs(MetaBlocks.ASPHALT.getItemVariant(BlockAsphalt.BlockType.ASPHALT))
                .duration(60).EUt(16).buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder()
                .input(dust, Carbon)
                .fluidInputs(Concrete.getFluid(L))
                .outputs(MetaBlocks.ASPHALT.getItemVariant(BlockAsphalt.BlockType.ASPHALT))
                .duration(60).EUt(16).buildAndRegister();
    }

    private static final MaterialStack[][] alloySmelterList = {
            { new MaterialStack(Materials.Copper, 3L), new MaterialStack(Materials.Tin, 1),
                    new MaterialStack(Materials.Bronze, 4L) },
            { new MaterialStack(Materials.Copper, 3L), new MaterialStack(Materials.Zinc, 1),
                    new MaterialStack(Materials.Brass, 4L) },
            { new MaterialStack(Materials.Copper, 1), new MaterialStack(Materials.Nickel, 1),
                    new MaterialStack(Materials.Cupronickel, 2L) },
            { new MaterialStack(Materials.Copper, 1), new MaterialStack(Materials.Redstone, 4L),
                    new MaterialStack(Materials.RedAlloy, 1) },
            { new MaterialStack(Materials.AnnealedCopper, 3L), new MaterialStack(Materials.Tin, 1),
                    new MaterialStack(Materials.Bronze, 4L) },
            { new MaterialStack(Materials.AnnealedCopper, 3L), new MaterialStack(Materials.Zinc, 1),
                    new MaterialStack(Materials.Brass, 4L) },
            { new MaterialStack(Materials.AnnealedCopper, 1), new MaterialStack(Materials.Nickel, 1),
                    new MaterialStack(Materials.Cupronickel, 2L) },
            { new MaterialStack(Materials.AnnealedCopper, 1), new MaterialStack(Materials.Redstone, 4L),
                    new MaterialStack(Materials.RedAlloy, 1) },
            { new MaterialStack(Materials.Iron, 1), new MaterialStack(Materials.Tin, 1),
                    new MaterialStack(Materials.TinAlloy, 2L) },
            { new MaterialStack(Materials.WroughtIron, 1), new MaterialStack(Materials.Tin, 1),
                    new MaterialStack(Materials.TinAlloy, 2L) },
            { new MaterialStack(Materials.Iron, 2L), new MaterialStack(Materials.Nickel, 1),
                    new MaterialStack(Materials.Invar, 3L) },
            { new MaterialStack(Materials.WroughtIron, 2L), new MaterialStack(Materials.Nickel, 1),
                    new MaterialStack(Materials.Invar, 3L) },
            { new MaterialStack(Materials.Lead, 4L), new MaterialStack(Materials.Antimony, 1),
                    new MaterialStack(Materials.BatteryAlloy, 5L) },
            { new MaterialStack(Materials.Gold, 1), new MaterialStack(Materials.Silver, 1),
                    new MaterialStack(Materials.Electrum, 2L) },
            { new MaterialStack(Materials.Magnesium, 1), new MaterialStack(Materials.Aluminium, 2L),
                    new MaterialStack(Materials.Magnalium, 3L) },
            { new MaterialStack(Materials.Silver, 1), new MaterialStack(Materials.Electrotine, 4),
                    new MaterialStack(Materials.BlueAlloy, 1) } };

    private static void registerAlloyRecipes() {
        for (MaterialStack[] stack : alloySmelterList) {
            if (stack[0].material.hasProperty(PropertyKey.INGOT)) {
                RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                        .duration((int) stack[2].amount * 50).EUt(16)
                        .input(OrePrefix.ingot, stack[0].material, (int) stack[0].amount)
                        .input(OrePrefix.dust, stack[1].material, (int) stack[1].amount)
                        .outputs(OreDictUnifier.get(OrePrefix.ingot, stack[2].material, (int) stack[2].amount))
                        .buildAndRegister();
            }
            if (stack[1].material.hasProperty(PropertyKey.INGOT)) {
                RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                        .duration((int) stack[2].amount * 50).EUt(16)
                        .input(OrePrefix.dust, stack[0].material, (int) stack[0].amount)
                        .input(OrePrefix.ingot, stack[1].material, (int) stack[1].amount)
                        .outputs(OreDictUnifier.get(OrePrefix.ingot, stack[2].material, (int) stack[2].amount))
                        .buildAndRegister();
            }
            if (stack[0].material.hasProperty(PropertyKey.INGOT) && stack[1].material.hasProperty(PropertyKey.INGOT)) {
                RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                        .duration((int) stack[2].amount * 50).EUt(16)
                        .input(OrePrefix.ingot, stack[0].material, (int) stack[0].amount)
                        .input(OrePrefix.ingot, stack[1].material, (int) stack[1].amount)
                        .outputs(OreDictUnifier.get(OrePrefix.ingot, stack[2].material, (int) stack[2].amount))
                        .buildAndRegister();
            }
            RecipeMaps.ALLOY_SMELTER_RECIPES.recipeBuilder()
                    .duration((int) stack[2].amount * 50).EUt(16)
                    .input(OrePrefix.dust, stack[0].material, (int) stack[0].amount)
                    .input(OrePrefix.dust, stack[1].material, (int) stack[1].amount)
                    .outputs(OreDictUnifier.get(OrePrefix.ingot, stack[2].material, (int) stack[2].amount))
                    .buildAndRegister();
        }

        COMPRESSOR_RECIPES.recipeBuilder().inputs(MetaItems.CARBON_FIBERS.getStackForm(2))
                .outputs(MetaItems.CARBON_MESH.getStackForm()).duration(100).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().inputs(MetaItems.CARBON_MESH.getStackForm())
                .outputs(MetaItems.CARBON_FIBER_PLATE.getStackForm()).buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().duration(10).EUt(VA[ULV]).input(OrePrefix.ingot, Materials.Rubber, 2)
                .notConsumable(MetaItems.SHAPE_MOLD_PLATE).output(OrePrefix.plate, Materials.Rubber).buildAndRegister();
        ALLOY_SMELTER_RECIPES.recipeBuilder().duration(100).EUt(VA[ULV]).input(OrePrefix.dust, Materials.Sulfur)
                .input(OrePrefix.dust, Materials.RawRubber, 3).output(OrePrefix.ingot, Materials.Rubber)
                .buildAndRegister();

        ALLOY_SMELTER_RECIPES.recipeBuilder().duration(150).EUt(VA[ULV]).inputs(OreDictUnifier.get("sand"))
                .inputs(new ItemStack(Items.CLAY_BALL)).outputs(COKE_OVEN_BRICK.getStackForm(2)).buildAndRegister();
    }

    private static void registerAssemblerRecipes() {
        for (int i = 0; i < Materials.CHEMICAL_DYES.length; i++) {
            CANNER_RECIPES.recipeBuilder()
                    .inputs(MetaItems.SPRAY_EMPTY.getStackForm())
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(GTValues.L * 4))
                    .outputs(MetaItems.SPRAY_CAN_DYES[i].getStackForm())
                    .EUt(VA[ULV]).duration(200)
                    .buildAndRegister();

            EnumDyeColor color = EnumDyeColor.byMetadata(i);
            BlockLamp lamp = MetaBlocks.LAMPS.get(color);
            for (int lampMeta = 0; lampMeta < lamp.getItemMetadataStates(); lampMeta++) {
                ASSEMBLER_RECIPES.recipeBuilder()
                        .input(plate, Glass, 6)
                        .input(dust, Glowstone, 1)
                        .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(GTValues.L))
                        .outputs(new ItemStack(lamp, 6, lampMeta))
                        .circuitMeta(lampMeta + 1).EUt(VA[ULV]).duration(40)
                        .buildAndRegister();

                ASSEMBLER_RECIPES.recipeBuilder()
                        .input(lampGt, MarkerMaterials.Color.COLORS.get(color))
                        .outputs(new ItemStack(lamp, 1, lampMeta))
                        .circuitMeta(lampMeta + 1).EUt(VA[ULV]).duration(10)
                        .buildAndRegister();
            }
            lamp = MetaBlocks.BORDERLESS_LAMPS.get(color);
            for (int lampMeta = 0; lampMeta < lamp.getItemMetadataStates(); lampMeta++) {
                ASSEMBLER_RECIPES.recipeBuilder()
                        .input(plate, Glass, 6)
                        .input(dust, Glowstone, 1)
                        .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(GTValues.L))
                        .outputs(new ItemStack(lamp, 6, lampMeta))
                        .circuitMeta(lampMeta + 9).EUt(VA[ULV]).duration(40)
                        .buildAndRegister();

                ASSEMBLER_RECIPES.recipeBuilder()
                        .input(lampGt, MarkerMaterials.Color.COLORS.get(color))
                        .outputs(new ItemStack(lamp, 1, lampMeta))
                        .circuitMeta(lampMeta + 9).EUt(VA[ULV]).duration(10)
                        .buildAndRegister();
            }
        }

        CANNER_RECIPES.recipeBuilder()
                .input(SPRAY_EMPTY)
                .fluidInputs(Acetone.getFluid(1000))
                .output(SPRAY_SOLVENT)
                .EUt(VA[ULV]).duration(200)
                .buildAndRegister();

        Material material = Materials.Iron;

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.IRON_DOOR))
                .input(OrePrefix.plate, material, 2)
                .outputs(MetaItems.COVER_SHUTTER.getStackForm(2))
                .EUt(16).duration(100)
                .buildAndRegister();

        FluidStack solder = SolderingAlloy.getFluid(L / 2);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.LEVER))
                .input(OrePrefix.plate, material)
                .fluidInputs(solder)
                .outputs(MetaItems.COVER_MACHINE_CONTROLLER.getStackForm(1))
                .EUt(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(cableGtSingle, Copper, 4)
                .input(circuit, MarkerMaterials.Tier.LV)
                .input(plate, material)
                .fluidInputs(solder)
                .outputs(COVER_ENERGY_DETECTOR.getStackForm())
                .EUt(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(COVER_ENERGY_DETECTOR)
                .input(SENSOR_HV)
                .fluidInputs(solder)
                .output(COVER_ENERGY_DETECTOR_ADVANCED)
                .EUt(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.REDSTONE_TORCH))
                .input(plate, material)
                .fluidInputs(solder)
                .outputs(COVER_ACTIVITY_DETECTOR.getStackForm())
                .EUt(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(wireFine, Gold, 4)
                .input(circuit, MarkerMaterials.Tier.HV)
                .input(plate, Aluminium)
                .fluidInputs(solder)
                .outputs(COVER_ACTIVITY_DETECTOR_ADVANCED.getStackForm())
                .EUt(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE))
                .input(plate, material)
                .fluidInputs(solder)
                .outputs(COVER_FLUID_DETECTOR.getStackForm())
                .EUt(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE))
                .input(plate, material)
                .fluidInputs(solder)
                .outputs(COVER_ITEM_DETECTOR.getStackForm())
                .EUt(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(COVER_FLUID_DETECTOR)
                .input(SENSOR_HV)
                .fluidInputs(solder)
                .outputs(COVER_FLUID_DETECTOR_ADVANCED.getStackForm())
                .EUt(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(COVER_ITEM_DETECTOR)
                .input(SENSOR_HV)
                .fluidInputs(solder)
                .outputs(COVER_ITEM_DETECTOR_ADVANCED.getStackForm())
                .EUt(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(EMITTER_LV)
                .input(plate, Steel)
                .circuitMeta(1)
                .fluidInputs(solder)
                .outputs(COVER_MAINTENANCE_DETECTOR.getStackForm())
                .EUt(16).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Glass)
                .input(foil, Aluminium, 4)
                .input(circuit, MarkerMaterials.Tier.LV)
                .input(wireFine, Copper, 4)
                .outputs(COVER_SCREEN.getStackForm())
                .EUt(16).duration(50)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ELECTRIC_PUMP_HV, 2)
                .inputs(new ItemStack(Items.CAULDRON))
                .input(circuit, MarkerMaterials.Tier.HV)
                .output(COVER_INFINITE_WATER)
                .EUt(VA[HV]).duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, EnderPearl, 9)
                .input(plateDouble, StainlessSteel)
                .input(SENSOR_HV)
                .input(EMITTER_HV)
                .input(ELECTRIC_PUMP_HV)
                .fluidInputs(Polyethylene.getFluid(L * 2))
                .output(COVER_ENDER_FLUID_LINK)
                .EUt(VA[HV]).duration(320)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OreDictNames.chestWood.toString())
                .input(ELECTRIC_PISTON_LV)
                .input(plate, Iron)
                .fluidInputs(SolderingAlloy.getFluid(72))
                .output(COVER_STORAGE)
                .EUt(16)
                .duration(100)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, WroughtIron, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ULV)).circuitMeta(8).duration(25)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Steel, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LV)).circuitMeta(8).duration(50)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Aluminium, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.MV)).circuitMeta(8).duration(50)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, StainlessSteel, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.HV)).circuitMeta(8).duration(50)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Titanium, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.EV)).circuitMeta(8).duration(50)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, TungstenSteel, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.IV)).circuitMeta(8).duration(50)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, RhodiumPlatedPalladium, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LuV)).circuitMeta(8).duration(50)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, NaquadahAlloy, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ZPM)).circuitMeta(8).duration(50)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Darmstadtium, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.UV)).circuitMeta(8).duration(50)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Neutronium, 8)
                .outputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.UHV)).circuitMeta(8).duration(50)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LV]).input(OrePrefix.wireGtDouble, Materials.Cupronickel, 8)
                .input(OrePrefix.foil, Materials.Bronze, 8).fluidInputs(Materials.TinAlloy.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.CUPRONICKEL)).duration(200).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[MV]).input(OrePrefix.wireGtDouble, Materials.Kanthal, 8)
                .input(OrePrefix.foil, Materials.Aluminium, 8).fluidInputs(Materials.Copper.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.KANTHAL)).duration(300).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[HV]).input(OrePrefix.wireGtDouble, Materials.Nichrome, 8)
                .input(OrePrefix.foil, Materials.StainlessSteel, 8)
                .fluidInputs(Materials.Aluminium.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.NICHROME)).duration(400).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[EV]).input(OrePrefix.wireGtDouble, Materials.RTMAlloy, 8)
                .input(OrePrefix.foil, Materials.VanadiumSteel, 8).fluidInputs(Materials.Nichrome.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.RTM_ALLOY)).duration(500).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[IV]).input(OrePrefix.wireGtDouble, Materials.HSSG, 8)
                .input(OrePrefix.foil, Materials.TungstenCarbide, 8)
                .fluidInputs(Materials.Tungsten.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.HSS_G)).duration(600).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LuV]).input(OrePrefix.wireGtDouble, Materials.Naquadah, 8)
                .input(OrePrefix.foil, Materials.Osmium, 8).fluidInputs(Materials.TungstenSteel.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.NAQUADAH)).duration(700).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[ZPM]).input(OrePrefix.wireGtDouble, Materials.Trinium, 8)
                .input(OrePrefix.foil, Materials.NaquadahEnriched, 8)
                .fluidInputs(Materials.Naquadah.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.TRINIUM)).duration(800).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[UV]).input(OrePrefix.wireGtDouble, Materials.Tritanium, 8)
                .input(OrePrefix.foil, Materials.Naquadria, 8).fluidInputs(Materials.Trinium.getFluid(GTValues.L))
                .outputs(MetaBlocks.WIRE_COIL.getItemVariant(CoilType.TRITANIUM)).duration(900).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Bronze, 6)
                .inputs(new ItemStack(Blocks.BRICK_BLOCK, 1)).circuitMeta(6)
                .outputs(METAL_CASING.getItemVariant(BRONZE_BRICKS, ConfigHolder.recipes.casingsPerCraft)).duration(50)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Invar, 6)
                .input(OrePrefix.frameGt, Materials.Invar, 1).circuitMeta(6).outputs(MetaBlocks.METAL_CASING
                        .getItemVariant(MetalCasingType.INVAR_HEATPROOF, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Steel, 6)
                .input(OrePrefix.frameGt, Materials.Steel, 1).circuitMeta(6).outputs(MetaBlocks.METAL_CASING
                        .getItemVariant(MetalCasingType.STEEL_SOLID, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Aluminium, 6)
                .input(OrePrefix.frameGt, Materials.Aluminium, 1).circuitMeta(6).outputs(MetaBlocks.METAL_CASING
                        .getItemVariant(MetalCasingType.ALUMINIUM_FROSTPROOF, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.TungstenSteel, 6)
                .input(OrePrefix.frameGt, Materials.TungstenSteel, 1).circuitMeta(6).outputs(MetaBlocks.METAL_CASING
                        .getItemVariant(MetalCasingType.TUNGSTENSTEEL_ROBUST, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.StainlessSteel, 6)
                .input(OrePrefix.frameGt, Materials.StainlessSteel, 1).circuitMeta(6).outputs(MetaBlocks.METAL_CASING
                        .getItemVariant(MetalCasingType.STAINLESS_CLEAN, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Titanium, 6)
                .input(OrePrefix.frameGt, Materials.Titanium, 1).circuitMeta(6).outputs(MetaBlocks.METAL_CASING
                        .getItemVariant(MetalCasingType.TITANIUM_STABLE, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(plate, HSSE, 6).input(frameGt, Europium).circuitMeta(6)
                .outputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.HSSE_STURDY,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(plate, Palladium, 6).input(frameGt, Iridium).circuitMeta(6)
                .outputs(METAL_CASING.getItemVariant(MetalCasingType.PALLADIUM_SUBSTATION,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16)
                .inputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.STEEL_SOLID))
                .fluidInputs(Materials.Polytetrafluoroethylene.getFluid(216)).circuitMeta(6)
                .outputs(MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.PTFE_INERT_CASING)).duration(50)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LuV])
                .input(OrePrefix.wireGtDouble, Materials.IndiumTinBariumTitaniumCuprate, 32)
                .input(OrePrefix.foil, Materials.NiobiumTitanium, 32)
                .fluidInputs(Materials.Trinium.getFluid(GTValues.L * 24))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .duration(100).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[ZPM])
                .input(OrePrefix.wireGtDouble, Materials.UraniumRhodiumDinaquadide, 16)
                .input(OrePrefix.foil, Materials.NiobiumTitanium, 16)
                .fluidInputs(Materials.Trinium.getFluid(GTValues.L * 16))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .duration(100).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[UV])
                .input(OrePrefix.wireGtDouble, Materials.EnrichedNaquadahTriniumEuropiumDuranide, 8)
                .input(OrePrefix.foil, Materials.NiobiumTitanium, 8)
                .fluidInputs(Materials.Trinium.getFluid(GTValues.L * 8))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .duration(100).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[UV])
                .input(OrePrefix.wireGtDouble, Materials.RutheniumTriniumAmericiumNeutronate, 4)
                .input(OrePrefix.foil, Materials.NiobiumTitanium, 4)
                .fluidInputs(Materials.Trinium.getFluid(GTValues.L * 4))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .duration(200).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[ZPM])
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .inputs(MetaItems.FIELD_GENERATOR_IV.getStackForm(2)).inputs(MetaItems.ELECTRIC_PUMP_IV.getStackForm())
                .inputs(MetaItems.NEUTRON_REFLECTOR.getStackForm(2))
                .input(OrePrefix.circuit, MarkerMaterials.Tier.LuV, 4)
                .input(OrePrefix.pipeSmallFluid, Materials.Naquadah, 4).input(OrePrefix.plate, Materials.Europium, 4)
                .fluidInputs(Materials.VanadiumGallium.getFluid(GTValues.L * 4))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_COIL))
                .duration(100).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LuV])
                .inputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.LAMINATED_GLASS))
                .input(OrePrefix.plate, Materials.Naquadah, 4).inputs(MetaItems.NEUTRON_REFLECTOR.getStackForm(4))
                .outputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.FUSION_GLASS,
                        ConfigHolder.recipes.casingsPerCraft))
                .fluidInputs(Materials.Polybenzimidazole.getFluid(GTValues.L)).duration(50)
                .cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LuV])
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LuV))
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL))
                .inputs(MetaItems.NEUTRON_REFLECTOR.getStackForm()).inputs(MetaItems.ELECTRIC_PUMP_LuV.getStackForm())
                .input(OrePrefix.plate, Materials.TungstenSteel, 6)
                .fluidInputs(Materials.Polybenzimidazole.getFluid(GTValues.L))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_CASING,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(100).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[ZPM])
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ZPM))
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_COIL))
                .inputs(MetaItems.VOLTAGE_COIL_ZPM.getStackForm(2)).inputs(MetaItems.FIELD_GENERATOR_LuV.getStackForm())
                .input(OrePrefix.plate, Materials.Europium, 6)
                .fluidInputs(Materials.Polybenzimidazole.getFluid(GTValues.L * 2))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_CASING_MK2,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(100).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[UV])
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.UV))
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_COIL))
                .inputs(MetaItems.VOLTAGE_COIL_UV.getStackForm(2)).inputs(MetaItems.FIELD_GENERATOR_ZPM.getStackForm())
                .input(OrePrefix.plate, Materials.Americium, 6)
                .fluidInputs(Materials.Polybenzimidazole.getFluid(GTValues.L * 4))
                .outputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_CASING_MK3,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(100).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plate, Materials.Magnalium, 6)
                .input(OrePrefix.frameGt, Materials.BlueSteel, 1).circuitMeta(6).outputs(MetaBlocks.TURBINE_CASING
                        .getItemVariant(TurbineCasingType.STEEL_TURBINE_CASING, ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16)
                .inputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.STEEL_TURBINE_CASING))
                .input(OrePrefix.plate, Materials.StainlessSteel, 6).circuitMeta(6)
                .outputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.STAINLESS_TURBINE_CASING,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16)
                .inputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.STEEL_TURBINE_CASING))
                .input(OrePrefix.plate, Materials.Titanium, 6).circuitMeta(6)
                .outputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.TITANIUM_TURBINE_CASING,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(16)
                .inputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.STEEL_TURBINE_CASING))
                .input(OrePrefix.plate, Materials.TungstenSteel, 6).circuitMeta(6)
                .outputs(MetaBlocks.TURBINE_CASING.getItemVariant(TurbineCasingType.TUNGSTENSTEEL_TURBINE_CASING,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(50).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(48).input(OrePrefix.frameGt, Materials.Steel)
                .input(OrePrefix.plate, Materials.Polyethylene, 6).fluidInputs(Concrete.getFluid(L))
                .outputs(MetaBlocks.CLEANROOM_CASING.getItemVariant(BlockCleanroomCasing.CasingType.PLASCRETE,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(200).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().EUt(48).input(OrePrefix.frameGt, Materials.Steel)
                .input(OrePrefix.plate, Materials.Polyethylene, 6).fluidInputs(Glass.getFluid(L))
                .outputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.CLEANROOM_GLASS,
                        ConfigHolder.recipes.casingsPerCraft))
                .duration(200).buildAndRegister();

        // If these recipes are changed, change the values in MaterialInfoLoader.java

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(25).EUt(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ULV))
                .input(OrePrefix.cableGtSingle, Materials.RedAlloy, 2)
                .fluidInputs(Materials.Polyethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[0].getStackForm())
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LV))
                .input(OrePrefix.cableGtSingle, Materials.Tin, 2).fluidInputs(Materials.Polyethylene.getFluid(L * 2))
                .outputs(MetaTileEntities.HULL[1].getStackForm()).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.MV))
                .input(OrePrefix.cableGtSingle, Materials.Copper, 2).fluidInputs(Materials.Polyethylene.getFluid(L * 2))
                .outputs(MetaTileEntities.HULL[2].getStackForm()).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.MV))
                .input(OrePrefix.cableGtSingle, Materials.AnnealedCopper, 2)
                .fluidInputs(Materials.Polyethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[2].getStackForm())
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.HV))
                .input(OrePrefix.cableGtSingle, Materials.Gold, 2).fluidInputs(Materials.Polyethylene.getFluid(L * 2))
                .outputs(MetaTileEntities.HULL[3].getStackForm()).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.EV))
                .input(OrePrefix.cableGtSingle, Materials.Aluminium, 2)
                .fluidInputs(Materials.Polyethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[4].getStackForm())
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.IV))
                .input(OrePrefix.cableGtSingle, Materials.Platinum, 2)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[5].getStackForm())
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.LuV))
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 2)).outputs(MetaTileEntities.HULL[6].getStackForm())
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ZPM))
                .input(OrePrefix.cableGtSingle, Materials.VanadiumGallium, 2)
                .fluidInputs(Polybenzimidazole.getFluid(L * 2)).outputs(MetaTileEntities.HULL[7].getStackForm())
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.UV))
                .input(cableGtSingle, Materials.YttriumBariumCuprate, 2).fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .outputs(MetaTileEntities.HULL[8].getStackForm()).buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().duration(50).EUt(16)
                .inputs(MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.UHV))
                .input(cableGtSingle, Materials.Europium, 2).fluidInputs(Polybenzimidazole.getFluid(L * 2))
                .outputs(MetaTileEntities.HULL[9].getStackForm()).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().EUt(2).input(OreDictNames.chestWood.toString()).input(plate, Iron, 5)
                .outputs(new ItemStack(Blocks.HOPPER)).duration(800).circuitMeta(1).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(2).input(OreDictNames.chestWood.toString()).input(plate, WroughtIron, 5)
                .outputs(new ItemStack(Blocks.HOPPER)).duration(800).circuitMeta(1).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(OrePrefix.plank, Wood, 4).input(screw, Iron, 4)
                .outputs(WOODEN_CRATE.getStackForm()).duration(100).circuitMeta(5).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(stickLong, Bronze, 4).input(plate, Bronze, 4)
                .outputs(BRONZE_CRATE.getStackForm()).duration(200).circuitMeta(1).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(stickLong, Steel, 4).input(plate, Steel, 4)
                .outputs(STEEL_CRATE.getStackForm()).duration(200).circuitMeta(1).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(stickLong, Aluminium, 4).input(plate, Aluminium, 4)
                .outputs(ALUMINIUM_CRATE.getStackForm()).duration(200).circuitMeta(1).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(stickLong, StainlessSteel, 4).input(plate, StainlessSteel, 4)
                .outputs(STAINLESS_STEEL_CRATE.getStackForm()).circuitMeta(1).duration(200).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(stickLong, Titanium, 4).input(plate, Titanium, 4)
                .outputs(TITANIUM_CRATE.getStackForm()).duration(200).circuitMeta(1).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(stickLong, TungstenSteel, 4).input(plate, TungstenSteel, 4)
                .outputs(TUNGSTENSTEEL_CRATE.getStackForm()).duration(200).circuitMeta(1).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(stickLong, Bronze, 2).input(plate, Bronze, 4)
                .outputs(BRONZE_DRUM.getStackForm()).duration(200).circuitMeta(2).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(stickLong, Steel, 2).input(plate, Steel, 4)
                .outputs(STEEL_DRUM.getStackForm()).duration(200).circuitMeta(2).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(stickLong, Aluminium, 2).input(plate, Aluminium, 4)
                .outputs(ALUMINIUM_DRUM.getStackForm()).duration(200).circuitMeta(2).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(stickLong, StainlessSteel, 2).input(plate, StainlessSteel, 4)
                .outputs(STAINLESS_STEEL_DRUM.getStackForm()).duration(200).circuitMeta(2).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(stickLong, Titanium, 2).input(plate, Titanium, 4)
                .outputs(TITANIUM_DRUM.getStackForm()).duration(200).circuitMeta(2).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(stickLong, TungstenSteel, 2).input(plate, TungstenSteel, 4)
                .outputs(TUNGSTENSTEEL_DRUM.getStackForm()).duration(200).circuitMeta(2).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).input(stickLong, Gold, 2).input(plate, Gold, 4)
                .outputs(GOLD_DRUM.getStackForm()).duration(200).circuitMeta(2).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LV]).input(foil, Polyethylene, 4).input(CARBON_MESH)
                .fluidInputs(Polyethylene.getFluid(288)).output(DUCT_TAPE).duration(100).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LV]).input(foil, SiliconeRubber, 2).input(CARBON_MESH)
                .fluidInputs(Polyethylene.getFluid(288)).output(DUCT_TAPE, 2).duration(100).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LV]).input(foil, Polycaprolactam, 2).input(CARBON_MESH)
                .fluidInputs(Polyethylene.getFluid(144)).output(DUCT_TAPE, 4).duration(100).buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LV]).input(foil, Polybenzimidazole).input(CARBON_MESH)
                .fluidInputs(Polyethylene.getFluid(72)).output(DUCT_TAPE, 8).duration(100).buildAndRegister();

        ModHandler.addShapedRecipe("basic_tape", BASIC_TAPE.getStackForm(), " P ", "PSP", " P ", 'P',
                new UnificationEntry(plate, Paper), 'S', STICKY_RESIN.getStackForm());
        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[ULV]).input(plate, Paper, 2).input(STICKY_RESIN).output(BASIC_TAPE, 2)
                .duration(100).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plateDouble, Steel, 2)
                .input(ring, Bronze, 2)
                .output(FLUID_CELL_LARGE_STEEL)
                .duration(200).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plateDouble, Aluminium, 2)
                .input(ring, Silver, 2)
                .output(FLUID_CELL_LARGE_ALUMINIUM)
                .duration(200).EUt(64).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plateDouble, StainlessSteel, 3)
                .input(ring, Electrum, 3)
                .output(FLUID_CELL_LARGE_STAINLESS_STEEL)
                .duration(200).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plateDouble, Titanium, 3)
                .input(ring, RoseGold, 3)
                .output(FLUID_CELL_LARGE_TITANIUM)
                .duration(200).EUt(256).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plateDouble, TungstenSteel, 4)
                .input(ring, Platinum, 4)
                .output(FLUID_CELL_LARGE_TUNGSTEN_STEEL)
                .duration(200).EUt(VA[HV]).buildAndRegister();
    }

    private static void registerBlastFurnaceRecipes() {
        // Steel
        BLAST_RECIPES.recipeBuilder().duration(500).EUt(VA[MV])
                .input(ingot, Iron)
                .fluidInputs(Oxygen.getFluid(200))
                .output(ingot, Steel)
                .chancedOutput(dust, Ash, "1/9", 0)
                .blastFurnaceTemp(1000)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(400).EUt(VA[MV])
                .input(dust, Iron)
                .fluidInputs(Oxygen.getFluid(200))
                .output(ingot, Steel)
                .chancedOutput(dust, Ash, "1/9", 0)
                .circuitMeta(2)
                .blastFurnaceTemp(1000)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(300).EUt(VA[MV])
                .input(ingot, WroughtIron)
                .fluidInputs(Oxygen.getFluid(200))
                .output(ingot, Steel)
                .chancedOutput(dust, Ash, "1/9", 0)
                .blastFurnaceTemp(1000)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(100).EUt(VA[MV])
                .input(dust, WroughtIron)
                .fluidInputs(Oxygen.getFluid(200))
                .output(ingot, Steel)
                .chancedOutput(dust, Ash, "1/9", 0)
                .circuitMeta(2)
                .blastFurnaceTemp(1000)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(250).EUt(VA[EV])
                .input(dust, Iron, 4)
                .input(dust, Carbon)
                .output(ingot, Steel, 4)
                .chancedOutput(dust, Ash, "1/9", 0)
                .blastFurnaceTemp(2000)
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(50).EUt(VA[EV])
                .input(dust, WroughtIron, 4)
                .input(dust, Carbon)
                .output(ingot, Steel, 4)
                .chancedOutput(dust, Ash, "1/9", 0)
                .blastFurnaceTemp(2000)
                .buildAndRegister();

        // Aluminium from aluminium oxide gems
        BLAST_RECIPES.recipeBuilder().duration(400).EUt(100).input(dust, Ruby).output(nugget, Aluminium, 3)
                .chancedOutput(dust, Ash, "1/9", 0).blastFurnaceTemp(1200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(320).EUt(100).input(gem, Ruby).output(nugget, Aluminium, 3)
                .chancedOutput(dust, Ash, "1/9", 0).blastFurnaceTemp(1200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(400).EUt(100).input(dust, GreenSapphire).output(nugget, Aluminium, 3)
                .chancedOutput(dust, Ash, "1/9", 0).blastFurnaceTemp(1200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(320).EUt(100).input(gem, GreenSapphire).output(nugget, Aluminium, 3)
                .chancedOutput(dust, Ash, "1/9", 0).blastFurnaceTemp(1200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(400).EUt(100).input(dust, Sapphire).output(nugget, Aluminium, 3)
                .blastFurnaceTemp(1200).buildAndRegister();
        BLAST_RECIPES.recipeBuilder().duration(320).EUt(100).input(gem, Sapphire).output(nugget, Aluminium, 3)
                .blastFurnaceTemp(1200).buildAndRegister();

        // Tempered Glass
        BLAST_RECIPES.recipeBuilder()
                .input(block, Glass)
                .fluidInputs(Oxygen.getFluid(100))
                .outputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(
                        BlockGlassCasing.CasingType.TEMPERED_GLASS))
                .blastFurnaceTemp(1000)
                .duration(200).EUt(VA[MV]).buildAndRegister();

        registerBlastFurnaceMetallurgyRecipes();
    }

    private static void registerBlastFurnaceMetallurgyRecipes() {
        createSulfurDioxideRecipe(Stibnite, AntimonyTrioxide, 1500);
        createSulfurDioxideRecipe(Sphalerite, Zincite, 1000);
        createSulfurDioxideRecipe(Pyrite, BandedIron, 2000);
        createSulfurDioxideRecipe(Pentlandite, Garnierite, 1000);

        BLAST_RECIPES.recipeBuilder().duration(120).EUt(VA[MV]).blastFurnaceTemp(1200)
                .input(dust, Tetrahedrite)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, CupricOxide)
                .output(dustTiny, AntimonyTrioxide, 3)
                .fluidOutputs(SulfurDioxide.getFluid(2000))
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(120).EUt(VA[MV]).blastFurnaceTemp(1200)
                .input(dust, Cobaltite)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, CobaltOxide)
                .output(dust, ArsenicTrioxide)
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(120).EUt(VA[MV]).blastFurnaceTemp(1200)
                .input(dust, Galena)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, Massicot)
                .output(nugget, Silver, 6)
                .fluidOutputs(SulfurDioxide.getFluid(1000))
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(120).EUt(VA[MV]).blastFurnaceTemp(1200)
                .input(dust, Chalcopyrite)
                .input(dust, SiliconDioxide)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, CupricOxide)
                .output(dust, Ferrosilite)
                .fluidOutputs(SulfurDioxide.getFluid(2000))
                .buildAndRegister();

        BLAST_RECIPES.recipeBuilder().duration(240).EUt(VA[MV]).blastFurnaceTemp(2273)
                .input(dust, SiliconDioxide, 3)
                .input(dust, Carbon, 2)
                .output(ingotHot, Silicon)
                .chancedOutput(dust, Ash, "1/9", 0)
                .fluidOutputs(CarbonMonoxide.getFluid(2000))
                .buildAndRegister();
    }

    private static void createSulfurDioxideRecipe(Material inputMaterial, Material outputMaterial,
                                                  int sulfurDioxideAmount) {
        BLAST_RECIPES.recipeBuilder().duration(120).EUt(VA[MV]).blastFurnaceTemp(1200)
                .input(dust, inputMaterial)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, outputMaterial)
                .chancedOutput(dust, Ash, "1/9", 0)
                .fluidOutputs(SulfurDioxide.getFluid(sulfurDioxideAmount))
                .buildAndRegister();
    }

    private static void registerDecompositionRecipes() {
        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(STICKY_RESIN.getStackForm())
                .output(dust, RawRubber, 3)
                .duration(150).EUt(2)
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .inputs(new ItemStack(MetaBlocks.RUBBER_LEAVES, 16))
                .output(dust, RawRubber)
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .inputs(new ItemStack(MetaBlocks.RUBBER_LOG))
                .output(dust, RawRubber)
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(300).EUt(2)
                .inputs(new ItemStack(MetaBlocks.RUBBER_SAPLING))
                .output(dust, RawRubber)
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(150).EUt(2)
                .inputs(new ItemStack(Items.SLIME_BALL))
                .output(dust, RawRubber, 2)
                .buildAndRegister();

        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).input("treeSapling", 8).output(PLANT_BALL)
                .buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Items.WHEAT, 8)).output(PLANT_BALL)
                .buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Items.POTATO, 8))
                .output(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Items.CARROT, 8))
                .output(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Blocks.CACTUS, 8))
                .output(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Items.REEDS, 8)).output(PLANT_BALL)
                .buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Blocks.BROWN_MUSHROOM, 8))
                .output(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Blocks.RED_MUSHROOM, 8))
                .output(PLANT_BALL).buildAndRegister();
        COMPRESSOR_RECIPES.recipeBuilder().duration(300).EUt(2).inputs(new ItemStack(Items.BEETROOT, 8))
                .output(PLANT_BALL).buildAndRegister();
    }

    private static void registerRecyclingRecipes() {
        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, Endstone)
                .output(dust, Endstone)
                .chancedOutput(dust, Tungstate, 130, 30)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, Netherrack)
                .output(dust, Netherrack)
                .chancedOutput(nugget, Gold, 500, 120)
                .buildAndRegister();

        if (!OreDictionary.getOres("stoneSoapstone").isEmpty())
            MACERATOR_RECIPES.recipeBuilder()
                    .input(stone, Soapstone)
                    .output(dustImpure, Talc)
                    .chancedOutput(dust, Chromite, "1/90", 30)
                    .buildAndRegister();

        if (!OreDictionary.getOres("stoneRedrock").isEmpty())
            MACERATOR_RECIPES.recipeBuilder()
                    .input(stone, Redrock)
                    .output(dust, Redrock)
                    .chancedOutput(dust, Redrock, 1000, 380)
                    .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, Marble)
                .output(dust, Marble)
                .chancedOutput(dust, Marble, 1000, 380)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, Basalt)
                .output(dust, Basalt)
                .chancedOutput(dust, Basalt, 1000, 380)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, GraniteBlack)
                .output(dust, GraniteBlack)
                .chancedOutput(dust, Thorium, 100, 40)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, GraniteRed)
                .output(dust, GraniteRed)
                .chancedOutput(dust, Uranium, 10, 5)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, Andesite)
                .output(dust, Andesite)
                .chancedOutput(dust, Stone, 10, 5)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, Diorite)
                .output(dust, Diorite)
                .chancedOutput(dust, Stone, 10, 5)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, Granite)
                .output(dust, Granite)
                .chancedOutput(dust, Stone, 10, 5)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.PORKCHOP))
                .output(dust, Meat)
                .chancedOutput(dust, Meat, 5000, 0)
                .output(dustTiny, Bone)
                .duration(102).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.FISH, 1, GTValues.W))
                .output(dust, Meat)
                .chancedOutput(dust, Meat, 5000, 0)
                .output(dustTiny, Bone)
                .duration(102).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.CHICKEN))
                .output(dust, Meat)
                .output(dustTiny, Bone)
                .duration(102).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.BEEF))
                .output(dust, Meat)
                .chancedOutput(dust, Meat, 5000, 0)
                .output(dustTiny, Bone)
                .duration(102).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.RABBIT))
                .output(dust, Meat)
                .chancedOutput(dust, Meat, 5000, 0)
                .output(dustTiny, Bone)
                .duration(102).buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.MUTTON))
                .output(dust, Meat)
                .output(dustTiny, Bone)
                .duration(102).buildAndRegister();
    }

    private static void registerFluidRecipes() {
        FLUID_HEATER_RECIPES.recipeBuilder().duration(32).EUt(4)
                .fluidInputs(Ice.getFluid(L))
                .circuitMeta(1)
                .fluidOutputs(Water.getFluid(L)).buildAndRegister();

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                .fluidInputs(Toluene.getFluid(100))
                .notConsumable(SHAPE_MOLD_BALL)
                .output(GELLED_TOLUENE)
                .duration(100).EUt(16).buildAndRegister();

        for (int i = 0; i < Materials.CHEMICAL_DYES.length; i++) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(GTValues.L / 2))
                    .notConsumable(MetaItems.SHAPE_MOLD_BALL.getStackForm())
                    .outputs(MetaItems.DYE_ONLY_ITEMS[i].getStackForm())
                    .duration(100).EUt(16).buildAndRegister();
        }

        FLUID_HEATER_RECIPES.recipeBuilder().duration(30).EUt(VA[LV]).fluidInputs(Water.getFluid(6)).circuitMeta(1)
                .fluidOutputs(Steam.getFluid(960)).buildAndRegister();
        FLUID_HEATER_RECIPES.recipeBuilder().duration(30).EUt(VA[LV]).fluidInputs(DistilledWater.getFluid(6))
                .circuitMeta(1).fluidOutputs(Steam.getFluid(960)).buildAndRegister();
    }

    private static void registerSmoothRecipe(List<ItemStack> roughStack, List<ItemStack> smoothStack) {
        for (int i = 0; i < roughStack.size(); i++) {
            ModHandler.addSmeltingRecipe(roughStack.get(i), smoothStack.get(i), 0.1f);

            EXTRUDER_RECIPES.recipeBuilder()
                    .inputs(roughStack.get(i))
                    .notConsumable(SHAPE_EXTRUDER_BLOCK.getStackForm())
                    .outputs(smoothStack.get(i))
                    .duration(24).EUt(8).buildAndRegister();
        }
    }

    private static void registerCobbleRecipe(List<ItemStack> smoothStack, List<ItemStack> cobbleStack) {
        for (int i = 0; i < smoothStack.size(); i++) {
            FORGE_HAMMER_RECIPES.recipeBuilder()
                    .inputs(smoothStack.get(i))
                    .outputs(cobbleStack.get(i))
                    .duration(12).EUt(4).buildAndRegister();
        }
    }

    private static void registerBricksRecipe(List<ItemStack> polishedStack, List<ItemStack> brickStack,
                                             MarkerMaterial color) {
        for (int i = 0; i < polishedStack.size(); i++) {
            LASER_ENGRAVER_RECIPES.recipeBuilder()
                    .inputs(polishedStack.get(i))
                    .notConsumable(craftingLens, color)
                    .outputs(brickStack.get(i))
                    .duration(50).EUt(16).buildAndRegister();
        }
    }

    private static void registerMossRecipe(List<ItemStack> regularStack, List<ItemStack> mossStack) {
        for (int i = 0; i < regularStack.size(); i++) {
            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputs(regularStack.get(i))
                    .fluidInputs(Water.getFluid(100))
                    .outputs(mossStack.get(i))
                    .duration(50).EUt(16).buildAndRegister();
        }
    }

    private static void registerNBTRemoval() {
        for (MetaTileEntityQuantumChest chest : MetaTileEntities.QUANTUM_CHEST)
            if (chest != null) {
                ModHandler.addShapelessNBTClearingRecipe("quantum_chest_nbt_" + chest.getTier() + chest.getMetaName(),
                        chest.getStackForm(), chest.getStackForm());
            }

        for (MetaTileEntityQuantumTank tank : MetaTileEntities.QUANTUM_TANK)
            if (tank != null) {
                ModHandler.addShapelessNBTClearingRecipe("quantum_tank_nbt_" + tank.getTier() + tank.getMetaName(),
                        tank.getStackForm(), tank.getStackForm());
            }

        // Drums
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_wood", MetaTileEntities.WOODEN_DRUM.getStackForm(),
                MetaTileEntities.WOODEN_DRUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_bronze", MetaTileEntities.BRONZE_DRUM.getStackForm(),
                MetaTileEntities.BRONZE_DRUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_steel", MetaTileEntities.STEEL_DRUM.getStackForm(),
                MetaTileEntities.STEEL_DRUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_aluminium", MetaTileEntities.ALUMINIUM_DRUM.getStackForm(),
                MetaTileEntities.ALUMINIUM_DRUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_stainless_steel",
                MetaTileEntities.STAINLESS_STEEL_DRUM.getStackForm(),
                MetaTileEntities.STAINLESS_STEEL_DRUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_titanium", MetaTileEntities.TITANIUM_DRUM.getStackForm(),
                MetaTileEntities.TITANIUM_DRUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_tungstensteel",
                MetaTileEntities.TUNGSTENSTEEL_DRUM.getStackForm(), MetaTileEntities.TUNGSTENSTEEL_DRUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_gold", MetaTileEntities.GOLD_DRUM.getStackForm(),
                MetaTileEntities.GOLD_DRUM.getStackForm());

        // Cells
        ModHandler.addShapedNBTClearingRecipe("cell_nbt_regular", MetaItems.FLUID_CELL.getStackForm(), " C", "  ", 'C',
                MetaItems.FLUID_CELL.getStackForm());
        ModHandler.addShapedNBTClearingRecipe("cell_nbt_universal", MetaItems.FLUID_CELL_UNIVERSAL.getStackForm(), " C",
                "  ", 'C', MetaItems.FLUID_CELL_UNIVERSAL.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_steel", MetaItems.FLUID_CELL_LARGE_STEEL.getStackForm(),
                MetaItems.FLUID_CELL_LARGE_STEEL.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_aluminium",
                MetaItems.FLUID_CELL_LARGE_ALUMINIUM.getStackForm(),
                MetaItems.FLUID_CELL_LARGE_ALUMINIUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_stainless_steel",
                MetaItems.FLUID_CELL_LARGE_STAINLESS_STEEL.getStackForm(),
                MetaItems.FLUID_CELL_LARGE_STAINLESS_STEEL.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_titanium",
                MetaItems.FLUID_CELL_LARGE_TITANIUM.getStackForm(), MetaItems.FLUID_CELL_LARGE_TITANIUM.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_tungstensteel",
                MetaItems.FLUID_CELL_LARGE_TUNGSTEN_STEEL.getStackForm(),
                MetaItems.FLUID_CELL_LARGE_TUNGSTEN_STEEL.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("cell_vial_nbt", MetaItems.FLUID_CELL_GLASS_VIAL.getStackForm(),
                MetaItems.FLUID_CELL_GLASS_VIAL.getStackForm());

        // Data Items
        ModHandler.addShapelessNBTClearingRecipe("data_stick_nbt", TOOL_DATA_STICK.getStackForm(),
                TOOL_DATA_STICK.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("data_orb_nbt", TOOL_DATA_ORB.getStackForm(),
                TOOL_DATA_ORB.getStackForm());
        ModHandler.addShapelessNBTClearingRecipe("data_module_nbt", TOOL_DATA_MODULE.getStackForm(),
                TOOL_DATA_MODULE.getStackForm());

        // Filters
        ModHandler.addShapelessNBTClearingRecipe("clear_item_filter",
                ITEM_FILTER.getStackForm(), ITEM_FILTER);
        ModHandler.addShapelessNBTClearingRecipe("clear_fluid_filter",
                FLUID_FILTER.getStackForm(), FLUID_FILTER);
        ModHandler.addShapelessNBTClearingRecipe("clear_smart_filter",
                SMART_FILTER.getStackForm(), SMART_FILTER);
        ModHandler.addShapelessNBTClearingRecipe("clear_oredict_filter",
                ORE_DICTIONARY_FILTER.getStackForm(), ORE_DICTIONARY_FILTER);

        // Jetpacks
        ModHandler.addShapelessRecipe("fluid_jetpack_clear", SEMIFLUID_JETPACK.getStackForm(),
                SEMIFLUID_JETPACK.getStackForm());

        // ClipBoard
        ModHandler.addShapelessNBTClearingRecipe("clipboard_nbt", CLIPBOARD.getStackForm(), CLIPBOARD.getStackForm());
    }

    private static void ConvertHatchToHatch() {
        for (int i = 0; i < FLUID_IMPORT_HATCH.length; i++) {
            if (FLUID_IMPORT_HATCH[i] != null && FLUID_EXPORT_HATCH[i] != null) {

                ModHandler.addShapedRecipe("fluid_hatch_output_to_input_" + FLUID_IMPORT_HATCH[i].getTier(),
                        FLUID_IMPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', FLUID_EXPORT_HATCH[i].getStackForm());
                ModHandler.addShapedRecipe("fluid_hatch_input_to_output_" + FLUID_EXPORT_HATCH[i].getTier(),
                        FLUID_EXPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', FLUID_IMPORT_HATCH[i].getStackForm());
            }
        }
        for (int i = 0; i < ITEM_IMPORT_BUS.length; i++) {
            if (ITEM_IMPORT_BUS[i] != null && ITEM_EXPORT_BUS[i] != null) {

                ModHandler.addShapedRecipe("item_bus_output_to_input_" + ITEM_IMPORT_BUS[i].getTier(),
                        ITEM_IMPORT_BUS[i].getStackForm(),
                        "d", "B", 'B', ITEM_EXPORT_BUS[i].getStackForm());
                ModHandler.addShapedRecipe("item_bus_input_to_output_" + ITEM_EXPORT_BUS[i].getTier(),
                        ITEM_EXPORT_BUS[i].getStackForm(),
                        "d", "B", 'B', ITEM_IMPORT_BUS[i].getStackForm());
            }
        }

        for (int i = 0; i < QUADRUPLE_IMPORT_HATCH.length; i++) {
            if (QUADRUPLE_IMPORT_HATCH[i] != null && QUADRUPLE_EXPORT_HATCH[i] != null) {
                ModHandler.addShapedRecipe(
                        "quadruple_fluid_hatch_output_to_input_" + QUADRUPLE_IMPORT_HATCH[i].getTier(),
                        QUADRUPLE_IMPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', QUADRUPLE_EXPORT_HATCH[i].getStackForm());
                ModHandler.addShapedRecipe(
                        "quadruple_fluid_hatch_input_to_output_" + QUADRUPLE_EXPORT_HATCH[i].getTier(),
                        QUADRUPLE_EXPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', QUADRUPLE_IMPORT_HATCH[i].getStackForm());
            }
        }

        for (int i = 0; i < NONUPLE_IMPORT_HATCH.length; i++) {
            if (NONUPLE_IMPORT_HATCH[i] != null && NONUPLE_EXPORT_HATCH[i] != null) {
                ModHandler.addShapedRecipe("nonuple_fluid_hatch_output_to_input_" + NONUPLE_IMPORT_HATCH[i].getTier(),
                        NONUPLE_IMPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', NONUPLE_EXPORT_HATCH[i].getStackForm());
                ModHandler.addShapedRecipe("nonuple_fluid_hatch_input_to_output_" + NONUPLE_EXPORT_HATCH[i].getTier(),
                        NONUPLE_EXPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', NONUPLE_IMPORT_HATCH[i].getStackForm());
            }
        }

        if (Mods.AppliedEnergistics2.isModLoaded()) {
            ModHandler.addShapedRecipe("me_fluid_hatch_output_to_input", FLUID_IMPORT_HATCH_ME.getStackForm(), "d", "B",
                    'B', FLUID_EXPORT_HATCH_ME.getStackForm());
            ModHandler.addShapedRecipe("me_fluid_hatch_input_to_output", FLUID_EXPORT_HATCH_ME.getStackForm(), "d", "B",
                    'B', FLUID_IMPORT_HATCH_ME.getStackForm());
            ModHandler.addShapedRecipe("me_item_bus_output_to_input", ITEM_IMPORT_BUS_ME.getStackForm(), "d", "B", 'B',
                    ITEM_EXPORT_BUS_ME.getStackForm());
            ModHandler.addShapedRecipe("me_item_bus_input_to_output", ITEM_EXPORT_BUS_ME.getStackForm(), "d", "B", 'B',
                    ITEM_IMPORT_BUS_ME.getStackForm());
        }

        if (STEAM_EXPORT_BUS != null && STEAM_IMPORT_BUS != null) {
            // Steam
            ModHandler.addShapedRecipe("steam_bus_output_to_input_" + STEAM_EXPORT_BUS.getTier(),
                    STEAM_EXPORT_BUS.getStackForm(),
                    "d", "B", 'B', STEAM_IMPORT_BUS.getStackForm());
            ModHandler.addShapedRecipe("steam_bus_input_to_output_" + STEAM_IMPORT_BUS.getTier(),
                    STEAM_IMPORT_BUS.getStackForm(),
                    "d", "B", 'B', STEAM_EXPORT_BUS.getStackForm());
        }
    }
}
