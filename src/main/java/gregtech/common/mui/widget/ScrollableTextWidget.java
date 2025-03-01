package gregtech.common.mui.widget;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.drawable.IHoverable;
import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.drawable.text.RichText;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.HoveredWidgetList;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.scroll.ScrollArea;
import com.cleanroommc.modularui.widget.scroll.ScrollData;
import com.cleanroommc.modularui.widget.sizer.Area;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ScrollableTextWidget extends Widget<ScrollableTextWidget>
                                  implements IRichTextBuilder<ScrollableTextWidget>, Interactable, IViewport {

    private final RichText text = new RichText();
    private Consumer<RichText> builder;
    private boolean dirty = false;
    private boolean autoUpdate = false;

    private final ScrollArea scroll = new ScrollArea();
    private final TextRenderer renderer = new TextRenderer();

    public ScrollableTextWidget() {
        listenGuiAction((IGuiAction.MouseReleased) mouseButton -> {
            this.scroll.mouseReleased(getContext());
            return false;
        });
    }

    @Override
    public void onInit() {
        this.scroll.setScrollData(ScrollData.of(GuiAxis.Y));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.scroll.drag(getContext().getAbsMouseX(), getContext().getAbsMouseY());
    }

    public void markDirty() {
        this.dirty = true;
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        super.drawForeground(context);
        if (getHoveredElement() instanceof IHoverable hoverable) {
            hoverable.onHover();
            RichTooltip tooltip = hoverable.getTooltip();
            if (tooltip != null) {
                tooltip.draw(context);
            }
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        ModularGuiContext context = getContext();
        if (this.scroll.mouseClicked(context)) {
            return Result.STOP;
        }
        if (getHoveredElement() instanceof Interactable interactable) {
            return interactable.onMousePressed(mouseButton);
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        if (this.scroll.mouseScroll(getContext())) {
            return true;
        }
        if (getHoveredElement() instanceof Interactable interactable) {
            return interactable.onMouseScroll(scrollDirection, amount);
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        this.scroll.mouseReleased(getContext());
        if (getHoveredElement() instanceof Interactable interactable) {
            return interactable.onMouseRelease(mouseButton);
        }
        return false;
    }

    @Nullable
    public Object getHoveredElement() {
        if (!isHovering()) return null;
        return this.text.getHoveringElement(getContext());
    }

    @Override
    public Area getArea() {
        return getScrollArea();
    }

    public ScrollArea getScrollArea() {
        return this.scroll;
    }

    @Override
    public void transformChildren(IViewportStack stack) {
        stack.translate(0, -getScrollY());
    }

    @Override
    public void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (isInside(stack, x, y)) {
            widgets.add(this, stack.peek());
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {}

    @Override
    public void onResized() {
        this.scroll.getScrollY().clamp(this.scroll);
    }

    @Override
    public boolean canHover() {
        return super.canHover() ||
                this.scroll.isInsideScrollbarArea(getContext().getMouseX(), getContext().getMouseY());
    }

    @Override
    public void preDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.applyAtZero(this.scroll, context);
        } else {
            drawText(context);
        }
    }

    private void drawText(ModularGuiContext context) {
        if (this.autoUpdate || this.dirty) {
            if (this.builder != null) {
                this.text.clearText();
                this.builder.accept(this.text);
            }
            this.dirty = false;
        }
        this.text.setupRenderer(this.renderer, getArea().getPadding().left, getArea().getPadding().top - getScrollY(),
                getArea().paddedWidth(), getArea().paddedHeight(),
                getWidgetTheme(context.getTheme()).getTextColor(),
                getWidgetTheme(context.getTheme()).getTextShadow());
        this.text.compileAndDraw(this.renderer, context, false);
        int diff = ((int) this.renderer.getLastHeight() - getArea().h()) / 2;
        this.scroll.getScrollY().setScrollSize(getArea().h() + Math.max(0, diff));
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.remove();
            this.scroll.drawScrollbar();
        }
    }

    public int getScrollY() {
        return this.scroll.getScrollY() != null ? this.scroll.getScrollY().getScroll() : 0;
    }

    @Override
    public IRichTextBuilder<?> getRichText() {
        return this.text;
    }

    /**
     * Sets the auto update property. If auto update is true the text will be deleted each time it is drawn.
     * If {@link #builder} is not null, it will then be called.
     *
     * @param autoUpdate auto update
     * @return this
     */
    public ScrollableTextWidget autoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        return this;
    }

    /**
     * A builder which is called every time before drawing when {@link #dirty} is true.
     *
     * @param builder text builder
     * @return this
     */
    public ScrollableTextWidget textBuilder(Consumer<RichText> builder) {
        this.builder = builder;
        markDirty();
        return this;
    }
}
