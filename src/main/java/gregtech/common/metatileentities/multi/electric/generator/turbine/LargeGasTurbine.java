package gregtech.common.metatileentities.multi.electric.generator.turbine;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;

import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.state.IBlockState;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public class LargeGasTurbine extends AbstractLargeTurbine {

    public LargeGasTurbine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.GAS_TURBINE_FUELS, GTValues.IV, TurbineType.GAS); //TODO tier
        this.recipeMapWorkable = new GasTurbineLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new LargeGasTurbine(metaTileEntityId);
    }

    @Override
    protected @NotNull IBlockState getCasingState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_TURBINE_CASING);
    }

    @Override
    protected @NotNull IBlockState getGearboxState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_STEEL_GEARBOX);
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return Textures.LARGE_GAS_TURBINE_OVERLAY;
    }
}
