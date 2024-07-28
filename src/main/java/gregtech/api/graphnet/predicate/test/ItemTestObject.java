package gregtech.api.graphnet.predicate.test;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Range;

import java.util.Objects;

public class ItemTestObject implements IPredicateTestObject {

    public final Item item;
    public final int meta;
    public final NBTTagCompound tag;

    public final int stackLimit;

    public ItemTestObject(ItemStack stack) {
        item = stack.getItem();
        meta = stack.getMetadata();
        tag = stack.getTagCompound();
        stackLimit = stack.getMaxStackSize();
    }

    public ItemStack recombine() {
        return new ItemStack(item, 1, meta, tag);
    }

    public ItemStack recombine(int amount) {
        assert amount < getStackLimit() && amount > 0;
        return new ItemStack(item, amount, meta, tag);
    }

    public int getStackLimit() {
        return stackLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemTestObject that = (ItemTestObject) o;
        return meta == that.meta && Objects.equals(item, that.item) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, meta, tag);
    }
}