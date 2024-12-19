package gregtech.common.terminal2;

import gregtech.api.mui.GTGuis;
import gregtech.api.terminal2.ITerminalApp;
import gregtech.api.terminal2.Terminal2Theme;
import gregtech.common.mui.widget.ColorableVScrollData;
import gregtech.common.mui.widget.GTColorPickerDialog;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.scroll.HorizontalScrollData;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SettingsApp implements ITerminalApp {

    @Override
    public IWidget buildWidgets(HandGuiData guiData, PanelSyncManager guiSyncManager, ModularPanel panel) {
        var backgroundSelectPanel = (PanelSyncHandler) guiSyncManager.panel("terminal_background_select",
                backgroundSelectWidget(), true);

        int rows = Terminal2Theme.colors.size() + 1;

        var column = Flow.column()
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .widthRel(0.95F)
                .height(rows * 24)
                .leftRel(0.4F);

        column.child(Flow.row()
                .widthRel(1.0F)
                .height(24)
                .child(IKey.lang("terminal.settings.background").asWidget())
                .child(new ButtonWidget<>()
                        .overlay(IKey.dynamic(() -> Terminal2Theme.currentBackground))
                        .size(140, 18)
                        .align(Alignment.CenterRight)
                        .onMousePressed(i -> {
                            if (backgroundSelectPanel.isPanelOpen()) {
                                backgroundSelectPanel.closePanel();
                            } else {
                                backgroundSelectPanel.openPanel();
                            }
                            return true;
                        })));

        for (String color : Terminal2Theme.colors) {
            IPanelHandler colorPanel = IPanelHandler.simple(panel,
                    (mainPanel, player) -> new GTColorPickerDialog("color_select_" + color, i -> {
                        Terminal2Theme.setColor(color, i);
                        Terminal2Theme.saveConfig();
                    }, Terminal2Theme.getColorRect(color).getColor(), true),
                    true);

            column.child(Flow.row()
                    .widthRel(1.0F)
                    .height(24)
                    .child(IKey.lang("terminal.settings.color", color.substring(6)).asWidget())
                    .child(new ButtonWidget<>()
                            .overlay(GuiTextures.REFRESH)
                            .left(276)
                            .topRelAnchor(0.5F, 0.5F)
                            .addTooltipLine(IKey.lang("terminal.settings.reset_color"))
                            .onMousePressed(i -> {
                                Terminal2Theme.resetToDefaultColor(color);
                                Terminal2Theme.saveConfig();
                                // force reset color picker state
                                colorPanel.deleteCachedPanel();
                                return true;
                            }))
                    .child(new ButtonWidget<>()
                            .overlay(Terminal2Theme.getColorRect(color))
                            .background(GuiTextures.CHECKBOARD)
                            .disableHoverOverlay()
                            .disableHoverBackground()
                            .align(Alignment.CenterRight)
                            .onMousePressed(i -> {
                                colorPanel.openPanel();
                                return true;
                            })));
        }

        var scroll = new ScrollWidget<>(new ColorableVScrollData())
                .child(column)
                .sizeRel(1.0F);

        scroll.getScrollArea().getScrollY().setScrollSize(rows * 24);
        Terminal2Theme.COLOR_FOREGROUND_BRIGHT.bindScrollFG(scroll);

        return new ParentWidget<>()
                .sizeRel(0.98F)
                .posRel(0.5F, 0.5F)
                .background(Terminal2Theme.COLOR_BACKGROUND_1)
                .child(scroll);
    }

    @Override
    public IDrawable getIcon() {
        return GuiTextures.GEAR;
    }

    private PanelSyncHandler.IPanelBuilder backgroundSelectWidget() {
        return (syncManager, syncHandler) -> {
            String[] files = Terminal2Theme.backgroundsDir.list();
            List<String> options;
            if (files == null) {
                options = new ArrayList<>();
            } else {
                options = new ArrayList<>(Arrays.asList(files));
            }
            options.sort(Comparator.naturalOrder());
            options.add(0, "default");

            var list = ListWidget.builder(options, (st) -> new ButtonWidget<>()
                    .overlay(IKey.str(st))
                    .size(140, 18)
                    .leftRel(0.2F)
                    .onMousePressed(i -> {
                        Terminal2Theme.setBackground(st);
                        Terminal2Theme.saveConfig();
                        return true;
                    }))
                    // convince it to accept the colorable scroll data by giving it one with a different axis first
                    .scrollDirection(new HorizontalScrollData())
                    .scrollDirection(new ColorableVScrollData())
                    .size(150, 90).pos(9, 16);

            Terminal2Theme.COLOR_FOREGROUND_BRIGHT.bindScrollFG(list);

            return GTGuis.createPopupPanel("terminal_background_select", 168, 112)
                    .child(IKey.lang("terminal.settings.background_select").asWidget()
                            .leftRel(0.5F)
                            .top(6))
                    .child(list);
        };
    }
}
