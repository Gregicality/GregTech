package gregtech.api.capability;

import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;

/**
 * For Item and Fluid handlers capable of notifying entities when
 * their contents change
 */
public interface INotifiableHandler {

    /**
     * Adds the notified handler to the notified list
     *
     * @param isExport boolean specifying if a handler is an output handler
     */

    default <T> void addToNotifiedList(MetaTileEntity metaTileEntity, T handler, boolean isExport) {
        if (metaTileEntity != null && metaTileEntity.isValid()) {
            if (isExport) {
                metaTileEntity.addNotifiedOutput(handler);
            } else {
                metaTileEntity.addNotifiedInput(handler);
            }
        }
    }

    /**
     * @param metaTileEntity MetaTileEntity to be notified
     */
    void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity);

    void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity);

    default int size() {
        if (this instanceof IFluidTank)
            return 1;
        else if (this instanceof IItemHandler handler)
            return handler.getSlots();
        else if (this instanceof IMultipleTankHandler tankHandler)
            return tankHandler.getTanks();
        return 0;
    }
}
