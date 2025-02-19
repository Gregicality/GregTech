package gregtech.api.metatileentity.multiblock;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.mui.sync.FixedIntArraySyncValue;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class FuelMultiblockController extends RecipeMapMultiblockController {

    public FuelMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier) {
        super(metaTileEntityId, recipeMap);
        this.recipeMapWorkable = new MultiblockFuelRecipeLogic(this);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
    }

    @Override
    protected void initializeAbilities() {
        super.initializeAbilities();
        List<IEnergyContainer> outputEnergy = new ArrayList<>(getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        outputEnergy.addAll(getAbilities(MultiblockAbility.SUBSTATION_OUTPUT_ENERGY));
        outputEnergy.addAll(getAbilities(MultiblockAbility.OUTPUT_LASER));
        this.energyContainer = new EnergyContainerList(outputEnergy);
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        MultiblockFuelRecipeLogic recipeLogic = (MultiblockFuelRecipeLogic) recipeMapWorkable;

        builder.setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addEnergyProductionLine(getMaxVoltage(), recipeLogic.getRecipeEUt())
                .addFuelNeededLine(recipeLogic.getRecipeFluidInputInfo(), recipeLogic.getPreviousRecipeDuration())
                .addWorkingStatusLine();
    }

    @Override
    protected void configureWarningText(MultiblockUIBuilder builder) {
        builder.addLowDynamoTierLine(isDynamoTierTooLow())
                .addMaintenanceProblemLines(getMaintenanceProblems());
    }

    protected long getMaxVoltage() {
        IEnergyContainer energyContainer = recipeMapWorkable.getEnergyContainer();
        if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
            return Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
        } else {
            return 0L;
        }
    }

    protected boolean isDynamoTierTooLow() {
        if (isStructureFormed()) {
            IEnergyContainer energyContainer = recipeMapWorkable.getEnergyContainer();
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
                return maxVoltage < recipeMapWorkable.getRecipeEUt();
            }
        }
        return false;
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
        if (recipeMapWorkable.getMaxProgress() > 0) {
            list.add(new TextComponentTranslation("behavior.tricorder.workable_progress",
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(recipeMapWorkable.getProgress() / 20))
                            .setStyle(new Style().setColor(TextFormatting.GREEN)),
                    new TextComponentTranslation(
                            TextFormattingUtil.formatNumbers(recipeMapWorkable.getMaxProgress() / 20))
                                    .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        }

        list.add(new TextComponentTranslation("behavior.tricorder.energy_container_storage",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getEnergyStored()))
                        .setStyle(new Style().setColor(TextFormatting.GREEN)),
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getEnergyCapacity()))
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))));

        if (!recipeMapWorkable.consumesEnergy()) {
            list.add(new TextComponentTranslation("behavior.tricorder.workable_production",
                    new TextComponentTranslation(
                            TextFormattingUtil.formatNumbers(Math.abs(recipeMapWorkable.getInfoProviderEUt())))
                                    .setStyle(new Style().setColor(TextFormatting.RED)),
                    new TextComponentTranslation(
                            TextFormattingUtil.formatNumbers(recipeMapWorkable.getInfoProviderEUt() == 0 ? 0 : 1))
                                    .setStyle(new Style().setColor(TextFormatting.RED))));

            list.add(new TextComponentTranslation("behavior.tricorder.multiblock_energy_output",
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getOutputVoltage()))
                            .setStyle(new Style().setColor(TextFormatting.YELLOW)),
                    new TextComponentTranslation(
                            GTValues.VN[GTUtility.getTierByVoltage(energyContainer.getOutputVoltage())])
                                    .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        }

        if (ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics()) {
            list.add(new TextComponentTranslation("behavior.tricorder.multiblock_maintenance",
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(getNumMaintenanceProblems()))
                            .setStyle(new Style().setColor(TextFormatting.RED))));
        }

        return list;
    }

    protected int[] getTotalFluidAmount(FluidStack testStack, IMultipleTankHandler multiTank) {
        int fluidAmount = 0;
        int fluidCapacity = 0;
        for (var tank : multiTank) {
            if (tank != null) {
                FluidStack drainStack = tank.drain(testStack, false);
                if (drainStack != null && drainStack.amount > 0) {
                    fluidAmount += drainStack.amount;
                    fluidCapacity += tank.getCapacity();
                }
            }
        }
        return new int[] { fluidAmount, fluidCapacity };
    }

    @Deprecated
    protected void addFuelText(List<ITextComponent> textList) {
        // Fuel
        int fuelStored = 0;
        int fuelCapacity = 0;
        FluidStack fuelStack = null;
        MultiblockFuelRecipeLogic recipeLogic = (MultiblockFuelRecipeLogic) recipeMapWorkable;
        if (isStructureFormed() && recipeLogic.getInputFluidStack() != null && getInputFluidInventory() != null) {
            fuelStack = recipeLogic.getInputFluidStack().copy();
            fuelStack.amount = Integer.MAX_VALUE;
            int[] fuelAmount = getTotalFluidAmount(fuelStack, getInputFluidInventory());
            fuelStored = fuelAmount[0];
            fuelCapacity = fuelAmount[1];
        }

        if (fuelStack != null) {
            ITextComponent fuelName = TextComponentUtil.setColor(GTUtility.getFluidTranslation(fuelStack),
                    TextFormatting.GOLD);
            ITextComponent fuelInfo = new TextComponentTranslation("%s / %s L (%s)",
                    TextFormattingUtil.formatNumbers(fuelStored),
                    TextFormattingUtil.formatNumbers(fuelCapacity),
                    fuelName);
            textList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "gregtech.multiblock.large_combustion_engine.fuel_amount",
                    TextComponentUtil.setColor(fuelInfo, TextFormatting.GOLD)));
        } else {
            textList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "gregtech.multiblock.large_combustion_engine.fuel_amount",
                    "0 / 0 L"));
        }
    }

    /**
     * @param tooltip       the tooltip to populate
     * @param amounts       the sync value containing an array of [fuel stored, fuel capacity]
     * @param fuelNameValue the name of the fuel
     */
    protected void createFuelTooltip(@NotNull RichTooltip tooltip, @NotNull FixedIntArraySyncValue amounts,
                                     @NotNull StringSyncValue fuelNameValue) {
        if (isStructureFormed()) {
            Fluid fluid = fuelNameValue.getStringValue() == null ? null :
                    FluidRegistry.getFluid(fuelNameValue.getStringValue());
            if (fluid == null) {
                tooltip.addLine(IKey.lang("gregtech.multiblock.large_combustion_engine.fuel_none"));
            } else {
                tooltip.addLine(
                        IKey.lang("gregtech.multiblock.large_combustion_engine.fuel_amount", amounts.getValue(0),
                                amounts.getValue(1), fluid.getLocalizedName(new FluidStack(fluid, 1))));
            }
        } else {
            tooltip.addLine(IKey.lang("gregtech.multiblock.invalid_structure"));
        }
    }
}
