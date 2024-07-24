package gregtech.integration.forestry.recipes;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;
import gregtech.common.items.MetaItems;
import gregtech.integration.forestry.ForestryUtil;
import gregtech.integration.forestry.bees.GTCombItem;
import gregtech.integration.forestry.bees.GTCombType;
import gregtech.integration.forestry.bees.GTDropType;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.core.Api;
import com.google.common.collect.ImmutableMap;
import forestry.api.recipes.ICentrifugeRecipe;
import forestry.api.recipes.RecipeManagers;
import forestry.core.ModuleCore;
import forestry.factory.MachineUIDs;
import forestry.factory.ModuleFactory;

import java.util.Arrays;
import java.util.Map;

public class CombRecipes {

    public static void initForestryCombs() {
        if (!ModuleFactory.machineEnabled(MachineUIDs.CENTRIFUGE)) return;
        for (ICentrifugeRecipe recipe : RecipeManagers.centrifugeManager.recipes()) {
            // For some reason Forestry occasionally has recipes that have no outputs at all, which will
            // cause us to error. Discard these if we come across them.
            if (recipe.getAllProducts().isEmpty()) continue;

            ItemStack combStack = recipe.getInput();
            if (combStack.getItem() instanceof GTCombItem) continue;
            RecipeBuilder<?> builder = RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                    .inputs(combStack.copy())
                    .duration(Voltage.ULV.getCentrifugeTime()).EUt(Voltage.ULV.getCentrifugeEnergy());

            for (Map.Entry<ItemStack, Float> entry : recipe.getAllProducts().entrySet()) {
                if (entry.getValue() >= 1.0f) {
                    builder.outputs(entry.getKey());
                } else {
                    builder.chancedOutput(entry.getKey(), Math.max(1, Math.round(entry.getValue() * 10000)), 0);
                }
            }
            builder.buildAndRegister();
        }
    }

    // forgive me for the code i am about to write
    public static void initGTCombs() {
        // Organic
        addProcessGT(GTCombType.COAL, new Material[] { Materials.Coal }, Voltage.LV);
        addProcessGT(GTCombType.COKE, new Material[] { Materials.Coke }, Voltage.LV);
        addCentrifugeToItemStack(GTCombType.STICKY,
                new ItemStack[] { MetaItems.STICKY_RESIN.getStackForm(), MetaItems.PLANT_BALL.getStackForm(),
                        ModuleCore.getItems().beeswax.getItemStack() },
                new int[] { 50 * 100, 15 * 100, 50 * 100 }, Voltage.ULV);
        addProcessGT(GTCombType.OIL, new Material[] { Materials.Oilsands }, Voltage.LV);
        addProcessGT(GTCombType.APATITE, new Material[] { Materials.Apatite, Materials.TricalciumPhosphate },
                Voltage.LV);
        addCentrifugeToMaterial(GTCombType.ASH, new Material[] { Materials.DarkAsh, Materials.Ash },
                new int[] { 50 * 100, 50 * 100 }, new int[] { 9, 9 }, Voltage.ULV, ItemStack.EMPTY, 50 * 100);
        addCentrifugeToItemStack(GTCombType.BIOMASS,
                new ItemStack[] { ForestryUtil.getDropStack(GTDropType.BIOMASS),
                        ForestryUtil.getDropStack(GTDropType.ETHANOL), ModuleCore.getItems().beeswax.getItemStack() },
                new int[] { 70 * 100, 30 * 100, 50 * 100 }, Voltage.ULV);
        addCentrifugeToItemStack(GTCombType.PHOSPHORUS,
                new ItemStack[] { OreDictUnifier.get(OrePrefix.dust, Materials.Phosphorus),
                        OreDictUnifier.get(OrePrefix.dust, Materials.TricalciumPhosphate),
                        ModuleCore.getItems().beeswax.getItemStack() },
                new int[] { 100 * 100, 100 * 100, 100 * 100 }, Voltage.HV);
        addCentrifugeToItemStack(GTCombType.COAL, new ItemStack[] { OreDictUnifier.get(OrePrefix.gem, Materials.Coal),
                ModuleCore.getItems().beeswax.getItemStack() }, new int[] { 5 * 100, 50 * 100 }, Voltage.ULV);
        addCentrifugeToItemStack(GTCombType.COKE, new ItemStack[] { OreDictUnifier.get(OrePrefix.gem, Materials.Coke),
                ModuleCore.getItems().beeswax.getItemStack() }, new int[] { 5 * 100, 50 * 100 }, Voltage.ULV);
        addCentrifugeToItemStack(GTCombType.OIL,
                new ItemStack[] { OreDictUnifier.get(OrePrefix.dustTiny, Materials.Oilsands),
                        ForestryUtil.getDropStack(GTDropType.OIL), ModuleCore.getItems().beeswax.getItemStack() },
                new int[] { 70 * 100, 100 * 100, 50 * 100 }, Voltage.ULV);

        // Industrial
        addCentrifugeToItemStack(GTCombType.ENERGY,
                new ItemStack[] { MetaItems.ENERGIUM_DUST.getStackForm(),
                        ModuleCore.getItems().refractoryWax.getItemStack() },
                new int[] { 20 * 100, 50 * 100 }, Voltage.HV, 196);
        ItemStack wax = ModuleCore.getItems().beeswax.getItemStack();
        if (Mods.MagicBees.isModLoaded()) {
            wax = Mods.MagicBees.getItem("wax", 2);
        }
        addCentrifugeToItemStack(GTCombType.LAPOTRON,
                new ItemStack[] { OreDictUnifier.get(OrePrefix.dust, Materials.Lapotron), wax },
                new int[] { 100 * 100, 40 * 100 }, Voltage.HV, 196);

        // Alloy
        addProcessGT(GTCombType.REDALLOY, new Material[] { Materials.RedAlloy, Materials.Redstone, Materials.Copper },
                Voltage.LV);
        addProcessGT(GTCombType.STAINLESSSTEEL, new Material[] { Materials.StainlessSteel, Materials.Iron,
                Materials.Chrome, Materials.Manganese, Materials.Nickel }, Voltage.HV);
        addCentrifugeToMaterial(GTCombType.REDALLOY, new Material[] { Materials.RedAlloy }, new int[] { 100 * 100 },
                new int[] { 9 }, Voltage.ULV, ModuleCore.getItems().refractoryWax.getItemStack(), 50 * 100);
        addCentrifugeToMaterial(GTCombType.STAINLESSSTEEL, new Material[] { Materials.StainlessSteel },
                new int[] { 50 * 100 }, new int[] { 9 }, Voltage.HV, ModuleCore.getItems().refractoryWax.getItemStack(),
                50 * 100);

        // Gem
        addProcessGT(GTCombType.STONE, new Material[] { Materials.Soapstone, Materials.Talc, Materials.Apatite,
                Materials.Phosphate, Materials.TricalciumPhosphate }, Voltage.LV);
        addProcessGT(GTCombType.CERTUS,
                new Material[] { Materials.CertusQuartz, Materials.Quartzite, Materials.Barite }, Voltage.LV);
        addProcessGT(GTCombType.REDSTONE, new Material[] { Materials.Redstone, Materials.Cinnabar }, Voltage.LV);
        addCentrifugeToMaterial(GTCombType.RAREEARTH, new Material[] { Materials.RareEarth }, new int[] { 100 * 100 },
                new int[] { 9 }, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
        addProcessGT(GTCombType.LAPIS,
                new Material[] { Materials.Lapis, Materials.Sodalite, Materials.Lazurite, Materials.Calcite },
                Voltage.LV);
        addProcessGT(GTCombType.RUBY, new Material[] { Materials.Ruby, Materials.Redstone }, Voltage.LV);
        addProcessGT(GTCombType.SAPPHIRE,
                new Material[] { Materials.Sapphire, Materials.GreenSapphire, Materials.Almandine, Materials.Pyrope },
                Voltage.LV);
        addProcessGT(GTCombType.DIAMOND, new Material[] { Materials.Diamond, Materials.Graphite }, Voltage.LV);
        addProcessGT(GTCombType.OLIVINE, new Material[] { Materials.Olivine, Materials.Bentonite, Materials.Magnesite,
                Materials.GlauconiteSand }, Voltage.LV);
        addProcessGT(GTCombType.EMERALD, new Material[] { Materials.Emerald, Materials.Beryllium, Materials.Thorium },
                Voltage.LV);
        addProcessGT(GTCombType.PYROPE,
                new Material[] { Materials.Pyrope, Materials.Aluminium, Materials.Magnesium, Materials.Silicon },
                Voltage.LV);
        addProcessGT(GTCombType.GROSSULAR,
                new Material[] { Materials.Grossular, Materials.Aluminium, Materials.Silicon }, Voltage.LV);
        addCentrifugeToMaterial(GTCombType.STONE,
                new Material[] { Materials.Stone, Materials.GraniteBlack, Materials.GraniteRed, Materials.Basalt,
                        Materials.Marble },
                new int[] { 70 * 100, 50 * 100, 50 * 100, 50 * 100, 50 * 100 }, new int[] { 9, 9, 9, 9, 9 },
                Voltage.ULV, ItemStack.EMPTY, 50 * 100);

        // Metals
        addProcessGT(GTCombType.COPPER, new Material[] { Materials.Copper, Materials.Tetrahedrite,
                Materials.Chalcopyrite, Materials.Malachite, Materials.Pyrite, Materials.Stibnite }, Voltage.LV);
        addProcessGT(GTCombType.TIN, new Material[] { Materials.Tin, Materials.Cassiterite, Materials.CassiteriteSand },
                Voltage.LV);
        addProcessGT(GTCombType.LEAD, new Material[] { Materials.Lead, Materials.Galena }, Voltage.LV);
        addProcessGT(GTCombType.NICKEL, new Material[] { Materials.Nickel, Materials.Garnierite, Materials.Pentlandite,
                Materials.Cobaltite, Materials.Wulfenite, Materials.Powellite }, Voltage.LV);
        addProcessGT(GTCombType.ZINC, new Material[] { Materials.Sphalerite, Materials.Sulfur }, Voltage.LV);
        addProcessGT(GTCombType.SILVER, new Material[] { Materials.Silver, Materials.Galena }, Voltage.LV);
        addProcessGT(GTCombType.GOLD, new Material[] { Materials.Gold, Materials.Magnetite }, Voltage.LV);
        addProcessGT(GTCombType.SULFUR, new Material[] { Materials.Sulfur, Materials.Pyrite, Materials.Sphalerite },
                Voltage.LV);
        addProcessGT(GTCombType.GALLIUM, new Material[] { Materials.Gallium, Materials.Niobium }, Voltage.LV);
        addProcessGT(GTCombType.ARSENIC, new Material[] { Materials.Realgar, Materials.Cassiterite, Materials.Zeolite },
                Voltage.LV);
        addProcessGT(
                GTCombType.IRON, new Material[] { Materials.Iron, Materials.Magnetite, Materials.BrownLimonite,
                        Materials.YellowLimonite, Materials.VanadiumMagnetite, Materials.BandedIron, Materials.Pyrite },
                Voltage.LV);

        addCentrifugeToMaterial(GTCombType.SLAG,
                new Material[] { Materials.Stone, Materials.GraniteBlack, Materials.GraniteRed },
                new int[] { 50 * 100, 20 * 100, 20 * 100 }, new int[] { 9, 9, 9 }, Voltage.ULV, ItemStack.EMPTY,
                30 * 100);
        addCentrifugeToMaterial(GTCombType.COPPER, new Material[] { Materials.Copper }, new int[] { 70 * 100 },
                new int[] { 9 }, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
        addCentrifugeToMaterial(GTCombType.TIN, new Material[] { Materials.Tin }, new int[] { 60 * 100 },
                new int[] { 9 }, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
        addCentrifugeToMaterial(GTCombType.LEAD, new Material[] { Materials.Lead }, new int[] { 45 * 100 },
                new int[] { 9 }, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
        addCentrifugeToMaterial(GTCombType.IRON, new Material[] { Materials.Iron }, new int[] { 30 * 100 },
                new int[] { 9 }, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
        addCentrifugeToMaterial(GTCombType.STEEL, new Material[] { Materials.Steel }, new int[] { 40 * 100 },
                new int[] { 9 }, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
        addCentrifugeToMaterial(GTCombType.SILVER, new Material[] { Materials.Silver }, new int[] { 80 * 100 },
                new int[] { 9 }, Voltage.ULV, ItemStack.EMPTY, 30 * 100);

        // Rare Metals
        addProcessGT(GTCombType.BAUXITE, new Material[] { Materials.Bauxite, Materials.Aluminium }, Voltage.LV);
        addProcessGT(GTCombType.ALUMINIUM, new Material[] { Materials.Aluminium, Materials.Bauxite }, Voltage.LV);
        addProcessGT(GTCombType.MANGANESE, new Material[] { Materials.Manganese, Materials.Grossular,
                Materials.Spessartine, Materials.Pyrolusite, Materials.Tantalite }, Voltage.LV);
        addProcessGT(GTCombType.TITANIUM,
                new Material[] { Materials.Titanium, Materials.Ilmenite, Materials.Bauxite, Materials.Rutile },
                Voltage.EV);
        addProcessGT(GTCombType.MAGNESIUM, new Material[] { Materials.Magnesium, Materials.Magnesite }, Voltage.LV);
        addProcessGT(GTCombType.CHROME, new Material[] { Materials.Chrome, Materials.Ruby, Materials.Chromite,
                Materials.Redstone, Materials.Neodymium, Materials.Bastnasite }, Voltage.HV);
        addProcessGT(GTCombType.TUNGSTEN,
                new Material[] { Materials.Tungsten, Materials.Tungstate, Materials.Scheelite, Materials.Lithium },
                Voltage.IV);
        addProcessGT(GTCombType.PLATINUM,
                new Material[] { Materials.Platinum, Materials.Cooperite, Materials.Palladium }, Voltage.HV);
        addProcessGT(GTCombType.MOLYBDENUM, new Material[] { Materials.Molybdenum, Materials.Molybdenite,
                Materials.Powellite, Materials.Wulfenite }, Voltage.LV);
        addProcessGT(GTCombType.LITHIUM,
                new Material[] { Materials.Lithium, Materials.Lepidolite, Materials.Spodumene }, Voltage.MV);
        addProcessGT(GTCombType.SALT, new Material[] { Materials.Salt, Materials.RockSalt, Materials.Saltpeter },
                Voltage.MV);
        addProcessGT(GTCombType.ELECTROTINE,
                new Material[] { Materials.Electrotine, Materials.Electrum, Materials.Redstone }, Voltage.MV);
        addCentrifugeToMaterial(GTCombType.SALT,
                new Material[] { Materials.Salt, Materials.RockSalt, Materials.Saltpeter },
                new int[] { 100 * 100, 100 * 100, 25 * 100 }, new int[] { 9 * 6, 9 * 6, 9 * 6 }, Voltage.MV, 160,
                ItemStack.EMPTY, 50 * 100);

        // Special Iridium Recipe
        RecipeMaps.CHEMICAL_RECIPES.recipeBuilder()
                .inputs(ForestryUtil.getCombStack(GTCombType.IRIDIUM, 4))
                .fluidInputs(Voltage.IV.getFluid())
                .output(OrePrefix.nugget, Materials.Iridium)
                .output(OrePrefix.dust, Materials.IridiumMetalResidue, 5)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1000).EUt(Voltage.IV.getChemicalEnergy())
                .buildAndRegister();

        // Special Osmium Recipe
        RecipeMaps.CHEMICAL_RECIPES.recipeBuilder()
                .inputs(ForestryUtil.getCombStack(GTCombType.OSMIUM, 4))
                .fluidInputs(Voltage.IV.getFluid())
                .output(OrePrefix.nugget, Materials.Osmium)
                .fluidOutputs(Materials.AcidicOsmiumSolution.getFluid(2000))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(1000).EUt(Voltage.IV.getChemicalEnergy())
                .buildAndRegister();

        // Special Indium Recipe
        RecipeMaps.CHEMICAL_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.Aluminium, 4)
                .inputs(ForestryUtil.getCombStack(GTCombType.INDIUM))
                .fluidInputs(Materials.IndiumConcentrate.getFluid(1000))
                .output(OrePrefix.dustSmall, Materials.Indium, 2)
                .output(OrePrefix.dust, Materials.AluminiumSulfite, 4)
                .fluidOutputs(Materials.LeadZincSolution.getFluid(1000))
                .duration(50).EUt(600).buildAndRegister();

        // Radioactive
        addProcessGT(GTCombType.ALMANDINE,
                new Material[] { Materials.Almandine, Materials.Pyrope, Materials.Sapphire, Materials.GreenSapphire },
                Voltage.LV);
        addProcessGT(GTCombType.URANIUM, new Material[] { Materials.Uranium, Materials.Pitchblende,
                Materials.Uraninite, Materials.Uranium235 }, Voltage.EV);
        addProcessGT(GTCombType.PLUTONIUM, new Material[] { Materials.Plutonium239, Materials.Uranium235 }, Voltage.EV);
        addProcessGT(GTCombType.NAQUADAH,
                new Material[] { Materials.Naquadah, Materials.NaquadahEnriched, Materials.Naquadria }, Voltage.IV);
        addProcessGT(GTCombType.NAQUADRIA,
                new Material[] { Materials.Naquadria, Materials.NaquadahEnriched, Materials.Naquadah }, Voltage.LUV);
        addProcessGT(GTCombType.THORIUM, new Material[] { Materials.Thorium, Materials.Uranium, Materials.Coal },
                Voltage.EV);
        addProcessGT(GTCombType.LUTETIUM, new Material[] { Materials.Lutetium, Materials.Thorium }, Voltage.IV);
        addProcessGT(GTCombType.AMERICIUM, new Material[] { Materials.Americium, Materials.Lutetium }, Voltage.LUV);
        addProcessGT(GTCombType.TRINIUM, new Material[] { Materials.Trinium, Materials.Naquadah, Materials.Naquadria },
                Voltage.ZPM);

        // Special Neutronium Recipe
        RecipeMaps.CHEMICAL_RECIPES.recipeBuilder()
                .inputs(ForestryUtil.getCombStack(GTCombType.NEUTRONIUM, 4))
                .fluidInputs(Voltage.UV.getFluid())
                .output(OrePrefix.nugget, Materials.Neutronium)
                .fluidOutputs(Materials.Neutronium.getFluid(GTValues.L * 4))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(3000).EUt(Voltage.UV.getChemicalEnergy()).buildAndRegister();

        if (Mods.MagicBees.isModLoaded()) {
            addProcessGT(GTCombType.SPARKLING, new Material[] { Materials.NetherStar }, Voltage.EV);
            addCentrifugeToItemStack(GTCombType.SPARKLING,
                    new ItemStack[] { Mods.MagicBees.getItem("wax", 0), Mods.MagicBees.getItem("resource", 5),
                            OreDictUnifier.get(OrePrefix.dustTiny, Materials.NetherStar) },
                    new int[] { 50 * 100, 10 * 100, 10 * 100 }, Voltage.EV);
        }

        addExtractorProcess(GTCombType.HELIUM, Materials.Helium.getFluid(250), Voltage.HV, 100);
        addExtractorProcess(GTCombType.ARGON, Materials.Argon.getFluid(250), Voltage.MV, 100);
        addExtractorProcess(GTCombType.XENON, Materials.Xenon.getFluid(250), Voltage.IV, 50);
        addExtractorProcess(GTCombType.NEON, Materials.Neon.getFluid(250), Voltage.IV, 15);
        addExtractorProcess(GTCombType.KRYPTON, Materials.Krypton.getFluid(250), Voltage.IV, 25);
        addExtractorProcess(GTCombType.NITROGEN, Materials.Nitrogen.getFluid(500), Voltage.MV, 100);
        addExtractorProcess(GTCombType.OXYGEN, Materials.Oxygen.getFluid(500), Voltage.MV, 100);
        addExtractorProcess(GTCombType.HYDROGEN, Materials.Hydrogen.getFluid(500), Voltage.MV, 100);
        addExtractorProcess(GTCombType.FLUORINE, Materials.Fluorine.getFluid(250), Voltage.MV, 128);

        if (Mods.AppliedEnergistics2.isModLoaded()) {
            ItemStack fluixDust = OreDictUnifier.get("dustFluix");
            if (fluixDust == ItemStack.EMPTY) {
                fluixDust = Api.INSTANCE.definitions().materials().fluixDust().maybeStack(1).orElse(ItemStack.EMPTY);
            }
            if (fluixDust != ItemStack.EMPTY) {
                addCentrifugeToItemStack(GTCombType.FLUIX,
                        new ItemStack[] { fluixDust, ModuleCore.getItems().beeswax.getItemStack() },
                        new int[] { 25 * 100, 30 * 100 }, Voltage.ULV);
            }
        }
    }

    private static void addChemicalProcess(GTCombType comb, Material inMaterial, Material outMaterial, Voltage volt) {
        if (OreDictUnifier.get(OrePrefix.crushedPurified, outMaterial, 4).isEmpty() ||
                OreDictUnifier.get(OrePrefix.crushed, inMaterial).isEmpty() ||
                inMaterial.hasFlag(MaterialFlags.DISABLE_ORE_BLOCK))
            return;

        RecipeBuilder<?> builder = RecipeMaps.CHEMICAL_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(9, ForestryUtil.getCombStack(comb)))
                .input(OrePrefix.crushed, inMaterial)
                .fluidInputs(volt.getFluid())
                .output(OrePrefix.crushedPurified, outMaterial, 4)
                .duration(volt.getChemicalTime())
                .EUt(volt.getChemicalEnergy());

        OreProperty property = inMaterial.getProperty(PropertyKey.ORE);
        if (property != null && !property.getOreByProducts().isEmpty()) {
            Material byproduct = property.getOreByProducts().get(0);
            if (byproduct != null && byproduct.hasProperty(PropertyKey.FLUID)) {
                if (!byproduct.hasProperty(PropertyKey.BLAST)) {
                    builder.fluidOutputs(byproduct.getFluid(GTValues.L));
                }
            }
        }

        if (volt.compareTo(Voltage.IV) > 0) {
            builder.cleanroom(CleanroomType.CLEANROOM);
        }
        builder.buildAndRegister();
    }

    /**
     * Currently only used separately for GTCombType.MOLYBDENUM
     *
     * @param circuitNumber should not conflict with addProcessGT
     **/
    private static void addAutoclaveProcess(GTCombType comb, Material material, Voltage volt, int circuitNumber) {
        if (OreDictUnifier.get(OrePrefix.crushedPurified, material, 4).isEmpty()) return;

        RecipeBuilder<?> builder = RecipeMaps.AUTOCLAVE_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(9, ForestryUtil.getCombStack(comb)))
                .circuitMeta(circuitNumber)
                .fluidInputs(
                        Materials.Mutagen.getFluid((int) Math.max(1, material.getMass() + volt.getMutagenAmount())))
                .output(OrePrefix.crushedPurified, material, 4)
                .duration((int) (material.getMass() * 128))
                .EUt(volt.getAutoclaveEnergy());

        if (volt.compareTo(Voltage.HV) > 0) {
            builder.cleanroom(CleanroomType.CLEANROOM);
        }
        builder.buildAndRegister();
    }

    private static void addExtractorProcess(GTCombType comb, FluidStack fluidStack, Voltage volt, int duration) {
        RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(ForestryUtil.getCombStack(comb))
                .fluidOutputs(fluidStack)
                .duration(duration)
                .EUt(volt.getCentrifugeEnergy() / 2)
                .buildAndRegister();
    }

    /**
     * this only adds Chemical and AutoClave process.
     * If you need Centrifuge recipe. use addCentrifugeToMaterial or addCentrifugeToItemStack
     *
     * @param volt     This determines the required Tier of process for these recipes. This decides the required aEU/t,
     *                 progress time, required additional Mutagen, requirement of cleanRoom, needed fluid stack for
     *                 Chemical.
     * @param material result of Material that should be generated by this process.
     **/
    private static void addProcessGT(GTCombType comb, Material[] material, Voltage volt) {
        for (int i = 0; i < material.length; i++) {
            addChemicalProcess(comb, material[i], material[i], volt);
            addAutoclaveProcess(comb, material[i], volt, i + 1);
        }
    }

    /**
     * this method only adds Centrifuge based on Material. If volt is lower than MV than it will also add forestry
     * centrifuge recipe.
     *
     * @param comb      BeeComb
     * @param material  resulting Material of processing. must be less than or equal to 9.
     * @param chance    chance to get result, 10000 == 100%
     * @param volt      required Voltage Tier for this recipe, this also affect the duration, amount of Mutagen, and
     *                  needed liquid type and amount for chemical reactor
     * @param stackSize This parameter can be null, in that case stack size will be just 1. This handle the stackSize of
     *                  the resulting Item, and Also the Type of Item. if this value is multiple of 9, than related
     *                  Material output will be dust, if this value is multiple of 4 than output will be Small dust,
     *                  else the output will be Tiny dust
     * @param beeWax    if this is null, then the comb will product default Bee wax. But if material is more than 5,
     *                  beeWax will be ignored in Gregtech Centrifuge.
     * @param waxChance have same format like "chance"
     **/
    private static void addCentrifugeToMaterial(GTCombType comb, Material[] material, int[] chance, int[] stackSize,
                                                Voltage volt, ItemStack beeWax, int waxChance) {
        addCentrifugeToMaterial(comb, material, chance, stackSize, volt, volt.getCentrifugeTime(), beeWax, waxChance);
    }

    private static void addCentrifugeToMaterial(GTCombType comb, Material[] material, int[] chance, int[] stackSize,
                                                Voltage volt, int duration, ItemStack beeWax, int waxChance) {
        ItemStack[] output = new ItemStack[material.length + 1];
        stackSize = Arrays.copyOf(stackSize, material.length);
        chance = Arrays.copyOf(chance, output.length);
        chance[chance.length - 1] = waxChance;
        for (int i = 0; i < material.length; i++) {
            if (chance[i] == 0) continue;

            if (Math.max(1, stackSize[i]) % 9 == 0) {
                output[i] = OreDictUnifier.get(OrePrefix.dust, material[i], Math.max(1, stackSize[i]) / 9);
            } else if (Math.max(1, stackSize[i]) % 4 == 0) {
                output[i] = OreDictUnifier.get(OrePrefix.dustSmall, material[i], Math.max(1, stackSize[i]) / 4);
            } else {
                output[i] = OreDictUnifier.get(OrePrefix.dustTiny, material[i], Math.max(1, stackSize[i]));
            }
        }
        if (beeWax != ItemStack.EMPTY) {
            output[output.length - 1] = beeWax;
        } else {
            output[output.length - 1] = ModuleCore.getItems().beeswax.getItemStack();
        }

        addCentrifugeToItemStack(comb, output, chance, volt, duration);
    }

    /**
     * @param volt required Tier of system. If it's lower than MV, it will also add forestry centrifuge.
     * @param item must be less than or equal to 9.
     **/
    private static void addCentrifugeToItemStack(GTCombType comb, ItemStack[] item, int[] chance, Voltage volt) {
        addCentrifugeToItemStack(comb, item, chance, volt, volt.getCentrifugeTime());
    }

    private static void addCentrifugeToItemStack(GTCombType comb, ItemStack[] item, int[] chance, Voltage volt,
                                                 int duration) {
        ItemStack combStack = ForestryUtil.getCombStack(comb);

        // Start of the Forestry Map
        ImmutableMap.Builder<ItemStack, Float> product = new ImmutableMap.Builder<>();
        // Start of the GregTech Map
        RecipeBuilder<?> builder = RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                .inputs(combStack)
                .duration(duration)
                .EUt(volt.getCentrifugeEnergy());

        int numGTOutputs = 0;
        for (int i = 0; i < item.length; i++) {
            if (item[i] == null || item[i] == ItemStack.EMPTY) continue;
            // Add to Forestry Map
            product.put(item[i], chance[i] / 10000.0f);
            // Add to GregTech Map
            if (numGTOutputs < RecipeMaps.CENTRIFUGE_RECIPES.getMaxOutputs()) {
                if (chance[i] >= 10000) {
                    builder.outputs(item[i]);
                } else {
                    builder.chancedOutput(item[i], chance[i], 0);
                }
                numGTOutputs++;
            }
        }

        // Finalize Forestry Map
        if (volt.compareTo(Voltage.MV) < 0) {
            if (ModuleFactory.machineEnabled(MachineUIDs.CENTRIFUGE)) {
                RecipeManagers.centrifugeManager.addRecipe(duration, combStack, product.build());
            }
        }
        // Finalize GregTech Map
        builder.buildAndRegister();
    }

    private enum Voltage {

        ULV,
        LV,
        MV,
        HV,
        EV,
        IV,
        LUV,
        ZPM,
        UV,
        UHV,
        UEV,
        UIV,
        UXV,
        OPV,
        MAX;

        public int getVoltage() {
            return (int) GTValues.V[ordinal()];
        }

        public int getChemicalEnergy() {
            return getVoltage() * 3 / 4;
        }

        public int getAutoclaveEnergy() {
            return (int) ((getVoltage() * 3 / 4) * Math.max(1, Math.pow(2, 5 - ordinal())));
        }

        public int getCentrifugeEnergy() {
            return this == Voltage.ULV ? 5 : (getVoltage() / 16) * 15;
        }

        public int getChemicalTime() {
            return 64 + ordinal() * 32;
        }

        public int getCentrifugeTime() {
            return 128 * (Math.max(1, ordinal()));
        }

        public FluidStack getFluid() {
            if (this.compareTo(Voltage.MV) < 0) {
                return Materials.Water.getFluid((this.compareTo(Voltage.ULV) > 0) ? 1000 : 500);
            } else if (this.compareTo(Voltage.HV) < 0) {
                return Materials.DistilledWater.getFluid(1000);
            } else if (this.compareTo(Voltage.EV) < 0) {
                return Materials.SulfuricAcid.getFluid(125);
            } else if (this.compareTo(Voltage.IV) < 0) {
                return Materials.HydrochloricAcid.getFluid(125);
            } else if (this.compareTo(Voltage.LUV) < 0) {
                return Materials.HydrofluoricAcid.getFluid((int) (Math.pow(2, this.compareTo(Voltage.HV)) * 125));
            } else {
                return Materials.FluoroantimonicAcid.getFluid((int) (Math.pow(2, this.compareTo(Voltage.LUV)) * 125));
            }
        }

        public int getMutagenAmount() {
            return 9 * ((this.compareTo(Voltage.MV) < 0) ? 10 : 10 * this.compareTo(Voltage.MV));
        }
    }
}
