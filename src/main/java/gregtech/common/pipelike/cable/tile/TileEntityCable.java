package gregtech.common.pipelike.cable.tile;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.util.TaskScheduler;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.particle.GTOverheatParticle;
import gregtech.client.particle.GTParticleManager;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.cable.net.EnergyNet;
import gregtech.common.pipelike.cable.net.EnergyNetHandler;
import gregtech.common.pipelike.cable.net.WorldENet;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.vec.Cuboid6;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.CABLE_TEMPERATURE;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_CONNECTIONS;

public class TileEntityCable extends TileEntityMaterialPipeBase<Insulation, WireProperties>
                             implements IDataInfoProvider {

    private static final int meltTemp = 3000;

    private final EnumMap<EnumFacing, EnergyNetHandler> handlers = new EnumMap<>(EnumFacing.class);
    private final PerTickLongCounter maxVoltageCounter = new PerTickLongCounter();
    private final AveragingPerTickCounter averageVoltageCounter = new AveragingPerTickCounter();
    private final AveragingPerTickCounter averageAmperageCounter = new AveragingPerTickCounter();
    private EnergyNetHandler defaultHandler;
    // the EnergyNetHandler can only be created on the server, so we have an empty placeholder for the client
    private final IEnergyContainer clientCapability = IEnergyContainer.DEFAULT;
    private WeakReference<EnergyNet> currentEnergyNet = new WeakReference<>(null);
    @SideOnly(Side.CLIENT)
    private GTOverheatParticle particle;
    private int heatQueue;
    private int temperature = getDefaultTemp();
    private boolean isTicking = false;

    public long getWorldTime() {
        return hasWorld() ? getWorld().getTotalWorldTime() : 0L;
    }

    @Override
    public Class<Insulation> getPipeTypeClass() {
        return Insulation.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    private void initHandlers() {
        EnergyNet net = getEnergyNet();
        if (net == null) {
            return;
        }
        for (EnumFacing facing : EnumFacing.VALUES) {
            handlers.put(facing, new EnergyNetHandler(net, this, facing));
        }
        defaultHandler = new EnergyNetHandler(net, this, null);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!world.isRemote) {
            setTemperature(temperature);
            if (temperature > getDefaultTemp()) {
                TaskScheduler.scheduleTask(world, this::update);
            }
        }
    }

    /**
     * Should only be called internally
     *
     * @return if the cable should be destroyed
     */
    public boolean incrementAmperage(long amps, long voltage) {
        if (voltage > maxVoltageCounter.get(getWorld())) {
            maxVoltageCounter.set(getWorld(), voltage);
        }
        averageVoltageCounter.increment(getWorld(), voltage);
        averageAmperageCounter.increment(getWorld(), amps);

        int dif = (int) (averageAmperageCounter.getLast(getWorld()) - getMaxAmperage());
        if (dif > 0) {
            applyHeat(dif * 40);
            return true;
        }

        return false;
    }

    public void applyHeat(int amount) {
        heatQueue += amount;
        if (!world.isRemote && !isTicking && temperature + heatQueue > getDefaultTemp()) {
            TaskScheduler.scheduleTask(world, this::update);
            isTicking = true;
        }
    }

    private boolean update() {
        if (heatQueue > 0) {
            // if received heat from overvolting or overamping, add heat
            setTemperature(temperature + heatQueue);
        }

        if (temperature >= meltTemp) {
            // cable melted
            world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            isTicking = false;
            return false;
        }

        if (temperature <= getDefaultTemp()) {
            isTicking = false;
            return false;
        }

        if (getPipeType().insulationLevel >= 0 && temperature >= 1500 && GTValues.RNG.nextFloat() < 0.1) {
            // insulation melted
            uninsulate();
            isTicking = false;
            return false;
        }

        if (heatQueue == 0) {
            // otherwise cool down
            setTemperature((int) (temperature - Math.pow(temperature - getDefaultTemp(), 0.35)));
        } else {
            heatQueue = 0;
        }
        return true;
    }

    private void uninsulate() {
        int temp = temperature;
        setTemperature(getDefaultTemp());
        int index = getPipeType().insulationLevel;
        BlockCable newBlock = MetaBlocks.CABLES.get(getPipeBlock().getMaterialRegistry().getModid())[index];
        world.setBlockState(pos, newBlock.getDefaultState());
        TileEntityCable newCable = (TileEntityCable) world.getTileEntity(pos);
        if (newCable != null) { // should never be null
            // TODO: use transfer data method
            newCable.setPipeData(newBlock, newBlock.getItemPipeType(null), getPipeMaterial());
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (isConnected(facing)) {
                    newCable.setConnection(facing, true, true);
                }
            }
            newCable.setTemperature(temp);
            if (!newCable.isTicking) {
                TaskScheduler.scheduleTask(world, newCable::update);
                newCable.isTicking = true;
            }
        }
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
        world.checkLight(pos);
        if (!world.isRemote) {
            writeCustomData(CABLE_TEMPERATURE, buf -> buf.writeVarInt(temperature));
        } else {
            if (temperature <= getDefaultTemp()) {
                if (isParticleAlive())
                    particle.setExpired();
            } else {
                if (!isParticleAlive()) {
                    createParticle();
                }
                particle.setTemperature(temperature);
            }
        }
    }

    public int getDefaultTemp() {
        return 293;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getMeltTemp() {
        return meltTemp;
    }

    @SideOnly(Side.CLIENT)
    public void createParticle() {
        particle = new GTOverheatParticle(this, meltTemp, getPipeBoxes(), getPipeType().insulationLevel >= 0);
        GTParticleManager.INSTANCE.addEffect(particle);
    }

    @SideOnly(Side.CLIENT)
    public void killParticle() {
        if (isParticleAlive()) {
            particle.setExpired();
            particle = null;
        }
    }

    public double getAverageAmperage() {
        return averageAmperageCounter.getAverage(getWorld());
    }

    public long getCurrentMaxVoltage() {
        return maxVoltageCounter.get(getWorld());
    }

    public double getAverageVoltage() {
        return averageVoltageCounter.getAverage(getWorld());
    }

    public long getMaxAmperage() {
        return getNodeData().getAmperage();
    }

    public long getMaxVoltage() {
        return getNodeData().getVoltage();
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            if (world.isRemote)
                return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(clientCapability);
            if (handlers.isEmpty())
                initHandlers();
            checkNetwork();
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(handlers.getOrDefault(facing, defaultHandler));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    public void checkNetwork() {
        if (defaultHandler != null) {
            EnergyNet current = getEnergyNet();
            if (defaultHandler.getNet() != current) {
                defaultHandler.updateNetwork(current);
                for (EnergyNetHandler handler : handlers.values()) {
                    handler.updateNetwork(current);
                }
            }
        }
    }

    private EnergyNet getEnergyNet() {
        if (world == null || world.isRemote)
            return null;
        EnergyNet currentEnergyNet = this.currentEnergyNet.get();
        if (currentEnergyNet != null && currentEnergyNet.isValid() &&
                currentEnergyNet.containsNode(getPos()))
            return currentEnergyNet; // return current net if it is still valid
        WorldENet worldENet = WorldENet.getWorldENet(getWorld());
        currentEnergyNet = worldENet.getNetFromPos(getPos());
        if (currentEnergyNet != null) {
            this.currentEnergyNet = new WeakReference<>(currentEnergyNet);
        }
        return currentEnergyNet;
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        this.handlers.clear();
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0x404040;
    }

    @Override
    public void receiveCustomData(int discriminator, @NotNull AdvancedPacketBuffer buf) {
        if (discriminator == CABLE_TEMPERATURE) {
            setTemperature(buf.readVarInt());
        } else {
            super.receiveCustomData(discriminator, buf);
            if (isParticleAlive() && discriminator == UPDATE_CONNECTIONS) {
                particle.updatePipeBoxes(getPipeBoxes());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean isParticleAlive() {
        return particle != null && particle.isAlive();
    }

    protected List<Cuboid6> getPipeBoxes() {
        List<Cuboid6> pipeBoxes = new ArrayList<>();
        float thickness = getPipeType().getThickness();
        if ((getConnections() & 63) < 63) {
            pipeBoxes.add(BlockPipe.getSideBox(null, thickness));
        }
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (isConnected(facing))
                pipeBoxes.add(BlockPipe.getSideBox(facing, thickness));
        }
        return pipeBoxes;
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Temp", temperature);
        return compound;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        temperature = compound.getInteger("Temp");
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
        list.add(new TextComponentTranslation("behavior.tricorder.eut_per_sec",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.getAverageVoltage()))
                        .setStyle(new Style().setColor(TextFormatting.RED))));
        list.add(new TextComponentTranslation("behavior.tricorder.amp_per_sec",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.getAverageAmperage()))
                        .setStyle(new Style().setColor(TextFormatting.RED))));
        return list;
    }
}
