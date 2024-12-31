package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IQuantumController;
import gregtech.api.capability.IQuantumStorage;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.utils.PipelineUtil;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

public class MetaTileEntityEnergyHatch extends MetaTileEntityMultiblockPart
                                       implements IMultiblockAbilityPart<IEnergyContainer>,
                                       IQuantumStorage<IEnergyContainer> {

    protected final boolean isExportHatch;
    protected final int amperage;
    protected final IEnergyContainer energyContainer;

    /** not synced, server only. lazily initialized from pos */
    private WeakReference<IQuantumController> controller = new WeakReference<>(null);

    /** synced, server and client */
    private BlockPos controllerPos;

    public MetaTileEntityEnergyHatch(ResourceLocation metaTileEntityId, int tier, int amperage, boolean isExportHatch) {
        super(metaTileEntityId, tier);
        this.isExportHatch = isExportHatch;
        this.amperage = amperage;
        if (isExportHatch) {
            this.energyContainer = EnergyContainerHandler.emitterContainer(this, GTValues.V[tier] * 64L * amperage,
                    GTValues.V[tier], amperage);
            ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s == getFrontFacing());
        } else {
            this.energyContainer = EnergyContainerHandler.receiverContainer(this, GTValues.V[tier] * 16L * amperage,
                    GTValues.V[tier], amperage);
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityEnergyHatch(metaTileEntityId, getTier(), amperage, isExportHatch);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            getOverlay().renderSided(getFrontFacing(), renderState, translation,
                    PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
        }
    }

    @Override
    public void update() {
        super.update();
        checkWeatherOrTerrainExplosion(getTier(), getTier() * 10, energyContainer);
    }

    @NotNull
    private SimpleOverlayRenderer getOverlay() {
        if (isExportHatch) {
            if (amperage <= 2) {
                return Textures.ENERGY_OUT_MULTI;
            } else if (amperage <= 4) {
                return Textures.ENERGY_OUT_HI;
            } else if (amperage <= 16) {
                return Textures.ENERGY_OUT_ULTRA;
            } else {
                return Textures.ENERGY_OUT_MAX;
            }
        } else {
            if (amperage <= 2) {
                return Textures.ENERGY_IN_MULTI;
            } else if (amperage <= 4) {
                return Textures.ENERGY_IN_HI;
            } else if (amperage <= 16) {
                return Textures.ENERGY_IN_ULTRA;
            } else {
                return Textures.ENERGY_IN_MAX;
            }
        }
    }

    @Override
    public MultiblockAbility<IEnergyContainer> getAbility() {
        return isExportHatch ? MultiblockAbility.OUTPUT_ENERGY : MultiblockAbility.INPUT_ENERGY;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(energyContainer);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        String tierName = GTValues.VNF[getTier()];
        addDescriptorTooltip(stack, world, tooltip, advanced);

        if (isExportHatch) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", energyContainer.getOutputVoltage(),
                    tierName));
            tooltip.add(
                    I18n.format("gregtech.universal.tooltip.amperage_out_till", energyContainer.getOutputAmperage()));
        } else {
            tooltip.add(
                    I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_till", energyContainer.getInputAmperage()));
        }
        tooltip.add(
                I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    protected void addDescriptorTooltip(ItemStack stack, @Nullable World world, List<String> tooltip,
                                        boolean advanced) {
        if (isExportHatch) {
            if (amperage > 2) {
                tooltip.add(I18n.format("gregtech.machine.energy_hatch.output_hi_amp.tooltip"));
            } else {
                tooltip.add(I18n.format("gregtech.machine.energy_hatch.output.tooltip"));
            }
        } else {
            if (amperage > 2) {
                tooltip.add(I18n.format("gregtech.machine.energy_hatch.input_hi_amp.tooltip"));
            } else {
                tooltip.add(I18n.format("gregtech.machine.energy_hatch.input.tooltip"));
            }
        }
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public boolean canRenderFrontFaceX() {
        return isExportHatch;
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        // override here is gross, but keeps things in order despite
        // IDs being out of order, due to EV 4A and UEV+ 4A+ hatches being added later
        if (this == MetaTileEntities.ENERGY_INPUT_HATCH[0]) {
            for (MetaTileEntityEnergyHatch hatch : MetaTileEntities.ENERGY_INPUT_HATCH) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityEnergyHatch hatch : MetaTileEntities.ENERGY_OUTPUT_HATCH) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityEnergyHatch hatch : MetaTileEntities.ENERGY_INPUT_HATCH_4A) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityEnergyHatch hatch : MetaTileEntities.ENERGY_INPUT_HATCH_16A) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityEnergyHatch hatch : MetaTileEntities.ENERGY_OUTPUT_HATCH_4A) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityEnergyHatch hatch : MetaTileEntities.ENERGY_OUTPUT_HATCH_16A) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityEnergyHatch hatch : MetaTileEntities.SUBSTATION_ENERGY_INPUT_HATCH) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityEnergyHatch hatch : MetaTileEntities.SUBSTATION_ENERGY_OUTPUT_HATCH) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
        } else if (this.getClass() != MetaTileEntityEnergyHatch.class &&
                this.getClass() != MetaTileEntitySubstationEnergyHatch.class) {
                    // let subclasses fall through this override
                    super.getSubItems(creativeTab, subItems);
                }
    }

    @Override
    public void doExplosion(float explosionPower) {
        if (getController() != null)
            getController().explodeMultiblock(explosionPower);
        else {
            super.doExplosion(explosionPower);
        }
    }

    @Override
    public void onRemoval() {
        if (!getWorld().isRemote && isConnected()) {
            IQuantumController controller = getQuantumController();
            if (controller != null) controller.rebuildNetwork();
        }
    }

    @Override
    public void onPlacement(@Nullable EntityLivingBase placer) {
        super.onPlacement(placer);
        if (getWorld() == null || getWorld().isRemote || isExportHatch)
            return;

        // add to the network if an adjacent block is part of a network
        // use whatever we find first, merging networks is not supported
        tryFindNetwork();
    }

    @Override
    public Type getType() {
        return Type.ENERGY;
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        if (isConnected()) {
            return Textures.QUANTUM_CASING;
        }
        return super.getBaseTexture();
    }

    @Override
    public void setConnected(IQuantumController controller) {
        if (getWorld().isRemote) return;
        if (isExportHatch) return;

        if (!controller.getPos().equals(controllerPos)) {
            this.controller = new WeakReference<>(controller);
            this.controllerPos = controller.getPos();
            writeCustomData(GregtechDataCodes.UPDATE_CONTROLLER_POS, buf -> buf.writeBlockPos(controllerPos));
            markDirty();
        }
    }

    @Override
    public void setDisconnected() {
        if (getWorld().isRemote) return;

        controller.clear();
        controllerPos = null;
        writeCustomData(GregtechDataCodes.REMOVE_CONTROLLER, buf -> {});
        markDirty();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_CONTROLLER_POS) {
            this.controllerPos = buf.readBlockPos();
            this.controller.clear();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.REMOVE_CONTROLLER) {
            this.controllerPos = null;
            this.controller.clear();
            scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(controllerPos != null);
        if (controllerPos != null) {
            buf.writeBlockPos(controllerPos);
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            controllerPos = buf.readBlockPos();
            scheduleRenderUpdate();
        }
    }

    // use this to make sure controller is properly initialized
    @Override
    public final IQuantumController getQuantumController() {
        if (isConnected()) {
            if (controller.get() != null) return controller.get();
            MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), controllerPos);
            if (mte instanceof IQuantumController quantumController) {
                controller = new WeakReference<>(quantumController);
                return quantumController;
            } else {
                // controller is no longer there for some reason, need to disconnect
                setDisconnected();
                tryFindNetwork();
            }
        }
        return null;
    }

    @Override
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    @Override
    public IEnergyContainer getTypeValue() {
        return this.energyContainer;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tagCompound = super.writeToNBT(data);
        tagCompound.setBoolean("HasController", controllerPos != null);
        if (controllerPos != null) {
            tagCompound.setLong("ControllerPos", controllerPos.toLong());
        }
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.getBoolean("HasController")) {
            this.controllerPos = BlockPos.fromLong(data.getLong("ControllerPos"));
        }
    }
}
