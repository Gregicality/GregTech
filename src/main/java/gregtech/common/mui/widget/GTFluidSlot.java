package gregtech.common.mui.widget;

import gregtech.api.GTValues;
import gregtech.api.mui.sync.GTFluidSyncHandler;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GTFluidSlot extends Widget<GTFluidSlot> implements Interactable, JeiIngredientProvider,
                               JeiGhostIngredientSlot<FluidStack> {

    private final TextRenderer textRenderer = new TextRenderer();
    private GTFluidSyncHandler syncHandler;
    private boolean disableBackground = false;

    public GTFluidSlot() {
        tooltip().setAutoUpdate(true);
        // .setHasTitleMargin(true);
        tooltipBuilder(tooltip -> {
            if (!isSynced()) return;
            var fluid = this.syncHandler.getFluid();

            if (fluid == null)
                fluid = this.syncHandler.getLockedFluid();

            if (fluid == null) return;

            tooltip.addLine(IKey.str(fluid.getLocalizedName()));
            if (this.syncHandler.showAmountInTooltip())
                tooltip.addLine(IKey.lang("gregtech.fluid.amount", fluid.amount, this.syncHandler.getCapacity()));

            if (this.syncHandler.isPhantom() && this.syncHandler.showAmountInTooltip())
                tooltip.addLine(IKey.lang("modularui.fluid.phantom.control"));

            // Add various tooltips from the material
            for (String s : FluidTooltipUtil.getFluidTooltip(fluid)) {
                if (s.isEmpty()) continue;
                tooltip.addLine(IKey.str(s));
            }

            if (this.syncHandler.showAmountInTooltip())
                addIngotMolFluidTooltip(fluid, tooltip);
        });
    }

    public static GTFluidSyncHandler sync(IFluidTank tank) {
        return new GTFluidSyncHandler(tank);
    }

    @Override
    public void onInit() {
        this.textRenderer.setShadow(true);
        this.textRenderer.setScale(0.5f);
        this.textRenderer.setColor(Color.WHITE.main);
        getContext().getJeiSettings().addJeiGhostIngredientSlot(this);
    }

    public GTFluidSlot syncHandler(IFluidTank fluidTank) {
        return syncHandler(sync(fluidTank));
    }

    public GTFluidSlot syncHandler(GTFluidSyncHandler syncHandler) {
        setSyncHandler(syncHandler);
        this.syncHandler = syncHandler;
        return this;
    }

    public GTFluidSlot disableBackground() {
        this.disableBackground = true;
        return this;
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof GTFluidSyncHandler;
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (disableBackground) return;
        super.drawBackground(context, widgetTheme);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (widgetTheme instanceof WidgetSlotTheme slotTheme) {
            draw(context, slotTheme);
        }
    }

    public void draw(ModularGuiContext context, WidgetSlotTheme widgetTheme) {
        FluidStack content = this.syncHandler.getFluid();
        if (content == null)
            content = this.syncHandler.getLockedFluid();

        float height = getArea().h() - 2;
        int y = 1;

        if (!this.syncHandler.drawAlwaysFull()) {
            float newHeight = height * ((float) content.amount / this.syncHandler.getCapacity());
            y += (int) (height - newHeight);
            height = newHeight;
        }

        GuiDraw.drawFluidTexture(content, 1, y, getArea().w() - 2, height, 0);

        if (content != null && this.syncHandler.showAmountOnSlot()) {
            String amount = NumberFormat.formatWithMaxDigits(content.amount, 3) + "L";
            this.textRenderer.setAlignment(Alignment.CenterRight, getArea().width - 1f);
            this.textRenderer.setPos(0, 12);
            this.textRenderer.draw(amount);
        }

        if (isHovering()) {
            GlStateManager.colorMask(true, true, true, false);
            GuiDraw.drawRect(1, 1, getArea().w() - 2, getArea().h() - 2, widgetTheme.getSlotHoverColor());
            GlStateManager.colorMask(true, true, true, true);
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        var data = MouseData.create(mouseButton);
        if (this.syncHandler.canFillSlot() || this.syncHandler.canDrainSlot()) {
            this.syncHandler.handleClick(data);

            if (this.syncHandler.canLockFluid())
                this.syncHandler.toggleLockFluid();

            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        if (!this.syncHandler.isPhantom()) return false;
        if ((scrollDirection.isUp() && !this.syncHandler.canFillSlot()) ||
                (scrollDirection.isDown() && !this.syncHandler.canDrainSlot())) {
            return false;
        }
        MouseData mouseData = MouseData.create(scrollDirection.modifier);
        this.syncHandler.handlePhantomScroll(mouseData);
        return true;
    }

    @Override
    protected WidgetTheme getWidgetThemeInternal(ITheme theme) {
        return theme.getFluidSlotTheme();
    }

    public static void addIngotMolFluidTooltip(FluidStack fluidStack, RichTooltip tooltip) {
        // Add tooltip showing how many "ingot moles" (increments of 144) this fluid is if shift is held
        if (TooltipHelper.isShiftDown() && fluidStack.amount > GTValues.L) {
            int numIngots = fluidStack.amount / GTValues.L;
            int extra = fluidStack.amount % GTValues.L;
            String fluidAmount = String.format(" %,d L = %,d * %d L", fluidStack.amount, numIngots, GTValues.L);
            if (extra != 0) {
                fluidAmount += String.format(" + %d L", extra);
            }
            tooltip.add(TextFormatting.GRAY + LocalizationUtils.format("gregtech.gui.amount_raw") + fluidAmount);
        }
    }

    @Override
    public void setGhostIngredient(@NotNull FluidStack ingredient) {
        if (this.syncHandler.isPhantom()) {
            this.syncHandler.setFluid(ingredient);
            this.syncHandler.syncToServer(GTFluidSyncHandler.UPDATE_TANK,
                    buffer -> NetworkUtils.writeFluidStack(buffer, ingredient));
        } else {
            this.syncHandler.lockFluid(ingredient, true);
        }
    }

    @Override
    public @Nullable FluidStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        if (ingredient instanceof FluidStack stack) {
            return stack;
        } else if (ingredient instanceof ItemStack stack &&
                stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                    if (stack.getCount() > 1) stack = GTUtility.copy(1, stack);

                    var handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                    return handler == null ? null : handler.drain(Integer.MAX_VALUE, true);
                }
        return null;
    }

    @Override
    public @Nullable Object getIngredient() {
        return this.syncHandler.getFluid();
    }
}
