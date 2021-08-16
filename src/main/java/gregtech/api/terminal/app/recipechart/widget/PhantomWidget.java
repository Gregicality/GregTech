package gregtech.api.terminal.app.recipechart.widget;

import gregtech.api.gui.Widget;
import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.gui.widgets.PhantomFluidWidget;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.common.inventory.handlers.SingleItemStackHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PhantomWidget extends WidgetGroup implements IGhostIngredientTarget {
    private final IItemHandlerModifiable itemHandler;
    private FluidStack fluidStack;
    private PhantomFluidWidget fluidWidget;
    private PhantomSlotWidget slotWidget;
    private Consumer<Object> onChanged;

    public PhantomWidget(int x, int y, Object defaultObj) {
        super(x, y, 18, 18);
        itemHandler = new SingleItemStackHandler(1);
        fluidStack = null;
        fluidWidget = new PhantomFluidWidget(0, 0, 18, 18, null, null)
        .setFluidStackUpdater(fluid -> {
            fluidStack = fluid.copy();
            if (fluidStack != null && fluidStack.amount > 0) {
                itemHandler.setStackInSlot(0, ItemStack.EMPTY);
                slotWidget.setVisible(false);
                fluidWidget.setVisible(true);
                if (onChanged != null) {
                    onChanged.accept(fluidStack);
                }
            }
        }, true).setBackgroundTexture(TerminalTheme.COLOR_B_2).showTip(true)
        .setFluidStackSupplier(() -> fluidStack,true);
        slotWidget = new PhantomSlotWidget(itemHandler, 0, 0, 0);
        slotWidget.setChangeListener(()-> {
            if (!itemHandler.getStackInSlot(0).isEmpty()) {
                fluidStack = null;
                fluidWidget.setVisible(false);
                slotWidget.setVisible(true);
                if (onChanged != null) {
                    onChanged.accept(itemHandler.getStackInSlot(0));
                }
            }
        }).setBackgroundTexture(TerminalTheme.COLOR_B_2);
        this.addWidget(fluidWidget);
        this.addWidget(slotWidget);

        if (defaultObj instanceof ItemStack) {
            itemHandler.setStackInSlot(0, (ItemStack) defaultObj);
            fluidWidget.setVisible(false);
            slotWidget.setVisible(true);
        } else if (defaultObj instanceof FluidStack) {
            fluidStack = (FluidStack) defaultObj;
            slotWidget.setVisible(false);
            fluidWidget.setVisible(true);
        }
    }

    public PhantomWidget setChangeListener(Consumer<Object> onChanged) {
        this.onChanged = onChanged;
        return this;
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        if (!isVisible()) {
            return Collections.emptyList();
        }
        ArrayList<IGhostIngredientHandler.Target<?>> targets = new ArrayList<>();
        for (Widget widget : widgets) {
            if (widget instanceof IGhostIngredientTarget) {
                targets.addAll(((IGhostIngredientTarget) widget).getPhantomTargets(ingredient));
            }
        }
        return targets;
    }
}
