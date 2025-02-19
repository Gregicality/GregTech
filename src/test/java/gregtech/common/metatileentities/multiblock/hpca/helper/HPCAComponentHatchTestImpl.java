package gregtech.common.metatileentities.multiblock.hpca.helper;

import gregtech.api.capability.IHPCAComponentHatch;
import gregtech.api.gui.resources.TextureArea;

public class HPCAComponentHatchTestImpl implements IHPCAComponentHatch {

    private final int upkeepEUt;
    private final int maxEUt;

    protected HPCAComponentHatchTestImpl(int upkeepEUt, int maxEUt) {
        this.upkeepEUt = upkeepEUt;
        this.maxEUt = maxEUt;
    }

    @Override
    public int getUpkeepEUt() {
        return upkeepEUt;
    }

    @Override
    public int getMaxEUt() {
        return maxEUt;
    }

    // not tested
    @Override
    public boolean canBeDamaged() {
        return false;
    }

    // not tested
    @Override
    public boolean isBridge() {
        return false;
    }

    // not tested
    @Override
    public TextureArea getComponentIcon() {
        return null;
    }

    // not tested
    @Override
    public String getTileName() {
        return "";
    }
}
