package gregtech.api.items.armoritem;

import gregtech.api.items.armoritem.armorset.IArmorSet;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.IRarity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// TODO A lot of these do not need to be in the "definition" class, but in the Item class
public interface IGTArmorDefinition {

    @NotNull
    EntityEquipmentSlot getEquippedSlot();

    @NotNull
    List<IArmorBehavior> getBehaviors();

    @Nullable
    IArmorSet getArmorSet();

    /* ArmorProperties */
    double getDamageAbsorption(EntityEquipmentSlot slot, @Nullable DamageSource damageSource);

    @NotNull
    List<DamageSource> handledUnblockableSources();

    boolean isEnchantable();

    int getEnchantability();

    @NotNull
    IRarity getRarity();

    int getMaxDurability();

    default boolean hasDurability() {
        return getMaxDurability() > 0;
    }
}
