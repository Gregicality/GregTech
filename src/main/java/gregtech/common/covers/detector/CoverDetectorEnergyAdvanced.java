package gregtech.common.covers.detector;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.RedstoneUtil;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;

public class CoverDetectorEnergyAdvanced extends CoverDetectorEnergy implements CoverWithUI {

    private static final long DEFAULT_MIN_EU = 0, DEFAULT_MAX_EU = 2048;
    private static final int DEFAULT_MIN_PERCENT = 33, DEFAULT_MAX_PERCENT = 66;

    public long minValue = DEFAULT_MIN_EU;
    public long maxValue = DEFAULT_MAX_EU;
    private int outputAmount = 0;
    private boolean usePercent = false;

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
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager syncManager) {
        var min = new StringSyncValue(this::getMinValue, this::setMinValue);
        var max = new StringSyncValue(this::getMaxValue, this::setMaxValue);

        return GTGuis.defaultPanel(this)
                .child(CoverWithUI.createTitleRow(getPickItem()))
                .child(Flow.column()
                        .top(28)
                        .left(10).right(10)
                        .coverChildrenHeight()
                        .child(Flow.row()
                                .widthRel(1f)
                                .coverChildrenHeight()
                                .marginBottom(5)
                                .child(IKey.lang("cover.advanced_energy_detector.min").asWidget())
                                .child(new TextFieldWidget()
                                        .right(0)
                                        .size(90, 18)
                                        .onUpdateListener(this::updateWidget)
                                        .value(min)))
                        .child(Flow.row()
                                .widthRel(1f)
                                .coverChildrenHeight()
                                .marginBottom(5)
                                .child(IKey.lang("cover.advanced_energy_detector.max").asWidget())
                                .child(new TextFieldWidget()
                                        .right(0)
                                        .size(90, 18)
                                        .onUpdateListener(this::updateWidget)
                                        .value(max)))
                        .child(Flow.row()
                                .widthRel(1f)
                                .coverChildrenHeight()
                                .marginBottom(5)
                                .child(IKey.lang("cover.advanced_energy_detector.modes_label").asWidget()
                                        .size(72, 18))
                                .child(new ToggleButton()
                                        .right(0)
                                        .size(72, 18)
                                        .addTooltipLine(IKey.lang("cover.advanced_energy_detector.modes_tooltip"))
                                        .value(new BooleanSyncValue(this::isUsePercent, this::setUsePercent))
                                        .overlay(new DynamicDrawable(() -> IKey
                                                .lang("cover.advanced_energy_detector.mode_" +
                                                        (isUsePercent() ? "percent" : "eu"))
                                                .format(TextFormatting.WHITE)))))
                        .child(Flow.row()
                                .widthRel(1f)
                                .coverChildrenHeight()
                                .child(IKey.lang("cover.generic.advanced_detector.invert_label").asWidget()
                                        .size(72, 18))
                                .child(new ToggleButton()
                                        .right(0)
                                        .size(72, 18)
                                        .addTooltipLine(IKey.lang("cover.advanced_energy_detector.invert_tooltip"))
                                        .value(new BooleanSyncValue(this::isInverted, this::setInverted))
                                        .overlay(new DynamicDrawable(() -> IKey
                                                .lang("cover.advanced_energy_detector." +
                                                        (isInverted() ? "inverted" : "normal"))
                                                .format(TextFormatting.WHITE))))));
    }

    private void updateWidget(TextFieldWidget w) {
        w.setMaxLength(getLength());
        w.setNumbers(0, isUsePercent() ? 100 : Integer.MAX_VALUE);
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
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeLong(this.minValue);
        packetBuffer.writeLong(this.maxValue);
        packetBuffer.writeInt(this.outputAmount);
        packetBuffer.writeBoolean(this.usePercent);
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.minValue = packetBuffer.readLong();
        this.maxValue = packetBuffer.readLong();
        this.outputAmount = packetBuffer.readInt();
        this.usePercent = packetBuffer.readBoolean();
    }
}
