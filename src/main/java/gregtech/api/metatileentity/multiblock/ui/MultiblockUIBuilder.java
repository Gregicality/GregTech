package gregtech.api.metatileentity.multiblock.ui;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.util.JsonUtils;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings({ "UnusedReturnValue", "unused" })
public class MultiblockUIBuilder implements KeyManager {

    private final List<IDrawable> textList = new ArrayList<>();
    private final List<Operation> operations = new ArrayList<>();

    private Consumer<MultiblockUIBuilder> action;
    private final SyncHandler syncHandler = makeSyncHandler();

    private boolean isWorkingEnabled;
    private boolean isActive;
    private boolean isStructureFormed;

    // Keys for the three-state working system, can be set custom by multiblocks.
    private IKey idlingKey = IKey.lang("gregtech.multiblock.idling").style(TextFormatting.GRAY);
    private IKey pausedKey = IKey.lang("gregtech.multiblock.work_paused").style(TextFormatting.GOLD);
    private IKey runningKey = IKey.lang("gregtech.multiblock.running").style(TextFormatting.GREEN);
    private boolean dirty;
    private Runnable onRebuild;

    void updateFormed(boolean isStructureFormed) {
        this.isStructureFormed = isStructureFormed;
    }

    public MultiblockUIBuilder structureFormed(boolean structureFormed) {
        updateFormed(structureFormed);
        if (!structureFormed) {
            var base = KeyUtil.lang(TextFormatting.RED, "gregtech.multiblock.invalid_structure");
            var hover = KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.invalid_structure.tooltip");
            addKey(base, hover);
        }
        return this;
    }

    public MultiblockUIBuilder title(String lang) {
        addKey(KeyUtil.lang(TextFormatting.WHITE, lang));
        return this;
    }

    /** Set the current working enabled and active status of this multiblock, used by many line addition calls. */
    public MultiblockUIBuilder setWorkingStatus(boolean isWorkingEnabled, boolean isActive) {
        this.isWorkingEnabled = isWorkingEnabled;
        this.isActive = isActive;
        return this;
    }

    /**
     * Set custom translation keys for the three-state "Idling", "Paused", "Running" display text.
     * <strong>You still must call {@link MultiblockUIBuilder#addWorkingStatusLine()} for these to appear!</strong>
     * <br>
     * Pass any key as null for it to continue to use the default key.
     *
     * @param idlingKey  The translation key for the Idle state, or "!isActive && isWorkingEnabled".
     * @param pausedKey  The translation key for the Paused state, or "!isWorkingEnabled".
     * @param runningKey The translation key for the Running state, or "isActive".
     */
    public MultiblockUIBuilder setWorkingStatusKeys(String idlingKey, String pausedKey, String runningKey) {
        if (idlingKey != null) this.idlingKey = IKey.lang(idlingKey).style(TextFormatting.GRAY);
        if (pausedKey != null) this.pausedKey = IKey.lang(pausedKey).style(TextFormatting.GOLD);
        if (runningKey != null) this.runningKey = IKey.lang(runningKey).style(TextFormatting.GREEN);
        return this;
    }

    /**
     * Adds the max EU/t that this multiblock can use.
     * <br>
     * Added if the structure is formed and if the passed energy container has greater than zero capacity.
     */
    public MultiblockUIBuilder addEnergyUsageLine(IEnergyContainer energyContainer) {
        if (!isStructureFormed || energyContainer == null) return this;
        if (energyContainer.getEnergyCapacity() <= 0) return this;

        long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());

        IKey bodyText = KeyUtil.lang(TextFormatting.GRAY,
                "gregtech.multiblock.max_energy_per_tick",
                KeyUtil.number(maxVoltage),
                KeyUtil.voltage(GTValues.VOCNF, maxVoltage));

        var hoverText = KeyUtil.lang(TextFormatting.GRAY,
                "gregtech.multiblock.max_energy_per_tick_hover");

        addKey(bodyText, hoverText);
        return this;
    }

    /**
     * Adds the max Recipe Tier that this multiblock can use for recipe lookup.
     * <br>
     * Added if the structure is formed and if the passed tier is a valid energy tier index for {@link GTValues#VNF}.
     */
    public MultiblockUIBuilder addEnergyTierLine(int tier) {
        if (!isStructureFormed) return this;
        if (tier < GTValues.ULV || tier > GTValues.MAX) return this;

        var bodyText = KeyUtil.lang(TextFormatting.GRAY,
                "gregtech.multiblock.max_recipe_tier", GTValues.VNF[tier]);
        var hoverText = KeyUtil.lang(TextFormatting.GRAY,
                "gregtech.multiblock.max_recipe_tier_hover");
        addKey(bodyText, hoverText);
        return this;
    }

    /**
     * Adds the exact EU/t that this multiblock needs to run.
     * <br>
     * Added if the structure is formed and if the passed value is greater than zero.
     */
    public MultiblockUIBuilder addEnergyUsageExactLine(long energyUsage) {
        if (!isStructureFormed) return this;
        if (energyUsage > 0) {
            String energyFormatted = TextFormattingUtil.formatNumbers(energyUsage);
            // wrap in text component to keep it from being formatted
            var voltageName = KeyUtil.overclock(GTValues.VOCNF, energyUsage);

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
    public MultiblockUIBuilder addEnergyProductionLine(long maxVoltage, long recipeEUt) {
        if (!isStructureFormed) return this;
        if (maxVoltage != 0 && maxVoltage >= -recipeEUt) {
            String energyFormatted = TextFormattingUtil.formatNumbers(maxVoltage);
            // wrap in text component to keep it from being formatted
            var voltageName = KeyUtil.voltage(GTValues.VOCNF, maxVoltage);

            addKey(KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.max_energy_per_tick", energyFormatted, voltageName));
        }
        return this;
    }

    /**
     * Adds the max EU/t that this multiblock can produce, including how many amps. Recommended for multi-amp outputting
     * multis.
     * <br>
     * Added if the structure is formed, if the amperage is greater than zero and if the max voltage is greater than
     * zero.
     */
    public MultiblockUIBuilder addEnergyProductionAmpsLine(long maxVoltage, int amperage) {
        if (!isStructureFormed) return this;
        if (maxVoltage != 0 && amperage != 0) {
            String energyFormatted = TextFormattingUtil.formatNumbers(maxVoltage);
            // wrap in text component to keep it from being formatted
            var voltageName = KeyUtil.voltage(GTValues.VOCNF, maxVoltage);

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
    public MultiblockUIBuilder addComputationUsageLine(int maxCWUt) {
        if (!isStructureFormed) return this;
        if (maxCWUt > 0) {
            var computation = KeyUtil.number(TextFormatting.AQUA, maxCWUt);
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
    public MultiblockUIBuilder addComputationUsageExactLine(int currentCWUt) {
        if (!isStructureFormed) return this;
        if (isActive && currentCWUt > 0) {
            var computation = KeyUtil.number(TextFormatting.AQUA, currentCWUt, " CWU/t");
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
    public MultiblockUIBuilder addWorkingStatusLine() {
        if (!isStructureFormed) return this;

        if (!isWorkingEnabled) {
            addKey(pausedKey);
        } else if (isActive) {
            addKey(runningKey);
        } else {
            addKey(idlingKey);
        }
        return this;
    }

    /**
     * Adds the "Work Paused." line.
     * <br>
     * Added if working is not enabled, or if the checkState passed parameter is false. Also added only if formed.
     */
    public MultiblockUIBuilder addWorkPausedLine(boolean checkState) {
        if (!isStructureFormed) return this;
        if (!checkState || !isWorkingEnabled) {
            addKey(pausedKey);
        }
        return this;
    }

    /**
     * Adds the "Running Perfectly." line.
     * <br>
     * Added if machine is active, or if the checkState passed parameter is false. Also added only if formed.
     */
    public MultiblockUIBuilder addRunningPerfectlyLine(boolean checkState) {
        if (!isStructureFormed) return this;
        if (!checkState || isActive) {
            addKey(runningKey);
        }
        return this;
    }

    /**
     * Adds the "Idling." line.
     * <br>
     * Added if the machine is not active and working is enabled, or if the checkState passed parameter is false. Also
     * added only if formed.
     */
    public MultiblockUIBuilder addIdlingLine(boolean checkState) {
        if (!isStructureFormed) return this;
        if (!checkState || (isWorkingEnabled && !isActive)) {
            addKey(idlingKey);
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
    public MultiblockUIBuilder addProgressLine(double progressPercent) {
        if (!isStructureFormed || !isActive) return this;
        addKey(KeyUtil.lang(TextFormatting.GRAY,
                "gregtech.multiblock.progress",
                (int) (progressPercent * 100)));
        return this;
    }

    /**
     * Adds a line indicating how many parallels this multi can potentially perform.
     * <br>
     * Added if structure is formed and the number of parallels is greater than one.
     */
    public MultiblockUIBuilder addParallelsLine(int numParallels) {
        if (!isStructureFormed) return this;
        if (numParallels > 1) {
            var parallels = KeyUtil.number(TextFormatting.DARK_PURPLE, numParallels);

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
    public MultiblockUIBuilder addLowPowerLine(boolean isLowPower) {
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
    public MultiblockUIBuilder addLowComputationLine(boolean isLowComputation) {
        if (!isStructureFormed) return this;
        if (isLowComputation) {
            addKey(KeyUtil.lang(TextFormatting.YELLOW,
                    "gregtech.multiblock.computation.not_enough_computation"));
        }
        return this;
    }

    /**
     * Adds a warning line when the machine's dynamo tier is too low for current conditions.
     * <br>
     * Added if the structure is formed and if the passed parameter is true.
     */
    public MultiblockUIBuilder addLowDynamoTierLine(boolean isTooLow) {
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
     * Will check the config
     * setting for if maintenance is enabled automatically.
     */
    public MultiblockUIBuilder addMaintenanceProblemLines(byte maintenanceProblems) {
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
    public MultiblockUIBuilder addMufflerObstructedLine(boolean isObstructed) {
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
    public MultiblockUIBuilder addFuelNeededLine(String fuelName, int previousRecipeDuration) {
        if (!isStructureFormed || !isActive || fuelName == null) return this;

        addKey(KeyUtil.lang(TextFormatting.GRAY,
                "gregtech.multiblock.turbine.fuel_needed",
                KeyUtil.string(TextFormatting.RED, fuelName),
                KeyUtil.number(TextFormatting.AQUA, previousRecipeDuration)));
        return this;
    }

    /** Insert an empty line into the text list. */
    public MultiblockUIBuilder addEmptyLine() {
        addKey(IKey.LINE_FEED);
        return this;
    }

    /** Add custom text dynamically, allowing for custom application logic. */
    public MultiblockUIBuilder addCustom(Consumer<KeyManager> customConsumer) {
        customConsumer.accept(this);
        return this;
    }

    public boolean isEmpty() {
        return this.textList.isEmpty();
    }

    public void clear() {
        this.textList.clear();
        this.operations.clear();
    }

    protected boolean hasChanged() {
        if (this.action == null) return false;
        List<String> old = toString(this.textList);
        build();
        if (textList.size() != old.size()) return true;
        for (int i = 0; i < textList.size(); i++) {
            if (!JsonUtils.toJsonString(textList.get(i)).equals(old.get(i)))
                return true;
        }
        return false;
    }

    private static List<String> toString(List<? extends IDrawable> drawables) {
        List<String> strings = new ArrayList<>();
        for (IDrawable drawable : drawables) {
            strings.add(JsonUtils.toJsonString(drawable));
        }
        return strings;
    }

    protected void sync(String key, PanelSyncManager syncManager) {
        syncManager.syncValue(key, this.syncHandler);
    }

    private SyncHandler makeSyncHandler() {
        return new SyncHandler() {

            @Override
            public void detectAndSendChanges(boolean init) {
                if (init || hasChanged()) {
                    if (init) {
                        onRebuild();
                        build();
                    }
                    sync(0, this::syncText);
                    markDirty();
                }
            }

            private void syncText(PacketBuffer buffer) {
                buffer.writeVarInt(textList.size());
                for (int i = 0; i < textList.size(); i++) {
                    buffer.writeByte(Operation.getId(operations.get(i)));
                    var jsonString = JsonUtils.toJsonString(textList.get(i));
                    NetworkUtils.writeStringSafe(buffer, jsonString);
                }
            }

            @Override
            public void readOnClient(int id, PacketBuffer buf) {
                if (id == 0) {
                    clear();
                    for (int i = buf.readVarInt(); i > 0; i--) {
                        int op = buf.readByte();
                        String jsonString = NetworkUtils.readStringSafe(buf);
                        addKey(JsonUtils.fromJsonString(jsonString), Operation.getById(op));
                    }
                }
            }

            @Override
            public void readOnServer(int id, PacketBuffer buf) {}
        };
    }

    public void build(IRichTextBuilder<?> richText) {
        if (dirty) {
            onRebuild();
            build();
            dirty = false;
        }
        for (int i = 0; i < operations.size(); i++) {
            operations.get(i).apply(textList.get(i), richText);
        }
    }

    private void onRebuild() {
        if (this.onRebuild != null) {
            this.onRebuild.run();
        }
    }

    public void markDirty() {
        dirty = true;
    }

    protected void build() {
        clear();
        if (this.action != null) this.action.accept(this);
    }

    protected void setAction(Consumer<MultiblockUIBuilder> action) {
        this.action = action;
    }

    public void onRebuild(Runnable onRebuild) {
        this.onRebuild = onRebuild;
    }

    private void addKey(IKey key, IDrawable... hover) {
        addKey(KeyUtil.setHover(key, hover));
    }

    private void addKey(IDrawable key) {
        addKey(key, Operation.NEW_LINE_SPACE);
    }

    private void addKey(@NotNull IDrawable key, @NotNull Operation op) {
        if (textList.size() != operations.size()) {
            throw new IllegalStateException("textList and operations must be the same size!");
        }
        this.textList.add(key);
        Operation.checkOp(op);
        this.operations.add(op);
    }

    @Override
    public void add(IDrawable drawable, Operation op) {
        addKey(drawable, op);
    }
}
