package gregtech.common.metatileentities.multi.electric;

import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.machines.RecipeMapFurnace;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.drawable.IKey;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.recipes.logic.OverclockingLogic.standardOC;

public class MetaTileEntityMultiSmelter extends RecipeMapMultiblockController {

    protected int heatingCoilLevel;
    protected int heatingCoilDiscount;

    public MetaTileEntityMultiSmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.FURNACE_RECIPES);
        this.recipeMapWorkable = new MultiSmelterWorkable(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMultiSmelter(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.setWorkingStatus(recipeMapWorkable.isWorkingEnabled(), recipeMapWorkable.isActive())
                .addEnergyUsageLine(getEnergyContainer())
                .addEnergyTierLine(GTUtility.getTierByVoltage(recipeMapWorkable.getMaxVoltage()))
                .addCustom((richText, syncer) -> {
                    if (!isStructureFormed()) return;

                    if (heatingCoilDiscount > 1) {
                        IKey coilDiscount = KeyUtil.number(TextFormatting.AQUA,
                                (long) (100.0 / heatingCoilDiscount), "%");

                        IKey base = KeyUtil.lang(TextFormatting.GRAY,
                                "gregtech.multiblock.multi_furnace.heating_coil_discount",
                                coilDiscount);

                        IKey hoverText = KeyUtil.lang(TextFormatting.GRAY,
                                "gregtech.multiblock.multi_furnace.heating_coil_discount_hover");

                        richText.add(KeyUtil.setHover(base, hoverText));
                    }

                    if (recipeMapWorkable.getParallelLimit() > 0) {
                        IKey parallels = KeyUtil.number(TextFormatting.DARK_PURPLE,
                                recipeMapWorkable.getParallelLimit());

                        IKey bodyText = KeyUtil.lang(TextFormatting.GRAY,
                                "gregtech.multiblock.parallel",
                                parallels);

                        IKey hoverText = KeyUtil.lang(TextFormatting.GRAY,
                                "gregtech.multiblock.multi_furnace.parallel_hover");

                        richText.add(KeyUtil.setHover(bodyText, hoverText));
                    }
                })
                .addWorkingStatusLine()
                .addProgressLine(recipeMapWorkable.getProgress(), recipeMapWorkable.getMaxProgress())
                .addRecipeOutputLine(recipeMapWorkable);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        IHeatingCoilBlockStats coilType = context.getOrDefault("CoilType", CoilType.CUPRONICKEL);
        this.heatingCoilLevel = coilType.getLevel();
        this.heatingCoilDiscount = coilType.getEnergyDiscount();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.heatingCoilLevel = 0;
        this.heatingCoilDiscount = 0;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "XXX")
                .aisle("XXX", "C#C", "XMX")
                .aisle("XSX", "CCC", "XXX")
                .where('S', selfPredicate())
                .where('X',
                        states(getCasingState()).setMinGlobalLimited(9)
                                .or(autoAbilities(true, true, true, true, true, true, false)))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where('C', heatingCoils())
                .where('#', air())
                .build();
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.INVAR_HEATPROOF);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.HEAT_PROOF_CASING;
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_ELECTRICAL;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.MULTI_FURNACE_OVERLAY;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    /**
     * @param parallel the amount of parallel recipes
     * @param discount the energy discount
     * @return the un-overclocked EUt for an amount of parallel recipes
     */
    public static int getEUtForParallel(int parallel, int discount) {
        return RecipeMapFurnace.RECIPE_EUT * Math.max(1, (parallel / 8) / discount);
    }

    /**
     * @param heatingCoilLevel the level to get the parallel for
     * @return the max parallel for the heating coil level
     */
    public static int getMaxParallel(int heatingCoilLevel) {
        return 32 * heatingCoilLevel;
    }

    /**
     * @param parallel      the amount of parallel recipes
     * @param parallelLimit the maximum limit on parallel recipes
     * @return the un-overclocked duration for an amount of parallel recipes
     */
    public static int getDurationForParallel(int parallel, int parallelLimit) {
        return (int) Math.max(1.0, RecipeMapFurnace.RECIPE_DURATION * 2 * parallel / Math.max(1, parallelLimit * 1.0));
    }

    protected class MultiSmelterWorkable extends MultiblockRecipeLogic {

        public MultiSmelterWorkable(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @NotNull
        @Override
        public ParallelLogicType getParallelLogicType() {
            return ParallelLogicType.APPEND_ITEMS;
        }

        @Override
        protected void runOverclockingLogic(@NotNull OCParams ocParams, @NotNull OCResult ocResult,
                                            @NotNull RecipePropertyStorage propertyStorage, long maxVoltage) {
            standardOC(ocParams, ocResult, maxVoltage, getOverclockingDurationFactor(),
                    getOverclockingVoltageFactor());
        }

        @Override
        public void applyParallelBonus(@NotNull RecipeBuilder<?> builder) {
            builder.EUt(getEUtForParallel(builder.getParallel(), heatingCoilDiscount))
                    .duration(getDurationForParallel(builder.getParallel(), getParallelLimit()));
        }

        @Override
        public int getParallelLimit() {
            return getMaxParallel(heatingCoilLevel);
        }
    }
}
