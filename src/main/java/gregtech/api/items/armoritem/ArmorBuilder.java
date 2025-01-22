package gregtech.api.items.armoritem;

import gregtech.api.items.armoritem.armorset.IArmorSet;
import gregtech.api.items.armoritem.jetpack.IJetpackStats;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.IRarity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public abstract class ArmorBuilder<T extends IGTArmor, U extends ArmorBuilder<T, U>> {

    protected final String domain, id;

    /* IGTToolDefinition held values, extended builders do not need to access these */
    private final EntityEquipmentSlot slot;
    private final List<IArmorBehavior> behaviors = new ArrayList<>();
    private IArmorSet armorSet;
    private IJetpackStats jetpackStats;
    private double damageAbsorption;
    private final List<DamageSource> handledUnblockableSources = new ArrayList<>();
    private boolean isEnchantable = true;
    private int enchantability = 10; // default to vanilla diamond armor enchantability
    private IRarity rarity = EnumRarity.COMMON;
    private int durability;

    protected ArmorBuilder(@NotNull String domain, @NotNull String id, @NotNull EntityEquipmentSlot slot) {
        this.domain = domain;
        this.id = id;
        this.slot = slot;
    }

    public U behaviors(IArmorBehavior... behaviors) {
        Collections.addAll(this.behaviors, behaviors);
        return cast(this);
    }

    public U rarity(IRarity rarity) {
        this.rarity = rarity;
        return cast(this);
    }

    public U durability(int durability) {
        this.durability = durability;
        return cast(this);
    }

    public U enchantability(int enchantability) {
        this.enchantability = enchantability;
        return cast(this);
    }

    public U allowBlocking(DamageSource... sources) {
        Collections.addAll(handledUnblockableSources, sources);
        return cast(this);
    }

    public U armorSet(IArmorSet armorSet) {
        this.armorSet = armorSet;
        return cast(this);
    }

    public abstract Supplier<T> supply(IGTArmorDefinition definition);

    public abstract U cast(ArmorBuilder<T, U> builder);

    public final T build() {
        return supply(buildDefinition()).get();
    }

    protected final IGTArmorDefinition buildDefinition() {
        return new IGTArmorDefinition() {

            @Override
            public @NotNull EntityEquipmentSlot getEquippedSlot() {
                return slot;
            }

            @Override
            public @NotNull List<IArmorBehavior> getBehaviors() {
                return behaviors;
            }

            @Override
            public @Nullable IArmorSet getArmorSet() {
                return armorSet;
            }

            @Override
            public double getDamageAbsorption(EntityEquipmentSlot slot, @Nullable DamageSource damageSource) {
                return damageAbsorption;
            }

            @Override
            public @NotNull List<DamageSource> handledUnblockableSources() {
                return handledUnblockableSources;
            }

            @Override
            public boolean isEnchantable() {
                return isEnchantable;
            }

            @Override
            public int getEnchantability() {
                return enchantability;
            }

            @Override
            public @NotNull IRarity getRarity() {
                return rarity;
            }

            @Override
            public int getMaxDurability() {
                return durability;
            }
        };
    }
}
