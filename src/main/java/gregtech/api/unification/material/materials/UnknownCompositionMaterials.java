package gregtech.api.unification.material.materials;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;

import net.minecraftforge.fluids.FluidRegistry;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;
import static gregtech.api.util.GTUtility.gregtechId;

public class UnknownCompositionMaterials {

    public static void register() {
        WoodGas = new Material.Builder(1500, gregtechId("wood_gas"))
                .gas().color(0xDECD87).build();

        WoodVinegar = new Material.Builder(1501, gregtechId("wood_vinegar"))
                .fluid().color(0xD45500).build();

        WoodTar = new Material.Builder(1502, gregtechId("wood_tar"))
                .fluid().color(0x28170B)
                .flags(STICKY, FLAMMABLE).build();

        CharcoalByproducts = new Material.Builder(1503, gregtechId("charcoal_byproducts"))
                .fluid().color(0x784421).build();

        Biomass = new Material.Builder(1504, gregtechId("biomass"))
                .liquid(new FluidBuilder().customStill())
                .color(0x14CC04).build();

        BioDiesel = new Material.Builder(1505, gregtechId("bio_diesel"))
                .fluid().color(0xFF8000)
                .flags(FLAMMABLE, EXPLOSIVE).build();

        FermentedBiomass = new Material.Builder(1506, gregtechId("fermented_biomass"))
                .liquid(new FluidBuilder().temperature(300))
                .color(0x445500)
                .build();

        Creosote = new Material.Builder(1507, gregtechId("creosote"))
                .liquid(new FluidBuilder().customStill())
                .color(0x804000)
                .flags(STICKY).build();

        Diesel = new Material.Builder(1508, gregtechId("diesel"))
                .liquid(new FluidBuilder().customStill().alternativeName("fuel"))
                .color(0xFCF404)
                .flags(FLAMMABLE, EXPLOSIVE).build();

        RocketFuel = new Material.Builder(1509, gregtechId("rocket_fuel"))
                .fluid().flags(FLAMMABLE, EXPLOSIVE).color(0xBDB78C).build();

        Glue = new Material.Builder(1510, gregtechId("glue"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFCCC8C).flags(STICKY).build();

        Lubricant = new Material.Builder(1511, gregtechId("lubricant"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFEEDAF).build();

        McGuffium239 = new Material.Builder(1512, gregtechId("mc_guffium_239"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFC04FC).build();

        IndiumConcentrate = new Material.Builder(1513, gregtechId("indium_concentrate"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x0E2950).build();

        SeedOil = new Material.Builder(1514, gregtechId("seed_oil"))
                .liquid(new FluidBuilder().customStill().alternativeName("seed.oil"))
                .color(0xE4FC8C)
                .flags(STICKY, FLAMMABLE).build();

        DrillingFluid = new Material.Builder(1515, gregtechId("drilling_fluid"))
                .fluid().color(0xFFFFAA).build();

        ConstructionFoam = new Material.Builder(1516, gregtechId("construction_foam"))
                .fluid().color(0x808080).build();

        // Free IDs 1517-1521

        SulfuricHeavyFuel = new Material.Builder(1522, gregtechId("sulfuric_heavy_fuel"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFCEC94)
                .flags(FLAMMABLE).build();

        HeavyFuel = new Material.Builder(1523, gregtechId("heavy_fuel"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFCECAC)
                .flags(FLAMMABLE).build();

        LightlyHydroCrackedHeavyFuel = new Material.Builder(1524, gregtechId("lightly_hydrocracked_heavy_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xD4C494)
                .flags(FLAMMABLE)
                .build();

        SeverelyHydroCrackedHeavyFuel = new Material.Builder(1525, gregtechId("severely_hydrocracked_heavy_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBCAC84)
                .flags(FLAMMABLE)
                .build();

        LightlySteamCrackedHeavyFuel = new Material.Builder(1526, gregtechId("lightly_steamcracked_heavy_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFCDC7C)
                .flags(FLAMMABLE)
                .build();

        SeverelySteamCrackedHeavyFuel = new Material.Builder(1527, gregtechId("severely_steamcracked_heavy_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFCFCD4)
                .flags(FLAMMABLE)
                .build();

        SulfuricLightFuel = new Material.Builder(1528, gregtechId("sulfuric_light_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFCCC04)
                .flags(FLAMMABLE).build();

        LightFuel = new Material.Builder(1529, gregtechId("light_fuel"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFCF404)
                .flags(FLAMMABLE).build();

        LightlyHydroCrackedLightFuel = new Material.Builder(1530, gregtechId("lightly_hydrocracked_light_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xA49C04)
                .flags(FLAMMABLE)
                .build();

        SeverelyHydroCrackedLightFuel = new Material.Builder(1531, gregtechId("severely_hydrocracked_light_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0x847C04)
                .flags(FLAMMABLE)
                .build();

        LightlySteamCrackedLightFuel = new Material.Builder(1532, gregtechId("lightly_steamcracked_light_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFCFC0C)
                .flags(FLAMMABLE)
                .build();

        SeverelySteamCrackedLightFuel = new Material.Builder(1533, gregtechId("severely_steamcracked_light_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFCFC2C)
                .flags(FLAMMABLE)
                .build();

        SulfuricNaphtha = new Material.Builder(1534, gregtechId("sulfuric_naphtha"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFCF404)
                .flags(FLAMMABLE)
                .build();

        Naphtha = new Material.Builder(1535, gregtechId("naphtha"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFFF404)
                .flags(FLAMMABLE)
                .build();

        LightlyHydroCrackedNaphtha = new Material.Builder(1536, gregtechId("lightly_hydrocracked_naphtha"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xD4C404)
                .flags(FLAMMABLE)
                .build();

        SeverelyHydroCrackedNaphtha = new Material.Builder(1537, gregtechId("severely_hydrocracked_naphtha"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xDCD40C)
                .flags(FLAMMABLE)
                .build();

        LightlySteamCrackedNaphtha = new Material.Builder(1538, gregtechId("lightly_steamcracked_naphtha"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608)
                .flags(FLAMMABLE)
                .build();

        SeverelySteamCrackedNaphtha = new Material.Builder(1539, gregtechId("severely_steamcracked_naphtha"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xCCC434)
                .flags(FLAMMABLE)
                .build();

        SulfuricGas = new Material.Builder(1540, gregtechId("sulfuric_gas"))
                .gas(new FluidBuilder().customStill())
                .color(0xECDCCC).build();

        RefineryGas = new Material.Builder(1541, gregtechId("refinery_gas"))
                .gas(new FluidBuilder().customStill())
                .color(0xB4B4B4)
                .flags(FLAMMABLE).build();

        LightlyHydroCrackedGas = new Material.Builder(1542, gregtechId("lightly_hydrocracked_gas"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xA0A0A0)
                .flags(FLAMMABLE)
                .build();

        SeverelyHydroCrackedGas = new Material.Builder(1543, gregtechId("severely_hydrocracked_gas"))
                .gas(new FluidBuilder().temperature(775))
                .color(0x919191)
                .flags(FLAMMABLE)
                .build();

        LightlySteamCrackedGas = new Material.Builder(1544, gregtechId("lightly_steamcracked_gas"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xC8C8C8)
                .flags(FLAMMABLE)
                .build();

        SeverelySteamCrackedGas = new Material.Builder(1545, gregtechId("severely_steamcracked_gas"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xE0E0E0)
                .flags(FLAMMABLE)
                .build();

        HydroCrackedEthane = new Material.Builder(1546, gregtechId("hydrocracked_ethane"))
                .gas(new FluidBuilder().temperature(775))
                .color(0x9696BC)
                .flags(FLAMMABLE)
                .build();

        HydroCrackedEthylene = new Material.Builder(1547, gregtechId("hydrocracked_ethylene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xA3A3A0)
                .flags(FLAMMABLE)
                .build();

        HydroCrackedPropene = new Material.Builder(1548, gregtechId("hydrocracked_propene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540)
                .flags(FLAMMABLE)
                .build();

        HydroCrackedPropane = new Material.Builder(1549, gregtechId("hydrocracked_propane"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540)
                .flags(FLAMMABLE)
                .build();

        HydroCrackedButane = new Material.Builder(1550, gregtechId("hydrocracked_butane"))
                .gas(new FluidBuilder().temperature(775))
                .color(0x852C18)
                .flags(FLAMMABLE)
                .build();

        HydroCrackedButene = new Material.Builder(1551, gregtechId("hydrocracked_butene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0x993E05)
                .flags(FLAMMABLE)
                .build();

        HydroCrackedButadiene = new Material.Builder(1552, gregtechId("hydrocracked_butadiene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xAD5203)
                .flags(FLAMMABLE)
                .build();

        SteamCrackedEthane = new Material.Builder(1553, gregtechId("steamcracked_ethane"))
                .gas(new FluidBuilder().temperature(775))
                .color(0x9696BC)
                .flags(FLAMMABLE)
                .build();

        SteamCrackedEthylene = new Material.Builder(1554, gregtechId("steamcracked_ethylene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xA3A3A0)
                .flags(FLAMMABLE)
                .build();

        SteamCrackedPropene = new Material.Builder(1555, gregtechId("steamcracked_propene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540)
                .flags(FLAMMABLE)
                .build();

        SteamCrackedPropane = new Material.Builder(1556, gregtechId("steamcracked_propane"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540)
                .flags(FLAMMABLE)
                .build();

        SteamCrackedButane = new Material.Builder(1557, gregtechId("steamcracked_butane"))
                .gas(new FluidBuilder().temperature(775))
                .color(0x852C18)
                .flags(FLAMMABLE)
                .build();

        SteamCrackedButene = new Material.Builder(1558, gregtechId("steamcracked_butene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0x993E05)
                .flags(FLAMMABLE)
                .build();

        SteamCrackedButadiene = new Material.Builder(1559, gregtechId("steamcracked_butadiene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xAD5203)
                .flags(FLAMMABLE)
                .build();

        Corium = new Material.Builder(1560, gregtechId("corium"))
                .liquid(new FluidBuilder().temperature(2500).block().density(8.0D).viscosity(10000))
                .color(0x7A6B50)
                .iconSet(DULL)
                .flags(NO_UNIFICATION, STICKY, GLOWING)
                .build();

        // Free IDs 1561-1575

        LPG = new Material.Builder(1576, gregtechId("lpg"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFCFCAC)
                .flags(FLAMMABLE, EXPLOSIVE).build();

        RawGrowthMedium = new Material.Builder(1577, gregtechId("raw_growth_medium"))
                .fluid().color(0xA47351).build();

        SterileGrowthMedium = new Material.Builder(1578, gregtechId("sterilized_growth_medium"))
                .fluid().color(0xAC876E).build();

        Oil = new Material.Builder(1579, gregtechId("oil"))
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .build();

        OilHeavy = new Material.Builder(1580, gregtechId("oil_heavy"))
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .build();

        RawOil = new Material.Builder(1581, gregtechId("oil_medium"))
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .build();

        OilLight = new Material.Builder(1582, gregtechId("oil_light"))
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .build();

        NaturalGas = new Material.Builder(1583, gregtechId("natural_gas"))
                .gas(new FluidBuilder().block().customStill())
                .color(0xDCDCDC)
                .flags(FLAMMABLE, EXPLOSIVE).build();

        Bacteria = new Material.Builder(1584, gregtechId("bacteria"))
                .fluid().color(0x808000).build();

        BacterialSludge = new Material.Builder(1585, gregtechId("bacterial_sludge"))
                .fluid().color(0x355E3B).build();

        EnrichedBacterialSludge = new Material.Builder(1586, gregtechId("enriched_bacterial_sludge"))
                .fluid().color(0x7FFF00).build();

        // free id: 1587

        Mutagen = new Material.Builder(1588, gregtechId("mutagen"))
                .fluid().color(0x00FF7F).build();

        GelatinMixture = new Material.Builder(1589, gregtechId("gelatin_mixture"))
                .fluid().color(0x588BAE).build();

        RawGasoline = new Material.Builder(1590, gregtechId("raw_gasoline"))
                .fluid().color(0xFF6400).flags(FLAMMABLE).build();

        Gasoline = new Material.Builder(1591, gregtechId("gasoline"))
                .fluid().color(0xFAA500).flags(FLAMMABLE, EXPLOSIVE).build();

        HighOctaneGasoline = new Material.Builder(1592, gregtechId("gasoline_premium"))
                .fluid().color(0xFFA500).flags(FLAMMABLE, EXPLOSIVE).build();

        // free id: 1593

        CoalGas = new Material.Builder(1594, gregtechId("coal_gas"))
                .gas().color(0x333333).build();

        CoalTar = new Material.Builder(1595, gregtechId("coal_tar"))
                .fluid().color(0x1A1A1A).flags(STICKY, FLAMMABLE).build();

        Gunpowder = new Material.Builder(1596, gregtechId("gunpowder"))
                .dust(0)
                .color(0x808080).iconSet(ROUGH)
                .flags(FLAMMABLE, EXPLOSIVE, NO_SMELTING, NO_SMASHING)
                .build();

        Oilsands = new Material.Builder(1597, gregtechId("oilsands"))
                .dust(1).ore()
                .color(0x0A0A0A).iconSet(SAND)
                .flags(FLAMMABLE)
                .build();

        RareEarth = new Material.Builder(1598, gregtechId("rare_earth"))
                .dust(0)
                .color(0x808064).iconSet(FINE)
                .build();

        Stone = new Material.Builder(1599, gregtechId("stone"))
                .dust(2)
                .color(0xCDCDCD).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, GENERATE_GEAR, NO_SMASHING, NO_SMELTING)
                .build();

        Lava = new Material.Builder(1600, gregtechId("lava"))
                .fluid(FluidRegistry.LAVA, FluidStorageKeys.LIQUID, FluidState.LIQUID)
                .color(0xFF4000)
                .flags(GLOWING)
                .build();

        Glowstone = new Material.Builder(1601, gregtechId("glowstone"))
                .dust(1)
                .liquid(new FluidBuilder().temperature(500))
                .color(0xFFFF00).iconSet(SHINY)
                .flags(NO_SMASHING, GENERATE_PLATE, EXCLUDE_PLATE_COMPRESSOR_RECIPE,
                        EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, GLOWING)
                .build();

        NetherStar = new Material.Builder(1602, gregtechId("nether_star"))
                .gem(4)
                .iconSet(NETHERSTAR)
                .flags(NO_SMASHING, NO_SMELTING, GENERATE_LENS)
                .build();

        Endstone = new Material.Builder(1603, gregtechId("endstone"))
                .dust(1)
                .color(0xD9DE9E)
                .flags(NO_SMASHING)
                .build();

        Netherrack = new Material.Builder(1604, gregtechId("netherrack"))
                .dust(1)
                .color(0xC80000)
                .flags(NO_SMASHING, FLAMMABLE)
                .build();

        CetaneBoostedDiesel = new Material.Builder(1605, gregtechId("nitro_fuel"))
                .liquid(new FluidBuilder().customStill())
                .color(0xC8FF00)
                .flags(FLAMMABLE, EXPLOSIVE)
                .build();

        Collagen = new Material.Builder(1606, gregtechId("collagen"))
                .dust(1)
                .color(0x80471C).iconSet(ROUGH)
                .build();

        Gelatin = new Material.Builder(1607, gregtechId("gelatin"))
                .dust(1)
                .color(0x588BAE).iconSet(ROUGH)
                .build();

        Agar = new Material.Builder(1608, gregtechId("agar"))
                .dust(1)
                .color(0x4F7942).iconSet(ROUGH)
                .build();

        // FREE ID 1609

        // FREE ID 1610

        // FREE ID 1611

        // Free ID 1612

        Milk = new Material.Builder(1613, gregtechId("milk"))
                .liquid(new FluidBuilder()
                        .temperature(295)
                        .customStill())
                .color(0xFEFEFE).iconSet(FINE)
                .build();

        Cocoa = new Material.Builder(1614, gregtechId("cocoa"))
                .dust(0)
                .color(0x643200).iconSet(FINE)
                .build();

        Wheat = new Material.Builder(1615, gregtechId("wheat"))
                .dust(0)
                .color(0xFFFFC4).iconSet(FINE)
                .build();

        Meat = new Material.Builder(1616, gregtechId("meat"))
                .dust(1)
                .color(0xC14C4C).iconSet(SAND)
                .build();

        Wood = new Material.Builder(1617, gregtechId("wood"))
                .wood()
                .color(0x896727).iconSet(WOOD)
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_BOLT_SCREW, GENERATE_LONG_ROD, GENERATE_GEAR,
                        GENERATE_FRAME)
                .fluidPipeProperties(340, 5, false)
                .build();

        Paper = new Material.Builder(1618, gregtechId("paper"))
                .dust(0)
                .color(0xFAFAFA).iconSet(FINE)
                .flags(GENERATE_PLATE, FLAMMABLE, NO_SMELTING, NO_SMASHING,
                        MORTAR_GRINDABLE, EXCLUDE_PLATE_COMPRESSOR_RECIPE)
                .build();

        FishOil = new Material.Builder(1619, gregtechId("fish_oil"))
                .fluid()
                .color(0xDCC15D)
                .flags(STICKY, FLAMMABLE)
                .build();

        RubySlurry = new Material.Builder(1620, gregtechId("ruby_slurry"))
                .fluid().color(0xff6464).build();

        SapphireSlurry = new Material.Builder(1621, gregtechId("sapphire_slurry"))
                .fluid().color(0x6464c8).build();

        GreenSapphireSlurry = new Material.Builder(1622, gregtechId("green_sapphire_slurry"))
                .fluid().color(0x64c882).build();

        // These colors are much nicer looking than those in MC's EnumDyeColor
        DyeBlack = new Material.Builder(1623, gregtechId("dye_black"))
                .fluid().color(0x202020).build();

        DyeRed = new Material.Builder(1624, gregtechId("dye_red"))
                .fluid().color(0xFF0000).build();

        DyeGreen = new Material.Builder(1625, gregtechId("dye_green"))
                .fluid().color(0x00FF00).build();

        DyeBrown = new Material.Builder(1626, gregtechId("dye_brown"))
                .fluid().color(0x604000).build();

        DyeBlue = new Material.Builder(1627, gregtechId("dye_blue"))
                .fluid().color(0x0020FF).build();

        DyePurple = new Material.Builder(1628, gregtechId("dye_purple"))
                .fluid().color(0x800080).build();

        DyeCyan = new Material.Builder(1629, gregtechId("dye_cyan"))
                .fluid().color(0x00FFFF).build();

        DyeLightGray = new Material.Builder(1630, gregtechId("dye_light_gray"))
                .fluid().color(0xC0C0C0).build();

        DyeGray = new Material.Builder(1631, gregtechId("dye_gray"))
                .fluid().color(0x808080).build();

        DyePink = new Material.Builder(1632, gregtechId("dye_pink"))
                .fluid().color(0xFFC0C0).build();

        DyeLime = new Material.Builder(1633, gregtechId("dye_lime"))
                .fluid().color(0x80FF80).build();

        DyeYellow = new Material.Builder(1634, gregtechId("dye_yellow"))
                .fluid().color(0xFFFF00).build();

        DyeLightBlue = new Material.Builder(1635, gregtechId("dye_light_blue"))
                .fluid().color(0x6080FF).build();

        DyeMagenta = new Material.Builder(1636, gregtechId("dye_magenta"))
                .fluid().color(0xFF00FF).build();

        DyeOrange = new Material.Builder(1637, gregtechId("dye_orange"))
                .fluid().color(0xFF8000).build();

        DyeWhite = new Material.Builder(1638, gregtechId("dye_white"))
                .fluid().color(0xFFFFFF).build();

        ImpureEnrichedNaquadahSolution = new Material.Builder(1639, gregtechId("impure_enriched_naquadah_solution"))
                .fluid().color(0x388438).build();

        EnrichedNaquadahSolution = new Material.Builder(1640, gregtechId("enriched_naquadah_solution"))
                .fluid().color(0x3AAD3A).build();

        AcidicEnrichedNaquadahSolution = new Material.Builder(1641, gregtechId("acidic_enriched_naquadah_solution"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x3DD63D).build();

        EnrichedNaquadahWaste = new Material.Builder(1642, gregtechId("enriched_naquadah_waste"))
                .fluid().color(0x355B35).build();

        ImpureNaquadriaSolution = new Material.Builder(1643, gregtechId("impure_naquadria_solution"))
                .fluid().color(0x518451).build();

        NaquadriaSolution = new Material.Builder(1644, gregtechId("naquadria_solution"))
                .fluid().color(0x61AD61).build();

        AcidicNaquadriaSolution = new Material.Builder(1645, gregtechId("acidic_naquadria_solution"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x70D670).build();

        NaquadriaWaste = new Material.Builder(1646, gregtechId("naquadria_waste"))
                .fluid().color(0x425B42).build();

        Lapotron = new Material.Builder(1647, gregtechId("lapotron"))
                .gem()
                .color(0x2C39B1).iconSet(DIAMOND)
                .flags(NO_UNIFICATION)
                .build();

        TreatedWood = new Material.Builder(1648, gregtechId("treated_wood"))
                .wood()
                .color(0x674A28).iconSet(WOOD)
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_FRAME)
                .fluidPipeProperties(340, 10, false)
                .build();

        UUMatter = new Material.Builder(1649, gregtechId("uu_matter"))
                .liquid(new FluidBuilder()
                        .temperature(300)
                        .customStill())
                .color(0x36042C)
                .build();

        PCBCoolant = new Material.Builder(1650, gregtechId("pcb_coolant"))
                .fluid().color(0xD5D69C).build();

        SpentUraniumFuelSolution = new Material.Builder(1651, gregtechId("spent_uranium_fuel_solution"))
                .liquid()
                .color(0x384536).build();

        RadonRichGasMixture = new Material.Builder(1652, gregtechId("radon_rich_gas_mixture"))
                .gas()
                .color(0xd78dd9).build();
    }
}
