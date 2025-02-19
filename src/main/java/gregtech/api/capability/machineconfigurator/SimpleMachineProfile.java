package gregtech.api.capability.machineconfigurator;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.function.NBTTagCompoundSupplier;

import net.minecraft.util.EnumFacing;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleMachineProfile implements IMachineConfiguratorProfile {

    public static final SimpleMachineProfile INSTANCE = new SimpleMachineProfile();

    private SimpleMachineProfile() {}

    @Override
    public @NotNull String getName() {
        return "SimpleMachineProfile";
    }

    @NotNull
    @Override
    public ModularPanel createConfiguratorPanel(PanelSyncManager panelSyncManager, NBTTagCompoundSupplier nbt) {
        var panel = GTGuis.createPopupPanel("simple_machine_configurator", 75, 150);

        BooleanSyncValue autoOutputItems = new BooleanSyncValue(() -> nbt.getBoolean("AutoOutputItems"),
                bool -> nbt.setBoolean("AutoOutputItems", bool));
        BooleanSyncValue autoOutputFluids = new BooleanSyncValue(() -> nbt.getBoolean("AutoOutputFluids"),
                bool -> nbt.setBoolean("AutoOutputFluids", bool));

        BooleanSyncValue allowItemInputFromOutput = new BooleanSyncValue(
                () -> nbt.getBoolean("AllowItemInputFromOutput"),
                bool -> nbt.setBoolean("AllowItemInputFromOutput", bool));
        BooleanSyncValue allowFluidInputFromOutput = new BooleanSyncValue(
                () -> nbt.getBoolean("AllowFluidInputFromOutput"),
                bool -> nbt.setBoolean("AllowFluidInputFromOutput", bool));

        EnumSyncValue<EnumFacing> itemOutputSide = new EnumSyncValue<>(EnumFacing.class,
                () -> EnumFacing.values()[nbt.getByte("ItemOutputSide")],
                side -> nbt.setByte("ItemOutputSide", (byte) side.getIndex()));
        EnumSyncValue<EnumFacing> fluidOutputSide = new EnumSyncValue<>(EnumFacing.class,
                () -> EnumFacing.values()[nbt.getByte("FluidOutputSide")],
                side -> nbt.setByte("FluidOutputSide", (byte) side.getIndex()));

        panelSyncManager.syncValue("AutoOutputItems", autoOutputItems);
        panelSyncManager.syncValue("AutoOutputFluids", autoOutputFluids);
        panelSyncManager.syncValue("AllowItemInputFromOutput", allowItemInputFromOutput);
        panelSyncManager.syncValue("AllowFluidInputFromOutput", allowFluidInputFromOutput);
        panelSyncManager.syncValue("ItemOutputSide", itemOutputSide);
        panelSyncManager.syncValue("FluidOutputSide", fluidOutputSide);

        panel.child(new ToggleButton()
                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                .value(autoOutputItems));
        panel.child(new ToggleButton()
                .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                .value(autoOutputFluids)
                .pos(18, 0));

        panel.child(new ToggleButton()
                .value(allowItemInputFromOutput)
                .pos(0, 18));
        panel.child(new ToggleButton()
                .value(allowFluidInputFromOutput)
                .pos(18, 18));

        panel.child(SlotGroupWidget.builder()
                .matrix(" B ",
                        "BBB",
                        " BB")
                .key('B', val -> new ButtonWidget<>()
                        .overlay(new DynamicDrawable(() -> {
                            switch (val) {
                                case (0) -> {
                                    boolean isItem = itemOutputSide.getValue().equals(EnumFacing.UP);
                                    boolean isFluid = fluidOutputSide.getValue().equals(EnumFacing.UP);

                                    return getOverlay(isItem, isFluid);
                                }
                                case (1) -> {
                                    boolean isItem = itemOutputSide.getValue().equals(EnumFacing.EAST);
                                    boolean isFluid = fluidOutputSide.getValue().equals(EnumFacing.EAST);

                                    return getOverlay(isItem, isFluid);
                                }
                                case (2) -> {
                                    boolean isItem = itemOutputSide.getValue().equals(EnumFacing.NORTH);
                                    boolean isFluid = fluidOutputSide.getValue().equals(EnumFacing.NORTH);

                                    return getOverlay(isItem, isFluid);
                                }
                                case (3) -> {
                                    boolean isItem = itemOutputSide.getValue().equals(EnumFacing.WEST);
                                    boolean isFluid = fluidOutputSide.getValue().equals(EnumFacing.WEST);

                                    return getOverlay(isItem, isFluid);
                                }
                                case (4) -> {
                                    boolean isItem = itemOutputSide.getValue().equals(EnumFacing.DOWN);
                                    boolean isFluid = fluidOutputSide.getValue().equals(EnumFacing.DOWN);

                                    return getOverlay(isItem, isFluid);
                                }
                                case (5) -> {
                                    boolean isItem = itemOutputSide.getValue().equals(EnumFacing.SOUTH);
                                    boolean isFluid = fluidOutputSide.getValue().equals(EnumFacing.SOUTH);

                                    return getOverlay(isItem, isFluid);
                                }
                                default -> throw new IllegalArgumentException(
                                        "Tried to get the overlay of a button that doesn't exist!");
                            }
                        }))
                        .onMousePressed(button -> {
                            switch (val) {
                                case (0) -> {
                                    if (button == 0) {
                                        itemOutputSide.setValue(EnumFacing.UP);
                                        return true;
                                    } else if (button == 1) {
                                        fluidOutputSide.setValue(EnumFacing.UP);
                                        return true;
                                    }

                                    return false;
                                }
                                case (1) -> {
                                    if (button == 0) {
                                        itemOutputSide.setValue(EnumFacing.EAST);
                                        return true;
                                    } else if (button == 1) {
                                        fluidOutputSide.setValue(EnumFacing.EAST);
                                        return true;
                                    }

                                    return false;
                                }
                                case (2) -> {
                                    if (button == 0) {
                                        itemOutputSide.setValue(EnumFacing.NORTH);
                                        return true;
                                    } else if (button == 1) {
                                        fluidOutputSide.setValue(EnumFacing.NORTH);
                                        return true;
                                    }

                                    return false;
                                }
                                case (3) -> {
                                    if (button == 0) {
                                        itemOutputSide.setValue(EnumFacing.WEST);
                                        return true;
                                    } else if (button == 1) {
                                        fluidOutputSide.setValue(EnumFacing.WEST);
                                        return true;
                                    }

                                    return false;
                                }
                                case (4) -> {
                                    if (button == 0) {
                                        itemOutputSide.setValue(EnumFacing.DOWN);
                                        return true;
                                    } else if (button == 1) {
                                        fluidOutputSide.setValue(EnumFacing.DOWN);
                                        return true;
                                    }

                                    return false;
                                }
                                case (5) -> {
                                    if (button == 0) {
                                        itemOutputSide.setValue(EnumFacing.SOUTH);
                                        return true;
                                    } else if (button == 1) {
                                        fluidOutputSide.setValue(EnumFacing.SOUTH);
                                        return true;
                                    }

                                    return false;
                                }
                                default -> throw new IllegalArgumentException("Clicked a button that doesn't exist!");
                            }
                        }))
                .build()
                .pos(0, 40));

        return panel;
    }

    @Nullable
    private static IDrawable getOverlay(boolean isItem, boolean isFluid) {
        if (isItem && !isFluid) {
            return GTGuiTextures.ITEM_OUTPUT_OVERLAY;
        } else if (!isItem && isFluid) {
            return GTGuiTextures.FLUID_OUTPUT_OVERLAY;
        } else if (isItem) {
            return IDrawable.of(GTGuiTextures.ITEM_OUTPUT_OVERLAY, GTGuiTextures.FLUID_OUTPUT_OVERLAY);
        } else {
            return null;
        }
    }
}
