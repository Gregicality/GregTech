package gregtech.api.metatileentity.multiblock.ui;

import io.netty.buffer.ByteBuf;

import java.math.BigInteger;

public interface UISyncer {

    boolean syncBoolean(boolean initial);

    int syncInt(int initial);

    long syncLong(long initial);

    String syncString(String initial);

    byte syncByte(byte initial);

    double syncDouble(double initial);

    float syncFloat(float initial);

    BigInteger syncBigInt(BigInteger initial);

    void readBuffer(ByteBuf buf);

    void writeBuffer(ByteBuf buf);

    boolean hasChanged();

    void clear();
}
