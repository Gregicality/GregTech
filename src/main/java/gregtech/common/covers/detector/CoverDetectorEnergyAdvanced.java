package gregtech.common.covers.detector;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.util.RedstoneUtil;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

public class CoverDetectorEnergyAdvanced extends CoverDetectorEnergy implements CoverWithUI {

    private static final int PADDING = 5, SIZE = 18;

    private static final long DEFAULT_MIN_EU = 0, DEFAULT_MAX_EU = 2048;
    private static final int DEFAULT_MIN_PERCENT = 33, DEFAULT_MAX_PERCENT = 66;

    public long minValue = DEFAULT_MIN_EU;
    public long maxValue = DEFAULT_MAX_EU;
    private int outputAmount = 0;
    private boolean usePercent = false;
    private WidgetGroup widgetsToUpdate;

    public CoverDetectorEnergyAdvanced(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                       @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.DETECTOR_ENERGY_ADVANCED.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void update() {
        if (getOffsetTimer() % 20 != 0) return;

        long storedEnergy = getCoverHolderStored();
        long energyCapacity = getCoverHolderCapacity();

        if (usePercent) {
            if (energyCapacity > 0) {
                float ratio = (float) storedEnergy / energyCapacity;
                this.outputAmount = RedstoneUtil.computeLatchedRedstoneBetweenValues(ratio * 100, this.maxValue,
                        this.minValue, isInverted(), this.outputAmount);
            } else {
                this.outputAmount = isInverted() ? 0 : 15;
            }
        } else {
            this.outputAmount = RedstoneUtil.computeLatchedRedstoneBetweenValues(storedEnergy,
                    this.maxValue, this.minValue, isInverted(), this.outputAmount);
        }
        setRedstoneSignalOutput(outputAmount);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup group = new WidgetGroup();
        group.addWidget(new LabelWidget(10, 8, "cover.advanced_energy_detector.label"));

        // get/set min EU
        group.addWidget(new LabelWidget(10, 5 + (SIZE + PADDING), "cover.advanced_energy_detector.min"));
        group.addWidget(new ImageWidget(72, (SIZE + PADDING), 8 * SIZE, SIZE, GuiTextures.DISPLAY));

        // get/set max EU
        group.addWidget(new LabelWidget(10, 5 + 2 * (SIZE + PADDING), "cover.advanced_energy_detector.max"));
        group.addWidget(new ImageWidget(72, 2 * (SIZE + PADDING), 8 * SIZE, SIZE, GuiTextures.DISPLAY));

        // surely this is a good idea :clueless:
        // construct widgets that need to be updated
        this.widgetsToUpdate = constructWidgetsToUpdate();

        // change modes between percent and discrete EU
        group.addWidget(new LabelWidget(10, 5 + 3 * (SIZE + PADDING), "cover.advanced_energy_detector.modes_label"));
        group.addWidget(
                new CycleButtonWidget(72, 3 * (SIZE + PADDING), 4 * SIZE, SIZE, this::isUsePercent, this::setUsePercent,
                        "cover.advanced_energy_detector.mode_eu", "cover.advanced_energy_detector.mode_percent")
                                .setTooltipHoverString("cover.advanced_energy_detector.modes_tooltip"));

        // invert logic button
        group.addWidget(new LabelWidget(10, 5 + 4 * (SIZE + PADDING), "cover.generic.advanced_detector.invert_label"));
        group.addWidget(
                new CycleButtonWidget(72, 4 * (SIZE + PADDING), 4 * SIZE, SIZE, this::isInverted, this::setInverted,
                        "cover.machine_controller.normal", "cover.machine_controller.inverted")
                                .setTooltipHoverString("cover.advanced_energy_detector.invert_tooltip"));

        return ModularUI.builder(GuiTextures.BACKGROUND, 176 + (3 * SIZE), 108 + (SIZE))
                .widget(group)
                .widget(widgetsToUpdate) // add synced widgets
                .build(this, player);
    }

    private WidgetGroup constructWidgetsToUpdate() {
        WidgetGroup sync = new WidgetGroup();

        sync.addWidget(
                new TextFieldWidget2(76, 5 + (SIZE + PADDING), 8 * SIZE, SIZE, this::getMinValue, this::setMinValue)
                        .setAllowedChars(TextFieldWidget2.NATURAL_NUMS)
                        .setMaxLength(this.getLength())
                        .setPostFix(this.getPostFix()));
        sync.addWidget(
                new TextFieldWidget2(76, 5 + 2 * (SIZE + PADDING), 8 * SIZE, SIZE, this::getMaxValue, this::setMaxValue)
                        .setAllowedChars(TextFieldWidget2.NATURAL_NUMS)
                        .setMaxLength(this.getLength())
                        .setPostFix(this.getPostFix()));
        return sync;
    }

    private String getMinValue() {
        return String.valueOf(minValue);
    }

    private String getMaxValue() {
        return String.valueOf(maxValue);
    }

    private void setMinValue(String val) {
        this.minValue = CoverDetectorBase.parseCapped(val,
                0,
                this.maxValue - 1,
                usePercent ? DEFAULT_MIN_PERCENT : DEFAULT_MIN_EU);
    }

    private void setMaxValue(String val) {
        this.maxValue = CoverDetectorBase.parseCapped(val,
                this.minValue + 1,
                usePercent ? 100 : Long.MAX_VALUE,
                usePercent ? DEFAULT_MAX_PERCENT : DEFAULT_MAX_EU);
    }

    private boolean isUsePercent() {
        return this.usePercent;
    }

    private void setUsePercent(boolean b) {
        this.usePercent = b;

        if (this.usePercent) { // using percent
            this.minValue = DEFAULT_MIN_PERCENT;
            this.maxValue = DEFAULT_MAX_PERCENT;
        } else { // using discrete EU
            this.minValue = DEFAULT_MIN_EU;
            this.maxValue = DEFAULT_MAX_EU;
        }

        // update widgets
        updateSyncedWidgets();
    }

    private void updateSyncedWidgets() {
        for (Widget widget : widgetsToUpdate.widgets) {
            ((TextFieldWidget2) widget).setPostFix(getPostFix());
            ((TextFieldWidget2) widget).setMaxLength(getLength());
        }
    }

    private String getPostFix() {
        return usePercent ? " %" : " EU";
    }

    private int getLength() {
        return usePercent ? 3 : 19;
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setLong("maxEU", this.maxValue);
        tagCompound.setLong("minEU", this.minValue);
        tagCompound.setInteger("outputAmount", this.outputAmount);
        tagCompound.setBoolean("usePercent", this.usePercent);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.minValue = tagCompound.getLong("minEU");
        this.maxValue = tagCompound.getLong("maxEU");
        this.outputAmount = tagCompound.getInteger("outputAmount");
        this.usePercent = tagCompound.getBoolean("usePercent");

        readDeprecatedInvertedKeyFromNBT(tagCompound);
    }

    // inverted here was saved using different key, now it is normalized but construction is for compatibility
    private void readDeprecatedInvertedKeyFromNBT(@NotNull NBTTagCompound tagCompound) {
        String oldInvertedKey = "inverted";
        if (!tagCompound.hasKey(NBT_KEY_IS_INVERTED) && tagCompound.hasKey(oldInvertedKey)) {
            setInverted(tagCompound.getBoolean(oldInvertedKey));
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeLong(this.minValue);
        packetBuffer.writeLong(this.maxValue);
        packetBuffer.writeInt(this.outputAmount);
        packetBuffer.writeBoolean(this.usePercent);
    }

    @Override
    public void readInitialSyncData(@NotNull AdvancedPacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.minValue = packetBuffer.readLong();
        this.maxValue = packetBuffer.readLong();
        this.outputAmount = packetBuffer.readInt();
        this.usePercent = packetBuffer.readBoolean();
    }
}
