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

public class LargePlasmaTurbine extends AbstractLargeTurbine {

    public LargePlasmaTurbine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.PLASMA_GENERATOR_FUELS, GTValues.IV, TurbineType.PLASMA); //TODO tier
        this.recipeMapWorkable = new PlasmaTurbineLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new LargePlasmaTurbine(metaTileEntityId);
    }

    @Override
    protected @NotNull IBlockState getCasingState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TUNGSTENSTEEL_TURBINE_CASING);
    }

    @Override
    protected @NotNull IBlockState getGearboxState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TUNGSTENSTEEL_GEARBOX);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.ROBUST_TUNGSTENSTEEL_CASING;
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return Textures.LARGE_PLASMA_TURBINE_OVERLAY;
    }
}
