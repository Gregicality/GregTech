package gregtech.api.nuclear.fission;

import gregtech.api.util.ItemStackHashStrategy;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class FissionFuelRegistry {

    private static final Map<String, IFissionFuelStats> IDENTIFIED_FUELS = new Object2ObjectOpenHashMap<>();
    private static final Map<ItemStack, IFissionFuelStats> FUELS = new Object2ObjectOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());
    private static final Map<IFissionFuelStats, ItemStack> DEPLETED_FUELS = new Object2ObjectOpenHashMap<>();

    public static void registerFuel(@NotNull ItemStack item, @NotNull IFissionFuelStats fuel,
                                    @NotNull ItemStack depletedFuel) {
        IDENTIFIED_FUELS.put(item.getTranslationKey(), fuel);
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
    public static IFissionFuelStats getFissionFuelFromTranslationKey(String name) {
        return IDENTIFIED_FUELS.get(name);
    }

    public static @NotNull ItemStack getDepletedFuel(IFissionFuelStats stats) {
        return DEPLETED_FUELS.get(stats).copy();
    }
}
