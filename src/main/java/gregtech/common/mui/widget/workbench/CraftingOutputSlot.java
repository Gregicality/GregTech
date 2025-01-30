package gregtech.common.mui.widget.workbench;

import gregtech.common.metatileentities.storage.CraftingRecipeLogic;
import gregtech.common.metatileentities.storage.CraftingRecipeMemory;
import gregtech.common.metatileentities.storage.MetaTileEntityWorkbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CraftingOutputSlot extends Widget<CraftingOutputSlot> implements Interactable, JeiIngredientProvider {

    private static final int MOUSE_CLICK = 2;
    private static final int SYNC_STACK = 5;
    private final CraftingSlotSH syncHandler;

    public CraftingOutputSlot(IntSyncValue syncValue, MetaTileEntityWorkbench workbench) {
        this.syncHandler = new CraftingSlotSH(
                new CraftingOutputMS(
                        workbench.getCraftingRecipeLogic().getCraftingResultInventory(),
                        syncValue, workbench));
        setSyncHandler(this.syncHandler);
        tooltipAutoUpdate(true);
        tooltipBuilder(tooltip -> {
            if (!isSynced()) return;
            ItemStack stack = this.syncHandler.getOutputStack();
            if (stack.isEmpty()) return;
            tooltip.addFromItem(stack);
        });
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof CraftingSlotSH;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        MouseData mouseData = MouseData.create(mouseButton);
        this.syncHandler.syncToServer(MOUSE_CLICK, mouseData::writeToPacket);
        return Result.SUCCESS;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        ItemStack itemstack = this.syncHandler.getOutputStack();
        if (itemstack.isEmpty()) return;

        GuiDraw.drawItem(itemstack, 1, 1, 16, 16);
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        RichTooltip tooltip = getTooltip();
        if (tooltip != null && isHoveringFor(tooltip.getShowUpTimer())) {
            tooltip.draw(getContext(), this.syncHandler.getOutputStack());
        }
    }

    @Override
    public @Nullable ItemStack getIngredient() {
        return this.syncHandler.getOutputStack();
    }

    protected static class CraftingSlotSH extends SyncHandler {

        private final CraftingRecipeLogic recipeLogic;
        private final CraftingOutputMS slot;

        private final List<ModularSlot> shiftClickSlots = new ArrayList<>();

        public CraftingSlotSH(CraftingOutputMS slot) {
            this.slot = slot;
            this.recipeLogic = slot.recipeLogic;
        }

        @Override
        public void init(String key, PanelSyncManager syncManager) {
            super.init(key, syncManager);
            getSyncManager().getSlotGroups().stream()
                    .filter(SlotGroup::allowShiftTransfer)
                    .sorted(Comparator.comparingInt(SlotGroup::getShiftClickPriority))
                    .collect(Collectors.toList())
                    .forEach(slotGroup -> {
                        for (Slot slot : slotGroup.getSlots()) {
                            if (slot instanceof ModularSlot modularSlot) {
                                this.shiftClickSlots.add(modularSlot);
                            }
                        }
                    });
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) {
            if (id == MOUSE_CLICK) {
                var data = MouseData.readPacket(buf);

                if (recipeLogic.isRecipeValid() && this.slot.canTakeStack(getSyncManager().getPlayer())) {
                    if (recipeLogic.performRecipe()) {
                        ItemStack craftedStack = getOutputStack();
                        handleItemCraft(craftedStack, getSyncManager().getPlayer());

                        if (data.shift) {
                            ItemStack finalStack = craftedStack.copy();
                            while (finalStack.getCount() < craftedStack.getMaxStackSize()) {
                                if (!recipeLogic.performRecipe()) break;
                                finalStack.setCount(finalStack.getCount() + craftedStack.getCount());
                                handleItemCraft(craftedStack, getSyncManager().getPlayer());
                            }
                            quickTransfer(finalStack);
                        } else {
                            syncToClient(SYNC_STACK, this::syncCraftedStack);
                        }
                    }
                }
            }
        }

        public void quickTransfer(ItemStack fromStack) {
            List<ModularSlot> emptySlots = new ArrayList<>();
            for (ModularSlot toSlot : this.shiftClickSlots) {
                if (toSlot.isEnabled() && toSlot.isItemValid(fromStack)) {
                    ItemStack toStack = toSlot.getStack().copy();
                    if (toStack.isEmpty()) {
                        emptySlots.add(toSlot);
                        continue;
                    }

                    if (ItemHandlerHelper.canItemStacksStack(fromStack, toStack)) {
                        int j = toStack.getCount() + fromStack.getCount();
                        int maxSize = Math.min(toSlot.getSlotStackLimit(), fromStack.getMaxStackSize());

                        if (j <= maxSize) {
                            fromStack.setCount(0);
                            toStack.setCount(j);
                            toSlot.putStack(toStack);
                        } else if (toStack.getCount() < maxSize) {
                            fromStack.shrink(maxSize - toStack.getCount());
                            toStack.setCount(maxSize);
                            toSlot.putStack(toStack);
                        }

                        if (fromStack.isEmpty()) {
                            return;
                        }
                    }
                }
            }
            for (ModularSlot emptySlot : emptySlots) {
                ItemStack itemstack = emptySlot.getStack();
                if (emptySlot.isEnabled() && itemstack.isEmpty() && emptySlot.isItemValid(fromStack)) {
                    if (fromStack.getCount() > emptySlot.getSlotStackLimit()) {
                        emptySlot.putStack(fromStack.splitStack(emptySlot.getSlotStackLimit()));
                    } else {
                        emptySlot.putStack(fromStack.splitStack(fromStack.getCount()));
                    }
                    if (fromStack.isEmpty()) {
                        return;
                    }
                }
            }
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) {
            if (id == SYNC_STACK) {
                getSyncManager().setCursorItem(NetworkUtils.readItemStack(buf));
            }
        }

        private void syncCraftedStack(PacketBuffer buf) {
            ItemStack curStack = getSyncManager().getCursorItem();
            ItemStack outStack = this.slot.getStack();
            ItemStack toSync = outStack.copy();
            if (curStack.getItem() == outStack.getItem() &&
                    curStack.getMetadata() == outStack.getMetadata() &&
                    ItemStack.areItemStackTagsEqual(curStack, outStack)) {

                int combined = curStack.getCount() + outStack.getCount();
                if (combined <= outStack.getMaxStackSize()) {
                    toSync.setCount(curStack.getCount() + outStack.getCount());
                } else {
                    toSync.setCount(outStack.getMaxStackSize());
                }
            } else if (!curStack.isEmpty()) {
                toSync = curStack;
            }
            NetworkUtils.writeItemStack(buf, toSync);
        }

        public ItemStack getOutputStack() {
            return slot.getStack();
        }

        public void handleItemCraft(ItemStack craftedStack, EntityPlayer player) {
            craftedStack.onCrafting(player.world, player, 1);

            var inventoryCrafting = recipeLogic.getCraftingMatrix();

            // if we're not simulated, fire the event, unlock recipe and add crafted items, and play sounds
            FMLCommonHandler.instance().firePlayerCraftingEvent(player, craftedStack, inventoryCrafting);

            var cachedRecipe = recipeLogic.getCachedRecipe();
            if (cachedRecipe != null && !cachedRecipe.isDynamic()) {
                player.unlockRecipes(Lists.newArrayList(cachedRecipe));
            }
            if (cachedRecipe != null) {
                ItemStack resultStack = cachedRecipe.getCraftingResult(inventoryCrafting);
                this.slot.notifyRecipePerformed(resultStack);
            }
        }
    }

    protected static class CraftingOutputMS extends ModularSlot {

        private final IntSyncValue syncValue;
        private final CraftingRecipeLogic recipeLogic;
        private final CraftingRecipeMemory recipeMemory;
        private final IItemHandler craftingGrid;

        public CraftingOutputMS(IInventory craftingInventory, IntSyncValue syncValue,
                                MetaTileEntityWorkbench workbench) {
            super(new InventoryWrapper(craftingInventory, workbench.getCraftingRecipeLogic()), 0, true);
            this.syncValue = syncValue;
            this.recipeLogic = workbench.getCraftingRecipeLogic();
            this.recipeMemory = workbench.getRecipeMemory();
            this.craftingGrid = workbench.getCraftingGrid();
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            ItemStack curStack = playerIn.inventory.getItemStack();
            if (curStack.isEmpty()) return true;

            ItemStack outStack = recipeLogic.getCachedRecipe().getRecipeOutput();
            if (curStack.getItem() == outStack.getItem() &&
                    curStack.getMetadata() == outStack.getMetadata() &&
                    ItemStack.areItemStackTagsEqual(curStack, outStack)) {

                int combined = curStack.getCount() + outStack.getCount();
                return combined <= outStack.getMaxStackSize();
            } else {
                return false;
            }
        }

        public void notifyRecipePerformed(ItemStack stack) {
            this.syncValue.setValue(this.syncValue.getValue() + stack.getCount(), true, true);
            this.recipeMemory.notifyRecipePerformed(this.craftingGrid, stack);
        }

        @Override
        public void putStack(@NotNull ItemStack stack) {
            super.putStack(getStack());
        }

        @Override
        public @NotNull ItemStack decrStackSize(int amount) {
            return getStack();
        }
    }

    private static class InventoryWrapper implements IItemHandlerModifiable {

        private final IInventory inventory;
        private final CraftingRecipeLogic recipeLogic;

        private InventoryWrapper(IInventory inventory, CraftingRecipeLogic recipeLogic) {
            this.inventory = inventory;
            this.recipeLogic = recipeLogic;
        }

        @Override
        public int getSlots() {
            return inventory.getSizeInventory();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot).copy();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getInventoryStackLimit();
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (!recipeLogic.isRecipeValid()) {
                inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
            }

            if (!stack.isEmpty())
                inventory.setInventorySlotContents(slot, stack);
        }
    }
}
