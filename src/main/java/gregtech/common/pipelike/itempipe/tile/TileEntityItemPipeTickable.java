package gregtech.common.pipelike.itempipe.tile;

import net.minecraft.util.ITickable;

public class TileEntityItemPipeTickable extends TileEntityItemPipe implements ITickable {

    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    private int transferredItems = 0;
    private long timer = 0;

    @Override
    public void update() {
        if(++timer == 1000000000) timer = 0;
        getCoverableImplementation().update();
        if (timer % 20 == 0) {
            transferredItems = 0;
        }
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

    public void transferItems(int amount) {
        transferredItems += amount;
    }

    public int getTransferredItems() {
        return transferredItems;
    }
}
