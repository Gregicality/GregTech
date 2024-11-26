package gregtech.api.metatileentity.multiblock;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widget.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

public class MultiblockDisplayTextPort {

    /**
     * Construct a new Multiblock Display Text builder.
     * <br>
     * Automatically adds the "Invalid Structure" line if the structure is not formed.
     */
    public static Builder builder(List<Widget<?>> textList, MultiblockWithDisplayBase mte) {
        return builder(textList, mte, true);
    }

    public static Builder builder(List<Widget<?>> textList, MultiblockWithDisplayBase mte,
                                  boolean showIncompleteStructureWarning) {
        return new Builder(textList, mte, showIncompleteStructureWarning, true);
    }

    public static Builder builder(List<Widget<?>> textList, MultiblockWithDisplayBase mte,
                                  boolean showIncompleteStructureWarning, boolean showTitle) {
        return new Builder(textList, mte, showIncompleteStructureWarning, showTitle);
    }

    public static class Builder {

        private final List<Widget<?>> textList;
        private Function<IKey, Widget<?>> widgetFunction = Builder::keyMapper;
        private final boolean isStructureFormed;

        private boolean isWorkingEnabled, isActive;

        // Keys for the three-state working system, can be set custom by multiblocks.
        private IKey idlingKey = IKey.lang("gregtech.multiblock.idling");
        private IKey pausedKey = IKey.lang("gregtech.multiblock.work_paused");
        private IKey runningKey = IKey.lang("gregtech.multiblock.running");

        private Builder(List<Widget<?>> textList, MultiblockWithDisplayBase mte,
                        boolean showIncompleteStructureWarning, boolean showTitle) {
            this.textList = textList;
            this.isStructureFormed = mte.isStructureFormed();

            if (showTitle)
                addKey(KeyUtil.lang(TextFormatting.WHITE, mte.getMetaFullName()));

            if (!isStructureFormed && showIncompleteStructureWarning) {
                var base = KeyUtil.lang(TextFormatting.RED, "gregtech.multiblock.invalid_structure");
                var hover = KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.invalid_structure.tooltip");
                addKey(base).addTooltipLine(hover);
            }
        }

        public static Widget<?> keyMapper(IKey key) {
            return key.asWidget()
                    .widthRel(1f)
                    .height(12);
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
            if (idlingKey != null) this.idlingKey = IKey.str(idlingKey);
            if (pausedKey != null) this.pausedKey = IKey.str(pausedKey);
            if (runningKey != null) this.runningKey = IKey.str(runningKey);
            return this;
        }

        /**
         * Adds the max EU/t that this multiblock can use.
         * <br>
         * Added if the structure is formed and if the passed energy container has greater than zero capacity.
         */
        public Builder addEnergyUsageLine(IEnergyContainer energyContainer) {
            if (!isStructureFormed || energyContainer == null) return this;

            if (energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());

                String energyFormatted = TextFormattingUtil.formatNumbers(maxVoltage);
                // wrap in text component to keep it from being formatted
                IKey voltageName = IKey.str(GTValues.VOCNF[GTUtility.getFloorTierByVoltage(maxVoltage)]);

                var bodyText = KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.max_energy_per_tick", energyFormatted, voltageName);
                var hoverText = KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.max_energy_per_tick_hover");
                addKey(bodyText).addTooltipLine(hoverText);
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

            var bodyText = KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.max_recipe_tier", GTValues.VNF[tier]);
            var hoverText = KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.max_recipe_tier_hover");
            addKey(bodyText).addTooltipLine(hoverText);
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
                var voltageName = KeyUtil.string(
                        GTValues.VOCNF[GTUtility.getOCTierByVoltage(energyUsage)]);

                addKey(KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.energy_consumption", energyFormatted, voltageName));
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
            if (maxVoltage != 0 && maxVoltage >= -recipeEUt) {
                String energyFormatted = TextFormattingUtil.formatNumbers(maxVoltage);
                // wrap in text component to keep it from being formatted
                var voltageName = KeyUtil.string(
                        GTValues.VOCNF[GTUtility.getFloorTierByVoltage(maxVoltage)]);

                addKey(KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.max_energy_per_tick", energyFormatted, voltageName));
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
                var voltageName = KeyUtil.string(
                        GTValues.VOCNF[GTUtility.getFloorTierByVoltage(maxVoltage)]);

                addKey(KeyUtil.lang(TextFormatting.GRAY,
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
                var computation = KeyUtil.string(TextFormatting.AQUA, TextFormattingUtil.formatNumbers(maxCWUt));
                addKey(KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.computation.max", computation));
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
                var computation = KeyUtil.string(TextFormatting.AQUA,
                        TextFormattingUtil.formatNumbers(currentCWUt) + " CWU/t");
                addKey(KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.computation.usage", computation));
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
                addKey(KeyUtil.colored(TextFormatting.GOLD, pausedKey));
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
                addKey(KeyUtil.colored(TextFormatting.GREEN, runningKey));
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
                addKey(KeyUtil.colored(TextFormatting.GRAY, idlingKey));
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
        public Builder addProgressLine(DoubleSupplier progressPercent) { // todo
            if (!isStructureFormed || !isActive) return this;
            addKey(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.progress",
                    () -> ((int) (progressPercent.getAsDouble() * 100))));
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
                var parallels = KeyUtil.string(TextFormatting.DARK_PURPLE,
                        TextFormattingUtil.formatNumbers(numParallels));

                addKey(KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.parallel", parallels));
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
                addKey(KeyUtil.lang(TextFormatting.YELLOW,
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
                addKey(IKey.comp(IKey.str(TextFormatting.YELLOW.toString()),
                        IKey.lang("gregtech.multiblock.computation.not_enough_computation")));
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
                addKey(KeyUtil.lang(TextFormatting.YELLOW,
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
                addKey(KeyUtil.lang(TextFormatting.YELLOW,
                        "gregtech.multiblock.universal.has_problems"));

                // Wrench
                if ((maintenanceProblems & 1) == 0) {
                    addKey(KeyUtil.lang(TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.wrench"));
                }

                // Screwdriver
                if (((maintenanceProblems >> 1) & 1) == 0) {
                    addKey(KeyUtil.lang(TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.screwdriver"));
                }

                // Soft Mallet
                if (((maintenanceProblems >> 2) & 1) == 0) {
                    addKey(KeyUtil.lang(TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.soft_mallet"));
                }

                // Hammer
                if (((maintenanceProblems >> 3) & 1) == 0) {
                    addKey(KeyUtil.lang(TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.hard_hammer"));
                }

                // Wire Cutters
                if (((maintenanceProblems >> 4) & 1) == 0) {
                    addKey(KeyUtil.lang(TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.wire_cutter"));
                }

                // Crowbar
                if (((maintenanceProblems >> 5) & 1) == 0) {
                    addKey(KeyUtil.lang(TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.crowbar"));
                }
            }
            return this;
        }

        /**
         * Adds two error lines when the machine's muffler hatch is obstructed.
         * <br>
         * Added if the structure is formed and if the passed parameter is true.
         */
        public Builder addMufflerObstructedLine(boolean isObstructed) {
            if (!isStructureFormed) return this;
            if (isObstructed) {
                addKey(KeyUtil.lang(TextFormatting.RED,
                        "gregtech.multiblock.universal.muffler_obstructed"));
                addKey(KeyUtil.lang(TextFormatting.GRAY,
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

            addKey(KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.turbine.fuel_needed",
                    KeyUtil.string(TextFormatting.RED, fuelName),
                    KeyUtil.number(TextFormatting.AQUA, previousRecipeDuration)));

            return this;
        }

        /** Insert an empty line into the text list. */
        public Builder addEmptyLine() {
            addKey(IKey.EMPTY); // this is going to cause problems maybe
            return this;
        }

        /** Add custom text dynamically, allowing for custom application logic. */
        public Builder addCustom(Consumer<List<IKey>> customConsumer) {
            List<IKey> customKeys = new ArrayList<>();
            customConsumer.accept(customKeys);
            customKeys.forEach(this::addKey);
            return this;
        }

        private Widget<?> addKey(IKey key) {
            var w = this.widgetFunction.apply(key);
            this.textList.add(w);
            return w;
        }

        public Builder widgetFunction(Function<IKey, Widget<?>> widgetFunction) {
            this.widgetFunction = widgetFunction;
            return this;
        }
    }
}
