package gregtech.common.metatileentities.converter;

import gregtech.api.GTValues;
import gregtech.api.capability.FeCompat;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.PipelineUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.SYNC_TILE_MODE;

public class MetaTileEntityConverter extends TieredMetaTileEntity {

    protected final ConverterTrait converterTrait;

    protected final int amps;

    public MetaTileEntityConverter(ResourceLocation metaTileEntityId, int tier, int amps) {
        super(metaTileEntityId, tier);
        this.amps = amps;
        this.converterTrait = initializeTrait();
        reinitializeEnergyContainer();
    }

    protected ConverterTrait initializeTrait() {
        return new ConverterTrait(this, amps, true);
    }

    @Override
    protected void reinitializeEnergyContainer() {
        if (converterTrait == null) return;
        this.energyContainer = converterTrait.getEnergyEUContainer();
    }

    @Override
    public boolean onSoftMalletClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                     CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote) {
            scheduleRenderUpdate();
            return true;
        }
        if (converterTrait.isFeToEu()) {
            setFeToEu(false);
            playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.energy_converter.message_conversion_eu",
                    converterTrait.getBaseAmps(), converterTrait.getVoltage(),
                    FeCompat.toFe(converterTrait.getVoltage() * converterTrait.getBaseAmps(), FeCompat.ratio(false))));
        } else {
            setFeToEu(true);
            playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.energy_converter.message_conversion_fe",
                    FeCompat.toFe(converterTrait.getVoltage() * converterTrait.getBaseAmps(), FeCompat.ratio(true)),
                    converterTrait.getBaseAmps(), converterTrait.getVoltage()));
        }
        return true;
    }

    public void setFeToEu(boolean feToEu) {
        converterTrait.setFeToEu(feToEu);
        if (!getWorld().isRemote) {
            writeCustomData(SYNC_TILE_MODE, b -> b.writeBoolean(converterTrait.isFeToEu()));
            notifyBlockUpdate();
            markDirty();
        }
    }

    public boolean isFeToEu() {
        return converterTrait.isFeToEu();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        if (dataId == SYNC_TILE_MODE) {
            converterTrait.setFeToEu(buf.readBoolean());
            scheduleRenderUpdate();
        }
        super.receiveCustomData(dataId, buf);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityConverter(metaTileEntityId, getTier(), amps);
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        if (ConfigHolder.compat.energy.enableFEConverters) {
            super.getSubItems(creativeTab, subItems);
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        buf.writeBoolean(converterTrait.isFeToEu());
        super.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        converterTrait.setFeToEu(buf.readBoolean());
        super.receiveInitialSyncData(buf);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[getTier()].render(renderState, translation, colouredPipeline);
        if (converterTrait.isFeToEu()) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (facing == frontFacing)
                    Textures.ENERGY_OUT.renderSided(facing, renderState, translation,
                            PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
                else
                    Textures.CONVERTER_FE_IN.renderSided(facing, renderState, translation, pipeline);
            }
        } else {
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (facing == frontFacing)
                    Textures.CONVERTER_FE_OUT.renderSided(facing, renderState, translation, pipeline);
                else
                    Textures.ENERGY_IN.renderSided(facing, renderState, translation,
                            PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[getTier()].getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return side != (!converterTrait.isFeToEu() ? frontFacing : null) ?
                    GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(converterTrait.getEnergyEUContainer()) : null;
        }
        if (capability == CapabilityEnergy.ENERGY) {
            return side != (converterTrait.isFeToEu() ? frontFacing : null) ?
                    CapabilityEnergy.ENERGY.cast(converterTrait.getEnergyFEContainer()) : null;
        }
        if (capability == GregtechCapabilities.CAPABILITY_CONVERTER) {
            return GregtechCapabilities.CAPABILITY_CONVERTER.cast(converterTrait);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        long voltage = converterTrait.getVoltage();
        long amps = converterTrait.getBaseAmps();
        tooltip.add(I18n.format("gregtech.machine.energy_converter.description"));
        tooltip.add(I18n.format("gregtech.machine.energy_converter.tooltip_tool_usage"));
        tooltip.add(I18n.format("gregtech.machine.energy_converter.tooltip_conversion_fe",
                FeCompat.toFe(voltage * amps, FeCompat.ratio(true)), amps, voltage, GTValues.VNF[getTier()]));
        tooltip.add(I18n.format("gregtech.machine.energy_converter.tooltip_conversion_eu", amps, voltage,
                GTValues.VNF[getTier()], FeCompat.toFe(voltage * amps, FeCompat.ratio(false))));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.toggle_mode"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return converterTrait.getBaseAmps();
    }

    @Override
    protected boolean isEnergyEmitter() {
        return converterTrait.isFeToEu();
    }
}
