package gregtech.integration.jei.basic;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class CoolantInfo implements IRecipeWrapper {

    public FluidStack coolant;
    public FluidStack hotCoolant;

    public CoolantInfo(Fluid coolant, Fluid hotCoolant) {
        this.coolant = new FluidStack(coolant, 1000);
        this.hotCoolant = new FluidStack(hotCoolant, 1000);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, coolant);
        ingredients.setOutput(VanillaTypes.FLUID, hotCoolant);
    }
}
