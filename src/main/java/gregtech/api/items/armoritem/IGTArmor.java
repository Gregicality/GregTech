package gregtech.api.items.armoritem;

import gregtech.api.items.armoritem.armorset.IArmorSet;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IGTArmor extends ISpecialArmor {

    String getDomain();

    String getId();

    @NotNull
    IGTArmorDefinition getDefinition();

    @NotNull
    List<IArmorBehavior> getBehaviors();

    @NotNull
    EntityEquipmentSlot getEquipmentSlot();

    @Nullable
    IArmorSet getArmorSet();

    /**
     * If behavior logic can be run at all. Most commonly used for energy checks on electric armors.
     * Typically used externally for behaviors which require event listeners to function properly.
     */
    boolean areBehaviorsActive(@NotNull ItemStack stack);

    void onArmorEquip(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack);

    void onArmorUnequip(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack);

    default @NotNull Item get() {
        return (Item) this;
    }

    @NotNull
    ItemStack getStack();

    @SideOnly(Side.CLIENT)
    default String getModelPath() {
        return getDomain() + ":" + "armors/" + getId();
    }

    @SideOnly(Side.CLIENT)
    default ModelResourceLocation getModelLocation() {
        return new ModelResourceLocation(getModelPath(), "inventory");
    }

    // todo material-based armors
    @SideOnly(Side.CLIENT)
    default int getColor(ItemStack stack, int tintIndex) {
        return 0xFFFFFF;
    }
}
