package gregtech.api.recipes.logic;

import gregtech.Bootstrap;
import gregtech.api.capability.MultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMapBuilder;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.api.util.world.DummyWorld;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityElectricBlastFurnace;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.*;

public class IParallelableRecipeLogicTest {

    private static final ItemStackHashStrategy hashStrategy = ItemStackHashStrategy.comparingAll();
    private static RecipeMapMultiblockController mbt;
    private static MetaTileEntityItemBus importItemBus;
    private static MetaTileEntityItemBus exportItemBus;
    private static MetaTileEntityFluidHatch importFluidBus;
    private static MetaTileEntityFluidHatch secondImportFluidBus;
    private static MetaTileEntityFluidHatch exportFluidBus;

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    private static ResourceLocation gregtechId(String name) {
        return GTUtility.gregtechId(name);
    }

    private static MetaTileEntityElectricBlastFurnace initEBF(int id) {
        World world = DummyWorld.INSTANCE;

        mbt = MetaTileEntities.registerMetaTileEntity(id,
                new MetaTileEntityElectricBlastFurnace(
                        // super function calls the world, which equal null in test
                        GTUtility.gregtechId("electric_blast_furnace")) {

                    @Override
                    public boolean canBeDistinct() {
                        return false;
                    }

                    @Override
                    public void reinitializeStructurePattern() {}

                    // function checks for the temperature of the recipe against the coils
                    @Override
                    public boolean checkRecipe(@NotNull Recipe recipe, boolean consumeIfSuccess) {
                        return true;
                    }

                    // ignore maintenance problems
                    @Override
                    public boolean hasMaintenanceMechanics() {
                        return false;
                    }
                });

        try {
            Field field = MetaTileEntityElectricBlastFurnace.class.getSuperclass()
                    .getDeclaredField("recipeMapWorkable");
            field.setAccessible(true);

            Object recipeMapWorkableField = field.get(mbt);
            Method setParallelLimitMethod = recipeMapWorkableField.getClass().getSuperclass().getSuperclass()
                    .getDeclaredMethod("setParallelLimit", int.class);
            setParallelLimitMethod.setAccessible(true);

            setParallelLimitMethod.invoke(recipeMapWorkableField, 4);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        // isValid() check in the dirtying logic requires both a metatileentity and a holder
        try {
            Field field = MetaTileEntity.class.getDeclaredField("holder");
            field.setAccessible(true);
            field.set(mbt, new MetaTileEntityHolder());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            Field field = MetaTileEntityHolder.class.getDeclaredField("metaTileEntity");
            field.setAccessible(true);
            field.set(mbt.getHolder(), mbt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        ((MetaTileEntityHolder) mbt.getHolder()).setWorld(world);

        // Controller and isAttachedToMultiBlock need the world so we fake it here.
        importItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, false) {

            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        exportItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, true) {

            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        importFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import.lv"), 1, false) {

            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        secondImportFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import.zpm"), 7, false) {

            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        exportFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.export.uhv"), 9, true) {

            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };

        // Controller is a private field but we need that information
        try {
            Field field = MetaTileEntityMultiblockPart.class.getDeclaredField("controllerTile");
            field.setAccessible(true);
            field.set(importItemBus, mbt);
            field.set(exportItemBus, mbt);
            field.set(importFluidBus, mbt);
            field.set(exportFluidBus, mbt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return (MetaTileEntityElectricBlastFurnace) mbt;
    }

    @Test
    public void findMultipliedRecipe_AtMaxParallelsTest() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(511);

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
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        IParallelableRecipeLogic logic = new ParallelableTestLogic(EBF, map, ParallelLogicType.MULTIPLY);

        // Initially populate the input bus
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        RecipeBuilder<?> parallelRecipe = logic.findMultipliedParallelRecipe(map, recipe,
                importItemBus.getImportItems(), importFluidBus.getImportFluids(),
                exportItemBus.getExportItems(), exportFluidBus.getExportFluids(), parallelLimit, Integer.MAX_VALUE,
                EBF);

        // Check if the correct number of parallels were done
        MatcherAssert.assertThat(parallelRecipe.getParallel(), is(4));

        // Check that the EUt of the recipe was multiplied correctly
        MatcherAssert.assertThat(parallelRecipe.getEUt(), is(120L));

        // Check if the recipe duration was not modified
        MatcherAssert.assertThat(parallelRecipe.getDuration(), is(100));

        // Check the recipe outputs
        MatcherAssert.assertThat(parallelRecipe.getOutputs().isEmpty(), is(false));

        MatcherAssert.assertThat(
                hashStrategy.equals(new ItemStack(Blocks.STONE, 4), parallelRecipe.getOutputs().get(0)), is(true));

        // Check the recipe inputs
        // assertEquals(CountableIngredient.from(new ItemStack(Blocks.COBBLESTONE), 4),
        // parallelRecipe.getInputs().get(0));
    }

    @Test
    public void findMultipliedRecipe_LessThanMaxParallelsTest() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(512);

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
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        IParallelableRecipeLogic logic = new ParallelableTestLogic(EBF, map, ParallelLogicType.MULTIPLY);

        // Initially populate the input bus
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 2), false);

        RecipeBuilder<?> parallelRecipe = logic.findMultipliedParallelRecipe(map, recipe,
                importItemBus.getImportItems(), importFluidBus.getImportFluids(),
                exportItemBus.getExportItems(), exportFluidBus.getExportFluids(), parallelLimit, Integer.MAX_VALUE,
                EBF);

        // Check if the correct number of parallels were done
        MatcherAssert.assertThat(parallelRecipe.getParallel(), is(2));

        // Check that the EUt of the recipe was multiplied correctly
        MatcherAssert.assertThat(parallelRecipe.getEUt(), is(60L));

        // Check if the recipe duration was not modified
        MatcherAssert.assertThat(parallelRecipe.getDuration(), is(100));

        // Check the recipe outputs
        MatcherAssert.assertThat(parallelRecipe.getOutputs().isEmpty(), is(false));

        MatcherAssert.assertThat(
                hashStrategy.equals(new ItemStack(Blocks.STONE, 2), parallelRecipe.getOutputs().get(0)), is(true));

        // Check the recipe inputs
        MatcherAssert.assertThat(parallelRecipe.getInputs().get(0).getAmount(), is(2));
    }

    @Test
    public void findMultipliedRecipe_FluidOnlyMaxParallelTest() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(519);

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
                .blastFurnaceTemp(1000)
                .fluidInputs(Materials.Toluene.getFluid(1000), Materials.RawGasoline.getFluid(10000))
                .fluidOutputs(Materials.Gasoline.getFluid(11000))
                .EUt(480).duration(10)
                .build().getResult();

        IParallelableRecipeLogic logic = new ParallelableTestLogic(EBF, map, ParallelLogicType.MULTIPLY);

        // Initially populate the input buses
        importFluidBus.getImportFluids().fill(Materials.Toluene.getFluid(4000), true);
        secondImportFluidBus.getImportFluids().fill(Materials.RawGasoline.getFluid(50000), true);

        MultipleTankHandler tankHandler = new FluidTankList(false, importFluidBus.getImportFluids().getTankAt(0),
                secondImportFluidBus.getImportFluids().getTankAt(0));

        RecipeBuilder<?> parallelRecipe = logic.findMultipliedParallelRecipe(map, recipe,
                importItemBus.getImportItems(), tankHandler,
                exportItemBus.getExportItems(), exportFluidBus.getExportFluids(), parallelLimit, Integer.MAX_VALUE,
                EBF);

        // Check if the correct number of parallels were done
        MatcherAssert.assertThat(parallelRecipe.getParallel(), is(4));

        // Check that the EUt of the recipe was multiplied correctly
        MatcherAssert.assertThat(parallelRecipe.getEUt(), is(1920L));

        // Check if the recipe duration was not modified
        MatcherAssert.assertThat(parallelRecipe.getDuration(), is(10));

        // Check the recipe outputs
        MatcherAssert.assertThat(parallelRecipe.getFluidOutputs().isEmpty(), is(false));

        MatcherAssert.assertThat(Materials.Gasoline.getFluid(44000).equals(parallelRecipe.getFluidOutputs().get(0)),
                is(true));
    }

    @Test
    public void findMultipliedRecipe_FluidOnlyLessThanMaxParallelTest() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(520);

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
                .blastFurnaceTemp(1000)
                .fluidInputs(Materials.Toluene.getFluid(1000), Materials.RawGasoline.getFluid(10000))
                .fluidOutputs(Materials.Gasoline.getFluid(11000))
                .EUt(480).duration(10)
                .build().getResult();

        IParallelableRecipeLogic logic = new ParallelableTestLogic(EBF, map, ParallelLogicType.MULTIPLY);

        // Initially populate the input buses
        importFluidBus.getImportFluids().fill(Materials.Toluene.getFluid(2000), true);
        secondImportFluidBus.getImportFluids().fill(Materials.RawGasoline.getFluid(50000), true);

        MultipleTankHandler tankHandler = new FluidTankList(false, importFluidBus.getImportFluids().getTankAt(0),
                secondImportFluidBus.getImportFluids().getTankAt(0));

        RecipeBuilder<?> parallelRecipe = logic.findMultipliedParallelRecipe(map, recipe,
                importItemBus.getImportItems(), tankHandler,
                exportItemBus.getExportItems(), exportFluidBus.getExportFluids(), parallelLimit, Integer.MAX_VALUE,
                EBF);

        // Check if the correct number of parallels were done
        MatcherAssert.assertThat(parallelRecipe.getParallel(), is(2));

        // Check that the EUt of the recipe was multiplied correctly
        MatcherAssert.assertThat(parallelRecipe.getEUt(), is(960L));

        // Check if the recipe duration was not modified
        MatcherAssert.assertThat(parallelRecipe.getDuration(), is(10));

        // Check the recipe outputs
        MatcherAssert.assertThat(parallelRecipe.getFluidOutputs().isEmpty(), is(false));

        MatcherAssert.assertThat(Materials.Gasoline.getFluid(22000).equals(parallelRecipe.getFluidOutputs().get(0)),
                is(true));
    }

    @Test
    public void findAppendedParallelItemRecipe_AtMaxParallelsTest() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(513);

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("test_blast_furnace",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .buildAndRegister();

        IParallelableRecipeLogic logic = new ParallelableTestLogic(EBF, map, ParallelLogicType.APPEND_ITEMS);

        // Initially populate the input bus
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        RecipeBuilder<?> parallelRecipe = logic.findAppendedParallelItemRecipe(map, importItemBus.getImportItems(),
                exportItemBus.getExportItems(), parallelLimit, 120, EBF);

        // Check if the correct number of parallels were done
        MatcherAssert.assertThat(parallelRecipe.getParallel(), is(4));

        // Check that the EUt of the recipe was not modified
        MatcherAssert.assertThat(parallelRecipe.getEUt(), is(30L));

        // Check if the recipe duration was multiplied correctly
        MatcherAssert.assertThat(parallelRecipe.getDuration(), is(400));

        // Check the recipe outputs
        MatcherAssert.assertThat(parallelRecipe.getOutputs().isEmpty(), is(false));

        MatcherAssert.assertThat(
                hashStrategy.equals(new ItemStack(Blocks.STONE, 4), parallelRecipe.getOutputs().get(0)), is(true));
    }

    @Test
    public void findAppendedParallelItemRecipe_LessThanMaxParallelsTest() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(514);

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("test_blast_furnace_2",
                3,
                2,
                1,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .buildAndRegister();

        IParallelableRecipeLogic logic = new ParallelableTestLogic(EBF, map, ParallelLogicType.APPEND_ITEMS);

        // Initially populate the input bus
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 2), false);

        RecipeBuilder<?> parallelRecipe = logic.findAppendedParallelItemRecipe(map, importItemBus.getImportItems(),
                exportItemBus.getExportItems(), parallelLimit, 120, EBF);

        // Check if the correct number of parallels were done
        MatcherAssert.assertThat(parallelRecipe.getParallel(), is(2));

        // Check that the EUt of the recipe was not modified
        MatcherAssert.assertThat(parallelRecipe.getEUt(), is(30L));

        // Check if the recipe duration was multiplied correctly
        MatcherAssert.assertThat(parallelRecipe.getDuration(), is(200));

        // Check the recipe outputs
        MatcherAssert.assertThat(parallelRecipe.getOutputs().isEmpty(), is(false));

        MatcherAssert.assertThat(
                hashStrategy.equals(new ItemStack(Blocks.STONE, 2), parallelRecipe.getOutputs().get(0)), is(true));
    }

    // An end-to-end test for finding parallel recipes
    @Test
    public void findParallelRecipe_Test() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(515);

        int parallelLimit = 4;

        MultiblockRecipeLogic mrl = new MultiblockRecipeLogic(mbt) {

            @Override
            protected long getEnergyStored() {
                return Long.MAX_VALUE;
            }

            @Override
            protected long getEnergyCapacity() {
                return Long.MAX_VALUE;
            }

            @Override
            protected boolean drawEnergy(long recipeEUt, boolean simulate) {
                return true;
            }

            @Override
            public long getMaxVoltage() {
                return 32;
            }

            @NotNull
            @Override
            public MetaTileEntity getMetaTileEntity() {
                return EBF;
            }
        };

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
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        IParallelableRecipeLogic logic = new ParallelableTestLogic(EBF, map, ParallelLogicType.MULTIPLY);

        // Initially populate the input bus
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        Recipe outputRecipe = logic.findParallelRecipe(recipe, importItemBus.getImportItems(),
                importFluidBus.getImportFluids(), exportItemBus.getExportItems(),
                exportFluidBus.getExportFluids(), 128, parallelLimit);

        // Check that the EUt of the recipe was multiplied correctly
        MatcherAssert.assertThat(outputRecipe.getEUt(), is(120L));

        // Check if the recipe duration was not modified
        MatcherAssert.assertThat(outputRecipe.getDuration(), is(100));

        // Check the recipe outputs
        MatcherAssert.assertThat(outputRecipe.getOutputs().isEmpty(), is(false));

        MatcherAssert.assertThat(hashStrategy.equals(new ItemStack(Blocks.STONE, 4), outputRecipe.getOutputs().get(0)),
                is(true));
    }

    // An end-to-end test for finding parallel recipes
    @Test
    public void findParallelRecipe_FailingFromInputTest() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(516);

        int parallelLimit = 4;

        MultiblockRecipeLogic mrl = new MultiblockRecipeLogic(mbt) {

            @Override
            protected long getEnergyStored() {
                return Long.MAX_VALUE;
            }

            @Override
            protected long getEnergyCapacity() {
                return Long.MAX_VALUE;
            }

            @Override
            protected boolean drawEnergy(long recipeEUt, boolean simulate) {
                return true;
            }

            @Override
            public long getMaxVoltage() {
                return 32;
            }

            @NotNull
            @Override
            public MetaTileEntity getMetaTileEntity() {
                return EBF;
            }
        };

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
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        IParallelableRecipeLogic logic = new ParallelableTestLogic(EBF, map, ParallelLogicType.MULTIPLY);

        // Don't populate the input bus, so the recipe will fail
        // importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        Recipe outputRecipe = logic.findParallelRecipe(recipe, importItemBus.getImportItems(),
                importFluidBus.getImportFluids(), exportItemBus.getExportItems(),
                exportFluidBus.getExportFluids(), 32, parallelLimit);

        MatcherAssert.assertThat(outputRecipe, nullValue());
    }

    // An end-to-end test for finding parallel recipes
    @Test
    public void findParallelRecipe_FailingFromOutputTest() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(517);

        int parallelLimit = 4;

        MultiblockRecipeLogic mrl = new MultiblockRecipeLogic(mbt) {

            @Override
            protected long getEnergyStored() {
                return Long.MAX_VALUE;
            }

            @Override
            protected long getEnergyCapacity() {
                return Long.MAX_VALUE;
            }

            @Override
            protected boolean drawEnergy(long recipeEUt, boolean simulate) {
                return true;
            }

            @Override
            public long getMaxVoltage() {
                return 32;
            }

            @NotNull
            @Override
            public MetaTileEntity getMetaTileEntity() {
                return EBF;
            }
        };

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
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        IParallelableRecipeLogic logic = new ParallelableTestLogic(EBF, map, ParallelLogicType.MULTIPLY, true);

        // Saturate the export bus
        exportItemBus.getExportItems().insertItem(0, new ItemStack(Blocks.BONE_BLOCK, 16), false);
        exportItemBus.getExportItems().insertItem(1, new ItemStack(Blocks.BONE_BLOCK, 16), false);
        exportItemBus.getExportItems().insertItem(2, new ItemStack(Blocks.BONE_BLOCK, 16), false);
        exportItemBus.getExportItems().insertItem(3, new ItemStack(Blocks.BONE_BLOCK, 16), false);

        Recipe outputRecipe = logic.findParallelRecipe(recipe, importItemBus.getImportItems(),
                importFluidBus.getImportFluids(), exportItemBus.getExportItems(),
                exportFluidBus.getExportFluids(), Integer.MAX_VALUE, parallelLimit);

        MatcherAssert.assertThat(outputRecipe, nullValue());
    }

    @Test
    public void findParallelRecipe_SmallMaxStackSize() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(521);

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMapBuilder<>("electric_blast_furnace", new BlastRecipeBuilder())
                .itemInputs(3).itemOutputs(2).fluidInputs(1).fluidOutputs(1).build();

        // Create a simple recipe to be used for testing
        // Use an output item with small max stack size
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Items.ELYTRA))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        IParallelableRecipeLogic logic = new ParallelableTestLogic(EBF, map, ParallelLogicType.MULTIPLY);

        // Populate the Input Bus
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        // Saturate the export bus, except for one slot
        exportItemBus.getExportItems().insertItem(0, new ItemStack(Blocks.BONE_BLOCK, 16), false);
        exportItemBus.getExportItems().insertItem(1, new ItemStack(Blocks.BONE_BLOCK, 16), false);
        exportItemBus.getExportItems().insertItem(2, new ItemStack(Blocks.BONE_BLOCK, 16), false);

        RecipeBuilder<?> parallelRecipe = logic.findMultipliedParallelRecipe(map, recipe,
                importItemBus.getImportItems(), importFluidBus.getImportFluids(),
                exportItemBus.getExportItems(), exportFluidBus.getExportFluids(), parallelLimit, Integer.MAX_VALUE,
                EBF);

        MatcherAssert.assertThat(parallelRecipe, notNullValue());
        MatcherAssert.assertThat(parallelRecipe.getParallel(), is(1));
    }

    @Test
    public void applyParallelBonus_Test() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(518);

        int parallelLimit = 4;

        MultiblockRecipeLogic mrl = new MultiblockRecipeLogic(mbt) {

            @Override
            protected long getEnergyStored() {
                return Long.MAX_VALUE;
            }

            @Override
            protected long getEnergyCapacity() {
                return Long.MAX_VALUE;
            }

            @Override
            protected boolean drawEnergy(long recipeEUt, boolean simulate) {
                return true;
            }

            @Override
            public long getMaxVoltage() {
                return 32;
            }

            @NotNull
            @Override
            public MetaTileEntity getMetaTileEntity() {
                return EBF;
            }
        };

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
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        IParallelableRecipeLogic logic = new ParallelableTestLogic(EBF, map, ParallelLogicType.MULTIPLY, true);

        // Initially populate the input bus
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        Recipe outputRecipe = logic.findParallelRecipe(recipe, importItemBus.getImportItems(),
                importFluidBus.getImportFluids(), exportItemBus.getExportItems(),
                exportFluidBus.getExportFluids(), 128, parallelLimit);

        // Check that the EUt of the recipe was not modified
        MatcherAssert.assertThat(outputRecipe.getEUt(), is(1L));

        // Check if the recipe duration was multiplied correctly
        MatcherAssert.assertThat(outputRecipe.getDuration(), is(50));

        // Check the recipe outputs
        MatcherAssert.assertThat(outputRecipe.getOutputs().isEmpty(), is(false));

        MatcherAssert.assertThat(hashStrategy.equals(new ItemStack(Blocks.STONE, 4), outputRecipe.getOutputs().get(0)),
                is(true));
    }

    private static class ParallelableTestLogic implements IParallelableRecipeLogic {

        private final MetaTileEntity metaTileEntity;
        private final RecipeMap<?> recipeMap;
        private final ParallelLogicType logicType;
        private final boolean enableBonusOverride;

        public ParallelableTestLogic(MetaTileEntity metaTileEntity, RecipeMap<?> recipeMap,
                                     ParallelLogicType logicType) {
            this(metaTileEntity, recipeMap, logicType, false);
        }

        public ParallelableTestLogic(@NotNull MetaTileEntity metaTileEntity, @NotNull RecipeMap<?> recipeMap,
                                     @NotNull ParallelLogicType logicType, boolean enableBonusOverride) {
            this.metaTileEntity = metaTileEntity;
            this.recipeMap = recipeMap;
            this.logicType = logicType;
            this.enableBonusOverride = enableBonusOverride;
        }

        @NotNull
        @Override
        public MetaTileEntity getMetaTileEntity() {
            return this.metaTileEntity;
        }

        @Nullable
        @Override
        public RecipeMap<?> getRecipeMap() {
            return this.recipeMap;
        }

        @Override
        public @NotNull ParallelLogicType getParallelLogicType() {
            return logicType;
        }

        @Override
        public void setParallelRecipesPerformed(int amount) {}

        @Override
        public void invalidateInputs() {}

        @Override
        public void invalidateOutputs() {}

        @Override
        public void applyParallelBonus(@NotNull RecipeBuilder<?> builder) {
            if (enableBonusOverride) {
                builder.EUt(1).duration(50);
            }
        }
    }
}
