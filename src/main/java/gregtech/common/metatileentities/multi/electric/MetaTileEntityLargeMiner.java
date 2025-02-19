package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.miner.MultiblockMinerLogic;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIFactory;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static gregtech.api.unification.material.Materials.DrillingFluid;

public class MetaTileEntityLargeMiner extends MultiblockWithDisplayBase
                                      implements IMiner, IControllable, IDataInfoProvider {

    private static final int CHUNK_LENGTH = 16;

    private final Material material;
    private final int tier;

    private IEnergyContainer energyContainer;
    protected IMultipleTankHandler inputFluidInventory;
    protected IItemHandlerModifiable outputInventory;

    private boolean silkTouch = false;
    private boolean chunkMode = false;

    private boolean isInventoryFull = false;

    private final int drillingFluidConsumePerTick;

    private final MultiblockMinerLogic minerLogic;

    public MetaTileEntityLargeMiner(ResourceLocation metaTileEntityId, int tier, int speed, int maximumChunkDiameter,
                                    int fortune, Material material, int drillingFluidConsumePerTick) {
        super(metaTileEntityId);
        this.material = material;
        this.tier = tier;
        this.drillingFluidConsumePerTick = drillingFluidConsumePerTick;
        this.minerLogic = new MultiblockMinerLogic(this, fortune, speed, maximumChunkDiameter * CHUNK_LENGTH / 2,
                RecipeMaps.MACERATOR_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeMiner(metaTileEntityId, this.tier, this.minerLogic.getSpeed(),
                this.minerLogic.getMaximumRadius() * 2 / CHUNK_LENGTH, this.minerLogic.getFortune(), getMaterial(),
                getDrillingFluidConsumePerTick());
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        if (this.minerLogic.isActive())
            this.minerLogic.setActive(false);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    private void initializeAbilities() {
        this.inputFluidInventory = new FluidTankList(false, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.minerLogic.setVoltageTier(GTUtility.getTierByVoltage(this.energyContainer.getInputVoltage()));
        this.minerLogic.setOverclockAmount(
                Math.max(1, GTUtility.getTierByVoltage(this.energyContainer.getInputVoltage()) - this.tier));
        this.minerLogic.initPos(getPos(), this.minerLogic.getCurrentRadius());
    }

    private void resetTileAbilities() {
        this.inputFluidInventory = new FluidTankList(true);
        this.outputInventory = new GTItemStackHandler(this, 0);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    public int getEnergyTier() {
        if (energyContainer == null) return this.tier;
        return Math.min(this.tier + 1,
                Math.max(this.tier, GTUtility.getFloorTierByVoltage(energyContainer.getInputVoltage())));
    }

    @Override
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
    public boolean drainFluid(boolean simulate) {
        FluidStack drillingFluid = DrillingFluid
                .getFluid(this.drillingFluidConsumePerTick * this.minerLogic.getOverclockAmount());
        FluidStack fluidStack = inputFluidInventory.getTankAt(0).getFluid();
        if (fluidStack != null && fluidStack.isFluidEqual(DrillingFluid.getFluid(1)) &&
                fluidStack.amount >= drillingFluid.amount) {
            if (!simulate)
                inputFluidInventory.drain(drillingFluid, true);
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                this.minerLogic.isWorking(), this.isWorkingEnabled());
        minerLogic.renderPipe(renderState, translation, pipeline);
    }

    @Override
    protected void updateFormedValid() {
        this.minerLogic.performMining();
        if (!getWorld().isRemote && this.minerLogic.wasActiveAndNeedsUpdate()) {
            this.minerLogic.setWasActiveAndNeedsUpdate(false);
            this.minerLogic.setActive(false);
        }
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                .aisle("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                .aisle("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                .where('S', selfPredicate())
                .where('X', states(getCasingState())
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setExactLimit(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3)
                                .setPreviewCount(1)))
                .where('C', states(getCasingState()))
                .where('F', getFramePredicate())
                .where('#', any())
                .build();
    }

    @Override
    public String[] getDescription() {
        return new String[] { I18n.format("gregtech.machine.miner.multi.description") };
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        int workingAreaChunks = this.minerLogic.getCurrentRadius() * 2 / CHUNK_LENGTH;
        tooltip.add(I18n.format("gregtech.machine.miner.multi.modes"));
        tooltip.add(I18n.format("gregtech.machine.miner.multi.production"));
        tooltip.add(I18n.format("gregtech.machine.miner.fluid_usage", getDrillingFluidConsumePerTick(),
                DrillingFluid.getLocalizedName()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.working_area_chunks_max", workingAreaChunks,
                workingAreaChunks));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_tier_range", GTValues.VNF[this.tier],
                GTValues.VNF[this.tier + 1]));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.toggle_mode_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        if (getSound() != null) {
            tooltip.add(I18n.format("gregtech.tool_action.hammer"));
        }
        tooltip.add(I18n.format("gregtech.tool_action.crowbar"));
    }

    @Override
    protected MultiblockUIFactory createUIFactory() {
        return super.createUIFactory()
                .createFlexButton((posGuiData, panelSyncManager) -> {
                    IntSyncValue buttonSync = new IntSyncValue(this::getCurrentMode, this::setCurrentMode);

                    return new CycleButtonWidget() {

                        @Override
                        public @NotNull Result onMousePressed(int mouseButton) {
                            if (minerLogic.isWorking()) {
                                Interactable.playButtonClickSound();
                                return Result.IGNORE;
                            } else {
                                return super.onMousePressed(mouseButton);
                            }
                        }
                    }
                            .stateCount(4)
                            .value(buttonSync)
                            .stateBackground(GTGuiTextures.BUTTON_MINER_MODES)
                            .addTooltip(0, IKey.lang("gregtech.multiblock.miner.neither_mode"))
                            .addTooltip(1, IKey.lang("gregtech.multiblock.miner.chunk_mode"))
                            .addTooltip(2, IKey.lang("gregtech.multiblock.miner.silk_touch_mode"))
                            .addTooltip(3, IKey.lang("gregtech.multiblock.miner.both_modes"));
                });
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.setWorkingStatus(minerLogic.isWorkingEnabled(), minerLogic.isActive())
                .addEnergyUsageLine(energyContainer)
                .addCustom(list -> {
                    if (isStructureFormed()) {
                        int workingAreaChunks = this.minerLogic.getCurrentRadius() * 2 / CHUNK_LENGTH;
                        int workingArea = getWorkingArea(minerLogic.getCurrentRadius());

                        list.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.machine.miner.mining_at"));
                        list.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.machine.miner.mining_pos",
                                minerLogic.getMineX().get(),
                                minerLogic.getMineY().get(),
                                minerLogic.getMineZ().get()));

                        if (minerLogic.isChunkMode()) {
                            list.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.machine.miner.working_area_chunks",
                                    workingAreaChunks,
                                    workingAreaChunks));
                        } else {
                            list.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.machine.miner.working_area",
                                    workingArea, workingArea));
                        }

                        if (minerLogic.isDone()) {
                            list.add(KeyUtil.lang(TextFormatting.GREEN, "gregtech.machine.miner.done"));
                        } else if (minerLogic.isWorking()) {
                            list.add(KeyUtil.lang(TextFormatting.GOLD, "gregtech.machine.miner.working"));
                        } else if (!isWorkingEnabled()) {
                            list.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.work_paused"));
                        }
                    }
                });
    }

    @Override
    protected void configureErrorText(MultiblockUIBuilder builder) {
        builder.addCustom(list -> {
            if (isStructureFormed() && !drainFluid(false)) {
                list.add(KeyUtil.lang(TextFormatting.RED, "gregtech.machine.miner.multi.needsfluid"));
            }
        });
    }

    @Override
    protected void configureWarningText(MultiblockUIBuilder builder) {
        builder.addLowPowerLine(!drainEnergy(true));
        builder.addCustom(list -> {
            if (isStructureFormed() && isInventoryFull) {
                list.add(KeyUtil.lang(TextFormatting.YELLOW, "gregtech.machine.miner.invfull"));
            }
        });
    }

    public IBlockState getCasingState() {
        if (this.material.equals(Materials.Titanium))
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE);
        if (this.material.equals(Materials.TungstenSteel))
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @NotNull
    private TraceabilityPredicate getFramePredicate() {
        if (this.material.equals(Materials.Titanium))
            return frames(Materials.Titanium);
        if (this.material.equals(Materials.TungstenSteel))
            return frames(Materials.TungstenSteel);
        return frames(Materials.Steel);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (this.material.equals(Materials.Titanium))
            return Textures.STABLE_TITANIUM_CASING;
        if (this.material.equals(Materials.TungstenSteel))
            return Textures.ROBUST_TUNGSTENSTEEL_CASING;
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("chunkMode", new NBTTagInt(chunkMode ? 1 : 0));
        data.setTag("silkTouch", new NBTTagInt(silkTouch ? 1 : 0));
        return this.minerLogic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        chunkMode = data.getInteger("chunkMode") != 0;
        silkTouch = data.getInteger("silkTouch") != 0;
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

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        if (this.tier == 5)
            return Textures.LARGE_MINER_OVERLAY_ADVANCED;
        if (this.tier == 6)
            return Textures.LARGE_MINER_OVERLAY_ADVANCED_2;
        return Textures.LARGE_MINER_OVERLAY_BASIC;
    }

    public long getMaxVoltage() {
        return GTValues.V[GTUtility.getTierByVoltage(energyContainer.getInputVoltage())];
    }

    // used for UI
    private int getCurrentMode() {
        // 0 -> not chunk mode, not silk touch mode
        if (!minerLogic.isChunkMode() && !minerLogic.isSilkTouchMode()) {
            return 0;
        }
        // 1 -> is chunk mode, not silk touch mode
        if (minerLogic.isChunkMode() && !minerLogic.isSilkTouchMode()) {
            return 1;
        }
        // 2 -> not chunk mode, is silk touch mode
        if (!minerLogic.isChunkMode() && minerLogic.isSilkTouchMode()) {
            return 2;
        }
        // 3 -> is chunk mode, is silk touch mode
        return 3;
    }

    // used for UI
    private void setCurrentMode(int mode) {
        switch (mode) {
            case 0 -> {
                minerLogic.setChunkMode(false);
                minerLogic.setSilkTouchMode(false);
            }
            case 1 -> {
                minerLogic.setChunkMode(true);
                minerLogic.setSilkTouchMode(false);
            }
            case 2 -> {
                minerLogic.setChunkMode(false);
                minerLogic.setSilkTouchMode(true);
            }
            default -> {
                minerLogic.setChunkMode(true);
                minerLogic.setSilkTouchMode(true);
            }
        }
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote || !this.isStructureFormed())
            return true;

        if (!this.isActive()) {
            int currentRadius = this.minerLogic.getCurrentRadius();
            if (this.minerLogic.isChunkMode()) {
                if (currentRadius - CHUNK_LENGTH <= 0) {
                    this.minerLogic.setCurrentRadius(this.minerLogic.getMaximumRadius());
                } else {
                    this.minerLogic.setCurrentRadius(currentRadius - CHUNK_LENGTH);
                }
                int workingAreaChunks = this.minerLogic.getCurrentRadius() * 2 / CHUNK_LENGTH;
                playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.miner.working_area_chunks",
                        workingAreaChunks, workingAreaChunks));
            } else {
                if (currentRadius - CHUNK_LENGTH / 2 <= 0) {
                    this.minerLogic.setCurrentRadius(this.minerLogic.getMaximumRadius());
                } else {
                    this.minerLogic.setCurrentRadius(currentRadius - CHUNK_LENGTH / 2);
                }
                int workingArea = getWorkingArea(minerLogic.getCurrentRadius());
                playerIn.sendMessage(new TextComponentTranslation("gregtech.universal.tooltip.working_area",
                        workingArea, workingArea));
            }
            this.minerLogic.resetArea();
        } else {
            playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.miner.errorradius"));
        }
        return true;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public boolean isInventoryFull() {
        return this.isInventoryFull;
    }

    @Override
    public void setInventoryFull(boolean isFull) {
        this.isInventoryFull = isFull;
    }

    public Material getMaterial() {
        return material;
    }

    public int getTier() {
        return this.tier;
    }

    public int getDrillingFluidConsumePerTick() {
        return this.drillingFluidConsumePerTick;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.minerLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.minerLogic.setWorkingEnabled(isActivationAllowed);
    }

    public int getMaxChunkRadius() {
        return this.minerLogic.getMaximumRadius() / CHUNK_LENGTH;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public IItemHandlerModifiable getExportItems() {
        return this.outputInventory;
    }

    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.MINER;
    }

    @Override
    public boolean isActive() {
        return minerLogic.isActive() && isWorkingEnabled();
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        int workingArea = getWorkingArea(this.minerLogic.getCurrentRadius());
        return Collections.singletonList(
                new TextComponentTranslation("gregtech.machine.miner.working_area", workingArea, workingArea));
    }

    @Override
    public boolean shouldShowVoidingModeButton() {
        return false;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }
}
