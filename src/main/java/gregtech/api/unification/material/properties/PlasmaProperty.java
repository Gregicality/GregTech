package gregtech.api.unification.material.properties;

import com.google.common.base.Preconditions;
import crafttweaker.api.liquid.ILiquidDefinition;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gregtech.api.GTValues;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class PlasmaProperty implements IMaterialProperty {

    /**
     * Internal material plasma fluid field
     */
    private Fluid plasma;

    @Override
    public void verifyProperty(Properties properties) {
    }

    //@ZenGetter("hasPlasma") // todo relocate and rework
    public boolean shouldGeneratePlasma() {
        return true;
    }

    /**
     * internal usage only
     */
    public void setPlasma(@Nonnull Fluid plasma) {
        Preconditions.checkNotNull(plasma);
        this.plasma = plasma;
    }

    @Nonnull
    public Fluid getPlasma() {
        return plasma;
    }

    @Nonnull
    public FluidStack getPlasma(int amount) {
        return new FluidStack(plasma, amount);
    }

    //@ZenGetter("plasma")
    @net.minecraftforge.fml.common.Optional.Method(modid = GTValues.MODID_CT)
    public ILiquidDefinition ctGetPlasma() {
        return CraftTweakerMC.getILiquidDefinition(plasma);
    }
}
