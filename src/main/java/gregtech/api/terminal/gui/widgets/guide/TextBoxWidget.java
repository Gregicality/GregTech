package gregtech.api.terminal.gui.widgets.guide;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.guide.configurator.BooleanConfigurator;
import gregtech.api.terminal.gui.widgets.guide.configurator.ColorConfigurator;
import gregtech.api.terminal.gui.widgets.guide.configurator.NumberConfigurator;
import gregtech.api.terminal.gui.widgets.guide.configurator.TextListConfigurator;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class TextBoxWidget extends GuideWidget {
    public final static String NAME = "textbox";

    // config
    public List<String> content;
    public int space = 1;
    public int fontSize = 9;
    public int fontColor = 0xff000000;
    public boolean isShadow = false;
    public boolean isCenter = false;

    private transient List<String> textLines;

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
        this.initFixed();
    }

    public TextBoxWidget() {}

    @Override
    public String getRegistryName() {
        return NAME;
    }

    @Override
    public JsonObject getTemplate(boolean isFixed) {
        JsonObject template = super.getTemplate(isFixed);
        template.addProperty("space", space);
        template.addProperty("fontSize", fontSize);
        template.addProperty("fontColor", fontColor);
        template.addProperty("isCenter", isCenter);
        template.addProperty("isShadow", isShadow);
        template.add("content", new Gson().toJsonTree(Arrays.asList("this is a", "textbox!")));
        return template;
    }

    @Override
    public void loadConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, boolean isFixed, Consumer<String> needUpdate) {
        super.loadConfigurator(group, config, isFixed, needUpdate);
        group.addWidget(new NumberConfigurator(5, group.getWidgetBottomHeight() + 5, config, "space", 1).setOnUpdated(needUpdate));
        group.addWidget(new NumberConfigurator(5, group.getWidgetBottomHeight() + 5, config, "fontSize", 9).setOnUpdated(needUpdate));
        group.addWidget(new ColorConfigurator(5, group.getWidgetBottomHeight() + 5, config, "fontColor", 0xff000000).setOnUpdated(needUpdate));
        group.addWidget(new BooleanConfigurator(5, group.getWidgetBottomHeight() + 5, config, "isShadow", false).setOnUpdated(needUpdate));
        group.addWidget(new BooleanConfigurator(5, group.getWidgetBottomHeight() + 5, config, "isCenter", false).setOnUpdated(needUpdate));
        group.addWidget(new TextListConfigurator(5, group.getWidgetBottomHeight() + 5, 200, config, "content", false).setOnUpdated(needUpdate));
    }

    @Override
    protected Widget initFixed() {
        this.textLines = new ArrayList<>();
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        this.space = Math.max(space, 0);
        this.fontSize = Math.max(fontSize, 1);
        int wrapWidth = getSize().width * font.FONT_HEIGHT / fontSize;
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
