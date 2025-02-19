package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.capability.IControllable;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import appeng.api.AEApi;
import appeng.api.networking.GridFlags;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_ONLINE_STATUS;

public abstract class MetaTileEntityAEHostablePart<T extends IAEStack<T>> extends MetaTileEntityMultiblockNotifiablePart
                                                  implements IControllable {

    private final Class<? extends IStorageChannel<T>> storageChannel;
    private AENetworkProxy aeProxy;
    private int meUpdateTick;
    protected boolean isOnline;
    private boolean allowExtraConnections;
    protected boolean meStatusChanged = false;

    public MetaTileEntityAEHostablePart(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch,
                                        Class<? extends IStorageChannel<T>> storageChannel) {
        super(metaTileEntityId, tier, isExportHatch);
        this.meUpdateTick = 0;
        this.storageChannel = storageChannel;
        this.allowExtraConnections = false;
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            this.meUpdateTick++;
        }
    }

    /**
     * ME hatch will try to put its buffer back to me system when removal.
     * So there is no need to drop them.
     */
    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {}

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.writeInitialSyncData(buf);
        if (this.aeProxy != null) {
            buf.writeBoolean(true);
            NBTTagCompound proxy = new NBTTagCompound();
            this.aeProxy.writeToNBT(proxy);
            buf.writeCompoundTag(proxy);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeInt(this.meUpdateTick);
        buf.writeBoolean(this.isOnline);
        buf.writeBoolean(this.allowExtraConnections);
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            NBTTagCompound nbtTagCompound;
            try {
                nbtTagCompound = buf.readCompoundTag();
            } catch (IOException ignored) {
                nbtTagCompound = null;
            }

            if (this.aeProxy != null && nbtTagCompound != null) {
                this.aeProxy.readFromNBT(nbtTagCompound);
            }
        }
        this.meUpdateTick = buf.readInt();
        this.isOnline = buf.readBoolean();
        this.allowExtraConnections = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_ONLINE_STATUS) {
            boolean isOnline = buf.readBoolean();
            if (this.isOnline != isOnline) {
                this.isOnline = isOnline;
                scheduleRenderUpdate();
            }
        }
    }

    @NotNull
    @Override
    public AECableType getCableConnectionType(@NotNull AEPartLocation part) {
        if (part.getFacing() != this.frontFacing && !this.allowExtraConnections) {
            return AECableType.NONE;
        }
        return AECableType.SMART;
    }

    @Nullable
    @Override
    public AENetworkProxy getProxy() {
        if (this.aeProxy == null) {
            return this.aeProxy = this.createProxy();
        }
        if (!this.aeProxy.isReady() && this.getWorld() != null) {
            this.aeProxy.onReady();
        }
        return this.aeProxy;
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        updateConnectableSides();
    }

    @Override
    public void gridChanged() {}

    /**
     * Get the me network connection status, updating it if on serverside.
     * 
     * @return the updated status.
     */
    public boolean updateMEStatus() {
        if (!getWorld().isRemote) {
            boolean isOnline = this.aeProxy != null && this.aeProxy.isActive() && this.aeProxy.isPowered();
            if (this.isOnline != isOnline) {
                writeCustomData(UPDATE_ONLINE_STATUS, buf -> buf.writeBoolean(isOnline));
                this.isOnline = isOnline;
                this.meStatusChanged = true;
            } else {
                this.meStatusChanged = false;
            }
        }
        return this.isOnline;
    }

    protected boolean shouldSyncME() {
        return this.meUpdateTick % ConfigHolder.compat.ae2.updateIntervals == 0;
    }

    protected IActionSource getActionSource() {
        if (this.getHolder() instanceof IActionHost holder) {
            return new MachineSource(holder);
        }
        return new BaseActionSource();
    }

    @Nullable
    private AENetworkProxy createProxy() {
        if (this.getHolder() instanceof IGridProxyable holder) {
            AENetworkProxy proxy = new AENetworkProxy(holder, "mte_proxy", this.getStackForm(), true);
            proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            proxy.setIdlePowerUsage(ConfigHolder.compat.ae2.meHatchEnergyUsage);
            proxy.setValidSides(getConnectableSides());
            return proxy;
        }
        return null;
    }

    @NotNull
    protected IStorageChannel<T> getStorageChannel() {
        return AEApi.instance().storage().getStorageChannel(storageChannel);
    }

    @Nullable
    protected IMEMonitor<T> getMonitor() {
        AENetworkProxy proxy = getProxy();
        if (proxy == null) return null;

        IStorageChannel<T> channel = getStorageChannel();

        try {
            return proxy.getStorage().getInventory(channel);
        } catch (GridAccessException ignored) {
            return null;
        }
    }

    public EnumSet<EnumFacing> getConnectableSides() {
        return this.allowExtraConnections ? EnumSet.allOf(EnumFacing.class) : EnumSet.of(getFrontFacing());
    }

    public void updateConnectableSides() {
        if (this.aeProxy != null) {
            this.aeProxy.setValidSides(getConnectableSides());
        }
    }

    @Override
    public boolean onWireCutterClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                     CuboidRayTraceResult hitResult) {
        this.allowExtraConnections = !this.allowExtraConnections;
        updateConnectableSides();

        if (!getWorld().isRemote) {
            playerIn.sendStatusMessage(new TextComponentTranslation(this.allowExtraConnections ?
                    "gregtech.machine.me.extra_connections.enabled" : "gregtech.machine.me.extra_connections.disabled"),
                    true);
        }

        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("AllowExtraConnections", this.allowExtraConnections);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.allowExtraConnections = data.getBoolean("AllowExtraConnections");
    }
}
