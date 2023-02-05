package gregtech.common.covers.detector;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.filter.ItemFilterContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.regex.Pattern;

public class CoverDetectorItemAdvanced extends CoverDetectorItem implements CoverWithUI {

    private boolean isInverted;
    private int min, max;
    protected ItemFilterContainer itemFilter;

    public CoverDetectorItemAdvanced(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.isInverted = false;
        this.itemFilter = new ItemFilterContainer(this);
        this.min = 64;
        this.max = 512;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        // todo replace with unique texture
        Textures.DETECTOR_ITEM.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        int PADDING = 3;
        int SIZE = 18;

        WidgetGroup group = new WidgetGroup();
        group.addWidget(new LabelWidget(10, 8, "cover.advanced_item_detector.label"));

        // set min fluid amount
        group.addWidget(new LabelWidget(10, 5 + (SIZE + PADDING), "cover.advanced_item_detector.min"));
        group.addWidget(new ImageWidget(98 - 4, (SIZE + PADDING), 4 * SIZE, SIZE, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget2(98, 5 + (SIZE + PADDING), 4 * SIZE, SIZE,
                this::getMinValue, this::setMinValue)
                .setMaxLength(10)
                .setAllowedChars(Pattern.compile(".[0-9]*"))
        );

        // set max fluid amount
        group.addWidget(new LabelWidget(10, 5 + 2 * (SIZE + PADDING), "cover.advanced_item_detector.max"));
        group.addWidget(new ImageWidget(98 - 4, 2 * (SIZE + PADDING), 4 * SIZE, SIZE, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget2(98, 5 + 2 * (SIZE + PADDING), 4 * SIZE, SIZE,
                this::getMaxValue, this::setMaxValue)
                .setMaxLength(10)
                .setAllowedChars(Pattern.compile(".[0-9]*"))
        );

        // invert logic button
        group.addWidget(new LabelWidget(10, 5 + 3 * (SIZE + PADDING), "cover.advanced_energy_detector.invert_label"));
        group.addWidget(new CycleButtonWidget(98 - 4, 3 * (SIZE + PADDING), 4 * SIZE, SIZE, this::isInverted, this::setInverted,
                "cover.advanced_energy_detector.normal", "cover.advanced_energy_detector.inverted")
                .setTooltipHoverString("cover.advanced_item_detector.invert_tooltip")
        );

        this.itemFilter.initUI(5 + 4 * (SIZE + PADDING), group::addWidget);

        return ModularUI.builder(GuiTextures.BACKGROUND,  176, 184 + 82)
                .widget(group)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 184)
                .build(this, player);
    }

    private String getMinValue() {
        return String.valueOf(min);
    }
    private String getMaxValue() {
        return String.valueOf(max);
    }
    private void setMinValue(String val){
        try {
            int c = Integer.parseInt(val);
            this.min = Math.min(max - 1, c);
        } catch (NumberFormatException e) {
            GTLog.logger.warn(e);
            this.min = Math.max(max - 1, 64);
        }
    }
    private void setMaxValue(String val){
        try {
            int c = Integer.parseInt(val);
            max = Math.max(min + 1, c);
        } catch (NumberFormatException e) {
            GTLog.logger.warn(e);
            this.max = Math.max(min + 1, 512);
        }
    }

    private boolean isInverted(){
        return this.isInverted;
    }
    private void setInverted(boolean b){
        this.isInverted = b;
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!this.coverHolder.getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        IItemHandler itemHandler = coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (itemHandler == null)
            return;

        int storedItems = 0;

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if(itemFilter.testItemStack(itemHandler.getStackInSlot(i)))
                storedItems += itemHandler.getStackInSlot(i).getCount();
        }

        setRedstoneSignalOutput(compareValue(storedItems, max, min));
    }

    private int compareValue(int value, float maxValue, float minValue) {
        if (value >= maxValue) {
            return isInverted ? 0 : 15; // value above maxValue should normally be 15, otherwise 0
        } else if (value <= minValue) {
            return isInverted ? 15 : 0; // value below minValue should normally be 0, otherwise 15
        }

        float ratio;
        if (!isInverted) {
            ratio = 15 * (value - minValue) / (maxValue - minValue); // value closer to max results in higher output
        } else {
            ratio = 15 * (maxValue - value) / (maxValue - minValue); // value closer to min results in higher output
        }

        return Math.round(ratio);
    }
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("isInverted", this.isInverted);
        tagCompound.setInteger("min", this.min);
        tagCompound.setInteger("max", this.max);
        tagCompound.setTag("filter", itemFilter.serializeNBT());

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.isInverted = tagCompound.getBoolean("isInverted");
        this.min = tagCompound.getInteger("min");
        this.max = tagCompound.getInteger("max");
        this.itemFilter.deserializeNBT(tagCompound.getCompoundTag("filter"));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeBoolean(this.isInverted);
        packetBuffer.writeInt(this.min);
        packetBuffer.writeInt(this.max);
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.isInverted = packetBuffer.readBoolean();
        this.min = packetBuffer.readInt();
        this.max = packetBuffer.readInt();
    }
}
