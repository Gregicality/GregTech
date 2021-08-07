package gregtech.api.terminal.gui.widgets.guide.configurator;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.*;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.gui.widgets.guide.TankListWidget;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FluidStackConfigurator extends ConfiguratorWidget<List<TankListWidget.FluidStackInfo>>{
    DraggableScrollableWidgetGroup container;
    List<TankListWidget.FluidStackInfo> tanks;

    public FluidStackConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, String name) {
        super(group, config, name);
    }

    protected void init() {
        container = new DraggableScrollableWidgetGroup(0, 27,116, 100);
        this.addWidget(container);
        this.addWidget(new RectButtonWidget(0, 15, 116, 10, 1)
                .setIcon(new TextTexture("Add Slot", -1))
                .setClickListener(cd->{
                    addSlot(container, new TankListWidget.FluidStackInfo(null, 0));
                    updateValue();
                })
                .setColors(new Color(0, 0, 0, 74).getRGB(),
                new Color(128, 255, 128).getRGB(),
                new Color(255, 255, 255, 0).getRGB()));
        tanks = new ArrayList<>();
        if (!config.get(name).isJsonNull()) {
            Gson gson = new Gson();
            for (JsonElement o : config.get(name).getAsJsonArray()) {
                addSlot(container, gson.fromJson(o, TankListWidget.FluidStackInfo.class));
            }
        }
    }

    private void addSlot(DraggableScrollableWidgetGroup container, TankListWidget.FluidStackInfo fluidStackInfo) {
        WidgetGroup group = new WidgetGroup(0, tanks.size() * 20, 116, 20);
        tanks.add(fluidStackInfo);
        group.addWidget(new PhantomFluidWidget(1, 1, 18, 18, null, null)
                .setBackgroundTexture(new ColorRectTexture(0x9f000000))
                .setFluidStackSupplier(fluidStackInfo::getInstance, true)
                .setFluidStackUpdater(fluidStack->{
                    fluidStackInfo.update(fluidStack);
                    updateValue();
                    }, true));
        group.addWidget(new RectButtonWidget(20, 0, 20, 20)
                .setColors(new Color(0, 0, 0, 74).getRGB(),
                        new Color(128, 255, 128).getRGB(),
                        new Color(255, 255, 255, 0).getRGB())
                .setClickListener(data -> {
                    fluidStackInfo.amount = Math.max(0, fluidStackInfo.amount - (data.isShiftClick ? data.isCtrlClick ? 1000 : 10 : data.isCtrlClick? 100: 1));
                    updateValue();
                })
                .setHoverText("Shift -10|Ctrl -100|Shift+Ctrl -1000")
                .setIcon(new TextTexture("-1", -1)));
        group.addWidget(new RectButtonWidget(76, 0, 20, 20)
                .setColors(new Color(0, 0, 0, 74).getRGB(),
                        new Color(128, 255, 128).getRGB(),
                        new Color(255, 255, 255, 0).getRGB())
                .setClickListener(data -> {
                    fluidStackInfo.amount = Math.max(0, fluidStackInfo.amount + (data.isShiftClick ? data.isCtrlClick ? 1000 : 10 : data.isCtrlClick? 100: 1));
                    updateValue();
                })
                .setHoverText("Shift +10|Ctrl +100|Shift+Ctrl +1000")
                .setIcon(new TextTexture("+1", -1)));
        group.addWidget(new ImageWidget(40, 0, 36, 20, new ColorRectTexture(0x9f000000)));
        group.addWidget(new SimpleTextWidget(58, 10, "", 0xFFFFFF, () -> Integer.toString(fluidStackInfo.amount), true));
        group.addWidget(new RectButtonWidget(96, 0, 20, 20)
                .setColors(new Color(0, 0, 0, 74).getRGB(),
                        new Color(128, 255, 128).getRGB(),
                        new Color(255, 255, 255, 0).getRGB())
                .setClickListener(data -> {
                    container.waitToRemoved(group);
                    tanks.remove(fluidStackInfo);
                    int index = container.widgets.indexOf(group);
                    for (int i = container.widgets.size() - 1; i > index; i--) {
                        container.widgets.get(i).addSelfPosition(0, -20);
                    }
                    updateValue();
                })
                .setIcon(GuiTextures.TERMINAL_DELETE));
        container.addWidget(group);
    }

    private void updateValue() {
        updateValue(tanks);
    }
}
