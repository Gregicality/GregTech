package gregtech.common.metatileentities.multi.electric;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.BlockFusionCasing;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityActiveTransformer extends MultiblockWithDisplayBase implements IControllable {

    private boolean isWorkingEnabled = false;
    private IEnergyContainer powerOutput;
    private IEnergyContainer powerInput;
    private boolean isActive = false;
    private long averageIOLastSec;
    private long netIOLastSec;

    public MetaTileEntityActiveTransformer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.powerOutput = new EnergyContainerList(new ArrayList<>());
        this.powerInput = new EnergyContainerList(new ArrayList<>());
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityActiveTransformer(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        if (!getWorld().isRemote) {
            if ((getOffsetTimer() % 20) == 0) {
                averageIOLastSec = netIOLastSec / 20;
                netIOLastSec = 0;
            }

            if (isWorkingEnabled()) {
                long canDrain = powerInput.getEnergyStored();
                long totalDrained = powerOutput.changeEnergy(canDrain);
                powerInput.removeEnergy(totalDrained);
                netIOLastSec += totalDrained;
            }
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<IEnergyContainer> powerInput = new ArrayList<>(getAbilities(MultiblockAbility.INPUT_ENERGY));
        powerInput.addAll(getAbilities(MultiblockAbility.SUBSTATION_INPUT_ENERGY));

        List<IEnergyContainer> powerOutput = new ArrayList<>(getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        powerOutput.addAll(getAbilities(MultiblockAbility.SUBSTATION_OUTPUT_ENERGY));

        powerInput.addAll(getAbilities(MultiblockAbility.INPUT_LASER));
        powerOutput.addAll(getAbilities(MultiblockAbility.OUTPUT_LASER));

        // Invalidate the structure if there is not at least one output and one input
        if (powerInput.isEmpty() || powerOutput.isEmpty()) {
            this.invalidateStructure();
        }

        this.powerInput = new EnergyContainerList(powerInput);
        this.powerOutput = new EnergyContainerList(powerOutput);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.powerOutput = new EnergyContainerList(new ArrayList<>());
        this.powerInput = new EnergyContainerList(new ArrayList<>());
        setActive(false);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XCX", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('X', states(getCasingState()).setMinGlobalLimited(12).or(getHatchPredicates()))
                .where('S', selfPredicate())
                .where('C', states(MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL)))
                .build();
    }

    private TraceabilityPredicate getHatchPredicates() {
        // preview could be revised
        return abilities(MultiblockAbility.INPUT_ENERGY).setPreviewCount(1)
                .or(abilities(MultiblockAbility.OUTPUT_ENERGY).setPreviewCount(2))
                .or(abilities(MultiblockAbility.SUBSTATION_INPUT_ENERGY).setPreviewCount(1))
                .or(abilities(MultiblockAbility.SUBSTATION_OUTPUT_ENERGY).setPreviewCount(1))
                .or(abilities(MultiblockAbility.INPUT_LASER).setPreviewCount(1))
                .or(abilities(MultiblockAbility.OUTPUT_LASER).setPreviewCount(1));
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.HIGH_POWER_CASING;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.HIGH_POWER_CASING);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return Textures.DATA_BANK_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.isActive(),
                this.isWorkingEnabled());
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.setWorkingStatus(true, isActive())
                .setWorkingStatusKeys(
                        "gregtech.multiblock.idling",
                        "gregtech.multiblock.idling",
                        "gregtech.machine.active_transformer.routing")
                .addCustom(list -> {
                    if (isStructureFormed()) {
                        // Max input line
                        IKey maxInputFormatted = KeyUtil.number(TextFormatting.WHITE,
                                powerInput.getInputVoltage() * powerInput.getInputAmperage(), " EU/t");
                        list.add(KeyUtil.lang(TextFormatting.GREEN, "gregtech.multiblock.active_transformer.max_in",
                                maxInputFormatted));

                        // Max output line
                        IKey maxOutputFormatted = KeyUtil.number(TextFormatting.WHITE,
                                powerOutput.getOutputVoltage() * powerOutput.getOutputAmperage(), " EU/t");
                        list.add(KeyUtil.lang(TextFormatting.RED, "gregtech.multiblock.active_transformer.max_out",
                                maxOutputFormatted));

                        // Average I/O line
                        IKey avgIOFormatted = KeyUtil.number(TextFormatting.WHITE, averageIOLastSec, " EU/t");
                        list.add(KeyUtil.lang(TextFormatting.AQUA, "gregtech.multiblock.active_transformer.average_io",
                                avgIOFormatted));
                    }
                })
                .addWorkingStatusLine();
    }

    @Override
    public boolean shouldShowVoidingModeButton() {
        return false;
    }

    @Override
    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.isWorkingEnabled = isWorkingAllowed;
        markDirty();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
        }
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.isWorkingEnabled;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            markDirty();
            World world = getWorld();
            if (world != null && !world.isRemote) {
                writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", this.isActive);
        data.setBoolean("isWorkingEnabled", this.isWorkingEnabled);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isActive = data.getBoolean("isActive");
        this.isWorkingEnabled = data.getBoolean("isWorkingEnabled");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.isWorkingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
        this.isWorkingEnabled = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.isActive = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.isWorkingEnabled = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.active_transformer.tooltip1"));
        tooltip.add(I18n.format("gregtech.machine.active_transformer.tooltip2"));
        tooltip.add(I18n.format("gregtech.machine.active_transformer.tooltip3") + TooltipHelper.RAINBOW_SLOW +
                I18n.format("gregtech.machine.active_transformer.tooltip3.5"));
    }

    public long getAverageIOLastSec() {
        return this.averageIOLastSec;
    }
}
