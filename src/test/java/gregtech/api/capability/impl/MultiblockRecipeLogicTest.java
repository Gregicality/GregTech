package gregtech.api.capability.impl;

import gregtech.Bootstrap;
import gregtech.api.capability.MultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.util.world.DummyWorld;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityElectricBlastFurnace;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMaintenanceHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.google.common.collect.ImmutableList;
import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.GTUtility.gregtechId;
import static org.hamcrest.CoreMatchers.*;

public class MultiblockRecipeLogicTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void trySearchNewRecipe() {
        World world = DummyWorld.INSTANCE;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("test_recipemap",
                3,
                3,
                1,
                1,
                new BlastRecipeBuilder().EUt(32),
                false);

        RecipeMaps.BLAST_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .blastFurnaceTemp(1)
                .buildAndRegister();

        RecipeMapMultiblockController mbt = MetaTileEntities.registerMetaTileEntity(509,
                new MetaTileEntityElectricBlastFurnace(
                        // super function calls the world, which equal null in test
                        gregtechId("electric_blast_furnace")) {

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

                    // ignore muffler outputs
                    @Override
                    public boolean hasMufflerMechanics() {
                        return false;
                    }
                });

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
        MetaTileEntityItemBus importItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, false) {

            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityItemBus exportItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, true) {

            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityFluidHatch importFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import.lv"), 1,
                false) {

            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityFluidHatch exportFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.export.lv"), 1,
                true) {

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

        MultiblockRecipeLogic mbl = new MultiblockRecipeLogic(mbt) {

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

            // since the hatches were not really added to a valid multiblock structure,
            // refer to their inventories directly
            @Override
            protected IItemHandlerModifiable getInputInventory() {
                return importItemBus.getImportItems();
            }

            @Override
            protected IItemHandlerModifiable getOutputInventory() {
                return exportItemBus.getExportItems();
            }

            @Override
            protected MultipleTankHandler getInputTank() {
                return importFluidBus.getImportFluids();
            }

            @Override
            protected MultipleTankHandler getOutputTank() {
                return importFluidBus.getExportFluids();
            }
        };

        mbl.isOutputsFull = false;
        mbl.invalidInputsForRecipes = false;
        mbl.trySearchNewRecipe();

        // no recipe found
        MatcherAssert.assertThat(mbt.isDistinct(), is(false));
        MatcherAssert.assertThat(mbl.invalidInputsForRecipes, is(true));
        MatcherAssert.assertThat(mbl.isActive, is(false));
        MatcherAssert.assertThat(mbl.previousRecipe, nullValue());

        // put an item in the inventory that will trigger recipe recheck
        mbl.getInputInventory().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        // Inputs change. did we detect it ?
        MatcherAssert.assertThat(mbl.hasNotifiedInputs(), is(true));
        mbl.trySearchNewRecipe();
        MatcherAssert.assertThat(mbl.invalidInputsForRecipes, is(false));
        MatcherAssert.assertThat(mbl.previousRecipe, notNullValue());
        MatcherAssert.assertThat(mbl.isActive, is(true));
        MatcherAssert.assertThat(mbl.getInputInventory().getStackInSlot(0).getCount(), is(15));

        // Save a reference to the old recipe so we can make sure it's getting reused
        Recipe prev = mbl.previousRecipe;

        // Finish the recipe, the output should generate, and the next iteration should begin
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.previousRecipe, is(prev));
        MatcherAssert.assertThat(AbstractRecipeLogic.areItemStacksEqual(mbl.getOutputInventory().getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)), is(true));
        MatcherAssert.assertThat(mbl.isActive, is(true));

        // Complete the second iteration, but the machine stops because its output is now full
        mbl.getOutputInventory().setStackInSlot(0, new ItemStack(Blocks.STONE, 63));
        mbl.getOutputInventory().setStackInSlot(1, new ItemStack(Blocks.STONE, 64));
        mbl.getOutputInventory().setStackInSlot(2, new ItemStack(Blocks.STONE, 64));
        mbl.getOutputInventory().setStackInSlot(3, new ItemStack(Blocks.STONE, 64));
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.isActive, is(false));
        MatcherAssert.assertThat(mbl.isOutputsFull, is(true));

        // Try to process again and get failed out because of full buffer.
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.isActive, is(false));
        MatcherAssert.assertThat(mbl.isOutputsFull, is(true));

        // Some room is freed in the output bus, so we can continue now.
        mbl.getOutputInventory().setStackInSlot(1, ItemStack.EMPTY);
        MatcherAssert.assertThat(mbl.hasNotifiedOutputs(), is(true));
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.isActive, is(true));
        MatcherAssert.assertThat(mbl.isOutputsFull, is(false));
        mbl.completeRecipe();
        MatcherAssert.assertThat(AbstractRecipeLogic.areItemStacksEqual(mbl.getOutputInventory().getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)), is(true));
    }

    @Test
    public void trySearchNewRecipeDistinct() {
        World world = DummyWorld.INSTANCE;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("test_recipemap_2",
                3,
                3,
                1,
                1,
                new BlastRecipeBuilder().EUt(32),
                false);

        RecipeMaps.BLAST_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .blastFurnaceTemp(1)
                .buildAndRegister();

        RecipeMapMultiblockController mbt = MetaTileEntities.registerMetaTileEntity(510,
                new MetaTileEntityElectricBlastFurnace(
                        // super function calls the world, which equal null in test
                        gregtechId("electric_blast_furnace")) {

                    @Override
                    public boolean hasMufflerMechanics() {
                        return false;
                    }

                    // ignore maintenance problems
                    @Override
                    public boolean hasMaintenanceMechanics() {
                        return false;
                    }

                    @Override
                    public void reinitializeStructurePattern() {}

                    @Override
                    public boolean isDistinct() {
                        return true;
                    }

                    // function checks for the temperature of the recipe against the coils
                    @Override
                    public boolean checkRecipe(@NotNull Recipe recipe, boolean consumeIfSuccess) {
                        return true;
                    }
                });

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
        MetaTileEntityItemBus importItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, false) {

            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityItemBus importItemBus2 = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, false) {

            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityItemBus exportItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, true) {

            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityFluidHatch importFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import.lv"), 1,
                false) {

            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityFluidHatch exportFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.export.lv"), 1,
                true) {

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
            field.set(importItemBus2, mbt);
            field.set(exportItemBus, mbt);
            field.set(importFluidBus, mbt);
            field.set(exportFluidBus, mbt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        MultiblockRecipeLogic mbl = new MultiblockRecipeLogic(mbt) {

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

            // since the hatches were not really added to a valid multiblock structure,
            // refer to their inventories directly
            @Override
            protected IItemHandlerModifiable getInputInventory() {
                return importItemBus.getImportItems();
            }

            @Override
            protected IItemHandlerModifiable getOutputInventory() {
                return exportItemBus.getExportItems();
            }

            @Override
            protected MultipleTankHandler getInputTank() {
                return importFluidBus.getImportFluids();
            }

            @Override
            protected MultipleTankHandler getOutputTank() {
                return importFluidBus.getExportFluids();
            }

            @Override
            protected List<IItemHandlerModifiable> getInputBuses() {
                List<IItemHandlerModifiable> a = new ArrayList<>();
                a.add(importItemBus.getImportItems());
                a.add(importItemBus2.getImportItems());
                return a;
            }
        };

        MatcherAssert.assertThat(mbt.isDistinct(), is(true));

        mbl.isOutputsFull = false;
        mbl.invalidInputsForRecipes = false;
        mbl.trySearchNewRecipe();

        // no recipe found
        MatcherAssert.assertThat(mbt.isDistinct(), is(true));
        MatcherAssert.assertThat(mbl.invalidatedInputList.containsAll(mbl.getInputBuses()), is(true));
        MatcherAssert.assertThat(mbl.isActive, is(false));
        MatcherAssert.assertThat(mbl.previousRecipe, nullValue());

        // put an item in the first input bus that will trigger recipe recheck

        IItemHandlerModifiable firstBus = mbl.getInputBuses().get(0);
        firstBus.insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        // extract the specific notified item handler, as it's not the entire bus
        IItemHandlerModifiable notified = null;
        for (IItemHandler h : ((ItemHandlerList) firstBus).getBackingHandlers()) {
            if (h.getSlots() == 4 && h instanceof IItemHandlerModifiable) {
                notified = (IItemHandlerModifiable) h;
            }
        }

        // Inputs change. did we detect it ?
        MatcherAssert.assertThat(mbl.hasNotifiedInputs(), is(true));
        MatcherAssert.assertThat(mbl.getMetaTileEntity().getNotifiedItemInputList(), hasItem(notified));
        MatcherAssert.assertThat(mbl.canWorkWithInputs(), is(true));
        mbl.trySearchNewRecipe();
        MatcherAssert.assertThat(mbl.invalidatedInputList, not(hasItem(firstBus)));
        MatcherAssert.assertThat(mbl.previousRecipe, notNullValue());
        MatcherAssert.assertThat(mbl.isActive, is(true));
        MatcherAssert.assertThat(firstBus.getStackInSlot(0).getCount(), is(15));

        // Save a reference to the old recipe so we can make sure it's getting reused
        Recipe prev = mbl.previousRecipe;

        // Finish the recipe, the output should generate, and the next iteration should begin
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.previousRecipe, is(prev));
        MatcherAssert.assertThat(AbstractRecipeLogic.areItemStacksEqual(mbl.getOutputInventory().getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)), is(true));
        MatcherAssert.assertThat(mbl.isActive, is(true));

        // Complete the second iteration, but the machine stops because its output is now full
        mbl.getOutputInventory().setStackInSlot(0, new ItemStack(Blocks.STONE, 63));
        mbl.getOutputInventory().setStackInSlot(1, new ItemStack(Blocks.STONE, 64));
        mbl.getOutputInventory().setStackInSlot(2, new ItemStack(Blocks.STONE, 64));
        mbl.getOutputInventory().setStackInSlot(3, new ItemStack(Blocks.STONE, 64));
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.isActive, is(false));
        MatcherAssert.assertThat(mbl.isOutputsFull, is(true));

        // Try to process again and get failed out because of full buffer.
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.isActive, is(false));
        MatcherAssert.assertThat(mbl.isOutputsFull, is(true));

        // Some room is freed in the output bus, so we can continue now.
        mbl.getOutputInventory().setStackInSlot(1, ItemStack.EMPTY);
        MatcherAssert.assertThat(mbl.hasNotifiedOutputs(), is(true));
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.isActive, is(true));
        MatcherAssert.assertThat(mbl.isOutputsFull, is(false));
        mbl.completeRecipe();
        MatcherAssert.assertThat(AbstractRecipeLogic.areItemStacksEqual(mbl.getOutputInventory().getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)), is(true));
    }

    @Test
    public void testMaintenancePenalties() {
        TestableMaintenanceHatch maintenanceHatch = new TestableMaintenanceHatch(gregtechId("maintenance.hatch"),
                false);

        RecipeMapMultiblockController mbt = MetaTileEntities.registerMetaTileEntity(508,
                new MetaTileEntityElectricBlastFurnace(
                        // super function calls the world, which equal null in test
                        gregtechId("electric_blast_furnace")) {

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

                    // testing maintenance problems
                    @Override
                    public boolean hasMaintenanceMechanics() {
                        return true;
                    }

                    // ignore muffler outputs
                    @Override
                    public boolean hasMufflerMechanics() {
                        return false;
                    }

                    @Override
                    public <T> List<T> getAbilities(MultiblockAbility<T> ability) {
                        if (ability == MultiblockAbility.MAINTENANCE_HATCH) {
                            // noinspection unchecked
                            return (List<T>) ImmutableList.of(maintenanceHatch);
                        }
                        return super.getAbilities(ability);
                    }
                });

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

        ((MetaTileEntityHolder) mbt.getHolder()).setWorld(DummyWorld.INSTANCE);

        maintenanceHatch.myController = mbt;

        // Controller and isAttachedToMultiBlock need the world so we fake it here.
        MetaTileEntityItemBus importItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, false) {

            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityItemBus exportItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, true) {

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
            field.set(maintenanceHatch, mbt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        MultiblockRecipeLogic mbl = new MultiblockRecipeLogic(mbt) {

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

            // since the hatches were not really added to a valid multiblock structure,
            // refer to their inventories directly
            @Override
            protected IItemHandlerModifiable getInputInventory() {
                return importItemBus.getImportItems();
            }

            @Override
            protected IItemHandlerModifiable getOutputInventory() {
                return exportItemBus.getExportItems();
            }

            @Override
            protected MultipleTankHandler getInputTank() {
                return new FluidTankList(false);
            }

            @Override
            protected MultipleTankHandler getOutputTank() {
                return new FluidTankList(false);
            }

            @Override
            protected List<IItemHandlerModifiable> getInputBuses() {
                List<IItemHandlerModifiable> a = new ArrayList<>();
                a.add(importItemBus.getImportItems());
                return a;
            }
        };

        RecipeMaps.BLAST_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.CRAFTING_TABLE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(10).duration(10)
                .blastFurnaceTemp(1)
                .buildAndRegister();

        // start off as fixed
        for (int i = 0; i < 6; i++) {
            mbt.setMaintenanceFixed(i);
        }

        // cause one problem
        mbt.causeMaintenanceProblems();

        MatcherAssert.assertThat(mbt.getNumMaintenanceProblems(), is(1));

        IItemHandlerModifiable firstBus = mbl.getInputBuses().get(0);
        firstBus.insertItem(0, new ItemStack(Blocks.CRAFTING_TABLE, 1), false);
        mbl.trySearchNewRecipe();

        // 1 problem is 10% slower. 10 * 1.1 = 11
        MatcherAssert.assertThat(mbl.maxProgressTime, is(11));

        mbl.completeRecipe();

        // fix old problems
        for (int i = 0; i < 6; i++) {
            mbt.setMaintenanceFixed(i);
        }

        firstBus.insertItem(0, new ItemStack(Blocks.CRAFTING_TABLE, 1), false);
        mbl.trySearchNewRecipe();

        // 0 problems should have the regular duration of 10
        MatcherAssert.assertThat(mbl.maxProgressTime, is(10));
    }

    // needed to prevent cyclic references in anonymous class creation
    private static class TestableMaintenanceHatch extends MetaTileEntityMaintenanceHatch {

        public RecipeMapMultiblockController myController;

        public TestableMaintenanceHatch(ResourceLocation metaTileEntityId, boolean isConfigurable) {
            super(metaTileEntityId, isConfigurable);
        }

        @Override
        public MultiblockControllerBase getController() {
            return myController;
        }
    }
}
