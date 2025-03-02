package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.IMachineHatchMultiblock;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.DummyCleanroom;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.cleanroommc.modularui.api.drawable.IKey;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.recipes.logic.OverclockingLogic.subTickNonParallelOC;

public class MetaTileEntityProcessingArray extends RecipeMapMultiblockController implements IMachineHatchMultiblock {

    private final int tier;
    private boolean machineChanged;

    public MetaTileEntityProcessingArray(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, null);
        this.tier = tier;
        this.recipeMapWorkable = new ProcessingArrayWorkable(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityProcessingArray(metaTileEntityId, tier);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        ((ProcessingArrayWorkable) this.recipeMapWorkable).findMachineStack();
    }

    @Override
    public int getMachineLimit() {
        return tier == 0 ? 16 : 64;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('L', states(getCasingState()))
                .where('S', selfPredicate())
                .where('X', states(getCasingState())
                        .setMinGlobalLimited(tier == 0 ? 11 : 4)
                        .or(autoAbilities(false, true, true, true, true, true, true))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                        .or(abilities(MultiblockAbility.MACHINE_HATCH).setExactLimit(1)))
                .where('#', air())
                .build();
    }

    public IBlockState getCasingState() {
        return tier == 0 ? MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST) :
                MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.HSSE_STURDY);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return tier == 0 ? Textures.ROBUST_TUNGSTENSTEEL_CASING : Textures.STURDY_HSSE_CASING;
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        ProcessingArrayWorkable logic = (ProcessingArrayWorkable) recipeMapWorkable;

        builder.setWorkingStatus(recipeMapWorkable.isWorkingEnabled(), recipeMapWorkable.isActive())
                .addEnergyUsageLine(this.getEnergyContainer())
                .addEnergyTierLine(GTUtility.getTierByVoltage(recipeMapWorkable.getMaxVoltage()))
                .addCustom((manager, syncer) -> {
                    if (!isStructureFormed()) return;

                    // Machine mode text
                    // Shared text components for both states
                    IKey maxMachinesText = KeyUtil.number(TextFormatting.DARK_PURPLE,
                            syncer.syncInt(getMachineLimit()));
                    maxMachinesText = KeyUtil.lang(TextFormatting.GRAY,
                            "gregtech.machine.machine_hatch.machines_max", maxMachinesText);

                    if (syncer.syncBoolean(logic.activeRecipeMap == null)) {
                        // No machines in hatch
                        IKey noneText = KeyUtil.lang(TextFormatting.YELLOW,
                                "gregtech.machine.machine_hatch.machines_none");
                        IKey bodyText = KeyUtil.lang(TextFormatting.GRAY,
                                "gregtech.machine.machine_hatch.machines", noneText);
                        IKey hoverText1 = KeyUtil.lang(TextFormatting.GRAY,
                                "gregtech.machine.machine_hatch.machines_none_hover");
                        manager.add(KeyUtil.setHover(bodyText, hoverText1, maxMachinesText));
                    } else {
                        // Some amount of machines in hatch
                        String key = syncer.syncString(logic.getMachineStack().getTranslationKey());
                        IKey mapText = KeyUtil.lang(TextFormatting.DARK_PURPLE,
                                key + ".name");
                        mapText = KeyUtil.string(
                                TextFormatting.DARK_PURPLE,
                                "%sx %s",
                                syncer.syncInt(logic.getParallelLimit()), mapText);
                        IKey bodyText = KeyUtil.lang(TextFormatting.GRAY,
                                "gregtech.machine.machine_hatch.machines", mapText);
                        int tier = syncer.syncInt(logic.machineTier);
                        IKey voltageName = KeyUtil.string(GTValues.VNF[tier]);
                        int amps = syncer.syncInt(logic.getMachineStack().getCount());
                        String energyFormatted = TextFormattingUtil
                                .formatNumbers(GTValues.V[tier] * amps);
                        IKey hoverText = KeyUtil.lang(
                                TextFormatting.GRAY,
                                "gregtech.machine.machine_hatch.machines_max_eut",
                                energyFormatted, amps, voltageName);
                        manager.add(KeyUtil.setHover(bodyText, hoverText, maxMachinesText));
                    }

                    // Hatch locked status
                    if (syncer.syncBoolean(isActive())) {
                        manager.add(KeyUtil.lang(TextFormatting.DARK_RED,
                                "gregtech.machine.machine_hatch.locked"));
                    }
                })
                .addParallelsLine(recipeMapWorkable.getParallelLimit())
                .addWorkingStatusLine()
                .addProgressLine(recipeMapWorkable.getProgress(), recipeMapWorkable.getMaxProgress())
                .addRecipeOutputLine(recipeMapWorkable);
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return tier == 0 ? Textures.PROCESSING_ARRAY_OVERLAY : Textures.ADVANCED_PROCESSING_ARRAY_OVERLAY;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public void notifyMachineChanged() {
        machineChanged = true;
    }

    @Override
    public String[] getBlacklist() {
        return ConfigHolder.machines.processingArrayBlacklist;
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_MECHANICAL;
    }

    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.ARC;
    }

    @Override
    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn, boolean checkMaintenance, boolean checkItemIn,
                                               boolean checkItemOut, boolean checkFluidIn, boolean checkFluidOut,
                                               boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintenance, checkMuffler)
                .or(checkEnergyIn ? abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                        .setMaxGlobalLimited(4).setPreviewCount(1) : new TraceabilityPredicate());

        predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1));

        return predicate;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.parallel", getMachineLimit()));
    }

    @Override
    public int getItemOutputLimit() {
        ItemStack machineStack = ((ProcessingArrayWorkable) this.recipeMapWorkable).getMachineStack();
        MetaTileEntity mte = GTUtility.getMetaTileEntity(machineStack);
        return mte == null ? 0 : mte.getItemOutputLimit();
    }

    @Override
    public void setCleanroom(@NotNull ICleanroomProvider provider) {
        super.setCleanroom(provider);

        // Sync Cleanroom Change to Internal Workable MTE
        ((ProcessingArrayWorkable) this.recipeMapWorkable).updateCleanroom();
    }

    @Override
    public void unsetCleanroom() {
        super.unsetCleanroom();

        ((ProcessingArrayWorkable) this.recipeMapWorkable).updateCleanroom();
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    protected class ProcessingArrayWorkable extends MultiblockRecipeLogic {

        private static final ICleanroomProvider DUMMY_CLEANROOM = DummyCleanroom.createForAllTypes();

        ItemStack currentMachineStack = ItemStack.EMPTY;
        MetaTileEntity mte = null;
        // The Voltage Tier of the machines the PA is operating upon, from GTValues.V
        private int machineTier;
        // The maximum Voltage of the machines the PA is operating upon
        private long machineVoltage;
        // The Recipe Map of the machines the PA is operating upon
        private RecipeMap<?> activeRecipeMap;

        public ProcessingArrayWorkable(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public void invalidate() {
            super.invalidate();

            // invalidate mte's cleanroom reference
            if (mte != null && mte instanceof ICleanroomReceiver cleanroomMTE) {
                cleanroomMTE.unsetCleanroom();
            }

            // Reset locally cached variables upon invalidation
            currentMachineStack = ItemStack.EMPTY;
            mte = null;
            machineChanged = true;
            machineTier = 0;
            machineVoltage = 0L;
            activeRecipeMap = null;
        }

        /**
         * Checks if a provided Recipe Map is valid to be used in the processing array
         * Will filter out anything in the config blacklist, and also any non-single block machines
         *
         * @param recipeMap The recipeMap to check
         * @return {@code true} if the provided recipeMap is valid for use
         */
        @Override
        public boolean isRecipeMapValid(@NotNull RecipeMap<?> recipeMap) {
            if (ArrayUtils.contains(((IMachineHatchMultiblock) metaTileEntity).getBlacklist(),
                    recipeMap.getUnlocalizedName())) {
                return false;
            }

            return GTUtility.isMachineValidForMachineHatch(currentMachineStack,
                    ((IMachineHatchMultiblock) metaTileEntity).getBlacklist());
        }

        @Override
        protected boolean shouldSearchForRecipes() {
            return canWorkWithMachines() && super.shouldSearchForRecipes();
        }

        public boolean canWorkWithMachines() {
            if (machineChanged) {
                findMachineStack();
                machineChanged = false;
                previousRecipe = null;
                if (isDistinct()) {
                    invalidatedInputList.clear();
                } else {
                    invalidInputsForRecipes = false;
                }
            }
            return (!currentMachineStack.isEmpty() && this.activeRecipeMap != null);
        }

        @Nullable
        @Override
        public RecipeMap<?> getRecipeMap() {
            return activeRecipeMap;
        }

        public void findMachineStack() {
            RecipeMapMultiblockController controller = (RecipeMapMultiblockController) this.metaTileEntity;

            // The Processing Array is limited to 1 Machine Interface per multiblock, and only has 1 slot
            ItemStack machine = controller.getAbilities(MultiblockAbility.MACHINE_HATCH).get(0).getStackInSlot(0);

            mte = GTUtility.getMetaTileEntity(machine);

            if (mte == null) {
                this.activeRecipeMap = null;
            } else {
                this.activeRecipeMap = mte.getRecipeMap();
                // Set the world for MTEs, as some need it for checking their recipes
                MetaTileEntityHolder holder = new MetaTileEntityHolder();
                mte = holder.setMetaTileEntity(mte);
                holder.setWorld(this.metaTileEntity.getWorld());

                updateCleanroom();
            }

            // Find the voltage tier of the machine.
            this.machineTier = mte instanceof ITieredMetaTileEntity ? ((ITieredMetaTileEntity) mte).getTier() : 0;

            this.machineVoltage = GTValues.V[this.machineTier];

            this.currentMachineStack = machine;
        }

        private void updateCleanroom() {
            // Set the cleanroom of the MTEs to the PA's cleanroom reference
            if (mte instanceof ICleanroomReceiver receiver) {
                if (ConfigHolder.machines.cleanMultiblocks) {
                    receiver.setCleanroom(DUMMY_CLEANROOM);
                } else {
                    ICleanroomProvider provider = ((RecipeMapMultiblockController) metaTileEntity).getCleanroom();
                    if (provider == null) {
                        receiver.unsetCleanroom();
                    } else {
                        receiver.setCleanroom(provider);
                    }
                }
            }
        }

        @Override
        public boolean checkRecipe(@NotNull Recipe recipe) {
            if (mte == null) return false;

            AbstractRecipeLogic arl = mte.getRecipeLogic();
            if (arl == null) return false;

            return arl.checkRecipe(recipe) && super.checkRecipe(recipe);
        }

        @Override
        protected int getOverclockForTier(long voltage) {
            return super.getOverclockForTier(Math.min(machineVoltage, getMaximumOverclockVoltage()));
        }

        @Override
        public int getParallelLimit() {
            return (currentMachineStack == null || currentMachineStack.isEmpty()) ? getMachineLimit() :
                    Math.min(currentMachineStack.getCount(), getMachineLimit());
        }

        @Override
        protected Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs) {
            return super.findRecipe(Math.min(super.getMaxVoltage(), this.machineVoltage), inputs, fluidInputs);
        }

        @Override
        public long getMaxVoltage() {
            // Allow the PA to use as much power as provided, since tier is gated by the machine anyway.
            // UI text uses the machine stack's tier instead of the getMaxVoltage() tier as well.
            return super.getMaximumOverclockVoltage();
        }

        @Override
        protected int getNumberOfOCs(long recipeEUt) {
            if (!isAllowOverclocking()) return 0;

            int recipeTier = Math.max(0,
                    GTUtility.getTierByVoltage(recipeEUt / Math.max(1, this.parallelRecipesPerformed)));
            int maximumTier = Math.min(this.machineTier, GTUtility.getTierByVoltage(getMaxVoltage()));

            // The maximum number of overclocks is determined by the difference between the tier the recipe is running
            // at,
            // and the maximum tier that the machine can overclock to.
            int numberOfOCs = maximumTier - recipeTier;
            if (recipeTier == ULV) numberOfOCs--; // no ULV overclocking

            return numberOfOCs;
        }

        @Override
        protected void runOverclockingLogic(@NotNull OCParams ocParams, @NotNull OCResult ocResult,
                                            @NotNull RecipePropertyStorage propertyStorage, long maxVoltage) {
            subTickNonParallelOC(ocParams, ocResult, maxVoltage, getOverclockingDurationFactor(),
                    getOverclockingVoltageFactor());
        }

        private ItemStack getMachineStack() {
            return currentMachineStack;
        }
    }
}
