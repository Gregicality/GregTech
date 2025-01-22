package gregtech.common.items.armor.behavior;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armoritem.jetpack.IJetpackStats;
import gregtech.api.items.armoritem.jetpack.JetpackBehavior;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class ElectricJetpackBehavior extends JetpackBehavior {

    private final int energyPerUse;

    public ElectricJetpackBehavior(IJetpackStats jetpackStats, int energyPerUse) {
        super(jetpackStats);
        this.energyPerUse = energyPerUse;
    }

    @Override
    protected int getFuelPerUse() {
        return energyPerUse;
    }

    @Override
    protected boolean drainFuel(@NotNull ItemStack stack, int amount, boolean simulate) {
        IElectricItem electricItem = getElectricItem(stack);
        if (electricItem != null) {
            return electricItem.discharge(amount, Integer.MAX_VALUE, true, false, simulate) >= amount;
        }
        return false;
    }

    private static IElectricItem getElectricItem(@NotNull ItemStack stack) {
        return stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
    }
}
