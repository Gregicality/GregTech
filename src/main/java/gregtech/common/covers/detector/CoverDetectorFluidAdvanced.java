package gregtech.common.covers.detector;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.gui.widgets.*;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.RedstoneUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.filter.FluidFilterContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

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
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;

public class CoverDetectorFluidAdvanced extends CoverDetectorFluid implements CoverWithUI {

    private static final int PADDING = 3;
    private static final int SIZE = 18;

    private static final int DEFAULT_MIN = 1000; // 1 Bucket
    private static final int DEFAULT_MAX = 16000; // 16 Buckets

    private int min = DEFAULT_MIN, max = DEFAULT_MAX, outputAmount;
    private boolean isLatched = false;

    protected FluidFilterContainer fluidFilter;

    public CoverDetectorFluidAdvanced(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                      @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
        this.fluidFilter = new FluidFilterContainer(this);
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
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.DETECTOR_FLUID_ADVANCED.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager) {
        return GTGuis.defaultPanel(this)
                .height(202)
                .child(CoverWithUI.createTitleRow(getPickItem()))
                .child(Flow.column()
                        .top(28)
                        .left(5).right(5)
                        .child(createMinMaxRow("cover.advanced_fluid_detector.min",
                                this::getMinValue, this::setMinValue,
                                () -> "L", w -> w.setMaxLength(10)))
                        .child(createMinMaxRow("cover.advanced_fluid_detector.max",
                                this::getMaxValue, this::setMaxValue,
                                () -> "L", w -> w.setMaxLength(10)))
                        .child(Flow.row()
                                .widthRel(1f)
                                .coverChildrenHeight()
                                .marginBottom(5)
                                .child(new ToggleButton()
                                        .size(72, 18)
                                        .value(new BooleanSyncValue(this::isInverted, this::setInverted))
                                        .addTooltipLine(IKey.lang("cover.generic.advanced_detector.invert_tooltip"))
                                        .overlay(new DynamicDrawable(() -> {
                                            String lang = "cover.advanced_energy_detector.";
                                            lang += isInverted() ? "inverted" : "normal";
                                            return IKey.lang(lang).format(TextFormatting.WHITE);
                                        })))
                                .child(new ToggleButton()
                                        .size(72, 18)
                                        .right(0)
                                        .overlay(new DynamicDrawable(() -> {
                                            String lang = "cover.generic.advanced_detector.";
                                            lang += isLatched() ? "latched" : "continuous";
                                            return IKey.lang(lang).format(TextFormatting.WHITE);
                                        }))
                                        .addTooltipLine(IKey.lang("cover.generic.advanced_detector.latch_tooltip"))
                                        .value(new BooleanSyncValue(this::isLatched, this::setLatched)))))
                .child(this.fluidFilter.initUI(guiData, guiSyncManager))
                .bindPlayerInventory();
    }

    private String getMinValue() {
        return String.valueOf(min);
    }

    private String getMaxValue() {
        return String.valueOf(max);
    }

    private void setMinValue(String val) {
        this.min = CoverDetectorBase.parseCapped(val, 0, max - 1, DEFAULT_MIN);
    }

    private void setMaxValue(String val) {
        this.max = CoverDetectorBase.parseCapped(val, min + 1, Integer.MAX_VALUE, DEFAULT_MAX);
    }

    private void setLatched(boolean isLatched) {
        this.isLatched = isLatched;
    }

    public boolean isLatched() {
        return this.isLatched;
    }

    @Override
    public void update() {
        if (getOffsetTimer() % 20 != 0) return;

        IFluidHandler fluidHandler = getCoverableView().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                null);
        if (fluidHandler == null) return;

        IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
        int storedFluid = 0;

        for (IFluidTankProperties properties : tankProperties) {
            FluidStack contents = properties.getContents();

            if (contents != null && fluidFilter.test(contents))
                storedFluid += contents.amount;
        }

        if (isLatched) {
            outputAmount = RedstoneUtil.computeLatchedRedstoneBetweenValues(storedFluid, max, min, isInverted(),
                    outputAmount);
        } else {
            outputAmount = RedstoneUtil.computeRedstoneBetweenValues(storedFluid, max, min, isInverted());
        }

        setRedstoneSignalOutput(outputAmount);
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("min", this.min);
        tagCompound.setInteger("max", this.max);
        tagCompound.setBoolean("isLatched", this.isLatched);
        tagCompound.setTag("filter", this.fluidFilter.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.min = tagCompound.getInteger("min");
        this.max = tagCompound.getInteger("max");
        this.isLatched = tagCompound.getBoolean("isLatched");
        this.fluidFilter.deserializeNBT(tagCompound.getCompoundTag("filter"));
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeInt(this.min);
        packetBuffer.writeInt(this.max);
        packetBuffer.writeBoolean(this.isLatched);
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.min = packetBuffer.readInt();
        this.max = packetBuffer.readInt();
        this.isLatched = packetBuffer.readBoolean();
    }
}
