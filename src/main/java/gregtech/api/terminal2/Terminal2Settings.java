package gregtech.api.terminal2;

import gregtech.api.util.GTUtility;
import gregtech.common.mui.widget.FileTexture;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.UITexture;

import java.io.File;

public class Terminal2Settings {

    private static String currentBackground = null;
    public static File backgroundsDir = new File(Terminal2.TERMINAL_PATH, "backgrounds");
    private static final UITexture defaultBackground = UITexture
            .fullImage(GTUtility.gregtechId("textures/gui/terminal/terminal_background.png"));

    public static IDrawable getBackgroundDrawable() {
        if (currentBackground == null) {
            return defaultBackground;
        }
        return new FileTexture(new File(backgroundsDir, currentBackground));
    }

    public static void setBackground(String file) {
        if (file.equals("default")) {
            currentBackground = null;
        } else {
            currentBackground = file;
        }
    }
}
