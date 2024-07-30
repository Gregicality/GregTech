package gregtech.common.metatileentities.multi.electric.generator.turbine;

import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractLargeTurbine extends FuelMultiblockController {

    private final TurbineType turbineType;

    public AbstractLargeTurbine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier,
                                @NotNull TurbineType turbineType) {
        super(metaTileEntityId, recipeMap, tier);
        this.turbineType = turbineType;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCC", "CHHC", "CCCC")
                .aisle("CHHC", "RGGR", "CHHC")
                .aisle("CCCC", "CSHC", "CCCC")
                .where('S', selfPredicate())
                .where('G', states(getGearboxState()))
                .where('C', states(getCasingState()))
                .where('R', abilities(MultiblockAbility.ROTOR_HOLDER_2)
                        .or(abilities(MultiblockAbility.OUTPUT_ENERGY).setExactLimit(1)))
                .where('H', states(getCasingState())
                        .or(autoAbilities(false, true, true, true, true, true, true))
                        .or(autoAbilities(true, true)))
                .build();
    }

    protected abstract @NotNull IBlockState getCasingState();

    protected abstract @NotNull IBlockState getGearboxState();

    /**
     * @return the turbine type id of the turbine
     */
    public @NotNull TurbineType turbineType() {
        return turbineType;
    }
}
