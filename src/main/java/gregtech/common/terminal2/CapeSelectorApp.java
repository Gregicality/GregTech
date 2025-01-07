package gregtech.common.terminal2;

import gregtech.api.terminal2.ITerminalApp;
import gregtech.api.terminal2.Terminal2Theme;
import gregtech.api.util.CapesRegistry;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.mui.widget.ColorableVScrollData;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// todo: this needs testing on dedicated server once that no longer crashes (mui rc3)
public class CapeSelectorApp implements ITerminalApp {

    private static final int SYNC_UNLOCKED_CAPES = 1;
    private static final int SELECT_CAPE = 2;

    @Override
    public IWidget buildWidgets(HandGuiData guiData, PanelSyncManager guiSyncManager, ModularPanel panel) {
        UUID uuid = guiData.getPlayer().getPersistentID();
        var syncHandler = new CapeSelectorSyncHandler(uuid);
        guiSyncManager.syncValue("cape_handler", syncHandler);

        var capeGrid = new Grid()
                .sizeRel(1f)
                .posRel(0.5f, 0.5f)
                .margin(21)
                .scrollable(new ColorableVScrollData())
                .nextRow();
        Terminal2Theme.COLOR_FOREGROUND_BRIGHT.bindScrollFG(capeGrid);

        for (ResourceLocation cape : CapesRegistry.allCapes()) {
            if (capeGrid.getChildren().size() % 4 == 0 && !capeGrid.getChildren().isEmpty()) {
                capeGrid.nextRow();
            }

            capeGrid.child(new ButtonWidget<>()
                    .size(40, 72)
                    .overlay(new UITexture(cape, 1f / 64, 1f / 32, 11f / 64, 17f / 32, false))
                    .background(Terminal2Theme.COLOR_BACKGROUND_2)
                    .disableHoverOverlay()
                    .hoverBackground(Terminal2Theme.COLOR_DARK_4)
                    .onMousePressed(b -> {
                        GTLog.logger.info("set {}", cape.toString());
                        syncHandler.syncToServer(SELECT_CAPE, buf -> buf.writeString(cape.toString()));
                        return true;
                    }));
        }

        return new ParentWidget<>()
                .sizeRel(0.98f)
                .posRel(0.5f, 0.5f)
                .background(Terminal2Theme.COLOR_BACKGROUND_1)
                .child(capeGrid);
    }

    @Override
    public IDrawable getIcon() {
        return new UITexture(Textures.GREGTECH_CAPE_TEXTURE, 32f / 64, 0, 46f / 64, 22f / 32, false);
    }

    private static final class CapeSelectorSyncHandler extends SyncHandler {

        private final UUID uuid;
        private List<ResourceLocation> unlockedCapes;

        public CapeSelectorSyncHandler(UUID uuid) {
            this.uuid = uuid;
        }

//        @Override
//        public void init(String key, PanelSyncManager syncManager) {
//            super.init(key, syncManager);
//            if (!getSyncManager().isClient()) {
//                unlockedCapes = CapesRegistry.getUnlockedCapes(uuid);
//                syncToClient(SYNC_UNLOCKED_CAPES, buf -> {
//                    buf.writeVarInt(unlockedCapes.size());
//                    for (ResourceLocation cape : unlockedCapes) {
//                        buf.writeString(cape.toString());
//                    }
//                });
//            }
//        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) {
            if (id == SYNC_UNLOCKED_CAPES) {
                int len = buf.readVarInt();
                unlockedCapes = new ArrayList<>(len);
                for (int i = 0; i < len; i++) {
                    unlockedCapes.add(new ResourceLocation(buf.readString(Short.MAX_VALUE)));
                }
            }
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) {
            if (id == SELECT_CAPE) {
                CapesRegistry.giveCape(uuid, new ResourceLocation(buf.readString(Short.MAX_VALUE)));
            }
        }
    }
}
