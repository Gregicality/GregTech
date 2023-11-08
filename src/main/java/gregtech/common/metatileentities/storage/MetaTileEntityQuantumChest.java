package gregtech.common.metatileentities.storage;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.ItemHandlerProxy;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.custom.QuantumStorageRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.*;

public class MetaTileEntityQuantumChest extends MetaTileEntity implements ITieredMetaTileEntity, IActiveOutputSide, IFastRenderMetaTileEntity {

    private final int tier;
    private final long maxStoredItems;
    protected ItemStack virtualItemStack = ItemStack.EMPTY;
    private long itemsStoredInside = 0L;
    private boolean autoOutputItems;
    private EnumFacing outputFacing;
    private boolean allowInputFromOutputSide = false;
    private static final String NBT_ITEMSTACK = "ItemStack";
    private static final String NBT_PARTIALSTACK = "PartialStack";
    private static final String NBT_ITEMCOUNT = "ItemAmount";
    protected IItemHandler outputItemInventory;
    private ItemHandlerList combinedInventory;
    private ItemStack previousStack;
    private long previousStackSize;

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
                ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))),
                this.getFrontFacing(), this.tier);
        Textures.QUANTUM_CHEST_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        if (outputFacing != null) {
            Textures.PIPE_OUT_OVERLAY.renderSided(outputFacing, renderState, translation, pipeline);
            if (isAutoOutputItems()) {
                Textures.ITEM_OUTPUT_OVERLAY.renderSided(outputFacing, renderState, translation, pipeline);
            }
        }
    }

    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        QuantumStorageRenderer.renderChestStack(x, y, z, this, virtualItemStack, itemsStoredInside, partialTicks);
    }

    @Override
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
                if (outputStack.isEmpty() || outputStack.isItemEqual(inputStack) && ItemStack.areItemStackTagsEqual(inputStack, outputStack)) {
                    if (!inputStack.isEmpty() && (virtualItemStack.isEmpty() || areItemStackIdentical(virtualItemStack, inputStack))) {
                        int amountOfItemsToInsert = (int) Math.min(inputStack.getCount(), maxStoredItems - itemsStoredInside);
                        if (this.itemsStoredInside == 0L || virtualItemStack.isEmpty()) {
                            this.virtualItemStack = GTUtility.copy(1, inputStack);
                        }
                        inputStack.shrink(amountOfItemsToInsert);
                        importItems.setStackInSlot(0, inputStack);
                        this.itemsStoredInside += amountOfItemsToInsert;

                        markDirty();
                    }
                }
            }
            if (itemsStoredInside > 0 && !virtualItemStack.isEmpty()) {
                ItemStack outputStack = exportItems.getStackInSlot(0);
                int maxStackSize = virtualItemStack.getMaxStackSize();
                if (outputStack.isEmpty() || (areItemStackIdentical(virtualItemStack, outputStack) && outputStack.getCount() < maxStackSize)) {
                    int amountOfItemsToRemove = (int) Math.min(maxStackSize - outputStack.getCount(), itemsStoredInside);
                    if (outputStack.isEmpty()) {
                        outputStack = GTUtility.copy(amountOfItemsToRemove, virtualItemStack);
                    } else outputStack.grow(amountOfItemsToRemove);
                    exportItems.setStackInSlot(0, outputStack);
                    this.itemsStoredInside -= amountOfItemsToRemove;
                    if (this.itemsStoredInside == 0) {
                        this.virtualItemStack = ItemStack.EMPTY;
                    }

                    markDirty();
                }

            }
            if (isAutoOutputItems()) {
                pushItemsIntoNearbyHandlers(currentOutputFacing);
            }
            if (previousStack == null || !areItemStackIdentical(previousStack, virtualItemStack)) {
                writeCustomData(UPDATE_ITEM, buf -> buf.writeItemStack(virtualItemStack));
                previousStack = virtualItemStack;
            }
            if (previousStackSize != itemsStoredInside) {
                writeCustomData(UPDATE_ITEM_COUNT, buf -> buf.writeLong(itemsStoredInside));
                previousStackSize = itemsStoredInside;
            }
        }
    }

    private static boolean areItemStackIdentical(ItemStack first, ItemStack second) {
        return ItemStack.areItemsEqual(first, second) &&
                ItemStack.areItemStackTagsEqual(first, second);
    }

    protected void addDisplayInformation(List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation("gregtech.machine.quantum_chest.items_stored"));
        textList.add(new TextComponentString(String.format("%,d", itemsStoredInside)));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.quantum_chest.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_total", maxStoredItems));

        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null) {
            String translationKey = null;
            long count = 0;
            if (compound.hasKey(NBT_ITEMSTACK)) {
                translationKey = new ItemStack(compound.getCompoundTag(NBT_ITEMSTACK)).getDisplayName();
                count = compound.getLong(NBT_ITEMCOUNT);
            } else if (compound.hasKey(NBT_PARTIALSTACK)) {
                ItemStack tempStack = new ItemStack(compound.getCompoundTag(NBT_PARTIALSTACK));
                translationKey = tempStack.getDisplayName();
                count = tempStack.getCount();
            }
            if (translationKey != null) {
                tooltip.add(I18n.format("gregtech.universal.tooltip.item_stored",
                        I18n.format(translationKey), count));
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
        this.outputItemInventory = new ItemHandlerProxy(new ItemStackHandler(0), exportItems);
        List<IItemHandler> temp = new ArrayList<>();
        temp.add(outputItemInventory);
        temp.add(itemInventory);
        combinedInventory = new ItemHandlerList(temp);

    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                NBTTagCompound compound = stack.getTagCompound();
                if (compound == null) return true;
                return !(compound.hasKey(NBT_ITEMSTACK, NBT.TAG_COMPOUND) || compound.hasKey("Fluid", NBT.TAG_COMPOUND)); //prevents inserting items with NBT to the Quantum Chest
            }
        };
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
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
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        if (itemStack.hasKey(NBT_ITEMSTACK, NBT.TAG_COMPOUND)) {
            this.virtualItemStack = new ItemStack(itemStack.getCompoundTag(NBT_ITEMSTACK));
            if (!this.virtualItemStack.isEmpty()) {
                this.itemsStoredInside = itemStack.getLong(NBT_ITEMCOUNT);
            }
        } else if (itemStack.hasKey(NBT_PARTIALSTACK, NBT.TAG_COMPOUND)) {
            exportItems.setStackInSlot(0, new ItemStack(itemStack.getCompoundTag(NBT_PARTIALSTACK)));
        }
    }

    @Override
    public void writeItemStackData(NBTTagCompound itemStack) {
        super.writeItemStackData(itemStack);
        if (!this.virtualItemStack.isEmpty()) {
            itemStack.setTag(NBT_ITEMSTACK, this.virtualItemStack.writeToNBT(new NBTTagCompound()));
            itemStack.setLong(NBT_ITEMCOUNT, itemsStoredInside + this.exportItems.getStackInSlot(0).getCount());
        } else {
            ItemStack partialStack = exportItems.extractItem(0, 64, false);
            if (!partialStack.isEmpty()) {
                itemStack.setTag(NBT_PARTIALSTACK, partialStack.writeToNBT(new NBTTagCompound()));
            }
        }
        this.virtualItemStack = ItemStack.EMPTY;
        this.itemsStoredInside = 0;
        exportItems.setStackInSlot(0, ItemStack.EMPTY);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        Builder builder = ModularUI.defaultBuilder();
        int leftButtonStartX = 7;
        builder.image(7, 16, 81, 55, GuiTextures.DISPLAY);
        builder.widget(new AdvancedTextWidget(11, 20, this::addDisplayInformation, 0xFFFFFF));
        return builder.label(6, 6, getMetaFullName())
                .widget(new SlotWidget(importItems, 0, 90, 17, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
                .widget(new SlotWidget(exportItems, 0, 90, 54, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY)).widget(new ToggleButtonWidget(leftButtonStartX, 53, 18, 18,
                        GuiTextures.BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setAutoOutputItems).shouldUseBaseBackground()
                        .setTooltipText("gregtech.gui.item_auto_output.tooltip"))
                .bindPlayerInventory(entityPlayer.inventory)
                .build(getHolder(), entityPlayer);
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
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
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
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(getOutputFacing().getIndex());
        buf.writeBoolean(autoOutputItems);
        buf.writeItemStack(virtualItemStack);
        buf.writeLong(itemsStoredInside);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.outputFacing = EnumFacing.VALUES[buf.readByte()];
        this.autoOutputItems = buf.readBoolean();
        try {
            this.virtualItemStack = buf.readItemStack();
        } catch (IOException ignored) {
            GTLog.logger.warn("Failed to load item from NBT in a quantum chest at " + this.getPos() + " on initial server/client sync");
        }
        this.itemsStoredInside = buf.readLong();
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        //use direct outputFacing field instead of getter method because otherwise
        //it will just return SOUTH for null output facing
        return super.isValidFrontFacing(facing) && facing != outputFacing;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
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
        }
    }

    public void setAutoOutputItems(boolean autoOutputItems) {
        this.autoOutputItems = autoOutputItems;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_OUTPUT_ITEMS, buf -> buf.writeBoolean(autoOutputItems));
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
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(combinedInventory);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.outputFacing == null) {
            //set initial output facing as opposite to front
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
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        clearInventory(itemBuffer, importItems);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        EnumFacing hitFacing = ICoverable.determineGridSideHit(hitResult);
        if (facing == getOutputFacing() || (hitFacing == getOutputFacing() && playerIn.isSneaking())) {
            if (!getWorld().isRemote) {
                if (isAllowInputFromOutputSideItems()) {
                    setAllowInputFromOutputSide(false);
                    playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.disallow"));
                } else {
                    setAllowInputFromOutputSide(true);
                    playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.allow"));
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
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos());
    }

    private class QuantumChestItemHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
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

        @Nonnull
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

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack insertedStack, boolean simulate) {
            if (insertedStack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            // If there is a virtualized stack and the stack to insert does not match it, do not insert anything
            if (itemsStoredInside > 0L &&
                    !virtualItemStack.isEmpty() &&
                    !areItemStackIdentical(virtualItemStack, insertedStack)) {
                return insertedStack;
            }

            // The Quantum Chest automatically populates the export slot, so we need to check what is contained in it
            ItemStack exportItems = getExportItems().getStackInSlot(0);

            // If the item being inserted does not match the item in the export slot, insert into the input slot and do not virtualize
            if (!areItemStackIdentical(insertedStack, exportItems)) {
                return MetaTileEntityQuantumChest.this.importItems.insertItem(0, insertedStack, simulate);
            }

            int spaceInExport = Math.abs(exportItems.getMaxStackSize() - exportItems.getCount());

            // Attempt to insert into the export slot first
            int amountCanInsertIntoExport = Math.min(spaceInExport, insertedStack.getCount());

            if (insertedStack.getCount() <= amountCanInsertIntoExport) {
                // If all the items can fit into export slot, store it there
                return MetaTileEntityQuantumChest.this.exportItems.insertItem(0, insertedStack, simulate);
            }

            // Have more items than would fit into the export slot, so virtualize the remainder
            long amountLeftInChest = virtualItemStack.isEmpty() ? maxStoredItems : maxStoredItems - itemsStoredInside;

            int maxVirtualAmount = insertedStack.getCount() - amountCanInsertIntoExport;
            int virtualizedAmount = (int) Math.min(maxVirtualAmount, amountLeftInChest);

            ItemStack remainingStack = ItemStack.EMPTY;

            // If we are at the maximum that the chest can hold, the remainder stack has all items that could not fit
            if (virtualizedAmount < maxVirtualAmount) {
                remainingStack = insertedStack.copy();
                remainingStack.setCount(insertedStack.getCount() - virtualizedAmount);
            }

            if (!simulate) {
                if (remainingStack.isEmpty()) {
                    // inserted everything
                    if (virtualItemStack.isEmpty()) {
                        // have no virtual stack, so set it to the inserted stack
                        ItemStack virtualStack = insertedStack.copy();
                        virtualStack.setCount(virtualizedAmount);
                        MetaTileEntityQuantumChest.this.virtualItemStack = virtualStack;
                        MetaTileEntityQuantumChest.this.itemsStoredInside = virtualizedAmount;
                    } else {
                        // update the virtualized total count
                        MetaTileEntityQuantumChest.this.itemsStoredInside += virtualizedAmount;
                    }

                    if (amountCanInsertIntoExport != 0) {
                        // fill the export slot as much as possible
                        ItemStack insertedStackCopy = insertedStack.copy();
                        insertedStackCopy.setCount(amountCanInsertIntoExport);
                        MetaTileEntityQuantumChest.this.exportItems.insertItem(0, insertedStackCopy, simulate);
                    }
                } else {
                    // could not fit everything, but still need to update the virtualized total count
                    MetaTileEntityQuantumChest.this.itemsStoredInside += virtualizedAmount;
                }
            }

            return remainingStack;
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
