package gregtech.api.nuclear.fission.components;

public class ReactorComponent {

    private final double moderationFactor;
    protected double maxTemperature;
    private final double thermalConductivity;
    private final double mass;

    private int x;
    private int y;

    private int index = -1;

    public ReactorComponent(double moderationFactor, double maxTemperature, double thermalConductivity, double mass) {
        this.moderationFactor = moderationFactor;
        this.maxTemperature = maxTemperature;
        this.thermalConductivity = thermalConductivity;
        this.mass = mass;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getModerationFactor() {
        return moderationFactor;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public double getThermalConductivity() {
        return thermalConductivity;
    }

    /**
     * @return The index of the reactor component, which is -1 if unset
     */
    public int getIndex() {
        return index;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean samePositionAs(ReactorComponent component) {
        return this.getX() == component.getX() && this.getY() == component.getY();
    }

    public double getDistance(ReactorComponent component) {
        return Math.sqrt(Math.pow(this.getX() - component.getX(), 2) +
                Math.pow(this.getY() - component.getY(), 2));
    }

    public double getMass() {
        return mass;
    }
}
