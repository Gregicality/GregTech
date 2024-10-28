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

public class LargeSteamTurbine extends AbstractLargeTurbine {

    private final boolean isHighPressure;

    public LargeSteamTurbine(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, RecipeMaps.STEAM_TURBINE_FUELS, GTValues.IV, TurbineType.STEAM); // TODO tier
        this.isHighPressure = isHighPressure;
        this.recipeMapWorkable = new SteamTurbineLogic(this, isHighPressure);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new LargeSteamTurbine(metaTileEntityId, isHighPressure);
    }

    @Override
    protected @NotNull IBlockState getCasingState() {
        if (isHighPressure) {
            return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TITANIUM_TURBINE_CASING);
        }
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_TURBINE_CASING);
    }

    @Override
    protected @NotNull IBlockState getGearboxState() {
        if (isHighPressure) {
            return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TITANIUM_GEARBOX);
        }
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return isHighPressure ? Textures.STABLE_TITANIUM_CASING : Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return Textures.LARGE_STEAM_TURBINE_OVERLAY;
    }
}
