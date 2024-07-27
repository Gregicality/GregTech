package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IWorkable;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.IParallelableRecipeLogic;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.recipes.properties.impl.CleanroomProperty;
import gregtech.api.recipes.properties.impl.DimensionProperty;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.recipes.logic.OverclockingLogic.*;

public abstract class AbstractRecipeLogic extends MTETrait implements IWorkable, IParallelableRecipeLogic {

    private static final String ALLOW_OVERCLOCKING = "AllowOverclocking";
    private static final String OVERCLOCK_VOLTAGE = "OverclockVoltage";

    private final RecipeMap<?> recipeMap;

    private double euDiscount = -1;
    private double speedBonus = -1;

    protected Recipe previousRecipe;
    private boolean allowOverclocking = true;
    protected int parallelRecipesPerformed;
    private long overclockVoltage;
    private final OCParams ocParams = new OCParams();
    private final OCResult ocResult = new OCResult();

    protected boolean canRecipeProgress = true;

    protected int progressTime;
    protected int maxProgressTime;
    protected long recipeEUt;
    protected List<FluidStack> fluidOutputs;
    protected List<ItemStack> itemOutputs;

    protected boolean isActive;
    protected boolean workingEnabled = true;
    protected boolean hasNotEnoughEnergy;
    protected boolean wasActiveAndNeedsUpdate;
    protected boolean isOutputsFull;
    protected boolean invalidInputsForRecipes;

    protected boolean hasPerfectOC;

    /**
     * DO NOT use the parallelLimit field directly, EVER
     * use {@link AbstractRecipeLogic#setParallelLimit(int)} instead
     */
    private int parallelLimit = 1;

    public AbstractRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap) {
        super(tileEntity);
        this.recipeMap = recipeMap;
    }

    public AbstractRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, boolean hasPerfectOC) {
        super(tileEntity);
        this.recipeMap = recipeMap;
        this.hasPerfectOC = hasPerfectOC;
    }

    /**
     * @return the energy container's energy input per second
     */
    protected long getEnergyInputPerSecond() {
        return getEnergyContainer().getInputPerSec();
    }

    /**
     * @return the energy container's current stored energy
     */
    protected long getEnergyStored() {
        return getEnergyContainer().getEnergyStored();
    }

    /**
     * @return the energy container's maximum energy capacity
     */
    protected long getEnergyCapacity() {
        return getEnergyContainer().getEnergyCapacity();
    }

    protected IEnergyContainer getEnergyContainer() {
        return IEnergyContainer.DEFAULT;
    }

    /**
     * Draw energy from the energy container
     *
     * @param recipeEUt the EUt to remove
     * @param simulate  whether to simulate energy extraction or not
     * @return true if the energy can/was drained, otherwise false
     */
    protected boolean drawEnergy(long recipeEUt, boolean simulate) {
        // this should be the ONLY time eut is negative!
        if (consumesEnergy()) recipeEUt = -recipeEUt;
        long resultEnergy = getEnergyStored() + recipeEUt;
        if (resultEnergy >= 0L && resultEnergy <= getEnergyCapacity()) {
            if (!simulate) getEnergyContainer().changeEnergy(recipeEUt);
            return true;
        } else return false;
    }

    /**
     * @return the maximum voltage the machine can use/handle for recipe searching
     */
    public long getMaxVoltage() {
        return Math.max(getEnergyContainer().getInputVoltage(), getEnergyContainer().getOutputVoltage());
    }

    /**
     *
     * @return the maximum voltage the machine can use/handle for parallel recipe creation
     */
    protected long getMaxParallelVoltage() {
        return getMaxVoltage();
    }

    /**
     * @return the inventory to input items from
     */
    protected IItemHandlerModifiable getInputInventory() {
        return metaTileEntity.getImportItems();
    }

    /**
     * @return the inventory to output items to
     */
    protected IItemHandlerModifiable getOutputInventory() {
        return metaTileEntity.getExportItems();
    }

    /**
     * @return the fluid inventory to input fluids from
     */
    protected IMultipleTankHandler getInputTank() {
        return metaTileEntity.getImportFluids();
    }

    /**
     * @return the fluid inventory to output fluids to
     */
    protected IMultipleTankHandler getOutputTank() {
        return metaTileEntity.getExportFluids();
    }

    /**
     * @return true if energy is consumed by this Recipe Logic, otherwise false
     */
    public boolean consumesEnergy() {
        return true;
    }

    @NotNull
    @Override
    public final String getName() {
        // this is final so machines are not accidentally given multiple workable instances
        return GregtechDataCodes.ABSTRACT_WORKABLE_TRAIT;
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE) {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC) {
            return GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC.cast(this);
        }
        return null;
    }

    @Override
    public void update() {
        World world = getMetaTileEntity().getWorld();
        if (world != null && !world.isRemote) {
            if (workingEnabled) {
                if (getMetaTileEntity().getOffsetTimer() % 20 == 0)
                    this.canRecipeProgress = canProgressRecipe();

                if (progressTime > 0) {
                    updateRecipeProgress();
                }
                // check everything that would make a recipe never start here.
                if (progressTime == 0 && shouldSearchForRecipes()) {
                    trySearchNewRecipe();
                }
            }
            if (wasActiveAndNeedsUpdate) {
                this.wasActiveAndNeedsUpdate = false;
                setActive(false);
            }
        }
    }

    /**
     * DO NOT use the recipeMap field directly, EVER
     * <p>
     * This can be null due to Processing Array logic.
     * Normally this should never be null.
     *
     * @return the current RecipeMap of the logic
     */
    @Override
    @Nullable
    public RecipeMap<?> getRecipeMap() {
        return this.recipeMap;
    }

    /**
     * Can be null if a recipe has not yet been run
     *
     * @return the previous recipe run
     */
    @Nullable
    public Recipe getPreviousRecipe() {
        return previousRecipe;
    }

    /**
     * @return true if recipes should be searched for
     */
    protected boolean shouldSearchForRecipes() {
        return canWorkWithInputs() && canFitNewOutputs();
    }

    /**
     * @return true if input inventory contents have changed
     */
    protected boolean hasNotifiedInputs() {
        return !metaTileEntity.getNotifiedItemInputList().isEmpty() ||
                !metaTileEntity.getNotifiedFluidInputList().isEmpty();
    }

    /**
     * @return true if output inventory contents have changed
     */
    protected boolean hasNotifiedOutputs() {
        return !metaTileEntity.getNotifiedItemOutputList().isEmpty() ||
                !metaTileEntity.getNotifiedFluidOutputList().isEmpty();
    }

    /**
     * @return if the output inventory can fit new outputs
     */
    protected boolean canFitNewOutputs() {
        // if the output is full check if the output changed, so we can process recipes results again.
        if (this.isOutputsFull && !hasNotifiedOutputs()) {
            return false;
        } else {
            this.isOutputsFull = false;
            metaTileEntity.getNotifiedItemOutputList().clear();
            metaTileEntity.getNotifiedFluidOutputList().clear();
            return true;
        }
    }

    /**
     * @return true if the input inventory's content can be worked with
     */
    protected boolean canWorkWithInputs() {
        // if the inputs were bad last time, check if they've changed before trying to find a new recipe.
        if (this.invalidInputsForRecipes && !hasNotifiedInputs()) return false;

        // the change in inputs (especially by removal of ingredient by the player) might change the current valid
        // recipe.
        // and if the previous recipe produced fluids and the new recipe doesn't, then outputs are not full.
        this.isOutputsFull = false;
        this.invalidInputsForRecipes = false;
        this.metaTileEntity.getNotifiedItemInputList().clear();
        this.metaTileEntity.getNotifiedFluidInputList().clear();
        return true;
    }

    /**
     * Invalidate the current state of input inventory contents
     */
    @Override
    public void invalidateInputs() {
        this.invalidInputsForRecipes = true;
    }

    /**
     * Invalidate the current state of output inventory contents
     */
    @Override
    public void invalidateOutputs() {
        this.isOutputsFull = true;
    }

    /**
     * Set the amount of parallel recipes currently being performed
     *
     * @param amount the amount to set
     */
    @Override
    public void setParallelRecipesPerformed(int amount) {
        this.parallelRecipesPerformed = amount;
    }

    /**
     * Update the current running recipe's progress
     * <p>
     * Also handles consuming running energy by default
     * </p>
     */
    protected void updateRecipeProgress() {
        if (canRecipeProgress && drawEnergy(recipeEUt, true)) {
            drawEnergy(recipeEUt, false);
            // as recipe starts with progress on 1 this has to be > only not => to compensate for it
            if (++progressTime > maxProgressTime) {
                completeRecipe();
            }
            if (this.hasNotEnoughEnergy && getEnergyInputPerSecond() > 19L * recipeEUt) {
                this.hasNotEnoughEnergy = false;
            }
        } else if (recipeEUt > 0) {
            // only set hasNotEnoughEnergy if this recipe is consuming recipe
            // generators always have enough energy
            this.hasNotEnoughEnergy = true;
            decreaseProgress();
        }
    }

    /**
     * Decrease the recipe progress time in the case that some state was not right, like available EU to drain.
     */
    protected void decreaseProgress() {
        // if current progress value is greater than 2, decrement it by 2
        if (progressTime >= 2) {
            if (ConfigHolder.machines.recipeProgressLowEnergy) {
                this.progressTime = 1;
            } else {
                this.progressTime = Math.max(1, progressTime - 2);
            }
        }
    }

    /**
     * @return true if the recipe can progress, otherwise false
     */
    protected boolean canProgressRecipe() {
        if (previousRecipe == null) return true;
        return checkCleanroomRequirement(previousRecipe);
    }

    /**
     * Force the workable to search for new recipes.
     * This can be performance intensive. Use sparingly.
     */
    public void forceRecipeRecheck() {
        this.previousRecipe = null;
        trySearchNewRecipe();
    }

    /**
     * Try to search for a new recipe
     */
    protected void trySearchNewRecipe() {
        long maxVoltage = getMaxVoltage();
        Recipe currentRecipe;
        IItemHandlerModifiable importInventory = getInputInventory();
        IMultipleTankHandler importFluids = getInputTank();

        // see if the last recipe we used still works
        if (checkPreviousRecipe()) {
            currentRecipe = this.previousRecipe;
            // If there is no active recipe, then we need to find one.
        } else {
            currentRecipe = findRecipe(maxVoltage, importInventory, importFluids);
        }
        // If a recipe was found, then inputs were valid. Cache found recipe.
        if (currentRecipe != null) {
            this.previousRecipe = currentRecipe;
        }
        this.invalidInputsForRecipes = (currentRecipe == null);

        // proceed if we have a usable recipe.
        if (currentRecipe != null && checkRecipe(currentRecipe)) {
            prepareRecipe(currentRecipe);
        }
    }

    /**
     * @return true if the previous recipe is valid and can be run again
     */
    protected boolean checkPreviousRecipe() {
        if (this.previousRecipe == null) return false;
        if (this.previousRecipe.getEUt() > this.getMaxVoltage()) return false;
        return recipeMatch(this.previousRecipe, false);
    }

    /**
     * checks the recipe before preparing it
     *
     * @param recipe the recipe to check
     * @return true if the recipe is allowed to be used, else false
     */
    public boolean checkRecipe(@NotNull Recipe recipe) {
        return checkCleanroomRequirement(recipe) && checkDimensionRequirement(recipe);
    }

    /**
     * @param recipe the recipe to check
     * @return if the cleanroom requirement is met
     */
    protected boolean checkCleanroomRequirement(@NotNull Recipe recipe) {
        CleanroomType requiredType = recipe.getProperty(CleanroomProperty.getInstance(), null);
        if (requiredType == null) return true;

        MetaTileEntity mte = getMetaTileEntity();
        if (mte instanceof ICleanroomReceiver receiver) {
            if (ConfigHolder.machines.cleanMultiblocks && mte instanceof IMultiblockController) return true;

            ICleanroomProvider cleanroomProvider = receiver.getCleanroom();
            if (cleanroomProvider == null) return false;

            return cleanroomProvider.isClean() && cleanroomProvider.checkCleanroomType(requiredType);
        }

        return false;
    }

    protected boolean checkDimensionRequirement(@NotNull Recipe recipe) {
        DimensionProperty.DimensionPropertyList list = recipe.getProperty(DimensionProperty.getInstance(), null);
        if (list == null) {
            return true;
        }
        return list.checkDimension(this.getMetaTileEntity().getWorld().provider.getDimension());
    }

    /**
     * Prepares the recipe to be run.
     * <ol>
     * <li>The recipe is run in parallel if possible.</li>
     * <li>The potentially parallel recipe is then checked to exist.</li>
     * <li>If it exists, it checks if the recipe is runnable with the inputs provided.</li>
     * </ol>
     * If the above conditions are met, the recipe is engaged to be run
     *
     * @param recipe              the recipe to prepare
     * @param inputInventory      the inventory to draw items from
     * @param inputFluidInventory the fluid tanks to draw fluid from
     * @return true if the recipe was successfully prepared, else false
     */
    public boolean prepareRecipe(Recipe recipe, IItemHandlerModifiable inputInventory,
                                 IMultipleTankHandler inputFluidInventory) {
        recipe = Recipe.trimRecipeOutputs(recipe, getRecipeMap(), metaTileEntity.getItemOutputLimit(),
                metaTileEntity.getFluidOutputLimit());

        // apply EU/speed discount (if any) before parallel
        if (euDiscount > 0 || speedBonus > 0) { // if-statement to avoid unnecessarily creating RecipeBuilder object
            RecipeBuilder<?> builder = new RecipeBuilder<>(recipe, recipeMap);
            if (euDiscount > 0) {
                int newEUt = (int) Math.round(recipe.getEUt() * euDiscount);
                if (newEUt <= 0) newEUt = 1;
                builder.EUt(newEUt);
            }
            if (speedBonus > 0) {
                int duration = recipe.getDuration();
                int newDuration = (int) Math.round(duration * speedBonus);
                if (newDuration <= 0) newDuration = 1;
                builder.duration(newDuration);
            }
            recipe = builder.build().getResult();
        }

        // Pass in the trimmed recipe to the parallel logic
        recipe = findParallelRecipe(
                recipe,
                inputInventory,
                inputFluidInventory,
                getOutputInventory(),
                getOutputTank(),
                getMaxParallelVoltage(),
                getParallelLimit());

        if (recipe != null) {
            recipe = setupAndConsumeRecipeInputs(recipe, inputInventory, inputFluidInventory);
            if (recipe != null) {
                setupRecipe(recipe);
                return true;
            }
        }
        return false;
    }

    /**
     * Prepares the recipe to be run.
     * <ol>
     * <li>The recipe is run in parallel if possible.</li>
     * <li>The potentially parallel recipe is then checked to exist.</li>
     * <li>If it exists, it checks if the recipe is runnable with the current inputs.</li>
     * </ol>
     * If the above conditions are met, the recipe is engaged to be run
     *
     * @param recipe the recipe to prepare
     * @return true if the recipe was successfully prepared from the default inventory, else false
     */
    public boolean prepareRecipe(Recipe recipe) {
        return prepareRecipe(recipe, getInputInventory(), getInputTank());
    }

    /**
     * DO NOT use the parallelLimit field directly, EVER
     *
     * @return the current parallel limit of the logic
     */
    public int getParallelLimit() {
        return parallelLimit;
    }

    /**
     * Set the parallel limit
     * 
     * @param amount the amount to set
     */
    public void setParallelLimit(int amount) {
        parallelLimit = amount;
    }

    /**
     * Sets an EU/t discount to apply to a machine when running recipes.<br>
     * This does NOT affect recipe lookup voltage, even if the discount drops it to a lower voltage tier.<br>
     * This discount is applied pre-parallel/pre-overclock.
     *
     * @param discount The discount, must be greater than 0 and less than 1.
     *                 If discount == 0.75, then the recipe will only require 75% of the listed power to run.
     *                 If discount is > 1, then the recipe will require more than the listed power to run.
     *                 <strong>Be careful as this may not always be possible within the EU/t maximums of the machine!
     *                 </strong>
     */
    public void setEUDiscount(double discount) {
        if (discount <= 0) {
            GTLog.logger.warn("Cannot set EU discount for recipe logic to {}, discount must be > 0", discount);
            return;
        }
        euDiscount = discount;
    }

    /**
     * @return the EU/t discount, or -1 if no discount.
     */
    public double getEUtDiscount() {
        return euDiscount;
    }

    /**
     * Sets a speed multiplier to apply to a machine when running recipes.<br>
     * This discount is applied pre-parallel/pre-overclock.
     *
     * @param bonus The bonus, must be greater than 0.
     *              If bonus == 0.2, then the recipe will be 20% of the normal duration.
     *              If bonus is > 1, then the recipe will be slower than the normal duration.
     */
    public void setSpeedBonus(double bonus) {
        if (bonus <= 0) {
            GTLog.logger.warn("Cannot set speed bonus for recipe logic to {}, bonus must be > 0", bonus);
            return;
        }
        speedBonus = bonus;
    }

    /**
     * @return the speed bonus, or -1 if no bonus.
     */
    public double getSpeedBonus() {
        return speedBonus;
    }

    /**
     * @return the parallel logic type to use for recipes
     */
    @Override
    @NotNull
    public ParallelLogicType getParallelLogicType() {
        return ParallelLogicType.MULTIPLY;
    }

    /**
     * @param tanks the tanks to check
     * @return the minimum fluid capacity of the tanks
     */
    protected static int getMinTankCapacity(@NotNull IMultipleTankHandler tanks) {
        if (tanks.getTanks() == 0) {
            return 0;
        }
        int result = Integer.MAX_VALUE;
        for (IFluidTank fluidTank : tanks.getFluidTanks()) {
            result = Math.min(fluidTank.getCapacity(), result);
        }
        return result;
    }

    /**
     * Find a recipe using inputs
     * 
     * @param maxVoltage  the maximum voltage the recipe can have
     * @param inputs      the item inputs used to search for the recipe
     * @param fluidInputs the fluid inputs used to search for the recipe
     * @return the recipe if found, otherwise null
     */
    @Nullable
    protected Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs) {
        RecipeMap<?> map = getRecipeMap();
        if (map == null || !isRecipeMapValid(map)) {
            return null;
        }

        return map.findRecipe(maxVoltage, inputs, fluidInputs);
    }

    /**
     * @param recipeMap the recipemap to check
     * @return true if the recipemap is valid for recipe search
     */
    public boolean isRecipeMapValid(@NotNull RecipeMap<?> recipeMap) {
        return true;
    }

    /**
     * @param stackA the first stack to check
     * @param stackB the second stack to check
     * @return true if both ItemStacks are equal
     */
    protected static boolean areItemStacksEqual(@NotNull ItemStack stackA, @NotNull ItemStack stackB) {
        return (stackA.isEmpty() && stackB.isEmpty()) ||
                (ItemStack.areItemsEqual(stackA, stackB) &&
                        ItemStack.areItemStackTagsEqual(stackA, stackB));
    }

    /**
     * Determines if the provided recipe is possible to run from the provided inventory, or if there is anything
     * preventing the Recipe from being completed.
     * <p>
     * Will consume the inputs of the Recipe if it is possible to run.
     *
     * @param recipe          The Recipe that will be consumed from the inputs and ran in the machine
     * @param importInventory The inventory that the recipe should be consumed from. Used mainly for Distinct bus
     *                        implementation for multiblocks to specify a specific bus
     * @return the recipe if the setup is successful, null if the setup is not successful
     */
    @MustBeInvokedByOverriders
    protected @Nullable Recipe setupAndConsumeRecipeInputs(@NotNull Recipe recipe,
                                                           @NotNull IItemHandlerModifiable importInventory) {
        return setupAndConsumeRecipeInputs(recipe, importInventory, this.getInputTank());
    }

    /**
     * Determines if the provided recipe is possible to run from the provided inventory, or if there is anything
     * preventing the Recipe from being completed.
     * <p>
     * Will consume the inputs of the Recipe if it is possible to run.
     *
     * @param recipe          The Recipe that will be consumed from the inputs and ran in the machine
     * @param importInventory The inventory that the recipe should be consumed from. Used mainly for Distinct bus
     *                        implementation for multiblocks to specify a specific bus, or for addons to use external
     *                        inventories.
     * @param importFluids    The tanks that the recipe should be consumed from Used currently in addons to use
     *                        external tanks.
     * @return the recipe if the setup is successful, null if the setup is not successful
     */
    protected final @Nullable Recipe setupAndConsumeRecipeInputs(@NotNull Recipe recipe,
                                                                 @NotNull IItemHandlerModifiable importInventory,
                                                                 @NotNull IMultipleTankHandler importFluids) {
        calculateOverclock(recipe);

        modifyOverclockPost(ocResult, recipe.propertyStorage());

        if (ocResult.parallel() > 1) {
            recipe = subTickOC(ocResult, recipe, importInventory, importFluids);
            if (recipe == null) {
                invalidateInputs();
                return null;
            }
        }

        if (!hasEnoughPower(ocResult.eut(), ocResult.duration())) {
            ocResult.reset();
            return null;
        }

        if (checkOutputSpaceItems(recipe, getOutputInventory()) && checkOutputSpaceFluids(recipe, getOutputTank())) {
            this.isOutputsFull = false;
            if (recipeMatch(recipe, true)) {
                this.metaTileEntity.addNotifiedInput(importInventory);
                return recipe;
            }
        }

        return null;
    }

    /**
     * @param recipe          the recipe to check
     * @param exportInventory the inventory to output to
     * @return if the recipe can be successfully output to the inventory
     */
    protected boolean checkOutputSpaceItems(@NotNull Recipe recipe, @NotNull IItemHandlerModifiable exportInventory) {
        // We have already trimmed outputs and chanced outputs at this time
        // Attempt to merge all outputs + chanced outputs into the output bus, to prevent voiding chanced outputs
        if (!metaTileEntity.canVoidRecipeItemOutputs() &&
                !GTTransferUtils.addItemsToItemHandler(exportInventory, true, recipe.getAllItemOutputs())) {
            this.isOutputsFull = true;
            return false;
        }
        return true;
    }

    /**
     * @param recipe       the recipe to check
     * @param exportFluids the inventory to output to
     * @return if the recipe can be successfully output to the inventory
     */
    protected boolean checkOutputSpaceFluids(@NotNull Recipe recipe, @NotNull IMultipleTankHandler exportFluids) {
        // We have already trimmed fluid outputs at this time
        if (!metaTileEntity.canVoidRecipeFluidOutputs() &&
                !GTTransferUtils.addFluidsToFluidHandler(exportFluids, true, recipe.getAllFluidOutputs())) {
            this.isOutputsFull = true;
            return false;
        }
        return true;
    }

    protected boolean recipeMatch(Recipe recipe, boolean shouldConsume) {
        return recipe.matches(shouldConsume, getInputInventory(), getInputTank());
    }

    protected IItemHandlerModifiable gatherItems(IItemHandlerModifiable inputInventory, IMultipleTankHandler inputFluids) {
        List<IItemHandlerModifiable> items = new ArrayList<>();

        items.add(inputInventory);

        for (var tank : inputFluids.getFluidTanks()) {
            if (tank.getDelegate() instanceof IItemHandlerModifiable handler)
                items.add(handler);
        }

        return new ItemHandlerList(items);
    }

    protected IMultipleTankHandler gatherFluids(IItemHandlerModifiable inputInventory, IMultipleTankHandler inputFluids) {
        List<IFluidTank> fluids = new ArrayList<>(inputFluids.getFluidTanks());

        if (inputInventory instanceof IMultipleTankHandler tankHandler) {
            fluids.addAll(tankHandler.getFluidTanks());
        } else if (inputInventory instanceof ItemHandlerList handlerList) {
            for (IItemHandler handler : handlerList.getBackingHandlers()) {
                if (handler instanceof IMultipleTankHandler tankHandler)
                    fluids.addAll(tankHandler.getFluidTanks());
            }
        }

        return new FluidTankList(inputFluids.allowSameFluidFill(), fluids);
    }

    /**
     * Overclock a recipe beyond a duration of 1 tick using parallelization.
     *
     * @param ocResult        the result of the overclock
     * @param recipe          the recipe to overclock
     * @param importInventory the input item inventory
     * @param importFluids    the input fluid inventory
     * @return the recipe if a valid recipe is produced, otherwise null
     */
    protected @Nullable Recipe subTickOC(@NotNull OCResult ocResult, @NotNull Recipe recipe,
                                         @NotNull IItemHandlerModifiable importInventory,
                                         @NotNull IMultipleTankHandler importFluids) {
        RecipeMap<?> map = getRecipeMap();
        if (map == null) {
            return null;
        }

        Recipe r = new RecipeBuilder<>(recipe, map)
                .EUt(ocResult.eut())
                .build()
                .getResult();

        if (r == null) {
            // should be impossible, but check anyway
            return recipe;
        }

        RecipeBuilder<?> builder = findMultipliedParallelRecipe(map, r, importInventory, importFluids,
                getOutputInventory(), getOutputTank(), ocResult.parallel(), ocResult.parallelEUt(),
                getMetaTileEntity());

        if (builder == null) {
            return null;
        }

        if (builder.getParallel() == 0) {
            return recipe;
        }

        ocResult.setEut(builder.getEUt());
        r = builder.EUt(builder.getEUt())
                .build()
                .getResult();

        if (r == null) {
            return recipe;
        }

        return r;
    }

    /**
     * @param eut      the overclocked EUt to check
     * @param duration the overclocked duration to check
     * @return true if there is enough energy to continue recipe progress
     */
    protected boolean hasEnoughPower(long eut, int duration) {
        if (eut >= 0) {
            // Power Consumption case
            // ensure it can run for at least 8 ticks. Arbitrary value, but should prevent instant failures
            return getEnergyStored() >= (eut << 3);
        } else {
            // Power Generation case
            // Return true if we can fit at least 1A of energy into the energy output
            return getEnergyStored() - eut <= getEnergyCapacity();
        }
    }

    /**
     * Method for modifying the overclock results, such as for Multiblock coil bonuses. Is always called, even if no
     * overclocks are performed.
     *
     * @param ocResult The overclock result
     * @param storage  the RecipePropertyStorage of the recipe being processed
     */
    protected void modifyOverclockPost(@NotNull OCResult ocResult, @NotNull RecipePropertyStorage storage) {}

    /**
     * Calculates the overclocked Recipe's final duration and EU/t
     *
     * @param recipe the recipe to run
     */
    protected final void calculateOverclock(@NotNull Recipe recipe) {
        // perform the actual overclocking
        ocParams.initialize(recipe.getEUt(), recipe.getDuration(), getNumberOfOCs(recipe.getEUt()));
        performOverclocking(recipe, this.ocParams, this.ocResult);
        ocParams.reset();
    }

    /**
     * Determines the maximum number of overclocks that can be performed for a recipe. Then performs overclocking on the
     * Recipe.
     *
     * @param recipe   the recipe to overclock
     * @param ocParams the parameters for overclocking
     * @param ocResult the result of overclocking
     */
    protected void performOverclocking(@NotNull Recipe recipe, @NotNull OCParams ocParams, @NotNull OCResult ocResult) {
        modifyOverclockPre(ocParams, recipe.propertyStorage());

        if (ocParams.ocAmount() <= 0) {
            // number of OCs is <= 0, so do not overclock
            ocResult.init(ocParams.eut(), ocParams.duration());
        } else {
            runOverclockingLogic(ocParams, ocResult, recipe.propertyStorage(), getMaximumOverclockVoltage());
        }
    }

    /**
     * @param recipeEUt the EUt of the recipe
     * @return the number of times to overclock the recipe
     */
    protected int getNumberOfOCs(long recipeEUt) {
        if (!isAllowOverclocking()) return 0;

        int recipeTier = GTUtility.getOCTierByVoltage(recipeEUt);
        int maximumTier = getOverclockForTier(getMaximumOverclockVoltage());
        if (maximumTier <= GTValues.LV) return 0;

        // The maximum number of overclocks is determined by the difference between the tier the recipe is running at,
        // and the maximum tier that the machine can overclock to.
        int numberOfOCs = maximumTier - recipeTier;
        if (recipeTier == ULV) numberOfOCs--; // no ULV overclocking

        return numberOfOCs;
    }

    /**
     * Perform changes to the recipe EUt, duration, and OC count before overclocking. Is always called, even if no
     * overclocks are to be performed.
     *
     * @param ocParams an array of [recipeEUt, recipeDuration, numberOfOCs]
     * @param storage  the RecipePropertyStorage of the recipe being processed
     */
    protected void modifyOverclockPre(@NotNull OCParams ocParams, @NotNull RecipePropertyStorage storage) {}

    /**
     * Calls the desired overclocking logic to be run for the recipe. Performs the actual overclocking on the provided
     * recipe. Override this to call custom overclocking mechanics
     *
     * @param ocParams        the parameters for the overclock
     * @param ocResult        the result to store the overclock in
     * @param propertyStorage the recipe's property storage
     * @param maxVoltage      the maximum voltage the recipe is allowed to be run at
     */
    protected void runOverclockingLogic(@NotNull OCParams ocParams, @NotNull OCResult ocResult,
                                        @NotNull RecipePropertyStorage propertyStorage, long maxVoltage) {
        standardOC(ocParams, ocResult, maxVoltage, getOverclockingDurationFactor(), getOverclockingVoltageFactor());
    }

    /**
     * @return the multiplier to use for reducing duration upon overclocking
     */
    protected double getOverclockingDurationFactor() {
        return hasPerfectOC ? PERFECT_DURATION_FACTOR : STD_DURATION_FACTOR;
    }

    /**
     * @return the multiplier to use for increasing voltage upon overclocking
     */
    protected double getOverclockingVoltageFactor() {
        return STD_VOLTAGE_FACTOR;
    }

    /**
     * Finds the maximum tier that a recipe can overclock to, when provided the maximum voltage a recipe can overclock
     * to.
     *
     * @param voltage The maximum voltage the recipe is allowed to overclock to.
     * @return the highest voltage tier the machine should use to overclock with
     */
    protected int getOverclockForTier(long voltage) {
        return GTUtility.getOCTierByVoltage(voltage);
    }

    /**
     * Creates an array of Voltage Names that the machine/multiblock can overclock to.
     * Since this is for use with the customizable overclock button, all tiers up to
     * {@link AbstractRecipeLogic#getMaxVoltage()}
     * are allowed, since the button is initialized to this value.
     *
     * @return a String array of the voltage names allowed to be used for overclocking
     */
    public String[] getAvailableOverclockingTiers() {
        final int maxTier = getOverclockForTier(getMaxVoltage());
        final String[] result = new String[maxTier + 1];
        result[0] = "gregtech.gui.overclock.off";
        if (maxTier >= 0) System.arraycopy(GTValues.VNF, 1, result, 1, maxTier);
        return result;
    }

    /**
     * sets up the recipe to be run
     *
     * @param recipe the recipe to run
     */
    @MustBeInvokedByOverriders
    protected void setupRecipe(@NotNull Recipe recipe) {
        this.progressTime = 1;
        setMaxProgress(ocResult.duration());
        this.recipeEUt = ocResult.eut();

        int recipeTier = GTUtility.getTierByVoltage(recipe.getEUt());
        int machineTier = getOverclockForTier(getMaximumOverclockVoltage());

        RecipeMap<?> map = getRecipeMap();
        if (map != null) {
            this.fluidOutputs = GTUtility
                    .copyFluidList(recipe.getResultFluidOutputs(recipeTier, machineTier, map));
            this.itemOutputs = GTUtility
                    .copyStackList(recipe.getResultItemOutputs(recipeTier, machineTier, map));
        }

        if (this.wasActiveAndNeedsUpdate) {
            this.wasActiveAndNeedsUpdate = false;
        } else {
            this.setActive(true);
        }
    }

    /**
     * completes the recipe which was being run, and performs actions done upon recipe completion
     */
    protected void completeRecipe() {
        outputRecipeOutputs();
        this.progressTime = 0;
        setMaxProgress(0);
        this.recipeEUt = 0;
        this.fluidOutputs = null;
        this.itemOutputs = null;
        this.hasNotEnoughEnergy = false;
        this.wasActiveAndNeedsUpdate = true;
        this.parallelRecipesPerformed = 0;
        this.ocResult.reset();
    }

    /**
     * outputs the items created by the recipe
     */
    protected void outputRecipeOutputs() {
        GTTransferUtils.addItemsToItemHandler(getOutputInventory(), false, itemOutputs);
        GTTransferUtils.addFluidsToFluidHandler(getOutputTank(), false, fluidOutputs);
    }

    /**
     * @return the progress percentage towards completion. Format: {@code 0.1 = 10%}.
     */
    public double getProgressPercent() {
        return getMaxProgress() == 0 ? 0.0 : getProgress() / (getMaxProgress() * 1.0);
    }

    @Override
    public int getProgress() {
        return progressTime;
    }

    @Override
    public int getMaxProgress() {
        return maxProgressTime;
    }

    /**
     * @return the current recipe's EU/t
     */
    public long getRecipeEUt() {
        return recipeEUt;
    }

    /**
     * @return the current recipe's EU/t for TOP/Waila/Tricorder
     */
    public long getInfoProviderEUt() {
        return getRecipeEUt();
    }

    /**
     * @return the previous recipe's duration
     */
    public int getPreviousRecipeDuration() {
        return getPreviousRecipe() == null ? 0 : getPreviousRecipe().getDuration();
    }

    /**
     * sets the amount of ticks of running time to finish the recipe
     *
     * @param maxProgress the amount of ticks to set
     */
    public void setMaxProgress(int maxProgress) {
        this.maxProgressTime = maxProgress;
        metaTileEntity.markDirty();
    }

    /**
     * Set the workable as active or inactive
     *
     * @param active true to set active, false to set inactive
     */
    protected void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            metaTileEntity.markDirty();
            World world = metaTileEntity.getWorld();
            if (world != null && !world.isRemote) {
                writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        metaTileEntity.markDirty();
        World world = metaTileEntity.getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }

    /**
     * @return true if there is not enough energy to continue recipe progress
     */
    public boolean isHasNotEnoughEnergy() {
        return hasNotEnoughEnergy;
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    /**
     * @return true if the workable is actively working
     */
    public boolean isWorking() {
        return isActive && !hasNotEnoughEnergy && workingEnabled;
    }

    /**
     * Toggles if the Machine/Multiblock is allowed to overclock.
     *
     * @param allowOverclocking If overclocking is allowed
     */
    public void setAllowOverclocking(boolean allowOverclocking) {
        this.allowOverclocking = allowOverclocking;
        this.overclockVoltage = allowOverclocking ? getMaximumOverclockVoltage() : GTValues.V[GTValues.ULV];
        metaTileEntity.markDirty();
    }

    /**
     * @return Whether Overclocking is allowed for the current machine/multiblock
     */
    public boolean isAllowOverclocking() {
        return allowOverclocking;
    }

    /**
     * Sets the maximum voltage that the machine is allowed to overclock to.
     * If the passed tier is ULV, overclocking for this machine is disabled.
     *
     * @param overclockVoltage The maximum voltage that the machine can overclock to. This must correspond to a
     *                         voltage tier in <B>GTValues.V</>
     */
    public void setMaximumOverclockVoltage(final long overclockVoltage) {
        this.overclockVoltage = overclockVoltage;
        // Overclocking is not allowed if the passed voltage is ULV
        this.allowOverclocking = (overclockVoltage != GTValues.V[GTValues.ULV]);
        metaTileEntity.markDirty();
    }

    /**
     * @return The maximum voltage the machine/multiblock can overclock to
     */
    public long getMaximumOverclockVoltage() {
        return overclockVoltage;
    }

    /**
     * This is needed as CycleButtonWidget requires an IntSupplier, without making an Enum of tiers.
     *
     * @return The current Tier for the voltage the machine is allowed to overclock to
     */
    public int getOverclockTier() {
        // If we do not allow overclocking, return ULV tier
        if (!isAllowOverclocking()) {
            return GTValues.ULV;
        }

        // This will automatically handle ULV, and return 0
        return getOverclockForTier(this.overclockVoltage);
    }

    /**
     * Sets the maximum Tier that the machine/multiblock is allowed to overclock to.
     * This is used for the Overclock button in Machine GUIs.
     * This is needed as CycleButtonWidget requires an Int Supplier, without making an Enum of tiers.
     *
     * @param tier The maximum tier the multiblock/machine can overclock to
     */
    public void setOverclockTier(final int tier) {
        setMaximumOverclockVoltage(GTValues.V[tier]);
    }

    /**
     * Used to reset cached values in the Recipe Logic on events such as multiblock structure deformation
     */
    @MustBeInvokedByOverriders
    public void invalidate() {
        previousRecipe = null;
        progressTime = 0;
        maxProgressTime = 0;
        recipeEUt = 0;
        fluidOutputs = null;
        itemOutputs = null;
        parallelRecipesPerformed = 0;
        isOutputsFull = false;
        invalidInputsForRecipes = false;
        this.ocResult.reset();
        setActive(false); // this marks dirty for us
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.isActive = buf.readBoolean();
            getMetaTileEntity().scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
            getMetaTileEntity().scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        this.isActive = buf.readBoolean();
        this.workingEnabled = buf.readBoolean();
    }

    @NotNull
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("WorkEnabled", workingEnabled);
        compound.setBoolean("CanRecipeProgress", canRecipeProgress);
        compound.setBoolean(ALLOW_OVERCLOCKING, allowOverclocking);
        compound.setLong(OVERCLOCK_VOLTAGE, this.overclockVoltage);
        if (progressTime > 0) {
            compound.setInteger("Progress", progressTime);
            compound.setInteger("MaxProgress", maxProgressTime);
            compound.setLong("RecipeEUt", this.recipeEUt);
            NBTTagList itemOutputsList = new NBTTagList();
            for (ItemStack itemOutput : itemOutputs) {
                itemOutputsList.appendTag(itemOutput.writeToNBT(new NBTTagCompound()));
            }
            NBTTagList fluidOutputsList = new NBTTagList();
            for (FluidStack fluidOutput : fluidOutputs) {
                fluidOutputsList.appendTag(fluidOutput.writeToNBT(new NBTTagCompound()));
            }
            compound.setTag("ItemOutputs", itemOutputsList);
            compound.setTag("FluidOutputs", fluidOutputsList);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        this.workingEnabled = compound.getBoolean("WorkEnabled");
        this.canRecipeProgress = compound.getBoolean("CanRecipeProgress");
        this.progressTime = compound.getInteger("Progress");
        this.allowOverclocking = compound.getBoolean(ALLOW_OVERCLOCKING);
        this.overclockVoltage = compound.getLong(OVERCLOCK_VOLTAGE);
        this.isActive = false;
        if (progressTime > 0) {
            this.isActive = true;
            this.maxProgressTime = compound.getInteger("MaxProgress");
            this.recipeEUt = Math.abs(compound.getLong("RecipeEUt"));
            NBTTagList itemOutputsList = compound.getTagList("ItemOutputs", Constants.NBT.TAG_COMPOUND);
            this.itemOutputs = new ArrayList<>(itemOutputsList.tagCount());
            for (int i = 0; i < itemOutputsList.tagCount(); i++) {
                this.itemOutputs.add(new ItemStack(itemOutputsList.getCompoundTagAt(i)));
            }
            NBTTagList fluidOutputsList = compound.getTagList("FluidOutputs", Constants.NBT.TAG_COMPOUND);
            this.fluidOutputs = new ArrayList<>();
            for (int i = 0; i < fluidOutputsList.tagCount(); i++) {
                this.fluidOutputs.add(FluidStack.loadFluidStackFromNBT(fluidOutputsList.getCompoundTagAt(i)));
            }
        }
    }
}
