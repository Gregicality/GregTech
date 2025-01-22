package gregtech.api.items.armoritem.armorset;

import gregtech.api.items.armoritem.ArmorHelper;

import net.minecraft.entity.player.EntityPlayer;

import java.util.Set;

public interface IArmorSet {

    Set<ISetBonus> getSetBonuses();

    default boolean hasFullSet(EntityPlayer player) {
        return ArmorHelper.hasFullSet(player, this);
    }
}
