package gregtech.api.capability;

import net.minecraftforge.items.IItemHandler;

public interface IDualHandler {

    boolean hasFluidTanks();

    boolean hasItemHandlers();

    MultipleTankHandler getFluidTanks();

    IItemHandler getItemHandlers();
}
