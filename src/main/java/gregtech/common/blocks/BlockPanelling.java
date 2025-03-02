package gregtech.common.blocks;

import gregtech.api.block.VariantBlock;

import net.minecraft.block.SoundType;
import net.minecraft.util.IStringSerializable;

public class BlockPanelling extends VariantBlock<BlockPanelling.PanellingType> {

    public BlockPanelling() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("panelling");
        setHardness(2.0f);
        setResistance(5.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(PanellingType.WHITE));
    }

    public enum PanellingType implements IStringSerializable {

        WHITE("white"),
        ORANGE("orange"),
        MAGENTA("magenta"),
        LIGHT_BLUE("light_blue"),
        YELLOW("yellow"),
        LIME("lime"),
        PINK("pink"),
        GRAY("gray"),
        LIGHT_GRAY("light_gray"),
        CYAN("cyan"),
        PURPLE("purple"),
        BLUE("blue"),
        BROWN("brown"),
        GREEN("green"),
        RED("red"),
        BLACK("black");

        private final String name;

        PanellingType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
