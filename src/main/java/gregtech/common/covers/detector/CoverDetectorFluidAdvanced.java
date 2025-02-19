package gregtech.common.covers.detector;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.util.RedstoneUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.filter.FluidFilterContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
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
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup group = new WidgetGroup();
        group.addWidget(new LabelWidget(10, 8, "cover.advanced_fluid_detector.label"));

        // set min fluid amount
        group.addWidget(new LabelWidget(10, 5 + (SIZE + PADDING), "cover.advanced_fluid_detector.min"));
        group.addWidget(new ImageWidget(98 - 4, (SIZE + PADDING), 4 * SIZE, SIZE, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget2(98, 5 + (SIZE + PADDING), 4 * SIZE, SIZE,
                this::getMinValue, this::setMinValue)
                        .setMaxLength(10)
                        .setAllowedChars(TextFieldWidget2.WHOLE_NUMS)
                        .setPostFix("L"));

        // set max fluid amount
        group.addWidget(new LabelWidget(10, 5 + 2 * (SIZE + PADDING), "cover.advanced_fluid_detector.max"));
        group.addWidget(new ImageWidget(98 - 4, 2 * (SIZE + PADDING), 4 * SIZE, SIZE, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget2(98, 5 + 2 * (SIZE + PADDING), 4 * SIZE, SIZE,
                this::getMaxValue, this::setMaxValue)
                        .setMaxLength(10)
                        .setAllowedChars(TextFieldWidget2.WHOLE_NUMS)
                        .setPostFix("L"));

        // invert logic button
        // group.addWidget(new LabelWidget(10, 5 + 3 * (SIZE + PADDING),
        // "cover.generic.advanced_detector.invert_label"));
        group.addWidget(
                new CycleButtonWidget(10, 3 * (SIZE + PADDING), 4 * SIZE, SIZE, this::isInverted, this::setInverted,
                        "cover.advanced_energy_detector.normal", "cover.advanced_energy_detector.inverted")
                                .setTooltipHoverString("cover.generic.advanced_detector.invert_tooltip"));
        group.addWidget(
                new CycleButtonWidget(94, 3 * (SIZE + PADDING), 4 * SIZE, SIZE, this::isLatched, this::setLatched,
                        "cover.generic.advanced_detector.continuous", "cover.generic.advanced_detector.latched")
                                .setTooltipHoverString("cover.generic.advanced_detector.latch_tooltip"));

        this.fluidFilter.initUI(5 + 4 * (SIZE + PADDING), group::addWidget);

        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 164 + 4 * (SIZE + PADDING))
                .widget(group)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 164)
                .build(this, player);
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
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeInt(this.min);
        packetBuffer.writeInt(this.max);
        packetBuffer.writeBoolean(this.isLatched);
    }

    @Override
    public void readInitialSyncData(@NotNull AdvancedPacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.min = packetBuffer.readInt();
        this.max = packetBuffer.readInt();
        this.isLatched = packetBuffer.readBoolean();
    }
}
