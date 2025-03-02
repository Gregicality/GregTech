package gregtech.api.nuclear.fission.components;

import java.util.List;

public class ControlRod extends ReactorComponent {

    private double weight;
    private final boolean tipModeration;
    private int relatedFuelRodPairs;

    public ControlRod(double maxTemperature, boolean tipModeration, double thermalConductivity, double mass) {
        super(0, maxTemperature, thermalConductivity, mass);
        this.tipModeration = tipModeration;
        this.weight = 0;
    }

    /**
     * Normalizes the weights of a list of control rods based on the true number of fuel rod pairs.
     * 
     * @param effectiveControlRods The control rods to normalize.
     * @param fuelRodNum           The number of fuel rods in the reactor.
     */
    public static void normalizeWeights(List<ControlRod> effectiveControlRods, int fuelRodNum) {
        for (ControlRod control_rod : effectiveControlRods) {
            if (fuelRodNum != 1)
                control_rod.weight /= (fuelRodNum * fuelRodNum) - fuelRodNum;
        }
    }

    /**
     * Determines the effect of a list of control rods based on how far they are inserted into a reactor.
     * 
     * @param effectiveControlRods The list of control rods to be analyzed.
     * @param insertion            How far the control rods are inserted into the reactor, from 0 to 1.
     * @return A number representing the reactivity change of the reactor from the control rods.
     */
    public static double controlRodFactor(List<ControlRod> effectiveControlRods, double insertion) {
        double crf = 0;
        for (ControlRod control_rod : effectiveControlRods) {
            if (control_rod.hasModeratorTip()) {
                if (insertion <= 0.3) {
                    crf -= insertion / 3 * control_rod.weight;
                } else {
                    crf += (11F / 7 * (insertion - 0.3) - 0.1) * control_rod.weight;
                }
            } else {
                crf += insertion * control_rod.weight;
            }
        }
        return crf;
    }

    /**
     * @return How much the control rod affects reactivity.
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @param weight How much the control rod should affect reactivity.
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Notify the control rod of a pair of fuel rods that it is in the way of, to help increase its weight later.
     */
    public void addFuelRodPair() {
        relatedFuelRodPairs++;
    }

    /**
     * @return Whether the control rod increases reactivity at low insertion levels (due to having a moderating tip).
     */
    public boolean hasModeratorTip() {
        return tipModeration;
    }

    /**
     * Automatically calculates the control rod weight (its effect on reactivity) based on the number of fuel rod pairs
     * that it is in the way of.
     */
    public void computeWeightFromFuelRodMap() {
        this.weight = relatedFuelRodPairs * 4; // 4 being a constant to help balance this out
    }
}
