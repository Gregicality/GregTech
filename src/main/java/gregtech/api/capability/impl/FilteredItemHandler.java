package gregtech.api.capability.impl;

import gregtech.api.capability.IFilter;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@Deprecated
public class FilteredItemHandler extends GTItemStackHandler {

    public static Predicate<ItemStack> getCapabilityFilter(Capability<?> cap) {
        return stack -> stack.hasCapability(cap, null);
    }

    public FilteredItemHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity, 1);
    }

    public FilteredItemHandler(MetaTileEntity metaTileEntity, int size) {
        super(metaTileEntity, size);
    }

    public FilteredItemHandler(MetaTileEntity metaTileEntity, NonNullList<ItemStack> stacks) {
        super(metaTileEntity, stacks);
    }

    public FilteredItemHandler setFillPredicate(Predicate<ItemStack> fillPredicate) {
        setFilter(new IFilter<>() {

            @Override
            public boolean test(@NotNull ItemStack stack) {
                return fillPredicate.test(stack);
            }

            @Override
            public int getPriority() {
                return IFilter.noPriority();
            }
        });
        return this;
    }
}
