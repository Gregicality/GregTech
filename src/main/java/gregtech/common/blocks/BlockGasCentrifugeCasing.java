package gregtech.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

public class BlockGasCentrifugeCasing extends VariantBlock<BlockGasCentrifugeCasing.GasCentrifugeCasingType> {

    public BlockGasCentrifugeCasing() {
        super(Material.IRON);
        setTranslationKey("gas_centrifuge_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(GasCentrifugeCasingType.GAS_CENTRIFUGE_COLUMN));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return state != getState(GasCentrifugeCasingType.GAS_CENTRIFUGE_COLUMN);
    }

    public enum GasCentrifugeCasingType implements IStringSerializable, IStateHarvestLevel {

        GAS_CENTRIFUGE_COLUMN("gas_centrifuge_column", 2);

        private String name;
        private int harvestLevel;

        GasCentrifugeCasingType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return harvestLevel;
        }
    }
}
