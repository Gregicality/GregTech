package gregtech.common;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;

import net.minecraftforge.common.config.Config;

@Config(modid = GTValues.MODID, name = GTValues.MODID + '/' + GTValues.MODID)
public class ConfigHolder {

    @Config.Comment("Config options for client-only features")
    @Config.Name("Client Options")
    public static ClientOptions client = new ClientOptions();

    @Config.Comment("Config options for Mod Compatibility")
    @Config.Name("Compatibility Options")
    @Config.RequiresMcRestart
    public static CompatibilityOptions compat = new CompatibilityOptions();

    @Config.Comment("Config options for GT Machines, Pipes, Cables, and Electric Items")
    @Config.Name("Machine Options")
    @Config.RequiresMcRestart
    public static MachineOptions machines = new MachineOptions();

    @Config.Comment("Config options for miscellaneous features")
    @Config.Name("Miscellaneous Options")
    @Config.RequiresMcRestart
    public static MiscOptions misc = new MiscOptions();

    @Config.Comment("Config Options for GregTech and Vanilla Recipes")
    @Config.Name("Recipe Options")
    @Config.RequiresMcRestart
    public static RecipeOptions recipes = new RecipeOptions();

    // TODO move to ToolsModule config
    @Config.Comment("Config options for Tools and Armor")
    @Config.Name("Tool and Armor Options")
    public static ToolOptions tools = new ToolOptions();

    @Config.Comment("Config options for World Generation features")
    @Config.Name("Worldgen Options")
    @Config.RequiresMcRestart
    public static WorldGenOptions worldgen = new WorldGenOptions();

    public static class MachineOptions {

        @Config.Comment({ "Whether insufficient energy supply should reset Machine recipe progress to zero.",
                "If true, progress will reset.", "If false, progress will decrease to zero with 2x speed",
                "Default: false" })
        public boolean recipeProgressLowEnergy = false;

        @Config.Comment({
                "Whether to require a Wrench, Wirecutter, or other GregTech tools to break machines, casings, wires, and more.",
                "Default: false" })
        public boolean requireGTToolsForBlocks = false;

        @Config.Comment({ "Whether to enable the Maintenance Hatch, required for Multiblocks.", "Default: true" })
        public boolean enableMaintenance = true;

        @Config.Comment({ "Whether to enable High-Tier Solar Panels (IV-UV). They will not have recipes.",
                "Default: false" })
        public boolean enableHighTierSolars = false;

        @Config.Comment({
                "Whether to enable World Accelerators, which accelerate ticks for surrounding Tile Entities, Crops, etc.",
                "Default: true" })
        public boolean enableWorldAccelerators = true;

        @Config.Comment({ "List of TileEntities that the World Accelerator should not accelerate.",
                "GregTech TileEntities are always blocked.",
                "Entries must be in a fully qualified format. For example: appeng.tile.networking.TileController",
                "Default: none" })
        public String[] worldAcceleratorBlacklist = new String[0];

        @Config.Comment({ "Whether to use GT6-style pipe and cable connections, meaning they will not auto-connect " +
                "unless placed directly onto another pipe or cable.", "Default: true" })
        public boolean gt6StylePipesCables = true;

        @Config.Comment({ "Minimum distance between Long Distance Item Pipe Endpoints", "Default: 50" })
        public int ldItemPipeMinDistance = 50;

        @Config.Comment({ "Minimum distance betweeb Long Distance Fluid Pipe Endpoints", "Default: 50" })
        public int ldFluidPipeMinDistance = 50;

        @Config.Comment({ "Whether Steam Multiblocks should use Steel instead of Bronze.", "Default: false" })
        public boolean steelSteamMultiblocks = false;

        @Config.Comment({ "Steam to EU multiplier for Steam Multiblocks.",
                "1.0 means 1L Steam -> 1 EU. 0.5 means 2L Steam -> 1 EU.", "Default: 0.5" })
        public double multiblockSteamToEU = 0.5;

        @Config.Comment({ "Whether machines or boilers damage the terrain when they explode.",
                "Note machines and boilers always explode when overloaded with power or met with special conditions, regardless of this config.",
                "Default: true" })
        public boolean doesExplosionDamagesTerrain = true;

        @Config.Comment({
                "Whether machines explode in rainy weather or when placed next to certain terrain, such as fire or lava",
                "Default: false" })
        public boolean doTerrainExplosion = false;

        @Config.Comment({ "Energy use multiplier for electric items.", "Default: 100" })
        public int energyUsageMultiplier = 100;

        @Config.Comment({ "The EU/t drain for each screen of the Central Monitor.", "Default: 8" })
        @Config.RangeInt(min = 0)
        public int centralMonitorEuCost = 8;

        @Config.Comment({ "Whether to play machine sounds while machines are active.", "Default: true" })
        public boolean machineSounds = true;

        @Config.Comment({ "Additional Fluids to allow in GT Boilers in place of Water or Distilled Water.",
                "Useful for mods like TerraFirmaCraft with different Fluids for Water", "Default: none" })
        public String[] boilerFluids = new String[0];

        @Config.Comment({ "Blacklist of machines for the Processing Array.",
                "Add the unlocalized Recipe Map name to blacklist the machine.",
                "Default: All machines allowed" })
        public String[] processingArrayBlacklist = new String[0];

        @Config.Comment({ "Whether to enable the cleanroom, required for various recipes.", "Default: true" })
        public boolean enableCleanroom = true;

        @Config.Comment({ "Whether multiblocks should ignore all cleanroom requirements.",
                "This does nothing if B:enableCleanroom is false.",
                "Default: false" })
        public boolean cleanMultiblocks = false;

        @Config.Comment({ "Block to replace mined ores with in the miner and multiblock miner.",
                "Default: minecraft:cobblestone" })
        public String replaceMinedBlocksWith = "minecraft:cobblestone";

        @Config.Comment({ "Whether to enable Assembly Line research for recipes.", "Default: true" })
        @Config.RequiresMcRestart
        public boolean enableResearch = true;

        @Config.Comment({ "Whether the Assembly Line should require the item inputs to be in order.", "Default: true" })
        @Config.RequiresMcRestart
        public boolean orderedAssembly = true;

        @Config.Comment({ "Whether the Assembly Line should require the fluid inputs to be in order.",
                "This does nothing if B:orderedAssembly is false.",
                "Default: false" })
        @Config.RequiresMcRestart
        public boolean orderedFluidAssembly = false;

        /**
         * <strong>Addons mods should not reference this config directly.</strong>
         * Use {@link GregTechAPI#isHighTier()} instead.
         */
        @Config.Comment({ "If High Tier (>UV-tier) GT content should be registered.",
                "Items and Machines enabled with this config will have missing recipes by default.",
                "This is intended for modpack developers only, and is not playable without custom tweaks or addons.",
                "Other mods can override this to true, regardless of the config file.",
                "Default: false" })
        @Config.RequiresMcRestart
        public boolean highTierContent = false;

        @Config.Comment({ "Whether tick acceleration effects are allowed to affect GT machines.",
                "This does NOT apply to the World Accelerator, but to external effects like Time in a Bottle.",
                "Default: true" })
        public boolean allowTickAcceleration = true;
    }

    public static class WorldGenOptions {

        @Config.Comment({ "Specifies the minimum number of veins in a section.", "Default: 1" })
        public int minVeinsInSection = 1;

        @Config.Comment({ "Specifies an additional random number of veins in a section.", "Default: 0" })
        public int additionalVeinsInSection = 0;

        @Config.Comment({ "Whether veins should be generated in the center of chunks.", "Default: true" })
        public boolean generateVeinsInCenterOfChunk = true;

        @Config.Comment({ "Whether to disable Vanilla ore generation in world.", "Default: true" })
        public boolean disableVanillaOres = true;

        @Config.Comment({ "Whether to disable Rubber Tree world generation.", "Default: false" })
        public boolean disableRubberTreeGeneration = false;

        @Config.Comment({
                "Multiplier for the chance to spawn a Rubber Tree on any given roll. Higher values make Rubber Trees more common.",
                "Default: 1.0" })
        @Config.RangeDouble(min = 0)
        public double rubberTreeRateIncrease = 1.0;

        @Config.Comment({ "Whether to increase number of rolls for dungeon chests. Increases dungeon loot drastically.",
                "Default: true" })
        public boolean increaseDungeonLoot = true;

        @Config.Comment({ "Allow GregTech to add additional GregTech Items as loot in various structures.",
                "Default: true" })
        public boolean addLoot = true;

        @Config.Comment({ "Should all Stone Types drop unique Ore Item Blocks?",
                "Default: false (meaning only Stone, Netherrack, and Endstone" })
        public boolean allUniqueStoneTypes = false;
    }

    public static class RecipeOptions {

        @Config.Comment({
                "Change the recipe of Rods in the Lathe to 1 Rod and 2 Small Piles of Dust, instead of 2 Rods.",
                "Default: false" })
        public boolean harderRods = false;

        @Config.Comment({ "Whether to make Glass related recipes harder. Default: true" })
        public boolean hardGlassRecipes = true;

        @Config.Comment({ "Whether to nerf Wood crafting to 2 Planks from 1 Log, and 2 Sticks from 2 Planks.",
                "Default: false" })
        public boolean nerfWoodCrafting = false;

        @Config.Comment({ "Whether to nerf the Paper crafting recipe.", "Default: true" })
        public boolean nerfPaperCrafting = true;

        @Config.Comment({ "Whether to make Wood related recipes harder.", "Excludes sticks and planks.",
                "Default: false" })
        public boolean hardWoodRecipes = false;

        @Config.Comment({ "Whether to make Redstone related recipes harder.", "Default: false" })
        public boolean hardRedstoneRecipes = false;

        @Config.Comment({ "Recipes for Buckets, Cauldrons, Hoppers, and Iron Bars" +
                " require Iron Plates, Rods, and more.", "Default: true" })
        public boolean hardIronRecipes = true;

        @Config.Comment({ "Recipes for items like Iron Doors, Trapdoors, Anvil" +
                " require Iron Plates, Rods, and more.", "Default: false" })
        public boolean hardAdvancedIronRecipes = false;

        @Config.Comment({ "Whether to make miscellaneous recipes harder.", "Default: false" })
        public boolean hardMiscRecipes = false;

        @Config.Comment({ "Whether to make coloring blocks like Concrete or Glass harder.", "Default: false" })
        public boolean hardDyeRecipes = false;

        @Config.Comment({ "Whether to remove charcoal smelting recipes from the vanilla furnace.", "Default: true" })
        public boolean harderCharcoalRecipe = true;

        @Config.Comment({ "Whether to make the Flint and Steel recipe require steel parts.", "Default: true." })
        public boolean flintAndSteelRequireSteel = true;

        @Config.Comment({ "Whether to make Vanilla Tools and Armor recipes harder.",
                "Excludes Flint and Steel, and Buckets.", "Default: false" })
        public boolean hardToolArmorRecipes = false;

        @Config.Comment({
                "Whether to disable the Vanilla Concrete from Powder with Water behavior, forcing the GT recipe.",
                "Default: false" })
        public boolean disableConcreteInWorld = false;

        @Config.Comment({ "Whether to generate Flawed and Chipped Gems for materials and recipes involving them.",
                "Useful for mods like TerraFirmaCraft.", "Default: false" })
        public boolean generateLowQualityGems = false;

        @Config.Comment({ "Whether to remove Block/Ingot compression and decompression in the Crafting Table.",
                "Default: false" })
        public boolean disableManualCompression = false;

        @Config.Comment({ "Whether to remove Vanilla Block Recipes from the Crafting Table.", "Default: false" })
        public boolean removeVanillaBlockRecipes = false;

        @Config.Comment({
                "Whether to make crafting recipes for Bricks, Nether Bricks, Firebricks, and Coke Bricks harder.",
                "Default: false" })
        public boolean harderBrickRecipes = false;

        @Config.Comment({ "Whether to make the recipe for the EBF Controller harder.", "Default: false" })
        public boolean harderEBFControllerRecipe = false;

        @Config.Comment({ "How many Multiblock Casings to make per craft. Either 1, 2, or 3.", "Default: 2" })
        @Config.RangeInt(min = 1, max = 3)
        public int casingsPerCraft = 2;

        @Config.Comment({
                "Whether to nerf the output amounts of the first circuit in a set to 1 (from 2) and SoC to 2 (from 4).",
                "Default: false" })
        public boolean harderCircuitRecipes = false;

        @Config.Comment({
                "Whether to change the byproducts in Ore Processing from a chanced output for a full dust into a guaranteed output for tiny dusts. For example a 33% chance of getting a full dust would change into a 100% chance of getting 3 tiny dusts.",
                "Default: false" })
        public boolean deterministicOreProcessing = false;
    }

    public static class CompatibilityOptions {

        @Config.Comment("Config options regarding GTEU compatibility with other energy systems")
        @Config.Name("Energy Compat Options")
        public EnergyCompatOptions energy = new EnergyCompatOptions();

        @Config.Comment("Config options regarding GTEU compatibility with AE2")
        @Config.Name("Energy Compat Options")
        public AE2CompatOptions ae2 = new AE2CompatOptions();

        @Config.Comment({ "Whether to hide facades of all blocks in JEI and creative search menu.", "Default: true" })
        public boolean hideFacadesInJEI = true;

        @Config.Comment({ "Whether to hide filled cells in JEI and creative search menu.", "Default: true" })
        public boolean hideFilledCellsInJEI = true;

        @Config.Comment({ "Specifies priorities of mods in Ore Dictionary item registration.",
                "First ModID has highest priority, last has lowest. " +
                        "Unspecified ModIDs follow standard sorting, but always have lower priority than the last specified ModID.",
                "Default: [\"minecraft\", \"gregtech\"]" })
        public String[] modPriorities = {
                "minecraft",
                "gregtech"
        };

        @Config.Comment({
                "Whether Gregtech should remove smelting recipes from the vanilla furnace for ingots requiring the Electric Blast Furnace.",
                "Default: true" })
        public boolean removeSmeltingForEBFMetals = true;

        public static class EnergyCompatOptions {

            @Config.Comment({ "Enable Native GTEU to Forge Energy (RF and alike) on GT Cables and Wires.",
                    "This does not enable nor disable Converters.", "Default: true" })
            public boolean nativeEUToFE = true;

            @Config.Comment({ "Enable GTEU to FE (and vice versa) Converters.", "Default: false" })
            public boolean enableFEConverters = false;

            @Config.Comment({ "Forge Energy to GTEU ratio for converting FE to EU.", "Only affects converters.",
                    "Default: 4 FE == 1 EU" })
            @Config.RangeInt(min = 1, max = 16)
            public int feToEuRatio = 4;

            @Config.Comment({ "GTEU to Forge Energy ratio for converting EU to FE.",
                    "Affects native conversion and Converters.", "Default: 4 FE == 1 EU" })
            @Config.RangeInt(min = 1, max = 16)
            public int euToFeRatio = 4;
        }

        public static class AE2CompatOptions {

            @Config.Comment({ "The interval between ME Hatch/Bus interact ME network.",
                    "It may cause lag if the interval is too small.", "Default: 2 sec" })
            @Config.RangeInt(min = 1, max = 80)
            public int updateIntervals = 40;

            @Config.Comment({ "The energy consumption of ME Hatch/Bus.", "Default: 1.0AE/t" })
            @Config.RangeDouble(min = 0.0, max = 10.0)
            public double meHatchEnergyUsage = 1.0;
        }
    }

    public static class MiscOptions {

        @Config.Comment({ "Whether to enable more verbose logging.", "Default: false" })
        public boolean debug = false;

        @Config.Comment({ "Whether to enable Special Event features (e.g. Christmas, etc).", "Default: true" })
        public boolean specialEvents = true;

        @Config.Comment({
                "Setting this to true makes GTCEu ignore error and invalid recipes that would otherwise cause crash.",
                "Default: true" })
        public boolean ignoreErrorOrInvalidRecipes = true;

        @Config.Comment({ "Whether to enable a login message to players when they join the world.", "Default: true" })
        public boolean loginMessage = true;

        @Config.RangeInt(min = 0, max = 100)
        @Config.Comment({ "Chance with which flint and steel will create fire.", "Default: 50" })
        @Config.SlidingOption
        public int flintChanceToCreateFire = 50;

        @Config.Comment({ "Whether to give the terminal to new players on login", "Default: true" })
        public boolean spawnTerminal = true;
    }

    public static class ClientOptions {

        @Config.Name("Gui Config")
        public GuiConfig guiConfig = new GuiConfig();

        @Config.Name("Armor HUD Location")
        @Config.RequiresMcRestart
        public ArmorHud armorHud = new ArmorHud();

        @Config.Comment("Config options for Shaders and Post-processing Effects")
        @Config.Name("Shader Options")
        public ShaderOptions shader = new ShaderOptions();

        @Config.Comment({ "Terminal root path.", "Default: {.../config}/gregtech/terminal" })
        @Config.RequiresMcRestart
        public String terminalRootPath = "gregtech/terminal";

        @Config.Comment({
                "Whether to hook depth texture. Has no effect on performance, but if there is a problem with rendering, try disabling it.",
                "Default: true" })
        public boolean hookDepthTexture = true;

        @Config.Comment({ "Resolution level for fragment shaders.",
                "Higher values increase quality (limited by the resolution of your screen) but are more GPU intensive.",
                "Default: 2" })
        @Config.RangeDouble(min = 0, max = 5)
        @Config.SlidingOption
        public double resolution = 2;

        @Config.Comment({ "Whether or not to enable Emissive Textures for GregTech Machines and multiblock parts.",
                "Default: true" })
        public boolean machinesEmissiveTextures = true;

        @Config.Comment({
                "Whether or not to enable Emissive Textures for Electric Blast Furnace Coils when the multiblock is working.",
                "Default: true" })
        public boolean coilsActiveEmissiveTextures = true;

        @Config.Comment({ "Whether or not sounds should be played when using tools outside of crafting.",
                "Default: true" })
        public boolean toolUseSounds = true;

        @Config.Comment({ "Whether or not sounds should be played when crafting with tools.", "Default: true" })
        public boolean toolCraftingSounds = true;

        @Config.Comment({
                "Overrides the MC total playable sounds limit. MC's default is 28, which causes problems with many machine sounds at once",
                "If sounds are causing large amounts of lag, try lowering this.",
                "If sounds are not working at all, try setting this to the lowest value (28).", "Default: 512" })
        @Config.RangeInt(min = 28, max = 2048)
        @Config.RequiresMcRestart
        public int maxNumSounds = 512;

        @Config.Comment({ "The default color to overlay onto machines.",
                "16777215 (0xFFFFFF in decimal) is no coloring (like GTCE).",
                "13819135 (0xD2DCFF in decimal) is the classic blue from GT5 (default)." })
        @Config.RangeInt(min = 0, max = 0xFFFFFF)
        public int defaultPaintingColor = 0xD2DCFF;

        @Config.Comment({ "The default color to overlay onto Machine (and other) UIs.",
                "16777215 (0xFFFFFF) is no coloring (like GTCE).",
                "13819135 (0xD2DCFF in decimal) is the classic blue from GT5 (default)." })
        @Config.RangeInt(min = 0, max = 0xFFFFFF)
        public int defaultUIColor = 0xD2DCFF;

        // requires mc restart, color is set upon jei plugin registration
        @Config.Comment({ "The color to use as a background for the Multiblock Preview JEI Page.",
                "Default: 13027014 (0xC6C6C6), which is JEI's background color." })
        @Config.RangeInt(min = 0, max = 0xFFFFFF)
        @Config.RequiresMcRestart
        public int multiblockPreviewColor = 0xC6C6C6;

        @Config.Comment({ "Whether to use the Spray Can color in UIs when a machine is painted.", "Default: true" })
        public boolean useSprayCanColorInUI = true;

        // does not require mc restart, drawn dynamically
        @Config.Comment({ "The color to use for the text in the Multiblock Preview JEI Page.",
                "Default: 3355443 (0x333333), which is minecraft's dark gray color." })
        @Config.RangeInt(min = 0, max = 0xFFFFFF)
        public int multiblockPreviewFontColor = 0x333333;

        @Config.Comment("Prevent tooltips from blinking for better visibility")
        public boolean preventBlinkingTooltips = false;

        @Config.Comment({ "Prevent optical and laser cables from animating when active.", "Default: false" })
        public boolean preventAnimatedCables = false;

        @Config.Comment({ "Enable the fancy rendering for Super/Quantum Chests/Tanks.",
                "Default: true" })
        public boolean enableFancyChestRender = true;

        public static class GuiConfig {

            @Config.Comment({ "The scrolling speed of widgets", "Default: 13" })
            @Config.RangeInt(min = 1)
            public int scrollSpeed = 13;

            @Config.Comment({ "If progress bars should move smoothly.",
                    "False is incremental like the Minecraft furnace.",
                    "Default: true" })
            public boolean smoothProgressBars = true;
        }

        public static class ArmorHud {

            @Config.Comment({ "Sets HUD location", "1 - left-upper corner", "2 - right-upper corner",
                    "3 - left-bottom corner", "4 - right-bottom corner", "Default: 1" })
            @Config.RangeInt(min = 1, max = 4)
            @Config.SlidingOption
            public int hudLocation = 1;

            @Config.Comment({ "Horizontal offset of HUD.", "Default: 0" })
            @Config.RangeInt(min = 0, max = 100)
            @Config.SlidingOption
            public int hudOffsetX = 0;

            @Config.Comment({ "Vertical offset of HUD.", "Default: 0" })
            @Config.RangeInt(min = 0, max = 100)
            @Config.SlidingOption
            public int hudOffsetY = 0;
        }

        public static class ShaderOptions {

            @Config.Comment("Bloom config options for the fusion reactor.")
            @Config.Name("Fusion Reactor")
            public FusionBloom fusionBloom = new FusionBloom();

            @Config.Comment("Particle config option for the Assembly Line")
            public boolean assemblyLineParticles = true;

            @Config.Comment("Bloom config options for the heat effect (cable burning).")
            @Config.Name("Heat Effect")
            public HeatEffectBloom heatEffectBloom = new HeatEffectBloom();

            @Config.Comment({ "Whether to use shader programs.", "Default: true" })
            public boolean useShader = true;

            @Config.Comment({ "Whether or not to enable Emissive Textures with bloom effect.", "Default: true" })
            public boolean emissiveTexturesBloom = true;

            @Config.Comment({ "Bloom Algorithm", "0 - Simple Gaussian Blur Bloom (Fast)", "1 - Unity Bloom",
                    "2 - Unreal Bloom", "Default: 2" })
            @Config.RangeInt(min = 0, max = 2)
            @Config.SlidingOption
            public int bloomStyle = 2;

            @Config.Comment({
                    "The brightness after bloom should not exceed this value. It can be used to limit the brightness of highlights " +
                            "(e.g., daytime).",
                    "OUTPUT = BACKGROUND + BLOOM * strength * (base + LT + (1 - BACKGROUND_BRIGHTNESS)*({HT}-LT)))",
                    "This value should be greater than lowBrightnessThreshold.", "Default: 0.5" })
            @Config.RangeDouble(min = 0)
            public double highBrightnessThreshold = 0.5;

            @Config.Comment({
                    "The brightness after bloom should not smaller than this value. It can be used to limit the brightness of dusky parts " +
                            "(e.g., night/caves).",
                    "OUTPUT = BACKGROUND + BLOOM * strength * (base + {LT} + (1 - BACKGROUND_BRIGHTNESS)*(HT-{LT})))",
                    "This value should be smaller than highBrightnessThreshold.", "Default: 0.2" })
            @Config.RangeDouble(min = 0)
            public double lowBrightnessThreshold = 0.2;

            @Config.Comment({ "The base brightness of the bloom.", "It is similar to strength",
                    "This value should be smaller than highBrightnessThreshold.",
                    "OUTPUT = BACKGROUND + BLOOM * strength * ({base} + LT + (1 - BACKGROUND_BRIGHTNESS)*(HT-LT)))",
                    "Default: 0.1" })
            @Config.RangeDouble(min = 0)
            public double baseBrightness = 0.1;

            @Config.Comment({ "Mipmap Size.", "Higher values increase quality, but are slower to render.",
                    "Default: 5" })
            @Config.RangeInt(min = 2, max = 5)
            @Config.SlidingOption
            public int nMips = 5;

            @Config.Comment({ "Bloom Strength",
                    "OUTPUT = BACKGROUND + BLOOM * {strength} * (base + LT + (1 - BACKGROUND_BRIGHTNESS)*(HT-LT)))",
                    "Default: 2" })
            @Config.RangeDouble(min = 0)
            public double strength = 1.5;

            @Config.Comment({ "Blur Step (bloom range)", "Default: 1" })
            @Config.RangeDouble(min = 0)
            public double step = 1;
        }
    }

    public static class FusionBloom {

        @Config.Comment({ "Whether to use shader programs.", "Default: true" })
        public boolean useShader = true;

        @Config.Comment({ "Bloom Strength",
                "OUTPUT = BACKGROUND + BLOOM * {strength} * (base + LT + (1 - BACKGROUND_BRIGHTNESS)*(HT-LT)))",
                "Default: 2" })
        @Config.RangeDouble(min = 0)
        public double strength = 1.5;

        @Config.Comment({ "Bloom Algorithm", "0 - Simple Gaussian Blur Bloom (Fast)", "1 - Unity Bloom",
                "2 - Unreal Bloom", "Default: 2" })
        @Config.RangeInt(min = 0, max = 2)
        @Config.SlidingOption
        public int bloomStyle = 1;

        @Config.Comment({
                "The brightness after bloom should not exceed this value. It can be used to limit the brightness of highlights " +
                        "(e.g., daytime).",
                "OUTPUT = BACKGROUND + BLOOM * strength * (base + LT + (1 - BACKGROUND_BRIGHTNESS)*({HT}-LT)))",
                "This value should be greater than lowBrightnessThreshold.", "Default: 0.5" })
        @Config.RangeDouble(min = 0)
        public double highBrightnessThreshold = 1.3;

        @Config.Comment({
                "The brightness after bloom should not smaller than this value. It can be used to limit the brightness of dusky parts " +
                        "(e.g., night/caves).",
                "OUTPUT = BACKGROUND + BLOOM * strength * (base + {LT} + (1 - BACKGROUND_BRIGHTNESS)*(HT-{LT})))",
                "This value should be smaller than highBrightnessThreshold.", "Default: 0.2" })
        @Config.RangeDouble(min = 0)
        public double lowBrightnessThreshold = 0.3;

        @Config.Comment({ "The base brightness of the bloom.", "It is similar to strength",
                "This value should be smaller than highBrightnessThreshold.",
                "OUTPUT = BACKGROUND + BLOOM * strength * ({base} + LT + (1 - BACKGROUND_BRIGHTNESS)*(HT-LT)))",
                "Default: 0.1" })
        @Config.RangeDouble(min = 0)
        public double baseBrightness = 0;
    }

    public static class HeatEffectBloom {

        @Config.Comment({ "Whether to use shader programs.", "Default: true" })
        public boolean useShader = true;

        @Config.Comment({ "Bloom Strength",
                "OUTPUT = BACKGROUND + BLOOM * {strength} * (base + LT + (1 - BACKGROUND_BRIGHTNESS)*(HT-LT)))",
                "Default: 2" })
        @Config.RangeDouble(min = 0)
        public double strength = 1.1;

        @Config.Comment({ "Bloom Algorithm", "0 - Simple Gaussian Blur Bloom (Fast)", "1 - Unity Bloom",
                "2 - Unreal Bloom", "Default: 2" })
        @Config.RangeInt(min = 0, max = 2)
        @Config.SlidingOption
        public int bloomStyle = 2;

        @Config.Comment({
                "The brightness after bloom should not exceed this value. It can be used to limit the brightness of highlights " +
                        "(e.g., daytime).",
                "OUTPUT = BACKGROUND + BLOOM * strength * (base + LT + (1 - BACKGROUND_BRIGHTNESS)*({HT}-LT)))",
                "This value should be greater than lowBrightnessThreshold.", "Default: 0.5" })
        @Config.RangeDouble(min = 0)
        public double highBrightnessThreshold = 1.4;

        @Config.Comment({
                "The brightness after bloom should not smaller than this value. It can be used to limit the brightness of dusky parts " +
                        "(e.g., night/caves).",
                "OUTPUT = BACKGROUND + BLOOM * strength * (base + {LT} + (1 - BACKGROUND_BRIGHTNESS)*(HT-{LT})))",
                "This value should be smaller than highBrightnessThreshold.", "Default: 0.2" })
        @Config.RangeDouble(min = 0)
        public double lowBrightnessThreshold = 0.6;

        @Config.Comment({ "The base brightness of the bloom.", "It is similar to strength",
                "This value should be smaller than highBrightnessThreshold.",
                "OUTPUT = BACKGROUND + BLOOM * strength * ({base} + LT + (1 - BACKGROUND_BRIGHTNESS)*(HT-LT)))",
                "Default: 0.1" })
        @Config.RangeDouble(min = 0)
        public double baseBrightness = 0;
    }

    public static class ToolOptions {

        @Config.RequiresMcRestart
        @Config.Name("NanoSaber Options")
        public NanoSaber nanoSaber = new NanoSaber();

        @Config.RequiresMcRestart
        @Config.Comment("NightVision Goggles Voltage Tier. Default: 1 (LV)")
        @Config.RangeInt(min = 0, max = 14)
        public int voltageTierNightVision = 1;

        @Config.RequiresMcRestart
        @Config.Comment("NanoSuit Voltage Tier. Default: 3 (HV)")
        @Config.RangeInt(min = 0, max = 14)
        public int voltageTierNanoSuit = 3;

        @Config.RequiresMcRestart
        @Config.Comment({ "Advanced NanoSuit Chestplate Voltage Tier.", "Default: 3 (HV)" })
        @Config.RangeInt(min = 0, max = 14)
        public int voltageTierAdvNanoSuit = 3;

        @Config.RequiresMcRestart
        @Config.Comment({ "QuarkTech Suit Voltage Tier.", "Default: 5 (IV)" })
        @Config.RangeInt(min = 0, max = 14)
        @Config.SlidingOption
        public int voltageTierQuarkTech = 5;

        @Config.RequiresMcRestart
        @Config.Comment({ "Advanced QuarkTech Suit Chestplate Voltage Tier.", "Default: 5 (LuV)" })
        @Config.RangeInt(min = 0, max = 14)
        public int voltageTierAdvQuarkTech = 6;

        @Config.RequiresMcRestart
        @Config.Comment({ "Electric Impeller Jetpack Voltage Tier.", "Default: 2 (MV)" })
        @Config.RangeInt(min = 0, max = 14)
        public int voltageTierImpeller = 2;

        @Config.RequiresMcRestart
        @Config.Comment({ "Advanced Electric Jetpack Voltage Tier.", "Default: 3 (HV)" })
        @Config.RangeInt(min = 0, max = 14)
        public int voltageTierAdvImpeller = 3;

        @Config.Comment({ "Random chance for electric tools to take actual damage", "Default: 10%" })
        @Config.RangeInt(min = 0, max = 100)
        @Config.SlidingOption
        public int rngDamageElectricTools = 10;

        @Config.Comment("Armor HUD Location")
        public ArmorHud armorHud = new ArmorHud();

        @Config.Comment({ "How often items should be moved by a magnet", "Default: 10 ticks" })
        @Config.RangeInt(min = 1, max = 100)
        @Config.SlidingOption
        public int magnetDelay = 10;
    }

    public static class ArmorHud {

        @Config.Comment({ "Sets HUD location", "1 - left-upper corner", "2 - right-upper corner",
                "3 - left-bottom corner", "4 - right-bottom corner" })
        public byte hudLocation = 1;
        @Config.Comment("Horizontal offset of HUD [0 ~ 100)")
        public byte hudOffsetX = 0;
        @Config.Comment("Vertical offset of HUD [0 ~ 100)")
        public byte hudOffsetY = 0;
    }

    public static class NanoSaber {

        @Config.RangeDouble(min = 0, max = 100)
        @Config.Comment({ "The additional damage added when the NanoSaber is powered.", "Default: 20.0" })
        public double nanoSaberDamageBoost = 20;

        @Config.RangeDouble(min = 0, max = 100)
        @Config.Comment({ "The base damage of the NanoSaber.", "Default: 5.0" })
        public double nanoSaberBaseDamage = 5;

        @Config.Comment({ "Should Zombies spawn with charged, active NanoSabers on hard difficulty?", "Default: true" })
        public boolean zombieSpawnWithSabers = true;

        @Config.RangeInt(min = 1, max = 512)
        @Config.Comment({ "The EU/t consumption of the NanoSaber.", "Default: 64" })
        public int energyConsumption = 64;
    }
}
