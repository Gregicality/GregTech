package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.capability.impl.LaserContainerHandler;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static gregtech.api.GTValues.V;
import static gregtech.api.GTValues.VNF;

public class MetaTileEntityLaserHatch extends MetaTileEntityMultiblockPart
                                      implements IMultiblockAbilityPart<ILaserContainer>, IDataInfoProvider {

    private final boolean isOutput;
    private final int tier;
    private final int amperage;
    private final ILaserContainer buffer;

    public MetaTileEntityLaserHatch(ResourceLocation metaTileEntityId, boolean isOutput, int tier, int amperage) {
        super(metaTileEntityId, tier);
        this.isOutput = isOutput;
        this.tier = tier;
        this.amperage = amperage;
        if (isOutput) {
            this.buffer = LaserContainerHandler.emitterContainer(this, GTValues.V[tier] * 64L * amperage,
                    GTValues.V[tier], amperage);
            ((LaserContainerHandler) this.buffer).setSideOutputCondition(s -> s == getFrontFacing());
        } else {
            this.buffer = LaserContainerHandler.receiverContainer(this, GTValues.V[tier] * 64L * amperage,
                    GTValues.V[tier], amperage);
            ((LaserContainerHandler) this.buffer).setSideInputCondition(s -> s == getFrontFacing());
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLaserHatch(metaTileEntityId, isOutput, tier, amperage);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public MultiblockAbility<ILaserContainer> getAbility() {
        return isOutput ? MultiblockAbility.OUTPUT_LASER : MultiblockAbility.INPUT_LASER;
    }

    @Override
    public @NotNull List<? extends ILaserContainer> registerAbilities(
            @NotNull MultiblockAbility<? extends ILaserContainer> multiblockAbility) {
        return Collections.singletonList(this.buffer);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            if (isOutput) {
                Textures.LASER_SOURCE.renderSided(getFrontFacing(), renderState, translation, pipeline);
            } else {
                Textures.LASER_TARGET.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format(isOutput ? "gregtech.machine.laser_hatch.source.tooltip1" :
                "gregtech.machine.laser_hatch.target.tooltip1"));
        tooltip.add(I18n.format("gregtech.machine.laser_hatch.tooltip2"));

        if (isOutput) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", V[tier], VNF[tier]));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_out_till", amperage));
        } else {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", V[tier], VNF[tier]));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_till", amperage));
        }
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", buffer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        return Collections.singletonList(new TextComponentString(
                String.format("%d/%d EU", this.buffer.getEnergyStored(), this.buffer.getEnergyCapacity())));
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        return super.getCapability(capability, side);
    }
}
