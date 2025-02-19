package gregtech.loaders.recipe;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockCleanroomCasing;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.crafting.FacadeRecipe;
import gregtech.common.items.MetaItems;
import gregtech.loaders.recipe.handlers.ToolRecipeHandler;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreIngredient;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.common.items.MetaItems.*;

public class CraftingRecipeLoader {

    public static void init() {
        loadCraftingRecipes();
        GTLog.logger.info(
                "Modifying vanilla recipes according to config. DON'T BE SCARED OF FML's WARNING ABOUT DANGEROUS ALTERNATIVE PREFIX.");
        VanillaOverrideRecipes.init();
        VanillaStandardRecipes.init();
    }

    private static void loadCraftingRecipes() {
        registerFacadeRecipe(Materials.Iron, 4);

        ToolRecipeHandler.registerPowerUnitRecipes();
        ToolRecipeHandler.registerCustomToolRecipes();

        ModHandler.addShapelessRecipe("integrated_circuit", IntCircuitIngredient.getIntegratedCircuit(0),
                new UnificationEntry(OrePrefix.circuit, Tier.LV));

        ModHandler.addShapedRecipe("item_filter", ITEM_FILTER.getStackForm(), "XXX", "XYX", "XXX", 'X',
                new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y',
                new UnificationEntry(OrePrefix.plate, Materials.Steel));
        ModHandler.addShapedRecipe("fluid_filter_lapis", FLUID_FILTER.getStackForm(), "XXX", "XYX", "XXX", 'X',
                new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y',
                new UnificationEntry(OrePrefix.plate, Materials.Lapis));
        ModHandler.addShapedRecipe("fluid_filter_lazurite", FLUID_FILTER.getStackForm(), "XXX", "XYX", "XXX", 'X',
                new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y',
                new UnificationEntry(OrePrefix.plate, Materials.Lazurite));
        ModHandler.addShapedRecipe("fluid_filter_sodalite", FLUID_FILTER.getStackForm(), "XXX", "XYX", "XXX", 'X',
                new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y',
                new UnificationEntry(OrePrefix.plate, Materials.Sodalite));

        ModHandler.addShapedRecipe("ore_dictionary_filter_olivine", ORE_DICTIONARY_FILTER.getStackForm(), "XXX", "XYX",
                "XXX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y',
                new UnificationEntry(OrePrefix.plate, Materials.Olivine));
        ModHandler.addShapedRecipe("ore_dictionary_filter_emerald", ORE_DICTIONARY_FILTER.getStackForm(), "XXX", "XYX",
                "XXX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y',
                new UnificationEntry(OrePrefix.plate, Materials.Emerald));

        ModHandler.addShapedRecipe("smart_item_filter_olivine", SMART_FILTER.getStackForm(), "XEX", "XCX", "XEX", 'X',
                new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'C',
                new UnificationEntry(OrePrefix.circuit, Tier.LV), 'E',
                new UnificationEntry(OrePrefix.plate, Materials.Olivine));
        ModHandler.addShapedRecipe("smart_item_filter_emerald", SMART_FILTER.getStackForm(), "XEX", "XCX", "XEX", 'X',
                new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'C',
                new UnificationEntry(OrePrefix.circuit, Tier.LV), 'E',
                new UnificationEntry(OrePrefix.plate, Materials.Emerald));

        ModHandler.addShapedRecipe("plank_to_wooden_shape", WOODEN_FORM_EMPTY.getStackForm(), "   ", " X ", "s  ", 'X',
                new UnificationEntry(OrePrefix.plank, Materials.Wood));
        ModHandler.addShapedRecipe("wooden_shape_brick", WOODEN_FORM_BRICK.getStackForm(), "k ", " X", 'X',
                WOODEN_FORM_EMPTY.getStackForm());

        if (ConfigHolder.recipes.harderBrickRecipes) {
            ModHandler.addShapelessRecipe("compressed_clay", COMPRESSED_CLAY.getStackForm(),
                    WOODEN_FORM_BRICK.getStackForm(), new ItemStack(Items.CLAY_BALL));
            ModHandler.addSmeltingRecipe(COMPRESSED_CLAY.getStackForm(), new ItemStack(Items.BRICK), 0.3f);
        }

        ModHandler.addShapedRecipe("compressed_coke_clay", COMPRESSED_COKE_CLAY.getStackForm(3), "XXX", "SYS", "SSS",
                'Y', WOODEN_FORM_BRICK.getStackForm(), 'X', new ItemStack(Items.CLAY_BALL), 'S', "sand");
        ModHandler.addShapelessRecipe("fireclay_dust", OreDictUnifier.get(OrePrefix.dust, Materials.Fireclay, 2),
                new UnificationEntry(OrePrefix.dust, Materials.Brick),
                new UnificationEntry(OrePrefix.dust, Materials.Clay));
        ModHandler.addSmeltingRecipe(COMPRESSED_COKE_CLAY.getStackForm(), COKE_OVEN_BRICK.getStackForm(), 0.3f);
        ModHandler.addSmeltingRecipe(COMPRESSED_FIRECLAY.getStackForm(), FIRECLAY_BRICK.getStackForm(), 0.3f);

        ModHandler.addSmeltingRecipe(new UnificationEntry(OrePrefix.nugget, Materials.Iron),
                OreDictUnifier.get(OrePrefix.nugget, Materials.WroughtIron));

        ModHandler.addShapedRecipe("clipboard", CLIPBOARD.getStackForm(), " Sd", "BWR", "PPP", 'P', Items.PAPER, 'R',
                new UnificationEntry(OrePrefix.springSmall, Iron), 'B', new UnificationEntry(OrePrefix.bolt, Iron), 'S',
                new UnificationEntry(OrePrefix.screw, Iron), 'W', new UnificationEntry(OrePrefix.plate, Wood));

        ModHandler.addShapedRecipe("rubber_ring", OreDictUnifier.get(OrePrefix.ring, Materials.Rubber), "k", "X", 'X',
                new UnificationEntry(OrePrefix.plate, Materials.Rubber));
        ModHandler.addShapedRecipe("silicone_rubber_ring", OreDictUnifier.get(OrePrefix.ring, Materials.SiliconeRubber),
                "k", "P", 'P', OreDictUnifier.get(OrePrefix.plate, Materials.SiliconeRubber));
        ModHandler.addShapedRecipe("styrene_rubber_ring",
                OreDictUnifier.get(OrePrefix.ring, Materials.StyreneButadieneRubber), "k", "P", 'P',
                OreDictUnifier.get(OrePrefix.plate, Materials.StyreneButadieneRubber));

        ModHandler.addShapelessRecipe("iron_magnetic_stick",
                OreDictUnifier.get(OrePrefix.stick, Materials.IronMagnetic),
                new UnificationEntry(OrePrefix.stick, Materials.Iron),
                new UnificationEntry(OrePrefix.dust, Materials.Redstone),
                new UnificationEntry(OrePrefix.dust, Materials.Redstone),
                new UnificationEntry(OrePrefix.dust, Materials.Redstone),
                new UnificationEntry(OrePrefix.dust, Materials.Redstone));

        ModHandler.addShapedRecipe("component_grinder_diamond", COMPONENT_GRINDER_DIAMOND.getStackForm(), "XSX", "SDS",
                "XSX", 'X', new UnificationEntry(OrePrefix.dust, Materials.Diamond), 'S',
                new UnificationEntry(OrePrefix.plateDouble, Materials.Steel), 'D',
                new UnificationEntry(OrePrefix.gem, Materials.Diamond));
        ModHandler.addShapedRecipe("component_grinder_tungsten", COMPONENT_GRINDER_TUNGSTEN.getStackForm(), "WSW",
                "SDS", "WSW", 'W', new UnificationEntry(OrePrefix.plate, Materials.Tungsten), 'S',
                new UnificationEntry(OrePrefix.plateDouble, Materials.VanadiumSteel), 'D',
                new UnificationEntry(OrePrefix.gem, Materials.Diamond));

        ModHandler.addShapedRecipe("minecart_wheels_iron", IRON_MINECART_WHEELS.getStackForm(), " h ", "RSR", " w ",
                'R', new UnificationEntry(OrePrefix.ring, Materials.Iron), 'S',
                new UnificationEntry(OrePrefix.stick, Materials.Iron));
        ModHandler.addShapedRecipe("minecart_wheels_steel", STEEL_MINECART_WHEELS.getStackForm(), " h ", "RSR", " w ",
                'R', new UnificationEntry(OrePrefix.ring, Materials.Steel), 'S',
                new UnificationEntry(OrePrefix.stick, Materials.Steel));

        ModHandler.addShapedRecipe("nano_saber", NANO_SABER.getStackForm(), "PIC", "PIC", "XEX", 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Platinum), 'I',
                new UnificationEntry(OrePrefix.plate, Ruridit), 'C', CARBON_FIBER_PLATE.getStackForm(), 'X',
                new UnificationEntry(OrePrefix.circuit, Tier.EV), 'E', ENERGIUM_CRYSTAL.getStackForm());

        ModHandler.addShapedRecipe("solar_panel_basic", COVER_SOLAR_PANEL.getStackForm(), "WGW", "CPC", 'W',
                SILICON_WAFER.getStackForm(), 'G', "paneGlass", 'C', new UnificationEntry(OrePrefix.circuit, Tier.LV),
                'P', CARBON_FIBER_PLATE.getStackForm());
        ModHandler.addShapedRecipe("solar_panel_ulv", COVER_SOLAR_PANEL_ULV.getStackForm(), "WGW", "CAC", "P P", 'W',
                PHOSPHORUS_WAFER.getStackForm(), 'G', "paneGlass", 'C',
                new UnificationEntry(OrePrefix.circuit, Tier.HV), 'P',
                OreDictUnifier.get(OrePrefix.plate, GalliumArsenide), 'A',
                OreDictUnifier.get(OrePrefix.wireGtQuadruple, Graphene));
        ModHandler.addShapedRecipe("solar_panel_lv", COVER_SOLAR_PANEL_LV.getStackForm(), "WGW", "CAC", "P P", 'W',
                NAQUADAH_WAFER.getStackForm(), 'G', MetaBlocks.TRANSPARENT_CASING.getItemVariant(
                        BlockGlassCasing.CasingType.TEMPERED_GLASS),
                'C', new UnificationEntry(OrePrefix.circuit, Tier.LuV), 'P',
                OreDictUnifier.get(OrePrefix.plate, IndiumGalliumPhosphide), 'A',
                OreDictUnifier.get(OrePrefix.wireGtHex, Graphene));

        ModHandler.addShapedRecipe("universal_fluid_cell", FLUID_CELL_UNIVERSAL.getStackForm(), "C ", "  ", 'C',
                FLUID_CELL);
        ModHandler.addShapedRecipe("universal_fluid_cell_revert", FLUID_CELL.getStackForm(), "C ", "  ", 'C',
                FLUID_CELL_UNIVERSAL);

        ModHandler.addShapedRecipe("blacklight", BLACKLIGHT.getStackForm(), "SPS", "GRG", "CPK", 'S',
                new UnificationEntry(OrePrefix.screw, TungstenCarbide), 'P',
                new UnificationEntry(OrePrefix.plate, TungstenCarbide), 'G',
                MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.LAMINATED_GLASS), 'R',
                new UnificationEntry(OrePrefix.spring, Europium), 'C', new UnificationEntry(OrePrefix.circuit, Tier.IV),
                'K', new UnificationEntry(OrePrefix.cableGtSingle, Platinum));

        ModHandler.addShapedRecipe(true, "filter_casing",
                MetaBlocks.CLEANROOM_CASING.getItemVariant(BlockCleanroomCasing.CasingType.FILTER_CASING,
                        ConfigHolder.recipes.casingsPerCraft),
                "BBB", "III", "MFR", 'B', new ItemStack(Blocks.IRON_BARS), 'I', ITEM_FILTER.getStackForm(), 'M',
                ELECTRIC_MOTOR_MV.getStackForm(), 'F', new UnificationEntry(OrePrefix.frameGt, Steel), 'R',
                new UnificationEntry(OrePrefix.rotor, Steel));
        ModHandler.addShapedRecipe(true, "filter_casing_sterile",
                MetaBlocks.CLEANROOM_CASING.getItemVariant(BlockCleanroomCasing.CasingType.FILTER_CASING_STERILE,
                        ConfigHolder.recipes.casingsPerCraft),
                "BEB", "ISI", "MFR", 'B', new UnificationEntry(OrePrefix.pipeLargeFluid, Polybenzimidazole), 'E',
                EMITTER_ZPM.getStackForm(), 'I', ITEM_FILTER.getStackForm(), 'S', BLACKLIGHT.getStackForm(), 'M',
                ELECTRIC_MOTOR_ZPM.getStackForm(), 'F', new UnificationEntry(OrePrefix.frameGt, Tritanium), 'R',
                new UnificationEntry(OrePrefix.rotor, NaquadahAlloy));

        ///////////////////////////////////////////////////
        // Shapes and Molds //
        ///////////////////////////////////////////////////
        ModHandler.addShapedRecipe("shape_empty", SHAPE_EMPTY.getStackForm(), "hf", "PP", "PP", 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Steel));

        ModHandler.addShapedRecipe("shape_extruder_bottle", SHAPE_EXTRUDER_BOTTLE.getStackForm(), "  x", " S ", "   ",
                'S', SHAPE_EXTRUDER_RING.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_gear", SHAPE_EXTRUDER_GEAR.getStackForm(), "x  ", " S ", "   ", 'S',
                SHAPE_EXTRUDER_RING.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_block", SHAPE_EXTRUDER_BLOCK.getStackForm(), "x  ", " S ", "   ",
                'S', SHAPE_EXTRUDER_INGOT.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_pipe_huge", SHAPE_EXTRUDER_PIPE_HUGE.getStackForm(), "   ", " S ",
                "  x", 'S', SHAPE_EXTRUDER_BOLT.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_pipe_large", SHAPE_EXTRUDER_PIPE_LARGE.getStackForm(), "   ", " Sx",
                "   ", 'S', SHAPE_EXTRUDER_BOLT.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_pipe_normal", SHAPE_EXTRUDER_PIPE_NORMAL.getStackForm(), "  x",
                " S ", "   ", 'S', SHAPE_EXTRUDER_BOLT.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_pipe_small", SHAPE_EXTRUDER_PIPE_SMALL.getStackForm(), " x ", " S ",
                "   ", 'S', SHAPE_EXTRUDER_BOLT.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_pipe_tiny", SHAPE_EXTRUDER_PIPE_TINY.getStackForm(), "x  ", " S ",
                "   ", 'S', SHAPE_EXTRUDER_BOLT.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_wire", SHAPE_EXTRUDER_WIRE.getStackForm(), " x ", " S ", "   ", 'S',
                SHAPE_EXTRUDER_ROD.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_ingot", SHAPE_EXTRUDER_INGOT.getStackForm(), "x  ", " S ", "   ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_cell", SHAPE_EXTRUDER_CELL.getStackForm(), "   ", " Sx", "   ", 'S',
                SHAPE_EXTRUDER_RING.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_ring", SHAPE_EXTRUDER_RING.getStackForm(), "   ", " S ", " x ", 'S',
                SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_bolt", SHAPE_EXTRUDER_BOLT.getStackForm(), "x  ", " S ", "   ", 'S',
                SHAPE_EXTRUDER_ROD.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_rod", SHAPE_EXTRUDER_ROD.getStackForm(), "   ", " Sx", "   ", 'S',
                SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_rod_long", SHAPE_EXTRUDER_ROD_LONG.getStackForm(), "  x", " S ",
                "   ", 'S', SHAPE_EXTRUDER_ROD.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_plate", SHAPE_EXTRUDER_PLATE.getStackForm(), "x  ", " S ", "   ",
                'S', SHAPE_EXTRUDER_FOIL.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_gear_small", SHAPE_EXTRUDER_GEAR_SMALL.getStackForm(), " x ", " S ",
                "   ", 'S', SHAPE_EXTRUDER_RING.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_foil", SHAPE_EXTRUDER_FOIL.getStackForm(), "   ", " S ", "  x", 'S',
                SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_extruder_rotor", SHAPE_EXTRUDER_ROTOR.getStackForm(), "   ", " S ", "x  ",
                'S', SHAPE_EMPTY.getStackForm());

        ModHandler.addShapedRecipe("shape_mold_rotor", SHAPE_MOLD_ROTOR.getStackForm(),
                "  h",
                " S ",
                "   ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_gear_small", SHAPE_MOLD_GEAR_SMALL.getStackForm(),
                "   ",
                "   ",
                "h S",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_name", SHAPE_MOLD_NAME.getStackForm(),
                "  S",
                "   ",
                "h  ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_anvil", SHAPE_MOLD_ANVIL.getStackForm(),
                "  S",
                "   ",
                " h ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_cylinder", SHAPE_MOLD_CYLINDER.getStackForm(),
                "  S",
                "   ",
                "  h",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_nugget", SHAPE_MOLD_NUGGET.getStackForm(),
                "S h",
                "   ",
                "   ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_block", SHAPE_MOLD_BLOCK.getStackForm(),
                "   ",
                "hS ",
                "   ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_ball", SHAPE_MOLD_BALL.getStackForm(),
                "   ",
                " S ",
                "h  ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_ingot", SHAPE_MOLD_INGOT.getStackForm(),
                "   ",
                " S ",
                " h ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_bottle", SHAPE_MOLD_BOTTLE.getStackForm(),
                "   ",
                " S ",
                "  h",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_credit", SHAPE_MOLD_CREDIT.getStackForm(),
                "h  ",
                " S ",
                "   ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_gear", SHAPE_MOLD_GEAR.getStackForm(),
                "   ",
                " Sh",
                "   ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_plate", SHAPE_MOLD_PLATE.getStackForm(),
                " h ",
                " S ",
                "   ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_ring", SHAPE_MOLD_RING.getStackForm(),
                "   ",
                " h ",
                " S ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_bolt", SHAPE_MOLD_BOLT.getStackForm(),
                "h  ",
                "   ",
                "S  ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_rod", SHAPE_MOLD_ROD.getStackForm(),
                " h ",
                "   ",
                "S  ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_rod_long", SHAPE_MOLD_ROD_LONG.getStackForm(),
                "  h",
                "   ",
                "S  ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_screw", SHAPE_MOLD_SCREW.getStackForm(),
                "   ",
                "h  ",
                "S  ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_round", SHAPE_MOLD_ROUND.getStackForm(),
                "   ",
                " h ",
                "S  ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_turbine_blade", SHAPE_MOLD_TURBINE_BLADE.getStackForm(),
                "   ",
                "  h",
                "S  ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_pipe_tiny", SHAPE_MOLD_PIPE_TINY.getStackForm(),
                "   ",
                "   ",
                "Sh ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_pipe_small", SHAPE_MOLD_PIPE_SMALL.getStackForm(),
                "   ",
                "   ",
                "S h",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_pipe_normal", SHAPE_MOLD_PIPE_NORMAL.getStackForm(),
                "   ",
                "   ",
                " Sh",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_pipe_large", SHAPE_MOLD_PIPE_LARGE.getStackForm(),
                "   ",
                "   ",
                "hS ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_pipe_huge", SHAPE_MOLD_PIPE_HUGE.getStackForm(),
                "   ",
                "  h",
                " S ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_bun", SHAPE_MOLD_BUN.getStackForm(),
                "   ",
                "h  ",
                " S ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_bread", SHAPE_MOLD_BREAD.getStackForm(),
                "h  ",
                "   ",
                " S ",
                'S', SHAPE_EMPTY.getStackForm());
        ModHandler.addShapedRecipe("shape_mold_baguette", SHAPE_MOLD_BAGUETTE.getStackForm(),
                "   ",
                "  h",
                " S ",
                'S', SHAPE_EMPTY.getStackForm());

        ///////////////////////////////////////////////////
        // Credits //
        ///////////////////////////////////////////////////
        ModHandler.addShapelessRecipe("coin_chocolate", COIN_CHOCOLATE.getStackForm(),
                new UnificationEntry(OrePrefix.dust, Materials.Cocoa),
                new UnificationEntry(OrePrefix.foil, Materials.Gold), new ItemStack(Items.MILK_BUCKET),
                new UnificationEntry(OrePrefix.dust, Materials.Sugar));

        ModHandler.addShapelessRecipe("credit_copper", CREDIT_COPPER.getStackForm(8),
                CREDIT_CUPRONICKEL.getStackForm());
        ModHandler.addShapelessRecipe("credit_cupronickel_alt", CREDIT_CUPRONICKEL.getStackForm(),
                CREDIT_COPPER.getStackForm(), CREDIT_COPPER.getStackForm(), CREDIT_COPPER.getStackForm(),
                CREDIT_COPPER.getStackForm(), CREDIT_COPPER.getStackForm(), CREDIT_COPPER.getStackForm(),
                CREDIT_COPPER.getStackForm(), CREDIT_COPPER.getStackForm());
        ModHandler.addShapelessRecipe("credit_cupronickel", CREDIT_CUPRONICKEL.getStackForm(8),
                CREDIT_SILVER.getStackForm());
        ModHandler.addShapelessRecipe("credit_silver_alt", CREDIT_SILVER.getStackForm(),
                CREDIT_CUPRONICKEL.getStackForm(), CREDIT_CUPRONICKEL.getStackForm(), CREDIT_CUPRONICKEL.getStackForm(),
                CREDIT_CUPRONICKEL.getStackForm(), CREDIT_CUPRONICKEL.getStackForm(), CREDIT_CUPRONICKEL.getStackForm(),
                CREDIT_CUPRONICKEL.getStackForm(), CREDIT_CUPRONICKEL.getStackForm());
        ModHandler.addShapelessRecipe("credit_silver", CREDIT_SILVER.getStackForm(8), CREDIT_GOLD.getStackForm());
        ModHandler.addShapelessRecipe("credit_gold_alt", CREDIT_GOLD.getStackForm(), CREDIT_SILVER.getStackForm(),
                CREDIT_SILVER.getStackForm(), CREDIT_SILVER.getStackForm(), CREDIT_SILVER.getStackForm(),
                CREDIT_SILVER.getStackForm(), CREDIT_SILVER.getStackForm(), CREDIT_SILVER.getStackForm(),
                CREDIT_SILVER.getStackForm());
        ModHandler.addShapelessRecipe("credit_gold", CREDIT_GOLD.getStackForm(8), CREDIT_PLATINUM.getStackForm());
        ModHandler.addShapelessRecipe("credit_platinum_alt", CREDIT_PLATINUM.getStackForm(), CREDIT_GOLD.getStackForm(),
                CREDIT_GOLD.getStackForm(), CREDIT_GOLD.getStackForm(), CREDIT_GOLD.getStackForm(),
                CREDIT_GOLD.getStackForm(), CREDIT_GOLD.getStackForm(), CREDIT_GOLD.getStackForm(),
                CREDIT_GOLD.getStackForm());
        ModHandler.addShapelessRecipe("credit_platinum", CREDIT_PLATINUM.getStackForm(8), CREDIT_OSMIUM.getStackForm());
        ModHandler.addShapelessRecipe("credit_osmium_alt", CREDIT_OSMIUM.getStackForm(), CREDIT_PLATINUM.getStackForm(),
                CREDIT_PLATINUM.getStackForm(), CREDIT_PLATINUM.getStackForm(), CREDIT_PLATINUM.getStackForm(),
                CREDIT_PLATINUM.getStackForm(), CREDIT_PLATINUM.getStackForm(), CREDIT_PLATINUM.getStackForm(),
                CREDIT_PLATINUM.getStackForm());
        ModHandler.addShapelessRecipe("credit_osmium", CREDIT_OSMIUM.getStackForm(8), CREDIT_NAQUADAH.getStackForm());
        ModHandler.addShapelessRecipe("credit_naquadah_alt", CREDIT_NAQUADAH.getStackForm(),
                CREDIT_OSMIUM.getStackForm(), CREDIT_OSMIUM.getStackForm(), CREDIT_OSMIUM.getStackForm(),
                CREDIT_OSMIUM.getStackForm(), CREDIT_OSMIUM.getStackForm(), CREDIT_OSMIUM.getStackForm(),
                CREDIT_OSMIUM.getStackForm(), CREDIT_OSMIUM.getStackForm());
        ModHandler.addShapelessRecipe("credit_naquadah", CREDIT_NAQUADAH.getStackForm(8),
                CREDIT_NEUTRONIUM.getStackForm());
        ModHandler.addShapelessRecipe("credit_darmstadtium", CREDIT_NEUTRONIUM.getStackForm(),
                CREDIT_NAQUADAH.getStackForm(), CREDIT_NAQUADAH.getStackForm(), CREDIT_NAQUADAH.getStackForm(),
                CREDIT_NAQUADAH.getStackForm(), CREDIT_NAQUADAH.getStackForm(), CREDIT_NAQUADAH.getStackForm(),
                CREDIT_NAQUADAH.getStackForm(), CREDIT_NAQUADAH.getStackForm());
        ///////////////////////////////////////////////////
        // Armors //
        ///////////////////////////////////////////////////
        ModHandler.addShapedRecipe("nightvision_goggles", MetaItems.NIGHTVISION_GOGGLES.getStackForm(), "CSC", "RBR",
                "LdL", 'C', new UnificationEntry(OrePrefix.circuit, Tier.ULV), 'S',
                new UnificationEntry(OrePrefix.screw, Steel), 'R', new UnificationEntry(OrePrefix.ring, Rubber), 'B',
                MetaItems.BATTERY_LV_SODIUM, 'L', new UnificationEntry(OrePrefix.lens, Glass));
        ModHandler.addShapedRecipe("fluid_jetpack", MetaItems.SEMIFLUID_JETPACK.getStackForm(), "xCw", "SUS", "RIR",
                'C', new UnificationEntry(OrePrefix.circuit, Tier.LV), 'S',
                MetaItems.FLUID_CELL_LARGE_STEEL.getStackForm(), 'U', MetaItems.ELECTRIC_PUMP_LV.getStackForm(), 'R',
                new UnificationEntry(OrePrefix.rotor, Lead), 'I',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Potin));
        ModHandler.addShapedRecipe("electric_jetpack", MetaItems.ELECTRIC_JETPACK.getStackForm(), "xCd", "TBT", "I I",
                'C', new UnificationEntry(OrePrefix.circuit, Tier.MV), 'T', MetaItems.POWER_THRUSTER.getStackForm(),
                'B', MetaItems.BATTERY_MV_LITHIUM.getStackForm(), 'I',
                new UnificationEntry(OrePrefix.wireGtDouble, AnnealedCopper));
        ModHandler.addShapedRecipe("electric_jetpack_advanced", MetaItems.ELECTRIC_JETPACK_ADVANCED.getStackForm(),
                "xJd", "TBT", "WCW", 'J', MetaItems.ELECTRIC_JETPACK.getStackForm(), 'T',
                MetaItems.POWER_THRUSTER_ADVANCED.getStackForm(), 'B', ENERGIUM_CRYSTAL.getStackForm(), 'W',
                new UnificationEntry(OrePrefix.wireGtQuadruple, Gold), 'C',
                new UnificationEntry(OrePrefix.circuit, Tier.HV));
        ModHandler.addShapedRecipe("nano_helmet", MetaItems.NANO_HELMET.getStackForm(), "PPP", "PNP", "xEd", 'P',
                MetaItems.CARBON_FIBER_PLATE.getStackForm(), 'N', MetaItems.NIGHTVISION_GOGGLES.getStackForm(), 'E',
                MetaItems.ENERGIUM_CRYSTAL.getStackForm());
        ModHandler.addShapedRecipe("nano_chestplate", MetaItems.NANO_CHESTPLATE.getStackForm(), "PEP", "PPP", "PPP",
                'P', MetaItems.CARBON_FIBER_PLATE.getStackForm(), 'E', MetaItems.ENERGIUM_CRYSTAL.getStackForm());
        ModHandler.addShapedRecipe("nano_leggings", MetaItems.NANO_LEGGINGS.getStackForm(), "PPP", "PEP", "PxP", 'P',
                MetaItems.CARBON_FIBER_PLATE.getStackForm(), 'E', MetaItems.ENERGIUM_CRYSTAL.getStackForm());
        ModHandler.addShapedRecipe("nano_boots", MetaItems.NANO_BOOTS.getStackForm(), "PxP", "PEP", 'P',
                MetaItems.CARBON_FIBER_PLATE.getStackForm(), 'E', MetaItems.ENERGIUM_CRYSTAL.getStackForm());
        ModHandler.addShapedRecipe("nano_chestplate_advanced", MetaItems.NANO_CHESTPLATE_ADVANCED.getStackForm(), "xJd",
                "PNP", "WCW", 'J', MetaItems.ELECTRIC_JETPACK_ADVANCED.getStackForm(), 'P',
                MetaItems.LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(), 'N', MetaItems.NANO_CHESTPLATE.getStackForm(),
                'W', new UnificationEntry(OrePrefix.wireGtQuadruple, Platinum), 'C',
                new UnificationEntry(OrePrefix.circuit, Tier.IV));
        ModHandler.addShapedRecipe("gravitation_engine", MetaItems.GRAVITATION_ENGINE.getStackForm(), "ESE", "POP",
                "ESE", 'E', MetaItems.EMITTER_LuV.getStackForm(), 'S',
                new UnificationEntry(OrePrefix.wireGtQuadruple, Osmium), 'P',
                new UnificationEntry(OrePrefix.plateDouble, Iridium), 'O',
                MetaItems.ENERGY_LAPOTRONIC_ORB.getStackForm());

        ModHandler.addShapedRecipe("powderbarrel", new ItemStack(MetaBlocks.POWDERBARREL), "PSP", "GGG", "PGP",
                'P', new UnificationEntry(OrePrefix.plate, Wood),
                'S', new ItemStack(Items.STRING),
                'G', new UnificationEntry(OrePrefix.dust, Gunpowder));
    }

    private static void registerFacadeRecipe(Material material, int facadeAmount) {
        OreIngredient ingredient = new OreIngredient(new UnificationEntry(OrePrefix.plate, material).toString());
        ForgeRegistries.RECIPES
                .register(new FacadeRecipe(null, ingredient, facadeAmount).setRegistryName("facade_" + material));
    }
}
