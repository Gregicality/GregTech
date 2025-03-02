package gregtech.mixins.mui2;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.Icon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Icon.class, remap = false)
public interface IconAccessor {

    @Accessor()
    IDrawable getDrawable();
}
