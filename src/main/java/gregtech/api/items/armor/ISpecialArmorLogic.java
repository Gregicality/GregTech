package gregtech.api.items.armor;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;

import org.jetbrains.annotations.NotNull;

/**
 * Armor logic that wraps {@link net.minecraftforge.common.ISpecialArmor} methods
 * to allow full control over damage absorption additionally to vanilla attribute values
 */
@Deprecated
public interface ISpecialArmorLogic extends IArmorLogic {

    /**
     * Retrieves the modifiers to be used when calculating armor damage.
     * <p>
     * Armor will higher priority will have damage applied to them before
     * lower priority ones. If there are multiple pieces of armor with the
     * same priority, damage will be distributed between them based on there
     * absorption ratio.
     */
    ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor, DamageSource source, double damage,
                                  EntityEquipmentSlot equipmentSlot);

    /**
     * Get the displayed effective armor.
     *
     * @return The number of armor points for display, 2 per shield.
     */
    int getArmorDisplay(EntityPlayer player, @NotNull ItemStack armor, int slot);

    /**
     * Simple check to see if the armor should interact with "Unblockable" damage
     * sources. A fair number of vanilla damage sources have this tag, such as
     * Anvils, Falling, Fire, and Magic.
     * <p>
     * Returning true here means that the armor is able to meaningfully respond
     * to this damage source. Otherwise, no interaction is allowed.
     */
    default boolean handleUnblockableDamage(EntityLivingBase entity, @NotNull ItemStack armor, DamageSource source,
                                            double damage, EntityEquipmentSlot equipmentSlot) {
        return false;
    }
}
