package gregtech.api.capability;

import gregtech.api.items.itemhandlers.LockableItemStackHandler;
import gregtech.api.nuclear.fission.IFissionFuelStats;
import gregtech.api.nuclear.fission.components.FuelRod;

import net.minecraft.item.ItemStack;

public interface IFuelRodHandler extends ILockableHandler<ItemStack> {

    IFissionFuelStats getFuel();

    void setFuel(IFissionFuelStats prop);

    IFissionFuelStats getPartialFuel();

    /**
     * Set the fuel type that's currently being processed by this specific handler.
     * 
     * @param prop The new fuel type.
     * @return true if the partial fuel changed.
     */
    boolean setPartialFuel(IFissionFuelStats prop);

    void setInternalFuelRod(FuelRod rod);

    LockableItemStackHandler getStackHandler();
}
