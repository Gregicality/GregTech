package gregtech.mixins.mui2;

import net.minecraft.client.gui.FontRenderer;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.text.RichTextCompiler;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = RichTextCompiler.class, remap = false)
public class RichTextCompilerMixin {

    @Shadow
    private int x;

    @Shadow
    private FontRenderer fr;

    @ModifyArg(method = "trimRight",
               at = @At(value = "INVOKE",
                        target = "Ljava/lang/String;substring(II)Ljava/lang/String;"),
               index = 1)
    private static int fixTrim(int beginIndex) {
        return beginIndex + 1;
    }

    @Inject(method = "compile",
            at = @At(value = "INVOKE",
                     target = "Lcom/cleanroommc/modularui/drawable/text/RichTextCompiler;addLineElement(Ljava/lang/Object;)V",
                     ordinal = 0))
    private void moveXString(List<Object> raw, CallbackInfo ci, @Local IKey key) {
        x += fr.getStringWidth(key.get());
    }
}
