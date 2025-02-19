package gregtech.integration.jei.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;

import mezz.jei.api.recipe.IRecipeWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AdvancedRecipeWrapper implements IRecipeWrapper {

    protected final List<JeiButton> buttons = new ArrayList<>();
    protected final List<JeiInteractableText> jeiTexts = new ArrayList<>();

    public AdvancedRecipeWrapper() {
        initExtras();
    }

    public abstract void initExtras();

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        for (JeiButton button : buttons) {
            button.render(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
            if (button.isHovering(mouseX, mouseY)) {
                List<String> lines = new ArrayList<>();
                button.buildTooltip(lines);
                if (lines.isEmpty())
                    continue;
                Minecraft mc = Minecraft.getMinecraft();
                int width = (int) (mc.displayWidth / 2f + recipeWidth / 2f);
                int maxWidth = Math.min(200, width - mouseX - 5);
                GuiUtils.drawHoveringText(ItemStack.EMPTY, lines, mouseX, mouseY,
                        width,
                        mc.displayHeight, maxWidth, mc.fontRenderer);
                GlStateManager.disableLighting();
            }
        }

        for (JeiInteractableText text : jeiTexts) {
            text.render(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
            if (text.isHovering(mouseX, mouseY)) {
                List<String> tooltip = new ArrayList<>();
                text.buildTooltip(tooltip);
                if (tooltip.isEmpty()) continue;
                int width = (int) (minecraft.displayWidth / 2f + recipeWidth / 2f);
                GuiUtils.drawHoveringText(tooltip, mouseX, mouseY, width, minecraft.displayHeight,
                        Math.min(150, width - mouseX - 5), minecraft.fontRenderer);
                GlStateManager.disableLighting();
            }
        }
    }

    @Override
    public boolean handleClick(@NotNull Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        for (JeiButton button : buttons) {
            if (button.isHovering(mouseX, mouseY) &&
                    button.getClickAction().click(minecraft, mouseX, mouseY, mouseButton)) {
                Minecraft.getMinecraft().getSoundHandler()
                        .playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        for (JeiInteractableText text : jeiTexts) {
            if (text.isHovering(mouseX, mouseY) &&
                    text.getTextClickAction().click(minecraft, text, mouseX, mouseY, mouseButton)) {
                Minecraft.getMinecraft().getSoundHandler()
                        .playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        return false;
    }
}
