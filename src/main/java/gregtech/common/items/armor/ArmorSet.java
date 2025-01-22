package gregtech.common.items.armor;

import gregtech.api.items.armoritem.armorset.IArmorSet;
import gregtech.api.items.armoritem.armorset.ISetBonus;

import java.util.Set;

public class ArmorSet implements IArmorSet {

    public static final ArmorSet NANO = new ArmorSet();
    public static final ArmorSet QUANTUM = new ArmorSet();

    @Override
    public Set<ISetBonus> getSetBonuses() {
        return null;
    }
}
