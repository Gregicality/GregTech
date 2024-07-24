package gregtech.api.unification.ore;

import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.function.TriConsumer;
import gregtech.common.ConfigHolder;

import net.minecraft.client.resources.I18n;

import com.google.common.base.Preconditions;
import crafttweaker.annotations.ZenRegister;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import static gregtech.api.GTValues.M;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.ore.OrePrefix.Conditions.*;
import static gregtech.api.unification.ore.OrePrefix.Flags.ENABLE_UNIFICATION;
import static gregtech.api.unification.ore.OrePrefix.Flags.SELF_REFERENCING;

@ZenClass("mods.gregtech.ore.OrePrefix")
@ZenRegister
public class OrePrefix {

    private final static Map<String, OrePrefix> PREFIXES = new HashMap<>();
    private final static AtomicInteger idCounter = new AtomicInteger(0);

    // Regular Ore Prefix. Ore -> Material is a Oneway Operation! Introduced by Eloraam
    public static final OrePrefix ore = new OrePrefix("ore", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION,
            hasOreProperty);
    public static final OrePrefix oreGranite = new OrePrefix("oreGranite", -1, null, MaterialIconType.ore,
            ENABLE_UNIFICATION, hasOreProperty);
    public static final OrePrefix oreDiorite = new OrePrefix("oreDiorite", -1, null, MaterialIconType.ore,
            ENABLE_UNIFICATION, hasOreProperty);
    public static final OrePrefix oreAndesite = new OrePrefix("oreAndesite", -1, null, MaterialIconType.ore,
            ENABLE_UNIFICATION, hasOreProperty);
    public static final OrePrefix oreBlackgranite = new OrePrefix("oreBlackgranite", -1, null, MaterialIconType.ore,
            ENABLE_UNIFICATION, hasOreProperty);
    public static final OrePrefix oreRedgranite = new OrePrefix("oreRedgranite", -1, null, MaterialIconType.ore,
            ENABLE_UNIFICATION, hasOreProperty);
    public static final OrePrefix oreMarble = new OrePrefix("oreMarble", -1, null, MaterialIconType.ore,
            ENABLE_UNIFICATION, hasOreProperty);
    public static final OrePrefix oreBasalt = new OrePrefix("oreBasalt", -1, null, MaterialIconType.ore,
            ENABLE_UNIFICATION, hasOreProperty);

    // In case of an Sand-Ores Mod. Ore -> Material is a Oneway Operation!
    public static final OrePrefix oreSand = new OrePrefix("oreSand", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION,
            null);
    public static final OrePrefix oreRedSand = new OrePrefix("oreRedSand", -1, null, MaterialIconType.ore,
            ENABLE_UNIFICATION, null);

    // Prefix of the Nether-Ores Mod. Causes Ores to double. Ore -> Material is a Oneway Operation!
    public static final OrePrefix oreNetherrack = new OrePrefix("oreNetherrack", -1, null, MaterialIconType.ore,
            ENABLE_UNIFICATION, hasOreProperty);
    // In case of an End-Ores Mod. Ore -> Material is a Oneway Operation!
    public static final OrePrefix oreEndstone = new OrePrefix("oreEndstone", -1, null, MaterialIconType.ore,
            ENABLE_UNIFICATION, hasOreProperty);

    public static final OrePrefix crushedCentrifuged = new OrePrefix("crushedCentrifuged", -1, null,
            MaterialIconType.crushedCentrifuged, ENABLE_UNIFICATION, hasOreProperty);
    public static final OrePrefix crushedPurified = new OrePrefix("crushedPurified", -1, null,
            MaterialIconType.crushedPurified, ENABLE_UNIFICATION, hasOreProperty);
    public static final OrePrefix crushed = new OrePrefix("crushed", -1, null, MaterialIconType.crushed,
            ENABLE_UNIFICATION, hasOreProperty,
            mat -> Collections.singletonList(I18n.format("metaitem.crushed.tooltip.purify")));

    // Introduced by Mekanism
    public static final OrePrefix shard = new OrePrefix("shard", -1, null, null, ENABLE_UNIFICATION, null);
    public static final OrePrefix clump = new OrePrefix("clump", -1, null, null, ENABLE_UNIFICATION, null);
    public static final OrePrefix reduced = new OrePrefix("reduced", -1, null, null, ENABLE_UNIFICATION, null);
    public static final OrePrefix crystalline = new OrePrefix("crystalline", -1, null, null, ENABLE_UNIFICATION, null);

    public static final OrePrefix cleanGravel = new OrePrefix("cleanGravel", -1, null, null, ENABLE_UNIFICATION, null);
    public static final OrePrefix dirtyGravel = new OrePrefix("dirtyGravel", -1, null, null, ENABLE_UNIFICATION, null);

    // A hot Ingot, which has to be cooled down by a Vacuum Freezer.
    public static final OrePrefix ingotHot = new OrePrefix("ingotHot", M, null, MaterialIconType.ingotHot,
            ENABLE_UNIFICATION,
            hasBlastProperty.and(mat -> mat.getProperty(PropertyKey.BLAST).getBlastTemperature() > 1750));
    // A regular Ingot. Introduced by Eloraam
    public static final OrePrefix ingot = new OrePrefix("ingot", M, null, MaterialIconType.ingot, ENABLE_UNIFICATION,
            hasIngotProperty);

    // A regular Gem worth one Dust. Introduced by Eloraam
    public static final OrePrefix gem = new OrePrefix("gem", M, null, MaterialIconType.gem, ENABLE_UNIFICATION,
            hasGemProperty);
    // A regular Gem worth one small Dust. Introduced by TerraFirmaCraft
    public static final OrePrefix gemChipped = new OrePrefix("gemChipped", M / 4, null, MaterialIconType.gemChipped,
            ENABLE_UNIFICATION, hasGemProperty.and(unused -> ConfigHolder.recipes.generateLowQualityGems));
    // A regular Gem worth two small Dusts. Introduced by TerraFirmaCraft
    public static final OrePrefix gemFlawed = new OrePrefix("gemFlawed", M / 2, null, MaterialIconType.gemFlawed,
            ENABLE_UNIFICATION, hasGemProperty.and(unused -> ConfigHolder.recipes.generateLowQualityGems));
    // A regular Gem worth two Dusts. Introduced by TerraFirmaCraft
    public static final OrePrefix gemFlawless = new OrePrefix("gemFlawless", M * 2, null, MaterialIconType.gemFlawless,
            ENABLE_UNIFICATION, hasGemProperty);
    // A regular Gem worth four Dusts. Introduced by TerraFirmaCraft
    public static final OrePrefix gemExquisite = new OrePrefix("gemExquisite", M * 4, null,
            MaterialIconType.gemExquisite, ENABLE_UNIFICATION, hasGemProperty);

    // 1/4th of a Dust.
    public static final OrePrefix dustSmall = new OrePrefix("dustSmall", M / 4, null, MaterialIconType.dustSmall,
            ENABLE_UNIFICATION, hasDustProperty);
    // 1/9th of a Dust.
    public static final OrePrefix dustTiny = new OrePrefix("dustTiny", M / 9, null, MaterialIconType.dustTiny,
            ENABLE_UNIFICATION, hasDustProperty);
    // Dust with impurities. 1 Unit of Main Material and 1/9 - 1/4 Unit of secondary Material
    public static final OrePrefix dustImpure = new OrePrefix("dustImpure", M, null, MaterialIconType.dustImpure,
            ENABLE_UNIFICATION, hasOreProperty,
            mat -> Collections.singletonList(I18n.format("metaitem.dust.tooltip.purify")));
    // Pure Dust worth of one Ingot or Gem. Introduced by Alblaka.
    public static final OrePrefix dustPure = new OrePrefix("dustPure", M, null, MaterialIconType.dustPure,
            ENABLE_UNIFICATION, hasOreProperty,
            mat -> Collections.singletonList(I18n.format("metaitem.dust.tooltip.purify")));
    public static final OrePrefix dust = new OrePrefix("dust", M, null, MaterialIconType.dust, ENABLE_UNIFICATION,
            hasDustProperty);

    // A Nugget. Introduced by Eloraam
    public static final OrePrefix nugget = new OrePrefix("nugget", M / 9, null, MaterialIconType.nugget,
            ENABLE_UNIFICATION, hasIngotProperty);

    // 9 Plates combined in one Item.
    public static final OrePrefix plateDense = new OrePrefix("plateDense", M * 9, null, MaterialIconType.plateDense,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_DENSE));
    // 2 Plates combined in one Item
    public static final OrePrefix plateDouble = new OrePrefix("plateDouble", M * 2, null, MaterialIconType.plateDouble,
            ENABLE_UNIFICATION, hasIngotProperty
                    .and(mat -> mat.hasFlags(GENERATE_PLATE, GENERATE_DOUBLE_PLATE) && !mat.hasFlag(NO_SMASHING)));
    // Regular Plate made of one Ingot/Dust. Introduced by Calclavia
    public static final OrePrefix plate = new OrePrefix("plate", M, null, MaterialIconType.plate, ENABLE_UNIFICATION,
            mat -> mat.hasFlag(GENERATE_PLATE));

    // Round made of 1 Nugget
    public static final OrePrefix round = new OrePrefix("round", M / 9, null, MaterialIconType.round,
            OrePrefix.Flags.ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_ROUND));
    // Foil made of 1/4 Ingot/Dust.
    public static final OrePrefix foil = new OrePrefix("foil", M / 4, null, MaterialIconType.foil, ENABLE_UNIFICATION,
            mat -> mat.hasFlag(GENERATE_FOIL));

    // Stick made of an Ingot.
    public static final OrePrefix stickLong = new OrePrefix("stickLong", M, null, MaterialIconType.stickLong,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_LONG_ROD));
    // Stick made of half an Ingot. Introduced by Eloraam
    public static final OrePrefix stick = new OrePrefix("stick", M / 2, null, MaterialIconType.stick,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_ROD));

    // consisting out of 1/8 Ingot or 1/4 Stick.
    public static final OrePrefix bolt = new OrePrefix("bolt", M / 8, null, MaterialIconType.bolt, ENABLE_UNIFICATION,
            mat -> mat.hasFlag(GENERATE_BOLT_SCREW));
    // consisting out of 1/9 Ingot.
    public static final OrePrefix screw = new OrePrefix("screw", M / 9, null, MaterialIconType.screw,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_BOLT_SCREW));
    // consisting out of 1/2 Stick.
    public static final OrePrefix ring = new OrePrefix("ring", M / 4, null, MaterialIconType.ring, ENABLE_UNIFICATION,
            mat -> mat.hasFlag(GENERATE_RING));
    // consisting out of 1 Fine Wire.
    public static final OrePrefix springSmall = new OrePrefix("springSmall", M / 4, null, MaterialIconType.springSmall,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_SPRING_SMALL) && !mat.hasFlag(NO_SMASHING));
    // consisting out of 2 Sticks.
    public static final OrePrefix spring = new OrePrefix("spring", M, null, MaterialIconType.spring, ENABLE_UNIFICATION,
            mat -> mat.hasFlag(GENERATE_SPRING) && !mat.hasFlag(NO_SMASHING));
    // consisting out of 1/8 Ingot or 1/4 Wire.
    public static final OrePrefix wireFine = new OrePrefix("wireFine", M / 8, null, MaterialIconType.wireFine,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_FINE_WIRE));
    // consisting out of 4 Plates, 1 Ring and 1 Screw.
    public static final OrePrefix rotor = new OrePrefix("rotor", M * 4, null, MaterialIconType.rotor,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_ROTOR));
    public static final OrePrefix gearSmall = new OrePrefix("gearSmall", M, null, MaterialIconType.gearSmall,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_SMALL_GEAR));
    // Introduced by me because BuildCraft has ruined the gear Prefix...
    public static final OrePrefix gear = new OrePrefix("gear", M * 4, null, MaterialIconType.gear, ENABLE_UNIFICATION,
            mat -> mat.hasFlag(GENERATE_GEAR));
    // 3/4 of a Plate or Gem used to shape a Lens. Normally only used on Transparent Materials.
    public static final OrePrefix lens = new OrePrefix("lens", (M * 3) / 4, null, MaterialIconType.lens,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_LENS));

    // made of 4 Ingots.
    public static final OrePrefix toolHeadBuzzSaw = new OrePrefix("toolHeadBuzzSaw", M * 4, null,
            MaterialIconType.toolHeadBuzzSaw, ENABLE_UNIFICATION,
            hasNoCraftingToolProperty.and(mat -> mat.hasFlag(GENERATE_PLATE)));
    // made of 1 Ingots.
    public static final OrePrefix toolHeadScrewdriver = new OrePrefix("toolHeadScrewdriver", M, null,
            MaterialIconType.toolHeadScrewdriver, ENABLE_UNIFICATION,
            hasNoCraftingToolProperty.and(mat -> mat.hasFlag(GENERATE_LONG_ROD)));
    // made of 4 Ingots.
    public static final OrePrefix toolHeadDrill = new OrePrefix("toolHeadDrill", M * 4, null,
            MaterialIconType.toolHeadDrill, ENABLE_UNIFICATION,
            hasToolProperty.and(mat -> mat.hasFlag(GENERATE_PLATE)));
    // made of 2 Ingots.
    public static final OrePrefix toolHeadChainsaw = new OrePrefix("toolHeadChainsaw", M * 2, null,
            MaterialIconType.toolHeadChainsaw, ENABLE_UNIFICATION,
            hasNoCraftingToolProperty.and(mat -> mat.hasFlag(GENERATE_PLATE)));
    // made of 4 Ingots.
    public static final OrePrefix toolHeadWrench = new OrePrefix("toolHeadWrench", M * 4, null,
            MaterialIconType.toolHeadWrench, ENABLE_UNIFICATION,
            hasNoCraftingToolProperty.and(mat -> mat.hasFlag(GENERATE_PLATE)));
    // made of 5 Ingots.
    public static final OrePrefix turbineBlade = new OrePrefix("turbineBlade", M * 10, null,
            MaterialIconType.turbineBlade, ENABLE_UNIFICATION, hasRotorProperty
                    .and(m -> m.hasFlags(GENERATE_BOLT_SCREW, GENERATE_PLATE) && !m.hasProperty(PropertyKey.GEM)));

    public static final OrePrefix paneGlass = new OrePrefix("paneGlass", -1, MarkerMaterials.Color.Colorless, null,
            SELF_REFERENCING, null);
    public static final OrePrefix blockGlass = new OrePrefix("blockGlass", -1, MarkerMaterials.Color.Colorless, null,
            SELF_REFERENCING, null);

    // Storage Block consisting out of 9 Ingots/Gems/Dusts. Introduced by CovertJaguar
    public static final OrePrefix block = new OrePrefix("block", M * 9, null, MaterialIconType.block,
            ENABLE_UNIFICATION, null);

    // Prefix used for Logs. Usually as "logWood". Introduced by Eloraam
    public static final OrePrefix log = new OrePrefix("log", -1, null, null, 0, null);
    // Prefix for Planks. Usually "plankWood". Introduced by Eloraam
    public static final OrePrefix plank = new OrePrefix("plank", -1, null, null, 0, null);
    // Prefix for Wood Slabs
    public static final OrePrefix slab = new OrePrefix("slab", -1, null, null, 0, null);
    // Prefix for Stairs
    public static final OrePrefix stair = new OrePrefix("stair", -1, null, null, 0, null);
    // Prefix for Fences
    public static final OrePrefix fence = new OrePrefix("fence", -1, null, null, 0, null);
    // Prefix for Fence Gates
    public static final OrePrefix fenceGate = new OrePrefix("fenceGate", -1, null, null, 0, null);
    // Prefix for Doors
    public static final OrePrefix door = new OrePrefix("door", -1, null, null, 0, null);

    // Prefix for Lamps
    public static final OrePrefix lampGt = new OrePrefix("lampGt", -1, null, null, 0, null);

    // Prefix to determine which kind of Rock this is.
    public static final OrePrefix stone = new OrePrefix("stone", -1, Materials.Stone, null, SELF_REFERENCING, null);

    public static final OrePrefix frameGt = new OrePrefix("frameGt", M * 2, null, null, ENABLE_UNIFICATION,
            material -> material.hasFlag(GENERATE_FRAME));

    public static final OrePrefix pipeTinyFluid = new OrePrefix("pipeTinyFluid", M / 2, null, null, ENABLE_UNIFICATION,
            null);
    public static final OrePrefix pipeSmallFluid = new OrePrefix("pipeSmallFluid", M, null, null, ENABLE_UNIFICATION,
            null);
    public static final OrePrefix pipeNormalFluid = new OrePrefix("pipeNormalFluid", M * 3, null, null,
            ENABLE_UNIFICATION, null);
    public static final OrePrefix pipeLargeFluid = new OrePrefix("pipeLargeFluid", M * 6, null, null,
            ENABLE_UNIFICATION, null);
    public static final OrePrefix pipeHugeFluid = new OrePrefix("pipeHugeFluid", M * 12, null, null, ENABLE_UNIFICATION,
            null);
    public static final OrePrefix pipeQuadrupleFluid = new OrePrefix("pipeQuadrupleFluid", M * 4, null, null,
            ENABLE_UNIFICATION, null);
    public static final OrePrefix pipeNonupleFluid = new OrePrefix("pipeNonupleFluid", M * 9, null, null,
            ENABLE_UNIFICATION, null);

    public static final OrePrefix pipeTinyItem = new OrePrefix("pipeTinyItem", M / 2, null, null, ENABLE_UNIFICATION,
            null);
    public static final OrePrefix pipeSmallItem = new OrePrefix("pipeSmallItem", M, null, null, ENABLE_UNIFICATION,
            null);
    public static final OrePrefix pipeNormalItem = new OrePrefix("pipeNormalItem", M * 3, null, null,
            ENABLE_UNIFICATION, null);
    public static final OrePrefix pipeLargeItem = new OrePrefix("pipeLargeItem", M * 6, null, null, ENABLE_UNIFICATION,
            null);
    public static final OrePrefix pipeHugeItem = new OrePrefix("pipeHugeItem", M * 12, null, null, ENABLE_UNIFICATION,
            null);

    public static final OrePrefix pipeSmallRestrictive = new OrePrefix("pipeSmallRestrictive", M, null, null,
            ENABLE_UNIFICATION, null);
    public static final OrePrefix pipeNormalRestrictive = new OrePrefix("pipeNormalRestrictive", M * 3, null, null,
            ENABLE_UNIFICATION, null);
    public static final OrePrefix pipeLargeRestrictive = new OrePrefix("pipeLargeRestrictive", M * 6, null, null,
            ENABLE_UNIFICATION, null);
    public static final OrePrefix pipeHugeRestrictive = new OrePrefix("pipeHugeRestrictive", M * 12, null, null,
            ENABLE_UNIFICATION, null);

    public static final OrePrefix wireGtHex = new OrePrefix("wireGtHex", M * 8, null, null, ENABLE_UNIFICATION, null);
    public static final OrePrefix wireGtOctal = new OrePrefix("wireGtOctal", M * 4, null, null, ENABLE_UNIFICATION,
            null);
    public static final OrePrefix wireGtQuadruple = new OrePrefix("wireGtQuadruple", M * 2, null, null,
            ENABLE_UNIFICATION, null);
    public static final OrePrefix wireGtDouble = new OrePrefix("wireGtDouble", M, null, null, ENABLE_UNIFICATION, null);
    public static final OrePrefix wireGtSingle = new OrePrefix("wireGtSingle", M / 2, null, null, ENABLE_UNIFICATION,
            null);

    public static final OrePrefix cableGtHex = new OrePrefix("cableGtHex", M * 8, null, null, ENABLE_UNIFICATION, null);
    public static final OrePrefix cableGtOctal = new OrePrefix("cableGtOctal", M * 4, null, null, ENABLE_UNIFICATION,
            null);
    public static final OrePrefix cableGtQuadruple = new OrePrefix("cableGtQuadruple", M * 2, null, null,
            ENABLE_UNIFICATION, null);
    public static final OrePrefix cableGtDouble = new OrePrefix("cableGtDouble", M, null, null, ENABLE_UNIFICATION,
            null);
    public static final OrePrefix cableGtSingle = new OrePrefix("cableGtSingle", M / 2, null, null, ENABLE_UNIFICATION,
            null);

    // Special Prefix used mainly for the Crafting Handler.
    public static final OrePrefix craftingLens = new OrePrefix("craftingLens", -1, null, null, 0, null);
    // Used for the 16 dyes. Introduced by Eloraam
    public static final OrePrefix dye = new OrePrefix("dye", -1, null, null, 0, null);

    /**
     * Electric Components.
     *
     * @see MarkerMaterials.Tier
     */
    // Introduced by Calclavia
    public static final OrePrefix battery = new OrePrefix("battery", -1, null, null, 0, null);
    // Introduced by Calclavia
    public static final OrePrefix circuit = new OrePrefix("circuit", -1, null, null, ENABLE_UNIFICATION, null);
    public static final OrePrefix component = new OrePrefix("component", -1, null, null, ENABLE_UNIFICATION, null);

    // Nuclear stuff, introduced by Zalgo and Bruberu
    public static final OrePrefix fuelRod = new OrePrefix("fuelRod", -1, null, MaterialIconType.fuelRod, 0,
            material -> material.hasProperty(PropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));
    public static final OrePrefix fuelRodDepleted = new OrePrefix("fuelRodDepleted", -1, null,
            MaterialIconType.fuelRodDepleted, 0, material -> material.hasProperty(PropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));
    public static final OrePrefix fuelRodHotDepleted = new OrePrefix("fuelRodHotDepleted", -1, null,
            MaterialIconType.fuelRodHotDepleted, 0, material -> material.hasProperty(PropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));
    public static final OrePrefix fuelPellet = new OrePrefix("fuelPellet", -1, null,
            MaterialIconType.fuelPellet, 0, material -> material.hasProperty(PropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));
    public static final OrePrefix fuelPelletDepleted = new OrePrefix("fuelPelletDepleted", -1, null,
            MaterialIconType.fuelPelletDepleted, 0, material -> material.hasProperty(PropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));

    public static final OrePrefix dustSpentFuel = new OrePrefix("dustSpentFuel", -1, null,
            MaterialIconType.dustSpentFuel, 0, material -> material.hasProperty(PropertyKey.FISSION_FUEL));
    public static final OrePrefix dustBredFuel = new OrePrefix("dustBredFuel", -1, null,
            MaterialIconType.dustBredFuel, 0, material -> material.hasProperty(PropertyKey.FISSION_FUEL));
    public static final OrePrefix dustFissionByproduct = new OrePrefix("dustFissionByproduct", -1, null,
            MaterialIconType.dustFissionByproduct, 0, material -> material.hasProperty(PropertyKey.FISSION_FUEL));

    public static class Flags {

        public static final long ENABLE_UNIFICATION = 1;
        public static final long SELF_REFERENCING = 1 << 1;
    }

    public static class Conditions {

        public static final Predicate<Material> hasToolProperty = mat -> mat.hasProperty(PropertyKey.TOOL);
        public static final Predicate<Material> hasNoCraftingToolProperty = hasToolProperty
                .and(mat -> !mat.getProperty(PropertyKey.TOOL).getShouldIgnoreCraftingTools());
        public static final Predicate<Material> hasOreProperty = mat -> mat.hasProperty(PropertyKey.ORE);
        public static final Predicate<Material> hasGemProperty = mat -> mat.hasProperty(PropertyKey.GEM);
        public static final Predicate<Material> hasDustProperty = mat -> mat.hasProperty(PropertyKey.DUST);
        public static final Predicate<Material> hasIngotProperty = mat -> mat.hasProperty(PropertyKey.INGOT);
        public static final Predicate<Material> hasBlastProperty = mat -> mat.hasProperty(PropertyKey.BLAST);
        public static final Predicate<Material> hasRotorProperty = mat -> mat.hasProperty(PropertyKey.ROTOR);
    }

    public static void init() {
        ingotHot.heatDamageFunction = (temp) -> ((temp - 1750) / 1000.0F) + 2;
        gemFlawless.maxStackSize = 32;
        gemExquisite.maxStackSize = 16;

        plateDouble.maxStackSize = 32;
        plateDense.maxStackSize = 7;
        rotor.maxStackSize = 16;
        gear.maxStackSize = 16;

        toolHeadBuzzSaw.maxStackSize = 16;
        toolHeadScrewdriver.maxStackSize = 16;
        toolHeadDrill.maxStackSize = 16;
        toolHeadChainsaw.maxStackSize = 16;
        toolHeadWrench.maxStackSize = 16;

        craftingLens.setMarkerPrefix(true);
        dye.setMarkerPrefix(true);
        battery.setMarkerPrefix(true);
        circuit.setMarkerPrefix(true);

        gemExquisite.setIgnored(Materials.Sugar);

        gemFlawless.setIgnored(Materials.Sugar);

        gem.setIgnored(Materials.Diamond);
        gem.setIgnored(Materials.Emerald);
        gem.setIgnored(Materials.Lapis);
        gem.setIgnored(Materials.NetherQuartz);
        gem.setIgnored(Materials.Coal);

        excludeAllGems(Materials.Charcoal);
        excludeAllGems(Materials.NetherStar);
        excludeAllGems(Materials.EnderPearl);
        excludeAllGems(Materials.EnderEye);
        excludeAllGems(Materials.Flint);
        excludeAllGemsButNormal(Materials.Lapotron);

        dust.setIgnored(Materials.Redstone);
        dust.setIgnored(Materials.Glowstone);
        dust.setIgnored(Materials.Gunpowder);
        dust.setIgnored(Materials.Sugar);
        dust.setIgnored(Materials.Bone);
        dust.setIgnored(Materials.Blaze);

        stick.setIgnored(Materials.Wood);
        stick.setIgnored(Materials.Bone);
        stick.setIgnored(Materials.Blaze);
        stick.setIgnored(Materials.Paper);

        ingot.setIgnored(Materials.Iron);
        ingot.setIgnored(Materials.Gold);
        ingot.setIgnored(Materials.Paper);

        nugget.setIgnored(Materials.Gold);
        nugget.setIgnored(Materials.Paper);
        nugget.setIgnored(Materials.Iron);

        plate.setIgnored(Materials.Paper);

        block.setIgnored(Materials.Iron);
        block.setIgnored(Materials.Gold);
        block.setIgnored(Materials.Lapis);
        block.setIgnored(Materials.Emerald);
        block.setIgnored(Materials.Redstone);
        block.setIgnored(Materials.Diamond);
        block.setIgnored(Materials.Coal);
        block.setIgnored(Materials.Glass);
        block.setIgnored(Materials.Marble);
        block.setIgnored(Materials.GraniteRed);
        block.setIgnored(Materials.Stone);
        block.setIgnored(Materials.Glowstone);
        block.setIgnored(Materials.Endstone);
        block.setIgnored(Materials.Wheat);
        block.setIgnored(Materials.Oilsands);
        block.setIgnored(Materials.Wood);
        block.setIgnored(Materials.TreatedWood);
        block.setIgnored(Materials.RawRubber);
        block.setIgnored(Materials.Clay);
        block.setIgnored(Materials.Brick);
        block.setIgnored(Materials.Bone);
        block.setIgnored(Materials.NetherQuartz);
        block.setIgnored(Materials.Ice);
        block.setIgnored(Materials.Netherrack);
        block.setIgnored(Materials.Concrete);
        block.setIgnored(Materials.Blaze);
        block.setIgnored(Materials.Lapotron);

        ore.addSecondaryMaterial(new MaterialStack(Materials.Stone, dust.materialAmount));
        oreNetherrack.addSecondaryMaterial(new MaterialStack(Materials.Netherrack, dust.materialAmount));
        oreEndstone.addSecondaryMaterial(new MaterialStack(Materials.Endstone, dust.materialAmount));

        if (ConfigHolder.worldgen.allUniqueStoneTypes) {
            oreGranite.addSecondaryMaterial(new MaterialStack(Materials.Granite, dust.materialAmount));
            oreDiorite.addSecondaryMaterial(new MaterialStack(Materials.Diorite, dust.materialAmount));
            oreAndesite.addSecondaryMaterial(new MaterialStack(Materials.Andesite, dust.materialAmount));
            oreRedgranite.addSecondaryMaterial(new MaterialStack(Materials.GraniteRed, dust.materialAmount));
            oreBlackgranite.addSecondaryMaterial(new MaterialStack(Materials.GraniteBlack, dust.materialAmount));
            oreBasalt.addSecondaryMaterial(new MaterialStack(Materials.Basalt, dust.materialAmount));
            oreMarble.addSecondaryMaterial(new MaterialStack(Materials.Marble, dust.materialAmount));
            oreSand.addSecondaryMaterial(new MaterialStack(Materials.SiliconDioxide, dustTiny.materialAmount));
            oreRedSand.addSecondaryMaterial(new MaterialStack(Materials.SiliconDioxide, dustTiny.materialAmount));
        }

        crushed.addSecondaryMaterial(new MaterialStack(Materials.Stone, dust.materialAmount));

        toolHeadDrill.addSecondaryMaterial(new MaterialStack(Materials.Steel, plate.materialAmount * 4));
        toolHeadChainsaw.addSecondaryMaterial(
                new MaterialStack(Materials.Steel, plate.materialAmount * 4 + ring.materialAmount * 2));
        toolHeadWrench.addSecondaryMaterial(
                new MaterialStack(Materials.Steel, ring.materialAmount + screw.materialAmount * 2));

        pipeSmallRestrictive.addSecondaryMaterial(new MaterialStack(Materials.Iron, ring.materialAmount * 2));
        pipeNormalRestrictive.addSecondaryMaterial(new MaterialStack(Materials.Iron, ring.materialAmount * 2));
        pipeLargeRestrictive.addSecondaryMaterial(new MaterialStack(Materials.Iron, ring.materialAmount * 2));
        pipeHugeRestrictive.addSecondaryMaterial(new MaterialStack(Materials.Iron, ring.materialAmount * 2));

        cableGtSingle.addSecondaryMaterial(new MaterialStack(Materials.Rubber, plate.materialAmount));
        cableGtDouble.addSecondaryMaterial(new MaterialStack(Materials.Rubber, plate.materialAmount));
        cableGtQuadruple.addSecondaryMaterial(new MaterialStack(Materials.Rubber, plate.materialAmount * 2));
        cableGtOctal.addSecondaryMaterial(new MaterialStack(Materials.Rubber, plate.materialAmount * 3));
        cableGtHex.addSecondaryMaterial(new MaterialStack(Materials.Rubber, plate.materialAmount * 5));

        plate.setIgnored(Materials.BorosilicateGlass);
        foil.setIgnored(Materials.BorosilicateGlass);

        dustSmall.setIgnored(Materials.Lapotron);
        dustTiny.setIgnored(Materials.Lapotron);

        block.modifyMaterialAmount(Materials.Glowstone, 4);
        block.modifyMaterialAmount(Materials.NetherQuartz, 4);
        block.modifyMaterialAmount(Materials.CertusQuartz, 4);
        block.modifyMaterialAmount(Materials.Brick, 4);
        block.modifyMaterialAmount(Materials.Clay, 4);
        block.modifyMaterialAmount(Materials.Glass, 1);
        block.modifyMaterialAmount(Materials.Ice, 1);
        block.modifyMaterialAmount(Materials.Obsidian, 1);
        block.modifyMaterialAmount(Materials.Concrete, 1);

        stick.modifyMaterialAmount(Materials.Blaze, 4);
        stick.modifyMaterialAmount(Materials.Bone, 5);

        fuelRod.radiationDamageFunction = (neutrons) -> neutrons / 10e23;
        fuelPellet.radiationDamageFunction = (neutrons) -> neutrons / 160e23;

        fuelRodDepleted.radiationDamageFunction = (neutrons) -> neutrons / 1.5e23;
        fuelRodHotDepleted.radiationDamageFunction = (neutrons) -> neutrons / 1e23;
        fuelRodHotDepleted.heatDamageFunction = (x) -> 2f;
        fuelPelletDepleted.radiationDamageFunction = (neutrons) -> neutrons / 24e23;
    }

    private static void excludeAllGems(Material material) {
        gem.setIgnored(material);
        excludeAllGemsButNormal(material);
    }

    private static void excludeAllGemsButNormal(Material material) {
        gemChipped.setIgnored(material);
        gemFlawed.setIgnored(material);
        gemFlawless.setIgnored(material);
        gemExquisite.setIgnored(material);
    }

    public final String name;
    public final int id;

    public final boolean isUnificationEnabled;
    public final boolean isSelfReferencing;

    private @Nullable Predicate<Material> generationCondition;
    public final @Nullable MaterialIconType materialIconType;

    private final long materialAmount;

    /**
     * Contains a default material type for self-referencing OrePrefix For self-referencing prefixes, it is always
     * guaranteed for it to be not null
     * <p>
     * NOTE: Ore registrations with self-referencing OrePrefix still can occur with other materials
     */
    public @Nullable Material materialType;

    private final List<IOreRegistrationHandler> oreProcessingHandlers = new ArrayList<>();
    private final Set<Material> ignoredMaterials = new HashSet<>();
    private final Set<Material> generatedMaterials = new HashSet<>();
    private final Object2FloatMap<Material> materialAmounts = new Object2FloatOpenHashMap<>();
    private boolean isMarkerPrefix = false;

    public byte maxStackSize = 64;
    public final List<MaterialStack> secondaryMaterials = new ArrayList<>();
    public Function<Integer, Float> heatDamageFunction = null; // Negative for Frost Damage
    public Function<Double, Double> radiationDamageFunction = null;
    public Function<Material, List<String>> tooltipFunc;

    private String alternativeOreName = null;

    public OrePrefix(String name, long materialAmount, @Nullable Material material,
                     @Nullable MaterialIconType materialIconType, long flags, @Nullable Predicate<Material> condition) {
        this(name, materialAmount, material, materialIconType, flags, condition, null);
    }

    public OrePrefix(String name, long materialAmount, @Nullable Material material,
                     @Nullable MaterialIconType materialIconType, long flags, @Nullable Predicate<Material> condition,
                     @Nullable Function<Material, List<String>> tooltipFunc) {
        Preconditions.checkArgument(!PREFIXES.containsKey(name), "OrePrefix " + name + " already registered!");
        this.name = name;
        this.id = idCounter.getAndIncrement();
        this.materialAmount = materialAmount;
        this.isSelfReferencing = (flags & SELF_REFERENCING) != 0;
        this.isUnificationEnabled = (flags & ENABLE_UNIFICATION) != 0;
        this.materialIconType = materialIconType;
        this.generationCondition = condition;
        this.tooltipFunc = tooltipFunc;
        if (isSelfReferencing) {
            Preconditions.checkNotNull(material, "Material is null for self-referencing OrePrefix");
            this.materialType = material;
        }
        PREFIXES.put(name, this);
    }

    public String name() {
        return this.name;
    }

    public void addSecondaryMaterial(MaterialStack secondaryMaterial) {
        Preconditions.checkNotNull(secondaryMaterial, "secondaryMaterial");
        secondaryMaterials.add(secondaryMaterial);
    }

    public void setMarkerPrefix(boolean isMarkerPrefix) {
        this.isMarkerPrefix = isMarkerPrefix;
    }

    public long getMaterialAmount(@Nullable Material material) {
        if (material == null || !isAmountModified(material)) {
            return this.materialAmount;
        }
        return (long) (M * materialAmounts.getFloat(material));
    }

    @ZenMethod
    public static OrePrefix getPrefix(String prefixName) {
        return getPrefix(prefixName, null);
    }

    public static OrePrefix getPrefix(String prefixName, @Nullable OrePrefix replacement) {
        return PREFIXES.getOrDefault(prefixName, replacement);
    }

    public boolean doGenerateItem(Material material) {
        return !isSelfReferencing && !isIgnored(material) &&
                (generationCondition == null || generationCondition.test(material));
    }

    public void setGenerationCondition(@Nullable Predicate<Material> in) {
        generationCondition = in;
    }

    public boolean addProcessingHandler(IOreRegistrationHandler... processingHandler) {
        Preconditions.checkNotNull(processingHandler);
        Validate.noNullElements(processingHandler);
        return oreProcessingHandlers.addAll(Arrays.asList(processingHandler));
    }

    public <T extends IMaterialProperty> void addProcessingHandler(PropertyKey<T> propertyKey,
                                                                   TriConsumer<OrePrefix, Material, T> handler) {
        addProcessingHandler((orePrefix, material) -> {
            if (material.hasProperty(propertyKey) && !material.hasFlag(NO_UNIFICATION)) {
                handler.accept(orePrefix, material, material.getProperty(propertyKey));
            }
        });
    }

    public void processOreRegistration(@Nullable Material material) {
        if (this.isSelfReferencing && material == null) {
            material = materialType; // append default material for self-referencing OrePrefix
        }
        if (material != null) generatedMaterials.add(material);
    }

    public static void runMaterialHandlers() {
        for (OrePrefix orePrefix : PREFIXES.values()) {
            orePrefix.runGeneratedMaterialHandlers();
        }
    }

    private static final ThreadLocal<OrePrefix> currentProcessingPrefix = new ThreadLocal<>();
    private static final ThreadLocal<Material> currentMaterial = new ThreadLocal<>();

    public static OrePrefix getCurrentProcessingPrefix() {
        return currentProcessingPrefix.get();
    }

    public static Material getCurrentMaterial() {
        return currentMaterial.get();
    }

    private void runGeneratedMaterialHandlers() {
        currentProcessingPrefix.set(this);
        for (Material registeredMaterial : generatedMaterials) {
            currentMaterial.set(registeredMaterial);
            for (IOreRegistrationHandler registrationHandler : oreProcessingHandlers) {
                registrationHandler.processMaterial(this, registeredMaterial);
            }
            currentMaterial.remove();
        }
        // clear generated materials for next pass
        generatedMaterials.clear();
        currentProcessingPrefix.remove();
    }

    public void setAlternativeOreName(String name) {
        this.alternativeOreName = name;
    }

    public String getAlternativeOreName() {
        return alternativeOreName;
    }

    // todo clean this up
    public String getLocalNameForItem(@NotNull Material material) {
        String specifiedUnlocalized = "item." + material.getUnlocalizedName() + "." + this.name;
        if (LocalizationUtils.hasKey(specifiedUnlocalized)) return LocalizationUtils.format(specifiedUnlocalized);
        String unlocalized = findUnlocalizedName(material);
        String matLocalized = material.getLocalizedName();
        String formatted = LocalizationUtils.format(unlocalized, matLocalized);
        return formatted.equals(unlocalized) ? matLocalized : formatted;
    }

    private String findUnlocalizedName(@NotNull Material material) {
        if (material.hasProperty(PropertyKey.POLYMER)) {
            String localizationKey = String.format("item.material.oreprefix.polymer.%s", this.name);
            // Not every polymer ore prefix gets a special name
            if (LocalizationUtils.hasKey(localizationKey)) {
                return localizationKey;
            }
        }

        return String.format("item.material.oreprefix.%s", this.name);
    }

    public boolean isIgnored(Material material) {
        return ignoredMaterials.contains(material);
    }

    @ZenMethod
    public void setIgnored(Material material) {
        ignoredMaterials.add(material);
    }

    @ZenMethod
    public void removeIgnored(@NotNull Material material) {
        ignoredMaterials.remove(material);
    }

    public boolean isAmountModified(Material material) {
        return materialAmounts.containsKey(material);
    }

    @ZenMethod
    public void modifyMaterialAmount(@NotNull Material material, float amount) {
        materialAmounts.put(material, amount);
    }

    public boolean isMarkerPrefix() {
        return isMarkerPrefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrePrefix orePrefix = (OrePrefix) o;
        return name.equals(orePrefix.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static Collection<OrePrefix> values() {
        return PREFIXES.values();
    }

    @Override
    public String toString() {
        return name + "/" + id;
    }
}
