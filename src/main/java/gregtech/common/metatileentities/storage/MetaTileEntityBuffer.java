package gregtech.common.metatileentities.storage;

import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.RenderUtil;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.*;

public class MetaTileEntityBuffer extends MetaTileEntity implements ITieredMetaTileEntity {

    private final int tier;
    private FluidTankList fluidTankList;
    private ItemStackHandler itemStackHandler;
    private boolean autoOutputItems;
    private boolean autoOutputFluids;
    private EnumFacing outputFacingItems;
    private EnumFacing outputFacingFluids;

    public MetaTileEntityBuffer(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId);
        this.tier = tier;
        initializeInventory();
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        FilteredFluidHandler[] fluidHandlers = new FilteredFluidHandler[tier + 2];
        for (int i = 0; i < tier + 2; i++) {
            fluidHandlers[i] = new FilteredFluidHandler((int) (2000 * Math.pow(2, getTier())));
        }
        fluidInventory = fluidTankList = new FluidTankList(false, fluidHandlers);
        itemInventory = itemStackHandler = new GTItemStackHandler(this, ((int) Math.pow(tier + 2, 2)));
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    @Override
    public IItemHandlerModifiable getExportItems() {
        return itemStackHandler;
    }

    @Override
    public FluidTankList getExportFluids() {
        return fluidTankList;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBuffer(metaTileEntityId, tier);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[tier].getParticleSprite(), this.getPaintingColorForRendering());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int invTier = tier + 2;
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND,
                176, 18 + Math.max(166, 112 + 18 * invTier));// 176, 166
        for (int i = 0; i < this.fluidTankList.getTanks(); i++) {
            builder.widget(new TankWidget(this.fluidTankList.getTankAt(i), 151, 18 * (i + 1), 18, 18)
                    .setAlwaysShowFull(true)
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT)
                    .setContainerClicking(true, true));
        }
        for (int y = 0; y < invTier; y++) {
            for (int x = 0; x < invTier; x++) {
                int index = y * invTier + x;
                builder.slot(itemStackHandler, index, 8 + x * 18, 18 * (y + 1), GuiTextures.SLOT);
            }
        }
        builder.widget(new ToggleButtonWidget(8, 18 * invTier + 21, 18, 18,
                GuiTextures.BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setAutoOutputItems)
                        .setTooltipText("gregtech.gui.item_auto_output.tooltip")
                        .shouldUseBaseBackground());
        builder.widget(new ToggleButtonWidget(26, 18 * invTier + 21, 18, 18,
                GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)
                        .setTooltipText("gregtech.gui.item_auto_output.tooltip")
                        .shouldUseBaseBackground());
        return builder.label(6, 6, getMetaFullName())
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 8, 42 + 18 * invTier)
                .build(getHolder(), entityPlayer);
    }

    @Override
    public int getTier() {
        return tier;
    }

    public boolean isAutoOutputItems() {
        return autoOutputItems;
    }

    public boolean isAutoOutputFluids() {
        return autoOutputFluids;
    }

    public void setAutoOutputItems(boolean autoOutputItems) {
        this.autoOutputItems = autoOutputItems;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_OUTPUT_ITEMS, buf -> buf.writeBoolean(autoOutputItems));
            markDirty();
        }
    }

    public void setAutoOutputFluids(boolean autoOutputFluids) {
        this.autoOutputFluids = autoOutputFluids;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_OUTPUT_FLUIDS, buf -> buf.writeBoolean(autoOutputFluids));
            markDirty();
        }
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                 CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getOutputFacingItems() == facing) return false;
            if (hasFrontFacing() && facing == getFrontFacing()) return false;
            if (!getWorld().isRemote) {
                setOutputFacing(facing);
            }
            return true;
        }
        return super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    public void setOutputFacing(EnumFacing outputFacing) {
        this.outputFacingItems = outputFacing;
        this.outputFacingFluids = outputFacing;
        if (!getWorld().isRemote) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_OUTPUT_FACING, buf -> {
                buf.writeByte(outputFacingItems.getIndex());
                buf.writeByte(outputFacingFluids.getIndex());
            });
            markDirty();
        }
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.outputFacingItems == null || this.outputFacingFluids == null) {
            setOutputFacing(frontFacing.getOpposite());
        }
    }

    public EnumFacing getOutputFacingItems() {
        return outputFacingItems == null ? getFrontFacing().getOpposite() : outputFacingItems;
    }

    public EnumFacing getOutputFacingFluids() {
        return outputFacingFluids == null ? getFrontFacing().getOpposite() : outputFacingFluids;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (getOffsetTimer() % 5 == 0) {
                if (isAutoOutputFluids()) {
                    pushFluidsIntoNearbyHandlers(getOutputFacingFluids());
                }
                if (isAutoOutputItems()) {
                    pushItemsIntoNearbyHandlers(getOutputFacingItems());
                }
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.VOLTAGE_CASINGS[tier].render(renderState, translation, ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))));
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing == getOutputFacingItems()) {
                Textures.PIPE_OUT_OVERLAY.renderSided(facing, renderState, translation, pipeline);
            } else {
                Textures.BUFFER_OVERLAY.renderSided(facing, renderState, translation, pipeline);
            }
        }
        if (isAutoOutputItems() && outputFacingItems != null) {
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(outputFacingItems, renderState,
                    RenderUtil.adjustTrans(translation, outputFacingItems, 2), pipeline);
        }
        if (isAutoOutputFluids() && outputFacingFluids != null) {
            Textures.FLUID_OUTPUT_OVERLAY.renderSided(outputFacingFluids, renderState,
                    RenderUtil.adjustTrans(translation, outputFacingFluids, 2), pipeline);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("Inventory", itemStackHandler.serializeNBT());
        tag.setTag("FluidInventory", fluidTankList.serializeNBT());
        tag.setInteger("OutputFacing", getOutputFacingItems().getIndex());
        tag.setInteger("OutputFacingF", getOutputFacingFluids().getIndex());
        tag.setBoolean("AutoOutputItems", autoOutputItems);
        tag.setBoolean("AutoOutputFluids", autoOutputFluids);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.itemStackHandler.deserializeNBT(tag.getCompoundTag("Inventory"));
        this.fluidTankList.deserializeNBT(tag.getCompoundTag("FluidInventory"));
        this.outputFacingItems = EnumFacing.VALUES[tag.getInteger("OutputFacing")];
        this.outputFacingFluids = EnumFacing.VALUES[tag.getInteger("OutputFacingF")];
        this.autoOutputItems = tag.getBoolean("AutoOutputItems");
        this.autoOutputFluids = tag.getBoolean("AutoOutputFluids");
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(autoOutputFluids);
        buf.writeBoolean(autoOutputItems);
        buf.writeByte(getOutputFacingFluids().getIndex());
        buf.writeByte(getOutputFacingItems().getIndex());
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.autoOutputFluids = buf.readBoolean();
        this.autoOutputItems = buf.readBoolean();
        this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
        this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_OUTPUT_FACING) {
            this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
            this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_ITEMS) {
            this.autoOutputItems = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_FLUIDS) {
            this.autoOutputFluids = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return true;
    }

    @Override
    public boolean hasFrontFacing() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.buffer.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", (int) Math.pow(tier + 2, 2)));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity_mult", tier + 2,
                (int) (1000 * Math.pow(2, tier))));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        clearInventory(itemBuffer, itemStackHandler);
    }
}
