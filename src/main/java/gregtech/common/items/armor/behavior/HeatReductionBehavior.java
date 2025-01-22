package gregtech.common.items.armor.behavior;

import gregtech.api.items.armoritem.ArmorHelper;
import gregtech.api.items.armoritem.IArmorBehavior;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

// todo Nano was 0.75, quantum was 0.5
// todo tooltip?
public class HeatReductionBehavior implements IArmorBehavior {

    private final float reductionFactor;

    public HeatReductionBehavior(float reductionFactor) {
        this.reductionFactor = reductionFactor;
    }

    @Override
    public void addBehaviorNBT(@NotNull ItemStack stack, @NotNull NBTTagCompound tag) {
        tag.setFloat(ArmorHelper.HEAT_REDUCTION_KEY, reductionFactor);
    }
}
