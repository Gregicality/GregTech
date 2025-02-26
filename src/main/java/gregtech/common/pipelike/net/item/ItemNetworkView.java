package gregtech.common.pipelike.net.item;

import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.util.collection.ListHashSet;

import net.minecraftforge.items.IItemHandler;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class ItemNetworkView {

    public static final ItemNetworkView EMPTY = ItemNetworkView.of(ImmutableBiMap.of());
    private final ItemHandlerList handler;
    private final BiMap<IItemHandler, NetNode> handlerNetNodeBiMap;
    private @Nullable Map<NetNode, ListHashSet<NetPath>> pathCache;

    public ItemNetworkView(ItemHandlerList handler, BiMap<IItemHandler, NetNode> handlerNetNodeBiMap) {
        this.handler = handler;
        this.handlerNetNodeBiMap = handlerNetNodeBiMap;
    }

    public static ItemNetworkView of(BiMap<IItemHandler, NetNode> handlerNetNodeBiMap) {
        return new ItemNetworkView(new ItemHandlerList(handlerNetNodeBiMap.keySet()), handlerNetNodeBiMap);
    }

    public ItemHandlerList getHandler() {
        return handler;
    }

    public BiMap<IItemHandler, NetNode> getBiMap() {
        return handlerNetNodeBiMap;
    }

    public Map<NetNode, ListHashSet<NetPath>> getPathCache() {
        if (pathCache == null) pathCache = new Reference2ReferenceOpenHashMap<>();
        return pathCache;
    }

    public ListHashSet<NetPath> getPathCache(@NotNull NetNode target) {
        if (pathCache == null) pathCache = new Reference2ReferenceOpenHashMap<>();
        return pathCache.computeIfAbsent(target, n -> new ListHashSet<>(1));
    }
}
