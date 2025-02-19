package morph.avaritia.api;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IHaloRenderItem {

    @SideOnly (Side.CLIENT)
    boolean shouldDrawHalo(ItemStack stack);

    @SideOnly (Side.CLIENT)
    TextureAtlasSprite getHaloTexture(ItemStack stack);

    @SideOnly (Side.CLIENT)
    int getHaloColour(ItemStack stack);

    @SideOnly (Side.CLIENT)
    int getHaloSize(ItemStack stack);

    @SideOnly (Side.CLIENT)
    boolean shouldDrawPulse(ItemStack stack);

}
