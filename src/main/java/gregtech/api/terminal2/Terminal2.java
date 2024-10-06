package gregtech.api.terminal2;

import gregtech.common.ConfigHolder;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;

public class Terminal2 {

    @SideOnly(Side.CLIENT)
    public static File TERMINAL_PATH;

    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            TERMINAL_PATH = new File(Loader.instance().getConfigDir(), ConfigHolder.client.terminalRootPath);
        }
    }
}
