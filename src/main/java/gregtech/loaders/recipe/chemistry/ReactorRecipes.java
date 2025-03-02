package gregtech.loaders.recipe.chemistry;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class ReactorRecipes {

    public static void init() {
        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Isoprene.getFluid(144))
                .fluidInputs(Air.getFluid(2000))
                .output(dust, RawRubber)
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Isoprene.getFluid(144))
                .fluidInputs(Oxygen.getFluid(2000))
                .output(dust, RawRubber, 3)
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .fluidInputs(Propene.getFluid(2000))
                .fluidOutputs(Methane.getFluid(1000))
                .fluidOutputs(Isoprene.getFluid(1000))
                .duration(120).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .input(dust, Carbon)
                .fluidInputs(Hydrogen.getFluid(4000))
                .fluidOutputs(Methane.getFluid(1000))
                .duration(3500).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Ethylene.getFluid(1000))
                .fluidInputs(Propene.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(2000))
                .fluidOutputs(Isoprene.getFluid(1000))
                .duration(120).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Sodium, 2)
                .input(dust, Sulfur)
                .output(dust, SodiumSulfide, 3)
                .duration(60).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumSulfide, 3)
                .fluidInputs(Dichlorobenzene.getFluid(1000))
                .fluidInputs(Air.getFluid(16000))
                .output(dust, Salt, 4)
                .fluidOutputs(PolyphenyleneSulfide.getFluid(1000))
                .duration(240).EUt(360).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumSulfide, 3)
                .fluidInputs(Dichlorobenzene.getFluid(1000))
                .fluidInputs(Oxygen.getFluid(8000))
                .output(dust, Salt, 4)
                .fluidOutputs(PolyphenyleneSulfide.getFluid(1500))
                .duration(240).EUt(360).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Polydimethylsiloxane, 9)
                .input(dust, Sulfur)
                .fluidOutputs(SiliconeRubber.getFluid(1296))
                .duration(600).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Dimethyldichlorosilane.getFluid(1000))
                .fluidInputs(Water.getFluid(1000))
                .output(dust, Polydimethylsiloxane, 3)
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(1000))
                .duration(240).EUt(96).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Chlorine.getFluid(1000))
                .fluidInputs(Hydrogen.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .duration(60).EUt(VA[ULV]).buildAndRegister();

        // NaCl + H2SO4 -> NaHSO4 + HCl
        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Salt, 2)
                .circuitMeta(1)
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .output(dust, SodiumBisulfate, 7)
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .duration(60).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Iron)
                .fluidInputs(HydrochloricAcid.getFluid(3000))
                .circuitMeta(1)
                .fluidOutputs(Iron3Chloride.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(3000))
                .duration(400).EUt(VA[LV])
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Chlorine.getFluid(4000))
                .fluidInputs(Benzene.getFluid(1000))
                .circuitMeta(2)
                .fluidOutputs(HydrochloricAcid.getFluid(2000))
                .fluidOutputs(Dichlorobenzene.getFluid(1000))
                .duration(120).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(NitrationMixture.getFluid(3000))
                .fluidInputs(Glycerol.getFluid(1000))
                .fluidOutputs(GlycerylTrinitrate.getFluid(1000))
                .fluidOutputs(DilutedSulfuricAcid.getFluid(3000))
                .duration(180).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .fluidInputs(AceticAcid.getFluid(1000))
                .fluidOutputs(Ethenone.getFluid(1000))
                .fluidOutputs(DilutedSulfuricAcid.getFluid(1000))
                .duration(160).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Calcite, 5)
                .fluidInputs(AceticAcid.getFluid(2000))
                .fluidOutputs(DissolvedCalciumAcetate.getFluid(1000))
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(200).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Quicklime, 2)
                .fluidInputs(AceticAcid.getFluid(2000))
                .circuitMeta(1)
                .fluidOutputs(DissolvedCalciumAcetate.getFluid(1000))
                .duration(400).EUt(380).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Calcium)
                .fluidInputs(AceticAcid.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(DissolvedCalciumAcetate.getFluid(1000))
                .duration(400).EUt(380).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Methanol.getFluid(1000))
                .fluidInputs(AceticAcid.getFluid(1000))
                .circuitMeta(1)
                .fluidOutputs(MethylAcetate.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .duration(240).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Sulfur)
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(HydrogenSulfide.getFluid(1000))
                .duration(60).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Air.getFluid(1000))
                .fluidInputs(VinylAcetate.getFluid(144))
                .fluidOutputs(PolyvinylAcetate.getFluid(144))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(VinylAcetate.getFluid(144))
                .fluidOutputs(PolyvinylAcetate.getFluid(216))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(Air.getFluid(7500))
                .fluidInputs(VinylAcetate.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(PolyvinylAcetate.getFluid(3240))
                .duration(800).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(Oxygen.getFluid(7500))
                .fluidInputs(VinylAcetate.getFluid(2160))
                .fluidInputs(TitaniumTetrachloride.getFluid(100))
                .fluidOutputs(PolyvinylAcetate.getFluid(4320))
                .duration(800).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(6000))
                .fluidInputs(CarbonDioxide.getFluid(1000))
                .circuitMeta(2)
                .fluidOutputs(Water.getFluid(1000))
                .fluidOutputs(Methanol.getFluid(1000))
                .duration(120).EUt(96).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Hydrogen.getFluid(4000))
                .fluidInputs(CarbonMonoxide.getFluid(1000))
                .fluidOutputs(Methanol.getFluid(1000))
                .duration(120).EUt(96).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .input(dust, Carbon)
                .fluidInputs(Hydrogen.getFluid(4000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(Methanol.getFluid(1000))
                .duration(320).EUt(96).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Mercury.getFluid(1000))
                .fluidInputs(Water.getFluid(10000))
                .fluidInputs(Chlorine.getFluid(10000))
                .fluidOutputs(HypochlorousAcid.getFluid(10000))
                .duration(600).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Water.getFluid(1000))
                .fluidInputs(Chlorine.getFluid(2000))
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(1000))
                .fluidOutputs(HypochlorousAcid.getFluid(1000))
                .duration(120).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Dimethylamine.getFluid(1000))
                .fluidInputs(Monochloramine.getFluid(1000))
                .fluidOutputs(Dimethylhydrazine.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(1000))
                .duration(960).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Methanol.getFluid(2000))
                .fluidInputs(Ammonia.getFluid(2000))
                .fluidInputs(HypochlorousAcid.getFluid(1000))
                .fluidOutputs(Dimethylhydrazine.getFluid(1000))
                .fluidOutputs(DilutedHydrochloricAcid.getFluid(2000))
                .duration(1040).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(1000))
                .fluidInputs(Fluorine.getFluid(1000))
                .fluidOutputs(HydrofluoricAcid.getFluid(1000))
                .duration(60).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(PhosphoricAcid.getFluid(1000))
                .fluidInputs(Benzene.getFluid(8000))
                .fluidInputs(Propene.getFluid(8000))
                .fluidOutputs(Cumene.getFluid(8000))
                .duration(1920).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Silicon)
                .fluidInputs(Chloromethane.getFluid(2000))
                .fluidOutputs(Dimethyldichlorosilane.getFluid(1000))
                .duration(240).EUt(96).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidInputs(Ethylene.getFluid(1000))
                .fluidOutputs(AceticAcid.getFluid(1000))
                .duration(100).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(CarbonMonoxide.getFluid(1000))
                .fluidInputs(Methanol.getFluid(1000))
                .fluidOutputs(AceticAcid.getFluid(1000))
                .duration(300).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(Hydrogen.getFluid(4000))
                .fluidInputs(CarbonMonoxide.getFluid(2000))
                .fluidOutputs(AceticAcid.getFluid(1000))
                .duration(320).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(4)
                .input(dust, Carbon, 2)
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidInputs(Hydrogen.getFluid(4000))
                .fluidOutputs(AceticAcid.getFluid(1000))
                .duration(480).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .input(dust, Aluminium, 4)
                .fluidInputs(IndiumConcentrate.getFluid(1000))
                .output(dustSmall, Indium)
                .output(dust, AluminiumSulfite, 4)
                .fluidOutputs(LeadZincSolution.getFluid(1000))
                .duration(50).EUt(600).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(4)
                .input(dust, Aluminium, 16)
                .fluidInputs(IndiumConcentrate.getFluid(4000))
                .output(dust, Indium)
                .output(dust, AluminiumSulfite, 16)
                .fluidOutputs(LeadZincSolution.getFluid(4000))
                .duration(200).EUt(600).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidInputs(AceticAcid.getFluid(1000))
                .fluidInputs(Ethylene.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .fluidOutputs(VinylAcetate.getFluid(1000))
                .duration(180).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .input(dust, Carbon)
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(CarbonMonoxide.getFluid(1000))
                .duration(40).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .input(gem, Charcoal)
                .fluidInputs(Oxygen.getFluid(1000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .fluidOutputs(CarbonMonoxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .input(gem, Coal)
                .fluidInputs(Oxygen.getFluid(1000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .fluidOutputs(CarbonMonoxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .input(dust, Charcoal)
                .fluidInputs(Oxygen.getFluid(1000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .fluidOutputs(CarbonMonoxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .duration(80).EUt(VA[ULV])
                .input(dust, Coal)
                .circuitMeta(1)
                .fluidInputs(Oxygen.getFluid(1000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .fluidOutputs(CarbonMonoxide.getFluid(1000))
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Carbon)
                .fluidInputs(CarbonDioxide.getFluid(1000))
                .fluidOutputs(CarbonMonoxide.getFluid(2000))
                .duration(800).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(HypochlorousAcid.getFluid(1000))
                .fluidInputs(Ammonia.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .circuitMeta(1)
                .fluidOutputs(Monochloramine.getFluid(1000))
                .duration(160).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(Ammonia.getFluid(1000))
                .fluidInputs(Methanol.getFluid(2000))
                .fluidOutputs(Water.getFluid(2000))
                .fluidOutputs(Dimethylamine.getFluid(1000))
                .duration(240).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(HydrochloricAcid.getFluid(1000))
                .fluidInputs(Methanol.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000))
                .fluidOutputs(Chloromethane.getFluid(1000))
                .duration(80).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .input(dust, Carbon)
                .fluidInputs(Oxygen.getFluid(2000))
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(40).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .input(gem, Charcoal)
                .fluidInputs(Oxygen.getFluid(2000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .input(gem, Coal)
                .fluidInputs(Oxygen.getFluid(2000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .input(dust, Charcoal)
                .fluidInputs(Oxygen.getFluid(2000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .input(dust, Coal)
                .fluidInputs(Oxygen.getFluid(2000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .fluidInputs(Water.getFluid(2000))
                .fluidInputs(Methane.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(8000))
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(150).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(MethylAcetate.getFluid(2000))
                .fluidInputs(NitricAcid.getFluid(4000))
                .output(dust, Carbon, 5)
                .fluidOutputs(Tetranitromethane.getFluid(1000))
                .fluidOutputs(Water.getFluid(8000))
                .duration(480).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(NitricAcid.getFluid(8000))
                .fluidInputs(Ethenone.getFluid(1000))
                .fluidOutputs(Tetranitromethane.getFluid(2000))
                .fluidOutputs(Water.getFluid(5000))
                .duration(480).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .fluidInputs(Oxygen.getFluid(7000))
                .fluidInputs(Ammonia.getFluid(2000))
                .fluidOutputs(DinitrogenTetroxide.getFluid(1000))
                .fluidOutputs(Water.getFluid(3000))
                .duration(480).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .fluidInputs(NitrogenDioxide.getFluid(2000))
                .fluidOutputs(DinitrogenTetroxide.getFluid(1000))
                .duration(640).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumHydroxide, 3)
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .output(dust, SodiumBisulfate, 7)
                .fluidOutputs(Water.getFluid(1000))
                .duration(60).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.SUGAR, 9))
                .input(dust, Polyethylene)
                .fluidInputs(Toluene.getFluid(1000))
                .outputs(MetaItems.GELLED_TOLUENE.getStackForm(20))
                .duration(140).EUt(192).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Calcium)
                .input(dust, Carbon)
                .fluidInputs(Oxygen.getFluid(3000))
                .output(dust, Calcite, 5)
                .duration(500).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Magnesia, 2)
                .fluidInputs(CarbonDioxide.getFluid(1000))
                .output(dust, Magnesite, 5)
                .duration(80).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Magnesite, 5)
                .output(dust, Magnesia, 2)
                .fluidOutputs(CarbonDioxide.getFluid(1000))
                .duration(240).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, RawRubber, 9)
                .input(dust, Sulfur)
                .fluidOutputs(Rubber.getFluid(1296))
                .duration(600).EUt(16).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.MELON, 1, OreDictionary.WILDCARD_VALUE))
                .input(nugget, Gold, 8)
                .outputs(new ItemStack(Items.SPECKLED_MELON))
                .duration(50).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.CARROT, 1, OreDictionary.WILDCARD_VALUE))
                .input(nugget, Gold, 8)
                .outputs(new ItemStack(Items.GOLDEN_CARROT))
                .duration(50).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.APPLE, 1, OreDictionary.WILDCARD_VALUE))
                .input(ingot, Gold, 8)
                .outputs(new ItemStack(Items.GOLDEN_APPLE))
                .duration(50).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.APPLE, 1, OreDictionary.WILDCARD_VALUE))
                .input(block, Gold, 8)
                .outputs(new ItemStack(Items.GOLDEN_APPLE, 1, 1))
                .duration(50).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.BLAZE_POWDER))
                .inputs(new ItemStack(Items.SLIME_BALL))
                .outputs(new ItemStack(Items.MAGMA_CREAM))
                .duration(50).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(MetaItems.GELLED_TOLUENE.getStackForm(4))
                .fluidInputs(SulfuricAcid.getFluid(250))
                .outputs(new ItemStack(Blocks.TNT))
                .duration(200).EUt(24).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(MetaItems.GELLED_TOLUENE.getStackForm(4))
                .fluidInputs(NitrationMixture.getFluid(200))
                .outputs(new ItemStack(MetaBlocks.ITNT))
                .fluidOutputs(DilutedSulfuricAcid.getFluid(150))
                .duration(80).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, SodiumHydroxide, 6)
                .fluidInputs(Dichlorobenzene.getFluid(1000))
                .output(dust, Salt, 4)
                .fluidOutputs(Phenol.getFluid(1000))
                .fluidOutputs(Oxygen.getFluid(1000))
                .duration(120).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(MethylAcetate.getFluid(1000))
                .fluidInputs(Water.getFluid(1000))
                .notConsumable(OreDictUnifier.get(dust, SodiumHydroxide))
                .fluidOutputs(AceticAcid.getFluid(1000))
                .fluidOutputs(Methanol.getFluid(1000))
                .duration(264).EUt(60).buildAndRegister();

        LARGE_CHEMICAL_RECIPES.recipeBuilder()
                .input(ingot, Plutonium239, 8)
                .input(dust, Uranium238)
                .fluidInputs(Air.getFluid(10000))
                .output(dust, Plutonium239, 8)
                .fluidOutputs(Radon.getFluid(1000))
                .duration(4000).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(Items.PAPER)
                .input(Items.STRING)
                .fluidInputs(GlycerylTrinitrate.getFluid(500))
                .output(MetaItems.DYNAMITE)
                .duration(160).EUt(4).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Niobium)
                .fluidInputs(Nitrogen.getFluid(1000))
                .output(dust, NiobiumNitride, 2)
                .duration(200).EUt(VA[HV]).buildAndRegister();

        // Dyes
        for (int i = 0; i < Materials.CHEMICAL_DYES.length; i++) {
            CHEMICAL_RECIPES.recipeBuilder()
                    .input(dye, MarkerMaterials.Color.VALUES[i])
                    .input(dust, Salt, 2)
                    .fluidInputs(SulfuricAcid.getFluid(250))
                    .fluidOutputs(Materials.CHEMICAL_DYES[i].getFluid(288))
                    .duration(600).EUt(24).buildAndRegister();
        }

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Carbon)
                .input(dust, Sulfur)
                .output(dust, Blaze)
                .duration(200).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Potassium)
                .fluidInputs(Oxygen.getFluid(3000))
                .fluidInputs(Nitrogen.getFluid(1000))
                .output(dust, Saltpeter, 5)
                .duration(180).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.GHAST_TEAR))
                .fluidInputs(Water.getFluid(1000))
                .output(dustTiny, Potassium)
                .output(dustTiny, Lithium)
                .fluidOutputs(SaltWater.getFluid(1000))
                .duration(400).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Sodium)
                .input(dust, Potassium)
                .fluidOutputs(SodiumPotassium.getFluid(1000))
                .duration(300).EUt(VA[LV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Sodium)
                .fluidInputs(Chlorine.getFluid(1000))
                .output(dust, Salt, 2)
                .duration(200).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Propene.getFluid(1000))
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(CarbonMonoxide.getFluid(1000))
                .fluidOutputs(Butyraldehyde.getFluid(1000))
                .duration(200).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Butyraldehyde.getFluid(250))
                .fluidInputs(PolyvinylAcetate.getFluid(144))
                .fluidOutputs(PolyvinylButyral.getFluid(144))
                .duration(400).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Benzene.getFluid(1000))
                .fluidInputs(Toluene.getFluid(1000))
                .output(dust, Biphenyl, 2)
                .fluidOutputs(Methane.getFluid(1000))
                .duration(200).EUt(VH[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Benzene.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .circuitMeta(1)
                .output(dust, Biphenyl, 2)
                .fluidOutputs(Water.getFluid(1000))
                .duration(400).EUt(VA[HV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Biphenyl, 2)
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidOutputs(Benzene.getFluid(2000))
                .duration(400).EUt(VA[EV]).buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, Biphenyl, 2)
                .fluidInputs(Chlorine.getFluid(4000))
                .fluidOutputs(PolychlorinatedBiphenyl.getFluid(1000))
                .fluidOutputs(HydrochloricAcid.getFluid(2000))
                .duration(200).EUt(VH[HV]).buildAndRegister();
    }
}
