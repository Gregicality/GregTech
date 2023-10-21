package gregtech.api.metatileentity.multiblock;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextureArea;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public interface IProgressBarMultiblock {

    default boolean showProgressBar() {
        return true;
    }

    /** Can optionally have two progress bars side-by-side. Only values of 1 or 2 here will be respected, others default to 1. */
    default int getNumProgressBars() {
        return 1;
    }

    /** Fill percentages, formatted as a double from [0, 1] of the progress bar(s). */
    double[] getFillPercentages();

    /** Textures for the progress bar(s). */
    default TextureArea[] getProgressBarTextures() {
        return new TextureArea[]{GuiTextures.PROGRESS_BAR_MULTI_ENERGY_YELLOW};
    }

    /**
     * Add hover text to your progress bar.
     *
     * @param index The index, 1 or 2, of your progress bar. Only relevant if you have multiple bars.
     */
    default void addBarHoverText(List<ITextComponent> hoverList, int index) {
    }
}
