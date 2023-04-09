package gregtech.api.metatileentity;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public abstract class MTETrait {

    private static final Object2IntFunction<String> traitIds = new Object2IntOpenHashMap<>();
    private static int rollingNetworkId = 0;

    protected final MetaTileEntity metaTileEntity;
    private final int networkId;
    private final String name;

    /**
     * Create a new MTE trait.
     *
     * @param metaTileEntity the MTE to reference, and add the trait to
     */
    public MTETrait(@Nonnull MetaTileEntity metaTileEntity) {
        this.metaTileEntity = metaTileEntity;
        this.name = createName();

        if (!traitIds.containsKey(this.name)) {
            this.networkId = rollingNetworkId++;
            traitIds.put(this.name, this.networkId);
        } else {
            this.networkId = traitIds.getInt(this.name);
        }
        metaTileEntity.addMetaTileEntityTrait(this);
    }

    @Nonnull
    public MetaTileEntity getMetaTileEntity() {
        return metaTileEntity;
    }

    @Nonnull
    protected abstract String createName();

    /**
     * @return the name of the MTE Trait
     */
    @Nonnull
    public final String getName() {
        return this.name;
    }

    /**
     * @return the network ID of the MTE Trait
     */
    public final int getNetworkID() {
        return this.networkId;
    }

    public abstract <T> T getCapability(Capability<T> capability);

    public void onFrontFacingSet(EnumFacing newFrontFacing) {
    }

    public void update() {
    }

    @Nonnull
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    public void deserializeNBT(@Nonnull NBTTagCompound compound) {
    }

    public void writeInitialData(@Nonnull PacketBuffer buffer) {
    }

    public void receiveInitialData(@Nonnull PacketBuffer buffer) {
    }

    public void receiveCustomData(int id, @Nonnull PacketBuffer buffer) {
    }

    public final void writeCustomData(int id, @Nonnull Consumer<PacketBuffer> writer) {
        metaTileEntity.writeTraitData(this, id, writer);
    }

    @Override
    public String toString() {
        return "MTETrait{" +
                "metaTileEntity=" + metaTileEntity +
                ", networkId=" + networkId +
                ", name='" + name + '\'' +
                '}';
    }
}
