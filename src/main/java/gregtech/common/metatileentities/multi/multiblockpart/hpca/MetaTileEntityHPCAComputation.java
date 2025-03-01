package gregtech.common.metatileentities.multi.multiblockpart.hpca;

import gregtech.api.GTValues;
import gregtech.api.capability.IHPCAComputationProvider;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.drawable.UITexture;

public class MetaTileEntityHPCAComputation extends MetaTileEntityHPCAComponent implements IHPCAComputationProvider {

    private final boolean advanced;

    public MetaTileEntityHPCAComputation(ResourceLocation metaTileEntityId, boolean advanced) {
        super(metaTileEntityId);
        this.advanced = advanced;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityHPCAComputation(metaTileEntityId, advanced);
    }

    @Override
    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    public SimpleOverlayRenderer getFrontOverlay() {
        if (isDamaged()) return advanced ? Textures.HPCA_ADVANCED_DAMAGED_OVERLAY : Textures.HPCA_DAMAGED_OVERLAY;
        return advanced ? Textures.HPCA_ADVANCED_COMPUTATION_OVERLAY : Textures.HPCA_COMPUTATION_OVERLAY;
    }

    @Override
    public TextureArea getComponentIcon() {
        if (isDamaged()) {
            return advanced ? GuiTextures.HPCA_ICON_DAMAGED_ADVANCED_COMPUTATION_COMPONENT :
                    GuiTextures.HPCA_ICON_DAMAGED_COMPUTATION_COMPONENT;
        }
        return advanced ? GuiTextures.HPCA_ICON_ADVANCED_COMPUTATION_COMPONENT :
                GuiTextures.HPCA_ICON_COMPUTATION_COMPONENT;
    }

    @Override
    public UITexture getComponentIcon2() {
        if (isDamaged()) {
            return advanced ? GTGuiTextures.HPCA_ICON_DAMAGED_ADVANCED_COMPUTATION_COMPONENT :
                    GTGuiTextures.HPCA_ICON_DAMAGED_COMPUTATION_COMPONENT;
        }
        return advanced ? GTGuiTextures.HPCA_ICON_ADVANCED_COMPUTATION_COMPONENT :
                GTGuiTextures.HPCA_ICON_COMPUTATION_COMPONENT;
    }

    @Override
    public SimpleOverlayRenderer getFrontActiveOverlay() {
        if (isDamaged())
            return advanced ? Textures.HPCA_ADVANCED_DAMAGED_ACTIVE_OVERLAY : Textures.HPCA_DAMAGED_ACTIVE_OVERLAY;
        return advanced ? Textures.HPCA_ADVANCED_COMPUTATION_ACTIVE_OVERLAY : Textures.HPCA_COMPUTATION_ACTIVE_OVERLAY;
    }

    @Override
    public int getUpkeepEUt() {
        return GTValues.VA[advanced ? GTValues.IV : GTValues.EV];
    }

    @Override
    public int getMaxEUt() {
        return GTValues.VA[advanced ? GTValues.ZPM : GTValues.LuV];
    }

    @Override
    public int getCWUPerTick() {
        if (isDamaged()) return 0;
        return advanced ? 16 : 4;
    }

    @Override
    public int getCoolingPerTick() {
        return advanced ? 4 : 2;
    }

    @Override
    public boolean canBeDamaged() {
        return true;
    }

    @Override
    public String getTileName() {
        return getMetaFullName();
    }
}
