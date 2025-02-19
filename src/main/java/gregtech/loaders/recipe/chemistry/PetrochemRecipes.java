package gregtech.loaders.recipe.chemistry;

import gregtech.api.unification.material.Material;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class PetrochemRecipes {

    public static void init() {
        moderatelyCrack(Ethane, HydroCrackedEthane, SteamCrackedEthane);
        moderatelyCrack(Ethylene, HydroCrackedEthylene, SteamCrackedEthylene);
        moderatelyCrack(Propene, HydroCrackedPropene, SteamCrackedPropene);
        moderatelyCrack(Propane, HydroCrackedPropane, SteamCrackedPropane);
        moderatelyCrack(Butane, HydroCrackedButane, SteamCrackedButane);
        moderatelyCrack(Butene, HydroCrackedButene, SteamCrackedButene);
        moderatelyCrack(Butadiene, HydroCrackedButadiene, SteamCrackedButadiene);

        lightlyCrack(HeavyFuel, LightlyHydroCrackedHeavyFuel, LightlySteamCrackedHeavyFuel);
        severelyCrack(HeavyFuel, SeverelyHydroCrackedHeavyFuel, SeverelySteamCrackedHeavyFuel);
        lightlyCrack(LightFuel, LightlyHydroCrackedLightFuel, LightlySteamCrackedLightFuel);
        severelyCrack(LightFuel, SeverelyHydroCrackedLightFuel, SeverelySteamCrackedLightFuel);
        lightlyCrack(Naphtha, LightlyHydroCrackedNaphtha, LightlySteamCrackedNaphtha);
        severelyCrack(Naphtha, SeverelyHydroCrackedNaphtha, SeverelySteamCrackedNaphtha);
        lightlyCrack(RefineryGas, LightlyHydroCrackedGas, LightlySteamCrackedGas);
        severelyCrack(RefineryGas, SeverelyHydroCrackedGas, SeverelySteamCrackedGas);

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(Oil.getFluid(50))
                .fluidOutputs(SulfuricHeavyFuel.getFluid(15))
                .fluidOutputs(SulfuricLightFuel.getFluid(50))
                .fluidOutputs(SulfuricNaphtha.getFluid(20))
                .fluidOutputs(SulfuricGas.getFluid(60))
                .duration(20).EUt(96).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(OilLight.getFluid(150))
                .fluidOutputs(SulfuricHeavyFuel.getFluid(10))
                .fluidOutputs(SulfuricLightFuel.getFluid(20))
                .fluidOutputs(SulfuricNaphtha.getFluid(30))
                .fluidOutputs(SulfuricGas.getFluid(240))
                .duration(20).EUt(96).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(OilHeavy.getFluid(100))
                .fluidOutputs(SulfuricHeavyFuel.getFluid(250))
                .fluidOutputs(SulfuricLightFuel.getFluid(45))
                .fluidOutputs(SulfuricNaphtha.getFluid(15))
                .fluidOutputs(SulfuricGas.getFluid(60))
                .duration(20).EUt(288).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(RawOil.getFluid(100))
                .fluidOutputs(SulfuricHeavyFuel.getFluid(10))
                .fluidOutputs(SulfuricLightFuel.getFluid(50))
                .fluidOutputs(SulfuricNaphtha.getFluid(150))
                .fluidOutputs(SulfuricGas.getFluid(60))
                .duration(20).EUt(96).buildAndRegister();

        desulfurizationRecipes();
        distillationRecipes();
        distilleryRecipes();
    }

    private static void desulfurizationRecipes() {
        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfuricHeavyFuel.getFluid(8000))
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(HydrogenSulfide.getFluid(1000))
                .fluidOutputs(HeavyFuel.getFluid(8000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfuricLightFuel.getFluid(12000))
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(HydrogenSulfide.getFluid(1000))
                .fluidOutputs(LightFuel.getFluid(12000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfuricNaphtha.getFluid(12000))
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(HydrogenSulfide.getFluid(1000))
                .fluidOutputs(Naphtha.getFluid(12000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfuricGas.getFluid(16000))
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(HydrogenSulfide.getFluid(1000))
                .fluidOutputs(RefineryGas.getFluid(16000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(NaturalGas.getFluid(16000))
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(HydrogenSulfide.getFluid(1000))
                .fluidOutputs(RefineryGas.getFluid(16000))
                .duration(160).EUt(VA[LV]).buildAndRegister();
    }

    private static void distillationRecipes() {
        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(RefineryGas.getFluid(1000))
                .fluidOutputs(Butane.getFluid(60))
                .fluidOutputs(Propane.getFluid(70))
                .fluidOutputs(Ethane.getFluid(100))
                .fluidOutputs(Methane.getFluid(750))
                .fluidOutputs(Helium.getFluid(20))
                .duration(240).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedEthane.getFluid(1000))
                .fluidOutputs(Methane.getFluid(2000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedEthane.getFluid(1000))
                .chancedOutput(dust, Carbon, 2500, 0)
                .fluidOutputs(Ethylene.getFluid(250))
                .fluidOutputs(Methane.getFluid(1250))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedEthylene.getFluid(1000))
                .fluidOutputs(Ethane.getFluid(1000))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedEthylene.getFluid(1000))
                .output(dust, Carbon)
                .fluidOutputs(Methane.getFluid(1000))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedPropene.getFluid(1000))
                .fluidOutputs(Propane.getFluid(500))
                .fluidOutputs(Ethylene.getFluid(500))
                .fluidOutputs(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedPropene.getFluid(1000))
                .chancedOutput(dust, Carbon, 5000, 0)
                .fluidOutputs(Ethylene.getFluid(1000))
                .fluidOutputs(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedPropane.getFluid(1000))
                .fluidOutputs(Ethane.getFluid(1000))
                .fluidOutputs(Methane.getFluid(1000))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedPropane.getFluid(1000))
                .chancedOutput(dust, Carbon, 2500, 0)
                .fluidOutputs(Ethylene.getFluid(750))
                .fluidOutputs(Methane.getFluid(1250))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedButane.getFluid(1000))
                .fluidOutputs(Propane.getFluid(750))
                .fluidOutputs(Ethane.getFluid(750))
                .fluidOutputs(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedButane.getFluid(1000))
                .chancedOutput(dust, Carbon, 2500, 0)
                .fluidOutputs(Propane.getFluid(125))
                .fluidOutputs(Ethane.getFluid(750))
                .fluidOutputs(Ethylene.getFluid(750))
                .fluidOutputs(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedButene.getFluid(750))
                .fluidOutputs(Butane.getFluid(500))
                .fluidOutputs(Propene.getFluid(250))
                .fluidOutputs(Ethane.getFluid(250))
                .fluidOutputs(Ethylene.getFluid(250))
                .fluidOutputs(Methane.getFluid(250))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedButene.getFluid(1000))
                .chancedOutput(dust, Carbon, 2500, 0)
                .fluidOutputs(Propene.getFluid(250))
                .fluidOutputs(Ethylene.getFluid(1500))
                .fluidOutputs(Methane.getFluid(250))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(HydroCrackedButadiene.getFluid(1000))
                .fluidOutputs(Butene.getFluid(750))
                .fluidOutputs(Ethylene.getFluid(500))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SteamCrackedButadiene.getFluid(1000))
                .chancedOutput(dust, Carbon, 5000, 0)
                .fluidOutputs(Propene.getFluid(125))
                .fluidOutputs(Ethylene.getFluid(250))
                .fluidOutputs(Methane.getFluid(1125))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(LightlyHydroCrackedHeavyFuel.getFluid(1000))
                .fluidOutputs(LightFuel.getFluid(600))
                .fluidOutputs(Naphtha.getFluid(100))
                .fluidOutputs(Butane.getFluid(100))
                .fluidOutputs(Propane.getFluid(100))
                .fluidOutputs(Ethane.getFluid(75))
                .fluidOutputs(Methane.getFluid(75))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SeverelyHydroCrackedHeavyFuel.getFluid(1000))
                .fluidOutputs(LightFuel.getFluid(200))
                .fluidOutputs(Naphtha.getFluid(250))
                .fluidOutputs(Butane.getFluid(300))
                .fluidOutputs(Propane.getFluid(300))
                .fluidOutputs(Ethane.getFluid(175))
                .fluidOutputs(Methane.getFluid(175))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(LightlySteamCrackedHeavyFuel.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/9", 0)
                .fluidOutputs(LightFuel.getFluid(300))
                .fluidOutputs(Naphtha.getFluid(50))
                .fluidOutputs(Toluene.getFluid(25))
                .fluidOutputs(Benzene.getFluid(125))
                .fluidOutputs(Butene.getFluid(25))
                .fluidOutputs(Butadiene.getFluid(15))
                .fluidOutputs(Propane.getFluid(3))
                .fluidOutputs(Propene.getFluid(30))
                .fluidOutputs(Ethane.getFluid(5))
                .fluidOutputs(Ethylene.getFluid(50))
                .fluidOutputs(Methane.getFluid(50))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SeverelySteamCrackedHeavyFuel.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/3", 0)
                .fluidOutputs(LightFuel.getFluid(100))
                .fluidOutputs(Naphtha.getFluid(125))
                .fluidOutputs(Toluene.getFluid(80))
                .fluidOutputs(Benzene.getFluid(400))
                .fluidOutputs(Butene.getFluid(80))
                .fluidOutputs(Butadiene.getFluid(50))
                .fluidOutputs(Propane.getFluid(10))
                .fluidOutputs(Propene.getFluid(100))
                .fluidOutputs(Ethane.getFluid(15))
                .fluidOutputs(Ethylene.getFluid(150))
                .fluidOutputs(Methane.getFluid(150))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(LightlyHydroCrackedLightFuel.getFluid(1000))
                .fluidOutputs(Naphtha.getFluid(800))
                .fluidOutputs(Octane.getFluid(100))
                .fluidOutputs(Butane.getFluid(150))
                .fluidOutputs(Propane.getFluid(200))
                .fluidOutputs(Ethane.getFluid(125))
                .fluidOutputs(Methane.getFluid(125))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SeverelyHydroCrackedLightFuel.getFluid(1000))
                .fluidOutputs(Naphtha.getFluid(200))
                .fluidOutputs(Octane.getFluid(20))
                .fluidOutputs(Butane.getFluid(125))
                .fluidOutputs(Propane.getFluid(125))
                .fluidOutputs(Ethane.getFluid(1500))
                .fluidOutputs(Methane.getFluid(1500))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(LightlySteamCrackedLightFuel.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/9", 0)
                .fluidOutputs(HeavyFuel.getFluid(150))
                .fluidOutputs(Naphtha.getFluid(400))
                .fluidOutputs(Toluene.getFluid(40))
                .fluidOutputs(Benzene.getFluid(200))
                .fluidOutputs(Butene.getFluid(75))
                .fluidOutputs(Butadiene.getFluid(60))
                .fluidOutputs(Propane.getFluid(20))
                .fluidOutputs(Propene.getFluid(150))
                .fluidOutputs(Ethane.getFluid(10))
                .fluidOutputs(Ethylene.getFluid(50))
                .fluidOutputs(Methane.getFluid(50))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SeverelySteamCrackedLightFuel.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/3", 0)
                .fluidOutputs(HeavyFuel.getFluid(50))
                .fluidOutputs(Naphtha.getFluid(100))
                .fluidOutputs(Toluene.getFluid(30))
                .fluidOutputs(Benzene.getFluid(150))
                .fluidOutputs(Butene.getFluid(65))
                .fluidOutputs(Butadiene.getFluid(50))
                .fluidOutputs(Propane.getFluid(50))
                .fluidOutputs(Propene.getFluid(250))
                .fluidOutputs(Ethane.getFluid(50))
                .fluidOutputs(Ethylene.getFluid(250))
                .fluidOutputs(Methane.getFluid(250))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(LightlyHydroCrackedNaphtha.getFluid(1000))
                .fluidOutputs(Butane.getFluid(800))
                .fluidOutputs(Propane.getFluid(300))
                .fluidOutputs(Ethane.getFluid(250))
                .fluidOutputs(Methane.getFluid(250))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SeverelyHydroCrackedNaphtha.getFluid(1000))
                .fluidOutputs(Butane.getFluid(125))
                .fluidOutputs(Propane.getFluid(125))
                .fluidOutputs(Ethane.getFluid(1500))
                .fluidOutputs(Methane.getFluid(1500))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(LightlySteamCrackedNaphtha.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/9", 0)
                .fluidOutputs(HeavyFuel.getFluid(75))
                .fluidOutputs(LightFuel.getFluid(150))
                .fluidOutputs(Toluene.getFluid(40))
                .fluidOutputs(Benzene.getFluid(150))
                .fluidOutputs(Butene.getFluid(80))
                .fluidOutputs(Butadiene.getFluid(150))
                .fluidOutputs(Propane.getFluid(15))
                .fluidOutputs(Propene.getFluid(200))
                .fluidOutputs(Ethane.getFluid(35))
                .fluidOutputs(Ethylene.getFluid(200))
                .fluidOutputs(Methane.getFluid(200))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SeverelySteamCrackedNaphtha.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/3", 0)
                .fluidOutputs(HeavyFuel.getFluid(25))
                .fluidOutputs(LightFuel.getFluid(50))
                .fluidOutputs(Toluene.getFluid(20))
                .fluidOutputs(Benzene.getFluid(100))
                .fluidOutputs(Butene.getFluid(50))
                .fluidOutputs(Butadiene.getFluid(50))
                .fluidOutputs(Propane.getFluid(15))
                .fluidOutputs(Propene.getFluid(300))
                .fluidOutputs(Ethane.getFluid(65))
                .fluidOutputs(Ethylene.getFluid(500))
                .fluidOutputs(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(LightlyHydroCrackedGas.getFluid(1000))
                .fluidOutputs(Methane.getFluid(1400))
                .fluidOutputs(Hydrogen.getFluid(1340))
                .fluidOutputs(Helium.getFluid(20))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SeverelyHydroCrackedGas.getFluid(1000))
                .fluidOutputs(Methane.getFluid(1400))
                .fluidOutputs(Hydrogen.getFluid(4340))
                .fluidOutputs(Helium.getFluid(20))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(LightlySteamCrackedGas.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/9", 0)
                .fluidOutputs(Propene.getFluid(45))
                .fluidOutputs(Ethane.getFluid(8))
                .fluidOutputs(Ethylene.getFluid(85))
                .fluidOutputs(Methane.getFluid(1026))
                .fluidOutputs(Helium.getFluid(20))
                .duration(120).EUt(VA[MV]).buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(SeverelySteamCrackedGas.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/9", 0)
                .fluidOutputs(Propene.getFluid(8))
                .fluidOutputs(Ethane.getFluid(45))
                .fluidOutputs(Ethylene.getFluid(92))
                .fluidOutputs(Methane.getFluid(1018))
                .fluidOutputs(Helium.getFluid(20))
                .duration(120).EUt(VA[MV]).buildAndRegister();
    }

    private static void distilleryRecipes() {
        DISTILLERY_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Toluene.getFluid(30))
                .fluidOutputs(LightFuel.getFluid(30))
                .duration(160).EUt(24).buildAndRegister();

        DISTILLERY_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(HeavyFuel.getFluid(10))
                .fluidOutputs(Toluene.getFluid(4))
                .duration(16).EUt(24).buildAndRegister();

        DISTILLERY_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(HeavyFuel.getFluid(10))
                .fluidOutputs(Benzene.getFluid(4))
                .duration(16).EUt(24).buildAndRegister();

        DISTILLERY_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .fluidInputs(HeavyFuel.getFluid(20))
                .fluidOutputs(Phenol.getFluid(5))
                .duration(32).EUt(24).buildAndRegister();
    }

    private static void lightlyCrack(Material raw, Material hydroCracked, Material steamCracked) {
        CRACKING_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(raw.getFluid(1000))
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(hydroCracked.getFluid(1000))
                .duration(80).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(raw.getFluid(500))
                .fluidInputs(Hydrogen.getFluid(1000))
                .fluidOutputs(hydroCracked.getFluid(250))
                .duration(80).EUt(30).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(raw.getFluid(1000))
                .fluidInputs(Steam.getFluid(1000))
                .fluidOutputs(steamCracked.getFluid(1000))
                .duration(80).EUt(240).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(raw.getFluid(1000))
                .fluidInputs(Steam.getFluid(1000))
                .fluidOutputs(steamCracked.getFluid(500))
                .duration(160).duration(VA[LV]).buildAndRegister();
    }

    private static void moderatelyCrack(Material raw, Material hydroCracked, Material steamCracked) {
        CRACKING_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(raw.getFluid(1000))
                .fluidInputs(Hydrogen.getFluid(4000))
                .fluidOutputs(hydroCracked.getFluid(1000))
                .duration(120).EUt(180).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(raw.getFluid(500))
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(hydroCracked.getFluid(250))
                .duration(60).EUt(VA[LV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(raw.getFluid(1000))
                .fluidInputs(Steam.getFluid(1000))
                .fluidOutputs(steamCracked.getFluid(1000))
                .duration(120).EUt(360).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(raw.getFluid(1000))
                .fluidInputs(Steam.getFluid(1000))
                .fluidOutputs(steamCracked.getFluid(500))
                .duration(240).EUt(VA[LV]).buildAndRegister();
    }

    private static void severelyCrack(Material raw, Material hydroCracked, Material steamCracked) {
        CRACKING_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(raw.getFluid(1000))
                .fluidInputs(Hydrogen.getFluid(6000))
                .fluidOutputs(hydroCracked.getFluid(1000))
                .duration(160).EUt(240).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(raw.getFluid(500))
                .fluidInputs(Hydrogen.getFluid(3000))
                .fluidOutputs(hydroCracked.getFluid(250))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CRACKING_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .fluidInputs(raw.getFluid(1000))
                .fluidInputs(Steam.getFluid(1000))
                .fluidOutputs(steamCracked.getFluid(1000))
                .duration(160).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .fluidInputs(raw.getFluid(1000))
                .fluidInputs(Steam.getFluid(1000))
                .fluidOutputs(steamCracked.getFluid(500))
                .duration(240).EUt(VA[LV]).buildAndRegister();
    }
}
