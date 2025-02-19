package gregtech.common.covers;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.cover.IFacadeCover;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.handler.FacadeRenderer;
import gregtech.common.covers.facade.FacadeHelper;
import gregtech.common.items.behaviors.FacadeItem;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public class CoverFacade extends CoverBase implements IFacadeCover {

    private ItemStack facadeStack = ItemStack.EMPTY;
    private IBlockState facadeState = Blocks.STONE.getDefaultState();

    public CoverFacade(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                       @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    public void setFacadeStack(@NotNull ItemStack facadeStack) {
        this.facadeStack = facadeStack.copy();
        writeCustomData(GregtechDataCodes.UPDATE_FACADE_STACK, buf -> buf.writeItemStack(this.facadeStack));
        updateFacadeState();
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return true;
    }

    @Override
    public void onAttachment(@NotNull CoverableView coverableView, @NotNull EnumFacing side,
                             @Nullable EntityPlayer player, @NotNull ItemStack itemStack) {
        super.onAttachment(coverableView, side, player, itemStack);
        setFacadeStack(FacadeItem.getFacadeStack(itemStack));
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline,
                            @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        BlockRenderLayer oldLayer = MinecraftForgeClient.getRenderLayer();
        ForgeHooksClient.setRenderLayer(layer);
        FacadeRenderer.renderBlockCover(renderState, translation, getCoverableView().getWorld(),
                getCoverableView().getPos(), getAttachedSide().getIndex(), facadeState, plateBox, layer);
        ForgeHooksClient.setRenderLayer(oldLayer);
    }

    @Override
    public boolean canRenderInLayer(@NotNull BlockRenderLayer renderLayer) {
        return true;
    }

    @Override
    public IBlockState getVisualState() {
        return facadeState;
    }

    @Override
    public @NotNull ItemStack getPickItem() {
        ItemStack dropStack = getDefinition().getDropItemStack();
        FacadeItem.setFacadeStack(dropStack, facadeStack);
        return dropStack;
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeShort(Item.getIdFromItem(facadeStack.getItem()));
        packetBuffer.writeShort(Items.FEATHER.getDamage(facadeStack));
    }

    @Override
    public void readInitialSyncData(@NotNull AdvancedPacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        Item item = Item.getItemById(packetBuffer.readShort());
        int itemDamage = packetBuffer.readShort();
        this.facadeStack = new ItemStack(item, 1, itemDamage);
        updateFacadeState();
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setTag("Facade", facadeStack.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.facadeStack = new ItemStack(tagCompound.getCompoundTag("Facade"));
        this.updateFacadeState();
    }

    @Override
    public void readCustomData(int discriminator, @NotNull PacketBuffer buf) {
        super.readCustomData(discriminator, buf);
        if (discriminator == GregtechDataCodes.UPDATE_FACADE_STACK) {
            try {
                this.facadeStack = buf.readItemStack();
            } catch (IOException e) {
                GTLog.logger.error("Error reading facade stack from network", e);
                this.facadeStack = new ItemStack(Objects.requireNonNull(Blocks.STONE));
                return;
            }
            updateFacadeState();
        }
    }

    @Override
    public boolean canPipePassThrough() {
        return true;
    }

    private void updateFacadeState() {
        this.facadeState = FacadeHelper.lookupBlockForItem(facadeStack);
        // called during world load, where world can be null
        if (getWorld() != null && !getWorld().isRemote) {
            scheduleRenderUpdate();
        }
    }

    @Override
    public boolean shouldAutoConnectToPipes() {
        return false;
    }

    @Override
    public boolean canRenderBackside() {
        return false;
    }

    @Override
    public void renderCoverPlate(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                                 IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox,
                                 @NotNull BlockRenderLayer layer) {}
}
