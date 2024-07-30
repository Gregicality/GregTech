package gregtech.common.metatileentities.multi.electric.generator.turbine;

import org.jetbrains.annotations.NotNull;

public final class TurbineType {

    public static final TurbineType STEAM = new TurbineType("gregtech.turbine_type.steam");
    public static final TurbineType GAS = new TurbineType("gregtech.turbine_type.gas");
    public static final TurbineType PLASMA = new TurbineType("gregtech.turbine_type.plasma");

    private static int rollingId;

    private final String translationKey;
    private final int id;

    public TurbineType(@NotNull String translationKey) {
        this.translationKey = translationKey;
        this.id = rollingId++;
    }

    public @NotNull String translationKey() {
        return translationKey;
    }

    public int id() {
        return id;
    }

    public static int size() {
        return rollingId;
    }
}
