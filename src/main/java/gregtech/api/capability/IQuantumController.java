package gregtech.api.capability;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

// ICapabilityProvider is needed because getCapability is called in the quantum proxy against this interface
public interface IQuantumController extends ICapabilityProvider {

    /**
     * Constructs the network upon placement and when storages are added/removed
     * <br />
     */
    void rebuildNetwork();

    /**
     * Return whether this storage block can connect. Can be used to implement a maximum distance from controller for
     * example.
     */
    boolean canConnect(IQuantumStorage<?> storage);

    BlockPos getPos();

    DualHandler getHandler();

    boolean isPowered();

    long getEnergyUsage();

    int getCount(IQuantumStorage.Type type);

    long getTypeEnergy(IQuantumStorage<?> storage);

    void onHandlerUpdate();
}
