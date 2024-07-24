package gregtech.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityLargeChemicalReactor extends RecipeMapMultiblockController {

    public MetaTileEntityLargeChemicalReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.LARGE_CHEMICAL_RECIPES);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeChemicalReactor(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        TraceabilityPredicate casing = states(getCasingState()).setMinGlobalLimited(10);
        TraceabilityPredicate abilities = autoAbilities();
        return FactoryBlockPattern.start()
                .aisle("XXX", "XCX", "XXX")
                .aisle("XCX", "CPC", "XCX")
                .aisle("XXX", "XSX", "XXX")
                .where('S', selfPredicate())
                .where('X', casing.or(abilities))
                .where('P', states(getPipeCasingState()))
                .where('C', heatingCoils().setMinGlobalLimited(1).setMaxGlobalLimited(1)
                        .or(abilities)
                        .or(casing))
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder baseBuilder = MultiblockShapeInfo.builder(RIGHT, DOWN, FRONT)
                .where('S', MetaTileEntities.LARGE_CHEMICAL_REACTOR, EnumFacing.SOUTH)
                .where('X', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING))
                .where('P',
                        MetaBlocks.BOILER_CASING
                                .getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE))
                .where('C', MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.CUPRONICKEL))
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[3], EnumFacing.SOUTH)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[3], EnumFacing.NORTH)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[3], EnumFacing.SOUTH)
                .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[3], EnumFacing.SOUTH)
                .where('H', MetaTileEntities.FLUID_EXPORT_HATCH[3], EnumFacing.SOUTH)
                .where('M',
                        () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                                MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING),
                        EnumFacing.SOUTH);
        shapeInfo.add(baseBuilder.shallowCopy()
                .aisle("XEX", "XCX", "XXX")
                .aisle("XXX", "XPX", "XXX")
                .aisle("IMO", "FSH", "XXX")
                .build());
        shapeInfo.add(baseBuilder.shallowCopy()
                .aisle("XEX", "XXX", "XXX")
                .aisle("XXX", "XPX", "XCX")
                .aisle("IMO", "FSH", "XXX")
                .build());
        shapeInfo.add(baseBuilder.shallowCopy()
                .aisle("XEX", "XXX", "XXX")
                .aisle("XCX", "XPX", "XXX")
                .aisle("IMO", "FSH", "XXX")
                .build());
        shapeInfo.add(baseBuilder.shallowCopy()
                .aisle("XEX", "XXX", "XXX")
                .aisle("XXX", "CPX", "XXX")
                .aisle("IMO", "FSH", "XXX")
                .build());
        shapeInfo.add(baseBuilder.shallowCopy()
                .aisle("XEX", "XXX", "XXX")
                .aisle("XXX", "XPC", "XXX")
                .aisle("IMO", "FSH", "XXX")
                .build());
        return shapeInfo;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.INERT_PTFE_CASING;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING);
    }

    protected IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE);
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_ELECTRICAL;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("gregtech.machine.perfect_oc"));
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.LARGE_CHEMICAL_REACTOR_OVERLAY;
    }
}
