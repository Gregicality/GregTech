package gregtech.api.metatileentity;

import gregtech.api.block.BlockStateTileEntity;
import gregtech.api.metatileentity.interfaces.ISyncedTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.network.PacketDataList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;

import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;

public abstract class SyncedTileEntityBase extends BlockStateTileEntity implements ISyncedTileEntity {

    private final PacketDataList updates = new PacketDataList();

    public @Nullable TileEntity getNeighbor(EnumFacing facing) {
        if (world == null || pos == null) return null;
        return world.getTileEntity(pos.offset(facing));
    }

    @Override
    public final void writeCustomData(int discriminator, @NotNull Consumer<@NotNull AdvancedPacketBuffer> dataWriter) {
        AdvancedPacketBuffer buf = new AdvancedPacketBuffer(Unpooled::buffer);
        dataWriter.accept(buf);
        byte[] updateData = Arrays.copyOfRange(buf.array(), 0, buf.writerIndex());
        this.updates.add(discriminator, updateData);
        notifyWorld();
    }

    /**
     * Adds all data packets from another synced tile entity. Useful when the old tile is replaced with a new one.
     *
     * @param syncedTileEntityBase other synced tile entity
     */
    public void addPacketsFrom(SyncedTileEntityBase syncedTileEntityBase) {
        if (this == syncedTileEntityBase || syncedTileEntityBase.updates.isEmpty()) return;
        boolean wasEmpty = this.updates.isEmpty();
        this.updates.addAll(syncedTileEntityBase.updates);
        syncedTileEntityBase.updates.clear();
        if (wasEmpty) notifyWorld(); // if the data is not empty we already notified the world
    }

    private void notifyWorld() {
        @SuppressWarnings("deprecation")
        IBlockState blockState = getBlockType().getStateFromMeta(getBlockMetadata());
        world.notifyBlockUpdate(getPos(), blockState, blockState, 0);
    }

    @Override
    public final @Nullable SPacketUpdateTileEntity getUpdatePacket() {
        if (this.updates.isEmpty()) {
            return null;
        }
        NBTTagCompound updateTag = new NBTTagCompound();
        updateTag.setTag("d", this.updates.dumpToNbt());
        return new SPacketUpdateTileEntity(getPos(), 0, updateTag);
    }

    @Override
    public final void onDataPacket(@NotNull NetworkManager net, @NotNull SPacketUpdateTileEntity pkt) {
        NBTTagCompound updateTag = pkt.getNbtCompound();
        NBTTagList listTag = updateTag.getTagList("d", Constants.NBT.TAG_COMPOUND);
        for (NBTBase entryBase : listTag) {
            NBTTagCompound entryTag = (NBTTagCompound) entryBase;
            for (String discriminatorKey : entryTag.getKeySet()) {
                AdvancedPacketBuffer buf = new AdvancedPacketBuffer(
                        Unpooled.copiedBuffer(entryTag.getByteArray(discriminatorKey)), Unpooled::buffer);
                int dataId = Integer.parseInt(discriminatorKey);
                buf.getDatacodes().add(dataId);
                receiveCustomData(dataId, buf);
                ISyncedTileEntity.checkData(buf, this);
            }
        }
    }

    @Override
    public final @NotNull NBTTagCompound getUpdateTag() {
        NBTTagCompound updateTag = super.getUpdateTag();
        AdvancedPacketBuffer buffer = new AdvancedPacketBuffer(Unpooled::buffer);
        writeInitialSyncData(buffer);
        updateTag.setByteArray("d", Arrays.copyOfRange(buffer.array(), 0, buffer.writerIndex()));
        return updateTag;
    }

    @Override
    public final void handleUpdateTag(@NotNull NBTTagCompound tag) {
        super.readFromNBT(tag); // deserializes Forge data and capabilities
        byte[] updateData = tag.getByteArray("d");
        AdvancedPacketBuffer buffer = new AdvancedPacketBuffer(Unpooled.copiedBuffer(updateData), Unpooled::buffer);
        receiveInitialSyncData(buffer);
        ISyncedTileEntity.checkData(buffer, this);
    }
}
