package gregtech.api.mui.sync;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BigIntegerSyncValue extends ValueSyncHandler<BigInteger> implements IStringSyncValue<BigInteger> {

    private BigInteger cache = BigInteger.ZERO;
    private final Supplier<BigInteger> getter;
    private final @Nullable Consumer<BigInteger> setter;

    public BigIntegerSyncValue(@NotNull Supplier<@NotNull BigInteger> getter,
                               @Nullable Consumer<@NotNull BigInteger> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Contract("null, _, null, _ -> fail")
    public BigIntegerSyncValue(@Nullable Supplier<@NotNull BigInteger> clientGetter,
                               @Nullable Consumer<@NotNull BigInteger> clientSetter,
                               @Nullable Supplier<@NotNull BigInteger> serverGetter,
                               @Nullable Consumer<@NotNull BigInteger> serverSetter) {
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
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setValue(new BigInteger(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return cache.toString();
    }

    @Override
    public void setValue(BigInteger value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (this.getter != null && (isFirstSync || !Objects.equals(this.getter.get(), this.cache))) {
            setValue(this.getter.get(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void write(@NotNull PacketBuffer buffer) throws IOException {
        buffer.writeByteArray(getValue().toByteArray());
    }

    @Override
    public void read(@NotNull PacketBuffer buffer) throws IOException {
        setValue(new BigInteger(buffer.readByteArray()), true, false);
    }

    @Override
    public BigInteger getValue() {
        return this.cache;
    }
}
