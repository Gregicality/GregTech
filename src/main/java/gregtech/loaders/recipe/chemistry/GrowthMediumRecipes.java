package gregtech.loaders.recipe.chemistry;

import gregtech.api.metatileentity.multiblock.CleanroomType;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.dustTiny;
import static gregtech.common.items.MetaItems.*;

public class GrowthMediumRecipes {

    public static void init() {
        // Bio Chaff
        MACERATOR_RECIPES.recipeBuilder().duration(200)
                .input(PLANT_BALL, 2)
                .output(BIO_CHAFF)
                .output(BIO_CHAFF)
                .chancedOutput(BIO_CHAFF, 5000, 0)
                .chancedOutput(BIO_CHAFF, 2500, 0)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder().duration(300)
                .input(BIO_CHAFF)
                .outputs(new ItemStack(Blocks.DIRT))
                .buildAndRegister();

        // Bacteria
        BREWING_RECIPES.recipeBuilder().EUt(VA[HV]).duration(300)
                .input(BIO_CHAFF, 4)
                .fluidInputs(DistilledWater.getFluid(1000))
                .fluidOutputs(Bacteria.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();

        // Bacterial Sludge
        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[EV]).duration(600)
                .fluidInputs(Biomass.getFluid(1000))
                .fluidInputs(Bacteria.getFluid(1000))
                .fluidOutputs(BacterialSludge.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();

        // Enriched Bacterial Sludge
        BREWING_RECIPES.recipeBuilder().EUt(4).duration(128)
                .input(dust, Uranium)
                .fluidInputs(BacterialSludge.getFluid(1000))
                .fluidOutputs(EnrichedBacterialSludge.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();

        BREWING_RECIPES.recipeBuilder().EUt(4).duration(128)
                .input(dustTiny, Uranium235)
                .fluidInputs(BacterialSludge.getFluid(1000))
                .fluidOutputs(EnrichedBacterialSludge.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();

        BREWING_RECIPES.recipeBuilder().EUt(4).duration(128)
                .input(dustTiny, Naquadria)
                .fluidInputs(BacterialSludge.getFluid(1000))
                .fluidOutputs(EnrichedBacterialSludge.getFluid(2000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();

        // Mutagen
        DISTILLERY_RECIPES.recipeBuilder().EUt(VA[IV]).duration(100)
                .fluidInputs(EnrichedBacterialSludge.getFluid(1000))
                .circuitMeta(1)
                .fluidOutputs(Mutagen.getFluid(100))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();

        // Collagen
        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[HV]).duration(800)
                .input(dust, Meat)
                .inputs(new ItemStack(Items.DYE, 1, 15))
                .fluidInputs(SulfuricAcid.getFluid(500))
                .output(dust, Collagen)
                .fluidOutputs(DilutedSulfuricAcid.getFluid(500))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[HV]).duration(1600)
                .input(dust, Meat, 2)
                .inputs(new ItemStack(Items.BONE))
                .fluidInputs(SulfuricAcid.getFluid(1000))
                .output(dust, Collagen, 2)
                .fluidOutputs(DilutedSulfuricAcid.getFluid(1000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();

        // Gelatin
        MIXER_RECIPES.recipeBuilder().EUt(VA[HV]).duration(1600)
                .input(dust, Collagen, 4)
                .fluidInputs(PhosphoricAcid.getFluid(1000))
                .fluidInputs(Water.getFluid(3000))
                .fluidOutputs(GelatinMixture.getFluid(4000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();

        CENTRIFUGE_RECIPES.recipeBuilder().EUt(VA[HV]).duration(2400)
                .fluidInputs(GelatinMixture.getFluid(6000))
                .output(dust, Phosphorus)
                .output(dust, Gelatin, 4)
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();

        // Agar
        AUTOCLAVE_RECIPES.recipeBuilder().EUt(VA[HV]).duration(600)
                .input(dust, Gelatin)
                .fluidInputs(DistilledWater.getFluid(1000))
                .output(dust, Agar)
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();

        // Raw Growth Medium
        MIXER_RECIPES.recipeBuilder().EUt(VA[IV]).duration(1200)
                .input(dust, Meat, 4)
                .input(dust, Salt, 4)
                .input(dust, Calcium, 4)
                .input(dust, Agar, 4)
                .fluidInputs(Mutagen.getFluid(4000))
                .fluidOutputs(RawGrowthMedium.getFluid(4000))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();

        // Sterile Growth Medium
        FLUID_HEATER_RECIPES.recipeBuilder().EUt(VA[IV]).duration(20)
                .circuitMeta(1)
                .fluidInputs(RawGrowthMedium.getFluid(100))
                .fluidOutputs(SterileGrowthMedium.getFluid(100))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();

        // Stem Cells
        CHEMICAL_RECIPES.recipeBuilder().EUt(VA[LuV]).duration(300)
                .input(dust, Osmiridium)
                .fluidInputs(Bacteria.getFluid(500))
                .fluidInputs(SterileGrowthMedium.getFluid(500))
                .output(STEM_CELLS, 32)
                .fluidOutputs(BacterialSludge.getFluid(500))
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();
    }
}
