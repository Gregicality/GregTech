package morph.avaritia.api;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Any item implementing this that also binds itself to TODO INPUT MODEL NAME HERE.
 * Will automatically have the cosmic shader applied to the mask with the given opacity.
 */
public interface ICosmicRenderItem {

    /**
     * The mask where the cosmic overlay will be.
     *
     * @param stack  The stack being rendered.
     * @param player The entity holding the item, May be null, If null assume either inventory, or ground.
     * @return The masked area where the cosmic overlay will be.
     */
    @SideOnly (Side.CLIENT)
    TextureAtlasSprite getMaskTexture(ItemStack stack, @Nullable EntityLivingBase player);

    /**
     * The opacity that the mask overlay will be rendered with.
     *
     * @param stack  The stack being rendered.
     * @param player The entity holding the item, May be null, If null assume either inventory, or ground.
     * @return The opacity that the mask overlay will be rendered with.
     */
    @SideOnly (Side.CLIENT)
    float getMaskOpacity(ItemStack stack, @Nullable EntityLivingBase player);
}
