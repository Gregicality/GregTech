package gregtech.common.metatileentities.multi.multiblockpart.hpca;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.drawable.UITexture;

public class MetaTileEntityHPCABridge extends MetaTileEntityHPCAComponent {

    public MetaTileEntityHPCABridge(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityHPCABridge(metaTileEntityId);
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    @Override
    public boolean doesAllowBridging() {
        return true;
    }

    @Override
    public SimpleOverlayRenderer getFrontOverlay() {
        return Textures.HPCA_BRIDGE_OVERLAY;
    }

    @Override
    public TextureArea getComponentIcon() {
        return GuiTextures.HPCA_ICON_BRIDGE_COMPONENT;
    }

    @Override
    public UITexture getComponentIcon2() {
        return GTGuiTextures.HPCA_ICON_BRIDGE_COMPONENT;
    }

    @Override
    public SimpleOverlayRenderer getFrontActiveOverlay() {
        return Textures.HPCA_BRIDGE_ACTIVE_OVERLAY;
    }

    @Override
    public int getUpkeepEUt() {
        return GTValues.VA[GTValues.IV];
    }

    @Override
    public boolean canBeDamaged() {
        return false;
    }

    @Override
    public String getTileName() {
        return getMetaFullName();
    }
}
