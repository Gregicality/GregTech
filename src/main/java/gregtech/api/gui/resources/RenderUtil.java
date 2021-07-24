package gregtech.api.gui.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderUtil {

    public static int packColor(int red, int green, int blue, int alpha) {
        return (red & 0xFF) << 24 | (green & 0xFF) << 16 | (blue & 0xFF) << 8 | (alpha & 0xFF);
    }

    public static void setGlColorFromInt(int colorValue, int opacity) {
        int i = (colorValue & 16711680) >> 16;
        int j = (colorValue & 65280) >> 8;
        int k = (colorValue & 255);
        GlStateManager.color(i / 255.0f, j / 255.0f, k / 255.0f, opacity / 255.0f);
    }

    public static void setGlClearColorFromInt(int colorValue, int opacity) {
        int i = (colorValue & 16711680) >> 16;
        int j = (colorValue & 65280) >> 8;
        int k = (colorValue & 255);
        GlStateManager.clearColor(i / 255.0f, j / 255.0f, k / 255.0f, opacity / 255.0f);
    }

    public static int getFluidColor(FluidStack fluidStack) {
        if (fluidStack.getFluid() == FluidRegistry.WATER)
            return 0x3094CF;
        else if (fluidStack.getFluid() == FluidRegistry.LAVA)
            return 0xFFD700;
        return fluidStack.getFluid().getColor(fluidStack);
    }

    public static void drawFluidForGui(FluidStack contents, int tankCapacity, int startX, int startY, int widthT, int heightT) {
        widthT--;
        heightT--;
        Fluid fluid = contents.getFluid();
        ResourceLocation fluidStill = fluid.getStill();
        TextureAtlasSprite fluidStillSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidStill.toString());
        int fluidColor = fluid.getColor(contents);
        int scaledAmount = contents.amount * heightT / tankCapacity;
        if (contents.amount > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        if (scaledAmount > heightT) {
            scaledAmount = heightT;
        }
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        setGlColorFromInt(fluidColor, 200);

        final int xTileCount = widthT / 16;
        final int xRemainder = widthT - xTileCount * 16;
        final int yTileCount = scaledAmount / 16;
        final int yRemainder = scaledAmount - yTileCount * 16;

        final int yStart = startY + heightT;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int width = xTile == xTileCount ? xRemainder : 16;
                int height = yTile == yTileCount ? yRemainder : 16;
                int x = startX + xTile * 16;
                int y = yStart - (yTile + 1) * 16;
                if (width > 0 && height > 0) {
                    int maskTop = 16 - height;
                    int maskRight = 16 - width;

                    drawFluidTexture(x, y, fluidStillSprite, maskTop, maskRight, 0.0);
                }
            }
        }
        GlStateManager.disableBlend();
    }

    private static void drawFluidTexture(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel) {
        double uMin = textureSprite.getMinU();
        double uMax = textureSprite.getMaxU();
        double vMin = textureSprite.getMinV();
        double vMax = textureSprite.getMaxV();
        uMax = uMax - maskRight / 16.0 * (uMax - uMin);
        vMax = vMax - maskTop / 16.0 * (vMax - vMin);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(xCoord, yCoord + 16, zLevel).tex(uMin, vMax).endVertex();
        buffer.pos(xCoord + 16 - maskRight, yCoord + 16, zLevel).tex(uMax, vMax).endVertex();
        buffer.pos(xCoord + 16 - maskRight, yCoord + maskTop, zLevel).tex(uMax, vMin).endVertex();
        buffer.pos(xCoord, yCoord + maskTop, zLevel).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }

    public static void setColor(int color) { // ARGB
        GlStateManager.color((color >> 16 & 255) / 255.0F,
                (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F,
                (color >> 24 & 255) / 255.0F);
    }

    public static void renderCircle(float x, float y, float r, int color, int detail) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        setColor(color);
        bufferbuilder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        for (int i = 0; i < detail; i++) {
            bufferbuilder.pos(x + r * Math.cos(-2 * Math.PI * i / detail), y + r * Math.sin(-2 * Math.PI * i / detail), 0.0D).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

}
