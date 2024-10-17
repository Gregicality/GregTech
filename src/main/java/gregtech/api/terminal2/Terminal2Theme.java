package gregtech.api.terminal2;

import gregtech.api.util.FileUtility;
import gregtech.api.util.GTUtility;
import gregtech.common.mui.drawable.FileTexture;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Terminal2Theme {

    private static final String CONFIG_PATH = "config/theme.json";
    private static IDrawable currentBackgroundDrawable = null;
    private static final UITexture defaultBackground = UITexture
            .fullImage(GTUtility.gregtechId("textures/gui/terminal/terminal_background.png"));
    public static File backgroundsDir;
    private static final Map<String, Integer> defaultColors = new Object2IntOpenHashMap<>();

    public static String currentBackground = "default";
    // theres enough colors here to make specifying stuff for all of them annoying
    // therefore, we use reflection for its intended purpose
    public static final List<String> colors = Arrays.stream(Terminal2Theme.class.getFields())
            .map(Field::getName).filter(name -> name.startsWith("COLOR_")).collect(Collectors.toList());

    public static Rectangle COLOR_BRIGHT_1 = new Rectangle().setColor(new Color(144, 243, 116).getRGB());
    public static Rectangle COLOR_BRIGHT_2 = new Rectangle().setColor(new Color(243, 208, 116).getRGB());
    public static Rectangle COLOR_BRIGHT_3 = new Rectangle().setColor(new Color(231, 95, 95).getRGB());
    public static Rectangle COLOR_BRIGHT_4 = new Rectangle().setColor(new Color(230, 230, 230).getRGB());

    public static Rectangle COLOR_DARK_1 = new Rectangle().setColor(new Color(0, 115, 255).getRGB());
    public static Rectangle COLOR_DARK_2 = new Rectangle().setColor(new Color(113, 27, 217).getRGB());
    public static Rectangle COLOR_DARK_3 = new Rectangle().setColor(new Color(30, 80, 30).getRGB());
    public static Rectangle COLOR_DARK_4 = new Rectangle().setColor(new Color(30, 30, 30).getRGB());

    public static Rectangle COLOR_FOREGROUND_BRIGHT = new Rectangle().setColor(new Color(148, 226, 193).getRGB());
    public static Rectangle COLOR_FOREGROUND_DARK = new Rectangle().setColor(new Color(175, 0, 0, 131).getRGB());

    public static Rectangle COLOR_BACKGROUND_1 = new Rectangle().setColor(new Color(0, 0, 0, 80).getRGB());
    public static Rectangle COLOR_BACKGROUND_2 = new Rectangle().setColor(new Color(0, 0, 0, 160).getRGB());
    public static Rectangle COLOR_BACKGROUND_3 = new Rectangle().setColor(new Color(246, 120, 120, 160).getRGB());

    public static void init() {
        backgroundsDir = new File(Terminal2.TERMINAL_PATH, "backgrounds");

        JsonElement element = FileUtility.loadJson(new File(Terminal2.TERMINAL_PATH, CONFIG_PATH));
        if (element == null || !element.isJsonObject()) {
            saveConfig();
            return;
        }

        JsonObject config = element.getAsJsonObject();

        for (String color : colors) {
            defaultColors.put(color, getColorRect(color).getColor());
            if (config.has(color)) {
                setColor(color, config.get(color).getAsInt());
            }
        }

        if (config.has("BACKGROUND_FILE")) {
            setBackground(config.get("BACKGROUND_FILE").getAsString());
        }
    }

    public static boolean saveConfig() {
        JsonObject config = new JsonObject();

        for (String color : colors) {
            config.addProperty(color, getColorRect(color).getColor());
        }

        config.addProperty("BACKGROUND_FILE", currentBackground);

        return FileUtility.saveJson(new File(Terminal2.TERMINAL_PATH, CONFIG_PATH), config);
    }

    public static IDrawable getBackgroundDrawable() {
        // noinspection ReplaceNullCheck
        if (currentBackgroundDrawable == null) {
            return defaultBackground;
        }
        return currentBackgroundDrawable;
    }

    public static void setBackground(String file) {
        currentBackground = file;
        if (file.equals("default")) {
            currentBackgroundDrawable = null;
        } else {
            currentBackgroundDrawable = new FileTexture(new File(backgroundsDir, file));
        }
    }

    public static Rectangle getColorRect(String color) {
        try {
            return (Rectangle) Terminal2Theme.class.getField(color).get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Failed to get color rect " + color, e);
        }
    }

    public static void setColor(String color, int i) {
        try {
            ((Rectangle) Terminal2Theme.class.getField(color).get(null)).setColor(i);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Failed to set color " + color + " to " + i, e);
        }
    }

    public static void resetToDefaultColor(String color) {
        setColor(color, defaultColors.get(color));
    }
}
