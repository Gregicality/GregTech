package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.category.RecipeCategories;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.MarkerMaterials.Color;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;

public class MiscRecipeLoader {

    public static void init() {
        // Basic Terminal Recipe
        ModHandler.addShapedRecipe(true, "basic_terminal", TERMINAL.getStackForm(),
                "SGS", "PBP", "PWP", 'S', new UnificationEntry(screw, WroughtIron), 'G',
                OreDictUnifier.get("paneGlass"), 'B', new ItemStack(Items.BOOK),
                'P', new UnificationEntry(plate, WroughtIron), 'W', new UnificationEntry(wireGtSingle, RedAlloy));

        // Multiblock Builder
        ModHandler.addShapedRecipe(true, "multiblock_builder", MULTIBLOCK_BUILDER.getStackForm(),
                "wCE", "SRC", "RSd",
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.HV),
                'E', new UnificationEntry(gem, EnderEye),
                'S', new UnificationEntry(screw, StainlessSteel),
                'R', new UnificationEntry(stick, StainlessSteel));

        // Potin Recipe
        ModHandler.addShapelessRecipe("potin_dust", OreDictUnifier.get(dust, Potin, 8),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Tin),
                new UnificationEntry(dust, Tin),
                new UnificationEntry(dust, Lead));

        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[ULV])
                .input(dust, Sugar)
                .inputs(new ItemStack(Blocks.BROWN_MUSHROOM))
                .inputs(new ItemStack(Items.SPIDER_EYE))
                .outputs(new ItemStack(Items.FERMENTED_SPIDER_EYE))
                .buildAndRegister();

        RecipeMaps.MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[ULV])
                .input(dust, Sugar)
                .inputs(new ItemStack(Blocks.RED_MUSHROOM))
                .inputs(new ItemStack(Items.SPIDER_EYE))
                .outputs(new ItemStack(Items.FERMENTED_SPIDER_EYE))
                .buildAndRegister();

        RecipeMaps.SIFTER_RECIPES.recipeBuilder().duration(100).EUt(16)
                .inputs(new ItemStack(Blocks.GRAVEL))
                .output(gem, Flint)
                .chancedOutput(gem, Flint, 9000, 0)
                .chancedOutput(gem, Flint, 8000, 0)
                .chancedOutput(gem, Flint, 6000, 0)
                .chancedOutput(gem, Flint, "1/3", 0)
                .chancedOutput(gem, Flint, 2500, 0)
                .buildAndRegister();

        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .inputs(TOOL_MATCHES.getStackForm(16))
                .input(OrePrefix.plate, Materials.Paper)
                .outputs(TOOL_MATCHBOX.getStackForm())
                .duration(64)
                .EUt(16)
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.COBBLESTONE))
                .duration(16)
                .EUt(VA[ULV])
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.STONE, 1, 0))
                .outputs(new ItemStack(Blocks.STONE, 1, 0))
                .duration(16)
                .EUt(VA[ULV])
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(stone, Andesite)
                .output(stone, Andesite)
                .duration(16)
                .EUt(60)
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(stone, Granite)
                .output(stone, Granite)
                .duration(16)
                .EUt(60)
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(stone, Diorite)
                .output(stone, Diorite)
                .duration(16)
                .EUt(60)
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(dust, Redstone)
                .outputs(new ItemStack(Blocks.OBSIDIAN, 1))
                .duration(16)
                .EUt(240)
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(stone, Marble)
                .output(stone, Marble)
                .duration(16)
                .EUt(240)
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(stone, Basalt)
                .output(stone, Basalt)
                .duration(16)
                .EUt(240)
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(stone, GraniteRed)
                .output(stone, GraniteRed)
                .duration(16)
                .EUt(960)
                .buildAndRegister();

        RecipeMaps.ROCK_BREAKER_RECIPES.recipeBuilder()
                .notConsumable(stone, GraniteBlack)
                .output(stone, GraniteBlack)
                .duration(16)
                .EUt(960)
                .buildAndRegister();

        // Jetpacks
        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(30)
                .inputs(ELECTRIC_MOTOR_MV.getStackForm())
                .input(ring, Aluminium, 2)
                .input(stick, Aluminium)
                .input(rotor, Steel)
                .input(cableGtSingle, Copper, 2)
                .outputs(POWER_THRUSTER.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(200).EUt(30)
                .inputs(ELECTRIC_MOTOR_HV.getStackForm())
                .input(ring, StainlessSteel, 2)
                .input(stick, StainlessSteel)
                .input(rotor, Chrome)
                .input(cableGtSingle, Gold, 2)
                .outputs(POWER_THRUSTER_ADVANCED.getStackForm())
                .buildAndRegister();

        // QuarkTech Suite
        ASSEMBLER_RECIPES.recipeBuilder().duration(1500).EUt(GTValues.VA[GTValues.IV])
                .input(circuit, MarkerMaterials.Tier.LuV, 2)
                .input(wireGtQuadruple, Tungsten, 5)
                .inputNBT(ENERGY_LAPOTRONIC_ORB, NBTMatcher.ANY, NBTCondition.ANY)
                .inputs(SENSOR_IV.getStackForm())
                .inputs(FIELD_GENERATOR_IV.getStackForm())
                .input(screw, TungstenSteel, 4)
                .input(plate, Iridium, 5)
                .input(foil, Ruthenium, 20)
                .input(wireFine, Rhodium, 32)
                .fluidInputs(Titanium.getFluid(L * 10))
                .outputs(QUANTUM_HELMET.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(1500).EUt(GTValues.VA[GTValues.IV])
                .input(circuit, MarkerMaterials.Tier.LuV, 2)
                .input(wireGtQuadruple, Tungsten, 8)
                .inputNBT(ENERGY_LAPOTRONIC_ORB, NBTMatcher.ANY, NBTCondition.ANY)
                .inputs(EMITTER_IV.getStackForm(2))
                .inputs(FIELD_GENERATOR_IV.getStackForm())
                .input(screw, TungstenSteel, 4)
                .input(plate, Iridium, 8)
                .input(foil, Ruthenium, 32)
                .input(wireFine, Rhodium, 48)
                .fluidInputs(Titanium.getFluid(L * 16))
                .outputs(QUANTUM_CHESTPLATE.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(1500).EUt(GTValues.VA[GTValues.IV])
                .input(circuit, MarkerMaterials.Tier.LuV, 2)
                .input(wireGtQuadruple, Tungsten, 7)
                .inputNBT(ENERGY_LAPOTRONIC_ORB, NBTMatcher.ANY, NBTCondition.ANY)
                .inputs(ELECTRIC_MOTOR_IV.getStackForm(4))
                .inputs(FIELD_GENERATOR_IV.getStackForm())
                .input(screw, TungstenSteel, 4)
                .input(plate, Iridium, 7)
                .input(foil, Ruthenium, 28)
                .input(wireFine, Rhodium, 40)
                .fluidInputs(Titanium.getFluid(L * 14))
                .outputs(QUANTUM_LEGGINGS.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(1500).EUt(GTValues.VA[GTValues.IV])
                .input(circuit, MarkerMaterials.Tier.LuV, 2)
                .input(wireGtQuadruple, Tungsten, 4)
                .inputNBT(ENERGY_LAPOTRONIC_ORB, NBTMatcher.ANY, NBTCondition.ANY)
                .inputs(ELECTRIC_PISTON_IV.getStackForm(2))
                .inputs(FIELD_GENERATOR_IV.getStackForm())
                .input(screw, TungstenSteel, 4)
                .input(plate, Iridium, 4)
                .input(foil, Ruthenium, 16)
                .input(wireFine, Rhodium, 16)
                .fluidInputs(Titanium.getFluid(L * 8))
                .outputs(QUANTUM_BOOTS.getStackForm())
                .buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder().duration(1000).EUt(GTValues.VA[GTValues.LuV])
                .inputNBT(((ArmorMetaItem<?>) QUANTUM_CHESTPLATE.getStackForm().getItem())
                        .getItem(QUANTUM_CHESTPLATE.getStackForm()), NBTMatcher.ANY, NBTCondition.ANY)
                .inputs(HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                .input(wireFine, NiobiumTitanium, 64)
                .input(wireGtQuadruple, Osmium, 6)
                .input(plateDouble, Iridium, 4)
                .inputs(GRAVITATION_ENGINE.getStackForm(2))
                .input(circuit, MarkerMaterials.Tier.ZPM)
                .input(plateDense, RhodiumPlatedPalladium, 2)
                .inputNBT(ENERGY_LAPOTRONIC_ORB_CLUSTER, NBTMatcher.ANY, NBTCondition.ANY)
                .inputs(FIELD_GENERATOR_LuV.getStackForm(2))
                .inputs(ELECTRIC_MOTOR_LuV.getStackForm(2))
                .input(screw, HSSS, 8)
                .outputs(QUANTUM_CHESTPLATE_ADVANCED.getStackForm())
                .scannerResearch(GRAVITATION_ENGINE.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(VA[HV])
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .inputs((ItemStack) CraftingComponent.HULL.getIngredient(1))
                .input(wireFine, AnnealedCopper, 8)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(MetaTileEntities.MONITOR_SCREEN.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[HV])
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .inputs((ItemStack) CraftingComponent.HULL.getIngredient(3))
                .input(circuit, MarkerMaterials.Tier.HV, 2)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(MetaTileEntities.CENTRAL_MONITOR.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[HV])
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .input(plate, Aluminium)
                .input(circuit, MarkerMaterials.Tier.MV)
                .input(screw, StainlessSteel, 4)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(COVER_DIGITAL_INTERFACE.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[HV])
                .inputs(COVER_DIGITAL_INTERFACE.getStackForm())
                .inputs(WIRELESS.getStackForm())
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(COVER_DIGITAL_INTERFACE_WIRELESS.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(400)
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .input(circuit, MarkerMaterials.Tier.LV)
                .input(wireFine, Copper, 2)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(PLUGIN_TEXT.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(400)
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .input(circuit, MarkerMaterials.Tier.LV)
                .input(wireFine, Silver, 2)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(PLUGIN_ONLINE_PIC.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(400)
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .input(circuit, MarkerMaterials.Tier.LV)
                .input(wireFine, Gold, 2)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(PLUGIN_FAKE_GUI.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(400)
                .inputs(MetaItems.COVER_SCREEN.getStackForm())
                .input(circuit, MarkerMaterials.Tier.HV)
                .input(wireFine, Aluminium, 2)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(PLUGIN_ADVANCED_MONITOR.getStackForm())
                .buildAndRegister();

        // terminal
        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[MV])
                .input(circuit, MarkerMaterials.Tier.MV, 4)
                .input(EMITTER_MV, 2)
                .input(SENSOR_MV, 2)
                .input(plate, StainlessSteel)
                .fluidInputs(Polyethylene.getFluid(L))
                .outputs(WIRELESS.getStackForm())
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[LV])
                .input(ELECTRIC_PISTON_LV, 2)
                .input(EMITTER_LV)
                .input(lens, Glass)
                .input(lens, Diamond)
                .input(circuit, MarkerMaterials.Tier.LV, 4)
                .fluidInputs(SolderingAlloy.getFluid(L))
                .outputs(CAMERA.getStackForm())
                .buildAndRegister();

        // Tempered Glass in Arc Furnace
        ARC_FURNACE_RECIPES.recipeBuilder().duration(60).EUt(VA[LV])
                .input(block, Glass)
                .outputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(
                        BlockGlassCasing.CasingType.TEMPERED_GLASS))
                .buildAndRegister();

        // Dyed Lens Decomposition
        for (MetaValueItem item : GLASS_LENSES.values()) {
            EXTRACTOR_RECIPES.recipeBuilder().EUt(VA[LV]).duration(15)
                    .input(item)
                    .fluidOutputs(Glass.getFluid(108))
                    .category(RecipeCategories.EXTRACTOR_RECYCLING)
                    .buildAndRegister();

            MACERATOR_RECIPES.recipeBuilder().duration(15)
                    .input(item)
                    .output(dustSmall, Glass, 3)
                    .category(RecipeCategories.MACERATOR_RECYCLING)
                    .buildAndRegister();
        }

        // Glass Fluid Extraction
        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.GLASS))
                .fluidOutputs(Glass.getFluid(L))
                .duration(20).EUt(30).buildAndRegister();

        // Glass Plate in Alloy Smelter
        ALLOY_SMELTER_RECIPES.recipeBuilder()
                .input(dust, Glass, 2)
                .notConsumable(SHAPE_MOLD_PLATE.getStackForm())
                .output(plate, Glass)
                .duration(40).EUt(6).buildAndRegister();

        // Dyed Lens Recipes
        RecipeBuilder<?> builder = CHEMICAL_BATH_RECIPES.recipeBuilder().EUt(VA[HV]).duration(200).input(craftingLens,
                Glass);
        final int dyeAmount = 288;

        builder.copy().fluidInputs(DyeWhite.getFluid(dyeAmount)).output(lens, Glass).buildAndRegister();
        builder.copy().fluidInputs(DyeOrange.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.Orange))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeMagenta.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.Magenta))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeLightBlue.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.LightBlue))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeYellow.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.Yellow))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeLime.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.Lime)).buildAndRegister();
        builder.copy().fluidInputs(DyePink.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.Pink)).buildAndRegister();
        builder.copy().fluidInputs(DyeGray.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.Gray)).buildAndRegister();
        builder.copy().fluidInputs(DyeLightGray.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.LightGray))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeCyan.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.Cyan)).buildAndRegister();
        builder.copy().fluidInputs(DyePurple.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.Purple))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeBlue.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.Blue)).buildAndRegister();
        builder.copy().fluidInputs(DyeBrown.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.Brown))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeGreen.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.Green))
                .buildAndRegister();
        builder.copy().fluidInputs(DyeRed.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.Red)).buildAndRegister();
        builder.copy().fluidInputs(DyeBlack.getFluid(dyeAmount)).output(GLASS_LENSES.get(Color.Black))
                .buildAndRegister();

        // NAN Certificate
        EXTRUDER_RECIPES.recipeBuilder()
                .input(block, Neutronium, 64)
                .input(block, Neutronium, 64)
                .output(NAN_CERTIFICATE)
                .duration(Integer.MAX_VALUE).EUt(VA[ULV]).buildAndRegister();

        // Fertilizer
        MIXER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.DIRT))
                .input(dust, Wood, 2)
                .inputs(new ItemStack(Blocks.SAND, 4))
                .fluidInputs(Water.getFluid(1000))
                .output(FERTILIZER, 4)
                .duration(100).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().input(dust, Calcite).input(dust, Sulfur).fluidInputs(Water.getFluid(1000))
                .output(FERTILIZER, 2).duration(200).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, Calcite).input(dust, TricalciumPhosphate)
                .fluidInputs(Water.getFluid(1000)).output(FERTILIZER, 3).duration(300).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, Calcite).input(dust, Phosphate).fluidInputs(Water.getFluid(1000))
                .output(FERTILIZER, 2).duration(200).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, Calcite).input(dust, Ash, 3).fluidInputs(Water.getFluid(1000))
                .output(FERTILIZER, 1).duration(100).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, Calcite).input(dust, DarkAsh).fluidInputs(Water.getFluid(1000))
                .output(FERTILIZER, 1).duration(100).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, Calcium).input(dust, Sulfur).fluidInputs(Water.getFluid(1000))
                .output(FERTILIZER, 3).duration(300).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, Calcium).input(dust, TricalciumPhosphate)
                .fluidInputs(Water.getFluid(1000)).output(FERTILIZER, 4).duration(400).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, Calcium).input(dust, Phosphate).fluidInputs(Water.getFluid(1000))
                .output(FERTILIZER, 3).duration(300).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, Calcium).input(dust, Ash, 3).fluidInputs(Water.getFluid(1000))
                .output(FERTILIZER, 2).duration(200).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, Calcium).input(dust, DarkAsh).fluidInputs(Water.getFluid(1000))
                .output(FERTILIZER, 2).duration(200).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, Apatite).input(dust, Sulfur).fluidInputs(Water.getFluid(1000))
                .output(FERTILIZER, 3).duration(300).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, Apatite).input(dust, TricalciumPhosphate)
                .fluidInputs(Water.getFluid(1000)).output(FERTILIZER, 4).duration(400).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, Apatite).input(dust, Phosphate).fluidInputs(Water.getFluid(1000))
                .output(FERTILIZER, 3).duration(300).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, Apatite).input(dust, Ash, 3).fluidInputs(Water.getFluid(1000))
                .output(FERTILIZER, 2).duration(200).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, Apatite).input(dust, DarkAsh).fluidInputs(Water.getFluid(1000))
                .output(FERTILIZER, 2).duration(200).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, GlauconiteSand).input(dust, Sulfur)
                .fluidInputs(Water.getFluid(1000)).output(FERTILIZER, 3).duration(300).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, GlauconiteSand).input(dust, TricalciumPhosphate)
                .fluidInputs(Water.getFluid(1000)).output(FERTILIZER, 4).duration(400).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, GlauconiteSand).input(dust, Phosphate)
                .fluidInputs(Water.getFluid(1000)).output(FERTILIZER, 3).duration(300).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, GlauconiteSand).input(dust, Ash, 3)
                .fluidInputs(Water.getFluid(1000)).output(FERTILIZER, 2).duration(200).EUt(VA[LV]).buildAndRegister();
        CHEMICAL_RECIPES.recipeBuilder().input(dust, GlauconiteSand).input(dust, DarkAsh)
                .fluidInputs(Water.getFluid(1000)).output(FERTILIZER, 2).duration(200).EUt(VA[LV]).buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .input(FERTILIZER)
                .output(dust, Calcite)
                .output(dust, Carbon)
                .fluidOutputs(Water.getFluid(1000))
                .duration(100).EUt(VA[LV]).buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.TEMPERED_GLASS, 2))
                .input(plate, PolyvinylButyral)
                .outputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.LAMINATED_GLASS))
                .duration(200).EUt(VA[HV]).buildAndRegister();

        LATHE_RECIPES.recipeBuilder()
                .input(plank, TreatedWood)
                .output(stick, TreatedWood, 2)
                .duration(10).EUt(VA[ULV])
                .buildAndRegister();

        // Coke Brick and Firebrick decomposition
        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.COKE_BRICKS))
                .output(COKE_OVEN_BRICK, 4)
                .duration(300).EUt(2)
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS))
                .output(FIRECLAY_BRICK, 4)
                .duration(300).EUt(2)
                .buildAndRegister();

        // Minecart wheels
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(stick, Iron)
                .input(ring, Iron, 2)
                .output(IRON_MINECART_WHEELS)
                .duration(100).EUt(20).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(stick, Steel)
                .input(ring, Steel, 2)
                .output(STEEL_MINECART_WHEELS)
                .duration(60).EUt(20).buildAndRegister();
    }
}
