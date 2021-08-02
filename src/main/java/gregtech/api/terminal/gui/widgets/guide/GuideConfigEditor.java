package gregtech.api.terminal.gui.widgets.guide;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TabGroup;
import gregtech.api.gui.widgets.tab.IGuiTextureTabInfo;
import gregtech.api.terminal.gui.CustomTabListRenderer;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.util.Size;

import java.awt.*;
import java.util.Map;

public class GuideConfigEditor extends TabGroup {
    public String json;
    private IGuideWidget selected;
    private GuidePageEditorWidget pageEditor;
    private final DraggableScrollableWidgetGroup widgetSelector;
    private final DraggableScrollableWidgetGroup widgetConfigurator;

    public GuideConfigEditor(int x, int y, int width, int height) {
        super(x, y + 10, new CustomTabListRenderer(
                new ColorRectTexture(new Color(175, 0, 0, 131)),
                new ColorRectTexture(new Color(246, 120, 120, 190)), 30, 10));
        setSize(new Size(width, height));
        widgetSelector = createWidgetSelector();
        widgetConfigurator = createConfigurator();
        this.addTab(new IGuiTextureTabInfo(new TextTexture("W", -1), "widget"), widgetSelector);
        this.addTab(new IGuiTextureTabInfo(new TextTexture("C", -1), "config"), widgetConfigurator);
        this.addWidget(new CircleButtonWidget(100, -5, 5, 1, 3)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        new Color(255, 255, 255).getRGB(),
                        new Color(0, 115, 255).getRGB())
                .setIcon(GuiTextures.TERMINAL_ADD)
                .setHoverText("add stream")
                .setClickListener(this::addStream));
        this.addWidget(new CircleButtonWidget(120, -5, 5, 1, 3)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        new Color(255, 255, 255).getRGB(),
                        new Color(113, 27, 217).getRGB())
                .setIcon(GuiTextures.TERMINAL_ADD)
                .setHoverText("add fixed")
                .setClickListener(this::addFixed));
    }

    public void setGuidePageEditorWidget(GuidePageEditorWidget pageEditor) {
        this.pageEditor = pageEditor;
    }

    private DraggableScrollableWidgetGroup createWidgetSelector() {
        DraggableScrollableWidgetGroup group = new DraggableScrollableWidgetGroup(0, 0, getSize().width, getSize().height - 10)
                .setBackground(new ColorRectTexture(new Color(246, 120, 120, 190)))
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(new Color(148, 226, 193)));
        int y = 10; //133
        for (Map.Entry<String, IGuideWidget> entry : GuidePageWidget.REGISTER_WIDGETS.entrySet()) {
            IGuideWidget widgetTemplate = entry.getValue();
            JsonObject template = widgetTemplate.getTemplate(false);
//            template.addProperty("stroke", 0xFF7CA1FF);
            Widget guideWidget = widgetTemplate.createStreamWidget(5, y + 10, getSize().width - 14, template);
            group.addWidget(new LabelWidget(getSize().width / 2 - 1, y, entry.getKey(), -1).setXCentered(true));
            y += guideWidget.getSize().height + 25;
            group.addWidget(guideWidget);
        }
        return group;
    }

    private DraggableScrollableWidgetGroup createConfigurator() {
        return new DraggableScrollableWidgetGroup(0, 0, getSize().width, getSize().height - 10)
                .setBackground(new ColorRectTexture(new Color(246, 120, 120, 190)))
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(new Color(148, 226, 193)));
    }

    public void loadConfigurator(IGuideWidget widget, JsonObject config, boolean isFixed) {
        widgetConfigurator.clearAllWidgets();
        if (widget != null) {
            widget.loadConfigurator(widgetConfigurator, config, isFixed, attr->{
                widget.updateValue(attr, config.get(attr));
            });
        }
    }

    private void addFixed(ClickData data) {
        if (pageEditor != null && selected != null) {
            selected.setStroke(0);
            pageEditor.addGuideWidget(selected, true);
            selected.setStroke(0xFF7CA1FF);
        }
    }

    private void addStream(ClickData data) {
        if (pageEditor != null && selected != null) {
            selected.setStroke(0);
            pageEditor.addGuideWidget(selected, false);
            selected.setStroke(0xFF7CA1FF);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean flag = super.mouseClicked(mouseX, mouseY, button);
        if (selectedTabIndex == 0 && widgetSelector != null) {
            for (Widget widget : widgetSelector.widgets) {
                if (widget.isMouseOverElement(mouseX, mouseY)) {
                    if (widget instanceof IGuideWidget) {
                        if (selected != null) {
                            selected.setStroke(0);
                        }
                        ((IGuideWidget) widget).setStroke(0xFF7CA1FF);
                        selected = (IGuideWidget) widget;
                    }
                    playButtonClickSound();
                    return true;
                }
            }
        }
        return flag;
    }
}
