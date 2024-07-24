package gregtech.api.unification.material.materials;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.BlastProperty.GasTier;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;
import static gregtech.api.util.GTUtility.gregtechId;

public class ElementMaterials {

    public static void register() {
        Actinium = new Material.Builder(1, gregtechId("actinium"))
                .color(0xC3D1FF).iconSet(METALLIC)
                .element(Elements.Ac)
                .build();

        Aluminium = new Material.Builder(2, gregtechId("aluminium"))
                .ingot()
                .liquid(new FluidBuilder().temperature(933))
                .ore()
                .color(0x80C8F0)
                .flags(EXT2_METAL, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING,
                        GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_DOUBLE_PLATE)
                .element(Elements.Al)
                .toolStats(ToolProperty.Builder.of(6.0F, 7.5F, 768, 2)
                        .enchantability(14).build())
                .rotorStats(10.0f, 2.0f, 128)
                .cableProperties(V[EV], 1, 1)
                .fluidPipeProperties(1166, 100, true)
                .blast(1700, GasTier.LOW)
                .build();

        Americium = new Material.Builder(3, gregtechId("americium"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(1449))
                .plasma()
                .color(0x287869).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_DOUBLE_PLATE)
                .element(Elements.Am)
                .itemPipeProperties(64, 64)
                .build();

        Antimony = new Material.Builder(4, gregtechId("antimony"))
                .ingot()
                .liquid(new FluidBuilder().temperature(904))
                .color(0xDCDCF0).iconSet(SHINY)
                .flags(MORTAR_GRINDABLE)
                .element(Elements.Sb)
                .build();

        Argon = new Material.Builder(5, gregtechId("argon"))
                .gas().plasma()
                .color(0x00FF00)
                .element(Elements.Ar)
                .build();

        Arsenic = new Material.Builder(6, gregtechId("arsenic"))
                .dust()
                .gas(new FluidBuilder().temperature(887))
                .color(0x676756)
                .element(Elements.As)
                .build();

        Astatine = new Material.Builder(7, gregtechId("astatine"))
                .color(0x241A24)
                .element(Elements.At)
                .build();

        Barium = new Material.Builder(8, gregtechId("barium"))
                .dust()
                .color(0x83824C).iconSet(METALLIC)
                .element(Elements.Ba)
                .build();

        Berkelium = new Material.Builder(9, gregtechId("berkelium"))
                .color(0x645A88).iconSet(METALLIC)
                .element(Elements.Bk)
                .build();

        Beryllium = new Material.Builder(10, gregtechId("beryllium"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1560))
                .ore()
                .color(0x64B464).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_DOUBLE_PLATE)
                .element(Elements.Be)
                .build();

        Bismuth = new Material.Builder(11, gregtechId("bismuth"))
                .ingot(1)
                .liquid(new FluidBuilder().temperature(545))
                .color(0x64A0A0).iconSet(METALLIC)
                .element(Elements.Bi)
                .build();

        Bohrium = new Material.Builder(12, gregtechId("bohrium"))
                .color(0xDC57FF).iconSet(SHINY)
                .element(Elements.Bh)
                .build();

        Boron = new Material.Builder(13, gregtechId("boron"))
                .dust()
                .color(0xD2FAD2)
                .element(Elements.B)
                .build();

        Bromine = new Material.Builder(14, gregtechId("bromine"))
                .color(0x500A0A).iconSet(SHINY)
                .element(Elements.Br)
                .build();

        Caesium = new Material.Builder(15, gregtechId("caesium"))
                .dust()
                .color(0x80620B).iconSet(METALLIC)
                .element(Elements.Cs)
                .build();

        Calcium = new Material.Builder(16, gregtechId("calcium"))
                .dust()
                .color(0xFFF5DE).iconSet(METALLIC)
                .element(Elements.Ca)
                .build();

        Californium = new Material.Builder(17, gregtechId("californium"))
                .color(0xA85A12).iconSet(METALLIC)
                .element(Elements.Cf)
                .build();

        Carbon = new Material.Builder(18, gregtechId("carbon"))
                .dust()
                .liquid(new FluidBuilder().temperature(4600))
                .color(0x141414)
                .element(Elements.C)
                .build();

        Cadmium = new Material.Builder(19, gregtechId("cadmium"))
                .dust()
                .color(0x32323C).iconSet(SHINY)
                .element(Elements.Cd)
                .build();

        Cerium = new Material.Builder(20, gregtechId("cerium"))
                .dust()
                .liquid(new FluidBuilder().temperature(1068))
                .color(0x87917D).iconSet(METALLIC)
                .element(Elements.Ce)
                .build();

        Chlorine = new Material.Builder(21, gregtechId("chlorine"))
                .gas(new FluidBuilder().customStill())
                .color(0x2D8C8C)
                .element(Elements.Cl)
                .build();

        Chrome = new Material.Builder(22, gregtechId("chrome"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(2180))
                .color(0xEAC4D8).iconSet(SHINY)
                .flags(EXT_METAL, GENERATE_ROTOR, GENERATE_DOUBLE_PLATE)
                .element(Elements.Cr)
                .rotorStats(12.0f, 3.0f, 512)
                .fluidPipeProperties(2180, 35, true, true, false, false)
                .blast(1700, GasTier.LOW)
                .build();

        Cobalt = new Material.Builder(23, gregtechId("cobalt"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1768))
                .ore() // leave for TiCon ore processing
                .color(0x5050FA).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_DOUBLE_PLATE, GENERATE_FINE_WIRE)
                .element(Elements.Co)
                .cableProperties(V[LV], 2, 2)
                .itemPipeProperties(2560, 2.0f)
                .build();

        Copernicium = new Material.Builder(24, gregtechId("copernicium"))
                .color(0xFFFEFF)
                .element(Elements.Cn)
                .build();

        Copper = new Material.Builder(25, gregtechId("copper"))
                .ingot(1)
                .liquid(new FluidBuilder().temperature(1358))
                .ore()
                .color(0xFF6400).iconSet(SHINY)
                .flags(EXT_METAL, MORTAR_GRINDABLE, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE,
                        GENERATE_DOUBLE_PLATE)
                .element(Elements.Cu)
                .cableProperties(V[MV], 1, 2)
                .fluidPipeProperties(1696, 6, true)
                .build();

        Curium = new Material.Builder(26, gregtechId("curium"))
                .color(0x7B544E).iconSet(METALLIC)
                .element(Elements.Cm)
                .build();

        Darmstadtium = new Material.Builder(27, gregtechId("darmstadtium"))
                .ingot().fluid()
                .color(0x578062)
                .flags(EXT2_METAL, GENERATE_DOUBLE_PLATE, GENERATE_ROTOR, GENERATE_DENSE, GENERATE_SMALL_GEAR)
                .element(Elements.Ds)
                .build();

        Deuterium = new Material.Builder(28, gregtechId("deuterium"))
                .gas(new FluidBuilder().customStill())
                .color(0xFCFC84)
                .element(Elements.D)
                .build();

        Dubnium = new Material.Builder(29, gregtechId("dubnium"))
                .color(0xD3FDFF).iconSet(SHINY)
                .element(Elements.Db)
                .build();

        Dysprosium = new Material.Builder(30, gregtechId("dysprosium"))
                .iconSet(METALLIC)
                .element(Elements.Dy)
                .build();

        Einsteinium = new Material.Builder(31, gregtechId("einsteinium"))
                .color(0xCE9F00).iconSet(METALLIC)
                .element(Elements.Es)
                .build();

        Erbium = new Material.Builder(32, gregtechId("erbium"))
                .iconSet(METALLIC)
                .element(Elements.Er)
                .build();

        Europium = new Material.Builder(33, gregtechId("europium"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1099))
                .color(0x20FFFF).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_LONG_ROD, GENERATE_FINE_WIRE, GENERATE_SPRING, GENERATE_FOIL, GENERATE_FRAME,
                        GENERATE_DOUBLE_PLATE)
                .element(Elements.Eu)
                .cableProperties(GTValues.V[GTValues.UHV], 2, 32)
                .fluidPipeProperties(7750, 300, true)
                .blast(b -> b
                        .temp(6000, GasTier.MID)
                        .blastStats(VA[IV], 180)
                        .vacuumStats(VA[HV]))
                .build();

        Fermium = new Material.Builder(34, gregtechId("fermium"))
                .color(0x984ACF).iconSet(METALLIC)
                .element(Elements.Fm)
                .build();

        Flerovium = new Material.Builder(35, gregtechId("flerovium"))
                .iconSet(SHINY)
                .element(Elements.Fl)
                .build();

        Fluorine = new Material.Builder(36, gregtechId("fluorine"))
                .gas(new FluidBuilder().customStill())
                .color(0x6EA7DC)
                .element(Elements.F)
                .build();

        Francium = new Material.Builder(37, gregtechId("francium"))
                .color(0xAAAAAA).iconSet(SHINY)
                .element(Elements.Fr)
                .build();

        Gadolinium = new Material.Builder(38, gregtechId("gadolinium"))
                .color(0xDDDDFF).iconSet(METALLIC)
                .element(Elements.Gd)
                .build();

        Gallium = new Material.Builder(39, gregtechId("gallium"))
                .ingot()
                .liquid(new FluidBuilder().temperature(303))
                .color(0xDCDCFF).iconSet(SHINY)
                .flags(STD_METAL, GENERATE_FOIL)
                .element(Elements.Ga)
                .build();

        Germanium = new Material.Builder(40, gregtechId("germanium"))
                .color(0x434343).iconSet(SHINY)
                .element(Elements.Ge)
                .build();

        Gold = new Material.Builder(41, gregtechId("gold"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1337))
                .ore()
                .color(0xFFE650).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_RING, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FINE_WIRE, GENERATE_FOIL,
                        GENERATE_DOUBLE_PLATE)
                .element(Elements.Au)
                .cableProperties(V[HV], 3, 2)
                .fluidPipeProperties(1671, 25, true, true, false, false)
                .build();

        Hafnium = new Material.Builder(42, gregtechId("hafnium"))
                .ingot()
                .color(0x99999A).iconSet(SHINY)
                .flags(GENERATE_LONG_ROD)
                .blast(2227)
                .element(Elements.Hf)
                .build();

        Hassium = new Material.Builder(43, gregtechId("hassium"))
                .color(0xDDDDDD)
                .element(Elements.Hs)
                .build();

        Holmium = new Material.Builder(44, gregtechId("holmium"))
                .iconSet(METALLIC)
                .element(Elements.Ho)
                .build();

        Hydrogen = new Material.Builder(45, gregtechId("hydrogen"))
                .gas()
                .color(0x0000B5)
                .element(Elements.H)
                .build();

        Helium = new Material.Builder(46, gregtechId("helium"))
                .gas(new FluidBuilder().customStill())
                .plasma(new FluidBuilder().customStill())
                .liquid(new FluidBuilder()
                        .temperature(4)
                        .color(0xFCFF90)
                        .name("liquid_helium")
                        .translation("gregtech.fluid.liquid_generic"))
                .color(0xFCFC94)
                .element(Elements.He)
                .build();
        Helium.getProperty(PropertyKey.FLUID).setPrimaryKey(FluidStorageKeys.GAS);

        Helium3 = new Material.Builder(47, gregtechId("helium_3"))
                .gas(new FluidBuilder()
                        .customStill()
                        .translation("gregtech.fluid.generic"))
                .color(0xFCFCCC)
                .element(Elements.He3)
                .build();

        Indium = new Material.Builder(48, gregtechId("indium"))
                .ingot()
                .liquid(new FluidBuilder().temperature(430))
                .color(0x400080).iconSet(SHINY)
                .element(Elements.In)
                .build();

        Iodine = new Material.Builder(49, gregtechId("iodine"))
                .color(0x2C344F).iconSet(SHINY)
                .element(Elements.I)
                .build();

        Iridium = new Material.Builder(50, gregtechId("iridium"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(2719))
                .color(0xA1E4E4).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_DOUBLE_PLATE, GENERATE_FINE_WIRE, GENERATE_GEAR, GENERATE_FRAME)
                .element(Elements.Ir)
                .rotorStats(7.0f, 3.0f, 2560)
                .fluidPipeProperties(3398, 250, true, false, true, false)
                .blast(b -> b
                        .temp(4500, GasTier.HIGH)
                        .blastStats(VA[IV], 1100)
                        .vacuumStats(VA[EV], 250))
                .build();

        Iron = new Material.Builder(51, gregtechId("iron"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1811))
                .plasma()
                .ore()
                .color(0xC8C8C8).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_GEAR,
                        GENERATE_SPRING_SMALL, GENERATE_SPRING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        BLAST_FURNACE_CALCITE_TRIPLE)
                .element(Elements.Fe)
                .toolStats(ToolProperty.Builder.of(2.0F, 2.0F, 256, 2)
                        .enchantability(14).build())
                .rotorStats(7.0f, 2.5f, 256)
                .cableProperties(V[MV], 2, 3)
                .build();

        Krypton = new Material.Builder(52, gregtechId("krypton"))
                .gas(new FluidBuilder()
                        .customStill())
                .color(0x80FF80)
                .element(Elements.Kr)
                .build();

        Lanthanum = new Material.Builder(53, gregtechId("lanthanum"))
                .dust()
                .liquid(new FluidBuilder().temperature(1193))
                .color(0x5D7575).iconSet(METALLIC)
                .element(Elements.La)
                .build();

        Lawrencium = new Material.Builder(54, gregtechId("lawrencium"))
                .iconSet(METALLIC)
                .element(Elements.Lr)
                .build();

        Lead = new Material.Builder(55, gregtechId("lead"))
                .ingot(1)
                .liquid(new FluidBuilder().temperature(600))
                .ore()
                .color(0x8C648C)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SPRING, GENERATE_SPRING_SMALL,
                        GENERATE_FINE_WIRE, GENERATE_DOUBLE_PLATE)
                .element(Elements.Pb)
                .cableProperties(V[ULV], 2, 2)
                .fluidPipeProperties(1200, 32, true)
                .build();

        Lithium = new Material.Builder(56, gregtechId("lithium"))
                .dust()
                .liquid(new FluidBuilder().temperature(454))
                .ore()
                .color(0xBDC7DB)
                .element(Elements.Li)
                .build();

        Livermorium = new Material.Builder(57, gregtechId("livermorium"))
                .color(0xAAAAAA).iconSet(SHINY)
                .element(Elements.Lv)
                .build();

        Lutetium = new Material.Builder(58, gregtechId("lutetium"))
                .dust()
                .liquid(new FluidBuilder().temperature(1925))
                .color(0x00AAFF).iconSet(METALLIC)
                .element(Elements.Lu)
                .build();

        Magnesium = new Material.Builder(59, gregtechId("magnesium"))
                .dust()
                .liquid(new FluidBuilder().temperature(923))
                .color(0xFFC8C8).iconSet(METALLIC)
                .element(Elements.Mg)
                .build();

        Mendelevium = new Material.Builder(60, gregtechId("mendelevium"))
                .color(0x1D4ACF).iconSet(METALLIC)
                .element(Elements.Md)
                .build();

        Manganese = new Material.Builder(61, gregtechId("manganese"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1519))
                .color(0xCDE1B9)
                .flags(STD_METAL, GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .element(Elements.Mn)
                .rotorStats(7.0f, 2.0f, 512)
                .build();

        Meitnerium = new Material.Builder(62, gregtechId("meitnerium"))
                .color(0x2246BE).iconSet(SHINY)
                .element(Elements.Mt)
                .build();

        Mercury = new Material.Builder(63, gregtechId("mercury"))
                .fluid()
                .color(0xE6DCDC).iconSet(DULL)
                .element(Elements.Hg)
                .build();

        Molybdenum = new Material.Builder(64, gregtechId("molybdenum"))
                .ingot()
                .liquid(new FluidBuilder().temperature(2896))
                .ore()
                .color(0xB4B4DC).iconSet(SHINY)
                .element(Elements.Mo)
                .flags(GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .rotorStats(7.0f, 2.0f, 512)
                .build();

        Moscovium = new Material.Builder(65, gregtechId("moscovium"))
                .color(0x7854AD).iconSet(SHINY)
                .element(Elements.Mc)
                .build();

        Neodymium = new Material.Builder(66, gregtechId("neodymium"))
                .ingot().fluid().ore()
                .color(0x646464).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_ROD, GENERATE_BOLT_SCREW)
                .element(Elements.Nd)
                .rotorStats(7.0f, 2.0f, 512)
                .blast(1297, GasTier.MID)
                .build();

        Neon = new Material.Builder(67, gregtechId("neon"))
                .gas()
                .color(0xFAB4B4)
                .element(Elements.Ne)
                .build();

        Neptunium = new Material.Builder(68, gregtechId("neptunium"))
                .color(0x284D7B).iconSet(METALLIC)
                .element(Elements.Np)
                .build();

        Nickel = new Material.Builder(69, gregtechId("nickel"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1728))
                .plasma()
                .ore()
                .color(0xC8C8FA).iconSet(METALLIC)
                .flags(STD_METAL, MORTAR_GRINDABLE, GENERATE_DOUBLE_PLATE)
                .element(Elements.Ni)
                .cableProperties(GTValues.V[GTValues.LV], 3, 3)
                .itemPipeProperties(2048, 1.0f)
                .build();

        Nihonium = new Material.Builder(70, gregtechId("nihonium"))
                .color(0x08269E).iconSet(SHINY)
                .element(Elements.Nh)
                .build();

        Niobium = new Material.Builder(71, gregtechId("niobium"))
                .ingot().fluid()
                .color(0xBEB4C8).iconSet(METALLIC)
                .element(Elements.Nb)
                .blast(b -> b
                        .temp(2750, GasTier.MID)
                        .blastStats(VA[HV], 900))
                .build();

        Nitrogen = new Material.Builder(72, gregtechId("nitrogen"))
                .gas().plasma()
                .color(0x00BFC1)
                .element(Elements.N)
                .build();

        Nobelium = new Material.Builder(73, gregtechId("nobelium"))
                .iconSet(SHINY)
                .element(Elements.No)
                .build();

        Oganesson = new Material.Builder(74, gregtechId("oganesson"))
                .color(0x142D64).iconSet(METALLIC)
                .element(Elements.Og)
                .build();

        Osmium = new Material.Builder(75, gregtechId("osmium"))
                .ingot(4)
                .liquid(new FluidBuilder().temperature(3306))
                .color(0x3232FF).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_FOIL, GENERATE_DOUBLE_PLATE)
                .element(Elements.Os)
                .rotorStats(16.0f, 4.0f, 1280)
                .cableProperties(V[LuV], 4, 2)
                .itemPipeProperties(256, 8.0f)
                .blast(b -> b
                        .temp(4500, GasTier.HIGH)
                        .blastStats(VA[LuV], 1000)
                        .vacuumStats(VA[EV], 300))
                .build();

        Oxygen = new Material.Builder(76, gregtechId("oxygen"))
                .gas()
                .liquid(new FluidBuilder()
                        .temperature(85)
                        .color(0x6688DD)
                        .name("liquid_oxygen")
                        .translation("gregtech.fluid.liquid_generic"))
                .plasma()
                .color(0x4CC3FF)
                .element(Elements.O)
                .build();
        Oxygen.getProperty(PropertyKey.FLUID).setPrimaryKey(FluidStorageKeys.GAS);

        Palladium = new Material.Builder(77, gregtechId("palladium"))
                .ingot().fluid().ore()
                .color(0x808080).iconSet(SHINY)
                .flags(EXT_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE)
                .element(Elements.Pd)
                .blast(b -> b
                        .temp(1828, GasTier.LOW)
                        .blastStats(VA[HV], 900)
                        .vacuumStats(VA[HV], 150))
                .build();

        Phosphorus = new Material.Builder(78, gregtechId("phosphorus"))
                .dust()
                .color(0xFFFF00)
                .element(Elements.P)
                .build();

        Polonium = new Material.Builder(79, gregtechId("polonium"))
                .color(0xC9D47E)
                .element(Elements.Po)
                .build();

        Platinum = new Material.Builder(80, gregtechId("platinum"))
                .ingot()
                .liquid(new FluidBuilder().temperature(2041))
                .ore()
                .color(0xFFFFC8).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_RING, GENERATE_DOUBLE_PLATE)
                .element(Elements.Pt)
                .cableProperties(V[IV], 2, 1)
                .itemPipeProperties(512, 4.0f)
                .build();

        Plutonium239 = new Material.Builder(81, gregtechId("plutonium_239"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(METALLIC)
                .element(Elements.Pu239)
                .build();

        Plutonium241 = new Material.Builder(82, gregtechId("plutonium_241"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(913))
                .color(0xFA4646).iconSet(SHINY)
                .flags(EXT_METAL, GENERATE_DOUBLE_PLATE)
                .element(Elements.Pu241)
                .build();

        Potassium = new Material.Builder(83, gregtechId("potassium"))
                .dust(1)
                .liquid(new FluidBuilder().temperature(337))
                .color(0xBEDCFF).iconSet(METALLIC)
                .element(Elements.K)
                .build();

        Praseodymium = new Material.Builder(84, gregtechId("praseodymium"))
                .color(0xCECECE).iconSet(METALLIC)
                .element(Elements.Pr)
                .build();

        Promethium = new Material.Builder(85, gregtechId("promethium"))
                .iconSet(METALLIC)
                .element(Elements.Pm)
                .build();

        Protactinium = new Material.Builder(86, gregtechId("protactinium"))
                .color(0xA78B6D).iconSet(METALLIC)
                .element(Elements.Pa)
                .build();

        Radon = new Material.Builder(87, gregtechId("radon"))
                .gas()
                .color(0xFF39FF)
                .element(Elements.Rn)
                .build();

        Radium = new Material.Builder(88, gregtechId("radium"))
                .color(0xFFFFCD).iconSet(SHINY)
                .element(Elements.Ra)
                .build();

        Rhenium = new Material.Builder(89, gregtechId("rhenium"))
                .color(0xB6BAC3).iconSet(SHINY)
                .element(Elements.Re)
                .build();

        Rhodium = new Material.Builder(90, gregtechId("rhodium"))
                .ingot().fluid()
                .color(0xDC0C58).iconSet(BRIGHT)
                .flags(EXT2_METAL, GENERATE_GEAR, GENERATE_FINE_WIRE)
                .element(Elements.Rh)
                .blast(b -> b
                        .temp(2237, GasTier.MID)
                        .blastStats(VA[EV], 1200)
                        .vacuumStats(VA[HV]))
                .build();

        Roentgenium = new Material.Builder(91, gregtechId("roentgenium"))
                .color(0xE3FDEC).iconSet(SHINY)
                .element(Elements.Rg)
                .build();

        Rubidium = new Material.Builder(92, gregtechId("rubidium"))
                .color(0xF01E1E).iconSet(SHINY)
                .element(Elements.Rb)
                .build();

        Ruthenium = new Material.Builder(93, gregtechId("ruthenium"))
                .ingot().fluid()
                .color(0x50ACCD).iconSet(SHINY)
                .flags(GENERATE_FOIL, GENERATE_GEAR)
                .element(Elements.Ru)
                .blast(b -> b
                        .temp(2607, GasTier.MID)
                        .blastStats(VA[EV], 900)
                        .vacuumStats(VA[HV], 200))
                .build();

        Rutherfordium = new Material.Builder(94, gregtechId("rutherfordium"))
                .color(0xFFF6A1).iconSet(SHINY)
                .element(Elements.Rf)
                .build();

        Samarium = new Material.Builder(95, gregtechId("samarium"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1345))
                .color(0xFFFFCC).iconSet(METALLIC)
                .flags(GENERATE_LONG_ROD)
                .element(Elements.Sm)
                .blast(b -> b
                        .temp(5400, GasTier.HIGH)
                        .blastStats(VA[EV], 1500)
                        .vacuumStats(VA[HV], 200))
                .build();

        Scandium = new Material.Builder(96, gregtechId("scandium"))
                .iconSet(METALLIC)
                .element(Elements.Sc)
                .build();

        Seaborgium = new Material.Builder(97, gregtechId("seaborgium"))
                .color(0x19C5FF).iconSet(SHINY)
                .element(Elements.Sg)
                .build();

        Selenium = new Material.Builder(98, gregtechId("selenium"))
                .color(0xB6BA6B).iconSet(SHINY)
                .element(Elements.Se)
                .build();

        Silicon = new Material.Builder(99, gregtechId("silicon"))
                .ingot().fluid()
                .color(0x3C3C50).iconSet(METALLIC)
                .flags(GENERATE_FOIL)
                .element(Elements.Si)
                .blast(2273) // no gas tier for silicon
                .build();

        Silver = new Material.Builder(100, gregtechId("silver"))
                .ingot()
                .liquid(new FluidBuilder().temperature(1235))
                .ore()
                .color(0xDCDCFF).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_DOUBLE_PLATE, MORTAR_GRINDABLE, GENERATE_FINE_WIRE, GENERATE_RING)
                .element(Elements.Ag)
                .cableProperties(V[HV], 1, 1)
                .build();

        Sodium = new Material.Builder(101, gregtechId("sodium"))
                .dust()
                .color(0x000096).iconSet(METALLIC)
                .element(Elements.Na)
                .build();

        Strontium = new Material.Builder(102, gregtechId("strontium"))
                .color(0xC8C8C8).iconSet(METALLIC)
                .element(Elements.Sr)
                .build();

        Sulfur = new Material.Builder(103, gregtechId("sulfur"))
                .dust().ore()
                .color(0xC8C800)
                .flags(FLAMMABLE)
                .element(Elements.S)
                .build();

        Tantalum = new Material.Builder(104, gregtechId("tantalum"))
                .ingot()
                .liquid(new FluidBuilder().temperature(3290))
                .color(0x69B7FF).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE)
                .element(Elements.Ta)
                .build();

        Technetium = new Material.Builder(105, gregtechId("technetium"))
                .color(0x545455).iconSet(SHINY)
                .element(Elements.Tc)
                .build();

        Tellurium = new Material.Builder(106, gregtechId("tellurium"))
                .iconSet(METALLIC)
                .element(Elements.Te)
                .build();

        Tennessine = new Material.Builder(107, gregtechId("tennessine"))
                .color(0x977FD6).iconSet(SHINY)
                .element(Elements.Ts)
                .build();

        Terbium = new Material.Builder(108, gregtechId("terbium"))
                .iconSet(METALLIC)
                .element(Elements.Tb)
                .build();

        Thorium = new Material.Builder(109, gregtechId("thorium"))
                .ingot()
                .liquid(new FluidBuilder().temperature(2023))
                .ore()
                .color(0x001E00).iconSet(SHINY)
                .flags(STD_METAL, GENERATE_ROD)
                .element(Elements.Th)
                .build();

        Thallium = new Material.Builder(110, gregtechId("thallium"))
                .color(0xC1C1DE).iconSet(SHINY)
                .element(Elements.Tl)
                .build();

        Thulium = new Material.Builder(111, gregtechId("thulium"))
                .iconSet(METALLIC)
                .element(Elements.Tm)
                .build();

        Tin = new Material.Builder(112, gregtechId("tin"))
                .ingot(1)
                .liquid(new FluidBuilder().temperature(505))
                .plasma()
                .ore()
                .color(0xDCDCDC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SPRING, GENERATE_SPRING_SMALL,
                        GENERATE_FINE_WIRE, GENERATE_DOUBLE_PLATE)
                .element(Elements.Sn)
                .cableProperties(V[LV], 1, 1)
                .itemPipeProperties(4096, 0.5f)
                .build();

        Titanium = new Material.Builder(113, gregtechId("titanium")) // todo Ore? Look at EBF recipe here if we do Ti
                                                                     // ores
                .ingot(3).fluid()
                .color(0xDCA0F0).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_DOUBLE_PLATE, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_GEAR,
                        GENERATE_FRAME)
                .element(Elements.Ti)
                .toolStats(ToolProperty.Builder.of(8.0F, 6.0F, 1536, 3)
                        .enchantability(14).build())
                .rotorStats(7.0f, 3.0f, 1600)
                .fluidPipeProperties(2426, 150, true, true, false, false)
                .blast(b -> b
                        .temp(1941, GasTier.MID)
                        .blastStats(VA[HV], 1500)
                        .vacuumStats(VA[HV]))
                .build();

        Tritium = new Material.Builder(114, gregtechId("tritium"))
                .gas(new FluidBuilder().customStill())
                .color(0xFC5C5C)
                .iconSet(METALLIC)
                .element(Elements.T)
                .build();

        Tungsten = new Material.Builder(115, gregtechId("tungsten"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(3695))
                .color(0x323232).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FOIL, GENERATE_GEAR,
                        GENERATE_DOUBLE_PLATE)
                .element(Elements.W)
                .rotorStats(7.0f, 3.0f, 2560)
                .cableProperties(V[IV], 2, 2)
                .fluidPipeProperties(4618, 50, true, true, false, true)
                .blast(b -> b
                        .temp(3600, GasTier.MID)
                        .blastStats(VA[EV], 1800)
                        .vacuumStats(VA[HV], 300))
                .build();

        Uranium = new Material.Builder(116, gregtechId("uranium"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(1405))
                .color(0x32F032).iconSet(METALLIC)
                .flags(EXT_METAL)
                .element(Elements.U)
                .build();

        Uranium235 = new Material.Builder(117, gregtechId("uranium_235"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(1405))
                .color(0x46FA46).iconSet(SHINY)
                .flags(EXT_METAL)
                .element(Elements.U235)
                .build();

        Vanadium = new Material.Builder(118, gregtechId("vanadium"))
                .ingot().fluid()
                .color(0x323232).iconSet(METALLIC)
                .element(Elements.V)
                .blast(2183, GasTier.MID)
                .build();

        Xenon = new Material.Builder(119, gregtechId("xenon"))
                .gas()
                .color(0x00FFFF)
                .element(Elements.Xe)
                .build();

        Ytterbium = new Material.Builder(120, gregtechId("ytterbium"))
                .color(0xA7A7A7).iconSet(METALLIC)
                .element(Elements.Yb)
                .build();

        Yttrium = new Material.Builder(121, gregtechId("yttrium"))
                .ingot().fluid()
                .color(0x76524C).iconSet(METALLIC)
                .element(Elements.Y)
                .blast(1799)
                .build();

        Zinc = new Material.Builder(122, gregtechId("zinc"))
                .ingot(1)
                .liquid(new FluidBuilder().temperature(693))
                .color(0xEBEBFA).iconSet(METALLIC)
                .flags(STD_METAL, MORTAR_GRINDABLE, GENERATE_FOIL, GENERATE_RING, GENERATE_FINE_WIRE)
                .element(Elements.Zn)
                .build();

        Zirconium = new Material.Builder(123, gregtechId("zirconium"))
                .dust()
                .color(0xC8FFFF).iconSet(METALLIC)
                .element(Elements.Zr)
                .build();

        Naquadah = new Material.Builder(124, gregtechId("naquadah"))
                .ingot(4)
                .liquid(new FluidBuilder().customStill())
                .ore()
                .color(0x323232).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FOIL, GENERATE_SPRING, GENERATE_FINE_WIRE, GENERATE_BOLT_SCREW,
                        GENERATE_DOUBLE_PLATE)
                .element(Elements.Nq)
                .rotorStats(6.0f, 4.0f, 1280)
                .cableProperties(V[ZPM], 2, 2)
                .fluidPipeProperties(3776, 200, true, false, true, true)
                .blast(b -> b
                        .temp(5000, GasTier.HIGH)
                        .blastStats(VA[IV], 600)
                        .vacuumStats(VA[EV], 150))
                .build();

        NaquadahEnriched = new Material.Builder(125, gregtechId("naquadah_enriched"))
                .ingot(4)
                .liquid(new FluidBuilder().customStill())
                .color(0x3C3C3C).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FOIL)
                .element(Elements.Nq1)
                .blast(b -> b
                        .temp(7000, GasTier.HIGH)
                        .blastStats(VA[IV], 1000)
                        .vacuumStats(VA[EV], 150))
                .build();

        Naquadria = new Material.Builder(126, gregtechId("naquadria"))
                .ingot(3)
                .liquid(new FluidBuilder().customStill())
                .color(0x1E1E1E).iconSet(SHINY)
                .flags(EXT_METAL, GENERATE_DOUBLE_PLATE, GENERATE_FOIL, GENERATE_GEAR, GENERATE_FINE_WIRE,
                        GENERATE_BOLT_SCREW)
                .element(Elements.Nq2)
                .blast(b -> b
                        .temp(9000, GasTier.HIGH)
                        .blastStats(VA[ZPM], 1200)
                        .vacuumStats(VA[LuV], 200))
                .build();

        Neutronium = new Material.Builder(127, gregtechId("neutronium"))
                .ingot(6)
                .liquid(new FluidBuilder().temperature(100_000))
                .color(0xFAFAFA)
                .flags(EXT_METAL, GENERATE_BOLT_SCREW, GENERATE_FRAME, GENERATE_GEAR, GENERATE_LONG_ROD,
                        GENERATE_DOUBLE_PLATE)
                .element(Elements.Nt)
                .toolStats(ToolProperty.Builder.of(180.0F, 100.0F, 65535, 6)
                        .attackSpeed(0.5F).enchantability(33).magnetic().unbreakable().build())
                .rotorStats(24.0f, 12.0f, 655360)
                .fluidPipeProperties(100_000, 5000, true, true, true, true)
                .build();

        Tritanium = new Material.Builder(128, gregtechId("tritanium"))
                .ingot(6)
                .liquid(new FluidBuilder().temperature(25_000))
                .color(0x600000).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_FRAME, GENERATE_RING, GENERATE_SMALL_GEAR, GENERATE_ROUND, GENERATE_FOIL,
                        GENERATE_FINE_WIRE, GENERATE_GEAR)
                .element(Elements.Tr)
                .cableProperties(V[UV], 1, 8)
                .rotorStats(20.0f, 6.0f, 10240)
                .build();

        Duranium = new Material.Builder(129, gregtechId("duranium"))
                .ingot(5)
                .liquid(new FluidBuilder().temperature(7500))
                .color(0x4BAFAF).iconSet(BRIGHT)
                .flags(EXT_METAL, GENERATE_FOIL, GENERATE_GEAR, GENERATE_DOUBLE_PLATE)
                .element(Elements.Dr)
                .toolStats(ToolProperty.Builder.of(14.0F, 12.0F, 8192, 5)
                        .attackSpeed(0.3F).enchantability(33).magnetic().build())
                .fluidPipeProperties(9625, 500, true, true, true, true)
                .build();

        Trinium = new Material.Builder(130, gregtechId("trinium"))
                .ingot(7).fluid()
                .color(0x9973BD).iconSet(SHINY)
                .flags(GENERATE_FOIL, GENERATE_BOLT_SCREW, GENERATE_GEAR)
                .element(Elements.Ke)
                .cableProperties(V[ZPM], 6, 4)
                .blast(b -> b
                        .temp(7200, GasTier.HIGH)
                        .blastStats(VA[LuV], 1500)
                        .vacuumStats(VA[IV], 300))
                .build();

        Uranium238 = new Material.Builder(131, gregtechId("uranium_238"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(1405))
                .color(0x46FA46).iconSet(SHINY)
                .flags(EXT_METAL)
                .element(Elements.U238)
                .build();

        Uranium239 = new Material.Builder(132, gregtechId("uranium_239"))
                .color(0x46FA46).iconSet(SHINY)
                .element(Elements.U239)
                .build();

        Neptunium235 = new Material.Builder(133, gregtechId("neptunium_235"))
                .color(0x284D7B).iconSet(METALLIC)
                .element(Elements.Np235)
                .build();

        Neptunium236 = new Material.Builder(134, gregtechId("neptunium_236"))
                .color(0x284D7B).iconSet(METALLIC)
                .element(Elements.Np236)
                .build();

        Neptunium237 = new Material.Builder(135, gregtechId("neptunium_237"))
                .color(0x284D7B).iconSet(METALLIC)
                .element(Elements.Np237)
                .build();

        Neptunium239 = new Material.Builder(136, gregtechId("neptunium_239"))
                .color(0x284D7B).iconSet(METALLIC)
                .element(Elements.Np239)
                .build();

        Plutonium238 = new Material.Builder(137, gregtechId("plutonium_238"))
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(METALLIC)
                .element(Elements.Pu238)
                .build();

        Plutonium240 = new Material.Builder(138, gregtechId("plutonium_240"))
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(METALLIC)
                .element(Elements.Pu240)
                .build();

        Plutonium242 = new Material.Builder(139, gregtechId("plutonium_242"))
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(METALLIC)
                .element(Elements.Pu242)
                .build();

        Plutonium244 = new Material.Builder(140, gregtechId("plutonium_244"))
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(METALLIC)
                .element(Elements.Pu244)
                .build();

        Plutonium = new Material.Builder(141, gregtechId("plutonium"))
                .color(0xF03232).iconSet(METALLIC)
                .element(Elements.Pu)
                .build();
    }
}
