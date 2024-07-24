package gregtech.api.unification.material.materials;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.BlastProperty.GasTier;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;

import net.minecraft.init.Enchantments;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;
import static gregtech.api.util.GTUtility.gregtechId;

public class SecondDegreeMaterials {

    public static void register() {
        Glass = new Material.Builder(2000, gregtechId("glass"))
                .gem(0)
                .liquid(new FluidBuilder().temperature(1200).customStill())
                .color(0xFAFAFA).iconSet(GLASS)
                .flags(GENERATE_LENS, NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_RECIPES, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .build();

        Perlite = new Material.Builder(2001, gregtechId("perlite"))
                .dust(1)
                .color(0x1E141E)
                .components(Obsidian, 2, Water, 1)
                .build();

        Borax = new Material.Builder(2002, gregtechId("borax"))
                .dust(1)
                .color(0xFAFAFA).iconSet(FINE)
                .components(Sodium, 2, Boron, 4, Water, 10, Oxygen, 7)
                .build();

        SaltWater = new Material.Builder(2003, gregtechId("salt_water"))
                .fluid()
                .color(0x0000C8)
                .flags(DISABLE_DECOMPOSITION)
                .components(Salt, 1, Water, 1)
                .build();

        Olivine = new Material.Builder(2004, gregtechId("olivine"))
                .gem().ore(2, 1)
                .color(0x96FF96).iconSet(RUBY)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Magnesium, 2, Iron, 1, SiliconDioxide, 2)
                .build();

        Opal = new Material.Builder(2005, gregtechId("opal"))
                .gem().ore()
                .color(0x0000FF).iconSet(OPAL)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .build();

        Amethyst = new Material.Builder(2006, gregtechId("amethyst"))
                .gem(3).ore()
                .color(0xD232D2).iconSet(RUBY)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(SiliconDioxide, 4, Iron, 1)
                .build();

        Lapis = new Material.Builder(2007, gregtechId("lapis"))
                .gem(1).ore(6, 4)
                .color(0x4646DC).iconSet(LAPIS)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, NO_WORKING, DECOMPOSITION_BY_ELECTROLYZING,
                        EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        GENERATE_PLATE, GENERATE_ROD)
                .components(Lazurite, 12, Sodalite, 2, Pyrite, 1, Calcite, 1)
                .build();

        Blaze = new Material.Builder(2008, gregtechId("blaze"))
                .dust(1)
                .liquid(new FluidBuilder().temperature(4000).customStill())
                .color(0xFFC800).iconSet(FINE)
                .flags(NO_SMELTING, MORTAR_GRINDABLE, DECOMPOSITION_BY_CENTRIFUGING) // todo burning flag
                .components(DarkAsh, 1, Sulfur, 1)
                .build();

        // Free ID 2009

        Apatite = new Material.Builder(2010, gregtechId("apatite"))
                .gem(1).ore(2, 2)
                .color(0xC8C8FF).iconSet(DIAMOND)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, GENERATE_BOLT_SCREW, DISABLE_DECOMPOSITION)
                .components(Calcium, 5, Phosphate, 3, Chlorine, 1)
                .build();

        BlackSteel = new Material.Builder(2011, gregtechId("black_steel"))
                .ingot().fluid()
                .color(0x646464).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FINE_WIRE, GENERATE_GEAR, GENERATE_FRAME)
                .components(Nickel, 1, BlackBronze, 1, Steel, 3)
                .cableProperties(V[EV], 3, 2)
                .blast(1200, GasTier.LOW)
                .build();

        DamascusSteel = new Material.Builder(2012, gregtechId("damascus_steel"))
                .ingot(3).fluid()
                .color(0x6E6E6E).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_GEAR)
                .components(Steel, 1)
                .toolStats(ToolProperty.Builder.of(6.0F, 4.0F, 1024, 3)
                        .attackSpeed(0.3F).enchantability(33)
                        .enchantment(Enchantments.LOOTING, 3)
                        .enchantment(Enchantments.FORTUNE, 3).build())
                .blast(1500, GasTier.LOW)
                .build();

        TungstenSteel = new Material.Builder(2013, gregtechId("tungsten_steel"))
                .ingot(4).fluid()
                .color(0x6464A0).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_DENSE, GENERATE_FRAME,
                        GENERATE_SPRING, GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_GEAR, GENERATE_DOUBLE_PLATE)
                .components(Steel, 1, Tungsten, 1)
                .toolStats(ToolProperty.Builder.of(9.0F, 7.0F, 2048, 4)
                        .enchantability(14).build())
                .rotorStats(8.0f, 4.0f, 2560)
                .fluidPipeProperties(3587, 225, true, true, false, false)
                .cableProperties(V[IV], 3, 2)
                .blast(b -> b
                        .temp(4000, GasTier.MID)
                        .blastStats(VA[EV], 1000)
                        .vacuumStats(VA[HV]))
                .build();

        CobaltBrass = new Material.Builder(2014, gregtechId("cobalt_brass"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1202))
                .color(0xB4B4A0).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_GEAR, GENERATE_DOUBLE_PLATE)
                .components(Brass, 7, Aluminium, 1, Cobalt, 1)
                .toolStats(ToolProperty.Builder.of(2.5F, 2.0F, 1024, 2)
                        .attackSpeed(-0.2F).enchantability(5).build())
                .rotorStats(8.0f, 2.0f, 256)
                .itemPipeProperties(2048, 1)
                .build();

        TricalciumPhosphate = new Material.Builder(2015, gregtechId("tricalcium_phosphate"))
                .dust().ore(3, 1)
                .color(0xFFFF00).iconSet(FLINT)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE, EXPLOSIVE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Calcium, 3, Phosphate, 2)
                .build();

        GarnetRed = new Material.Builder(2016, gregtechId("garnet_red"))
                .gem().ore(4, 1)
                .color(0xC85050).iconSet(RUBY)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Pyrope, 3, Almandine, 5, Spessartine, 8)
                .build();

        GarnetYellow = new Material.Builder(2017, gregtechId("garnet_yellow"))
                .gem().ore(4, 1)
                .color(0xC8C850).iconSet(RUBY)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Andradite, 5, Grossular, 8, Uvarovite, 3)
                .build();

        Marble = new Material.Builder(2018, gregtechId("marble"))
                .dust()
                .color(0xC8C8C8).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Magnesium, 1, Calcite, 7)
                .build();

        GraniteBlack = new Material.Builder(2019, gregtechId("granite_black"))
                .dust()
                .color(0x0A0A0A).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 4, Biotite, 1)
                .build();

        GraniteRed = new Material.Builder(2020, gregtechId("granite_red"))
                .dust()
                .color(0xFF0080).iconSet(ROUGH)
                .flags(NO_SMASHING)
                .components(Aluminium, 2, PotassiumFeldspar, 1, Oxygen, 3)
                .build();

        // Free ID 2021

        VanadiumMagnetite = new Material.Builder(2022, gregtechId("vanadium_magnetite"))
                .dust().ore()
                .color(0x23233C).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Magnetite, 1, Vanadium, 1)
                .build();

        QuartzSand = new Material.Builder(2023, gregtechId("quartz_sand"))
                .dust(1)
                .color(0xC8C8C8).iconSet(SAND)
                .flags(DISABLE_DECOMPOSITION)
                .components(CertusQuartz, 1, Quartzite, 1)
                .build();

        Pollucite = new Material.Builder(2024, gregtechId("pollucite"))
                .dust().ore()
                .color(0xF0D2D2)
                .components(Caesium, 2, Aluminium, 2, Silicon, 4, Water, 2, Oxygen, 12)
                .build();

        // Free ID 2025

        Bentonite = new Material.Builder(2026, gregtechId("bentonite"))
                .dust().ore(3, 1)
                .color(0xF5D7D2).iconSet(ROUGH)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 1, Magnesium, 6, Silicon, 12, Hydrogen, 4, Water, 5, Oxygen, 36)
                .build();

        FullersEarth = new Material.Builder(2027, gregtechId("fullers_earth"))
                .dust().ore(2, 1)
                .color(0xA0A078).iconSet(FINE)
                .components(Magnesium, 1, Silicon, 4, Hydrogen, 1, Water, 4, Oxygen, 11)
                .build();

        Pitchblende = new Material.Builder(2028, gregtechId("pitchblende"))
                .dust(3).ore(true)
                .color(0xC8D200)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Uraninite, 3, Thorium, 1, Lead, 1)
                .build()
                .setFormula("(UO2)3ThPb", true);

        Monazite = new Material.Builder(2029, gregtechId("monazite"))
                .gem(1).ore(4, 2, true)
                .color(0x324632).iconSet(DIAMOND)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE)
                .components(RareEarth, 1, Phosphate, 1)
                .build();

        Mirabilite = new Material.Builder(2030, gregtechId("mirabilite"))
                .dust()
                .color(0xF0FAD2)
                .components(Sodium, 2, Sulfur, 1, Water, 10, Oxygen, 4)
                .build();

        Trona = new Material.Builder(2031, gregtechId("trona"))
                .dust(1).ore(2, 1)
                .color(0x87875F).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 3, Carbon, 2, Hydrogen, 1, Water, 2, Oxygen, 6)
                .build();

        Gypsum = new Material.Builder(2032, gregtechId("gypsum"))
                .dust(1).ore()
                .color(0xE6E6FA)
                .components(Calcium, 1, Sulfur, 1, Water, 2, Oxygen, 4)
                .build();

        Zeolite = new Material.Builder(2033, gregtechId("zeolite"))
                .dust().ore(3, 1)
                .color(0xF0E6E6)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 1, Calcium, 4, Silicon, 27, Aluminium, 9, Water, 28, Oxygen, 72)
                .build();

        Concrete = new Material.Builder(2034, gregtechId("concrete"))
                .dust()
                .liquid(new FluidBuilder().temperature(286))
                .color(0x646464).iconSet(ROUGH)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Stone, 1)
                .build();

        SteelMagnetic = new Material.Builder(2035, gregtechId("steel_magnetic"))
                .ingot()
                .color(0x808080).iconSet(MAGNETIC)
                .flags(GENERATE_ROD, IS_MAGNETIC)
                .components(Steel, 1)
                .ingotSmeltInto(Steel)
                .arcSmeltInto(Steel)
                .macerateInto(Steel)
                .build();
        Steel.getProperty(PropertyKey.INGOT).setMagneticMaterial(SteelMagnetic);

        VanadiumSteel = new Material.Builder(2036, gregtechId("vanadium_steel"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(2073))
                .color(0xc0c0c0).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_DOUBLE_PLATE, GENERATE_FOIL, GENERATE_GEAR)
                .components(Vanadium, 1, Chrome, 1, Steel, 7)
                .toolStats(ToolProperty.Builder.of(3.0F, 3.0F, 1536, 3)
                        .attackSpeed(-0.2F).enchantability(5).build())
                .rotorStats(7.0f, 3.0f, 1920)
                .fluidPipeProperties(2073, 50, true, true, false, false)
                .blast(1453, GasTier.LOW)
                .build();

        Potin = new Material.Builder(2037, gregtechId("potin"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1084))
                .color(0xc99781).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_GEAR, GENERATE_DOUBLE_PLATE)
                .components(Copper, 6, Tin, 2, Lead, 1)
                .fluidPipeProperties(1456, 40, true)
                .build();

        BorosilicateGlass = new Material.Builder(2038, gregtechId("borosilicate_glass"))
                .ingot(1)
                .liquid(new FluidBuilder().temperature(1921))
                .color(0xE6F3E6).iconSet(SHINY)
                .flags(GENERATE_FINE_WIRE, GENERATE_PLATE)
                .components(Boron, 1, SiliconDioxide, 7)
                .build();

        Andesite = new Material.Builder(2039, gregtechId("andesite"))
                .dust()
                .color(0xBEBEBE).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Asbestos, 4, Saltpeter, 1)
                .build();

        // FREE ID 2040

        // FREE ID 2041

        NaquadahAlloy = new Material.Builder(2042, gregtechId("naquadah_alloy"))
                .ingot(5).fluid()
                .color(0x282828).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_SPRING, GENERATE_RING, GENERATE_ROTOR, GENERATE_SMALL_GEAR,
                        GENERATE_FRAME, GENERATE_DENSE, GENERATE_FOIL, GENERATE_GEAR, GENERATE_DOUBLE_PLATE)
                .components(Naquadah, 2, Osmiridium, 1, Trinium, 1)
                .toolStats(ToolProperty.Builder.of(40.0F, 12.0F, 3072, 5)
                        .attackSpeed(0.3F).enchantability(33).magnetic().build())
                .rotorStats(8.0f, 5.0f, 5120)
                .cableProperties(V[UV], 2, 4)
                .blast(b -> b
                        .temp(7200, GasTier.HIGH)
                        .blastStats(VA[LuV], 1000)
                        .vacuumStats(VA[IV], 300))
                .build();

        SulfuricNickelSolution = new Material.Builder(2043, gregtechId("sulfuric_nickel_solution"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x3EB640)
                .components(Nickel, 1, Oxygen, 1, SulfuricAcid, 1)
                .build();

        SulfuricCopperSolution = new Material.Builder(2044, gregtechId("sulfuric_copper_solution"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x48A5C0)
                .components(Copper, 1, Oxygen, 1, SulfuricAcid, 1)
                .build();

        LeadZincSolution = new Material.Builder(2045, gregtechId("lead_zinc_solution"))
                .liquid(new FluidBuilder().customStill())
                .color(0x3C0404)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Lead, 1, Silver, 1, Zinc, 1, Sulfur, 3, Water, 1)
                .build();

        NitrationMixture = new Material.Builder(2046, gregtechId("nitration_mixture"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xE6E2AB)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitricAcid, 1, SulfuricAcid, 1)
                .build();

        DilutedSulfuricAcid = new Material.Builder(2047, gregtechId("diluted_sulfuric_acid"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xC07820)
                .flags(DISABLE_DECOMPOSITION)
                .components(SulfuricAcid, 2, Water, 1)
                .build();

        DilutedHydrochloricAcid = new Material.Builder(2048, gregtechId("diluted_hydrochloric_acid"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x99A7A3)
                .flags(DISABLE_DECOMPOSITION)
                .components(HydrochloricAcid, 1, Water, 1)
                .build();

        Flint = new Material.Builder(2049, gregtechId("flint"))
                .gem(1)
                .color(0x002040).iconSet(FLINT)
                .flags(NO_SMASHING, MORTAR_GRINDABLE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .toolStats(ToolProperty.Builder.of(0.0F, 1.0F, 64, 1)
                        .enchantability(5).ignoreCraftingTools()
                        .enchantment(Enchantments.FIRE_ASPECT, 2).build())
                .build();

        Air = new Material.Builder(2050, gregtechId("air"))
                .gas(new FluidBuilder().customStill())
                .color(0xA9D0F5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Nitrogen, 78, Oxygen, 21, Argon, 9)
                .build();

        LiquidAir = new Material.Builder(2051, gregtechId("liquid_air"))
                .liquid(new FluidBuilder()
                        .temperature(79)
                        .customStill())
                .color(0x84BCFC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Nitrogen, 70, Oxygen, 22, CarbonDioxide, 5, Helium, 2, Argon, 1, Ice, 1)
                .build();

        NetherAir = new Material.Builder(2052, gregtechId("nether_air"))
                .gas()
                .color(0x4C3434)
                .flags(DISABLE_DECOMPOSITION)
                .components(CarbonMonoxide, 78, HydrogenSulfide, 21, Neon, 9)
                .build();

        LiquidNetherAir = new Material.Builder(2053, gregtechId("liquid_nether_air"))
                .liquid(new FluidBuilder().temperature(58))
                .color(0x4C3434)
                .flags(DISABLE_DECOMPOSITION)
                .components(CarbonMonoxide, 144, CoalGas, 20, HydrogenSulfide, 15, SulfurDioxide, 15, Helium3, 5, Neon,
                        1, Ash, 1)
                .build();

        EnderAir = new Material.Builder(2054, gregtechId("ender_air"))
                .gas()
                .color(0x283454)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitrogenDioxide, 78, Deuterium, 21, Xenon, 9)
                .build();

        LiquidEnderAir = new Material.Builder(2055, gregtechId("liquid_ender_air"))
                .liquid(new FluidBuilder().temperature(36))
                .color(0x283454)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitrogenDioxide, 122, Deuterium, 50, Helium, 15, Tritium, 10, Krypton, 1, Xenon, 1, Radon,
                        1, EnderPearl, 1)
                .build();

        AquaRegia = new Material.Builder(2056, gregtechId("aqua_regia"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xFFB132)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitricAcid, 1, HydrochloricAcid, 2)
                .build();

        PlatinumSludgeResidue = new Material.Builder(2057, gregtechId("platinum_sludge_residue"))
                .dust()
                .color(0x827951)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 2, Gold, 3)
                .build();

        PalladiumRaw = new Material.Builder(2058, gregtechId("palladium_raw"))
                .dust()
                .color(Palladium.getMaterialRGB()).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Palladium, 1, Ammonia, 1)
                .build();

        RarestMetalMixture = new Material.Builder(2059, gregtechId("rarest_metal_mixture"))
                .dust()
                .color(0x832E11).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iridium, 1, Osmium, 1, Oxygen, 4, Water, 1)
                .build();

        AmmoniumChloride = new Material.Builder(2060, gregtechId("ammonium_chloride"))
                .dust()
                .color(0x9711A6)
                .components(Ammonia, 1, HydrochloricAcid, 1)
                .build()
                .setFormula("NH4Cl", true);

        AcidicOsmiumSolution = new Material.Builder(2061, gregtechId("acidic_osmium_solution"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0xA3AA8A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Osmium, 1, Oxygen, 4, Water, 1, HydrochloricAcid, 1)
                .build();

        RhodiumPlatedPalladium = new Material.Builder(2062, gregtechId("rhodium_plated_palladium"))
                .ingot().fluid()
                .color(0xDAC5C5).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE, GENERATE_SMALL_GEAR, GENERATE_DOUBLE_PLATE)
                .components(Palladium, 3, Rhodium, 1)
                .rotorStats(12.0f, 3.0f, 1024)
                .blast(b -> b
                        .temp(4500, GasTier.HIGH)
                        .blastStats(VA[IV], 1200)
                        .vacuumStats(VA[EV], 300))
                .build();

        Clay = new Material.Builder(2063, gregtechId("clay"))
                .dust(1)
                .color(0xC8C8DC).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Sodium, 2, Lithium, 1, Aluminium, 2, Silicon, 2, Water, 6)
                .build();

        Redstone = new Material.Builder(2064, gregtechId("redstone"))
                .dust().ore(5, 1, true)
                .liquid(new FluidBuilder().temperature(500))
                .color(0xC80000).iconSet(ROUGH)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        EXCLUDE_PLATE_COMPRESSOR_RECIPE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Silicon, 1, Pyrite, 5, Ruby, 1, Mercury, 3)
                .build();

        LEU235 = new Material.Builder(2065, gregtechId("leu_235"))
                .dust(3)
                .color(0x232323).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(HighEnrichedUraniumDioxide, 1, DepletedUraniumDioxide, 19)
                .fissionFuel(1500, 750, 55., 1., 2500., 0., 3.5)
                .build()
                .setFormula("UO2", true);

        HEU235 = new Material.Builder(2066, gregtechId("heu_235"))
                .dust(3)
                .color(0x424845).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(HighEnrichedUraniumDioxide, 1, DepletedUraniumDioxide, 4)
                .fissionFuel(1800, 600, 40., 1., 3000., 0., 2.5)
                .build()
                .setFormula("UO2", true);

        LowGradeMOX = new Material.Builder(2067, gregtechId("low_grade_mox"))
                .dust(3)
                .color(0x62C032).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(FissilePlutoniumDioxide, 1, Uraninite, 19)
                .fissionFuel(1600, 1000, 50., 10., 4000., 10., 1.5)
                .build()
                .setFormula("(U,Pu)O2", true);

        HighGradeMOX = new Material.Builder(2068, gregtechId("high_grade_mox"))
                .dust(3)
                .color(0x7EA432).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(FissilePlutoniumDioxide, 1, Uraninite, 4)
                .fissionFuel(2000, 800, 35., 25., 5500., 0., 1)
                .build()
                .setFormula("(U,Pu)O2", true);
    }
}
