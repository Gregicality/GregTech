package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static gregtech.api.capability.GregtechDataCodes.IS_WORKING;
import static gregtech.api.capability.GregtechDataCodes.SYNC_TILE_MODE;

public class MetaTileEntityWorldAccelerator extends TieredMetaTileEntity implements IControllable {

    private static final Map<String, Class<?>> blacklistedClasses = new Object2ObjectOpenHashMap<>();
    private static final Object2BooleanFunction<Class<? extends TileEntity>> blacklistCache = new Object2BooleanOpenHashMap<>();
    private static boolean gatheredClasses = false;

    private final int speed;

    private boolean tileMode = false;
    private boolean isActive = false;
    private boolean isPaused = false;

    // Variables for Random Tick mode optimization
    // limit = ((tier - min) / (max - min)) * 2^tier
    private static final int[] SUCCESS_LIMITS = { 1, 8, 27, 64, 125, 216, 343, 512 };
    private final int successLimit;
    private BlockPos bottomLeftCorner;

    public MetaTileEntityWorldAccelerator(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.speed = (int) Math.pow(2, tier);
        this.successLimit = SUCCESS_LIMITS[tier - 1];
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityWorldAccelerator(metaTileEntityId, getTier());
    }

    @Override
    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        this.energyContainer = EnergyContainerHandler.receiverContainer(this,
                tierVoltage * 256L, tierVoltage, getMaxInputOutputAmperage());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.world_accelerator.description"));
        tooltip.add(I18n.format("gregtech.machine.world_accelerator.power_usage", getRandomTickModeAmperage(),
                getTEModeAmperage()));
        tooltip.add(I18n.format("gregtech.machine.world_accelerator.acceleration", speed));
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(),
                GTValues.VNF[getTier()]));
        tooltip.add(
                I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.machine.world_accelerator.working_area"));
        tooltip.add(I18n.format("gregtech.machine.world_accelerator.working_area_tile"));
        tooltip.add(I18n.format("gregtech.machine.world_accelerator.working_area_random", getTier() * 2 + 1,
                getTier() * 2 + 1));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.toggle_mode_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.reset"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        // Take in 8A so that we have a little extra room for cable loss/gaining a buffer in TE mode.
        // Could change this to 5A for random tick mode, but I think it's not very important.
        return 8L;
    }

    protected long getRandomTickModeAmperage() {
        return 3L;
    }

    protected long getTEModeAmperage() {
        return 6L;
    }

    @Override
    public boolean allowTickAcceleration() {
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (isPaused && isActive) {
                setActive(false);
            } else if (!isPaused) {
                boolean wasSuccessful = isTEMode() ? handleTEMode() : handleRandomTickMode();
                if (!wasSuccessful) {
                    setActive(false);
                } else if (!isActive) {
                    setActive(true);
                }
            }
        }
    }

    private boolean handleTEMode() {
        if (!drawEnergy(getTEModeAmperage() * GTValues.V[getTier()])) return false;

        World world = getWorld();
        BlockPos pos = getPos();
        for (EnumFacing facing : EnumFacing.VALUES) {
            TileEntity te = getNeighbor(facing);

            if (!(te instanceof ITickable tickable)) continue;
            if (te.isInvalid()) continue;
            if (!canTileAccelerate(te)) continue;

            for (int i = 0; i < speed; i++) {
                tickable.update();
            }
        }
        return true;
    }

    private boolean handleRandomTickMode() {
        if (!drawEnergy(getRandomTickModeAmperage() * GTValues.V[getTier()])) return false;

        World world = getWorld();
        int maxHeight = world.getHeight();
        // room for hitting ourselves randomly, or blocks not loaded, or blocks outside of height limits
        int attempts = successLimit * 3;
        BlockPos cornerPos = getCornerPos();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(cornerPos);
        int randRange = (getTier() << 1) + 1;
        for (int i = 0, j = 0; i < successLimit && j < attempts; j++) {
            int x = GTValues.RNG.nextInt(randRange);
            int y = GTValues.RNG.nextInt(randRange);
            int z = GTValues.RNG.nextInt(randRange);
            mutablePos.setPos(
                    cornerPos.getX() + x,
                    cornerPos.getY() + y,
                    cornerPos.getZ() + z);

            if (mutablePos.getY() > maxHeight || mutablePos.getY() < 0) continue;
            if (!world.isBlockLoaded(mutablePos)) continue;
            if (mutablePos.equals(getPos())) continue;

            IBlockState state = world.getBlockState(mutablePos);
            Block block = state.getBlock();
            if (block.getTickRandomly()) {
                block.randomTick(world, mutablePos.toImmutable(), state, world.rand);
            }
            i++; // success, whether it actually ticked or not
        }
        return true;
    }

    private boolean drawEnergy(long usage) {
        if (energyContainer.getEnergyStored() < usage) return false;
        energyContainer.removeEnergy(usage);
        return true;
    }

    private BlockPos getCornerPos() {
        if (bottomLeftCorner == null) {
            bottomLeftCorner = new BlockPos(
                    getPos().getX() - getTier(),
                    getPos().getY() - getTier(),
                    getPos().getZ() - getTier());
        }
        return bottomLeftCorner;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (isTEMode()) {
            Textures.WORLD_ACCELERATOR_TE_OVERLAY.renderOrientedState(renderState, translation, pipeline,
                    getFrontFacing(), isActive, isWorkingEnabled());
        } else {
            Textures.WORLD_ACCELERATOR_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                    isActive, isWorkingEnabled());
        }
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            if (isTEMode()) {
                setTEMode(false);
                playerIn.sendStatusMessage(
                        new TextComponentTranslation("gregtech.machine.world_accelerator.mode_entity"), false);
            } else {
                setTEMode(true);
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.machine.world_accelerator.mode_tile"),
                        false);
            }
        }
        return true;
    }

    public void setTEMode(boolean inverted) {
        if (tileMode != inverted) {
            this.tileMode = inverted;
            World world = getWorld();
            if (world != null && !world.isRemote) {
                writeCustomData(SYNC_TILE_MODE, b -> b.writeBoolean(tileMode));
                notifyBlockUpdate();
                markDirty();
            }
        }
    }

    public boolean isTEMode() {
        return tileMode;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("TileMode", tileMode);
        data.setBoolean("isPaused", isPaused);
        data.setBoolean("IsActive", isActive);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        tileMode = data.getBoolean("TileMode");
        isPaused = data.getBoolean("isPaused");
        isActive = data.getBoolean("IsActive");
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(tileMode);
        buf.writeBoolean(isPaused);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.tileMode = buf.readBoolean();
        this.isPaused = buf.readBoolean();
        this.isActive = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == IS_WORKING) {
            this.isActive = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == SYNC_TILE_MODE) {
            this.tileMode = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    protected void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            markDirty();
            World world = getWorld();
            if (world != null && !world.isRemote) {
                writeCustomData(IS_WORKING, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return !isPaused;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingEnabled) {
        if (this.isPaused != isWorkingEnabled) {
            this.isPaused = isWorkingEnabled;
            notifyBlockUpdate();
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    private static void gatherWorldAcceleratorBlacklist() {
        if (!gatheredClasses) {
            for (String name : ConfigHolder.machines.worldAcceleratorBlacklist) {
                if (!blacklistedClasses.containsKey(name)) {
                    try {
                        blacklistedClasses.put(name, Class.forName(name));
                    } catch (ClassNotFoundException ignored) {
                        GTLog.logger.warn("Could not find class {} for World Accelerator Blacklist!", name);
                    }
                }
            }

            try {
                // Block CoFH tile entities by default, non-overridable
                String cofhTileClass = "cofh.thermalexpansion.block.device.TileDeviceBase";
                blacklistedClasses.put(cofhTileClass, Class.forName(cofhTileClass));
            } catch (ClassNotFoundException ignored) {/**/}

            gatheredClasses = true;
        }
    }

    private static boolean canTileAccelerate(TileEntity tile) {
        // Check GT tiles first
        if (tile instanceof IGregTechTileEntity || tile instanceof TileEntityPipeBase) return false;

        gatherWorldAcceleratorBlacklist();

        final Class<? extends TileEntity> tileClass = tile.getClass();
        if (blacklistCache.containsKey(tileClass)) {
            // Tile already tracked, return the value
            return blacklistCache.getBoolean(tileClass);
        }

        // Tile not tracked, see if it is a subclass of a blacklisted class or not
        for (Class<?> clazz : blacklistedClasses.values()) {
            if (clazz.isAssignableFrom(tileClass)) {
                // Is a subclass, so it cannot be accelerated
                blacklistCache.put(tileClass, false);
                return false;
            }
        }
        // Is not a subclass, so it can be accelerated
        blacklistCache.put(tileClass, true);
        return true;
    }
}
