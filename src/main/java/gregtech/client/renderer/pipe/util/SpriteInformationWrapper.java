package gregtech.client.renderer.pipe.util;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;

@SideOnly(Side.CLIENT)
public class SpriteInformationWrapper implements Supplier<SpriteInformation>, Consumer<SpriteInformation>,
                                      ObjIntConsumer<TextureAtlasSprite> {

    private SpriteInformation sprite;

    @Override
    public void accept(TextureAtlasSprite sprite, int colorID) {
        accept(new SpriteInformation(sprite, colorID));
    }

    @Override
    public void accept(SpriteInformation spriteInformation) {
        this.sprite = spriteInformation;
    }

    @Override
    public SpriteInformation get() {
        return this.sprite;
    }

    public static SpriteInformationWrapper[] array(int size) {
        SpriteInformationWrapper[] array = new SpriteInformationWrapper[size];
        for (int i = 0; i < size; i++) {
            array[i] = new SpriteInformationWrapper();
        }
        return array;
    }
}
