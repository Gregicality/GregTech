package gregtech.api.terminal2;

import gregtech.api.util.FileUtility;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.terminal2.SettingsApp;
import gregtech.common.terminal2.TestApp;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Terminal2 {

    public static final Map<ResourceLocation, ITerminalApp> appMap = new HashMap<>();
    public static final ResourceLocation HOME_ID = GTUtility.gregtechId("home");

    @SideOnly(Side.CLIENT)
    public static File TERMINAL_PATH;

    public static void init() {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            TERMINAL_PATH = new File(Loader.instance().getConfigDir(), ConfigHolder.client.terminalRootPath);
            FileUtility.extractJarFiles("/assets/gregtech/terminal", TERMINAL_PATH, false);
            Terminal2Theme.init();
        }
        registerApp(GTUtility.gregtechId("settings"), new SettingsApp());
        registerApp(GTUtility.gregtechId("test"), new TestApp());
    }

    public static void registerApp(ResourceLocation id, ITerminalApp app) {
        if (appMap.containsKey(id) || HOME_ID.equals(id)) {
            throw new AssertionError("A terminal app with id " + id + " already exists!");
        }
        appMap.put(id, app);
    }
}
