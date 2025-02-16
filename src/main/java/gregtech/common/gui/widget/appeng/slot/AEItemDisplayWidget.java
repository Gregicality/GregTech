package gregtech.common.gui.widget.appeng.slot;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.utils.RenderUtil;
import gregtech.common.gui.widget.appeng.AEListGridWidget;

import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;

/**
 * @Author GlodBlock
 * @Description Display a certain {@link IAEItemStack} element.
 * @Date 2023/4/19-21:23
 */
public class AEItemDisplayWidget extends Widget {

    private final AEListGridWidget<IAEItemStack> gridWidget;
    private final int index;

    public AEItemDisplayWidget(int x, int y, AEListGridWidget<IAEItemStack> gridWidget, int index) {
        super(new Position(x, y), new Size(18, 18));
        this.gridWidget = gridWidget;
        this.index = index;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        Position position = getPosition();
        IAEItemStack item = this.gridWidget.getAt(this.index);
        GuiTextures.SLOT.draw(position.x, position.y, 18, 18);
        GuiTextures.NUMBER_BACKGROUND.draw(position.x + 18, position.y, 140, 18);
        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (item != null) {
            ItemStack realStack = item.createItemStack();
            realStack.setCount(1);
            RenderUtil.drawItemStack(realStack, stackX, stackY, null);
            String amountStr = String.format("x%,d", item.getStackSize());
            drawText(amountStr, stackX + 20, stackY + 5, 1, 0xFFFFFFFF);
        }
        if (isMouseOverElement(mouseX, mouseY)) {
            drawSelectionOverlay(stackX, stackY, 16, 16);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            IAEItemStack item = this.gridWidget.getAt(this.index);
            if (item != null) {
                drawHoveringText(item.createItemStack(), getItemToolTip(item.createItemStack()), -1, mouseX, mouseY);
            }
        }
    }
}
