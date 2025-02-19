package gregtech.api.metatileentity.multiblock;

import gregtech.api.GTValues;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.capability.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.Widget.ClickData;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.IndicatorImageWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.ConfigHolder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static gregtech.api.capability.GregtechDataCodes.IS_WORKING;
import static gregtech.api.capability.GregtechDataCodes.STORE_TAPED;

public abstract class MultiblockWithDisplayBase extends MultiblockControllerBase implements IMaintenance {

    private static final String NBT_VOIDING_MODE = "VoidingMode";
    private static final String NBT_VOIDING_ITEMS = "VoidingItems";
    private static final String NBT_VOIDING_FLUIDS = "VoidingFluids";

    private boolean voidingItems = false;
    private boolean voidingFluids = false;
    private VoidingMode voidingMode;
    private boolean fluidInfSink = false;
    private boolean itemInfSink = false;

    // Held since it is frequently accessed
    private IMaintenanceHatch maintenanceHatch;

    /**
     * Items to recover in a muffler hatch
     */
    protected final List<ItemStack> recoveryItems = new ArrayList<>(
            Collections.singleton(OreDictUnifier.get(OrePrefix.dustTiny, Materials.Ash)));

    private int timeActive;

    /**
     * This value stores whether each of the 5 maintenance problems have been fixed.
     * A value of 0 means the problem is not fixed, else it is fixed
     * Value positions correspond to the following from left to right: 0=Wrench, 1=Screwdriver, 2=Soft Mallet, 3=Hard
     * Hammer, 4=Wire Cutter, 5=Crowbar
     */
    protected byte maintenance_problems;
    // Used for tracking if this is the initial state of the machine, for maintenance hatches which automatically fix
    // initial issues.
    private boolean initialMaintenanceDone;

    // Used for data preservation with Maintenance Hatch
    private boolean storedTaped = false;

    protected List<BlockPos> variantActiveBlocks;
    protected boolean lastActive;

    public MultiblockWithDisplayBase(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.maintenance_problems = 0b000000;
        this.voidingMode = VoidingMode.VOID_NONE;
    }

    /**
     * Sets the maintenance problem corresponding to index to fixed
     *
     * @param index of the maintenance problem
     */
    @Override
    public void setMaintenanceFixed(int index) {
        this.maintenance_problems |= 1 << index;
    }

    /**
     * Used to cause a single random maintenance problem
     */
    @Override
    public void causeMaintenanceProblems() {
        this.maintenance_problems &= ~(1 << ((int) (GTValues.RNG.nextFloat() * 5)));
        this.getWorld().playSound(null, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(),
                this.getBreakdownSound(), SoundCategory.BLOCKS, 1.f, 1.f);
    }

    /**
     * @return the byte value representing the maintenance problems
     */
    @Override
    public byte getMaintenanceProblems() {
        return ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics() ? maintenance_problems : 0b111111;
    }

    /**
     * @return the amount of maintenance problems the multiblock has
     */
    @Override
    public int getNumMaintenanceProblems() {
        return ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics() ?
                6 - Integer.bitCount(maintenance_problems) : 0;
    }

    /**
     * @return whether the multiblock has any maintenance problems
     */
    @Override
    public boolean hasMaintenanceProblems() {
        return ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics() && this.maintenance_problems < 63;
    }

    /**
     * @return whether this multiblock has maintenance mechanics
     */
    @Override
    public boolean hasMaintenanceMechanics() {
        return true;
    }

    public boolean hasMufflerMechanics() {
        return false;
    }

    /**
     * Used to calculate whether a maintenance problem should happen based on machine time active
     */
    public void calculateMaintenance() {
        if (!ConfigHolder.machines.enableMaintenance || !hasMaintenanceMechanics() || maintenanceHatch == null)
            return;

        if (maintenanceHatch.isFullAuto()) {
            return;
        }

        if (++timeActive >= 1000 / maintenanceHatch.getTimeMultiplier()) {
            timeActive = 0;
            if (GTValues.RNG.nextInt(6000) == 0) {
                causeMaintenanceProblems();
                maintenanceHatch.setTaped(false);
            }
        }
    }

    /**
     * Configurable Maintenance modifier applied to external effects, like recipe durations.
     * Ranges from 0.9 to 1.1. Lower is a "better" effect than normal, higher is a "worse" effect than normal.
     */
    public double getMaintenanceDurationMultiplier() {
        if (!ConfigHolder.machines.enableMaintenance || !hasMaintenanceMechanics() || maintenanceHatch == null) {
            return 1.0;
        }
        return maintenanceHatch.getDurationMultiplier();
    }

    @Override
    public boolean isStructureObstructed() {
        return hasMufflerMechanics() && !isMufflerFaceFree();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        if (this.hasMaintenanceMechanics() && ConfigHolder.machines.enableMaintenance) { // nothing extra if no
                                                                                         // maintenance
            if (getAbilities(MultiblockAbility.MAINTENANCE_HATCH).isEmpty())
                return;
            maintenanceHatch = getAbilities(MultiblockAbility.MAINTENANCE_HATCH).get(0);
            if (maintenanceHatch.startWithoutProblems() && !initialMaintenanceDone) {
                this.maintenance_problems = (byte) 0b111111;
                this.timeActive = 0;
                this.initialMaintenanceDone = true;
            }
            readMaintenanceData(maintenanceHatch);
            if (storedTaped) {
                maintenanceHatch.setTaped(true);
                storeTaped(false);
            }
        }
        this.variantActiveBlocks = context.getOrDefault("VABlock", new LinkedList<>());
        replaceVariantBlocksActive(false);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            boolean state = isActive();
            if (lastActive != state) {
                this.setLastActive(state);
                this.markDirty();
                this.replaceVariantBlocksActive(lastActive);
            }
            if (state) {
                calculateMaintenance();
            }
        }
    }

    public void setLastActive(boolean lastActive) {
        this.lastActive = lastActive;
        this.writeCustomData(IS_WORKING, buf -> buf.writeBoolean(lastActive));
    }

    /**
     * Stores the taped state of the maintenance hatch
     *
     * @param isTaped is whether the maintenance hatch is taped or not
     */
    @Override
    public void storeTaped(boolean isTaped) {
        this.storedTaped = isTaped;
        writeCustomData(STORE_TAPED, buf -> buf.writeBoolean(isTaped));
    }

    /**
     * reads maintenance data from a maintenance hatch
     *
     * @param hatch is the hatch to read the data from
     */
    private void readMaintenanceData(IMaintenanceHatch hatch) {
        if (hatch.hasMaintenanceData()) {
            Tuple<Byte, Integer> data = hatch.readMaintenanceData();
            this.maintenance_problems = data.getFirst();
            this.timeActive = data.getSecond();
        }
    }

    /**
     * Outputs the recovery items into the muffler hatch
     */
    public void outputRecoveryItems() {
        IMufflerHatch muffler = getAbilities(MultiblockAbility.MUFFLER_HATCH).get(0);
        muffler.recoverItemsTable(recoveryItems);
    }

    public void outputRecoveryItems(int parallel) {
        IMufflerHatch muffler = getAbilities(MultiblockAbility.MUFFLER_HATCH).get(0);
        for (int i = 0; i < parallel; i++) {
            muffler.recoverItemsTable(recoveryItems);
        }
    }

    /**
     * @return whether the muffler hatch's front face is free
     */
    public boolean isMufflerFaceFree() {
        if (hasMufflerMechanics() && getAbilities(MultiblockAbility.MUFFLER_HATCH).size() == 0)
            return false;

        return isStructureFormed() && hasMufflerMechanics() &&
                getAbilities(MultiblockAbility.MUFFLER_HATCH).get(0).isFrontFaceFree();
    }

    /**
     * @deprecated Override {@link #getMufflerParticle()} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    @Deprecated
    @SideOnly(Side.CLIENT)
    public void runMufflerEffect(float xPos, float yPos, float zPos, float xSpd, float ySpd, float zSpd) {
        getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos, yPos, zPos, xSpd, ySpd, zSpd);
    }

    @SideOnly(Side.CLIENT)
    public @NotNull EnumParticleTypes getMufflerParticle() {
        return EnumParticleTypes.SMOKE_LARGE;
    }

    /**
     * Sets the recovery items of this multiblock
     *
     * @param recoveryItems is the items to set
     */
    protected void setRecoveryItems(ItemStack... recoveryItems) {
        this.recoveryItems.clear();
        this.recoveryItems.addAll(Arrays.asList(recoveryItems));
    }

    /**
     * @return whether the current multiblock is active or not
     */
    public boolean isActive() {
        return isStructureFormed();
    }

    @Override
    public void invalidateStructure() {
        if (hasMaintenanceMechanics() && ConfigHolder.machines.enableMaintenance) { // nothing extra if no maintenance
            if (!getAbilities(MultiblockAbility.MAINTENANCE_HATCH).isEmpty())
                getAbilities(MultiblockAbility.MAINTENANCE_HATCH).get(0)
                        .storeMaintenanceData(maintenance_problems, timeActive);
        }
        this.lastActive = false;
        this.replaceVariantBlocksActive(false);
        this.fluidInfSink = false;
        this.itemInfSink = false;
        this.maintenanceHatch = null;
        super.invalidateStructure();
    }

    protected void replaceVariantBlocksActive(boolean isActive) {
        if (variantActiveBlocks != null && !variantActiveBlocks.isEmpty()) {
            int id = getWorld().provider.getDimension();

            writeCustomData(GregtechDataCodes.VARIANT_RENDER_UPDATE, buf -> {
                buf.writeInt(id);
                buf.writeBoolean(isActive);
                buf.writeInt(variantActiveBlocks.size());
                for (BlockPos blockPos : variantActiveBlocks) {
                    VariantActiveBlock.setBlockActive(id, blockPos, isActive);
                    buf.writeBlockPos(blockPos);
                }
            });
        }
    }

    public TraceabilityPredicate autoAbilities() {
        return autoAbilities(true, true);
    }

    public TraceabilityPredicate autoAbilities(boolean checkMaintenance, boolean checkMuffler) {
        TraceabilityPredicate predicate = new TraceabilityPredicate();
        if (checkMaintenance && hasMaintenanceMechanics()) {
            predicate = predicate.or(maintenancePredicate());
        }
        if (checkMuffler && hasMufflerMechanics()) {
            predicate = predicate
                    .or(abilities(MultiblockAbility.MUFFLER_HATCH).setMinGlobalLimited(1).setMaxGlobalLimited(1));
        }
        return predicate;
    }

    protected TraceabilityPredicate maintenancePredicate() {
        if (hasMaintenanceMechanics()) {
            return abilities(MultiblockAbility.MAINTENANCE_HATCH)
                    .setMinGlobalLimited(ConfigHolder.machines.enableMaintenance ? 1 : 0).setMaxGlobalLimited(1);
        }
        return new TraceabilityPredicate();
    }

    /**
     * Called serverside to obtain text displayed in GUI
     * each element of list is displayed on new line
     * to use translation, use TextComponentTranslation
     */
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed());
    }

    /**
     * Called on serverside when client is clicked on the specific text component
     * with special click event handler
     * Data is the data specified in the component
     */
    protected void handleDisplayClick(String componentData, ClickData clickData) {}

    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 198, 208);

        // Display
        if (this instanceof IProgressBarMultiblock progressMulti && progressMulti.showProgressBar()) {
            builder.image(4, 4, 190, 109, GuiTextures.DISPLAY);

            if (progressMulti.getNumProgressBars() == 3) {
                // triple bar
                ProgressWidget progressBar = new ProgressWidget(
                        () -> progressMulti.getFillPercentage(0),
                        4, 115, 62, 7,
                        progressMulti.getProgressBarTexture(0), ProgressWidget.MoveType.HORIZONTAL)
                                .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 0));
                builder.widget(progressBar);

                progressBar = new ProgressWidget(
                        () -> progressMulti.getFillPercentage(1),
                        68, 115, 62, 7,
                        progressMulti.getProgressBarTexture(1), ProgressWidget.MoveType.HORIZONTAL)
                                .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 1));
                builder.widget(progressBar);

                progressBar = new ProgressWidget(
                        () -> progressMulti.getFillPercentage(2),
                        132, 115, 62, 7,
                        progressMulti.getProgressBarTexture(2), ProgressWidget.MoveType.HORIZONTAL)
                                .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 2));
                builder.widget(progressBar);
            } else if (progressMulti.getNumProgressBars() == 2) {
                // double bar
                ProgressWidget progressBar = new ProgressWidget(
                        () -> progressMulti.getFillPercentage(0),
                        4, 115, 94, 7,
                        progressMulti.getProgressBarTexture(0), ProgressWidget.MoveType.HORIZONTAL)
                                .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 0));
                builder.widget(progressBar);

                progressBar = new ProgressWidget(
                        () -> progressMulti.getFillPercentage(1),
                        100, 115, 94, 7,
                        progressMulti.getProgressBarTexture(1), ProgressWidget.MoveType.HORIZONTAL)
                                .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 1));
                builder.widget(progressBar);
            } else {
                // single bar
                ProgressWidget progressBar = new ProgressWidget(
                        () -> progressMulti.getFillPercentage(0),
                        4, 115, 190, 7,
                        progressMulti.getProgressBarTexture(0), ProgressWidget.MoveType.HORIZONTAL)
                                .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 0));
                builder.widget(progressBar);
            }
            builder.widget(new IndicatorImageWidget(174, 93, 17, 17, getLogo())
                    .setWarningStatus(getWarningLogo(), this::addWarningText)
                    .setErrorStatus(getErrorLogo(), this::addErrorText));
        } else {
            builder.image(4, 4, 190, 117, GuiTextures.DISPLAY);
            builder.widget(new IndicatorImageWidget(174, 101, 17, 17, getLogo())
                    .setWarningStatus(getWarningLogo(), this::addWarningText)
                    .setErrorStatus(getErrorLogo(), this::addErrorText));
        }

        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
        builder.widget(new AdvancedTextWidget(9, 20, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(181)
                .setClickHandler(this::handleDisplayClick));

        // Power Button
        // todo in the future, refactor so that this class is instanceof IControllable.
        IControllable controllable = getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        if (controllable != null) {
            builder.widget(new ImageCycleButtonWidget(173, 183, 18, 18, GuiTextures.BUTTON_POWER,
                    controllable::isWorkingEnabled, controllable::setWorkingEnabled));
            builder.widget(new ImageWidget(173, 201, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));
        }

        // Voiding Mode Button
        if (shouldShowVoidingModeButton()) {
            builder.widget(new ImageCycleButtonWidget(173, 161, 18, 18, GuiTextures.BUTTON_VOID_MULTIBLOCK,
                    4, this::getVoidingMode, this::setVoidingMode)
                            .setTooltipHoverString(MultiblockWithDisplayBase::getVoidingModeTooltip));
        } else {
            builder.widget(new ImageWidget(173, 161, 18, 18, GuiTextures.BUTTON_VOID_NONE)
                    .setTooltip("gregtech.gui.multiblock_voiding_not_supported"));
        }

        // Distinct Buses Button
        if (this instanceof IDistinctBusController distinct && distinct.canBeDistinct()) {
            builder.widget(new ImageCycleButtonWidget(173, 143, 18, 18, GuiTextures.BUTTON_DISTINCT_BUSES,
                    distinct::isDistinct, distinct::setDistinct)
                            .setTooltipHoverString(i -> "gregtech.multiblock.universal.distinct_" +
                                    (i == 0 ? "disabled" : "enabled")));
        } else {
            builder.widget(new ImageWidget(173, 143, 18, 18, GuiTextures.BUTTON_NO_DISTINCT_BUSES)
                    .setTooltip("gregtech.multiblock.universal.distinct_not_supported"));
        }

        // Flex Button
        builder.widget(getFlexButton(173, 125, 18, 18));

        builder.bindPlayerInventory(entityPlayer.inventory, 125);
        return builder;
    }

    /**
     * Add a custom third button to the Multiblock UI. By default, this is a placeholder
     * stating that there is no additional functionality for this Multiblock.
     * <br>
     * <br>
     * Parameters should be passed directly to the created widget. Size will be 18x18.
     */
    @SuppressWarnings("SameParameterValue")
    @NotNull
    protected Widget getFlexButton(int x, int y, int width, int height) {
        return new ImageWidget(x, y, width, height, GuiTextures.BUTTON_NO_FLEX)
                .setTooltip("gregtech.multiblock.universal.no_flex_button");
    }

    protected @NotNull TextureArea getLogo() {
        return GuiTextures.GREGTECH_LOGO_DARK;
    }

    protected @NotNull TextureArea getWarningLogo() {
        return GuiTextures.GREGTECH_LOGO_BLINKING_YELLOW;
    }

    protected @NotNull TextureArea getErrorLogo() {
        return GuiTextures.GREGTECH_LOGO_BLINKING_RED;
    }

    /**
     * Returns a list of text indicating any current warnings in this Multiblock.
     * Recommended to only display warnings if the structure is already formed.
     */
    protected void addWarningText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed(), false)
                .addMaintenanceProblemLines(getMaintenanceProblems());
    }

    /**
     * Returns a list of translation keys indicating any current errors in this Multiblock.
     * Prioritized over any warnings provided by {@link MultiblockWithDisplayBase#addWarningText}.
     */
    protected void addErrorText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .addMufflerObstructedLine(hasMufflerMechanics() && !isMufflerFaceFree());
    }

    protected boolean shouldShowVoidingModeButton() {
        return true;
    }

    protected int getVoidingMode() {
        return voidingMode.ordinal();
    }

    protected void setVoidingMode(int mode) {
        this.voidingMode = VoidingMode.VALUES[mode];

        this.voidingFluids = mode >= 2;

        this.voidingItems = mode == 1 || mode == 3;

        // After changing the voiding mode, reset the notified buses in case a recipe can run now that voiding mode has
        // been changed
        for (IFluidTank tank : this.getAbilities(MultiblockAbility.IMPORT_FLUIDS)) {
            this.getNotifiedFluidInputList().add((IFluidHandler) tank);
        }
        this.getNotifiedItemInputList().addAll(this.getAbilities(MultiblockAbility.IMPORT_ITEMS));

        markDirty();
    }

    protected static String getVoidingModeTooltip(int mode) {
        return VoidingMode.VALUES[mode].getName();
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer).build(getHolder(), entityPlayer);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setByte("Maintenance", maintenance_problems);
        data.setBoolean("InitialMaintenance", initialMaintenanceDone);
        data.setInteger("ActiveTimer", timeActive);
        data.setBoolean(NBT_VOIDING_ITEMS, voidingItems);
        data.setBoolean(NBT_VOIDING_FLUIDS, voidingFluids);
        data.setInteger(NBT_VOIDING_MODE, voidingMode.ordinal());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        maintenance_problems = data.getByte("Maintenance");
        initialMaintenanceDone = data.getBoolean("InitialMaintenance");
        timeActive = data.getInteger("ActiveTimer");
        if (data.hasKey(NBT_VOIDING_ITEMS)) {
            voidingItems = data.getBoolean(NBT_VOIDING_ITEMS);
        }

        if (data.hasKey(NBT_VOIDING_FLUIDS)) {
            voidingFluids = data.getBoolean(NBT_VOIDING_FLUIDS);
        }

        if (data.hasKey(NBT_VOIDING_MODE)) {
            voidingMode = VoidingMode.values()[data.getInteger(NBT_VOIDING_MODE)];
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(maintenance_problems);
        buf.writeInt(timeActive);
        buf.writeBoolean(voidingFluids);
        buf.writeBoolean(voidingItems);
        buf.writeInt(voidingMode.ordinal());
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        maintenance_problems = buf.readByte();
        timeActive = buf.readInt();
        voidingFluids = buf.readBoolean();
        voidingItems = buf.readBoolean();
        voidingMode = VoidingMode.values()[buf.readInt()];
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == STORE_TAPED) {
            storedTaped = buf.readBoolean();
        }
        if (dataId == GregtechDataCodes.VARIANT_RENDER_UPDATE) {
            int minX;
            int minY;
            int minZ;
            minX = minY = minZ = Integer.MAX_VALUE;
            int maxX;
            int maxY;
            int maxZ;
            maxX = maxY = maxZ = Integer.MIN_VALUE;

            int id = buf.readInt();
            boolean isActive = buf.readBoolean();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                BlockPos blockPos = buf.readBlockPos();
                VariantActiveBlock.setBlockActive(id, blockPos, isActive);
                minX = Math.min(minX, blockPos.getX());
                minY = Math.min(minY, blockPos.getY());
                minZ = Math.min(minZ, blockPos.getZ());
                maxX = Math.max(maxX, blockPos.getX());
                maxY = Math.max(maxY, blockPos.getY());
                maxZ = Math.max(maxZ, blockPos.getZ());
            }

            if (getWorld().provider.getDimension() == id) {
                getWorld().markBlockRangeForRenderUpdate(new BlockPos(minX, minY, minZ),
                        new BlockPos(maxX, maxY, maxZ));
            }
        }
        if (dataId == IS_WORKING) {
            lastActive = buf.readBoolean();
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        T capabilityResult = super.getCapability(capability, side);
        if (capabilityResult != null) return capabilityResult;
        if (capability == GregtechTileCapabilities.CAPABILITY_MAINTENANCE) {
            if (this.hasMaintenanceMechanics() && ConfigHolder.machines.enableMaintenance) {
                return GregtechTileCapabilities.CAPABILITY_MAINTENANCE.cast(this);
            }
        }
        return null;
    }

    @Override
    public boolean canVoidRecipeFluidOutputs() {
        return voidingFluids || fluidInfSink;
    }

    @Override
    public boolean canVoidRecipeItemOutputs() {
        return voidingItems || itemInfSink;
    }

    public void enableFluidInfSink() {
        this.fluidInfSink = true;
    }

    public void enableItemInfSink() {
        this.itemInfSink = true;
    }
}
