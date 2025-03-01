package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IBatteryData;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ProgressBarMultiblock;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIFactory;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.sync.BigIntegerSyncValue;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityPowerSubstation extends MultiblockWithDisplayBase
                                           implements IControllable, ProgressBarMultiblock {

    // Structure Constants
    public static final int MAX_BATTERY_LAYERS = 18;
    private static final int MIN_CASINGS = 14;

    // Passive Drain Constants
    // 1% capacity per 24 hours
    public static final long PASSIVE_DRAIN_DIVISOR = 20 * 60 * 60 * 24 * 100;
    // no more than 100kEU/t per storage block
    public static final long PASSIVE_DRAIN_MAX_PER_STORAGE = 100_000L;

    // NBT Keys
    private static final String NBT_ENERGY_BANK = "EnergyBank";

    // Match Context Headers
    private static final String PMC_BATTERY_HEADER = "PSSBattery_";

    private static final BigInteger BIG_INTEGER_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

    private PowerStationEnergyBank energyBank;
    private EnergyContainerList inputHatches;
    private EnergyContainerList outputHatches;
    private long passiveDrain;
    private boolean isActive, isWorkingEnabled = true;

    // Stats tracked for UI display
    private long netInLastSec;
    private long averageInLastSec;
    private long netOutLastSec;
    private long averageOutLastSec;

    public MetaTileEntityPowerSubstation(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPowerSubstation(metaTileEntityId);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<IEnergyContainer> inputs = new ArrayList<>();
        inputs.addAll(getAbilities(MultiblockAbility.INPUT_ENERGY));
        inputs.addAll(getAbilities(MultiblockAbility.SUBSTATION_INPUT_ENERGY));
        inputs.addAll(getAbilities(MultiblockAbility.INPUT_LASER));
        this.inputHatches = new EnergyContainerList(inputs);

        List<IEnergyContainer> outputs = new ArrayList<>();
        outputs.addAll(getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        outputs.addAll(getAbilities(MultiblockAbility.SUBSTATION_OUTPUT_ENERGY));
        outputs.addAll(getAbilities(MultiblockAbility.OUTPUT_LASER));
        this.outputHatches = new EnergyContainerList(outputs);

        List<IBatteryData> parts = new ArrayList<>();
        for (Map.Entry<String, Object> battery : context.entrySet()) {
            if (battery.getKey().startsWith(PMC_BATTERY_HEADER) &&
                    battery.getValue() instanceof BatteryMatchWrapper wrapper) {
                for (int i = 0; i < wrapper.amount; i++) {
                    parts.add(wrapper.partType);
                }
            }
        }
        if (parts.isEmpty()) {
            // only empty batteries found in the structure
            invalidateStructure();
            return;
        }
        if (this.energyBank == null) {
            this.energyBank = new PowerStationEnergyBank(parts);
        } else {
            this.energyBank = energyBank.rebuild(parts);
        }
        this.passiveDrain = this.energyBank.getPassiveDrainPerTick();
    }

    @Override
    public void invalidateStructure() {
        // don't null out energyBank since it holds the stored energy, which
        // we need to hold on to across rebuilds to not void all energy if a
        // multiblock part or block other than the controller is broken.
        inputHatches = null;
        outputHatches = null;
        passiveDrain = 0;
        netInLastSec = 0;
        averageInLastSec = 0;
        netOutLastSec = 0;
        averageOutLastSec = 0;
        super.invalidateStructure();
    }

    @Override
    protected void updateFormedValid() {
        if (!getWorld().isRemote) {
            if (getOffsetTimer() % 20 == 0) {
                // active here is just used for rendering
                setActive(energyBank.hasEnergy());
                averageInLastSec = netInLastSec / 20;
                averageOutLastSec = netOutLastSec / 20;
                netInLastSec = 0;
                netOutLastSec = 0;
            }

            if (isWorkingEnabled()) {
                // Bank from Energy Input Hatches
                long energyBanked = energyBank.fill(inputHatches.getEnergyStored());
                inputHatches.changeEnergy(-energyBanked);
                netInLastSec += energyBanked;

                // Passive drain
                long energyPassiveDrained = energyBank.drain(getPassiveDrain());
                netOutLastSec += energyPassiveDrained;

                // Debank to Dynamo Hatches
                long energyDebanked = energyBank
                        .drain(outputHatches.getEnergyCapacity() - outputHatches.getEnergyStored());
                outputHatches.changeEnergy(energyDebanked);
                netOutLastSec += energyDebanked;
            }
        }
    }

    public long getPassiveDrain() {
        if (ConfigHolder.machines.enableMaintenance) {
            int multiplier = 1 + getNumMaintenanceProblems();
            double modifier = getMaintenanceDurationMultiplier();
            return (long) (passiveDrain * multiplier * modifier);
        }
        return passiveDrain;
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.isActive;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            markDirty();
            World world = getWorld();
            if (world != null && !world.isRemote) {
                writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.isWorkingEnabled = isWorkingAllowed;
        markDirty();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
        }
    }

    @Override
    public boolean shouldShowVoidingModeButton() {
        return false;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, UP)
                .aisle("XXSXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .aisle("XXXXX", "XCCCX", "XCCCX", "XCCCX", "XXXXX")
                .aisle("GGGGG", "GBBBG", "GBBBG", "GBBBG", "GGGGG").setRepeatable(1, MAX_BATTERY_LAYERS)
                .aisle("GGGGG", "GGGGG", "GGGGG", "GGGGG", "GGGGG")
                .where('S', selfPredicate())
                .where('C', states(getCasingState()))
                .where('X', states(getCasingState()).setMinGlobalLimited(MIN_CASINGS)
                        .or(maintenancePredicate())
                        .or(abilities(MultiblockAbility.INPUT_ENERGY, MultiblockAbility.SUBSTATION_INPUT_ENERGY,
                                MultiblockAbility.INPUT_LASER).setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.OUTPUT_ENERGY, MultiblockAbility.SUBSTATION_OUTPUT_ENERGY,
                                MultiblockAbility.OUTPUT_LASER).setMinGlobalLimited(1)))
                .where('G', states(getGlassState()))
                .where('B', BATTERY_PREDICATE.get())
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder(RIGHT, DOWN, FRONT)
                .aisle("CCCCC", "CCCCC", "GGGGG", "GGGGG", "GGGGG")
                .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                .aisle("ICSCO", "NCMCT", "GGGGG", "GGGGG", "GGGGG")
                .where('S', MetaTileEntities.POWER_SUBSTATION, EnumFacing.SOUTH)
                .where('C', getCasingState())
                .where('G', getGlassState())
                .where('I', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.HV], EnumFacing.SOUTH)
                .where('N', MetaTileEntities.SUBSTATION_ENERGY_INPUT_HATCH[0], EnumFacing.SOUTH)
                .where('O', MetaTileEntities.ENERGY_OUTPUT_HATCH[GTValues.HV], EnumFacing.SOUTH)
                .where('T', MetaTileEntities.SUBSTATION_ENERGY_OUTPUT_HATCH[0], EnumFacing.SOUTH)
                .where('M',
                        () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                                MetaBlocks.METAL_CASING.getState(MetalCasingType.PALLADIUM_SUBSTATION),
                        EnumFacing.SOUTH);

        GregTechAPI.PSS_BATTERIES.entrySet().stream()
                // filter out empty batteries in example structures, though they are still
                // allowed in the predicate (so you can see them on right-click)
                .filter(entry -> entry.getValue().getCapacity() > 0)
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
                .forEach(entry -> shapeInfo.add(builder.where('B', entry.getKey()).build()));

        return shapeInfo;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PALLADIUM_SUBSTATION);
    }

    protected IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.LAMINATED_GLASS);
    }

    protected static final Supplier<TraceabilityPredicate> BATTERY_PREDICATE = () -> new TraceabilityPredicate(
            blockWorldState -> {
                IBlockState state = blockWorldState.getBlockState();
                if (GregTechAPI.PSS_BATTERIES.containsKey(state)) {
                    IBatteryData battery = GregTechAPI.PSS_BATTERIES.get(state);
                    // Allow unfilled batteries in the structure, but do not add them to match context.
                    // This lets you use empty batteries as "filler slots" for convenience if desired.
                    if (battery.getTier() != -1 && battery.getCapacity() > 0) {
                        String key = PMC_BATTERY_HEADER + battery.getBatteryName();
                        BatteryMatchWrapper wrapper = blockWorldState.getMatchContext().get(key);
                        if (wrapper == null) wrapper = new BatteryMatchWrapper(battery);
                        blockWorldState.getMatchContext().set(key, wrapper.increment());
                    }
                    return true;
                }
                return false;
            }, () -> GregTechAPI.PSS_BATTERIES.entrySet().stream()
                    .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
                    .map(entry -> new BlockInfo(entry.getKey(), null))
                    .toArray(BlockInfo[]::new))
                            .addTooltips("gregtech.multiblock.pattern.error.batteries");

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.PALLADIUM_SUBSTATION_CASING;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.POWER_SUBSTATION_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.isActive(),
                this.isWorkingEnabled());
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.structureFormed(isStructureFormed());
        builder.setWorkingStatus(true, isActive() && isWorkingEnabled()); // transform into two-state system for display
        builder.setWorkingStatusKeys("gregtech.multiblock.idling", "gregtech.multiblock.idling",
                "gregtech.machine.active_transformer.routing");
        builder.addCustom((manager, syncer) -> {
            if (isStructureFormed() && syncer.syncBoolean(energyBank != null)) {
                BigInteger energyStored = syncer
                        .syncBigInt(energyBank == null ? BigInteger.ZERO : energyBank.getStored());
                BigInteger energyCapacity = syncer
                        .syncBigInt(energyBank == null ? BigInteger.ZERO : energyBank.getCapacity());

                // Stored EU line
                IKey storedFormatted = KeyUtil.string(
                        TextFormattingUtil.formatNumbers(energyStored) + " EU");

                IKey truncated = KeyUtil.string(TextFormatting.GOLD,
                        TextFormattingUtil.formatBigIntToCompactString(energyStored, 7) + " EU");

                IKey bodyStored = (KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.power_substation.stored",
                        truncated));

                manager.add(KeyUtil.setHover(bodyStored, storedFormatted));

                // EU Capacity line
                IKey capacityFormatted = KeyUtil.string(
                        TextFormattingUtil.formatNumbers(energyCapacity) + " EU");

                IKey capCompact = KeyUtil.string(TextFormatting.GOLD,
                        TextFormattingUtil.formatBigIntToCompactString(energyCapacity, 7) + " EU");

                IKey bodyCap = KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.power_substation.capacity",
                        capCompact);

                manager.add(KeyUtil.setHover(bodyCap, capacityFormatted));

                // Passive Drain line
                IKey passiveDrain = KeyUtil.string(TextFormatting.DARK_RED,
                        TextFormattingUtil.formatNumbers(syncer.syncLong(getPassiveDrain())) + " EU/t");
                manager.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.power_substation.passive_drain",
                        passiveDrain));

                // Average EU IN line
                long avgIn = syncer.syncLong(averageInLastSec);
                long avgOut = syncer.syncLong(averageOutLastSec);

                IKey avgValue = KeyUtil.number(TextFormatting.GREEN, avgIn, " EU/t");
                IKey base = KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.power_substation.average_in",
                        avgValue);
                IKey hover = KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.power_substation.average_in_hover");
                manager.add(KeyUtil.setHover(base, hover));

                // Average EU OUT line
                avgValue = KeyUtil.number(TextFormatting.RED, avgOut, " EU/t");
                base = KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.power_substation.average_out", avgValue);
                hover = KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.power_substation.average_out_hover");
                manager.add(KeyUtil.setHover(base, hover));

                // Time to fill/drain line
                if (avgIn > avgOut) {
                    IKey timeToFill = getTimeToFillDrainText(energyCapacity.subtract(energyStored)
                            .divide(BigInteger.valueOf((avgIn - avgOut) * 20)))
                                    .style(TextFormatting.GREEN);

                    manager.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.power_substation.time_to_fill",
                            timeToFill));

                } else if (avgIn < avgOut) {
                    IKey timeToDrain = getTimeToFillDrainText(
                            energyStored.divide(BigInteger.valueOf((avgOut - avgIn) * 20)))
                                    .style(TextFormatting.RED);

                    manager.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.power_substation.time_to_drain",
                            timeToDrain));
                }
            }
        });
        builder.addWorkingStatusLine();
    }

    @Override
    protected void configureWarningText(MultiblockUIBuilder builder) {
        builder.addCustom((list, syncer) -> {
            if (isStructureFormed() && averageInLastSec < averageOutLastSec) {
                BigInteger timeToDrainSeconds = energyBank.getStored()
                        .divide(BigInteger.valueOf((averageOutLastSec - averageInLastSec) * 20));
                if (timeToDrainSeconds.compareTo(BigInteger.valueOf(60 * 60)) < 0) { // less than 1 hour left
                    list.add(KeyUtil.lang(TextFormatting.YELLOW,
                            "gregtech.multiblock.power_substation.under_one_hour_left"));
                }
            }
        });
    }

    private static IKey getTimeToFillDrainText(BigInteger timeToFillSeconds) {
        if (timeToFillSeconds.compareTo(BIG_INTEGER_MAX_LONG) > 0) {
            // too large to represent in a java Duration
            timeToFillSeconds = BIG_INTEGER_MAX_LONG;
        }

        Duration duration = Duration.ofSeconds(timeToFillSeconds.longValue());
        String key;
        long fillTime;
        if (duration.getSeconds() <= 180) {
            fillTime = duration.getSeconds();
            key = "gregtech.multiblock.power_substation.time_seconds";
        } else if (duration.toMinutes() <= 180) {
            fillTime = duration.toMinutes();
            key = "gregtech.multiblock.power_substation.time_minutes";
        } else if (duration.toHours() <= 72) {
            fillTime = duration.toHours();
            key = "gregtech.multiblock.power_substation.time_hours";
        } else if (duration.toDays() <= 730) { // 2 years
            fillTime = duration.toDays();
            key = "gregtech.multiblock.power_substation.time_days";
        } else if (duration.toDays() / 365 < 1_000_000) {
            fillTime = duration.toDays() / 365;
            key = "gregtech.multiblock.power_substation.time_years";
        } else {
            return KeyUtil.lang("gregtech.multiblock.power_substation.time_forever");
        }

        return KeyUtil.lang(key, TextFormattingUtil.formatNumbers(fillTime));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", isActive);
        data.setBoolean("isWorkingEnabled", isWorkingEnabled);
        if (energyBank != null) {
            data.setTag(NBT_ENERGY_BANK, energyBank.writeToNBT(new NBTTagCompound()));
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean("isActive");
        isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        if (data.hasKey(NBT_ENERGY_BANK)) {
            energyBank = new PowerStationEnergyBank(data.getCompoundTag(NBT_ENERGY_BANK));
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
        buf.writeBoolean(isWorkingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isActive = buf.readBoolean();
        isWorkingEnabled = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            isActive = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            isWorkingEnabled = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.power_substation.tooltip1"));
        tooltip.add(I18n.format("gregtech.machine.power_substation.tooltip2"));
        tooltip.add(I18n.format("gregtech.machine.power_substation.tooltip3", MAX_BATTERY_LAYERS));
        tooltip.add(I18n.format("gregtech.machine.power_substation.tooltip4"));
        tooltip.add(I18n.format("gregtech.machine.power_substation.tooltip5", PASSIVE_DRAIN_MAX_PER_STORAGE));
        tooltip.add(I18n.format("gregtech.machine.power_substation.tooltip6") + TooltipHelper.RAINBOW_SLOW +
                I18n.format("gregtech.machine.power_substation.tooltip6.5"));
    }

    public String getStored() {
        if (energyBank == null) {
            return "0";
        }
        return TextFormattingUtil.formatNumbers(energyBank.getStored());
    }

    public long getStoredLong() {
        if (energyBank == null) {
            return 0;
        }
        return energyBank.getStored().longValue() & ~(1L << 63);
    }

    public long getCapacityLong() {
        if (energyBank == null) {
            return 0;
        }
        return energyBank.getCapacity().longValue() & ~(1L << 63);
    }

    public String getCapacity() {
        if (energyBank == null) {
            return "0";
        }
        return TextFormattingUtil.formatNumbers(energyBank.getCapacity());
    }

    public long getAverageInLastSec() {
        return averageInLastSec;
    }

    public long getAverageOutLastSec() {
        return averageOutLastSec;
    }

    @Override
    public int getProgressBarCount() {
        return 1;
    }

    @Override
    public @NotNull ProgressWidget createProgressBar(@NotNull PanelSyncManager panelSyncManager, int index) {
        BigIntegerSyncValue energyStoredValue = new BigIntegerSyncValue(
                () -> energyBank == null ? BigInteger.ZERO : energyBank.getStored(), null);
        BigIntegerSyncValue energyCapacityValue = new BigIntegerSyncValue(
                () -> energyBank == null ? BigInteger.ZERO : energyBank.getCapacity(), null);
        panelSyncManager.syncValue("energy_stored", energyStoredValue);
        panelSyncManager.syncValue("energy_capacity", energyCapacityValue);

        return new ProgressWidget()
                .progress(
                        () -> energyStoredValue.getValue().doubleValue() / energyCapacityValue.getValue().doubleValue())
                .texture(GTGuiTextures.PROGRESS_BAR_MULTI_ENERGY_YELLOW, MultiblockUIFactory.Bars.FULL_WIDTH)
                .tooltipAutoUpdate(true)
                .tooltipBuilder(t -> {
                    if (isStructureFormed()) {
                        t.addLine(IKey.lang("gregtech.multiblock.energy_stored", energyStoredValue.getValue(),
                                energyCapacityValue.getValue()));
                    } else {
                        t.addLine(IKey.lang("gregtech.multiblock.invalid_structure"));
                    }
                });
    }

    public static class PowerStationEnergyBank {

        private static final String NBT_SIZE = "Size";
        private static final String NBT_STORED = "Stored";
        private static final String NBT_MAX = "Max";

        private final long[] storage;
        private final long[] maximums;
        private final BigInteger capacity;
        private int index;

        public PowerStationEnergyBank(List<IBatteryData> batteries) {
            storage = new long[batteries.size()];
            maximums = new long[batteries.size()];
            for (int i = 0; i < batteries.size(); i++) {
                maximums[i] = batteries.get(i).getCapacity();
            }
            capacity = summarize(maximums);
        }

        public PowerStationEnergyBank(NBTTagCompound storageTag) {
            int size = storageTag.getInteger(NBT_SIZE);
            storage = new long[size];
            maximums = new long[size];
            for (int i = 0; i < size; i++) {
                NBTTagCompound subtag = storageTag.getCompoundTag(String.valueOf(i));
                if (subtag.hasKey(NBT_STORED)) {
                    storage[i] = subtag.getLong(NBT_STORED);
                }
                maximums[i] = subtag.getLong(NBT_MAX);
            }
            capacity = summarize(maximums);
        }

        private NBTTagCompound writeToNBT(NBTTagCompound compound) {
            compound.setInteger(NBT_SIZE, storage.length);
            for (int i = 0; i < storage.length; i++) {
                NBTTagCompound subtag = new NBTTagCompound();
                if (storage[i] > 0) {
                    subtag.setLong(NBT_STORED, storage[i]);
                }
                subtag.setLong(NBT_MAX, maximums[i]);
                compound.setTag(String.valueOf(i), subtag);
            }
            return compound;
        }

        /**
         * Rebuild the power storage with a new list of batteries.
         * Will use existing stored power and try to map it onto new batteries.
         * If there was more power before the rebuild operation, it will be lost.
         */
        public PowerStationEnergyBank rebuild(@NotNull List<IBatteryData> batteries) {
            if (batteries.isEmpty()) {
                throw new IllegalArgumentException("Cannot rebuild Power Substation power bank with no batteries!");
            }
            PowerStationEnergyBank newStorage = new PowerStationEnergyBank(batteries);
            for (long stored : storage) {
                newStorage.fill(stored);
            }
            return newStorage;
        }

        /** @return Amount filled into storage */
        public long fill(long amount) {
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative!");

            // ensure index
            if (index != storage.length - 1 && storage[index] == maximums[index]) {
                index++;
            }

            long maxFill = Math.min(maximums[index] - storage[index], amount);

            // storage is completely full
            if (maxFill == 0 && index == storage.length - 1) {
                return 0;
            }

            // fill this "battery" as much as possible
            storage[index] += maxFill;
            amount -= maxFill;

            // try to fill other "batteries" if necessary
            if (amount > 0 && index != storage.length - 1) {
                return maxFill + fill(amount);
            }

            // other fill not necessary, either because the storage is now completely full,
            // or we were able to consume all the energy in this "battery"
            return maxFill;
        }

        /** @return Amount drained from storage */
        public long drain(long amount) {
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative!");

            // ensure index
            if (index != 0 && storage[index] == 0) {
                index--;
            }

            long maxDrain = Math.min(storage[index], amount);

            // storage is completely empty
            if (maxDrain == 0 && index == 0) {
                return 0;
            }

            // drain this "battery" as much as possible
            storage[index] -= maxDrain;
            amount -= maxDrain;

            // try to drain other "batteries" if necessary
            if (amount > 0 && index != 0) {
                index--;
                return maxDrain + drain(amount);
            }

            // other drain not necessary, either because the storage is now completely empty,
            // or we were able to drain all the energy from this "battery"
            return maxDrain;
        }

        public BigInteger getCapacity() {
            return capacity;
        }

        public BigInteger getStored() {
            return summarize(storage);
        }

        public boolean hasEnergy() {
            for (long l : storage) {
                if (l > 0) return true;
            }
            return false;
        }

        private static BigInteger summarize(long[] values) {
            BigInteger retVal = BigInteger.ZERO;
            long currentSum = 0;
            for (long value : values) {
                if (currentSum != 0 && value > Long.MAX_VALUE - currentSum) {
                    // will overflow if added
                    retVal = retVal.add(BigInteger.valueOf(currentSum));
                    currentSum = 0;
                }
                currentSum += value;
            }
            if (currentSum != 0) {
                retVal = retVal.add(BigInteger.valueOf(currentSum));
            }
            return retVal;
        }

        @VisibleForTesting
        public long getPassiveDrainPerTick() {
            long[] maximumsExcl = new long[maximums.length];
            int index = 0;
            int numExcl = 0;
            for (long maximum : maximums) {
                if (maximum / PASSIVE_DRAIN_DIVISOR >= PASSIVE_DRAIN_MAX_PER_STORAGE) {
                    numExcl++;
                } else {
                    maximumsExcl[index++] = maximum;
                }
            }
            maximumsExcl = Arrays.copyOf(maximumsExcl, index);
            BigInteger capacityExcl = summarize(maximumsExcl);

            return capacityExcl.divide(BigInteger.valueOf(PASSIVE_DRAIN_DIVISOR))
                    .add(BigInteger.valueOf(PASSIVE_DRAIN_MAX_PER_STORAGE * numExcl))
                    .longValue();
        }
    }

    private static class BatteryMatchWrapper {

        private final IBatteryData partType;
        private int amount;

        public BatteryMatchWrapper(IBatteryData partType) {
            this.partType = partType;
        }

        public BatteryMatchWrapper increment() {
            amount++;
            return this;
        }
    }
}
