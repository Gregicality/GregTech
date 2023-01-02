package gregtech.asm.hooks;

import com.google.common.collect.Lists;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ToolHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.List;

public class RecipeRepairItemHooks {

    public static boolean matchesGTTool(InventoryCrafting inv, World worldIn) {
        // from MC
        List<ItemStack> list = Lists.newArrayList();

        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack itemstack = inv.getStackInSlot(i);

            if (!itemstack.isEmpty()) {
                list.add(itemstack);

                if (list.size() > 1) {
                    ItemStack stack = list.get(0);
                    if (!stack.getItem().isRepairable()) return false;
                    if (stack.getCount() != 1 || itemstack.getCount() != 1) return false;
                    if (itemstack.getItem() != stack.getItem()) return false;
                    if (itemstack.getItem() instanceof IGTTool && stack.getItem() instanceof IGTTool) {
                        // require the same materials
                        IGTTool first = (IGTTool) itemstack.getItem();
                        IGTTool second = (IGTTool) stack.getItem();
                        return first.getToolMaterial(itemstack) == second.getToolMaterial(stack);
                    }
                }
            }
        }

        return list.size() == 2;
    }

    public static ItemStack resultGTTool(InventoryCrafting inv) {
        List<ItemStack> list = Lists.newArrayList();

        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack itemstack = inv.getStackInSlot(i);

            if (!itemstack.isEmpty()) {
                list.add(itemstack);

                if (list.size() > 1) {
                    ItemStack stack = list.get(0);
                    if (itemstack.getItem() != stack.getItem()) return ItemStack.EMPTY;
                    if (stack.getCount() != 1 || itemstack.getCount() != 1) return ItemStack.EMPTY;
                    if (!stack.getItem().isRepairable()) return ItemStack.EMPTY;
                }
            }
        }

        if (list.size() == 2) {
            ItemStack first = list.get(0);
            ItemStack second = list.get(1);

            if (first.getItem() == second.getItem() && first.getCount() == 1 && second.getCount() == 1 && first.getItem().isRepairable()) {
                int j = first.getMaxDamage() - first.getItemDamage();
                int k = first.getMaxDamage() - second.getItemDamage();
                int l = j + k + first.getMaxDamage() * 5 / 100;
                int i1 = first.getMaxDamage() - l;

                if (i1 < 0) {
                    i1 = 0;
                }

                if (first.getItem() instanceof IGTTool && second.getItem() instanceof IGTTool) {
                    ItemStack output = first.copy();
                    NBTTagCompound toolTag = ToolHelper.getToolTag(output);
                    toolTag.setInteger(ToolHelper.DURABILITY_KEY, i1);
                    return output;
                }

                return new ItemStack(first.getItem(), 1, i1);
            }
        }

        return ItemStack.EMPTY;
    }
}
