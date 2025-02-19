package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_PULL;

public class MetaTileEntityMEStockingHatch extends MetaTileEntityMEInputHatch {

    private static final int CONFIG_SIZE = 16;
    private boolean autoPull;
    private Predicate<FluidStack> autoPullTest;

    public MetaTileEntityMEStockingHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.LuV);
        this.autoPullTest = $ -> false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMEStockingHatch(metaTileEntityId);
    }

    @Override
    protected ExportOnlyAEStockingFluidList getAEFluidHandler() {
        if (this.aeFluidHandler == null) {
            this.aeFluidHandler = new ExportOnlyAEStockingFluidList(this, CONFIG_SIZE, getController());
        }
        return (ExportOnlyAEStockingFluidList) this.aeFluidHandler;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (isWorkingEnabled() && autoPull && getOffsetTimer() % 100 == 0) {
                refreshList();
                syncME();
            }

            // Immediately clear cached fluids if the status changed, to prevent running recipes while offline
            if (this.meStatusChanged && !this.isOnline) {
                if (autoPull) {
                    this.getAEFluidHandler().clearConfig();
                } else {
                    for (int i = 0; i < CONFIG_SIZE; i++) {
                        getAEFluidHandler().getInventory()[i].setStack(null);
                    }
                }
            }
        }
    }

    @Override
    protected void syncME() {
        IMEMonitor<IAEFluidStack> monitor = super.getMonitor();
        if (monitor == null) return;

        for (ExportOnlyAEStockingFluidSlot slot : this.getAEFluidHandler().getInventory()) {
            if (slot.getConfig() == null) {
                slot.setStack(null);
            } else {
                // Try to fill the slot
                IAEFluidStack request;
                if (slot.getConfig() instanceof WrappedFluidStack wfs) {
                    request = wfs.getAEStack();
                } else {
                    request = slot.getConfig().copy();
                }
                request.setStackSize(Integer.MAX_VALUE);
                IAEFluidStack result = monitor.extractItems(request, Actionable.SIMULATE, getActionSource());
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
        this.autoPullTest = stack -> !this.testConfiguredInOtherHatch(stack);
        validateConfig();
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        this.autoPullTest = $ -> false;
        if (this.autoPull) {
            this.getAEFluidHandler().clearConfig();
        }
        super.removeFromMultiBlock(controllerBase);
    }

    private void validateConfig() {
        for (var slot : this.getAEFluidHandler().getInventory()) {
            if (slot.getConfig() != null) {
                FluidStack configuredStack = slot.getConfig().getFluidStack();
                if (testConfiguredInOtherHatch(configuredStack)) {
                    slot.setConfig(null);
                    slot.setStock(null);
                }
            }
        }
    }

    private boolean testConfiguredInOtherHatch(FluidStack stack) {
        if (stack == null) return false;
        MultiblockControllerBase controller = getController();
        if (controller == null) return false;

        var abilityList = controller.getAbilities(MultiblockAbility.IMPORT_FLUIDS);
        for (var ability : abilityList) {
            if (ability instanceof ExportOnlyAEStockingFluidSlot aeSlot) {
                if (aeSlot.getConfig() == null) continue;
                if (getAEFluidHandler().ownsSlot(aeSlot)) continue;
                if (aeSlot.getConfig().getFluid().equals(stack.getFluid())) {
                    return true;
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
                this.getAEFluidHandler().clearConfig();
            } else if (updateMEStatus()) {
                this.refreshList();
                syncME();
            }
            writeCustomData(UPDATE_AUTO_PULL, buf -> buf.writeBoolean(this.autoPull));
        }
    }

    private void refreshList() {
        IMEMonitor<IAEFluidStack> monitor = getMonitor();
        if (monitor == null) {
            clearInventory(0);
            return;
        }

        IItemList<IAEFluidStack> storageList = monitor.getStorageList();
        if (storageList == null) {
            clearInventory(0);
            return;
        }

        int index = 0;
        for (IAEFluidStack stack : storageList) {
            if (index >= CONFIG_SIZE) break;
            if (stack.getStackSize() == 0) continue;
            stack = monitor.extractItems(stack, Actionable.SIMULATE, getActionSource());
            if (stack == null || stack.getStackSize() == 0) continue;

            FluidStack fluidStack = stack.getFluidStack();
            if (fluidStack == null) continue;
            if (autoPullTest != null && !autoPullTest.test(fluidStack)) continue;
            IAEFluidStack selectedStack = WrappedFluidStack.fromFluidStack(fluidStack);
            IAEFluidStack configStack = selectedStack.copy().setStackSize(1);
            var slot = this.getAEFluidHandler().getInventory()[index];
            slot.setConfig(configStack);
            slot.setStack(selectedStack);
            index++;
        }

        clearInventory(index);
    }

    private void clearInventory(int startIndex) {
        for (int i = startIndex; i < CONFIG_SIZE; i++) {
            var slot = this.getAEFluidHandler().getInventory()[i];
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

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.fluid_hatch.import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.stocking_fluid.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me_import_fluid_hatch.configs.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.copy_paste.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.stocking_fluid.tooltip.2"));
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
        return tag;
    }

    @Override
    protected void readConfigFromTag(NBTTagCompound tag) {
        if (tag.getBoolean("AutoPull")) {
            // if being set to auto-pull, no need to read the configured slots
            this.setAutoPull(true);
            return;
        }
        // set auto pull first to avoid issues with clearing the config after reading from the data stick
        this.setAutoPull(false);
        super.readConfigFromTag(tag);
    }

    private static class ExportOnlyAEStockingFluidSlot extends ExportOnlyAEFluidSlot {

        public ExportOnlyAEStockingFluidSlot(MetaTileEntityMEStockingHatch holder, IAEFluidStack config,
                                             IAEFluidStack stock, MetaTileEntity entityToNotify) {
            super(holder, config, stock, entityToNotify);
        }

        public ExportOnlyAEStockingFluidSlot(MetaTileEntityMEStockingHatch holder, MetaTileEntity entityToNotify) {
            super(holder, entityToNotify);
        }

        @Override
        protected MetaTileEntityMEStockingHatch getHolder() {
            return (MetaTileEntityMEStockingHatch) super.getHolder();
        }

        @Override
        public ExportOnlyAEFluidSlot copy() {
            return new ExportOnlyAEStockingFluidSlot(
                    this.getHolder(),
                    this.config == null ? null : this.config.copy(),
                    this.stock == null ? null : this.stock.copy(),
                    null);
        }

        @Override
        public @Nullable FluidStack drain(int maxDrain, boolean doDrain) {
            if (this.stock == null) {
                return null;
            }
            if (this.config != null) {
                IMEMonitor<IAEFluidStack> monitor = getHolder().getMonitor();
                if (monitor == null) return null;

                Actionable action = doDrain ? Actionable.MODULATE : Actionable.SIMULATE;
                IAEFluidStack request;
                if (this.config instanceof WrappedFluidStack wfs) {
                    request = wfs.getAEStack();
                } else {
                    request = this.config.copy();
                }
                request.setStackSize(maxDrain);

                IAEFluidStack result = monitor.extractItems(request, action, getHolder().getActionSource());
                if (result != null) {
                    int extracted = (int) Math.min(result.getStackSize(), maxDrain);
                    this.stock.decStackSize(extracted);
                    trigger();
                    if (extracted != 0) {
                        FluidStack resultStack = this.config.getFluidStack();
                        resultStack.amount = extracted;
                        return resultStack;
                    }
                }
            }
            return null;
        }
    }

    private static class ExportOnlyAEStockingFluidList extends ExportOnlyAEFluidList {

        private final MetaTileEntityMEStockingHatch holder;

        public ExportOnlyAEStockingFluidList(MetaTileEntityMEStockingHatch holder, int slots,
                                             MetaTileEntity entityToNotify) {
            super(holder, slots, entityToNotify);
            this.holder = holder;
        }

        @Override
        protected void createInventory(MetaTileEntity holder, MetaTileEntity entityToNotify) {
            if (!(holder instanceof MetaTileEntityMEStockingHatch stocking)) {
                throw new IllegalArgumentException("Cannot create Stocking Fluid List for nonstocking MetaTileEntity!");
            }
            this.inventory = new ExportOnlyAEStockingFluidSlot[size];
            for (int i = 0; i < size; i++) {
                this.inventory[i] = new ExportOnlyAEStockingFluidSlot(stocking, entityToNotify);
            }
        }

        @Override
        public ExportOnlyAEStockingFluidSlot[] getInventory() {
            return (ExportOnlyAEStockingFluidSlot[]) super.getInventory();
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
        public boolean hasStackInConfig(FluidStack stack, boolean checkExternal) {
            boolean inThisHatch = super.hasStackInConfig(stack, false);
            if (inThisHatch) return true;
            if (checkExternal) {
                return holder.testConfiguredInOtherHatch(stack);
            }
            return false;
        }
    }
}
