package gregtech.api.gui.widgets;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.function.FloatConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class SliderWidget extends Widget {

    public static final BiFunction<String, Float, String> DEFAULT_TEXT_SUPPLIER = (name, value) -> I18n.format(name,
            value.intValue());

    protected int sliderWidth = 8;
    protected TextureArea backgroundArea = GuiTextures.SLIDER_BACKGROUND;
    protected TextureArea sliderIcon = GuiTextures.SLIDER_ICON;
    private final BiFunction<String, Float, String> textSupplier = DEFAULT_TEXT_SUPPLIER;
    protected int textColor = 0xFFFFFF;

    protected final float min;
    protected final float max;
    private final String name;

    private final FloatConsumer responder;
    private boolean isPositionSent;

    protected String displayString;
    protected float sliderPosition;
    public boolean isMouseDown;

    public SliderWidget(String name, int xPosition, int yPosition, int width, int height, float min, float max,
                        float currentValue, FloatConsumer responder) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        Preconditions.checkNotNull(responder, "responder");
        Preconditions.checkNotNull(name, "name");
        this.min = min;
        this.max = max;
        this.name = name;
        this.responder = responder;
        this.sliderPosition = (currentValue - min) / (max - min);
    }

    public SliderWidget setSliderIcon(@NotNull TextureArea sliderIcon) {
        Preconditions.checkNotNull(sliderIcon, "sliderIcon");
        this.sliderIcon = sliderIcon;
        return this;
    }

    public SliderWidget setBackground(@Nullable TextureArea background) {
        this.backgroundArea = background;
        return this;
    }

    public SliderWidget setSliderWidth(int sliderWidth) {
        this.sliderWidth = sliderWidth;
        return this;
    }

    public SliderWidget setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    @Override
    public void detectAndSendChanges() {
        if (!isPositionSent) {
            writeUpdateInfo(1, buffer -> buffer.writeFloat(sliderPosition));
            this.isPositionSent = true;
        }
    }

    public float getSliderValue() {
        return this.min + (this.max - this.min) * this.sliderPosition;
    }

    protected String getDisplayString() {
        return textSupplier.apply(name, getSliderValue());
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundArea != null) {
            backgroundArea.draw(pos.x, pos.y, size.width, size.height);
        }
        if (displayString == null) {
            this.displayString = getDisplayString();
        }
        sliderIcon.draw(pos.x + (int) (this.sliderPosition * (float) (size.width - 8)), pos.y, sliderWidth,
                size.height);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawString(displayString,
                pos.x + size.width / 2 - fontRenderer.getStringWidth(displayString) / 2,
                pos.y + size.height / 2 - fontRenderer.FONT_HEIGHT / 2, textColor);
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (this.isMouseDown) {
            Position pos = getPosition();
            Size size = getSize();
            this.sliderPosition = (float) (mouseX - (pos.x + 4)) / (float) (size.width - 8);

            if (this.sliderPosition < 0.0F) {
                this.sliderPosition = 0.0F;
            }

            if (this.sliderPosition > 1.0F) {
                this.sliderPosition = 1.0F;
            }

            this.displayString = this.getDisplayString();
            writeClientAction(1, buffer -> buffer.writeFloat(sliderPosition));
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            Position pos = getPosition();
            Size size = getSize();
            this.sliderPosition = (float) (mouseX - (pos.x + 4)) / (float) (size.width - 8);

            if (this.sliderPosition < 0.0F) {
                this.sliderPosition = 0.0F;
            }

            if (this.sliderPosition > 1.0F) {
                this.sliderPosition = 1.0F;
            }
            this.displayString = this.getDisplayString();
            writeClientAction(1, buffer -> buffer.writeFloat(sliderPosition));
            this.isMouseDown = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        this.isMouseDown = false;
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.sliderPosition = buffer.readFloat();
            this.sliderPosition = MathHelper.clamp(sliderPosition, 0.0f, 1.0f);
            this.responder.apply(getSliderValue());
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.sliderPosition = buffer.readFloat();
            this.sliderPosition = MathHelper.clamp(sliderPosition, 0.0f, 1.0f);
            this.displayString = getDisplayString();
        }
    }
}
