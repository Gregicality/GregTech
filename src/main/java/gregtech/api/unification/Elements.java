package gregtech.api.unification;

import com.google.common.base.CaseFormat;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.*;

@ZenClass("mods.gregtech.material.Elements")
@ZenRegister
public class Elements {

    private static final Map<String, Element> elements = new HashMap<>();

    private Elements() {}

    public static final Element H = add(1, 0, -1, null, "Hydrogen", "H", false);
    public static final Element D = add(1, 1, -1, "H", "Deuterium", "D", true);
    public static final Element T = add(1, 2, -1, "D", "Tritium", "T", true);
    public static final Element He = add(2, 2, -1, null, "Helium", "He", false);
    public static final Element He3 = add(2, 1, -1, "H&D", "Helium-3", "He-3", true);
    public static final Element Li = add(3, 4, -1, null, "Lithium", "Li", false);
    public static final Element Be = add(4, 5, -1, null, "Beryllium", "Be", false);
    public static final Element B = add(5, 5, -1, null, "Boron", "B", false);
    public static final Element C = add(6, 6, -1, null, "Carbon", "C", false);
    public static final Element N = add(7, 7, -1, null, "Nitrogen", "N", false);
    public static final Element O = add(8, 8, -1, null, "Oxygen", "O", false);
    public static final Element F = add(9, 9, -1, null, "Fluorine", "F", false);
    public static final Element Ne = add(10, 10, -1, null, "Neon", "Ne", false);
    public static final Element Na = add(11, 11, -1, null, "Sodium", "Na", false);
    public static final Element Mg = add(12, 12, -1, null, "Magnesium", "Mg", false);
    public static final Element Al = add(13, 13, -1, null, "Aluminium", "Al", false);
    public static final Element Si = add(14, 14, -1, null, "Silicon", "Si", false);
    public static final Element P = add(15, 15, -1, null, "Phosphorus", "P", false);
    public static final Element S = add(16, 16, -1, null, "Sulfur", "S", false);
    public static final Element Cl = add(17, 18, -1, null, "Chlorine", "Cl", false);
    public static final Element Ar = add(18, 22, -1, null, "Argon", "Ar", false);
    public static final Element K = add(19, 20, -1, null, "Potassium", "K", false);
    public static final Element Ca = add(20, 20, -1, null, "Calcium", "Ca", false);
    public static final Element Sc = add(21, 24, -1, null, "Scandium", "Sc", false);
    public static final Element Ti = add(22, 26, -1, null, "Titanium", "Ti", false);
    public static final Element V = add(23, 28, -1, null, "Vanadium", "V", false);
    public static final Element Cr = add(24, 28, -1, null, "Chrome", "Cr", false);
    public static final Element Mn = add(25, 30, -1, null, "Manganese", "Mn", false);
    public static final Element Fe = add(26, 30, -1, null, "Iron", "Fe", false);
    public static final Element Co = add(27, 32, -1, null, "Cobalt", "Co", false);
    public static final Element Ni = add(28, 30, -1, null, "Nickel", "Ni", false);
    public static final Element Cu = add(29, 34, -1, null, "Copper", "Cu", false);
    public static final Element Zn = add(30, 35, -1, null, "Zinc", "Zn", false);
    public static final Element Ga = add(31, 39, -1, null, "Gallium", "Ga", false);
    public static final Element Ge = add(32, 40, -1, null, "Germanium", "Ge", false);
    public static final Element As = add(33, 42, -1, null, "Arsenic", "As", false);
    public static final Element Se = add(34, 45, -1, null, "Selenium", "Se", false);
    public static final Element Br = add(35, 45, -1, null, "Bromine", "Br", false);
    public static final Element Kr = add(36, 48, -1, null, "Krypton", "Kr", false);
    public static final Element Rb = add(37, 48, -1, null, "Rubidium", "Rb", false);
    public static final Element Sr = add(38, 49, -1, null, "Strontium", "Sr", false);
    public static final Element Y = add(39, 50, -1, null, "Yttrium", "Y", false);
    public static final Element Zr = add(40, 51, -1, null, "Zirconium", "Zr", false);
    public static final Element Nb = add(41, 53, -1, null, "Niobium", "Nb", false);
    public static final Element Mo = add(42, 53, -1, null, "Molybdenum", "Mo", false);
    public static final Element Tc = add(43, 55, -1, null, "Technetium", "Tc", false);
    public static final Element Ru = add(44, 57, -1, null, "Ruthenium", "Ru", false);
    public static final Element Rh = add(45, 58, -1, null, "Rhodium", "Rh", false);
    public static final Element Pd = add(46, 60, -1, null, "Palladium", "Pd", false);
    public static final Element Ag = add(47, 60, -1, null, "Silver", "Ag", false);
    public static final Element Cd = add(48, 64, -1, null, "Cadmium", "Cd", false);
    public static final Element In = add(49, 65, -1, null, "Indium", "In", false);
    public static final Element Sn = add(50, 68, -1, null, "Tin", "Sn", false);
    public static final Element Sb = add(51, 70, -1, null, "Antimony", "Sb", false);
    public static final Element Te = add(52, 75, -1, null, "Tellurium", "Te", false);
    public static final Element I = add(53, 74, -1, null, "Iodine", "I", false);
    public static final Element Xe = add(54, 77, -1, null, "Xenon", "Xe", false);
    public static final Element Cs = add(55, 77, -1, null, "Caesium", "Cs", false);
    public static final Element Ba = add(56, 81, -1, null, "Barium", "Ba", false);
    public static final Element La = add(57, 81, -1, null, "Lanthanum", "La", false);
    public static final Element Ce = add(58, 82, -1, null, "Cerium", "Ce", false);
    public static final Element Pr = add(59, 81, -1, null, "Praseodymium", "Pr", false);
    public static final Element Nd = add(60, 84, -1, null, "Neodymium", "Nd", false);
    public static final Element Pm = add(61, 83, -1, null, "Promethium", "Pm", false);
    public static final Element Sm = add(62, 88, -1, null, "Samarium", "Sm", false);
    public static final Element Eu = add(63, 88, -1, null, "Europium", "Eu", false);
    public static final Element Gd = add(64, 93, -1, null, "Gadolinium", "Gd", false);
    public static final Element Tb = add(65, 93, -1, null, "Terbium", "Tb", false);
    public static final Element Dy = add(66, 96, -1, null, "Dysprosium", "Dy", false);
    public static final Element Ho = add(67, 97, -1, null, "Holmium", "Ho", false);
    public static final Element Er = add(68, 99, -1, null, "Erbium", "Er", false);
    public static final Element Tm = add(69, 99, -1, null, "Thulium", "Tm", false);
    public static final Element Yb = add(70, 103, -1, null, "Ytterbium", "Yb", false);
    public static final Element Lu = add(71, 103, -1, null, "Lutetium", "Lu", false);
    public static final Element Hf = add(72, 106, -1, null, "Hafnium", "Hf", false);
    public static final Element Ta = add(73, 107, -1, null, "Tantalum", "Ta", false);
    public static final Element W = add(74, 109, -1, null, "Tungsten", "W", false);
    public static final Element Re = add(75, 111, -1, null, "Rhenium", "Re", false);
    public static final Element Os = add(76, 114, -1, null, "Osmium", "Os", false);
    public static final Element Ir = add(77, 115, -1, null, "Iridium", "Ir", false);
    public static final Element Pt = add(78, 117, -1, null, "Platinum", "Pt", false);
    public static final Element Au = add(79, 117, -1, null, "Gold", "Au", false);
    public static final Element Hg = add(80, 120, -1, null, "Mercury", "Hg", false);
    public static final Element Tl = add(81, 123, -1, null, "Thallium", "Tl", false);
    public static final Element Pb = add(82, 125, -1, null, "Lead", "Pb", false);
    public static final Element Bi = add(83, 125, -1, null, "Bismuth", "Bi", false);
    public static final Element Po = add(84, 124, -1, null, "Polonium", "Po", false);
    public static final Element At = add(85, 124, -1, null, "Astatine", "At", false);
    public static final Element Rn = add(86, 134, -1, null, "Radon", "Rn", false);
    public static final Element Fr = add(87, 134, -1, null, "Francium", "Fr", false);
    public static final Element Ra = add(88, 136, -1, null, "Radium", "Ra", false);
    public static final Element Ac = add(89, 136, -1, null, "Actinium", "Ac", false);
    public static final Element Th = add(90, 140, -1, null, "Thorium", "Th", false);
    public static final Element Pa = add(91, 138, -1, null, "Protactinium", "Pa", false);
    public static final Element U = add(92, 146, 1.4090285e+17, null, "Uranium", "U", false);
    public static final Element U238 = add(92, 146, 1.4090285e+17, null, "Uranium-238", "U-238", false);
    public static final Element U235 = add(92, 143, 2.2195037e+16, null, "Uranium-235", "U-235", true);
    public static final Element U239 = add(92, 147, 1407, null, "Uranium-239", "U-239", true);
    public static final Element Np = add(93, 144, -1, null, "Neptunium", "Np", false);
    public static final Element Np235 = add(93, 142, -1, null, "Neptunium-235", "Np-235", true);
    public static final Element Np236 = add(93, 143, -1, null, "Neptunium-236", "Np-236", true);
    public static final Element Np237 = add(93, 144, -1, null, "Neptunium-237", "Np-237", true);
    public static final Element Np239 = add(93, 146, -1, null, "Neptunium-239", "Np-239", true);
    public static final Element Pu = add(94, 152, -1, null, "Plutonium", "Pu", false);
    public static final Element Pu238 = add(94, 144, 2765707200d, null, "Plutonium-238", "Pu-238", true);
    public static final Element Pu239 = add(94, 145, 760332960000d, null, "Plutonium-239", "Pu-239", true);
    public static final Element Pu240 = add(94, 146, 206907696000d, null, "Plutonium-240", "Pu-240", true);
    public static final Element Pu241 = add(94, 147, 450649440d, null, "Plutonium-241", "Pu-241", true);
    public static final Element Pu242 = add(94, 148, 1.1826e+13, null, "Plutonium-242", "Pu-242", true);
    public static final Element Pu244 = add(94, 150, 2.52288e+15, null, "Plutonium-244", "Pu-244", true);
    public static final Element Am = add(95, 150, -1, null, "Americium", "Am", false);
    public static final Element Cm = add(96, 153, -1, null, "Curium", "Cm", false);
    public static final Element Bk = add(97, 152, -1, null, "Berkelium", "Bk", false);
    public static final Element Cf = add(98, 153, -1, null, "Californium", "Cf", false);
    public static final Element Es = add(99, 153, -1, null, "Einsteinium", "Es", false);
    public static final Element Fm = add(100, 157, -1, null, "Fermium", "Fm", false);
    public static final Element Md = add(101, 157, -1, null, "Mendelevium", "Md", false);
    public static final Element No = add(102, 157, -1, null, "Nobelium", "No", false);
    public static final Element Lr = add(103, 159, -1, null, "Lawrencium", "Lr", false);
    public static final Element Rf = add(104, 161, -1, null, "Rutherfordium", "Rf", false);
    public static final Element Db = add(105, 163, -1, null, "Dubnium", "Db", false);
    public static final Element Sg = add(106, 165, -1, null, "Seaborgium", "Sg", false);
    public static final Element Bh = add(107, 163, -1, null, "Bohrium", "Bh", false);
    public static final Element Hs = add(108, 169, -1, null, "Hassium", "Hs", false);
    public static final Element Mt = add(109, 167, -1, null, "Meitnerium", "Mt", false);
    public static final Element Ds = add(110, 171, -1, null, "Darmstadtium", "Ds", false);
    public static final Element Rg = add(111, 169, -1, null, "Roentgenium", "Rg", false);
    public static final Element Cn = add(112, 173, -1, null, "Copernicium", "Cn", false);
    public static final Element Nh = add(113, 171, -1, null, "Nihonium", "Nh", false);
    public static final Element Fl = add(114, 175, -1, null, "Flerovium", "Fl", false);
    public static final Element Mc = add(115, 173, -1, null, "Moscovium", "Mc", false);
    public static final Element Lv = add(116, 177, -1, null, "Livermorium", "Lv", false);
    public static final Element Ts = add(117, 177, -1, null, "Tennessine", "Ts", false);
    public static final Element Og = add(118, 176, -1, null, "Oganesson", "Og", false);

    // fantasy todo Naquadah element names
    public static final Element Tr = add(119, 178, -1, null, "Tritanium", "Tr", false);
    public static final Element Dr = add(120, 180, -1, null, "Duranium", "Dr", false);
    public static final Element Ke = add(125, 198, -1, null, "Trinium", "Ke", false);
    public static final Element Nq = add(174, 352, 140, null, "Naquadah", "Nq", true);
    public static final Element Nq1 = add(174, 354, 140, null, "NaquadahEnriched", "Nq+", true);
    public static final Element Nq2 = add(174, 348, 140, null, "Naquadria", "*Nq*", true);
    public static final Element Nt = add(0, 1000, -1, null, "Neutronium", "Nt", false);
    public static final Element Sp = add(1, 0, -1, null, "Space", "Sp", false);
    public static final Element Ma = add(1, 0, -1, null, "Magic", "Ma", false);

    // TODO Cosmic Neutronium, other Gregicality Elements

    @ZenMethod
    public static Element add(long protons, long neutrons, double halfLifeSeconds, String decayTo, String name,
                              String symbol, boolean isIsotope) {
        Element element = new Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
        elements.put(name, element);
        return element;
    }

    public static List<Element> getAllElements() {
        return Collections.unmodifiableList(new ArrayList<>(elements.values()));
    }

    @ZenMethod("getAllElements")
    public static Element[] getAllElementsCT() {
        return elements.values().toArray(new Element[0]);
    }

    @ZenMethod
    public static Element get(String name) {
        return elements.get(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name));
    }
}
