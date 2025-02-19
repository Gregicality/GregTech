package gregtech.api.metatileentity.interfaces;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverableView;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.util.GTLog;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Functions which sync data between the Server and Client sides in a TileEntity.
 */
public interface ISyncedTileEntity {

    Consumer<AdvancedPacketBuffer> NO_OP = buf -> {};

    /**
     * Used to sync data from Server -> Client.
     * Called during initial loading of the chunk or when many blocks change at once.
     * <p>
     * Data is received in {@link #receiveInitialSyncData(AdvancedPacketBuffer)}.
     * <p>
     * Typically used to send server side data to the client on initial chunk loading.
     * <p>
     * <em>Should be called automatically</em>.
     * <p>
     * This method is called <strong>Server-Side</strong>.
     * <p>
     * Equivalent to {@link TileEntity#getUpdateTag}.
     *
     * @deprecated Replaced by {@link #writeInitialSyncData(AdvancedPacketBuffer)} for improved capability.
     * @param buf the buffer to write data to
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    default void writeInitialSyncData(@NotNull PacketBuffer buf) {}

    /**
     * Used to sync data from Server -> Client.
     * Called during initial loading of the chunk or when many blocks change at once.
     * <p>
     * Data is received in {@link #receiveInitialSyncData(AdvancedPacketBuffer)}.
     * <p>
     * Typically used to send server side data to the client on initial chunk loading.
     * <p>
     * <em>Should be called automatically</em>.
     * <p>
     * This method is called <strong>Server-Side</strong>.
     * <p>
     * Equivalent to {@link TileEntity#getUpdateTag}.
     *
     * @apiNote will become non-default once {@link #writeInitialSyncData(PacketBuffer)} is removed.
     * @param buf the buffer to write data to
     */
    default void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        writeInitialSyncData((PacketBuffer) buf);
    }

    /**
     * Used to receive Server -> Client sync data.
     * Called during initial loading of the chunk or when many blocks change at once.
     * <p>
     * Data sent is from {@link #writeInitialSyncData(AdvancedPacketBuffer)}.
     * <p>
     * Typically used to receive server side data on initial chunk loading.
     * <p>
     * <em>Should be called automatically</em>.
     * <p>
     * This method is called <strong>Client-Side</strong>.
     * <p>
     * Equivalent to {@link TileEntity#handleUpdateTag}.
     *
     * @deprecated Replaced by {@link #receiveInitialSyncData(AdvancedPacketBuffer)} for improved capability.
     * @param buf the buffer to read data from
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    default void receiveInitialSyncData(@NotNull PacketBuffer buf) {}

    /**
     * Used to receive Server -> Client sync data.
     * Called during initial loading of the chunk or when many blocks change at once.
     * <p>
     * Data sent is from {@link #writeInitialSyncData(AdvancedPacketBuffer)}.
     * <p>
     * Typically used to receive server side data on initial chunk loading.
     * <p>
     * <em>Should be called automatically</em>.
     * <p>
     * This method is called <strong>Client-Side</strong>.
     * <p>
     * Equivalent to {@link TileEntity#handleUpdateTag}.
     *
     * @apiNote will become non-default once {@link #receiveInitialSyncData(PacketBuffer)} is removed.
     * @param buf the buffer to read data from
     */
    default void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        receiveInitialSyncData((PacketBuffer) buf);
    }

    /**
     * Used to send an anonymous Server -> Client packet.
     * Call to build up the packet to send to the client when it is re-synced.
     * <p>
     * Data is received in {@link #receiveCustomData(int, AdvancedPacketBuffer)};
     * <p>
     * Typically used to signal to the client that a rendering update is needed
     * when sending a server-side state update.
     * <p>
     * <em>Should be called manually</em>.
     * <p>
     * This method is called <strong>Server-Side</strong>.
     * <p>
     * Equivalent to {@link TileEntity#getUpdatePacket}
     *
     * @param discriminator the discriminator determining the packet sent.
     * @param dataWriter    a consumer which writes packet data to a buffer.
     * @see GregtechDataCodes
     */
    void writeCustomData(int discriminator, @NotNull Consumer<@NotNull AdvancedPacketBuffer> dataWriter);

    /**
     * Used to send an empty anonymous Server -> Client packet.
     * <p>
     * Data is received in {@link #receiveCustomData(int, AdvancedPacketBuffer)};
     * <p>
     * Typically used to signal to the client that a rendering update is needed
     * when sending a server-side state update.
     * <p>
     * <em>Should be called manually</em>.
     * <p>
     * This method is called <strong>Server-Side</strong>.
     * <p>
     * Equivalent to {@link TileEntity#getUpdatePacket}
     *
     * @param discriminator the discriminator determining the packet sent.
     * @see GregtechDataCodes
     */
    default void writeCustomData(int discriminator) {
        writeCustomData(discriminator, NO_OP);
    }

    /**
     * Used to receive an anonymous Server -> Client packet.
     * Called when receiving a packet for the location this TileEntity is currently in.
     * <p>
     * Data is sent with {@link #writeCustomData(int, Consumer)}.
     * <p>
     * Typically used to perform a rendering update when receiving server-side state changes.
     * <p>
     * <em>Should be called automatically</em>.
     * <p>
     * This method is called <strong>Client-Side</strong>.
     * <p>
     * Equivalent to {@link TileEntity#onDataPacket}
     *
     * @param discriminator the discriminator determining the packet sent.
     * @param buf           the buffer containing the packet data.
     * @see GregtechDataCodes
     */
    void receiveCustomData(int discriminator, @NotNull AdvancedPacketBuffer buf);

    static void checkData(@NotNull ByteBuf buf, Object tracked) {
        if (buf.readableBytes() != 0) {
            if (!(buf instanceof AdvancedPacketBuffer adv) || adv.getDatacodes().isEmpty()) {
                GTLog.logger.error("Class {} failed to finish reading initialSyncData with {} bytes remaining",
                        stringify(tracked), buf.readableBytes());
            } else {
                GTLog.logger.error(
                        "Class {} failed to finish reading receiveCustomData at code path [{}] with {} bytes remaining",
                        stringify(tracked), getCodePath(adv.getDatacodes()), buf.readableBytes());
            }
        }
    }

    static String stringify(Object obj) {
        if (obj instanceof IGregTechTileEntity gtte && gtte.getMetaTileEntity() != null)
            obj = gtte.getMetaTileEntity();

        StringBuilder builder = new StringBuilder(obj.getClass().getSimpleName());

        BlockPos pos = null;
        if (obj instanceof TileEntity tileEntity) {
            pos = tileEntity.getPos(); // TE pos
        } else if (obj instanceof CoverableView view) {
            pos = view.getPos(); // MTE pos
        } else if (obj instanceof Cover cover) {
            pos = cover.getPos(); // Cover pos and side
            builder.append("[side=").append(cover.getAttachedSide()).append("]");
        }

        if (pos != null) builder.append(" @ {")
                .append(pos.getX()).append("X, ")
                .append(pos.getY()).append("Y, ")
                .append(pos.getZ()).append("Z}");

        return builder.toString();
    }

    static String getCodePath(IntList datacodes) {
        var builder = new StringBuilder();
        for (int i = 0; i < datacodes.size(); i++) {
            builder.append(GregtechDataCodes.getNameFor(datacodes.get(i)));
            if (i < datacodes.size() - 1) builder.append(" > ");
        }
        return builder.toString();
    }
}
