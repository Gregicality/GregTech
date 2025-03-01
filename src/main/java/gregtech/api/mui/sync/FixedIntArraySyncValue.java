package gregtech.api.mui.sync;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Sync Value for an array with fixed length.
 * <p>
 * Does not create new arrays.
 */
public class FixedIntArraySyncValue extends ValueSyncHandler<int[]> {

    private final int[] cache;
    private final Supplier<int[]> getter;
    private final @Nullable Consumer<int[]> setter;

    public FixedIntArraySyncValue(@NotNull Supplier<int[]> getter, @Nullable Consumer<int[]> setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.get();
    }

    @Contract("null, _, null, _ -> fail")
    public FixedIntArraySyncValue(@Nullable Supplier<int[]> clientGetter, @Nullable Consumer<int[]> clientSetter,
                                  @Nullable Supplier<int[]> serverGetter, @Nullable Consumer<int[]> serverSetter) {
        if (clientGetter == null && serverGetter == null) {
            throw new NullPointerException("Client or server getter must not be null!");
        }
        if (NetworkUtils.isClient()) {
            this.getter = clientGetter != null ? clientGetter : serverGetter;
            this.setter = clientSetter != null ? clientSetter : serverSetter;
        } else {
            this.getter = serverGetter != null ? serverGetter : clientGetter;
            this.setter = serverSetter != null ? serverSetter : clientSetter;
        }
        this.cache = this.getter.get();
    }

    @Override
    public void setValue(int @NotNull [] value, boolean setSource, boolean sync) {
        if (value.length != cache.length) {
            throw new IllegalArgumentException("Incompatible array lengths");
        }
        System.arraycopy(value, 0, cache, 0, value.length);
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || !Arrays.equals(this.getter.get(), this.cache)) {
            setValue(this.getter.get(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void write(@NotNull PacketBuffer buffer) throws IOException {
        for (int i : cache) {
            buffer.writeVarInt(i);
        }
    }

    @Override
    public void read(@NotNull PacketBuffer buffer) throws IOException {
        for (int i = 0; i < cache.length; i++) {
            cache[i] = buffer.readVarInt();
        }
    }

    @Override
    public int[] getValue() {
        return this.cache;
    }

    public int getValue(int index) {
        return this.cache[index];
    }
}
