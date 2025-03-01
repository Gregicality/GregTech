package gregtech.common.metatileentities.multi.multiblockpart.hpca;

import gregtech.api.GTValues;
import gregtech.api.capability.IHPCACoolantProvider;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.drawable.UITexture;

public class MetaTileEntityHPCACooler extends MetaTileEntityHPCAComponent implements IHPCACoolantProvider {

    private final boolean advanced;

    public MetaTileEntityHPCACooler(ResourceLocation metaTileEntityId, boolean advanced) {
        super(metaTileEntityId);
        this.advanced = advanced;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityHPCACooler(metaTileEntityId, advanced);
    }

    @Override
    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    public SimpleOverlayRenderer getFrontOverlay() {
        return advanced ? Textures.HPCA_ACTIVE_COOLER_OVERLAY : Textures.HPCA_HEAT_SINK_OVERLAY;
    }

    @Override
    public TextureArea getComponentIcon() {
        return advanced ? GuiTextures.HPCA_ICON_ACTIVE_COOLER_COMPONENT : GuiTextures.HPCA_ICON_HEAT_SINK_COMPONENT;
    }

    @Override
    public UITexture getComponentIcon2() {
        return advanced ? GTGuiTextures.HPCA_ICON_ACTIVE_COOLER_COMPONENT : GTGuiTextures.HPCA_ICON_HEAT_SINK_COMPONENT;
    }

    @Override
    public SimpleOverlayRenderer getFrontActiveOverlay() {
        return advanced ? Textures.HPCA_ACTIVE_COOLER_ACTIVE_OVERLAY : getFrontOverlay();
    }

    @Override
    public int getUpkeepEUt() {
        return advanced ? GTValues.VA[GTValues.IV] : 0;
    }

    @Override
    public boolean canBeDamaged() {
        return false;
    }

    @Override
    public int getCoolingAmount() {
        return advanced ? 2 : 1;
    }

    @Override
    public boolean isActiveCooler() {
        return advanced;
    }

    @Override
    public int getMaxCoolantPerTick() {
        return advanced ? 8 : 0;
    }

    @Override
    public String getTileName() {
        return getMetaFullName();
    }
}
