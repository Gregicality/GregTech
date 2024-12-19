package gregtech.common.mui.widget;

import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;

public class ColorableVScrollData extends VerticalScrollData implements IColorableScrollData {

    private int color;

    public ColorableVScrollData() {
        this(0xFFFFFFFF);
    }

    public ColorableVScrollData(int color) {
        super();
        this.color = color;
    }

    public ColorableVScrollData(int color, boolean leftAlignment) {
        super(leftAlignment);
        this.color = color;
    }

    public ColorableVScrollData(int color, boolean leftAlignment, int thickness) {
        super(leftAlignment, thickness);
        this.color = color;
    }

    @Override
    public IColorableScrollData setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    protected void drawScrollBar(int x, int y, int w, int h) {
        GuiDraw.drawRect(x, y, w, h, Color.multiply(color, 14f / 15f, false));
        GuiDraw.drawRect(x + 1, y + 1, w - 1, h - 1, Color.multiply(color, 6f / 15f, false));
        GuiDraw.drawRect(x + 1, y + 1, w - 2, h - 2, Color.multiply(color, 10f / 15f, false));
    }
}
