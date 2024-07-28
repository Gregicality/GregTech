package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.path.INetPath;
import gregtech.api.graphnet.traverse.util.FlowConsumptionStack;
import gregtech.api.util.GTUtility;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.util.Pair;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TraverseHelpers {

    private TraverseHelpers() {}

    /**
     * Provides logic for traversing a flow net in a 'flood' manner;
     * specifically, find the lowest weight path, fill it to capacity, find the next lowest weight path, etc.
     * Requires dynamic weights to function properly.
     *
     * @param data   the traversal data.
     * @param paths  the paths to traverse.
     * @param flowIn the flow to traverse with.
     * @return the consumed flow.
     */
    public static <N extends NetNode, E extends AbstractNetFlowEdge, P extends INetPath<N, E>,
            D extends ITraverseData<N, P>> long traverseFlood(
                                                              @NotNull D data,
                                                              @NotNull Iterator<P> paths,
                                                              long flowIn) {
        boolean simulate = data.getSimulatorKey() != null;
        long availableFlow = flowIn;
        pathloop:
        while (paths.hasNext()) {
            List<Runnable> pathTraverseCalls = simulate ? null : new ObjectArrayList<>();
            long pathFlow = availableFlow;
            P path = paths.next();
            if (data.prepareForPathWalk(path, pathFlow)) continue;

            List<N> nodes = path.getOrderedNodes();
            List<E> edges = path.getOrderedEdges();
            assert nodes.size() == edges.size() + 1;

            FlowConsumptionStack stack = new FlowConsumptionStack(data.traverseToNode(nodes.get(0), pathFlow));
            pathFlow = stack.applyLatestLossFunction(pathFlow);

            for (int i = 0; i < edges.size(); i++) {
                E edge = edges.get(i);
                N targetNode = nodes.get(i + 1);

                if (targetNode.traverse(data.getQueryTick(), true)) {
                    if (!simulate) {
                        pathTraverseCalls.add(() -> targetNode.traverse(data.getQueryTick(), false));
                    }
                } else continue pathloop;

                pathFlow = Math.min(data.getFlowLimit(edge), pathFlow);
                stack.add(flow -> data.consumeFlowLimit(edge, targetNode, flow),
                        data.traverseToNode(targetNode, pathFlow));
                pathFlow = stack.applyLatestLossFunction(pathFlow);

                if (pathFlow <= 0) continue pathloop;
            }

            long accepted = data.finalizeAtDestination(nodes.get(nodes.size() - 1), pathFlow);
            if (!simulate) pathTraverseCalls.forEach(Runnable::run);
            availableFlow -= stack.consumeWithEndValue(accepted);

            if (availableFlow <= 0) break;
        }

        return flowIn - availableFlow;
    }

    /**
     * Provides logic for traversing a net that simply finds the lowest weight path that it can traverse,
     * and then traverses it. Optionally supports flow, in which case overflows will be reported
     * and paths will be iterated over until flow or paths are exhausted.
     *
     * @param data             the traversal data.
     * @param paths            the paths to traverse.
     * @param overflowListener will be provided with a node and incoming overflow once a path is walked
     *                         and the final overflows are calculated. If null, no overflow logic will be calculated.
     * @param flowIn           the flow to traverse with.
     * @return the consumed flow.
     */
    public static <N extends NetNode, E extends NetEdge, P extends INetPath<N, E>,
            D extends ITraverseData<N, P>> long traverseDumb(
                                                             @NotNull D data,
                                                             @NotNull Iterator<P> paths,
                                                             @Nullable BiConsumer<N, Long> overflowListener,
                                                             long flowIn) {
        boolean simulate = data.getSimulatorKey() != null;
        boolean isFlow = overflowListener != null;
        long availableFlow = isFlow ? flowIn : 0;
        pathloop:
        while (paths.hasNext()) {
            List<Runnable> pathTraverseCalls = simulate ? null : new ObjectArrayList<>();
            long pathFlow = isFlow ? availableFlow : 1;
            P path = paths.next();
            if (data.prepareForPathWalk(path, pathFlow)) continue;

            List<N> nodes = path.getOrderedNodes();
            List<E> edges = path.getOrderedEdges();

            List<Consumer<Long>> overflowReporters = isFlow ? new ObjectArrayList<>() : null;
            assert nodes.size() == edges.size() + 1;

            FlowConsumptionStack stack = isFlow ?
                    new FlowConsumptionStack(data.traverseToNode(nodes.get(0), pathFlow)) : null;
            if (isFlow) pathFlow = stack.applyLatestLossFunction(pathFlow);

            for (int i = 0; i < edges.size(); i++) {
                E edge = edges.get(i);
                N targetNode = nodes.get(i + 1);

                if (targetNode.traverse(data.getQueryTick(), true)) {
                    if (!simulate) {
                        pathTraverseCalls.add(() -> targetNode.traverse(data.getQueryTick(), false));
                    }
                } else continue pathloop;

                if (isFlow) {
                    AbstractNetFlowEdge flowEdge = (AbstractNetFlowEdge) edge;
                    long limit = data.getFlowLimit(flowEdge);
                    if (pathFlow > limit) {
                        long overflow = pathFlow - limit;
                        pathFlow = limit;
                        overflowReporters.add(reduction -> {
                            long finalOverflow = overflow - reduction;
                            if (finalOverflow > 0) overflowListener.accept(targetNode, finalOverflow);
                        });
                    }
                    stack.add(flow -> data.consumeFlowLimit(flowEdge, targetNode, flow),
                            data.traverseToNode(targetNode, pathFlow));
                    pathFlow = stack.applyLatestLossFunction(pathFlow);
                }

                if (pathFlow <= 0) continue pathloop;
            }
            long accepted = data.finalizeAtDestination(nodes.get(edges.size()), pathFlow);
            long unaccepted = pathFlow - accepted;
            if (isFlow) {
                availableFlow -= stack.consumeWithEndValue(accepted);
                overflowReporters.forEach((c) -> c.accept(unaccepted));
            }
            if (!simulate) pathTraverseCalls.forEach(Runnable::run);

            if (availableFlow <= 0) break;
        }

        return flowIn - availableFlow;
    }

    /**
     * Provides logic for traversing a flow net in an equal distribution manner. Operates in the following stages:
     * <br>
     * <br>
     * Stage One (collection) -- collect paths into a list while iterating, gathering a map of edges to flow info.
     * Get numbers for what is needed to provide 1 flow per node destination per path, ignoring potential loss.
     * If a path cannot be traversed, it is either trimmed or we abort based on {@code strict}.
     * <br>
     * <br>
     * Stage Two (scaling) -- scale up the mult with a binary search until we get the largest mult where no edge
     * capacities are exceeded. If this mult is 0, abort and return 0.
     * <br>
     * <br>
     * Stage Three (traversal) -- traverse the collected paths and perform consumption and flow, using the multiplier
     * from stage two.
     *
     * @param data   the traversal data.
     * @param paths  the paths to traverse.
     * @param flowIn the flow to traverse with.
     * @param strict whether to abort if one of the destination paths cannot be traversed due to node restrictions,
     *               not edge restrictions.
     * @return the consumed flow.
     */
    public static <N extends NetNode, E extends AbstractNetFlowEdge, P extends INetPath<N, E>,
            D extends ITraverseData<N, P> & IEqualizableTraverseData<N, P>> long traverseEqualDistribution(
                                                                                                           @NotNull D data,
                                                                                                           @NotNull Iterator<P> paths,
                                                                                                           long flowIn,
                                                                                                           boolean strict) {
        // collection
        Map<Pair<E, N>, DistributorHelper> distributorHelperMap = new Object2ObjectOpenHashMap<>();
        Object2IntOpenHashMap<P> desiredMap = new Object2IntOpenHashMap<>();
        int totalDesired = 0;
        long maxMult = flowIn;
        pathsloop:
        while (paths.hasNext()) {
            P path = paths.next();
            if (data.shouldSkipPath(path)) continue;

            List<N> nodes = path.getOrderedNodes();
            List<E> edges = path.getOrderedEdges();
            assert nodes.size() == edges.size() + 1;
            for (N node : nodes) {
                if (!node.traverse(data.getQueryTick(), true)) {
                    if (strict) return 0;
                    else continue pathsloop;
                }
            }
            N dest = nodes.get(nodes.size() - 1);
            int desired = data.getDestinationsAtNode(dest);
            totalDesired += desired;
            desiredMap.put(path, desired);
            maxMult = Math.min(maxMult, data.getMaxFlowToLeastDestination(dest));
            for (int i = 0; i < edges.size(); i++) {
                E edge = edges.get(i);
                N targetNode = nodes.get(i + 1);
                distributorHelperMap.compute(Pair.of(edge, targetNode), (k, v) -> {
                    if (v == null) {
                        v = new DistributorHelper(k.getFirst().getFlowLimit(data.getTestObject(),
                                data.getGraphNet(), data.getQueryTick(), data.getSimulatorKey()), desired);
                        return v;
                    } else {
                        v.addConsumption(desired);
                        return v;
                    }
                });
            }
        }
        maxMult = Math.min(maxMult, flowIn / totalDesired);
        // scaling
        long mult = GTUtility.binarySearch(0, maxMult, l -> {
            for (DistributorHelper helper : distributorHelperMap.values()) {
                if (!helper.supportsMult(l)) return false;
            }
            return true;
        }, false);
        if (mult == 0) return 0;
        // traversal
        boolean simulate = data.getSimulatorKey() != null;
        long availableFlow = flowIn;
        pathloop:
        for (var entry : desiredMap.object2IntEntrySet()) {
            List<Runnable> pathTraverseCalls = simulate ? null : new ObjectArrayList<>();
            long pathFlow = entry.getIntValue() * mult;
            P path = entry.getKey();

            // no skipping paths at this stage
            data.prepareForPathWalk(path, pathFlow);

            List<N> nodes = path.getOrderedNodes();
            List<E> edges = path.getOrderedEdges();
            assert nodes.size() == edges.size() + 1;

            FlowConsumptionStack stack = new FlowConsumptionStack(data.traverseToNode(nodes.get(0), pathFlow));
            pathFlow = stack.applyLatestLossFunction(pathFlow);

            for (int i = 0; i < edges.size(); i++) {
                E edge = edges.get(i);
                N targetNode = nodes.get(i + 1);

                if (targetNode.traverse(data.getQueryTick(), true)) {
                    if (!simulate) {
                        pathTraverseCalls.add(() -> targetNode.traverse(data.getQueryTick(), false));
                    }
                } else continue pathloop;

                pathFlow = Math.min(data.getFlowLimit(edge), pathFlow);
                stack.add(flow -> data.consumeFlowLimit(edge, targetNode, flow),
                        data.traverseToNode(targetNode, pathFlow));
                pathFlow = stack.applyLatestLossFunction(pathFlow);

                if (pathFlow <= 0) continue pathloop;
            }

            long accepted = data.finalizeAtDestination(nodes.get(nodes.size() - 1), pathFlow);
            if (!simulate) pathTraverseCalls.forEach(Runnable::run);
            availableFlow -= stack.consumeWithEndValue(accepted);

            if (availableFlow <= 0) break;
        }

        return flowIn - availableFlow;
    }

    /**
     * Provides logic for traversing a flow net in an equal distribution manner within a dynamic weights context.
     * Calls {@link #traverseEqualDistribution(ITraverseData, Iterator, long, boolean)} repeatedly, using a fresh
     * collection of paths from the {@code pathsSupplier} until
     * {@link #traverseEqualDistribution(ITraverseData, Iterator, long, boolean)} returns 0 or 100 iterations are
     * performed.
     *
     * @param data          the traversal data.
     * @param pathsSupplier the supplier for a fresh set of paths.
     * @param flowIn        the flow to traverse with.
     * @param strict        whether to abort if one of the destination paths cannot be traversed due to node
     *                      restrictions,
     *                      not edge restrictions.
     * @return the consumed flow.
     */
    public static <N extends NetNode, E extends AbstractNetFlowEdge, P extends INetPath<N, E>,
            D extends ITraverseData<N, P> & IEqualizableTraverseData<N, P>> long traverseEqualDistribution(
                                                                                                           @NotNull D data,
                                                                                                           @NotNull Supplier<Iterator<P>> pathsSupplier,
                                                                                                           long flowIn,
                                                                                                           boolean strict) {
        long availableFlow = flowIn;
        byte iterationCount = 0;
        while (iterationCount <= 100) {
            iterationCount++;
            Iterator<P> paths = pathsSupplier.get();
            long flow = traverseEqualDistribution(data, paths, availableFlow, strict);
            if (flow == 0) break;
            else availableFlow -= flow;
        }

        return flowIn - availableFlow;
    }

    /**
     * Provides logic for traversing a flow net in a round robin manner. This implementation merely ensures that
     * destinations will be walked to in a predictable manner through the use of a cache, any balancing must
     * be done on the {@link ITraverseData}'s end.
     *
     * @param data   the traversal data.
     * @param paths  the paths to traverse.
     * @param flowIn the flow to traverse with.
     * @param strict whether to abort if one of the destination paths cannot be traversed due to node restrictions,
     *               not edge restrictions.
     * @return the consumed flow.
     */
    public static <N extends NetNode, E extends AbstractNetFlowEdge, P extends INetPath<N, E>,
            D extends ITraverseData<N, P> & IRoundRobinTraverseData<N, P>> long traverseRoundRobin(
                                                                                                   @NotNull D data,
                                                                                                   @NotNull Iterator<P> paths,
                                                                                                   long flowIn,
                                                                                                   boolean strict) {
        long availableFlow = flowIn;
        ArrayDeque<N> cache = data.getTraversalCache();

        Map<N, P> skippedPaths = new Object2ObjectOpenHashMap<>();
        while (paths.hasNext()) {
            P path = paths.next();
            N destinationNode = path.getTargetNode();
            if (data.shouldSkipPath(path)) continue;

            N nextUp = cache.peekFirst();
            if (destinationNode.equals(nextUp)) {
                // path is next up in the ordering, we can traverse.
                cache.removeFirst();
                cache.addLast(destinationNode);
                long accepted = rrTraverse(data, path, availableFlow, strict);
                if (accepted == -1) return flowIn - availableFlow;
                else availableFlow -= accepted;
            } else {
                // this path isn't the next one up, skip it unless it's a completely new destination.
                if (cache.contains(destinationNode)) skippedPaths.put(destinationNode, path);
                else {
                    cache.addLast(destinationNode);
                    long accepted = rrTraverse(data, path, availableFlow, strict);
                    if (accepted == -1) return flowIn - availableFlow;
                    else availableFlow -= accepted;
                }
            }
        }
        // finally, try and work through skipped paths
        while (true) {
            P path = skippedPaths.get(cache.peekFirst());
            if (path == null) break;
            N dest = cache.removeFirst();
            skippedPaths.remove(dest);
            cache.addLast(dest);
            long accepted = rrTraverse(data, path, availableFlow, strict);
            if (accepted == -1) return flowIn - availableFlow;
            else availableFlow -= accepted;
        }

        return flowIn - availableFlow;
    }

    private static <N extends NetNode, E extends AbstractNetFlowEdge, P extends INetPath<N, E>,
            D extends ITraverseData<N, P> & IRoundRobinTraverseData<N, P>> long rrTraverse(
                                                                                           @NotNull D data,
                                                                                           @NotNull P path,
                                                                                           long flowIn,
                                                                                           boolean strict) {
        boolean simulate = data.getSimulatorKey() != null;
        List<Runnable> pathTraverseCalls = simulate ? null : new ObjectArrayList<>();
        long pathFlow = flowIn;

        data.prepareForPathWalk(path, pathFlow);

        List<N> nodes = path.getOrderedNodes();
        List<E> edges = path.getOrderedEdges();
        assert nodes.size() == edges.size() + 1;

        FlowConsumptionStack stack = new FlowConsumptionStack(data.traverseToNode(nodes.get(0), pathFlow));
        pathFlow = stack.applyLatestLossFunction(pathFlow);

        for (int i = 0; i < edges.size(); i++) {
            E edge = edges.get(i);
            N targetNode = nodes.get(i + 1);

            if (targetNode.traverse(data.getQueryTick(), true)) {
                if (!simulate) {
                    pathTraverseCalls.add(() -> targetNode.traverse(data.getQueryTick(), false));
                }
            } else return strict ? -1 : 0;

            pathFlow = Math.min(data.getFlowLimit(edge), pathFlow);
            stack.add(flow -> data.consumeFlowLimit(edge, targetNode, flow), data.traverseToNode(targetNode, pathFlow));
            pathFlow = stack.applyLatestLossFunction(pathFlow);

            if (pathFlow <= 0) return 0;
        }

        long accepted = data.finalizeAtDestination(nodes.get(nodes.size() - 1), pathFlow);
        if (!simulate) pathTraverseCalls.forEach(Runnable::run);

        return stack.consumeWithEndValue(accepted);
    }
}
