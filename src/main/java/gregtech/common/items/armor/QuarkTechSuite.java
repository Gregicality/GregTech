package gregtech.common.items.armor;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorLogicSuite;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.input.KeyBind;
import gregtech.common.items.MetaItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class QuarkTechSuite extends ArmorLogicSuite implements IStepAssist {

    protected static final Map<Potion, Integer> potionRemovalCost = new IdentityHashMap<>();
    private float charge = 0.0F;

    @SideOnly(Side.CLIENT)
    protected ArmorUtils.ModularHUD HUD;

    public QuarkTechSuite(EntityEquipmentSlot slot, int energyPerUse, long capacity, int tier) {
        super(energyPerUse, capacity, tier, slot);
        potionRemovalCost.put(MobEffects.POISON, 10000);
        potionRemovalCost.put(MobEffects.WITHER, 25000);
        if (ArmorUtils.SIDE.isClient() && this.shouldDrawHUD()) {
            // noinspection NewExpressionSideOnly
            HUD = new ArmorUtils.ModularHUD();
        }
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        IElectricItem item = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (item == null)
            return;

        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(itemStack);
        byte toggleTimer = data.hasKey("toggleTimer") ? data.getByte("toggleTimer") : 0;

        if (!player.getItemStackFromSlot(EntityEquipmentSlot.HEAD)
                .isItemEqual(MetaItems.QUANTUM_HELMET.getStackForm())) {
            disableNightVision(world, player, false);
        } else if (!player.getItemStackFromSlot(EntityEquipmentSlot.CHEST)
                .isItemEqual(MetaItems.QUANTUM_CHESTPLATE.getStackForm()) &&
                !player.getItemStackFromSlot(EntityEquipmentSlot.CHEST)
                        .isItemEqual(MetaItems.QUANTUM_CHESTPLATE_ADVANCED.getStackForm())) {
                            if (!world.isRemote) player.isImmuneToFire = false;
                        }

        boolean ret = false;
        if (SLOT == EntityEquipmentSlot.HEAD) {
            int air = player.getAir();
            if (item.canUse(energyPerUse / 100) && air < 100) {
                player.setAir(air + 200);
                item.discharge(energyPerUse / 100, item.getTier(), true, false, false);
                ret = true;
            }

            if (item.canUse(energyPerUse / 10) && player.getFoodStats().needFood()) {
                int slotId = -1;
                for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                    ItemStack current = player.inventory.mainInventory.get(i);
                    if (current.getItem() instanceof ItemFood) {
                        slotId = i;
                        break;
                    }
                }

                if (slotId > -1) {
                    ItemStack stack = player.inventory.mainInventory.get(slotId);
                    ActionResult<ItemStack> result = ArmorUtils.canEat(player, stack);
                    stack = result.getResult();
                    if (stack.isEmpty())
                        player.inventory.mainInventory.set(slotId, ItemStack.EMPTY);

                    if (result.getType() == EnumActionResult.SUCCESS)
                        item.discharge(energyPerUse / 10, item.getTier(), true, false, false);

                    ret = true;
                }
            }

            for (PotionEffect effect : new LinkedList<>(player.getActivePotionEffects())) {
                Potion potion = effect.getPotion();
                Integer cost = potionRemovalCost.get(potion);
                if (cost != null) {
                    cost = cost * (effect.getAmplifier() + 1);
                    if (item.canUse(cost)) {
                        item.discharge(cost, item.getTier(), true, false, false);
                        player.removePotionEffect(potion);
                    }
                }
            }

            boolean nightvision = data.getBoolean("Nightvision");
            if (toggleTimer == 0 && KeyBind.ARMOR_MODE_SWITCH.isKeyDown(player)) {
                toggleTimer = 5;
                if (!nightvision && item.getCharge() >= 4) {
                    nightvision = true;
                    if (!world.isRemote)
                        player.sendStatusMessage(new TextComponentTranslation("metaarmor.qts.nightvision.enabled"),
                                true);
                } else if (nightvision) {
                    nightvision = false;
                    disableNightVision(world, player, true);
                } else {
                    if (!world.isRemote) {
                        player.sendStatusMessage(new TextComponentTranslation("metaarmor.qts.nightvision.error"), true);
                    }
                }

                if (!world.isRemote) {
                    data.setBoolean("Nightvision", nightvision);
                }
            }

            if (nightvision && !world.isRemote && item.getCharge() >= energyPerUse) {
                player.removePotionEffect(MobEffects.BLINDNESS);
                player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 999999, 0, true, false));
                item.discharge(4, this.tier, true, false, false);
            }

            if (!world.isRemote && toggleTimer > 0) {
                --toggleTimer;
                data.setByte("toggleTimer", toggleTimer);
            }
        } else if (SLOT == EntityEquipmentSlot.CHEST && !player.isImmuneToFire) {
            player.isImmuneToFire = true;
            if (player.isBurning())
                player.extinguish();
        } else if (SLOT == EntityEquipmentSlot.LEGS) {
            if (item.canUse(energyPerUse / 100) && (player.onGround || player.isInWater()) &&
                    KeyBind.VANILLA_FORWARD.isKeyDown(player) && (player.isSprinting())) {
                byte consumerTicks = data.getByte("consumerTicks");
                ++consumerTicks;
                if (consumerTicks >= 10) {
                    consumerTicks = 0;
                    item.discharge(energyPerUse / 100, item.getTier(), true, false, false);
                    ret = true;
                }
                data.setByte("consumerTicks", consumerTicks);
                float speed = 0.25F;
                if (player.isInWater()) {
                    speed = 0.1F;
                    if (KeyBind.VANILLA_JUMP.isKeyDown(player)) {
                        player.motionY += 0.1D;
                    }
                }
                player.moveRelative(0.0F, 0.0F, 1.0F, speed);
            } else
                if (item.canUse(energyPerUse / 100) && player.isInWater() && KeyBind.VANILLA_SNEAK.isKeyDown(player) ||
                        KeyBind.VANILLA_JUMP.isKeyDown(player)) {
                            byte consumerTicks = data.getByte("consumerTicks");
                            ++consumerTicks;
                            if (consumerTicks >= 10) {
                                consumerTicks = 0;
                                item.discharge(energyPerUse / 100, item.getTier(), true, false, false);
                                ret = true;
                            }
                            data.setByte("consumerTicks", consumerTicks);
                            double acceleration = 0.085D;
                            if (KeyBind.VANILLA_SNEAK.isKeyDown(player))
                                player.motionY -= acceleration;
                            if (KeyBind.VANILLA_JUMP.isKeyDown(player))
                                player.motionY += acceleration;
                        }
        } else if (SLOT == EntityEquipmentSlot.FEET) {
            if (!world.isRemote) {
                boolean onGround = !data.hasKey("onGround") || data.getBoolean("onGround");
                if (onGround && !player.onGround && KeyBind.VANILLA_JUMP.isKeyDown(player)) {
                    item.discharge(energyPerUse / 100, item.getTier(), true, false, false);
                    ret = true;
                }

                if (player.onGround != onGround) {
                    data.setBoolean("onGround", player.onGround);
                }
            } else {
                if (item.canUse(energyPerUse / 100) && player.onGround) {
                    this.charge = 1.0F;
                }

                if (player.motionY >= 0.0D && this.charge > 0.0F && !player.isInWater()) {
                    if (KeyBind.VANILLA_JUMP.isKeyDown(player)) {
                        if (this.charge == 1.0F) {
                            player.motionX *= 3.6D;
                            player.motionZ *= 3.6D;
                        }

                        player.motionY += this.charge * 0.32F;
                        this.charge = (float) (this.charge * 0.7D);
                    } else if (this.charge < 1.0F) {
                        this.charge = 0.0F;
                    }
                }
            }
            updateStepHeight(player);
        }

        if (ret) {
            player.inventoryContainer.detectAndSendChanges();
        }
    }

    public static void disableNightVision(@NotNull World world, EntityPlayer player, boolean sendMsg) {
        if (!world.isRemote) {
            player.removePotionEffect(MobEffects.NIGHT_VISION);
            if (sendMsg)
                player.sendStatusMessage(new TextComponentTranslation("metaarmor.qts.nightvision.disabled"), true);
        }
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
            damageLimit = (int) Math.min(damageLimit, 25.0D * item.getCharge() / (energyPerUse * 100.0D));
        }

        if (source == DamageSource.FALL) {
            if (SLOT == EntityEquipmentSlot.FEET) {
                return new ArmorProperties(10, 1.0D, damageLimit);
            }

            if (SLOT == EntityEquipmentSlot.LEGS) {
                return new ArmorProperties(9, 0.8D, damageLimit);
            }
        }
        return new ArmorProperties(8, getDamageAbsorption() * getAbsorption(armor), damageLimit);
    }

    @Override
    public boolean handleUnblockableDamage(EntityLivingBase entity, @NotNull ItemStack armor, DamageSource source,
                                           double damage, EntityEquipmentSlot equipmentSlot) {
        return source != DamageSource.FALL && source != DamageSource.DROWN && source != DamageSource.STARVE &&
                source != DamageSource.OUT_OF_WORLD;
    }

    @Override
    public void damageArmor(EntityLivingBase entity, @NotNull ItemStack itemStack, DamageSource source, int damage,
                            EntityEquipmentSlot equipmentSlot) {
        IElectricItem item = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (item == null) {
            return;
        }
        item.discharge(energyPerUse / 100L * damage, item.getTier(), true, false, false);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        ItemStack currentChest = Minecraft.getMinecraft().player.inventory
                .armorItemInSlot(EntityEquipmentSlot.CHEST.getIndex());
        ItemStack advancedChest = MetaItems.QUANTUM_CHESTPLATE_ADVANCED.getStackForm();
        String armorTexture = "quark_tech_suite";
        if (advancedChest.isItemEqual(currentChest)) armorTexture = "advanced_quark_tech_suite";
        return SLOT != EntityEquipmentSlot.LEGS ?
                String.format("gregtech:textures/armor/%s_1.png", armorTexture) :
                String.format("gregtech:textures/armor/%s_2.png", armorTexture);
    }

    @Override
    public double getDamageAbsorption() {
        return SLOT == EntityEquipmentSlot.CHEST ? 1.2D : 1.0D;
    }

    @Override
    public float getHeatResistance() {
        return 0.5f;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawHUD(ItemStack item) {
        addCapacityHUD(item, this.HUD);
        this.HUD.draw();
        this.HUD.reset();
    }

    @Override
    public void addInfo(ItemStack itemStack, List<String> lines) {
        super.addInfo(itemStack, lines);
        if (SLOT == EntityEquipmentSlot.HEAD) {
            NBTTagCompound nbtData = GTUtility.getOrCreateNbtCompound(itemStack);
            boolean nv = nbtData.getBoolean("Nightvision");
            if (nv) {
                lines.add(I18n.format("metaarmor.message.nightvision.enabled"));
            } else {
                lines.add(I18n.format("metaarmor.message.nightvision.disabled"));
            }
            lines.add(I18n.format("metaarmor.tooltip.potions"));
            lines.add(I18n.format("metaarmor.tooltip.breath"));
            lines.add(I18n.format("metaarmor.tooltip.autoeat"));
        } else if (SLOT == EntityEquipmentSlot.CHEST) {
            lines.add(I18n.format("metaarmor.tooltip.burning"));
        } else if (SLOT == EntityEquipmentSlot.LEGS) {
            lines.add(I18n.format("metaarmor.tooltip.speed"));
        } else if (SLOT == EntityEquipmentSlot.FEET) {
            lines.add(I18n.format("metaarmor.tooltip.stepassist"));
            lines.add(I18n.format("metaarmor.tooltip.falldamage"));
            lines.add(I18n.format("metaarmor.tooltip.jump"));
        }
    }

    @Override
    public float getRadiationResistance() {
        return 0.25f;
    }
}
