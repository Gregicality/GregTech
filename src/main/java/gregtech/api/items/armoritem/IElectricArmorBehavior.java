package gregtech.api.items.armoritem;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

public interface IElectricArmorBehavior extends IArmorBehavior {

    @Override
    default void onArmorTick(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            onArmorTick(world, player, stack, electricItem);
        }
    }

    void onArmorTick(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack,
                     @NotNull IElectricItem electricItem);
}
