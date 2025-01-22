package gregtech.common.items.armor.behavior;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.impl.GTFluidHandlerItemStack;
import gregtech.api.items.armoritem.ArmorHelper;
import gregtech.api.items.armoritem.jetpack.IJetpackStats;
import gregtech.api.items.armoritem.jetpack.JetpackBehavior;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Creates a Jetpack which runs on any Fuel in a provided fuel RecipeMap.
 */
public class FueledJetpackBehavior extends JetpackBehavior {

    public static final Function<FluidStack, Integer> COMBUSTION_FUEL_BURN_TIME = fluidStack -> {
        Recipe recipe = RecipeMaps.COMBUSTION_GENERATOR_FUELS.findRecipe(
                Integer.MAX_VALUE,
                Collections.emptyList(),
                Collections.singletonList(fluidStack));
        return recipe != null ? recipe.getDuration() : 0;
    };

    private final int maxCapacity;
    private final Function<FluidStack, Integer> fuelBurnTimeFunc;

    public FueledJetpackBehavior(IJetpackStats jetpackStats, int maxCapacity,
                                 Function<FluidStack, Integer> fuelBurnTimeFunc) {
        super(jetpackStats);
        this.maxCapacity = maxCapacity;
        this.fuelBurnTimeFunc = fuelBurnTimeFunc;
    }

    @Override
    protected int getFuelPerUse() {
        return 1;
    }

    @Override
    protected boolean drainFuel(@NotNull ItemStack stack, int amount, boolean simulate) {
        NBTTagCompound tag = ArmorHelper.getBehaviorsTag(stack);
        short burnTimer = tag.getShort(ArmorHelper.FUELED_JETPACK_BURN_TIMER);
        if (burnTimer > 0) {
            if (!simulate) {
                tag.setShort(ArmorHelper.FUELED_JETPACK_BURN_TIMER, (short) (burnTimer - 1));
            }
            return true;
        }

        IFluidHandlerItem fluidHandler = getFluidHandler(stack);
        if (fluidHandler == null) return false;
        FluidStack fuelStack = fluidHandler.drain(amount, false);
        if (fuelStack == null) return false;

        int burnTime = fuelBurnTimeFunc.apply(fuelStack);
        if (burnTime <= 0) return false;

        if (!simulate) {
            tag.setShort(ArmorHelper.FUELED_JETPACK_BURN_TIMER, (short) burnTime);
            fluidHandler.drain(amount, true);
        }
        return true;
    }

    private IFluidHandlerItem getFluidHandler(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    }

    @Override
    public void addHudInformation(@NotNull ItemStack stack, @NotNull List<String> hudText) {
        IFluidHandlerItem fluidHandler = getFluidHandler(stack);
        if (fluidHandler != null) {
            IFluidTankProperties[] properties = fluidHandler.getTankProperties();
            if (properties[0] != null) {
                FluidStack fuel = properties[0].getContents();
                int capacity = properties[0].getCapacity();
                if (fuel != null && fuel.amount != 0) {
                    String formatted = String.format("%.1f", fuel.amount * 100.0F / capacity);
                    hudText.add(I18n.format("metaarmor.hud.fuel_lvl", formatted + "%"));
                }
            }
        }
        // call super last so that fuel HUD info is in the same spot that energy would be
        super.addHudInformation(stack, hudText);
    }

    // todo move this to ItemGTFueledArmor if that ends up staying around
    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip) {
        IFluidHandlerItem fluidHandler = getFluidHandler(stack);
        if (fluidHandler != null) {
            IFluidTankProperties prop = fluidHandler.getTankProperties()[0];
            FluidStack fuel = prop.getContents();
            tooltip.add(I18n.format("metaitem.generic.fluid_container.tooltip",
                    fuel != null ? fuel.amount : 0,
                    prop.getCapacity(),
                    fuel != null ? fuel.getLocalizedName() : ""));
        }
        super.addInformation(stack, world, tooltip);
    }

    @Override
    public ICapabilityProvider createProvider(@NotNull ItemStack stack, @Nullable NBTTagCompound tag) {
        return new GTFluidHandlerItemStack(stack, maxCapacity).setFilter(new FueledJetpackFilter());
    }

    private final class FueledJetpackFilter implements IFilter<FluidStack> {

        @Override
        public boolean test(@NotNull FluidStack fluidStack) {
            return fuelBurnTimeFunc.apply(fluidStack) > 0;
        }

        @Override
        public int getPriority() {
            return IFilter.whitelistLikePriority();
        }
    }
}
