package gregtech.api.metatileentity.multiblock.ui;

import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;
import com.cleanroommc.modularui.utils.serialization.IEquals;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Collection;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

public interface UISyncer {

    IByteBufAdapter<BigInteger> BIG_INT = ByteBufAdapters.makeAdapter(
            buffer -> new BigInteger(buffer.readByteArray()),
            (buffer, value) -> buffer.writeByteArray(value.toByteArray()),
            IEquals.defaultTester());

    /**
     * Calls the supplier server side only so there's no potential NPEs for the client
     *
     * @param initial supplier to be called on the server
     * @return synced value
     */
    boolean syncBoolean(@NotNull BooleanSupplier initial);

    /**
     * Calls the supplier server side only so there's no potential NPEs for the client
     *
     * @param initial supplier to be called on the server
     * @return synced value
     */
    int syncInt(@NotNull IntSupplier initial);

    /**
     * Calls the supplier server side only so there's no potential NPEs for the client
     *
     * @param initial supplier to be called on the server
     * @return synced value
     */
    long syncLong(@NotNull LongSupplier initial);

    /**
     * Calls the supplier server side only so there's no potential NPEs for the client
     *
     * @param initial supplier to be called on the server
     * @return synced value
     */
    byte syncByte(@NotNull ByteSupplier initial);

    /**
     * Calls the supplier server side only so there's no potential NPEs for the client
     *
     * @param initial supplier to be called on the server
     * @return synced value
     */
    double syncDouble(@NotNull DoubleSupplier initial);

    /**
     * Calls the supplier server side only so there's no potential NPEs for the client
     *
     * @param initial supplier to be called on the server
     * @return synced value
     */
    float syncFloat(@NotNull FloatSupplier initial);

    default boolean syncBoolean(boolean initial) {
        return syncBoolean(() -> initial);
    }

    default int syncInt(int initial) {
        return syncInt(() -> initial);
    }

    default long syncLong(long initial) {
        return syncLong(() -> initial);
    }

    default byte syncByte(byte initial) {
        return syncByte(() -> initial);
    }

    default double syncDouble(double initial) {
        return syncDouble(() -> initial);
    }

    default float syncFloat(float initial) {
        return syncFloat(() -> initial);
    }

    default @NotNull String syncString(@NotNull String initial) {
        return syncObject(initial, ByteBufAdapters.STRING);
    }

    default BigInteger syncBigInt(BigInteger initial) {
        return syncObject(initial, BIG_INT);
    }

    <T> T syncObject(T initial, IByteBufSerializer<T> serializer, IByteBufDeserializer<T> deserializer);

    default <T> T syncObject(T initial, IByteBufAdapter<T> adapter) {
        return syncObject(initial, adapter, adapter);
    }

    <T, C extends Collection<T>> C syncCollection(C initial,
                                                  IByteBufSerializer<T> serializer,
                                                  IByteBufDeserializer<T> deserializer);

    default <T, C extends Collection<T>> C syncCollection(C initial, IByteBufAdapter<T> adapter) {
        return syncCollection(initial, adapter, adapter);
    }

    <T> T[] syncArray(T[] initial, IByteBufSerializer<T> serializer, IByteBufDeserializer<T> deserializer);

    default <T> T[] syncArray(T[] initial, IByteBufAdapter<T> adapter) {
        return syncArray(initial, adapter, adapter);
    }

    interface ByteSupplier {

        byte getByte();
    }

    interface FloatSupplier {

        float getFloat();
    }
}
