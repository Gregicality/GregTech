package gregtech.api.graphnet.logic;

import net.minecraft.nbt.NBTTagLong;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.Contract;

public abstract class AbstractLongLogicData<T extends AbstractLongLogicData<T>> implements INetLogicEntry<T, NBTTagLong> {

    private long value;

    public T getWith(long value) {
        return getNew().setValue(value);
    }

    @Contract("_ -> this")
    public T setValue(long value) {
        this.value = value;
        return (T) this;
    }

    public long getValue() {
        return this.value;
    }

    @Override
    public NBTTagLong serializeNBT() {
        return new NBTTagLong(this.value);
    }

    @Override
    public void deserializeNBT(NBTTagLong nbt) {
        this.value = nbt.getLong();
    }

    @Override
    public void encode(PacketBuffer buf, boolean fullChange) {
        buf.writeVarLong(this.value);
    }

    @Override
    public void decode(PacketBuffer buf, boolean fullChange) {
        this.value = buf.readVarLong();
    }
}