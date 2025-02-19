package gregtech.common.covers;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.util.FacingPos;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.items.behaviors.CoverDigitalInterfaceWirelessPlaceBehaviour;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityCentralMonitor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoverDigitalInterfaceWireless extends CoverDigitalInterface {

    private BlockPos remote;

    public CoverDigitalInterfaceWireless(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                         @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public void setMode(MODE mode, int slot, EnumFacing spin) {}

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        if (this.remote != null) {
            tagCompound.setTag("cdiRemote", NBTUtil.createPosTag(this.remote));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.remote = tagCompound.hasKey("cdiRemote") ? NBTUtil.getPosFromTag(tagCompound.getCompoundTag("cdiRemote")) :
                null;
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer packetBuffer) {
        packetBuffer.writeBoolean(remote != null);
        if (remote != null) {
            packetBuffer.writeBlockPos(remote);
        }
        super.writeInitialSyncData(packetBuffer);
    }

    @Override
    public void readInitialSyncData(@NotNull AdvancedPacketBuffer packetBuffer) {
        if (packetBuffer.readBoolean()) {
            this.remote = packetBuffer.readBlockPos();
        }
        super.readInitialSyncData(packetBuffer);
    }

    @Override
    public void onAttachment(@NotNull CoverableView coverableView, @NotNull EnumFacing side,
                             @Nullable EntityPlayer player, @NotNull ItemStack itemStack) {
        remote = CoverDigitalInterfaceWirelessPlaceBehaviour.getRemotePos(itemStack);
    }

    @Override
    public void update() {
        super.update();
        if (remote != null && !isRemote() && getOffsetTimer() % 20 == 0) {
            TileEntity te = getWorld().getTileEntity(remote);
            if (te instanceof IGregTechTileEntity igtte &&
                    igtte.getMetaTileEntity() instanceof MetaTileEntityCentralMonitor monitor) {
                monitor.addRemoteCover(new FacingPos(getPos(), getAttachedSide()));
            }
        }
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        return EnumActionResult.SUCCESS;
    }

    @Override
    public @NotNull ItemStack getPickItem() {
        ItemStack drop = super.getPickItem();
        if (remote != null) {
            drop.setTagCompound(NBTUtil.createPosTag(remote));
        }
        return drop;
    }

    @Override
    public void renderCover(CCRenderState ccRenderState, Matrix4 translation, IVertexOperation[] ops, Cuboid6 cuboid6,
                            BlockRenderLayer blockRenderLayer) {
        Textures.COVER_INTERFACE_WIRELESS.renderSided(getAttachedSide(), cuboid6, ccRenderState, ops, translation);
    }
}
