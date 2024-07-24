package gregtech.api.unification;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

/**
 * This is some kind of Periodic Table, which can be used to determine Properties of the Materials.
 */
@ZenClass("mods.gregtech.material.Element")
@ZenRegister
public class Element {

    public final String name;
    public final String symbol;
    public final long protons;
    public final long neutrons;

    @ZenProperty("isotope")
    public final boolean isIsotope;
    @ZenProperty("halfLifeSeconds")
    public final double halfLifeSeconds;
    @ZenProperty("decayTo")
    public final String decayTo;

    /**
     * @param protons         Amount of Protons
     * @param neutrons        Amount of Neutrons (I could have made mistakes with the Neutron amount calculation, please
     *                        tell me if I did something wrong)
     * @param halfLifeSeconds Amount of Half Life this Material has in Seconds. -1 for stable Materials
     * @param decayTo         String representing the Elements it decays to. Separated by an '&' Character
     * @param name            Name of the Element
     * @param symbol          Symbol of the Element
     */
    public Element(long protons, long neutrons, double halfLifeSeconds, String decayTo, String name, String symbol,
                   boolean isIsotope) {
        this.protons = protons;
        this.neutrons = neutrons;
        this.halfLifeSeconds = halfLifeSeconds;
        this.decayTo = decayTo;
        this.name = name;
        this.symbol = symbol;
        this.isIsotope = isIsotope;
    }

    @ZenGetter("name")
    public String getName() {
        return name;
    }

    @ZenGetter("symbol")
    public String getSymbol() {
        return symbol;
    }

    @ZenGetter("protons")
    public long getProtons() {
        return protons;
    }

    @ZenGetter("neutrons")
    public long getNeutrons() {
        return neutrons;
    }

    @ZenGetter("mass")
    public long getMass() {
        return protons + neutrons;
    }

    @Override
    @ZenMethod
    public String toString() {
        return super.toString();
    }
}
