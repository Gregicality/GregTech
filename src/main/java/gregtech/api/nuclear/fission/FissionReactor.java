package gregtech.api.nuclear.fission;

import gregtech.api.fluids.fluidhandlers.LockableFluidTank;
import gregtech.api.nuclear.fission.components.ControlRod;
import gregtech.api.nuclear.fission.components.CoolantChannel;
import gregtech.api.nuclear.fission.components.FuelRod;
import gregtech.api.nuclear.fission.components.ReactorComponent;
import gregtech.common.ConfigHolder;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FissionReactor {

    /**
     * The gas constant in J * K^-1 * mol^-1 if you want to use a different set of units prepare thy life to become the
     * worst of nightmares
     */
    public static final double R = 8.31446261815324;
    /**
     * Standard pressure in Pascal, corresponds to one standard atmosphere
     */
    public static final double standardPressure = 101325;

    /**
     * The starting temperature of the reactor in Kelvin
     */
    public static final double roomTemperature = 273;

    /**
     * Boiling point of air at standard pressure in Kelvin
     */
    public static final double airBoilingPoint = 78.8;

    private ReactorComponent[][] reactorLayout;
    private final List<FuelRod> fuelRods;
    private final List<ControlRod> controlRods;
    private final List<CoolantChannel> coolantChannels;
    private final List<ControlRod> effectiveControlRods;
    private final List<CoolantChannel> effectiveCoolantChannels;

    private double k;

    private double controlRodFactor;

    public double kEff; // criticality value, based on k

    /**
     * Integers used on variables with direct player control for easier adjustments (normalize this to 0,1)
     */
    public double controlRodInsertion;
    private int reactorDepth;
    private double reactorRadius;

    private boolean moderatorTipped; // set by the type of control rod in the reactor(prepInitialConditions)

    /**
     * Megawatts
     */
    public double power;

    /**
     * Temperature of the reactor
     */
    public double temperature = roomTemperature;
    public double pressure = standardPressure;
    private double exteriorPressure = standardPressure;
    /**
     * Temperature of boiling point in kelvin at standard pressure Determined by a weighted sum of the individual
     * coolant boiling points in {@link FissionReactor#prepareInitialConditions()}
     */
    private double coolantBoilingPointStandardPressure;

    /**
     * Average temperature of the coolant in kelvin as coolant exits the reactor.
     */
    private double coolantExitTemperature;

    private double prevTemperature;

    /**
     * Latent heat of vaporization in J/mol Determined by a weighted sum of the individual heats of vaporization in
     * {@link FissionReactor#prepareInitialConditions()}
     */
    private double coolantHeatOfVaporization;
    /**
     * Equilibrium temperature in kelvin Determined by a weighted sum of the individual coolant temperatures in
     * {@link FissionReactor#prepareInitialConditions()}
     */
    private double coolantBaseTemperature;
    public double maxFuelDepletion = 1;
    public double fuelDepletion = -1;
    private double neutronPoisonAmount; // can kill reactor if power is lowered and this value is high
    private double decayProductsAmount;
    private double envTemperature = roomTemperature; // maybe gotten from config per dim
    public double accumulatedHydrogen;
    private double weightedGenerationTime = 2; // The mean generation time in seconds, accounting for delayed neutrons

    public double maxTemperature = 2000;
    // Pascals
    public double maxPressure = 15000000;
    // In MW apparently
    public double maxPower = 3; // determined by the amount of fuel in reactor and neutron matricies
    public static double zircaloyHydrogenReactionTemperature = 1500;

    private double surfaceArea;
    public static double thermalConductivity = 45; // 45 W/(m K), for steel
    public static double wallThickness = 0.1;
    public static double coolantWallThickness = 0.06; // Ideal for a 1-m diameter steel pipe with the given maximum
    // pressure
    public static double specificHeatCapacity = 420; // 420 J/(kg K), for steel
    public static double convectiveHeatTransferCoefficient = 10; // 10 W/(m^2 K), for slow-moving air

    public static double powerDefectCoefficient = 0.016; // In units of reactivity
    public static double decayProductRate = 0.997; // Based on the half-life of xenon-135, using real-life days as
    // Minecraft days, and yes, I am using this for plutonium too
    public static double poisonFraction = 0.063; // Xenon-135 yield from fission
    public static double crossSectionRatio = 4; // The ratio between the cross section for typical fuels and xenon-135;

    // very much changed here for balance purposes

    private double coolantMass;
    public double fuelMass;
    private double structuralMass;
    public boolean needsOutput;
    public boolean controlRodRegulationOn = true;
    protected boolean isOn = false;

    public static final double UPDATE_RATE = 0.2;

    protected static double responseFunction(double target, double current) {
        if (current < 0) {
            current = 0.1;
        }
        double expDecay = Math.exp(-UPDATE_RATE);
        return current * expDecay + target * (1 - expDecay);
    }

    protected double responseFunctionTemperature(double envTemperature, double currentTemperature, double heatAdded,
                                                 double heatAbsorbed) {
        currentTemperature = Math.max(0.1, currentTemperature);
        heatAbsorbed = Math.max(0, heatAbsorbed);
        /*
         * Simplifies what is the following:
         * heatTransferCoefficient = 1 / (1 / convectiveHeatTransferCoefficient + wallThickness / thermalConductivity);
         * (https://en.wikipedia.org/wiki/Newton%27s_law_of_cooling#First-order_transient_response_of_lumped-
         * capacitance_objects)
         * This assumes that we're extracting heat from the reactor through the wall into slowly moving air, removing
         * the second convective heat.
         * timeConstant = heatTransferCoefficient * this.surfaceArea / specificHeatCapacity;
         */
        // Technically the inverse.
        double timeConstant = specificHeatCapacity *
                (1 / convectiveHeatTransferCoefficient + wallThickness / thermalConductivity) / this.surfaceArea;

        // Solves the following differential equation:
        // dT/dt = h_added_tot / m_tot - k(T - T_env) at t = 1s with T(0) = T_0
        double expDecay = Math.exp(-timeConstant);

        double effectiveEnvTemperature = envTemperature +
                (heatAdded - heatAbsorbed) / (timeConstant * (this.coolantMass + this.structuralMass + this.fuelMass));
        return currentTemperature * expDecay + effectiveEnvTemperature * (1 - expDecay);
    }

    public FissionReactor(int size, int depth, double controlRodInsertion) {
        reactorLayout = new ReactorComponent[size][size];
        reactorDepth = depth;
        reactorRadius = (double) size / 2 + 1.5; // Includes the extra block plus the distance from the center of a
        // block to its edge
        // Maps (0, 15) -> (0.01, 1)
        this.controlRodInsertion = Math.max(0.001, controlRodInsertion);
        fuelRods = new ArrayList<>();
        controlRods = new ArrayList<>();
        coolantChannels = new ArrayList<>();
        effectiveControlRods = new ArrayList<>();
        effectiveCoolantChannels = new ArrayList<>();
        // 2pi * r^2 + 2pi * r * l
        surfaceArea = (reactorRadius * reactorRadius) * Math.PI * 2 + reactorDepth * reactorRadius * Math.PI * 2;
        structuralMass = reactorDepth * reactorRadius * reactorRadius * Math.PI *
                300; // Assuming 300 kg/m^3 when it's basically empty, does not have to be precise
    }

    public void prepareThermalProperties() {
        int idRod = 0, idControl = 0, idChannel = 0;

        for (int i = 0; i < reactorLayout.length; i++) {
            for (int j = 0; j < reactorLayout[i].length; j++) {
                /*
                 * Check for null because the layout
                 * is in general not a square
                 */
                ReactorComponent comp = reactorLayout[i][j];
                if (comp != null) {
                    comp.setPos(i, j);
                    maxTemperature = Double.min(maxTemperature, comp.getMaxTemperature());
                    structuralMass += comp.getMass();
                    if (comp instanceof FuelRod fuelRod) {
                        comp.setIndex(idRod);
                        fuelRods.add(fuelRod);
                        idRod++;
                    }

                    if (comp instanceof ControlRod controlRod) {
                        comp.setIndex(idControl);
                        controlRods.add(controlRod);
                        idControl++;
                    }

                    if (comp instanceof CoolantChannel coolantChannel) {
                        comp.setIndex(idChannel);
                        coolantChannels.add(coolantChannel);
                        idChannel++;
                    }
                }
            }
        }
    }

    public void computeGeometry() {
        effectiveControlRods.clear();
        effectiveCoolantChannels.clear();
        moderatorTipped = false;
        double[][] geometricMatrixSlowNeutrons = new double[fuelRods.size()][fuelRods.size()];
        double[][] geometricMatrixFastNeutrons = new double[fuelRods.size()][fuelRods.size()];

        /*
         * We calculate geometric factor matrices to determine how many neutrons go from the i-th to the j-th fuel rod
         * This factor is different for slow and fast neutrons because they interact differently with the materials and
         * fuel
         */
        for (int i = 0; i < fuelRods.size(); i++) {
            for (int j = 0; j < i; j++) {
                boolean pathIsClear = true;
                double mij = 0;
                FuelRod rodOne = fuelRods.get(i);
                FuelRod rodTwo = fuelRods.get(j);

                /*
                 * Geometric factor calculation is done by (rough) numerical integration along a straight path between
                 * the two cells
                 */
                int prevX = rodOne.getX();
                int prevY = rodOne.getY();
                double resolution = ConfigHolder.machines.nuclear.fissionReactorResolution;
                for (int t = 0; t < resolution; t++) {
                    int x = (int) Math.round((rodTwo.getX() - rodOne.getX()) *
                            ((float) t / resolution) + rodOne.getX());
                    int y = (int) Math.round((rodTwo.getY() - rodOne.getY()) *
                            ((float) t / resolution) + rodOne.getY());
                    if (x < 0 || x > reactorLayout.length - 1 || y < 0 || y > reactorLayout.length - 1) {
                        continue;
                    }
                    ReactorComponent component = reactorLayout[x][y];

                    if (component == null) {
                        continue;
                    }

                    mij += component.getModerationFactor();

                    if (x == prevX && y == prevY) {
                        continue;
                    }
                    prevX = x;
                    prevY = y;

                    /*
                     * For simplicity, we pretend that fuel rods are completely opaque to neutrons, paths that hit fuel
                     * rods are ignored as obstructed
                     */
                    if (component instanceof FuelRod && !component.samePositionAs(rodOne) &&
                            !component.samePositionAs(rodTwo)) {
                        pathIsClear = false;
                        break;
                    }

                    /*
                     * We keep track of which active elements we hit, so we can determine how important they are
                     * relative to the others later
                     */
                    if (component instanceof ControlRod rod) {
                        rod.addFuelRodPair();
                    }
                }

                /*
                 * The actual calculation of the geometric factors, fast neutrons are randomly converted into slow
                 * neutrons along the path, we pretend that fuel rods are infinitely tall and thin for simplicity
                 * This means the fraction of slow neutrons will go as (1-exp(-m * x))/x where x is the distance between
                 * the cells
                 * The fraction of fast neutrons is simply one minus the fraction of slow neutrons
                 */
                if (pathIsClear) {
                    mij /= resolution;
                    geometricMatrixSlowNeutrons[i][j] = (1.0 -
                            Math.exp(-mij * rodOne.getDistance(rodTwo))) /
                            rodOne.getDistance(rodTwo);
                    geometricMatrixSlowNeutrons[j][i] = geometricMatrixSlowNeutrons[i][j];
                    geometricMatrixFastNeutrons[i][j] = 1.0 /
                            rodOne.getDistance(rodTwo) - geometricMatrixSlowNeutrons[i][j];
                    geometricMatrixFastNeutrons[j][i] = geometricMatrixFastNeutrons[i][j];
                }
            }
        }

        /*
         * We now use the data we have on the geometry to calculate the reactor's stats
         */
        double avgGeometricFactorSlowNeutrons = 0;
        double avgGeometricFactorFastNeutrons = 0;

        double avgHighEnergyFissionFactor = 0;
        double avgLowEnergyFissionFactor = 0;
        double avgHighEnergyCaptureFactor = 0;
        double avgLowEnergyCaptureFactor = 0;

        double avgFuelRodDistance = 0;

        for (int iIdx = 0; iIdx < fuelRods.size(); iIdx++) {
            FuelRod i = fuelRods.get(iIdx);
            for (int jIdx = 0; jIdx < iIdx; jIdx++) {
                FuelRod j = fuelRods.get(jIdx);
                avgGeometricFactorSlowNeutrons += geometricMatrixSlowNeutrons[i.getIndex()][j.getIndex()];
                avgGeometricFactorFastNeutrons += geometricMatrixFastNeutrons[i.getIndex()][j.getIndex()];

                avgFuelRodDistance += i.getDistance(j);
            }
            avgHighEnergyFissionFactor += i.getHEFissionFactor();
            avgLowEnergyFissionFactor += i.getLEFissionFactor();
            avgHighEnergyCaptureFactor += i.getHECaptureFactor();
            avgLowEnergyCaptureFactor += i.getLECaptureFactor();
        }

        if (fuelRods.size() > 1) {
            avgGeometricFactorSlowNeutrons *= 0.25 / fuelRods.size();
            avgGeometricFactorFastNeutrons *= 0.25 / fuelRods.size();

            avgHighEnergyFissionFactor /= fuelRods.size();
            avgLowEnergyFissionFactor /= fuelRods.size();
            avgHighEnergyCaptureFactor /= fuelRods.size();
            avgLowEnergyCaptureFactor /= fuelRods.size();

            avgFuelRodDistance /= (fuelRods.size() * fuelRods.size() - fuelRods.size());
            avgFuelRodDistance *= 2;

            double kSlow = avgLowEnergyFissionFactor / avgLowEnergyCaptureFactor * avgGeometricFactorSlowNeutrons;
            double kFast = avgHighEnergyFissionFactor / avgHighEnergyCaptureFactor * avgGeometricFactorFastNeutrons;
            k = (kSlow + kFast) * reactorDepth / (1. + reactorDepth);

            double depthDiameterDifference = 0.5 * (reactorDepth - reactorRadius * 2) / reactorRadius;
            double sigmoid = 1 / (1 + Math.exp(-depthDiameterDifference));
            double fuelRodFactor = sigmoid * Math.pow(avgFuelRodDistance, -2) +
                    (1 - sigmoid) * Math.pow(avgFuelRodDistance, -1);

            maxPower = fuelRods.size() * (avgHighEnergyFissionFactor + avgLowEnergyFissionFactor) * fuelRodFactor *
                    ConfigHolder.machines.nuclear.nuclearPowerMultiplier;
        } else {
            // The calculations break down for the geometry, so we just do this instead.
            k = 0.00001;
            maxPower = 0.1 * ConfigHolder.machines.nuclear.nuclearPowerMultiplier;
        }
        /*
         * We give each control rod and coolant channel a weight depending on how many fuel rods they affect
         */
        this.computeControlRodWeights();

        controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods, this.controlRodInsertion);

        this.prepareInitialConditions();
    }

    /**
     * Loops over all the control rods, determines which ones actually affect reactivity, and gives them a weight
     * depending on how many fuel rods they affect
     */
    protected void computeControlRodWeights() {
        for (ControlRod rod : controlRods) {
            rod.computeWeightFromFuelRodMap();
            if (rod.getWeight() > 0) {
                effectiveControlRods.add(rod);
            }
        }
        ControlRod.normalizeWeights(effectiveControlRods, fuelRods.size());
    }

    public boolean isDepleted() {
        return fuelDepletion >= maxFuelDepletion || fuelDepletion < 0;
    }

    public void resetFuelDepletion() {
        this.fuelDepletion = 0;
    }

    public void prepareInitialConditions() {
        coolantBaseTemperature = 0;
        coolantBoilingPointStandardPressure = 0;
        coolantExitTemperature = 0;
        coolantHeatOfVaporization = 0;
        maxFuelDepletion = 0;
        weightedGenerationTime = 0;

        for (FuelRod rod : fuelRods) {
            maxFuelDepletion += rod.getDuration();
            weightedGenerationTime += rod.getNeutronGenerationTime();
        }
        if (fuelDepletion < 0) {
            fuelDepletion = maxFuelDepletion;
        }
        weightedGenerationTime /= fuelRods.size();

        for (CoolantChannel channel : coolantChannels) {
            ICoolantStats prop = channel.getCoolant();
            Fluid fluid = CoolantRegistry.originalFluid(prop);

            if (fluid != null) {
                coolantBaseTemperature += fluid.getTemperature();
            }
            coolantBoilingPointStandardPressure += prop.getBoilingPoint();
            coolantExitTemperature += prop.getHotCoolant().getTemperature();
            coolantHeatOfVaporization += prop.getHeatOfVaporization();
        }

        if (!coolantChannels.isEmpty()) {
            coolantBaseTemperature /= coolantChannels.size();
            coolantBoilingPointStandardPressure /= coolantChannels.size();
            coolantExitTemperature /= coolantChannels.size();
            coolantHeatOfVaporization /= coolantChannels.size();

            if (coolantBaseTemperature == 0) {
                coolantBaseTemperature = envTemperature;
            }
            if (coolantBoilingPointStandardPressure == 0) {
                coolantBoilingPointStandardPressure = airBoilingPoint;
            }
        }
        isOn = true;
    }

    /**
     * Consumes the coolant. Calculates the heat removed by the coolant based on an amalgamation of different equations.
     * It is not particularly realistic, but allows for some fine-tuning to happen. Heat removed is proportional to the
     * surface area of the coolant channel (which is equivalent to the reactor's depth), as well as the flow rate of
     * coolant and the difference in temperature between the reactor and the coolant
     */
    public double makeCoolantFlow(int flowRate) {
        double heatRemoved = 0;
        coolantMass = 0;
        for (CoolantChannel channel : coolantChannels) {
            LockableFluidTank in = channel.getInputHandler().getFluidTank();
            LockableFluidTank out = channel.getOutputHandler().getFluidTank();
            FluidStack tryFluidDrain = in.drain(flowRate, false);
            if (tryFluidDrain != null) {
                int drained = tryFluidDrain.amount;

                ICoolantStats prop = channel.getCoolant();
                int coolantTemp = CoolantRegistry.originalFluid(prop).getTemperature();

                double cooledTemperature = prop.getHotCoolant().getTemperature();
                if (cooledTemperature > this.temperature) {
                    continue;
                }

                double heatRemovedPerLiter = prop.getSpecificHeatCapacity() /
                        ConfigHolder.machines.nuclear.fissionCoolantDivisor *
                        (cooledTemperature - coolantTemp);
                // Explained by:
                // https://physics.stackexchange.com/questions/153434/heat-transfer-between-the-bulk-of-the-fluid-inside-the-pipe-and-the-pipe-externa
                double heatFluxPerAreaAndTemp = 1 /
                        (1 / prop.getCoolingFactor() + coolantWallThickness / thermalConductivity);
                double idealHeatFlux = heatFluxPerAreaAndTemp * 4 * reactorDepth *
                        (temperature - cooledTemperature);

                // Don't cut off cooling when it turns off
                if (realMaxPower() != this.getDecayHeat() && realMaxPower() != 0) {
                    idealHeatFlux = Math.min(idealHeatFlux, realMaxPower() * 1e6 / coolantChannels.size());
                }

                double idealFluidUsed = idealHeatFlux / heatRemovedPerLiter;
                double cappedFluidUsed = Math.min(drained, idealFluidUsed);

                int remainingSpace = out.getCapacity() -
                        out.getFluidAmount();
                int actualFlowRate = Math.min(remainingSpace,
                        (int) (cappedFluidUsed + channel.partialCoolant));
                // Should occasionally decrease when coolant is actually consumed.
                channel.partialCoolant += cappedFluidUsed - actualFlowRate;

                FluidStack HPCoolant = new FluidStack(
                        prop.getHotCoolant(), actualFlowRate);

                in.drain(actualFlowRate, true);
                out.fill(HPCoolant, true);
                if (prop.accumulatesHydrogen() &&
                        this.temperature > zircaloyHydrogenReactionTemperature) {
                    double boilingPoint = coolantBoilingPoint(prop);
                    if (this.temperature > boilingPoint) {
                        this.accumulatedHydrogen += (this.temperature - boilingPoint) / boilingPoint;
                    } else if (actualFlowRate < Math.min(remainingSpace, idealFluidUsed)) {
                        this.accumulatedHydrogen += (this.temperature - zircaloyHydrogenReactionTemperature) /
                                zircaloyHydrogenReactionTemperature;
                    }
                }

                this.coolantMass += cappedFluidUsed * prop.getMass();
                heatRemoved += cappedFluidUsed * heatRemovedPerLiter;
            }
        }
        this.coolantMass /= 1000;
        this.accumulatedHydrogen *= 0.98;
        return heatRemoved;
    }

    /**
     * The thermodynamics is not completely realistic, but it's close enough for simple things like this, the boiling
     * point depends on pressure
     */
    protected double coolantBoilingPoint() {
        return 1. / (1. / this.coolantBoilingPointStandardPressure -
                R * Math.log(this.pressure / standardPressure) / this.coolantHeatOfVaporization);
    }

    protected double coolantBoilingPoint(ICoolantStats coolant) {
        if (coolant.getBoilingPoint() == 0) {
            return coolantBoilingPoint();
        }
        return 1. / (1. / coolant.getBoilingPoint() -
                R * Math.log(this.pressure / standardPressure) /
                        coolant.getHeatOfVaporization());
    }

    public void updateTemperature(int flowRate) {
        this.prevTemperature = this.temperature;
        // simulate heat based only on the reactor power
        this.temperature = responseFunctionTemperature(envTemperature, this.temperature, this.power * 1e6, 0);
        // prevent temperature from going above meltdown temp, to stop coolant from absorbing more heat than it should
        this.temperature = Math.min(maxTemperature, temperature);
        double heatRemoved = this.makeCoolantFlow(flowRate);
        // calculate the actual temperature based on the reactor power and the heat removed
        this.temperature = responseFunctionTemperature(envTemperature, prevTemperature, this.power * 1e6, heatRemoved);
        this.temperature = Math.max(this.temperature, this.coolantBaseTemperature);
    }

    public void updatePressure() {
        this.pressure = responseFunction(
                !(this.temperature <= this.coolantBoilingPoint()) && this.isOn ? 1000. * R * this.temperature :
                        this.exteriorPressure,
                this.pressure);
    }

    public void updateNeutronPoisoning() {
        this.neutronPoisonAmount += this.decayProductsAmount * (1 - decayProductRate) * poisonFraction;
        this.neutronPoisonAmount *= decayProductRate * Math.exp(-crossSectionRatio * power / surfaceArea);
    }

    public double getDecayHeat() {
        return this.neutronPoisonAmount * 0.05 + this.decayProductsAmount * 0.1 + 0.0001; // The extra constant is to
        // kickstart the reactor.
    }

    public void updatePower() {
        if (this.isOn) {
            this.kEff = this.k;
            // Since the power defect is a change in the reactivity rho (1 - 1 / kEff), we have to do this thing.
            // (1 - 1 / k) = rho(k) => rho^-1(rho) = 1 / (1 - rho)
            // rho^-1(rho(k) - defect) is thus 1 / (1 - (1 - 1/k - defect)) = 1 / (1/k + defect)
            this.kEff = 1 / ((1 / this.kEff) + powerDefectCoefficient * (this.power / this.maxPower) +
                    neutronPoisonAmount * crossSectionRatio / surfaceArea + controlRodFactor);
            this.kEff = Math.max(0, this.kEff);

            double inverseReactorPeriod = (this.kEff - 1) / weightedGenerationTime;

            this.power += 0.001; // Let it kickstart itself
            this.power *= Math.exp(inverseReactorPeriod);

            this.fuelDepletion += this.power;

            this.decayProductsAmount += Math.max(power, 0.) / 1000;
        } else {
            this.power *= 0.5;
        }
        this.decayProductsAmount *= decayProductRate;
    }

    public double realMaxPower() {
        if (this.moderatorTipped && (this.controlRodInsertion <= 9. / 16 && this.controlRodInsertion >= 7. / 16)) {
            return this.maxPower * 1.1;
        } else if (!this.isOn) {
            return this.getDecayHeat();
        } else {
            return this.maxPower;
        }
    }

    public boolean checkForMeltdown() {
        return this.temperature > this.maxTemperature;
    }

    public boolean checkForExplosion() {
        return this.pressure > this.maxPressure;
    }

    public void addComponent(ReactorComponent component, int x, int y) {
        reactorLayout[x][y] = component;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setDouble("Temperature", this.temperature);
        tagCompound.setDouble("PrevTemperature", this.prevTemperature);
        tagCompound.setDouble("Pressure", this.pressure);
        tagCompound.setDouble("Power", this.power);
        tagCompound.setDouble("FuelDepletion", this.fuelDepletion);
        tagCompound.setDouble("AccumulatedHydrogen", this.accumulatedHydrogen);
        tagCompound.setDouble("NeutronPoisonAmount", this.neutronPoisonAmount);
        tagCompound.setDouble("DecayProductsAmount", this.decayProductsAmount);
        tagCompound.setBoolean("NeedsOutput", this.needsOutput);
        tagCompound.setDouble("ControlRodInsertion", this.controlRodInsertion);
        tagCompound.setBoolean("IsOn", this.isOn);
        tagCompound.setBoolean("ControlRodRegulationOn", this.controlRodRegulationOn);

        return tagCompound;
    }

    public void deserializeNBT(NBTTagCompound tagCompound) {
        this.temperature = tagCompound.getDouble("Temperature");
        this.prevTemperature = tagCompound.getDouble("PrevTemperature");
        this.pressure = tagCompound.getDouble("Pressure");
        this.power = tagCompound.getDouble("Power");
        this.fuelDepletion = tagCompound.getDouble("FuelDepletion");
        this.accumulatedHydrogen = tagCompound.getDouble("AccumulatedHydrogen");
        this.neutronPoisonAmount = tagCompound.getDouble("NeutronPoisonAmount");
        this.decayProductsAmount = tagCompound.getDouble("DecayProductsAmount");
        this.needsOutput = tagCompound.getBoolean("NeedsOutput");
        this.controlRodInsertion = tagCompound.getDouble("ControlRodInsertion");
        this.isOn = tagCompound.getBoolean("IsOn");
        this.controlRodRegulationOn = tagCompound.getBoolean("ControlRodRegulationOn");
    }

    public void updateControlRodInsertion(double controlRodInsertion) {
        this.controlRodInsertion = Math.max(0.001, controlRodInsertion);
        this.controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods, this.controlRodInsertion);
    }

    public void regulateControlRods() {
        if (!this.isOn || !this.controlRodRegulationOn)
            return;

        boolean adjustFactor = false;
        if (pressure > maxPressure * 0.8 || temperature > (coolantExitTemperature + maxTemperature) / 2 ||
                temperature > maxTemperature - 150 || temperature - prevTemperature > 30) {
            if (kEff > 1) {
                this.controlRodInsertion += 0.004;
                adjustFactor = true;
            }
        } else if (temperature > coolantExitTemperature * 0.3 + coolantBaseTemperature * 0.7) {
            if (kEff > 1.01) {
                this.controlRodInsertion += 0.008;
                adjustFactor = true;
            } else if (kEff < 1.005) {
                this.controlRodInsertion -= 0.001;
                adjustFactor = true;
            }
        } else if (temperature > coolantExitTemperature * 0.1 + coolantBaseTemperature * 0.9) {
            if (kEff > 1.025) {
                this.controlRodInsertion += 0.012;
                adjustFactor = true;
            } else if (kEff < 1.015) {
                this.controlRodInsertion -= 0.004;
                adjustFactor = true;
            }
        } else {
            if (kEff > 1.1) {
                this.controlRodInsertion += 0.02;
                adjustFactor = true;
            } else if (kEff < 1.05) {
                this.controlRodInsertion -= 0.006;
                adjustFactor = true;
            }
        }

        if (adjustFactor) {
            this.controlRodInsertion = Math.max(0, Math.min(1, this.controlRodInsertion));
            this.controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods, this.controlRodInsertion);
        }
    }

    public void turnOff() {
        this.isOn = false;
        this.maxPower = 0;
        this.k = 0;
        this.kEff = 0;
        this.coolantMass = 0;
        this.fuelMass = 0;
        for (ReactorComponent[] components : reactorLayout) {
            Arrays.fill(components, null);
        }
        fuelRods.clear();
        controlRods.clear();
        coolantChannels.clear();
        effectiveControlRods.clear();
        effectiveCoolantChannels.clear();
    }
}
