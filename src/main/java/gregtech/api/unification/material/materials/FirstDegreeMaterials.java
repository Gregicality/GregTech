package gregtech.api.unification.material.materials;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.BlastProperty.GasTier;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;

import net.minecraft.init.Enchantments;
import net.minecraftforge.fluids.FluidRegistry;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;
import static gregtech.api.util.GTUtility.gregtechId;

public class FirstDegreeMaterials {

    public static void register() {
        Almandine = new Material.Builder(250, gregtechId("almandine"))
                .gem(1).ore(3, 1)
                .color(0xFF0000)
                .components(Aluminium, 2, Iron, 3, Silicon, 3, Oxygen, 12)
                .build();

        Andradite = new Material.Builder(251, gregtechId("andradite"))
                .gem(1)
                .color(0x967800).iconSet(RUBY)
                .components(Calcium, 3, Iron, 2, Silicon, 3, Oxygen, 12)
                .build();

        AnnealedCopper = new Material.Builder(252, gregtechId("annealed_copper"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1358))
                .color(0xFF8D3B).iconSet(BRIGHT)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_FINE_WIRE)
                .components(Copper, 1)
                .cableProperties(V[MV], 1, 1)
                .build();
        Copper.getProperty(PropertyKey.INGOT).setArcSmeltingInto(AnnealedCopper);

        Asbestos = new Material.Builder(253, gregtechId("asbestos"))
                .dust(1).ore(3, 1)
                .color(0xE6E6E6)
                .components(Magnesium, 3, Silicon, 2, Hydrogen, 4, Oxygen, 9)
                .build();

        Ash = new Material.Builder(254, gregtechId("ash"))
                .dust(1)
                .color(0x969696)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        BandedIron = new Material.Builder(255, gregtechId("banded_iron"))
                .dust().ore()
                .color(0x915A5A)
                .components(Iron, 2, Oxygen, 3)
                .build();

        BatteryAlloy = new Material.Builder(256, gregtechId("battery_alloy"))
                .ingot(1)
                .liquid(new FluidBuilder().temperature(660))
                .color(0x9C7CA0)
                .flags(EXT_METAL)
                .components(Lead, 4, Antimony, 1)
                .build();

        BlueTopaz = new Material.Builder(257, gregtechId("blue_topaz"))
                .gem(3).ore(2, 1)
                .color(0x7B96DC).iconSet(GEM_HORIZONTAL)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Silicon, 1, Fluorine, 2, Hydrogen, 2, Oxygen, 6)
                .build();

        Bone = new Material.Builder(258, gregtechId("bone"))
                .dust(1)
                .color(0xFAFAFA)
                .flags(MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Calcium, 1)
                .build();

        Brass = new Material.Builder(259, gregtechId("brass"))
                .ingot(1)
                .liquid(new FluidBuilder().temperature(1160))
                .color(0xFFB400).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_DOUBLE_PLATE)
                .components(Zinc, 1, Copper, 3)
                .rotorStats(8.0f, 3.0f, 152)
                .itemPipeProperties(2048, 1)
                .build();

        Bronze = new Material.Builder(260, gregtechId("bronze"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1357))
                .color(0xFF8000).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_FRAME, GENERATE_SMALL_GEAR,
                        GENERATE_FOIL, GENERATE_GEAR, GENERATE_DOUBLE_PLATE)
                .components(Tin, 1, Copper, 3)
                .toolStats(ToolProperty.Builder.of(3.0F, 2.0F, 192, 2)
                        .enchantability(18).build())
                .rotorStats(6.0f, 2.5f, 192)
                .fluidPipeProperties(1696, 20, true)
                .build();

        BrownLimonite = new Material.Builder(261, gregtechId("brown_limonite"))
                .dust(1).ore()
                .color(0xC86400).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, BLAST_FURNACE_CALCITE_TRIPLE)
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .build();

        Calcite = new Material.Builder(262, gregtechId("calcite"))
                .dust(1).ore()
                .color(0xFAE6DC)
                .components(Calcium, 1, Carbon, 1, Oxygen, 3)
                .build();

        Cassiterite = new Material.Builder(263, gregtechId("cassiterite"))
                .dust(1).ore(2, 1)
                .color(0xDCDCDC).iconSet(METALLIC)
                .components(Tin, 1, Oxygen, 2)
                .build();

        CassiteriteSand = new Material.Builder(264, gregtechId("cassiterite_sand"))
                .dust(1).ore(2, 1)
                .color(0xDCDCDC).iconSet(SAND)
                .components(Tin, 1, Oxygen, 2)
                .build();

        Chalcopyrite = new Material.Builder(265, gregtechId("chalcopyrite"))
                .dust(1).ore()
                .color(0xA07828)
                .components(Copper, 1, Iron, 1, Sulfur, 2)
                .build();

        Charcoal = new Material.Builder(266, gregtechId("charcoal"))
                .gem(1, 1600) // default charcoal burn time in vanilla
                .color(0x644646).iconSet(FINE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE)
                .components(Carbon, 1)
                .build();

        Chromite = new Material.Builder(267, gregtechId("chromite"))
                .dust(1).ore()
                .color(0x23140F).iconSet(METALLIC)
                .components(Iron, 1, Chrome, 2, Oxygen, 4)
                .build();

        Cinnabar = new Material.Builder(268, gregtechId("cinnabar"))
                .dust(1).ore()
                .color(0x960000).iconSet(EMERALD)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Mercury, 1, Sulfur, 1)
                .build();

        Water = new Material.Builder(269, gregtechId("water"))
                .fluid(FluidRegistry.WATER, FluidStorageKeys.LIQUID, FluidState.LIQUID)
                .color(0x0000FF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .build();

        // FREE ID 270

        Coal = new Material.Builder(271, gregtechId("coal"))
                .gem(1, 1600).ore(2, 1) // default coal burn time in vanilla
                .color(0x464646).iconSet(LIGNITE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Cobaltite = new Material.Builder(272, gregtechId("cobaltite"))
                .dust(1).ore()
                .color(0x5050FA).iconSet(METALLIC)
                .components(Cobalt, 1, Arsenic, 1, Sulfur, 1)
                .build();

        Cooperite = new Material.Builder(273, gregtechId("cooperite"))
                .dust(1).ore()
                .color(0xFFFFC8).iconSet(METALLIC)
                .components(Platinum, 3, Nickel, 1, Sulfur, 1, Palladium, 1)
                .build();

        Cupronickel = new Material.Builder(274, gregtechId("cupronickel"))
                .ingot(1)
                .liquid(new FluidBuilder().temperature(1542))
                .color(0xE39680).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING, GENERATE_FINE_WIRE, GENERATE_DOUBLE_PLATE)
                .components(Copper, 1, Nickel, 1)
                .itemPipeProperties(2048, 1)
                .cableProperties(V[MV], 1, 1)
                .build();

        DarkAsh = new Material.Builder(275, gregtechId("dark_ash"))
                .dust(1)
                .color(0x323232)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Diamond = new Material.Builder(276, gregtechId("diamond"))
                .gem(3).ore()
                .color(0xC8FFFF).iconSet(DIAMOND)
                .flags(GENERATE_BOLT_SCREW, GENERATE_LENS, GENERATE_GEAR, NO_SMASHING, NO_SMELTING,
                        HIGH_SIFTER_OUTPUT, DISABLE_DECOMPOSITION, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Carbon, 1)
                .toolStats(ToolProperty.Builder.of(6.0F, 7.0F, 768, 3)
                        .attackSpeed(0.1F).enchantability(18).build())
                .build();

        Electrum = new Material.Builder(277, gregtechId("electrum"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1285))
                .color(0xFFFF64).iconSet(SHINY)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_FINE_WIRE, GENERATE_RING, GENERATE_DOUBLE_PLATE)
                .components(Silver, 1, Gold, 1)
                .itemPipeProperties(1024, 2)
                .cableProperties(V[HV], 2, 2)
                .build();

        Emerald = new Material.Builder(278, gregtechId("emerald"))
                .gem().ore(2, 1)
                .color(0x50FF50).iconSet(EMERALD)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        GENERATE_LENS)
                .components(Beryllium, 3, Aluminium, 2, Silicon, 6, Oxygen, 18)
                .build();

        Galena = new Material.Builder(279, gregtechId("galena"))
                .dust(3).ore()
                .color(0x643C64)
                .flags(NO_SMELTING)
                .components(Lead, 1, Sulfur, 1)
                .build();

        Garnierite = new Material.Builder(280, gregtechId("garnierite"))
                .dust(3).ore()
                .color(0x32C846).iconSet(METALLIC)
                .components(Nickel, 1, Oxygen, 1)
                .build();

        GreenSapphire = new Material.Builder(281, gregtechId("green_sapphire"))
                .gem().ore()
                .color(0x64C882).iconSet(GEM_HORIZONTAL)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Oxygen, 3)
                .build();

        Grossular = new Material.Builder(282, gregtechId("grossular"))
                .gem(1).ore(3, 1)
                .color(0xC86400).iconSet(RUBY)
                .components(Calcium, 3, Aluminium, 2, Silicon, 3, Oxygen, 12)
                .build();

        Ice = new Material.Builder(283, gregtechId("ice"))
                .dust(0)
                .liquid(new FluidBuilder()
                        .temperature(273)
                        .customStill()
                        .alternativeName("fluid.ice"))
                .color(0xC8C8FF).iconSet(SHINY)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .build();

        Ilmenite = new Material.Builder(284, gregtechId("ilmenite"))
                .dust(3).ore()
                .color(0x463732).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iron, 1, Titanium, 1, Oxygen, 3)
                .build();

        Rutile = new Material.Builder(285, gregtechId("rutile"))
                .gem()
                .color(0xD40D5C).iconSet(GEM_HORIZONTAL)
                .flags(DISABLE_DECOMPOSITION)
                .components(Titanium, 1, Oxygen, 2)
                .build();

        Bauxite = new Material.Builder(286, gregtechId("bauxite"))
                .dust(1).ore()
                .color(0xC86400)
                .flags(DISABLE_DECOMPOSITION)
                .components(Aluminium, 2, Oxygen, 3)
                .build();

        Invar = new Material.Builder(287, gregtechId("invar"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1916))
                .color(0xB4B478).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_FRAME, GENERATE_GEAR)
                .components(Iron, 2, Nickel, 1)
                .toolStats(ToolProperty.Builder.of(4.0F, 3.0F, 384, 2)
                        .enchantability(18)
                        .enchantment(Enchantments.BANE_OF_ARTHROPODS, 3)
                        .enchantment(Enchantments.EFFICIENCY, 1).build())
                .rotorStats(7.0f, 3.0f, 512)
                .build();

        Kanthal = new Material.Builder(288, gregtechId("kanthal"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1708))
                .color(0xC2D2DF).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING)
                .components(Iron, 1, Aluminium, 1, Chrome, 1)
                .cableProperties(V[HV], 4, 3)
                .blast(b -> b.temp(1800, GasTier.LOW).blastStats(VA[HV], 900))
                .build();

        Lazurite = new Material.Builder(289, gregtechId("lazurite"))
                .gem(1).ore(6, 4)
                .color(0x6478FF).iconSet(LAPIS)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, GENERATE_ROD,
                        DECOMPOSITION_BY_ELECTROLYZING)
                .components(Aluminium, 6, Silicon, 6, Calcium, 8, Sodium, 8)
                .build();

        Magnalium = new Material.Builder(290, gregtechId("magnalium"))
                .ingot()
                .liquid(new FluidBuilder().temperature(929))
                .color(0xC8BEFF)
                .flags(EXT2_METAL, GENERATE_DOUBLE_PLATE)
                .components(Magnesium, 1, Aluminium, 2)
                .rotorStats(6.0f, 2.0f, 256)
                .itemPipeProperties(1024, 2)
                .build();

        Magnesite = new Material.Builder(291, gregtechId("magnesite"))
                .dust().ore()
                .color(0xFAFAB4).iconSet(METALLIC)
                .components(Magnesium, 1, Carbon, 1, Oxygen, 3)
                .build();

        Magnetite = new Material.Builder(292, gregtechId("magnetite"))
                .dust().ore()
                .color(0x1E1E1E).iconSet(METALLIC)
                .components(Iron, 3, Oxygen, 4)
                .build();

        Molybdenite = new Material.Builder(293, gregtechId("molybdenite"))
                .dust().ore()
                .color(0x191919).iconSet(METALLIC)
                .components(Molybdenum, 1, Sulfur, 2)
                .build();

        Nichrome = new Material.Builder(294, gregtechId("nichrome"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1818))
                .color(0xCDCEF6).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING)
                .components(Nickel, 4, Chrome, 1)
                .cableProperties(V[EV], 4, 4)
                .blast(b -> b
                        .temp(2700, GasTier.LOW)
                        .blastStats(VA[EV], 1000)
                        .vacuumStats(VA[HV]))
                .build();

        NiobiumNitride = new Material.Builder(295, gregtechId("niobium_nitride"))
                .ingot().fluid()
                .color(0x1D291D)
                .flags(EXT_METAL, GENERATE_FOIL)
                .components(Niobium, 1, Nitrogen, 1)
                .cableProperties(V[LuV], 1, 1)
                .blast(2846, GasTier.MID)
                .build();

        NiobiumTitanium = new Material.Builder(296, gregtechId("niobium_titanium"))
                .ingot()
                .liquid(new FluidBuilder().temperature(2345))
                .color(0x1D1D29)
                .flags(EXT2_METAL, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FOIL, GENERATE_FINE_WIRE,
                        GENERATE_DOUBLE_PLATE)
                .components(Niobium, 1, Titanium, 1)
                .fluidPipeProperties(5900, 175, true)
                .cableProperties(V[LuV], 4, 2)
                .blast(b -> b
                        .temp(4500, GasTier.HIGH)
                        .blastStats(VA[HV], 1500)
                        .vacuumStats(VA[HV], 200))
                .build();

        Obsidian = new Material.Builder(297, gregtechId("obsidian"))
                .dust(3)
                .color(0x503264)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_RECIPES, GENERATE_PLATE, GENERATE_DENSE)
                .components(Magnesium, 1, Iron, 1, Silicon, 2, Oxygen, 4)
                .build();

        Phosphate = new Material.Builder(298, gregtechId("phosphate"))
                .dust(1)
                .color(0xFFFF00)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE, EXPLOSIVE)
                .components(Phosphorus, 1, Oxygen, 4)
                .build();

        PlatinumRaw = new Material.Builder(299, gregtechId("platinum_raw"))
                .dust()
                .color(0xFFFFC8).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Platinum, 1, Chlorine, 2)
                .build();

        SterlingSilver = new Material.Builder(300, gregtechId("sterling_silver"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1258))
                .color(0xFADCE1).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_GEAR, GENERATE_DOUBLE_PLATE)
                .components(Copper, 1, Silver, 4)
                .toolStats(ToolProperty.Builder.of(3.0F, 8.0F, 768, 2)
                        .attackSpeed(0.3F).enchantability(33)
                        .enchantment(Enchantments.SMITE, 3).build())
                .rotorStats(13.0f, 2.0f, 196)
                .itemPipeProperties(1024, 2)
                .blast(b -> b.temp(1700, GasTier.LOW).blastStats(VA[MV], 1000))
                .build();

        RoseGold = new Material.Builder(301, gregtechId("rose_gold"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1341))
                .color(0xFFE61E).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_RING, GENERATE_GEAR, GENERATE_DOUBLE_PLATE)
                .components(Copper, 1, Gold, 4)
                .toolStats(ToolProperty.Builder.of(12.0F, 2.0F, 768, 2)
                        .enchantability(33)
                        .enchantment(Enchantments.FORTUNE, 2).build())
                .rotorStats(14.0f, 2.0f, 152)
                .itemPipeProperties(1024, 2)
                .blast(b -> b.temp(1600, GasTier.LOW).blastStats(VA[MV], 1000))
                .build();

        BlackBronze = new Material.Builder(302, gregtechId("black_bronze"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1328))
                .color(0x64327D)
                .flags(EXT2_METAL, GENERATE_GEAR, GENERATE_DOUBLE_PLATE)
                .components(Gold, 1, Silver, 1, Copper, 3)
                .rotorStats(12.0f, 2.0f, 256)
                .itemPipeProperties(1024, 2)
                .blast(b -> b.temp(2000, GasTier.LOW).blastStats(VA[MV], 1000))
                .build();

        BismuthBronze = new Material.Builder(303, gregtechId("bismuth_bronze"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1036))
                .color(0x647D7D)
                .flags(EXT2_METAL)
                .components(Bismuth, 1, Zinc, 1, Copper, 3)
                .rotorStats(8.0f, 3.0f, 256)
                .blast(b -> b.temp(1100, GasTier.LOW).blastStats(VA[MV], 1000))
                .build();

        Biotite = new Material.Builder(304, gregtechId("biotite"))
                .dust(1)
                .color(0x141E14).iconSet(METALLIC)
                .components(Potassium, 1, Magnesium, 3, Aluminium, 3, Fluorine, 2, Silicon, 3, Oxygen, 10)
                .build();

        Powellite = new Material.Builder(305, gregtechId("powellite"))
                .dust().ore()
                .color(0xFFFF00)
                .components(Calcium, 1, Molybdenum, 1, Oxygen, 4)
                .build();

        Pyrite = new Material.Builder(306, gregtechId("pyrite"))
                .dust(1).ore()
                .color(0x967828).iconSet(ROUGH)
                .flags(BLAST_FURNACE_CALCITE_DOUBLE)
                .components(Iron, 1, Sulfur, 2)
                .build();

        Pyrolusite = new Material.Builder(307, gregtechId("pyrolusite"))
                .dust().ore()
                .color(0x9696AA)
                .components(Manganese, 1, Oxygen, 2)
                .build();

        Pyrope = new Material.Builder(308, gregtechId("pyrope"))
                .gem().ore(3, 1)
                .color(0x783264).iconSet(RUBY)
                .components(Aluminium, 2, Magnesium, 3, Silicon, 3, Oxygen, 12)
                .build();

        RockSalt = new Material.Builder(309, gregtechId("rock_salt"))
                .gem(1).ore(2, 1)
                .color(0xF0C8C8).iconSet(FINE)
                .flags(NO_SMASHING)
                .components(Potassium, 1, Chlorine, 1)
                .build();

        Ruridit = new Material.Builder(310, gregtechId("ruridit"))
                .ingot(3).fluid()
                .colorAverage().iconSet(BRIGHT)
                .flags(GENERATE_FINE_WIRE, GENERATE_GEAR, GENERATE_LONG_ROD, GENERATE_BOLT_SCREW, GENERATE_FRAME)
                .components(Ruthenium, 2, Iridium, 1)
                .blast(b -> b
                        .temp(4500, GasTier.HIGH)
                        .blastStats(VA[EV], 1600)
                        .vacuumStats(VA[HV], 300))
                .build();

        Ruby = new Material.Builder(311, gregtechId("ruby"))
                .gem().ore()
                .color(0xFF6464).iconSet(RUBY)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, GENERATE_LENS)
                .components(Chrome, 1, Aluminium, 2, Oxygen, 3)
                .build();

        Salt = new Material.Builder(312, gregtechId("salt"))
                .gem(1).ore(2, 1)
                .liquid(new FluidBuilder()
                        .translation("gregtech.fluid.molten"))
                .color(0xFAFAFA).iconSet(FINE)
                .flags(NO_SMASHING)
                .components(Sodium, 1, Chlorine, 1)
                .build();

        Saltpeter = new Material.Builder(313, gregtechId("saltpeter"))
                .dust(1).ore(2, 1)
                .color(0xE6E6E6).iconSet(FINE)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE)
                .components(Potassium, 1, Nitrogen, 1, Oxygen, 3)
                .build();

        Sapphire = new Material.Builder(314, gregtechId("sapphire"))
                .gem().ore()
                .color(0x6464C8).iconSet(GEM_VERTICAL)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, GENERATE_LENS)
                .components(Aluminium, 2, Oxygen, 3)
                .build();

        Scheelite = new Material.Builder(315, gregtechId("scheelite"))
                .dust(3).ore()
                .color(0xC88C14)
                .flags(DISABLE_DECOMPOSITION)
                .components(Calcium, 1, Tungsten, 1, Oxygen, 4)
                .build()
                .setFormula("Ca(WO3)O", true);

        Sodalite = new Material.Builder(316, gregtechId("sodalite"))
                .gem(1).ore(6, 4)
                .color(0x1414FF).iconSet(LAPIS)
                .flags(GENERATE_PLATE, GENERATE_ROD, NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE,
                        DECOMPOSITION_BY_ELECTROLYZING)
                .components(Aluminium, 3, Silicon, 3, Sodium, 4, Chlorine, 1)
                .build();

        AluminiumSulfite = new Material.Builder(317, gregtechId("aluminium_sulfite"))
                .dust()
                .color(0xCC4BBB).iconSet(DULL)
                .components(Aluminium, 2, Sulfur, 3, Oxygen, 9)
                .build().setFormula("Al2(SO3)3", true);

        Tantalite = new Material.Builder(318, gregtechId("tantalite"))
                .dust(3).ore()
                .color(0x915028).iconSet(METALLIC)
                .components(Manganese, 1, Tantalum, 2, Oxygen, 6)
                .build();

        Coke = new Material.Builder(319, gregtechId("coke"))
                .gem(2, 3200) // 2x burn time of coal
                .color(0x666666).iconSet(LIGNITE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE)
                .components(Carbon, 1)
                .build();

        SolderingAlloy = new Material.Builder(320, gregtechId("soldering_alloy"))
                .ingot(1)
                .liquid(new FluidBuilder().temperature(544))
                .color(0x9696A0)
                .components(Tin, 6, Lead, 3, Antimony, 1)
                .build();

        Spessartine = new Material.Builder(321, gregtechId("spessartine"))
                .gem().ore(3, 1)
                .color(0xFF6464).iconSet(RUBY)
                .components(Aluminium, 2, Manganese, 3, Silicon, 3, Oxygen, 12)
                .build();

        Sphalerite = new Material.Builder(322, gregtechId("sphalerite"))
                .dust(1).ore()
                .color(0xFFFFFF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Zinc, 1, Sulfur, 1)
                .build();

        StainlessSteel = new Material.Builder(323, gregtechId("stainless_steel"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(2011))
                .color(0xC8C8DC).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_FRAME, GENERATE_LONG_ROD,
                        GENERATE_FOIL, GENERATE_GEAR, GENERATE_DOUBLE_PLATE, GENERATE_ROUND)
                .components(Iron, 6, Chrome, 1, Manganese, 1, Nickel, 1)
                .toolStats(ToolProperty.Builder.of(7.0F, 5.0F, 1024, 3)
                        .enchantability(14).build())
                .rotorStats(7.0f, 4.0f, 480)
                .fluidPipeProperties(2428, 75, true, true, true, false)
                .blast(b -> b.temp(1700, GasTier.LOW).blastStats(VA[HV], 1100))
                .build();

        Steel = new Material.Builder(324, gregtechId("steel"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(2046))
                .color(0x808080).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_SPRING,
                        GENERATE_SPRING_SMALL, GENERATE_FRAME, DISABLE_DECOMPOSITION, GENERATE_FINE_WIRE, GENERATE_GEAR,
                        GENERATE_DOUBLE_PLATE)
                .components(Iron, 1)
                .toolStats(ToolProperty.Builder.of(5.0F, 3.0F, 512, 3)
                        .enchantability(14).build())
                .rotorStats(6.0f, 3.0f, 512)
                .fluidPipeProperties(1855, 50, true)
                .cableProperties(V[EV], 2, 2)
                .blast(b -> b.temp(1000).blastStats(VA[MV], 800)) // no gas tier for steel
                .build();

        Stibnite = new Material.Builder(325, gregtechId("stibnite"))
                .dust().ore()
                .color(0x464646).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Antimony, 2, Sulfur, 3)
                .build();

        // Free ID 326

        Tetrahedrite = new Material.Builder(327, gregtechId("tetrahedrite"))
                .dust().ore()
                .color(0xC82000)
                .components(Copper, 3, Antimony, 1, Sulfur, 3, Iron, 1)
                .build();

        TinAlloy = new Material.Builder(328, gregtechId("tin_alloy"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1258))
                .color(0xC8C8C8).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_DOUBLE_PLATE)
                .components(Tin, 1, Iron, 1)
                .fluidPipeProperties(1572, 20, true)
                .build();

        Topaz = new Material.Builder(329, gregtechId("topaz"))
                .gem(3).ore()
                .color(0xFF8000).iconSet(GEM_HORIZONTAL)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Silicon, 1, Fluorine, 1, Hydrogen, 2)
                .build();

        Tungstate = new Material.Builder(330, gregtechId("tungstate"))
                .dust(3).ore()
                .color(0x373223)
                .flags(DISABLE_DECOMPOSITION)
                .components(Tungsten, 1, Lithium, 2, Oxygen, 4)
                .build()
                .setFormula("Li2(WO3)O", true);

        Ultimet = new Material.Builder(331, gregtechId("ultimet"))
                .ingot(4)
                .liquid(new FluidBuilder().temperature(1980))
                .color(0xB4B4E6).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_GEAR, GENERATE_DOUBLE_PLATE, GENERATE_FRAME)
                .components(Cobalt, 5, Chrome, 2, Nickel, 1, Molybdenum, 1)
                .toolStats(ToolProperty.Builder.of(10.0F, 7.0F, 2048, 4)
                        .attackSpeed(0.1F).enchantability(21).build())
                .rotorStats(9.0f, 4.0f, 2048)
                .itemPipeProperties(128, 16)
                .blast(b -> b.temp(2700, GasTier.MID).blastStats(VA[HV], 1300))
                .build();

        Uraninite = new Material.Builder(332, gregtechId("uraninite"))
                .dust(3).ore(true)
                .color(0x232323).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium, 1, Oxygen, 2)
                .fissionFuel(1800, 300, 55., 1., 1000., 0., 2.4)
                .build()
                .setFormula("UO2", true);

        Uvarovite = new Material.Builder(333, gregtechId("uvarovite"))
                .gem()
                .color(0xB4ffB4).iconSet(RUBY)
                .components(Calcium, 3, Chrome, 2, Silicon, 3, Oxygen, 12)
                .build();

        VanadiumGallium = new Material.Builder(334, gregtechId("vanadium_gallium"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1712))
                .color(0x80808C).iconSet(SHINY)
                .flags(STD_METAL, GENERATE_FOIL, GENERATE_SPRING, GENERATE_SPRING_SMALL)
                .components(Vanadium, 3, Gallium, 1)
                .cableProperties(V[ZPM], 4, 2)
                .blast(b -> b
                        .temp(4500, GasTier.HIGH)
                        .blastStats(VA[EV], 1200)
                        .vacuumStats(VA[HV]))
                .build();

        WroughtIron = new Material.Builder(335, gregtechId("wrought_iron"))
                .ingot()
                .liquid(new FluidBuilder().temperature(2011))
                .color(0xC8B4B4).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_GEAR, GENERATE_FOIL, MORTAR_GRINDABLE, GENERATE_RING, GENERATE_LONG_ROD,
                        GENERATE_BOLT_SCREW, DISABLE_DECOMPOSITION, BLAST_FURNACE_CALCITE_TRIPLE, GENERATE_DOUBLE_PLATE)
                .components(Iron, 1)
                .toolStats(ToolProperty.Builder.of(2.0F, 2.0F, 384, 2)
                        .attackSpeed(-0.2F).enchantability(5).build())
                .rotorStats(6.0f, 3.5f, 384)
                .build();
        Iron.getProperty(PropertyKey.INGOT).setSmeltingInto(WroughtIron);
        Iron.getProperty(PropertyKey.INGOT).setArcSmeltingInto(WroughtIron);

        Wulfenite = new Material.Builder(336, gregtechId("wulfenite"))
                .dust(3).ore()
                .color(0xFF8000)
                .components(Lead, 1, Molybdenum, 1, Oxygen, 4)
                .build();

        YellowLimonite = new Material.Builder(337, gregtechId("yellow_limonite"))
                .dust().ore()
                .color(0xC8C800).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, BLAST_FURNACE_CALCITE_DOUBLE)
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .build();

        YttriumBariumCuprate = new Material.Builder(338, gregtechId("yttrium_barium_cuprate"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1799))
                .color(0x504046).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FINE_WIRE, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FOIL,
                        GENERATE_BOLT_SCREW)
                .components(Yttrium, 1, Barium, 2, Copper, 3, Oxygen, 7)
                .cableProperties(V[UV], 4, 4)
                .blast(b -> b
                        .temp(4500, GasTier.HIGH)
                        .blastStats(VA[IV], 1000)
                        .vacuumStats(VA[EV], 150))
                .build();

        NetherQuartz = new Material.Builder(339, gregtechId("nether_quartz"))
                .gem(1).ore(2, 1)
                .color(0xE6D2D2).iconSet(QUARTZ)
                .flags(GENERATE_PLATE, NO_SMELTING, CRYSTALLIZABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        DISABLE_DECOMPOSITION)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        CertusQuartz = new Material.Builder(214, gregtechId("certus_quartz"))
                .gem(1).ore(2, 1)
                .color(0xD2D2E6).iconSet(CERTUS)
                .flags(GENERATE_PLATE, NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        Quartzite = new Material.Builder(340, gregtechId("quartzite"))
                .gem(1).ore(2, 1)
                .color(0xD2E6D2).iconSet(QUARTZ)
                .flags(NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION, GENERATE_PLATE)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        Graphite = new Material.Builder(341, gregtechId("graphite"))
                .ore()
                .color(0x808080)
                .flags(NO_SMELTING, FLAMMABLE, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Graphene = new Material.Builder(342, gregtechId("graphene"))
                .dust()
                .color(0x808080).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .cableProperties(V[IV], 1, 1)
                .build();

        TungsticAcid = new Material.Builder(343, gregtechId("tungstic_acid"))
                .dust()
                .color(0xBCC800).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Tungsten, 1, Oxygen, 4)
                .build();

        Osmiridium = new Material.Builder(344, gregtechId("osmiridium"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(3012))
                .color(0x6464FF).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_ROTOR, GENERATE_ROUND,
                        GENERATE_FINE_WIRE, GENERATE_GEAR, GENERATE_DOUBLE_PLATE)
                .components(Iridium, 3, Osmium, 1)
                .rotorStats(9.0f, 3.0f, 3152)
                .itemPipeProperties(64, 32)
                .blast(b -> b
                        .temp(4500, GasTier.HIGH)
                        .blastStats(VA[LuV], 900)
                        .vacuumStats(VA[EV], 200))
                .build();

        LithiumChloride = new Material.Builder(345, gregtechId("lithium_chloride"))
                .dust()
                .color(0xDEDEFA).iconSet(FINE)
                .components(Lithium, 1, Chlorine, 1)
                .build();

        CalciumChloride = new Material.Builder(346, gregtechId("calcium_chloride"))
                .dust()
                .color(0xEBEBFA).iconSet(FINE)
                .components(Calcium, 1, Chlorine, 2)
                .build();

        Bornite = new Material.Builder(347, gregtechId("bornite"))
                .dust(1).ore()
                .color(0x97662B).iconSet(METALLIC)
                .components(Copper, 5, Iron, 1, Sulfur, 4)
                .build();

        Chalcocite = new Material.Builder(348, gregtechId("chalcocite"))
                .dust().ore()
                .color(0x353535).iconSet(GEM_VERTICAL)
                .components(Copper, 2, Sulfur, 1)
                .build();

        // Free ID 349

        // Free ID 350

        GalliumArsenide = new Material.Builder(351, gregtechId("gallium_arsenide"))
                .ingot(1)
                .liquid(new FluidBuilder().temperature(1511))
                .color(0xA0A0A0)
                .flags(STD_METAL, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Arsenic, 1, Gallium, 1)
                .blast(b -> b.temp(1200, GasTier.LOW).blastStats(VA[MV], 1200))
                .build();

        Potash = new Material.Builder(352, gregtechId("potash"))
                .dust(1)
                .color(0x784137)
                .components(Potassium, 2, Oxygen, 1)
                .build();

        SodaAsh = new Material.Builder(353, gregtechId("soda_ash"))
                .dust(1)
                .color(0xDCDCFF)
                .components(Sodium, 2, Carbon, 1, Oxygen, 3)
                .build();

        IndiumGalliumPhosphide = new Material.Builder(354, gregtechId("indium_gallium_phosphide"))
                .ingot(1)
                .liquid(new FluidBuilder().temperature(350))
                .color(0xA08CBE)
                .flags(STD_METAL, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Indium, 1, Gallium, 1, Phosphorus, 1)
                .build();

        NickelZincFerrite = new Material.Builder(355, gregtechId("nickel_zinc_ferrite"))
                .ingot(0)
                .liquid(new FluidBuilder().temperature(1410))
                .color(0x3C3C3C).iconSet(METALLIC)
                .flags(GENERATE_RING)
                .components(Nickel, 1, Zinc, 1, Iron, 4, Oxygen, 8)
                .build();

        SiliconDioxide = new Material.Builder(356, gregtechId("silicon_dioxide"))
                .dust(1)
                .color(0xC8C8C8).iconSet(QUARTZ)
                .flags(NO_SMASHING, NO_SMELTING)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        MagnesiumChloride = new Material.Builder(357, gregtechId("magnesium_chloride"))
                .dust(1)
                .color(0xD40D5C)
                .components(Magnesium, 1, Chlorine, 2)
                .build();

        SodiumSulfide = new Material.Builder(358, gregtechId("sodium_sulfide"))
                .dust(1)
                .color(0xFFE680)
                .components(Sodium, 2, Sulfur, 1)
                .build();

        PhosphorusPentoxide = new Material.Builder(359, gregtechId("phosphorus_pentoxide"))
                .dust(1)
                .color(0xDCDC00)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Phosphorus, 4, Oxygen, 10)
                .build();

        Quicklime = new Material.Builder(360, gregtechId("quicklime"))
                .dust(1)
                .color(0xF0F0F0)
                .components(Calcium, 1, Oxygen, 1)
                .build();

        SodiumBisulfate = new Material.Builder(361, gregtechId("sodium_bisulfate"))
                .dust(1)
                .color(0x004455)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 1, Hydrogen, 1, Sulfur, 1, Oxygen, 4)
                .build();

        FerriteMixture = new Material.Builder(362, gregtechId("ferrite_mixture"))
                .dust(1)
                .color(0xB4B4B4).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Nickel, 1, Zinc, 1, Iron, 4)
                .build();

        Magnesia = new Material.Builder(363, gregtechId("magnesia"))
                .dust(1)
                .color(0x887878)
                .components(Magnesium, 1, Oxygen, 1)
                .build();

        PlatinumGroupSludge = new Material.Builder(364, gregtechId("platinum_group_sludge"))
                .dust(1)
                .color(0x001E00).iconSet(FINE)
                .flags(DISABLE_DECOMPOSITION)
                .build();

        Realgar = new Material.Builder(365, gregtechId("realgar"))
                .gem().ore()
                .color(0x9D2123).iconSet(EMERALD)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Arsenic, 4, Sulfur, 4)
                .build();

        SodiumBicarbonate = new Material.Builder(366, gregtechId("sodium_bicarbonate"))
                .dust(1)
                .color(0x565b96).iconSet(ROUGH)
                .components(Sodium, 1, Hydrogen, 1, Carbon, 1, Oxygen, 3)
                .build();

        PotassiumDichromate = new Material.Builder(367, gregtechId("potassium_dichromate"))
                .dust(1)
                .color(0xFF084E)
                .components(Potassium, 2, Chrome, 2, Oxygen, 7)
                .build();

        ChromiumTrioxide = new Material.Builder(368, gregtechId("chromium_trioxide"))
                .dust(1)
                .color(0xFFE4E1)
                .components(Chrome, 1, Oxygen, 3)
                .build();

        AntimonyTrioxide = new Material.Builder(369, gregtechId("antimony_trioxide"))
                .dust(1)
                .color(0xE6E6F0)
                .components(Antimony, 2, Oxygen, 3)
                .build();

        Zincite = new Material.Builder(370, gregtechId("zincite"))
                .dust(1)
                .color(0xFFFFF5)
                .components(Zinc, 1, Oxygen, 1)
                .build();

        CupricOxide = new Material.Builder(371, gregtechId("cupric_oxide"))
                .dust(1)
                .color(0x0F0F0F)
                .components(Copper, 1, Oxygen, 1)
                .build();

        CobaltOxide = new Material.Builder(372, gregtechId("cobalt_oxide"))
                .dust(1)
                .color(0x788000)
                .components(Cobalt, 1, Oxygen, 1)
                .build();

        ArsenicTrioxide = new Material.Builder(373, gregtechId("arsenic_trioxide"))
                .dust(1)
                .iconSet(ROUGH)
                .components(Arsenic, 2, Oxygen, 3)
                .build();

        Massicot = new Material.Builder(374, gregtechId("massicot"))
                .dust(1)
                .color(0xFFDD55)
                .components(Lead, 1, Oxygen, 1)
                .build();

        Ferrosilite = new Material.Builder(375, gregtechId("ferrosilite"))
                .dust(1)
                .color(0x97632A)
                .components(Iron, 1, Silicon, 1, Oxygen, 3)
                .build();

        MetalMixture = new Material.Builder(376, gregtechId("metal_mixture"))
                .dust(1)
                .color(0x502d16).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .build();

        SodiumHydroxide = new Material.Builder(377, gregtechId("sodium_hydroxide"))
                .dust(1)
                .color(0x003380)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 1, Oxygen, 1, Hydrogen, 1)
                .build();

        SodiumPersulfate = new Material.Builder(378, gregtechId("sodium_persulfate"))
                .liquid(new FluidBuilder().customStill())
                .color(0x045C5C)
                .components(Sodium, 2, Sulfur, 2, Oxygen, 8)
                .build();

        Bastnasite = new Material.Builder(379, gregtechId("bastnasite"))
                .dust().ore(2, 1)
                .color(0xC86E2D).iconSet(FINE)
                .components(Cerium, 1, Carbon, 1, Fluorine, 1, Oxygen, 3)
                .build();

        Pentlandite = new Material.Builder(380, gregtechId("pentlandite"))
                .dust().ore()
                .color(0xA59605)
                .components(Nickel, 9, Sulfur, 8)
                .build();

        Spodumene = new Material.Builder(381, gregtechId("spodumene"))
                .dust().ore()
                .color(0xBEAAAA)
                .components(Lithium, 1, Aluminium, 1, Silicon, 2, Oxygen, 6)
                .build();

        Lepidolite = new Material.Builder(382, gregtechId("lepidolite"))
                .dust().ore(2, 1)
                .color(0xF0328C).iconSet(FINE)
                .components(Potassium, 1, Lithium, 3, Aluminium, 4, Fluorine, 2, Oxygen, 10)
                .build();

        // Free ID 383

        GlauconiteSand = new Material.Builder(384, gregtechId("glauconite_sand"))
                .dust().ore(3, 1)
                .color(0x82B43C).iconSet(SAND)
                .components(Potassium, 1, Magnesium, 2, Aluminium, 4, Hydrogen, 2, Oxygen, 12)
                .build();

        Malachite = new Material.Builder(385, gregtechId("malachite"))
                .gem().ore()
                .color(0x055F05).iconSet(LAPIS)
                .components(Copper, 2, Carbon, 1, Hydrogen, 2, Oxygen, 5)
                .build();

        Mica = new Material.Builder(386, gregtechId("mica"))
                .dust().ore(2, 1)
                .color(0xC3C3CD).iconSet(FINE)
                .components(Potassium, 1, Aluminium, 3, Silicon, 3, Fluorine, 2, Oxygen, 10)
                .build();

        Barite = new Material.Builder(387, gregtechId("barite"))
                .dust().ore()
                .color(0xE6EBEB)
                .components(Barium, 1, Sulfur, 1, Oxygen, 4)
                .build();

        Alunite = new Material.Builder(388, gregtechId("alunite"))
                .dust().ore(3, 1)
                .color(0xE1B441).iconSet(METALLIC)
                .components(Potassium, 1, Aluminium, 3, Silicon, 2, Hydrogen, 6, Oxygen, 14)
                .build();

        // Free ID 389

        // Free ID 390

        // Free ID 391

        Talc = new Material.Builder(392, gregtechId("talc"))
                .dust().ore(2, 1)
                .color(0x5AB45A).iconSet(FINE)
                .components(Magnesium, 3, Silicon, 4, Hydrogen, 2, Oxygen, 12)
                .build();

        Soapstone = new Material.Builder(393, gregtechId("soapstone"))
                .dust(1).ore(3, 1)
                .color(0x5F915F)
                .components(Magnesium, 3, Silicon, 4, Hydrogen, 2, Oxygen, 12)
                .build();

        Kyanite = new Material.Builder(394, gregtechId("kyanite"))
                .dust().ore()
                .color(0x6E6EFA).iconSet(FLINT)
                .components(Aluminium, 2, Silicon, 1, Oxygen, 5)
                .build();

        IronMagnetic = new Material.Builder(395, gregtechId("iron_magnetic"))
                .ingot()
                .color(0xC8C8C8).iconSet(MAGNETIC)
                .flags(GENERATE_BOLT_SCREW, IS_MAGNETIC)
                .components(Iron, 1)
                .ingotSmeltInto(Iron)
                .arcSmeltInto(WroughtIron)
                .macerateInto(Iron)
                .build();
        Iron.getProperty(PropertyKey.INGOT).setMagneticMaterial(IronMagnetic);

        TungstenCarbide = new Material.Builder(396, gregtechId("tungsten_carbide"))
                .ingot(4).fluid()
                .color(0x330066).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_FOIL, GENERATE_GEAR, DECOMPOSITION_BY_CENTRIFUGING, GENERATE_DOUBLE_PLATE)
                .components(Tungsten, 1, Carbon, 1)
                .toolStats(ToolProperty.Builder.of(60.0F, 2.0F, 1024, 4)
                        .enchantability(21).build())
                .rotorStats(12.0f, 4.0f, 1280)
                .fluidPipeProperties(3837, 200, true)
                .blast(b -> b
                        .temp(3058, GasTier.MID)
                        .blastStats(VA[EV], 1200)
                        .vacuumStats(VA[HV]))
                .build();

        CarbonDioxide = new Material.Builder(397, gregtechId("carbon_dioxide"))
                .gas()
                .color(0xA9D0F5)
                .components(Carbon, 1, Oxygen, 2)
                .build();

        TitaniumTetrachloride = new Material.Builder(398, gregtechId("titanium_tetrachloride"))
                .liquid(new FluidBuilder().customStill())
                .color(0xD40D5C)
                .flags(DISABLE_DECOMPOSITION)
                .components(Titanium, 1, Chlorine, 4)
                .build();

        NitrogenDioxide = new Material.Builder(399, gregtechId("nitrogen_dioxide"))
                .gas()
                .color(0xF05800)
                .components(Nitrogen, 1, Oxygen, 2)
                .build();

        HydrogenSulfide = new Material.Builder(400, gregtechId("hydrogen_sulfide"))
                .gas(new FluidBuilder().customStill())
                .color(0xFC5304)
                .components(Hydrogen, 2, Sulfur, 1)
                .build();

        NitricAcid = new Material.Builder(401, gregtechId("nitric_acid"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xCCCC00)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 1, Nitrogen, 1, Oxygen, 3)
                .build();

        SulfuricAcid = new Material.Builder(402, gregtechId("sulfuric_acid"))
                .liquid(new FluidBuilder()
                        .attribute(FluidAttributes.ACID)
                        .customStill())
                .color(0xFC5304)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Sulfur, 1, Oxygen, 4)
                .build();

        PhosphoricAcid = new Material.Builder(403, gregtechId("phosphoric_acid"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xDCDC01)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 3, Phosphorus, 1, Oxygen, 4)
                .build();

        SulfurTrioxide = new Material.Builder(404, gregtechId("sulfur_trioxide"))
                .gas()
                .color(0xA0A014)
                .components(Sulfur, 1, Oxygen, 3)
                .build();

        SulfurDioxide = new Material.Builder(405, gregtechId("sulfur_dioxide"))
                .gas()
                .color(0xC8C819)
                .components(Sulfur, 1, Oxygen, 2)
                .build();

        CarbonMonoxide = new Material.Builder(406, gregtechId("carbon_monoxide"))
                .gas()
                .color(0x0E4880)
                .components(Carbon, 1, Oxygen, 1)
                .build();

        HypochlorousAcid = new Material.Builder(407, gregtechId("hypochlorous_acid"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x6F8A91)
                .components(Hydrogen, 1, Chlorine, 1, Oxygen, 1)
                .build();

        Ammonia = new Material.Builder(408, gregtechId("ammonia"))
                .gas()
                .color(0x3F3480)
                .components(Nitrogen, 1, Hydrogen, 3)
                .build();

        HydrofluoricAcid = new Material.Builder(409, gregtechId("hydrofluoric_acid"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x0088AA)
                .components(Hydrogen, 1, Fluorine, 1)
                .build();

        NitricOxide = new Material.Builder(410, gregtechId("nitric_oxide"))
                .gas()
                .color(0x7DC8F0)
                .components(Nitrogen, 1, Oxygen, 1)
                .build();

        Iron3Chloride = new Material.Builder(411, gregtechId("iron_iii_chloride"))
                .liquid()
                .color(0x060B0B)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Iron, 1, Chlorine, 3)
                .build();

        UraniumHexafluoride = new Material.Builder(412, gregtechId("uranium_hexafluoride"))
                .gas()
                .color(0x42D126)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium, 1, Fluorine, 6)
                .build()
                .setFormula("UF6", true);

        LowEnrichedUraniumHexafluoride = new Material.Builder(413, gregtechId("enriched_uranium_hexafluoride"))
                .gas()
                .color(0x4BF52A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium235, 1, Fluorine, 6)
                .build()
                .setFormula("UF6", true);

        DepletedUraniumHexafluoride = new Material.Builder(414, gregtechId("depleted_uranium_hexafluoride"))
                .gas()
                .color(0x74BA66)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium, 1, Fluorine, 6)
                .build()
                .setFormula("UF6", true);

        NitrousOxide = new Material.Builder(415, gregtechId("nitrous_oxide"))
                .gas()
                .color(0x7DC8FF)
                .components(Nitrogen, 2, Oxygen, 1)
                .build();

        EnderPearl = new Material.Builder(416, gregtechId("ender_pearl"))
                .gem(1)
                .color(0x6CDCC8)
                .flags(NO_SMASHING, NO_SMELTING, GENERATE_PLATE)
                .components(Beryllium, 1, Potassium, 4, Nitrogen, 5)
                .build();

        PotassiumFeldspar = new Material.Builder(417, gregtechId("potassium_feldspar"))
                .dust(1)
                .color(0x782828).iconSet(FINE)
                .components(Potassium, 1, Aluminium, 1, Silicon, 1, Oxygen, 8)
                .build();

        NeodymiumMagnetic = new Material.Builder(418, gregtechId("neodymium_magnetic"))
                .ingot()
                .color(0x646464).iconSet(MAGNETIC)
                .flags(GENERATE_ROD, IS_MAGNETIC)
                .components(Neodymium, 1)
                .ingotSmeltInto(Neodymium)
                .arcSmeltInto(Neodymium)
                .macerateInto(Neodymium)
                .build();
        Neodymium.getProperty(PropertyKey.INGOT).setMagneticMaterial(NeodymiumMagnetic);

        HydrochloricAcid = new Material.Builder(419, gregtechId("hydrochloric_acid"))
                .liquid(new FluidBuilder()
                        .attribute(FluidAttributes.ACID)
                        .customStill())
                .color(0xBCBCB5)
                .components(Hydrogen, 1, Chlorine, 1)
                .build();

        Steam = new Material.Builder(420, gregtechId("steam"))
                .gas(new FluidBuilder()
                        .temperature(373)
                        .customStill())
                .color(0xC4C4C4)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .build();

        DistilledWater = new Material.Builder(421, gregtechId("distilled_water"))
                .liquid(new FluidBuilder().alternativeName("fluidDistWater"))
                .color(0x4A94FF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .build();

        SodiumPotassium = new Material.Builder(422, gregtechId("sodium_potassium"))
                .fluid()
                .color(0x64FCB4)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Sodium, 1, Potassium, 1)
                .build();

        SamariumMagnetic = new Material.Builder(423, gregtechId("samarium_magnetic"))
                .ingot()
                .color(0xFFFFCD).iconSet(MAGNETIC)
                .flags(GENERATE_LONG_ROD, IS_MAGNETIC)
                .components(Samarium, 1)
                .ingotSmeltInto(Samarium)
                .arcSmeltInto(Samarium)
                .macerateInto(Samarium)
                .build();
        Samarium.getProperty(PropertyKey.INGOT).setMagneticMaterial(SamariumMagnetic);

        ManganesePhosphide = new Material.Builder(424, gregtechId("manganese_phosphide"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1368))
                .color(0xE1B454).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Manganese, 1, Phosphorus, 1)
                .cableProperties(GTValues.V[GTValues.LV], 2, 0, true, 78)
                .blast(1200, GasTier.LOW)
                .build();

        MagnesiumDiboride = new Material.Builder(425, gregtechId("magnesium_diboride"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1103))
                .color(0x331900).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Magnesium, 1, Boron, 2)
                .cableProperties(GTValues.V[GTValues.MV], 4, 0, true, 78)
                .blast(b -> b
                        .temp(2500, GasTier.LOW)
                        .blastStats(VA[HV], 1000)
                        .vacuumStats(VA[MV], 200))
                .build();

        MercuryBariumCalciumCuprate = new Material.Builder(426, gregtechId("mercury_barium_calcium_cuprate"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1075))
                .color(0x555555).iconSet(SHINY)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Mercury, 1, Barium, 2, Calcium, 2, Copper, 3, Oxygen, 8)
                .cableProperties(GTValues.V[GTValues.HV], 4, 0, true, 78)
                .blast(b -> b
                        .temp(3300, GasTier.LOW)
                        .blastStats(VA[HV], 1500)
                        .vacuumStats(VA[HV]))
                .build();

        UraniumTriplatinum = new Material.Builder(427, gregtechId("uranium_triplatinum"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1882))
                .color(0x008700).iconSet(SHINY)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Uranium, 1, Platinum, 3)
                .cableProperties(GTValues.V[GTValues.EV], 6, 0, true, 30)
                .blast(b -> b
                        .temp(4400, GasTier.MID)
                        .blastStats(VA[EV], 1000)
                        .vacuumStats(VA[EV], 200))
                .build()
                .setFormula("UPt3", true);

        SamariumIronArsenicOxide = new Material.Builder(428, gregtechId("samarium_iron_arsenic_oxide"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1347))
                .color(0x330033).iconSet(SHINY)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Samarium, 1, Iron, 1, Arsenic, 1, Oxygen, 1)
                .cableProperties(GTValues.V[GTValues.IV], 6, 0, true, 30)
                .blast(b -> b
                        .temp(5200, GasTier.MID)
                        .blastStats(VA[EV], 1500)
                        .vacuumStats(VA[IV], 200))
                .build();

        IndiumTinBariumTitaniumCuprate = new Material.Builder(429, gregtechId("indium_tin_barium_titanium_cuprate"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1012))
                .color(0x994C00).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_ELECTROLYZING, GENERATE_FINE_WIRE)
                .components(Indium, 4, Tin, 2, Barium, 2, Titanium, 1, Copper, 7, Oxygen, 14)
                .cableProperties(GTValues.V[GTValues.LuV], 8, 0, true, 5)
                .blast(b -> b
                        .temp(6000, GasTier.HIGH)
                        .blastStats(VA[IV], 1000)
                        .vacuumStats(VA[LuV]))
                .build();

        UraniumRhodiumDinaquadide = new Material.Builder(430, gregtechId("uranium_rhodium_dinaquadide"))
                .ingot()
                .liquid(new FluidBuilder().temperature(3410))
                .color(0x0A0A0A)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, GENERATE_FINE_WIRE)
                .components(Uranium, 1, Rhodium, 1, Naquadah, 2)
                .cableProperties(GTValues.V[GTValues.ZPM], 8, 0, true, 5)
                .blast(b -> b
                        .temp(9000, GasTier.HIGH)
                        .blastStats(VA[IV], 1500)
                        .vacuumStats(VA[ZPM], 200))
                .build()
                .setFormula("URhNq2", true);

        EnrichedNaquadahTriniumEuropiumDuranide = new Material.Builder(431,
                gregtechId("enriched_naquadah_trinium_europium_duranide"))
                        .ingot()
                        .liquid(new FluidBuilder().temperature(5930))
                        .color(0x7D9673).iconSet(METALLIC)
                        .flags(DECOMPOSITION_BY_CENTRIFUGING, GENERATE_FINE_WIRE)
                        .components(NaquadahEnriched, 4, Trinium, 3, Europium, 2, Duranium, 1)
                        .cableProperties(GTValues.V[GTValues.UV], 16, 0, true, 3)
                        .blast(b -> b
                                .temp(9900, GasTier.HIGH)
                                .blastStats(VA[LuV], 1200)
                                .vacuumStats(VA[UV], 200))
                        .build();

        RutheniumTriniumAmericiumNeutronate = new Material.Builder(432,
                gregtechId("ruthenium_trinium_americium_neutronate"))
                        .ingot()
                        .liquid(new FluidBuilder().temperature(23691))
                        .color(0xFFFFFF).iconSet(BRIGHT)
                        .flags(DECOMPOSITION_BY_ELECTROLYZING)
                        .components(Ruthenium, 1, Trinium, 2, Americium, 1, Neutronium, 2, Oxygen, 8)
                        .cableProperties(GTValues.V[GTValues.UHV], 24, 0, true, 3)
                        .blast(b -> b
                                .temp(10800, GasTier.HIGHER)
                                .blastStats(VA[ZPM], 1000)
                                .vacuumStats(VA[UHV], 200))
                        .build();

        InertMetalMixture = new Material.Builder(433, gregtechId("inert_metal_mixture"))
                .dust()
                .color(0xE2AE72).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Rhodium, 1, Ruthenium, 1, Oxygen, 4)
                .build();

        RhodiumSulfate = new Material.Builder(434, gregtechId("rhodium_sulfate"))
                .liquid(new FluidBuilder().temperature(1128))
                .color(0xEEAA55)
                .flags(DISABLE_DECOMPOSITION)
                .components(Rhodium, 2, Sulfur, 3, Oxygen, 12)
                .build().setFormula("Rh2(SO4)3", true);

        RutheniumTetroxide = new Material.Builder(435, gregtechId("ruthenium_tetroxide"))
                .dust()
                .color(0xC7C7C7)
                .flags(DISABLE_DECOMPOSITION)
                .components(Ruthenium, 1, Oxygen, 4)
                .build();

        OsmiumTetroxide = new Material.Builder(436, gregtechId("osmium_tetroxide"))
                .dust()
                .color(0xACAD71).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Osmium, 1, Oxygen, 4)
                .build();

        IridiumChloride = new Material.Builder(437, gregtechId("iridium_chloride"))
                .dust()
                .color(0x013220).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iridium, 1, Chlorine, 3)
                .build();

        FluoroantimonicAcid = new Material.Builder(438, gregtechId("fluoroantimonic_acid"))
                .liquid(new FluidBuilder()
                        .attribute(FluidAttributes.ACID)
                        .customStill())
                .color(0x8CA4C4)
                .components(Hydrogen, 2, Antimony, 1, Fluorine, 7)
                .build();

        TitaniumTrifluoride = new Material.Builder(439, gregtechId("titanium_trifluoride"))
                .dust()
                .color(0x8F00FF).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Titanium, 1, Fluorine, 3)
                .build();

        CalciumPhosphide = new Material.Builder(440, gregtechId("calcium_phosphide"))
                .dust()
                .color(0xA52A2A).iconSet(METALLIC)
                .components(Calcium, 1, Phosphorus, 1)
                .build();

        IndiumPhosphide = new Material.Builder(441, gregtechId("indium_phosphide"))
                .dust()
                .color(0x582E5C).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Indium, 1, Phosphorus, 1)
                .build();

        BariumSulfide = new Material.Builder(442, gregtechId("barium_sulfide"))
                .dust()
                .color(0xF0EAD6).iconSet(METALLIC)
                .components(Barium, 1, Sulfur, 1)
                .build();

        TriniumSulfide = new Material.Builder(443, gregtechId("trinium_sulfide"))
                .dust()
                .color(0xE68066).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Trinium, 1, Sulfur, 1)
                .build();

        ZincSulfide = new Material.Builder(444, gregtechId("zinc_sulfide"))
                .dust()
                .color(0xFFFFF6).iconSet(DULL)
                .components(Zinc, 1, Sulfur, 1)
                .build();

        GalliumSulfide = new Material.Builder(445, gregtechId("gallium_sulfide"))
                .dust()
                .color(0xFFF59E).iconSet(SHINY)
                .components(Gallium, 1, Sulfur, 1)
                .build();

        AntimonyTrifluoride = new Material.Builder(446, gregtechId("antimony_trifluoride"))
                .dust()
                .color(0xF7EABC).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Antimony, 1, Fluorine, 3)
                .build();

        EnrichedNaquadahSulfate = new Material.Builder(447, gregtechId("enriched_naquadah_sulfate"))
                .dust()
                .color(0x2E2E1C).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(NaquadahEnriched, 1, Sulfur, 1, Oxygen, 4)
                .build();

        NaquadriaSulfate = new Material.Builder(448, gregtechId("naquadria_sulfate"))
                .dust()
                .color(0x006633).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Naquadria, 1, Sulfur, 1, Oxygen, 4)
                .build();

        Pyrochlore = new Material.Builder(449, gregtechId("pyrochlore"))
                .dust().ore()
                .color(0x2B1100).iconSet(METALLIC)
                .flags()
                .components(Calcium, 2, Niobium, 2, Oxygen, 7)
                .build();

        // FREE ID 450

        RTMAlloy = new Material.Builder(451, gregtechId("rtm_alloy"))
                .ingot().fluid()
                .color(0x30306B).iconSet(SHINY)
                .components(Ruthenium, 4, Tungsten, 2, Molybdenum, 1)
                .flags(GENERATE_SPRING)
                .cableProperties(V[EV], 6, 2)
                .blast(b -> b
                        .temp(3000, GasTier.MID)
                        .blastStats(VA[EV], 1400)
                        .vacuumStats(VA[HV], 250))
                .build();

        HighEnrichedUraniumDioxide = new Material.Builder(452, gregtechId("high_enriched_uranium_dioxide"))
                .dust(3)
                .color(0x53E353).iconSet(DULL)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium235, 1, Oxygen, 2)
                .build()
                .setFormula("UO2", true);

        DepletedUraniumDioxide = new Material.Builder(453, gregtechId("depleted_uranium_dioxide"))
                .dust(3)
                .color(0x335323).iconSet(DULL)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium, 1, Oxygen, 2)
                .build()
                .setFormula("UO2", true);

        HighPressureSteam = new Material.Builder(454, gregtechId("high_pressure_steam"))
                .gas(new FluidBuilder()
                        .temperature(500)
                        .customStill())
                .color(0xC4C4C4)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .build();

        FissilePlutoniumDioxide = new Material.Builder(455, gregtechId("fissile_plutonium_dioxide"))
                .dust(3)
                .color(0xF03232).iconSet(DULL)
                .flags(DISABLE_DECOMPOSITION)
                .components(Plutonium, 1, Oxygen, 2)
                .build();

        Zircaloy = new Material.Builder(456, gregtechId("zircaloy"))
                .ingot()
                .color(0x566570).iconSet(METALLIC)
                .flags(GENERATE_RING, GENERATE_PLATE)
                .components(Zirconium, 16, Tin, 2, Chrome, 1)
                .blast(1700, GasTier.LOW)
                .build();

        LowEnrichedUraniumDioxide = new Material.Builder(457, gregtechId("low_enriched_uranium_dioxide"))
                .dust()
                .color(0x43A333)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium235, 1, Oxygen, 2)
                .build()
                .setFormula("UO2", true);

        Zircon = new Material.Builder(458, gregtechId("zircon"))
                .gem().ore()
                .color(0x6E0909)
                .flags(DISABLE_DECOMPOSITION)
                .components(Zirconium, 1, Silicon, 1, Oxygen, 4)
                .iconSet(SHINY)
                .build();

        ZirconiumDioxide = new Material.Builder(459, gregtechId("zirconium_dioxide"))
                .dust()
                .color(0x689F9F)
                .flags(DISABLE_DECOMPOSITION)
                .components(Zirconium, 1, Oxygen, 2)
                .build();

        ZirconiumTetrachloride = new Material.Builder(460, gregtechId("zirconium_tetrachloride"))
                .dust()
                .color(0x689FBF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Zirconium, 1, Chlorine, 4)
                .iconSet(SHINY)
                .build();

        HafniumDioxide = new Material.Builder(461, gregtechId("hafnium_dioxide"))
                .dust()
                .color(0x39393A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hafnium, 1, Oxygen, 2)
                .build();

        HafniumTetrachloride = new Material.Builder(462, gregtechId("hafnium_tetrachloride"))
                .dust()
                .color(0x69699A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hafnium, 1, Chlorine, 4)
                .iconSet(SHINY)
                .build();

        Inconel = new Material.Builder(463, gregtechId("inconel"))
                .ingot().fluid()
                .color(0x7F8F75).iconSet(SHINY)
                .flags(GENERATE_DOUBLE_PLATE, GENERATE_SPRING, DISABLE_DECOMPOSITION)
                .components(Nickel, 5, Chrome, 2, Iron, 2, Niobium, 1, Molybdenum, 1)
                .blastTemp(1610, GasTier.MID, GTValues.VA[EV], 200)
                .fluidPipeProperties(2010, 175, true, true, true, false)
                .build();

        HighEnrichedUraniumHexafluoride = new Material.Builder(464, gregtechId("high_enriched_uranium_hexafluoride"))
                .gas()
                .color(0x5BF93A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium235, 1, Fluorine, 6)
                .build();

        BoronTrioxide = new Material.Builder(465, gregtechId("boron_trioxide"))
                .dust()
                .color(0xC1E9E1)
                .components(Boron, 2, Oxygen, 3)
                .iconSet(METALLIC)
                .build();

        BoronCarbide = new Material.Builder(466, gregtechId("boron_carbide"))
                .ingot()
                .flags(GENERATE_ROD, DISABLE_DECOMPOSITION)
                .blast(2620)
                .color(0xC1E9C1)
                .components(Boron, 4, Carbon, 1)
                .iconSet(METALLIC)
                .build();

        HeavyWater = new Material.Builder(467, gregtechId("heavy_water"))
                .fluid()
                .color(0x3673D6)
                .components(Deuterium, 2, Oxygen, 1)
                .build();

        HighPressureHeavyWater = new Material.Builder(468, gregtechId("high_pressure_heavy_water"))
                .gas(new FluidBuilder().temperature(500))
                .color(0xCCD9F0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Deuterium, 2, Oxygen, 1)
                .build();
    }
}
