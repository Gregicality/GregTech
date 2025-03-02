package gregtech.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
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
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockNuclearCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityGasCentrifuge extends RecipeMapMultiblockController {

    public MetaTileEntityGasCentrifuge(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.GAS_CENTRIFUGE_RECIPES);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(FRONT, UP, RIGHT)
                .aisle("SI", "HH", "CC", "CC", "CC", "CC", "CC")
                .aisle("EE", "HH", "CC", "CC", "CC", "CC", "CC").setRepeatable(1, 14)
                .aisle("OO", "HH", "CC", "CC", "CC", "CC", "CC")
                .where('S', selfPredicate())
                .where('P', states(getPipeState()))
                .where('H', states(getHeaterState()))
                .where('C', states(getCentrifugeState()))
                .where('I', states(getPipeState()).or(autoAbilities(false, false, false, false, true, false, false)))
                .where('E', states(getPipeState()).or(autoAbilities(true, true, false, false, false, false, false)))
                .where('O', states(getPipeState()).or(autoAbilities(false, false, false, false, false, true, false)))
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        if (structurePattern != null) {
            this.recipeMapWorkable.setParallelLimit(structurePattern.formedRepetitionCount[0]);
        }
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.parallel",
                I18n.format("gregtech.gas_centrifuge.tooltip.parallel")));
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.INERT_PTFE_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityGasCentrifuge(metaTileEntityId);
    }

    private IBlockState getPipeState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE);
    }

    private IBlockState getHeaterState() {
        return MetaBlocks.NUCLEAR_CASING.getState(
                BlockNuclearCasing.NuclearCasingType.GAS_CENTRIFUGE_HEATER);
    }

    private IBlockState getCentrifugeState() {
        return MetaBlocks.NUCLEAR_CASING
                .getState(BlockNuclearCasing.NuclearCasingType.GAS_CENTRIFUGE_COLUMN);
    }

    protected ICubeRenderer getFrontOverlay() {
        return Textures.GAS_CENTRIFUGE_OVERLAY;
    }
}
