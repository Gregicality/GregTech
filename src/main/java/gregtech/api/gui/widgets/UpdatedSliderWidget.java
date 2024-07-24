package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.function.FloatConsumer;
import gregtech.api.util.function.FloatSupplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class UpdatedSliderWidget extends SliderWidget {

    private FloatSupplier detector;

    public UpdatedSliderWidget(String name, int xPosition, int yPosition, int width, int height, float min, float max,
                               float currentValue, FloatConsumer responder, FloatSupplier detector) {
        super(name, xPosition, yPosition, width, height, min, max, currentValue, responder);
        this.detector = detector;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundArea != null) {
            backgroundArea.draw(pos.x, pos.y, size.width, size.height);
        }
        sliderPosition = (detector.get() - min) / (max - min);
        this.displayString = getDisplayString();

        sliderIcon.draw(pos.x + (int) (this.sliderPosition * (float) (size.width - 8)), pos.y, sliderWidth,
                size.height);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawString(displayString,
                pos.x + size.width / 2 - fontRenderer.getStringWidth(displayString) / 2,
                pos.y + size.height / 2 - fontRenderer.FONT_HEIGHT / 2, textColor);
    }
}
