package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.MultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.SteamMultiblockRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.IndicatorImageWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import java.util.List;

public abstract class RecipeMapSteamMultiblockController extends MultiblockWithDisplayBase {

    protected static final double CONVERSION_RATE = ConfigHolder.machines.multiblockSteamToEU;

    public final RecipeMap<?> recipeMap;
    protected SteamMultiblockRecipeLogic recipeMapWorkable;

    protected IItemHandlerModifiable inputInventory;
    protected IItemHandlerModifiable outputInventory;
    protected MultipleTankHandler steamFluidTank;

    public RecipeMapSteamMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                              double conversionRate) {
        super(metaTileEntityId);
        this.recipeMap = recipeMap;
        this.recipeMapWorkable = new SteamMultiblockRecipeLogic(this, recipeMap, steamFluidTank, conversionRate);
        resetTileAbilities();
    }

    public IItemHandlerModifiable getInputInventory() {
        return inputInventory;
    }

    public IItemHandlerModifiable getOutputInventory() {
        return outputInventory;
    }

    public MultipleTankHandler getSteamFluidTank() {
        return steamFluidTank;
    }

    /**
     * Performs extra checks for validity of given recipe before multiblock
     * will start it's processing.
     */
    public boolean checkRecipe(Recipe recipe, boolean consumeIfProcess) {
        return true;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
    }

    @Override
    protected void updateFormedValid() {
        recipeMapWorkable.update();
    }

    private void initializeAbilities() {
        this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.STEAM_IMPORT_ITEMS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.STEAM_EXPORT_ITEMS));
        this.steamFluidTank = new FluidTankList(true, getAbilities(MultiblockAbility.STEAM));
    }

    private void resetTileAbilities() {
        this.inputInventory = new GTItemStackHandler(this, 0);
        this.outputInventory = new GTItemStackHandler(this, 0);
        this.steamFluidTank = new FluidTankList(true);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(recipeMapWorkable.isWorkingEnabled(), recipeMapWorkable.isActive())
                .addCustom(tl -> {
                    // custom steam tank line
                    IFluidTank steamFluidTank = recipeMapWorkable.getSteamFluidTankCombined();
                    if (steamFluidTank != null && steamFluidTank.getCapacity() > 0) {
                        String stored = TextFormattingUtil.formatNumbers(steamFluidTank.getFluidAmount());
                        String capacity = TextFormattingUtil.formatNumbers(steamFluidTank.getCapacity());

                        ITextComponent steamInfo = TextComponentUtil.stringWithColor(
                                TextFormatting.BLUE,
                                stored + " / " + capacity + " L");

                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "gregtech.multiblock.steam.steam_stored",
                                steamInfo));
                    }
                })
                .addParallelsLine(recipeMapWorkable.getParallelLimit())
                .addWorkingStatusLine()
                .addProgressLine(recipeMapWorkable.getProgressPercent());
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed(), false)
                .addCustom(tl -> {
                    if (isStructureFormed() && recipeMapWorkable.isHasNotEnoughEnergy()) {
                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.YELLOW,
                                "gregtech.multiblock.steam.low_steam"));
                    }
                })
                .addMaintenanceProblemLines(getMaintenanceProblems());
    }

    @Override
    public TraceabilityPredicate autoAbilities() {
        return autoAbilities(true, true, true, true, true);
    }

    public TraceabilityPredicate autoAbilities(boolean checkSteam,
                                               boolean checkMaintainer,
                                               boolean checkItemIn,
                                               boolean checkItemOut,
                                               boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintainer, checkMuffler)
                .or(checkSteam ? abilities(MultiblockAbility.STEAM).setMinGlobalLimited(1).setPreviewCount(1) :
                        new TraceabilityPredicate());
        if (checkItemIn) {
            if (recipeMap.getMaxInputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1));
            }
        }
        if (checkItemOut) {
            if (recipeMap.getMaxOutputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1));
            }
        }
        return predicate;
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return !(trait instanceof SteamMultiblockRecipeLogic);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());
    }

    @Override
    public SoundEvent getSound() {
        return recipeMap.getSound();
    }

    @Override
    public boolean isActive() {
        return super.isActive() && recipeMapWorkable.isActive() && recipeMapWorkable.isWorkingEnabled();
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI
                .builder(GuiTextures.BACKGROUND_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks), 176, 208);
        builder.shouldColor(false);
        builder.image(4, 4, 168, 117, GuiTextures.DISPLAY_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks));
        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
        builder.widget(new AdvancedTextWidget(9, 20, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(162)
                .setClickHandler(this::handleDisplayClick));
        builder.widget(new IndicatorImageWidget(152, 101, 17, 17, getLogo())
                .setWarningStatus(getWarningLogo(), this::addWarningText)
                .setErrorStatus(getErrorLogo(), this::addErrorText));
        builder.bindPlayerInventory(entityPlayer.inventory,
                GuiTextures.SLOT_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks), 7, 125);
        return builder;
    }
}
