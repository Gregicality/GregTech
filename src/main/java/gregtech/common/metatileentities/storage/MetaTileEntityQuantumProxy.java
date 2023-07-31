package gregtech.common.metatileentities.storage;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IDualHandler;
import gregtech.api.capability.IQuantumController;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityQuantumProxy extends MetaTileEntityQuantumStorage<IDualHandler> {

    IDualHandler handler = null;
    public MetaTileEntityQuantumProxy(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityQuantumProxy(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        // todo make a unique texture
        if (isConnected()) {
            Textures.ADVANCED_COMPUTER_CASING.render(renderState, translation, pipeline); // testing
        } else {
            Textures.SOLID_STEEL_CASING.render(renderState, translation, pipeline);
        }
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
    public void setConnected(IQuantumController controller) {
        super.setConnected(controller);
        this.handler = getController().getHandler();
    }

    @Override
    public void setDisconnected() {
        super.setDisconnected();
        this.handler = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (getController() == null && this.handler == null) return super.getCapability(capability, side);

        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) getTypeValue();
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) getTypeValue();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public Type getType() {
        return Type.EXTENDER;
    }

    @Override
    public IDualHandler getTypeValue() {
        return getController().getHandler();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.quantum_chest.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.quantum_storage_proxy.tooltip"));
    }
}
