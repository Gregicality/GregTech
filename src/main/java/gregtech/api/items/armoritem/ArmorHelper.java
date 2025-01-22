package gregtech.api.items.armoritem;

import gregtech.api.items.armoritem.armorset.IArmorSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.inventory.EntityEquipmentSlot.*;

public class ArmorHelper {

    public static final String BEHAVIORS_TAG_KEY = "GT.Behaviors";

    public static final String NIGHT_VISION_KEY = "NightVision";
    public static final String FALL_DAMAGE_KEY = "FallDamageCancel";
    public static final String STEP_ASSIST_KEY = "StepAssist";
    public static final String HEAT_REDUCTION_KEY = "HeatReduction";

    /* Jetpack Keys */
    public static final String JETPACK_HOVER_KEY = "JetpackHover";
    public static final String FUELED_JETPACK_BURN_TIMER = "FuelBurnTime";

    private static final EntityEquipmentSlot[] ARMOR_SLOTS = { FEET, LEGS, CHEST, HEAD };

    public static NBTTagCompound getBehaviorsTag(@NotNull ItemStack stack) {
        return stack.getOrCreateSubCompound(BEHAVIORS_TAG_KEY);
    }

    public static EntityEquipmentSlot[] getArmorSlots() {
        return ARMOR_SLOTS;
    }

    public static boolean hasFullSet(@NotNull EntityPlayer player, @Nullable IArmorSet armorSet) {
        if (armorSet == null) {
            return false;
        }
        for (EntityEquipmentSlot slot : getArmorSlots()) {
            ItemStack armorStack = player.getItemStackFromSlot(slot);
            if (armorStack.isEmpty() || !(armorStack.getItem() instanceof IGTArmor gtArmor) ||
                    gtArmor.getArmorSet() != armorSet) {
                return false;
            }
        }
        return true;
    }
}
