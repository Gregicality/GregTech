package gregtech.api.items.armor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

@Deprecated
public interface IArmorLogic {

    EntityEquipmentSlot getEquipmentSlot(ItemStack itemStack);

    default void damageArmor(EntityLivingBase entity, ItemStack itemStack, DamageSource source, int damage,
                             EntityEquipmentSlot equipmentSlot) {}

    default void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {}

    String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type);
}
