package gregtech.api.items.armor;

import gregtech.api.items.armor.ArmorMetaItem.ArmorMetaValueItem;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Defines abstract armor logic that can be added to ArmorMetaItem to control it
 * It can implement {@link net.minecraftforge.common.ISpecialArmor} for extended damage calculations
 * supported instead of using vanilla attributes
 */
public interface IArmorLogic {

    UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A144-9C13A33DB5CF");
    UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4288-B05C-BCCE9785ACA3");

    default void addToolComponents(ArmorMetaValueItem metaValueItem) {}

    EntityEquipmentSlot getEquipmentSlot(ItemStack itemStack);

    default boolean canBreakWithDamage(ItemStack stack) {
        return false;
    }

    default void damageArmor(EntityLivingBase entity, ItemStack itemStack, DamageSource source, int damage,
                             EntityEquipmentSlot equipmentSlot) {}

    default Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        return ImmutableMultimap.of();
    }

    default boolean isValidArmor(ItemStack itemStack, Entity entity, EntityEquipmentSlot equipmentSlot) {
        return getEquipmentSlot(itemStack) == equipmentSlot;
    }

    default void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {}

    @SideOnly(Side.CLIENT)
    default void renderHelmetOverlay(ItemStack itemStack, EntityPlayer player, ScaledResolution resolution,
                                     float partialTicks) {}

    default int getArmorLayersAmount(ItemStack itemStack) {
        return 1;
    }

    default int getArmorLayerColor(ItemStack itemStack, int layerIndex) {
        return 0xFFFFFF;
    }

    String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type);

    @Nullable
    default ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot,
                                     ModelBiped defaultModel) {
        return null;
    }

    /**
     *
     * @return the value to multiply heat damage by
     */
    default float getHeatResistance() {
        return 1.0f;
    }

    /**
     *
     * @return the value to multiply radiation damage by
     */
    default float getRadiationResistance() {
        return 1.0f;
    }
}
