package gregtech.api.pipenet.flow;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverHolder;
import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetEdge;
import gregtech.api.pipenet.NodeG;
import gregtech.api.pipenet.block.IPipeType;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public abstract class FlowChannel<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> {

    protected final Graph<NodeG<PT, NDT>, NetEdge> network;

    protected final Set<NodeG<PT, NDT>> activeSources = new ObjectOpenHashSet<>();

    protected final Map<NodeG<PT, NDT>, Byte> receiveSidesMap = new Object2ObjectOpenHashMap<>();

    protected FlowChannelManager<PT, NDT> manager;

    public FlowChannel(Graph<NodeG<PT, NDT>, NetEdge> network) {
        this.network = network;
    }

    FlowChannel<PT, NDT> setManager(FlowChannelManager<PT, NDT> manager) {
        if (this.manager != null) this.manager.removeChannel(this.getKey());
        this.manager = manager;
        return this;
    }

    public abstract void clearAlg();

    public abstract void evaluate();

    /**
     * Prime the edges to the super nodes to prepare for calculations.
     */
    protected void activate() {
        for (NodeG<PT, NDT> source : new ArrayList<>(activeSources)) {
            double v = getSourceValue(source);
            // TODO find source of random NPE crash where a source edge doesn't exist after world load (pls help)
            network.setEdgeWeight(this.manager.getSuperSource(), source, v);
            if (v == 0) removeSource(source);
        }
        for (NodeG<PT, NDT> sink : new ArrayList<>(this.manager.getActiveSinks())) {
            double v = getSinkValue(sink);
            network.setEdgeWeight(sink, this.manager.getSuperSink(), v);
        }
    }

    /**
     * Zero out the edges to the super nodes to prevent other calculations from using them.
     */
    protected void deactivate() {
        for (NodeG<PT, NDT> source : new ArrayList<>(activeSources)) {
            network.setEdgeWeight(this.manager.getSuperSource(), source, 0);
        }
        for (NodeG<PT, NDT> sink : new ArrayList<>(this.manager.getActiveSinks())) {
            network.setEdgeWeight(sink, this.manager.getSuperSink(), 0);
        }
    }

    void disconnectSuperNodes() {
        for (NodeG<PT, NDT> source : activeSources) {
            network.removeEdge(this.manager.getSuperSource(), source);
        }
    }

    void reconnectSuperNodes() {
        for (NodeG<PT, NDT> source : activeSources) {
            network.addEdge(this.manager.getSuperSource(), source);
        }
    }

    protected abstract double getSourceValue(NodeG<PT, NDT> source);

    protected abstract double getSinkValue(NodeG<PT, NDT> sink);

    public void addReceiveSide(NodeG<PT, NDT> node, EnumFacing side) {
        this.receiveSidesMap.compute(node, (k, v) -> {
            if (v == null) {
                byte a = 0;
                a |= (1 << side.getIndex());
                return a;
            }
            byte a = v;
            a |= (1 << side.getIndex());
            return a;
        });
    }

    public void addSource(NodeG<PT, NDT> source) {
        if (this.activeSources.add(source)) this.network.addEdge(this.manager.getSuperSource(), source);
    }

    public void removeSource(NodeG<PT, NDT> source) {
        if (this.activeSources.remove(source)) this.network.removeEdge(this.manager.getSuperSource(), source);
    }

    @Nullable
    protected static Cover getCoverOnNeighbour(NodeG<?, ?> node, EnumFacing facing) {
        TileEntity tile = node.getConnnected(facing);
        if (tile != null) {
            CoverHolder coverHolder = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER,
                    facing.getOpposite());
            if (coverHolder == null) return null;
            return coverHolder.getCoverAtSide(facing.getOpposite());
        }
        return null;
    }

    protected FlowChannel<PT, NDT> merge(FlowChannel<PT, NDT> otherChannel) {
        this.activeSources.addAll(otherChannel.activeSources);
        for (Map.Entry<NodeG<PT, NDT>, Byte> entry : otherChannel.receiveSidesMap.entrySet()) {
            this.receiveSidesMap.merge(entry.getKey(), entry.getValue(), (a, b) -> (byte) (a | b));
        }
        return this;
    }

    protected void removeNodes(Set<NodeG<PT, NDT>> nodes) {
        this.activeSources.removeAll(nodes);
        for (NodeG<PT, NDT> node : nodes) {
            this.receiveSidesMap.remove(node);
        }
    }

    protected void removeNode(NodeG<PT, NDT> node) {
        this.activeSources.remove(node);
        this.receiveSidesMap.remove(node);
    }

    protected abstract Object getKey();
}
