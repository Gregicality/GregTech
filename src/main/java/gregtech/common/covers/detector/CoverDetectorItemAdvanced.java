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
import gregtech.common.covers.filter.ItemFilterContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

public class CoverDetectorItemAdvanced extends CoverDetectorItem implements CoverWithUI {

    private static final int PADDING = 3;
    private static final int SIZE = 18;

    private static final int DEFAULT_MIN = 64;
    private static final int DEFAULT_MAX = 512;

    private int min = DEFAULT_MIN, max = DEFAULT_MAX, outputAmount;
    private boolean isLatched = false;
    protected ItemFilterContainer itemFilter;

    public CoverDetectorItemAdvanced(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                     @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
        this.itemFilter = new ItemFilterContainer(this);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.DETECTOR_ITEM_ADVANCED.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup group = new WidgetGroup();
        group.addWidget(new LabelWidget(10, 8, "cover.advanced_item_detector.label"));

        // set min fluid amount
        group.addWidget(new LabelWidget(10, 5 + (SIZE + PADDING), "cover.advanced_item_detector.min"));
        group.addWidget(new ImageWidget(98 - 4, (SIZE + PADDING), 4 * SIZE, SIZE, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget2(98, 5 + (SIZE + PADDING), 4 * SIZE, SIZE,
                this::getMinValue, this::setMinValue)
                        .setMaxLength(10)
                        .setAllowedChars(TextFieldWidget2.WHOLE_NUMS));

        // set max fluid amount
        group.addWidget(new LabelWidget(10, 5 + 2 * (SIZE + PADDING), "cover.advanced_item_detector.max"));
        group.addWidget(new ImageWidget(98 - 4, 2 * (SIZE + PADDING), 4 * SIZE, SIZE, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget2(98, 5 + 2 * (SIZE + PADDING), 4 * SIZE, SIZE,
                this::getMaxValue, this::setMaxValue)
                        .setMaxLength(10)
                        .setAllowedChars(TextFieldWidget2.WHOLE_NUMS));

        // invert logic button
        // group.addWidget(new LabelWidget(10, 5 + 3 * (SIZE + PADDING),
        // "cover.generic.advanced_detector.invert_label"));
        group.addWidget(
                new CycleButtonWidget(10, 3 * (SIZE + PADDING), 4 * SIZE, SIZE, this::isInverted, this::setInverted,
                        "cover.advanced_energy_detector.normal", "cover.advanced_energy_detector.inverted")
                                .setTooltipHoverString("cover.generic.advanced_detector.invert_tooltip"));
        // group.addWidget(new LabelWidget(10, 5 + 4 * (SIZE + PADDING),
        // "cover.generic.advanced_detector.latch_label"));
        group.addWidget(
                new CycleButtonWidget(94, 3 * (SIZE + PADDING), 4 * SIZE, SIZE, this::isLatched, this::setLatched,
                        "cover.generic.advanced_detector.continuous", "cover.generic.advanced_detector.latched")
                                .setTooltipHoverString("cover.generic.advanced_detector.latch_tooltip"));

        this.itemFilter.initUI(5 + 4 * (SIZE + PADDING), group::addWidget);

        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 188 + 4 * (SIZE + PADDING))
                .widget(group)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 188)
                .build(this, player);
    }

    private String getMinValue() {
        return String.valueOf(min);
    }

    private String getMaxValue() {
        return String.valueOf(max);
    }

    private void setMinValue(String val) {
        int parsedValue;
        try {
            parsedValue = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            parsedValue = DEFAULT_MIN;
        }
        this.min = Math.min(max - 1, Math.max(0, parsedValue));
    }

    private void setMaxValue(String val) {
        int parsedValue;
        try {
            parsedValue = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            parsedValue = DEFAULT_MAX;
        }
        max = Math.max(min + 1, parsedValue);
    }

    private void setLatched(boolean isLatched) {
        this.isLatched = isLatched;
    }

    public boolean isLatched() {
        return this.isLatched;
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

        IItemHandler itemHandler = getCoverableView().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                null);
        if (itemHandler == null) return;

        int storedItems = 0;

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (itemFilter.test(itemHandler.getStackInSlot(i)))
                storedItems += itemHandler.getStackInSlot(i).getCount();
        }

        if (isLatched) {
            outputAmount = RedstoneUtil.computeLatchedRedstoneBetweenValues(storedItems, max, min, isInverted(),
                    outputAmount);
        } else {
            outputAmount = RedstoneUtil.computeRedstoneBetweenValues(storedItems, max, min, isInverted());
        }

        setRedstoneSignalOutput(outputAmount);
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("min", this.min);
        tagCompound.setInteger("max", this.max);
        tagCompound.setBoolean("isLatched", this.isLatched);
        tagCompound.setTag("filter", this.itemFilter.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.min = tagCompound.getInteger("min");
        this.max = tagCompound.getInteger("max");
        this.isLatched = tagCompound.getBoolean("isLatched");
        this.itemFilter.deserializeNBT(tagCompound.getCompoundTag("filter"));
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
