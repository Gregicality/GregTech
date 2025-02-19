package gregtech.common.metatileentities.storage;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.custom.QuantumStorageRenderer;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityCreativeChest extends MetaTileEntityQuantumChest {

    private int itemsPerCycle = 1;
    private int ticksPerCycle = 1;

    private final GTItemStackHandler handler;

    private boolean active;

    public MetaTileEntityCreativeChest(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.MAX, 0);
        this.handler = new CreativeItemStackHandler(1);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.QUANTUM_STORAGE_RENDERER.renderMachine(renderState, translation,
                ArrayUtils.add(pipeline,
                        new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))),
                this);
        Textures.CREATIVE_CONTAINER_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (!isConnected() && active) {
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        }
        renderIndicatorOverlay(renderState, translation, pipeline);
    }

    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        QuantumStorageRenderer.renderChestStack(x, y, z, this, this.virtualItemStack, 420, partialTicks);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCreativeChest(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 209)
                .bindPlayerInventory(entityPlayer.inventory, 126);
        builder.widget(new PhantomSlotWidget(handler, 0, 36, 6)
                .setClearSlotOnRightClick(true)
                .setBackgroundTexture(GuiTextures.SLOT)
                .setChangeListener(this::markDirty));
        builder.label(7, 9, "gregtech.creative.chest.item");
        builder.widget(new ImageWidget(7, 48, 154, 14, GuiTextures.DISPLAY));
        builder.widget(new TextFieldWidget2(9, 50, 152, 10, () -> String.valueOf(itemsPerCycle), value -> {
            if (!value.isEmpty()) {
                itemsPerCycle = Integer.parseInt(value);
            }
        }).setMaxLength(11).setNumbersOnly(1, Integer.MAX_VALUE));
        builder.label(7, 28, "gregtech.creative.chest.ipc");

        builder.widget(new ImageWidget(7, 85, 154, 14, GuiTextures.DISPLAY));
        builder.widget(new TextFieldWidget2(9, 87, 152, 10, () -> String.valueOf(ticksPerCycle), value -> {
            if (!value.isEmpty()) {
                ticksPerCycle = Integer.parseInt(value);
            }
        }).setMaxLength(11).setNumbersOnly(1, Integer.MAX_VALUE));
        builder.label(7, 65, "gregtech.creative.chest.tpc");

        builder.widget(new CycleButtonWidget(7, 101, 162, 20, () -> active, value -> {
            active = value;
            scheduleRenderUpdate();
            var c = getQuantumController();
            if (c != null) c.onHandlerUpdate();
        }, "gregtech.creative.activity.off", "gregtech.creative.activity.on"));

        builder.widget(createConnectedGui(6));

        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void update() {
        ItemStack stack = handler.getStackInSlot(0).copy();
        this.virtualItemStack = stack; // For rendering purposes
        super.update();
        if (ticksPerCycle == 0 || getOffsetTimer() % ticksPerCycle != 0) return;
        if (getWorld().isRemote || !active || stack.isEmpty() || isConnected()) return;

        TileEntity tile = getNeighbor(getOutputFacing());
        if (tile != null) {
            IItemHandler container = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    this.getOutputFacing().getOpposite());
            if (container == null || container.getSlots() == 0)
                return;
            stack.setCount(itemsPerCycle);

            ItemStack remainder = GTTransferUtils.insertItem(container, stack, true);
            int amountToInsert = stack.getCount() - remainder.getCount();
            if (amountToInsert > 0) {
                GTTransferUtils.insertItem(container, stack, false);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("ItemStackHandler", handler.serializeNBT());
        data.setInteger("ItemsPerCycle", itemsPerCycle);
        data.setInteger("TicksPerCycle", ticksPerCycle);
        data.setBoolean("Active", active);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        handler.deserializeNBT(data.getCompoundTag("ItemStackHandler"));
        this.virtualItemStack = handler.getStackInSlot(0); // For rendering purposes
        itemsPerCycle = data.getInteger("ItemsPerCycle");
        ticksPerCycle = data.getInteger("TicksPerCycle");
        active = data.getBoolean("Active");
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        if (itemStack.hasKey("id", 8)) { // Check if ItemStack wrote to this
            this.handler.setStackInSlot(0, new ItemStack(itemStack));
        }
        itemsPerCycle = itemStack.getInteger("mBPerCycle");
        ticksPerCycle = itemStack.getInteger("ticksPerCycle");
    }

    @Override
    public void writeItemStackData(NBTTagCompound tag) {
        super.writeItemStackData(tag);
        ItemStack stack = this.handler.getStackInSlot(0);
        if (!stack.isEmpty()) {
            stack.writeToNBT(tag);
        }
        tag.setInteger("mBPerCycle", itemsPerCycle);
        tag.setInteger("ticksPerCycle", ticksPerCycle);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.creative_tooltip.1") + TooltipHelper.RAINBOW +
                I18n.format("gregtech.creative_tooltip.2") + I18n.format("gregtech.creative_tooltip.3"));
        // do not append the normal tooltips
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.handler.setStackInSlot(0, this.virtualItemStack);
    }

    @Override
    public IItemHandler getTypeValue() {
        return this.handler;
    }

    protected class CreativeItemStackHandler extends GTItemStackHandler {

        CreativeItemStackHandler(int size) {
            super(MetaTileEntityCreativeChest.this, size);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!active) return ItemStack.EMPTY;
            return GTUtility.copy(Math.min(amount, itemsPerCycle), getStackInSlot(0));
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            super.setStackInSlot(slot, stack);
            virtualItemStack = GTUtility.copy(1, stack);
        }
    }
}
