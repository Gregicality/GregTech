package gregtech.common.metatileentities.multi;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockNuclearCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntitySpentFuelPool extends RecipeMapMultiblockController {

    public MetaTileEntitySpentFuelPool(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.SPENT_FUEL_POOL_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySpentFuelPool(metaTileEntityId);
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(FRONT, UP, RIGHT)
                // spotless:off
                .aisle("CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "TTTTTTTTTT")
                .aisle("CCCCCCCCCC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CUUUUUUUUC", "S........T")
                .aisle("CCCCCCCCCC", "CWRRRRRRWC", "CWRRRRRRWC", "CWRRRRRRWC", "CWRRRRRRWC", "CWRRRRRRWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CUUUUUUUUC", "T........T")
                .setRepeatable(1, 10)
                .aisle("CCCCCCCCCC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CUUUUUUUUC", "T........T")
                .aisle("CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "TTTTTTTTTT")
                //spotless:on
                .where('S', selfPredicate())
                .where('.', any())
                .where('C', blocks(MetaBlocks.PANELLING))
                .where('W', blocks(Blocks.WATER).or(blocks(Blocks.FLOWING_WATER)))
                .where('U', blocks(Blocks.WATER))
                .where('R', states(getRodState()))
                .where('T',
                        states(getMetalCasingState()).or(autoAbilities(true, false, true, true, false, true, false)))
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.recipeMapWorkable.setParallelLimit(structurePattern.formedRepetitionCount[0] * 32);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.SPENT_FUEL_POOL_OVERLAY;
    }

    private IBlockState getRodState() {
        return MetaBlocks.NUCLEAR_CASING.getState(BlockNuclearCasing.NuclearCasingType.SPENT_FUEL_CASING);
    }

    private IBlockState getMetalCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.parallel", "32 per block of pool length"));
    }
}
