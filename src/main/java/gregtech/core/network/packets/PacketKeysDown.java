package gregtech.core.network.packets;

import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import gregtech.api.util.input.KeyBind;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;

public class PacketKeysDown implements IPacket, IServerExecutor {

    private Int2BooleanMap updateKeys;

    @SuppressWarnings("unused")
    public PacketKeysDown() {/**/}

    public PacketKeysDown(Int2BooleanMap updateKeys) {
        this.updateKeys = updateKeys;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(updateKeys.size());
        for (var entry : updateKeys.int2BooleanEntrySet()) {
            buf.writeVarInt(entry.getIntKey());
            buf.writeBoolean(entry.getBooleanValue());
        }
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.updateKeys = new Int2BooleanOpenHashMap();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            updateKeys.put(buf.readVarInt(), buf.readBoolean());
        }
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        KeyBind[] keybinds = KeyBind.VALUES;
        for (var entry : updateKeys.int2BooleanEntrySet()) {
            keybinds[entry.getIntKey()].updateKeyDown(entry.getBooleanValue(), handler.player);
        }
    }
}
