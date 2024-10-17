package gregtech.common.terminal2;

import gregtech.api.terminal2.ITerminalApp;
import gregtech.api.terminal2.Terminal2;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;

public class HomeApp implements ITerminalApp {

    private final PagedWidget.Controller pageController = new PagedWidget.Controller();

    public void setPageWidget(PagedWidget<?> widget) {
        widget.controller(pageController);
    }

    @Override
    public IWidget buildWidgets(HandGuiData guiData, PanelSyncManager guiSyncManager, ModularPanel panel) {
        Grid appGrid = new Grid()
                .size(340, 240)
                .nextRow();

        for (ResourceLocation appID : Terminal2.idList) {
            if (appID == Terminal2.HOME_ID) continue;

            appGrid.child(new ButtonWidget<>()
                    .overlay(Terminal2.appMap.get(appID).getIcon())
                    .addTooltipLine(IKey.lang("terminal.app." + appID.getNamespace() + "." + appID.getPath() + ".name"))
                    .onMousePressed(i -> {
                        pageController.setPage(Terminal2.idList.indexOf(appID));
                        return true;
                    }));
        }

        return appGrid;
    }
}
