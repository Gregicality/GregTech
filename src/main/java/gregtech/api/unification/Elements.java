package gregtech.api.unification;

import gregtech.api.util.GTLog;

import com.google.common.base.CaseFormat;
import crafttweaker.annotations.ZenRegister;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@ZenClass("mods.gregtech.material.Elements")
@ZenRegister
public class Elements {

    private static final Pattern namePattern = Pattern.compile("[A-Z]+[A-Za-z]*(-\\d+)?");

    private static final Map<String, Element> elements = new Object2ObjectOpenHashMap<>();
    private static final Map<String, Element> elementsBySymbol = new Object2ObjectOpenHashMap<>();
    private static final List<Element> elementList = new ArrayList<>();
    private static final List<Element> elementsView = Collections.unmodifiableList(elementList);

    private Elements() {}

    public static final Element H = add(1, 0, "Hydrogen", "H");
    public static final Element D = add(1, 1, -1, "H", "Deuterium", "D", true);
    public static final Element T = add(1, 2, -1, "D", "Tritium", "T", true);
    public static final Element He = add(2, 2, "Helium", "He");
    public static final Element He3 = add(2, 1, -1, "H&D", "Helium-3", "He-3", true);
    public static final Element Li = add(3, 4, "Lithium", "Li");
    public static final Element Be = add(4, 5, "Beryllium", "Be");
    public static final Element B = add(5, 5, "Boron", "B");
    public static final Element C = add(6, 6, "Carbon", "C");
    public static final Element N = add(7, 7, "Nitrogen", "N");
    public static final Element O = add(8, 8, "Oxygen", "O");
    public static final Element F = add(9, 9, "Fluorine", "F");
    public static final Element Ne = add(10, 10, "Neon", "Ne");
    public static final Element Na = add(11, 11, "Sodium", "Na");
    public static final Element Mg = add(12, 12, "Magnesium", "Mg");
    public static final Element Al = add(13, 13, "Aluminium", "Al");
    public static final Element Si = add(14, 14, "Silicon", "Si");
    public static final Element P = add(15, 15, "Phosphorus", "P");
    public static final Element S = add(16, 16, "Sulfur", "S");
    public static final Element Cl = add(17, 18, "Chlorine", "Cl");
    public static final Element Ar = add(18, 22, "Argon", "Ar");
    public static final Element K = add(19, 20, "Potassium", "K");
    public static final Element Ca = add(20, 20, "Calcium", "Ca");
    public static final Element Sc = add(21, 24, "Scandium", "Sc");
    public static final Element Ti = add(22, 26, "Titanium", "Ti");
    public static final Element V = add(23, 28, "Vanadium", "V");
    public static final Element Cr = add(24, 28, "Chrome", "Cr");
    public static final Element Mn = add(25, 30, "Manganese", "Mn");
    public static final Element Fe = add(26, 30, "Iron", "Fe");
    public static final Element Co = add(27, 32, "Cobalt", "Co");
    public static final Element Ni = add(28, 30, "Nickel", "Ni");
    public static final Element Cu = add(29, 34, "Copper", "Cu");
    public static final Element Zn = add(30, 35, "Zinc", "Zn");
    public static final Element Ga = add(31, 39, "Gallium", "Ga");
    public static final Element Ge = add(32, 40, "Germanium", "Ge");
    public static final Element As = add(33, 42, "Arsenic", "As");
    public static final Element Se = add(34, 45, "Selenium", "Se");
    public static final Element Br = add(35, 45, "Bromine", "Br");
    public static final Element Kr = add(36, 48, "Krypton", "Kr");
    public static final Element Rb = add(37, 48, "Rubidium", "Rb");
    public static final Element Sr = add(38, 49, "Strontium", "Sr");
    public static final Element Y = add(39, 50, "Yttrium", "Y");
    public static final Element Zr = add(40, 51, "Zirconium", "Zr");
    public static final Element Nb = add(41, 53, "Niobium", "Nb");
    public static final Element Mo = add(42, 53, "Molybdenum", "Mo");
    public static final Element Tc = add(43, 55, "Technetium", "Tc");
    public static final Element Ru = add(44, 57, "Ruthenium", "Ru");
    public static final Element Rh = add(45, 58, "Rhodium", "Rh");
    public static final Element Pd = add(46, 60, "Palladium", "Pd");
    public static final Element Ag = add(47, 60, "Silver", "Ag");
    public static final Element Cd = add(48, 64, "Cadmium", "Cd");
    public static final Element In = add(49, 65, "Indium", "In");
    public static final Element Sn = add(50, 68, "Tin", "Sn");
    public static final Element Sb = add(51, 70, "Antimony", "Sb");
    public static final Element Te = add(52, 75, "Tellurium", "Te");
    public static final Element I = add(53, 74, "Iodine", "I");
    public static final Element Xe = add(54, 77, "Xenon", "Xe");
    public static final Element Cs = add(55, 77, "Caesium", "Cs");
    public static final Element Ba = add(56, 81, "Barium", "Ba");
    public static final Element La = add(57, 81, "Lanthanum", "La");
    public static final Element Ce = add(58, 82, "Cerium", "Ce");
    public static final Element Pr = add(59, 81, "Praseodymium", "Pr");
    public static final Element Nd = add(60, 84, "Neodymium", "Nd");
    public static final Element Pm = add(61, 83, "Promethium", "Pm");
    public static final Element Sm = add(62, 88, "Samarium", "Sm");
    public static final Element Eu = add(63, 88, "Europium", "Eu");
    public static final Element Gd = add(64, 93, "Gadolinium", "Gd");
    public static final Element Tb = add(65, 93, "Terbium", "Tb");
    public static final Element Dy = add(66, 96, "Dysprosium", "Dy");
    public static final Element Ho = add(67, 97, "Holmium", "Ho");
    public static final Element Er = add(68, 99, "Erbium", "Er");
    public static final Element Tm = add(69, 99, "Thulium", "Tm");
    public static final Element Yb = add(70, 103, "Ytterbium", "Yb");
    public static final Element Lu = add(71, 103, "Lutetium", "Lu");
    public static final Element Hf = add(72, 106, "Hafnium", "Hf");
    public static final Element Ta = add(73, 107, "Tantalum", "Ta");
    public static final Element W = add(74, 109, "Tungsten", "W");
    public static final Element Re = add(75, 111, "Rhenium", "Re");
    public static final Element Os = add(76, 114, "Osmium", "Os");
    public static final Element Ir = add(77, 115, "Iridium", "Ir");
    public static final Element Pt = add(78, 117, "Platinum", "Pt");
    public static final Element Au = add(79, 117, "Gold", "Au");
    public static final Element Hg = add(80, 120, "Mercury", "Hg");
    public static final Element Tl = add(81, 123, "Thallium", "Tl");
    public static final Element Pb = add(82, 125, "Lead", "Pb");
    public static final Element Bi = add(83, 125, "Bismuth", "Bi");
    public static final Element Po = add(84, 124, "Polonium", "Po");
    public static final Element At = add(85, 124, "Astatine", "At");
    public static final Element Rn = add(86, 134, "Radon", "Rn");
    public static final Element Fr = add(87, 134, "Francium", "Fr");
    public static final Element Ra = add(88, 136, "Radium", "Ra");
    public static final Element Ac = add(89, 136, "Actinium", "Ac");
    public static final Element Th = add(90, 140, "Thorium", "Th");
    public static final Element Pa = add(91, 138, "Protactinium", "Pa");
    public static final Element U = add(92, 146, 1.4090285e+17, null, "Uranium", "U", false);
    public static final Element U238 = add(92, 146, 1.4090285e+17, null, "Uranium-238", "U-238", true);
    public static final Element U235 = add(92, 143, 2.2195037e+16, null, "Uranium-235", "U-235", true);
    public static final Element Np = add(93, 144, "Neptunium", "Np");
    public static final Element Pu = add(94, 150, 2522880000000000L, null, "Plutonium", "Pu", false);
    public static final Element Pu239 = add(94, 145, 760332960000.0, null, "Plutonium-239", "Pu-239", true);
    public static final Element Pu241 = add(94, 147, 450649440.0, null, "Plutonium-241", "Pu-241", true);
    public static final Element Am = add(95, 150, "Americium", "Am");
    public static final Element Cm = add(96, 153, "Curium", "Cm");
    public static final Element Bk = add(97, 152, "Berkelium", "Bk");
    public static final Element Cf = add(98, 153, "Californium", "Cf");
    public static final Element Es = add(99, 153, "Einsteinium", "Es");
    public static final Element Fm = add(100, 157, "Fermium", "Fm");
    public static final Element Md = add(101, 157, "Mendelevium", "Md");
    public static final Element No = add(102, 157, "Nobelium", "No");
    public static final Element Lr = add(103, 159, "Lawrencium", "Lr");
    public static final Element Rf = add(104, 161, "Rutherfordium", "Rf");
    public static final Element Db = add(105, 163, "Dubnium", "Db");
    public static final Element Sg = add(106, 165, "Seaborgium", "Sg");
    public static final Element Bh = add(107, 163, "Bohrium", "Bh");
    public static final Element Hs = add(108, 169, "Hassium", "Hs");
    public static final Element Mt = add(109, 167, "Meitnerium", "Mt");
    public static final Element Ds = add(110, 171, "Darmstadtium", "Ds");
    public static final Element Rg = add(111, 169, "Roentgenium", "Rg");
    public static final Element Cn = add(112, 173, "Copernicium", "Cn");
    public static final Element Nh = add(113, 171, "Nihonium", "Nh");
    public static final Element Fl = add(114, 175, "Flerovium", "Fl");
    public static final Element Mc = add(115, 173, "Moscovium", "Mc");
    public static final Element Lv = add(116, 177, "Livermorium", "Lv");
    public static final Element Ts = add(117, 177, "Tennessine", "Ts");
    public static final Element Og = add(118, 176, "Oganesson", "Og");

    // fantasy todo Naquadah element names
    public static final Element Tr = add(119, 178, "Tritanium", "Tr");
    public static final Element Dr = add(120, 180, "Duranium", "Dr");
    public static final Element Ke = add(125, 198, "Trinium", "Ke");
    public static final Element Nq = add(174, 352, 140, null, "Naquadah", "Nq", true);
    public static final Element Nq1 = add(174, 354, 140, null, "NaquadahEnriched", "Nq+", true);
    public static final Element Nq2 = add(174, 348, 140, null, "Naquadria", "*Nq*", true);
    public static final Element Nt = add(0, 1000, "Neutronium", "Nt");
    public static final Element Sp = add(1, 0, "Space", "Sp");
    public static final Element Ma = add(1, 0, "Magic", "Ma");

    // TODO Cosmic Neutronium, other Gregicality Elements

    @ZenMethod
    public static Element add(long protons, long neutrons, String name, String symbol) {
        return add(protons, neutrons, -1, null, name, symbol, false);
    }

    @ZenMethod
    public static Element add(long protons, long neutrons, String name, String symbol, boolean isotope) {
        return add(protons, neutrons, -1, null, name, symbol, isotope);
    }

    @ZenMethod
    public static Element add(long protons, long neutrons, double halfLifeSeconds, String decayTo, String name,
                              String symbol, boolean isIsotope) {
        validateNameAndSymbol(name, symbol);
        String key = toMapKey(name);
        Element current = elements.get(key);
        if (current != null) {
            // this might be intended by addons or scripts
            GTLog.logger.warn("Element with name '{}' already exists. Current element will be overwritten!", name);
            elements.remove(key);
            elementsBySymbol.remove(current.symbol);
            elementList.remove(current);
        }
        if (elementsBySymbol.containsKey(symbol)) {
            // this might be intended by addons or scripts
            GTLog.logger.warn(
                    "Element with symbol '{}' already exists. The element in the symbol map will be overwritten!",
                    symbol);
        }
        Element element = new Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
        elements.put(key, element);
        elementsBySymbol.put(symbol, element);
        elementList.add(element);
        return element;
    }

    private static String toMapKey(String name) {
        name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
        return name;
    }

    private static void validateNameAndSymbol(String name, String symbol) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Element name must not be null or empty");
        }
        if (!namePattern.matcher(name).matches()) {
            throw new IllegalArgumentException("Element name '" + name + "' does not match required format! " +
                    "Name must be in upper camel case format. Isotope number must be separated with an - at the end.");
        }
        if (symbol == null || symbol.isEmpty()) {
            throw new IllegalArgumentException("Element symbol must not be null or empty!");
        }
    }

    public static List<Element> getAllElements() {
        return elementsView;
    }

    @ZenMethod("getAllElements")
    public static Element[] getAllElementsCT() {
        return elementsView.toArray(new Element[0]);
    }

    public static Element getByName(String name) {
        if (name == null || name.isEmpty()) return null;
        return elements.get(toMapKey(name));
    }

    public static Element getBySymbol(String name) {
        if (name == null || name.isEmpty()) return null;
        return elementsBySymbol.get(name); // symbol should be exact
    }

    @ZenMethod
    public static Element get(String name) {
        if (name == null || name.isEmpty()) return null;
        Element e = elementsBySymbol.get(name);
        if (e != null) return e;
        return elements.get(toMapKey(name));
    }
}
