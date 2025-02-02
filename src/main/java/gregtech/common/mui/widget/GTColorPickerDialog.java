package gregtech.common.mui.widget;

import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widgets.ColorPickerDialog;
import com.cleanroommc.modularui.widgets.SliderWidget;

import java.util.function.Consumer;

// TODO remove this class when the weird resizing bugs are fixed
public class GTColorPickerDialog extends ColorPickerDialog {

    public GTColorPickerDialog(String name, Consumer<Integer> resultConsumer, int startColor, boolean controlAlpha) {
        super(name, resultConsumer, startColor, controlAlpha);
    }

    @Override
    public void onResized() {
        super.onResized();
        // forEachChild has flawed logic and doesnt actually work
        WidgetTree.foreachChildBFS(getThis(), (w) -> {
            if (w instanceof SliderWidget slider) {
                // HACK: work around this not getting called by MUI2 in some situations
                slider.onResized();
            }
            return true;
        });
    }
}
