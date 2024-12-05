package gregtech.api.graphnet.traverseold;

import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.net.NetNode;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public interface IRoundRobinData<N extends NetNode> {

    /**
     * Called to notify this {@link IRoundRobinData} to reset its internals to prepare for iteration over internal
     * destinations. If this object has not finished progressing over its internal destinations, this should do nothing.
     */
    void resetIfFinished(N node, @Nullable SimulatorKey simulator);

    /**
     * Similar to {@link Iterator#hasNext()}, this method determines whether there is another internal destination in
     * this {@link IRoundRobinData} to finalize at. When false is returned, the RR traversal will move on from
     * this {@link IRoundRobinData}
     * 
     * @return whether another internal destination is present
     */
    boolean hasNextInternalDestination(N node, @Nullable SimulatorKey simulator);

    /**
     * Similar to {@link Iterator#next()}, this is called to notify this {@link IRoundRobinData} that it should
     * progress to the next internal destination.
     */
    void progressToNextInternalDestination(N node, @Nullable SimulatorKey simulator);
}
