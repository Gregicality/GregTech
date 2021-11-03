package gregtech.integration.jei.multiblock.infos;

import com.google.common.collect.Lists;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.*;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

import java.util.List;

public class FusionReactorInfo extends MultiblockInfoPage {

    private final int tier;

    public FusionReactorInfo(int tier) {
        this.tier = tier;
    }

    @Override
    public MultiblockControllerBase getController() {
        return MetaTileEntities.FUSION_REACTOR[tier];
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo shapeInfo = MultiblockShapeInfo.builder()
                .aisle("###############", "######SGS######", "###############")
                .aisle("######DCD######", "####GG###GG####", "######UCU######")
                .aisle("####CC###CC####", "###n##NMN##e###", "####CC###CC####")
                .aisle("###C#######C###", "##scwC###Cwcs##", "###C#######C###")
                .aisle("##C#########C##", "#G#e#######n#G#", "##C#########C##")
                .aisle("##C#########C##", "#G#C#######C#G#", "##C#########C##")
                .aisle("#D###########D#", "W#E#########W#E", "#U###########U#")
                .aisle("#C###########C#", "G#C#########C#G", "#C###########C#")
                .aisle("#D###########D#", "W#E#########W#E", "#U###########U#")
                .aisle("##C#########C##", "#G#C#######C#G#", "##C#########C##")
                .aisle("##C#########C##", "#G#e#######n#G#", "##C#########C##")
                .aisle("###C#######C###", "##wcsC###Cscw##", "###C#######C###")
                .aisle("####CC###CC####", "###n##SCS##e###", "####CC###CC####")
                .aisle("######DCD######", "####GG###GG####", "######UCU######")
                .aisle("###############", "######NGN######", "###############")
                .where('M', MetaTileEntities.FUSION_REACTOR[tier], EnumFacing.WEST)
                .where('C', getCasing(tier))
                .where('G', MetaBlocks.TRANSPARENT_CASING.getState(BlockTransparentCasing.CasingType.REINFORCED_GLASS))
                .where('c', getCoil(tier))
                .where('W', MetaTileEntities.FLUID_EXPORT_HATCH[6 + tier], EnumFacing.NORTH)
                .where('E', MetaTileEntities.FLUID_EXPORT_HATCH[6 + tier], EnumFacing.SOUTH)
                .where('S', MetaTileEntities.FLUID_EXPORT_HATCH[6 + tier], EnumFacing.EAST)
                .where('N', MetaTileEntities.FLUID_EXPORT_HATCH[6 + tier], EnumFacing.WEST)
                .where('w', MetaTileEntities.ENERGY_INPUT_HATCH[6 + tier], EnumFacing.WEST)
                .where('e', MetaTileEntities.ENERGY_INPUT_HATCH[6 + tier], EnumFacing.SOUTH)
                .where('s', MetaTileEntities.ENERGY_INPUT_HATCH[6 + tier], EnumFacing.EAST)
                .where('n', MetaTileEntities.ENERGY_INPUT_HATCH[6 + tier], EnumFacing.NORTH)
                .where('U', MetaTileEntities.FLUID_IMPORT_HATCH[6 + tier], EnumFacing.UP)
                .where('D', MetaTileEntities.FLUID_IMPORT_HATCH[6 + tier], EnumFacing.DOWN)
                .where('#', Blocks.AIR.getDefaultState()).build();

        return Lists.newArrayList(shapeInfo);
    }

    private static IBlockState getCasing(int tier) {
        switch (tier) {
            case 0:
                return MetaBlocks.MACHINE_CASING.getState(BlockMachineCasing.MachineCasingType.LuV);
            case 1:
                return MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING);
            default:
                return MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING_MK2);
        }
    }

    private static IBlockState getCoil(int tier) {
        if (tier == 0)
            return MetaBlocks.FUSION_COIL.getState(BlockFusionCoil.CoilType.SUPERCONDUCTOR);
        return MetaBlocks.FUSION_COIL.getState(BlockFusionCoil.CoilType.FUSION_COIL);
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                I18n.format(String.format("gregtech.multiblock.fusion_reactor_mk%d.description", tier + 1))
        };
    }

    @Override
    public float getDefaultZoom() {
        return 0.5f;
    }
}
