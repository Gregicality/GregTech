package gregtech.api.recipes.logic;

import gregtech.Bootstrap;
import gregtech.api.GTValues;
import gregtech.api.capability.MultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTHashMaps;
import gregtech.api.util.OverlayedItemHandler;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.electric.SimpleMachineMetaTileEntityResizable;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static gregtech.api.recipes.logic.ParallelLogic.doParallelRecipes;
import static gregtech.api.util.GTUtility.gregtechId;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@SuppressWarnings("DataFlowIssue")
public class ParallelLogicTest {

    MetaTileEntityItemBus importItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, false);
    MetaTileEntityItemBus exportItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, true);
    MetaTileEntityFluidHatch importFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import.lv"), 1,
            false);
    MetaTileEntityFluidHatch secondImportFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import.lv"), 1,
            false);
    MetaTileEntityFluidHatch exportFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import.lv"), 1,
            true);

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void getMaxRecipeMultiplier_ItemLimitTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 3), false);
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(8000), true);

        int itemRatio = ParallelLogic.getMaxRecipeMultiplier(recipe, importItemBus.getImportItems(),
                importFluidBus.getImportFluids(), parallelLimit);

        assertThat(itemRatio, is(3));
    }

    @Test
    public void getMaxRecipeMultiplier_FluidLimitTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(4000))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(8000), true);

        int itemRatio = ParallelLogic.getMaxRecipeMultiplier(recipe, importItemBus.getImportItems(),
                importFluidBus.getImportFluids(), parallelLimit);

        assertThat(itemRatio, is(2));
    }

    @Test
    public void getMaxRecipeMultiplier_LimitFailureTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(1000))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.STONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Naphtha.getFluid(8000), true);

        int itemRatio = ParallelLogic.getMaxRecipeMultiplier(recipe, importItemBus.getImportItems(),
                importFluidBus.getImportFluids(), parallelLimit);

        assertThat(itemRatio, is(0));
    }

    @Test
    public void getMaxRecipeMultiplier_ItemFailureTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.STONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(8000), true);

        int itemRatio = ParallelLogic.getMaxRecipeMultiplier(recipe, importItemBus.getImportItems(),
                importFluidBus.getImportFluids(), parallelLimit);

        assertThat(itemRatio, is(0));
    }

    @Test
    public void getMaxRecipeMultiplier_FluidFailureTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Naphtha.getFluid(8000), true);

        int itemRatio = ParallelLogic.getMaxRecipeMultiplier(recipe, importItemBus.getImportItems(),
                importFluidBus.getImportFluids(), parallelLimit);

        assertThat(itemRatio, is(0));
    }

    @Test
    public void limitParallelByItems_MaxParallelTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(8000), true);

        int itemRatio = ParallelLogic.limitParallelByItems(recipe,
                new OverlayedItemHandler(exportItemBus.getExportItems()), parallelLimit);

        assertThat(itemRatio, is(4));
    }

    @Test
    public void limitParallelByItems_LessThanMaxParallelsTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(8000), true);
        exportItemBus.getExportItems().insertItem(0, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(1, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(2, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(3, new ItemStack(Blocks.STONE, 62), false);

        int itemRatio = ParallelLogic.limitParallelByItems(recipe,
                new OverlayedItemHandler(exportItemBus.getExportItems()), parallelLimit);

        assertThat(itemRatio, is(2));
    }

    @Test
    public void limitParallelByItems_SplitAcrossStacksTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(8000), true);
        exportItemBus.getExportItems().insertItem(0, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(1, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(2, new ItemStack(Blocks.STONE, 62), false);
        exportItemBus.getExportItems().insertItem(3, new ItemStack(Blocks.STONE, 62), false);

        int itemRatio = ParallelLogic.limitParallelByItems(recipe,
                new OverlayedItemHandler(exportItemBus.getExportItems()), parallelLimit);

        assertThat(itemRatio, is(4));
    }

    @Test
    public void limitParallelByItems_ItemOutputFullTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidInputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(8000), true);

        // Fill the export bus
        exportItemBus.getExportItems().insertItem(0, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(1, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(2, new ItemStack(Blocks.BONE_BLOCK), false);
        exportItemBus.getExportItems().insertItem(3, new ItemStack(Blocks.BONE_BLOCK), false);

        int itemRatio = ParallelLogic.limitParallelByItems(recipe,
                new OverlayedItemHandler(exportItemBus.getExportItems()), parallelLimit);

        assertThat(itemRatio, is(0));
    }

    @Test
    public void limitParallelByFluids_MaxParallelTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);
        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidOutputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        int itemRatio = ParallelLogic.limitParallelByFluids(recipe,
                exportFluidBus.getExportFluids(), parallelLimit);

        assertThat(itemRatio, is(4));
    }

    @Test
    public void limitParallelByFluids_PartialParallelsTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidOutputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        exportFluidBus.getExportFluids().fill(Materials.Acetone.getFluid(15800), true);

        int itemRatio = ParallelLogic.limitParallelByFluids(recipe,
                exportFluidBus.getExportFluids(), parallelLimit);

        assertThat(itemRatio, is(2));
    }

    @Test
    public void limitParallelByFluids_FluidOutputFullTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .fluidOutputs(Materials.Acetone.getFluid(100))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        exportFluidBus.getExportFluids().fill(Materials.Acetone.getFluid(16000), true);

        int itemRatio = ParallelLogic.limitParallelByFluids(recipe,
                exportFluidBus.getExportFluids(), parallelLimit);

        assertThat(itemRatio, is(0));
    }

    @Test
    public void getMaxRatioItem_SameNonConsumedTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .notConsumable(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        // Test less than maximum limit
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 3), false);

        int itemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
                recipe, parallelLimit);

        assertThat(itemRatio, is(2));

        // Test = max limit
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 2), false);

        int secondItemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
                recipe, parallelLimit);

        assertThat(secondItemRatio, is(parallelLimit));

        // Test > max limit
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 2), false);

        int thirdItemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
                recipe, parallelLimit);

        assertThat(thirdItemRatio, is(parallelLimit));
    }

    @Test
    public void getMaxRatioItem_DifferentNonConsumedTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .notConsumable(new ItemStack(Blocks.STONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 4), false);
        importItemBus.getImportItems().insertItem(1, new ItemStack(Blocks.STONE, 1), false);

        int itemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
                recipe, parallelLimit);

        assertThat(itemRatio, is(4));
    }

    @Test
    public void getMaxRatioItem_OnlyNonConsumedTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.STONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.STONE, 1), false);

        int itemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
                recipe, parallelLimit);

        assertThat(itemRatio, is(parallelLimit));
    }

    @Test
    public void getMaxRatioItem_OnlyNonConsumedWithStacksizeTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.STONE, 2))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        // Test Parallel Limit with not enough Non-consumed items
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.STONE, 1), false);

        int itemRatioFailure = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
                recipe, parallelLimit);

        assertThat(itemRatioFailure, is(0));

        // Test Parallel Limit by Non-consumed item amounts
        // Add one more stone to meet the recipe NC amount
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.STONE, 1), false);

        int itemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
                recipe, parallelLimit);

        assertThat(itemRatio, is(parallelLimit));

        // Test Parallel Limit for > max
        importItemBus.getImportItems().insertItem(1, new ItemStack(Blocks.STONE, 6), false);

        int secondItemRatio = ParallelLogic.getMaxRatioItem(GTHashMaps.fromItemHandler(importItemBus.getImportItems()),
                recipe, parallelLimit);

        assertThat(secondItemRatio, is(parallelLimit));
    }

    @Test
    public void getMaxRatioFluid_RegularFluidInputsTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .fluidInputs(Materials.Water.getFluid(1000))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        // Test Not enough fluid for 1 parallel
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(500), true);

        int fluidRatioFailure = ParallelLogic.getMaxRatioFluid(
                GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
                recipe, parallelLimit);

        assertThat(fluidRatioFailure, is(0));

        // Test Parallel Limit with > min, < max parallels
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(2500), true);

        int fluidRatio = ParallelLogic.getMaxRatioFluid(GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
                recipe, parallelLimit);

        assertThat(fluidRatio, is(3));

        // Test Parallel Limit with > max parallels
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(2500), true);

        int secondFluidRatio = ParallelLogic.getMaxRatioFluid(
                GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
                recipe, parallelLimit);

        assertThat(secondFluidRatio, is(parallelLimit));
    }

    @Test
    public void getMaxRatioFluid_SameNonConsumedTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .fluidInputs(Materials.Water.getFluid(1000))
                .notConsumable(Materials.Water.getFluid())
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        // Test Not enough fluid for 1 parallel
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(500), true);

        int fluidRatioFailure = ParallelLogic.getMaxRatioFluid(
                GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
                recipe, parallelLimit);

        assertThat(fluidRatioFailure, is(0));

        // Test Parallel Limit with > min, < max parallels
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(1501), true);

        int fluidRatio = ParallelLogic.getMaxRatioFluid(GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
                recipe, parallelLimit);

        assertThat(fluidRatio, is(2));

        // Test Parallel Limit Exactly equal inputs
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(2000), true);

        int fluidRatioExact = ParallelLogic.getMaxRatioFluid(
                GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
                recipe, parallelLimit);

        assertThat(fluidRatioExact, is(parallelLimit));

        // Test Parallel Limit with > max parallels
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(2500), true);

        int secondFluidRatio = ParallelLogic.getMaxRatioFluid(
                GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
                recipe, parallelLimit);

        assertThat(secondFluidRatio, is(parallelLimit));
    }

    @Test
    public void getMaxRatioFluid_DifferentNonConsumedTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                2,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .fluidInputs(Materials.Water.getFluid(1000))
                .notConsumable(Materials.Acetone.getFluid())
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        // Test Not enough fluid for 1 parallel
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(1000), true);

        int fluidRatioFailure = ParallelLogic.getMaxRatioFluid(
                GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
                recipe, parallelLimit);

        assertThat(fluidRatioFailure, is(0));

        // Test Parallel Limit with > min, < max parallels
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(1000), true);
        secondImportFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(1), true);

        MultipleTankHandler tankHandler = new FluidTankList(false, importFluidBus.getImportFluids().getTankAt(0),
                secondImportFluidBus.getImportFluids().getTankAt(0));

        int fluidRatio = ParallelLogic.getMaxRatioFluid(GTHashMaps.fromFluidHandler(tankHandler),
                recipe, parallelLimit);

        assertThat(fluidRatio, is(2));

        // Test Parallel Limit Exactly equal inputs
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(2000), true);

        int fluidRatioExact = ParallelLogic.getMaxRatioFluid(GTHashMaps.fromFluidHandler(tankHandler),
                recipe, parallelLimit);

        assertThat(fluidRatioExact, is(parallelLimit));

        // Test Parallel Limit with > max parallels
        importFluidBus.getImportFluids().fill(Materials.Water.getFluid(2500), true);

        int secondFluidRatio = ParallelLogic.getMaxRatioFluid(GTHashMaps.fromFluidHandler(tankHandler),
                recipe, parallelLimit);

        assertThat(secondFluidRatio, is(parallelLimit));
    }

    @Test
    public void getMaxRatioFluid_OnlyNonConsumedTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                2,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .notConsumable(Materials.Acetone.getFluid())
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        // Test Not enough fluid for 1 parallel
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(0), true);

        int fluidRatioFailure = ParallelLogic.getMaxRatioFluid(
                GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
                recipe, parallelLimit);

        assertThat(fluidRatioFailure, is(0));

        // Test Parallel Limit Exactly equal inputs
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(4), true);

        int fluidRatioExact = ParallelLogic.getMaxRatioFluid(
                GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
                recipe, parallelLimit);

        assertThat(fluidRatioExact, is(parallelLimit));

        // Test Parallel Limit with > max parallels
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(2500), true);

        int secondFluidRatio = ParallelLogic.getMaxRatioFluid(
                GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
                recipe, parallelLimit);

        assertThat(secondFluidRatio, is(parallelLimit));
    }

    @Test
    public void getMaxRatioFluid_OnlyNonConsumedWithStacksizeTest() {
        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                3,
                2,
                2,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .notConsumable(Materials.Acetone.getFluid(1000))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        // Test Not enough fluid for 1 parallel
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(500), true);

        int fluidRatioFailure = ParallelLogic.getMaxRatioFluid(
                GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
                recipe, parallelLimit);

        assertThat(fluidRatioFailure, is(0));

        // Test Parallel Limit Exactly equal inputs
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(500), true);

        int fluidRatioExact = ParallelLogic.getMaxRatioFluid(
                GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
                recipe, parallelLimit);

        assertThat(fluidRatioExact, is(parallelLimit));

        // Test Parallel Limit with > max parallels
        importFluidBus.getImportFluids().fill(Materials.Acetone.getFluid(2500), true);

        int secondFluidRatio = ParallelLogic.getMaxRatioFluid(
                GTHashMaps.fromFluidHandler(importFluidBus.getImportFluids()),
                recipe, parallelLimit);

        assertThat(secondFluidRatio, is(parallelLimit));
    }

    @Test
    public void doParallelRecipes_ExistingEUValueTest() {
        int parallelAmount = 4;

        // Do not specify the EUt or duration to test how they are taken into account
        Recipe maceratorRecipe = RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .input(Blocks.STONE)
                .output(Items.CARROT)
                .build().getResult();

        SimpleMachineMetaTileEntityResizable macerator = MetaTileEntities.registerMetaTileEntity(1,
                new SimpleMachineMetaTileEntityResizable(
                        gregtechId("macerator"),
                        RecipeMaps.MACERATOR_RECIPES,
                        -1,
                        4,
                        null,
                        GTValues.EV));

        macerator.getImportItems().setStackInSlot(0, new ItemStack(Blocks.STONE, 10));

        RecipeBuilder<?> testMaceratorRecipe = doParallelRecipes(maceratorRecipe, RecipeMaps.MACERATOR_RECIPES,
                macerator.getImportItems(),
                macerator.getImportFluids(), macerator.getExportItems(), macerator.getExportFluids(), parallelAmount,
                GTValues.V[GTValues.EV], macerator);

        assertThat(testMaceratorRecipe, notNullValue());

        // 2 is the default EUt value assigned to macerator recipes when not specified
        assertThat(testMaceratorRecipe.getEUt(), is(2L * parallelAmount));

        // 150 is the default duration value assigned to macerator recipes when not specified
        assertThat(testMaceratorRecipe.getDuration(), is(150));
    }
}
