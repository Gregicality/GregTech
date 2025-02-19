package gregtech.api.nuclear.fission.components;

import gregtech.api.nuclear.fission.IFissionFuelStats;

public class FuelRod extends ReactorComponent {

    private IFissionFuelStats fuel;

    public FuelRod(double maxTemperature, double thermalConductivity, IFissionFuelStats fuel, double mass) {
        super(0, maxTemperature, thermalConductivity, mass);
        this.fuel = fuel;
    }

    public double getDuration() {
        return fuel.getDuration();
    }

    public double getHEFissionFactor() {
        return fuel.getFastNeutronFissionCrossSection();
    }

    public double getLEFissionFactor() {
        return fuel.getSlowNeutronFissionCrossSection();
    }

    public double getHECaptureFactor() {
        return fuel.getFastNeutronCaptureCrossSection();
    }

    public double getLECaptureFactor() {
        return fuel.getSlowNeutronCaptureCrossSection();
    }

    public double getNeutronGenerationTime() {
        return fuel.getNeutronGenerationTime();
    }

    public IFissionFuelStats getFuel() {
        return fuel;
    }

    public void setFuel(IFissionFuelStats property) {
        this.fuel = property;
        this.maxTemperature = property.getMaxTemperature();
    }
}
