package gregtech.loaders.recipe;

import gregtech.common.ConfigHolder;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLY_LINE_RECIPES;
import static gregtech.api.unification.material.MarkerMaterials.Tier.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.metatileentities.MetaTileEntities.*;

public class MetaTileEntityMachineRecipeLoader {

    public static void init() {
        if (ConfigHolder.recipes.harderEnergyHatches) {

            // Energy Output Hatches

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[ULV])
                    .input(spring, Lead)
                    .input(circuit, Primitive, 2)
                    .input(VOLTAGE_COIL_ULV, 2)
                    .input(rotor, Lead)
                    .fluidInputs(Lubricant.getFluid(2000))
                    .output(ENERGY_OUTPUT_HATCH[ULV])
                    .duration(200).EUt(VA[ULV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[LV])
                    .input(spring, Tin)
                    .input(circuit, Basic, 2)
                    .input(VOLTAGE_COIL_LV, 2)
                    .input(ELECTRIC_PUMP_LV)
                    .fluidInputs(Lubricant.getFluid(2000))
                    .output(ENERGY_OUTPUT_HATCH[LV])
                    .duration(200).EUt(VA[LV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[MV])
                    .input(spring, Copper)
                    .input(SMALL_COIL, 2)
                    .input(VOLTAGE_COIL_MV, 2)
                    .input(ELECTRIC_PUMP_MV)
                    .fluidInputs(Lubricant.getFluid(2000))
                    .output(ENERGY_OUTPUT_HATCH[MV])
                    .duration(200).EUt(VA[MV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[HV])
                    .input(spring, Gold)
                    .input(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(VOLTAGE_COIL_HV, 2)
                    .input(ELECTRIC_PUMP_HV)
                    .fluidInputs(Helium.getFluid(1000))
                    .output(ENERGY_OUTPUT_HATCH[HV])
                    .duration(200).EUt(VA[HV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[HV])
                    .input(spring, Gold)
                    .input(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(VOLTAGE_COIL_HV, 2)
                    .input(ELECTRIC_PUMP_HV)
                    .fluidInputs(SodiumPotassium.getFluid(1000))
                    .output(ENERGY_OUTPUT_HATCH[HV])
                    .duration(200).EUt(VA[HV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[EV])
                    .input(spring, Aluminium)
                    .input(LOW_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(VOLTAGE_COIL_EV, 2)
                    .input(ELECTRIC_PUMP_EV)
                    .fluidInputs(Helium.getFluid(2000))
                    .output(ENERGY_OUTPUT_HATCH[EV])
                    .duration(200).EUt(VA[EV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[EV])
                    .input(spring, Aluminium)
                    .input(LOW_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(VOLTAGE_COIL_EV, 2)
                    .input(ELECTRIC_PUMP_EV)
                    .fluidInputs(SodiumPotassium.getFluid(2000))
                    .output(ENERGY_OUTPUT_HATCH[EV])
                    .duration(200).EUt(VA[EV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[IV])
                    .input(spring, Tungsten)
                    .input(POWER_INTEGRATED_CIRCUIT, 2)
                    .input(VOLTAGE_COIL_IV, 2)
                    .input(ELECTRIC_PUMP_IV)
                    .fluidInputs(Helium.getFluid(3000))
                    .output(ENERGY_OUTPUT_HATCH[IV])
                    .duration(200).EUt(VA[IV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[IV])
                    .input(spring, Tungsten)
                    .input(POWER_INTEGRATED_CIRCUIT, 2)
                    .input(VOLTAGE_COIL_IV, 2)
                    .input(ELECTRIC_PUMP_IV)
                    .fluidInputs(SodiumPotassium.getFluid(3000))
                    .output(ENERGY_OUTPUT_HATCH[IV])
                    .duration(200).EUt(VA[IV]).buildAndRegister();

            ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(HULL[LuV])
                    .input(spring, YttriumBariumCuprate, 2)
                    .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(circuit, Master, 2)
                    .input(VOLTAGE_COIL_LUV, 2)
                    .input(ELECTRIC_PUMP_LUV)
                    .fluidInputs(Helium.getFluid(6000))
                    .fluidInputs(SolderingAlloy.getFluid(720))
                    .output(ENERGY_OUTPUT_HATCH[LuV])
                    .duration(400).EUt(VA[LuV]).buildAndRegister();

            ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(HULL[LuV])
                    .input(spring, YttriumBariumCuprate, 2)
                    .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(circuit, Master, 2)
                    .input(VOLTAGE_COIL_LUV, 2)
                    .input(ELECTRIC_PUMP_LUV)
                    .fluidInputs(SodiumPotassium.getFluid(6000))
                    .fluidInputs(SolderingAlloy.getFluid(720))
                    .output(ENERGY_OUTPUT_HATCH[LuV])
                    .duration(400).EUt(VA[LuV]).buildAndRegister();

            ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(HULL[ZPM])
                    .input(spring, VanadiumGallium, 4)
                    .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(circuit, Ultimate, 2)
                    .input(VOLTAGE_COIL_ZPM, 2)
                    .input(ELECTRIC_PUMP_ZPM)
                    .fluidInputs(Helium.getFluid(6000))
                    .fluidInputs(SolderingAlloy.getFluid(1440))
                    .output(ENERGY_OUTPUT_HATCH[ZPM])
                    .duration(600).EUt(VA[ZPM]).buildAndRegister();

            ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(HULL[ZPM])
                    .input(spring, VanadiumGallium, 4)
                    .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(circuit, Ultimate, 2)
                    .input(VOLTAGE_COIL_ZPM, 2)
                    .input(ELECTRIC_PUMP_ZPM)
                    .fluidInputs(SodiumPotassium.getFluid(6000))
                    .fluidInputs(SolderingAlloy.getFluid(1440))
                    .output(ENERGY_OUTPUT_HATCH[ZPM])
                    .duration(600).EUt(VA[ZPM]).buildAndRegister();

            ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(HULL[UV])
                    .input(spring, NiobiumTitanium, 4)
                    .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(circuit, Super, 2)
                    .input(VOLTAGE_COIL_UV, 2)
                    .input(ELECTRIC_PUMP_UV)
                    .fluidInputs(Helium.getFluid(12000))
                    .fluidInputs(SolderingAlloy.getFluid(2880))
                    .output(ENERGY_OUTPUT_HATCH[UV])
                    .duration(800).EUt(VA[UV]).buildAndRegister();

            ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(HULL[UV])
                    .input(spring, NiobiumTitanium, 4)
                    .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(circuit, Super, 2)
                    .input(VOLTAGE_COIL_UV, 2)
                    .input(ELECTRIC_PUMP_UV)
                    .fluidInputs(SodiumPotassium.getFluid(12000))
                    .fluidInputs(SolderingAlloy.getFluid(2880))
                    .output(ENERGY_OUTPUT_HATCH[UV])
                    .duration(800).EUt(VA[UV]).buildAndRegister();

            // Energy Input Hatches

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[ULV])
                    .input(cableGtSingle, Lead, 2)
                    .input(circuit, Primitive)
                    .input(VOLTAGE_COIL_ULV, 2)
                    .input(rotor, Lead)
                    .fluidInputs(Lubricant.getFluid(2000))
                    .output(ENERGY_INPUT_HATCH[ULV])
                    .duration(200).EUt(VA[ULV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[LV])
                    .input(cableGtSingle, Tin, 2)
                    .input(circuit, Basic)
                    .input(VOLTAGE_COIL_LV, 2)
                    .input(ELECTRIC_PUMP_LV)
                    .fluidInputs(Lubricant.getFluid(2000))
                    .output(ENERGY_INPUT_HATCH[LV])
                    .duration(200).EUt(VA[LV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[MV])
                    .input(cableGtSingle, Copper)
                    .input(SMALL_COIL, 2)
                    .input(VOLTAGE_COIL_MV, 2)
                    .input(ELECTRIC_PUMP_MV)
                    .fluidInputs(Lubricant.getFluid(2000))
                    .output(ENERGY_INPUT_HATCH[MV])
                    .duration(200).EUt(VA[MV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[HV])
                    .input(cableGtSingle, Gold)
                    .input(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(VOLTAGE_COIL_HV, 2)
                    .input(ELECTRIC_PUMP_HV)
                    .fluidInputs(Helium.getFluid(1000))
                    .output(ENERGY_INPUT_HATCH[HV])
                    .duration(200).EUt(VA[HV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[HV])
                    .input(cableGtSingle, Gold)
                    .input(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(VOLTAGE_COIL_HV, 2)
                    .input(ELECTRIC_PUMP_HV)
                    .fluidInputs(SodiumPotassium.getFluid(1000))
                    .output(ENERGY_INPUT_HATCH[HV])
                    .duration(200).EUt(VA[HV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[EV])
                    .input(cableGtSingle, Aluminium)
                    .input(LOW_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(VOLTAGE_COIL_EV, 2)
                    .input(ELECTRIC_PUMP_EV)
                    .fluidInputs(Helium.getFluid(2000))
                    .output(ENERGY_INPUT_HATCH[EV])
                    .duration(200).EUt(VA[EV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[EV])
                    .input(cableGtSingle, Aluminium)
                    .input(LOW_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(VOLTAGE_COIL_EV, 2)
                    .input(ELECTRIC_PUMP_EV)
                    .fluidInputs(SodiumPotassium.getFluid(2000))
                    .output(ENERGY_INPUT_HATCH[EV])
                    .duration(200).EUt(VA[EV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[IV])
                    .input(cableGtSingle, Tungsten)
                    .input(POWER_INTEGRATED_CIRCUIT, 2)
                    .input(VOLTAGE_COIL_IV, 2)
                    .input(ELECTRIC_PUMP_IV)
                    .fluidInputs(Helium.getFluid(3000))
                    .output(ENERGY_INPUT_HATCH[IV])
                    .duration(200).EUt(VA[IV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[IV])
                    .input(cableGtSingle, Tungsten)
                    .input(POWER_INTEGRATED_CIRCUIT, 2)
                    .input(VOLTAGE_COIL_IV, 2)
                    .input(ELECTRIC_PUMP_IV)
                    .fluidInputs(SodiumPotassium.getFluid(3000))
                    .output(ENERGY_INPUT_HATCH[IV])
                    .duration(200).EUt(VA[IV]).buildAndRegister();

            ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(HULL[LuV])
                    .input(cableGtSingle, NiobiumTitanium, 2)
                    .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(circuit, Master, 2)
                    .input(VOLTAGE_COIL_LUV, 2)
                    .input(ELECTRIC_PUMP_LUV)
                    .fluidInputs(Helium.getFluid(6000))
                    .fluidInputs(SolderingAlloy.getFluid(720))
                    .output(ENERGY_INPUT_HATCH[LuV])
                    .duration(400).EUt(VA[LuV]).buildAndRegister();

            ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(HULL[LuV])
                    .input(cableGtSingle, NiobiumTitanium, 2)
                    .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(circuit, Master, 2)
                    .input(VOLTAGE_COIL_LUV, 2)
                    .input(ELECTRIC_PUMP_LUV)
                    .fluidInputs(SodiumPotassium.getFluid(6000))
                    .fluidInputs(SolderingAlloy.getFluid(720))
                    .output(ENERGY_INPUT_HATCH[LuV])
                    .duration(400).EUt(VA[LuV]).buildAndRegister();

            ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(HULL[ZPM])
                    .input(cableGtDouble, VanadiumGallium, 2)
                    .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(circuit, Ultimate, 2)
                    .input(VOLTAGE_COIL_ZPM, 2)
                    .input(ELECTRIC_PUMP_ZPM)
                    .fluidInputs(Helium.getFluid(6000))
                    .fluidInputs(SolderingAlloy.getFluid(1440))
                    .output(ENERGY_INPUT_HATCH[ZPM])
                    .duration(600).EUt(VA[ZPM]).buildAndRegister();

            ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(HULL[ZPM])
                    .input(cableGtDouble, VanadiumGallium, 2)
                    .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(circuit, Ultimate, 2)
                    .input(VOLTAGE_COIL_ZPM, 2)
                    .input(ELECTRIC_PUMP_ZPM)
                    .fluidInputs(SodiumPotassium.getFluid(6000))
                    .fluidInputs(SolderingAlloy.getFluid(1440))
                    .output(ENERGY_INPUT_HATCH[ZPM])
                    .duration(600).EUt(VA[ZPM]).buildAndRegister();

            ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(HULL[UV])
                    .input(cableGtDouble, YttriumBariumCuprate, 2)
                    .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(circuit, Super, 2)
                    .input(VOLTAGE_COIL_UV, 2)
                    .input(ELECTRIC_PUMP_UV)
                    .fluidInputs(Helium.getFluid(12000))
                    .fluidInputs(SolderingAlloy.getFluid(2880))
                    .output(ENERGY_INPUT_HATCH[UV])
                    .duration(800).EUt(VA[UV]).buildAndRegister();

            ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(HULL[UV])
                    .input(cableGtDouble, YttriumBariumCuprate, 2)
                    .input(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                    .input(circuit, Super, 2)
                    .input(VOLTAGE_COIL_UV, 2)
                    .input(ELECTRIC_PUMP_UV)
                    .fluidInputs(SodiumPotassium.getFluid(12000))
                    .fluidInputs(SolderingAlloy.getFluid(2880))
                    .output(ENERGY_INPUT_HATCH[UV])
                    .duration(800).EUt(VA[UV]).buildAndRegister();

        }

        // Adjustable Transformers

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[ULV])
                .input(ELECTRIC_PUMP_LV)
                .input(wireGtQuadruple, Tin)
                .input(wireGtOctal, Lead)
                .input(springSmall, Lead)
                .input(spring, Tin)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[ULV])
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[LV])
                .input(ELECTRIC_PUMP_LV)
                .input(wireGtQuadruple, Copper)
                .input(wireGtOctal, Tin)
                .input(springSmall, Tin)
                .input(spring, Copper)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[LV])
                .duration(200).EUt(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[MV])
                .input(ELECTRIC_PUMP_MV)
                .input(wireGtQuadruple, Gold)
                .input(wireGtOctal, Copper)
                .input(springSmall, Copper)
                .input(spring, Gold)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[MV])
                .duration(200).EUt(VA[MV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[HV])
                .input(ELECTRIC_PUMP_MV)
                .input(wireGtQuadruple, Aluminium)
                .input(wireGtOctal, Gold)
                .input(springSmall, Gold)
                .input(spring, Aluminium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[HV])
                .duration(200).EUt(VA[HV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[EV])
                .input(ELECTRIC_PUMP_HV)
                .input(wireGtQuadruple, Tungsten)
                .input(wireGtOctal, Aluminium)
                .input(springSmall, Aluminium)
                .input(spring, Tungsten)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[EV])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[IV])
                .input(ELECTRIC_PUMP_HV)
                .input(wireGtQuadruple, NiobiumTitanium)
                .input(wireGtOctal, Tungsten)
                .input(springSmall, Tungsten)
                .input(spring, NiobiumTitanium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[IV])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[LuV])
                .input(ELECTRIC_PUMP_EV)
                .input(wireGtQuadruple, VanadiumGallium)
                .input(wireGtOctal, NiobiumTitanium)
                .input(springSmall, NiobiumTitanium)
                .input(spring, VanadiumGallium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[LuV])
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[ZPM])
                .input(ELECTRIC_PUMP_EV)
                .input(wireGtQuadruple, YttriumBariumCuprate)
                .input(wireGtOctal, VanadiumGallium)
                .input(springSmall, VanadiumGallium)
                .input(spring, YttriumBariumCuprate)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[ZPM])
                .duration(200).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(TRANSFORMER[UV])
                .input(ELECTRIC_PUMP_IV)
                .input(wireGtQuadruple, Europium)
                .input(wireGtOctal, YttriumBariumCuprate)
                .input(springSmall, YttriumBariumCuprate)
                .input(spring, Europium)
                .fluidInputs(Lubricant.getFluid(2000))
                .output(ADJUSTABLE_TRANSFORMER[UV])
                .duration(200).EUt(VA[UV]).buildAndRegister();

        // 4A Energy Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH[IV])
                .input(wireGtQuadruple, Tungsten, 2)
                .input(plate, TungstenSteel, 2)
                .output(ENERGY_INPUT_HATCH_4A[0])
                .duration(100).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH[LuV])
                .input(wireGtQuadruple, NiobiumTitanium, 2)
                .input(plate, RhodiumPlatedPalladium, 2)
                .output(ENERGY_INPUT_HATCH_4A[1])
                .duration(100).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH[ZPM])
                .input(wireGtQuadruple, VanadiumGallium, 2)
                .input(plate, NaquadahAlloy, 2)
                .output(ENERGY_INPUT_HATCH_4A[2])
                .duration(100).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH[UV])
                .input(wireGtQuadruple, YttriumBariumCuprate, 2)
                .input(plate, Livermorium, 2)
                .output(ENERGY_INPUT_HATCH_4A[3])
                .duration(100).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH[UHV])
                .input(wireGtQuadruple, Europium, 2)
                .input(plate, Neutronium, 2)
                .output(ENERGY_INPUT_HATCH_4A[4])
                .duration(100).EUt(VA[UV]).buildAndRegister();

        // 16A Energy Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH_4A[0])
                .input(TRANSFORMER[IV])
                .input(wireGtOctal, Tungsten, 2)
                .input(plate, TungstenSteel, 4)
                .output(ENERGY_INPUT_HATCH_16A[0])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH_4A[1])
                .input(TRANSFORMER[LuV])
                .input(wireGtOctal, NiobiumTitanium, 2)
                .input(plate, RhodiumPlatedPalladium, 4)
                .output(ENERGY_INPUT_HATCH_16A[1])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH_4A[2])
                .input(TRANSFORMER[ZPM])
                .input(wireGtOctal, VanadiumGallium, 2)
                .input(plate, NaquadahAlloy, 4)
                .output(ENERGY_INPUT_HATCH_16A[2])
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH_4A[3])
                .input(TRANSFORMER[UV])
                .input(wireGtOctal, YttriumBariumCuprate, 2)
                .input(plate, Livermorium, 4)
                .output(ENERGY_INPUT_HATCH_16A[3])
                .duration(200).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_INPUT_HATCH_16A[3])
                .input(wireGtOctal, Europium, 2)
                .input(plate, Neutronium, 4)
                .output(ENERGY_INPUT_HATCH_16A[4])
                .duration(200).EUt(VA[UV]).buildAndRegister();

        // 4A Dynamo Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH[IV])
                .input(wireGtQuadruple, Tungsten, 2)
                .input(plate, TungstenSteel, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[0])
                .duration(100).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH[LuV])
                .input(wireGtQuadruple, NiobiumTitanium, 2)
                .input(plate, RhodiumPlatedPalladium, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[1])
                .duration(100).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH[ZPM])
                .input(wireGtQuadruple, VanadiumGallium, 2)
                .input(plate, NaquadahAlloy, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[2])
                .duration(100).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH[UV])
                .input(wireGtQuadruple, YttriumBariumCuprate, 2)
                .input(plate, Livermorium, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[3])
                .duration(100).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH[UHV])
                .input(wireGtQuadruple, Europium, 2)
                .input(plate, Neutronium, 2)
                .output(ENERGY_OUTPUT_HATCH_4A[4])
                .duration(100).EUt(VA[UV]).buildAndRegister();

        // 16A Dynamo Hatches

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH_4A[0])
                .input(TRANSFORMER[IV])
                .input(wireGtOctal, Tungsten, 2)
                .input(plate, TungstenSteel, 4)
                .output(ENERGY_OUTPUT_HATCH_16A[0])
                .duration(200).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH_4A[1])
                .input(TRANSFORMER[LuV])
                .input(wireGtOctal, NiobiumTitanium, 2)
                .input(plate, RhodiumPlatedPalladium, 4)
                .output(ENERGY_OUTPUT_HATCH_16A[1])
                .duration(200).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH_4A[2])
                .input(TRANSFORMER[ZPM])
                .input(wireGtOctal, VanadiumGallium, 2)
                .input(plate, NaquadahAlloy, 4)
                .output(ENERGY_OUTPUT_HATCH_16A[2])
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH_4A[3])
                .input(TRANSFORMER[UV])
                .input(wireGtOctal, YttriumBariumCuprate, 2)
                .input(plate, Livermorium, 4)
                .output(ENERGY_OUTPUT_HATCH_16A[3])
                .duration(200).EUt(VA[ZPM]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ENERGY_OUTPUT_HATCH_16A[3])
                .input(wireGtOctal, Europium, 2)
                .input(plate, Neutronium, 4)
                .output(ENERGY_OUTPUT_HATCH_16A[4])
                .duration(200).EUt(VA[UV]).buildAndRegister();

        // Maintenance Hatch

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LV])
                .circuitMeta(1)
                .output(MAINTENANCE_HATCH)
                .duration(100).EUt(VA[LV]).buildAndRegister();

        // Multiblock Miners

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[EV])
                .input(frameGt, Titanium, 4)
                .input(circuit, Extreme, 4)
                .input(ELECTRIC_MOTOR_EV, 4)
                .input(ELECTRIC_PUMP_EV, 4)
                .input(CONVEYOR_MODULE_EV, 4)
                .input(gear, Tungsten, 2)
                .output(BASIC_LARGE_MINER)
                .duration(400).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(frameGt, TungstenSteel, 4)
                .input(circuit, Elite, 4)
                .input(ELECTRIC_MOTOR_IV, 4)
                .input(ELECTRIC_PUMP_IV, 4)
                .input(CONVEYOR_MODULE_IV, 4)
                .input(gear, Ultimet, 2)
                .output(LARGE_MINER)
                .duration(400).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[LuV])
                .input(frameGt, HSSS, 4)
                .input(circuit, Master, 4)
                .input(ELECTRIC_MOTOR_LUV, 4)
                .input(ELECTRIC_PUMP_LUV, 4)
                .input(CONVEYOR_MODULE_LUV, 4)
                .input(gear, Ruridit, 2)
                .output(ADVANCED_LARGE_MINER)
                .duration(400).EUt(VA[LuV]).buildAndRegister();
    }
}
