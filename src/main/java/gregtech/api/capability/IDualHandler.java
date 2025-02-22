package gregtech.api.capability;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IDualHandler extends IItemHandlerModifiable, IMultipleTankHandler {

    boolean hasFluidTanks();

    boolean hasItemHandlers();

    IMultipleTankHandler getDelegateTank();

    IItemHandler getDelegatItemHandler();
}
