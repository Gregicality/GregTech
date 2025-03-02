package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidDrillLogic;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIFactory;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MetaTileEntityFluidDrill extends MultiblockWithDisplayBase
                                      implements ITieredMetaTileEntity, IWorkable, ProgressBarMultiblock {

    private final FluidDrillLogic minerLogic;
    private final int tier;

    protected IMultipleTankHandler inputFluidInventory;
    protected IMultipleTankHandler outputFluidInventory;
    protected IEnergyContainer energyContainer;

    public MetaTileEntityFluidDrill(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId);
        this.minerLogic = new FluidDrillLogic(this);
        this.tier = tier;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFluidDrill(metaTileEntityId, tier);
    }

    protected void initializeAbilities() {
        this.inputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
    }

    private void resetTileAbilities() {
        this.inputFluidInventory = new FluidTankList(true);
        this.outputFluidInventory = new FluidTankList(true);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
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
        this.minerLogic.performDrilling();
        if (!getWorld().isRemote && this.minerLogic.wasActiveAndNeedsUpdate()) {
            this.minerLogic.setWasActiveAndNeedsUpdate(false);
            this.minerLogic.setActive(false);
        }
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                .aisle("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                .aisle("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(3)
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3))
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1)))
                .where('C', states(getCasingState()))
                .where('F', getFramePredicate())
                .where('#', any())
                .build();
    }

    private IBlockState getCasingState() {
        if (tier == GTValues.MV)
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
        if (tier == GTValues.HV)
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE);
        if (tier == GTValues.EV)
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @NotNull
    private TraceabilityPredicate getFramePredicate() {
        if (tier == GTValues.MV)
            return frames(Materials.Steel);
        if (tier == GTValues.HV)
            return frames(Materials.Titanium);
        if (tier == GTValues.EV)
            return frames(Materials.TungstenSteel);
        return frames(Materials.Steel);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (tier == GTValues.MV)
            return Textures.SOLID_STEEL_CASING;
        if (tier == GTValues.HV)
            return Textures.STABLE_TITANIUM_CASING;
        if (tier == GTValues.EV)
            return Textures.ROBUST_TUNGSTENSTEEL_CASING;
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.setWorkingStatus(minerLogic.isWorkingEnabled(), minerLogic.isActive())
                .setWorkingStatusKeys(
                        "gregtech.multiblock.idling",
                        "gregtech.multiblock.work_paused",
                        "gregtech.multiblock.miner.drilling")
                .addEnergyUsageLine(energyContainer)
                .addCustom((list, syncer) -> {
                    if (isStructureFormed()) {
                        if (syncer.syncBoolean(minerLogic.getDrilledFluid() != null)) {
                            // Fluid name
                            Fluid drilledFluid = minerLogic.getDrilledFluid();
                            IKey fluidInfo = KeyUtil.fluid(drilledFluid).style(TextFormatting.GREEN);

                            // todo should this check if fluid info is empty?
                            list.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.fluid_rig.drilled_fluid",
                                    fluidInfo));

                            IKey amountInfo = KeyUtil.lang(TextFormatting.BLUE,
                                    TextFormattingUtil.formatNumbers(
                                            minerLogic.getFluidToProduce() * 20L / FluidDrillLogic.MAX_PROGRESS) +
                                            " L/s");
                            list.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.fluid_rig.fluid_amount",
                                    amountInfo));
                        } else {
                            IKey noFluid = KeyUtil.lang(TextFormatting.RED,
                                    "gregtech.multiblock.fluid_rig.no_fluid_in_area");
                            list.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.fluid_rig.drilled_fluid",
                                    noFluid));
                        }
                    }
                })
                .addProgressLine(minerLogic.getProgressTime(), FluidDrillLogic.MAX_PROGRESS)
                .addWorkingStatusLine();
    }

    @Override
    protected void configureWarningText(MultiblockUIBuilder builder) {
        builder.addLowPowerLine(isStructureFormed() && !drainEnergy(true))
                .addCustom((list, syncer) -> {
                    if (isStructureFormed() && syncer.syncBoolean(minerLogic.isInventoryFull())) {
                        list.add(KeyUtil.lang(TextFormatting.YELLOW, "gregtech.machine.miner.invfull"));
                    }
                });
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.fluid_drilling_rig.description"));
        tooltip.add(I18n.format("gregtech.machine.fluid_drilling_rig.depletion",
                TextFormattingUtil.formatNumbers(100.0 / getDepletionChance())));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_tier_range", GTValues.VNF[this.tier],
                GTValues.VNF[this.tier + 1]));
        tooltip.add(I18n.format("gregtech.machine.fluid_drilling_rig.production", getRigMultiplier(),
                TextFormattingUtil.formatNumbers(getRigMultiplier() * 1.5)));
        if (tier > GTValues.MV) {
            tooltip.add(I18n.format("gregtech.machine.fluid_drilling_rig.shows_depletion"));
        }
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    public int getRigMultiplier() {
        if (this.tier == GTValues.MV)
            return 1;
        if (this.tier == GTValues.HV)
            return 16;
        if (this.tier == GTValues.EV)
            return 64;
        return 1;
    }

    public int getDepletionChance() {
        if (this.tier == GTValues.MV)
            return 1;
        if (this.tier == GTValues.HV)
            return 2;
        if (this.tier == GTValues.EV)
            return 8;
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.FLUID_RIG_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                this.minerLogic.isActive(), this.minerLogic.isWorkingEnabled());
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.minerLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.minerLogic.setWorkingEnabled(isActivationAllowed);
    }

    public boolean fillTanks(FluidStack stack, boolean simulate) {
        return GTTransferUtils.addFluidsToFluidHandler(outputFluidInventory, simulate,
                Collections.singletonList(stack));
    }

    public int getEnergyTier() {
        if (energyContainer == null) return this.tier;
        return Math.min(this.tier + 1,
                Math.max(this.tier, GTUtility.getFloorTierByVoltage(energyContainer.getInputVoltage())));
    }

    public long getEnergyInputPerSecond() {
        return energyContainer.getInputPerSec();
    }

    public boolean drainEnergy(boolean simulate) {
        long energyToDrain = GTValues.VA[getEnergyTier()];
        long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.changeEnergy(-energyToDrain);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        return this.minerLogic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.minerLogic.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        this.minerLogic.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.minerLogic.receiveInitialSyncData(buf);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        this.minerLogic.receiveCustomData(dataId, buf);
    }

    @Override
    public int getProgress() {
        return minerLogic.getProgressTime();
    }

    @Override
    public int getMaxProgress() {
        return FluidDrillLogic.MAX_PROGRESS;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public boolean shouldShowVoidingModeButton() {
        return false;
    }

    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    public int getProgressBarCount() {
        // only show for T2/3 fluid rigs
        return tier > GTValues.MV ? 1 : 0;
    }

    @Override
    public @NotNull ProgressWidget createProgressBar(PanelSyncManager panelSyncManager, int index) {
        IntSyncValue operationsValue = new IntSyncValue(() -> BedrockFluidVeinHandler.getOperationsRemaining(getWorld(),
                minerLogic.getChunkX(), minerLogic.getChunkZ()));
        panelSyncManager.syncValue("operations_remaining", operationsValue);

        return new ProgressWidget()
                .progress(() -> operationsValue.getIntValue() * 1.0 / BedrockFluidVeinHandler.MAXIMUM_VEIN_OPERATIONS)
                .texture(GTGuiTextures.PROGRESS_BAR_FLUID_RIG_DEPLETION, MultiblockUIFactory.Bars.FULL_WIDTH)
                .tooltipAutoUpdate(true)
                .tooltipBuilder(t -> {
                    if (isStructureFormed()) {
                        if (operationsValue.getIntValue() == 0) {
                            t.addLine(IKey.lang("gregtech.multiblock.fluid_rig.vein_depleted"));
                        } else {
                            t.addLine(KeyUtil.string(() -> {
                                int percent = (int) Math.round(100.0 * operationsValue.getIntValue() /
                                        BedrockFluidVeinHandler.MAXIMUM_VEIN_OPERATIONS);
                                if (percent > 40) {
                                    return TextFormatting.GREEN + IKey
                                            .lang("gregtech.multiblock.fluid_rig.vein_depletion.high", percent).get();
                                } else if (percent > 10) {
                                    return TextFormatting.YELLOW + IKey
                                            .lang("gregtech.multiblock.fluid_rig.vein_depletion.medium", percent).get();
                                } else {
                                    return TextFormatting.RED + IKey
                                            .lang("gregtech.multiblock.fluid_rig.vein_depletion.low", percent).get();
                                }
                            }));
                        }
                    } else {
                        t.addLine(IKey.lang("gregtech.multiblock.invalid_structure"));
                    }
                });
    }
}
