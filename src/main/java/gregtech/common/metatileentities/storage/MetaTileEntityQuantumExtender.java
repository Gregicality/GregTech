package gregtech.common.metatileentities.storage;

import gregtech.api.capability.DualHandler;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class MetaTileEntityQuantumExtender extends MetaTileEntityQuantumStorage<DualHandler> {

    public MetaTileEntityQuantumExtender(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityQuantumExtender(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        var newPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        if (isConnected() && getQuantumController().isPowered()) {
            Textures.QUANTUM_EXTENDER_ACTIVE.render(renderState, translation, newPipeline);
        } else {
            Textures.QUANTUM_EXTENDER.render(renderState, translation, newPipeline);
        }
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.QUANTUM_EXTENDER.getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public boolean hasFrontFacing() {
        return false;
    }

    @Override
    public Type getType() {
        return Type.EXTENDER;
    }

    @Override
    public DualHandler getTypeValue() {
        return null;
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.REMOVE_CONTROLLER) scheduleRenderUpdate();
    }
}
