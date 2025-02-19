package gregtech.common.pipelike.itempipe.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.impl.ItemHandlerProxy;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverHolder;
import gregtech.api.util.FacingPos;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverItemFilter;
import gregtech.common.covers.CoverRoboticArm;
import gregtech.common.covers.DistributionMode;
import gregtech.common.covers.ItemFilterMode;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;

public class ItemNetHandler implements IItemHandler {

    private ItemPipeNet net;
    private TileEntityItemPipe pipe;
    private final EnumFacing facing;
    private final Object2IntMap<FacingPos> simulatedTransfersGlobalRoundRobin = new Object2IntOpenHashMap<>();
    // when doing a non-global dry run, this keeps track of the capacity of each route on the network
    private final Object2IntMap<FacingPos> simulatedRemainingCapacityRoundRobin = new Object2IntOpenHashMap<>();
    private int simulatedTransfers = 0;
    private final ItemStackHandler testHandler = new ItemStackHandler(1);

    public ItemNetHandler(ItemPipeNet net, TileEntityItemPipe pipe, EnumFacing facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
    }

    public void updateNetwork(ItemPipeNet net) {
        this.net = net;
    }

    public void updatePipe(TileEntityItemPipe pipe) {
        this.pipe = pipe;
    }

    public ItemPipeNet getNet() {
        return net;
    }

    public EnumFacing getFacing() {
        return facing;
    }

    private void copyTransferred() {
        simulatedTransfers = pipe.getTransferredItems();
        simulatedTransfersGlobalRoundRobin.clear();
        simulatedTransfersGlobalRoundRobin.putAll(pipe.getTransferred());
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return stack;

        if (net == null || pipe == null || pipe.isInvalid() || pipe.isFaceBlocked(facing)) {
            return stack;
        }

        copyTransferred();
        Cover pipeCover = this.pipe.getCoverableImplementation().getCoverAtSide(facing);
        Cover tileCover = getCoverOnNeighbour(this.pipe, facing);

        boolean pipeConveyor = pipeCover instanceof CoverConveyor, tileConveyor = tileCover instanceof CoverConveyor;
        // abort if there are two conveyors
        if (pipeConveyor && tileConveyor) return stack;

        if (tileCover != null && !checkImportCover(tileCover, false, stack))
            return stack;

        if (!pipeConveyor && !tileConveyor)
            return insertFirst(stack, simulate);

        CoverConveyor conveyor = (CoverConveyor) (pipeConveyor ? pipeCover : tileCover);
        if (conveyor.getConveyorMode() ==
                (pipeConveyor ? CoverConveyor.ConveyorMode.IMPORT : CoverConveyor.ConveyorMode.EXPORT)) {
            boolean roundRobinGlobal = conveyor.getDistributionMode() == DistributionMode.ROUND_ROBIN_GLOBAL;
            if (roundRobinGlobal || conveyor.getDistributionMode() == DistributionMode.ROUND_ROBIN_PRIO)
                return insertRoundRobin(stack, simulate, roundRobinGlobal);
        }

        return insertFirst(stack, simulate);
    }

    public static boolean checkImportCover(Cover cover, boolean onPipe, ItemStack stack) {
        if (cover == null) return true;
        if (cover instanceof CoverItemFilter filter) {
            return (filter.getFilterMode() != ItemFilterMode.FILTER_BOTH &&
                    (filter.getFilterMode() != ItemFilterMode.FILTER_INSERT || !onPipe) &&
                    (filter.getFilterMode() != ItemFilterMode.FILTER_EXTRACT || onPipe)) || filter.testItemStack(stack);
        }
        return true;
    }

    public ItemStack insertFirst(ItemStack stack, boolean simulate) {
        for (ItemRoutePath inv : net.getNetData(pipe.getPipePos(), facing)) {
            stack = insert(inv, stack, simulate);
            if (stack.isEmpty())
                return ItemStack.EMPTY;
        }
        return stack;
    }

    public ItemStack insertRoundRobin(ItemStack stack, boolean simulate, boolean global) {
        List<ItemRoutePath> routePaths = net.getNetData(pipe.getPipePos(), facing);
        if (routePaths.isEmpty())
            return stack;
        if (routePaths.size() == 1)
            return insert(routePaths.get(0), stack, simulate);
        List<ItemRoutePath> routePathsCopy = new ArrayList<>(routePaths);

        if (global) {
            stack = insertToHandlersEnhanced(routePathsCopy, stack, routePaths.size(), simulate);
        } else {
            stack = insertEquallyToHandlers(routePathsCopy, stack, simulate);
            // check if we have any remaining amount that can be inserted,
            // and that we have routes that have free space available
            if (!stack.isEmpty() && !routePathsCopy.isEmpty()) {
                // try to insert the remaining amount
                stack = insertEquallyToHandlers(routePathsCopy, stack, simulate);
            }
            simulatedRemainingCapacityRoundRobin.clear();
        }

        return stack;
    }

    /**
     * Inserts items equally to all handlers
     * if it couldn't insert all items, the handler will be removed
     *
     * @param copy     where to insert to
     * @param stack    what to insert
     * @param simulate whether to actually insert or not
     * @return remainder
     */
    private ItemStack insertEquallyToHandlers(List<ItemRoutePath> copy, ItemStack stack, boolean simulate) {
        Iterator<ItemRoutePath> routePathIterator = copy.listIterator();
        int inserted = 0;
        int count = stack.getCount();
        int amountToInsertInEach = count / copy.size();
        int m = amountToInsertInEach == 0 ? count % copy.size() : 0;
        while (routePathIterator.hasNext()) {
            ItemRoutePath routePath = routePathIterator.next();
            FacingPos routeFacing = routePath.toFacingPos();

            int remainingAmount = amountToInsertInEach;
            if (m > 0) {
                remainingAmount++;
                m--;
            }
            remainingAmount = Math.min(remainingAmount, stack.getCount() - inserted);

            // if we know the capacity from a previous dry run, subtract that from the amount we can insert in the route
            Integer capacityLeftAfterPreviousDryRun = simulatedRemainingCapacityRoundRobin.getOrDefault(routeFacing, null);
            if (capacityLeftAfterPreviousDryRun != null) {
                remainingAmount = Math.min(remainingAmount, capacityLeftAfterPreviousDryRun);
            }

            if (remainingAmount == 0) break;
            ItemStack toInsert = stack.copy();
            toInsert.setCount(remainingAmount);
            int remainingAfterSubrouteInsert = insert(routePath, toInsert, simulate).getCount();
            int amountInsertedIntoSubroute = remainingAmount - remainingAfterSubrouteInsert;

            // if we inserted anything into the subroute, add it to the inserted amount tally
            if (amountInsertedIntoSubroute != 0) {
                inserted += (remainingAmount - remainingAfterSubrouteInsert);
            }
            if (remainingAfterSubrouteInsert == 1 && amountToInsertInEach == 0 && remainingAmount == 1) {
                m++;
            }

            // if we can't insert anything more into the sub route, remove it from the iterator
            if (remainingAfterSubrouteInsert > 0) {
                routePathIterator.remove();
            } else {
                int freeInventoryInSubRoute = 0;
                int slots = routePath.getHandler().getSlots();

                for (int i = 0; i < slots; i++) {
                    int slotLimit = routePath.getHandler().getSlotLimit(i);
                    ItemStack testStackToInsert = stack.copy();
                    testStackToInsert.setCount(slotLimit);
                    ItemStack insertedTest = routePath.getHandler().insertItem(i, testStackToInsert, true);
                    ItemStack stackInSlot = routePath.getHandler().getStackInSlot(i);
                    freeInventoryInSubRoute += slotLimit - insertedTest.getCount();
                }
                simulatedRemainingCapacityRoundRobin.put(routePath.toFacingPos(), Integer.valueOf(freeInventoryInSubRoute - amountInsertedIntoSubroute));
            }
        }

        ItemStack remainder = stack.copy();
        remainder.setCount(count - inserted);
        return remainder;
    }

    private ItemStack insertToHandlersEnhanced(List<ItemRoutePath> copy, ItemStack stack, int dest, boolean simulate) {
        List<EnhancedRoundRobinData> transferred = new ArrayList<>();
        IntList steps = new IntArrayList();
        int min = Integer.MAX_VALUE;
        ItemStack simStack;

        // find inventories that are not full and get the amount that was inserted in total
        for (ItemRoutePath inv : copy) {
            simStack = stack.copy();
            int ins = stack.getCount() - insert(inv, simStack, true, true).getCount();
            if (ins <= 0)
                continue;
            int didTransfer = didTransferTo(inv, simulate);
            EnhancedRoundRobinData data = new EnhancedRoundRobinData(inv, ins, didTransfer);
            transferred.add(data);

            min = Math.min(min, didTransfer);

            if (!steps.contains(didTransfer)) {
                steps.add(didTransfer);
            }
        }

        if (transferred.isEmpty() || steps.isEmpty())
            return stack;

        if (!simulate && min < Integer.MAX_VALUE) {
            decrementBy(min);
        }

        transferred.sort(Comparator.comparingInt(data -> data.transferred));
        steps.sort(Integer::compare);

        if (transferred.get(0).transferred != steps.get(0)) {
            return stack;
        }

        int amount = stack.getCount();
        int c = amount / transferred.size();
        int m = amount % transferred.size();
        List<EnhancedRoundRobinData> transferredCopy = new ArrayList<>(transferred);
        int nextStep = steps.removeInt(0);

        // equally distribute items over all inventories
        // it takes into account how much was inserted in total
        // f.e. if inv1 has 2 inserted and inv2 has 6 inserted, it will first try to insert 4 into inv1 so that both
        // have 6 and then it will distribute the rest equally
        outer:
        while (amount > 0 && !transferredCopy.isEmpty()) {
            Iterator<EnhancedRoundRobinData> iterator = transferredCopy.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                EnhancedRoundRobinData data = iterator.next();
                if (nextStep >= 0 && data.transferred >= nextStep)
                    break;

                int toInsert;
                if (nextStep <= 0) {
                    if (amount <= m) {
                        // break outer;
                        toInsert = 1;
                    } else {
                        toInsert = Math.min(c, amount);
                    }
                } else {
                    toInsert = Math.min(amount, nextStep - data.transferred);
                }
                if (data.toTransfer + toInsert >= data.maxInsertable) {
                    data.toTransfer = data.maxInsertable;
                    iterator.remove();
                } else {
                    data.toTransfer += toInsert;
                }

                data.transferred += toInsert;

                if ((amount -= toInsert) == 0) {
                    break outer;
                }
                i++;
            }

            for (EnhancedRoundRobinData data : transferredCopy) {
                if (data.transferred < nextStep)
                    continue outer;
            }
            if (steps.isEmpty()) {
                if (nextStep >= 0) {
                    c = amount / transferredCopy.size();
                    m = amount % transferredCopy.size();
                    nextStep = -1;
                }
            } else {
                nextStep = steps.removeInt(0);
            }
        }

        int inserted = 0;

        // finally actually insert the item
        for (EnhancedRoundRobinData data : transferred) {
            ItemStack toInsert = stack.copy();
            toInsert.setCount(data.toTransfer);
            int ins = data.toTransfer - insert(data.routePath, toInsert, simulate).getCount();
            inserted += ins;
            transferTo(data.routePath, simulate, ins);
        }

        ItemStack remainder = stack.copy();
        remainder.shrink(inserted);
        return remainder;
    }

    public ItemStack insert(ItemRoutePath routePath, ItemStack stack, boolean simulate) {
        return insert(routePath, stack, simulate, false);
    }

    public ItemStack insert(ItemRoutePath routePath, ItemStack stack, boolean simulate, boolean ignoreLimit) {
        int allowed = ignoreLimit ? stack.getCount() :
                checkTransferable(routePath.getProperties().getTransferRate(), stack.getCount(), simulate);
        if (allowed == 0 || !routePath.matchesFilters(stack)) {
            return stack;
        }
        Cover pipeCover = routePath.getTargetPipe().getCoverableImplementation()
                .getCoverAtSide(routePath.getTargetFacing());
        Cover tileCover = getCoverOnNeighbour(routePath.getTargetPipe(), routePath.getTargetFacing());

        if (pipeCover != null) {
            testHandler.setStackInSlot(0, stack.copy());
            IItemHandler itemHandler = pipeCover.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    testHandler);
            if (itemHandler == null || (itemHandler != testHandler &&
                    (allowed = itemHandler.extractItem(0, allowed, true).getCount()) <= 0)) {
                testHandler.setStackInSlot(0, ItemStack.EMPTY);
                return stack;
            }
            testHandler.setStackInSlot(0, ItemStack.EMPTY);
        }
        IItemHandler neighbourHandler = routePath.getHandler();
        if (pipeCover instanceof CoverRoboticArm &&
                ((CoverRoboticArm) pipeCover).getConveyorMode() == CoverConveyor.ConveyorMode.EXPORT) {
            return insertOverRobotArm(neighbourHandler, (CoverRoboticArm) pipeCover, stack, simulate, allowed,
                    ignoreLimit);
        }
        if (tileCover instanceof CoverRoboticArm &&
                ((CoverRoboticArm) tileCover).getConveyorMode() == CoverConveyor.ConveyorMode.IMPORT) {
            return insertOverRobotArm(neighbourHandler, (CoverRoboticArm) tileCover, stack, simulate, allowed,
                    ignoreLimit);
        }

        return insert(neighbourHandler, stack, simulate, allowed, ignoreLimit);
    }

    private ItemStack insert(IItemHandler handler, ItemStack stack, boolean simulate, int allowed,
                             boolean ignoreLimit) {
        if (stack.getCount() == allowed) {
            ItemStack re = GTTransferUtils.insertItem(handler, stack, simulate);
            if (!ignoreLimit)
                transfer(simulate, stack.getCount() - re.getCount());
            return re;
        }
        ItemStack toInsert = stack.copy();
        toInsert.setCount(Math.min(allowed, stack.getCount()));
        int r = GTTransferUtils.insertItem(handler, toInsert, simulate).getCount();
        if (!ignoreLimit)
            transfer(simulate, toInsert.getCount() - r);
        ItemStack remainder = stack.copy();
        remainder.setCount(r + (stack.getCount() - toInsert.getCount()));
        return remainder;
    }

    public Cover getCoverOnNeighbour(TileEntityItemPipe itemPipe, EnumFacing facing) {
        TileEntity tile = itemPipe.getNeighbor(facing);
        if (tile != null) {
            CoverHolder coverHolder = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER,
                    facing.getOpposite());
            if (coverHolder == null) return null;
            return coverHolder.getCoverAtSide(facing.getOpposite());
        }
        return null;
    }

    public ItemStack insertOverRobotArm(IItemHandler handler, CoverRoboticArm arm, ItemStack stack, boolean simulate,
                                        int allowed, boolean ignoreLimit) {
        var matched = arm.getItemFilterContainer().match(stack);
        boolean isStackSpecific = false;
        int rate, count;

        if (matched.isMatched()) {
            int index = matched.getFilterIndex();
            rate = arm.getItemFilterContainer().getTransferLimit(index);
            isStackSpecific = true;
        } else {
            rate = arm.getItemFilterContainer().getTransferSize();
        }

        switch (arm.getTransferMode()) {
            case TRANSFER_ANY:
                return insert(handler, stack, simulate, allowed, ignoreLimit);
            case KEEP_EXACT:
                count = rate - countStack(handler, stack, arm, isStackSpecific);
                if (count <= 0) return stack;
                count = Math.min(allowed, Math.min(stack.getCount(), count));
                return insert(handler, stack, simulate, count, ignoreLimit);
            case TRANSFER_EXACT:
                int max = allowed + arm.getBuffer();
                count = Math.min(max, Math.min(rate, stack.getCount()));
                if (count < rate) {
                    arm.buffer(allowed);
                    return stack;
                } else {
                    arm.clearBuffer();
                }
                if (insert(handler, stack, true, count, ignoreLimit).getCount() != stack.getCount() - count) {
                    return stack;
                }
                return insert(handler, stack, simulate, count, ignoreLimit);
        }
        return stack;
    }

    public static int countStack(IItemHandler handler, ItemStack stack, CoverRoboticArm arm, boolean isStackSpecific) {
        if (arm == null) return 0;
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack slot = handler.getStackInSlot(i);
            if (slot.isEmpty()) continue;
            if (isStackSpecific ? ItemStackHashStrategy.comparingAllButCount().equals(stack, slot) :
                    arm.getItemFilterContainer().test(slot)) {
                count += slot.getCount();
            }
        }
        return count;
    }

    private int checkTransferable(float rate, int amount, boolean simulate) {
        int max = (int) ((rate * 64) + 0.5);
        if (simulate)
            return Math.max(0, Math.min(max - simulatedTransfers, amount));
        else
            return Math.max(0, Math.min(max - pipe.getTransferredItems(), amount));
    }

    private void transfer(boolean simulate, int amount) {
        if (simulate)
            simulatedTransfers += amount;
        else
            pipe.addTransferredItems(amount);
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int i) {
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int i) {
        return 64;
    }

    private void transferTo(ItemRoutePath routePath, boolean simulate, int amount) {
        if (simulate)
            simulatedTransfersGlobalRoundRobin.merge(routePath.toFacingPos(), amount, Integer::sum);
        else
            pipe.getTransferred().merge(routePath.toFacingPos(), amount, Integer::sum);
    }

    private boolean contains(ItemRoutePath routePath, boolean simulate) {
        return simulate ? simulatedTransfersGlobalRoundRobin.containsKey(routePath.toFacingPos()) :
                pipe.getTransferred().containsKey(routePath.toFacingPos());
    }

    private int didTransferTo(ItemRoutePath routePath, boolean simulate) {
        if (simulate)
            return simulatedTransfersGlobalRoundRobin.getInt(routePath.toFacingPos());
        return pipe.getTransferred().getInt(routePath.toFacingPos());
    }

    private void resetTransferred(boolean simulated) {
        if (simulated)
            simulatedTransfersGlobalRoundRobin.clear();
        else
            pipe.resetTransferred();
    }

    private void decrementBy(int amount) {
        for (Object2IntMap.Entry<FacingPos> entry : pipe.getTransferred().object2IntEntrySet()) {
            entry.setValue(entry.getIntValue() - amount);
        }
    }

    private static class EnhancedRoundRobinData {

        private final ItemRoutePath routePath;
        private final int maxInsertable;
        private int transferred;
        private int toTransfer = 0;

        private EnhancedRoundRobinData(ItemRoutePath routePath, int maxInsertable, int transferred) {
            this.maxInsertable = maxInsertable;
            this.transferred = transferred;
            this.routePath = routePath;
        }
    }
}
