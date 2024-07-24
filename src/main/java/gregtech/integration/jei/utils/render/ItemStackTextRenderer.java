package gregtech.integration.jei.utils.render;

import gregtech.api.recipes.chance.boost.BoostableChanceEntry;
import gregtech.api.recipes.chance.output.ChancedOutputLogic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemStackTextRenderer extends ItemStackRenderer {

    private final int chanceBase;
    private final int chanceBoost;
    private final ChancedOutputLogic chanceLogic;
    private final boolean notConsumed;

    public ItemStackTextRenderer(BoostableChanceEntry<ItemStack> chance, ChancedOutputLogic chanceLogic) {
        if (chance != null && chanceLogic != null) {
            this.chanceBase = chance.getChance();
            this.chanceBoost = chance.getChanceBoost();
            this.chanceLogic = chanceLogic;
        } else {
            this.chanceBase = -1;
            this.chanceBoost = -1;
            this.chanceLogic = null;
        }
        this.notConsumed = false;
    }

    public ItemStackTextRenderer(int chanceBase, int chanceBoost) {
        this.chanceBase = chanceBase;
        this.chanceBoost = chanceBoost;
        this.chanceLogic = null;
        this.notConsumed = false;
    }

    public ItemStackTextRenderer(boolean notConsumed) {
        this.chanceBase = -1;
        this.chanceBoost = -1;
        this.chanceLogic = null;
        this.notConsumed = notConsumed;
    }

    @Override
    public void render(@NotNull Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
        super.render(minecraft, xPosition, yPosition, ingredient);

        if (this.chanceBase >= 0) {
            GlStateManager.disableBlend();
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5, 0.5, 1);
            // z hackery to render the text above the item
            GlStateManager.translate(0, 0, 160);
            String s;
            if (chanceBase >= 100 || chanceBase == 0) {
                s = (this.chanceBase / 100) + "%";
            } else {
                s = "0." + this.chanceBase + "%";
            }
            if (this.chanceLogic != null && this.chanceLogic != ChancedOutputLogic.NONE &&
                    this.chanceLogic != ChancedOutputLogic.OR) {
                s += "*";
            } else if (this.chanceBoost > 0) {
                s += "+";
            }

            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            fontRenderer.drawStringWithShadow(s, (xPosition + 6) * 2 - fontRenderer.getStringWidth(s) + 19,
                    (yPosition + 1) * 2, 0xFFFF00);

            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
        } else if (this.notConsumed) {
            GlStateManager.disableBlend();
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5, 0.5, 1);
            // z hackery to render the text above the item
            GlStateManager.translate(0, 0, 160);

            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            fontRenderer.drawStringWithShadow("NC", (xPosition + 6) * 2 - fontRenderer.getStringWidth("NC") + 19,
                    (yPosition + 1) * 2, 0xFFFF00);

            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
        }
    }
}
