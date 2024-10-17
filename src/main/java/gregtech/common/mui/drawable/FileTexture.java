package gregtech.common.mui.drawable;

import gregtech.api.util.GTLog;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class FileTexture implements IDrawable {

    private static final Map<String, Integer> pathLookup = new HashMap<>();
    private int texID;

    public FileTexture(File file) {
        String fullPath = file.getAbsolutePath();
        if (pathLookup.containsKey(fullPath)) {
            texID = pathLookup.get(fullPath);
            return;
        }

        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                throw new IOException("Read null image from " + file.getName());
            }
            int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length * 4);
            for (int pixel : pixels) {
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
            buffer.flip();

            int texID = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0,
                    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            this.texID = texID;
            pathLookup.put(fullPath, texID);
        } catch (IOException e) {
            GTLog.logger.error("Could not create FileTexture for {}", file.getName(), e);
            this.texID = TextureUtil.MISSING_TEXTURE.getGlTextureId();
            pathLookup.put(fullPath, this.texID);
        }
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(texID);
        GuiDraw.drawTexture(x, y, 0, 0, width, height, width, height);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }
}
