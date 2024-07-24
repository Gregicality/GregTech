package gregtech.loaders.recipe.handlers;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.CoolantProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.common.ConfigHolder;

public class FluidRecipeHandler {

    public static void runRecipeGeneration() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty(PropertyKey.COOLANT))
                processCoolant(material, material.getProperty(PropertyKey.COOLANT));
        }
    }

    public static void processCoolant(Material mat, CoolantProperty coolant) {
        int waterAmt = 6;
        double multiplier = ConfigHolder.machines.nuclear.heatExchangerEfficiencyMultiplier;

        // water temp difference * water heat capacity * amount / coolantHeatCapacity * (hotHpTemp - coolantTemp)
        int coolantAmt = (int) Math.ceil(100 * 4168 * waterAmt / (coolant.getSpecificHeatCapacity() *
                (coolant.getHotHPCoolant().getFluid().getTemperature() - mat.getFluid().getTemperature())) *
                multiplier);

        RecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(1).circuitMeta(1)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(coolantAmt), Materials.Water.getFluid(waterAmt))
                .fluidOutputs(mat.getFluid(coolantAmt), Materials.Steam.getFluid(waterAmt * 160)).buildAndRegister();

        RecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(1).circuitMeta(1)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(coolantAmt),
                        Materials.DistilledWater.getFluid(waterAmt))
                .fluidOutputs(mat.getFluid(coolantAmt), Materials.Steam.getFluid(waterAmt * 160)).buildAndRegister();
        waterAmt = 600;
        // Slightly more efficient
        coolantAmt = (int) Math.ceil(100 * 4168 * waterAmt / (coolant.getSpecificHeatCapacity() *
                (coolant.getHotHPCoolant().getFluid().getTemperature() - mat.getFluid().getTemperature())) *
                multiplier);

        RecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(1).circuitMeta(2)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(coolantAmt), Materials.Water.getFluid(waterAmt))
                .fluidOutputs(mat.getFluid(coolantAmt), Materials.Steam.getFluid(waterAmt * 160)).buildAndRegister();

        RecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(1).circuitMeta(2)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(coolantAmt),
                        Materials.DistilledWater.getFluid(waterAmt))
                .fluidOutputs(mat.getFluid(coolantAmt), Materials.Steam.getFluid(waterAmt * 160)).buildAndRegister();

        // Radiator
        RecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(10).circuitMeta(3)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(8000)).fluidOutputs(mat.getFluid(8000))
                .buildAndRegister();
    }
}
