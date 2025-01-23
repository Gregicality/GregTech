package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.GTValues;
import gregtech.api.capability.IRotorHolder;
import gregtech.api.capability.MultipleTankHandler;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class LargeTurbineWorkableHandler extends MultiblockFuelRecipeLogic {

    private final int BASE_EU_OUTPUT;

    private long excessVoltage;

    public LargeTurbineWorkableHandler(RecipeMapMultiblockController metaTileEntity, int tier) {
        super(metaTileEntity);
        this.BASE_EU_OUTPUT = (int) GTValues.V[tier] * 2;
    }

    @Override
    protected void updateRecipeProgress() {
        if (canRecipeProgress) {
            // turbines can void energy
            drawEnergy(recipeEUt, false);
            // as recipe starts with progress on 1 this has to be > only not => to compensate for it
            if (++progressTime > maxProgressTime) {
                completeRecipe();
            }
        }
    }

    public FluidStack getInputFluidStack() {
        // Previous Recipe is always null on first world load, so try to acquire a new recipe
        if (previousRecipe == null) {
            // previousRecipe is set whenever a valid recipe is found
            // if it's not set, find *any* recipe we have at least the base (non-parallel) inputs for
            Recipe recipe = super.findRecipe(Integer.MAX_VALUE, getInputInventory(), getInputTank());

            return recipe == null ? null : getInputTank().drain(
                    new FluidStack(recipe.getFluidInputs().get(0).getInputFluidStack().getFluid(), Integer.MAX_VALUE),
                    false);
        }
        FluidStack fuelStack = previousRecipe.getFluidInputs().get(0).getInputFluidStack();
        return getInputTank().drain(new FluidStack(fuelStack.getFluid(), Integer.MAX_VALUE), false);
    }

    @Override
    public long getMaxVoltage() {
        IRotorHolder rotorHolder = ((MetaTileEntityLargeTurbine) metaTileEntity).getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor())
            return (long) BASE_EU_OUTPUT * rotorHolder.getTotalPower() / 100;
        return 0;
    }

    @Override
    protected long boostProduction(long production) {
        IRotorHolder rotorHolder = ((MetaTileEntityLargeTurbine) metaTileEntity).getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            int maxSpeed = rotorHolder.getMaxRotorHolderSpeed();
            int currentSpeed = rotorHolder.getRotorSpeed();
            if (currentSpeed >= maxSpeed)
                return production;
            return (long) (production * Math.pow(1.0 * currentSpeed / maxSpeed, 2));
        }
        return 0;
    }

    private int getParallel(@NotNull Recipe recipe, double totalHolderEfficiencyCoefficient, long turbineMaxVoltage) {
        return MathHelper.ceil((turbineMaxVoltage - this.excessVoltage) /
                (recipe.getEUt() * totalHolderEfficiencyCoefficient));
    }

    private boolean canDoRecipeWithParallel(Recipe recipe) {
        IRotorHolder rotorHolder = ((MetaTileEntityLargeTurbine) metaTileEntity).getRotorHolder();
        if (rotorHolder == null || !rotorHolder.hasRotor())
            return false;
        double totalHolderEfficiencyCoefficient = rotorHolder.getTotalEfficiency() / 100.0;
        long turbineMaxVoltage = getMaxVoltage();
        int parallel = getParallel(recipe, totalHolderEfficiencyCoefficient, turbineMaxVoltage);

        FluidStack recipeFluidStack = recipe.getFluidInputs().get(0).getInputFluidStack();

        // Intentionally not using this.getInputFluidStack because that is locked to the previous recipe
        FluidStack inputFluid = getInputTank().drain(
                new FluidStack(recipeFluidStack.getFluid(), Integer.MAX_VALUE),
                false);
        return inputFluid != null && inputFluid.amount >= recipeFluidStack.amount * parallel;
    }

    @Override
    protected boolean checkPreviousRecipe() {
        return super.checkPreviousRecipe() && canDoRecipeWithParallel(this.previousRecipe);
    }

    @Override
    protected @Nullable Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs,
                                          MultipleTankHandler fluidInputs) {
        RecipeMap<?> map = getRecipeMap();
        if (map == null || !isRecipeMapValid(map)) {
            return null;
        }

        final List<ItemStack> items = GTUtility.itemHandlerToList(inputs).stream().filter(s -> !s.isEmpty()).collect(
                Collectors.toList());
        final List<FluidStack> fluids = GTUtility.fluidHandlerToList(fluidInputs).stream()
                .filter(f -> f != null && f.amount != 0)
                .collect(Collectors.toList());

        return map.find(items, fluids, recipe -> {
            if (recipe.getEUt() > maxVoltage) return false;
            return recipe.matches(false, inputs, fluidInputs) && this.canDoRecipeWithParallel(recipe);
        });
    }

    @Override
    public boolean prepareRecipe(Recipe recipe) {
        IRotorHolder rotorHolder = ((MetaTileEntityLargeTurbine) metaTileEntity).getRotorHolder();
        if (rotorHolder == null || !rotorHolder.hasRotor())
            return false;

        long turbineMaxVoltage = getMaxVoltage();
        FluidStack recipeFluidStack = recipe.getFluidInputs().get(0).getInputFluidStack();
        int parallel = 0;

        if (this.excessVoltage >= turbineMaxVoltage) {
            this.excessVoltage -= turbineMaxVoltage;
        } else {
            double holderEfficiency = rotorHolder.getTotalEfficiency() / 100.0;
            // get the amount of parallel required to match the desired output voltage
            parallel = getParallel(recipe, holderEfficiency, turbineMaxVoltage);

            // Null check fluid here, since it can return null on first join into world or first form
            FluidStack inputFluid = getInputFluidStack();
            if (inputFluid == null || getInputFluidStack().amount < recipeFluidStack.amount * parallel) {
                return false;
            }

            // this is necessary to prevent over-consumption of fuel
            this.excessVoltage += (long) (parallel * recipe.getEUt() * holderEfficiency - turbineMaxVoltage);
        }

        // rebuild the recipe and adjust voltage to match the turbine
        RecipeBuilder<?> recipeBuilder = getRecipeMap().recipeBuilder();
        recipeBuilder.append(recipe, parallel, false)
                .EUt(turbineMaxVoltage);
        applyParallelBonus(recipeBuilder);
        recipe = recipeBuilder.build().getResult();

        if (recipe != null) {
            recipe = setupAndConsumeRecipeInputs(recipe, getInputInventory());
            if (recipe != null) {
                setupRecipe(recipe);
                return true;
            }
        }
        return false;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        excessVoltage = 0;
    }

    public void updateTanks() {
        FuelMultiblockController controller = (FuelMultiblockController) this.metaTileEntity;
        List<IFluidTank> tanks = controller.getNotifiedFluidInputList();
        tanks.addAll(controller.getAbilities(MultiblockAbility.IMPORT_FLUIDS));
    }
}
