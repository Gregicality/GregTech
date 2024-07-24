package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.items.OreDictNames;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.ingredients.GTRecipeOreInput;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLY_LINE_RECIPES;
import static gregtech.api.unification.material.MarkerMaterials.Tier;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.MetaBlocks.LD_FLUID_PIPE;
import static gregtech.common.blocks.MetaBlocks.LD_ITEM_PIPE;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.metatileentities.MetaTileEntities.*;

public class MetaTileEntityMachineRecipeLoader {

    public static void init() {
        // Fluid Hatches
        registerHatchBusRecipe(ULV, FLUID_IMPORT_HATCH[ULV], FLUID_EXPORT_HATCH[ULV], new ItemStack(Blocks.GLASS));
        registerHatchBusRecipe(LV, FLUID_IMPORT_HATCH[LV], FLUID_EXPORT_HATCH[LV], new ItemStack(Blocks.GLASS));
        registerHatchBusRecipe(MV, FLUID_IMPORT_HATCH[MV], FLUID_EXPORT_HATCH[MV], BRONZE_DRUM.getStackForm());
        registerHatchBusRecipe(HV, FLUID_IMPORT_HATCH[HV], FLUID_EXPORT_HATCH[HV], STEEL_DRUM.getStackForm());
        registerHatchBusRecipe(EV, FLUID_IMPORT_HATCH[EV], FLUID_EXPORT_HATCH[EV], ALUMINIUM_DRUM.getStackForm());
        registerHatchBusRecipe(IV, FLUID_IMPORT_HATCH[IV], FLUID_EXPORT_HATCH[IV], STAINLESS_STEEL_DRUM.getStackForm());
        registerHatchBusRecipe(LuV, FLUID_IMPORT_HATCH[LuV], FLUID_EXPORT_HATCH[LuV], TITANIUM_DRUM.getStackForm());
        registerHatchBusRecipe(ZPM, FLUID_IMPORT_HATCH[ZPM], FLUID_EXPORT_HATCH[ZPM],
                TUNGSTENSTEEL_DRUM.getStackForm());
        registerHatchBusRecipe(UV, FLUID_IMPORT_HATCH[UV], FLUID_EXPORT_HATCH[UV], QUANTUM_TANK[0].getStackForm());
        registerHatchBusRecipe(UHV, FLUID_IMPORT_HATCH[UHV], FLUID_EXPORT_HATCH[UHV], QUANTUM_TANK[1].getStackForm());

        // Quadruple Fluid Input Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_IMPORT_HATCH[EV])
                .input(pipeQuadrupleFluid, Titanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(4)
                .output(QUADRUPLE_IMPORT_HATCH[0])
                .duration(300).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_IMPORT_HATCH[IV])
                .input(pipeQuadrupleFluid, TungstenSteel)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(4)
                .output(QUADRUPLE_IMPORT_HATCH[1])
                .duration(300).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_IMPORT_HATCH[LuV])
                .input(pipeQuadrupleFluid, NiobiumTitanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(4)
                .output(QUADRUPLE_IMPORT_HATCH[2])
                .duration(300).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_IMPORT_HATCH[ZPM])
                .input(pipeQuadrupleFluid, Iridium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .circuitMeta(4)
                .output(QUADRUPLE_IMPORT_HATCH[3])
                .duration(300).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_IMPORT_HATCH[UV])
                .input(pipeQuadrupleFluid, Naquadah)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .circuitMeta(4)
                .output(QUADRUPLE_IMPORT_HATCH[4])
                .duration(300).EUt(VA[UV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_IMPORT_HATCH[UHV])
                .input(pipeQuadrupleFluid, Neutronium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .circuitMeta(4)
                .output(QUADRUPLE_IMPORT_HATCH[5])
                .duration(300).EUt(VA[UV]).buildAndRegister();

        // Nonuple Fluid Input Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_IMPORT_HATCH[EV])
                .input(pipeNonupleFluid, Titanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 9))
                .circuitMeta(9)
                .output(NONUPLE_IMPORT_HATCH[0])
                .duration(600).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_IMPORT_HATCH[IV])
                .input(pipeNonupleFluid, TungstenSteel)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 9))
                .circuitMeta(9)
                .output(NONUPLE_IMPORT_HATCH[1])
                .duration(600).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_IMPORT_HATCH[LuV])
                .input(pipeNonupleFluid, NiobiumTitanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 9))
                .circuitMeta(9)
                .output(NONUPLE_IMPORT_HATCH[2])
                .duration(600).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_IMPORT_HATCH[ZPM])
                .input(pipeNonupleFluid, Iridium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 9))
                .circuitMeta(9)
                .output(NONUPLE_IMPORT_HATCH[3])
                .duration(600).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_IMPORT_HATCH[UV])
                .input(pipeNonupleFluid, Naquadah)
                .fluidInputs(Polybenzimidazole.getFluid(L * 9))
                .circuitMeta(9)
                .output(NONUPLE_IMPORT_HATCH[4])
                .duration(600).EUt(VA[UV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_IMPORT_HATCH[UHV])
                .input(pipeNonupleFluid, Neutronium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 9))
                .circuitMeta(9)
                .output(NONUPLE_IMPORT_HATCH[5])
                .duration(600).EUt(VA[UV]).buildAndRegister();

        // Quadruple Fluid Output Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_EXPORT_HATCH[EV])
                .input(pipeQuadrupleFluid, Titanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(4)
                .output(QUADRUPLE_EXPORT_HATCH[0])
                .duration(300).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_EXPORT_HATCH[IV])
                .input(pipeQuadrupleFluid, TungstenSteel)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(4)
                .output(QUADRUPLE_EXPORT_HATCH[1])
                .duration(300).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_EXPORT_HATCH[LuV])
                .input(pipeQuadrupleFluid, NiobiumTitanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(4)
                .output(QUADRUPLE_EXPORT_HATCH[2])
                .duration(300).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_EXPORT_HATCH[ZPM])
                .input(pipeQuadrupleFluid, Iridium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .circuitMeta(4)
                .output(QUADRUPLE_EXPORT_HATCH[3])
                .duration(300).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_EXPORT_HATCH[UV])
                .input(pipeQuadrupleFluid, Naquadah)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .circuitMeta(4)
                .output(QUADRUPLE_EXPORT_HATCH[4])
                .duration(300).EUt(VA[UV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_EXPORT_HATCH[UHV])
                .input(pipeQuadrupleFluid, Neutronium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .circuitMeta(4)
                .output(QUADRUPLE_EXPORT_HATCH[5])
                .duration(300).EUt(VA[UV]).buildAndRegister();

        // Nonuple Fluid Output Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_EXPORT_HATCH[EV])
                .input(pipeNonupleFluid, Titanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 9))
                .circuitMeta(9)
                .output(NONUPLE_EXPORT_HATCH[0])
                .duration(600).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_EXPORT_HATCH[IV])
                .input(pipeNonupleFluid, TungstenSteel)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 9))
                .circuitMeta(9)
                .output(NONUPLE_EXPORT_HATCH[1])
                .duration(600).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_EXPORT_HATCH[LuV])
                .input(pipeNonupleFluid, NiobiumTitanium)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 9))
                .circuitMeta(9)
                .output(NONUPLE_EXPORT_HATCH[2])
                .duration(600).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_EXPORT_HATCH[ZPM])
                .input(pipeNonupleFluid, Iridium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 9))
                .circuitMeta(9)
                .output(NONUPLE_EXPORT_HATCH[3])
                .duration(600).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_EXPORT_HATCH[UV])
                .input(pipeNonupleFluid, Naquadah)
                .fluidInputs(Polybenzimidazole.getFluid(L * 9))
                .circuitMeta(9)
                .output(NONUPLE_EXPORT_HATCH[4])
                .duration(600).EUt(VA[UV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_EXPORT_HATCH[UHV])
                .input(pipeNonupleFluid, Neutronium)
                .fluidInputs(Polybenzimidazole.getFluid(L * 9))
                .circuitMeta(9)
                .output(NONUPLE_EXPORT_HATCH[5])
                .duration(600).EUt(VA[UV]).buildAndRegister();

        // Reservoir Hatch
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(COVER_INFINITE_WATER)
                .input(FLUID_IMPORT_HATCH[EV])
                .input(ELECTRIC_PUMP_EV)
                .output(RESERVOIR_HATCH)
                .duration(300).EUt(VA[EV]).buildAndRegister();

        // Item Buses
        registerHatchBusRecipe(ULV, ITEM_IMPORT_BUS[ULV], ITEM_EXPORT_BUS[ULV], OreDictNames.chestWood.toString());
        registerHatchBusRecipe(LV, ITEM_IMPORT_BUS[LV], ITEM_EXPORT_BUS[LV], OreDictNames.chestWood.toString());
        registerHatchBusRecipe(MV, ITEM_IMPORT_BUS[MV], ITEM_EXPORT_BUS[MV], BRONZE_CRATE.getStackForm());
        registerHatchBusRecipe(HV, ITEM_IMPORT_BUS[HV], ITEM_EXPORT_BUS[HV], STEEL_CRATE.getStackForm());
        registerHatchBusRecipe(EV, ITEM_IMPORT_BUS[EV], ITEM_EXPORT_BUS[EV], ALUMINIUM_CRATE.getStackForm());
        registerHatchBusRecipe(IV, ITEM_IMPORT_BUS[IV], ITEM_EXPORT_BUS[IV], STAINLESS_STEEL_CRATE.getStackForm());
        registerHatchBusRecipe(LuV, ITEM_IMPORT_BUS[LuV], ITEM_EXPORT_BUS[LuV], TITANIUM_CRATE.getStackForm());
        registerHatchBusRecipe(ZPM, ITEM_IMPORT_BUS[ZPM], ITEM_EXPORT_BUS[ZPM], TUNGSTENSTEEL_CRATE.getStackForm());
        registerHatchBusRecipe(UV, ITEM_IMPORT_BUS[UV], ITEM_EXPORT_BUS[UV], QUANTUM_CHEST[0].getStackForm());
        registerHatchBusRecipe(UHV, ITEM_IMPORT_BUS[UHV], ITEM_EXPORT_BUS[UHV], QUANTUM_CHEST[1].getStackForm());

        // Laser Hatches
        registerLaserRecipes();

        // Energy Output Hatches

        ModHandler.addShapedRecipe(true, "dynamo_hatch.ulv", ENERGY_OUTPUT_HATCH[ULV].getStackForm(),
                " V ", "SHS", "   ",
                'S', new UnificationEntry(spring, Lead),
                'V', VOLTAGE_COIL_ULV.getStackForm(),
                'H', HULL[ULV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[ULV])
                .input(spring, Lead, 2)
                .input(VOLTAGE_COIL_ULV)
                .output(ENERGY_OUTPUT_HATCH[ULV])
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        ModHandler.addShapedRecipe(true, "dynamo_hatch.lv", ENERGY_OUTPUT_HATCH[LV].getStackForm(),
                " V ", "SHS", "   ",
                'S', new UnificationEntry(spring, Tin),
                'V', VOLTAGE_COIL_LV.getStackForm(),
                'H', HULL[LV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LV])
                .input(spring, Tin, 2)
                .input(VOLTAGE_COIL_LV)
                .output(ENERGY_OUTPUT_HATCH[LV])
                .duration(200).EUt(VA[LV]).buildAndRegister();

        ModHandler.addShapedRecipe(true, "dynamo_hatch.mv", ENERGY_OUTPUT_HATCH[MV].getStackForm(),
                " V ", "SHS", " P ",
                'P', ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(),
                'S', new UnificationEntry(spring, Copper),
                'V', VOLTAGE_COIL_MV.getStackForm(),
                'H', HULL[MV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[MV])
                .input(spring, Copper, 2)
                .input(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT)
                .input(VOLTAGE_COIL_MV)
                .output(ENERGY_OUTPUT_HATCH[MV])
                .duration(200).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[HV])
                .input(spring, Gold, 2)
                .input(LOW_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_HV)
                .fluidInputs(SodiumPotassium.getFluid(1000))
                .output(ENERGY_OUTPUT_HATCH[HV])
                .duration(200).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[EV])
                .input(spring, Aluminium, 2)
                .input(POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_EV)
                .fluidInputs(SodiumPotassium.getFluid(2000))
                .output(ENERGY_OUTPUT_HATCH[EV])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(spring, Tungsten, 2)
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_IV)
                .fluidInputs(SodiumPotassium.getFluid(3000))
                .output(ENERGY_OUTPUT_HATCH[IV])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(spring, NiobiumTitanium, 4)
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.LuV)
                .input(VOLTAGE_COIL_LuV, 2)
                .fluidInputs(SodiumPotassium.getFluid(6000))
                .fluidInputs(SolderingAlloy.getFluid(720))
                .output(ENERGY_OUTPUT_HATCH[LuV])
                .scannerResearch(b -> b
                        .researchStack(ENERGY_OUTPUT_HATCH[IV].getStackForm())
                        .EUt(VA[EV]))
                .duration(400).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[ZPM])
                .input(spring, VanadiumGallium, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.ZPM)
                .input(VOLTAGE_COIL_ZPM, 2)
                .fluidInputs(SodiumPotassium.getFluid(8000))
                .fluidInputs(SolderingAlloy.getFluid(1440))
                .output(ENERGY_OUTPUT_HATCH[ZPM])
                .stationResearch(b -> b
                        .researchStack(ENERGY_OUTPUT_HATCH[LuV].getStackForm())
                        .CWUt(8))
                .duration(600).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[UV])
                .input(spring, YttriumBariumCuprate, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.UV)
                .input(VOLTAGE_COIL_UV, 2)
                .fluidInputs(SodiumPotassium.getFluid(10000))
                .fluidInputs(SolderingAlloy.getFluid(2880))
                .output(ENERGY_OUTPUT_HATCH[UV])
                .stationResearch(b -> b
                        .researchStack(ENERGY_OUTPUT_HATCH[ZPM].getStackForm())
                        .CWUt(64)
                        .EUt(VA[ZPM]))
                .duration(800).EUt(VA[UV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[UHV])
                .input(spring, Europium, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.UHV)
                .input(wireGtDouble, RutheniumTriniumAmericiumNeutronate, 2)
                .fluidInputs(SodiumPotassium.getFluid(12000))
                .fluidInputs(SolderingAlloy.getFluid(5760))
                .output(ENERGY_OUTPUT_HATCH[UHV])
                .stationResearch(b -> b
                        .researchStack(ENERGY_OUTPUT_HATCH[UV].getStackForm())
                        .CWUt(128)
                        .EUt(VA[UV]))
                .duration(1000).EUt(VA[UHV]).buildAndRegister();

        // Energy Input Hatches

        ModHandler.addShapedRecipe(true, "energy_hatch.ulv", ENERGY_INPUT_HATCH[ULV].getStackForm(),
                " V ", "CHC", "   ",
                'C', new UnificationEntry(cableGtSingle, RedAlloy),
                'V', VOLTAGE_COIL_ULV.getStackForm(),
                'H', HULL[ULV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[ULV])
                .input(cableGtSingle, RedAlloy, 2)
                .input(VOLTAGE_COIL_ULV)
                .output(ENERGY_INPUT_HATCH[ULV])
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        ModHandler.addShapedRecipe(true, "energy_hatch.lv", ENERGY_INPUT_HATCH[LV].getStackForm(),
                " V ", "CHC", "   ",
                'C', new UnificationEntry(cableGtSingle, Tin),
                'V', VOLTAGE_COIL_LV.getStackForm(),
                'H', HULL[LV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LV])
                .input(cableGtSingle, Tin, 2)
                .input(VOLTAGE_COIL_LV)
                .output(ENERGY_INPUT_HATCH[LV])
                .duration(200).EUt(VA[LV]).buildAndRegister();

        ModHandler.addShapedRecipe(true, "energy_hatch.mv", ENERGY_INPUT_HATCH[MV].getStackForm(),
                " V ", "CHC", " P ",
                'C', new UnificationEntry(cableGtSingle, Copper),
                'P', ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.getStackForm(),
                'V', VOLTAGE_COIL_MV.getStackForm(),
                'H', HULL[MV].getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[MV])
                .input(cableGtSingle, Copper, 2)
                .input(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT)
                .input(VOLTAGE_COIL_MV)
                .output(ENERGY_INPUT_HATCH[MV])
                .duration(200).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[HV])
                .input(cableGtSingle, Gold, 2)
                .input(LOW_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_HV)
                .fluidInputs(SodiumPotassium.getFluid(1000))
                .output(ENERGY_INPUT_HATCH[HV])
                .duration(200).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[EV])
                .input(cableGtSingle, Aluminium, 2)
                .input(POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_EV)
                .fluidInputs(SodiumPotassium.getFluid(2000))
                .output(ENERGY_INPUT_HATCH[EV])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(cableGtSingle, Tungsten, 2)
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(VOLTAGE_COIL_IV)
                .fluidInputs(SodiumPotassium.getFluid(3000))
                .output(ENERGY_INPUT_HATCH[IV])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(cableGtSingle, NiobiumTitanium, 4)
                .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.LuV)
                .input(VOLTAGE_COIL_LuV, 2)
                .fluidInputs(SodiumPotassium.getFluid(6000))
                .fluidInputs(SolderingAlloy.getFluid(720))
                .output(ENERGY_INPUT_HATCH[LuV])
                .scannerResearch(b -> b
                        .researchStack(ENERGY_INPUT_HATCH[IV].getStackForm())
                        .EUt(VA[EV]))
                .duration(400).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[ZPM])
                .input(cableGtSingle, VanadiumGallium, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.ZPM)
                .input(VOLTAGE_COIL_ZPM, 2)
                .fluidInputs(SodiumPotassium.getFluid(8000))
                .fluidInputs(SolderingAlloy.getFluid(1440))
                .output(ENERGY_INPUT_HATCH[ZPM])
                .stationResearch(b -> b
                        .researchStack(ENERGY_INPUT_HATCH[LuV].getStackForm())
                        .CWUt(8))
                .duration(600).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[UV])
                .input(cableGtSingle, YttriumBariumCuprate, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.UV)
                .input(VOLTAGE_COIL_UV, 2)
                .fluidInputs(SodiumPotassium.getFluid(10000))
                .fluidInputs(SolderingAlloy.getFluid(2880))
                .output(ENERGY_INPUT_HATCH[UV])
                .stationResearch(b -> b
                        .researchStack(ENERGY_INPUT_HATCH[ZPM].getStackForm())
                        .CWUt(64)
                        .EUt(VA[ZPM]))
                .duration(800).EUt(VA[UV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(HULL[UHV])
                .input(cableGtSingle, Europium, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(circuit, Tier.UHV)
                .input(wireGtDouble, RutheniumTriniumAmericiumNeutronate, 2)
                .fluidInputs(SodiumPotassium.getFluid(12000))
                .fluidInputs(SolderingAlloy.getFluid(5760))
                .output(ENERGY_INPUT_HATCH[UHV])
                .stationResearch(b -> b
                        .researchStack(ENERGY_INPUT_HATCH[UV].getStackForm())
                        .CWUt(128)
                        .EUt(VA[UV]))
                .duration(1000).EUt(VA[UHV]).buildAndRegister();

        // Power Transformers

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HI_AMP_TRANSFORMER[ULV])
                .input(ELECTRIC_PUMP_LV)
                .input(cableGtOctal, Tin)
                .input(cableGtHex, Lead, 2)
                .input(springSmall, Lead)
                .input(spring, Tin)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[ULV])
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HI_AMP_TRANSFORMER[LV])
                .input(ELECTRIC_PUMP_LV)
                .input(cableGtOctal, Copper)
                .input(cableGtHex, Tin, 2)
                .input(springSmall, Tin)
                .input(spring, Copper)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[LV])
                .duration(200).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HI_AMP_TRANSFORMER[MV])
                .input(ELECTRIC_PUMP_MV)
                .input(cableGtOctal, Gold)
                .input(cableGtHex, Copper, 2)
                .input(springSmall, Copper)
                .input(spring, Gold)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[MV])
                .duration(200).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HI_AMP_TRANSFORMER[HV])
                .input(ELECTRIC_PUMP_MV)
                .input(cableGtOctal, Aluminium)
                .input(cableGtHex, Gold, 2)
                .input(springSmall, Gold)
                .input(spring, Aluminium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[HV])
                .duration(200).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HI_AMP_TRANSFORMER[EV])
                .input(ELECTRIC_PUMP_HV)
                .input(cableGtOctal, Tungsten)
                .input(cableGtHex, Aluminium, 2)
                .input(springSmall, Aluminium)
                .input(spring, Tungsten)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[EV])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HI_AMP_TRANSFORMER[IV])
                .input(ELECTRIC_PUMP_HV)
                .input(cableGtOctal, NiobiumTitanium)
                .input(cableGtHex, Tungsten, 2)
                .input(springSmall, Tungsten)
                .input(spring, NiobiumTitanium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[IV])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HI_AMP_TRANSFORMER[LuV])
                .input(ELECTRIC_PUMP_EV)
                .input(cableGtOctal, VanadiumGallium)
                .input(cableGtHex, NiobiumTitanium, 2)
                .input(springSmall, NiobiumTitanium)
                .input(spring, VanadiumGallium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[LuV])
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HI_AMP_TRANSFORMER[ZPM])
                .input(ELECTRIC_PUMP_EV)
                .input(cableGtOctal, YttriumBariumCuprate)
                .input(cableGtHex, VanadiumGallium, 2)
                .input(springSmall, VanadiumGallium)
                .input(spring, YttriumBariumCuprate)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[ZPM])
                .duration(200).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HI_AMP_TRANSFORMER[UV])
                .input(ELECTRIC_PUMP_IV)
                .input(cableGtOctal, Europium)
                .input(cableGtHex, YttriumBariumCuprate, 2)
                .input(springSmall, YttriumBariumCuprate)
                .input(spring, Europium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(POWER_TRANSFORMER[UV])
                .duration(200).EUt(VA[UV]).buildAndRegister();

        // 4A Energy Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH[EV])
                .input(wireGtQuadruple, Aluminium, 2)
                .input(plate, Titanium, 2)
                .output(ENERGY_INPUT_HATCH_4A[0])
                .duration(100).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH[IV])
                .input(wireGtQuadruple, Tungsten, 2)
                .input(plate, TungstenSteel, 2)
                .output(ENERGY_INPUT_HATCH_4A[1])
                .duration(100).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH[LuV])
                .input(wireGtQuadruple, NiobiumTitanium, 2)
                .input(plate, RhodiumPlatedPalladium, 2)
                .output(ENERGY_INPUT_HATCH_4A[2])
                .duration(100).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH[ZPM])
                .input(wireGtQuadruple, VanadiumGallium, 2)
                .input(plate, NaquadahAlloy, 2)
                .output(ENERGY_INPUT_HATCH_4A[3])
                .duration(100).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH[UV])
                .input(wireGtQuadruple, YttriumBariumCuprate, 2)
                .input(plate, Darmstadtium, 2)
                .output(ENERGY_INPUT_HATCH_4A[4])
                .duration(100).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH[UHV])
                .input(wireGtQuadruple, Europium, 2)
                .input(plate, Neutronium, 2)
                .output(ENERGY_INPUT_HATCH_4A[5])
                .duration(100).EUt(VA[UV]).buildAndRegister();

        // 16A Energy Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[IV])
                .input(ENERGY_INPUT_HATCH_4A[1])
                .input(wireGtOctal, Tungsten, 2)
                .input(plate, TungstenSteel, 4)
                .output(ENERGY_INPUT_HATCH_16A[0])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[LuV])
                .input(ENERGY_INPUT_HATCH_4A[2])
                .input(wireGtOctal, NiobiumTitanium, 2)
                .input(plate, RhodiumPlatedPalladium, 4)
                .output(ENERGY_INPUT_HATCH_16A[1])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[ZPM])
                .input(ENERGY_INPUT_HATCH_4A[3])
                .input(wireGtOctal, VanadiumGallium, 2)
                .input(plate, NaquadahAlloy, 4)
                .output(ENERGY_INPUT_HATCH_16A[2])
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[UV])
                .input(ENERGY_INPUT_HATCH_4A[4])
                .input(wireGtOctal, YttriumBariumCuprate, 2)
                .input(plate, Darmstadtium, 4)
                .output(ENERGY_INPUT_HATCH_16A[3])
                .duration(200).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HI_AMP_TRANSFORMER[UV])
                .input(ENERGY_INPUT_HATCH_4A[5], 2)
                .input(wireGtOctal, Europium, 2)
                .input(plate, Neutronium, 4)
                .output(ENERGY_INPUT_HATCH_16A[4])
                .duration(200).EUt(VA[UV]).buildAndRegister();

        // 64A Substation Energy Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[IV])
                .input(ENERGY_INPUT_HATCH_16A[0])
                .input(wireGtHex, Tungsten, 2)
                .input(plate, TungstenSteel, 6)
                .output(SUBSTATION_ENERGY_INPUT_HATCH[0])
                .duration(400).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[LuV])
                .input(ENERGY_INPUT_HATCH_16A[1])
                .input(wireGtHex, NiobiumTitanium, 2)
                .input(plate, RhodiumPlatedPalladium, 6)
                .output(SUBSTATION_ENERGY_INPUT_HATCH[1])
                .duration(400).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[ZPM])
                .input(ENERGY_INPUT_HATCH_16A[2])
                .input(wireGtHex, VanadiumGallium, 2)
                .input(plate, NaquadahAlloy, 6)
                .output(SUBSTATION_ENERGY_INPUT_HATCH[2])
                .duration(400).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[UV])
                .input(ENERGY_INPUT_HATCH_16A[3])
                .input(wireGtHex, YttriumBariumCuprate, 2)
                .input(plate, Darmstadtium, 6)
                .output(SUBSTATION_ENERGY_INPUT_HATCH[3])
                .duration(400).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[UV])
                .input(ENERGY_INPUT_HATCH_16A[4])
                .input(wireGtHex, Europium, 2)
                .input(plate, Neutronium, 6)
                .output(SUBSTATION_ENERGY_INPUT_HATCH[4])
                .duration(400).EUt(VA[UV]).buildAndRegister();

        // 4A Dynamo Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH[EV])
                .input(wireGtQuadruple, Aluminium, 2)
                .input(plate, Titanium, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[0])
                .duration(100).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH[IV])
                .input(wireGtQuadruple, Tungsten, 2)
                .input(plate, TungstenSteel, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[1])
                .duration(100).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH[LuV])
                .input(wireGtQuadruple, NiobiumTitanium, 2)
                .input(plate, RhodiumPlatedPalladium, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[2])
                .duration(100).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[ZPM])
                .input(ENERGY_OUTPUT_HATCH[ZPM])
                .input(wireGtQuadruple, VanadiumGallium, 2)
                .input(plate, NaquadahAlloy, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[3])
                .duration(100).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH[UV])
                .input(wireGtQuadruple, YttriumBariumCuprate, 2)
                .input(plate, Darmstadtium, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[4])
                .duration(100).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH[UHV])
                .input(wireGtQuadruple, Europium, 2)
                .input(plate, Neutronium, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[5])
                .duration(100).EUt(VA[UV]).buildAndRegister();

        // 16A Dynamo Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[IV])
                .input(ENERGY_OUTPUT_HATCH_4A[1])
                .input(wireGtOctal, Tungsten, 2)
                .input(plate, TungstenSteel, 4)
                .output(ENERGY_OUTPUT_HATCH_16A[0])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[LuV])
                .input(ENERGY_OUTPUT_HATCH_4A[2])
                .input(wireGtOctal, NiobiumTitanium, 2)
                .input(plate, RhodiumPlatedPalladium, 4)
                .output(ENERGY_OUTPUT_HATCH_16A[1])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[ZPM])
                .input(ENERGY_OUTPUT_HATCH_4A[3])
                .input(wireGtOctal, VanadiumGallium, 2)
                .input(plate, NaquadahAlloy, 4)
                .output(ENERGY_OUTPUT_HATCH_16A[2])
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[UV])
                .input(ENERGY_OUTPUT_HATCH_4A[4])
                .input(wireGtOctal, YttriumBariumCuprate, 2)
                .input(plate, Darmstadtium, 4)
                .output(ENERGY_OUTPUT_HATCH_16A[3])
                .duration(200).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HI_AMP_TRANSFORMER[UV])
                .input(ENERGY_OUTPUT_HATCH_4A[5])
                .input(wireGtOctal, Europium, 2)
                .input(plate, Neutronium, 4)
                .output(ENERGY_OUTPUT_HATCH_16A[4])
                .duration(200).EUt(VA[UV]).buildAndRegister();

        // 64A Substation Dynamo Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[IV])
                .input(ENERGY_OUTPUT_HATCH_16A[0])
                .input(wireGtHex, Tungsten, 2)
                .input(plate, TungstenSteel, 6)
                .output(SUBSTATION_ENERGY_OUTPUT_HATCH[0])
                .duration(400).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[LuV])
                .input(ENERGY_OUTPUT_HATCH_16A[1])
                .input(wireGtHex, NiobiumTitanium, 2)
                .input(plate, RhodiumPlatedPalladium, 6)
                .output(SUBSTATION_ENERGY_OUTPUT_HATCH[1])
                .duration(400).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[ZPM])
                .input(ENERGY_OUTPUT_HATCH_16A[2])
                .input(wireGtHex, VanadiumGallium, 2)
                .input(plate, NaquadahAlloy, 6)
                .output(SUBSTATION_ENERGY_OUTPUT_HATCH[2])
                .duration(400).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[UV])
                .input(ENERGY_OUTPUT_HATCH_16A[3])
                .input(wireGtHex, YttriumBariumCuprate, 2)
                .input(plate, Darmstadtium, 6)
                .output(SUBSTATION_ENERGY_OUTPUT_HATCH[3])
                .duration(400).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(POWER_TRANSFORMER[UV])
                .input(ENERGY_OUTPUT_HATCH_16A[4])
                .input(wireGtHex, Europium, 2)
                .input(plate, Neutronium, 6)
                .output(SUBSTATION_ENERGY_OUTPUT_HATCH[4])
                .duration(400).EUt(VA[UV]).buildAndRegister();

        // Maintenance Hatch

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LV])
                .circuitMeta(8)
                .output(MAINTENANCE_HATCH)
                .duration(100).EUt(VA[LV]).buildAndRegister();

        // Multiblock Miners

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[EV])
                .input(frameGt, Titanium, 4)
                .input(circuit, Tier.EV, 4)
                .input(ELECTRIC_MOTOR_EV, 4)
                .input(ELECTRIC_PUMP_EV, 4)
                .input(CONVEYOR_MODULE_EV, 4)
                .input(gear, Tungsten, 4)
                .circuitMeta(2)
                .output(BASIC_LARGE_MINER)
                .duration(400).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(frameGt, TungstenSteel, 4)
                .input(circuit, Tier.IV, 4)
                .input(ELECTRIC_MOTOR_IV, 4)
                .input(ELECTRIC_PUMP_IV, 4)
                .input(CONVEYOR_MODULE_IV, 4)
                .input(gear, Iridium, 4)
                .circuitMeta(2)
                .output(LARGE_MINER)
                .duration(400).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(frameGt, HSSS, 4)
                .input(circuit, Tier.LuV, 4)
                .input(ELECTRIC_MOTOR_LuV, 4)
                .input(ELECTRIC_PUMP_LuV, 4)
                .input(CONVEYOR_MODULE_LuV, 4)
                .input(gear, Ruridit, 4)
                .circuitMeta(2)
                .output(ADVANCED_LARGE_MINER)
                .duration(400).EUt(VA[LuV]).buildAndRegister();

        // Multiblock Fluid Drills

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[MV])
                .input(frameGt, Steel, 4)
                .input(circuit, Tier.MV, 4)
                .input(ELECTRIC_MOTOR_MV, 4)
                .input(ELECTRIC_PUMP_MV, 4)
                .input(gear, VanadiumSteel, 4)
                .circuitMeta(2)
                .output(BASIC_FLUID_DRILLING_RIG)
                .duration(400).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[EV])
                .input(frameGt, Titanium, 4)
                .input(circuit, Tier.EV, 4)
                .input(ELECTRIC_MOTOR_EV, 4)
                .input(ELECTRIC_PUMP_EV, 4)
                .input(gear, TungstenCarbide, 4)
                .circuitMeta(2)
                .output(FLUID_DRILLING_RIG)
                .duration(400).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(frameGt, TungstenSteel, 4)
                .input(circuit, Tier.LuV, 4)
                .input(ELECTRIC_MOTOR_LuV, 4)
                .input(ELECTRIC_PUMP_LuV, 4)
                .input(gear, Osmiridium, 4)
                .circuitMeta(2)
                .output(ADVANCED_FLUID_DRILLING_RIG)
                .duration(400).EUt(VA[LuV]).buildAndRegister();

        // Long Distance Pipes
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(pipeLargeItem, Tin, 2)
                .input(plate, Steel, 8)
                .input(gear, Steel, 2)
                .circuitMeta(1)
                .fluidInputs(SolderingAlloy.getFluid(L / 2))
                .output(LONG_DIST_ITEM_ENDPOINT, 2)
                .duration(400).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(pipeLargeFluid, Bronze, 2)
                .input(plate, Steel, 8)
                .input(gear, Steel, 2)
                .circuitMeta(1)
                .fluidInputs(SolderingAlloy.getFluid(L / 2))
                .output(LONG_DIST_FLUID_ENDPOINT, 2)
                .duration(400).EUt(16).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(pipeLargeItem, Tin, 2)
                .input(plate, Steel, 8)
                .circuitMeta(2)
                .fluidInputs(SolderingAlloy.getFluid(L / 2))
                .output(LD_ITEM_PIPE, 64)
                .duration(600).EUt(24).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(pipeLargeFluid, Bronze, 2)
                .input(plate, Steel, 8)
                .circuitMeta(2)
                .fluidInputs(SolderingAlloy.getFluid(L / 2))
                .output(LD_FLUID_PIPE, 64)
                .duration(600).EUt(24).buildAndRegister();

        // ME Parts

        if (Mods.AppliedEnergistics2.isModLoaded()) {

            ItemStack fluidInterface = Mods.AppliedEnergistics2.getItem("fluid_interface");
            ItemStack normalInterface = Mods.AppliedEnergistics2.getItem("interface");
            ItemStack accelerationCard = Mods.AppliedEnergistics2.getItem("material", 30, 2);

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(FLUID_EXPORT_HATCH[EV])
                    .inputs(fluidInterface.copy())
                    .inputs(accelerationCard.copy())
                    .output(FLUID_EXPORT_HATCH_ME)
                    .duration(300).EUt(VA[HV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(FLUID_IMPORT_HATCH[EV])
                    .inputs(fluidInterface.copy())
                    .inputs(accelerationCard.copy())
                    .output(FLUID_IMPORT_HATCH_ME)
                    .duration(300).EUt(VA[HV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(ITEM_EXPORT_BUS[EV])
                    .inputs(normalInterface.copy())
                    .inputs(accelerationCard.copy())
                    .output(ITEM_EXPORT_BUS_ME)
                    .duration(300).EUt(VA[HV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(ITEM_IMPORT_BUS[EV])
                    .inputs(normalInterface.copy())
                    .inputs(accelerationCard.copy())
                    .output(ITEM_IMPORT_BUS_ME)
                    .duration(300).EUt(VA[HV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(ITEM_IMPORT_BUS[IV])
                    .inputs(normalInterface.copy())
                    .input(CONVEYOR_MODULE_IV)
                    .input(SENSOR_IV)
                    .inputs(GTUtility.copy(4, accelerationCard))
                    .output(STOCKING_BUS_ME)
                    .duration(300).EUt(VA[IV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(FLUID_IMPORT_HATCH[IV])
                    .inputs(fluidInterface.copy())
                    .input(ELECTRIC_PUMP_IV)
                    .input(SENSOR_IV)
                    .inputs(GTUtility.copy(4, accelerationCard))
                    .output(STOCKING_HATCH_ME)
                    .duration(300).EUt(VA[IV]).buildAndRegister();
        }
    }

    private static void registerHatchBusRecipe(int tier, MetaTileEntity inputBus, MetaTileEntity outputBus,
                                               Object extraInput) {
        GTRecipeInput extra;
        if (extraInput instanceof ItemStack stack) {
            extra = new GTRecipeItemInput(stack);
        } else if (extraInput instanceof String oreName) {
            extra = new GTRecipeOreInput(oreName);
        } else {
            throw new IllegalArgumentException("extraInput must be ItemStack or GTRecipeInput");
        }

        // Glue recipe for ULV and LV
        // 250L for ULV, 500L for LV
        if (tier <= GTValues.LV) {
            int fluidAmount = tier == GTValues.ULV ? 250 : 500;
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[tier])
                    .inputs(extra)
                    .fluidInputs(Glue.getFluid(fluidAmount))
                    .circuitMeta(1)
                    .output(inputBus)
                    .duration(300).EUt(VA[tier]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[tier])
                    .inputs(extra)
                    .fluidInputs(Glue.getFluid(fluidAmount))
                    .circuitMeta(2)
                    .output(outputBus)
                    .duration(300).EUt(VA[tier]).buildAndRegister();
        }

        // Polyethylene recipe for HV and below
        // 72L for ULV, 144L for LV, 288L for MV, 432L for HV
        if (tier <= GTValues.HV) {
            int peAmount = getFluidAmount(tier + 4);
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[tier])
                    .inputs(extra)
                    .fluidInputs(Polyethylene.getFluid(peAmount))
                    .circuitMeta(1)
                    .output(inputBus)
                    .duration(300).EUt(VA[tier]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[tier])
                    .inputs(extra)
                    .fluidInputs(Polyethylene.getFluid(peAmount))
                    .circuitMeta(2)
                    .output(outputBus)
                    .duration(300).EUt(VA[tier]).buildAndRegister();
        }

        // Polytetrafluoroethylene recipe for LuV and below
        // 36L for ULV, 72L for LV, 144L for MV, 288L for HV, 432L for EV, 576L for IV, 720L for LuV
        if (tier <= GTValues.LuV) {
            int ptfeAmount = getFluidAmount(tier + 3);
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[tier])
                    .inputs(extra)
                    .fluidInputs(Polytetrafluoroethylene.getFluid(ptfeAmount))
                    .circuitMeta(1)
                    .output(inputBus)
                    .duration(300).EUt(VA[tier]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[tier])
                    .inputs(extra)
                    .fluidInputs(Polytetrafluoroethylene.getFluid(ptfeAmount))
                    .circuitMeta(2)
                    .output(outputBus)
                    .duration(300).EUt(VA[tier]).buildAndRegister();
        }

        // PBI recipe for all
        // 4L for ULV, 9L for LV, 18L for MV, 36L for HV, 72L for EV, 144L for IV,
        // 288L for LuV, 432L for ZPM, 576L for UV, 720L for UHV
        // Use a Math.min() call on tier so that UHV hatches are still UV voltage
        int pbiAmount = getFluidAmount(tier);
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[tier])
                .inputs(extra)
                .fluidInputs(Polybenzimidazole.getFluid(pbiAmount))
                .circuitMeta(1)
                .output(inputBus)
                .withRecycling()
                .duration(300).EUt(VA[Math.min(GTValues.UV, tier)]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[tier])
                .inputs(extra)
                .fluidInputs(Polybenzimidazole.getFluid(pbiAmount))
                .circuitMeta(2)
                .output(outputBus)
                .withRecycling()
                .duration(300).EUt(VA[Math.min(GTValues.UV, tier)]).buildAndRegister();
    }

    private static int getFluidAmount(int offsetTier) {
        return switch (offsetTier) {
            case 0 -> 4;
            case 1 -> 9;
            case 2 -> 18;
            case 3 -> 36;
            case 4 -> 72;
            case 5 -> 144;
            case 6 -> 288;
            case 7 -> 432;
            case 8 -> 576;
            default -> 720;
        };
    }

    // TODO clean this up with a CraftingComponent rework
    private static void registerLaserRecipes() {
        // 256A Laser Source Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(lens, Diamond)
                .input(EMITTER_IV)
                .input(ELECTRIC_PUMP_IV)
                .input(cableGtSingle, Platinum, 4)
                .circuitMeta(1)
                .output(LASER_INPUT_HATCH_256[0])
                .duration(300).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(lens, Diamond)
                .input(EMITTER_LuV)
                .input(ELECTRIC_PUMP_LuV)
                .input(cableGtSingle, NiobiumTitanium, 4)
                .circuitMeta(1)
                .output(LASER_INPUT_HATCH_256[1])
                .duration(300).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[ZPM])
                .input(lens, Diamond)
                .input(EMITTER_ZPM)
                .input(ELECTRIC_PUMP_ZPM)
                .input(cableGtSingle, VanadiumGallium, 4)
                .circuitMeta(1)
                .output(LASER_INPUT_HATCH_256[2])
                .duration(300).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[UV])
                .input(lens, Diamond)
                .input(EMITTER_UV)
                .input(ELECTRIC_PUMP_UV)
                .input(cableGtSingle, YttriumBariumCuprate, 4)
                .circuitMeta(1)
                .output(LASER_INPUT_HATCH_256[3])
                .duration(300).EUt(VA[UV]).buildAndRegister();

        // 256A Laser Target Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(lens, Diamond)
                .input(SENSOR_IV)
                .input(ELECTRIC_PUMP_IV)
                .input(cableGtSingle, Platinum, 4)
                .circuitMeta(1)
                .output(LASER_OUTPUT_HATCH_256[0])
                .duration(300).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(lens, Diamond)
                .input(SENSOR_LuV)
                .input(ELECTRIC_PUMP_LuV)
                .input(cableGtSingle, NiobiumTitanium, 4)
                .circuitMeta(1)
                .output(LASER_OUTPUT_HATCH_256[1])
                .duration(300).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[ZPM])
                .input(lens, Diamond)
                .input(SENSOR_ZPM)
                .input(ELECTRIC_PUMP_ZPM)
                .input(cableGtSingle, VanadiumGallium, 4)
                .circuitMeta(1)
                .output(LASER_OUTPUT_HATCH_256[2])
                .duration(300).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[UV])
                .input(lens, Diamond)
                .input(SENSOR_UV)
                .input(ELECTRIC_PUMP_UV)
                .input(cableGtSingle, YttriumBariumCuprate, 4)
                .circuitMeta(1)
                .output(LASER_OUTPUT_HATCH_256[3])
                .duration(300).EUt(VA[UV]).buildAndRegister();

        // 1024A Laser Source Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(lens, Diamond, 2)
                .input(EMITTER_IV, 2)
                .input(ELECTRIC_PUMP_IV, 2)
                .input(cableGtDouble, Platinum, 4)
                .circuitMeta(2)
                .output(LASER_INPUT_HATCH_1024[0])
                .duration(600).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(lens, Diamond, 2)
                .input(EMITTER_LuV, 2)
                .input(ELECTRIC_PUMP_LuV, 2)
                .input(cableGtDouble, NiobiumTitanium, 4)
                .circuitMeta(2)
                .output(LASER_INPUT_HATCH_1024[1])
                .duration(600).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[ZPM])
                .input(lens, Diamond, 2)
                .input(EMITTER_ZPM, 2)
                .input(ELECTRIC_PUMP_ZPM, 2)
                .input(cableGtDouble, VanadiumGallium, 4)
                .circuitMeta(2)
                .output(LASER_INPUT_HATCH_1024[2])
                .duration(600).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[UV])
                .input(lens, Diamond, 2)
                .input(EMITTER_UV, 2)
                .input(ELECTRIC_PUMP_UV, 2)
                .input(cableGtDouble, YttriumBariumCuprate, 4)
                .circuitMeta(2)
                .output(LASER_INPUT_HATCH_1024[3])
                .duration(600).EUt(VA[UV]).buildAndRegister();

        // 1024A Laser Target Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(lens, Diamond, 2)
                .input(SENSOR_IV, 2)
                .input(ELECTRIC_PUMP_IV, 2)
                .input(cableGtDouble, Platinum, 4)
                .circuitMeta(2)
                .output(LASER_OUTPUT_HATCH_1024[0])
                .duration(600).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(lens, Diamond, 2)
                .input(SENSOR_LuV, 2)
                .input(ELECTRIC_PUMP_LuV, 2)
                .input(cableGtDouble, NiobiumTitanium, 4)
                .circuitMeta(2)
                .output(LASER_OUTPUT_HATCH_1024[1])
                .duration(600).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[ZPM])
                .input(lens, Diamond, 2)
                .input(SENSOR_ZPM, 2)
                .input(ELECTRIC_PUMP_ZPM, 2)
                .input(cableGtDouble, VanadiumGallium, 4)
                .circuitMeta(2)
                .output(LASER_OUTPUT_HATCH_1024[2])
                .duration(600).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[UV])
                .input(lens, Diamond, 2)
                .input(SENSOR_UV, 2)
                .input(ELECTRIC_PUMP_UV, 2)
                .input(cableGtDouble, YttriumBariumCuprate, 4)
                .circuitMeta(2)
                .output(LASER_OUTPUT_HATCH_1024[3])
                .duration(600).EUt(VA[UV]).buildAndRegister();

        // 4096A Laser Source Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(lens, Diamond, 4)
                .input(EMITTER_IV, 4)
                .input(ELECTRIC_PUMP_IV, 4)
                .input(cableGtQuadruple, Platinum, 4)
                .circuitMeta(3)
                .output(LASER_INPUT_HATCH_4096[0])
                .duration(1200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(lens, Diamond, 4)
                .input(EMITTER_LuV, 4)
                .input(ELECTRIC_PUMP_LuV, 4)
                .input(cableGtQuadruple, NiobiumTitanium, 4)
                .circuitMeta(3)
                .output(LASER_INPUT_HATCH_4096[1])
                .duration(1200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[ZPM])
                .input(lens, Diamond, 4)
                .input(EMITTER_ZPM, 4)
                .input(ELECTRIC_PUMP_ZPM, 4)
                .input(cableGtQuadruple, VanadiumGallium, 4)
                .circuitMeta(3)
                .output(LASER_INPUT_HATCH_4096[2])
                .duration(1200).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[UV])
                .input(lens, Diamond, 4)
                .input(EMITTER_UV, 4)
                .input(ELECTRIC_PUMP_UV, 4)
                .input(cableGtQuadruple, YttriumBariumCuprate, 4)
                .circuitMeta(3)
                .output(LASER_INPUT_HATCH_4096[3])
                .duration(1200).EUt(VA[UV]).buildAndRegister();

        // 4096A Laser Target Hatches
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(lens, Diamond, 4)
                .input(SENSOR_IV, 4)
                .input(ELECTRIC_PUMP_IV, 4)
                .input(cableGtQuadruple, Platinum, 4)
                .circuitMeta(3)
                .output(LASER_OUTPUT_HATCH_4096[0])
                .duration(1200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(lens, Diamond, 4)
                .input(SENSOR_LuV, 4)
                .input(ELECTRIC_PUMP_LuV, 4)
                .input(cableGtQuadruple, NiobiumTitanium, 4)
                .circuitMeta(3)
                .output(LASER_OUTPUT_HATCH_4096[1])
                .duration(1200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[ZPM])
                .input(lens, Diamond, 4)
                .input(SENSOR_ZPM, 4)
                .input(ELECTRIC_PUMP_ZPM, 4)
                .input(cableGtQuadruple, VanadiumGallium, 4)
                .circuitMeta(3)
                .output(LASER_OUTPUT_HATCH_4096[2])
                .duration(1200).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[UV])
                .input(lens, Diamond, 4)
                .input(SENSOR_UV, 4)
                .input(ELECTRIC_PUMP_UV, 4)
                .input(cableGtQuadruple, YttriumBariumCuprate, 4)
                .circuitMeta(3)
                .output(LASER_OUTPUT_HATCH_4096[3])
                .duration(1200).EUt(VA[UV]).buildAndRegister();

        // Nuclear Technology

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(pipeLargeFluid, Inconel)
                .input(HULL[EV])
                .fluidInputs(Polyethylene.getFluid(144))
                .circuitMeta(1)
                .outputs(COOLANT_INPUT.getStackForm())
                .duration(300).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(pipeLargeFluid, Inconel)
                .input(HULL[EV])
                .fluidInputs(Polyethylene.getFluid(144))
                .circuitMeta(2)
                .outputs(COOLANT_OUTPUT.getStackForm())
                .duration(300).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(stick, Zircaloy, 6)
                .input(HULL[EV])
                .fluidInputs(Polyethylene.getFluid(144))
                .circuitMeta(1)
                .outputs(FUEL_ROD_INPUT.getStackForm())
                .duration(300).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(stick, Zircaloy, 6)
                .input(HULL[EV])
                .fluidInputs(Polyethylene.getFluid(144))
                .circuitMeta(2)
                .outputs(FUEL_ROD_OUTPUT.getStackForm())
                .duration(300).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(stickLong, Hafnium)
                .input(circuit, MarkerMaterials.Tier.EV)
                .input(HULL[EV])
                .circuitMeta(1)
                .fluidInputs(Polyethylene.getFluid(144))
                .outputs(CONTROL_ROD.getStackForm())
                .duration(300).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(stickLong, Hafnium)
                .input(dust, Graphite)
                .input(circuit, MarkerMaterials.Tier.EV)
                .input(HULL[EV])
                .circuitMeta(2)
                .fluidInputs(Polyethylene.getFluid(144))
                .outputs(CONTROL_ROD_MODERATED.getStackForm())
                .duration(300).EUt(VA[EV]).buildAndRegister();
    }
}
