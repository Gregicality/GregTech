package gregtech.common.items.behaviors;

import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.factory.MetaItemGuiFactory;
import gregtech.api.terminal2.Terminal2Settings;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager) {
        var backgroundSelectPanel = (PanelSyncHandler) guiSyncManager.panel("terminal_background_select",
                backgroundSelectWidget(), true);
        return GTGuis.createPanel(guiData.getUsedItemStack(), 364, 248)
                .background(GTGuiTextures.TERMINAL_FRAME)
                .child(new DynamicDrawable(Terminal2Settings::getBackgroundDrawable).asWidget().size(340, 240).pos(4,
                        4))
                .child(new ButtonWidget<>()
                        .overlay(GuiTextures.GEAR)
                        // TODO lang
                        .addTooltipLine("Change Background")
                        .posRel(0.1F, 0.1F)
                        .onMousePressed(i -> {
                            if (backgroundSelectPanel.isPanelOpen()) {
                                backgroundSelectPanel.closePanel();
                            } else {
                                backgroundSelectPanel.openPanel();
                            }
                            return true;
                        }));
    }

    private PanelSyncHandler.IPanelBuilder backgroundSelectWidget() {
        return (syncManager, syncHandler) -> {
            String[] files = Terminal2Settings.backgroundsDir.list();
            List<String> options;
            if (files == null) {
                options = new ArrayList<>();
            } else {
                options = new ArrayList<>(Arrays.asList(files));
            }
            options.sort(Comparator.naturalOrder());
            options.add(0, "default");

            var list = ListWidget.builder(options, (st) -> new ButtonWidget<>()
                    .overlay(IKey.str(st))
                    .size(140, 18)
                    .leftRel(0.2F)
                    .onMousePressed(i -> {
                        Terminal2Settings.setBackground(st);
                        return true;
                    }))
                    .size(150, 90).pos(9, 16);

            return GTGuis.createPopupPanel("terminal_background_select", 168, 112)
                    // TODO lang
                    .child(IKey.str("Background Select").asWidget()
                            .leftRel(0.5F)
                            .top(6))
                    .child(list);
        };
    }
}
