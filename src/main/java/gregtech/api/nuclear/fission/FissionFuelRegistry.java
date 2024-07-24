package gregtech.api.nuclear.fission;

import gregtech.api.util.ItemStackHashStrategy;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class FissionFuelRegistry {

    private static final Map<Integer, IFissionFuelStats> HASHED_FUELS = new Int2ObjectArrayMap<>();
    private static final Map<ItemStack, IFissionFuelStats> FUELS = new Object2ObjectOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());
    private static final Map<IFissionFuelStats, ItemStack> DEPLETED_FUELS = new Object2ObjectOpenHashMap<>();

    public static void registerFuel(@NotNull ItemStack item, @NotNull IFissionFuelStats fuel,
                                    @NotNull ItemStack depletedFuel) {
        HASHED_FUELS.put(fuel.hashCode(), fuel);
        FUELS.put(item, fuel);
        DEPLETED_FUELS.put(fuel, depletedFuel);
    }

    @Nullable
    public static IFissionFuelStats getFissionFuel(ItemStack stack) {
        return FUELS.get(stack);
    }

    @NotNull
    public static Collection<ItemStack> getAllFissionableRods() {
        return FUELS.keySet();
    }

    @Nullable
    public static IFissionFuelStats getFissionFuel(int hash) {
        return HASHED_FUELS.get(hash);
    }

    @Nullable
    public static ItemStack getDepletedFuel(IFissionFuelStats stats) {
        return DEPLETED_FUELS.get(stats);
    }
}
