package gregtech.common.gui.widget.appeng.slot;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.common.gui.widget.appeng.AEItemConfigWidget;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.data.IAEItemStack;
import com.google.common.collect.Lists;
import mezz.jei.api.gui.IGhostIngredientHandler;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class AEItemConfigSlot extends AEConfigSlot<IAEItemStack> {

    public AEItemConfigSlot(int x, int y, AEItemConfigWidget widget, int index) {
        super(new Position(x, y), new Size(18 * 6, 18), widget, index);
    }

    @Override
    public AEItemConfigWidget getParentWidget() {
        return (AEItemConfigWidget) super.getParentWidget();
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        AEItemConfigWidget pw = getParentWidget();
        Position position = getPosition();
        IConfigurableSlot<IAEItemStack> slot = pw.getDisplay(this.index);
        IAEItemStack config = slot.getConfig();
        IAEItemStack stock = slot.getStock();
        drawSlots(pw.isAutoPull(), position.x, position.y);
        if (this.select) {
            GuiTextures.SELECT_BOX.draw(position.x, position.y, 18, 18);
        }
        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (config != null) {
            ItemStack stack = config.createItemStack();
            stack.setCount(1);
            RenderUtil.drawItemStack(stack, stackX, stackY, null);

            // Only draw the config amount if not stocking, as its meaningless when stocking
            if (!pw.isStocking()) {
                String amountStr = TextFormattingUtil.formatLongToCompactString(config.getStackSize(), 4);
                drawStringFixedCorner(amountStr, stackX + 17, stackY + 17, 16777215, true, 0.5f);
            }
        }
        if (stock != null) {
            ItemStack stack = stock.createItemStack();
            stack.setCount(1);
            RenderUtil.drawItemStack(stack, stackX + DISPLAY_X_OFFSET, stackY, null);
            String amountStr = TextFormattingUtil.formatLongToCompactString(stock.getStackSize(), 4);
            drawStringFixedCorner(amountStr, stackX + DISPLAY_X_OFFSET + 17, stackY + 17, 16777215, true, 0.5f);
        }
        if (mouseOverConfig(mouseX, mouseY)) {
            drawSelectionOverlay(stackX, stackY, 16, 16);
        } else if (mouseOverStock(mouseX, mouseY)) {
            drawSelectionOverlay(stackX + DISPLAY_X_OFFSET, stackY, 16, 16);
        }
    }

    private void drawSlots(boolean autoPull, int x, int y) {
        if (autoPull) {
            GuiTextures.SLOT_DARK.draw(x, y, 18, 18);
            GuiTextures.CONFIG_ARROW.draw(x, y, 18, 18);
        } else {
            GuiTextures.SLOT.draw(x, y, 18, 18);
            GuiTextures.CONFIG_ARROW_DARK.draw(x, y, 18, 18);
        }
        GuiTextures.SLOT_DARK.draw(x + DISPLAY_X_OFFSET, y, 18, 18);
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        IAEItemStack item = null;
        IConfigurableSlot<IAEItemStack> slot = this.getParentWidget().getDisplay(this.index);
        if (mouseOverConfig(mouseX, mouseY)) {
            item = slot.getConfig();
        } else if (mouseOverStock(mouseX, mouseY)) {
            item = slot.getStock();
        }
        if (item != null) {
            drawHoveringText(item.createItemStack(), getItemToolTip(item.createItemStack()), -1, mouseX, mouseY);
        }
    }

    @Override
    protected void addHoverText(List<String> hoverText) {
        if (getParentWidget().isAutoPull()) {
            hoverText.add(I18n.format("gregtech.gui.config_slot"));
            hoverText.add(I18n.format("gregtech.gui.config_slot.auto_pull_managed"));
        } else {
            super.addHoverText(hoverText);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        AEItemConfigWidget pw = getParentWidget();
        // don't allow manual interaction with config slots when auto pull is enabled
        if (pw.isAutoPull()) {
            return false;
        }

        if (mouseOverConfig(mouseX, mouseY)) {
            if (button == 1) {
                // Right click to clear
                writeClientAction(REMOVE_ID, buf -> {});

                if (!pw.isStocking()) {
                    pw.disableAmount();
                }
            } else if (button == 0) {
                // Left click to set/select
                ItemStack item = this.gui.entityPlayer.inventory.getItemStack();

                if (!item.isEmpty()) {
                    writeClientAction(UPDATE_ID, buf -> buf.writeItemStack(item));
                    return true;
                }

                if (!pw.isStocking()) {
                    pw.enableAmount(this.index);
                    this.select = true;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        IConfigurableSlot<IAEItemStack> slot = this.parentWidget.getConfig(this.index);
        if (id == REMOVE_ID) {
            slot.setConfig(null);
            this.parentWidget.disableAmount();
            writeUpdateInfo(REMOVE_ID, buf -> {});
        }
        if (id == UPDATE_ID) {
            try {
                ItemStack item = buffer.readItemStack();
                if (!isItemValidForSlot(item)) return;
                slot.setConfig(WrappedItemStack.fromItemStack(item));
                this.parentWidget.enableAmount(this.index);
                if (!item.isEmpty()) {
                    writeUpdateInfo(UPDATE_ID, buf -> buf.writeItemStack(item));
                }
            } catch (IOException ignored) {}
        }
        if (id == AMOUNT_CHANGE_ID) {
            if (slot.getConfig() != null) {
                int amt = buffer.readInt();
                slot.getConfig().setStackSize(amt);
                writeUpdateInfo(AMOUNT_CHANGE_ID, buf -> buf.writeInt(amt));
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        IConfigurableSlot<IAEItemStack> slot = this.parentWidget.getDisplay(this.index);
        if (id == REMOVE_ID) {
            slot.setConfig(null);
        }
        if (id == UPDATE_ID) {
            try {
                ItemStack item = buffer.readItemStack();
                slot.setConfig(WrappedItemStack.fromItemStack(item));
            } catch (IOException ignored) {}
        }
        if (id == AMOUNT_CHANGE_ID) {
            if (slot.getConfig() != null) {
                int amt = buffer.readInt();
                slot.getConfig().setStackSize(amt);
            }
        }
    }

    // Method for server-side validation of an attempted new configured item
    private boolean isItemValidForSlot(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return true;
        AEItemConfigWidget pw = getParentWidget();
        if (!pw.isStocking()) return true;
        return !pw.hasStackInConfig(stack);
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof ItemStack)) {
            return Collections.emptyList();
        }
        Rectangle rectangle = toRectangleBox();
        rectangle.width /= 6;
        return Lists.newArrayList(new IGhostIngredientHandler.Target<>() {

            @NotNull
            @Override
            public Rectangle getArea() {
                return rectangle;
            }

            @Override
            public void accept(@NotNull Object ingredient) {
                if (ingredient instanceof ItemStack) {
                    writeClientAction(UPDATE_ID, buf -> buf.writeItemStack((ItemStack) ingredient));
                }
            }
        });
    }

    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        // Only allow the amount scrolling if not stocking, as amount is useless for stocking
        if (parentWidget.isStocking()) return false;
        IConfigurableSlot<IAEItemStack> slot = this.parentWidget.getDisplay(this.index);
        Rectangle rectangle = toRectangleBox();
        rectangle.width /= 6;
        if (slot.getConfig() == null || wheelDelta == 0 || !rectangle.contains(mouseX, mouseY)) {
            return false;
        }
        ItemStack stack = slot.getConfig().createItemStack();
        long amt;
        if (isCtrlDown()) {
            amt = wheelDelta > 0 ? stack.getCount() * 2L : stack.getCount() / 2L;
        } else {
            amt = wheelDelta > 0 ? stack.getCount() + 1L : stack.getCount() - 1L;
        }
        if (amt > 0 && amt < Integer.MAX_VALUE + 1L) {
            int finalAmt = (int) amt;
            writeClientAction(AMOUNT_CHANGE_ID, buf -> buf.writeInt(finalAmt));
            return true;
        }
        return false;
    }
}
