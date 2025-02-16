package gregtech.api.gui;

import gregtech.api.gui.widgets.WidgetUIAccess;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Preconditions;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Widget is functional element of ModularUI
 * It can draw, perform actions, react to key press and mouse
 * It's information is also synced to client
 */
public abstract class Widget {

    protected transient ModularUI gui;
    protected transient ISizeProvider sizes;
    protected transient WidgetUIAccess uiAccess;
    private transient Position parentPosition = Position.ORIGIN;
    private transient Position selfPosition;
    private transient Position position;
    private transient Size size;
    private transient boolean isVisible;
    private transient boolean isActive;

    public Widget(Position selfPosition, Size size) {
        Preconditions.checkNotNull(selfPosition, "selfPosition");
        Preconditions.checkNotNull(size, "size");
        this.selfPosition = selfPosition;
        this.size = size;
        this.position = this.parentPosition.add(selfPosition);
        this.isVisible = true;
        this.isActive = true;
    }

    public Widget(int x, int y, int width, int height) {
        this(new Position(x, y), new Size(width, height));
    }

    public void setGui(ModularUI gui) {
        this.gui = gui;
    }

    public void setSizes(ISizeProvider sizes) {
        this.sizes = sizes;
    }

    public void setUiAccess(WidgetUIAccess uiAccess) {
        this.uiAccess = uiAccess;
    }

    public void setParentPosition(Position parentPosition) {
        Preconditions.checkNotNull(parentPosition, "parentPosition");
        this.parentPosition = parentPosition;
        recomputePosition();
    }

    public void setSelfPosition(Position selfPosition) {
        Preconditions.checkNotNull(selfPosition, "selfPosition");
        this.selfPosition = selfPosition;
        recomputePosition();
    }

    public Position addSelfPosition(int addX, int addY) {
        this.selfPosition = new Position(selfPosition.x + addX, selfPosition.y + addY);
        recomputePosition();
        return this.selfPosition;
    }

    public Position getSelfPosition() {
        return selfPosition;
    }

    public void setSize(Size size) {
        Preconditions.checkNotNull(size, "size");
        this.size = size;
        onSizeUpdate();
    }

    public final Position getPosition() {
        return position;
    }

    public final Size getSize() {
        return size;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Rectangle toRectangleBox() {
        Position pos = getPosition();
        Size size = getSize();
        return new Rectangle(pos.x, pos.y, size.width, size.height);
    }

    protected void recomputePosition() {
        this.position = this.parentPosition.add(selfPosition);
        onPositionUpdate();
    }

    protected void onPositionUpdate() {}

    protected void onSizeUpdate() {}

    public boolean isMouseOverElement(int mouseX, int mouseY) {
        Position position = getPosition();
        Size size = getSize();
        return isMouseOver(position.x, position.y, size.width, size.height, mouseX, mouseY);
    }

    public static boolean isMouseOver(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && x + width > mouseX && y + height > mouseY;
    }

    /**
     * Called on both sides to initialize widget data
     */
    public void initWidget() {}

    /**
     * Called on serverside to detect changes and synchronize them with clients
     */
    public void detectAndSendChanges() {}

    /**
     * Called clientside every tick with this modular UI open
     */
    public void updateScreen() {}

    /**
     * Called clientside approximately every 1/60th of a second with this modular UI open
     */
    public void updateScreenOnFrame() {}

    /**
     * Called each draw tick to draw this widget in GUI
     */
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {}

    /**
     * Called each draw tick to draw this widget in GUI
     */
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {}

    /**
     * Called when mouse wheel is moved in GUI
     * For some -redacted- reason mouseX position is relative against GUI not game window as in other mouse events
     */
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        return false;
    }

    /**
     * Called when mouse is clicked in GUI
     */
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return false;
    }

    /**
     * Called when mouse is pressed and hold down in GUI
     */
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        return false;
    }

    /**
     * Called when mouse is released in GUI
     */
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        return false;
    }

    /**
     * Called when key is typed in GUI
     */
    @SideOnly(Side.CLIENT)
    public boolean keyTyped(char charTyped, int keyCode) {
        return false;
    }

    /**
     * Read data received from server's {@link #writeUpdateInfo}
     */
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {}

    public void handleClientAction(int id, PacketBuffer buffer) {}

    public List<INativeWidget> getNativeWidgets() {
        if (this instanceof INativeWidget) {
            return Collections.singletonList((INativeWidget) this);
        }
        return Collections.emptyList();
    }

    /**
     * Writes data to be sent to client's {@link #readUpdateInfo}
     */
    protected void writeUpdateInfo(int id, Consumer<PacketBuffer> packetBufferWriter) {
        if (uiAccess != null && gui != null) {
            uiAccess.writeUpdateInfo(this, id, packetBufferWriter);
        }
    }

    @SideOnly(Side.CLIENT)
    protected void writeClientAction(int id, Consumer<PacketBuffer> packetBufferWriter) {
        if (uiAccess != null) {
            uiAccess.writeClientAction(this, id, packetBufferWriter);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void drawBorder(int x, int y, int width, int height, int color, int border) {
        drawSolidRect(x - border, y - border, width + 2 * border, border, color);
        drawSolidRect(x - border, y + height, width + 2 * border, border, color);
        drawSolidRect(x - border, y, border, height, color);
        drawSolidRect(x + width, y, border, height, color);
    }

    @SideOnly(Side.CLIENT)
    public void drawHoveringText(ItemStack itemStack, List<String> tooltip, int maxTextWidth, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiUtils.drawHoveringText(itemStack, tooltip, mouseX, mouseY,
                sizes.getScreenWidth(),
                sizes.getScreenHeight(), maxTextWidth, mc.fontRenderer);
        GlStateManager.disableLighting();
    }

    @SideOnly(Side.CLIENT)
    public static void drawStringSized(String text, double x, double y, int color, boolean dropShadow, float scale,
                                       boolean center) {
        GlStateManager.pushMatrix();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        double scaledTextWidth = center ? fontRenderer.getStringWidth(text) * scale : 0.0;
        GlStateManager.translate(x - scaledTextWidth / 2.0, y, 0.0f);
        GlStateManager.scale(scale, scale, scale);
        fontRenderer.drawString(text, 0, 0, color, dropShadow);
        GlStateManager.popMatrix();
    }

    @SideOnly(Side.CLIENT)
    public static void drawStringFixedCorner(String text, double x, double y, int color, boolean dropShadow,
                                             float scale) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        double scaledWidth = fontRenderer.getStringWidth(text) * scale;
        double scaledHeight = fontRenderer.FONT_HEIGHT * scale;
        drawStringSized(text, x - scaledWidth, y - scaledHeight, color, dropShadow, scale, false);
    }

    @SideOnly(Side.CLIENT)
    public static void drawText(String text, float x, float y, float scale, int color) {
        drawText(text, x, y, scale, color, false);
    }

    @SideOnly(Side.CLIENT)
    public static void drawText(String text, float x, float y, float scale, int color, boolean shadow) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 0f);
        float sf = 1 / scale;
        fontRenderer.drawString(text, x * sf, y * sf, color, shadow);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
    }

    @SideOnly(Side.CLIENT)
    public static List<String> getItemToolTip(ItemStack itemStack) {
        Minecraft mc = Minecraft.getMinecraft();
        ITooltipFlag flag = mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED :
                ITooltipFlag.TooltipFlags.NORMAL;
        List<String> tooltip = itemStack.getTooltip(mc.player, flag);
        for (int i = 0; i < tooltip.size(); ++i) {
            if (i == 0) {
                tooltip.set(i, itemStack.getItem().getForgeRarity(itemStack).getColor() + tooltip.get(i));
            } else {
                tooltip.set(i, TextFormatting.GRAY + tooltip.get(i));
            }
        }
        return tooltip;
    }

    @SideOnly(Side.CLIENT)
    public static void drawSelectionOverlay(int x, int y, int width, int height) {
        GlStateManager.disableDepth();
        GlStateManager.colorMask(true, true, true, false);
        drawGradientRect(x, y, width, height, -2130706433, -2130706433);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
    }

    @SideOnly(Side.CLIENT)
    public static void drawSolidRect(int x, int y, int width, int height, int color) {
        Gui.drawRect(x, y, x + width, y + height, color);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableBlend();
    }

    @SideOnly(Side.CLIENT)
    public static void drawRectShadow(int x, int y, int width, int height, int distance) {
        drawGradientRect(x + distance, y + height, width - distance, distance, 0x4f000000, 0, false);
        drawGradientRect(x + width, y + distance, distance, height - distance, 0x4f000000, 0, true);

        float startAlpha = (float) (0x4f) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        x += width;
        y += height;
        buffer.pos(x, y, 0).color(0, 0, 0, startAlpha).endVertex();
        buffer.pos(x, y + distance, 0).color(0, 0, 0, 0).endVertex();
        buffer.pos(x + distance, y + distance, 0).color(0, 0, 0, 0).endVertex();

        buffer.pos(x, y, 0).color(0, 0, 0, startAlpha).endVertex();
        buffer.pos(x + distance, y + distance, 0).color(0, 0, 0, 0).endVertex();
        buffer.pos(x + distance, y, 0).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    @SideOnly(Side.CLIENT)
    public static void drawGradientRect(int x, int y, int width, int height, int startColor, int endColor) {
        drawGradientRect(x, y, width, height, startColor, endColor, false);
    }

    @SideOnly(Side.CLIENT)
    public static void drawGradientRect(float x, float y, float width, float height, int startColor, int endColor,
                                        boolean horizontal) {
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        if (horizontal) {
            buffer.pos(x + width, y, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            buffer.pos(x, y, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.pos(x, y + height, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        } else {
            buffer.pos(x + width, y, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.pos(x, y, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.pos(x, y + height, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        }
        buffer.pos(x + width, y + height, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    @SideOnly(Side.CLIENT)
    public static void setColor(int color) { // ARGB
        GlStateManager.color((color >> 16 & 255) / 255.0F,
                (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F,
                (color >> 24 & 255) / 255.0F);
    }

    @SideOnly(Side.CLIENT)
    public static void drawCircle(float x, float y, float r, int color, int segments) {
        if (color == 0) return;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        setColor(color);
        bufferbuilder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        for (int i = 0; i < segments; i++) {
            bufferbuilder.pos(x + r * Math.cos(-2 * Math.PI * i / segments),
                    y + r * Math.sin(-2 * Math.PI * i / segments), 0.0D).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    @SideOnly(Side.CLIENT)
    public static void drawSector(float x, float y, float r, int color, int segments, int from, int to) {
        if (from > to || from < 0 || color == 0) return;
        if (to > segments) to = segments;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        setColor(color);
        bufferbuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
        for (int i = from; i < to; i++) {
            bufferbuilder.pos(x + r * Math.cos(-2 * Math.PI * i / segments),
                    y + r * Math.sin(-2 * Math.PI * i / segments), 0.0D).endVertex();
            bufferbuilder.pos(x + r * Math.cos(-2 * Math.PI * (i + 1) / segments),
                    y + r * Math.sin(-2 * Math.PI * (i + 1) / segments), 0.0D).endVertex();
            bufferbuilder.pos(x, y, 0.0D).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    public static void drawTorus(float x, float y, float outer, float inner, int color, int segments, int from,
                                 int to) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        setColor(color);
        bufferbuilder.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
        for (int i = from; i <= to; i++) {
            float angle = (i / (float) segments) * 3.14159f * 2.0f;
            bufferbuilder.pos(x + inner * Math.cos(-angle), y + inner * Math.sin(-angle), 0).endVertex();
            bufferbuilder.pos(x + outer * Math.cos(-angle), y + outer * Math.sin(-angle), 0).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    @SideOnly(Side.CLIENT)
    public static void drawLines(List<Vec2f> points, int startColor, int endColor, float width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(width);
        if (startColor == endColor) {
            setColor(startColor);
            bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
            for (Vec2f point : points) {
                bufferbuilder.pos(point.x, point.y, 0).endVertex();
            }
        } else {
            float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
            float startRed = (float) (startColor >> 16 & 255) / 255.0F;
            float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
            float startBlue = (float) (startColor & 255) / 255.0F;
            float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
            float endRed = (float) (endColor >> 16 & 255) / 255.0F;
            float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
            float endBlue = (float) (endColor & 255) / 255.0F;
            bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            int size = points.size();

            for (int i = 0; i < size; i++) {
                float p = i * 1.0f / size;
                bufferbuilder.pos(points.get(i).x, points.get(i).y, 0)
                        .color(startRed + (endRed - startRed) * p,
                                startGreen + (endGreen - startGreen) * p,
                                startBlue + (endBlue - startBlue) * p,
                                startAlpha + (endAlpha - startAlpha) * p)
                        .endVertex();
            }
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    @SideOnly(Side.CLIENT)
    public static void drawTextureRect(double x, double y, double width, double height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, 0.0D).tex(0, 0).endVertex();
        buffer.pos(x + width, y + height, 0.0D).tex(1, 0).endVertex();
        buffer.pos(x + width, y, 0.0D).tex(1, 1).endVertex();
        buffer.pos(x, y, 0.0D).tex(0, 1).endVertex();
        tessellator.draw();
    }

    @SideOnly(Side.CLIENT)
    public static List<Vec2f> genBezierPoints(Vec2f from, Vec2f to, boolean horizontal, float u) {
        Vec2f c1;
        Vec2f c2;
        if (horizontal) {
            c1 = new Vec2f((from.x + to.x) / 2, from.y);
            c2 = new Vec2f((from.x + to.x) / 2, to.y);
        } else {
            c1 = new Vec2f(from.x, (from.y + to.y) / 2);
            c2 = new Vec2f(to.x, (from.y + to.y) / 2);
        }
        Vec2f[] controlPoint = new Vec2f[] { from, c1, c2, to };
        int n = controlPoint.length - 1;
        int i, r;
        List<Vec2f> bezierPoints = new ArrayList<>();
        for (u = 0; u <= 1; u += 0.01) {
            Vec2f[] p = new Vec2f[n + 1];
            for (i = 0; i <= n; i++) {
                p[i] = new Vec2f(controlPoint[i].x, controlPoint[i].y);
            }
            for (r = 1; r <= n; r++) {
                for (i = 0; i <= n - r; i++) {
                    p[i] = new Vec2f((1 - u) * p[i].x + u * p[i + 1].x, (1 - u) * p[i].y + u * p[i + 1].y);
                }
            }
            bezierPoints.add(p[0]);
        }
        return bezierPoints;
    }

    @SideOnly(Side.CLIENT)
    protected static void playButtonClickSound() {
        Minecraft.getMinecraft().getSoundHandler()
                .playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @SideOnly(Side.CLIENT)
    protected static boolean isShiftDown() {
        return TooltipHelper.isShiftDown();
    }

    @SideOnly(Side.CLIENT)
    protected static boolean isCtrlDown() {
        return TooltipHelper.isCtrlDown();
    }

    public boolean isRemote() {
        return gui.holder.isRemote();
    }

    protected static boolean isClientSide() {
        return FMLCommonHandler.instance().getSide().isClient();
    }

    public static final class ClickData {

        public final int button;
        public final boolean isShiftClick;
        public final boolean isCtrlClick;
        public final boolean isClient;

        public ClickData(int button, boolean isShiftClick, boolean isCtrlClick) {
            this.button = button;
            this.isShiftClick = isShiftClick;
            this.isCtrlClick = isCtrlClick;
            this.isClient = false;
        }

        public ClickData(int button, boolean isShiftClick, boolean isCtrlClick, boolean isClient) {
            this.button = button;
            this.isShiftClick = isShiftClick;
            this.isCtrlClick = isCtrlClick;
            this.isClient = isClient;
        }

        public void writeToBuf(PacketBuffer buf) {
            buf.writeVarInt(button);
            buf.writeBoolean(isShiftClick);
            buf.writeBoolean(isCtrlClick);
            buf.writeBoolean(isClient);
        }

        public static ClickData readFromBuf(PacketBuffer buf) {
            int button = buf.readVarInt();
            boolean shiftClick = buf.readBoolean();
            boolean ctrlClick = buf.readBoolean();
            boolean isClient = buf.readBoolean();
            return new ClickData(button, shiftClick, ctrlClick, isClient);
        }
    }

    public static final class WheelData {

        public final int wheelDelta;
        public final boolean isShiftClick;
        public final boolean isCtrlClick;
        public final boolean isClient;

        public WheelData(int wheelDelta, boolean isShiftClick, boolean isCtrlClick) {
            this.wheelDelta = wheelDelta;
            this.isShiftClick = isShiftClick;
            this.isCtrlClick = isCtrlClick;
            this.isClient = false;
        }

        public WheelData(int wheelDelta, boolean isShiftClick, boolean isCtrlClick, boolean isClient) {
            this.wheelDelta = wheelDelta;
            this.isShiftClick = isShiftClick;
            this.isCtrlClick = isCtrlClick;
            this.isClient = isClient;
        }

        public void writeToBuf(PacketBuffer buf) {
            buf.writeVarInt(wheelDelta);
            buf.writeBoolean(isShiftClick);
            buf.writeBoolean(isCtrlClick);
            buf.writeBoolean(isClient);
        }

        public static WheelData readFromBuf(PacketBuffer buf) {
            int button = buf.readVarInt();
            boolean shiftClick = buf.readBoolean();
            boolean ctrlClick = buf.readBoolean();
            boolean isClient = buf.readBoolean();
            return new WheelData(button, shiftClick, ctrlClick, isClient);
        }
    }
}
