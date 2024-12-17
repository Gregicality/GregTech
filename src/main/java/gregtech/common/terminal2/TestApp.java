package gregtech.common.terminal2;

import gregtech.api.terminal2.ITerminalApp;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

public class TestApp implements ITerminalApp {

    private final int n;

    public TestApp(int n) {
        this.n = n;
    }

    @Override
    public IWidget buildWidgets(HandGuiData guiData, PanelSyncManager guiSyncManager, ModularPanel panel) {
        return IKey.str("test app " + n).color(0xFFFFFF).shadow(true).asWidget();
    }
}
