package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IMiner;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.capability.impl.miner.MinerLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.client.renderer.texture.Textures;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MetaTileEntityMiner extends TieredMetaTileEntity implements IMiner, IControllable, IDataInfoProvider {

    private final ItemStackHandler chargerInventory;

    private final int inventorySize;
    private final long energyPerTick;
    private boolean isInventoryFull = false;

    private final MinerLogic minerLogic;

    public MetaTileEntityMiner(ResourceLocation metaTileEntityId, int tier, int speed, int maximumRadius, int fortune) {
        super(metaTileEntityId, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.energyPerTick = GTValues.V[tier - 1];
        this.minerLogic = new MinerLogic(this, fortune, speed, maximumRadius, Textures.SOLID_STEEL_CASING);
        this.chargerInventory = new GTItemStackHandler(this, 1);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMiner(metaTileEntityId, getTier(), this.minerLogic.getSpeed(),
                this.minerLogic.getMaximumRadius(), this.minerLogic.getFortune());
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 0, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, inventorySize, this, true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.SCREEN.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        for (EnumFacing renderSide : EnumFacing.HORIZONTALS) {
            if (renderSide == getFrontFacing()) {
                Textures.PIPE_OUT_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
            } else
                Textures.CHUNK_MINER_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
        }
        minerLogic.renderPipe(renderState, translation, pipeline);
    }

    @Override
    protected ModularUI createUI(@NotNull EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);
        ModularUI.Builder builder = new ModularUI.Builder(GuiTextures.BACKGROUND, 195, 176);
        builder.bindPlayerInventory(entityPlayer.inventory, 94);

        if (getTier() == GTValues.HV) {
            for (int y = 0; y < rowSize; y++) {
                for (int x = 0; x < rowSize; x++) {
                    int index = y * rowSize + x;
                    builder.widget(
                            new SlotWidget(exportItems, index, 151 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                                    .setBackgroundTexture(GuiTextures.SLOT));
                }
            }
        } else {
            for (int y = 0; y < rowSize; y++) {
                for (int x = 0; x < rowSize; x++) {
                    int index = y * rowSize + x;
                    builder.widget(
                            new SlotWidget(exportItems, index, 142 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                                    .setBackgroundTexture(GuiTextures.SLOT));
                }
            }
        }

        builder.image(7, 16, 105, 75, GuiTextures.DISPLAY)
                .label(6, 6, getMetaFullName());
        builder.widget(new AdvancedTextWidget(10, 19, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(84));
        builder.widget(new AdvancedTextWidget(70, 19, this::addDisplayText2, 0xFFFFFF)
                .setMaxWidthLimit(84));
        builder.widget(new SlotWidget(chargerInventory, 0, 171, 152)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY));

        return builder.build(getHolder(), entityPlayer);
    }

    private void addDisplayText(@NotNull List<ITextComponent> textList) {
        int workingArea = getWorkingArea(minerLogic.getCurrentRadius());
        textList.add(new TextComponentTranslation("gregtech.machine.miner.startx", this.minerLogic.getX().get()));
        textList.add(new TextComponentTranslation("gregtech.machine.miner.starty", this.minerLogic.getY().get()));
        textList.add(new TextComponentTranslation("gregtech.machine.miner.startz", this.minerLogic.getZ().get()));
        textList.add(new TextComponentTranslation("gregtech.machine.miner.working_area", workingArea, workingArea));
        if (this.minerLogic.isDone())
            textList.add(new TextComponentTranslation("gregtech.machine.miner.done")
                    .setStyle(new Style().setColor(TextFormatting.GREEN)));
        else if (this.minerLogic.isWorking())
            textList.add(new TextComponentTranslation("gregtech.machine.miner.working")
                    .setStyle(new Style().setColor(TextFormatting.GOLD)));
        else if (!this.isWorkingEnabled())
            textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));
        if (isInventoryFull)
            textList.add(new TextComponentTranslation("gregtech.machine.miner.invfull")
                    .setStyle(new Style().setColor(TextFormatting.RED)));
        if (!drainEnergy(true))
            textList.add(new TextComponentTranslation("gregtech.machine.miner.needspower")
                    .setStyle(new Style().setColor(TextFormatting.RED)));
    }

    private void addDisplayText2(@NotNull List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation("gregtech.machine.miner.minex", this.minerLogic.getMineX().get()));
        textList.add(new TextComponentTranslation("gregtech.machine.miner.miney", this.minerLogic.getMineY().get()));
        textList.add(new TextComponentTranslation("gregtech.machine.miner.minez", this.minerLogic.getMineZ().get()));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        int currentArea = getWorkingArea(minerLogic.getCurrentRadius());
        tooltip.add(I18n.format("gregtech.machine.miner.tooltip", currentArea, currentArea));
        tooltip.add(I18n.format("gregtech.universal.tooltip.uses_per_tick", energyPerTick) + TextFormatting.GRAY +
                ", " + I18n.format("gregtech.machine.miner.per_block", this.minerLogic.getSpeed() / 20));
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(),
                GTValues.VNF[getTier()]));
        tooltip.add(
                I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        int maxArea = getWorkingArea(minerLogic.getMaximumRadius());
        tooltip.add(I18n.format("gregtech.universal.tooltip.working_area_max", maxArea, maxArea));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.toggle_mode_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.reset"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public boolean drainEnergy(boolean simulate) {
        long resultEnergy = energyContainer.getEnergyStored() - energyPerTick;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(energyPerTick);
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        super.update();
        this.minerLogic.performMining();
        if (!getWorld().isRemote) {
            ((EnergyContainerHandler) this.energyContainer).dischargeOrRechargeEnergyContainers(chargerInventory, 0);

            if (getOffsetTimer() % 5 == 0)
                pushItemsIntoNearbyHandlers(getFrontFacing());

            if (this.minerLogic.wasActiveAndNeedsUpdate()) {
                this.minerLogic.setWasActiveAndNeedsUpdate(false);
                this.minerLogic.setActive(false);
            }
        }
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote) return true;

        if (!this.isActive()) {
            int currentRadius = this.minerLogic.getCurrentRadius();
            if (currentRadius == 1)
                this.minerLogic.setCurrentRadius(this.minerLogic.getMaximumRadius());
            else if (playerIn.isSneaking())
                this.minerLogic.setCurrentRadius(Math.max(1, Math.round(currentRadius / 2.0f)));
            else
                this.minerLogic.setCurrentRadius(Math.max(1, currentRadius - 1));

            this.minerLogic.resetArea();

            int workingArea = getWorkingArea(minerLogic.getCurrentRadius());
            playerIn.sendMessage(
                    new TextComponentTranslation("gregtech.machine.miner.working_area", workingArea, workingArea));
        } else {
            playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.miner.errorradius"));
        }
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("ChargerInventory", chargerInventory.serializeNBT());
        return this.minerLogic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.chargerInventory.deserializeNBT(data.getCompoundTag("ChargerInventory"));
        this.minerLogic.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.writeInitialSyncData(buf);
        this.minerLogic.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.minerLogic.receiveInitialSyncData(buf);
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        this.minerLogic.receiveCustomData(dataId, buf);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, chargerInventory);
    }

    @Override
    public boolean isInventoryFull() {
        return isInventoryFull;
    }

    @Override
    public void setInventoryFull(boolean isFull) {
        this.isInventoryFull = isFull;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.minerLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.minerLogic.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.MINER;
    }

    @Override
    public boolean isActive() {
        return minerLogic.isActive() && isWorkingEnabled();
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        int workingArea = getWorkingArea(minerLogic.getCurrentRadius());
        return Collections.singletonList(
                new TextComponentTranslation("gregtech.machine.miner.working_area", workingArea, workingArea));
    }
}
