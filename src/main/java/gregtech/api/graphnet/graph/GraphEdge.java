package gregtech.api.graphnet.graph;

import gregtech.api.graphnet.net.NetEdge;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Objects;

public final class GraphEdge extends DefaultWeightedEdge {

    @ApiStatus.Internal
    public final NetEdge wrapped;

    public GraphEdge(@NotNull NetEdge wrapped) {
        this.wrapped = wrapped;
        wrapped.wrapper = this;
    }

    @Nullable
    @Contract("null->null")
    public static GraphEdge unwrap(NetEdge e) {
        return e == null ? null : e.wrapper;
    }

    @ApiStatus.Internal
    public GraphEdge() {
        this.wrapped = null;
    }

    public NetEdge getWrapped() {
        return wrapped;
    }

    @Override
    public GraphVertex getSource() {
        return (GraphVertex) super.getSource();
    }

    @Override
    public GraphVertex getTarget() {
        return (GraphVertex) super.getTarget();
    }

    public @Nullable GraphVertex getOppositeVertex(@NotNull GraphVertex node) {
        if (getSource() == node) return getTarget();
        else if (getTarget() == node) return getSource();
        else return null;
    }

    /**
     * Use this very sparingly. It's significantly better to go through {@link org.jgrapht.Graph#getEdgeWeight(Object)}
     * instead, unless you are doing nbt serialization for example.
     * 
     * @return the edge weight.
     */
    @Override
    public double getWeight() {
        return super.getWeight();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphEdge graphEdge = (GraphEdge) o;
        return Objects.equals(getSource(), graphEdge.getSource()) && Objects.equals(getTarget(), graphEdge.getTarget());
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrapped);
    }
}
