package gregtech.api.terminal2;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

public interface ITerminalApp {

    IWidget buildWidgets(HandGuiData guiData, PanelSyncManager guiSyncManager, ModularPanel panel);

    default IDrawable getIcon() {
        return GuiTextures.IMAGE;
    }

    default void dispose() {}
}
