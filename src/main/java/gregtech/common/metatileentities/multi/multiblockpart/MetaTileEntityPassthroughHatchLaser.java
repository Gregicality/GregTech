package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IPassthroughHatch;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityPassthroughHatchLaser extends MetaTileEntityMultiblockPart implements IPassthroughHatch,
                                                 IMultiblockAbilityPart<IPassthroughHatch> {

    public MetaTileEntityPassthroughHatchLaser(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_LASER && side == getFrontFacing().getOpposite()) {
            World world = getWorld();
            if (world != null && !world.isRemote) {
                TileEntity te = world.getTileEntity(getPos().offset(getFrontFacing()));
                if (te != null) {
                    return GregtechTileCapabilities.CAPABILITY_LASER.cast(te
                            .getCapability(GregtechTileCapabilities.CAPABILITY_LASER, getFrontFacing().getOpposite()));
                }
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPassthroughHatchLaser(metaTileEntityId, getTier());
    }

    @Override
    public MultiblockAbility<IPassthroughHatch> getAbility() {
        return MultiblockAbility.PASSTHROUGH_HATCH;
    }

    @Override
    public void registerAbilities(@NotNull List<IPassthroughHatch> abilityList) {
        abilityList.add(this);
    }

    @Override
    public @NotNull Class<?> getPassthroughType() {
        return ILaserContainer.class;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.laser_hatch.tooltip2"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.LASER_SOURCE.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.LASER_TARGET.renderSided(getFrontFacing().getOpposite(), renderState, translation, pipeline);
    }
}
