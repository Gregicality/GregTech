package gregtech.api.items.armoritem;

import gregtech.api.util.input.KeyBind;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface IArmorBehavior {

    /**
     * Called every tick that this behavior's armor item is equipped.
     */
    default void onArmorTick(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {}

    /** Called when this behavior's armor item is unequipped. */
    default void onArmorUnequip(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {}

    /**
     * Called when this behavior's armor item is equipped, as well as when the player logs into the world or is cloned.
     */
    default void onArmorEquip(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {}

    /** Add to this behavior's armor item tooltip. */
    default void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip) {}

    default Set<KeyBind> getListenedKeys() {
        return Collections.emptySet();
    }

    default void onKeyPressed(@NotNull ItemStack stack, @NotNull EntityPlayer player, KeyBind keyPressed) {}

    default void addBehaviorNBT(@NotNull ItemStack stack, @NotNull NBTTagCompound tag) {}

    /**
     * Add a capability to this behavior's armor.
     * Recommended to only use this if no other options exist.
     *
     * @param stack the armor
     * @param tag   the capability nbt of the item
     */
    default ICapabilityProvider createProvider(@NotNull ItemStack stack, @Nullable NBTTagCompound tag) {
        return null;
    }

    // TODO implement HUD
    /** Add to the Armor HUD text. */
    @SideOnly(Side.CLIENT)
    default void addHudInformation(@NotNull ItemStack stack, @NotNull List<String> hudText) {}

    /** Get the equipment slot for this behavior's armor item. Provided since the method call is somewhat obscure. */
    default EntityEquipmentSlot getEquipmentSlot(@NotNull ItemStack stack) {
        return EntityLiving.getSlotForItemStack(stack);
    }
}
