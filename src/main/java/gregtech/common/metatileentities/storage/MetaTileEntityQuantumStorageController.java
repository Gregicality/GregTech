package gregtech.common.metatileentities.storage;

import gregtech.api.GTValues;
import gregtech.api.capability.DualHandler;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IDualHandler;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IQuantumController;
import gregtech.api.capability.IQuantumStorage;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static gregtech.api.capability.IQuantumStorage.Type.*;

public class MetaTileEntityQuantumStorageController extends MetaTileEntity implements IQuantumController {

    private static final int MAX_DISTANCE_RADIUS = 16;

    private EnergyContainerList energyHandler = new EnergyContainerList(Collections.emptyList());
    private final List<IEnergyContainer> energyContainers = new ArrayList<>();
    /** Somewhat lazily initialized, make sure to call {@code getStorage()} before trying to access anything in this */
    private Map<BlockPos, WeakReference<IQuantumStorage<?>>> storageInstances = new HashMap<>();

    /** The "definitive" set of positions of storage instances */
    private Set<BlockPos> storagePositions = new HashSet<>();
    private final Map<IQuantumStorage.Type, Set<BlockPos>> typePosMap = new EnumMap<>(IQuantumStorage.Type.class);
    private final BlockPos[] bounds = new BlockPos[2];
    private long energyConsumption = 0;
    private final QuantumControllerHandler handler = new QuantumControllerHandler();

    private boolean isDead = false;
    private boolean isPowered = false;

    public MetaTileEntityQuantumStorageController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        for (var type : VALUES) {
            typePosMap.put(type, new HashSet<>());
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityQuantumStorageController(metaTileEntityId);
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) return;

        if (getOffsetTimer() % 10 == 0) {
            boolean isPowered = energyHandler.getEnergyStored() > energyConsumption && energyConsumption > 0;
            if (isPowered) energyHandler.removeEnergy(energyConsumption);

            if (isPowered != this.isPowered) {
                this.isPowered = isPowered;

                writeCustomData(GregtechDataCodes.UPDATE_ENERGY, buf -> {
                    buf.writeBoolean(this.isPowered);
                    buf.writeBlockPos(this.bounds[0]);
                    buf.writeBlockPos(this.bounds[1]);
                });
                onHandlerUpdate();
            }
        }
    }

    public boolean isPowered() {
        return isPowered;
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_ENERGY) {
            this.isPowered = buf.readBoolean();
            getWorld().markBlockRangeForRenderUpdate(buf.readBlockPos(), buf.readBlockPos());
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_ENERGY_PER) {
            this.energyConsumption = buf.readLong();
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        var front = isPowered() ?
                Textures.QUANTUM_CONTROLLER_FRONT_ACTIVE :
                Textures.QUANTUM_CONTROLLER_FRONT_INACTIVE;
        var sides = isPowered() ?
                Textures.QUANTUM_CONTROLLER_ACTIVE :
                Textures.QUANTUM_CONTROLLER_INACTIVE;

        var newPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));

        front.renderSided(getFrontFacing(), renderState, translation, newPipeline);

        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing == getFrontFacing()) continue;
            sides.renderSided(facing, renderState, translation, newPipeline);
        }
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.QUANTUM_CONTROLLER_ACTIVE.getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false; // todo use mui2 for ui?
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Nullable
    @SuppressWarnings("SameParameterValue")
    private IQuantumStorage<?> getStorage(BlockPos pos, boolean rebuild) {
        if (getWorld().isRemote) return null;
        if (storageInstances.containsKey(pos)) {
            WeakReference<IQuantumStorage<?>> storageRef = storageInstances.get(pos);
            IQuantumStorage<?> storage = storageRef.get();
            if (storage != null) {
                return storage;
            }
        }
        // need to make sure it still exists
        MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), pos);
        if (mte instanceof IQuantumStorage<?>storage) {
            storageInstances.put(pos, new WeakReference<>(storage));
            return storage;
        } else if (rebuild) {
            // need to remove and rebuild
            storagePositions.remove(pos);
            rebuildNetwork();
            return null;
        }
        return null;
    }

    @Nullable
    public final IQuantumStorage<?> getStorage(BlockPos pos) {
        return getStorage(pos, false);
    }

    @Override
    public boolean canConnect(IQuantumStorage<?> storage) {
        return !isDead && isInRange(storage.getPos());
    }

    private boolean isInRange(BlockPos pos) {
        return Math.abs(getPos().getX() - pos.getX()) <= MAX_DISTANCE_RADIUS &&     // valid X
                Math.abs(getPos().getY() - pos.getY()) <= MAX_DISTANCE_RADIUS &&    // valid Y
                Math.abs(getPos().getZ() - pos.getZ()) <= MAX_DISTANCE_RADIUS;      // valid Z
    }

    @Override
    public void onRemoval() {
        if (getWorld().isRemote) return;
        isDead = true;
        for (BlockPos pos : storagePositions) {
            IQuantumStorage<?> storage = getStorage(pos);
            if (storage != null) storage.setDisconnected();
        }
        handler.invalidate();
        storagePositions.clear();
        storageInstances.clear();
        typePosMap.clear();
    }

    @Override
    public void onPlacement(@Nullable EntityLivingBase placer) {
        super.onPlacement(placer);
        rebuildNetwork();
    }

    @Override
    public void onLoad() {
        calculateEnergyUsage();
        super.onLoad();
    }

    // Used when this controller is initially placed. Try to find all possible
    // storage instances that are connected and within our distance radius
    @Override
    public void rebuildNetwork() {
        if (getWorld().isRemote) return;
        var oldInstances = storageInstances;
        var oldPositions = storagePositions;

        storageInstances = new HashMap<>();
        storagePositions = new HashSet<>();

        typePosMap.values().forEach(Set::clear);

        Queue<BlockPos> searchQueue = new ArrayDeque<>();
        Set<BlockPos> checked = new HashSet<>();

        // check the posses around the controller
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (checkStorageNeighbor(this, facing)) {
                searchQueue.add(getPos().offset(facing));
            }
        }

        int minx = getPos().getX();
        int miny = getPos().getY();
        int minz = getPos().getZ();
        int maxx = minx;
        int maxy = miny;
        int maxz = minz;

        // while there are blocks to search
        while (!searchQueue.isEmpty()) {
            BlockPos pos = searchQueue.remove();

            if (!checked.add(pos))
                continue;

            if (!isInRange(pos) || !getWorld().isBlockLoaded(pos, false)) continue;

            var state = getWorld().getBlockState(pos);
            if (state.getBlock().isAir(state, getWorld(), pos)) continue;

            MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), pos);
            // the mte at this pos is always an instance of quantum storage
            IQuantumStorage<?> storage = (IQuantumStorage<?>) mte;

            // connected to some other network already, ignore
            if (storage.isConnected() && !storage.getControllerPos().equals(getPos())) continue;

            // valid chest/tank located, add it
            storageInstances.put(pos, new WeakReference<>(storage));
            storagePositions.add(pos);
            typePosMap.get(storage.getType()).add(pos);
            storage.setConnected(this);
            oldInstances.remove(pos);
            oldPositions.remove(pos);

            // calculate bounds
            minx = Math.min(minx, pos.getX());
            miny = Math.min(miny, pos.getY());
            minz = Math.min(minz, pos.getZ());

            maxx = Math.max(maxx, pos.getX());
            maxy = Math.max(maxy, pos.getY());
            maxz = Math.max(maxz, pos.getZ());

            // check against already check posses so we don't recheck a checked pos
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos offsetPos = pos.offset(facing);
                if (checked.contains(offsetPos) || getPos().equals(offsetPos)) continue;
                state = getWorld().getBlockState(offsetPos);
                if (state.getBlock().isAir(state, getWorld(), offsetPos)) continue;

                // add a new pos to search
                if (checkStorageNeighbor(mte, facing))
                    searchQueue.add(offsetPos);
            }
        }

        // update bounds
        this.bounds[0] = new BlockPos(minx, miny, minz);
        this.bounds[1] = new BlockPos(maxx, maxy, maxz);

        // check old posses to disconnect the storages
        for (BlockPos pos : oldPositions) {

            // if we already checked this pos before, don't check it again
            if (checked.contains(pos)) continue;

            // if the pos is air, there's nothing to check
            var state = getWorld().getBlockState(pos);
            if (state.getBlock().isAir(state, getWorld(), pos)) continue;

            IQuantumStorage<?> storage = oldInstances.get(pos).get();
            if (storage == null) {
                MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), pos);
                if (!(mte instanceof IQuantumStorage<?>quantumStorage)) {
                    continue;
                }
                storage = quantumStorage;
            }
            storage.setDisconnected();
        }
        handler.markDirty();
        onHandlerUpdate();
        calculateEnergyUsage();
        markDirty();
    }

    private static boolean checkStorageNeighbor(MetaTileEntity mte, EnumFacing facing) {
        if (mte.getNeighbor(facing) instanceof IGregTechTileEntity gtte) {
            return gtte.getMetaTileEntity() instanceof IQuantumStorage<?>;
        }
        return false;
    }

    @Override
    public void onHandlerUpdate() {
        if (getWorld().isRemote) return;
        notifyBlockUpdate();
        for (var pos : typePosMap.get(PROXY)) {
            var storage = getStorage(pos);
            if (storage == null) continue;
            storage.notifyBlockUpdate();
        }
    }

    private void calculateEnergyUsage() {
        energyContainers.clear();
        energyConsumption = 0;
        for (var pos : storagePositions) {
            var storage = getStorage(pos);
            if (storage != null) {
                typePosMap.get(storage.getType()).add(pos);
                energyConsumption += getTypeEnergy(storage);
                if (storage.getType() == ENERGY) {
                    energyContainers.add((IEnergyContainer) storage.getTypeValue());
                }
            }
        }
        energyHandler = new EnergyContainerList(energyContainers);
        writeCustomData(GregtechDataCodes.UPDATE_ENERGY_PER, buf -> buf.writeLong(energyConsumption));
    }

    public long getTypeEnergy(IQuantumStorage<?> storage) {
        return switch (storage.getType()) {
            case ITEM, FLUID -> {
                int tier = storage instanceof ITieredMetaTileEntity tieredMTE ? tieredMTE.getTier() : 1;
                yield tier > 5 ?
                        GTValues.VH[GTValues.HV] :
                        GTValues.VH[GTValues.LV];
            }
            case PROXY -> 8L;
            case EXTENDER -> 2L;
            case ENERGY -> 1L;
        };
    }

    @Override
    public int getCount(IQuantumStorage.Type type) {
        return typePosMap.get(type).size();
    }

    public final long getEnergyUsage() {
        return energyConsumption;
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isPowered);
        buf.writeLong(this.energyConsumption);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isPowered = buf.readBoolean();
        this.energyConsumption = buf.readLong();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tagCompound = super.writeToNBT(data);
        NBTTagList list = new NBTTagList();
        for (BlockPos pos : storagePositions) {
            list.appendTag(new NBTTagLong(pos.toLong()));
        }
        tagCompound.setTag("StorageInstances", list);
        tagCompound.setLong("MinBound", bounds[0].toLong());
        tagCompound.setLong("MaxBound", bounds[1].toLong());
        tagCompound.setBoolean("isPowered", this.isPowered);
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        NBTTagList list = data.getTagList("StorageInstances", Constants.NBT.TAG_LONG);
        for (int i = 0; i < list.tagCount(); i++) {
            storagePositions.add(BlockPos.fromLong(((NBTTagLong) list.get(i)).getLong()));
        }
        this.bounds[0] = BlockPos.fromLong(data.getLong("MinBound"));
        this.bounds[1] = BlockPos.fromLong(data.getLong("MaxBound"));
        this.isPowered = data.getBoolean("isPowered");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing side) {
        if (isPowered()) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && handler.hasItemHandlers()) {
                return (T) handler.getDelegatItemHandler();
            } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && handler.hasFluidTanks()) {
                return (T) handler.getDelegateTank();
            }
        }

        return super.getCapability(capability, side);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.quantum_chest.tooltip"));
    }

    @Override
    public IDualHandler getHandler() {
        return this.handler;
    }

    // todo use DualHandler instead once the multis ability pr is merged
    private class QuantumControllerHandler extends DualHandler {

        private static final IItemHandlerModifiable EMPTY_ITEM = new ItemStackHandler(0);
        private static final IMultipleTankHandler EMPTY_TANK = new FluidTankList(false);
        private boolean dirty = true;

        public QuantumControllerHandler() {
            super(EMPTY_ITEM, EMPTY_TANK, true);
        }

        private void invalidate() {
            fluidDelegate = EMPTY_TANK;
            itemDelegate = EMPTY_ITEM;
        }

        private void rebuildCache() {
            List<IItemHandler> itemHandlerList = new ArrayList<>();
            List<IFluidTank> fluidTankList = new ArrayList<>();
            for (BlockPos pos : storagePositions) {
                IQuantumStorage<?> storage = getStorage(pos);
                if (storage == null) continue;
                switch (storage.getType()) {
                    case ITEM -> itemHandlerList.add((IItemHandler) storage.getTypeValue());
                    case FLUID -> fluidTankList.add((IFluidTank) storage.getTypeValue());
                }
            }

            // todo allow this "allowSameFluidFill" to be configured in this controller?
            this.fluidDelegate = new FluidTankList(false, fluidTankList);
            this.itemDelegate = new ItemHandlerList(itemHandlerList);
            this.dirty = false;
        }

        public void markDirty() {
            this.dirty = true;
        }

        @Override
        public boolean hasItemHandlers() {
            if (dirty) rebuildCache();
            return super.hasItemHandlers();
        }

        @Override
        public boolean hasFluidTanks() {
            if (dirty) rebuildCache();
            return super.hasFluidTanks();
        }
    }
}
