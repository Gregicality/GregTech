package gregtech.common.metatileentities.storage;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.ItemHandlerProxy;
import gregtech.api.cover.CoverRayTracer;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.custom.QuantumStorageRenderer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.*;

public class MetaTileEntityQuantumChest extends MetaTileEntityQuantumStorage<IItemHandler>
                                        implements ITieredMetaTileEntity, IActiveOutputSide, IFastRenderMetaTileEntity {

    private final int tier;
    protected final long maxStoredItems;
    /** The ItemStack that the Quantum Chest is storing */
    protected ItemStack virtualItemStack = ItemStack.EMPTY;
    protected long itemsStoredInside = 0L;
    private boolean autoOutputItems;
    private EnumFacing outputFacing;
    private boolean allowInputFromOutputSide = false;
    private static final String NBT_ITEMSTACK = "ItemStack";
    private static final String NBT_PARTIALSTACK = "PartialStack";
    private static final String NBT_ITEMCOUNT = "ItemAmount";
    private static final String IS_VOIDING = "IsVoiding";
    protected IItemHandler outputItemInventory;
    private ItemHandlerList combinedInventory;
    protected ItemStack previousStack;
    protected long previousStackSize;
    protected boolean voiding;

    public MetaTileEntityQuantumChest(ResourceLocation metaTileEntityId, int tier, long maxStoredItems) {
        super(metaTileEntityId);
        this.tier = tier;
        this.maxStoredItems = maxStoredItems;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityQuantumChest(metaTileEntityId, tier, maxStoredItems);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.QUANTUM_STORAGE_RENDERER.renderMachine(renderState, translation,
                ArrayUtils.add(pipeline,
                        new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))),
                this);
        Textures.QUANTUM_CHEST_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        if (outputFacing != null) {
            Textures.PIPE_OUT_OVERLAY.renderSided(outputFacing, renderState, translation, pipeline);
            if (isAutoOutputItems()) {
                Textures.ITEM_OUTPUT_OVERLAY.renderSided(outputFacing, renderState, translation, pipeline);
            }
        }
        renderIndicatorOverlay(renderState, translation, pipeline);
    }

    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        QuantumStorageRenderer.renderChestStack(x, y, z, this, virtualItemStack, itemsStoredInside, partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[tier].getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    public void update() {
        super.update();
        EnumFacing currentOutputFacing = getOutputFacing();
        if (!getWorld().isRemote) {
            if (itemsStoredInside < maxStoredItems) {
                ItemStack inputStack = importItems.getStackInSlot(0);
                ItemStack outputStack = exportItems.getStackInSlot(0);
                if (!inputStack.isEmpty() &&
                        (virtualItemStack.isEmpty() || areItemStackIdentical(outputStack, inputStack))) {
                    GTTransferUtils.moveInventoryItems(importItems, combinedInventory);

                    markDirty();
                }
            }
            if (itemsStoredInside > 0 && !virtualItemStack.isEmpty()) {
                ItemStack outputStack = exportItems.getStackInSlot(0);
                int maxStackSize = virtualItemStack.getMaxStackSize();
                if (outputStack.isEmpty() || (areItemStackIdentical(virtualItemStack, outputStack) &&
                        outputStack.getCount() < maxStackSize)) {
                    GTTransferUtils.moveInventoryItems(itemInventory, exportItems);

                    markDirty();
                }
            }

            if (isAutoOutputItems()) {
                pushItemsIntoNearbyHandlers(currentOutputFacing);
            }

            if (this.voiding && !importItems.getStackInSlot(0).isEmpty()) {
                importItems.setStackInSlot(0, ItemStack.EMPTY);
            }

            if (previousStack == null || !areItemStackIdentical(previousStack, virtualItemStack)) {
                writeCustomData(UPDATE_ITEM, buf -> {
                    virtualItemStack.setCount(1);
                    buf.writeItemStack(virtualItemStack);
                });
                previousStack = virtualItemStack;
            }
            if (previousStackSize != itemsStoredInside) {
                writeCustomData(UPDATE_ITEM_COUNT, buf -> buf.writeLong(itemsStoredInside));
                previousStackSize = itemsStoredInside;
            }
        }
    }

    protected static boolean areItemStackIdentical(ItemStack first, ItemStack second) {
        return ItemStack.areItemsEqual(first, second) &&
                ItemStack.areItemStackTagsEqual(first, second);
    }

    protected void addDisplayInformation(List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation("gregtech.machine.quantum_chest.items_stored"));
        textList.add(new TextComponentString(String.format("%,d", itemsStoredInside)));
        ItemStack export = exportItems.getStackInSlot(0);
        if (!export.isEmpty()) {
            textList.add(
                    new TextComponentString(TextFormattingUtil.formatStringWithNewlines(export.getDisplayName(), 14)));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_total", maxStoredItems));

        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null) {
            String translationKey = null;
            long count = 0;
            int exportCount = 0;
            if (compound.hasKey(NBT_ITEMSTACK)) {
                count = compound.getLong(NBT_ITEMCOUNT);
            }
            if (compound.hasKey(NBT_PARTIALSTACK)) {
                ItemStack tempStack = new ItemStack(compound.getCompoundTag(NBT_PARTIALSTACK));
                translationKey = tempStack.getDisplayName();
                exportCount = tempStack.getCount();
            }
            if (translationKey != null) {
                tooltip.add(I18n.format("gregtech.universal.tooltip.item_stored",
                        I18n.format(translationKey), count, exportCount));
            }
        }
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.itemInventory = new QuantumChestItemHandler();
        List<IItemHandler> temp = new ArrayList<>();
        temp.add(this.exportItems);
        temp.add(this.itemInventory);
        this.combinedInventory = new ItemHandlerList(temp);
        this.outputItemInventory = new ItemHandlerProxy(new GTItemStackHandler(this, 0), combinedInventory);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new GTItemStackHandler(this, 1) {

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (!isItemValid(slot, stack)) return stack;
                return GTTransferUtils.insertItem(getCombinedInventory(), stack, simulate);
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                NBTTagCompound compound = stack.getTagCompound();
                ItemStack outStack = getExportItems().getStackInSlot(0);
                boolean outStackMatch = true;
                if (!outStack.isEmpty()) {
                    outStackMatch = areItemStackIdentical(stack, outStack);
                }
                if (compound == null) return true;
                return outStackMatch && !(compound.hasKey(NBT_ITEMSTACK, NBT.TAG_COMPOUND) ||
                        compound.hasKey("Fluid", NBT.TAG_COMPOUND)); // prevents inserting items with NBT to the Quantum
                                                                     // Chest
            }
        };
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, 1);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tagCompound = super.writeToNBT(data);
        data.setInteger("OutputFacing", getOutputFacing().getIndex());
        data.setBoolean("AutoOutputItems", autoOutputItems);
        data.setBoolean("AllowInputFromOutputSide", allowInputFromOutputSide);
        if (!virtualItemStack.isEmpty() && itemsStoredInside > 0L) {
            tagCompound.setTag(NBT_ITEMSTACK, virtualItemStack.writeToNBT(new NBTTagCompound()));
            tagCompound.setLong(NBT_ITEMCOUNT, itemsStoredInside);
        }
        data.setBoolean(IS_VOIDING, voiding);
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.outputFacing = EnumFacing.VALUES[data.getInteger("OutputFacing")];
        this.autoOutputItems = data.getBoolean("AutoOutputItems");
        this.allowInputFromOutputSide = data.getBoolean("AllowInputFromOutputSide");
        if (data.hasKey("ItemStack", NBT.TAG_COMPOUND)) {
            this.virtualItemStack = new ItemStack(data.getCompoundTag("ItemStack"));
            if (!virtualItemStack.isEmpty()) {
                this.itemsStoredInside = data.getLong(NBT_ITEMCOUNT);
            }
        }
        if (data.hasKey(IS_VOIDING)) {
            this.voiding = data.getBoolean(IS_VOIDING);
        }
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        if (itemStack.hasKey(NBT_ITEMSTACK, NBT.TAG_COMPOUND)) {
            this.virtualItemStack = new ItemStack(itemStack.getCompoundTag(NBT_ITEMSTACK));
            if (!this.virtualItemStack.isEmpty()) {
                this.itemsStoredInside = itemStack.getLong(NBT_ITEMCOUNT);
            }
        }
        if (itemStack.hasKey(NBT_PARTIALSTACK, NBT.TAG_COMPOUND)) {
            exportItems.setStackInSlot(0, new ItemStack(itemStack.getCompoundTag(NBT_PARTIALSTACK)));
        }

        if (itemStack.getBoolean(IS_VOIDING)) {
            setVoiding(true);
        }
    }

    @Override
    public void writeItemStackData(NBTTagCompound itemStack) {
        super.writeItemStackData(itemStack);
        if (!this.virtualItemStack.isEmpty()) {
            itemStack.setTag(NBT_ITEMSTACK, this.virtualItemStack.writeToNBT(new NBTTagCompound()));
            itemStack.setLong(NBT_ITEMCOUNT, itemsStoredInside);
        }
        ItemStack partialStack = exportItems.extractItem(0, 64, false);
        if (!partialStack.isEmpty()) {
            itemStack.setTag(NBT_PARTIALSTACK, partialStack.writeToNBT(new NBTTagCompound()));
        }

        if (this.voiding) {
            itemStack.setBoolean(IS_VOIDING, true);
        }

        this.virtualItemStack = ItemStack.EMPTY;
        this.itemsStoredInside = 0;
        exportItems.setStackInSlot(0, ItemStack.EMPTY);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        Builder builder = ModularUI.defaultBuilder();
        builder.image(7, 16, 81, 46, GuiTextures.DISPLAY);
        builder.widget(new AdvancedTextWidget(11, 20, this::addDisplayInformation, 0xFFFFFF));
        builder.label(6, 6, getMetaFullName())
                .widget(new SlotWidget(importItems, 0, 90, 17, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
                .widget(new SlotWidget(exportItems, 0, 90, 44, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY))
                .widget(new ToggleButtonWidget(7, 64, 18, 18,
                        GuiTextures.BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setAutoOutputItems)
                                .shouldUseBaseBackground()
                                .setTooltipText("gregtech.gui.item_auto_output.tooltip"))
                .widget(new ToggleButtonWidget(25, 64, 18, 18,
                        GuiTextures.BUTTON_ITEM_VOID, this::isVoiding, this::setVoiding)
                                .setTooltipText("gregtech.gui.item_voiding.tooltip")
                                .shouldUseBaseBackground())
                .bindPlayerInventory(entityPlayer.inventory);

        builder.widget(createConnectedGui(64));

        return builder.build(getHolder(), entityPlayer);
    }

    public EnumFacing getOutputFacing() {
        return outputFacing == null ? frontFacing.getOpposite() : outputFacing;
    }

    public void setOutputFacing(EnumFacing outputFacing) {
        this.outputFacing = outputFacing;
        if (!getWorld().isRemote) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_OUTPUT_FACING, buf -> buf.writeByte(outputFacing.getIndex()));
            markDirty();
        }
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                 CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getOutputFacing() == facing || getFrontFacing() == facing) {
                return false;
            }
            if (!getWorld().isRemote) {
                setOutputFacing(facing);
            }
            return true;
        }
        return super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(getOutputFacing().getIndex());
        buf.writeBoolean(autoOutputItems);
        this.virtualItemStack.setCount(1);
        buf.writeItemStack(virtualItemStack);
        buf.writeLong(itemsStoredInside);
        buf.writeBoolean(voiding);
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.outputFacing = EnumFacing.VALUES[buf.readByte()];
        this.autoOutputItems = buf.readBoolean();
        try {
            this.virtualItemStack = buf.readItemStack();
        } catch (IOException ignored) {
            GTLog.logger.warn("Failed to load item from NBT in a quantum chest at " + this.getPos() +
                    " on initial server/client sync");
        }
        this.itemsStoredInside = buf.readLong();
        this.voiding = buf.readBoolean();
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        // use direct outputFacing field instead of getter method because otherwise
        // it will just return SOUTH for null output facing
        return super.isValidFrontFacing(facing) && facing != outputFacing;
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_OUTPUT_FACING) {
            this.outputFacing = EnumFacing.VALUES[buf.readByte()];
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_ITEMS) {
            this.autoOutputItems = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_ITEM) {
            try {
                this.virtualItemStack = buf.readItemStack();
            } catch (IOException e) {
                GTLog.logger.error("Failed to read item stack in a quantum chest!");
            }
        } else if (dataId == UPDATE_ITEM_COUNT) {
            this.itemsStoredInside = buf.readLong();
        } else if (dataId == UPDATE_IS_VOIDING) {
            setVoiding(buf.readBoolean());
        }
    }

    public void setAutoOutputItems(boolean autoOutputItems) {
        this.autoOutputItems = autoOutputItems;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_OUTPUT_ITEMS, buf -> buf.writeBoolean(autoOutputItems));
            markDirty();
        }
    }

    protected boolean isVoiding() {
        return this.voiding;
    }

    protected void setVoiding(boolean isVoiding) {
        this.voiding = isVoiding;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_IS_VOIDING, buf -> buf.writeBoolean(this.voiding));
            markDirty();
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE) {
            if (side == getOutputFacing()) {
                return GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE.cast(this);
            }
            return null;
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {

            // for TOP/Waila
            if (side == null) return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(combinedInventory);

            // try fix being able to insert through output hole when input on output is disabled
            IItemHandler itemHandler = (side == getOutputFacing() && !isAllowInputFromOutputSideItems()) ?
                    outputItemInventory : combinedInventory;
            if (itemHandler.getSlots() > 0) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
            }
            return null;
        }
        return super.getCapability(capability, side);
    }

    public IItemHandler getCombinedInventory() {
        return this.combinedInventory;
    }

    public IItemHandler getOutputItemInventory() {
        return this.outputItemInventory;
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.outputFacing == null) {
            // set initial output facing as opposite to front
            setOutputFacing(frontFacing.getOpposite());
        }
    }

    public boolean isAutoOutputItems() {
        return autoOutputItems;
    }

    @Override
    public boolean isAutoOutputFluids() {
        return false;
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return allowInputFromOutputSide;
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return false;
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        clearInventory(itemBuffer, importItems);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        EnumFacing hitFacing = CoverRayTracer.determineGridSideHit(hitResult);
        if (facing == getOutputFacing() || (hitFacing == getOutputFacing() && playerIn.isSneaking())) {
            if (!getWorld().isRemote) {
                if (isAllowInputFromOutputSideItems()) {
                    setAllowInputFromOutputSide(false);
                    playerIn.sendStatusMessage(
                            new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.disallow"),
                            true);
                } else {
                    setAllowInputFromOutputSide(true);
                    playerIn.sendStatusMessage(
                            new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.allow"), true);
                }
            }
            return true;
        }
        return super.onScrewdriverClick(playerIn, hand, facing, hitResult);
    }

    public void setAllowInputFromOutputSide(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSide = allowInputFromOutputSide;
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    @Override
    public Type getType() {
        return Type.ITEM;
    }

    @Override
    public IItemHandler getTypeValue() {
        return this.combinedInventory;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos());
    }

    private class QuantumChestItemHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return 1;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            ItemStack itemStack = MetaTileEntityQuantumChest.this.virtualItemStack;
            long itemsStored = MetaTileEntityQuantumChest.this.itemsStoredInside;

            if (itemStack.isEmpty() || itemsStored == 0L) {
                return ItemStack.EMPTY;
            }

            ItemStack resultStack = itemStack.copy();
            resultStack.setCount((int) itemsStored);
            return resultStack;
        }

        @Override
        public int getSlotLimit(int slot) {
            return (int) MetaTileEntityQuantumChest.this.maxStoredItems;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int extractedAmount = (int) Math.min(amount, itemsStoredInside);
            if (virtualItemStack.isEmpty() || extractedAmount == 0) {
                return ItemStack.EMPTY;
            }

            ItemStack extractedStack = virtualItemStack.copy();
            extractedStack.setCount(extractedAmount);

            if (!simulate) {
                MetaTileEntityQuantumChest.this.itemsStoredInside -= extractedAmount;
                if (itemsStoredInside == 0L) {
                    MetaTileEntityQuantumChest.this.virtualItemStack = ItemStack.EMPTY;
                }
            }
            return extractedStack;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack insertedStack, boolean simulate) {
            if (insertedStack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            // If there is a virtualized stack and the stack to insert does not match it, do not insert anything
            if (itemsStoredInside > 0L &&
                    !virtualItemStack.isEmpty() &&
                    !areItemStackIdentical(virtualItemStack, insertedStack)) {
                return insertedStack;
            }

            ItemStack exportItems = getExportItems().getStackInSlot(0);

            // if there is an item in the export slot and the inserted stack does not match, do not insert
            if (!exportItems.isEmpty() && !areItemStackIdentical(exportItems, insertedStack)) {
                return insertedStack;
            }

            ItemStack remainingStack = insertedStack.copy();

            // Virtualize the items
            long amountLeftInChest = maxStoredItems - itemsStoredInside;
            int maxPotentialVirtualizedAmount = insertedStack.getCount();

            int actualVirtualizedAmount = (int) Math.min(maxPotentialVirtualizedAmount, amountLeftInChest);

            // if there are any items left over, shrink it by the amount virtualized,
            // which should always be between 0 and the amount left in chest
            remainingStack.shrink(actualVirtualizedAmount);

            if (!simulate) {
                if (actualVirtualizedAmount > 0) {
                    if (virtualItemStack.isEmpty()) {
                        ItemStack virtualStack = insertedStack.copy();

                        // set the virtual stack to 1, since it's mostly for display
                        virtualStack.setCount(1);
                        MetaTileEntityQuantumChest.this.virtualItemStack = virtualStack;
                        MetaTileEntityQuantumChest.this.itemsStoredInside = actualVirtualizedAmount;
                    } else {
                        MetaTileEntityQuantumChest.this.itemsStoredInside += actualVirtualizedAmount;
                    }
                }
            }

            if (isVoiding() && remainingStack.getCount() > 0) {
                return ItemStack.EMPTY;
            } else {
                return remainingStack;
            }
        }
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }
}
