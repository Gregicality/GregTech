package gregtech.api.metatileentity.multiblock.ui;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IDistinctBusController;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ProgressBarMultiblock;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTLog;
import gregtech.api.util.KeyUtil;

import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class MultiblockUIFactory {

    private final MultiblockWithDisplayBase mte;
    protected Consumer<MultiblockUIBuilder> displayText, warningText, errorText;
    protected BiFunction<PosGuiData, PanelSyncManager, IWidget> flexButton = (guiData, syncManager) -> null;
    private int width = 198, height = 202;
    private int screenHeight = 109;
    private Consumer<List<IWidget>> childrenConsumer;

    static {
        // register operations
        Operation.init();
    }

    public MultiblockUIFactory(@NotNull MultiblockWithDisplayBase mte) {
        this.mte = mte;
        configureErrorText(builder -> {
            if (mte.hasMufflerMechanics())
                builder.addMufflerObstructedLine(!mte.isMufflerFaceFree());
        });
        configureWarningText(builder -> {
            if (mte.hasMaintenanceMechanics())
                builder.addMaintenanceProblemLines(mte.getMaintenanceProblems());
        });
        configureDisplayText(builder -> builder.title(mte.getMetaFullName()).structureFormed(mte.isStructureFormed()));
    }

    private static @NotNull <T> Consumer<T> addAction(@Nullable Consumer<T> first, @NotNull Consumer<T> andThen) {
        return first == null ? andThen : first.andThen(andThen);
    }

    /**
     * Constructs the multiblock ui panel<br />
     * <i>It is not recommended to override this method</i>
     */
    public @NotNull ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        // this.valueSyncer.accept(panelSyncManager);
        var panel = createRootPanel();

        panel.child(createScreen(panelSyncManager));

        // TODO createExtras() hook for overrides?
        if (mte instanceof ProgressBarMultiblock progressBarMultiblock &&
                progressBarMultiblock.getProgressBarCount() > 0) {
            panel.height(height + (Bars.HEIGHT * 2) - 2);
            panel.child(createBars(progressBarMultiblock, panelSyncManager));
        }

        return panel.child(Flow.row()
                .bottom(7)
                .height(77)
                .margin(4, 0)
                .child(SlotGroupWidget.playerInventory(0)
                        .alignX(0f))
                .child(createButtons(panel, panelSyncManager, guiData)));
    }

    private Widget<?> createIndicator(PanelSyncManager syncManager) {
        MultiblockUIBuilder error = builder();
        error.sync("error", syncManager);
        error.setAction(this.errorText);
        error.onRebuild(() -> error.updateFormed(mte.isStructureFormed()));

        MultiblockUIBuilder warning = builder();
        warning.sync("warning", syncManager);
        warning.setAction(this.warningText);
        warning.onRebuild(() -> warning.updateFormed(mte.isStructureFormed()));

        IDrawable indicator = new DynamicDrawable(() -> {
            if (!error.isEmpty()) {
                return GTGuiTextures.GREGTECH_LOGO_BLINKING_RED;
            } else if (!warning.isEmpty()) {
                return GTGuiTextures.GREGTECH_LOGO_BLINKING_YELLOW;
            } else {
                // todo getLogo()?
                return GTGuiTextures.GREGTECH_LOGO;
            }
        });

        return new Widget<>()
                .size(18)
                .pos(174 - 5, screenHeight - 18 - 3)
                .overlay(indicator)
                .tooltipAutoUpdate(true)
                .tooltipBuilder(t -> {
                    if (!error.isEmpty()) {
                        error.build(t);
                    } else if (!warning.isEmpty()) {
                        warning.build(t);
                    }
                });
    }

    /**
     * Returns a list of text indicating any current warnings in this Multiblock. <br />
     * Recommended to only display warnings if the structure is already formed. <br />
     * This is called every tick on the client-side
     */
    public MultiblockUIFactory configureWarningText(boolean merge, Consumer<MultiblockUIBuilder> warningText) {
        this.warningText = merge ? addAction(this.warningText, warningText) : warningText;
        return this;
    }

    /**
     * Returns a list of text indicating any current warnings in this Multiblock. <br />
     * Recommended to only display warnings if the structure is already formed. <br />
     * This is called every tick on the client-side
     */
    public MultiblockUIFactory configureWarningText(Consumer<MultiblockUIBuilder> warningText) {
        return configureWarningText(true, warningText);
    }

    /**
     * Returns a list of translation keys indicating any current errors in this Multiblock. <br />
     * Prioritized over any warnings provided by {@link #configureWarningText(Consumer)}.<br />
     * This is called every tick on the client-side
     */
    public MultiblockUIFactory configureErrorText(boolean merge, Consumer<MultiblockUIBuilder> errorText) {
        this.errorText = merge ? addAction(this.errorText, errorText) : errorText;
        return this;
    }

    /**
     * Returns a list of translation keys indicating any current errors in this Multiblock. <br />
     * Prioritized over any warnings provided by {@link #configureWarningText(Consumer)}.<br />
     * This is called every tick on the client-side
     */
    public MultiblockUIFactory configureErrorText(Consumer<MultiblockUIBuilder> errorText) {
        return configureErrorText(true, errorText);
    }

    /**
     * Called per tick on client side <br />
     * Each element of list is displayed on new line <br />
     * To use translation, use {@link KeyUtil#lang(TextFormatting, String, Object...)}
     * or {@link KeyUtil#lang(String, Object...)}
     */
    public MultiblockUIFactory configureDisplayText(boolean merge, Consumer<MultiblockUIBuilder> displayText) {
        this.displayText = merge ? addAction(this.displayText, displayText) : displayText;
        return this;
    }

    /**
     * Called per tick on client side <br />
     * Each element of list is displayed on new line <br />
     * To use translation, use {@link KeyUtil#lang(TextFormatting, String, Object...)}
     * or {@link KeyUtil#lang(String, Object...)}
     */
    public MultiblockUIFactory configureDisplayText(Consumer<MultiblockUIBuilder> displayText) {
        return configureDisplayText(true, displayText);
    }

    /**
     * Add a custom third button to the Multiblock UI. By default, this is a placeholder stating that there is no
     * additional functionality for this Multiblock.
     * <br>
     * Size will be 18x18.
     */
    public MultiblockUIFactory createFlexButton(
                                                BiFunction<PosGuiData, PanelSyncManager, IWidget> flexButton) {
        this.flexButton = flexButton;
        return this;
    }

    public MultiblockUIFactory setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public MultiblockUIFactory setScreenHeight(int height) {
        this.screenHeight = height;
        return this;
    }

    protected @NotNull ModularPanel createRootPanel() {
        return GTGuis.createPanel(mte, width, height);
    }

    /**
     * @param progressMulti    the multiblock with progress bars
     * @param panelSyncManager the sync manager for synchronizing widgets
     */
    @Nullable
    protected Flow createBars(@NotNull ProgressBarMultiblock progressMulti,
                              @NotNull PanelSyncManager panelSyncManager) {
        final int rows = progressMulti.getProgressBarRows();
        final int cols = progressMulti.getProgressBarCols();

        Flow column = Flow.column()
                .margin(4, 0)
                .top(114)
                .widthRel(1f)
                .height(Bars.HEIGHT * 2);

        for (int r = 0; r < rows; r++) {

            Flow row = Flow.row()
                    .widthRel(1f)
                    .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                    .height(Bars.HEIGHT);

            // the numbers for the given row of bars
            int from = r * cols;
            int to = Math.min(from + cols, cols);

            // calculate bar width
            int barCount = Math.max(1, to - from);
            int barWidth = (Bars.FULL_WIDTH / barCount) - (barCount - 1);

            for (int i = from; i < to; i++) {
                row.child(progressMulti.createProgressBar(panelSyncManager, i)
                        .height(Bars.HEIGHT)
                        .width(barWidth)
                        .direction(ProgressWidget.Direction.RIGHT));
            }

            column.child(row);
        }
        return column;
    }

    public MultiblockUIFactory addScreenChildren(Consumer<List<IWidget>> consumer) {
        this.childrenConsumer = consumer;
        return this;
    }

    protected Widget<?> createScreen(PanelSyncManager syncManager) {
        MultiblockUIBuilder display = builder();
        display.setAction(this.displayText);
        display.sync("display", syncManager);

        // todo scrolling doesn't work for rich text widget
        var scrollWidget = new ScrollWidget<>(new VerticalScrollData())
                .sizeRel(1f)
                .child(new RichTextWidget()
                        .sizeRel(1f)
                        .alignment(Alignment.TopLeft)
                        .margin(4, 4)
                        .autoUpdate(true)
                        .textBuilder(display::build));

        if (this.childrenConsumer != null) {
            List<IWidget> extra = new ArrayList<>();
            this.childrenConsumer.accept(extra);
            extra.forEach(scrollWidget::child);
        }

        return new ParentWidget<>()
                .child(scrollWidget)
                .child(createIndicator(syncManager))
                .background(GTGuiTextures.DISPLAY)
                .size(190, screenHeight)
                .pos(4, 4);
    }

    @NotNull
    protected Flow createButtons(@NotNull ModularPanel mainPanel, @NotNull PanelSyncManager panelSyncManager,
                                 PosGuiData guiData) {
        IWidget flexButton = this.flexButton.apply(guiData, panelSyncManager);
        if (flexButton == null) {
            flexButton = GTGuiTextures.BUTTON_NO_FLEX.asWidget()
                    .size(18)
                    .addTooltipLine(IKey.lang("gregtech.multiblock.universal.no_flex_button"));
        }
        var powerButton = createPowerButton(mainPanel, panelSyncManager);

        return Flow.column()
                .alignX(1f)
                .right(4)
                .size(18, 77)
                .child(createDistinctButton(mainPanel, panelSyncManager))
                .child(createVoidingButton(mainPanel, panelSyncManager))
                .child(flexButton)
                .childIf(powerButton != null, powerButton);
    }

    protected IWidget createDistinctButton(@NotNull ModularPanel mainPanel,
                                           @NotNull PanelSyncManager panelSyncManager) {
        if (!(mte instanceof IDistinctBusController distinct) || !distinct.canBeDistinct()) {
            return GTGuiTextures.BUTTON_NO_DISTINCT_BUSES.asWidget()
                    .size(18, 18)
                    .addTooltipLine(IKey.lang("gregtech.multiblock.universal.distinct_not_supported"));
        }

        BooleanSyncValue distinctValue = new BooleanSyncValue(distinct::isDistinct, distinct::setDistinct);

        return new CycleButtonWidget()
                .size(18, 18)
                .value(distinctValue)
                .stateBackground(true, GTGuiTextures.BUTTON_DISTINCT_BUSES[1])
                .stateBackground(false, GTGuiTextures.BUTTON_DISTINCT_BUSES[0])
                .background(GTGuiTextures.BUTTON)
                .tooltipAutoUpdate(true)
                .tooltipBuilder(t -> t.addLine(distinctValue.getBoolValue() ?
                        IKey.lang("gregtech.multiblock.universal.distinct_enabled") :
                        IKey.lang("gregtech.multiblock.universal.distinct_disabled")));
    }

    protected IWidget createVoidingButton(@NotNull ModularPanel mainPanel, @NotNull PanelSyncManager panelSyncManager) {
        if (!mte.shouldShowVoidingModeButton()) {
            return GTGuiTextures.BUTTON_VOID_NONE.asWidget()
                    .size(18, 18)
                    .addTooltipLine(IKey.lang("gregtech.gui.multiblock_voiding_not_supported"));
        }

        IntSyncValue voidingValue = new IntSyncValue(mte::getVoidingMode, mte::setVoidingMode);

        return new CycleButtonWidget()
                .size(18, 18)
                .stateOverlay(0, GTGuiTextures.MULTIBLOCK_VOID[0])
                .stateOverlay(1, GTGuiTextures.MULTIBLOCK_VOID[1])
                .stateOverlay(2, GTGuiTextures.MULTIBLOCK_VOID[2])
                .stateOverlay(3, GTGuiTextures.MULTIBLOCK_VOID[3])
                .background(GTGuiTextures.BUTTON)
                .value(voidingValue)
                .length(4)
                .tooltipAutoUpdate(true)
                .tooltipBuilder(t -> t.addLine(IKey.lang(mte.getVoidingModeTooltip(voidingValue.getIntValue()))));
    }

    @Nullable
    protected Widget<?> createPowerButton(@NotNull ModularPanel mainPanel, @NotNull PanelSyncManager panelSyncManager) {
        IControllable controllable;
        if (!(mte instanceof IControllable)) {
            // is this actually relevant?
            // todo in the future, refactor so that this multis are instanceof IControllable.
            controllable = mte.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
            if (controllable == null) return null;
            GTLog.logger.warn("MTE [{}] does not extend IControllable when it should!", mte.getClass().getSimpleName());
        } else {
            controllable = (IControllable) mte;
        }

        return new CycleButtonWidget()
                .size(18)
                .stateOverlay(true, GTGuiTextures.BUTTON_POWER[1])
                .stateOverlay(false, GTGuiTextures.BUTTON_POWER[0])
                .disableHoverBackground()
                .background(GTGuiTextures.BUTTON_POWER_DETAIL.asIcon().size(18, 6).marginTop(24), GTGuiTextures.BUTTON)
                .value(new BooleanSyncValue(controllable::isWorkingEnabled, controllable::setWorkingEnabled))
                .marginTop(5);
    }

    public static final class Screen {

        public static int WIDTH = 190;

        private Screen() {}
    }

    public static final class Bars {

        public static int FULL_WIDTH = Screen.WIDTH;
        public static int HALF_WIDTH = 94;
        public static int THIRD_WIDTH = 62;
        public static int HEIGHT = 7;

        private Bars() {}
    }

    public static MultiblockUIBuilder builder() {
        return new MultiblockUIBuilder();
    }
}
