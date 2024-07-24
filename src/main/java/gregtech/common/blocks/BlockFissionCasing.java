package gregtech.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

public class BlockFissionCasing extends VariantBlock<BlockFissionCasing.FissionCasingType> {

    public BlockFissionCasing() {
        super(Material.IRON);
        setTranslationKey("fission_casing");
        setHardness(10.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(FissionCasingType.REACTOR_VESSEL));
    }

    public enum FissionCasingType implements IStringSerializable, IStateHarvestLevel {

        REACTOR_VESSEL("reactor_vessel", 2),
        FUEL_CHANNEL("fuel_channel", 2),
        CONTROL_ROD_CHANNEL("control_rod_channel", 2),
        COOLANT_CHANNEL("coolant_channel", 2);

        private final String name;
        private final int harvestLevel;

        FissionCasingType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return harvestLevel;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return ToolClasses.WRENCH;
        }
    }
}
