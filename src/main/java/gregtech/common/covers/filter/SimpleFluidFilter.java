package gregtech.common.covers.filter;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.mui.GTGuis;
import gregtech.common.covers.filter.readers.SimpleFluidFilterReader;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SimpleFluidFilter extends BaseFilter {

    private static final int MAX_FLUID_SLOTS = 9;

    private final SimpleFluidFilterReader filterReader;

    public SimpleFluidFilter(ItemStack stack) {
        filterReader = new SimpleFluidFilterReader(stack, MAX_FLUID_SLOTS);
    }

    @Override
    public SimpleFluidFilterReader getFilterReader() {
        return filterReader;
    }

    public void configureFilterTanks(int amount) {
        this.filterReader.setFluidAmounts(amount);
        this.markDirty();
    }

    @Override
    public @NotNull ModularPanel createPopupPanel(PanelSyncManager syncManager) {
        return GTGuis.createPopupPanel("simple_fluid_filter", 98, 81, false)
                .padding(4)
                .child(CoverWithUI.createTitleRow(getContainerStack()))
                .child(createWidgets(syncManager).top(22));
    }

    @Override
    public @NotNull ModularPanel createPanel(PanelSyncManager syncManager) {
        return GTGuis.createPanel(getContainerStack(), 176, 168);
    }

    @Override
    public @NotNull Widget<?> createWidgets(PanelSyncManager syncManager) {
        return Flow.row().coverChildrenHeight().widthRel(1f)
                .child(SlotGroupWidget.builder()
                        .matrix("FFF",
                                "FFF",
                                "FFF")
                        .key('F', i -> new GTFluidSlot()
                                .syncHandler(GTFluidSlot.sync(filterReader.getFluidTank(i))
                                        .phantom(true)
                                        .showAmountOnSlot(getFilterReader()::shouldShowAmount)
                                        .showAmountInTooltip(getFilterReader()::shouldShowAmount)))
                        .build().marginRight(4))
                .child(createBlacklistUI());
    }

    @Override
    public MatchResult matchFluid(FluidStack fluidStack) {
        int index = -1;
        FluidStack returnable = null;
        for (int i = 0; i < filterReader.getSize(); i++) {
            var fluid = filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(fluidStack)) {
                index = i;
                returnable = fluid.copy();
                break;
            }
        }
        return MatchResult.create(index != -1, returnable, index);
    }

    @Override
    public boolean testFluid(FluidStack toTest) {
        for (int i = 0; i < filterReader.getSize(); i++) {
            var fluid = filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(toTest)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup) {
        for (int i = 0; i < 9; ++i) {
            widgetGroup.accept((new gregtech.api.gui.widgets.PhantomFluidWidget(10 + 18 * (i % 3), 18 * (i / 3), 18, 18,
                    filterReader.getFluidTank(i)))
                            .setBackgroundTexture(gregtech.api.gui.GuiTextures.SLOT));
        }
    }

    @Override
    public int getTransferLimit(FluidStack fluidStack, int transferSize) {
        int limit = 0;

        for (int i = 0; i < this.filterReader.getSize(); i++) {
            var fluid = this.filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(fluidStack)) {
                limit = fluid.amount;
            }
        }
        return isBlacklistFilter() ? transferSize : limit;
    }

    @Override
    public FilterType getType() {
        return FilterType.FLUID;
    }
}
