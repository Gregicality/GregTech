package gregtech.client.utils;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.util.GTUtility;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.awt.*;

// Thanks to EnderIO, slightly modified
@SideOnly(Side.CLIENT)
public final class ToolChargeBarRenderer {

    private static final double BAR_W = 12d;

    private static final Color colorShadow = new Color(0, 0, 0, 255);
    private static final Color colorBG = new Color(0x0E, 0x01, 0x16, 255);

    private static final Color colorBarLeftEnergy = new Color(0, 101, 178, 255);
    private static final Color colorBarRightEnergy = new Color(217, 238, 255, 255);

    private static final Color colorBarLeftDurability = new Color(20, 124, 0, 255);
    private static final Color colorBarRightDurability = new Color(115, 255, 89, 255);

    private static final Color colorBarLeftDepleted = new Color(122, 0, 0, 255);
    private static final Color colorBarRightDepleted = new Color(255, 27, 27, 255);

    public static void render(double level, int xPosition, int yPosition, int offset, boolean shadow, Color left,
                              Color right, boolean doDepletedColor) {
        double width = level * BAR_W;
        if (doDepletedColor && level <= 0.25) {
            left = colorBarLeftDepleted;
            right = colorBarRightDepleted;
        }

        GlStateManager.enableLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        drawShadow(worldrenderer, xPosition + 2, yPosition + 13 - offset, 13, shadow ? 2 : 1);
        drawGrad(worldrenderer, xPosition + 2, yPosition + 13 - offset, (BAR_W + width) / 2, left, right);
        drawBG(worldrenderer, xPosition + 2 + (int) BAR_W, yPosition + 13 - offset, BAR_W - width);
        if (offset == 2) {
            overpaintVanillaRenderBug(worldrenderer, xPosition, yPosition);
        }
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    private static void drawGrad(BufferBuilder renderer, int x, int y, double width, Color left, Color right) {
        renderer.pos(x, y, 0.0D).color(left.getRed(), left.getGreen(), left.getBlue(), left.getAlpha()).endVertex();
        renderer.pos(x, y + (double) 1, 0.0D).color(left.getRed(), left.getGreen(), left.getBlue(), left.getAlpha())
                .endVertex();
        renderer.pos(x + width, y + (double) 1, 0.0D)
                .color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha()).endVertex();
        renderer.pos(x + width, y, 0.0D).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha())
                .endVertex();
    }

    private static void drawShadow(BufferBuilder renderer, int x, int y, double width, double height) {
        renderer.pos(x, y, 0.0D)
                .color(colorShadow.getRed(), colorShadow.getGreen(), colorShadow.getBlue(), colorShadow.getAlpha())
                .endVertex();
        renderer.pos(x, y + height, 0.0D)
                .color(colorShadow.getRed(), colorShadow.getGreen(), colorShadow.getBlue(), colorShadow.getAlpha())
                .endVertex();
        renderer.pos(x + width, y + height, 0.0D)
                .color(colorShadow.getRed(), colorShadow.getGreen(), colorShadow.getBlue(), colorShadow.getAlpha())
                .endVertex();
        renderer.pos(x + width, y, 0.0D)
                .color(colorShadow.getRed(), colorShadow.getGreen(), colorShadow.getBlue(), colorShadow.getAlpha())
                .endVertex();
    }

    private static void drawBG(BufferBuilder renderer, int x, int y, double width) {
        renderer.pos(x - width, y, 0.0D)
                .color(colorBG.getRed(), colorBG.getGreen(), colorBG.getBlue(), colorBG.getAlpha()).endVertex();
        renderer.pos(x - width, y + (double) 1, 0.0D)
                .color(colorBG.getRed(), colorBG.getGreen(), colorBG.getBlue(), colorBG.getAlpha()).endVertex();
        renderer.pos(x, y + (double) 1, 0.0D)
                .color(colorBG.getRed(), colorBG.getGreen(), colorBG.getBlue(), colorBG.getAlpha()).endVertex();
        renderer.pos(x, y, 0.0D).color(colorBG.getRed(), colorBG.getGreen(), colorBG.getBlue(), colorBG.getAlpha())
                .endVertex();
    }

    private static void overpaintVanillaRenderBug(BufferBuilder worldrenderer, int xPosition, int yPosition) {
        drawShadow(worldrenderer, xPosition + 2 + 12, yPosition + 13, 1, 1);
    }

    public static void renderBarsTool(IGTTool tool, ItemStack stack, int xPosition, int yPosition) {
        if (tool instanceof ItemGTToolbelt toolbelt) {
            ItemStack selected = toolbelt.getSelectedTool(stack);
            if (!selected.isEmpty() && selected.getItem() instanceof IGTTool toool) {
                tool = toool;
                stack = selected;
            }
        }
        boolean renderedDurability = false;
        NBTTagCompound tag = GTUtility.getOrCreateNbtCompound(stack);
        if (!tag.getBoolean(ToolHelper.UNBREAKABLE_KEY)) {
            renderedDurability = renderDurabilityBar(stack.getItem().getDurabilityForDisplay(stack), xPosition,
                    yPosition);
        }
        if (tool.isElectric()) {
            renderElectricBar(tool.getCharge(stack), tool.getMaxCharge(stack), xPosition, yPosition,
                    renderedDurability);
        }
    }

    public static void renderBarsItem(MetaItem<?> metaItem, ItemStack stack, int xPosition, int yPosition) {
        boolean renderedDurability = false;
        MetaItem<?>.MetaValueItem valueItem = metaItem.getItem(stack);
        if (valueItem != null && valueItem.getDurabilityManager() != null) {
            renderedDurability = renderDurabilityBar(stack, valueItem.getDurabilityManager(), xPosition, yPosition);
        }

        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            renderElectricBar(electricItem.getCharge(), electricItem.getMaxCharge(), xPosition, yPosition,
                    renderedDurability);
        }
    }

    private static void renderElectricBar(long charge, long maxCharge, int xPosition, int yPosition,
                                          boolean renderedDurability) {
        if (charge > 0 && maxCharge > 0) {
            double level = (double) charge / (double) maxCharge;
            render(level, xPosition, yPosition, renderedDurability ? 2 : 0, true, colorBarLeftEnergy,
                    colorBarRightEnergy, true);
        }
    }

    private static boolean renderDurabilityBar(ItemStack stack, IItemDurabilityManager manager, int xPosition,
                                               int yPosition) {
        double level = manager.getDurabilityForDisplay(stack);
        if (level == 0.0 && !manager.showEmptyBar(stack)) return false;
        if (level == 1.0 && !manager.showFullBar(stack)) return false;
        Pair<Color, Color> colors = manager.getDurabilityColorsForDisplay(stack);
        boolean doDepletedColor = manager.doDamagedStateColors(stack);
        Color left = colors != null ? colors.getLeft() : colorBarLeftDurability;
        Color right = colors != null ? colors.getRight() : colorBarRightDurability;
        render(1 - level, xPosition, yPosition, 0, true, left, right, doDepletedColor);
        return true;
    }

    private static boolean renderDurabilityBar(double level, int xPosition, int yPosition) {
        render(1 - level, xPosition, yPosition, 0, true, colorBarLeftDurability, colorBarRightDurability, true);
        return true;
    }

    private ToolChargeBarRenderer() {}
}
