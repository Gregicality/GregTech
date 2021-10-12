package gregtech.api.render.shader.postprocessing;

import gregtech.api.render.shader.PingPongBuffer;
import gregtech.api.render.shader.Shaders;
import gregtech.api.util.RenderUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class BlurEffect {
    private static Framebuffer BLUR_H;
    private static Framebuffer BLUR_W;
    private static Framebuffer BLUR_H2;
    private static Framebuffer BLUR_W2;

    public static void updateSize(int lastWidth, int lastHeight) {
        if (BLUR_H == null) {
            BLUR_H = new Framebuffer(lastWidth / 8, lastHeight / 8, false);
            BLUR_H2 = new Framebuffer(lastWidth / 4, lastHeight / 4, false);
            BLUR_W = new Framebuffer(lastWidth / 8, lastHeight / 8, false);
            BLUR_W2 = new Framebuffer(lastWidth / 4, lastHeight / 4, false);
            BLUR_H.setFramebufferColor(0, 0, 0, 0);
            BLUR_H2.setFramebufferColor(0, 0, 0, 0);
            BLUR_W.setFramebufferColor(0, 0, 0, 0);
            BLUR_W2.setFramebufferColor(0, 0, 0, 0);
        } else if (RenderUtil.updateFBOSize(BLUR_H, lastWidth / 8, lastHeight / 8)){
            RenderUtil.updateFBOSize(BLUR_H2, lastWidth / 4, lastHeight / 4);
            RenderUtil.updateFBOSize(BLUR_W, lastWidth / 8, lastHeight / 8);
            RenderUtil.updateFBOSize(BLUR_W2, lastWidth / 4, lastHeight / 4);
        }
        PingPongBuffer.updateSize(lastWidth, lastHeight);
    }

    public static Framebuffer renderBlur1(float step) {
        int lastOP = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        Shaders.renderFullImageInFBO(BLUR_H2, Shaders.BLUR, uniformCache -> uniformCache.glUniform2F("blurDir", 0, step)).bindFramebufferTexture();
        Shaders.renderFullImageInFBO(BLUR_W2, Shaders.BLUR, uniformCache -> uniformCache.glUniform2F("blurDir", step, 0)).bindFramebufferTexture();
        Shaders.renderFullImageInFBO(BLUR_H, Shaders.BLUR, uniformCache -> uniformCache.glUniform2F("blurDir", 0, step)).bindFramebufferTexture();
        Shaders.renderFullImageInFBO(BLUR_W, Shaders.BLUR, uniformCache -> uniformCache.glUniform2F("blurDir", step, 0)).bindFramebufferTexture();

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, lastOP);
        return BLUR_W;
    }

    public static Framebuffer renderBlur2(int loop, float step) {
        for (int i = 0; i < loop; i++) {
            Shaders.renderFullImageInFBO(PingPongBuffer.swap(true), Shaders.BLUR, uniformCache -> uniformCache.glUniform2F("blurDir", 0, step)).bindFramebufferTexture();
            Shaders.renderFullImageInFBO(PingPongBuffer.swap(), Shaders.BLUR, uniformCache -> uniformCache.glUniform2F("blurDir", step, 0)).bindFramebufferTexture();
        }
        return PingPongBuffer.getCurrentBuffer(false);
    }
}
