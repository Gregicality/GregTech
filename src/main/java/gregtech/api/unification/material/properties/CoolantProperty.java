package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.Material;

public class CoolantProperty implements IMaterialProperty {

    private Material hotCoolant;
    private Material hotHPCoolant;
    private double moderatorFactor;
    // eyeball this??
    private double coolingFactor;
    // in kelvin at standard conditions
    private double boilingPoint;
    // neutron absorption rate
    private double absorption;
    // in pascal
    private double pressure;
    // in J/mol
    private double heatOfVaporization;

    public CoolantProperty(Material hotCoolant, Material hotHPCoolant, double moderatorFactor, double coolingFactor,
                           double boilingPoint, double absorption, double pressure, double heatOfVaporization) {
        this.hotCoolant = hotCoolant;
        this.hotHPCoolant = hotHPCoolant;
        this.moderatorFactor = moderatorFactor;
        this.coolingFactor = coolingFactor;
        this.boilingPoint = boilingPoint;
        this.absorption = absorption;
        this.pressure = pressure;
        this.heatOfVaporization = heatOfVaporization;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.FLUID, true);
    }

    public void setHotCoolant(Material hotCoolant) {
        this.hotCoolant = hotCoolant;
    }

    public Material getHotCoolant() {
        return this.hotCoolant;
    }

    public void setHotHPCoolant(Material hotHPCoolant) {
        this.hotHPCoolant = hotHPCoolant;
    }

    public Material getHotHPCoolant() {
        return this.hotHPCoolant;
    }

    public void setModeratorFactor(double moderatorFactor) {
        this.moderatorFactor = moderatorFactor;
    }

    public double getModerationFactor() {
        return this.moderatorFactor;
    }

    public void setCoolingFactor(double coolingFactor) {
        this.coolingFactor = coolingFactor;
    }

    public double getCoolingFactor() {
        return this.coolingFactor;
    }

    public void setBoilingPoint(double boilingPoint) {
        this.boilingPoint = boilingPoint;
    }

    public double getBoilingPoint() {
        return this.boilingPoint;
    }

    public void setAbsorption(double absorption) {
        this.absorption = absorption;
    }

    public double getAbsorption() {
        return absorption;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getPressure() {
        return pressure;
    }

    public double getHeatOfVaporization() {
        return heatOfVaporization;
    }

    public void setHeatOfVaporization(double heatOfVaporization) {
        this.heatOfVaporization = heatOfVaporization;
    }
}
