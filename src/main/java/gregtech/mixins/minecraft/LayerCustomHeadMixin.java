package gregtech.mixins.minecraft;

import gregtech.api.items.armoritem.IGTArmor;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LayerCustomHead.class)
@Deprecated
public class LayerCustomHeadMixin {

    @WrapOperation(method = "doRenderLayer",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItem(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V"))
    public void shouldNotRenderHead(ItemRenderer instance, EntityLivingBase entitylivingbaseIn, ItemStack itemstack,
                                    ItemCameraTransforms.TransformType transformType, Operation<Void> original) {
        if (gregTechCEu$shouldNotRenderHeadItem(entitylivingbaseIn)) {
            return;
        }
        original.call(instance, entitylivingbaseIn, itemstack, transformType);
    }

    @Unique
    private static boolean gregTechCEu$shouldNotRenderHeadItem(EntityLivingBase entityLivingBase) {
        ItemStack itemStack = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        return (itemStack.getItem() instanceof IGTArmor &&
                itemStack.getItem().getEquipmentSlot(itemStack) == EntityEquipmentSlot.HEAD);
    }
}
