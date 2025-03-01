package gregtech.common.metatileentities.storage;

import gregtech.api.capability.DualHandler;
import gregtech.api.capability.IQuantumController;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public class MetaTileEntityQuantumProxy extends MetaTileEntityQuantumStorage<DualHandler> {

    public MetaTileEntityQuantumProxy(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityQuantumProxy(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        var newPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        if (isConnected() && getQuantumController().isPowered()) {
            Textures.QUANTUM_PROXY_ACTIVE.render(renderState, translation, newPipeline);
        } else {
            Textures.QUANTUM_PROXY_INACTIVE.render(renderState, translation, newPipeline);
        }
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.QUANTUM_PROXY_INACTIVE.getParticleSprite(), getPaintingColorForRendering());
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
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
                capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            var controller = getPoweredController();
            if (controller != null)
                return controller.getCapability(capability, side);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void setDisconnected() {
        super.setDisconnected();
        notifyBlockUpdate();
    }

    @Override
    public Type getType() {
        return Type.PROXY;
    }

    @Override
    public DualHandler getTypeValue() {
        var controller = getPoweredController();
        if (controller == null) return null;
        return controller.getHandler();
    }

    @Nullable
    private IQuantumController getPoweredController() {
        if (!isConnected()) return null;
        var controller = getQuantumController();
        if (controller == null || !controller.isPowered()) return null;
        return controller;
    }
}
