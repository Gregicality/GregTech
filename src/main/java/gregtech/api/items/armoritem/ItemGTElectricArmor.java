package gregtech.api.items.armoritem;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.ElectricItem;
import gregtech.api.items.metaitem.ElectricStats;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/** For armors which use power, either instead of or in addition to durability. */
public class ItemGTElectricArmor extends ItemGTArmor {

    private static final IElectricItem EMPTY_STATS = new ElectricItem(ItemStack.EMPTY, 0, 0, false, false);

    protected final int tier;
    protected final long maxCharge;
    protected final long energyPerUse; // todo better name
    protected final int energyScaleFactor; // todo better name
    protected final boolean canChargeExternally;

    public ItemGTElectricArmor(String domain, String id,
                               IGTArmorDefinition armorDefinition,
                               int tier,
                               long maxCharge,
                               long energyPerUse,
                               int energyScaleFactor,
                               boolean canChargeExternally) {
        super(domain, id, armorDefinition);
        this.tier = tier;
        this.maxCharge = maxCharge;
        this.energyPerUse = energyPerUse;
        this.energyScaleFactor = energyScaleFactor; // was originally 10 for nano, 100 for quantum
        this.canChargeExternally = canChargeExternally;
    }

    protected @NotNull IElectricItem getElectricItem(ItemStack armor) {
        IElectricItem electricItem = armor.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        return electricItem != null ? electricItem : EMPTY_STATS;
    }

    @Override
    public void damageArmor(EntityLivingBase entity, @NotNull ItemStack stack, @Nullable DamageSource source,
                            int damage, int slot) {
        IElectricItem electricItem = getElectricItem(stack);
        long amountDrawn, amountToDraw = energyPerUse * damage / energyScaleFactor;
        amountDrawn = electricItem.discharge(amountToDraw, electricItem.getTier(), true, false, false);
        if (amountDrawn < amountToDraw) {
            // send remaining damage to durability (will be tested in super if it should be checked)
            super.damageArmor(entity, stack, source,
                    (int) Math.max(1, damage * (amountToDraw - amountDrawn) / amountToDraw), slot);
        }
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, @NotNull ItemStack armor, int slot) {
        IElectricItem electricItem = getElectricItem(armor);
        if (electricItem.getCharge() < energyPerUse) {
            return (int) Math
                    .round(4.0f * getDamageModifier() * getDefinition().getDamageAbsorption(getEquipmentSlot(), null));
        }
        return super.getArmorDisplay(player, armor, slot);
    }

    @Override
    public boolean areBehaviorsActive(@NotNull ItemStack stack) {
        // all behaviors for electric armors need power to work, even if they don't have their own cost
        return getElectricItem(stack).getCharge() > 0;
    }

    @Override
    protected @NotNull List<ICapabilityProvider> getCapabilityProviders(@NotNull ItemStack stack,
                                                                        @Nullable NBTTagCompound tag) {
        List<ICapabilityProvider> providers = super.getCapabilityProviders(stack, tag);
        ElectricStats stats = new ElectricStats(maxCharge, tier, true, canChargeExternally);
        providers.add(stats.createProvider(stack));
        return providers;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        IElectricItem electricItem = getElectricItem(stack);
        tooltip.add(I18n.format("metaitem.generic.electric_item.tooltip",
                electricItem.getCharge(),
                electricItem.getMaxCharge(),
                GTValues.VNF[electricItem.getTier()]));
        super.addInformation(stack, world, tooltip, flag);
    }

    public static class Builder extends ArmorBuilder<ItemGTElectricArmor, Builder> {

        protected int tier;
        protected long maxCharge;
        protected long energyPerUse;
        protected int energyScaleFactor = 1; // todo, just setting this because of div by 0
        protected boolean canChargeExternally;

        public static @NotNull Builder of(@NotNull String domain, @NotNull String id,
                                          @NotNull EntityEquipmentSlot slot) {
            return new Builder(domain, id, slot);
        }

        private Builder(@NotNull String domain, @NotNull String id, @NotNull EntityEquipmentSlot slot) {
            super(domain, id, slot);
        }

        public Builder electric(int tier, long maxCharge) {
            this.tier = tier;
            this.maxCharge = maxCharge;
            return this;
        }

        public Builder electricCost(long energyPerUse) {
            this.energyPerUse = energyPerUse;
            return this;
        }

        public Builder electricCost(long energyPerUse, int energyScaleFactor) {
            this.energyPerUse = energyPerUse;
            this.energyScaleFactor = energyScaleFactor;
            return this;
        }

        public Builder chargeOtherItems() {
            this.canChargeExternally = true;
            return this;
        }

        @Override
        public Supplier<ItemGTElectricArmor> supply(IGTArmorDefinition definition) {
            return () -> new ItemGTElectricArmor(domain, id, definition, tier, maxCharge, energyPerUse,
                    energyScaleFactor, canChargeExternally);
        }

        @Override
        public Builder cast(ArmorBuilder<ItemGTElectricArmor, Builder> builder) {
            return (Builder) builder;
        }
    }
}
