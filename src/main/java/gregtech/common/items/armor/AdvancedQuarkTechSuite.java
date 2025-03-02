package gregtech.common.items.armor;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.input.KeyBind;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class AdvancedQuarkTechSuite extends QuarkTechSuite implements IJetpack {

    // A replacement for checking the current world time, to get around the gamerule that stops it
    private long timer = 0L;
    private List<Pair<NonNullList<ItemStack>, List<Integer>>> inventoryIndexMap;

    public AdvancedQuarkTechSuite(int energyPerUse, long capacity, int tier) {
        super(EntityEquipmentSlot.CHEST, energyPerUse, capacity, tier);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack item) {
        IElectricItem cont = item.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (cont == null) {
            return;
        }

        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(item);
        boolean hoverMode = data.hasKey("hover") && data.getBoolean("hover");
        boolean cancelInertiaMode = data.hasKey("cancelInertia") && data.getBoolean("cancelInertia");
        byte toggleTimer = data.hasKey("toggleTimer") ? data.getByte("toggleTimer") : 0;
        boolean canShare = data.hasKey("canShare") && data.getBoolean("canShare");

        if (toggleTimer == 0 && KeyBind.ARMOR_HOVER.isKeyDown(player)) {
            hoverMode = !hoverMode;
            toggleTimer = 5;
            data.setBoolean("hover", hoverMode);
            if (!world.isRemote) {
                if (hoverMode)
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.hover.enable"), true);
                else
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.hover.disable"), true);
            }
        }

        if (toggleTimer == 0 && KeyBind.ARMOR_CANCEL_INERTIA.isKeyDown(player)) {
            cancelInertiaMode = !cancelInertiaMode;
            toggleTimer = 5;
            data.setBoolean("cancelInertia", cancelInertiaMode);
            if (!world.isRemote) {
                if (cancelInertiaMode)
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.cancel_inertia.enable"),
                            true);
                else
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.cancel_inertia.disable"),
                            true);
            }
        }

        if (toggleTimer == 0 && KeyBind.ARMOR_CHARGING.isKeyDown(player)) {
            canShare = !canShare;
            toggleTimer = 5;
            if (!world.isRemote) {
                if (canShare && cont.getCharge() == 0)
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.qts.share.error"), true);
                else if (canShare)
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.qts.share.enable"), true);
                else
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.qts.share.disable"), true);
            }

            // Only allow for charging to be enabled if charge is nonzero
            canShare = canShare && (cont.getCharge() != 0);
            data.setBoolean("canShare", canShare);
        }

        performFlying(player, hoverMode, cancelInertiaMode, item);

        if (player.isBurning())
            player.extinguish();

        // Charging mechanics
        if (canShare && !world.isRemote) {
            // Check for new things to charge every 5 seconds
            if (timer % 100 == 0)
                inventoryIndexMap = ArmorUtils.getChargeableItem(player, cont.getTier());

            if (inventoryIndexMap != null && !inventoryIndexMap.isEmpty()) {
                // Charge all inventory slots
                for (int i = 0; i < inventoryIndexMap.size(); i++) {
                    Pair<NonNullList<ItemStack>, List<Integer>> inventoryMap = inventoryIndexMap.get(i);
                    Iterator<Integer> inventoryIterator = inventoryMap.getValue().iterator();
                    while (inventoryIterator.hasNext()) {
                        int slot = inventoryIterator.next();
                        IElectricItem chargable = inventoryMap.getKey().get(slot)
                                .getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);

                        // Safety check the null, it should not actually happen. Also don't try and charge itself
                        if (chargable == null || chargable == cont) {
                            inventoryIterator.remove();
                            continue;
                        }

                        long attemptedChargeAmount = chargable.getTransferLimit() * 10;

                        // Accounts for tick differences when charging items
                        if (chargable.getCharge() < chargable.getMaxCharge() && cont.canUse(attemptedChargeAmount) &&
                                timer % 10 == 0) {
                            long delta = chargable.charge(attemptedChargeAmount, cont.getTier(), true, false);
                            if (delta > 0) {
                                cont.discharge(delta, cont.getTier(), true, false, false);
                            }
                            if (chargable.getCharge() == chargable.getMaxCharge()) {
                                inventoryIterator.remove();
                            }
                            player.inventoryContainer.detectAndSendChanges();
                        }
                    }

                    if (inventoryMap.getValue().isEmpty())
                        inventoryIndexMap.remove(inventoryMap);
                }
            }
        }

        if (toggleTimer > 0) toggleTimer--;

        data.setBoolean("canShare", canShare);
        data.setBoolean("cancelInertia", cancelInertiaMode);
        data.setBoolean("hover", hoverMode);
        data.setByte("toggleTimer", toggleTimer);
        player.inventoryContainer.detectAndSendChanges();

        timer++;
        if (timer == Long.MAX_VALUE)
            timer = 0;
    }

    @Override
    public void addInfo(ItemStack itemStack, List<String> lines) {
        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(itemStack);
        String state;
        if (data.hasKey("canShare")) {
            state = data.getBoolean("canShare") ? I18n.format("metaarmor.hud.status.enabled") :
                    I18n.format("metaarmor.hud.status.disabled");
        } else {
            state = I18n.format("metaarmor.hud.status.disabled");
        }
        lines.add(I18n.format("metaarmor.energy_share.tooltip", state));
        lines.add(I18n.format("metaarmor.energy_share.tooltip.guide"));
        String status = I18n.format("metaarmor.hud.status.disabled");
        if (data.hasKey("hover")) {
            if (data.getBoolean("hover"))
                status = I18n.format("metaarmor.hud.status.enabled");
        }
        lines.add(I18n.format("metaarmor.hud.hover_mode", status));
        if (data.hasKey("cancelInertia")) {
            if (data.getBoolean("cancelInertia"))
                status = I18n.format("metaarmor.hud.status.enabled");
        }
        lines.add(I18n.format("metaarmor.hud.cancel_inertia_mode", status));
        super.addInfo(itemStack, lines);
    }

    @Override
    public ActionResult<ItemStack> onRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.getHeldItem(hand).getItem() instanceof ArmorMetaItem<?> && player.isSneaking()) {
            NBTTagCompound data = GTUtility.getOrCreateNbtCompound(player.getHeldItem(hand));
            boolean canShare = data.hasKey("canShare") && data.getBoolean("canShare");
            IElectricItem cont = player.getHeldItem(hand).getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM,
                    null);
            if (cont == null) {
                return ActionResult.newResult(EnumActionResult.FAIL, player.getHeldItem(hand));
            }

            canShare = !canShare;
            if (!world.isRemote) {
                if (canShare && cont.getCharge() == 0) {
                    player.sendMessage(new TextComponentTranslation("metaarmor.energy_share.error"));
                } else if (canShare) {
                    player.sendMessage(new TextComponentTranslation("metaarmor.energy_share.enable"));
                } else {
                    player.sendMessage(new TextComponentTranslation("metaarmor.energy_share.disable"));
                }
            }

            canShare = canShare && (cont.getCharge() != 0);
            data.setBoolean("canShare", canShare);
            return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        } else {
            return super.onRightClick(world, player, hand);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawHUD(ItemStack item) {
        addCapacityHUD(item, this.HUD);
        IElectricItem cont = item.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (cont == null) return;
        if (!cont.canUse(energyPerUse)) return;
        NBTTagCompound data = item.getTagCompound();
        if (data != null) {
            if (data.hasKey("canShare")) {
                String status = data.getBoolean("canShare") ? "metaarmor.hud.status.enabled" :
                        "metaarmor.hud.status.disabled";
                this.HUD.newString(I18n.format("mataarmor.hud.supply_mode", I18n.format(status)));
            }

            if (data.hasKey("hover")) {
                String status = data.getBoolean("hover") ? "metaarmor.hud.status.enabled" :
                        "metaarmor.hud.status.disabled";
                this.HUD.newString(I18n.format("metaarmor.hud.hover_mode", I18n.format(status)));
            }

            if (data.hasKey("cancelInertia")) {
                String status = data.getBoolean("cancelInertia") ? "metaarmor.hud.status.enabled" :
                        "metaarmor.hud.status.disabled";
                this.HUD.newString(I18n.format("metaarmor.hud.cancel_inertia_mode", I18n.format(status)));
            }
        }
        this.HUD.draw();
        this.HUD.reset();
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor, DamageSource source,
                                         double damage, EntityEquipmentSlot equipmentSlot) {
        int damageLimit = Integer.MAX_VALUE;
        IElectricItem item = armor.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (item == null) {
            return new ArmorProperties(0, 0, damageLimit);
        }
        if (energyPerUse > 0) {
            damageLimit = (int) Math.min(damageLimit, 25.0D * item.getCharge() / (energyPerUse * 250.0D));
        }
        return new ArmorProperties(8, getDamageAbsorption() * getAbsorption(armor), damageLimit);
    }

    @Override
    public boolean handleUnblockableDamage(EntityLivingBase entity, @NotNull ItemStack armor, DamageSource source,
                                           double damage, EntityEquipmentSlot equipmentSlot) {
        return source != DamageSource.FALL && source != DamageSource.DROWN && source != DamageSource.STARVE;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "gregtech:textures/armor/advanced_quark_tech_suite_1.png";
    }

    @Override
    public double getDamageAbsorption() {
        return 1.5D;
    }

    @Override
    public boolean canUseEnergy(@NotNull ItemStack stack, int amount) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return false;
        return container.canUse(amount);
    }

    @Override
    public void drainEnergy(@NotNull ItemStack stack, int amount) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return;
        container.discharge(amount, tier, true, false, false);
    }

    @Override
    public boolean hasEnergy(@NotNull ItemStack stack) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return false;
        return container.getCharge() > 0;
    }

    private static IElectricItem getIElectricItem(@NotNull ItemStack stack) {
        return stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
    }

    @Override
    public double getSprintEnergyModifier() {
        return 6.0D;
    }

    @Override
    public double getSprintSpeedModifier() {
        return 2.4D;
    }

    @Override
    public double getVerticalHoverSpeed() {
        return 0.45D;
    }

    @Override
    public double getVerticalHoverSlowSpeed() {
        return 0.0D;
    }

    @Override
    public double getVerticalAcceleration() {
        return 0.15D;
    }

    @Override
    public double getVerticalSpeed() {
        return 0.9D;
    }

    @Override
    public double getSidewaysSpeed() {
        return 0.21D;
    }

    @Override
    public EnumParticleTypes getParticle() {
        return null;
    }

    @Override
    public float getFallDamageReduction() {
        return 8f;
    }
}
