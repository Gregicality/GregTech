package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.capability.IMultipleRecipeMaps;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.util.Tuple;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static gregtech.api.recipes.logic.OverclockingLogic.subTickParallelOC;

public class MultiblockRecipeLogic extends AbstractRecipeLogic {

    // Used for distinct mode
    protected int lastRecipeIndex = 0;
    protected IItemHandlerModifiable currentDistinctInputBus;
    protected List<IItemHandlerModifiable> invalidatedInputList = new ArrayList<>();

    public MultiblockRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity, tileEntity.recipeMap);
    }

    public MultiblockRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
        super(tileEntity, tileEntity.recipeMap, hasPerfectOC);
    }

    @Override
    public void update() {}

    public void updateWorkable() {
        super.update();
    }

    @Override
    protected boolean canProgressRecipe() {
        return super.canProgressRecipe() && !((IMultiblockController) metaTileEntity).isStructureObstructed();
    }

    /**
     * Used to reset cached values in the Recipe Logic on structure deform
     */
    @Override
    public void invalidate() {
        super.invalidate();
        lastRecipeIndex = 0;
        invalidatedInputList.clear();
    }

    public void onDistinctChanged() {
        this.lastRecipeIndex = 0;
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
    protected boolean canWorkWithInputs() {
        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;
        if (controller instanceof RecipeMapMultiblockController) {
            RecipeMapMultiblockController distinctController = (RecipeMapMultiblockController) controller;

            if (distinctController.canBeDistinct() && distinctController.isDistinct() &&
                    getInputInventory().getSlots() > 0) {
                boolean canWork = false;
                if (invalidatedInputList.isEmpty()) {
                    return true;
                }
                if (!metaTileEntity.getNotifiedFluidInputList().isEmpty()) {
                    canWork = true;
                    invalidatedInputList.clear();
                    metaTileEntity.getNotifiedFluidInputList().clear();
                    metaTileEntity.getNotifiedItemInputList().clear();
                } else {
                    Iterator<IItemHandlerModifiable> notifiedIter = metaTileEntity.getNotifiedItemInputList()
                            .iterator();
                    while (notifiedIter.hasNext()) {
                        IItemHandlerModifiable bus = notifiedIter.next();
                        Iterator<IItemHandlerModifiable> invalidatedIter = invalidatedInputList.iterator();
                        while (invalidatedIter.hasNext()) {
                            IItemHandler invalidatedHandler = invalidatedIter.next();
                            if (invalidatedHandler instanceof ItemHandlerList) {
                                for (IItemHandler ih : ((ItemHandlerList) invalidatedHandler).getBackingHandlers()) {
                                    if (ih == bus) {
                                        canWork = true;
                                        invalidatedIter.remove();
                                        break;
                                    }
                                }
                            } else if (invalidatedHandler == bus) {
                                canWork = true;
                                invalidatedIter.remove();
                            }
                        }
                        notifiedIter.remove();
                    }
                }
                ArrayList<IItemHandler> flattenedHandlers = new ArrayList<>();
                for (IItemHandler ih : getInputBuses()) {
                    if (ih instanceof ItemHandlerList) {
                        flattenedHandlers.addAll(((ItemHandlerList) ih).getBackingHandlers());
                    }
                    flattenedHandlers.add(ih);
                }

                if (!invalidatedInputList.containsAll(flattenedHandlers)) {
                    canWork = true;
                }
                return canWork;
            }
        }
        return super.canWorkWithInputs();
    }

    @Override
    protected void trySearchNewRecipe() {
        // do not run recipes when there are more than 5 maintenance problems
        // Maintenance can apply to all multiblocks, so cast to a base multiblock class
        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;
        if (ConfigHolder.machines.enableMaintenance && controller.hasMaintenanceMechanics() &&
                controller.getNumMaintenanceProblems() > 5) {
            return;
        }

        // Distinct buses only apply to some multiblocks, so check the controller against a lower class
        if (controller instanceof RecipeMapMultiblockController) {
            RecipeMapMultiblockController distinctController = (RecipeMapMultiblockController) controller;

            if (distinctController.canBeDistinct() && distinctController.isDistinct() &&
                    getInputInventory().getSlots() > 0) {
                trySearchNewRecipeDistinct();
                return;
            }
        }

        trySearchNewRecipeCombined();
    }

    /**
     * Put into place so multiblocks can override {@link AbstractRecipeLogic#trySearchNewRecipe()} without having to
     * deal with
     * the maintenance and distinct logic in {@link MultiblockRecipeLogic#trySearchNewRecipe()}
     */
    protected void trySearchNewRecipeCombined() {
        super.trySearchNewRecipe();
    }

    protected void trySearchNewRecipeDistinct() {
        long maxVoltage = getMaxVoltage();
        Recipe currentRecipe;
        List<IItemHandlerModifiable> importInventory = getInputBuses();

        // Our caching implementation
        // This guarantees that if we get a recipe cache hit, our efficiency is no different from other machines
        if (checkPreviousRecipeDistinct(importInventory.get(lastRecipeIndex)) && checkRecipe(previousRecipe)) {
            currentRecipe = previousRecipe;
            currentDistinctInputBus = importInventory.get(lastRecipeIndex);
            if (prepareRecipeDistinct(currentRecipe)) {
                // No need to cache the previous recipe here, as it is not null and matched by the current recipe,
                // so it will always be the same
                return;
            }
        }

        // On a cache miss, our efficiency is much worse, as it will check
        // each bus individually instead of the combined inventory all at once.
        for (int i = 0; i < importInventory.size(); i++) {
            IItemHandlerModifiable bus = importInventory.get(i);
            // Skip this bus if no recipe was found last time
            if (invalidatedInputList.contains(bus)) {
                continue;
            }
            // Look for a new recipe after a cache miss
            currentRecipe = findRecipe(maxVoltage, bus, getInputTank());
            // Cache the current recipe, if one is found
            if (currentRecipe != null && checkRecipe(currentRecipe)) {
                this.previousRecipe = currentRecipe;
                currentDistinctInputBus = bus;
                if (prepareRecipeDistinct(currentRecipe)) {
                    lastRecipeIndex = i;
                    return;
                }
            }
            if (currentRecipe == null) {
                // no valid recipe found, invalidate this bus
                invalidatedInputList.add(bus);
            }
        }
    }

    @Override
    public void invalidateInputs() {
        MultiblockWithDisplayBase controller = (MultiblockWithDisplayBase) metaTileEntity;
        RecipeMapMultiblockController distinctController = (RecipeMapMultiblockController) controller;
        if (distinctController.canBeDistinct() && distinctController.isDistinct() &&
                getInputInventory().getSlots() > 0) {
            invalidatedInputList.add(currentDistinctInputBus);
        } else {
            super.invalidateInputs();
        }
    }

    protected boolean checkPreviousRecipeDistinct(IItemHandlerModifiable previousBus) {
        return previousRecipe != null && previousRecipe.matches(false, previousBus, getInputTank());
    }

    protected boolean prepareRecipeDistinct(Recipe recipe) {
        recipe = Recipe.trimRecipeOutputs(recipe, getRecipeMap(), metaTileEntity.getItemOutputLimit(),
                metaTileEntity.getFluidOutputLimit());

        recipe = findParallelRecipe(
                recipe,
                currentDistinctInputBus,
                getInputTank(),
                getOutputInventory(),
                getOutputTank(),
                getMaxParallelVoltage(),
                getParallelLimit());

        if (recipe != null) {
            recipe = setupAndConsumeRecipeInputs(recipe, currentDistinctInputBus, getInputTank());
            if (recipe != null) {
                setupRecipe(recipe);
                return true;
            }
        }

        return false;
    }

    @Override
    protected void modifyOverclockPre(@NotNull OCParams ocParams, @NotNull RecipePropertyStorage storage) {
        super.modifyOverclockPre(ocParams, storage);

        // apply maintenance bonuses
        Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

        // duration bonus
        if (maintenanceValues.getSecond() != 1.0) {
            ocParams.setDuration((int) Math.round(ocParams.duration() * maintenanceValues.getSecond()));
        }
    }

    @Override
    protected void runOverclockingLogic(@NotNull OCParams ocParams, @NotNull OCResult ocResult,
                                        @NotNull RecipePropertyStorage propertyStorage, long maxVoltage) {
        subTickParallelOC(ocParams, ocResult, maxVoltage, getOverclockingDurationFactor(),
                getOverclockingVoltageFactor());
    }

    @Override
    protected void modifyOverclockPost(@NotNull OCResult ocResult, @NotNull RecipePropertyStorage storage) {
        super.modifyOverclockPost(ocResult, storage);

        // apply maintenance penalties
        Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

        // duration penalty
        if (maintenanceValues.getFirst() > 0) {
            ocResult.setDuration((int) (ocResult.duration() * (1 + 0.1 * maintenanceValues.getFirst())));
        }
    }

    @Override
    public long getMaximumOverclockVoltage() {
        IEnergyContainer energyContainer = getEnergyContainer();
        if (energyContainer instanceof EnergyContainerList) {
            long voltage;
            long amperage;
            if (energyContainer.getInputVoltage() > energyContainer.getOutputVoltage()) {
                voltage = energyContainer.getInputVoltage();
                amperage = energyContainer.getInputAmperage();
            } else {
                voltage = energyContainer.getOutputVoltage();
                amperage = energyContainer.getOutputAmperage();
            }

            if (amperage == 1) {
                // amperage is 1 when the energy is not exactly on a tier

                // the voltage for recipe search is always on tier, so take the closest lower tier
                return GTValues.VOC[GTUtility.getFloorTierByVoltage(voltage)];
            } else {
                // amperage != 1 means the voltage is exactly on a tier
                // ignore amperage, since only the voltage is relevant for recipe search
                // amps are never > 3 in an EnergyContainerList
                return voltage;
            }
        }
        return Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
    }

    @NotNull
    protected Tuple<Integer, Double> getMaintenanceValues() {
        MultiblockWithDisplayBase displayBase = this.metaTileEntity instanceof MultiblockWithDisplayBase ?
                (MultiblockWithDisplayBase) metaTileEntity : null;
        int numMaintenanceProblems = displayBase == null || !displayBase.hasMaintenanceMechanics() ||
                !ConfigHolder.machines.enableMaintenance ? 0 : displayBase.getNumMaintenanceProblems();
        double durationMultiplier = 1.0D;
        if (displayBase != null && displayBase.hasMaintenanceMechanics() && ConfigHolder.machines.enableMaintenance) {
            durationMultiplier = displayBase.getMaintenanceDurationMultiplier();
        }
        return new Tuple<>(numMaintenanceProblems, durationMultiplier);
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        if (controller.checkRecipe(recipe, false)) {
            controller.checkRecipe(recipe, true);
            return super.checkRecipe(recipe);
        }
        return false;
    }

    @Override
    protected void completeRecipe() {
        performMufflerOperations();
        super.completeRecipe();
    }

    protected void performMufflerOperations() {
        if (metaTileEntity instanceof MultiblockWithDisplayBase controller) {
            // output muffler items
            if (controller.hasMufflerMechanics()) {
                if (parallelRecipesPerformed > 1) {
                    controller.outputRecoveryItems(parallelRecipesPerformed);
                } else {
                    controller.outputRecoveryItems();
                }
            }
        }
    }

    @Override
    public long getMaxVoltage() {
        IEnergyContainer energyContainer = getEnergyContainer();
        if (!consumesEnergy()) {
            // Generator Multiblocks
            long voltage = energyContainer.getOutputVoltage();
            long amperage = energyContainer.getOutputAmperage();
            if (energyContainer instanceof EnergyContainerList && amperage == 1) {
                // Amperage is 1 when the energy is not exactly on a tier.
                // The voltage for recipe search is always on tier, so take the closest lower tier.
                // List check is done because single hatches will always be a "clean voltage," no need
                // for any additional checks.
                return GTValues.VOC[GTUtility.getFloorTierByVoltage(voltage)];
            }
            return voltage;
        } else {
            // Machine Multiblocks
            if (energyContainer instanceof EnergyContainerList energyList) {
                long highestVoltage = energyList.getHighestInputVoltage();
                if (energyList.getNumHighestInputContainers() > 1) {
                    // allow tier + 1 if there are multiple hatches present at the highest tier
                    int tier = GTUtility.getTierByVoltage(highestVoltage);
                    return GTValues.V[Math.min(tier + 1, GTValues.MAX)];
                } else {
                    return highestVoltage;
                }
            } else {
                return energyContainer.getInputVoltage();
            }
        }
    }

    @Override
    protected long getMaxParallelVoltage() {
        return getMaximumOverclockVoltage();
    }

    @Nullable
    @Override
    public RecipeMap<?> getRecipeMap() {
        // if the multiblock has more than one RecipeMap, return the currently selected one
        if (metaTileEntity instanceof IMultipleRecipeMaps)
            return ((IMultipleRecipeMaps) metaTileEntity).getCurrentRecipeMap();
        return super.getRecipeMap();
    }
}
