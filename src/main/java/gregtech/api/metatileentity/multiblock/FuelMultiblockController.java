package gregtech.api.metatileentity.multiblock;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;

import java.util.List;

public abstract class FuelMultiblockController extends RecipeMapMultiblockController {

    public FuelMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier) {
        super(metaTileEntityId, recipeMap);
        this.recipeMapWorkable = new MultiblockFuelRecipeLogic(this);
        this.recipeMapWorkable.enableOverclockVoltage();
        this.recipeMapWorkable.setOverclockTier((int) tier);
    }

    @Override
    protected void initializeAbilities() {
        super.initializeAbilities();
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.OUTPUT_ENERGY));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (!isStructureFormed()) {
            ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.invalid_structure.tooltip");
            tooltip.setStyle(new Style().setColor(TextFormatting.GRAY));
            textList.add(new TextComponentTranslation("gregtech.multiblock.invalid_structure")
                    .setStyle(new Style().setColor(TextFormatting.RED)
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
        } else {
            if (hasMaintenanceMechanics())
                addMaintenanceText(textList);

            if (hasMufflerMechanics() && !isMufflerFaceFree())
                textList.add(new TextComponentTranslation("gregtech.multiblock.universal.muffler_obstructed")
                        .setStyle(new Style().setColor(TextFormatting.RED)
                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new TextComponentTranslation("gregtech.multiblock.universal.muffler_obstructed.tooltip")))));

            IEnergyContainer energyContainer = recipeMapWorkable.getEnergyContainer();
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
                String voltageName = GTValues.VN[GTUtility.getTierByVoltage(maxVoltage)];
                textList.add(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }

            if (!recipeMapWorkable.isWorkingEnabled()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));
            } else if (recipeMapWorkable.isActive()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.running"));
                int currentProgress = (int) (recipeMapWorkable.getProgressPercent() * 100);
                textList.add(new TextComponentTranslation("gregtech.multiblock.progress", currentProgress));
            } else {
                textList.add(new TextComponentTranslation("gregtech.multiblock.idling"));
            }
        }
    }
}
