package gregtech.common.items.armor.behavior;

import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armoritem.ArmorHelper;
import gregtech.api.items.armoritem.IElectricArmorBehavior;
import gregtech.api.util.input.KeyBind;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

// All MobEffects warn that they "might be null" for some reason, so suppress it
@SuppressWarnings("ConstantConditions")
public class NightvisionBehavior implements IElectricArmorBehavior {

    public static final NightvisionBehavior INSTANCE = new NightvisionBehavior();

    protected NightvisionBehavior() {/**/}

    @Override
    public Set<KeyBind> getListenedKeys() {
        return Collections.singleton(KeyBind.ARMOR_MODE_SWITCH);
    }

    @Override
    public void onKeyPressed(@NotNull ItemStack stack, @NotNull EntityPlayer player, KeyBind keyPressed) {
        if (keyPressed == KeyBind.ARMOR_MODE_SWITCH) {
            NBTTagCompound tag = ArmorHelper.getBehaviorsTag(stack);
            boolean wasEnabled = tag.getBoolean(ArmorHelper.NIGHT_VISION_KEY);
            tag.setBoolean(ArmorHelper.NIGHT_VISION_KEY, !wasEnabled);

            if (wasEnabled) {
                player.removePotionEffect(MobEffects.NIGHT_VISION);
                player.sendStatusMessage(new TextComponentTranslation("metaarmor.message.nightvision.disabled"), true);
            } else {
                player.sendStatusMessage(new TextComponentTranslation("metaarmor.message.nightvision.enabled"), true);
            }
        }
    }

    @Override
    public void onArmorTick(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack,
                            @NotNull IElectricItem electricItem) {
        if (world.isRemote) return;
        NBTTagCompound tag = ArmorHelper.getBehaviorsTag(stack);
        if (tag.getBoolean(ArmorHelper.NIGHT_VISION_KEY) && electricItem.canUse(2)) {
            electricItem.discharge(2, Integer.MAX_VALUE, true, false, false);
            player.removePotionEffect(MobEffects.BLINDNESS);
            player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 999999, 0, true, false));
        }
    }

    @Override
    public void onArmorUnequip(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
        player.removePotionEffect(MobEffects.NIGHT_VISION);
    }

    @Override
    public void addBehaviorNBT(@NotNull ItemStack stack, @NotNull NBTTagCompound tag) {
        tag.setBoolean(ArmorHelper.NIGHT_VISION_KEY, false); // disabled by default
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip) {
        NBTTagCompound tag = ArmorHelper.getBehaviorsTag(stack);
        if (tag.getBoolean(ArmorHelper.NIGHT_VISION_KEY)) {
            tooltip.add(I18n.format("metaarmor.message.nightvision.enabled"));
        } else {
            tooltip.add(I18n.format("metaarmor.message.nightvision.disabled"));
        }
    }
}
