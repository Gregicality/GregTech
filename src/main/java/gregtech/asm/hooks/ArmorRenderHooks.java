package gregtech.asm.hooks;

import gregtech.api.items.armor.ArmorMetaItem;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
@Deprecated
public class ArmorRenderHooks {

    public static boolean shouldNotRenderHeadItem(EntityLivingBase entityLivingBase) {
        ItemStack itemStack = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        return isArmorItem(itemStack, EntityEquipmentSlot.HEAD);
    }

    public static boolean isArmorItem(ItemStack itemStack, EntityEquipmentSlot slot) {
        return (itemStack.getItem() instanceof ArmorMetaItem<?> &&
                itemStack.getItem().getEquipmentSlot(itemStack) == slot);
    }

    public static void renderArmorLayer(LayerArmorBase<ModelBase> layer, EntityLivingBase entity, float limbSwing,
                                        float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
                                        float headPitch, float scale, EntityEquipmentSlot slotIn) {
        ItemStack itemStack = entity.getItemStackFromSlot(slotIn);

        if (isArmorItem(itemStack, slotIn)) {
            ModelBase armorModel = layer.getModelFromSlot(slotIn);
            if (armorModel instanceof ModelBiped) {
                armorModel = ForgeHooksClient.getArmorModel(entity, itemStack, slotIn, (ModelBiped) armorModel);
            }
            armorModel.setModelAttributes(layer.renderer.getMainModel());
            armorModel.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
            layer.setModelSlotVisible(armorModel, slotIn);

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);

            GlStateManager.color(1, 1, 1, 1);
            layer.renderer.bindTexture(getArmorTexture(entity, itemStack, slotIn));
            armorModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

            if (itemStack.hasEffect()) {
                LayerArmorBase.renderEnchantedGlint(layer.renderer, entity, armorModel, limbSwing, limbSwingAmount,
                        partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
            }
        }
    }

    private static boolean isLegSlot(EntityEquipmentSlot equipmentSlot) {
        return equipmentSlot == EntityEquipmentSlot.LEGS;
    }

    private static ResourceLocation getArmorTexture(EntityLivingBase entity, ItemStack itemStack,
                                                    EntityEquipmentSlot slot) {
        ResourceLocation registryName = itemStack.getItem().getRegistryName();
        if (registryName == null) {
            throw new IllegalArgumentException(
                    "ItemStack " + itemStack.getTranslationKey() + "has a null registry name");
        }

        String s1 = String.format("%s:textures/models/armor/%s_layer_%d%s.png", registryName.getNamespace(),
                registryName.getPath(),
                (isLegSlot(slot) ? 2 : 1), "");
        return new ResourceLocation(ForgeHooksClient.getArmorTexture(entity, itemStack, s1, slot, null));
    }
}
