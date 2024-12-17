package gregtech.common.mui.drawable;

import com.cleanroommc.modularui.drawable.Circle;
import com.cleanroommc.modularui.drawable.Rectangle;

public class RectangleWithCircle extends Rectangle {

    private Circle circle;

    @Override
    public RectangleWithCircle setColor(int color) {
        getCircle().setColor(color, color);
        super.setColor(color);
        return this;
    }

    public Circle getCircle() {
        // circle needs to be initialized during the super constructor somehow, and this is the simplest way
        return circle == null ? circle = new Circle() : circle;
    }
}
