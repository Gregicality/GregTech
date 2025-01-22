package gregtech.api.util.input;

import net.minecraft.entity.player.EntityPlayerMP;

public interface IKeyPressedListener {

    void onKeyPressed(EntityPlayerMP player, KeyBind keyPressed);
}
