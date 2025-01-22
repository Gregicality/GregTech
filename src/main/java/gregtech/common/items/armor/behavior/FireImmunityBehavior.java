package gregtech.common.items.armor.behavior;

import gregtech.api.items.armoritem.IArmorBehavior;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FireImmunityBehavior implements IArmorBehavior {

    public static FireImmunityBehavior INSTANCE = new FireImmunityBehavior();

    @Override
    public void onArmorTick(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
        player.isImmuneToFire = true;
        if (player.isBurning()) {
            player.extinguish();
        }
    }

    @Override
    public void onArmorUnequip(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
        player.isImmuneToFire = false;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip) {
        tooltip.add(I18n.format("metaarmor.tooltip.burning"));
    }
}
