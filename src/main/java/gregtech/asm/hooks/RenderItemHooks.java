package gregtech.asm.hooks;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.client.renderer.handler.LampItemOverlayRenderer;
import gregtech.client.utils.ToolChargeBarRenderer;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class RenderItemHooks {

    public static void renderElectricBar(@Nonnull ItemStack stack, int xPosition, int yPosition) {
        if (stack.getItem() instanceof IGTTool) {
            ToolChargeBarRenderer.renderBarsTool((IGTTool) stack.getItem(), stack, xPosition, yPosition);
        } else if (stack.getItem() instanceof MetaItem) {
            ToolChargeBarRenderer.renderBarsItem((MetaItem<?>) stack.getItem(), stack, xPosition, yPosition);
        }
    }

    public static void renderLampOverlay(@Nonnull ItemStack stack, int xPosition, int yPosition) {
        LampItemOverlayRenderer.OverlayType overlayType = LampItemOverlayRenderer.getOverlayType(stack);
        if (overlayType != LampItemOverlayRenderer.OverlayType.NONE) {
            LampItemOverlayRenderer.renderOverlay(overlayType, xPosition, yPosition);
        }
    }
}
