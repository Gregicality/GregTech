package gregtech.common.items.behaviors;

import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.factory.MetaItemGuiFactory;
import gregtech.api.terminal2.Terminal2;
import gregtech.api.terminal2.Terminal2Theme;
import gregtech.common.mui.widget.IDPagedWidget;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;

public class Terminal2Behavior implements IItemBehaviour, ItemUIFactory {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!world.isRemote) {
            MetaItemGuiFactory.open(player, hand);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
    }

    @Override
    public GTGuiTheme getUITheme() {
        return GTGuiTheme.TERMINAL;
    }

    @Override
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager) {
        ModularPanel panel = GTGuis.createPanel(guiData.getUsedItemStack(), 364, 248);
        IDPagedWidget<?> appPages = new IDPagedWidget<>();
        for (var app : Terminal2.appMap.entrySet()) {
            appPages.addPage(app.getKey(), app.getValue().buildWidgets(guiData, guiSyncManager, panel));
        }
        appPages.size(340, 240).pos(4, 4);

        Grid appGrid = new Grid()
                .pos(44, 22)
                .size(340 - 44 * 2, 240 - 22 * 2)
                .margin(6)
                .nextRow();

        for (ResourceLocation appID : Terminal2.appMap.keySet()) {
            if (appID == Terminal2.HOME_ID) continue;

            appGrid.child(new ButtonWidget<>()
                    .overlay(Terminal2.appMap.get(appID).getIcon())
                    .background(Terminal2Theme.COLOR_BACKGROUND_2.getCircle())
                    .hoverBackground(Terminal2Theme.COLOR_BACKGROUND_2.getCircle(),
                            Terminal2Theme.COLOR_BRIGHT_2.getRing())
                    .size(24, 24)
                    .addTooltipLine(IKey.lang("terminal.app." + appID.getNamespace() + "." + appID.getPath() + ".name"))
                    .onMousePressed(i -> {
                        appPages.setPage(appID);
                        return true;
                    }));

            if (appGrid.getChildren().size() % 7 == 0) {
                appGrid.nextRow();
            }
        }
        appPages.addPage(Terminal2.HOME_ID, appGrid).setDefaultPage(Terminal2.HOME_ID);

        return panel.background(GTGuiTextures.TERMINAL_FRAME)
                .child(new DynamicDrawable(Terminal2Theme::getBackgroundDrawable).asWidget()
                        .size(340, 240)
                        .pos(4, 4))
                .child(appPages)
                .child(new ButtonWidget<>()
                        .overlay(GTGuiTextures.HOME_BUTTON)
                        .hoverOverlay(GTGuiTextures.HOME_BUTTON_HOVER)
                        .background(IDrawable.NONE)
                        .disableHoverBackground()
                        .addTooltipLine(IKey.lang("terminal.app.gregtech.home.name"))
                        .size(16, 16)
                        .left(346)
                        .topRelAnchor(0.5F, 0.5F)
                        .onMousePressed(i -> {
                            appPages.setPage(Terminal2.HOME_ID);
                            return true;
                        }));
    }
}
