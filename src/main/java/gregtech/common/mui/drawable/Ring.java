package gregtech.common.mui.drawable;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import org.lwjgl.opengl.GL11;

public class Ring implements IDrawable {

    private int color;
    private float ringWidth;
    private int segments, start, end;

    public Ring() {
        this.color = 0;
        this.ringWidth = 0.2f;
        this.segments = 40;
        this.start = 0;
        this.end = this.segments;
    }

    public Ring setColor(int color) {
        this.color = color;
        return this;
    }

    public Ring setWidth(float ringWidth) {
        if (ringWidth < 0 || ringWidth > 1) {
            throw new IllegalArgumentException("ringWidth must be between 0 and 1");
        }
        this.ringWidth = ringWidth;
        return this;
    }

    /**
     * This will reset <code>end</code> to <code>segments</code>, call {@link this#setProgress} <i>after</i> this!
     */
    public Ring setSegments(int segments) {
        this.segments = segments;
        this.end = this.segments;
        return this;
    }

    public Ring setProgress(int start, int end) {
        this.start = start;
        this.end = end;
        return this;
    }

    public Ring setProgress(int end) {
        this.end = end;
        return this;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        drawRing(x, y, width, height, ringWidth, color, segments, start, end);
    }

    // modified from mui0 Widget#drawTorus to
    private static void drawRing(float x, float y, float w, float h, float ringWidth, int color, int segments, int from,
                                 int to) {
        float cx = x + w / 2f, cy = y + h / 2f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.color(Color.getRedF(color), Color.getGreenF(color), Color.getBlueF(color),
                Color.getAlphaF(color));
        bufferbuilder.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
        for (int i = from; i <= to; i++) {
            float angle = (i / (float) segments) * 3.14159f * 2.0f;
            bufferbuilder.pos(cx + (w / 2) * (1 - ringWidth) * Math.cos(-angle),
                    cy + (h / 2) * (1 - ringWidth) * Math.sin(-angle), 0).endVertex();
            bufferbuilder.pos(cx + (w / 2) * Math.cos(-angle), cy + (h / 2) * Math.sin(-angle), 0).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }
}
