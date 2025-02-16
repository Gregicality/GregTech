package gregtech.common.metatileentities.storage;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.IQuantumController;
import gregtech.api.capability.IQuantumStorage;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.handler.BlockPosHighlightRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.utils.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static gregtech.api.capability.GregtechDataCodes.*;

public abstract class MetaTileEntityQuantumStorage<T> extends MetaTileEntity implements IQuantumStorage<T>,
                                                  IActiveOutputSide {

    /** not synced, server only. lazily initialized from pos */
    private WeakReference<IQuantumController> controller = new WeakReference<>(null);

    /** synced, server and client */
    private BlockPos controllerPos;
    protected static final String IS_VOIDING = "IsVoiding";
    protected static final String INPUT_FROM_OUTPUT = "AllowInputFromOutputSide";
    protected static final String INPUT_FROM_OUTPUT_FLUID = "AllowInputFromOutputSideF";

    @Deprecated
    private ClickButtonWidget connectedIcon;
    protected EnumFacing outputFacing;
    protected boolean voiding = false;
    protected boolean autoOutput;
    protected boolean allowInputFromOutputSide = false;
    protected boolean locked = false;

    public MetaTileEntityQuantumStorage(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @SuppressWarnings("DataFlowIssue")
    @SideOnly(Side.CLIENT)
    protected void renderIndicatorOverlay(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        SimpleOverlayRenderer texture;
        if (isConnected()) {
            texture = getQuantumController().isPowered() ?
                    Textures.QUANTUM_INDICATOR_POWERED :
                    Textures.QUANTUM_INDICATOR_CONNECTED;
        } else {
            texture = Textures.QUANTUM_INDICATOR;
        }
        texture.renderSided(getFrontFacing(), renderState, RenderUtil.adjustTrans(translation, getFrontFacing(), 1),
                pipeline);
    }

    @Override
    public void setConnected(IQuantumController controller) {
        if (getWorld().isRemote) return;

        if (!controller.getPos().equals(controllerPos)) {
            this.controller = new WeakReference<>(controller);
            this.controllerPos = controller.getPos();
            writeCustomData(GregtechDataCodes.UPDATE_CONTROLLER_POS, buf -> buf.writeBlockPos(controllerPos));
            markDirty();
        }
    }

    @Override
    public void setDisconnected() {
        if (getWorld().isRemote) return;

        controller.clear();
        controllerPos = null;
        writeCustomData(GregtechDataCodes.REMOVE_CONTROLLER, buf -> {});
        markDirty();
    }

    // use this to make sure controller is properly initialized
    @Override
    public final IQuantumController getQuantumController() {
        if (isConnected()) {
            if (controller.get() != null) return controller.get();
            MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), controllerPos);
            if (mte instanceof IQuantumController quantumController) {
                controller = new WeakReference<>(quantumController);
                return quantumController;
            } else {
                // controller is no longer there for some reason, need to disconnect
                setDisconnected();
                tryFindNetwork();
            }
        }
        return null;
    }

    @Override
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    @Override
    public void onRemoval() {
        if (!getWorld().isRemote && isConnected()) {
            IQuantumController controller = getQuantumController();
            if (controller != null) controller.rebuildNetwork();
        }
    }

    @Override
    public void onPlacement(@Nullable EntityLivingBase placer) {
        super.onPlacement(placer);
        if (getWorld() == null || getWorld().isRemote)
            return;

        // add to the network if an adjacent block is part of a network
        // use whatever we find first, merging networks is not supported
        tryFindNetwork();
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        var panel = GTGuis.createPanel(this, 176, 166);
        createWidgets(panel, guiSyncManager);
        return panel.padding(4)
                .child(IKey.lang(getMetaFullName()).asWidget())
                .child(createQuantumIO(importItems, exportItems))
                .child(createQuantumButtonRow())
                .child(SlotGroupWidget.playerInventory().left(7));
    }

    protected void createWidgets(ModularPanel mainPanel, PanelSyncManager syncManager) {}

    public Flow createQuantumDisplay(String lang,
                                     Supplier<String> name, Predicate<TextWidget> condition,
                                     Supplier<String> count) {
        return Flow.column()
                .background(GTGuiTextures.DISPLAY)
                .padding(4)
                .height(46)
                .top(16)
                .child(IKey.lang(lang)
                        .alignment(Alignment.TopLeft)
                        .color(Color.WHITE.main)
                        .asWidget()
                        .widthRel(0.5f)
                        .left(4)
                        .marginBottom(2))
                .child(IKey.dynamic(name)
                        .alignment(Alignment.TopLeft)
                        .color(Color.WHITE.main)
                        .asWidget()
                        .setEnabledIf(condition)
                        .widthRel(0.5f)
                        .left(4)
                        .height(20)
                        .marginBottom(2))
                .child(IKey.dynamic(count)
                        .alignment(Alignment.TopLeft)
                        .color(Color.WHITE.main)
                        .asWidget()
                        .widthRel(0.5f)
                        .left(4));
    }

    public ParentWidget<?> createQuantumIO(IItemHandlerModifiable importHandler,
                                           IItemHandlerModifiable exportHandler) {
        return Flow.row()
                .pos(79, 18 + 45)
                .coverChildren()
                .child(new ItemSlot()
                        .background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
                        .slot(SyncHandlers.itemSlot(importHandler, 0)
                                .accessibility(true, true)
                                .singletonSlotGroup(200))
                        .marginRight(18))
                .child(new ItemSlot()
                        .background(GTGuiTextures.SLOT, GTGuiTextures.OUT_SLOT_OVERLAY)
                        .slot(SyncHandlers.itemSlot(exportHandler, 0)
                                .accessibility(false, true)));
    }

    public Flow createQuantumButtonRow() {
        boolean isFluid = getType() == Type.FLUID;
        BooleanSupplier getter = isFluid ? this::isAutoOutputFluids : this::isAutoOutputItems;

        return Flow.row()
                .coverChildren()
                .pos(7, 63)
                .child(new ToggleButton()
                        .overlay(isFluid ? GTGuiTextures.BUTTON_FLUID_OUTPUT : GTGuiTextures.BUTTON_ITEM_OUTPUT)
                        .value(new BooleanSyncValue(getter, this::setAutoOutput)))
                .child(new ToggleButton()
                        .overlay(GTGuiTextures.FLUID_LOCK_OVERLAY)
                        .value(new BooleanSyncValue(this::isLocked, this::setLocked)))
                .child(new ToggleButton()
                        .overlay(isFluid ? GTGuiTextures.FLUID_VOID_OVERLAY : GTGuiTextures.ITEM_VOID_OVERLAY)
                        .value(new BooleanSyncValue(this::isVoiding, this::setVoiding)));
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

    protected boolean isLocked() {
        return this.locked;
    }

    protected void setLocked(boolean locked) {
        if (this.locked == locked) return;
        this.locked = locked;
        if (!getWorld().isRemote) {
            markDirty();
        }
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

    public boolean isAutoOutputItems() {
        return getType() == Type.ITEM && autoOutput;
    }

    public boolean isAutoOutputFluids() {
        return getType() == Type.FLUID && autoOutput;
    }

    public void setAutoOutput(boolean autoOutput) {
        this.autoOutput = autoOutput;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_OUTPUT_ITEMS, buf -> buf.writeBoolean(autoOutput));
            markDirty();
        }
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return getType() == Type.ITEM && allowInputFromOutputSide;
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return getType() == Type.FLUID && allowInputFromOutputSide;
    }

    public void setAllowInputFromOutputSide(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSide = allowInputFromOutputSide;
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(controllerPos != null);
        if (controllerPos != null) {
            buf.writeBlockPos(controllerPos);
        }
        buf.writeByte(getOutputFacing().getIndex());
        buf.writeBoolean(autoOutput);
        buf.writeBoolean(voiding);
        buf.writeBoolean(locked);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            controllerPos = buf.readBlockPos();
            scheduleRenderUpdate();
        }
        this.outputFacing = EnumFacing.VALUES[buf.readByte()];
        this.autoOutput = buf.readBoolean();
        this.voiding = buf.readBoolean();
        this.locked = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_CONTROLLER_POS) {
            this.controllerPos = buf.readBlockPos();
            this.controller.clear();

            if (this.connectedIcon != null) {
                this.connectedIcon.setButtonTexture(GuiTextures.GREGTECH_LOGO);
                this.connectedIcon.setTooltipText("gregtech.machine.quantum_storage.connected",
                        controllerPos.getX(), controllerPos.getZ(), controllerPos.getY());
            }
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.REMOVE_CONTROLLER) {
            this.controllerPos = null;
            this.controller.clear();
            if (this.connectedIcon != null) {
                this.connectedIcon.setButtonTexture(GuiTextures.GREGTECH_LOGO_DARK);
                this.connectedIcon.setTooltipText("gregtech.machine.quantum_storage.disconnected");
            }
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.LOCATE_CONTROLLER) {
            // tell controller to highlight
            BlockPosHighlightRenderer.renderBlockBoxHighLight(getControllerPos(), 6000, 1500);
            Minecraft.getMinecraft().player.closeScreen();
        } else if (dataId == UPDATE_OUTPUT_FACING) {
            this.outputFacing = EnumFacing.VALUES[buf.readByte()];
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_FLUIDS || dataId == UPDATE_AUTO_OUTPUT_ITEMS) {
            this.autoOutput = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_IS_VOIDING) {
            setVoiding(buf.readBoolean());
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tagCompound = super.writeToNBT(data);
        tagCompound.setBoolean("HasController", controllerPos != null);
        if (controllerPos != null) {
            tagCompound.setLong("ControllerPos", controllerPos.toLong());
        }
        if (getType() == Type.ITEM || getType() == Type.FLUID) {
            data.setInteger("OutputFacing", getOutputFacing().getIndex());
            data.setBoolean("AutoOutputItems", autoOutput);
            data.setBoolean(INPUT_FROM_OUTPUT, this.allowInputFromOutputSide);
            data.setBoolean(IS_VOIDING, isVoiding());
            data.setBoolean("IsLocked", locked);
        }
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.getBoolean("HasController")) {
            this.controllerPos = BlockPos.fromLong(data.getLong("ControllerPos"));
        }
        if (getType() == Type.ITEM || getType() == Type.FLUID) {
            this.outputFacing = EnumFacing.VALUES[data.getInteger("OutputFacing")];
            this.autoOutput = data.getBoolean("AutoOutputItems");

            if (data.hasKey(INPUT_FROM_OUTPUT))
                this.allowInputFromOutputSide = data.getBoolean(INPUT_FROM_OUTPUT);
            else if (data.hasKey(INPUT_FROM_OUTPUT_FLUID))
                this.allowInputFromOutputSide = data.getBoolean(INPUT_FROM_OUTPUT_FLUID);

            this.voiding = data.getBoolean(IS_VOIDING) || data.getBoolean("IsPartiallyVoiding"); // legacy save support
            this.locked = data.getBoolean("IsLocked");
        }
    }

    protected ButtonWidget<?> createConnectionButton() {
        return new ButtonWidget<>()
                .left(151)
                .disableHoverBackground()
                .onMousePressed(mouseButton -> {
                    // tell controller to highlight
                    if (!isConnected()) return false;
                    BlockPosHighlightRenderer.renderBlockBoxHighLight(getControllerPos(), 6000, 1500);
                    Minecraft.getMinecraft().player.closeScreen();
                    return true;
                })
                .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                .tooltipBuilder(tooltip -> {
                    if (isConnected()) {
                        var pos = getControllerPos();
                        tooltip.addLine(IKey.lang("gregtech.machine.quantum_storage.connected", pos.getX(), pos.getY(),
                                pos.getZ()));
                    } else {
                        tooltip.addLine(IKey.lang("gregtech.machine.quantum_storage.disconnected"));
                    }
                })
                .background(new DynamicDrawable(
                        () -> isConnected() ? GTGuiTextures.GREGTECH_LOGO : GTGuiTextures.GREGTECH_LOGO_DARK));
    }

    @Deprecated
    protected ClickButtonWidget createConnectedGui(int y) {
        connectedIcon = new ClickButtonWidget(151, y, 18, 18, "",
                clickData -> {
                    if (isConnected())
                        writeCustomData(GregtechDataCodes.LOCATE_CONTROLLER);
                });
        connectedIcon.setButtonTexture(isConnected() ? GuiTextures.GREGTECH_LOGO : GuiTextures.GREGTECH_LOGO_DARK);

        if (isConnected()) {
            connectedIcon.setTooltipText("gregtech.machine.quantum_storage.connected",
                    controllerPos.getX(), controllerPos.getZ(), controllerPos.getY());
        } else {
            connectedIcon.setTooltipText("gregtech.machine.quantum_storage.disconnected");
        }

        return connectedIcon;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.quantum_chest.tooltip"));
    }
}
