package gregtech.api.terminal.gui.widgets.guide;

import com.google.gson.JsonObject;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.RenderUtil;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

public class TextBoxWidget extends GuideWidget {
    // config
    public List<String> content;
    public int space = 1;
    public int fontSize = 9;
    public int fontColor = 0xff000000;
    public boolean isShadow = false;
    public boolean isCenter = false;

    private List<String> textLines;

    public TextBoxWidget(int x, int y, int width, List<String> content, int space, int fontSize, int fontColor, int fill, int stroke, boolean isCenter, boolean isShadow) {
        super(x, y, width, 0);
        this.content = content;
        this.space = space;
        this.fontSize = fontSize;
        this.fontColor = fontColor;
        this.fill = fill;
        this.stroke = stroke;
        this.isCenter = isCenter;
        this.isShadow = isShadow;
        this.initFixed(x, y, width, 0, null);
    }

    public TextBoxWidget() {}

    @Override
    protected Widget initFixed(int x, int y, int width, int height, JsonObject config) {
        this.textLines = new ArrayList<>();
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        this.space = Math.max(space, 0);
        this.fontSize = Math.max(fontSize, 1);
        int wrapWidth = width * font.FONT_HEIGHT / fontSize;
        if (content != null) {
            for (String textLine : content) {
                this.textLines.addAll(font.listFormattedStringToWidth(I18n.format(textLine), wrapWidth));
            }
        }
        this.setSize(new Size(this.getSize().width, this.textLines.size() * (fontSize + space)));
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        if (!textLines.isEmpty()) {
            Position position = getPosition();
            Size size = getSize();
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            float scale = fontSize * 1.0f / font.FONT_HEIGHT;
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            GlStateManager.translate(position.x / scale, position.y / scale, 0);
            float x = 0;
            float y = 0;
            float ySpace = font.FONT_HEIGHT + space / scale;
            for (String textLine : textLines) {
                if (isCenter) {
                    x = (size.width / scale - font.getStringWidth(textLine)) / 2;
                }
                font.drawString(textLine, x, y, fontColor, isShadow);
                y += ySpace;
            }
            GlStateManager.popMatrix();
        }
    }
}
