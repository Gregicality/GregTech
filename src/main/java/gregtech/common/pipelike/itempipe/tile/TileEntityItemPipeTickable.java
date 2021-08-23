package gregtech.common.pipelike.itempipe.tile;

import net.minecraft.util.ITickable;

public class TileEntityItemPipeTickable extends TileEntityItemPipe implements ITickable {

    private int transferredItems = 0;
    private long timer = 0;

    @Override
    public void update() {
        getCoverable().update();
        if (++timer % 20 == 0) {
            transferredItems = 0;
        }
    }

    public void transferItems(int amount) {
        transferredItems += amount;
    }

    public int getTransferredItems() {
        return transferredItems;
    }
}
