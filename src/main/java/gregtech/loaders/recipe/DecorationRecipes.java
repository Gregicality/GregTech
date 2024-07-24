package gregtech.loaders.recipe;

import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.BlockPanelling;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.item.EnumDyeColor;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.Steel;
import static gregtech.api.unification.ore.OrePrefix.plate;

public class DecorationRecipes {

    private DecorationRecipes() {}

    public static void init() {
        assemblerRecipes();
        dyeRecipes();
    }

    private static void assemblerRecipes() {
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.block, Materials.Concrete, 5)
                .input(OrePrefix.plate, Materials.Iron, 2)
                .circuitMeta(8)
                .outputs(MetaBlocks.METAL_SHEET.getItemVariant(EnumDyeColor.WHITE, 32))
                .EUt(4).duration(20)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.block, Materials.Concrete, 5)
                .input(OrePrefix.plate, Materials.Iron, 4)
                .circuitMeta(9)
                .outputs(MetaBlocks.LARGE_METAL_SHEET.getItemVariant(EnumDyeColor.WHITE, 32))
                .EUt(4).duration(20)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.block, Materials.Concrete, 3)
                .input(OrePrefix.plate, Materials.Rubber, 3)
                .circuitMeta(8)
                .outputs(MetaBlocks.STUDS.getItemVariant(EnumDyeColor.BLACK, 32))
                .EUt(4).duration(20)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).duration(120)
                .input(plate, Steel, 4)
                .circuitMeta(16)
                .outputs(MetaBlocks.PANELLING.getItemVariant(
                        BlockPanelling.PanellingType.GRAY))
                .buildAndRegister();
    }

    private static void dyeRecipes() {
        for (int i = 0; i < Materials.CHEMICAL_DYES.length; i++) {
            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputs(MetaBlocks.METAL_SHEET.getItemVariant(EnumDyeColor.WHITE))
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(9))
                    .outputs(MetaBlocks.METAL_SHEET.getItemVariant(EnumDyeColor.values()[i]))
                    .EUt(2).duration(10)
                    .buildAndRegister();

            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputs(MetaBlocks.LARGE_METAL_SHEET.getItemVariant(EnumDyeColor.WHITE))
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(9))
                    .outputs(MetaBlocks.LARGE_METAL_SHEET.getItemVariant(EnumDyeColor.values()[i]))
                    .EUt(2).duration(10)
                    .buildAndRegister();

            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputs(MetaBlocks.STUDS.getItemVariant(EnumDyeColor.BLACK))
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(9))
                    .outputs(MetaBlocks.STUDS.getItemVariant(EnumDyeColor.values()[i]))
                    .EUt(2).duration(10)
                    .buildAndRegister();

            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputs(MetaBlocks.PANELLING.getItemVariant(BlockPanelling.PanellingType.GRAY))
                    .fluidInputs(Materials.CHEMICAL_DYES[i].getFluid(9))
                    .outputs(MetaBlocks.PANELLING.getItemVariant(BlockPanelling.PanellingType.values()[i]))
                    .EUt(2).duration(10)
                    .buildAndRegister();
        }
    }
}
