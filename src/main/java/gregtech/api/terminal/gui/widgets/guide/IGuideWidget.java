package gregtech.api.terminal.gui.widgets.guide;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;

import java.util.function.Consumer;

public interface IGuideWidget {
    Widget createStreamWidget(int x, int y, int pageWidth, JsonObject config);
    Widget createFixedWidget(int x, int y, int width, int height, JsonObject config);
    void setPage(GuidePageWidget page);
    void updateValue(String field, JsonElement value);
    String getRef();
    JsonObject getTemplate(boolean isFixed);
    void loadConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, boolean isFixed, Consumer<String> needUpdate);
    void setStroke(int color);
}
