package gregtech.common.covers.detector;

import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.common.mui.widget.GTTextFieldWidget;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_INVERTED;

public abstract class CoverDetectorBase extends CoverBase {

    protected static final String NBT_KEY_IS_INVERTED = "isInverted";

    private boolean isInverted = false;
    private int redstoneSignalOutput = 0;

    public CoverDetectorBase(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                             @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    protected boolean isInverted() {
        return this.isInverted;
    }

    protected void setInverted(boolean isInverted) {
        this.isInverted = isInverted;
    }

    private void toggleInvertedWithNotification() {
        setInverted(!isInverted());

        CoverableView coverable = getCoverableView();
        if (!coverable.getWorld().isRemote) {
            coverable.writeCoverData(this, UPDATE_INVERTED, b -> b.writeBoolean(isInverted()));
            coverable.notifyBlockUpdate();
            coverable.markDirty();
        }
    }

    public final void setRedstoneSignalOutput(int redstoneSignalOutput) {
        this.redstoneSignalOutput = redstoneSignalOutput;
        getCoverableView().notifyBlockUpdate();
        getCoverableView().markDirty();
    }

    @Override
    public int getRedstoneSignalOutput() {
        return this.redstoneSignalOutput;
    }

    public boolean usesMui2() {
        return true;
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean(NBT_KEY_IS_INVERTED, isInverted());
        if (redstoneSignalOutput > 0) {
            tagCompound.setInteger("RedstoneSignal", redstoneSignalOutput);
        }
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        if (tagCompound.hasKey(NBT_KEY_IS_INVERTED)) { // compatibility check
            setInverted(tagCompound.getBoolean(NBT_KEY_IS_INVERTED));
        }
        this.redstoneSignalOutput = tagCompound.getInteger("RedstoneSignal");
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeBoolean(isInverted());
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        setInverted(packetBuffer.readBoolean());
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote) {
            return EnumActionResult.SUCCESS;
        }

        String translationKey = isInverted() ? "gregtech.cover.detector_base.message_inverted_state" :
                "gregtech.cover.detector_base.message_normal_state";
        playerIn.sendStatusMessage(new TextComponentTranslation(translationKey), true);

        toggleInvertedWithNotification();

        return EnumActionResult.SUCCESS;
    }

    /**
     * Returns parsed result of {@code value} as long, or {@code fallbackValue} if the parse fails.
     *
     * @param value         String to parse
     * @param minValue      Minimum value
     * @param maxValue      Maximum value
     * @param fallbackValue Fallback value to be used in case of parse failure.
     * @return Capped value of either parsed result or {@code fallbackValue}
     */
    protected static long parseCapped(String value, long minValue, long maxValue, long fallbackValue) {
        long parsedValue;
        try {
            parsedValue = Long.parseLong(value);
        } catch (NumberFormatException e) {
            parsedValue = fallbackValue;
        }
        return Math.min(Math.max(parsedValue, minValue), maxValue);
    }

    /**
     * Returns parsed result of {@code value} as int, or {@code fallbackValue} if the parse fails.
     *
     * @param value         String to parse
     * @param minValue      Minimum value
     * @param maxValue      Maximum value
     * @param fallbackValue Fallback value to be used in case of parse failure.
     * @return Capped value of either parsed result or {@code fallbackValue}
     */
    protected static int parseCapped(String value, int minValue, int maxValue, int fallbackValue) {
        int parsedValue;
        try {
            parsedValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            parsedValue = fallbackValue;
        }
        return Math.min(Math.max(parsedValue, minValue), maxValue);
    }

    protected static Flow createMinMaxRow(@NotNull String lang, @NotNull Supplier<String> getter,
                                          @Nullable Consumer<String> setter) {
        return createMinMaxRow(lang, getter, setter, null, null);
    }

    protected static Flow createMinMaxRow(@NotNull String lang, @NotNull Supplier<String> getter,
                                          @Nullable Consumer<String> setter,
                                          @Nullable Supplier<String> postFix,
                                          @Nullable Consumer<GTTextFieldWidget> listener) {
        return Flow.row()
                .widthRel(1f)
                .coverChildrenHeight()
                .marginBottom(5)
                .child(IKey.lang(lang).asWidget())
                .child(new GTTextFieldWidget()
                        .right(0)
                        .size(90, 18)
                        .setTextColor(Color.WHITE.main)
                        .setPattern(TextFieldWidget.WHOLE_NUMS)
                        .setPostFix(postFix == null ? IKey.EMPTY::get : postFix)
                        .onUpdateListener(listener)
                        .value(new StringSyncValue(getter, setter)));
    }
}
