package gregtech.loaders.recipe.chemistry;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.UnificationEntry;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.MIXER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class MixerRecipes {

    public static void init() {
        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(NitricAcid.getFluid(1000))
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .fluidOutputs(NitrationMixture.getFluid(2000))
                .duration(500).EUt(2).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(PolyvinylAcetate.getFluid(1000))
                .fluidInputs(Acetone.getFluid(1500))
                .fluidOutputs(Glue.getFluid(2500))
                .duration(50).EUt(VA[ULV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(PolyvinylAcetate.getFluid(1000))
                .fluidInputs(MethylAcetate.getFluid(1500))
                .fluidOutputs(Glue.getFluid(2500))
                .duration(50).EUt(VA[ULV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .input(dust, Salt, 2)
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(SaltWater.getFluid(1000))
                .duration(40).EUt(VA[ULV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(BioDiesel.getFluid(1000))
                .fluidInputs(Tetranitromethane.getFluid(40))
                .fluidOutputs(CetaneBoostedDiesel.getFluid(750))
                .duration(20).EUt(VA[HV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(Diesel.getFluid(1000))
                .fluidInputs(Tetranitromethane.getFluid(20))
                .fluidOutputs(CetaneBoostedDiesel.getFluid(1000))
                .duration(20).EUt(VA[HV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(Dimethylhydrazine.getFluid(1000))
                .fluidOutputs(RocketFuel.getFluid(3000))
                .duration(60).EUt(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(DinitrogenTetroxide.getFluid(1000))
                .fluidInputs(Dimethylhydrazine.getFluid(1000))
                .fluidOutputs(RocketFuel.getFluid(6000))
                .duration(60).EUt(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(LightFuel.getFluid(5000))
                .fluidInputs(HeavyFuel.getFluid(1000))
                .fluidOutputs(Diesel.getFluid(6000))
                .duration(16).EUt(VA[MV]).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .input(dust, Clay)
                .input(dust, Stone, 3)
                .fluidInputs(Water.getFluid(500))
                .fluidOutputs(Concrete.getFluid(576))
                .duration(20).EUt(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .input(dust, Stone, 2)
                .input(dust, Calcite)
                .input(dust, Gypsum)
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(Concrete.getFluid(1152))
                .duration(40).EUt(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .input(dust, Stone, 2)
                .input(dust, Marble)
                .input(dust, Gypsum)
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(Concrete.getFluid(1152))
                .duration(40).EUt(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(Concrete.getFluid(576))
                .input(dust, RawRubber)
                .fluidOutputs(ConstructionFoam.getFluid(8000))
                .duration(20).EUt(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .input(dust, Stone)
                .fluidInputs(Lubricant.getFluid(20))
                .fluidInputs(Water.getFluid(4980))
                .fluidOutputs(DrillingFluid.getFluid(5000))
                .duration(64).EUt(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .input(dust, Stone)
                .fluidInputs(Lubricant.getFluid(20))
                .fluidInputs(DistilledWater.getFluid(4980))
                .fluidOutputs(DrillingFluid.getFluid(5000))
                .duration(48).EUt(16).buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(160).EUt(VA[HV])
                .input(dust, Beryllium)
                .input(dust, Potassium, 4)
                .fluidInputs(Nitrogen.getFluid(5000))
                .circuitMeta(1)
                .output(dust, EnderPearl, 10)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[HV])
                .fluidInputs(PolychlorinatedBiphenyl.getFluid(750))
                .fluidInputs(DistilledWater.getFluid(250))
                .fluidOutputs(PCBCoolant.getFluid(1000))
                .buildAndRegister();

        // Alloys
        ModHandler.addShapelessRecipe("dust_brass", OreDictUnifier.get(dust, Brass, 3),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Zinc));

        ModHandler.addShapelessRecipe("dust_bronze", OreDictUnifier.get(dust, Bronze, 3),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Tin));

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[ULV])
                .input(dust, Copper)
                .input(dust, Redstone, 4)
                .circuitMeta(2)
                .output(dust, RedAlloy)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[ULV])
                .input(dust, Silver)
                .input(dust, Electrotine, 4)
                .circuitMeta(2)
                .output(dust, BlueAlloy)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(dust, Redstone)
                .input(dust, Electrum)
                .circuitMeta(1)
                .output(dust, Electrotine)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(dust, Tin, 6)
                .input(dust, Lead, 3)
                .input(dust, Antimony)
                .circuitMeta(3)
                .output(dust, SolderingAlloy, 10)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(dust, Gallium)
                .input(dust, Arsenic)
                .circuitMeta(1)
                .output(dust, GalliumArsenide, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(dust, Gold)
                .input(dust, Silver)
                .circuitMeta(1)
                .output(dust, Electrum, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .circuitMeta(1)
                .input(dust, Boron)
                .input(dust, Glass, 7)
                .output(dust, BorosilicateGlass, 8)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                .input(dust, Indium)
                .input(dust, Gallium)
                .input(dust, Phosphorus)
                .circuitMeta(1)
                .output(dust, IndiumGalliumPhosphide, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(300).EUt(VA[ULV])
                .input(dust, Iron, 2)
                .input(dust, Nickel)
                .circuitMeta(1)
                .output(dust, Invar, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[ULV])
                .input(dust, Copper, 3)
                .input(dust, Tin)
                .circuitMeta(1)
                .output(dust, Bronze, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[ULV])
                .input(dust, Copper, 6)
                .input(dust, Tin, 2)
                .input(dust, Lead)
                .circuitMeta(3)
                .output(dust, Potin, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[ULV])
                .input(dust, Copper, 3)
                .input(dust, Zinc)
                .circuitMeta(1)
                .output(dust, Brass, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).EUt(VA[ULV])
                .input(dust, Bismuth)
                .input(dust, Brass, 4)
                .circuitMeta(1)
                .output(dust, BismuthBronze, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).EUt(VA[ULV])
                .input(dust, Copper, 3)
                .input(dust, Electrum, 2)
                .circuitMeta(1)
                .output(dust, BlackBronze, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).EUt(VA[ULV])
                .input(dust, BlackBronze)
                .input(dust, Nickel)
                .input(dust, Steel, 3)
                .circuitMeta(1)
                .output(dust, BlackSteel, 5)
                .buildAndRegister();

        // Black Steel shortcut
        // Normal recipe would be 125 ticks per recipe at HV overclock, plus 125 ticks for the Black Bronze step,
        // for a total of 750 ticks. This recipe is 5 batches at once, so is equivalent time but saves a step.
        MIXER_RECIPES.recipeBuilder().duration(750).EUt(VA[HV])
                .input(dust, Copper, 3)
                .input(dust, Gold)
                .input(dust, Silver)
                .input(dust, Nickel, 5)
                .input(dust, Steel, 15)
                .circuitMeta(2)
                .output(dust, BlackSteel, 25)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).EUt(VA[ULV])
                .input(dust, Saltpeter, 2)
                .input(dust, Sulfur)
                .input(dust, Coal, 3)
                .circuitMeta(1)
                .output(dust, Gunpowder, 6)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).EUt(VA[ULV])
                .input(dust, Saltpeter, 2)
                .input(dust, Sulfur)
                .input(dust, Charcoal, 3)
                .circuitMeta(1)
                .output(dust, Gunpowder, 6)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[ULV])
                .input(dust, Saltpeter, 2)
                .input(dust, Sulfur)
                .input(dust, Carbon, 3)
                .circuitMeta(1)
                .output(dust, Gunpowder, 6)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(800).EUt(VA[ULV])
                .input(dust, RoseGold)
                .input(dust, Brass)
                .input(dust, BlackSteel, 4)
                .input(dust, Steel, 2)
                .circuitMeta(1)
                .output(dust, BlueSteel, 8)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(800).EUt(VA[ULV])
                .input(dust, SterlingSilver)
                .input(dust, BismuthBronze)
                .input(dust, BlackSteel, 4)
                .input(dust, Steel, 2)
                .circuitMeta(1)
                .output(dust, RedSteel, 8)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(900).EUt(VA[ULV])
                .input(dust, Brass, 7)
                .input(dust, Aluminium)
                .input(dust, Cobalt)
                .circuitMeta(1)
                .output(dust, CobaltBrass, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(24)
                .input(dust, Copper)
                .input(dust, Nickel)
                .circuitMeta(1)
                .output(dust, Cupronickel, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[MV])
                .input(dust, Nickel)
                .input(dust, Zinc)
                .input(dust, Iron, 4)
                .circuitMeta(2)
                .output(dust, FerriteMixture, 6)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(300).EUt(VA[MV])
                .input(dust, Iron)
                .input(dust, Aluminium)
                .input(dust, Chrome)
                .circuitMeta(1)
                .output(dust, Kanthal, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).EUt(VA[MV])
                .input(dust, Nickel, 4)
                .input(dust, Chrome)
                .circuitMeta(2)
                .output(dust, Nichrome, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).EUt(VA[MV])
                .input(dust, Copper)
                .input(dust, Gold, 4)
                .circuitMeta(3)
                .output(dust, RoseGold, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).EUt(VA[MV])
                .input(dust, Iron, 4)
                .input(dust, Invar, 3)
                .input(dust, Manganese)
                .input(dust, Chrome)
                .circuitMeta(1)
                .output(dust, StainlessSteel, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).EUt(VA[MV])
                .input(dust, Iron, 6)
                .input(dust, Nickel)
                .input(dust, Manganese)
                .input(dust, Chrome)
                .circuitMeta(3)
                .output(dust, StainlessSteel, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[HV])
                .input(dust, Graphite)
                .input(dust, Silicon)
                .input(dust, Carbon, 4)
                .circuitMeta(1)
                .output(dust, Graphene)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[MV])
                .input(dust, Steel, 7)
                .input(dust, Vanadium)
                .input(dust, Chrome)
                .circuitMeta(1)
                .output(dust, VanadiumSteel, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(900).EUt(VA[HV])
                .input(dust, Cobalt, 5)
                .input(dust, Chrome, 2)
                .input(dust, Nickel)
                .input(dust, Molybdenum)
                .circuitMeta(1)
                .output(dust, Ultimet, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                .input(dust, Tungsten)
                .input(dust, Carbon)
                .circuitMeta(1)
                .output(dust, TungstenCarbide, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                .input(dust, Tungsten)
                .input(dust, Steel)
                .circuitMeta(1)
                .output(dust, TungstenSteel, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[EV])
                .input(dust, Vanadium, 3)
                .input(dust, Gallium)
                .circuitMeta(1)
                .output(dust, VanadiumGallium, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[EV])
                .input(dust, TungstenSteel, 5)
                .input(dust, Chrome)
                .input(dust, Molybdenum, 2)
                .input(dust, Vanadium)
                .circuitMeta(1)
                .output(dust, HSSG, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).EUt(VA[EV])
                .input(dust, Yttrium)
                .input(dust, Barium, 2)
                .input(dust, Copper, 3)
                .circuitMeta(2)
                .fluidInputs(Oxygen.getFluid(7000))
                .output(dust, YttriumBariumCuprate, 13)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(450).EUt(4046)
                .input(dust, HSSG, 6)
                .input(dust, Cobalt)
                .input(dust, Manganese)
                .input(dust, Silicon)
                .circuitMeta(1)
                .output(dust, HSSE, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[IV])
                .input(dust, Niobium)
                .input(dust, Titanium)
                .circuitMeta(1)
                .output(dust, NiobiumTitanium, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).EUt(VA[IV])
                .input(dust, HSSG, 6)
                .input(dust, Iridium, 2)
                .input(dust, Osmium)
                .circuitMeta(2)
                .output(dust, HSSS, 9)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[IV])
                .input(dust, Naquadah, 2)
                .input(dust, Osmiridium)
                .input(dust, Trinium)
                .circuitMeta(2)
                .output(dust, NaquadahAlloy, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(300).EUt(VA[LuV])
                .input(dust, Osmium)
                .input(dust, Iridium, 3)
                .circuitMeta(1)
                .output(dust, Osmiridium, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[IV])
                .input(dust, Palladium, 3)
                .input(dust, Rhodium)
                .circuitMeta(1)
                .output(dust, RhodiumPlatedPalladium, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[ULV])
                .input(dust, Aluminium, 2)
                .input(dust, Magnesium)
                .circuitMeta(1)
                .output(dust, Magnalium, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).EUt(VA[MV])
                .input(dust, Copper)
                .input(dust, Silver, 4)
                .circuitMeta(1)
                .output(dust, SterlingSilver, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[ULV])
                .input(dust, Tin)
                .input(dust, Iron)
                .circuitMeta(1)
                .output(dust, TinAlloy, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[ULV])
                .input(dust, Lead, 4)
                .input(dust, Antimony)
                .circuitMeta(1)
                .output(dust, BatteryAlloy, 5)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(350).EUt(4096)
                .input(dust, Ruthenium, 2)
                .input(dust, Iridium)
                .circuitMeta(1)
                .output(dust, Ruridit, 3)
                .buildAndRegister();

        // Superconductor Alloys
        MIXER_RECIPES.recipeBuilder().duration(400).EUt(24)
                .input(dust, Manganese)
                .input(dust, Phosphorus)
                .circuitMeta(4)
                .output(dust, ManganesePhosphide, 2)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).EUt(VA[MV])
                .input(dust, Magnesium)
                .input(dust, Boron, 2)
                .circuitMeta(4)
                .output(dust, MagnesiumDiboride, 3)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[MV])
                .input(dust, Barium, 2)
                .input(dust, Calcium, 2)
                .input(dust, Copper, 3)
                .fluidInputs(Mercury.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(8000))
                .circuitMeta(4)
                .output(dust, MercuryBariumCalciumCuprate, 16)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[EV])
                .input(dust, Uranium)
                .input(dust, Platinum, 3)
                .circuitMeta(4)
                .output(dust, UraniumTriplatinum, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(100).EUt(VA[IV])
                .input(dust, Samarium)
                .input(dust, Iron)
                .input(dust, Arsenic)
                .fluidInputs(Oxygen.getFluid(1000))
                .circuitMeta(4)
                .output(dust, SamariumIronArsenicOxide, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(600).EUt(VA[LuV])
                .input(dust, Indium, 4)
                .input(dust, Tin, 2)
                .input(dust, Barium, 2)
                .input(dust, Titanium)
                .input(dust, Copper, 7)
                .fluidInputs(Oxygen.getFluid(14000))
                .circuitMeta(4)
                .output(dust, IndiumTinBariumTitaniumCuprate, 16)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(150).EUt(VA[ZPM])
                .input(dust, Uranium)
                .input(dust, Rhodium)
                .input(dust, Naquadah, 2)
                .circuitMeta(4)
                .output(dust, UraniumRhodiumDinaquadide, 4)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(175).EUt(VA[UV])
                .input(dust, NaquadahEnriched, 4)
                .input(dust, Trinium, 3)
                .input(dust, Europium, 2)
                .input(dust, Duranium)
                .circuitMeta(4)
                .output(dust, EnrichedNaquadahTriniumEuropiumDuranide, 10)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(400).EUt(VA[UV])
                .input(dust, Ruthenium)
                .input(dust, Trinium, 2)
                .input(dust, Americium)
                .input(dust, Neutronium, 2)
                .fluidInputs(Oxygen.getFluid(8000))
                .circuitMeta(4)
                .output(dust, RutheniumTriniumAmericiumNeutronate, 14)
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(300).EUt(VA[EV])
                .input(dust, Ruthenium, 4)
                .input(dust, Tungsten, 2)
                .input(dust, Molybdenum)
                .circuitMeta(1)
                .output(dust, RTMAlloy, 7)
                .buildAndRegister();
    }
}
