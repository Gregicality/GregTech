package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.IOpticalDataAccessHatch;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.recipes.Recipe;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;

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

import java.util.Collection;
import java.util.List;

public class MetaTileEntityOpticalDataHatch extends MetaTileEntityMultiblockNotifiablePart implements
                                            IMultiblockAbilityPart<IOpticalDataAccessHatch>, IOpticalDataAccessHatch {

    private final boolean isTransmitter;

    public MetaTileEntityOpticalDataHatch(ResourceLocation metaTileEntityId, boolean isTransmitter) {
        super(metaTileEntityId, GTValues.LuV, false);
        this.isTransmitter = isTransmitter;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityOpticalDataHatch(metaTileEntityId, this.isTransmitter);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            if (isTransmitter()) {
                Textures.OPTICAL_DATA_ACCESS_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
            } else {
                Textures.OPTICAL_DATA_ACCESS_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @Override
    public boolean isRecipeAvailable(@NotNull Recipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
        seen.add(this);
        if (isAttachedToMultiBlock()) {
            if (isTransmitter()) {
                MultiblockControllerBase controller = getController();
                if (!controller.isActive()) return false;

                return isRecipeAvailable(controller.getAbilities(MultiblockAbility.DATA_ACCESS_HATCH), seen, recipe) ||
                        isRecipeAvailable(controller.getAbilities(MultiblockAbility.OPTICAL_DATA_RECEPTION), seen,
                                recipe);
            } else {
                TileEntity tileEntity = getNeighbor(getFrontFacing());
                if (tileEntity == null) return false;

                if (tileEntity instanceof TileEntityOpticalPipe) {
                    IDataAccessHatch cap = tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS,
                            getFrontFacing().getOpposite());
                    return cap != null && cap.isRecipeAvailable(recipe, seen);
                }
            }
        }
        return false;
    }

    private static boolean isRecipeAvailable(@NotNull Iterable<? extends IDataAccessHatch> hatches,
                                             @NotNull Collection<IDataAccessHatch> seen,
                                             @NotNull Recipe recipe) {
        for (IDataAccessHatch hatch : hatches) {
            if (seen.contains(hatch)) continue;
            if (hatch.isRecipeAvailable(recipe, seen)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public boolean isTransmitter() {
        return this.isTransmitter;
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (side == getFrontFacing() && capability == GregtechTileCapabilities.CAPABILITY_DATA_ACCESS) {
            return GregtechTileCapabilities.CAPABILITY_DATA_ACCESS.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public MultiblockAbility<IOpticalDataAccessHatch> getAbility() {
        return isTransmitter() ? MultiblockAbility.OPTICAL_DATA_TRANSMISSION : MultiblockAbility.OPTICAL_DATA_RECEPTION;
    }

    @Override
    public void registerAbilities(
                                  @NotNull MultiblockAbility<IOpticalDataAccessHatch> multiblockAbility,
                                  @NotNull List<IOpticalDataAccessHatch> abilities) {
        abilities.add(this);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.disabled"));
    }
}
