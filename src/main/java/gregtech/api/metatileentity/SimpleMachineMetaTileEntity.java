package gregtech.api.metatileentity;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.ItemHandlerProxy;
import gregtech.api.cover.Cover;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.GhostCircuitSlotWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.particle.IMachineParticleEffect;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.RenderUtil;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static gregtech.api.capability.GregtechDataCodes.*;

public class SimpleMachineMetaTileEntity extends WorkableTieredMetaTileEntity
                                         implements IActiveOutputSide, IGhostSlotConfigurable {

    private final boolean hasFrontFacing;

    protected final GTItemStackHandler chargerInventory;
    @Nullable
    protected GhostCircuitItemStackHandler circuitInventory;
    private EnumFacing outputFacingItems;
    private EnumFacing outputFacingFluids;

    private boolean autoOutputItems;
    private boolean autoOutputFluids;
    private boolean allowInputFromOutputSideItems = false;
    private boolean allowInputFromOutputSideFluids = false;

    protected IItemHandler outputItemInventory;
    protected IFluidHandler outputFluidInventory;

    private IItemHandlerModifiable actualImportItems;

    private static final int FONT_HEIGHT = 9; // Minecraft's FontRenderer FONT_HEIGHT value

    @Nullable // particle run every tick when the machine is active
    protected final IMachineParticleEffect tickingParticle;
    @Nullable // particle run in randomDisplayTick() when the machine is active
    protected final IMachineParticleEffect randomParticle;

    public SimpleMachineMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                       ICubeRenderer renderer, int tier, boolean hasFrontFacing) {
        this(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, GTUtility.defaultTankSizeFunction);
    }

    public SimpleMachineMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                       ICubeRenderer renderer, int tier, boolean hasFrontFacing,
                                       Function<Integer, Integer> tankScalingFunction) {
        this(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, tankScalingFunction, null, null);
    }

    public SimpleMachineMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                       ICubeRenderer renderer, int tier, boolean hasFrontFacing,
                                       Function<Integer, Integer> tankScalingFunction,
                                       @Nullable IMachineParticleEffect tickingParticle,
                                       @Nullable IMachineParticleEffect randomParticle) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction);
        this.hasFrontFacing = hasFrontFacing;
        this.chargerInventory = new GTItemStackHandler(this, 1);
        this.tickingParticle = tickingParticle;
        this.randomParticle = randomParticle;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SimpleMachineMetaTileEntity(metaTileEntityId, workable.getRecipeMap(), renderer, getTier(),
                hasFrontFacing, getTankScalingFunction(), tickingParticle, randomParticle);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.outputItemInventory = new ItemHandlerProxy(new GTItemStackHandler(this, 0), exportItems);
        this.outputFluidInventory = new FluidHandlerProxy(new FluidTankList(false), exportFluids);
        if (this.hasGhostCircuitInventory()) {
            this.circuitInventory = new GhostCircuitItemStackHandler(this);
            this.circuitInventory.addNotifiableMetaTileEntity(this);
        }

        this.actualImportItems = null;
    }

    @Override
    public IItemHandlerModifiable getImportItems() {
        if (this.actualImportItems == null) {
            this.actualImportItems = this.circuitInventory == null ?
                    super.getImportItems() :
                    new ItemHandlerList(Arrays.asList(super.getImportItems(), this.circuitInventory));
        }
        return this.actualImportItems;
    }

    @Override
    public boolean hasFrontFacing() {
        return hasFrontFacing;
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                 CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            // TODO Separate into two output getters
            if (getOutputFacing() == facing) return false;
            if (hasFrontFacing() && facing == getFrontFacing()) return false;
            if (!getWorld().isRemote) {
                // TODO Separate into two output setters
                setOutputFacing(facing);
            }
            return true;
        }
        return super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public void addCover(@NotNull EnumFacing side, @NotNull Cover cover) {
        super.addCover(side, cover);
        if (cover.canInteractWithOutputSide()) {
            if (getOutputFacingItems() == side) {
                setAllowInputFromOutputSideItems(true);
            }
            if (getOutputFacingFluids() == side) {
                setAllowInputFromOutputSideFluids(true);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (outputFacingFluids != null && getExportFluids().size() > 0) {
            Textures.PIPE_OUT_OVERLAY.renderSided(outputFacingFluids, renderState,
                    RenderUtil.adjustTrans(translation, outputFacingFluids, 2), pipeline);
        }
        if (outputFacingItems != null && getExportItems().getSlots() > 0) {
            Textures.PIPE_OUT_OVERLAY.renderSided(outputFacingItems, renderState,
                    RenderUtil.adjustTrans(translation, outputFacingItems, 2), pipeline);
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
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            ((EnergyContainerHandler) this.energyContainer).dischargeOrRechargeEnergyContainers(chargerInventory, 0);

            if (getOffsetTimer() % 5 == 0) {
                if (isAutoOutputFluids()) {
                    pushFluidsIntoNearbyHandlers(getOutputFacingFluids());
                }
                if (isAutoOutputItems()) {
                    pushItemsIntoNearbyHandlers(getOutputFacingItems());
                }
            }
        } else if (this.tickingParticle != null && isActive()) {
            tickingParticle.runEffect(this);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick() {
        if (this.randomParticle != null && isActive()) {
            randomParticle.runEffect(this);
        }
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            if (isAllowInputFromOutputSideItems()) {
                setAllowInputFromOutputSideItems(false);
                setAllowInputFromOutputSideFluids(false);
                playerIn.sendStatusMessage(
                        new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.disallow"), true);
            } else {
                setAllowInputFromOutputSideItems(true);
                setAllowInputFromOutputSideFluids(true);
                playerIn.sendStatusMessage(
                        new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.allow"), true);
            }
        }
        return true;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler fluidHandler = (side == getOutputFacingFluids() && !isAllowInputFromOutputSideFluids()) ?
                    outputFluidInventory : fluidInventory;
            if (fluidHandler.getTankProperties().length > 0) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
            }
            return null;
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            IItemHandler itemHandler = (side == getOutputFacingItems() && !isAllowInputFromOutputSideFluids()) ?
                    outputItemInventory : itemInventory;
            if (itemHandler.getSlots() > 0) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
            }
            return null;
        } else if (capability == GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE) {
            if (side == getOutputFacingItems() || side == getOutputFacingFluids()) {
                return GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE.cast(this);
            }
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("ChargerInventory", chargerInventory.serializeNBT());
        if (this.circuitInventory != null) {
            this.circuitInventory.write(data);
        }
        data.setInteger("OutputFacing", getOutputFacingItems().getIndex());
        data.setInteger("OutputFacingF", getOutputFacingFluids().getIndex());
        data.setBoolean("AutoOutputItems", autoOutputItems);
        data.setBoolean("AutoOutputFluids", autoOutputFluids);
        data.setBoolean("AllowInputFromOutputSide", allowInputFromOutputSideItems);
        data.setBoolean("AllowInputFromOutputSideF", allowInputFromOutputSideFluids);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.chargerInventory.deserializeNBT(data.getCompoundTag("ChargerInventory"));
        if (this.circuitInventory != null) {
            if (data.hasKey("CircuitInventory", Constants.NBT.TAG_COMPOUND)) {
                // legacy save support - move items in circuit inventory to importItems inventory, if possible
                ItemStackHandler legacyCircuitInventory = new ItemStackHandler();
                legacyCircuitInventory.deserializeNBT(data.getCompoundTag("CircuitInventory"));
                for (int i = 0; i < legacyCircuitInventory.getSlots(); i++) {
                    ItemStack stack = legacyCircuitInventory.getStackInSlot(i);
                    if (stack.isEmpty()) continue;
                    stack = GTTransferUtils.insertItem(this.importItems, stack, false);
                    // If there's no space left in importItems, just set it as ghost circuit and void the item
                    this.circuitInventory.setCircuitValueFromStack(stack);
                }
            } else {
                this.circuitInventory.read(data);
            }
        }
        this.outputFacingItems = EnumFacing.VALUES[data.getInteger("OutputFacing")];
        this.outputFacingFluids = EnumFacing.VALUES[data.getInteger("OutputFacingF")];
        this.autoOutputItems = data.getBoolean("AutoOutputItems");
        this.autoOutputFluids = data.getBoolean("AutoOutputFluids");
        this.allowInputFromOutputSideItems = data.getBoolean("AllowInputFromOutputSide");
        this.allowInputFromOutputSideFluids = data.getBoolean("AllowInputFromOutputSideF");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(getOutputFacingItems().getIndex());
        buf.writeByte(getOutputFacingFluids().getIndex());
        buf.writeBoolean(autoOutputItems);
        buf.writeBoolean(autoOutputFluids);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
        this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
        this.autoOutputItems = buf.readBoolean();
        this.autoOutputFluids = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
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
    public boolean isValidFrontFacing(EnumFacing facing) {
        // use direct outputFacing field instead of getter method because otherwise
        // it will just return SOUTH for null output facing
        return super.isValidFrontFacing(facing) && facing != outputFacingItems && facing != outputFacingFluids;
    }

    @Deprecated
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

    public void setOutputFacingItems(EnumFacing outputFacing) {
        this.outputFacingItems = outputFacing;
        if (!getWorld().isRemote) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_OUTPUT_FACING, buf -> {
                buf.writeByte(outputFacingItems.getIndex());
                buf.writeByte(outputFacingFluids.getIndex());
            });
            markDirty();
        }
    }

    public void setOutputFacingFluids(EnumFacing outputFacing) {
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

    public void setAllowInputFromOutputSideItems(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSideItems = allowInputFromOutputSide;
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    public void setAllowInputFromOutputSideFluids(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSideFluids = allowInputFromOutputSide;
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    @Override
    public void setGhostCircuitConfig(int config) {
        if (this.circuitInventory == null || this.circuitInventory.getCircuitValue() == config) {
            return;
        }
        this.circuitInventory.setCircuitValue(config);
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.outputFacingItems == null || this.outputFacingFluids == null) {
            // set initial output facing as opposite to front
            setOutputFacing(frontFacing.getOpposite());
        }
    }

    @Deprecated
    public EnumFacing getOutputFacing() {
        return getOutputFacingItems();
    }

    public EnumFacing getOutputFacingItems() {
        return outputFacingItems == null ? EnumFacing.SOUTH : outputFacingItems;
    }

    public EnumFacing getOutputFacingFluids() {
        return outputFacingFluids == null ? EnumFacing.SOUTH : outputFacingFluids;
    }

    public boolean isAutoOutputItems() {
        return autoOutputItems;
    }

    public boolean isAutoOutputFluids() {
        return autoOutputFluids;
    }

    public boolean isAllowInputFromOutputSideItems() {
        return allowInputFromOutputSideItems;
    }

    public boolean isAllowInputFromOutputSideFluids() {
        return allowInputFromOutputSideFluids;
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, chargerInventory);
    }

    protected ModularUI.Builder createGuiTemplate(EntityPlayer player) {
        RecipeMap<?> workableRecipeMap = workable.getRecipeMap();
        int yOffset = 0;
        if (workableRecipeMap.getMaxInputs() >= 6 || workableRecipeMap.getMaxFluidInputs() >= 6 ||
                workableRecipeMap.getMaxOutputs() >= 6 || workableRecipeMap.getMaxFluidOutputs() >= 6) {
            yOffset = FONT_HEIGHT;
        }

        ModularUI.Builder builder = workableRecipeMap.getRecipeMapUI()
                .createUITemplate(workable::getProgressPercent, importItems, exportItems, importFluids, exportFluids,
                        yOffset)
                .widget(new LabelWidget(5, 5, getMetaFullName()))
                .widget(new SlotWidget(chargerInventory, 0, 79, 62 + yOffset, true, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY)
                        .setTooltipText("gregtech.gui.charger_slot.tooltip", GTValues.VNF[getTier()],
                                GTValues.VNF[getTier()]))
                .widget(new ImageWidget(79, 42 + yOffset, 18, 18, GuiTextures.INDICATOR_NO_ENERGY).setIgnoreColor(true)
                        .setPredicate(workable::isHasNotEnoughEnergy))
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, yOffset);

        int leftButtonStartX = 7;

        if (exportItems.getSlots() > 0) {
            builder.widget(new ToggleButtonWidget(leftButtonStartX, 62 + yOffset, 18, 18,
                    GuiTextures.BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setAutoOutputItems)
                            .setTooltipText("gregtech.gui.item_auto_output.tooltip")
                            .shouldUseBaseBackground());
            leftButtonStartX += 18;
        }
        if (exportFluids.size() > 0) {
            builder.widget(new ToggleButtonWidget(leftButtonStartX, 62 + yOffset, 18, 18,
                    GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)
                            .setTooltipText("gregtech.gui.fluid_auto_output.tooltip")
                            .shouldUseBaseBackground());
            leftButtonStartX += 18;
        }

        builder.widget(new CycleButtonWidget(leftButtonStartX, 62 + yOffset, 18, 18,
                workable.getAvailableOverclockingTiers(), workable::getOverclockTier, workable::setOverclockTier)
                        .setTooltipHoverString("gregtech.gui.overclock.description")
                        .setButtonTexture(GuiTextures.BUTTON_OVERCLOCK));

        if (exportItems.getSlots() + exportFluids.size() <= 9) {
            ImageWidget logo = new ImageWidget(152, 63 + yOffset, 17, 17,
                    GTValues.XMAS.get() ? getXmasLogo() : getLogo()).setIgnoreColor(true);

            if (this.circuitInventory != null) {
                SlotWidget circuitSlot = new GhostCircuitSlotWidget(circuitInventory, 0, 124, 62 + yOffset)
                        .setBackgroundTexture(GuiTextures.SLOT, getCircuitSlotOverlay());
                builder.widget(circuitSlot.setConsumer(this::getCircuitSlotTooltip)).widget(logo);
            }
        }
        return builder;
    }

    protected @NotNull TextureArea getLogo() {
        return GuiTextures.GREGTECH_LOGO;
    }

    protected @NotNull TextureArea getXmasLogo() {
        return GuiTextures.GREGTECH_LOGO_XMAS;
    }

    @Override
    public boolean hasGhostCircuitInventory() {
        return true;
    }

    // Method provided to override
    protected TextureArea getCircuitSlotOverlay() {
        return GuiTextures.INT_CIRCUIT_OVERLAY;
    }

    // Method provided to override
    protected void getCircuitSlotTooltip(SlotWidget widget) {
        String configString;
        if (circuitInventory == null || circuitInventory.getCircuitValue() == GhostCircuitItemStackHandler.NO_CONFIG) {
            configString = new TextComponentTranslation("gregtech.gui.configurator_slot.no_value").getFormattedText();
        } else {
            configString = String.valueOf(circuitInventory.getCircuitValue());
        }

        widget.setTooltipText("gregtech.gui.configurator_slot.tooltip", configString);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createGuiTemplate(entityPlayer).build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        String key = this.metaTileEntityId.getPath().split("\\.")[0];
        String mainKey = String.format("gregtech.machine.%s.tooltip", key);
        if (I18n.hasKey(mainKey)) {
            tooltip.add(1, mainKey);
        }
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.reset"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
