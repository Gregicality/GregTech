package gregtech.core.network.packets;

import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import gregtech.api.util.input.KeyBind;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class PacketKeyPressed implements IPacket, IServerExecutor {

    private IntList pressedKeys;

    public PacketKeyPressed() {/**/}

    public PacketKeyPressed(IntList pressedKeys) {
        this.pressedKeys = pressedKeys;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(pressedKeys.size());
        for (int key : pressedKeys) {
            buf.writeVarInt(key);
        }
    }

    @Override
    public void decode(PacketBuffer buf) {
        pressedKeys = new IntArrayList();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            pressedKeys.add(buf.readVarInt());
        }
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        KeyBind[] keyBinds = KeyBind.VALUES;
        for (int index : pressedKeys) {
            keyBinds[index].onKeyPressed(handler.player);
        }
    }
}
