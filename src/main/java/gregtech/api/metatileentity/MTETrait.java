package gregtech.api.metatileentity;

import gregtech.api.metatileentity.interfaces.ISyncedTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class MTETrait implements ISyncedTileEntity {

    private static final Object2IntFunction<String> traitIds = new Object2IntOpenHashMap<>();
    private static final int NO_NETWORK_ID = -1;

    private static int rollingNetworkId = 0;

    static {
        traitIds.defaultReturnValue(NO_NETWORK_ID);
    }

    protected final MetaTileEntity metaTileEntity;
    private final int networkId;

    /**
     * Create a new MTE trait.
     *
     * @param metaTileEntity the MTE to reference, and add the trait to
     */
    public MTETrait(@NotNull MetaTileEntity metaTileEntity) {
        this.metaTileEntity = metaTileEntity;

        final String traitName = getName();
        int networkId = traitIds.getInt(traitName);
        if (networkId == NO_NETWORK_ID) {
            networkId = rollingNetworkId++;
            traitIds.put(traitName, networkId);
        }
        this.networkId = networkId;
        metaTileEntity.addMetaTileEntityTrait(this);
    }

    @NotNull
    public MetaTileEntity getMetaTileEntity() {
        return metaTileEntity;
    }

    /**
     * @return the name of the MTE Trait
     */
    @NotNull
    public abstract String getName();

    /**
     * @return the network ID of the MTE Trait
     */
    public final int getNetworkID() {
        return this.networkId;
    }

    public abstract <T> T getCapability(Capability<T> capability);

    public void onFrontFacingSet(EnumFacing newFrontFacing) {}

    public void update() {}

    @NotNull
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    public void deserializeNBT(@NotNull NBTTagCompound compound) {}

    @Override
    public void receiveCustomData(int discriminator, @NotNull AdvancedPacketBuffer buf) {}

    @Override
    public final void writeCustomData(int discriminator, @NotNull Consumer<@NotNull AdvancedPacketBuffer> dataWriter) {
        metaTileEntity.writeTraitData(this, discriminator, dataWriter);
    }

    @Override
    public String toString() {
        return "MTETrait{" +
                "metaTileEntity=" + metaTileEntity +
                ", networkId=" + networkId +
                ", name='" + getName() + '\'' +
                '}';
    }
}
