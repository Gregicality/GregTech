package gregtech.common.covers;

import gregtech.api.capability.*;
import gregtech.api.capability.impl.*;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.RenderUtil;
import gregtech.common.gui.widget.prospector.widget.WidgetOreList;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityPowerSubstation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class CoverDigitalInterface extends CoverBase implements IFastRenderMetaTileEntity, ITickable, CoverWithUI {

    public CoverDigitalInterface(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                 @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    public enum MODE {

        FLUID,
        ITEM,
        ENERGY,
        MACHINE,
        PROXY;

        public static MODE[] VALUES;

        static {
            VALUES = MODE.values();
        }
    }

    // run-time data
    private FluidTankProperties[] fluids = new FluidTankProperties[0];
    private ItemStack[] items = new ItemStack[0];
    private int maxItemCapability = 0;
    private long energyStored = 0;
    private long energyCapability = 0;
    private long energyInputPerDur = 0;
    private long energyOutputPerDur = 0;
    private final List<Long> inputEnergyList = new LinkedList<>();
    private final List<Long> outputEnergyList = new LinkedList<>();
    private int progress = 0;
    private int maxProgress = 0;
    private boolean isActive = true;
    private boolean isWorkingEnabled = false;
    private long lastClickTime;
    private UUID lastClickUUID;
    // persistent data
    protected int slot = 0;
    protected MODE mode = MODE.PROXY;
    protected EnumFacing spin = EnumFacing.NORTH;
    protected final int[] proxyMode = new int[] { 0, 0, 0, 0 }; // server-only

    public MODE getMode() {
        return mode;
    }

    public boolean isProxy() {
        return mode == MODE.PROXY;
    }

    public void setMode(MODE mode, int slot, EnumFacing spin) {
        if (this.mode == mode && (this.slot == slot || slot < 0) && this.spin == spin) return;
        if (!isRemote()) {
            if (this.mode != MODE.PROXY && mode == MODE.PROXY) {
                proxyMode[0] = 0;
                proxyMode[1] = 0;
                proxyMode[2] = 0;
                proxyMode[3] = 0;
            }
            this.mode = mode;
            this.slot = slot;
            this.spin = spin;
            writeCustomData(GregtechDataCodes.UPDATE_COVER_MODE, packetBuffer -> {
                packetBuffer.writeByte(mode.ordinal());
                packetBuffer.writeInt(slot);
                packetBuffer.writeByte(spin.getIndex());
            });
            notifyBlockUpdate();
            markDirty();
        } else {
            if (this.mode != mode || this.spin != spin) {
                scheduleRenderUpdate();
            }
            this.mode = mode;
            this.slot = slot;
            this.spin = spin;
        }
    }

    public void setMode(EnumFacing spin) {
        this.setMode(this.mode, this.slot, spin);
    }

    public void setMode(int slot) {
        this.setMode(this.mode, slot, this.spin);
    }

    public void setMode(MODE mode) {
        this.setMode(mode, this.slot, this.spin);
    }

    public boolean subProxyMode(MODE mode) {
        if (this.mode == MODE.PROXY) {
            proxyMode[mode.ordinal()]++;
            this.markAsDirty();
            return true;
        }
        return false;
    }

    public boolean unSubProxyMode(MODE mode) {
        if (this.mode == MODE.PROXY) {
            if (proxyMode[mode.ordinal()] > 0) {
                proxyMode[mode.ordinal()]--;
                this.markAsDirty();
                return true;
            }
        }
        return false;
    }

    public @Nullable TileEntity getCoveredTE() {
        if (this.getCoverableView() instanceof MetaTileEntity metaTileEntity) {
            return (TileEntity) metaTileEntity.getHolder();
        }
        return null;
    }

    public EnumFacing getCoveredFacing() {
        return this.getAttachedSide();
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setByte("cdiMode", (byte) this.mode.ordinal());
        tagCompound.setByte("cdiSpin", (byte) this.spin.ordinal());
        tagCompound.setInteger("cdiSlot", this.slot);
        tagCompound.setInteger("cdi0", this.proxyMode[0]);
        tagCompound.setInteger("cdi1", this.proxyMode[1]);
        tagCompound.setInteger("cdi2", this.proxyMode[2]);
        tagCompound.setInteger("cdi3", this.proxyMode[3]);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.mode = tagCompound.hasKey("cdiMode") ? MODE.VALUES[tagCompound.getByte("cdiMode")] : MODE.PROXY;
        this.spin = tagCompound.hasKey("cdiSpin") ? EnumFacing.byIndex(tagCompound.getByte("cdiSpin")) :
                EnumFacing.NORTH;
        this.slot = tagCompound.hasKey("cdiSlot") ? tagCompound.getInteger("cdiSlot") : 0;
        this.proxyMode[0] = tagCompound.hasKey("cdi0") ? tagCompound.getInteger("cdi0") : 0;
        this.proxyMode[1] = tagCompound.hasKey("cdi1") ? tagCompound.getInteger("cdi1") : 0;
        this.proxyMode[2] = tagCompound.hasKey("cdi2") ? tagCompound.getInteger("cdi2") : 0;
        this.proxyMode[3] = tagCompound.hasKey("cdi3") ? tagCompound.getInteger("cdi3") : 0;
    }

    @Override
    public void onAttachment(@NotNull CoverableView coverableView, @NotNull EnumFacing side,
                             @Nullable EntityPlayer player, @NotNull ItemStack itemStack) {
        if (getFluidCapability() != null) {
            fluids = new FluidTankProperties[getFluidCapability().getTankProperties().length];
            this.mode = MODE.FLUID;
        } else if (getItemCapability() != null) {
            items = new ItemStack[getItemCapability().getSlots()];
            this.mode = MODE.ITEM;
        } else if (getEnergyCapability() != null || getCoverableView() instanceof MetaTileEntityPowerSubstation) {
            this.mode = MODE.ENERGY;
        } else if (getMachineCapability() != null) {
            this.mode = MODE.MACHINE;
        }
        if (player != null) {
            this.spin = player.getHorizontalFacing();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeEnumValue(mode);
        packetBuffer.writeEnumValue(spin);
        packetBuffer.writeInt(slot);
        syncAllInfo();
        writeAllFluids(packetBuffer);
        writeAllItems(packetBuffer);
        packetBuffer.writeInt(maxItemCapability);
        packetBuffer.writeLong(energyStored);
        packetBuffer.writeLong(energyCapability);
        packetBuffer.writeInt(inputEnergyList.size());
        for (Long aLong : inputEnergyList) {
            packetBuffer.writeLong(aLong);
        }
        for (Long aLong : outputEnergyList) {
            packetBuffer.writeLong(aLong);
        }
        packetBuffer.writeInt(progress);
        packetBuffer.writeInt(maxProgress);
        packetBuffer.writeBoolean(isActive);
        packetBuffer.writeBoolean(isWorkingEnabled);
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.mode = packetBuffer.readEnumValue(MODE.class);
        this.spin = packetBuffer.readEnumValue(EnumFacing.class);
        this.slot = packetBuffer.readInt();
        readFluids(packetBuffer);
        readItems(packetBuffer);
        maxItemCapability = packetBuffer.readInt();
        energyStored = packetBuffer.readLong();
        energyCapability = packetBuffer.readLong();
        int size = packetBuffer.readInt();
        inputEnergyList.clear();
        outputEnergyList.clear();
        for (int i = 0; i < size; i++) {
            inputEnergyList.add(packetBuffer.readLong());
        }
        for (int i = 0; i < size; i++) {
            outputEnergyList.add(packetBuffer.readLong());
        }
        progress = packetBuffer.readInt();
        maxProgress = packetBuffer.readInt();
        isActive = packetBuffer.readBoolean();
        isWorkingEnabled = packetBuffer.readBoolean();
    }

    @Override
    public void update() {
        if (!isRemote() && getOffsetTimer() % 2 == 0) {
            syncAllInfo();
        }
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (!this.getWorld().isRemote) {
            this.openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public @NotNull EnumActionResult onRightClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                  @NotNull CuboidRayTraceResult rayTraceResult) {
        if (!isRemote()) {
            if (this.getWorld().getTotalWorldTime() - lastClickTime < 2 &&
                    playerIn.getPersistentID().equals(lastClickUUID)) {
                return EnumActionResult.SUCCESS;
            }
            lastClickTime = this.getWorld().getTotalWorldTime();
            lastClickUUID = playerIn.getPersistentID();
            if (playerIn.isSneaking() && playerIn.getHeldItemMainhand().isEmpty()) {
                if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                    int maxSlotLimit = Integer.MAX_VALUE;
                    if (this.getCoverableView() instanceof MetaTileEntity metaTileEntity) {
                        maxSlotLimit = this.mode == MODE.ITEM ? metaTileEntity.getImportItems().getSlots() :
                                metaTileEntity.getImportFluids().size();
                    }
                    double x = 0;
                    double y = 1 - rayTraceResult.hitVec.y + rayTraceResult.getBlockPos().getY();
                    if (rayTraceResult.sideHit == EnumFacing.EAST) {
                        x = 1 - rayTraceResult.hitVec.z + rayTraceResult.getBlockPos().getZ();
                    } else if (rayTraceResult.sideHit == EnumFacing.SOUTH) {
                        x = rayTraceResult.hitVec.x - rayTraceResult.getBlockPos().getX();
                    } else if (rayTraceResult.sideHit == EnumFacing.WEST) {
                        x = rayTraceResult.hitVec.z - rayTraceResult.getBlockPos().getZ();
                    } else if (rayTraceResult.sideHit == EnumFacing.NORTH) {
                        x = 1 - rayTraceResult.hitVec.x + rayTraceResult.getBlockPos().getX();
                    }
                    if (1f / 16 < x && x < 4f / 16 && 1f / 16 < y && y < 4f / 16) {
                        this.setMode(this.slot - 1 >= 0 ? this.slot - 1 : maxSlotLimit);
                        return EnumActionResult.SUCCESS;
                    } else if (12f / 16 < x && x < 15f / 16 && 1f / 16 < y && y < 4f / 16) {
                        this.setMode(this.slot + 1 >= maxSlotLimit ? 0 : this.slot + 1);
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
            return modeRightClick(playerIn, hand, this.mode, this.slot);
        }
        return EnumActionResult.PASS;
    }

    @Override
    public boolean onLeftClick(@NotNull EntityPlayer entityPlayer, @NotNull CuboidRayTraceResult hitResult) {
        if (!isRemote()) {
            if (this.getWorld().getTotalWorldTime() - lastClickTime < 2 &&
                    entityPlayer.getPersistentID().equals(lastClickUUID)) {
                return true;
            }
            lastClickTime = this.getWorld().getTotalWorldTime();
            lastClickUUID = entityPlayer.getPersistentID();
            return modeLeftClick(entityPlayer, this.mode, this.slot);
        }
        return false;
    }

    public EnumActionResult modeRightClick(EntityPlayer playerIn, EnumHand hand, MODE mode, int slot) {
        IFluidHandler fluidHandler = this.getFluidCapability();
        if (mode == MODE.FLUID && fluidHandler != null) {
            if (!FluidUtil.interactWithFluidHandler(playerIn, hand, fluidHandler)) {
                if (fluidHandler instanceof FluidHandlerProxy &&
                        FluidUtil.interactWithFluidHandler(playerIn, hand, ((FluidHandlerProxy) fluidHandler).input)) {
                    return EnumActionResult.SUCCESS;
                }
                return EnumActionResult.PASS;
            }
            return EnumActionResult.SUCCESS;
        }
        IItemHandler itemHandler = this.getItemCapability();
        if (mode == MODE.ITEM && itemHandler != null) {
            if (itemHandler.getSlots() > slot && slot >= 0) {
                ItemStack hold = playerIn.getHeldItemMainhand();
                if (!hold.isEmpty()) {
                    ItemStack origin = hold.copy();
                    boolean flag = false;
                    if (playerIn.isSneaking()) {
                        int size = playerIn.getHeldItemMainhand().getCount();
                        playerIn.setHeldItem(EnumHand.MAIN_HAND, itemHandler.insertItem(slot, hold, false));
                        flag = playerIn.getHeldItemMainhand().getCount() != size;
                    } else {
                        ItemStack itemStack = hold.copy();
                        itemStack.setCount(1);
                        if (itemHandler.insertItem(slot, itemStack, false).isEmpty()) {
                            hold.setCount(hold.getCount() - 1);
                            flag = true;
                        }
                    }
                    if (playerIn.getHeldItemMainhand().isEmpty()) {
                        for (ItemStack itemStack : playerIn.inventory.mainInventory) {
                            if (origin.isItemEqual(itemStack)) {
                                playerIn.setHeldItem(EnumHand.MAIN_HAND, itemStack.copy());
                                itemStack.setCount(0);
                                break;
                            }
                        }
                    }
                    return flag ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
                }
            }
            return EnumActionResult.PASS;
        }
        IWorkable workable = this.getMachineCapability();
        if (mode == MODE.MACHINE && workable != null) {
            if (playerIn.isSneaking()) {
                workable.setWorkingEnabled(!isWorkingEnabled);
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    public boolean modeLeftClick(EntityPlayer entityPlayer, MODE mode, int slot) {
        IItemHandler itemHandler = this.getItemCapability();
        if (mode == MODE.ITEM && itemHandler != null) {
            if (itemHandler.getSlots() > slot && slot >= 0) {
                ItemStack itemStack;
                if (entityPlayer.isSneaking()) {
                    itemStack = itemHandler.extractItem(slot, 64, false);
                } else {
                    itemStack = itemHandler.extractItem(slot, 1, false);
                }
                if (itemStack.isEmpty() && itemHandler instanceof ItemHandlerProxy) {
                    IItemHandler insertHandler = ObfuscationReflectionHelper.getPrivateValue(ItemHandlerProxy.class,
                            (ItemHandlerProxy) itemHandler, "insertHandler");
                    if (slot < insertHandler.getSlots()) {
                        if (entityPlayer.isSneaking()) {
                            itemStack = insertHandler.extractItem(slot, 64, false);
                        } else {
                            itemStack = insertHandler.extractItem(slot, 1, false);
                        }
                    }
                }
                if (!itemStack.isEmpty()) {
                    EntityItem entity = new EntityItem(entityPlayer.world, entityPlayer.posX + .5f,
                            entityPlayer.posY + .3f, entityPlayer.posZ + .5f, itemStack);
                    entity.addVelocity(-entity.motionX, -entity.motionY, -entity.motionZ);
                    entityPlayer.world.spawnEntity(entity);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup primaryGroup = new WidgetGroup(new Position(0, 10));
        primaryGroup.addWidget(new LabelWidget(10, 5, "metaitem.cover.digital.name", 0));
        ToggleButtonWidget[] buttons = new ToggleButtonWidget[5];
        buttons[0] = new ToggleButtonWidget(40, 20, 20, 20, GuiTextures.BUTTON_FLUID, () -> this.mode == MODE.FLUID,
                (pressed) -> {
                    if (pressed) setMode(MODE.FLUID);
                }).setTooltipText("metaitem.cover.digital.mode.fluid");
        buttons[1] = new ToggleButtonWidget(60, 20, 20, 20, GuiTextures.BUTTON_ITEM, () -> this.mode == MODE.ITEM,
                (pressed) -> {
                    if (pressed) setMode(MODE.ITEM);
                }).setTooltipText("metaitem.cover.digital.mode.item");
        buttons[2] = new ToggleButtonWidget(80, 20, 20, 20, GuiTextures.BUTTON_ENERGY, () -> this.mode == MODE.ENERGY,
                (pressed) -> {
                    if (pressed) setMode(MODE.ENERGY);
                }).setTooltipText("metaitem.cover.digital.mode.energy");
        buttons[3] = new ToggleButtonWidget(100, 20, 20, 20, GuiTextures.BUTTON_MACHINE,
                () -> this.mode == MODE.MACHINE, (pressed) -> {
                    if (pressed) setMode(MODE.MACHINE);
                }).setTooltipText("metaitem.cover.digital.mode.machine");
        buttons[4] = new ToggleButtonWidget(140, 20, 20, 20, GuiTextures.BUTTON_INTERFACE,
                () -> this.mode == MODE.PROXY, (pressed) -> {
                    if (pressed) setMode(MODE.PROXY);
                }).setTooltipText("metaitem.cover.digital.mode.proxy");
        primaryGroup.addWidget(new LabelWidget(10, 25, "metaitem.cover.digital.title.mode", 0));
        primaryGroup.addWidget(buttons[0]);
        primaryGroup.addWidget(buttons[1]);
        primaryGroup.addWidget(buttons[2]);
        primaryGroup.addWidget(buttons[3]);
        primaryGroup.addWidget(buttons[4]);

        primaryGroup.addWidget(new LabelWidget(10, 50, "monitor.gui.title.slot", 0));
        primaryGroup.addWidget(
                new ClickButtonWidget(40, 45, 20, 20, "-1", (data) -> setMode(slot - (data.isShiftClick ? 10 : 1))));
        primaryGroup.addWidget(
                new ClickButtonWidget(140, 45, 20, 20, "+1", (data) -> setMode(slot + (data.isShiftClick ? 10 : 1))));
        primaryGroup.addWidget(new ImageWidget(60, 45, 80, 20, GuiTextures.DISPLAY));
        primaryGroup.addWidget(new SimpleTextWidget(100, 55, "", 16777215, () -> Integer.toString(this.slot)));

        primaryGroup.addWidget(new LabelWidget(10, 75, "metaitem.cover.digital.title.spin", 0));
        primaryGroup.addWidget(new ClickButtonWidget(40, 70, 20, 20, "R", (data) -> setMode(this.spin.rotateY())));
        primaryGroup.addWidget(new ImageWidget(60, 70, 80, 20, GuiTextures.DISPLAY));
        primaryGroup.addWidget(new SimpleTextWidget(100, 80, "", 16777215, () -> this.spin.toString()));
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 202).widget(primaryGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 8, 120);
        return builder.build(this, player);
    }

    private void syncAllInfo() {
        if (mode == MODE.FLUID || (mode == MODE.PROXY && proxyMode[0] > 0)) {
            boolean syncFlag = false;
            IFluidHandler fluidHandler = this.getFluidCapability();
            if (fluidHandler != null) {
                IFluidTankProperties[] fluidTankProperties = fluidHandler.getTankProperties();
                if (fluidTankProperties.length != fluids.length) {
                    fluids = new FluidTankProperties[fluidTankProperties.length];
                    syncFlag = true;
                }
                List<Integer> toUpdate = new ArrayList<>();
                for (int i = 0; i < fluidTankProperties.length; i++) {
                    FluidStack content = fluidTankProperties[i].getContents();
                    if (fluids[i] == null || (content == null && fluids[i].getContents() != null) ||
                            (content != null && fluids[i].getContents() == null) ||
                            fluidTankProperties[i].getCapacity() != fluids[i].getCapacity() ||
                            fluidTankProperties[i].canDrain() != fluids[i].canDrain() ||
                            fluidTankProperties[i].canFill() != fluids[i].canFill()) {
                        syncFlag = true;
                        fluids[i] = new FluidTankProperties(content, fluidTankProperties[i].getCapacity(),
                                fluidTankProperties[i].canFill(), fluidTankProperties[i].canDrain());
                        toUpdate.add(i);
                    } else if (content != null && (fluids[i] != null && fluids[i].getContents() != null &&
                            (content.amount != fluids[i].getContents().amount ||
                                    !content.isFluidEqual(fluids[i].getContents())))) {
                                        syncFlag = true;
                                        fluids[i] = new FluidTankProperties(content,
                                                fluidTankProperties[i].getCapacity(), fluidTankProperties[i].canFill(),
                                                fluidTankProperties[i].canDrain());
                                        toUpdate.add(i);
                                    }
                }
                if (syncFlag) writeCustomData(GregtechDataCodes.UPDATE_FLUID, packetBuffer -> {
                    packetBuffer.writeVarInt(fluids.length);
                    packetBuffer.writeVarInt(toUpdate.size());
                    for (Integer index : toUpdate) {
                        writeFluid(packetBuffer, index);
                    }
                });
            }
        }
        if (mode == MODE.ITEM || (mode == MODE.PROXY && proxyMode[1] > 0)) {
            boolean syncFlag = false;
            IItemHandler itemHandler = this.getItemCapability();
            if (itemHandler != null) {
                int size = itemHandler.getSlots();
                if (this.slot < size) {
                    int maxStoredItems = itemHandler.getSlotLimit(this.slot);
                    if (maxStoredItems != maxItemCapability) {
                        maxItemCapability = maxStoredItems;
                        syncFlag = true;
                    }
                }
                List<Integer> toUpdate = new ArrayList<>();
                if (items.length != size) {
                    items = new ItemStack[size];
                    syncFlag = true;
                }
                for (int i = 0; i < size; i++) {
                    if (items[i] == null) {
                        items[i] = ItemStack.EMPTY;
                    }
                    ItemStack content = itemHandler.getStackInSlot(i);
                    if (!ItemStack.areItemStacksEqual(items[i], content)) {
                        syncFlag = true;
                        items[i] = content.copy();
                        toUpdate.add(i);
                    }
                }
                if (syncFlag) writeCustomData(GregtechDataCodes.UPDATE_ITEM, packetBuffer -> {
                    packetBuffer.writeVarInt(maxItemCapability);
                    packetBuffer.writeVarInt(items.length);
                    packetBuffer.writeVarInt(toUpdate.size());
                    for (Integer index : toUpdate) {
                        packetBuffer.writeVarInt(index);
                        packetBuffer.writeCompoundTag(fixItemStackSer(items[index]));
                    }
                });
            }
        }
        if (this.mode == MODE.ENERGY || (mode == MODE.PROXY && proxyMode[2] > 0)) {
            if (getCoverableView() instanceof MetaTileEntityPowerSubstation pss) {
                if (energyStored != pss.getStoredLong() || energyCapability != pss.getCapacityLong()) {
                    energyStored = pss.getStoredLong();
                    energyCapability = pss.getCapacityLong();
                    writeCustomData(GregtechDataCodes.UPDATE_ENERGY, packetBuffer -> {
                        packetBuffer.writeLong(energyStored);
                        packetBuffer.writeLong(energyCapability);
                    });
                }
                if (this.getOffsetTimer() % 20 == 0) {
                    writeCustomData(GregtechDataCodes.UPDATE_ENERGY_PER, packetBuffer -> {
                        packetBuffer.writeLong(pss.getAverageInLastSec() * 20L);
                        packetBuffer.writeLong(pss.getAverageOutLastSec() * 20L);
                        inputEnergyList.add(energyInputPerDur);
                        outputEnergyList.add(energyOutputPerDur);
                        if (inputEnergyList.size() > 13) {
                            inputEnergyList.remove(0);
                            outputEnergyList.remove(0);
                        }
                    });
                }
            } else {
                IEnergyContainer energyContainer = this.getEnergyCapability();
                if (energyContainer != null) {
                    // TODO, figure out what to do when values exceed Long.MAX_VALUE, ie with multiple Ultimate
                    // batteries
                    if (energyStored != energyContainer.getEnergyStored() ||
                            energyCapability != energyContainer.getEnergyCapacity()) {
                        energyStored = energyContainer.getEnergyStored();
                        energyCapability = energyContainer.getEnergyCapacity();
                        writeCustomData(GregtechDataCodes.UPDATE_ENERGY, packetBuffer -> {
                            packetBuffer.writeLong(energyStored);
                            packetBuffer.writeLong(energyCapability);
                        });
                    }
                    if (this.getOffsetTimer() % 20 == 0) { // per second
                        writeCustomData(GregtechDataCodes.UPDATE_ENERGY_PER, packetBuffer -> {
                            packetBuffer.writeLong(energyContainer.getInputPerSec());
                            packetBuffer.writeLong(energyContainer.getOutputPerSec());
                            inputEnergyList.add(energyInputPerDur);
                            outputEnergyList.add(energyOutputPerDur);
                            if (inputEnergyList.size() > 13) {
                                inputEnergyList.remove(0);
                                outputEnergyList.remove(0);
                            }
                        });
                    }
                }
            }
        }
        if (this.mode == MODE.MACHINE || (mode == MODE.PROXY && proxyMode[3] > 0)) {
            IWorkable workable = this.getMachineCapability();
            if (workable != null) {
                int progress = workable.getProgress();
                int maxProgress = workable.getMaxProgress();
                boolean isActive = workable.isActive();
                boolean isWorkingEnable = workable.isWorkingEnabled();
                if (isActive != this.isActive || isWorkingEnable != this.isWorkingEnabled ||
                        this.progress != progress || this.maxProgress != maxProgress) {
                    this.progress = progress;
                    this.maxProgress = maxProgress;
                    this.isWorkingEnabled = isWorkingEnable;
                    this.isActive = isActive;
                    writeCustomData(GregtechDataCodes.UPDATE_MACHINE, packetBuffer -> {
                        packetBuffer.writeInt(progress);
                        packetBuffer.writeInt(maxProgress);
                        packetBuffer.writeBoolean(isActive);
                        packetBuffer.writeBoolean(isWorkingEnable);
                    });
                }
                if (this.getOffsetTimer() % 20 == 0) {
                    if (getCoverableView() instanceof MetaTileEntityPowerSubstation pss) {
                        if (energyStored != pss.getStoredLong() || energyCapability != pss.getCapacityLong()) {
                            energyStored = pss.getStoredLong();
                            energyCapability = pss.getCapacityLong();
                            writeCustomData(GregtechDataCodes.UPDATE_ENERGY, packetBuffer -> {
                                packetBuffer.writeLong(energyStored);
                                packetBuffer.writeLong(energyCapability);
                            });
                        }
                    } else {
                        IEnergyContainer energyContainer = this.getEnergyCapability();
                        if (energyContainer != null) {
                            if (energyStored != energyContainer.getEnergyStored() ||
                                    energyCapability != energyContainer.getEnergyCapacity()) {
                                energyStored = energyContainer.getEnergyStored();
                                energyCapability = energyContainer.getEnergyCapacity();
                                writeCustomData(GregtechDataCodes.UPDATE_ENERGY, packetBuffer -> {
                                    packetBuffer.writeLong(energyStored);
                                    packetBuffer.writeLong(energyCapability);
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    private void writeAllFluids(PacketBuffer packetBuffer) {
        packetBuffer.writeVarInt(fluids.length);
        packetBuffer.writeVarInt(fluids.length);
        for (int i = 0; i < fluids.length; i++) {
            writeFluid(packetBuffer, i);
        }
    }

    private void writeFluid(PacketBuffer packetBuffer, int i) {
        packetBuffer.writeVarInt(i);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("Capacity", fluids[i].getCapacity());
        FluidStack fluidStack = fluids[i].getContents();
        if (fluidStack != null) {
            fluidStack.writeToNBT(nbt);
        }
        packetBuffer.writeCompoundTag(nbt);
    }

    private void readFluids(PacketBuffer packetBuffer) {
        int size = packetBuffer.readVarInt();
        if (fluids == null || fluids.length != size) {
            fluids = new FluidTankProperties[size];
        }
        size = packetBuffer.readVarInt();
        try {
            for (int i = 0; i < size; i++) {
                int index = packetBuffer.readVarInt();
                NBTTagCompound nbt = packetBuffer.readCompoundTag();
                if (nbt != null) {
                    fluids[index] = new FluidTankProperties(FluidStack.loadFluidStackFromNBT(nbt),
                            nbt.getInteger("Capacity"));
                }
            }
        } catch (IOException e) {
            GTLog.logger.error("Could not read fluids from NBT buffer", e);
        }
    }

    private void writeAllItems(PacketBuffer packetBuffer) {
        packetBuffer.writeVarInt(maxItemCapability);
        packetBuffer.writeVarInt(items.length);
        packetBuffer.writeVarInt(items.length);
        for (int i = 0; i < items.length; i++) {
            packetBuffer.writeVarInt(i);
            packetBuffer.writeCompoundTag(fixItemStackSer(items[i]));
        }
    }

    private void readItems(PacketBuffer packetBuffer) {
        maxItemCapability = packetBuffer.readVarInt();
        int size = packetBuffer.readVarInt();
        if (items == null || items.length != size) {
            items = new ItemStack[size];
        }
        size = packetBuffer.readVarInt();
        try {
            for (int i = 0; i < size; i++) {
                int index = packetBuffer.readVarInt();
                NBTTagCompound nbt = packetBuffer.readCompoundTag();
                if (nbt != null) {
                    items[index] = new ItemStack(nbt);
                    items[index].setCount(nbt.getInteger("count"));
                } else {
                    items[index] = ItemStack.EMPTY;
                }
            }
        } catch (IOException e) {
            GTLog.logger.error("Could not read items from NBT buffer", e);
        }
    }

    public static NBTTagCompound fixItemStackSer(ItemStack itemStack) {
        NBTTagCompound nbt = itemStack.serializeNBT();
        nbt.setInteger("count", itemStack.getCount());
        return nbt;
    }

    public IFluidHandler getFluidCapability() {
        TileEntity te = getCoveredTE();
        IFluidHandler capability = te == null ? null :
                te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getCoveredFacing());
        if (capability == null && this.getCoverableView() instanceof MultiblockControllerBase controllerBase) {
            List<IFluidTank> input = controllerBase.getAbilities(MultiblockAbility.IMPORT_FLUIDS);
            List<IFluidTank> output = controllerBase.getAbilities(MultiblockAbility.EXPORT_FLUIDS);
            List<IFluidTank> list = new ArrayList<>();
            if (input.size() > 0) {
                list.addAll(input);
            }
            if (output.size() > 0) {
                list.addAll(output);
            }
            capability = new FluidTankList(true, list);
        }
        return capability;
    }

    public IItemHandler getItemCapability() {
        TileEntity te = getCoveredTE();
        IItemHandler capability = te == null ? null :
                te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getCoveredFacing());
        if (capability == null && this.getCoverableView() instanceof MultiblockControllerBase controllerBase) {
            List<IItemHandlerModifiable> input = controllerBase.getAbilities(MultiblockAbility.IMPORT_ITEMS);
            List<IItemHandlerModifiable> output = controllerBase.getAbilities(MultiblockAbility.EXPORT_ITEMS);
            List<IItemHandlerModifiable> list = new ArrayList<>();
            if (input.size() > 0) {
                list.addAll(input);
            }
            if (output.size() > 0) {
                list.addAll(output);
            }
            capability = new ItemHandlerList(list);
        }
        return capability;
    }

    public IEnergyContainer getEnergyCapability() {
        TileEntity te = getCoveredTE();
        IEnergyContainer capability = te == null ? null :
                te.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, getCoveredFacing());
        if (capability == null && this.getCoverableView() instanceof MultiblockControllerBase controllerBase) {
            List<IEnergyContainer> input = controllerBase.getAbilities(MultiblockAbility.INPUT_ENERGY);
            List<IEnergyContainer> output = controllerBase.getAbilities(MultiblockAbility.OUTPUT_ENERGY);
            List<IEnergyContainer> list = new ArrayList<>();
            if (input.size() > 0) {
                list.addAll(input);
            }
            if (output.size() > 0) {
                list.addAll(output);
            }
            capability = new EnergyContainerList(list);
        } else if (capability == null && te != null) {
            IEnergyStorage fe = te.getCapability(CapabilityEnergy.ENERGY, getCoveredFacing());
            if (fe != null) {
                return new IEnergyContainer() {

                    public long acceptEnergyFromNetwork(EnumFacing enumFacing, long l, long l1) {
                        return 0;
                    }

                    public boolean inputsEnergy(EnumFacing enumFacing) {
                        return false;
                    }

                    public long changeEnergy(long l) {
                        return 0;
                    }

                    public long getEnergyStored() {
                        return FeCompat.toEu(fe.getEnergyStored(), FeCompat.ratio(false));
                    }

                    public long getEnergyCapacity() {
                        return FeCompat.toEu(fe.getMaxEnergyStored(), FeCompat.ratio(false));
                    }

                    public long getInputAmperage() {
                        return 0;
                    }

                    public long getInputVoltage() {
                        return 0;
                    }
                };
            }
        }
        return capability;
    }

    public IWorkable getMachineCapability() {
        TileEntity te = getCoveredTE();
        return te == null ? null : te.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, getCoveredFacing());
    }

    @Override
    public void readCustomData(int id, @NotNull PacketBuffer packetBuffer) {
        super.readCustomData(id, packetBuffer);
        if (id == GregtechDataCodes.UPDATE_COVER_MODE) { // set mode
            setMode(MODE.VALUES[packetBuffer.readByte()], packetBuffer.readInt(),
                    EnumFacing.byIndex(packetBuffer.readByte()));
        } else if (id == GregtechDataCodes.UPDATE_FLUID) { // sync fluids
            readFluids(packetBuffer);
        } else if (id == GregtechDataCodes.UPDATE_ITEM) {
            readItems(packetBuffer);
        } else if (id == GregtechDataCodes.UPDATE_ENERGY) {
            energyStored = packetBuffer.readLong();
            energyCapability = packetBuffer.readLong();
        } else if (id == GregtechDataCodes.UPDATE_ENERGY_PER) {
            energyInputPerDur = packetBuffer.readLong();
            energyOutputPerDur = packetBuffer.readLong();
            inputEnergyList.add(energyInputPerDur);
            outputEnergyList.add(energyOutputPerDur);
            if (inputEnergyList.size() > 13) {
                inputEnergyList.remove(0);
                outputEnergyList.remove(0);
            }
        } else if (id == GregtechDataCodes.UPDATE_MACHINE) {
            this.progress = packetBuffer.readInt();
            this.maxProgress = packetBuffer.readInt();
            this.isActive = packetBuffer.readBoolean();
            boolean isWorkingEnable = packetBuffer.readBoolean();
            if (this.isWorkingEnabled != isWorkingEnable && this.mode == MODE.MACHINE) {
                this.isWorkingEnabled = isWorkingEnable;
                this.scheduleRenderUpdate();
            }
            this.isWorkingEnabled = isWorkingEnable;
        }
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return canCapabilityAttach();
    }

    public boolean canCapabilityAttach() {
        return getFluidCapability() != null ||
                getItemCapability() != null ||
                getEnergyCapability() != null ||
                getCoverableView() instanceof MetaTileEntityPowerSubstation ||
                getMachineCapability() != null;
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, T defaultValue) {
        if (this.mode == MODE.PROXY) {
            if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER && defaultValue == null) {
                return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(IEnergyContainer.DEFAULT);
            }
        }
        return defaultValue;
    }

    @Override
    public void renderCover(CCRenderState ccRenderState, Matrix4 translation, IVertexOperation[] ops, Cuboid6 cuboid6,
                            BlockRenderLayer blockRenderLayer) {
        codechicken.lib.vec.Rotation rotation = new codechicken.lib.vec.Rotation(0, 0, 1, 0);
        if (this.getAttachedSide().getAxis().isVertical()) {
            if (this.spin == EnumFacing.WEST) {
                translation.translate(0, 0, 1);
                rotation = new codechicken.lib.vec.Rotation(Math.PI / 2, 0, 1, 0);
            } else if (this.spin == EnumFacing.EAST) {
                translation.translate(1, 0, 0);
                rotation = new codechicken.lib.vec.Rotation(-Math.PI / 2, 0, 1, 0);
            } else if (this.spin == EnumFacing.SOUTH) {
                translation.translate(1, 0, 1);
                rotation = new Rotation(Math.PI, 0, 1, 0);
            }
            translation.apply(rotation);
        }
        if (mode == MODE.PROXY) {
            Textures.COVER_INTERFACE_PROXY.renderSided(getAttachedSide(), cuboid6, ccRenderState, ops, translation);
        } else if (mode == MODE.FLUID) {
            Textures.COVER_INTERFACE_FLUID.renderSided(getAttachedSide(), cuboid6, ccRenderState,
                    ArrayUtils.addAll(ops, rotation), translation);
            Textures.COVER_INTERFACE_FLUID_GLASS.renderSided(getAttachedSide(), cuboid6, ccRenderState,
                    ArrayUtils.addAll(ops, rotation), RenderUtil.adjustTrans(translation, getAttachedSide(), 3));
        } else if (mode == MODE.ITEM) {
            Textures.COVER_INTERFACE_ITEM.renderSided(getAttachedSide(), cuboid6, ccRenderState,
                    ArrayUtils.addAll(ops, rotation), translation);
        } else if (mode == MODE.ENERGY) {
            Textures.COVER_INTERFACE_ENERGY.renderSided(getAttachedSide(), cuboid6, ccRenderState,
                    ArrayUtils.addAll(ops, rotation), translation);
        } else if (mode == MODE.MACHINE) {
            if (isWorkingEnabled) {
                Textures.COVER_INTERFACE_MACHINE_ON.renderSided(getAttachedSide(), cuboid6, ccRenderState,
                        ArrayUtils.addAll(ops, rotation), translation);
            } else {
                Textures.COVER_INTERFACE_MACHINE_OFF.renderSided(getAttachedSide(), cuboid6, ccRenderState,
                        ArrayUtils.addAll(ops, rotation), translation);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {}

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        GlStateManager.pushMatrix();
        /* hack the lightmap */
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

        RenderUtil.moveToFace(x, y, z, getAttachedSide());
        RenderUtil.rotateToFace(getAttachedSide(),
                getAttachedSide().getAxis() == EnumFacing.Axis.Y ? this.spin : EnumFacing.NORTH);

        if (!renderSneakingLookAt(this.getPos(), getAttachedSide(), this.slot, partialTicks)) {
            renderMode(this.mode, this.slot, partialTicks);
        }

        /* restore the lightmap */
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
        net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0 && this.mode != MODE.PROXY;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public boolean renderSneakingLookAt(BlockPos blockPos, EnumFacing side, int slot, float partialTicks) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null && player.isSneaking() && player.getHeldItemMainhand().isEmpty()) {
            RayTraceResult rayTraceResult = player
                    .rayTrace(Minecraft.getMinecraft().playerController.getBlockReachDistance(), partialTicks);
            if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK &&
                    rayTraceResult.sideHit == side && rayTraceResult.getBlockPos().equals(blockPos)) {
                RenderUtil.renderRect(-7f / 16, -7f / 16, 3f / 16, 3f / 16, 0.002f, 0XFF838583);
                RenderUtil.renderRect(4f / 16, -7f / 16, 3f / 16, 3f / 16, 0.002f, 0XFF838583);
                RenderUtil.renderText(-5.5f / 16, -5.5F / 16, 0, 1.0f / 70, 0XFFFFFFFF, "<", true);
                RenderUtil.renderText(5.7f / 16, -5.5F / 16, 0, 1.0f / 70, 0XFFFFFFFF, ">", true);
                RenderUtil.renderText(0, -5.5F / 16, 0, 1.0f / 120, 0XFFFFFFFF, "Slot: " + slot, true);
                TileEntity te = getCoveredTE();
                if (te != null) {
                    ItemStack itemStack;
                    if (te instanceof IGregTechTileEntity) {
                        itemStack = ((IGregTechTileEntity) te).getMetaTileEntity().getStackForm();
                    } else {
                        BlockPos pos = te.getPos();
                        itemStack = te.getBlockType().getPickBlock(te.getWorld().getBlockState(pos),
                                new RayTraceResult(new Vec3d(0.5, 0.5, 0.5), getCoveredFacing(), pos), te.getWorld(),
                                pos, Minecraft.getMinecraft().player);
                    }
                    String name = itemStack.getDisplayName();
                    RenderUtil.renderRect(-7f / 16, -4f / 16, 14f / 16, 1f / 16, 0.002f, 0XFF000000);
                    RenderUtil.renderText(0, -3.5F / 16, 0, 1.0f / 200, 0XFFFFFFFF, name, true);
                    RenderUtil.renderItemOverLay(-8f / 16, -5f / 16, 0.002f, 1f / 32, itemStack);
                }
                return true;
            }
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void renderMode(MODE mode, int slot, float partialTicks) {
        if (mode == MODE.FLUID && fluids.length > slot && slot >= 0 && fluids[slot] != null &&
                fluids[slot].getContents() != null) {
            renderFluidMode(slot);
        } else if (mode == MODE.ITEM && items.length > slot && slot >= 0 && items[slot] != null) {
            renderItemMode(slot);
        } else if (mode == MODE.ENERGY) {
            renderEnergyMode();
        } else if (mode == MODE.MACHINE) {
            renderMachineMode(partialTicks);
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderMachineMode(float partialTicks) {
        int color = energyCapability > 10 * energyStored ? 0XFFFF2F39 : isWorkingEnabled ? 0XFF00FF00 : 0XFFFF662E;
        if (isActive && maxProgress != 0) {
            float offset = ((this.getOffsetTimer() % 20 + partialTicks) * 0.875f / 20);
            float start = Math.max(-0.4375f, -0.875f + 2 * offset);
            float width = Math.min(0.4375f, -0.4375f + 2 * offset) - start;
            int startAlpha = 0X00;
            int endAlpha = 0XFF;
            if (offset < 0.4375f) {
                startAlpha = (int) (255 - 255 / 0.4375 * offset);
            } else if (start > 0.4375) {
                endAlpha = (int) (510 - 255 / 0.4375 * offset);
            }
            RenderUtil.renderRect(-7f / 16, -7f / 16, progress * 14f / (maxProgress * 16), 3f / 16, 0.002f, 0XFFFF5F44);
            RenderUtil.renderText(0, -5.5F / 16, 0, 1.0f / (isProxy() ? 110 : 70), 0XFFFFFFFF,
                    readAmountOrCountOrEnergy(progress * 100L / maxProgress, MODE.MACHINE), true);
            RenderUtil.renderGradientRect(start, -4f / 16, width, 1f / 16, 0.002f,
                    (color & 0X00FFFFFF) | (startAlpha << 24), (color & 0X00FFFFFF) | (endAlpha << 24), true);
        } else {
            RenderUtil.renderRect(-7f / 16, -4f / 16, 14f / 16, 1f / 16, 0.002f, color);
        }
        if (this.isProxy()) {
            if (isWorkingEnabled) {
                RenderUtil.renderTextureArea(GuiTextures.COVER_INTERFACE_MACHINE_ON_PROXY, -7f / 16, 1f / 16, 14f / 16,
                        3f / 16, 0.002f);
            } else {
                RenderUtil.renderTextureArea(GuiTextures.COVER_INTERFACE_MACHINE_OFF_PROXY, -7f / 16, -1f / 16,
                        14f / 16, 5f / 16, 0.002f);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderEnergyMode() {
        if (inputEnergyList.isEmpty()) return;
        long max = Long.MIN_VALUE;
        for (long d : inputEnergyList) {
            max = Math.max(max, d);
        }
        for (long d : outputEnergyList) {
            max = Math.max(max, d);
        }
        RenderUtil.renderLineChart(inputEnergyList, max, -5.5f / 16, 5.5f / 16, 12f / 16, 6f / 16, 0.005f, 0XFF03FF00);
        RenderUtil.renderLineChart(outputEnergyList, max, -5.5f / 16, 5.5f / 16, 12f / 16, 6f / 16, 0.005f, 0XFFFF2F39);
        RenderUtil.renderText(-5.7f / 16, -2.3f / 16, 0, 1.0f / 270, 0XFF03FF00,
                "EU I: " + TextFormattingUtil.formatNumbers(energyInputPerDur / 20) + "EU/t",
                false);
        RenderUtil.renderText(-5.7f / 16, -1.6f / 16, 0, 1.0f / 270, 0XFFFF0000,
                "EU O: " + TextFormattingUtil.formatNumbers(energyOutputPerDur / 20) + "EU/t",
                false);
        // Bandaid fix to prevent overflowing renders when dealing with items that cause long overflow, ie Ultimate
        // Battery
        RenderUtil.renderRect(-7f / 16, -7f / 16, Math.max(0, energyStored * 14f / (energyCapability * 16)), 3f / 16,
                0.002f, 0XFFFFD817);
        RenderUtil.renderText(0, -5.5F / 16, 0, 1.0f / (isProxy() ? 110 : 70), 0XFFFFFFFF,
                readAmountOrCountOrEnergy(energyStored, MODE.ENERGY), true);
    }

    @SideOnly(Side.CLIENT)
    private void renderItemMode(int slot) {
        ItemStack itemStack = items[slot];
        if (!itemStack.isEmpty()) {
            RenderUtil.renderItemOverLay(-8f / 16, -5f / 16, 0, 1f / 32, itemStack);
            if (maxItemCapability != 0) {
                RenderUtil.renderRect(-7f / 16, -7f / 16,
                        Math.max(itemStack.getCount() * 14f / (maxItemCapability * 16), 0.001f), 3f / 16, 0.002f,
                        0XFF25B9FF);
            } else {
                RenderUtil.renderRect(-7f / 16, -7f / 16,
                        Math.max(itemStack.getCount() * 14f / (itemStack.getMaxStackSize() * 16), 0.001f), 3f / 16,
                        0.002f, 0XFF25B9FF);
            }
            RenderUtil.renderText(0, -5.5F / 16, 0, 1.0f / (isProxy() ? 110 : 70), 0XFFFFFFFF,
                    readAmountOrCountOrEnergy(itemStack.getCount(), MODE.ITEM), true);

        }
    }

    @SideOnly(Side.CLIENT)
    private void renderFluidMode(int slot) {
        FluidStack fluidStack = fluids[slot].getContents();
        assert fluidStack != null;
        float height = 10f / 16 * Math.max(fluidStack.amount * 1.0f / fluids[slot].getCapacity(), 0.001f);
        RenderUtil.renderFluidOverLay(-7f / 16, 0.4375f - height, 14f / 16, height, 0.002f, fluidStack, 0.8f);
        int fluidColor = WidgetOreList.getFluidColor(fluidStack.getFluid());
        int textColor = ((fluidColor & 0xff) + ((fluidColor >> 8) & 0xff) + ((fluidColor >> 16) & 0xff)) / 3 >
                (255 / 2) ? 0X0 : 0XFFFFFFFF;
        RenderUtil.renderRect(-7f / 16, -7f / 16, 14f / 16, 3f / 16, 0.002f, fluidColor | (255 << 24));
        RenderUtil.renderText(0, -5.5F / 16, 0, 1.0f / (isProxy() ? 110 : 70), textColor,
                readAmountOrCountOrEnergy(fluidStack.amount, MODE.FLUID), true);
    }

    static String[][] units = {
            { "", "mB", "", "EU" },
            { "", "B", "K", "KEU" },
            { "", "KB", "M", "MEU" },
            { "", "MB", "G", "GEU" },
            { "", "GB", "T", "TEU" },
            { "", "TB", "P", "PEU" },
    };

    @SideOnly(Side.CLIENT)
    private static String readAmountOrCountOrEnergy(long number, MODE mode) {
        int unit = mode == MODE.FLUID ? 1 : mode == MODE.ITEM ? 2 : mode == MODE.ENERGY ? 3 : 0;
        if (mode == MODE.MACHINE) {
            return number + "%";
        }

        if (number / 1000 == 0) {
            return number + units[0][unit];
        }
        int i = 1;

        while (number / 10000000 != 0 && i < units.length) {
            number = number / 1000;
            ++i;
        }

        return new DecimalFormat("#.#").format(number * 1.0f / 1000) + units[i][unit];
    }
}
