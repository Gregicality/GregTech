package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;

public class MultiblockRecipeLogic extends AbstractRecipeLogic {

    // Used for distinct mode
    protected int lastRecipeIndex = 0;


    public MultiblockRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity, tileEntity.recipeMap);
    }

    public MultiblockRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
        super(tileEntity, tileEntity.recipeMap, hasPerfectOC);
    }

    @Override
    public void update() {
    }

    public void updateWorkable() {
        super.update();
    }

    /**
     * Used to reset cached values in the Recipe Logic on structure deform
     */
    public void invalidate() {
        previousRecipe = null;
        progressTime = 0;
        maxProgressTime = 0;
        recipeEUt = 0;
        fluidOutputs = null;
        itemOutputs = null;
        lastRecipeIndex = 0;
        setActive(false); // this marks dirty for us
    }

    public IEnergyContainer getEnergyContainer() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getEnergyContainer();
    }

    @Override
    protected IItemHandlerModifiable getInputInventory() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getInputInventory();
    }

    // Used for distinct bus recipe checking
    protected List<IItemHandlerModifiable> getInputBuses() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getAbilities(MultiblockAbility.IMPORT_ITEMS);
    }

    @Override
    protected IItemHandlerModifiable getOutputInventory() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getOutputInventory();
    }

    @Override
    protected IMultipleTankHandler getInputTank() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getInputFluidInventory();
    }

    @Override
    protected IMultipleTankHandler getOutputTank() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getOutputFluidInventory();
    }

    @Override
    protected void trySearchNewRecipe() {
        // do not run recipes when there are more than 5 maintenance problems
        // Maintenance can apply to all multiblocks, so cast to a base multiblock class
        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;
        if (controller.hasMaintenanceMechanics() && controller.getNumMaintenanceProblems() > 5) {
            return;
        }

        // Distinct buses only apply to some of the multiblocks, so check the controller against a lower class
        if(controller instanceof RecipeMapMultiblockController) {
            RecipeMapMultiblockController distinctController = (RecipeMapMultiblockController) controller;

            if(distinctController.canBeDistinct() && distinctController.isDistinct()) {
                trySearchNewRecipeDistinct();
                return;
            }
        }

        super.trySearchNewRecipe();
    }

    protected void trySearchNewRecipeDistinct() {
        long maxVoltage = getMaxVoltage();
        Recipe currentRecipe = null;
        List<IItemHandlerModifiable> importInventory = getInputBuses();
        IMultipleTankHandler importFluids = getInputTank();

        // Our caching implementation
        // This guarantees that if we get a recipe cache hit, our efficiency is no different from other machines
        if (previousRecipe != null && previousRecipe.matches(false, importInventory.get(lastRecipeIndex), importFluids)) {
            currentRecipe = previousRecipe;
            // TODO, is setting the invalidInputs here needed?
            invalidInputsForRecipes = false;
            // If a valid recipe is found, immediately attempt to return it to prevent inventory scanning
            if (setupAndConsumeRecipeInputs(currentRecipe, importInventory.get(lastRecipeIndex))) {
                setupRecipe(currentRecipe);
                metaTileEntity.getNotifiedItemInputList().clear();
                metaTileEntity.getNotifiedFluidInputList().clear();
                //TODO, should we set the previous recipe to the current recipe here, or would that cause some issues with caching once parallel is introduced?
                return;
            }
        }

        // On a cache miss, our efficiency is much worse, as it will check
        // each bus individually instead of the combined inventory all at once.
        for (int i = 0; i < importInventory.size(); i++) {
            IItemHandlerModifiable bus = importInventory.get(i);
            boolean inputsChanged = hasNotifiedInputs();
            // If the inputs have changed since the last recipe calculation, find a new recipe based on the new inputs
            if (inputsChanged) {
                currentRecipe = findRecipe(maxVoltage, bus, importFluids, MatchingMode.DEFAULT);
                // Cache the current recipe, if one is found
                if (currentRecipe != null) {
                    this.previousRecipe = currentRecipe;
                }
            }

            invalidInputsForRecipes = (currentRecipe == null);

            if (currentRecipe != null && setupAndConsumeRecipeInputs(currentRecipe, importInventory.get(i))) {
                lastRecipeIndex = i;
                setupRecipe(currentRecipe);
                metaTileEntity.getNotifiedItemInputList().clear();
                metaTileEntity.getNotifiedFluidInputList().clear();
                break;
            }
        }

        // TODO, if we don't find anything, should we reset the notified inputs?

    }

    @Override
    protected int[] calculateOverclock(int EUt, long voltage, int duration) {
        // apply maintenance penalties
        int numMaintenanceProblems = (this.metaTileEntity instanceof MultiblockWithDisplayBase) ?
                ((MultiblockWithDisplayBase) metaTileEntity).getNumMaintenanceProblems() : 0;

        int[] overclock = super.calculateOverclock(EUt, voltage, duration);
        overclock[1] = (int) (duration * (1 + 0.1 * numMaintenanceProblems));

        return overclock;
    }

    @Override
    protected boolean setupAndConsumeRecipeInputs(Recipe recipe, IItemHandlerModifiable importInventory) {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        if (controller.checkRecipe(recipe, false) &&
                super.setupAndConsumeRecipeInputs(recipe, importInventory)) {
            controller.checkRecipe(recipe, true);
            return true;
        } else return false;
    }

    @Override
    protected void completeRecipe() {
        if (metaTileEntity instanceof MultiblockWithDisplayBase) {
            MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;

            // output muffler items
            if (controller.hasMufflerMechanics())
                controller.outputRecoveryItems();

            // increase total on time
            if (controller.hasMaintenanceMechanics())
                controller.calculateMaintenance(this.progressTime);
        }
        super.completeRecipe();
    }

    @Override
    protected long getEnergyStored() {
        return getEnergyContainer().getEnergyStored();
    }

    @Override
    protected long getEnergyCapacity() {
        return getEnergyContainer().getEnergyCapacity();
    }

    @Override
    protected boolean drawEnergy(int recipeEUt) {
        long resultEnergy = getEnergyStored() - recipeEUt;
        if (resultEnergy >= 0L && resultEnergy <= getEnergyCapacity()) {
            getEnergyContainer().changeEnergy(-recipeEUt);
            return true;
        } else return false;
    }

    @Override
    protected long getMaxVoltage() {
        return Math.max(getEnergyContainer().getInputVoltage(), getEnergyContainer().getOutputVoltage());
    }
}
