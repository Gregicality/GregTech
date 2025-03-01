package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIFactory;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.sync.FixedIntArraySyncValue;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockMultiblockCasing.MultiblockCasingType;
import gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityLargeCombustionEngine extends FuelMultiblockController implements ProgressBarMultiblock {

    private final int tier;
    private final boolean isExtreme;
    private boolean boostAllowed;
    private boolean hasLubricant;

    public MetaTileEntityLargeCombustionEngine(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, RecipeMaps.COMBUSTION_GENERATOR_FUELS, tier);
        this.recipeMapWorkable = new LargeCombustionEngineWorkableHandler(this, tier > GTValues.EV);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
        this.tier = tier;
        this.isExtreme = tier > GTValues.EV;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeCombustionEngine(metaTileEntityId, tier);
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        var recipeLogic = (LargeCombustionEngineWorkableHandler) recipeMapWorkable;

        builder.setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive());

        if (isExtreme) {
            builder.addEnergyProductionLine(GTValues.V[tier + 1], recipeLogic.getRecipeEUt());
        } else {
            builder.addEnergyProductionAmpsLine(GTValues.V[tier] * 3, 3);
        }

        builder.addFuelNeededLine(recipeLogic.getRecipeFluidInputInfo(), recipeLogic.getPreviousRecipeDuration())
                .addCustom((richText, syncer) -> {
                    if (isStructureFormed() && syncer.syncBoolean(recipeLogic.isOxygenBoosted)) {
                        String key = isExtreme ?
                                "gregtech.multiblock.large_combustion_engine.liquid_oxygen_boosted" :
                                "gregtech.multiblock.large_combustion_engine.oxygen_boosted";
                        richText.add(KeyUtil.lang(TextFormatting.AQUA, key));
                    }
                })
                .addWorkingStatusLine();
    }

    @Override
    protected void configureErrorText(MultiblockUIBuilder builder) {
        var recipeLogic = (LargeCombustionEngineWorkableHandler) recipeMapWorkable;

        builder.addCustom((keyList, syncer) -> {
            if (!isStructureFormed()) return;

            if (syncer.syncBoolean(checkIntakesObstructed())) {
                keyList.add(KeyUtil.lang(TextFormatting.RED,
                        "gregtech.multiblock.large_combustion_engine.obstructed"));
                keyList.add(KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.large_combustion_engine.obstructed.desc"));
            }

            if (syncer.syncBoolean(!recipeLogic.checkLubricant())) {
                keyList.add(KeyUtil.lang(TextFormatting.RED,
                        "gregtech.multiblock.large_combustion_engine.no_lubricant"));
            }
        });
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.base_production_eut", GTValues.V[tier]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.uses_per_hour_lubricant", 1000));
        if (isExtreme) {
            tooltip.add(I18n.format("gregtech.machine.large_combustion_engine.tooltip.boost_extreme",
                    GTValues.V[tier] * 4));
        } else {
            tooltip.add(I18n.format("gregtech.machine.large_combustion_engine.tooltip.boost_regular",
                    GTValues.V[tier] * 3));
        }
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XDX", "XXX")
                .aisle("XCX", "CGC", "XCX")
                .aisle("XCX", "CGC", "XCX")
                .aisle("AAA", "AYA", "AAA")
                .where('X', states(getCasingState()))
                .where('G', states(getGearboxState()))
                .where('C',
                        states(getCasingState()).setMinGlobalLimited(3)
                                .or(autoAbilities(false, true, true, true, true, true, true)))
                .where('D', metaTileEntities(MultiblockAbility.REGISTRY.get(MultiblockAbility.OUTPUT_ENERGY).stream()
                        .filter(mte -> {
                            IEnergyContainer container = mte
                                    .getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
                            return container != null &&
                                    container.getOutputVoltage() * container.getOutputAmperage() >= GTValues.V[tier];
                        })
                        .toArray(MetaTileEntity[]::new))
                                .addTooltip("gregtech.multiblock.pattern.error.limited.1", GTValues.VN[tier]))
                .where('A', states(getIntakeState()).addTooltips("gregtech.multiblock.pattern.clear_amount_1"))
                .where('Y', selfPredicate())
                .build();
    }

    public IBlockState getCasingState() {
        return isExtreme ? MetaBlocks.METAL_CASING.getState(MetalCasingType.TUNGSTENSTEEL_ROBUST) :
                MetaBlocks.METAL_CASING.getState(MetalCasingType.TITANIUM_STABLE);
    }

    public IBlockState getGearboxState() {
        return isExtreme ? MetaBlocks.TURBINE_CASING.getState(TurbineCasingType.TUNGSTENSTEEL_GEARBOX) :
                MetaBlocks.TURBINE_CASING.getState(TurbineCasingType.TITANIUM_GEARBOX);
    }

    public IBlockState getIntakeState() {
        return isExtreme ? MetaBlocks.MULTIBLOCK_CASING.getState(MultiblockCasingType.EXTREME_ENGINE_INTAKE_CASING) :
                MetaBlocks.MULTIBLOCK_CASING.getState(MultiblockCasingType.ENGINE_INTAKE_CASING);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return isExtreme ? Textures.ROBUST_TUNGSTENSTEEL_CASING : Textures.STABLE_TITANIUM_CASING;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return isExtreme ? Textures.EXTREME_COMBUSTION_ENGINE_OVERLAY : Textures.LARGE_COMBUSTION_ENGINE_OVERLAY;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public boolean isStructureObstructed() {
        return super.isStructureObstructed() || checkIntakesObstructed();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        IEnergyContainer energyContainer = getEnergyContainer();
        this.boostAllowed = energyContainer != null && energyContainer.getOutputVoltage() >= GTValues.V[this.tier + 1];
    }

    private boolean checkIntakesObstructed() {
        for (int left = -1; left <= 1; left++) {
            for (int up = -1; up <= 1; up++) {
                if (left == 0 && up == 0) {
                    // Skip the controller block itself
                    continue;
                }

                final BlockPos checkPos = RelativeDirection.offsetPos(
                        getPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), up, left, 1);
                final IBlockState state = getWorld().getBlockState(checkPos);
                if (!state.getBlock().isAir(state, getWorld(), checkPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldShowVoidingModeButton() {
        return false;
    }

    public boolean isBoostAllowed() {
        return boostAllowed;
    }

    @Override
    public int getProgressBarCount() {
        return 3;
    }

    @Override
    public @NotNull ProgressWidget createProgressBar(PanelSyncManager panelSyncManager, int index) {
        return switch (index) {
            case 0 -> {
                FixedIntArraySyncValue fuelValue = new FixedIntArraySyncValue(this::getFuelAmount, null);
                StringSyncValue fuelNameValue = new StringSyncValue(() -> {
                    FluidStack stack = ((MultiblockFuelRecipeLogic) recipeMapWorkable).getInputFluidStack();
                    if (stack == null) {
                        return null;
                    }
                    Fluid fluid = stack.getFluid();
                    if (fluid == null) {
                        return null;
                    }
                    return fluid.getName();
                });
                panelSyncManager.syncValue("fuel_amount", fuelValue);
                panelSyncManager.syncValue("fuel_name", fuelNameValue);

                yield new ProgressWidget()
                        .progress(() -> fuelValue.getValue(1) == 0 ? 0 :
                                1.0 * fuelValue.getValue(0) / fuelValue.getValue(1))
                        .texture(GTGuiTextures.PROGRESS_BAR_LCE_FUEL, MultiblockUIFactory.Bars.THIRD_WIDTH)
                        .tooltipAutoUpdate(true)
                        .tooltipBuilder(t -> createFuelTooltip(t, fuelValue, fuelNameValue));
            }
            case 1 -> {
                FixedIntArraySyncValue lubricantValue = new FixedIntArraySyncValue(this::getLubricantAmount, null);
                panelSyncManager.syncValue("lubricant_amount", lubricantValue);

                yield new ProgressWidget()
                        .progress(() -> lubricantValue.getValue(1) == 0 ? 0 :
                                1.0 * lubricantValue.getValue(0) / lubricantValue.getValue(1))
                        .texture(GTGuiTextures.PROGRESS_BAR_LCE_LUBRICANT, MultiblockUIFactory.Bars.THIRD_WIDTH)
                        .tooltipAutoUpdate(true)
                        .tooltipBuilder(t -> {
                            if (isStructureFormed()) {
                                if (lubricantValue.getValue(0) == 0) {
                                    t.addLine(IKey.lang("gregtech.multiblock.large_combustion_engine.no_lubricant"));
                                } else {
                                    t.addLine(IKey.lang("gregtech.multiblock.large_combustion_engine.lubricant_amount",
                                            lubricantValue.getValue(0), lubricantValue.getValue(0)));
                                }
                            } else {
                                t.addLine(IKey.lang("gregtech.multiblock.invalid_structure"));
                            }
                        });
            }
            case 2 -> {
                FixedIntArraySyncValue oxygenValue = new FixedIntArraySyncValue(this::getOxygenAmount, null);
                BooleanSyncValue boostValue = new BooleanSyncValue(this::isBoostAllowed);
                panelSyncManager.syncValue("oxygen_amount", oxygenValue);
                panelSyncManager.syncValue("boost_allowed", boostValue);

                yield new ProgressWidget()
                        .progress(() -> oxygenValue.getValue(1) == 0 ? 0 :
                                1.0 * oxygenValue.getValue(0) / oxygenValue.getValue(1))
                        .texture(GTGuiTextures.PROGRESS_BAR_LCE_OXYGEN, MultiblockUIFactory.Bars.THIRD_WIDTH)
                        .tooltipAutoUpdate(true)
                        .tooltipBuilder(t -> {
                            if (isStructureFormed()) {
                                if (boostValue.getBoolValue()) {
                                    if (oxygenValue.getValue(0) == 0) {
                                        t.addLine(IKey.lang("gregtech.multiblock.large_combustion_engine.oxygen_none"));
                                    } else if (isExtreme) {
                                        t.addLine(IKey.lang(
                                                "gregtech.multiblock.large_combustion_engine.liquid_oxygen_amount",
                                                oxygenValue.getValue(0), oxygenValue.getValue(0)));
                                    } else {
                                        t.addLine(IKey.lang("gregtech.multiblock.large_combustion_engine.oxygen_amount",
                                                oxygenValue.getValue(0), oxygenValue.getValue(1)));
                                    }
                                } else if (isExtreme) {
                                    t.addLine(IKey.lang(
                                            "gregtech.multiblock.large_combustion_engine.liquid_oxygen_boost_disallowed"));
                                } else {
                                    t.addLine(IKey.lang(
                                            "gregtech.multiblock.large_combustion_engine.oxygen_boost_disallowed"));
                                }
                            } else {
                                t.addLine(IKey.lang("gregtech.multiblock.invalid_structure"));
                            }
                        });
            }
            default -> throw new IllegalStateException("Invalid index received " + index);
        };
    }

    /**
     * @return an array of [fuel stored, fuel capacity]
     */
    private int[] getFuelAmount() {
        if (getInputFluidInventory() != null) {
            MultiblockFuelRecipeLogic recipeLogic = (MultiblockFuelRecipeLogic) recipeMapWorkable;
            if (recipeLogic.getInputFluidStack() != null) {
                FluidStack testStack = recipeLogic.getInputFluidStack().copy();
                testStack.amount = Integer.MAX_VALUE;
                return getTotalFluidAmount(testStack, getInputFluidInventory());
            }
        }
        return new int[2];
    }

    /**
     * @return an array of [lubricant stored, lubricant capacity]
     */
    private int[] getLubricantAmount() {
        if (getInputFluidInventory() != null) {
            return getTotalFluidAmount(Materials.Lubricant.getFluid(Integer.MAX_VALUE),
                    getInputFluidInventory());
        }
        return new int[2];
    }

    /**
     * @return an array of [oxygen stored, oxygen capacity]
     */
    private int[] getOxygenAmount() {
        if (getInputFluidInventory() != null) {
            if (isBoostAllowed()) {
                FluidStack oxygenStack = isExtreme ?
                        Materials.Oxygen.getFluid(FluidStorageKeys.LIQUID, Integer.MAX_VALUE) :
                        Materials.Oxygen.getFluid(Integer.MAX_VALUE);
                return getTotalFluidAmount(oxygenStack, getInputFluidInventory());
            }
        }
        return new int[2];
    }

    private static class LargeCombustionEngineWorkableHandler extends MultiblockFuelRecipeLogic {

        private boolean isOxygenBoosted = false;

        private final MetaTileEntityLargeCombustionEngine combustionEngine;
        private final boolean isExtreme;
        private final int tier;

        private static final FluidStack OXYGEN_STACK = Materials.Oxygen.getFluid(20);
        private static final FluidStack LIQUID_OXYGEN_STACK = Materials.Oxygen.getFluid(FluidStorageKeys.LIQUID, 80);
        private static final FluidStack LUBRICANT_STACK = Materials.Lubricant.getFluid(1);

        public LargeCombustionEngineWorkableHandler(RecipeMapMultiblockController tileEntity, boolean isExtreme) {
            super(tileEntity);
            this.combustionEngine = (MetaTileEntityLargeCombustionEngine) tileEntity;
            this.isExtreme = isExtreme;
            this.tier = isExtreme ? GTValues.IV : GTValues.EV;
        }

        @Override
        protected void updateRecipeProgress() {
            if (canRecipeProgress && drawEnergy(recipeEUt, true)) {
                drainLubricant();
                drainOxygen();
                drawEnergy(recipeEUt, false);

                // as recipe starts with progress on 1 this has to be > only not => to compensate for it
                if (++progressTime > maxProgressTime) {
                    completeRecipe();
                }
            }
        }

        protected void checkOxygen() {
            // check oxygen if present to boost production, and if the dynamo hatch supports it
            if (combustionEngine.isBoostAllowed()) {
                IMultipleTankHandler inputTank = combustionEngine.getInputFluidInventory();
                FluidStack boosterStack = isExtreme ? LIQUID_OXYGEN_STACK : OXYGEN_STACK;
                isOxygenBoosted = boosterStack.isFluidStackIdentical(inputTank.drain(boosterStack, false));
            }
        }

        protected void drainOxygen() {
            if (isOxygenBoosted && totalContinuousRunningTime % 20 == 0) {
                FluidStack boosterStack = isExtreme ? LIQUID_OXYGEN_STACK : OXYGEN_STACK;
                combustionEngine.getInputFluidInventory().drain(boosterStack, true);
            }
        }

        protected boolean checkLubricant() {
            // check lubricant and invalidate if it fails
            IMultipleTankHandler inputTank = combustionEngine.getInputFluidInventory();
            if (LUBRICANT_STACK.isFluidStackIdentical(inputTank.drain(LUBRICANT_STACK, false))) {
                return true;
            } else {
                invalidate();
                return false;
            }
        }

        protected void drainLubricant() {
            if (totalContinuousRunningTime == 1 || totalContinuousRunningTime % 72 == 0) {
                IMultipleTankHandler inputTank = combustionEngine.getInputFluidInventory();
                inputTank.drain(LUBRICANT_STACK, true);
            }
        }

        @Override
        protected boolean shouldSearchForRecipes() {
            checkOxygen();
            return super.shouldSearchForRecipes() && checkLubricant();
        }

        @Override
        protected boolean canProgressRecipe() {
            return super.canProgressRecipe() && checkLubricant();
        }

        @Override
        public long getMaxVoltage() {
            // this multiplies consumption through parallel
            if (isOxygenBoosted)
                return GTValues.V[tier] * 2;
            else
                return GTValues.V[tier];
        }

        @Override
        protected long boostProduction(long production) {
            // this multiplies production without increasing consumption
            if (isOxygenBoosted)
                if (!isExtreme)
                    // recipe gives 2A EV and we want 3A EV, for 150% efficiency
                    return production * 3 / 2;
                else
                    // recipe gives 2A IV and we want 4A IV, for 200% efficiency
                    return production * 2;
            return production;
        }

        @Override
        public void invalidate() {
            super.invalidate();
            isOxygenBoosted = false;
        }
    }
}
