package gregtech.common.pipelike.handlers.properties;

import gregtech.api.capability.IPropertyFluidFilter;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.FluidConstants;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.logic.ThroughputLogic;
import gregtech.api.graphnet.logic.WeightFactorLogic;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.logic.TemperatureLossFunction;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PipeNetProperties;

import gregtech.api.unification.material.properties.PropertyKey;

import gregtech.api.unification.ore.IOreRegistrationHandler;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.function.TriConsumer;
import gregtech.common.pipelike.block.pipe.PipeStructure;

import gregtech.common.pipelike.net.fluid.FluidContainmentLogic;
import gregtech.common.pipelike.net.fluid.WorldFluidNet;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import static gregtech.api.unification.material.info.MaterialFlags.NO_UNIFICATION;

public final class MaterialFluidProperties implements PipeNetProperties.IPipeNetMaterialProperty, IPropertyFluidFilter {

    public static final MaterialPropertyKey<MaterialFluidProperties> KEY = new MaterialPropertyKey<>();

    private final Set<FluidAttribute> containableAttributes = new ObjectOpenHashSet<>();
    private final EnumSet<FluidState> containableStates = EnumSet.of(FluidState.LIQUID);

    private int maxFluidTemperature;
    private final int minFluidTemperature;

    private final long baseThroughput;
    private final float priority;

    public MaterialFluidProperties(long baseThroughput, int maxFluidTemperature, int minFluidTemperature, float priority) {
        this.baseThroughput = baseThroughput;
        this.maxFluidTemperature = maxFluidTemperature;
        this.minFluidTemperature = minFluidTemperature;
        this.priority = priority;
    }

    public MaterialFluidProperties(long baseThroughput, int maxFluidTemperature, int minFluidTemperature) {
        this(baseThroughput, maxFluidTemperature, minFluidTemperature, 2048f / baseThroughput);
    }

    public static MaterialFluidProperties createMax(long baseThroughput, int maxFluidTemperature) {
        return createMax(baseThroughput, maxFluidTemperature, 2048f / baseThroughput);
    }

    public static MaterialFluidProperties createMax(long baseThroughput, int maxFluidTemperature, float priority) {
        return new MaterialFluidProperties(baseThroughput, maxFluidTemperature, FluidConstants.CRYOGENIC_FLUID_THRESHOLD + 1, priority);
    }

    public static MaterialFluidProperties createMin(long baseThroughput, int minFluidTemperature) {
        return createMin(baseThroughput, minFluidTemperature, 2048f / baseThroughput);
    }

    public static MaterialFluidProperties createMin(long baseThroughput, int minFluidTemperature, float priority) {
        return new MaterialFluidProperties(baseThroughput, 0, minFluidTemperature, priority);
    }

    public static MaterialFluidProperties create(long baseThroughput) {
        return create(baseThroughput, 2048f / baseThroughput);
    }

    public static MaterialFluidProperties create(long baseThroughput, float priority) {
        return new MaterialFluidProperties(baseThroughput, 0, 0, priority);
    }

    public MaterialFluidProperties setContain(FluidState state, boolean canContain) {
        if (canContain) contain(state);
        else notContain(state);
        return this;
    }

    public MaterialFluidProperties setContain(FluidAttribute attribute, boolean canContain) {
        if (canContain) contain(attribute);
        else notContain(attribute);
        return this;
    }

    public MaterialFluidProperties contain(FluidState state) {
        this.containableStates.add(state);
        return this;
    }

    public MaterialFluidProperties contain(FluidAttribute attribute) {
        this.containableAttributes.add(attribute);
        return this;
    }

    public MaterialFluidProperties notContain(FluidState state) {
        this.containableStates.remove(state);
        return this;
    }

    public MaterialFluidProperties notContain(FluidAttribute attribute) {
        this.containableAttributes.remove(attribute);
        return this;
    }

    public boolean canContain(@NotNull FluidState state) {
        return this.containableStates.contains(state);
    }

    public boolean canContain(@NotNull FluidAttribute attribute) {
        return this.containableAttributes.contains(attribute);
    }

    @Override
    public @NotNull @UnmodifiableView Collection<@NotNull FluidAttribute> getContainedAttributes() {
        return containableAttributes;
    }

    public int getMaxFluidTemperature() {
        return maxFluidTemperature;
    }

    public int getMinFluidTemperature() {
        return minFluidTemperature;
    }

    @Override
    public MaterialPropertyKey<?> getKey() {
        return KEY;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.WOOD)) {
            properties.ensureSet(PropertyKey.INGOT, true);
        }

        if (this.maxFluidTemperature == 0 && properties.hasProperty(PropertyKey.FLUID)) {
            // autodetermine melt temperature from registered fluid
            FluidProperty prop = properties.getProperty(PropertyKey.FLUID);
            Fluid fluid = prop.getStorage().get(FluidStorageKeys.LIQUID);
            if (fluid == null) {
                FluidBuilder builder = prop.getStorage().getQueuedBuilder(FluidStorageKeys.LIQUID);
                if (builder != null) {
                    this.maxFluidTemperature = builder.currentTemp();
                }
            } else {
                this.maxFluidTemperature = fluid.getTemperature();
            }
        }
    }

    @Override
    public void addToNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof PipeStructure) {
            WorldPipeNetNode node = WorldFluidNet.getWorldNet(world).getOrCreateNode(pos);
            mutateData(node.getData(), structure);
        }
    }

    @Override
    public void mutateData(NetLogicData data, IPipeStructure structure) {
        if (structure instanceof PipeStructure pipe) {
            long throughput = baseThroughput * pipe.material();
            float coolingFactor = (float) Math.sqrt((double) pipe.material() / (4 + pipe.channelCount()));
            double weight = priority * (pipe.restrictive() ? 100d : 1d) * pipe.channelCount() / pipe.material();
            data.setLogicEntry(WeightFactorLogic.INSTANCE.getWith(weight))
                    .setLogicEntry(ThroughputLogic.INSTANCE.getWith(throughput))
                    .setLogicEntry(FluidContainmentLogic.INSTANCE.getWith(containableStates, containableAttributes))
                    .setLogicEntry(TemperatureLogic.INSTANCE
                            .getWith(TemperatureLossFunction.getOrCreatePipe(coolingFactor), maxFluidTemperature,
                                    minFluidTemperature, 50 * pipe.material(), null));
        }
    }

    @Override
    public @Nullable WorldPipeNetNode getFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof PipeStructure)
            return WorldFluidNet.getWorldNet(world).getNode(pos);
        else return null;
    }

    @Override
    public void removeFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof PipeStructure) {
            WorldFluidNet net = WorldFluidNet.getWorldNet(world);
            NetNode node = net.getNode(pos);
            if (node != null) net.removeNode(node);
        }
    }

    @Override
    public boolean generatesStructure(IPipeStructure structure) {
        return structure.getClass() == PipeStructure.class;
    }
}