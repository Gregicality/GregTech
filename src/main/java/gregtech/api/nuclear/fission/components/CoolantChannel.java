package gregtech.api.nuclear.fission.components;

import gregtech.api.capability.ICoolantHandler;
import gregtech.api.nuclear.fission.ICoolantStats;

public class CoolantChannel extends ReactorComponent {

    private final ICoolantStats coolant;
    private int relatedFuelRodPairs;

    private ICoolantHandler inputHandler;
    private ICoolantHandler outputHandler;

    // Allows fission reactors to heat up less than a full liter of coolant.
    public double partialCoolant;

    public CoolantChannel(double maxTemperature, double thermalConductivity, ICoolantStats coolant, double mass,
                          ICoolantHandler inputHandler, ICoolantHandler outputHandler) {
        super(coolant.getModeratorFactor(), maxTemperature, thermalConductivity, mass);
        this.coolant = coolant;
        this.inputHandler = inputHandler;
        this.outputHandler = outputHandler;
    }

    public ICoolantStats getCoolant() {
        return coolant;
    }

    public ICoolantHandler getInputHandler() {
        return inputHandler;
    }

    public ICoolantHandler getOutputHandler() {
        return outputHandler;
    }
}
