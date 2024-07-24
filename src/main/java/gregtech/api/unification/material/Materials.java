package gregtech.api.unification.material;

import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.materials.*;
import gregtech.api.unification.ore.OrePrefix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static gregtech.api.unification.material.info.MaterialFlags.*;

/**
 * Material Registration.
 * <p>
 * All Material Builders should follow this general formatting:
 * <p>
 * material = new MaterialBuilder(id, name)
 * .ingot().fluid().ore() <--- types
 * .color().iconSet() <--- appearance
 * .flags() <--- special generation
 * .element() / .components() <--- composition
 * .toolStats() <---
 * .oreByProducts() | additional properties
 * ... <---
 * .blastTemp() <--- blast temperature
 * .build();
 * <p>
 * Use defaults to your advantage! Some defaults:
 * - iconSet: DULL
 * - color: 0xFFFFFF
 */
public class Materials {

    private static final AtomicBoolean INIT = new AtomicBoolean(false);

    public static Material[] CHEMICAL_DYES;

    public static void register() {
        if (INIT.getAndSet(true)) {
            return;
        }

        MarkerMaterials.register();

        /*
         * Ranges 1-249
         */
        ElementMaterials.register();

        /*
         * Ranges 250-999
         */
        FirstDegreeMaterials.register();

        /*
         * Ranges 1000-1499
         */
        OrganicChemistryMaterials.register();

        /*
         * Ranges 1500-1999
         */
        UnknownCompositionMaterials.register();

        /*
         * Ranges 2000-2499
         */
        SecondDegreeMaterials.register();

        /*
         * Ranges 2500-2999
         */
        HigherDegreeMaterials.register();

        /*
         * Register info for cyclical references
         */
        MaterialFlagAddition.register();

        /*
         * FOR ADDON DEVELOPERS:
         *
         * GTCEu will not take more than 3000 IDs. Anything past ID 2999
         * is considered FAIR GAME, take whatever you like.
         *
         * If you would like to reserve IDs, feel free to reach out to the
         * development team and claim a range of IDs! We will mark any
         * claimed ranges below this comment. Max value is 32767.
         *
         * - Gregicality: 3000-19999
         * - Gregification: 20000-20999
         * - HtmlTech: 21000-21499
         * - GregTech Food Option: 21500-22499
         * - FREE RANGE 22500-23599
         * - MechTech: 23600-23999
         * - FREE RANGE 24000-31999
         * - Reserved for CraftTweaker: 32000-32767
         */

        CHEMICAL_DYES = new Material[] {
                Materials.DyeWhite, Materials.DyeOrange,
                Materials.DyeMagenta, Materials.DyeLightBlue,
                Materials.DyeYellow, Materials.DyeLime,
                Materials.DyePink, Materials.DyeGray,
                Materials.DyeLightGray, Materials.DyeCyan,
                Materials.DyePurple, Materials.DyeBlue,
                Materials.DyeBrown, Materials.DyeGreen,
                Materials.DyeRed, Materials.DyeBlack
        };

        OrePrefix.init();
    }

    public static final List<MaterialFlag> STD_METAL = new ArrayList<>();
    public static final List<MaterialFlag> EXT_METAL = new ArrayList<>();
    public static final List<MaterialFlag> EXT2_METAL = new ArrayList<>();

    static {
        STD_METAL.add(GENERATE_PLATE);

        EXT_METAL.addAll(STD_METAL);
        EXT_METAL.add(GENERATE_ROD);

        EXT2_METAL.addAll(EXT_METAL);
        EXT2_METAL.addAll(Arrays.asList(GENERATE_LONG_ROD, GENERATE_BOLT_SCREW));
    }

    public static final MarkerMaterial NULL = MarkerMaterial.create("null");

    /**
     * Direct Elements
     */
    public static Material Actinium;
    public static Material Aluminium;
    public static Material Americium;
    public static Material Antimony;
    public static Material Argon;
    public static Material Arsenic;
    public static Material Astatine;
    public static Material Barium;
    public static Material Berkelium;
    public static Material Beryllium;
    public static Material Bismuth;
    public static Material Bohrium;
    public static Material Boron;
    public static Material Bromine;
    public static Material Caesium;
    public static Material Calcium;
    public static Material Californium;
    public static Material Carbon;
    public static Material Cadmium;
    public static Material Cerium;
    public static Material Chlorine;
    public static Material Chrome;
    public static Material Cobalt;
    public static Material Copernicium;
    public static Material Copper;
    public static Material Curium;
    public static Material Darmstadtium;
    public static Material Deuterium;
    public static Material Dubnium;
    public static Material Dysprosium;
    public static Material Einsteinium;
    public static Material Erbium;
    public static Material Europium;
    public static Material Fermium;
    public static Material Flerovium;
    public static Material Fluorine;
    public static Material Francium;
    public static Material Gadolinium;
    public static Material Gallium;
    public static Material Germanium;
    public static Material Gold;
    public static Material Hafnium;
    public static Material Hassium;
    public static Material Holmium;
    public static Material Hydrogen;
    public static Material Helium;
    public static Material Helium3;
    public static Material Indium;
    public static Material Iodine;
    public static Material Iridium;
    public static Material Iron;
    public static Material Krypton;
    public static Material Lanthanum;
    public static Material Lawrencium;
    public static Material Lead;
    public static Material Lithium;
    public static Material Livermorium;
    public static Material Lutetium;
    public static Material Magnesium;
    public static Material Mendelevium;
    public static Material Manganese;
    public static Material Meitnerium;
    public static Material Mercury;
    public static Material Molybdenum;
    public static Material Moscovium;
    public static Material Neodymium;
    public static Material Neon;
    public static Material Neptunium;
    public static Material Neptunium235;
    public static Material Neptunium236;
    public static Material Neptunium237;
    public static Material Neptunium239;
    public static Material Nickel;
    public static Material Nihonium;
    public static Material Niobium;
    public static Material Nitrogen;
    public static Material Nobelium;
    public static Material Oganesson;
    public static Material Osmium;
    public static Material Oxygen;
    public static Material Palladium;
    public static Material Phosphorus;
    public static Material Polonium;
    public static Material Platinum;
    public static Material Plutonium;
    public static Material Plutonium238;
    public static Material Plutonium239;
    public static Material Plutonium240;
    public static Material Plutonium241;
    public static Material Plutonium242;
    public static Material Plutonium244;
    public static Material Potassium;
    public static Material Praseodymium;
    public static Material Promethium;
    public static Material Protactinium;
    public static Material Radon;
    public static Material Radium;
    public static Material Rhenium;
    public static Material Rhodium;
    public static Material Roentgenium;
    public static Material Rubidium;
    public static Material Ruthenium;
    public static Material Rutherfordium;
    public static Material Samarium;
    public static Material Scandium;
    public static Material Seaborgium;
    public static Material Selenium;
    public static Material Silicon;
    public static Material Silver;
    public static Material Sodium;
    public static Material Strontium;
    public static Material Sulfur;
    public static Material Tantalum;
    public static Material Technetium;
    public static Material Tellurium;
    public static Material Tennessine;
    public static Material Terbium;
    public static Material Thorium;
    public static Material Thallium;
    public static Material Thulium;
    public static Material Tin;
    public static Material Titanium;
    public static Material Tritium;
    public static Material Tungsten;
    public static Material Uranium;
    public static Material Uranium235;
    public static Material Uranium238;
    public static Material Uranium239;
    public static Material Vanadium;
    public static Material Xenon;
    public static Material Ytterbium;
    public static Material Yttrium;
    public static Material Zinc;
    public static Material Zirconium;

    /**
     * Fantasy Elements
     */
    public static Material Naquadah;
    public static Material NaquadahEnriched;
    public static Material Naquadria;
    public static Material Neutronium;
    public static Material Tritanium;
    public static Material Duranium;
    public static Material Trinium;

    /**
     * First Degree Compounds
     */
    public static Material Almandine;
    public static Material Andradite;
    public static Material AnnealedCopper;
    public static Material Asbestos;
    public static Material Ash;
    public static Material BandedIron;
    public static Material BatteryAlloy;
    public static Material BlueTopaz;
    public static Material Bone;
    public static Material Brass;
    public static Material Bronze;
    public static Material BrownLimonite;
    public static Material Calcite;
    public static Material Cassiterite;
    public static Material CassiteriteSand;
    public static Material Chalcopyrite;
    public static Material Charcoal;
    public static Material Chromite;
    public static Material Cinnabar;
    public static Material Water;
    public static Material Coal;
    public static Material Cobaltite;
    public static Material Cooperite;
    public static Material Cupronickel;
    public static Material DarkAsh;
    public static Material Diamond;
    public static Material Electrum;
    public static Material Emerald;
    public static Material Galena;
    public static Material Garnierite;
    public static Material GreenSapphire;
    public static Material Grossular;
    public static Material Ice;
    public static Material Ilmenite;
    public static Material Rutile;
    public static Material Bauxite;
    public static Material Invar;
    public static Material Kanthal;
    public static Material Lazurite;
    public static Material Magnalium;
    public static Material Magnesite;
    public static Material Magnetite;
    public static Material Molybdenite;
    public static Material Nichrome;
    public static Material NiobiumNitride;
    public static Material NiobiumTitanium;
    public static Material Obsidian;
    public static Material Phosphate;
    public static Material SterlingSilver;
    public static Material RoseGold;
    public static Material BlackBronze;
    public static Material BismuthBronze;
    public static Material Biotite;
    public static Material Powellite;
    public static Material Pyrite;
    public static Material Pyrolusite;
    public static Material Pyrope;
    public static Material RockSalt;
    public static Material Ruridit;
    public static Material Rubber;
    public static Material Ruby;
    public static Material Salt;
    public static Material Saltpeter;
    public static Material Sapphire;
    public static Material Scheelite;
    public static Material Sodalite;
    public static Material AluminiumSulfite;
    public static Material Tantalite;
    public static Material Coke;

    public static Material SolderingAlloy;
    public static Material Spessartine;
    public static Material Sphalerite;
    public static Material StainlessSteel;
    public static Material Steel;
    public static Material Stibnite;
    public static Material Tetrahedrite;
    public static Material TinAlloy;
    public static Material Topaz;
    public static Material Tungstate;
    public static Material Ultimet;
    public static Material Uraninite;
    public static Material Uvarovite;
    public static Material VanadiumGallium;
    public static Material WroughtIron;
    public static Material Wulfenite;
    public static Material YellowLimonite;
    public static Material YttriumBariumCuprate;
    public static Material NetherQuartz;
    public static Material CertusQuartz;
    public static Material Quartzite;
    public static Material Graphite;
    public static Material Graphene;
    public static Material TungsticAcid;
    public static Material Osmiridium;
    public static Material LithiumChloride;
    public static Material CalciumChloride;
    public static Material Bornite;
    public static Material Chalcocite;

    public static Material GalliumArsenide;
    public static Material Potash;
    public static Material SodaAsh;
    public static Material IndiumGalliumPhosphide;
    public static Material NickelZincFerrite;
    public static Material SiliconDioxide;
    public static Material MagnesiumChloride;
    public static Material SodiumSulfide;
    public static Material PhosphorusPentoxide;
    public static Material Quicklime;
    public static Material SodiumBisulfate;
    public static Material FerriteMixture;
    public static Material Magnesia;
    public static Material PlatinumGroupSludge;
    public static Material Realgar;
    public static Material SodiumBicarbonate;
    public static Material PotassiumDichromate;
    public static Material ChromiumTrioxide;
    public static Material AntimonyTrioxide;
    public static Material Zincite;
    public static Material CupricOxide;
    public static Material CobaltOxide;
    public static Material ArsenicTrioxide;
    public static Material Massicot;
    public static Material Ferrosilite;
    public static Material MetalMixture;
    public static Material SodiumHydroxide;
    public static Material SodiumPersulfate;
    public static Material Bastnasite;
    public static Material Pentlandite;
    public static Material Spodumene;
    public static Material Lepidolite;
    public static Material GlauconiteSand;
    public static Material Malachite;
    public static Material Mica;
    public static Material Barite;
    public static Material Alunite;
    public static Material Talc;
    public static Material Soapstone;
    public static Material Kyanite;
    public static Material IronMagnetic;
    public static Material TungstenCarbide;
    public static Material CarbonDioxide;
    public static Material TitaniumTetrachloride;
    public static Material NitrogenDioxide;
    public static Material HydrogenSulfide;
    public static Material NitricAcid;
    public static Material SulfuricAcid;
    public static Material PhosphoricAcid;
    public static Material SulfurTrioxide;
    public static Material SulfurDioxide;
    public static Material CarbonMonoxide;
    public static Material HypochlorousAcid;
    public static Material Ammonia;
    public static Material HydrofluoricAcid;
    public static Material NitricOxide;
    public static Material Iron3Chloride;
    public static Material UraniumHexafluoride;
    public static Material HighEnrichedUraniumHexafluoride;
    public static Material LowEnrichedUraniumHexafluoride;
    public static Material DepletedUraniumHexafluoride;
    public static Material LowEnrichedUraniumDioxide;
    public static Material NitrousOxide;
    public static Material EnderPearl;
    public static Material PotassiumFeldspar;
    public static Material NeodymiumMagnetic;
    public static Material HydrochloricAcid;
    public static Material Steam;
    public static Material DistilledWater;
    public static Material SodiumPotassium;
    public static Material SamariumMagnetic;
    public static Material ManganesePhosphide;
    public static Material MagnesiumDiboride;
    public static Material MercuryBariumCalciumCuprate;
    public static Material UraniumTriplatinum;
    public static Material SamariumIronArsenicOxide;
    public static Material IndiumTinBariumTitaniumCuprate;
    public static Material UraniumRhodiumDinaquadide;
    public static Material EnrichedNaquadahTriniumEuropiumDuranide;
    public static Material RutheniumTriniumAmericiumNeutronate;
    public static Material PlatinumRaw;
    public static Material InertMetalMixture;
    public static Material RhodiumSulfate;
    public static Material RutheniumTetroxide;
    public static Material OsmiumTetroxide;
    public static Material IridiumChloride;
    public static Material FluoroantimonicAcid;
    public static Material TitaniumTrifluoride;
    public static Material CalciumPhosphide;
    public static Material IndiumPhosphide;
    public static Material BariumSulfide;
    public static Material TriniumSulfide;
    public static Material ZincSulfide;
    public static Material GalliumSulfide;
    public static Material AntimonyTrifluoride;
    public static Material EnrichedNaquadahSulfate;
    public static Material NaquadriaSulfate;
    public static Material Pyrochlore;
    public static Material RTMAlloy;
    public static Material HighEnrichedUraniumDioxide;
    public static Material DepletedUraniumDioxide;
    public static Material HighPressureSteam;
    public static Material FissilePlutoniumDioxide;
    public static Material Zircaloy;
    public static Material Zircon;
    public static Material ZirconiumDioxide;
    public static Material ZirconiumTetrachloride;
    public static Material HafniumDioxide;
    public static Material HafniumTetrachloride;
    public static Material Inconel;
    public static Material BoronTrioxide;
    public static Material BoronCarbide;
    public static Material HeavyWater;
    public static Material HighPressureHeavyWater;

    /**
     * Organic chemistry
     */
    public static Material SiliconeRubber;
    public static Material RawRubber;
    public static Material RawStyreneButadieneRubber;
    public static Material StyreneButadieneRubber;
    public static Material PolyvinylAcetate;
    public static Material ReinforcedEpoxyResin;
    public static Material PolyvinylChloride;
    public static Material PolyphenyleneSulfide;
    public static Material GlycerylTrinitrate;
    public static Material Polybenzimidazole;
    public static Material Polydimethylsiloxane;
    public static Material Polyethylene;
    public static Material Epoxy;
    public static Material Polycaprolactam;
    public static Material Polytetrafluoroethylene;
    public static Material Sugar;
    public static Material Methane;
    public static Material Epichlorohydrin;
    public static Material Monochloramine;
    public static Material Chloroform;
    public static Material Cumene;
    public static Material Tetrafluoroethylene;
    public static Material Chloromethane;
    public static Material AllylChloride;
    public static Material Isoprene;
    public static Material Propane;
    public static Material Propene;
    public static Material Ethane;
    public static Material Butene;
    public static Material Butane;
    public static Material DissolvedCalciumAcetate;
    public static Material VinylAcetate;
    public static Material MethylAcetate;
    public static Material Ethenone;
    public static Material Tetranitromethane;
    public static Material Dimethylamine;
    public static Material Dimethylhydrazine;
    public static Material DinitrogenTetroxide;
    public static Material Dimethyldichlorosilane;
    public static Material Styrene;
    public static Material Butadiene;
    public static Material Dichlorobenzene;
    public static Material AceticAcid;
    public static Material Phenol;
    public static Material BisphenolA;
    public static Material VinylChloride;
    public static Material Ethylene;
    public static Material Benzene;
    public static Material Acetone;
    public static Material Glycerol;
    public static Material Methanol;
    public static Material Ethanol;
    public static Material Toluene;
    public static Material DiphenylIsophtalate;
    public static Material PhthalicAcid;
    public static Material Dimethylbenzene;
    public static Material Diaminobenzidine;
    public static Material Dichlorobenzidine;
    public static Material Nitrochlorobenzene;
    public static Material Chlorobenzene;
    public static Material Octane;
    public static Material EthylTertButylEther;
    public static Material Ethylbenzene;
    public static Material Naphthalene;
    public static Material Nitrobenzene;
    public static Material Cyclohexane;
    public static Material NitrosylChloride;
    public static Material CyclohexanoneOxime;
    public static Material Caprolactam;
    public static Material PlatinumSludgeResidue;
    public static Material PalladiumRaw;
    public static Material RarestMetalMixture;
    public static Material AmmoniumChloride;
    public static Material AcidicOsmiumSolution;
    public static Material RhodiumPlatedPalladium;
    public static Material Butyraldehyde;
    public static Material PolyvinylButyral;
    public static Material Biphenyl;
    public static Material PolychlorinatedBiphenyl;

    /**
     * Not possible to determine exact Components
     */
    public static Material WoodGas;
    public static Material WoodVinegar;
    public static Material WoodTar;
    public static Material CharcoalByproducts;
    public static Material Biomass;
    public static Material BioDiesel;
    public static Material FermentedBiomass;
    public static Material Creosote;
    public static Material Diesel;
    public static Material RocketFuel;
    public static Material Glue;
    public static Material Lubricant;
    public static Material McGuffium239;
    public static Material IndiumConcentrate;
    public static Material SeedOil;
    public static Material DrillingFluid;
    public static Material ConstructionFoam;

    public static Material Oil;
    public static Material OilHeavy;
    public static Material RawOil;
    public static Material OilLight;
    public static Material NaturalGas;
    public static Material SulfuricHeavyFuel;
    public static Material HeavyFuel;
    public static Material LightlyHydroCrackedHeavyFuel;
    public static Material SeverelyHydroCrackedHeavyFuel;
    public static Material LightlySteamCrackedHeavyFuel;
    public static Material SeverelySteamCrackedHeavyFuel;
    public static Material SulfuricLightFuel;
    public static Material LightFuel;
    public static Material LightlyHydroCrackedLightFuel;
    public static Material SeverelyHydroCrackedLightFuel;
    public static Material LightlySteamCrackedLightFuel;
    public static Material SeverelySteamCrackedLightFuel;
    public static Material SulfuricNaphtha;
    public static Material Naphtha;
    public static Material LightlyHydroCrackedNaphtha;
    public static Material SeverelyHydroCrackedNaphtha;
    public static Material LightlySteamCrackedNaphtha;
    public static Material SeverelySteamCrackedNaphtha;
    public static Material SulfuricGas;
    public static Material RefineryGas;
    public static Material LightlyHydroCrackedGas;
    public static Material SeverelyHydroCrackedGas;
    public static Material LightlySteamCrackedGas;
    public static Material SeverelySteamCrackedGas;
    public static Material HydroCrackedEthane;
    public static Material HydroCrackedEthylene;
    public static Material HydroCrackedPropene;
    public static Material HydroCrackedPropane;
    public static Material HydroCrackedButane;
    public static Material HydroCrackedButene;
    public static Material HydroCrackedButadiene;
    public static Material SteamCrackedEthane;
    public static Material SteamCrackedEthylene;
    public static Material SteamCrackedPropene;
    public static Material SteamCrackedPropane;
    public static Material SteamCrackedButane;
    public static Material SteamCrackedButene;
    public static Material SteamCrackedButadiene;
    public static Material LPG;

    public static Material RawGrowthMedium;
    public static Material SterileGrowthMedium;
    public static Material Bacteria;
    public static Material BacterialSludge;
    public static Material EnrichedBacterialSludge;
    public static Material Mutagen;
    public static Material GelatinMixture;
    public static Material RawGasoline;
    public static Material Gasoline;
    public static Material HighOctaneGasoline;
    public static Material CoalGas;
    public static Material CoalTar;
    public static Material Gunpowder;
    public static Material Oilsands;
    public static Material RareEarth;
    public static Material Stone;
    public static Material Lava;
    public static Material Glowstone;
    public static Material NetherStar;
    public static Material Endstone;
    public static Material Netherrack;
    public static Material CetaneBoostedDiesel;
    public static Material Collagen;
    public static Material Gelatin;
    public static Material Agar;
    public static Material Andesite;
    public static Material Milk;
    public static Material Cocoa;
    public static Material Wheat;
    public static Material Meat;
    public static Material Wood;
    public static Material TreatedWood;
    public static Material Paper;
    public static Material FishOil;
    public static Material RubySlurry;
    public static Material SapphireSlurry;
    public static Material GreenSapphireSlurry;
    public static Material DyeBlack;
    public static Material DyeRed;
    public static Material DyeGreen;
    public static Material DyeBrown;
    public static Material DyeBlue;
    public static Material DyePurple;
    public static Material DyeCyan;
    public static Material DyeLightGray;
    public static Material DyeGray;
    public static Material DyePink;
    public static Material DyeLime;
    public static Material DyeYellow;
    public static Material DyeLightBlue;
    public static Material DyeMagenta;
    public static Material DyeOrange;
    public static Material DyeWhite;

    public static Material ImpureEnrichedNaquadahSolution;
    public static Material EnrichedNaquadahSolution;
    public static Material AcidicEnrichedNaquadahSolution;
    public static Material EnrichedNaquadahWaste;
    public static Material ImpureNaquadriaSolution;
    public static Material NaquadriaSolution;
    public static Material AcidicNaquadriaSolution;
    public static Material NaquadriaWaste;
    public static Material Lapotron;
    public static Material UUMatter;
    public static Material PCBCoolant;
    public static Material Corium;
    public static Material SpentUraniumFuelSolution;
    public static Material RadonRichGasMixture;

    /**
     * Second Degree Compounds
     */
    public static Material Glass;
    public static Material Perlite;
    public static Material Borax;
    public static Material Olivine;
    public static Material Opal;
    public static Material Amethyst;
    public static Material Lapis;
    public static Material Blaze;
    public static Material Apatite;
    public static Material BlackSteel;
    public static Material DamascusSteel;
    public static Material TungstenSteel;
    public static Material CobaltBrass;
    public static Material TricalciumPhosphate;
    public static Material GarnetRed;
    public static Material GarnetYellow;
    public static Material Marble;
    public static Material GraniteBlack;
    public static Material GraniteRed;
    public static Material VanadiumMagnetite;
    public static Material QuartzSand;
    public static Material Pollucite;
    public static Material Bentonite;
    public static Material FullersEarth;
    public static Material Pitchblende;
    public static Material Monazite;
    public static Material Mirabilite;
    public static Material Trona;
    public static Material Gypsum;
    public static Material Zeolite;
    public static Material Concrete;
    public static Material SteelMagnetic;
    public static Material VanadiumSteel;
    public static Material Potin;
    public static Material BorosilicateGlass;
    public static Material NaquadahAlloy;
    public static Material SulfuricNickelSolution;
    public static Material SulfuricCopperSolution;
    public static Material LeadZincSolution;
    public static Material NitrationMixture;
    public static Material DilutedSulfuricAcid;
    public static Material DilutedHydrochloricAcid;
    public static Material Flint;
    public static Material Air;
    public static Material LiquidAir;
    public static Material NetherAir;
    public static Material LiquidNetherAir;
    public static Material EnderAir;
    public static Material LiquidEnderAir;
    public static Material AquaRegia;
    public static Material SaltWater;
    public static Material Clay;
    public static Material Redstone;
    public static Material LEU235;
    public static Material HEU235;
    public static Material LowGradeMOX;
    public static Material HighGradeMOX;

    /**
     * Third Degree Materials
     */
    public static Material Electrotine;
    public static Material EnderEye;
    public static Material Diatomite;
    public static Material RedSteel;
    public static Material BlueSteel;
    public static Material Basalt;
    public static Material GraniticMineralSand;
    public static Material Redrock;
    public static Material GarnetSand;
    public static Material HSSG;
    public static Material IridiumMetalResidue;
    public static Material Granite;
    public static Material Brick;
    public static Material Fireclay;
    public static Material Diorite;

    /**
     * Fourth Degree Materials
     */
    public static Material RedAlloy;
    public static Material BlueAlloy;
    public static Material BasalticMineralSand;
    public static Material HSSE;
    public static Material HSSS;
}
