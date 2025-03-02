package gregtech.integration.jei.utils;

import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.function.BiConsumer;

public class JeiInteractableText {

    private final int x;
    private final int y;
    private final boolean invertX;
    private int color;
    private String currentText;
    private int textWidth;
    private TextClickAction textClickAction;
    private BiConsumer<Integer, List<String>> tooltipBuilder;
    private int state;

    /**
     * Creates a new text object when can handle clicks and update state when clicked
     * 
     * @param x           x value, 0 on the left border, increases moving right.
     * @param y           x value, 0 on the top border, increases moving down.
     * @param defaultText the text that should be initially displayed(without any clicks)
     * @param color       the default color of the text, overridden by in-text formatting codes
     * @param baseState   the default state of the button, it is used for tooltip and general information storage
     * @param invertX     instead defines x as the distance from the right border,
     *                    this takes into account the text width,
     *                    ensuring the rightmost part of the text is always aligned
     */
    public JeiInteractableText(int x, int y, String defaultText, int color, int baseState, boolean invertX) {
        this.x = x;
        this.y = y;
        this.invertX = invertX;
        this.currentText = defaultText;
        this.textWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(defaultText);
        this.color = color;
        this.state = baseState;
    }

    public void render(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        minecraft.fontRenderer.drawString(currentText, invertX ? recipeWidth - x - textWidth : x, y, color);
    }

    public JeiInteractableText setTooltipBuilder(BiConsumer<Integer, List<String>> builder) {
        this.tooltipBuilder = builder;
        return this;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        if (!(mouseY >= y && mouseY <= y + 10)) return false;
        // seems like recipeWidth is always 176
        if (invertX) return 176 - textWidth - x <= mouseX && mouseX <= 176 - x;
        return mouseX >= x && mouseX <= x + textWidth;
    }

    public void setCurrentText(String text) {
        this.currentText = text;
        this.textWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(currentText);
    }

    public String getCurrentText() {
        return this.currentText;
    }

    /**
     * This is overriden by in-text formatting codes!
     * 
     * @param color The color to set the text to
     */
    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }

    public JeiInteractableText setClickAction(TextClickAction action) {
        this.textClickAction = action;
        return this;
    }

    public TextClickAction getTextClickAction() {
        return this.textClickAction;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return this.state;
    }

    public void buildTooltip(List<String> baseTooltip) {
        if (tooltipBuilder == null) return;
        tooltipBuilder.accept(this.state, baseTooltip);
    }

    @FunctionalInterface
    public interface TextClickAction {

        boolean click(Minecraft minecraft, JeiInteractableText text, int mouseX, int mouseY, int mouseButton);
    }
}
