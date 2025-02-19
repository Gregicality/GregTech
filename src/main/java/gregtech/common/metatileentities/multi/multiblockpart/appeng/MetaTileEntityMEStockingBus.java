package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_PULL;

public class MetaTileEntityMEStockingBus extends MetaTileEntityMEInputBus {

    private static final int CONFIG_SIZE = 16;
    private boolean autoPull;
    private Predicate<ItemStack> autoPullTest;

    public MetaTileEntityMEStockingBus(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.LuV);
        this.autoPullTest = $ -> false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMEStockingBus(metaTileEntityId);
    }

    @Override
    protected ExportOnlyAEStockingItemList getAEItemHandler() {
        if (this.aeItemHandler == null) {
            this.aeItemHandler = new ExportOnlyAEStockingItemList(this, CONFIG_SIZE, getController());
        }
        return (ExportOnlyAEStockingItemList) this.aeItemHandler;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (isWorkingEnabled() && autoPull && getOffsetTimer() % 100 == 0) {
                refreshList();
                syncME();
            }

            // Immediately clear cached items if the status changed, to prevent running recipes while offline
            if (this.meStatusChanged && !this.isOnline) {
                if (autoPull) {
                    clearInventory(0);
                } else {
                    for (int i = 0; i < CONFIG_SIZE; i++) {
                        getAEItemHandler().getInventory()[i].setStack(null);
                    }
                }
            }
        }
    }

    // Update the visual display for the fake items. This also is important for the item handler's
    // getStackInSlot() method, as it uses the cached items set here.
    @Override
    protected void syncME() {
        IMEMonitor<IAEItemStack> monitor = super.getMonitor();
        if (monitor == null) return;

        for (ExportOnlyAEStockingItemSlot slot : this.getAEItemHandler().getInventory()) {
            if (slot.getConfig() == null) {
                slot.setStack(null);
            } else {
                // Try to fill the slot
                IAEItemStack request;
                if (slot.getConfig() instanceof WrappedItemStack wis) {
                    request = wis.getAEStack();
                } else {
                    request = slot.getConfig().copy();
                }
                request.setStackSize(Integer.MAX_VALUE);
                IAEItemStack result = monitor.extractItems(request, Actionable.SIMULATE, getActionSource());
                slot.setStack(result);
            }
        }
    }

    @Override
    protected void flushInventory() {
        // no-op, nothing to send back to the network
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        // ensure that no other stocking bus on this multiblock is configured to hold the same item.
        // that we have in our own bus.
        this.autoPullTest = stack -> !this.testConfiguredInOtherBus(stack);
        // also ensure that our current config is valid given other inputs
        validateConfig();
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        // block auto-pull from working when not in a formed multiblock
        this.autoPullTest = $ -> false;
        if (this.autoPull) {
            // may as well clear if we are auto-pull, no reason to preserve the config
            this.getAEItemHandler().clearConfig();
        }
        super.removeFromMultiBlock(controllerBase);
    }

    @Override
    public void onDistinctChange(boolean newValue) {
        super.onDistinctChange(newValue);
        if (!getWorld().isRemote && !newValue) {
            // Ensure that our configured items won't match any other buses in the multiblock.
            // Needed since we allow duplicates in distinct mode on, but not off
            validateConfig();
        }
    }

    /**
     * Test for if any of our configured items are in another stocking bus on the multi
     * we are attached to. Prevents dupes in certain situations.
     */
    private void validateConfig() {
        for (var slot : this.getAEItemHandler().getInventory()) {
            if (slot.getConfig() != null) {
                ItemStack configuredStack = slot.getConfig().createItemStack();
                if (testConfiguredInOtherBus(configuredStack)) {
                    slot.setConfig(null);
                    slot.setStock(null);
                }
            }
        }
    }

    /**
     * @return True if the passed stack is found as a configuration in any other stocking buses on the multiblock.
     */
    private boolean testConfiguredInOtherBus(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        MultiblockControllerBase controller = getController();
        if (controller == null) return false;

        // In distinct mode, we don't need to check other buses since only one bus can run a recipe at a time.
        if (!(controller instanceof RecipeMapMultiblockController rmmc) || !rmmc.isDistinct()) {
            // Otherwise, we need to test for if the item is configured
            // in any stocking bus in the multi (besides ourselves).
            var abilityList = controller.getAbilities(MultiblockAbility.IMPORT_ITEMS);
            for (var ability : abilityList) {
                if (ability instanceof ExportOnlyAEStockingItemList aeList) {
                    // We don't need to check for ourselves, as this case is handled elsewhere.
                    if (aeList == this.aeItemHandler) continue;
                    if (aeList.hasStackInConfig(stack, false)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void setAutoPull(boolean autoPull) {
        this.autoPull = autoPull;
        markDirty();
        if (!getWorld().isRemote) {
            if (!this.autoPull) {
                this.getAEItemHandler().clearConfig();
            } else if (updateMEStatus()) {
                this.refreshList();
                syncME();
            }
            writeCustomData(UPDATE_AUTO_PULL, buf -> buf.writeBoolean(this.autoPull));
        }
    }

    /**
     * Refresh the configuration list in auto-pull mode.
     * Sets the config to the first 16 valid items found in the network.
     */
    private void refreshList() {
        IMEMonitor<IAEItemStack> monitor = getMonitor();
        if (monitor == null) {
            clearInventory(0);
            return;
        }

        IItemList<IAEItemStack> storageList = monitor.getStorageList();
        if (storageList == null) {
            clearInventory(0);
            return;
        }

        int index = 0;
        for (IAEItemStack stack : storageList) {
            if (index >= CONFIG_SIZE) break;
            if (stack.getStackSize() == 0) continue;
            stack = monitor.extractItems(stack, Actionable.SIMULATE, getActionSource());
            if (stack == null || stack.getStackSize() == 0) continue;

            ItemStack itemStack = stack.createItemStack();
            if (itemStack == null || itemStack.isEmpty()) continue;
            // Ensure that it is valid to configure with this stack
            if (autoPullTest != null && !autoPullTest.test(itemStack)) continue;
            IAEItemStack selectedStack = WrappedItemStack.fromItemStack(itemStack);
            if (selectedStack == null) continue;
            IAEItemStack configStack = selectedStack.copy().setStackSize(1);
            var slot = this.getAEItemHandler().getInventory()[index];
            slot.setConfig(configStack);
            slot.setStack(selectedStack);
            index++;
        }

        clearInventory(index);
    }

    private void clearInventory(int startIndex) {
        for (int i = startIndex; i < CONFIG_SIZE; i++) {
            var slot = this.getAEItemHandler().getInventory()[i];
            slot.setConfig(null);
            slot.setStack(null);
        }
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_AUTO_PULL) {
            this.autoPull = buf.readBoolean();
        }
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer player) {
        ModularUI.Builder builder = super.createUITemplate(player);
        builder.widget(new ImageCycleButtonWidget(7 + 18 * 4 + 1, 26, 16, 16, GuiTextures.BUTTON_AUTO_PULL,
                () -> autoPull, this::setAutoPull).setTooltipHoverString("gregtech.gui.me_bus.auto_pull_button"));
        return builder;
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            setAutoPull(!autoPull);
            if (autoPull) {
                playerIn.sendStatusMessage(
                        new TextComponentTranslation("gregtech.machine.me.stocking_auto_pull_enabled"), false);
            } else {
                playerIn.sendStatusMessage(
                        new TextComponentTranslation("gregtech.machine.me.stocking_auto_pull_disabled"), false);
            }
        }
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("AutoPull", autoPull);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.autoPull = data.getBoolean("AutoPull");
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(autoPull);
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.autoPull = buf.readBoolean();
    }

    private static class ExportOnlyAEStockingItemSlot extends ExportOnlyAEItemSlot {

        private final MetaTileEntityMEStockingBus holder;

        public ExportOnlyAEStockingItemSlot(IAEItemStack config, IAEItemStack stock,
                                            MetaTileEntityMEStockingBus holder) {
            super(config, stock);
            this.holder = holder;
        }

        public ExportOnlyAEStockingItemSlot(MetaTileEntityMEStockingBus holder) {
            super();
            this.holder = holder;
        }

        @Override
        public ExportOnlyAEStockingItemSlot copy() {
            return new ExportOnlyAEStockingItemSlot(
                    this.config == null ? null : this.config.copy(),
                    this.stock == null ? null : this.stock.copy(),
                    this.holder);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 0 && this.stock != null) {
                if (this.config != null) {
                    // Extract the items from the real net to either validate (simulate)
                    // or extract (modulate) when this is called
                    IMEMonitor<IAEItemStack> monitor = holder.getMonitor();
                    if (monitor == null) return ItemStack.EMPTY;

                    Actionable action = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
                    IAEItemStack request;
                    if (this.config instanceof WrappedItemStack wis) {
                        request = wis.getAEStack();
                    } else {
                        request = this.config.copy();
                    }
                    request.setStackSize(amount);

                    IAEItemStack result = monitor.extractItems(request, action, holder.getActionSource());
                    if (result != null) {
                        int extracted = (int) Math.min(result.getStackSize(), amount);
                        this.stock.decStackSize(extracted); // may as well update the display here
                        if (this.trigger != null) {
                            this.trigger.accept(0);
                        }
                        if (extracted != 0) {
                            ItemStack resultStack = this.config.createItemStack();
                            resultStack.setCount(extracted);
                            return resultStack;
                        }
                    }
                }
            }
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.item_bus.import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.stocking_item.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me_import_item_hatch.configs.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.copy_paste.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.stocking_item.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.me.extra_connections.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    protected NBTTagCompound writeConfigToTag() {
        if (!autoPull) {
            NBTTagCompound tag = super.writeConfigToTag();
            tag.setBoolean("AutoPull", false);
            return tag;
        }
        // if in auto-pull, no need to write actual configured slots, but still need to write the ghost circuit
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("AutoPull", true);
        tag.setByte("GhostCircuit", (byte) this.circuitInventory.getCircuitValue());
        return tag;
    }

    @Override
    protected void readConfigFromTag(NBTTagCompound tag) {
        if (tag.getBoolean("AutoPull")) {
            // if being set to auto-pull, no need to read the configured slots
            this.setAutoPull(true);
            this.setGhostCircuitConfig(tag.getByte("GhostCircuit"));
            return;
        }
        // set auto pull first to avoid issues with clearing the config after reading from the data stick
        this.setAutoPull(false);
        super.readConfigFromTag(tag);
    }

    private static class ExportOnlyAEStockingItemList extends ExportOnlyAEItemList {

        private final MetaTileEntityMEStockingBus holder;

        public ExportOnlyAEStockingItemList(MetaTileEntityMEStockingBus holder, int slots,
                                            MetaTileEntity entityToNotify) {
            super(holder, slots, entityToNotify);
            this.holder = holder;
        }

        @Override
        protected void createInventory(MetaTileEntity holder) {
            if (!(holder instanceof MetaTileEntityMEStockingBus stocking)) {
                throw new IllegalArgumentException("Cannot create Stocking Item List for nonstocking MetaTileEntity!");
            }
            this.inventory = new ExportOnlyAEStockingItemSlot[size];
            for (int i = 0; i < size; i++) {
                this.inventory[i] = new ExportOnlyAEStockingItemSlot(stocking);
            }
            for (ExportOnlyAEItemSlot slot : this.inventory) {
                slot.setTrigger(this::onContentsChanged);
            }
        }

        @Override
        public ExportOnlyAEStockingItemSlot[] getInventory() {
            return (ExportOnlyAEStockingItemSlot[]) super.getInventory();
        }

        @Override
        public boolean isStocking() {
            return true;
        }

        @Override
        public boolean isAutoPull() {
            return holder.autoPull;
        }

        @Override
        public boolean hasStackInConfig(ItemStack stack, boolean checkExternal) {
            boolean inThisBus = super.hasStackInConfig(stack, false);
            if (inThisBus) return true;
            if (checkExternal) {
                return holder.testConfiguredInOtherBus(stack);
            }
            return false;
        }
    }
}
