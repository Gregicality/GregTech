package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.utils.PipelineUtil;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.AMP_INDEX;
import static gregtech.api.capability.GregtechDataCodes.SYNC_TILE_MODE;

public class MetaTileEntityTransformer extends TieredMetaTileEntity {

    private final int[] highAmperages;

    private boolean isTransformUp;
    private int ampIndex;

    public MetaTileEntityTransformer(ResourceLocation metaTileEntityId, int tier, int... highAmperages) {
        super(metaTileEntityId, tier);
        if (highAmperages == null || highAmperages.length == 0) {
            this.highAmperages = new int[] { 1 }; // fallback case, "normal" transformer
        } else {
            this.highAmperages = highAmperages;
        }
        this.ampIndex = this.highAmperages.length - 1; // start as max amperage this transformer can do
        reinitializeEnergyContainer();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityTransformer(metaTileEntityId, getTier(), highAmperages);
    }

    public boolean hasMultipleAmperages() {
        return highAmperages != null && highAmperages.length > 1;
    }

    public int getCurrentHighAmperage() {
        return highAmperages != null ? highAmperages[ampIndex] : 4; // funny fallback moment
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("Inverted", isTransformUp);
        if (hasMultipleAmperages()) {
            data.setInteger("ampIndex", ampIndex);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isTransformUp = data.getBoolean("Inverted");
        if (hasMultipleAmperages()) {
            this.ampIndex = data.getInteger("ampIndex");
        }
        reinitializeEnergyContainer();
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isTransformUp);
        if (hasMultipleAmperages()) {
            buf.writeInt(ampIndex);
        }
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isTransformUp = buf.readBoolean();
        if (hasMultipleAmperages()) {
            this.ampIndex = buf.readInt();
        }
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == SYNC_TILE_MODE) {
            this.isTransformUp = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == AMP_INDEX) {
            this.ampIndex = buf.readInt();
            scheduleRenderUpdate();
        }
    }

    public boolean isInverted() {
        return isTransformUp;
    }

    public void setTransformUp(boolean inverted) {
        isTransformUp = inverted;
        if (!getWorld().isRemote) {
            reinitializeEnergyContainer();
            writeCustomData(SYNC_TILE_MODE, b -> b.writeBoolean(isTransformUp));
            notifyBlockUpdate();
            markDirty();
        }
    }

    protected void incrementAmpIndex() {
        if (hasMultipleAmperages()) {
            this.ampIndex++;
            if (this.ampIndex >= highAmperages.length) {
                this.ampIndex = 0;
            }
            if (!getWorld().isRemote) {
                reinitializeEnergyContainer();
                writeCustomData(AMP_INDEX, b -> b.writeInt(ampIndex));
                notifyBlockUpdate();
                markDirty();
            }
        }
    }

    @Override
    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        int highAmperage = getCurrentHighAmperage();
        int lowAmperage = highAmperage * 4;

        if (isTransformUp) {
            // storage = 1 amp high; input = tier / 4; amperage = 4; output = tier; amperage = 1
            this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 8L * lowAmperage, tierVoltage,
                    lowAmperage, tierVoltage * 4, highAmperage);
            ((EnergyContainerHandler) this.energyContainer).setSideInputCondition(s -> s != getFrontFacing());
            ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s == getFrontFacing());
        } else {
            // storage = 1 amp high; input = tier; amperage = 1; output = tier / 4; amperage = 4
            this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 8L * lowAmperage, tierVoltage * 4,
                    highAmperage, tierVoltage, lowAmperage);
            ((EnergyContainerHandler) this.energyContainer).setSideInputCondition(s -> s == getFrontFacing());
            ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s != getFrontFacing());
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        SimpleOverlayRenderer otherFaceTexture;
        SimpleOverlayRenderer frontFaceTexture;

        switch (getCurrentHighAmperage()) {
            case 1 -> { // 1A <-> 4A
                otherFaceTexture = isTransformUp ? Textures.ENERGY_IN : Textures.ENERGY_OUT;
                frontFaceTexture = isTransformUp ? Textures.ENERGY_OUT_MULTI : Textures.ENERGY_IN_MULTI;
            }
            case 2 -> { // 2A <-> 8A
                otherFaceTexture = isTransformUp ? Textures.ENERGY_IN_MULTI : Textures.ENERGY_OUT_MULTI;
                frontFaceTexture = isTransformUp ? Textures.ENERGY_OUT_HI : Textures.ENERGY_IN_HI;
            }
            case 4 -> { // 4A <-> 16A
                otherFaceTexture = isTransformUp ? Textures.ENERGY_IN_HI : Textures.ENERGY_OUT_HI;
                frontFaceTexture = isTransformUp ? Textures.ENERGY_OUT_ULTRA : Textures.ENERGY_IN_ULTRA;
            }
            default -> { // 16A <-> 64A or more
                otherFaceTexture = isTransformUp ? Textures.ENERGY_IN_ULTRA : Textures.ENERGY_OUT_ULTRA;
                frontFaceTexture = isTransformUp ? Textures.ENERGY_OUT_MAX : Textures.ENERGY_IN_MAX;
            }
        }

        frontFaceTexture.renderSided(frontFacing, renderState, translation,
                PipelineUtil.color(pipeline, GTValues.VC[getTier() + 1]));
        Arrays.stream(EnumFacing.values()).filter(f -> f != frontFacing)
                .forEach((f -> otherFaceTexture.renderSided(f, renderState, translation,
                        PipelineUtil.color(pipeline, GTValues.VC[getTier()]))));
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    public boolean onSoftMalletClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                     CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote) {
            scheduleRenderUpdate();
            return true;
        }
        if (isTransformUp) {
            setTransformUp(false);
            playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.transformer.message_transform_down",
                    energyContainer.getInputVoltage(), energyContainer.getInputAmperage(),
                    energyContainer.getOutputVoltage(), energyContainer.getOutputAmperage()));
        } else {
            setTransformUp(true);
            playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.transformer.message_transform_up",
                    energyContainer.getInputVoltage(), energyContainer.getInputAmperage(),
                    energyContainer.getOutputVoltage(), energyContainer.getOutputAmperage()));
        }
        return true;
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (hasMultipleAmperages()) {
            if (getWorld().isRemote) {
                scheduleRenderUpdate();
                return true;
            }

            incrementAmpIndex();
            playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.transformer_adjustable.message_adjust",
                    energyContainer.getInputVoltage(), energyContainer.getInputAmperage(),
                    energyContainer.getOutputVoltage(), energyContainer.getOutputAmperage()));

            return true;
        }
        return false;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        String lowerTierName = GTValues.VNF[getTier()];
        String higherTierName = GTValues.VNF[getTier() + 1];
        long lowerVoltage = energyContainer.getOutputVoltage();
        long higherVoltage = energyContainer.getInputVoltage();
        long lowerAmperage = energyContainer.getInputAmperage();
        long higherAmperage = energyContainer.getOutputAmperage();

        if (!hasMultipleAmperages() && highAmperages[0] == 1) { // just a basic transformer for 1A <-> 4A
            tooltip.add(I18n.format("gregtech.machine.transformer.description"));
        } else { // some higher amp or multiple-mode transformer
            tooltip.add(I18n.format("gregtech.machine.transformer.higher_amp.description"));
        }
        tooltip.add(I18n.format("gregtech.machine.transformer.tooltip_tool_usage"));
        if (hasMultipleAmperages()) {
            tooltip.add(I18n.format("gregtech.machine.transformer_adjustable.tooltip_tool_usage", higherAmperage));
        }
        tooltip.add(I18n.format("gregtech.machine.transformer.tooltip_transform_down", lowerAmperage, higherVoltage,
                higherTierName, higherAmperage, lowerVoltage, lowerTierName));
        tooltip.add(I18n.format("gregtech.machine.transformer.tooltip_transform_up", higherAmperage, lowerVoltage,
                lowerTierName, lowerAmperage, higherVoltage, higherTierName));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        if (hasMultipleAmperages()) {
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.toggle_mode_covers"));
        } else {
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        }
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.toggle_mode"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        // fix JEI ordering
        if (this == MetaTileEntities.TRANSFORMER[0]) {
            for (var transformer : MetaTileEntities.TRANSFORMER) {
                if (transformer != null) subItems.add(transformer.getStackForm());
            }
            for (var transformer : MetaTileEntities.HI_AMP_TRANSFORMER) {
                if (transformer != null) subItems.add(transformer.getStackForm());
            }
            for (var transformer : MetaTileEntities.POWER_TRANSFORMER) {
                if (transformer != null) subItems.add(transformer.getStackForm());
            }
        }
    }
}
