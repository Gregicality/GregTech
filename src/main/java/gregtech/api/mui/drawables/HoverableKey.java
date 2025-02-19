package gregtech.api.mui.drawables;

import net.minecraft.client.gui.FontRenderer;

import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IHoverable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.ITooltip;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Box;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HoverableKey implements IIcon, IHoverable, ITooltip<HoverableKey> {

    private final Box margin = new Box();
    private final Area area = new Area();
    private final List<IDrawable> tooltipLines = new ArrayList<>();
    private RichTooltip tooltip;
    private IKey key;

    private HoverableKey() {
        tooltipAutoUpdate(true);
        tooltipBuilder(t -> t.addDrawableLines(getTooltipLines()));
    }

    public static HoverableKey of(IKey key) {
        return new HoverableKey().setKey(key);
    }

    public static HoverableKey of(IKey key, IDrawable... lines) {
        return of(key).addLines(Arrays.asList(lines));
    }

    public FontRenderer getFontRenderer() {
        return MCHelper.getFontRenderer();
    }

    @Override
    public int getWidth() {
        return getFontRenderer().getStringWidth(key.get()) + this.margin.horizontal();
    }

    @Override
    public int getHeight() {
        return getFontRenderer().FONT_HEIGHT + this.margin.vertical();
    }

    @Override
    public Box getMargin() {
        return margin;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        int w = getWidth(), h = getHeight();
        x += (int) (width / 2f - w / 2f);
        y += (int) (height / 2f - h / 2f);
        this.key.draw(context, x, y, width, height, widgetTheme);
    }

    public IKey getKey() {
        return key;
    }

    public HoverableKey setKey(IKey key) {
        this.key = key;
        return getThis();
    }

    public List<IDrawable> getTooltipLines() {
        return tooltipLines;
    }

    public HoverableKey addLines(Collection<IDrawable> drawables) {
        this.getTooltipLines().addAll(drawables);
        return getThis();
    }

    @Override
    @Nullable
    public RichTooltip getTooltip() {
        return tooltip;
    }

    @Override
    public void setRenderedAt(int x, int y) {
        getRenderedArea().setPos(x, y);
    }

    @Override
    public Area getRenderedArea() {
        this.area.setSize(getWidth(), getHeight());
        return this.area;
    }

    @Override
    public @NotNull RichTooltip tooltip() {
        if (this.tooltip == null) this.tooltip = new RichTooltip(area -> area.set(getRenderedArea()));
        return tooltip;
    }

    @Override
    public HoverableKey tooltip(RichTooltip tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public String toString() {
        return "HoverableKey(" + key.getFormatted() + ")";
    }
}
