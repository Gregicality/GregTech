package gregtech.common.metatileentities.storage;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.IFilter;
import gregtech.api.capability.IFilteredFluidContainer;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.GTFluidHandlerItemStack;
import gregtech.api.cover.CoverRayTracer;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.custom.QuantumStorageRenderer;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.*;
import static net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack.FLUID_NBT_KEY;

public class MetaTileEntityQuantumTank extends MetaTileEntityQuantumStorage<IFluidTank>
                                       implements ITieredMetaTileEntity, IActiveOutputSide, IFastRenderMetaTileEntity {

    private final int tier;
    private final int maxFluidCapacity;
    protected FluidTank fluidTank;
    protected IFluidHandler outputFluidInventory;

    @Nullable
    protected FluidStack previousFluid;
    @Nullable
    private FluidStack lockedFluid;

    public MetaTileEntityQuantumTank(ResourceLocation metaTileEntityId, int tier, int maxFluidCapacity) {
        super(metaTileEntityId);
        this.tier = tier;
        this.maxFluidCapacity = maxFluidCapacity;
        initializeInventory();
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.fluidTank = new QuantumFluidTank(maxFluidCapacity);
        this.fluidInventory = fluidTank;
        this.importFluids = new FluidTankList(false, fluidTank);
        this.exportFluids = new FluidTankList(false, fluidTank);
        this.outputFluidInventory = new FluidHandlerProxy(new FluidTankList(false), exportFluids);
    }

    @Override
    public void update() {
        super.update();
        EnumFacing currentOutputFacing = getOutputFacing();
        if (!getWorld().isRemote) {
            fillContainerFromInternalTank();
            fillInternalTankFromFluidContainer();
            if (isAutoOutputFluids()) {
                pushFluidsIntoNearbyHandlers(currentOutputFacing);
            }

            FluidStack currentFluid = fluidTank.getFluid();
            if (previousFluid == null) {
                // tank was empty, but now is not
                if (currentFluid != null) {
                    updatePreviousFluid(currentFluid);
                }
            } else {
                if (currentFluid == null) {
                    // tank had fluid, but now is empty
                    updatePreviousFluid(null);
                } else if (previousFluid.getFluid().equals(currentFluid.getFluid()) &&
                        previousFluid.amount != currentFluid.amount) {
                            int currentFill = MathHelper
                                    .floor(16 * ((float) currentFluid.amount) / fluidTank.getCapacity());
                            int previousFill = MathHelper
                                    .floor(16 * ((float) previousFluid.amount) / fluidTank.getCapacity());
                            // tank has fluid with changed amount
                            previousFluid.amount = currentFluid.amount;
                            writeCustomData(UPDATE_FLUID_AMOUNT, buf -> {
                                buf.writeInt(currentFluid.amount);
                                buf.writeBoolean(currentFill != previousFill);
                            });

                        } else
                    if (!previousFluid.equals(currentFluid)) {
                        // tank has a different fluid from before
                        updatePreviousFluid(currentFluid);
                    }
            }
        }
    }

    // should only be called on the server
    protected void updatePreviousFluid(FluidStack currentFluid) {
        previousFluid = currentFluid == null ? null : currentFluid.copy();
        writeCustomData(UPDATE_FLUID, buf -> buf
                .writeCompoundTag(currentFluid == null ? null : currentFluid.writeToNBT(new NBTTagCompound())));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("FluidInventory", fluidTank.writeToNBT(new NBTTagCompound()));
        if (locked && lockedFluid != null) {
            data.setTag("LockedFluid", lockedFluid.writeToNBT(new NBTTagCompound()));
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("ContainerInventory")) {
            legacyTankItemHandlerNBTReading(this, data.getCompoundTag("ContainerInventory"), 0, 1);
        }
        this.fluidTank.readFromNBT(data.getCompoundTag("FluidInventory"));
        this.lockedFluid = this.locked ? FluidStack.loadFluidStackFromNBT(data.getCompoundTag("LockedFluid")) : null;
    }

    public static void legacyTankItemHandlerNBTReading(MetaTileEntity mte, NBTTagCompound nbt, int inputSlot,
                                                       int outputSlot) {
        if (mte == null || nbt == null) {
            return;
        }
        NBTTagList items = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        if (mte.getExportItems().getSlots() < 1 || mte.getImportItems().getSlots() < 1 || inputSlot < 0 ||
                outputSlot < 0 || inputSlot == outputSlot) {
            return;
        }
        for (int i = 0; i < items.tagCount(); ++i) {
            NBTTagCompound itemTags = items.getCompoundTagAt(i);
            int slot = itemTags.getInteger("Slot");
            if (slot == inputSlot) {
                mte.getImportItems().setStackInSlot(0, new ItemStack(itemTags));
            } else if (slot == outputSlot) {
                mte.getExportItems().setStackInSlot(0, new ItemStack(itemTags));
            }
        }
    }

    @Override
    public void initFromItemStackData(NBTTagCompound tag) {
        super.initFromItemStackData(tag);
        if (tag.hasKey(FLUID_NBT_KEY, Constants.NBT.TAG_COMPOUND)) {
            this.fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(tag.getCompoundTag(FLUID_NBT_KEY)));
        }
        if (tag.getBoolean("IsVoiding") || tag.getBoolean("IsPartialVoiding")) { // legacy save support
            setVoiding(true);
        }

        this.lockedFluid = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("LockedFluid"));
        this.locked = this.lockedFluid != null;
    }

    @Override
    public void writeItemStackData(NBTTagCompound tag) {
        super.writeItemStackData(tag);
        FluidStack stack = this.fluidTank.getFluid();
        if (stack != null && stack.amount > 0) {
            tag.setTag(FLUID_NBT_KEY, stack.writeToNBT(new NBTTagCompound()));
        }

        if (this.voiding) {
            tag.setBoolean("IsVoiding", true);
        }

        if (this.locked && this.lockedFluid != null) {
            tag.setTag("LockedFluid", this.lockedFluid.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityQuantumTank(metaTileEntityId, tier, maxFluidCapacity);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, fluidTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, fluidTank);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new FilteredItemHandler(this, 1).setFillPredicate(
                FilteredItemHandler.getCapabilityFilter(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, 1);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.QUANTUM_STORAGE_RENDERER.renderMachine(renderState, translation,
                ArrayUtils.add(pipeline,
                        new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))),
                this);
        Textures.QUANTUM_TANK_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        if (outputFacing != null) {
            Textures.PIPE_OUT_OVERLAY.renderSided(outputFacing, renderState, translation, pipeline);
            if (isAutoOutputFluids()) {
                Textures.FLUID_OUTPUT_OVERLAY.renderSided(outputFacing, renderState, translation, pipeline);
            }
        }
        QuantumStorageRenderer.renderTankFluid(renderState, translation, pipeline, fluidTank, getWorld(), getPos(),
                getFrontFacing());
        renderIndicatorOverlay(renderState, translation, pipeline);
    }

    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (this.fluidTank.getFluid() == null || this.fluidTank.getFluid().amount == 0)
            return;

        QuantumStorageRenderer.renderTankAmount(x, y, z, this.getFrontFacing(), this.fluidTank.getFluid().amount);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[tier].getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.quantum_tank.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", maxFluidCapacity));
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            if (tag.hasKey(FLUID_NBT_KEY, Constants.NBT.TAG_COMPOUND)) {
                FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag(FLUID_NBT_KEY));
                if (fluidStack != null) {
                    tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_stored", fluidStack.getLocalizedName(),
                            fluidStack.amount));
                }
            }
            if (tag.getBoolean("IsVoiding") || tag.getBoolean("IsPartialVoiding")) { // legacy save support
                tooltip.add(I18n.format("gregtech.machine.quantum_tank.tooltip.voiding_enabled"));
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
    protected void createWidgets(ModularPanel mainPanel, PanelSyncManager syncManager) {
        mainPanel.child(createQuantumDisplay("gregtech.gui.fluid_amount",
                () -> {
                    var f = fluidTank.getFluid();
                    if (f == null) return "";
                    return IKey.lang(f.getUnlocalizedName()).get();
                },
                textWidget -> fluidTank.getFluid() != null,
                () -> TextFormattingUtil.formatNumbers(fluidTank.getFluidAmount()) + " L"))
                .child(new GTFluidSlot()
                        .syncHandler(GTFluidSlot.sync(fluidTank)
                                .handleLocking(() -> lockedFluid, fluidStack -> {
                                    setLocked(fluidStack != null);
                                    lockedFluid = fluidStack;
                                }, this::setLocked)
                                .showAmount(true))
                        .pos(148, 41));
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        if (frontFacing == EnumFacing.UP) {
            if (this.outputFacing == null || this.outputFacing == EnumFacing.DOWN) {
                super.setFrontFacing(EnumFacing.NORTH);
            } else {
                super.setFrontFacing(outputFacing.getOpposite());
            }
        } else {
            super.setFrontFacing(frontFacing);
        }
        if (this.outputFacing == null) {
            // set initial output facing as opposite to front
            setOutputFacing(frontFacing.getOpposite());
        }
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_FLUID) {
            try {
                this.fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(buf.readCompoundTag()));
            } catch (IOException ignored) {
                GTLog.logger.warn("Failed to load fluid from NBT in a quantum tank at {} on a routine fluid update",
                        this.getPos());
            }
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_FLUID_AMOUNT) {
            // amount must always be read even if it cannot be used to ensure the reader index advances
            int amount = buf.readInt();
            boolean updateRendering = buf.readBoolean();
            FluidStack stack = fluidTank.getFluid();
            if (stack != null) {
                stack.amount = Math.min(amount, fluidTank.getCapacity());
                if (updateRendering)
                    scheduleRenderUpdate();
            }
        }
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return super.isValidFrontFacing(facing) && facing != outputFacing;
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        NetworkUtils.writeFluidStack(buf, fluidTank.getFluid());
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);

        if (this.frontFacing == EnumFacing.UP) {
            if (this.outputFacing != EnumFacing.DOWN) {
                this.frontFacing = this.outputFacing.getOpposite();
            } else {
                this.frontFacing = EnumFacing.NORTH;
            }
        }
        this.fluidTank.setFluid(NetworkUtils.readFluidStack(buf));
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE) {
            if (side == getOutputFacing()) {
                return GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE.cast(this);
            }
            return null;
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler fluidHandler = (side == getOutputFacing() && !isAllowInputFromOutputSideFluids()) ?
                    outputFluidInventory : fluidInventory;
            if (fluidHandler.getTankProperties().length > 0) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
            }

            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    public ICapabilityProvider initItemStackCapabilities(ItemStack itemStack) {
        return new GTFluidHandlerItemStack(itemStack, maxFluidCapacity);
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
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        EnumFacing hitFacing = CoverRayTracer.determineGridSideHit(hitResult);
        if (facing == getOutputFacing() || (hitFacing == getOutputFacing() && playerIn.isSneaking())) {
            if (!getWorld().isRemote) {
                if (isAllowInputFromOutputSideFluids()) {
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

    @Override
    protected void setLocked(boolean locked) {
        super.setLocked(locked);
        if (locked && fluidTank.getFluid() != null) {
            this.lockedFluid = fluidTank.getFluid().copy();
            this.lockedFluid.amount = 1;
        } else this.lockedFluid = null;
    }

    @Override
    public ItemStack getPickItem(EntityPlayer player) {
        if (!player.isCreative()) return super.getPickItem(player);

        ItemStack baseItemStack = getStackForm();
        NBTTagCompound tag = new NBTTagCompound();

        this.writeItemStackData(tag);
        if (!tag.isEmpty()) {
            baseItemStack.setTagCompound(tag);
        }
        return baseItemStack;
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos());
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }

    private class QuantumFluidTank extends FluidTank implements IFilteredFluidContainer, IFilter<FluidStack> {

        public QuantumFluidTank(int capacity) {
            super(capacity);
        }

        @Override
        public int fillInternal(FluidStack resource, boolean doFill) {
            int accepted = super.fillInternal(resource, doFill);

            // if we couldn't accept "resource", and "resource" is not the same as the stored fluid.
            if (accepted == 0 && !resource.isFluidEqual(getFluid())) {
                return 0;
            }

            if (doFill && locked && lockedFluid == null) {
                lockedFluid = resource.copy();
                lockedFluid.amount = 1;
            }
            return voiding ? resource.amount : accepted;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return test(fluid);
        }

        @Override
        public IFilter<FluidStack> getFilter() {
            return this;
        }

        @Override
        public boolean test(@NotNull FluidStack fluidStack) {
            return !locked || lockedFluid == null || fluidStack.isFluidEqual(lockedFluid);
        }

        @Override
        public int getPriority() {
            return !locked || lockedFluid == null ? IFilter.noPriority() : IFilter.whitelistPriority(1);
        }
    }

    @Override
    public Type getType() {
        return Type.FLUID;
    }

    @Override
    public IFluidTank getTypeValue() {
        return fluidTank;
    }
}
