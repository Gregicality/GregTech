package gregtech.api.terminal.app.guideeditor.widget.configurator;

import com.google.gson.JsonObject;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.TextFieldWidget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.os.TerminalTheme;

import java.awt.*;

public class StringConfigurator extends ConfiguratorWidget<String>{
    private TextFieldWidget textFieldWidget;

    public StringConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, String name) {
        super(group, config, name);
    }

    public StringConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, String name, String defaultValue) {
        super(group, config, name, defaultValue);
    }

    protected void init() {
        this.addWidget(new RectButtonWidget(76, 15, 40, 20)
                .setColors(TerminalTheme.COLOR_B_1.getColor(),
                        TerminalTheme.COLOR_1.getColor(),
                        new Color(255, 255, 255, 0).getRGB())
                .setClickListener(data -> updateString())
                .setIcon(new TextTexture("Update", -1)));
        textFieldWidget = new TextFieldWidget(0, 15, 76, 20, TerminalTheme.COLOR_B_2, null, null)
                .setMaxStringLength(Integer.MAX_VALUE)
                .setValidator(s->true);
        this.addWidget(textFieldWidget);
    }

    private void updateString() {
        updateValue(textFieldWidget.getCurrentString());
    }

    @Override
    protected void onDefault() {
        textFieldWidget.setCurrentString(defaultValue);
    }
}
