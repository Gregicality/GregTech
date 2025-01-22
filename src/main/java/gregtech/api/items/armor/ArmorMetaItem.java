package gregtech.api.items.armor;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.common.creativetab.GTCreativeTabs;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Deprecated
public class ArmorMetaItem<T extends ArmorMetaItem<?>.ArmorMetaValueItem> extends MetaItem<T> implements ISpecialArmor {

    public ArmorMetaItem() {
        super((short) 0);
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_TOOLS);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T constructMetaValueItem(short metaValue, String unlocalizedName) {
        return (T) new ArmorMetaValueItem(metaValue, unlocalizedName);
    }

    @NotNull
    private IArmorLogic getArmorLogic(ItemStack itemStack) {
        T metaValueItem = getItem(itemStack);
        return metaValueItem == null ? new DummyArmorLogic(EntityEquipmentSlot.HEAD, "") :
                metaValueItem.getArmorLogic();
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor, DamageSource source,
                                         double damage, int slot) {
        return new ArmorProperties(0, 0, Integer.MAX_VALUE);
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, @NotNull ItemStack armor, int slot) {
        return 0;
    }

    @Override
    public void damageArmor(EntityLivingBase entity, @NotNull ItemStack stack, DamageSource source, int damage,
                            int slot) {}

    @Override
    public boolean isValidArmor(@NotNull ItemStack stack, @NotNull EntityEquipmentSlot armorType,
                                @NotNull Entity entity) {
        IArmorLogic armorLogic = getArmorLogic(stack);
        return super.isValidArmor(stack, armorType, entity) && armorLogic.getEquipmentSlot(stack) == armorType;
    }

    @Nullable
    @Override
    public EntityEquipmentSlot getEquipmentSlot(@NotNull ItemStack stack) {
        IArmorLogic armorLogic = getArmorLogic(stack);
        return armorLogic.getEquipmentSlot(stack);
    }

    @Nullable
    @Override
    public String getArmorTexture(@NotNull ItemStack stack, @NotNull Entity entity, @NotNull EntityEquipmentSlot slot,
                                  @NotNull String type) {
        IArmorLogic armorLogic = getArmorLogic(stack);
        return armorLogic.getArmorTexture(stack, entity, slot, type);
    }

    @Override
    public void addInformation(@NotNull ItemStack itemStack, @Nullable World worldIn, @NotNull List<String> lines,
                               @NotNull ITooltipFlag tooltipFlag) {
        lines.add(TextFormatting.RED + "Deprecated Item! Convert in an Assembler to get the new version"); // todo lang
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    public class ArmorMetaValueItem extends MetaValueItem {

        private IArmorLogic armorLogic;

        protected ArmorMetaValueItem(int metaValue, String unlocalizedName) {
            super(metaValue, unlocalizedName);
            setMaxStackSize(1);
        }

        @NotNull
        public IArmorLogic getArmorLogic() {
            return armorLogic;
        }

        public ArmorMetaValueItem setArmorLogic(EntityEquipmentSlot slot, String armorTextureName) {
            this.armorLogic = new DummyArmorLogic(slot, armorTextureName);
            return this;
        }

        public ArmorMetaValueItem setArmorLogic(IArmorLogic armorLogic) {
            Preconditions.checkNotNull(armorLogic, "Cannot set ArmorLogic to null");
            this.armorLogic = armorLogic;
            return this;
        }

        @Override
        public ArmorMetaValueItem setRarity(EnumRarity rarity) {
            return (ArmorMetaValueItem) super.setRarity(rarity);
        }

        @Override
        public ArmorMetaValueItem setInvisible() {
            super.setInvisible();
            return this;
        }
    }

    private static class DummyArmorLogic implements IArmorLogic {

        private final EntityEquipmentSlot slot;
        private final String armorTextureName;

        public DummyArmorLogic(EntityEquipmentSlot slot, String armorTextureName) {
            this.slot = slot;
            this.armorTextureName = armorTextureName;
        }

        @Override
        public EntityEquipmentSlot getEquipmentSlot(ItemStack itemStack) {
            return slot;
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
            return String.format("gregtech:textures/items/armors/%s.png", armorTextureName);
        }
    }
}
