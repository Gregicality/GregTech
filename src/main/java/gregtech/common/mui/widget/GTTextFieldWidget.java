package gregtech.common.mui.widget;

import net.minecraft.client.renderer.GlStateManager;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTextFieldTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import com.cleanroommc.modularui.widgets.textfield.BaseTextFieldWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldHandler;
import com.cleanroommc.modularui.widgets.textfield.TextFieldRenderer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.ParsePosition;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.cleanroommc.modularui.widgets.textfield.TextFieldWidget.parse;

public class GTTextFieldWidget extends BaseTextFieldWidget<GTTextFieldWidget> {

    private IStringValue<?> stringValue;
    private Function<String, String> validator = val -> val;
    private boolean numbers = false;
    private final GTTextFieldRenderer renderer;

    protected boolean changedMarkedColor = false;

    public GTTextFieldWidget() {
        this.renderer = new GTTextFieldRenderer(this.handler);
        super.renderer = this.renderer;
    }

    @Override
    public void onInit() {
        super.onInit();
        if (this.stringValue == null) {
            this.stringValue = new StringValue("");
        }
        setText(this.stringValue.getStringValue());
        if (!hasTooltip()) {
            tooltipBuilder(tooltip -> tooltip.addLine(IKey.str(getText())));
        }
        if (!this.changedMarkedColor) {
            this.renderer.setMarkedColor(getMarkedColor());
        }
    }

    public int getMarkedColor() {
        WidgetTheme theme = getWidgetTheme(getContext().getTheme());
        if (theme instanceof WidgetTextFieldTheme textFieldTheme) {
            return textFieldTheme.getMarkedColor();
        }
        return ITheme.getDefault().getTextFieldTheme().getMarkedColor();
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof IStringValue<?>iStringValue &&
                syncHandler instanceof ValueSyncHandler<?>valueSyncHandler) {
            this.stringValue = iStringValue;
            valueSyncHandler.setChangeListener(() -> {
                markTooltipDirty();
                setText(this.stringValue.getValue().toString());
            });
            return true;
        }
        return false;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isFocused()) {
            String s = this.stringValue.getStringValue();
            if (!getText().equals(s)) {
                setText(s);
            }
        }
    }

    @Override
    public void drawText(ModularGuiContext context) {
        this.renderer.setSimulate(false);
        this.renderer.setPos(getArea().getPadding().left, 0);
        this.renderer.setScale(this.scale);
        this.renderer.setAlignment(this.textAlignment, -1, getArea().height);
        this.renderer.draw(this.handler.getText());
        getScrollData().setScrollSize(Math.max(0, (int) this.renderer.getLastWidth()));
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        if (hasTooltip() && getScrollData().isScrollBarActive(getScrollArea()) &&
                isHoveringFor(getTooltip().getShowUpTimer())) {
            getTooltip().draw(getContext());
        }
    }

    public GTTextFieldWidget setPostFix(String postFix) {
        return setPostFix(() -> postFix);
    }

    public GTTextFieldWidget setPostFix(Supplier<String> postFix) {
        return setPostFix(IKey.dynamic(postFix));
    }

    public GTTextFieldWidget setPostFix(IKey postFix) {
        this.renderer.setPostFix(postFix);
        return getThis();
    }

    @NotNull
    public String getText() {
        if (this.handler.getText().isEmpty()) {
            return "";
        }
        if (this.handler.getText().size() > 1) {
            throw new IllegalStateException("GTTextFieldWidget can only have one line!");
        }
        return this.handler.getText().get(0);
    }

    public void setText(@NotNull String text) {
        if (this.handler.getText().isEmpty()) {
            this.handler.getText().add(text);
        } else {
            this.handler.getText().set(0, text);
        }
    }

    @Override
    public void onFocus(ModularGuiContext context) {
        super.onFocus(context);
        Point main = this.handler.getMainCursor();
        if (main.x == 0) {
            this.handler.setCursor(main.y, getText().length(), true, true);
        }
    }

    @Override
    public void onRemoveFocus(ModularGuiContext context) {
        super.onRemoveFocus(context);
        this.setText(this.validator.apply(getText()));
        this.stringValue
                .setStringValue(this.numbers ? format.parse(getText(), new ParsePosition(0)).toString() : getText());
    }

    @Override
    public boolean canHover() {
        return true;
    }

    public GTTextFieldWidget setMaxLength(int maxLength) {
        this.handler.setMaxCharacters(maxLength);
        return this;
    }

    public GTTextFieldWidget setPattern(Pattern pattern) {
        this.handler.setPattern(pattern);
        return this;
    }

    public GTTextFieldWidget setTextColor(int textColor) {
        this.renderer.setColor(textColor);
        this.changedTextColor = true;
        return this;
    }

    public GTTextFieldWidget setMarkedColor(int color) {
        this.renderer.setMarkedColor(color);
        this.changedMarkedColor = true;
        return this;
    }

    public GTTextFieldWidget setValidator(Function<String, String> validator) {
        this.validator = validator;
        return this;
    }

    public GTTextFieldWidget setNumbersLong(Function<Long, Long> validator) {
        this.numbers = true;
        setValidator(val -> {
            long num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                num = (long) parse(val).doubleValue();
            }
            return format.format(validator.apply(num));
        });
        return this;
    }

    public GTTextFieldWidget setNumbers(Function<Integer, Integer> validator) {
        this.numbers = true;
        return setValidator(val -> {
            int num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                num = (int) parse(val).doubleValue();
            }
            return format.format(validator.apply(num));
        });
    }

    public GTTextFieldWidget setNumbersDouble(Function<Double, Double> validator) {
        this.numbers = true;
        return setValidator(val -> {
            double num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                num = parse(val).doubleValue();
            }
            return format.format(validator.apply(num));
        });
    }

    public GTTextFieldWidget setNumbers(Supplier<Integer> min, Supplier<Integer> max) {
        return setNumbers(val -> Math.min(max.get(), Math.max(min.get(), val)));
    }

    public GTTextFieldWidget setNumbersLong(Supplier<Long> min, Supplier<Long> max) {
        return setNumbersLong(val -> Math.min(max.get(), Math.max(min.get(), val)));
    }

    public GTTextFieldWidget setNumbers(int min, int max) {
        return setNumbers(val -> Math.min(max, Math.max(min, val)));
    }

    public GTTextFieldWidget setNumbers() {
        return setNumbers(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public GTTextFieldWidget value(IStringValue<?> stringValue) {
        this.stringValue = stringValue;
        setValue(stringValue);
        return this;
    }

    private static class GTTextFieldRenderer extends TextFieldRenderer {

        IKey postFix = IKey.EMPTY;

        public GTTextFieldRenderer(TextFieldHandler handler) {
            super(handler);
        }

        @Override
        protected void draw(String text, float x, float y) {
            if (this.simulate) return;
            GlStateManager.disableBlend();
            GlStateManager.pushMatrix();
            GlStateManager.scale(this.scale, this.scale, 0f);
            getFontRenderer().drawString(text + this.postFix.getFormatted(),
                    x / this.scale, y / this.scale,
                    this.color, this.shadow);
            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
        }

        public void setPostFix(IKey postFix) {
            this.postFix = postFix;
        }

        @Override
        public Point getCursorPos(List<String> lines, int x, int y) {
            if (lines.isEmpty()) {
                return new Point();
            }
            List<Line> measuredLines = measureLines(lines);
            y -= getStartY(measuredLines.size()) + this.y;
            int index = (int) (y / (getFontHeight()));
            if (index < 0) return new Point();
            if (index >= measuredLines.size())
                return new Point(measuredLines.get(measuredLines.size() - 1).getText().length(),
                        measuredLines.size() - 1);
            Line line = measuredLines.get(index);
            x -= getStartX(line.getWidth()) + this.x;
            if (x < 0) return new Point(0, index);
            if (x > line.getWidth()) return new Point(line.getText().length(), index);
            float currentX = 0;
            for (int i = 0; i < line.getText().length(); i++) {
                char c = line.getText().charAt(i);
                float cw = getFontRenderer().getCharWidth(c) * this.scale;
                currentX += cw;
                if (currentX >= x + (cw / 2)) {
                    return new Point(i + 1, index);
                }
            }
            return new Point();
        }
    }
}
