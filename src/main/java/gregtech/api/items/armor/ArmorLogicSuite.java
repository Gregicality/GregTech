package gregtech.api.items.armor;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.stats.IItemHUDProvider;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@Deprecated
public abstract class ArmorLogicSuite implements ISpecialArmorLogic, IItemHUDProvider {

    protected final int energyPerUse;
    protected final int tier;
    protected final long maxCapacity;
    protected final EntityEquipmentSlot SLOT;

    protected ArmorLogicSuite(int energyPerUse, long maxCapacity, int tier, EntityEquipmentSlot slot) {
        this.energyPerUse = energyPerUse;
        this.maxCapacity = maxCapacity;
        this.tier = tier;
        this.SLOT = slot;
    }

    @Override
    public abstract void onArmorTick(World world, EntityPlayer player, ItemStack itemStack);

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor, DamageSource source,
                                         double damage, EntityEquipmentSlot equipmentSlot) {
        IElectricItem item = armor.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (item == null) {
            return new ArmorProperties(0, 0.0, 0);
        }
        int damageLimit = Integer.MAX_VALUE;
        if (source.isUnblockable()) return new ArmorProperties(0, 0.0, 0);
        if (energyPerUse > 0) damageLimit = (int) Math.min(damageLimit, item.getCharge() * 1.0 / energyPerUse * 25.0);
        return new ArmorProperties(0, getAbsorption(armor) * getDamageAbsorption(), damageLimit);
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
        IElectricItem item = armor.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (item == null) return 0;
        if (item.getCharge() >= energyPerUse) {
            return (int) Math.round(20.0F * this.getAbsorption(armor) * this.getDamageAbsorption());
        } else {
            return (int) Math.round(4.0F * this.getAbsorption(armor) * this.getDamageAbsorption());
        }
    }

    public void addInfo(ItemStack itemStack, List<String> lines) {
        int armor = (int) Math.round(20.0F * this.getAbsorption(itemStack) * this.getDamageAbsorption());
        if (armor > 0)
            lines.add(I18n.format("attribute.modifier.plus.0", armor, I18n.format("attribute.name.generic.armor")));
    }

    public ActionResult<ItemStack> onRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.getHeldItem(hand).getItem() instanceof ArmorMetaItem) {
            ItemStack armor = player.getHeldItem(hand);
            if (armor.getItem() instanceof ArmorMetaItem &&
                    player.inventory.armorInventory.get(SLOT.getIndex()).isEmpty() && !player.isSneaking()) {
                player.inventory.armorInventory.set(SLOT.getIndex(), armor.copy());
                player.setHeldItem(hand, ItemStack.EMPTY);
                player.playSound(new SoundEvent(new ResourceLocation("item.armor.equip_generic")), 1.0F, 1.0F);
                return ActionResult.newResult(EnumActionResult.SUCCESS, armor);
            }
        }

        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack itemStack) {
        return SLOT;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "";
    }

    public double getDamageAbsorption() {
        return 0;
    }

    @SideOnly(Side.CLIENT)
    protected static void addCapacityHUD(ItemStack stack, ArmorUtils.ModularHUD hud) {
        IElectricItem cont = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (cont == null) return;
        if (cont.getCharge() == 0) return;
        float energyMultiplier = cont.getCharge() * 100.0F / cont.getMaxCharge();
        hud.newString(I18n.format("metaarmor.hud.energy_lvl", String.format("%.1f", energyMultiplier) + "%"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldDrawHUD() {
        return this.SLOT == EntityEquipmentSlot.CHEST;
    }

    protected float getAbsorption(ItemStack itemStack) {
        switch (this.getEquipmentSlot(itemStack)) {
            case HEAD:
            case FEET:
                return 0.15F;
            case CHEST:
                return 0.4F;
            case LEGS:
                return 0.3F;
            default:
                return 0.0F;
        }
    }
}
