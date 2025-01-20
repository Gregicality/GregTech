package gregtech.api.cover;

import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.GregTechGuiScreen;
import gregtech.api.mui.factory.CoverGuiFactory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CoverWithUI extends Cover, IUIHolder, IGuiHolder<SidedPosGuiData> {

    @ApiStatus.Experimental
    default boolean usesMui2() {
        return false;
    }

    default void openUI(EntityPlayerMP player) {
        if (usesMui2()) {
            CoverGuiFactory.open(player, this);
        } else {
            CoverUIFactory.INSTANCE.openUI(this, player);
        }
    }

    @Deprecated
    default ModularUI createUI(EntityPlayer player) {
        return null;
    }

    @ApiStatus.NonExtendable
    @SideOnly(Side.CLIENT)
    @Override
    default ModularScreen createScreen(SidedPosGuiData guiData, ModularPanel mainPanel) {
        return new GregTechGuiScreen(mainPanel, getUITheme());
    }

    default GTGuiTheme getUITheme() {
        return GTGuiTheme.COVER;
    }

    @Override
    default ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager) {
        var w = createUI(guiData, guiSyncManager);
        return confgurePanel(GTGuis.defaultPanel(getPickItem()), false)
                .childIf(w != null, w)
                .child(createTitleRow(getPickItem()).pos(5, 5))
                .bindPlayerInventory();
    }

    default ModularPanel confgurePanel(ModularPanel panel, boolean isSmallGui) {
        return panel;
    }

    default @NotNull ModularPanel getSmallGUI(@NotNull SidedPosGuiData guiData,
                                              @NotNull PanelSyncManager guiSyncManager) {
        var w = createUI(guiData, guiSyncManager);
        return confgurePanel(GTGuis.defaultPopupPanel(getPickItem().getTranslationKey()), true)
                .childIf(w != null, w)
                .child(createTitleRow(getPickItem()).pos(5, 5))
                .paddingBottom(24)
                .coverChildrenHeight();
    }

    default @Nullable IWidget createUI(SidedPosGuiData data, PanelSyncManager manager) {
        return null;
    }

    default boolean shouldShowSmallUI() {
        // Check buildUI is not null?
        return usesMui2();
    }

    @Override
    default boolean isValid() {
        return getCoverableView().isValid();
    }

    @Override
    default boolean isRemote() {
        return getCoverableView().getWorld().isRemote;
    }

    @Override
    default void markAsDirty() {
        getCoverableView().markDirty();
    }

    /* Helper methods for UI creation with covers that are commonly used */

    /**
     * The color used for Cover UI titles, and used in {@link #createTitleRow}.
     */
    int UI_TITLE_COLOR = 0xFF222222;
    /**
     * The color used for Cover UI text. Available for reference, but is
     * handled automatically by the {@link GTGuiTheme#COVER} theme.
     */
    int UI_TEXT_COLOR = 0xFF555555;

    /**
     * Create the Title bar widget for a Cover.
     */
    static Flow createTitleRow(ItemStack stack) {
        return Flow.row()
                .pos(4, 4)
                .height(16).coverChildrenWidth()
                .child(new ItemDrawable(stack).asWidget().size(16).marginRight(4))
                .child(IKey.str(stack.getDisplayName())
                        .color(UI_TITLE_COLOR)
                        .asWidget().heightRel(1.0f));
    }

    /**
     * Create a new settings row for a Cover setting.
     */
    default ParentWidget<?> createSettingsRow() {
        return new ParentWidget<>().height(16).widthRel(1.0f).marginBottom(2);
    }

    default int getIncrementValue(MouseData data) {
        int adjust = 1;
        if (data.shift) adjust *= 4;
        if (data.ctrl) adjust *= 16;
        if (data.alt) adjust *= 64;
        return adjust;
    }

    default IKey createAdjustOverlay(boolean increment) {
        final StringBuilder builder = new StringBuilder();
        builder.append(increment ? '+' : '-');
        builder.append(getIncrementValue(MouseData.create(-1)));

        float scale = 1f;
        if (builder.length() == 3) {
            scale = 0.8f;
        } else if (builder.length() == 4) {
            scale = 0.6f;
        } else if (builder.length() > 4) {
            scale = 0.5f;
        }
        return IKey.str(builder.toString())
                .color(Color.WHITE.main)
                .scale(scale);
    }

    /**
     * Get a BoolValue for use with toggle buttons which are "linked together,"
     * meaning only one of them can be pressed at a time.
     */
    default <T extends Enum<T>> BoolValue.Dynamic boolValueOf(EnumSyncValue<T> syncValue, T value) {
        return new BoolValue.Dynamic(() -> syncValue.getValue() == value, $ -> syncValue.setValue(value));
    }

    /**
     * Get a BoolValue for use with toggle buttons which are "linked together,"
     * meaning only one of them can be pressed at a time.
     */
    default BoolValue.Dynamic boolValueOf(IntSyncValue syncValue, int value) {
        return new BoolValue.Dynamic(() -> syncValue.getValue() == value, $ -> syncValue.setValue(value));
    }

    class EnumRowBuilder<T extends Enum<T>> {

        private EnumSyncValue<T> syncValue;
        private final Class<T> enumValue;
        private String lang;
        private IDrawable[] background;
        private IDrawable selectedBackground;
        private IDrawable[] overlay;

        public EnumRowBuilder(Class<T> enumValue) {
            this.enumValue = enumValue;
        }

        public EnumRowBuilder<T> value(EnumSyncValue<T> syncValue) {
            this.syncValue = syncValue;
            return this;
        }

        public EnumRowBuilder<T> lang(String lang) {
            this.lang = lang;
            return this;
        }

        public EnumRowBuilder<T> background(IDrawable... background) {
            this.background = background;
            return this;
        }

        public EnumRowBuilder<T> selectedBackground(IDrawable selectedBackground) {
            this.selectedBackground = selectedBackground;
            return this;
        }

        public EnumRowBuilder<T> overlay(IDrawable... overlay) {
            this.overlay = overlay;
            return this;
        }

        public EnumRowBuilder<T> overlay(int size, IDrawable... overlay) {
            this.overlay = new IDrawable[overlay.length];
            for (int i = 0; i < overlay.length; i++) {
                this.overlay[i] = overlay[i].asIcon().size(size);
            }
            return this;
        }

        private BoolValue.Dynamic boolValueOf(EnumSyncValue<T> syncValue, T value) {
            return new BoolValue.Dynamic(() -> syncValue.getValue() == value, $ -> syncValue.setValue(value));
        }

        public Flow build() {
            var row = Flow.row().marginBottom(2).coverChildrenHeight().widthRel(1f);
            if (this.enumValue != null && this.syncValue != null) {
                for (var enumVal : enumValue.getEnumConstants()) {
                    var button = new ToggleButton().size(18).marginRight(2)
                            .value(boolValueOf(this.syncValue, enumVal));

                    if (this.background != null && this.background.length > 0)
                        button.background(this.background);
                    else
                        button.background(GTGuiTextures.MC_BUTTON);

                    if (this.selectedBackground != null)
                        button.selectedBackground(this.selectedBackground);
                    else
                        button.selectedBackground(GTGuiTextures.MC_BUTTON_DISABLED);

                    if (this.overlay != null)
                        button.overlay(this.overlay[enumVal.ordinal()]);

                    if (enumVal instanceof IStringSerializable serializable) {
                        button.addTooltipLine(IKey.lang(serializable.getName()));
                    }
                    row.child(button);
                }
            }

            if (this.lang != null && !this.lang.isEmpty())
                row.child(IKey.lang(this.lang).asWidget().align(Alignment.CenterRight).height(18));

            return row;
        }
    }
}
