package gregtech.common.covers;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.*;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CoverMachineController extends CoverBase implements CoverWithUI {

    private boolean isInverted;
    private ControllerMode controllerMode;

    public CoverMachineController(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                  @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
        this.isInverted = false;
        this.controllerMode = ControllerMode.MACHINE;
    }

    public ControllerMode getControllerMode() {
        return controllerMode;
    }

    public boolean isInverted() {
        return isInverted;
    }

    public void setInverted(boolean inverted) {
        isInverted = inverted;
        updateRedstoneStatus();
        getCoverableView().markDirty();
    }

    public void setControllerMode(ControllerMode controllerMode) {
        resetCurrentControllable();
        this.controllerMode = controllerMode;
        updateRedstoneStatus();
        getCoverableView().markDirty();
    }

    public List<ControllerMode> getAllowedModes(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        List<ControllerMode> results = new ArrayList<>();
        for (ControllerMode controllerMode : ControllerMode.VALUES) {
            IControllable controllable = null;
            if (controllerMode.side == null) {
                controllable = coverable.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, side);
            } else {
                Cover cover = coverable.getCoverAtSide(controllerMode.side);
                if (cover != null) {
                    controllable = cover.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
                }
            }
            if (controllable != null) {
                results.add(controllerMode);
            }
        }
        return results;
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return !getAllowedModes(coverable, side).isEmpty();
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull CuboidRayTraceResult hitResult) {
        if (!getCoverableView().getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager) {
        EnumSyncValue<ControllerMode> controllerModeValue = new EnumSyncValue<>(ControllerMode.class,
                this::getControllerMode, this::setControllerMode);
        BooleanSyncValue invertedValue = new BooleanSyncValue(this::isInverted, this::setInverted);

        guiSyncManager.syncValue("controller_mode", controllerModeValue);
        guiSyncManager.syncValue("inverted", invertedValue);

        return GTGuis.createPanel(this, 176, 112)
                .child(CoverWithUI.createTitleRow(getPickItem()))
                .child(Flow.column()
                        .widthRel(1.0f).margin(7, 0)
                        .top(24).coverChildrenHeight()

                        // Inverted mode
                        .child(createSettingsRow()
                                .child(new ToggleButton()
                                        .size(16).left(0)
                                        .value(new BoolValue.Dynamic(invertedValue::getValue,
                                                $ -> invertedValue.setValue(true)))
                                        .overlay(GTGuiTextures.BUTTON_REDSTONE_ON)
                                        .selectedBackground(GTGuiTextures.MC_BUTTON_DISABLED))
                                .child(IKey.lang("cover.machine_controller.enable_with_redstone").asWidget()
                                        .heightRel(1.0f).left(20)))
                        .child(createSettingsRow()
                                .child(new ToggleButton()
                                        .size(16).left(0)
                                        .value(new BoolValue.Dynamic(() -> !invertedValue.getValue(),
                                                $ -> invertedValue.setValue(false)))
                                        .overlay(GTGuiTextures.BUTTON_REDSTONE_OFF)
                                        .selectedBackground(GTGuiTextures.MC_BUTTON_DISABLED))
                                .child(IKey.lang("cover.machine_controller.disable_with_redstone").asWidget()
                                        .heightRel(1.0f).left(20)))

                        // Separating line
                        .child(new Rectangle().setColor(UI_TEXT_COLOR).asWidget()
                                .height(1).widthRel(0.9f).alignX(0.5f).marginBottom(4).marginTop(4))

                        // Controlling selector
                        .child(createSettingsRow().height(16 + 2 + 16)
                                .child(Flow.column().heightRel(1.0f).coverChildrenWidth()
                                        .child(IKey.lang("cover.machine_controller.control").asWidget()
                                                .left(0).height(16).marginBottom(2))
                                        .child(modeButton(controllerModeValue, ControllerMode.MACHINE).left(0)))
                                .child(modeColumn(controllerModeValue, ControllerMode.COVER_UP, IKey.str("U"))
                                        .right(100))
                                .child(modeColumn(controllerModeValue, ControllerMode.COVER_DOWN, IKey.str("D"))
                                        .right(80))
                                .child(modeColumn(controllerModeValue, ControllerMode.COVER_NORTH, IKey.str("N"))
                                        .right(60))
                                .child(modeColumn(controllerModeValue, ControllerMode.COVER_SOUTH, IKey.str("S"))
                                        .right(40))
                                .child(modeColumn(controllerModeValue, ControllerMode.COVER_EAST, IKey.str("E"))
                                        .right(20))
                                .child(modeColumn(controllerModeValue, ControllerMode.COVER_WEST, IKey.str("W"))
                                        .right(0))));
    }

    private Flow modeColumn(EnumSyncValue<ControllerMode> syncValue, ControllerMode mode, IKey title) {
        return Flow.column().coverChildrenHeight().width(18)
                .child(title.asWidget().size(16).marginBottom(2).alignment(Alignment.Center))
                .child(modeButton(syncValue, mode));
    }

    private Widget<?> modeButton(EnumSyncValue<ControllerMode> syncValue, ControllerMode mode) {
        IControllable controllable = getControllable(mode);
        if (controllable == null) {
            // Nothing to control, put a placeholder widget
            // 3 states possible here:
            IKey detail;
            if (mode.side == getAttachedSide()) {
                // our own side, we can't control ourselves
                detail = IKey.lang("cover.machine_controller.this_cover");
            } else if (mode.side != null) {
                // some potential cover that either doesn't exist or isn't controllable
                detail = IKey.lang("cover.machine_controller.cover_not_controllable");
            } else {
                // cover holder is not controllable
                detail = IKey.lang("cover.machine_controller.machine_not_controllable");
            }

            return GTGuiTextures.MC_BUTTON.asWidget().size(18)
                    .overlay(GTGuiTextures.BUTTON_CROSS)
                    .tooltip(t -> t.addLine(IKey.lang(mode.localeName)).addLine(detail));
        }

        ItemStack stack;
        if (mode == ControllerMode.MACHINE) {
            stack = getCoverableView().getStackForm();
        } else {
            // this can't be null because we already checked IControllable, and it was not null
            // noinspection ConstantConditions
            stack = getCoverableView().getCoverAtSide(mode.side).getDefinition().getDropItemStack();
        }

        return new ToggleButton().size(18)
                .value(boolValueOf(syncValue, mode))
                .overlay(new ItemDrawable(stack).asIcon().size(16))
                .tooltip(t -> t.addLine(IKey.lang(mode.localeName))
                        .addLine(IKey.str(TextFormatting.GRAY + stack.getDisplayName())));
    }

    @Override
    public void onAttachment(@NotNull CoverableView coverableView, @NotNull EnumFacing side,
                             @Nullable EntityPlayer player, @NotNull ItemStack itemStack) {
        super.onAttachment(coverableView, side, player, itemStack);
        this.controllerMode = getAllowedModes(getCoverableView(), getAttachedSide()).iterator().next();
        updateRedstoneStatus();
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        resetCurrentControllable();
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.MACHINE_CONTROLLER_OVERLAY.renderSided(getAttachedSide(), plateBox, renderState, pipeline,
                translation);
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public void onRedstoneInputSignalChange(int newSignalStrength) {
        updateRedstoneStatus();
    }

    private @Nullable IControllable getControllable(ControllerMode mode) {
        EnumFacing side = mode.side;
        if (side == null) {
            return getCoverableView().getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE,
                    getAttachedSide());
        } else {
            Cover cover = getCoverableView().getCoverAtSide(side);
            if (cover == null) {
                return null;
            }
            return cover.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        }
    }

    private void resetCurrentControllable() {
        IControllable controllable = getControllable(controllerMode);
        if (controllable != null) {
            controllable.setWorkingEnabled(doesOtherAllowingWork());
        }
    }

    private void updateRedstoneStatus() {
        IControllable controllable = getControllable(controllerMode);
        if (controllable != null) {
            controllable.setWorkingEnabled(shouldAllowWorking() && doesOtherAllowingWork());
        }
    }

    private boolean shouldAllowWorking() {
        int inputSignal = getCoverableView().getInputRedstoneSignal(getAttachedSide(), true);
        return isInverted ? inputSignal > 0 : inputSignal == 0;
    }

    private boolean doesOtherAllowingWork() {
        boolean otherAllow = true;
        CoverMachineController cover;
        EnumFacing attachedSide = getAttachedSide();
        CoverableView coverable = getCoverableView();
        for (EnumFacing side : EnumFacing.values()) {
            if (side != attachedSide &&
                    coverable.getCoverAtSide(side) instanceof CoverMachineController machineController) {
                cover = machineController;
                otherAllow = otherAllow && cover.controllerMode == controllerMode && cover.shouldAllowWorking();
            }
        }
        return otherAllow;
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("Inverted", isInverted);
        tagCompound.setInteger("ControllerMode", controllerMode.ordinal());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.isInverted = tagCompound.getBoolean("Inverted");
        this.controllerMode = ControllerMode.VALUES[tagCompound.getInteger("ControllerMode")];
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeBoolean(isInverted);
        packetBuffer.writeShort(controllerMode.ordinal());
    }

    @Override
    public void readInitialSyncData(@NotNull AdvancedPacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.isInverted = packetBuffer.readBoolean();
        this.controllerMode = ControllerMode.VALUES[packetBuffer.readShort()];
    }

    public enum ControllerMode implements IStringSerializable {

        MACHINE("cover.machine_controller.mode.machine", null),
        COVER_UP("cover.machine_controller.mode.cover_up", EnumFacing.UP),
        COVER_DOWN("cover.machine_controller.mode.cover_down", EnumFacing.DOWN),
        COVER_NORTH("cover.machine_controller.mode.cover_north", EnumFacing.NORTH),
        COVER_SOUTH("cover.machine_controller.mode.cover_south", EnumFacing.SOUTH),
        COVER_EAST("cover.machine_controller.mode.cover_east", EnumFacing.EAST),
        COVER_WEST("cover.machine_controller.mode.cover_west", EnumFacing.WEST);

        public final String localeName;
        public final EnumFacing side;

        public static final ControllerMode[] VALUES = values();

        ControllerMode(String localeName, EnumFacing side) {
            this.localeName = localeName;
            this.side = side;
        }

        @NotNull
        @Override
        public String getName() {
            return localeName;
        }
    }
}
