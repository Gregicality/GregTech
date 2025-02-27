package gregtech.api;

import gregtech.api.util.XSTR;
import gregtech.common.ConfigHolder;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.oredict.OreDictionary;

import org.jetbrains.annotations.ApiStatus;

import java.time.LocalDate;
import java.util.Random;
import java.util.function.Supplier;

import static net.minecraft.util.text.TextFormatting.*;

/**
 * Made for static imports, this Class is just a Helper.
 */
public class GTValues {

    /**
     * <p/>
     * This is worth exactly one normal Item.
     * This Constant can be divided by many commonly used Numbers such as
     * 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 15, 16, 18, 20, 21, 24, ... 64 or 81
     * without loosing precision and is for that reason used as Unit of Amount.
     * But it is also small enough to be multiplied with larger Numbers.
     * <p/>
     * This is used to determine the amount of Material contained inside a prefixed Ore.
     * For example Nugget = M / 9 as it contains out of 1/9 of an Ingot.
     */
    public static final long M = 3628800;

    /**
     * Renamed from "FLUID_MATERIAL_UNIT" to just "L"
     * <p/>
     * Fluid per Material Unit (Prime Factors: 3 * 3 * 2 * 2 * 2 * 2)
     */
    public static final int L = 144;

    /**
     * The Item WildCard Tag. Even shorter than the "-1" of the past
     */
    public static final short W = OreDictionary.WILDCARD_VALUE;

    public static final Random RNG = new XSTR();

    /** Current time on the Client. Will always be zero on the server. */
    public static long CLIENT_TIME = 0;

    /**
     * The Voltage Tiers. Use this Array instead of the old named Voltage Variables
     */
    public static final long[] V = { 8, 32, 128, 512, 2048, 8192, 32768, 131072, 524288, 2097152, 8388608, 33554432,
            134217728, 536870912, 2147483648L };

    /**
     * The Voltage Tiers divided by 2.
     */
    public static final int[] VH = { 4, 16, 64, 256, 1024, 4096, 16384, 65536, 262144, 1048576, 4194304, 16777216,
            67108864, 268435456, 1073741824 };

    /**
     * The Voltage Tiers adjusted for cable loss. Use this for recipe EU/t to avoid full-amp recipes
     */
    public static final int[] VA = { 7, 30, 120, 480, 1920, 7680, 30720, 122880, 491520, 1966080, 7864320, 31457280,
            125829120, 503316480, 2013265920 };

    /**
     * The Voltage Tiers adjusted for cable loss, divided by 2.
     */
    public static final int[] VHA = { 4, 16, 60, 240, 960, 3840, 15360, 61440, 245760, 983040, 3932160, 15728640,
            62914560, 251658240, 1006632960 };

    /**
     * The Voltage Tiers extended all the way to max Long value for overclocking
     */
    public static final long[] VOC = { 8, 32, 128, 512, 2048, 8192, 32768, 131072, 524288, 2097152, 8388608, 33554432,
            134217728, 536870912, 2147483648L, 8589934592L, 34359738368L, 137438953472L, 549755813888L,
            2199023255552L, 8796093022208L, 35184372088832L, 140737488355328L, 562949953421312L, 2251799813685248L,
            9007199254740992L, 36028797018963968L, 144115188075855872L, 576460752303423488L, 2305843009213693952L,
            Long.MAX_VALUE };

    public static final long[] VAOC = { 7, 30, 120, 480, 1920, 7680, 30720, 123880, 491520, 1966080, 7864320,
            31457280, 125829120, 503316480, 2013265920, 8053063680L, 32212254720L, 128849018880L, 515396075520L,
            2061584302080L, 8246337208320L, 32985348833280L, 131948427333120L, 527865581332480L, 2111062325297920L,
            8444249301319680L, 33777097205278720L, 135108988821415880L, 540431751284459520L, 2161727821137838080L,
            8646911284550352320L
    };

    public static final int ULV = 0;
    public static final int LV = 1;
    public static final int MV = 2;
    public static final int HV = 3;
    public static final int EV = 4;
    public static final int IV = 5;
    public static final int LuV = 6;
    public static final int ZPM = 7;
    public static final int UV = 8;

    public static final int UHV = 9;
    public static final int UEV = 10;
    public static final int UIV = 11;
    public static final int UXV = 12;
    public static final int OpV = 13;
    public static final int MAX = 14;
    public static final int MAX_TRUE = 30;

    /**
     * The short names for the voltages, used for registration primarily
     */
    public static final String[] VN = new String[] { "ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV", "UHV",
            "UEV", "UIV", "UXV", "OpV", "MAX" };

    /**
     * The short names for the voltages, up to max Long, used for registration primarily
     */
    public static final String[] VOCN = new String[] { "ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV", "UHV",
            "UEV", "UIV", "UXV", "OpV", "MAX", "MAX+1", "MAX+2", "MAX+3", "MAX+4", "MAX+5", "MAX+6", "MAX+7", "MAX+8",
            "MAX+9", "MAX+10", "MAX+11", "MAX+12", "MAX+13", "MAX+14", "MAX+15", "MAX+16",
    };

    /**
     * The short names for the voltages, formatted for text
     */
    public static final String[] VNF = new String[] {
            DARK_GRAY + "ULV", GRAY + "LV", AQUA + "MV",
            GOLD + "HV", DARK_PURPLE + "EV", BLUE + "IV",
            LIGHT_PURPLE + "LuV", RED + "ZPM", DARK_AQUA + "UV",
            DARK_RED + "UHV", GREEN + "UEV", DARK_GREEN + "UIV",
            YELLOW + "UXV", BLUE.toString() + BOLD + "OpV", RED.toString() + BOLD + "MAX" };

    private static final String MAX_PLUS = RED.toString() + BOLD + "M" + YELLOW + BOLD + "A" + GREEN + BOLD + "X" +
            AQUA + BOLD + "+" + LIGHT_PURPLE + BOLD;

    /**
     * The short names for the voltages, up to max Long, formatted for text
     */
    public static final String[] VOCNF = new String[] {
            DARK_GRAY + "ULV", GRAY + "LV", AQUA + "MV",
            GOLD + "HV", DARK_PURPLE + "EV", BLUE + "IV",
            LIGHT_PURPLE + "LuV", RED + "ZPM", DARK_AQUA + "UV",
            DARK_RED + "UHV", GREEN + "UEV", DARK_GREEN + "UIV",
            YELLOW + "UXV", BLUE.toString() + BOLD + "OpV", RED.toString() + BOLD + "MAX",
            MAX_PLUS + "1", MAX_PLUS + "2", MAX_PLUS + "3", MAX_PLUS + "4",
            MAX_PLUS + "5", MAX_PLUS + "6", MAX_PLUS + "7", MAX_PLUS + "8",
            MAX_PLUS + "9", MAX_PLUS + "10", MAX_PLUS + "11", MAX_PLUS + "12",
            MAX_PLUS + "13", MAX_PLUS + "14", MAX_PLUS + "15", MAX_PLUS + "16", };

    /**
     * Color values for the voltages
     */
    public static final int[] VC = new int[] { 0xC80000, 0xDCDCDC, 0xFF6400, 0xFFFF1E, 0x808080, 0xF0F0F5, 0xE99797,
            0x7EC3C4, 0x7EB07E, 0xBF74C0, 0x0B5CFE, 0x914E91, 0x488748, 0x8C0000, 0x2828F5 };

    /**
     * The long names for the voltages
     */
    public static final String[] VOLTAGE_NAMES = new String[] { "Ultra Low Voltage", "Low Voltage", "Medium Voltage",
            "High Voltage", "Extreme Voltage", "Insane Voltage", "Ludicrous Voltage", "ZPM Voltage", "Ultimate Voltage",
            "Ultra High Voltage", "Ultra Excessive Voltage", "Ultra Immense Voltage", "Ultra Extreme Voltage",
            "Overpowered Voltage", "Maximum Voltage" };

    /**
     * GregTech Mod ID
     */
    public static final String MODID = "gregtech";

    /**
     * GregTech Mod Name
     */
    public static final String MOD_NAME = "GregTech";

    /** @deprecated Use {@link gregtech.api.util.Mods} instead */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public static final String MODID_FR = "forestry",
            MODID_CT = "crafttweaker",
            MODID_TOP = "theoneprobe",
            MODID_CTM = "ctm",
            MODID_CC = "cubicchunks",
            MODID_AR = "advancedrocketry",
            MODID_ECORE = "endercore",
            MODID_EIO = "enderio",
            MODID_BC = "buildcraftcore",
            MODID_COFH = "cofhcore",
            MODID_APPENG = "appliedenergistics2",
            MODID_JEI = "jei",
            MODID_GROOVYSCRIPT = "groovyscript",
            MODID_NC = "nuclearcraft",
            MODID_IE = "immersiveengineering",
            MODID_OC = "opencomputers",
            MODID_JOURNEYMAP = "journeymap",
            MODID_VOXELMAP = "voxelmap",
            MODID_XAERO_MINIMAP = "xaerominimap",
            MODID_HWYLA = "hwyla",
            MODID_BAUBLES = "baubles",
            MODID_TOP_ADDONS = "topaddons",
            MODID_IC2 = "ic2",
            MODID_GTFO = "gregtechfoodoption",
            MODID_BINNIE = "binniecore",
            MODID_XU2 = "extrautils2",
            MODID_TR = "techreborn",
            MODID_MB = "magicbees",
            MODID_EB = "extrabees",
            MODID_ET = "extratrees",
            MODID_GENETICS = "genetics",
            MODID_BOP = "biomesoplenty",
            MODID_TCON = "tconstruct",
            MODID_EN = "exnihilocreatio",
            MODID_PROJRED_CORE = "projectred-core",
            MODID_RC = "railcraft",
            MODID_CHISEL = "chisel",
            MODID_RS = "refinedstorage",
            MODID_LITTLETILES = "littletiles";

    private static Boolean isClient;

    public static boolean isClientSide() {
        if (isClient == null) isClient = FMLCommonHandler.instance().getSide().isClient();
        return isClient;
    }

    private static Boolean isDeobf;

    public static boolean isDeobfEnvironment() {
        if (isDeobf == null) isDeobf = FMLLaunchHandler.isDeobfuscatedEnvironment();
        return isDeobf;
    }

    /**
     * Default fallback value used for Map keys.
     * Currently only used in {@link gregtech.loaders.recipe.CraftingComponent}.
     */
    public static final int FALLBACK = -1;

    public static Supplier<Boolean> FOOLS = () -> {
        String[] yearMonthDay = LocalDate.now().toString().split("-");
        return ConfigHolder.misc.specialEvents && yearMonthDay[1].equals("04") && yearMonthDay[2].equals("01");
    };

    public static Supplier<Boolean> XMAS = () -> {
        String[] yearMonthDay = LocalDate.now().toString().split("-");
        return ConfigHolder.misc.specialEvents && yearMonthDay[1].equals("12") &&
                (yearMonthDay[2].equals("24") || yearMonthDay[2].equals("25"));
    };
}
