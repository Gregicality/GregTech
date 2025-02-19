package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.ISyncedTileEntity;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityFluidDrill;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

public class FluidDrillLogic {

    public static final int MAX_PROGRESS = 20;

    private int progressTime = 0;

    private final MetaTileEntityFluidDrill metaTileEntity;

    private boolean isActive;
    private boolean isWorkingEnabled = true;
    private boolean wasActiveAndNeedsUpdate;
    private boolean isDone = false;
    protected boolean isInventoryFull;

    private boolean hasNotEnoughEnergy;

    private Fluid veinFluid;

    public FluidDrillLogic(MetaTileEntityFluidDrill metaTileEntity) {
        this.metaTileEntity = metaTileEntity;
        this.veinFluid = null;
    }

    /**
     * Performs the actual drilling
     * Call this method every tick in update
     */
    public void performDrilling() {
        if (metaTileEntity.getWorld().isRemote) return;

        // if we have no fluid, try to get a new one
        if (veinFluid == null)
            if (!acquireNewFluid())
                return; // stop if we still have no fluid

        // drills that cannot work do nothing
        if (!this.isWorkingEnabled)
            return;

        // check if drilling is possible
        if (!checkCanDrain())
            return;

        // if the inventory is not full, drain energy etc. from the drill
        // the storages have already been checked earlier
        if (!isInventoryFull) {
            // actually drain the energy
            consumeEnergy(false);

            // since energy is being consumed the rig is now active
            if (!this.isActive)
                setActive(true);
        } else {
            // the rig cannot drain, therefore it is inactive
            if (this.isActive)
                setActive(false);
            return;
        }

        // increase progress
        progressTime++;
        if (progressTime % MAX_PROGRESS != 0)
            return;
        progressTime = 0;

        int amount = getFluidToProduce();

        if (metaTileEntity.fillTanks(new FluidStack(veinFluid, amount), true)) {
            metaTileEntity.fillTanks(new FluidStack(veinFluid, amount), false);
            depleteVein();
        } else {
            isInventoryFull = true;
            setActive(false);
            setWasActiveAndNeedsUpdate(true);
        }
    }

    protected boolean consumeEnergy(boolean simulate) {
        return metaTileEntity.drainEnergy(simulate);
    }

    private boolean acquireNewFluid() {
        this.veinFluid = BedrockFluidVeinHandler.getFluidInChunk(metaTileEntity.getWorld(), getChunkX(), getChunkZ());
        return this.veinFluid != null;
    }

    public Fluid getDrilledFluid() {
        return veinFluid;
    }

    protected void depleteVein() {
        int chance = metaTileEntity.getDepletionChance();

        // chance to deplete based on the rig
        if (chance == 1 || GTValues.RNG.nextInt(chance) == 0)
            BedrockFluidVeinHandler.depleteVein(metaTileEntity.getWorld(), getChunkX(), getChunkZ(), 0, false);
    }

    public int getFluidToProduce() {
        int depletedYield = BedrockFluidVeinHandler.getDepletedFluidYield(metaTileEntity.getWorld(), getChunkX(),
                getChunkZ());
        int regularYield = BedrockFluidVeinHandler.getFluidYield(metaTileEntity.getWorld(), getChunkX(), getChunkZ());
        int remainingOperations = BedrockFluidVeinHandler.getOperationsRemaining(metaTileEntity.getWorld(), getChunkX(),
                getChunkZ());

        int produced = Math.max(depletedYield,
                regularYield * remainingOperations / BedrockFluidVeinHandler.MAXIMUM_VEIN_OPERATIONS);
        produced *= metaTileEntity.getRigMultiplier();

        // Overclocks produce 50% more fluid
        if (isOverclocked()) {
            produced = produced * 3 / 2;
        }
        return produced;
    }

    /**
     *
     * @return true if the rig is able to drain, else false
     */
    protected boolean checkCanDrain() {
        if (!consumeEnergy(true)) {
            if (progressTime >= 2) {
                if (ConfigHolder.machines.recipeProgressLowEnergy)
                    this.progressTime = 1;
                else
                    this.progressTime = Math.max(1, progressTime - 2);

                hasNotEnoughEnergy = true;
            }
            return false;
        }

        if (this.hasNotEnoughEnergy &&
                metaTileEntity.getEnergyInputPerSecond() > 19L * GTValues.VA[metaTileEntity.getEnergyTier()]) {
            this.hasNotEnoughEnergy = false;
        }

        if (metaTileEntity.fillTanks(new FluidStack(veinFluid, getFluidToProduce()), true)) {
            this.isInventoryFull = false;
            return true;
        }
        this.isInventoryFull = true;

        if (isActive()) {
            setActive(false);
            setWasActiveAndNeedsUpdate(true);
        }
        return false;
    }

    public int getChunkX() {
        return Math.floorDiv(metaTileEntity.getPos().getX(), 16);
    }

    public int getChunkZ() {
        return Math.floorDiv(metaTileEntity.getPos().getZ(), 16);
    }

    /**
     *
     * @return true if the rig is active
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     *
     * @param active the new state of the rig's activity: true to change to active, else false
     */
    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            this.metaTileEntity.markDirty();
            if (metaTileEntity.getWorld() != null && !metaTileEntity.getWorld().isRemote) {
                this.metaTileEntity.writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    /**
     *
     * @param isWorkingEnabled the new state of the rig's ability to work: true to change to enabled, else false
     */
    public void setWorkingEnabled(boolean isWorkingEnabled) {
        if (this.isWorkingEnabled != isWorkingEnabled) {
            this.isWorkingEnabled = isWorkingEnabled;
            metaTileEntity.markDirty();
            if (metaTileEntity.getWorld() != null && !metaTileEntity.getWorld().isRemote) {
                this.metaTileEntity.writeCustomData(GregtechDataCodes.WORKING_ENABLED,
                        buf -> buf.writeBoolean(isWorkingEnabled));
            }
        }
    }

    /**
     *
     * @return whether working is enabled for the logic
     */
    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    /**
     *
     * @return whether the rig is currently working
     */
    public boolean isWorking() {
        return isActive && !hasNotEnoughEnergy && isWorkingEnabled;
    }

    /**
     *
     * @return the current progress towards producing fluid of the rig
     */
    public int getProgressTime() {
        return this.progressTime;
    }

    public double getProgressPercent() {
        return getProgressTime() * 1.0 / MAX_PROGRESS;
    }

    protected boolean isOverclocked() {
        return metaTileEntity.getEnergyTier() > metaTileEntity.getTier();
    }

    /**
     *
     * @return whether the inventory is full
     */
    public boolean isInventoryFull() {
        return this.isInventoryFull;
    }

    /**
     * writes all needed values to NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#writeToNBT(NBTTagCompound)} method
     */
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        data.setBoolean("isActive", this.isActive);
        data.setBoolean("isWorkingEnabled", this.isWorkingEnabled);
        data.setBoolean("wasActiveAndNeedsUpdate", this.wasActiveAndNeedsUpdate);
        data.setBoolean("isDone", isDone);
        data.setInteger("progressTime", progressTime);
        data.setBoolean("isInventoryFull", isInventoryFull);
        return data;
    }

    /**
     * reads all needed values from NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#readFromNBT(NBTTagCompound)}
     * method
     */
    public void readFromNBT(@NotNull NBTTagCompound data) {
        this.isActive = data.getBoolean("isActive");
        this.isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        this.wasActiveAndNeedsUpdate = data.getBoolean("wasActiveAndNeedsUpdate");
        this.isDone = data.getBoolean("isDone");
        this.progressTime = data.getInteger("progressTime");
        this.isInventoryFull = data.getBoolean("isInventoryFull");
    }

    /**
     * writes all needed values to InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's
     * {@link ISyncedTileEntity#writeInitialSyncData(gregtech.api.network.AdvancedPacketBuffer)} method
     */
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.isWorkingEnabled);
        buf.writeBoolean(this.wasActiveAndNeedsUpdate);
        buf.writeInt(this.progressTime);
        buf.writeBoolean(this.isInventoryFull);
    }

    /**
     * reads all needed values from InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's
     * {@link ISyncedTileEntity#receiveInitialSyncData(gregtech.api.network.AdvancedPacketBuffer)} method
     */
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        setActive(buf.readBoolean());
        setWorkingEnabled(buf.readBoolean());
        setWasActiveAndNeedsUpdate(buf.readBoolean());
        this.progressTime = buf.readInt();
        this.isInventoryFull = buf.readBoolean();
    }

    /**
     * reads all needed values from CustomData
     * This MUST be called and returned in the MetaTileEntity's
     * {@link ISyncedTileEntity#receiveCustomData(int, gregtech.api.network.AdvancedPacketBuffer)} method
     */
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.isActive = buf.readBoolean();
            metaTileEntity.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.isWorkingEnabled = buf.readBoolean();
            metaTileEntity.scheduleRenderUpdate();
        }
    }

    /**
     *
     * @return whether the rig was active and needs an update
     */
    public boolean wasActiveAndNeedsUpdate() {
        return this.wasActiveAndNeedsUpdate;
    }

    /**
     * set whether the rig was active and needs an update
     *
     * @param wasActiveAndNeedsUpdate the state to set
     */
    public void setWasActiveAndNeedsUpdate(boolean wasActiveAndNeedsUpdate) {
        this.wasActiveAndNeedsUpdate = wasActiveAndNeedsUpdate;
    }
}
