package gregtech.integration.jei.basic;

import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class FissionFuelInfo implements IRecipeWrapper {

    public ItemStack rod;
    public ItemStack depletedRod;

    public FissionFuelInfo(ItemStack rod, ItemStack depletedRod) {
        this.rod = rod;
        this.depletedRod = depletedRod;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, rod);
        ingredients.setOutput(VanillaTypes.ITEM, depletedRod);
    }
}
