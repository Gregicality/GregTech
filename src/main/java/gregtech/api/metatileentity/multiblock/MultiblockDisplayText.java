package gregtech.api.metatileentity.multiblock;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.util.text.*;

import java.util.List;
import java.util.function.Consumer;

public class MultiblockDisplayText {

    private static final ITextComponent EMPTY_COMPONENT = new TextComponentString("");

    /**
     * Construct a new Multiblock Display Text builder.
     * <br>
     * Automatically adds the "Invalid Structure" line if the structure is not formed.
     */
    public static Builder builder(List<ITextComponent> textList, boolean isStructureFormed) {
        return builder(textList, isStructureFormed, true);
    }

    public static Builder builder(List<ITextComponent> textList, boolean isStructureFormed,
                                  boolean showIncompleteStructureWarning) {
        return new Builder(textList, isStructureFormed, showIncompleteStructureWarning);
    }

    public static class Builder {

        private final List<ITextComponent> textList;
        private final boolean isStructureFormed;

        private boolean isWorkingEnabled, isActive;

        // Keys for the three-state working system, can be set custom by multiblocks.
        private String idlingKey = "gregtech.multiblock.idling";
        private String pausedKey = "gregtech.multiblock.work_paused";
        private String runningKey = "gregtech.multiblock.running";

        private Builder(List<ITextComponent> textList, boolean isStructureFormed,
                        boolean showIncompleteStructureWarning) {
            this.textList = textList;
            this.isStructureFormed = isStructureFormed;

            if (!isStructureFormed && showIncompleteStructureWarning) {
                ITextComponent base = TextComponentUtil.translationWithColor(TextFormatting.RED,
                        "gregtech.multiblock.invalid_structure");
                ITextComponent hover = TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                        "gregtech.multiblock.invalid_structure.tooltip");
                textList.add(TextComponentUtil.setHover(base, hover));
            }
        }

        /** Set the current working enabled and active status of this multiblock, used by many line addition calls. */
        public Builder setWorkingStatus(boolean isWorkingEnabled, boolean isActive) {
            this.isWorkingEnabled = isWorkingEnabled;
            this.isActive = isActive;
            return this;
        }

        /**
         * Set custom translation keys for the three-state "Idling", "Paused", "Running" display text.
         * <strong>You still must call {@link Builder#addWorkingStatusLine()} for these to appear!</strong>
         * <br>
         * Pass any key as null for it to continue to use the default key.
         *
         * @param idlingKey  The translation key for the Idle state, or "!isActive && isWorkingEnabled".
         * @param pausedKey  The translation key for the Paused state, or "!isWorkingEnabled".
         * @param runningKey The translation key for the Running state, or "isActive".
         */
        public Builder setWorkingStatusKeys(String idlingKey, String pausedKey, String runningKey) {
            if (idlingKey != null) this.idlingKey = idlingKey;
            if (pausedKey != null) this.pausedKey = pausedKey;
            if (runningKey != null) this.runningKey = runningKey;
            return this;
        }

        /**
         * Adds the max EU/t that this multiblock can use.
         * <br>
         * Added if the structure is formed and if the passed energy container has greater than zero capacity.
         */
        public Builder addEnergyUsageLine(IEnergyContainer energyContainer) {
            if (!isStructureFormed) return this;
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());

                String energyFormatted = TextFormattingUtil.formatNumbers(maxVoltage);
                // wrap in text component to keep it from being formatted
                ITextComponent voltageName = new TextComponentString(
                        GTValues.VOCNF[GTUtility.getFloorTierByVoltage(maxVoltage)]);

                ITextComponent bodyText = TextComponentUtil.translationWithColor(
                        TextFormatting.GRAY,
                        "gregtech.multiblock.max_energy_per_tick",
                        energyFormatted, voltageName);
                ITextComponent hoverText = TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                        "gregtech.multiblock.max_energy_per_tick_hover");
                textList.add(TextComponentUtil.setHover(bodyText, hoverText));
            }
            return this;
        }

        /**
         * Adds the max Recipe Tier that this multiblock can use for recipe lookup.
         * <br>
         * Added if the structure is formed and if the passed tier is a valid energy tier index for
         * {@link GTValues#VNF}.
         */
        public Builder addEnergyTierLine(int tier) {
            if (!isStructureFormed) return this;
            if (tier < GTValues.ULV || tier > GTValues.MAX) return this;

            ITextComponent voltageName = new TextComponentString(GTValues.VNF[tier]);
            ITextComponent bodyText = TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "gregtech.multiblock.max_recipe_tier",
                    voltageName);
            ITextComponent hoverText = TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                    "gregtech.multiblock.max_recipe_tier_hover");
            textList.add(TextComponentUtil.setHover(bodyText, hoverText));
            return this;
        }

        /**
         * Adds the exact EU/t that this multiblock needs to run.
         * <br>
         * Added if the structure is formed and if the passed value is greater than zero.
         */
        public Builder addEnergyUsageExactLine(long energyUsage) {
            if (!isStructureFormed) return this;
            if (energyUsage > 0) {
                String energyFormatted = TextFormattingUtil.formatNumbers(energyUsage);
                // wrap in text component to keep it from being formatted
                ITextComponent voltageName = new TextComponentString(
                        GTValues.VOCNF[GTUtility.getOCTierByVoltage(energyUsage)]);

                textList.add(TextComponentUtil.translationWithColor(
                        TextFormatting.GRAY,
                        "gregtech.multiblock.energy_consumption",
                        energyFormatted, voltageName));
            }
            return this;
        }

        /**
         * Adds the max EU/t that this multiblock can produce.
         * <br>
         * Added if the structure is formed and if the max voltage is greater than zero and the recipe EU/t.
         */
        public Builder addEnergyProductionLine(long maxVoltage, long recipeEUt) {
            if (!isStructureFormed) return this;
            if (maxVoltage != 0 && maxVoltage >= recipeEUt) {
                String energyFormatted = TextFormattingUtil.formatNumbers(maxVoltage);
                // wrap in text component to keep it from being formatted
                ITextComponent voltageName = new TextComponentString(
                        GTValues.VOCNF[GTUtility.getFloorTierByVoltage(maxVoltage)]);

                textList.add(TextComponentUtil.translationWithColor(
                        TextFormatting.GRAY,
                        "gregtech.multiblock.max_energy_per_tick",
                        energyFormatted, voltageName));
            }
            return this;
        }

        /**
         * Adds the max EU/t that this multiblock can produce, including how many amps. Recommended for multi-amp
         * outputting multis.
         * <br>
         * Added if the structure is formed, if the amperage is greater than zero and if the max voltage is greater than
         * zero.
         */
        public Builder addEnergyProductionAmpsLine(long maxVoltage, int amperage) {
            if (!isStructureFormed) return this;
            if (maxVoltage != 0 && amperage != 0) {
                String energyFormatted = TextFormattingUtil.formatNumbers(maxVoltage);
                // wrap in text component to keep it from being formatted
                ITextComponent voltageName = new TextComponentString(
                        GTValues.VOCNF[GTUtility.getFloorTierByVoltage(maxVoltage)]);

                textList.add(TextComponentUtil.translationWithColor(
                        TextFormatting.GRAY,
                        "gregtech.multiblock.max_energy_per_tick_amps",
                        energyFormatted, amperage, voltageName));
            }
            return this;
        }

        /**
         * Adds the max CWU/t that this multiblock can use.
         * <br>
         * Added if the structure is formed and if the max CWU/t is greater than zero.
         */
        public Builder addComputationUsageLine(int maxCWUt) {
            if (!isStructureFormed) return this;
            if (maxCWUt > 0) {
                ITextComponent computation = TextComponentUtil.stringWithColor(TextFormatting.AQUA,
                        TextFormattingUtil.formatNumbers(maxCWUt));
                textList.add(TextComponentUtil.translationWithColor(
                        TextFormatting.GRAY,
                        "gregtech.multiblock.computation.max",
                        computation));
            }
            return this;
        }

        /**
         * Adds a currently used CWU/t line.
         * <br>
         * Added if the structure is formed, the machine is active, and the current CWU/t is greater than zero.
         */
        public Builder addComputationUsageExactLine(int currentCWUt) {
            if (!isStructureFormed) return this;
            if (isActive && currentCWUt > 0) {
                ITextComponent computation = TextComponentUtil.stringWithColor(TextFormatting.AQUA,
                        TextFormattingUtil.formatNumbers(currentCWUt) + " CWU/t");
                textList.add(TextComponentUtil.translationWithColor(
                        TextFormatting.GRAY,
                        "gregtech.multiblock.computation.usage",
                        computation));
            }
            return this;
        }

        /**
         * Adds a three-state indicator line, showing if the machine is running, paused, or idling.
         * <br>
         * Added if the structure is formed.
         */
        public Builder addWorkingStatusLine() {
            if (!isStructureFormed) return this;

            if (!isWorkingEnabled) {
                return addWorkPausedLine(false);
            } else if (isActive) {
                return addRunningPerfectlyLine(false);
            } else {
                return addIdlingLine(false);
            }
        }

        /**
         * Adds the "Work Paused." line.
         * <br>
         * Added if working is not enabled, or if the checkState passed parameter is false.
         * Also added only if formed.
         */
        public Builder addWorkPausedLine(boolean checkState) {
            if (!isStructureFormed) return this;
            if (!checkState || !isWorkingEnabled) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.GOLD, pausedKey));
            }
            return this;
        }

        /**
         * Adds the "Running Perfectly." line.
         * <br>
         * Added if machine is active, or if the checkState passed parameter is false.
         * Also added only if formed.
         */
        public Builder addRunningPerfectlyLine(boolean checkState) {
            if (!isStructureFormed) return this;
            if (!checkState || isActive) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.GREEN, runningKey));
            }
            return this;
        }

        /**
         * Adds the "Idling." line.
         * <br>
         * Added if the machine is not active and working is enabled, or if the checkState passed parameter is false.
         * Also added only if formed.
         */
        public Builder addIdlingLine(boolean checkState) {
            if (!isStructureFormed) return this;
            if (!checkState || (isWorkingEnabled && !isActive)) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, idlingKey));
            }
            return this;
        }

        /**
         * Adds a simple progress line that displays progress as a percentage.
         * <br>
         * Added if structure is formed and the machine is active.
         *
         * @param progressPercent Progress formatted as a range of [0,1] representing the progress of the recipe.
         */
        public Builder addProgressLine(double progressPercent) { // todo
            if (!isStructureFormed || !isActive) return this;
            int currentProgress = (int) (progressPercent * 100);
            textList.add(new TextComponentTranslation("gregtech.multiblock.progress", currentProgress));
            return this;
        }

        /**
         * Adds a line indicating how many parallels this multi can potentially perform.
         * <br>
         * Added if structure is formed and the number of parallels is greater than one.
         */
        public Builder addParallelsLine(int numParallels) {
            if (!isStructureFormed) return this;
            if (numParallels > 1) {
                ITextComponent parallels = TextComponentUtil.stringWithColor(
                        TextFormatting.DARK_PURPLE,
                        TextFormattingUtil.formatNumbers(numParallels));

                textList.add(TextComponentUtil.translationWithColor(
                        TextFormatting.GRAY,
                        "gregtech.multiblock.parallel",
                        parallels));
            }
            return this;
        }

        /**
         * Adds a warning line when the machine is low on power.
         * <br>
         * Added if the structure is formed and if the passed parameter is true.
         */
        public Builder addLowPowerLine(boolean isLowPower) {
            if (!isStructureFormed) return this;
            if (isLowPower) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                        "gregtech.multiblock.not_enough_energy"));
            }
            return this;
        }

        /**
         * Adds a warning line when the machine is low on computation.
         * <br>
         * Added if the structure is formed and if the passed parameter is true.
         */
        public Builder addLowComputationLine(boolean isLowComputation) {
            if (!isStructureFormed) return this;
            if (isLowComputation) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                        "gregtech.multiblock.computation.not_enough_computation"));
            }
            return this;
        }

        /**
         * Adds a warning line when the machine's dynamo tier is too low for current conditions.
         * <br>
         * Added if the structure is formed and if the passed parameter is true.
         */
        public Builder addLowDynamoTierLine(boolean isTooLow) {
            if (!isStructureFormed) return this;
            if (isTooLow) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                        "gregtech.multiblock.not_enough_energy_output"));
            }
            return this;
        }

        /**
         * Adds warning line(s) when the machine has maintenance problems.
         * <br>
         * Added if there are any maintenance problems, one line per problem as well as a header. <br>
         * Will check the config setting for if maintenance is enabled automatically.
         */
        public Builder addMaintenanceProblemLines(byte maintenanceProblems) {
            if (!isStructureFormed || !ConfigHolder.machines.enableMaintenance) return this;
            if (maintenanceProblems < 63) {
                boolean hasAddedHeader = false;

                // Wrench
                if ((maintenanceProblems & 1) == 0) {
                    hasAddedHeader = addMaintenanceProblemHeader(hasAddedHeader);
                    textList.add(TextComponentUtil.translationWithColor(
                            TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.wrench"));
                }

                // Screwdriver
                if (((maintenanceProblems >> 1) & 1) == 0) {
                    hasAddedHeader = addMaintenanceProblemHeader(hasAddedHeader);
                    textList.add(TextComponentUtil.translationWithColor(
                            TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.screwdriver"));
                }

                // Soft Mallet
                if (((maintenanceProblems >> 2) & 1) == 0) {
                    hasAddedHeader = addMaintenanceProblemHeader(hasAddedHeader);
                    textList.add(TextComponentUtil.translationWithColor(
                            TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.soft_mallet"));
                }

                // Hammer
                if (((maintenanceProblems >> 3) & 1) == 0) {
                    hasAddedHeader = addMaintenanceProblemHeader(hasAddedHeader);
                    textList.add(TextComponentUtil.translationWithColor(
                            TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.hard_hammer"));
                }

                // Wire Cutters
                if (((maintenanceProblems >> 4) & 1) == 0) {
                    hasAddedHeader = addMaintenanceProblemHeader(hasAddedHeader);
                    textList.add(TextComponentUtil.translationWithColor(
                            TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.wire_cutter"));
                }

                // Crowbar
                if (((maintenanceProblems >> 5) & 1) == 0) {
                    addMaintenanceProblemHeader(hasAddedHeader);
                    textList.add(TextComponentUtil.translationWithColor(
                            TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.crowbar"));
                }
            }
            return this;
        }

        private boolean addMaintenanceProblemHeader(boolean hasAddedHeader) {
            if (!hasAddedHeader) {
                textList.add(TextComponentUtil.translationWithColor(
                        TextFormatting.YELLOW,
                        "gregtech.multiblock.universal.has_problems"));
            }
            return true;
        }

        /**
         * Adds two error lines when the machine's muffler hatch is obstructed.
         * <br>
         * Added if the structure is formed and if the passed parameter is true.
         */
        public Builder addMufflerObstructedLine(boolean isObstructed) {
            if (!isStructureFormed) return this;
            if (isObstructed) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.RED,
                        "gregtech.multiblock.universal.muffler_obstructed"));
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                        "gregtech.multiblock.universal.muffler_obstructed_desc"));
            }
            return this;
        }

        /**
         * Adds a fuel consumption line showing the fuel name and the number of ticks per recipe run.
         * <br>
         * Added if structure is formed, the machine is active, and the passed fuelName parameter is not null.
         */
        public Builder addFuelNeededLine(String fuelName, int previousRecipeDuration) {
            if (!isStructureFormed || !isActive || fuelName == null) return this;
            ITextComponent fuelNeeded = TextComponentUtil.stringWithColor(TextFormatting.RED, fuelName);
            ITextComponent numTicks = TextComponentUtil.stringWithColor(TextFormatting.AQUA,
                    TextFormattingUtil.formatNumbers(previousRecipeDuration));
            textList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "gregtech.multiblock.turbine.fuel_needed",
                    fuelNeeded, numTicks));
            return this;
        }

        public Builder addStructureTierLine(int tier) {
            textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                    "gregtech.multiblock.structure_tier", tier));
            return this;
        }

        /** Insert an empty line into the text list. */
        public Builder addEmptyLine() {
            textList.add(EMPTY_COMPONENT);
            return this;
        }

        /** Add custom text dynamically, allowing for custom application logic. */
        public Builder addCustom(Consumer<List<ITextComponent>> customConsumer) {
            customConsumer.accept(textList);
            return this;
        }
    }
}
