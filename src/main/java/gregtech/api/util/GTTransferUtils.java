package gregtech.api.util;

import gregtech.api.capability.MultipleTankHandler;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class GTTransferUtils {

    public static int transferFluids(@NotNull IFluidHandler sourceHandler, @NotNull IFluidHandler destHandler) {
        return transferFluids(sourceHandler, destHandler, Integer.MAX_VALUE, fluidStack -> true);
    }

    public static int transferFluids(@NotNull IFluidHandler sourceHandler, @NotNull IFluidHandler destHandler,
                                     int transferLimit) {
        return transferFluids(sourceHandler, destHandler, transferLimit, fluidStack -> true);
    }

    public static int transferFluids(@NotNull IFluidHandler sourceHandler, @NotNull IFluidHandler destHandler,
                                     int transferLimit, @NotNull Predicate<FluidStack> fluidFilter) {
        int fluidLeftToTransfer = transferLimit;

        for (IFluidTankProperties tankProperties : sourceHandler.getTankProperties()) {
            FluidStack currentFluid = tankProperties.getContents();
            if (currentFluid == null || currentFluid.amount == 0 || !fluidFilter.test(currentFluid)) {
                continue;
            }

            currentFluid.amount = fluidLeftToTransfer;
            FluidStack fluidStack = sourceHandler.drain(currentFluid, false);
            if (fluidStack == null || fluidStack.amount == 0) {
                continue;
            }

            int canInsertAmount = destHandler.fill(fluidStack, false);
            if (canInsertAmount > 0) {
                fluidStack.amount = canInsertAmount;
                fluidStack = sourceHandler.drain(fluidStack, true);
                if (fluidStack != null && fluidStack.amount > 0) {
                    destHandler.fill(fluidStack, true);

                    fluidLeftToTransfer -= fluidStack.amount;
                    if (fluidLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
        return transferLimit - fluidLeftToTransfer;
    }

    public static boolean transferExactFluidStack(@NotNull IFluidHandler sourceHandler,
                                                  @NotNull IFluidHandler destHandler, FluidStack fluidStack) {
        int amount = fluidStack.amount;
        FluidStack sourceFluid = sourceHandler.drain(fluidStack, false);
        if (sourceFluid == null || sourceFluid.amount != amount) {
            return false;
        }
        int canInsertAmount = destHandler.fill(sourceFluid, false);
        if (canInsertAmount == amount) {
            sourceFluid = sourceHandler.drain(sourceFluid, true);
            if (sourceFluid != null && sourceFluid.amount > 0) {
                destHandler.fill(sourceFluid, true);
                return true;
            }
        }
        return false;
    }

    public static void moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory) {
        for (int srcIndex = 0; srcIndex < sourceInventory.getSlots(); srcIndex++) {
            ItemStack sourceStack = sourceInventory.extractItem(srcIndex, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty()) {
                continue;
            }
            ItemStack remainder = insertItem(targetInventory, sourceStack, true);
            int amountToInsert = sourceStack.getCount() - remainder.getCount();
            if (amountToInsert > 0) {
                sourceStack = sourceInventory.extractItem(srcIndex, amountToInsert, false);
                insertItem(targetInventory, sourceStack, false);
            }
        }
    }

    /**
     * Simulates the insertion of items into a target inventory, then optionally performs the insertion.
     * <br />
     * <br />
     * Simulating will not modify any of the input parameters. Insertion will either succeed completely, or fail
     * without modifying anything.
     * This method should be called with {@code simulate} {@code true} first, then {@code simulate} {@code false},
     * only if it returned {@code true}.
     *
     * @param handler  the target inventory
     * @param simulate whether to simulate ({@code true}) or actually perform the insertion ({@code false})
     * @param items    the items to insert into {@code handler}.
     * @return {@code true} if the insertion succeeded, {@code false} otherwise.
     */
    public static boolean addItemsToItemHandler(final IItemHandler handler,
                                                final boolean simulate,
                                                final List<ItemStack> items) {
        // determine if there is sufficient room to insert all items into the target inventory
        if (simulate) {
            OverlayedItemHandler overlayedItemHandler = new OverlayedItemHandler(handler);
            Object2IntMap<ItemStack> stackKeyMap = GTHashMaps.fromItemStackCollection(items);

            for (Object2IntMap.Entry<ItemStack> entry : stackKeyMap.object2IntEntrySet()) {
                int amountToInsert = entry.getIntValue();
                int amount = overlayedItemHandler.insertStackedItemStack(entry.getKey(), amountToInsert);
                if (amount > 0) {
                    return false;
                }
            }
            return true;
        }

        // perform the merge.
        items.forEach(stack -> insertItem(handler, stack, false));
        return true;
    }

    /**
     * Simulates the insertion of fluid into a target fluid handler, then optionally performs the insertion. <br />
     * <br
     * />
     * Simulating will not modify any of the input parameters. Insertion will either succeed completely, or fail
     * without modifying anything. This method should be called with {@code simulate} {@code true} first, then
     * {@code simulate} {@code false}, only if it returned {@code true}.
     *
     * @param fluidStacks  the items to insert into {@code fluidHandler}.
     * @param fluidHandler the target inventory
     * @param simulate     whether to simulate ({@code true}) or actually perform the insertion ({@code false})
     * @return {@code true} if the insertion succeeded, {@code false} otherwise.
     */
    public static boolean addFluidsToFluidHandler(List<FluidStack> fluidStacks,
                                                  MultipleTankHandler fluidHandler,
                                                  boolean simulate) {
        if (simulate) {
            for (FluidStack fluidStack : fluidStacks) {
                int inserted = fluidHandler.fill(fluidStack, false);
                if (inserted != fluidStack.amount) {
                    return false;
                }
            }
            return true;
        }

        for (FluidStack fluidStack : fluidStacks) {
            fluidHandler.fill(fluidStack, true);
        }
        return true;
    }

    /**
     * Inserts items by trying to fill slots with the same item first, and then fill empty slots.
     */
    public static ItemStack insertItem(IItemHandler handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) {
            return stack;
        }

        IntList emptySlots = new IntArrayList();
        int slots = handler.getSlots();

        for (int i = 0; i < slots; i++) {
            ItemStack slotStack = handler.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                emptySlots.add(i);
            } else if (ItemHandlerHelper.canItemStacksStack(stack, slotStack)) {
                stack = handler.insertItem(i, stack, simulate);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        for (int slot : emptySlots) {
            stack = handler.insertItem(slot, stack, simulate);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    /**
     * Only inerts to empty slots. Perfect for not stackable items
     */
    public static ItemStack insertToEmpty(IItemHandler handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        int slots = handler.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack slotStack = handler.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                stack = handler.insertItem(i, stack, simulate);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }

    // TODO try to remove this one day
    public static void fillInternalTankFromFluidContainer(IFluidHandler fluidHandler,
                                                          IItemHandlerModifiable itemHandler, int inputSlot,
                                                          int outputSlot) {
        ItemStack inputContainerStack = itemHandler.extractItem(inputSlot, 1, true);
        FluidActionResult result = FluidUtil.tryEmptyContainer(inputContainerStack, fluidHandler, Integer.MAX_VALUE,
                null, false);
        if (result.isSuccess()) {
            ItemStack remainingItem = result.getResult();
            if (ItemStack.areItemStacksEqual(inputContainerStack, remainingItem))
                return; // do not fill if item stacks match
            if (!remainingItem.isEmpty() && !itemHandler.insertItem(outputSlot, remainingItem, true).isEmpty())
                return; // do not fill if can't put remaining item
            FluidUtil.tryEmptyContainer(inputContainerStack, fluidHandler, Integer.MAX_VALUE, null, true);
            itemHandler.extractItem(inputSlot, 1, false);
            itemHandler.insertItem(outputSlot, remainingItem, false);
        }
    }
}
