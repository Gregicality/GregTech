package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IFissionReactorHatch;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockFissionCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityControlRodPort extends MetaTileEntityMultiblockNotifiablePart
                                          implements IFissionReactorHatch,
                                          IMultiblockAbilityPart<MetaTileEntityControlRodPort> {

    private boolean hasModeratorTip;

    public MetaTileEntityControlRodPort(ResourceLocation metaTileEntityId, boolean hasModeratorTip) {
        super(metaTileEntityId, 4, false);
        this.hasModeratorTip = hasModeratorTip;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityControlRodPort(metaTileEntityId, hasModeratorTip);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public boolean checkValidity(int depth) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        for (int i = 1; i < depth; i++) {
            if (getWorld().getBlockState(pos.move(this.frontFacing.getOpposite())) !=
                    MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.CONTROL_ROD_CHANNEL)) {
                return false;
            }
        }
        return getWorld().getBlockState(pos.move(this.frontFacing.getOpposite())) ==
                MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.REACTOR_VESSEL);
    }

    @Override
    public MultiblockAbility<MetaTileEntityControlRodPort> getAbility() {
        return MultiblockAbility.CONTROL_ROD_PORT;
    }

    @Override
    public void registerAbilities(List<MetaTileEntityControlRodPort> abilityList) {
        abilityList.add(this);
    }

    public boolean hasModeratorTip() {
        return hasModeratorTip;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format(this.getMetaName() + ".tooltip.1"));
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (!this.hasModeratorTip) {
            Textures.CONTROL_ROD.renderSided(getFrontFacing(), renderState, translation, pipeline);
        } else {
            Textures.CONTROL_ROD_MODERATED.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }
}
