package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDataStickIntractable;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.GhostCircuitSlotWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.gui.widget.appeng.AEItemConfigWidget;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class MetaTileEntityMEInputBus extends MetaTileEntityAEHostablePart<IAEItemStack>
                                      implements IMultiblockAbilityPart<IItemHandlerModifiable>,
                                      IGhostSlotConfigurable, IDataStickIntractable {

    public final static String ITEM_BUFFER_TAG = "ItemSlots";
    public final static String WORKING_TAG = "WorkingEnabled";
    private final static int CONFIG_SIZE = 16;
    private boolean workingEnabled = true;
    protected ExportOnlyAEItemList aeItemHandler;
    protected GhostCircuitItemStackHandler circuitInventory;
    protected NotifiableItemStackHandler extraSlotInventory;
    private ItemHandlerList actualImportItems;

    public MetaTileEntityMEInputBus(ResourceLocation metaTileEntityId) {
        this(metaTileEntityId, GTValues.EV);
    }

    protected MetaTileEntityMEInputBus(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier, false, IItemStorageChannel.class);
    }

    protected ExportOnlyAEItemList getAEItemHandler() {
        if (aeItemHandler == null) {
            aeItemHandler = new ExportOnlyAEItemList(this, CONFIG_SIZE, this.getController());
        }
        return aeItemHandler;
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.aeItemHandler = getAEItemHandler();
        this.circuitInventory = new GhostCircuitItemStackHandler(this);
        this.circuitInventory.addNotifiableMetaTileEntity(this);
        this.extraSlotInventory = new NotifiableItemStackHandler(this, 1, this, false);
        this.extraSlotInventory.addNotifiableMetaTileEntity(this);
        this.actualImportItems = new ItemHandlerList(
                Arrays.asList(this.aeItemHandler, this.circuitInventory, this.extraSlotInventory));
        this.importItems = this.actualImportItems;
    }

    public IItemHandlerModifiable getImportItems() {
        return this.actualImportItems;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && this.workingEnabled && updateMEStatus() && shouldSyncME()) {
            syncME();
        }
    }

    protected void syncME() {
        IMEMonitor<IAEItemStack> monitor = getMonitor();
        if (monitor == null) return;

        for (ExportOnlyAEItemSlot aeSlot : this.getAEItemHandler().getInventory()) {
            // Try to clear the wrong item
            IAEItemStack exceedItem = aeSlot.exceedStack();
            if (exceedItem != null) {
                long total = exceedItem.getStackSize();
                IAEItemStack notInserted = monitor.injectItems(exceedItem, Actionable.MODULATE, this.getActionSource());
                if (notInserted != null && notInserted.getStackSize() > 0) {
                    aeSlot.extractItem(0, (int) (total - notInserted.getStackSize()), false);
                    continue;
                } else {
                    aeSlot.extractItem(0, (int) total, false);
                }
            }
            // Fill it
            IAEItemStack reqItem = aeSlot.requestStack();
            if (reqItem != null) {
                IAEItemStack extracted = monitor.extractItems(reqItem, Actionable.MODULATE, this.getActionSource());
                if (extracted != null) {
                    aeSlot.addStack(extracted);
                }
            }
        }
    }

    @Override
    public void onRemoval() {
        flushInventory();
        super.onRemoval();
    }

    protected void flushInventory() {
        IMEMonitor<IAEItemStack> monitor = getMonitor();
        if (monitor == null) return;

        for (ExportOnlyAEItemSlot aeSlot : this.getAEItemHandler().getInventory()) {
            IAEItemStack stock = aeSlot.getStock();
            if (stock instanceof WrappedItemStack) {
                stock = ((WrappedItemStack) stock).getAEStack();
            }
            if (stock != null) {
                monitor.injectItems(stock, Actionable.MODULATE, this.getActionSource());
            }
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMEInputBus(metaTileEntityId);
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        for (IItemHandler handler : this.actualImportItems.getBackingHandlers()) {
            if (handler instanceof INotifiableHandler notifiable) {
                notifiable.addNotifiableMetaTileEntity(controllerBase);
                notifiable.addToNotifiedList(this, handler, false);
            }
        }
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        for (IItemHandler handler : this.actualImportItems.getBackingHandlers()) {
            if (handler instanceof INotifiableHandler notifiable) {
                notifiable.removeNotifiableMetaTileEntity(controllerBase);
            }
        }
    }

    @Override
    protected final ModularUI createUI(EntityPlayer player) {
        ModularUI.Builder builder = createUITemplate(player);
        return builder.build(this.getHolder(), player);
    }

    protected ModularUI.Builder createUITemplate(EntityPlayer player) {
        ModularUI.Builder builder = ModularUI
                .builder(GuiTextures.BACKGROUND, 176, 18 + 18 * 4 + 94)
                .label(10, 5, getMetaFullName());
        // ME Network status
        builder.dynamicLabel(10, 15, () -> this.isOnline ?
                I18n.format("gregtech.gui.me_network.online") :
                I18n.format("gregtech.gui.me_network.offline"),
                0x404040);

        // Config slots
        builder.widget(new AEItemConfigWidget(7, 25, this.getAEItemHandler()));

        // Ghost circuit slot
        SlotWidget circuitSlot = new GhostCircuitSlotWidget(circuitInventory, 0, 7 + 18 * 4, 25 + 18 * 3)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY);
        builder.widget(circuitSlot.setConsumer(w -> {
            String configString;
            if (circuitInventory == null ||
                    circuitInventory.getCircuitValue() == GhostCircuitItemStackHandler.NO_CONFIG) {
                configString = new TextComponentTranslation("gregtech.gui.configurator_slot.no_value")
                        .getFormattedText();
            } else {
                configString = String.valueOf(circuitInventory.getCircuitValue());
            }

            w.setTooltipText("gregtech.gui.configurator_slot.tooltip", configString);
        }));

        // Extra slot
        builder.widget(new SlotWidget(extraSlotInventory, 0, 7 + 18 * 4, 25 + 18 * 2)
                .setBackgroundTexture(GuiTextures.SLOT)
                .setTooltipText("gregtech.gui.me_bus.extra_slot"));

        // Arrow image
        builder.image(7 + 18 * 4, 25 + 18, 18, 18, GuiTextures.ARROW_DOUBLE);

        builder.bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 18 + 18 * 4 + 12);
        return builder;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = this.getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean(WORKING_TAG, this.workingEnabled);
        NBTTagList slots = new NBTTagList();
        for (int i = 0; i < CONFIG_SIZE; i++) {
            ExportOnlyAEItemSlot slot = this.getAEItemHandler().getInventory()[i];
            NBTTagCompound slotTag = new NBTTagCompound();
            slotTag.setInteger("slot", i);
            slotTag.setTag("stack", slot.serializeNBT());
            slots.appendTag(slotTag);
        }
        data.setTag(ITEM_BUFFER_TAG, slots);
        this.circuitInventory.write(data);
        // Extra slot inventory
        GTUtility.writeItems(this.extraSlotInventory, "ExtraInventory", data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey(WORKING_TAG)) {
            this.workingEnabled = data.getBoolean(WORKING_TAG);
        }
        if (data.hasKey(ITEM_BUFFER_TAG, 9)) {
            NBTTagList slots = (NBTTagList) data.getTag(ITEM_BUFFER_TAG);
            for (NBTBase nbtBase : slots) {
                NBTTagCompound slotTag = (NBTTagCompound) nbtBase;
                ExportOnlyAEItemSlot slot = this.getAEItemHandler().getInventory()[slotTag.getInteger("slot")];
                slot.deserializeNBT(slotTag.getCompoundTag("stack"));
            }
        }
        this.circuitInventory.read(data);
        GTUtility.readItems(this.extraSlotInventory, "ExtraInventory", data);
        this.importItems = createImportItemHandler();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            if (isOnline) {
                Textures.ME_INPUT_BUS_ACTIVE.renderSided(getFrontFacing(), renderState, translation, pipeline);
            } else {
                Textures.ME_INPUT_BUS.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.item_bus.import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.item_import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me_import_item_hatch.configs.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.copy_paste.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.extra_connections.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return MultiblockAbility.IMPORT_ITEMS;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this.actualImportItems);
    }

    @Override
    public boolean hasGhostCircuitInventory() {
        return true;
    }

    @Override
    public void setGhostCircuitConfig(int config) {
        if (this.circuitInventory.getCircuitValue() == config) {
            return;
        }
        this.circuitInventory.setCircuitValue(config);
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    @Override
    public final void onDataStickLeftClick(EntityPlayer player, ItemStack dataStick) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("MEInputBus", writeConfigToTag());
        dataStick.setTagCompound(tag);
        dataStick.setTranslatableName("gregtech.machine.me.item_import.data_stick.name");
        player.sendStatusMessage(new TextComponentTranslation("gregtech.machine.me.import_copy_settings"), true);
    }

    protected NBTTagCompound writeConfigToTag() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound configStacks = new NBTTagCompound();
        tag.setTag("ConfigStacks", configStacks);
        for (int i = 0; i < CONFIG_SIZE; i++) {
            var slot = this.aeItemHandler.getInventory()[i];
            IAEItemStack config = slot.getConfig();
            if (config == null) {
                continue;
            }
            NBTTagCompound stackNbt = new NBTTagCompound();
            config.getDefinition().writeToNBT(stackNbt);
            configStacks.setTag(Integer.toString(i), stackNbt);
        }
        tag.setByte("GhostCircuit", (byte) this.circuitInventory.getCircuitValue());
        return tag;
    }

    @Override
    public final boolean onDataStickRightClick(EntityPlayer player, ItemStack dataStick) {
        NBTTagCompound tag = dataStick.getTagCompound();
        if (tag == null || !tag.hasKey("MEInputBus")) {
            return false;
        }
        readConfigFromTag(tag.getCompoundTag("MEInputBus"));
        syncME();
        player.sendStatusMessage(new TextComponentTranslation("gregtech.machine.me.import_paste_settings"), true);
        return true;
    }

    protected void readConfigFromTag(NBTTagCompound tag) {
        if (tag.hasKey("ConfigStacks")) {
            NBTTagCompound configStacks = tag.getCompoundTag("ConfigStacks");
            for (int i = 0; i < CONFIG_SIZE; i++) {
                String key = Integer.toString(i);
                if (configStacks.hasKey(key)) {
                    NBTTagCompound configTag = configStacks.getCompoundTag(key);
                    this.aeItemHandler.getInventory()[i].setConfig(WrappedItemStack.fromNBT(configTag));
                } else {
                    this.aeItemHandler.getInventory()[i].setConfig(null);
                }
            }
        }
        if (tag.hasKey("GhostCircuit")) {
            this.setGhostCircuitConfig(tag.getByte("GhostCircuit"));
        }
    }
}
