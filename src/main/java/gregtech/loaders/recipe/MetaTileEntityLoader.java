package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.items.OreDictNames;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockFissionCasing;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.BlockMultiblockCasing.MultiblockCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.wood.BlockGregPlanks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

import static gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType.*;
import static gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType.*;
import static gregtech.common.blocks.BlockHermeticCasing.HermeticCasingsType.*;
import static gregtech.common.blocks.BlockMachineCasing.MachineCasingType.*;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.*;
import static gregtech.common.blocks.BlockMultiblockCasing.MultiblockCasingType.*;
import static gregtech.common.blocks.BlockSteamCasing.SteamCasingType.*;
import static gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType.*;
import static gregtech.common.blocks.BlockWarningSign.SignType.*;
import static gregtech.common.blocks.BlockWarningSign1.SignType.*;
import static gregtech.common.blocks.BlockWireCoil.CoilType.CUPRONICKEL;
import static gregtech.loaders.recipe.CraftingComponent.*;

public class MetaTileEntityLoader {

    public static void init() {
        ModHandler.addShapedRecipe(true, "casing_ulv", MetaBlocks.MACHINE_CASING.getItemVariant(ULV), "PPP", "PwP",
                "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.WroughtIron));
        ModHandler.addShapedRecipe(true, "casing_lv", MetaBlocks.MACHINE_CASING.getItemVariant(LV), "PPP", "PwP", "PPP",
                'P', new UnificationEntry(OrePrefix.plate, Materials.Steel));
        ModHandler.addShapedRecipe(true, "casing_mv", MetaBlocks.MACHINE_CASING.getItemVariant(MV), "PPP", "PwP", "PPP",
                'P', new UnificationEntry(OrePrefix.plate, Materials.Aluminium));
        ModHandler.addShapedRecipe(true, "casing_hv", MetaBlocks.MACHINE_CASING.getItemVariant(HV), "PPP", "PwP", "PPP",
                'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel));
        ModHandler.addShapedRecipe(true, "casing_ev", MetaBlocks.MACHINE_CASING.getItemVariant(EV), "PPP", "PwP", "PPP",
                'P', new UnificationEntry(OrePrefix.plate, Materials.Titanium));
        ModHandler.addShapedRecipe(true, "casing_iv", MetaBlocks.MACHINE_CASING.getItemVariant(IV), "PPP", "PwP", "PPP",
                'P', new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel));
        ModHandler.addShapedRecipe(true, "casing_luv", MetaBlocks.MACHINE_CASING.getItemVariant(LuV), "PPP", "PwP",
                "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.RhodiumPlatedPalladium));
        ModHandler.addShapedRecipe(true, "casing_zpm", MetaBlocks.MACHINE_CASING.getItemVariant(ZPM), "PPP", "PwP",
                "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.NaquadahAlloy));
        ModHandler.addShapedRecipe(true, "casing_uv", MetaBlocks.MACHINE_CASING.getItemVariant(UV), "PPP", "PwP", "PPP",
                'P', new UnificationEntry(OrePrefix.plate, Materials.Darmstadtium));
        ModHandler.addShapedRecipe(true, "casing_uhv", MetaBlocks.MACHINE_CASING.getItemVariant(UHV), "PPP", "PwP",
                "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Neutronium));

        // If these recipes are changed, change the values in MaterialInfoLoader.java
        registerMachineRecipe(false, MetaTileEntities.HULL, "PLP", "CHC", 'P', HULL_PLATE, 'L', PLATE, 'C', CABLE, 'H',
                CASING);

        if (ConfigHolder.recipes.harderBrickRecipes) {
            ModHandler.addFluidReplaceRecipe("bucket_of_concrete",
                    FluidUtil.getFilledBucket(Materials.Concrete.getFluid(1000)),
                    "C S", "CWQ", " L ",
                    'C', new UnificationEntry(OrePrefix.dust, Materials.Calcite),
                    'S', new UnificationEntry(OrePrefix.dust, Materials.Stone),
                    'W', new ItemStack(Items.WATER_BUCKET),
                    'Q', new UnificationEntry(OrePrefix.dust, Materials.QuartzSand),
                    'L', new UnificationEntry(OrePrefix.dust, Materials.Clay));

            ModHandler.addShapedRecipe("casing_primitive_bricks",
                    MetaBlocks.METAL_CASING.getItemVariant(PRIMITIVE_BRICKS),
                    "BGB", "BCB", "BGB",
                    'B', MetaItems.FIRECLAY_BRICK.getStackForm(),
                    'G', new UnificationEntry(OrePrefix.dust, Materials.Gypsum),
                    'C', FluidUtil.getFilledBucket(Materials.Concrete.getFluid(1000)));
        } else {
            ModHandler.addShapedRecipe("casing_primitive_bricks",
                    MetaBlocks.METAL_CASING.getItemVariant(PRIMITIVE_BRICKS, 1), "XX", "XX", 'X',
                    MetaItems.FIRECLAY_BRICK);
        }

        ModHandler.addShapedRecipe(true, "casing_coke_bricks", MetaBlocks.METAL_CASING.getItemVariant(COKE_BRICKS, 1),
                "XX", "XX", 'X', MetaItems.COKE_OVEN_BRICK);
        ModHandler.addShapedRecipe(true, "casing_bronze_bricks",
                MetaBlocks.METAL_CASING.getItemVariant(BRONZE_BRICKS, ConfigHolder.recipes.casingsPerCraft), "PhP",
                "PBP", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Bronze), 'B',
                new ItemStack(Blocks.BRICK_BLOCK, 1));
        ModHandler.addShapedRecipe(true, "casing_steel_solid",
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID, ConfigHolder.recipes.casingsPerCraft), "PhP", "PFP",
                "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Steel), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Steel));
        ModHandler.addShapedRecipe(true, "casing_titanium_stable",
                MetaBlocks.METAL_CASING.getItemVariant(TITANIUM_STABLE, ConfigHolder.recipes.casingsPerCraft), "PhP",
                "PFP", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Titanium), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Titanium));
        ModHandler.addShapedRecipe(true, "casing_invar_heatproof",
                MetaBlocks.METAL_CASING.getItemVariant(INVAR_HEATPROOF, ConfigHolder.recipes.casingsPerCraft), "PhP",
                "PFP", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Invar), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Invar));
        ModHandler.addShapedRecipe(true, "casing_aluminium_frostproof",
                MetaBlocks.METAL_CASING.getItemVariant(ALUMINIUM_FROSTPROOF, ConfigHolder.recipes.casingsPerCraft),
                "PhP", "PFP", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Aluminium), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Aluminium));
        ModHandler.addShapedRecipe(true, "casing_stainless_clean",
                MetaBlocks.METAL_CASING.getItemVariant(STAINLESS_CLEAN, ConfigHolder.recipes.casingsPerCraft), "PhP",
                "PFP", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.StainlessSteel));
        ModHandler.addShapedRecipe(true, "casing_tungstensteel_robust",
                MetaBlocks.METAL_CASING.getItemVariant(TUNGSTENSTEEL_ROBUST, ConfigHolder.recipes.casingsPerCraft),
                "PhP", "PFP", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.TungstenSteel));
        ModHandler.addShapedRecipe(true, "casing_hssg_robust",
                MetaBlocks.METAL_CASING.getItemVariant(HSSE_STURDY, ConfigHolder.recipes.casingsPerCraft), "PhP", "PFP",
                "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.HSSE), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Europium));
        ModHandler.addShapedRecipe(true, "casing_palladium_substation",
                MetaBlocks.METAL_CASING.getItemVariant(PALLADIUM_SUBSTATION, ConfigHolder.recipes.casingsPerCraft),
                "PhP", "PFP", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Palladium), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Iridium));

        ModHandler.addShapedRecipe(true, "casing_steel_turbine_casing",
                MetaBlocks.TURBINE_CASING.getItemVariant(STEEL_TURBINE_CASING, ConfigHolder.recipes.casingsPerCraft),
                "PhP", "PFP", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Magnalium), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.BlueSteel));
        ModHandler.addShapedRecipe(true, "casing_stainless_turbine_casing",
                MetaBlocks.TURBINE_CASING.getItemVariant(STAINLESS_TURBINE_CASING,
                        ConfigHolder.recipes.casingsPerCraft),
                "PhP", "PFP", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel), 'F',
                MetaBlocks.TURBINE_CASING.getItemVariant(STEEL_TURBINE_CASING));
        ModHandler.addShapedRecipe(true, "casing_titanium_turbine_casing",
                MetaBlocks.TURBINE_CASING.getItemVariant(TITANIUM_TURBINE_CASING, ConfigHolder.recipes.casingsPerCraft),
                "PhP", "PFP", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Titanium), 'F',
                MetaBlocks.TURBINE_CASING.getItemVariant(STEEL_TURBINE_CASING));
        ModHandler.addShapedRecipe(true, "casing_tungstensteel_turbine_casing",
                MetaBlocks.TURBINE_CASING.getItemVariant(TUNGSTENSTEEL_TURBINE_CASING,
                        ConfigHolder.recipes.casingsPerCraft),
                "PhP", "PFP", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel), 'F',
                MetaBlocks.TURBINE_CASING.getItemVariant(STEEL_TURBINE_CASING));

        ModHandler.addShapedRecipe(true, "casing_bronze_pipe",
                MetaBlocks.BOILER_CASING.getItemVariant(BRONZE_PIPE, ConfigHolder.recipes.casingsPerCraft), "PIP",
                "IFI", "PIP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Bronze), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Bronze), 'I',
                new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Bronze));
        ModHandler.addShapedRecipe(true, "casing_steel_pipe",
                MetaBlocks.BOILER_CASING.getItemVariant(STEEL_PIPE, ConfigHolder.recipes.casingsPerCraft), "PIP", "IFI",
                "PIP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Steel), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Steel), 'I',
                new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Steel));
        ModHandler.addShapedRecipe(true, "casing_titanium_pipe",
                MetaBlocks.BOILER_CASING.getItemVariant(TITANIUM_PIPE, ConfigHolder.recipes.casingsPerCraft), "PIP",
                "IFI", "PIP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Titanium), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Titanium), 'I',
                new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Titanium));
        ModHandler.addShapedRecipe(true, "casing_tungstensteel_pipe",
                MetaBlocks.BOILER_CASING.getItemVariant(TUNGSTENSTEEL_PIPE, ConfigHolder.recipes.casingsPerCraft),
                "PIP", "IFI", "PIP", 'P', new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.TungstenSteel), 'I',
                new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.TungstenSteel));
        ModHandler.addShapedRecipe(true, "casing_ptfe_pipe",
                MetaBlocks.BOILER_CASING.getItemVariant(POLYTETRAFLUOROETHYLENE_PIPE,
                        ConfigHolder.recipes.casingsPerCraft),
                "PIP", "IFI", "PIP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Polytetrafluoroethylene), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Polytetrafluoroethylene), 'I',
                new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Polytetrafluoroethylene));
        ModHandler.addShapedRecipe(true, "casing_bronze_firebox",
                MetaBlocks.BOILER_FIREBOX_CASING.getItemVariant(BRONZE_FIREBOX, ConfigHolder.recipes.casingsPerCraft),
                "PSP", "SFS", "PSP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Bronze), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Bronze), 'S',
                new UnificationEntry(OrePrefix.stick, Materials.Bronze));
        ModHandler.addShapedRecipe(true, "casing_steel_firebox",
                MetaBlocks.BOILER_FIREBOX_CASING.getItemVariant(STEEL_FIREBOX, ConfigHolder.recipes.casingsPerCraft),
                "PSP", "SFS", "PSP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Steel), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Steel), 'S',
                new UnificationEntry(OrePrefix.stick, Materials.Steel));
        ModHandler.addShapedRecipe(true, "casing_titanium_firebox",
                MetaBlocks.BOILER_FIREBOX_CASING.getItemVariant(TITANIUM_FIREBOX, ConfigHolder.recipes.casingsPerCraft),
                "PSP", "SFS", "PSP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Titanium), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Titanium), 'S',
                new UnificationEntry(OrePrefix.stick, Materials.Titanium));
        ModHandler.addShapedRecipe(true, "casing_tungstensteel_firebox",
                MetaBlocks.BOILER_FIREBOX_CASING.getItemVariant(TUNGSTENSTEEL_FIREBOX,
                        ConfigHolder.recipes.casingsPerCraft),
                "PSP", "SFS", "PSP", 'P', new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.TungstenSteel), 'S',
                new UnificationEntry(OrePrefix.stick, Materials.TungstenSteel));

        ModHandler.addShapedRecipe(true, "casing_bronze_gearbox",
                MetaBlocks.TURBINE_CASING.getItemVariant(BRONZE_GEARBOX, ConfigHolder.recipes.casingsPerCraft), "PhP",
                "GFG", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Bronze), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Bronze), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.Bronze));
        ModHandler.addShapedRecipe(true, "casing_steel_gearbox",
                MetaBlocks.TURBINE_CASING.getItemVariant(STEEL_GEARBOX, ConfigHolder.recipes.casingsPerCraft), "PhP",
                "GFG", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Steel), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Steel), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.Steel));
        ModHandler.addShapedRecipe(true, "casing_stainless_steel_gearbox",
                MetaBlocks.TURBINE_CASING.getItemVariant(STAINLESS_STEEL_GEARBOX, ConfigHolder.recipes.casingsPerCraft),
                "PhP", "GFG", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.StainlessSteel), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.StainlessSteel));
        ModHandler.addShapedRecipe(true, "casing_titanium_gearbox",
                MetaBlocks.TURBINE_CASING.getItemVariant(TITANIUM_GEARBOX, ConfigHolder.recipes.casingsPerCraft), "PhP",
                "GFG", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Titanium), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Titanium), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.Titanium));
        ModHandler.addShapedRecipe(true, "casing_tungstensteel_gearbox",
                MetaBlocks.TURBINE_CASING.getItemVariant(TUNGSTENSTEEL_GEARBOX, ConfigHolder.recipes.casingsPerCraft),
                "PhP", "GFG", "PwP", 'P', new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.TungstenSteel), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.TungstenSteel));

        ModHandler.addShapedRecipe(true, "casing_grate_casing",
                MetaBlocks.MULTIBLOCK_CASING.getItemVariant(GRATE_CASING, ConfigHolder.recipes.casingsPerCraft), "PVP",
                "PFP", "PMP", 'P', new ItemStack(Blocks.IRON_BARS, 1), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.Steel), 'M', MetaItems.ELECTRIC_MOTOR_MV, 'V',
                new UnificationEntry(OrePrefix.rotor, Materials.Steel));
        ModHandler.addShapedRecipe(true, "casing_assembly_control",
                MetaBlocks.MULTIBLOCK_CASING.getItemVariant(ASSEMBLY_CONTROL, ConfigHolder.recipes.casingsPerCraft),
                "CPC", "SFE", "CMC", 'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LuV), 'P',
                MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT, 'S', MetaItems.SENSOR_IV.getStackForm(), 'F',
                new UnificationEntry(OrePrefix.frameGt, Materials.TungstenSteel), 'E',
                MetaItems.EMITTER_IV.getStackForm(), 'M', MetaItems.ELECTRIC_MOTOR_IV);
        ModHandler.addShapedRecipe(true, "casing_assembly_line",
                MetaBlocks.MULTIBLOCK_CASING.getItemVariant(ASSEMBLY_LINE_CASING, ConfigHolder.recipes.casingsPerCraft),
                "PGP", "AFA", "PGP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Steel), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.Ruridit), 'A', MetaItems.ROBOT_ARM_IV.getStackForm(),
                'F', OreDictUnifier.get(OrePrefix.frameGt, Materials.TungstenSteel));

        ModHandler.addShapedRecipe(true, "warning_sign_yellow_stripes",
                MetaBlocks.WARNING_SIGN.getItemVariant(YELLOW_STRIPES), "Y  ", " M ", "  B", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_small_yellow_stripes",
                MetaBlocks.WARNING_SIGN.getItemVariant(SMALL_YELLOW_STRIPES), "  Y", " M ", "B  ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_radioactive_hazard",
                MetaBlocks.WARNING_SIGN.getItemVariant(RADIOACTIVE_HAZARD), " YB", " M ", "   ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_bio_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(BIO_HAZARD),
                " Y ", " MB", "   ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B',
                "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_explosion_hazard",
                MetaBlocks.WARNING_SIGN.getItemVariant(EXPLOSION_HAZARD), " Y ", " M ", "  B", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_fire_hazard",
                MetaBlocks.WARNING_SIGN.getItemVariant(FIRE_HAZARD), " Y ", " M ", " B ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_acid_hazard",
                MetaBlocks.WARNING_SIGN.getItemVariant(ACID_HAZARD), " Y ", " M ", "B  ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_magic_hazard",
                MetaBlocks.WARNING_SIGN.getItemVariant(MAGIC_HAZARD), " Y ", "BM ", "   ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_frost_hazard",
                MetaBlocks.WARNING_SIGN.getItemVariant(FROST_HAZARD), "BY ", " M ", "   ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_noise_hazard",
                MetaBlocks.WARNING_SIGN.getItemVariant(NOISE_HAZARD), "   ", " M ", "BY ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_generic_hazard",
                MetaBlocks.WARNING_SIGN.getItemVariant(GENERIC_HAZARD), "   ", "BM ", " Y ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_high_voltage_hazard",
                MetaBlocks.WARNING_SIGN.getItemVariant(HIGH_VOLTAGE_HAZARD), "B  ", " M ", " Y ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_magnetic_hazard",
                MetaBlocks.WARNING_SIGN.getItemVariant(MAGNETIC_HAZARD), " B ", " M ", " Y ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_antimatter_hazard",
                MetaBlocks.WARNING_SIGN.getItemVariant(ANTIMATTER_HAZARD), "  B", " M ", " Y ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_high_temperature_hazard",
                MetaBlocks.WARNING_SIGN.getItemVariant(HIGH_TEMPERATURE_HAZARD), "   ", " MB", " Y ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_sign_void_hazard",
                MetaBlocks.WARNING_SIGN.getItemVariant(VOID_HAZARD), "   ", " M ", " YB", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");

        ModHandler.addShapedRecipe(true, "warning_mob_spawner_hazard",
                MetaBlocks.WARNING_SIGN_1.getItemVariant(MOB_SPAWNER_HAZARD), "B  ", "YM ", "   ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_spatial_storage_hazard",
                MetaBlocks.WARNING_SIGN_1.getItemVariant(SPATIAL_STORAGE_HAZARD), " B ", "YM ", "   ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_laser_hazard", MetaBlocks.WARNING_SIGN_1.getItemVariant(LASER_HAZARD),
                "  B", "YM ", "   ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B',
                "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_mob_hazard", MetaBlocks.WARNING_SIGN_1.getItemVariant(MOB_HAZARD),
                "   ", "YMB", "   ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B',
                "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_boss_hazard", MetaBlocks.WARNING_SIGN_1.getItemVariant(BOSS_HAZARD),
                "   ", "YM ", "  B", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B',
                "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_gregification_hazard",
                MetaBlocks.WARNING_SIGN_1.getItemVariant(GREGIFICATION_HAZARD), "   ", "YM ", " B ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_causality_hazard",
                MetaBlocks.WARNING_SIGN_1.getItemVariant(CAUSALITY_HAZARD), "   ", "YM ", "B  ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_automated_defenses_hazard",
                MetaBlocks.WARNING_SIGN_1.getItemVariant(AUTOMATED_DEFENSES_HAZARD), "   ", " MY", "  B", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        ModHandler.addShapedRecipe(true, "warning_high_pressure_hazard",
                MetaBlocks.WARNING_SIGN_1.getItemVariant(HIGH_PRESSURE_HAZARD), "   ", " MY", " B ", 'M',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");

        // TODO?
        ModHandler.addShapelessRecipe("yellow_stripes_to_steel_solid_casing",
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID),
                MetaBlocks.WARNING_SIGN.getItemVariant(YELLOW_STRIPES));
        ModHandler.addShapelessRecipe("small_yellow_stripes_to_steel_solid_casing",
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID),
                MetaBlocks.WARNING_SIGN.getItemVariant(SMALL_YELLOW_STRIPES));
        ModHandler.addShapelessRecipe("radioactive_hazard_to_steel_solid_casing",
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID),
                MetaBlocks.WARNING_SIGN.getItemVariant(RADIOACTIVE_HAZARD));
        ModHandler.addShapelessRecipe("bio_hazard_to_steel_solid_casing",
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID),
                MetaBlocks.WARNING_SIGN.getItemVariant(BIO_HAZARD));
        ModHandler.addShapelessRecipe("explosion_hazard_to_steel_solid_casing",
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID),
                MetaBlocks.WARNING_SIGN.getItemVariant(EXPLOSION_HAZARD));
        ModHandler.addShapelessRecipe("fire_hazard_to_steel_solid_casing",
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID),
                MetaBlocks.WARNING_SIGN.getItemVariant(FIRE_HAZARD));
        ModHandler.addShapelessRecipe("acid_hazard_to_steel_solid_casing",
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID),
                MetaBlocks.WARNING_SIGN.getItemVariant(ACID_HAZARD));
        ModHandler.addShapelessRecipe("magic_hazard_to_steel_solid_casing",
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID),
                MetaBlocks.WARNING_SIGN.getItemVariant(MAGIC_HAZARD));
        ModHandler.addShapelessRecipe("frost_hazard_to_steel_solid_casing",
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID),
                MetaBlocks.WARNING_SIGN.getItemVariant(FROST_HAZARD));
        ModHandler.addShapelessRecipe("noise_hazard_to_steel_solid_casing",
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID),
                MetaBlocks.WARNING_SIGN.getItemVariant(NOISE_HAZARD));

        ModHandler.addShapedRecipe(true, "rotor_holder_hv", MetaTileEntities.ROTOR_HOLDER[0].getStackForm(), "SGS",
                "GHG", "SGS", 'H', MetaTileEntities.HULL[GTValues.HV].getStackForm(), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.BlackSteel), 'S',
                new UnificationEntry(OrePrefix.gearSmall, Materials.StainlessSteel));
        ModHandler.addShapedRecipe(true, "rotor_holder_ev", MetaTileEntities.ROTOR_HOLDER[1].getStackForm(), "SGS",
                "GHG", "SGS", 'H', MetaTileEntities.HULL[GTValues.EV].getStackForm(), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.Ultimet), 'S',
                new UnificationEntry(OrePrefix.gearSmall, Materials.Titanium));
        ModHandler.addShapedRecipe(true, "rotor_holder_iv", MetaTileEntities.ROTOR_HOLDER[2].getStackForm(), "SGS",
                "GHG", "SGS", 'H', MetaTileEntities.HULL[GTValues.IV].getStackForm(), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.HSSG), 'S',
                new UnificationEntry(OrePrefix.gearSmall, Materials.TungstenSteel));
        ModHandler.addShapedRecipe(true, "rotor_holder_luv", MetaTileEntities.ROTOR_HOLDER[3].getStackForm(), "SGS",
                "GHG", "SGS", 'H', MetaTileEntities.HULL[GTValues.LuV].getStackForm(), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.Ruthenium), 'S',
                new UnificationEntry(OrePrefix.gearSmall, Materials.RhodiumPlatedPalladium));
        ModHandler.addShapedRecipe(true, "rotor_holder_zpm", MetaTileEntities.ROTOR_HOLDER[4].getStackForm(), "SGS",
                "GHG", "SGS", 'H', MetaTileEntities.HULL[GTValues.ZPM].getStackForm(), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.Trinium), 'S',
                new UnificationEntry(OrePrefix.gearSmall, Materials.NaquadahAlloy));
        ModHandler.addShapedRecipe(true, "rotor_holder_uv", MetaTileEntities.ROTOR_HOLDER[5].getStackForm(), "SGS",
                "GHG", "SGS", 'H', MetaTileEntities.HULL[GTValues.UV].getStackForm(), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.Tritanium), 'S',
                new UnificationEntry(OrePrefix.gearSmall, Materials.Darmstadtium));

        ModHandler.addShapedRecipe(true, "maintenance_hatch", MetaTileEntities.MAINTENANCE_HATCH.getStackForm(), "dwx",
                "hHc", "fsr", 'H', MetaTileEntities.HULL[GTValues.LV].getStackForm());
        ModHandler.addShapedRecipe(true, "maintenance_hatch_configurable",
                MetaTileEntities.CONFIGURABLE_MAINTENANCE_HATCH.getStackForm(), "   ", "CMC", "VHV", 'C',
                CIRCUIT.getIngredient(GTValues.HV), 'M', MetaTileEntities.MAINTENANCE_HATCH.getStackForm(), 'V',
                CONVEYOR.getIngredient(GTValues.HV), 'H', MetaTileEntities.HULL[GTValues.HV].getStackForm());
        ModHandler.addShapedRecipe(true, "maintenance_hatch_automatic",
                MetaTileEntities.AUTO_MAINTENANCE_HATCH.getStackForm(), "CMC", "RHR", "CMC", 'C',
                CIRCUIT.getIngredient(GTValues.HV), 'M', MetaTileEntities.MAINTENANCE_HATCH.getStackForm(), 'R',
                ROBOT_ARM.getIngredient(GTValues.HV), 'H', MetaTileEntities.HULL[GTValues.HV].getStackForm());
        ModHandler.addShapedRecipe(true, "maintenance_hatch_cleaning",
                MetaTileEntities.CLEANING_MAINTENANCE_HATCH.getStackForm(), "CMC", "RHR", "WCW", 'C',
                CIRCUIT.getIngredient(GTValues.UV), 'M', MetaTileEntities.AUTO_MAINTENANCE_HATCH.getStackForm(), 'R',
                ROBOT_ARM.getIngredient(GTValues.UV), 'H', MetaTileEntities.HULL[GTValues.UV].getStackForm(), 'W',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate));

        ModHandler.addShapedRecipe(true, "machine_access_interface", MetaTileEntities.MACHINE_HATCH.getStackForm(),
                "CHS", 'C', new UnificationEntry(OrePrefix.circuit, Tier.IV), 'H',
                MetaTileEntities.HULL[GTValues.IV].getStackForm(), 'S', MetaItems.SENSOR_IV.getStackForm());

        ModHandler.addShapedRecipe(true, "passthrough_hatch_item",
                MetaTileEntities.PASSTHROUGH_HATCH_ITEM.getStackForm(), " C ", "GHG", " S ", 'C',
                MetaItems.CONVEYOR_MODULE_HV.getStackForm(), 'G',
                new UnificationEntry(OrePrefix.gearSmall, Materials.Steel), 'H',
                MetaTileEntities.HULL[GTValues.HV].getStackForm(), 'S', OreDictNames.chestWood);
        ModHandler.addShapedRecipe(true, "passthrough_hatch_fluid",
                MetaTileEntities.PASSTHROUGH_HATCH_FLUID.getStackForm(), " C ", "GHG", " S ", 'C',
                MetaItems.ELECTRIC_PUMP_HV.getStackForm(), 'G',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.Steel), 'H',
                MetaTileEntities.HULL[GTValues.HV].getStackForm(), 'S',
                MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.TEMPERED_GLASS));

        ModHandler.addShapedRecipe(true, "charcoal_pile_igniter", MetaTileEntities.CHARCOAL_PILE_IGNITER.getStackForm(),
                "ERE", "EHE", "FFF", 'E', new UnificationEntry(OrePrefix.plate, Materials.Bronze), 'R',
                new UnificationEntry(OrePrefix.rotor, Materials.Iron), 'H',
                MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_BRICKS_HULL), 'F', new ItemStack(Items.FLINT));

        // STEAM MACHINES
        ModHandler.addShapedRecipe(true, "bronze_hull", MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_HULL), "PPP",
                "PhP", "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Bronze));
        ModHandler.addShapedRecipe(true, "bronze_bricks_hull",
                MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_BRICKS_HULL), "PPP", "PhP", "BBB", 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Bronze), 'B', new ItemStack(Blocks.BRICK_BLOCK));
        ModHandler.addShapedRecipe(true, "steel_hull", MetaBlocks.STEAM_CASING.getItemVariant(STEEL_HULL), "PPP", "PhP",
                "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Steel));
        ModHandler.addShapedRecipe(true, "steel_bricks_hull", MetaBlocks.STEAM_CASING.getItemVariant(STEEL_BRICKS_HULL),
                "PPP", "PhP", "BBB", 'P', new UnificationEntry(OrePrefix.plate, Materials.WroughtIron), 'B',
                new ItemStack(Blocks.BRICK_BLOCK));

        ModHandler.addShapedRecipe(true, "steam_boiler_coal_bronze",
                MetaTileEntities.STEAM_BOILER_COAL_BRONZE.getStackForm(), "PPP", "PwP", "BFB", 'F',
                OreDictNames.craftingFurnace, 'P', new UnificationEntry(OrePrefix.plate, Materials.Bronze), 'B',
                new ItemStack(Blocks.BRICK_BLOCK));
        ModHandler.addShapedRecipe(true, "steam_boiler_coal_steel",
                MetaTileEntities.STEAM_BOILER_COAL_STEEL.getStackForm(), "PPP", "PwP", "BFB", 'F',
                OreDictNames.craftingFurnace, 'P', new UnificationEntry(OrePrefix.plate, Materials.Steel), 'B',
                new ItemStack(Blocks.BRICK_BLOCK));
        ModHandler.addShapedRecipe(true, "steam_boiler_lava_bronze",
                MetaTileEntities.STEAM_BOILER_LAVA_BRONZE.getStackForm(), "PPP", "PGP", "PMP", 'M',
                MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_BRICKS_HULL), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Bronze), 'G', new ItemStack(Blocks.GLASS, 1));
        ModHandler.addShapedRecipe(true, "steam_boiler_lava_steel",
                MetaTileEntities.STEAM_BOILER_LAVA_STEEL.getStackForm(), "PPP", "PGP", "PMP", 'M',
                MetaBlocks.STEAM_CASING.getItemVariant(STEEL_BRICKS_HULL), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Steel), 'G', new ItemStack(Blocks.GLASS, 1));
        ModHandler.addShapedRecipe(true, "steam_boiler_solar_bronze",
                MetaTileEntities.STEAM_BOILER_SOLAR_BRONZE.getStackForm(), "GGG", "SSS", "PMP", 'M',
                MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_BRICKS_HULL), 'P',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.Bronze), 'S',
                new UnificationEntry(OrePrefix.plate, Materials.Silver), 'G', new ItemStack(Blocks.GLASS));
        ModHandler.addShapedRecipe(true, "steam_boiler_solar_steel",
                MetaTileEntities.STEAM_BOILER_SOLAR_STEEL.getStackForm(), "GGG", "SSS", "PMP", 'M',
                MetaBlocks.STEAM_CASING.getItemVariant(STEEL_BRICKS_HULL), 'P',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.Steel), 'S',
                new UnificationEntry(OrePrefix.plateDouble, Materials.Silver), 'G', new ItemStack(Blocks.GLASS));

        ModHandler.addShapedRecipe(true, "steam_furnace_bronze", MetaTileEntities.STEAM_FURNACE_BRONZE.getStackForm(),
                "XXX", "XMX", "XFX", 'M', MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_BRICKS_HULL), 'X',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.Bronze), 'F', OreDictNames.craftingFurnace);
        ModHandler.addShapedRecipe(true, "steam_furnace_steel", MetaTileEntities.STEAM_FURNACE_STEEL.getStackForm(),
                "XSX", "PMP", "XXX", 'M', MetaTileEntities.STEAM_FURNACE_BRONZE.getStackForm(), 'X',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.TinAlloy), 'S',
                new UnificationEntry(OrePrefix.plate, Materials.Steel), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.WroughtIron));
        ModHandler.addShapedRecipe(true, "steam_macerator_bronze",
                MetaTileEntities.STEAM_MACERATOR_BRONZE.getStackForm(), "DXD", "XMX", "PXP", 'M',
                MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_HULL), 'X',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.Bronze), 'P', OreDictNames.craftingPiston, 'D',
                new UnificationEntry(OrePrefix.gem, Materials.Diamond));
        ModHandler.addShapedRecipe(true, "steam_macerator_steel", MetaTileEntities.STEAM_MACERATOR_STEEL.getStackForm(),
                "WSW", "PMP", "WWW", 'M', MetaTileEntities.STEAM_MACERATOR_BRONZE.getStackForm(), 'W',
                new UnificationEntry(OrePrefix.plate, Materials.WroughtIron), 'S',
                new UnificationEntry(OrePrefix.plate, Materials.Steel), 'P',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.TinAlloy));
        ModHandler.addShapedRecipe(true, "steam_extractor_bronze",
                MetaTileEntities.STEAM_EXTRACTOR_BRONZE.getStackForm(), "XXX", "PMG", "XXX", 'M',
                MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_HULL), 'X',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.Bronze), 'P', OreDictNames.craftingPiston, 'G',
                new ItemStack(Blocks.GLASS));
        ModHandler.addShapedRecipe(true, "steam_extractor_steel", MetaTileEntities.STEAM_EXTRACTOR_STEEL.getStackForm(),
                "PSP", "WMW", "PPP", 'M', MetaTileEntities.STEAM_EXTRACTOR_BRONZE.getStackForm(), 'P',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.TinAlloy), 'S',
                new UnificationEntry(OrePrefix.plate, Materials.Steel), 'W',
                new UnificationEntry(OrePrefix.plate, Materials.WroughtIron));
        ModHandler.addShapedRecipe(true, "steam_hammer_bronze", MetaTileEntities.STEAM_HAMMER_BRONZE.getStackForm(),
                "XPX", "XMX", "XAX", 'M', MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_HULL), 'X',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.Bronze), 'P', OreDictNames.craftingPiston, 'A',
                OreDictNames.craftingAnvil);
        ModHandler.addShapedRecipe(true, "steam_hammer_steel", MetaTileEntities.STEAM_HAMMER_STEEL.getStackForm(),
                "WSW", "PMP", "WWW", 'M', MetaTileEntities.STEAM_HAMMER_BRONZE.getStackForm(), 'S',
                new UnificationEntry(OrePrefix.plate, Materials.Steel), 'W',
                new UnificationEntry(OrePrefix.plate, Materials.WroughtIron), 'P',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.TinAlloy));
        ModHandler.addShapedRecipe(true, "steam_compressor_bronze",
                MetaTileEntities.STEAM_COMPRESSOR_BRONZE.getStackForm(), "XXX", "PMP", "XXX", 'M',
                MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_HULL), 'X',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.Bronze), 'P', OreDictNames.craftingPiston);
        ModHandler.addShapedRecipe(true, "steam_compressor_steel",
                MetaTileEntities.STEAM_COMPRESSOR_STEEL.getStackForm(), "PSP", "WMW", "PPP", 'M',
                MetaTileEntities.STEAM_COMPRESSOR_BRONZE.getStackForm(), 'S',
                new UnificationEntry(OrePrefix.plate, Materials.Steel), 'W',
                new UnificationEntry(OrePrefix.plate, Materials.WroughtIron), 'P',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.TinAlloy));
        ModHandler.addShapedRecipe(true, "steam_alloy_smelter_bronze",
                MetaTileEntities.STEAM_ALLOY_SMELTER_BRONZE.getStackForm(), "XXX", "FMF", "XXX", 'M',
                MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_BRICKS_HULL), 'X',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.Bronze), 'F', OreDictNames.craftingFurnace);
        ModHandler.addShapedRecipe(true, "steam_alloy_smelter_steel",
                MetaTileEntities.STEAM_ALLOY_SMELTER_STEEL.getStackForm(), "WSW", "WMW", "WPW", 'M',
                MetaTileEntities.STEAM_ALLOY_SMELTER_BRONZE.getStackForm(), 'S',
                new UnificationEntry(OrePrefix.plate, Materials.Steel), 'W',
                new UnificationEntry(OrePrefix.plate, Materials.WroughtIron), 'P',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.TinAlloy));
        ModHandler.addShapedRecipe(true, "steam_rock_breaker_bronze",
                MetaTileEntities.STEAM_ROCK_BREAKER_BRONZE.getStackForm(), "PXP", "XMX", "DXD", 'M',
                MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_HULL), 'X',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.Bronze), 'P', "craftingPiston", 'D',
                new UnificationEntry(OrePrefix.gem, Materials.Diamond));
        ModHandler.addShapedRecipe(true, "steam_rock_breaker_steel",
                MetaTileEntities.STEAM_ROCK_BREAKER_STEEL.getStackForm(), "WSW", "PMP", "WWW", 'M',
                MetaTileEntities.STEAM_ROCK_BREAKER_BRONZE.getStackForm(), 'W',
                new UnificationEntry(OrePrefix.plate, Materials.WroughtIron), 'S',
                new UnificationEntry(OrePrefix.plate, Materials.Steel), 'P',
                new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.TinAlloy));
        ModHandler.addShapedRecipe(true, "steam_miner", MetaTileEntities.STEAM_MINER.getStackForm(), "DSD", "SMS",
                "GSG", 'M', MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_HULL), 'S',
                new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Bronze), 'D',
                new UnificationEntry(OrePrefix.gem, Materials.Diamond), 'G',
                new UnificationEntry(OrePrefix.gearSmall, Materials.Bronze));

        // MULTI BLOCK CONTROLLERS
        ModHandler.addShapedRecipe(true, "bronze_primitive_blast_furnace",
                MetaTileEntities.PRIMITIVE_BLAST_FURNACE.getStackForm(), "hRS", "PBR", "dRS", 'R',
                new UnificationEntry(OrePrefix.stick, Materials.Iron), 'S',
                new UnificationEntry(OrePrefix.screw, Materials.Iron), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Iron), 'B',
                MetaBlocks.METAL_CASING.getItemVariant(PRIMITIVE_BRICKS));
        ModHandler.addShapedRecipe(true, "coke_oven", MetaTileEntities.COKE_OVEN.getStackForm(), "PIP", "IwI", "PIP",
                'P', MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.COKE_BRICKS), 'I',
                new UnificationEntry(OrePrefix.plate, Materials.Iron));
        ModHandler.addShapedRecipe(true, "coke_oven_hatch", MetaTileEntities.COKE_OVEN_HATCH.getStackForm(), "CD", 'C',
                MetaBlocks.METAL_CASING.getItemVariant(MetalCasingType.COKE_BRICKS), 'D',
                MetaTileEntities.WOODEN_DRUM.getStackForm());
        ModHandler.addShapedRecipe(true, "vacuum_freezer", MetaTileEntities.VACUUM_FREEZER.getStackForm(), "PPP", "CMC",
                "WCW", 'M', MetaBlocks.METAL_CASING.getItemVariant(ALUMINIUM_FROSTPROOF), 'P',
                MetaItems.ELECTRIC_PUMP_HV, 'C', new UnificationEntry(OrePrefix.circuit, Tier.EV), 'W',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold));
        ModHandler.addShapedRecipe(true, "implosion_compressor", MetaTileEntities.IMPLOSION_COMPRESSOR.getStackForm(),
                "OOO", "CMC", "WCW", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'O',
                new UnificationEntry(OrePrefix.block, Materials.Obsidian), 'C',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.HV), 'W',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold));
        ModHandler.addShapedRecipe(true, "distillation_tower", MetaTileEntities.DISTILLATION_TOWER.getStackForm(),
                "CBC", "FMF", "CBC", 'M', MetaTileEntities.HULL[GTValues.HV].getStackForm(), 'B',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.StainlessSteel), 'C',
                new UnificationEntry(OrePrefix.circuit, Tier.EV), 'F', MetaItems.ELECTRIC_PUMP_HV);
        ModHandler.addShapedRecipe(true, "cracking_unit", MetaTileEntities.CRACKER.getStackForm(), "CEC", "PHP", "CEC",
                'C', MetaBlocks.WIRE_COIL.getItemVariant(CUPRONICKEL), 'E', MetaItems.ELECTRIC_PUMP_HV, 'P',
                new UnificationEntry(OrePrefix.circuit, Tier.HV), 'H',
                MetaTileEntities.HULL[GTValues.HV].getStackForm());

        ModHandler.addShapedRecipe(true, "pyrolyse_oven", MetaTileEntities.PYROLYSE_OVEN.getStackForm(), "WEP", "EME",
                "WCP", 'M', MetaTileEntities.HULL[GTValues.MV].getStackForm(), 'W', MetaItems.ELECTRIC_PISTON_MV, 'P',
                new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.Cupronickel), 'E',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.MV), 'C', MetaItems.ELECTRIC_PUMP_MV);
        ModHandler.addShapedRecipe(true, "large_combustion_engine",
                MetaTileEntities.LARGE_COMBUSTION_ENGINE.getStackForm(), "PCP", "EME", "GWG", 'M',
                MetaTileEntities.HULL[GTValues.EV].getStackForm(), 'P', MetaItems.ELECTRIC_PISTON_EV.getStackForm(),
                'E', MetaItems.ELECTRIC_MOTOR_EV.getStackForm(), 'C', new UnificationEntry(OrePrefix.circuit, Tier.IV),
                'W', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Aluminium), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.Titanium));
        ModHandler.addShapedRecipe(true, "extreme_combustion_engine",
                MetaTileEntities.EXTREME_COMBUSTION_ENGINE.getStackForm(), "PCP", "EME", "GWG", 'M',
                MetaTileEntities.HULL[GTValues.IV].getStackForm(), 'P', MetaItems.ELECTRIC_PISTON_IV.getStackForm(),
                'E', MetaItems.ELECTRIC_MOTOR_IV.getStackForm(), 'C', new UnificationEntry(OrePrefix.circuit, Tier.LuV),
                'W', new UnificationEntry(OrePrefix.cableGtSingle, Materials.HSSG), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.TungstenSteel));
        ModHandler.addShapedRecipe(true, "engine_intake_casing",
                MetaBlocks.MULTIBLOCK_CASING.getItemVariant(MultiblockCasingType.ENGINE_INTAKE_CASING,
                        ConfigHolder.recipes.casingsPerCraft),
                "PhP", "RFR", "PwP", 'R', new UnificationEntry(OrePrefix.rotor, Materials.Titanium), 'F',
                MetaBlocks.METAL_CASING.getItemVariant(TITANIUM_STABLE), 'P',
                new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Titanium));
        ModHandler.addShapedRecipe(true, "extreme_engine_intake_casing",
                MetaBlocks.MULTIBLOCK_CASING.getItemVariant(MultiblockCasingType.EXTREME_ENGINE_INTAKE_CASING,
                        ConfigHolder.recipes.casingsPerCraft),
                "PhP", "RFR", "PwP", 'R', new UnificationEntry(OrePrefix.rotor, Materials.TungstenSteel), 'F',
                MetaBlocks.METAL_CASING.getItemVariant(TUNGSTENSTEEL_ROBUST), 'P',
                new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.TungstenSteel));
        ModHandler.addShapedRecipe(true, "multi_furnace", MetaTileEntities.MULTI_FURNACE.getStackForm(), "PPP", "ASA",
                "CAC", 'P', Blocks.FURNACE, 'A', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.HV), 'S',
                MetaBlocks.METAL_CASING.getItemVariant(INVAR_HEATPROOF), 'C',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper));

        ModHandler.addShapedRecipe(true, "large_steam_turbine", MetaTileEntities.LARGE_STEAM_TURBINE.getStackForm(),
                "PSP", "SAS", "CSC", 'S', new UnificationEntry(OrePrefix.gear, Materials.Steel), 'P',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.HV), 'A',
                MetaTileEntities.HULL[GTValues.HV].getStackForm(), 'C',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Steel));
        ModHandler.addShapedRecipe(true, "large_gas_turbine", MetaTileEntities.LARGE_GAS_TURBINE.getStackForm(), "PSP",
                "SAS", "CSC", 'S', new UnificationEntry(OrePrefix.gear, Materials.StainlessSteel), 'P',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.EV), 'A',
                MetaTileEntities.HULL[GTValues.EV].getStackForm(), 'C',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.StainlessSteel));
        ModHandler.addShapedRecipe(true, "large_plasma_turbine", MetaTileEntities.LARGE_PLASMA_TURBINE.getStackForm(),
                "PSP", "SAS", "CSC", 'S', new UnificationEntry(OrePrefix.gear, Materials.TungstenSteel), 'P',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LuV), 'A',
                MetaTileEntities.HULL[GTValues.LuV].getStackForm(), 'C',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.TungstenSteel));

        ModHandler.addShapedRecipe(true, "large_bronze_boiler", MetaTileEntities.LARGE_BRONZE_BOILER.getStackForm(),
                "PSP", "SAS", "PSP", 'P', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin), 'S',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LV), 'A',
                MetaBlocks.BOILER_FIREBOX_CASING.getItemVariant(BRONZE_FIREBOX));
        ModHandler.addShapedRecipe(true, "large_steel_boiler", MetaTileEntities.LARGE_STEEL_BOILER.getStackForm(),
                "PSP", "SAS", "PSP", 'P', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper), 'S',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.HV), 'A',
                MetaBlocks.BOILER_FIREBOX_CASING.getItemVariant(STEEL_FIREBOX));
        ModHandler.addShapedRecipe(true, "large_titanium_boiler", MetaTileEntities.LARGE_TITANIUM_BOILER.getStackForm(),
                "PSP", "SAS", "PSP", 'P', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold), 'S',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.EV), 'A',
                MetaBlocks.BOILER_FIREBOX_CASING.getItemVariant(TITANIUM_FIREBOX));
        ModHandler.addShapedRecipe(true, "large_tungstensteel_boiler",
                MetaTileEntities.LARGE_TUNGSTENSTEEL_BOILER.getStackForm(), "PSP", "SAS", "PSP", 'P',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.Aluminium), 'S',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.IV), 'A',
                MetaBlocks.BOILER_FIREBOX_CASING.getItemVariant(TUNGSTENSTEEL_FIREBOX));

        ModHandler.addShapedRecipe(true, "assembly_line", MetaTileEntities.ASSEMBLY_LINE.getStackForm(), "CRC", "SAS",
                "CRC", 'A', MetaTileEntities.HULL[GTValues.IV].getStackForm(), 'R', MetaItems.ROBOT_ARM_IV, 'C',
                MetaBlocks.MULTIBLOCK_CASING
                        .getItemVariant(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_CONTROL),
                'S', new UnificationEntry(OrePrefix.circuit, Tier.ZPM));

        ModHandler.addShapedRecipe(true, "large_chemical_reactor",
                MetaTileEntities.LARGE_CHEMICAL_REACTOR.getStackForm(), "CRC", "PMP", "CHC", 'C',
                new UnificationEntry(OrePrefix.circuit, Tier.HV), 'R',
                OreDictUnifier.get(OrePrefix.rotor, Materials.StainlessSteel), 'P',
                OreDictUnifier.get(OrePrefix.pipeLargeFluid, Materials.Polytetrafluoroethylene), 'M',
                MetaItems.ELECTRIC_MOTOR_HV.getStackForm(), 'H', MetaTileEntities.HULL[GTValues.HV].getStackForm());

        ModHandler.addShapedRecipe(true, "power_substation", MetaTileEntities.POWER_SUBSTATION.getStackForm(), "LPL",
                "CBC", "LPL", 'L', MetaItems.LAPOTRON_CRYSTAL, 'P', MetaItems.POWER_INTEGRATED_CIRCUIT, 'C',
                new UnificationEntry(OrePrefix.circuit, Tier.LuV), 'B',
                MetaBlocks.METAL_CASING.getItemVariant(PALLADIUM_SUBSTATION));

        if (ConfigHolder.machines.steelSteamMultiblocks) {
            ModHandler.addShapedRecipe(true, "steam_oven", MetaTileEntities.STEAM_OVEN.getStackForm(), "CGC", "FMF",
                    "CGC", 'F', MetaBlocks.BOILER_FIREBOX_CASING.getItemVariant(STEEL_FIREBOX), 'C',
                    MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'M',
                    MetaTileEntities.STEAM_FURNACE_STEEL.getStackForm(), 'G',
                    new UnificationEntry(OrePrefix.gear, Materials.Invar));
            ModHandler.addShapedRecipe(true, "steam_grinder", MetaTileEntities.STEAM_GRINDER.getStackForm(), "CGC",
                    "CFC", "CGC", 'G', new UnificationEntry(OrePrefix.gear, Materials.Potin), 'F',
                    MetaTileEntities.STEAM_MACERATOR_STEEL.getStackForm(), 'C',
                    MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID));
            ModHandler.addShapedRecipe(true, "steam_hatch", MetaTileEntities.STEAM_HATCH.getStackForm(), "BPB", "BTB",
                    "BPB", 'B', new UnificationEntry(OrePrefix.plate, Materials.Steel), 'P',
                    new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Steel), 'T',
                    MetaTileEntities.STEEL_DRUM.getStackForm());
            ModHandler.addShapedRecipe(true, "steam_input_bus", MetaTileEntities.STEAM_IMPORT_BUS.getStackForm(), "C",
                    "H", 'H', MetaBlocks.STEAM_CASING.getItemVariant(STEEL_HULL), 'C', OreDictNames.chestWood);
            ModHandler.addShapedRecipe(true, "steam_output_bus", MetaTileEntities.STEAM_EXPORT_BUS.getStackForm(), "H",
                    "C", 'H', MetaBlocks.STEAM_CASING.getItemVariant(STEEL_HULL), 'C', OreDictNames.chestWood);
        } else {
            ModHandler.addShapedRecipe(true, "steam_oven", MetaTileEntities.STEAM_OVEN.getStackForm(), "CGC", "FMF",
                    "CGC", 'F', MetaBlocks.BOILER_FIREBOX_CASING.getItemVariant(BRONZE_FIREBOX), 'C',
                    MetaBlocks.METAL_CASING.getItemVariant(BRONZE_BRICKS), 'M',
                    MetaTileEntities.STEAM_FURNACE_BRONZE.getStackForm(), 'G',
                    new UnificationEntry(OrePrefix.gear, Materials.Invar));
            ModHandler.addShapedRecipe(true, "steam_grinder", MetaTileEntities.STEAM_GRINDER.getStackForm(), "CGC",
                    "CFC", "CGC", 'G', new UnificationEntry(OrePrefix.gear, Materials.Potin), 'F',
                    MetaTileEntities.STEAM_MACERATOR_BRONZE.getStackForm(), 'C',
                    MetaBlocks.METAL_CASING.getItemVariant(BRONZE_BRICKS));
            ModHandler.addShapedRecipe(true, "steam_hatch", MetaTileEntities.STEAM_HATCH.getStackForm(), "BPB", "BTB",
                    "BPB", 'B', new UnificationEntry(OrePrefix.plate, Materials.Bronze), 'P',
                    new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Bronze), 'T',
                    MetaTileEntities.BRONZE_DRUM.getStackForm());
            ModHandler.addShapedRecipe(true, "steam_input_bus", MetaTileEntities.STEAM_IMPORT_BUS.getStackForm(), "C",
                    "H", 'H', MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_HULL), 'C', OreDictNames.chestWood);
            ModHandler.addShapedRecipe(true, "steam_output_bus", MetaTileEntities.STEAM_EXPORT_BUS.getStackForm(), "H",
                    "C", 'H', MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_HULL), 'C', OreDictNames.chestWood);
        }

        ModHandler.addShapedRecipe(true, "processing_array", MetaTileEntities.PROCESSING_ARRAY.getStackForm(), "COC",
                "RHR", "CPC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.IV), 'O',
                MetaItems.TOOL_DATA_STICK.getStackForm(), 'R', MetaItems.ROBOT_ARM_EV.getStackForm(), 'P',
                OreDictUnifier.get(OrePrefix.pipeLargeFluid, Materials.StainlessSteel), 'H',
                MetaTileEntities.HULL[GTValues.EV].getStackForm());
        ModHandler.addShapedRecipe(true, "advanced_processing_array",
                MetaTileEntities.ADVANCED_PROCESSING_ARRAY.getStackForm(), "RCR", "SPE", "HNH", 'R',
                MetaItems.ROBOT_ARM_LuV, 'C', new UnificationEntry(OrePrefix.circuit, Tier.ZPM), 'S',
                MetaItems.SENSOR_LuV, 'P', MetaTileEntities.PROCESSING_ARRAY.getStackForm(), 'E', MetaItems.EMITTER_LuV,
                'H', new UnificationEntry(OrePrefix.plate, Materials.HSSE), 'N',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Naquadah));

        // GENERATORS
        ModHandler.addShapedRecipe(true, "diesel_generator_lv", MetaTileEntities.COMBUSTION_GENERATOR[0].getStackForm(),
                "PCP", "EME", "GWG", 'M', MetaTileEntities.HULL[GTValues.LV].getStackForm(), 'P',
                MetaItems.ELECTRIC_PISTON_LV, 'E', MetaItems.ELECTRIC_MOTOR_LV, 'C',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LV), 'W',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.Steel));
        ModHandler.addShapedRecipe(true, "diesel_generator_mv", MetaTileEntities.COMBUSTION_GENERATOR[1].getStackForm(),
                "PCP", "EME", "GWG", 'M', MetaTileEntities.HULL[GTValues.MV].getStackForm(), 'P',
                MetaItems.ELECTRIC_PISTON_MV, 'E', MetaItems.ELECTRIC_MOTOR_MV, 'C',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.MV), 'W',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.Aluminium));
        ModHandler.addShapedRecipe(true, "diesel_generator_hv", MetaTileEntities.COMBUSTION_GENERATOR[2].getStackForm(),
                "PCP", "EME", "GWG", 'M', MetaTileEntities.HULL[GTValues.HV].getStackForm(), 'P',
                MetaItems.ELECTRIC_PISTON_HV, 'E', MetaItems.ELECTRIC_MOTOR_HV, 'C',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.HV), 'W',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold), 'G',
                new UnificationEntry(OrePrefix.gear, Materials.StainlessSteel));

        ModHandler.addShapedRecipe(true, "gas_turbine_lv", MetaTileEntities.GAS_TURBINE[0].getStackForm(), "CRC", "RMR",
                "EWE", 'M', MetaTileEntities.HULL[GTValues.LV].getStackForm(), 'E', MetaItems.ELECTRIC_MOTOR_LV, 'R',
                new UnificationEntry(OrePrefix.rotor, Materials.Tin), 'C',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LV), 'W',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin));
        ModHandler.addShapedRecipe(true, "gas_turbine_mv", MetaTileEntities.GAS_TURBINE[1].getStackForm(), "CRC", "RMR",
                "EWE", 'M', MetaTileEntities.HULL[GTValues.MV].getStackForm(), 'E', MetaItems.ELECTRIC_MOTOR_MV, 'R',
                new UnificationEntry(OrePrefix.rotor, Materials.Bronze), 'C',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.MV), 'W',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper));
        ModHandler.addShapedRecipe(true, "gas_turbine_hv", MetaTileEntities.GAS_TURBINE[2].getStackForm(), "CRC", "RMR",
                "EWE", 'M', MetaTileEntities.HULL[GTValues.HV].getStackForm(), 'E', MetaItems.ELECTRIC_MOTOR_HV, 'R',
                new UnificationEntry(OrePrefix.rotor, Materials.Steel), 'C',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.HV), 'W',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold));

        ModHandler.addShapedRecipe(true, "steam_turbine_lv", MetaTileEntities.STEAM_TURBINE[0].getStackForm(), "PCP",
                "RMR", "EWE", 'M', MetaTileEntities.HULL[GTValues.LV].getStackForm(), 'E', MetaItems.ELECTRIC_MOTOR_LV,
                'R', new UnificationEntry(OrePrefix.rotor, Materials.Tin), 'C',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LV), 'W',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin), 'P',
                new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Bronze));
        ModHandler.addShapedRecipe(true, "steam_turbine_mv", MetaTileEntities.STEAM_TURBINE[1].getStackForm(), "PCP",
                "RMR", "EWE", 'M', MetaTileEntities.HULL[GTValues.MV].getStackForm(), 'E', MetaItems.ELECTRIC_MOTOR_MV,
                'R', new UnificationEntry(OrePrefix.rotor, Materials.Bronze), 'C',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.MV), 'W',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper), 'P',
                new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Steel));
        ModHandler.addShapedRecipe(true, "steam_turbine_hv", MetaTileEntities.STEAM_TURBINE[2].getStackForm(), "PCP",
                "RMR", "EWE", 'M', MetaTileEntities.HULL[GTValues.HV].getStackForm(), 'E', MetaItems.ELECTRIC_MOTOR_HV,
                'R', new UnificationEntry(OrePrefix.rotor, Materials.Steel), 'C',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.HV), 'W',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold), 'P',
                new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.StainlessSteel));

        ModHandler.addShapedRecipe(true, "workbench_bronze", MetaTileEntities.WORKBENCH.getStackForm(), "CSC", "PWP",
                "PsP", 'C', OreDictNames.chestWood, 'W', new ItemStack(Blocks.CRAFTING_TABLE), 'S',
                new UnificationEntry(OrePrefix.slab, Materials.Wood), 'P',
                new UnificationEntry(OrePrefix.plank, Materials.Wood));

        ModHandler.addShapedRecipe(true, "magic_energy_absorber", MetaTileEntities.MAGIC_ENERGY_ABSORBER.getStackForm(),
                "PCP", "PMP", "PCP", 'M', MetaTileEntities.HULL[GTValues.EV].getStackForm(), 'P', MetaItems.SENSOR_EV,
                'C', new UnificationEntry(OrePrefix.circuit, Tier.EV));

        ModHandler.addShapedRecipe(true, "primitive_pump", MetaTileEntities.PRIMITIVE_WATER_PUMP.getStackForm(), "RGS",
                "OWd", "CLC", 'R', new UnificationEntry(OrePrefix.ring, Materials.Iron), 'G',
                new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Wood), 'S',
                new UnificationEntry(OrePrefix.screw, Materials.Iron), 'O',
                new UnificationEntry(OrePrefix.rotor, Materials.Iron), 'W',
                MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK), 'C',
                new ItemStack(Blocks.STONE_SLAB, 1, 3), 'L',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Wood));
        ModHandler.addShapedRecipe(true, "pump_deck", MetaBlocks.STEAM_CASING.getItemVariant(PUMP_DECK, 2), "SWS",
                "dCh", 'S', new UnificationEntry(OrePrefix.screw, Materials.Iron), 'W',
                MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK), 'C',
                new ItemStack(Blocks.STONE_SLAB, 1, 3));
        ModHandler.addShapedRecipe(true, "pump_hatch", MetaTileEntities.PUMP_OUTPUT_HATCH.getStackForm(), "SRd", "PLP",
                "CRC", 'S', new UnificationEntry(OrePrefix.screw, Materials.Iron), 'R',
                new UnificationEntry(OrePrefix.ring, Materials.Iron), 'P',
                MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK), 'L',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Wood), 'C',
                new ItemStack(Blocks.STONE_SLAB, 1, 3));

        ModHandler.addShapedRecipe(true, "wood_multiblock_tank", MetaTileEntities.WOODEN_TANK.getStackForm(), " R ",
                "rCs", " R ", 'R', new UnificationEntry(OrePrefix.ring, Materials.Lead), 'C',
                MetaBlocks.STEAM_CASING.getItemVariant(WOOD_WALL));
        ModHandler.addShapedRecipe(true, "steel_multiblock_tank", MetaTileEntities.STEEL_TANK.getStackForm(), " R ",
                "hCw", " R ", 'R', new UnificationEntry(OrePrefix.ring, Materials.Steel), 'C',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID));
        ModHandler.addShapedRecipe(true, "wood_tank_valve", MetaTileEntities.WOODEN_TANK_VALVE.getStackForm(), " R ",
                "rCs", " O ", 'O', new UnificationEntry(OrePrefix.rotor, Materials.Lead), 'R',
                new UnificationEntry(OrePrefix.ring, Materials.Lead), 'C',
                MetaBlocks.STEAM_CASING.getItemVariant(WOOD_WALL));
        ModHandler.addShapedRecipe(true, "steel_tank_valve", MetaTileEntities.STEEL_TANK_VALVE.getStackForm(), " R ",
                "hCw", " O ", 'O', new UnificationEntry(OrePrefix.rotor, Materials.Steel), 'R',
                new UnificationEntry(OrePrefix.ring, Materials.Steel), 'C',
                MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID));
        ModHandler.addShapedRecipe(true, "wood_wall", MetaBlocks.STEAM_CASING.getItemVariant(WOOD_WALL), "W W", "sPh",
                "W W", 'W', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Lead));

        ModHandler.addShapedRecipe(true, "alarm", MetaTileEntities.ALARM.getStackForm(), "WRW", "CHL", "WPW", 'W',
                new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin), 'P', MetaItems.ELECTRIC_PUMP_LV, 'L',
                new UnificationEntry(OrePrefix.plate, Materials.Steel), 'C',
                new UnificationEntry(OrePrefix.circuit, Tier.LV), 'H',
                MetaTileEntities.HULL[GTValues.LV].getStackForm(), 'R',
                new UnificationEntry(OrePrefix.rotor, Materials.Tin));

        // MACHINES
        registerMachineRecipe(MetaTileEntities.ALLOY_SMELTER, "ECE", "CMC", "WCW", 'M', HULL, 'E', CIRCUIT, 'W', CABLE,
                'C', COIL_HEATING_DOUBLE);
        registerMachineRecipe(MetaTileEntities.ASSEMBLER, "ACA", "VMV", "WCW", 'M', HULL, 'V', CONVEYOR, 'A', ROBOT_ARM,
                'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(MetaTileEntities.BENDER, "PWP", "CMC", "EBE", 'M', HULL, 'E', MOTOR, 'P', PISTON, 'C',
                CIRCUIT, 'W', CABLE, 'B', PLATE);
        registerMachineRecipe(MetaTileEntities.CANNER, "WPW", "CMC", "GGG", 'M', HULL, 'P', PUMP, 'C', CIRCUIT, 'W',
                CABLE, 'G', GLASS);
        registerMachineRecipe(MetaTileEntities.COMPRESSOR, " C ", "PMP", "WCW", 'M', HULL, 'P', PISTON, 'C', CIRCUIT,
                'W', CABLE);
        registerMachineRecipe(MetaTileEntities.CUTTER, "WCG", "VMB", "CWE", 'M', HULL, 'E', MOTOR, 'V', CONVEYOR, 'C',
                CIRCUIT, 'W', CABLE, 'G', GLASS, 'B', SAWBLADE);
        registerMachineRecipe(MetaTileEntities.ELECTRIC_FURNACE, "ECE", "CMC", "WCW", 'M', HULL, 'E', CIRCUIT, 'W',
                CABLE, 'C', COIL_HEATING);
        registerMachineRecipe(MetaTileEntities.EXTRACTOR, "GCG", "EMP", "WCW", 'M', HULL, 'E', PISTON, 'P', PUMP, 'C',
                CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(MetaTileEntities.EXTRUDER, "CCE", "XMP", "CCE", 'M', HULL, 'X', PISTON, 'E', CIRCUIT, 'P',
                PIPE_NORMAL, 'C', COIL_HEATING_DOUBLE);
        registerMachineRecipe(MetaTileEntities.LATHE, "WCW", "EMD", "CWP", 'M', HULL, 'E', MOTOR, 'P', PISTON, 'C',
                CIRCUIT, 'W', CABLE, 'D', GRINDER);
        registerMachineRecipe(MetaTileEntities.MACERATOR, "PEG", "WWM", "CCW", 'M', HULL, 'E', MOTOR, 'P', PISTON, 'C',
                CIRCUIT, 'W', CABLE, 'G', GRINDER);
        registerMachineRecipe(MetaTileEntities.WIREMILL, "EWE", "CMC", "EWE", 'M', HULL, 'E', MOTOR, 'C', CIRCUIT, 'W',
                CABLE);
        registerMachineRecipe(MetaTileEntities.CENTRIFUGE, "CEC", "WMW", "CEC", 'M', HULL, 'E', MOTOR, 'C', CIRCUIT,
                'W', CABLE);
        registerMachineRecipe(MetaTileEntities.ELECTROLYZER, "IGI", "IMI", "CWC", 'M', HULL, 'C', CIRCUIT, 'W', CABLE,
                'I', WIRE_ELECTRIC, 'G', GLASS);
        registerMachineRecipe(MetaTileEntities.THERMAL_CENTRIFUGE, "CEC", "OMO", "WEW", 'M', HULL, 'E', MOTOR, 'C',
                CIRCUIT, 'W', CABLE, 'O', COIL_HEATING_DOUBLE);
        registerMachineRecipe(MetaTileEntities.ORE_WASHER, "RGR", "CEC", "WMW", 'M', HULL, 'R', ROTOR, 'E', MOTOR, 'C',
                CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(MetaTileEntities.PACKER, "BCB", "RMV", "WCW", 'M', HULL, 'R', ROBOT_ARM, 'V', CONVEYOR,
                'C', CIRCUIT, 'W', CABLE, 'B', OreDictNames.chestWood);
        registerMachineRecipe(MetaTileEntities.CHEMICAL_REACTOR, "GRG", "WEW", "CMC", 'M', HULL, 'R', ROTOR, 'E', MOTOR,
                'C', CIRCUIT, 'W', CABLE, 'G', PIPE_REACTOR);
        registerMachineRecipe(MetaTileEntities.BREWERY, "GPG", "WMW", "CBC", 'M', HULL, 'P', PUMP, 'B',
                STICK_DISTILLATION, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(MetaTileEntities.FERMENTER, "WPW", "GMG", "WCW", 'M', HULL, 'P', PUMP, 'C', CIRCUIT, 'W',
                CABLE, 'G', GLASS);
        registerMachineRecipe(MetaTileEntities.FLUID_SOLIDIFIER, "PGP", "WMW", "CBC", 'M', HULL, 'P', PUMP, 'C',
                CIRCUIT, 'W', CABLE, 'G', GLASS, 'B', OreDictNames.chestWood);
        registerMachineRecipe(MetaTileEntities.DISTILLERY, "GBG", "CMC", "WPW", 'M', HULL, 'P', PUMP, 'B',
                STICK_DISTILLATION, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(MetaTileEntities.CHEMICAL_BATH, "VGW", "PGV", "CMC", 'M', HULL, 'P', PUMP, 'V', CONVEYOR,
                'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(MetaTileEntities.POLARIZER, "ZSZ", "WMW", "ZSZ", 'M', HULL, 'S', STICK_ELECTROMAGNETIC,
                'Z', COIL_ELECTRIC, 'W', CABLE);
        registerMachineRecipe(MetaTileEntities.ELECTROMAGNETIC_SEPARATOR, "VWZ", "WMS", "CWZ", 'M', HULL, 'S',
                STICK_ELECTROMAGNETIC, 'Z', COIL_ELECTRIC, 'V', CONVEYOR, 'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(MetaTileEntities.AUTOCLAVE, "IGI", "IMI", "CPC", 'M', HULL, 'P', PUMP, 'C', CIRCUIT, 'I',
                PLATE, 'G', GLASS);
        registerMachineRecipe(MetaTileEntities.MIXER, "GRG", "GEG", "CMC", 'M', HULL, 'E', MOTOR, 'R', ROTOR, 'C',
                CIRCUIT, 'G', GLASS);
        registerMachineRecipe(MetaTileEntities.LASER_ENGRAVER, "PEP", "CMC", "WCW", 'M', HULL, 'E', EMITTER, 'P',
                PISTON, 'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(MetaTileEntities.FORMING_PRESS, "WPW", "CMC", "WPW", 'M', HULL, 'P', PISTON, 'C', CIRCUIT,
                'W', CABLE);
        registerMachineRecipe(MetaTileEntities.FORGE_HAMMER, "WPW", "CMC", "WAW", 'M', HULL, 'P', PISTON, 'C', CIRCUIT,
                'W', CABLE, 'A', OreDictNames.craftingAnvil);
        registerMachineRecipe(MetaTileEntities.FLUID_HEATER, "OGO", "PMP", "WCW", 'M', HULL, 'P', PUMP, 'O',
                COIL_HEATING_DOUBLE, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(MetaTileEntities.SIFTER, "WFW", "PMP", "CFC", 'M', HULL, 'P', PISTON, 'F',
                MetaItems.ITEM_FILTER, 'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(MetaTileEntities.ARC_FURNACE, "WGW", "CMC", "PPP", 'M', HULL, 'P', PLATE, 'C', CIRCUIT,
                'W', CABLE_QUAD, 'G', new UnificationEntry(OrePrefix.dust, Materials.Graphite));
        registerMachineRecipe(MetaTileEntities.CIRCUIT_ASSEMBLER, "RIE", "CHC", "WIW", 'R', ROBOT_ARM, 'I',
                BETTER_CIRCUIT, 'E', EMITTER, 'C', CONVEYOR, 'H', HULL, 'W', CABLE);
        // TODO Replication system
        // registerMachineRecipe(MetaTileEntities.MASS_FABRICATOR, "CFC", "QMQ", "CFC", 'M', HULL, 'Q', CABLE_QUAD, 'C',
        // BETTER_CIRCUIT, 'F', FIELD_GENERATOR);
        // registerMachineRecipe(MetaTileEntities.REPLICATOR, "EFE", "CMC", "EQE", 'M', HULL, 'Q', CABLE_QUAD, 'C',
        // BETTER_CIRCUIT, 'F', FIELD_GENERATOR, 'E', EMITTER);
        registerMachineRecipe(MetaTileEntities.SCANNER, "CEC", "WHW", "CSC", 'C', BETTER_CIRCUIT, 'E', EMITTER, 'W',
                CABLE, 'H', HULL, 'S', SENSOR);
        registerMachineRecipe(MetaTileEntities.GAS_COLLECTOR, "WFW", "PHP", "WCW", 'W', Blocks.IRON_BARS, 'F',
                MetaItems.FLUID_FILTER, 'P', PUMP, 'H', HULL, 'C', CIRCUIT);
        registerMachineRecipe(MetaTileEntities.ROCK_BREAKER, "PMW", "CHC", "GGG", 'P', PISTON, 'M', MOTOR, 'W', GRINDER,
                'C', CABLE, 'H', HULL, 'G', GLASS);

        registerMachineRecipe(MetaTileEntities.PUMP, "WGW", "GMG", "TGT", 'M', HULL, 'W', CIRCUIT, 'G', PUMP, 'T',
                PIPE_LARGE);
        registerMachineRecipe(MetaTileEntities.FISHER, "WTW", "PMP", "TGT", 'M', HULL, 'W', CIRCUIT, 'G', PUMP, 'T',
                MOTOR, 'P', PISTON);
        registerMachineRecipe(MetaTileEntities.ITEM_COLLECTOR, "MRM", "RHR", "CWC", 'M', MOTOR, 'R', ROTOR, 'H', HULL,
                'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(MetaTileEntities.BLOCK_BREAKER, "MGM", "CHC", "WSW", 'M', MOTOR, 'H', HULL, 'C', CIRCUIT,
                'W', CABLE, 'S', OreDictNames.chestWood, 'G', GRINDER);
        registerMachineRecipe(MetaTileEntities.WORLD_ACCELERATOR, "IGI", "FHF", "IGI", 'H', HULL, 'F', EMITTER, 'G',
                SENSOR, 'I', FIELD_GENERATOR);
        registerMachineRecipe(MetaTileEntities.MINER, "MMM", "WHW", "CSC", 'M', MOTOR, 'W', CABLE, 'H', HULL, 'C',
                CIRCUIT, 'S', SENSOR);

        registerMachineRecipe(MetaTileEntities.MUFFLER_HATCH, "HM", "PR", 'H', HULL, 'M', MOTOR, 'P', PIPE_NORMAL, 'R',
                ROTOR);

        registerMachineRecipe(ArrayUtils.subarray(MetaTileEntities.DIODES, GTValues.ULV, GTValues.HV), "CDC", "DHD",
                "PDP", 'H', HULL, 'D', new UnificationEntry(OrePrefix.component, MarkerMaterials.Component.Diode), 'P',
                PLATE, 'C', CABLE_QUAD);
        registerMachineRecipe(ArrayUtils.subarray(MetaTileEntities.DIODES, GTValues.HV, GTValues.LuV), "CDC", "DHD",
                "PDP", 'H', HULL, 'D', MetaItems.SMD_DIODE, 'P', PLATE, 'C', CABLE_QUAD);
        registerMachineRecipe(
                ArrayUtils.subarray(MetaTileEntities.DIODES, GTValues.LuV, MetaTileEntities.DIODES.length), "CDC",
                "DHD", "PDP", 'H', HULL, 'D', MetaItems.ADVANCED_SMD_DIODE, 'P', PLATE, 'C', CABLE_QUAD);

        registerMachineRecipe(ArrayUtils.subarray(MetaTileEntities.TRANSFORMER, GTValues.ULV, GTValues.MV), " CC",
                "TH ", " CC", 'C', CABLE, 'T', CABLE_TIER_UP, 'H', HULL);
        registerMachineRecipe(ArrayUtils.subarray(MetaTileEntities.TRANSFORMER, GTValues.MV, GTValues.UHV), "WCC",
                "TH ", "WCC", 'W', POWER_COMPONENT, 'C', CABLE, 'T', CABLE_TIER_UP, 'H', HULL);

        registerMachineRecipe(ArrayUtils.subarray(MetaTileEntities.HI_AMP_TRANSFORMER, GTValues.ULV, GTValues.MV),
                " CC", "TH ", " CC", 'C', CABLE_QUAD, 'T', CABLE_QUAD_TIER_UP, 'H', TRANSFORMER);
        registerMachineRecipe(ArrayUtils.subarray(MetaTileEntities.HI_AMP_TRANSFORMER, GTValues.MV, GTValues.UHV),
                "WCC", "TH ", "WCC", 'W', VOLTAGE_COIL, 'C', CABLE_QUAD, 'T', CABLE_QUAD_TIER_UP, 'H', TRANSFORMER);

        registerMachineRecipe(MetaTileEntities.BATTERY_BUFFER[0], "WTW", "WMW", 'M', HULL, 'W', WIRE_QUAD, 'T',
                OreDictNames.chestWood);
        registerMachineRecipe(MetaTileEntities.BATTERY_BUFFER[1], "WTW", "WMW", 'M', HULL, 'W', WIRE_OCT, 'T',
                OreDictNames.chestWood);
        registerMachineRecipe(MetaTileEntities.BATTERY_BUFFER[2], "WTW", "WMW", 'M', HULL, 'W', WIRE_HEX, 'T',
                OreDictNames.chestWood);

        registerMachineRecipe(MetaTileEntities.CHARGER, "WTW", "WMW", "BCB", 'M', HULL, 'W', WIRE_QUAD, 'T',
                OreDictNames.chestWood, 'B', CABLE, 'C', CIRCUIT);

        ModHandler.addShapedRecipe(true, "wooden_crate", MetaTileEntities.WOODEN_CRATE.getStackForm(), "RPR", "PsP",
                "RPR", 'P', "plankWood", 'R', new UnificationEntry(OrePrefix.screw, Materials.Iron));
        ModHandler.addShapedRecipe(true, "bronze_crate", MetaTileEntities.BRONZE_CRATE.getStackForm(), "RPR", "PhP",
                "RPR", 'P', new UnificationEntry(OrePrefix.plate, Materials.Bronze), 'R',
                new UnificationEntry(OrePrefix.stickLong, Materials.Bronze));
        ModHandler.addShapedRecipe(true, "steel_crate", MetaTileEntities.STEEL_CRATE.getStackForm(), "RPR", "PhP",
                "RPR", 'P', new UnificationEntry(OrePrefix.plate, Materials.Steel), 'R',
                new UnificationEntry(OrePrefix.stickLong, Materials.Steel));
        ModHandler.addShapedRecipe(true, "aluminium_crate", MetaTileEntities.ALUMINIUM_CRATE.getStackForm(), "RPR",
                "PhP", "RPR", 'P', new UnificationEntry(OrePrefix.plate, Materials.Aluminium), 'R',
                new UnificationEntry(OrePrefix.stickLong, Materials.Aluminium));
        ModHandler.addShapedRecipe(true, "stainless_steel_crate", MetaTileEntities.STAINLESS_STEEL_CRATE.getStackForm(),
                "RPR", "PhP", "RPR", 'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel), 'R',
                new UnificationEntry(OrePrefix.stickLong, Materials.StainlessSteel));
        ModHandler.addShapedRecipe(true, "titanium_crate", MetaTileEntities.TITANIUM_CRATE.getStackForm(), "RPR", "PhP",
                "RPR", 'P', new UnificationEntry(OrePrefix.plate, Materials.Titanium), 'R',
                new UnificationEntry(OrePrefix.stickLong, Materials.Titanium));
        ModHandler.addShapedRecipe(true, "tungstensteel_crate", MetaTileEntities.TUNGSTENSTEEL_CRATE.getStackForm(),
                "RPR", "PhP", "RPR", 'P', new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel), 'R',
                new UnificationEntry(OrePrefix.stickLong, Materials.TungstenSteel));

        ModHandler.addShapedRecipe(true, "wooden_barrel", MetaTileEntities.WOODEN_DRUM.getStackForm(), "rSs", "PRP",
                "PRP", 'S', MetaItems.STICKY_RESIN.getStackForm(), 'P', "plankWood", 'R',
                new UnificationEntry(OrePrefix.stickLong, Materials.Iron));
        ModHandler.addShapedRecipe(true, "bronze_drum", MetaTileEntities.BRONZE_DRUM.getStackForm(), " h ", "PRP",
                "PRP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Bronze), 'R',
                new UnificationEntry(OrePrefix.stickLong, Materials.Bronze));
        ModHandler.addShapedRecipe(true, "steel_drum", MetaTileEntities.STEEL_DRUM.getStackForm(), " h ", "PRP", "PRP",
                'P', new UnificationEntry(OrePrefix.plate, Materials.Steel), 'R',
                new UnificationEntry(OrePrefix.stickLong, Materials.Steel));
        ModHandler.addShapedRecipe(true, "aluminium_drum", MetaTileEntities.ALUMINIUM_DRUM.getStackForm(), " h ", "PRP",
                "PRP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Aluminium), 'R',
                new UnificationEntry(OrePrefix.stickLong, Materials.Aluminium));
        ModHandler.addShapedRecipe(true, "stainless_steel_drum", MetaTileEntities.STAINLESS_STEEL_DRUM.getStackForm(),
                " h ", "PRP", "PRP", 'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel), 'R',
                new UnificationEntry(OrePrefix.stickLong, Materials.StainlessSteel));
        ModHandler.addShapedRecipe(true, "titanium_drum", MetaTileEntities.TITANIUM_DRUM.getStackForm(), " h ", "PRP",
                "PRP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Titanium), 'R',
                new UnificationEntry(OrePrefix.stickLong, Materials.Titanium));
        ModHandler.addShapedRecipe(true, "tungstensteel_drum", MetaTileEntities.TUNGSTENSTEEL_DRUM.getStackForm(),
                " h ", "PRP", "PRP", 'P', new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel), 'R',
                new UnificationEntry(OrePrefix.stickLong, Materials.TungstenSteel));
        ModHandler.addShapedRecipe(true, "gold_drum", MetaTileEntities.GOLD_DRUM.getStackForm(), " h ", "PRP", "PRP",
                'P', new UnificationEntry(OrePrefix.plate, Materials.Gold), 'R',
                new UnificationEntry(OrePrefix.stickLong, Materials.Gold));

        // Hermetic Casings
        ModHandler.addShapedRecipe(true, "hermetic_casing_lv", MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_LV),
                "PPP", "PFP", "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Steel), 'F',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Polyethylene));
        ModHandler.addShapedRecipe(true, "hermetic_casing_mv", MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_MV),
                "PPP", "PFP", "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Aluminium), 'F',
                new UnificationEntry(OrePrefix.pipeLargeItem, Materials.PolyvinylChloride));
        ModHandler.addShapedRecipe(true, "hermetic_casing_hv", MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_HV),
                "PPP", "PFP", "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel), 'F',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Polytetrafluoroethylene));
        ModHandler.addShapedRecipe(true, "hermetic_casing_ev", MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_EV),
                "PPP", "PFP", "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Titanium), 'F',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.StainlessSteel));
        ModHandler.addShapedRecipe(true, "hermetic_casing_iv", MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_IV),
                "PPP", "PFP", "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel), 'F',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Titanium));
        ModHandler.addShapedRecipe(true, "hermetic_casing_luv", MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_LUV),
                "PPP", "PFP", "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.RhodiumPlatedPalladium), 'F',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.TungstenSteel));
        ModHandler.addShapedRecipe(true, "hermetic_casing_zpm", MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_ZPM),
                "PPP", "PFP", "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.NaquadahAlloy), 'F',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.NiobiumTitanium));
        ModHandler.addShapedRecipe(true, "hermetic_casing_uv", MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_UV),
                "PPP", "PFP", "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Darmstadtium), 'F',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Naquadah));
        ModHandler.addShapedRecipe(true, "hermetic_casing_max", MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_UHV),
                "PPP", "PFP", "PPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Neutronium), 'F',
                new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Duranium));

        // Super / Quantum Chests
        ModHandler.addShapedRecipe(true, "super_chest_lv", MetaTileEntities.QUANTUM_CHEST[0].getStackForm(), "CPC",
                "PFP", "CPC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.LV), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Steel), 'F',
                MetaTileEntities.STEEL_CRATE.getStackForm());
        ModHandler.addShapedRecipe(true, "super_chest_mv", MetaTileEntities.QUANTUM_CHEST[1].getStackForm(), "CPC",
                "PFP", "CPC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.MV), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Aluminium), 'F',
                MetaTileEntities.ALUMINIUM_CRATE.getStackForm());
        ModHandler.addShapedRecipe(true, "super_chest_hv", MetaTileEntities.QUANTUM_CHEST[2].getStackForm(), "CPC",
                "PFP", "CGC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.HV), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel), 'F',
                MetaTileEntities.STAINLESS_STEEL_CRATE.getStackForm(), 'G',
                MetaItems.FIELD_GENERATOR_LV.getStackForm());
        ModHandler.addShapedRecipe(true, "super_chest_ev", MetaTileEntities.QUANTUM_CHEST[3].getStackForm(), "CPC",
                "PFP", "CGC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.EV), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Titanium), 'F',
                MetaTileEntities.TITANIUM_CRATE.getStackForm(), 'G', MetaItems.FIELD_GENERATOR_MV.getStackForm());
        ModHandler.addShapedRecipe(true, "super_chest_iv", MetaTileEntities.QUANTUM_CHEST[4].getStackForm(), "CPC",
                "PFP", "CGC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.IV), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel), 'F',
                MetaTileEntities.TUNGSTENSTEEL_CRATE.getStackForm(), 'G', MetaItems.FIELD_GENERATOR_HV.getStackForm());

        ModHandler.addShapedRecipe(true, "quantum_chest_iv", MetaTileEntities.QUANTUM_CHEST[5].getStackForm(), "CPC",
                "PHP", "CFC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.IV), 'P',
                new UnificationEntry(OrePrefix.plateDense, Materials.TungstenSteel), 'F',
                MetaItems.FIELD_GENERATOR_EV.getStackForm(), 'H', MetaTileEntities.HULL[5].getStackForm());
        ModHandler.addShapedRecipe(true, "quantum_chest_luv", MetaTileEntities.QUANTUM_CHEST[6].getStackForm(), "CPC",
                "PHP", "CFC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.LuV), 'P',
                new UnificationEntry(OrePrefix.plateDense, Materials.RhodiumPlatedPalladium), 'F',
                MetaItems.FIELD_GENERATOR_IV.getStackForm(), 'H', MetaTileEntities.HULL[6].getStackForm());
        ModHandler.addShapedRecipe(true, "quantum_chest_zpm", MetaTileEntities.QUANTUM_CHEST[7].getStackForm(), "CPC",
                "PHP", "CFC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.ZPM), 'P',
                new UnificationEntry(OrePrefix.plateDense, Materials.NaquadahAlloy), 'F',
                MetaItems.FIELD_GENERATOR_LuV.getStackForm(), 'H', MetaTileEntities.HULL[7].getStackForm());
        ModHandler.addShapedRecipe(true, "quantum_chest_uv", MetaTileEntities.QUANTUM_CHEST[8].getStackForm(), "CPC",
                "PHP", "CFC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.UV), 'P',
                new UnificationEntry(OrePrefix.plateDense, Materials.Darmstadtium), 'F',
                MetaItems.FIELD_GENERATOR_ZPM.getStackForm(), 'H', MetaTileEntities.HULL[8].getStackForm());
        ModHandler.addShapedRecipe(true, "quantum_chest_uhv", MetaTileEntities.QUANTUM_CHEST[9].getStackForm(), "CPC",
                "PHP", "CFC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.UHV), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Neutronium), 'F',
                MetaItems.FIELD_GENERATOR_UV.getStackForm(), 'H', MetaTileEntities.HULL[9].getStackForm());

        // Super / Quantum Tanks
        ModHandler.addShapedRecipe(true, "super_tank_lv", MetaTileEntities.QUANTUM_TANK[0].getStackForm(), "CPC", "PHP",
                "CFC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.LV), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Steel), 'F', MetaItems.ELECTRIC_PUMP_LV.getStackForm(),
                'H', MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_LV));
        ModHandler.addShapedRecipe(true, "super_tank_mv", MetaTileEntities.QUANTUM_TANK[1].getStackForm(), "CPC", "PHP",
                "CFC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.MV), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Aluminium), 'F',
                MetaItems.ELECTRIC_PUMP_MV.getStackForm(), 'H', MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_MV));
        ModHandler.addShapedRecipe(true, "super_tank_hv", MetaTileEntities.QUANTUM_TANK[2].getStackForm(), "CGC", "PHP",
                "CFC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.HV), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel), 'F',
                MetaItems.ELECTRIC_PUMP_HV.getStackForm(), 'H', MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_HV),
                'G', MetaItems.FIELD_GENERATOR_LV.getStackForm());
        ModHandler.addShapedRecipe(true, "super_tank_ev", MetaTileEntities.QUANTUM_TANK[3].getStackForm(), "CGC", "PHP",
                "CFC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.EV), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Titanium), 'F',
                MetaItems.ELECTRIC_PUMP_EV.getStackForm(), 'H', MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_EV),
                'G', MetaItems.FIELD_GENERATOR_MV.getStackForm());
        ModHandler.addShapedRecipe(true, "super_tank_iv", MetaTileEntities.QUANTUM_TANK[4].getStackForm(), "CGC", "PHP",
                "CFC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.IV), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel), 'F',
                MetaItems.ELECTRIC_PUMP_IV.getStackForm(), 'H', MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_IV),
                'G', MetaItems.FIELD_GENERATOR_HV.getStackForm());

        ModHandler.addShapedRecipe(true, "quantum_tank_iv", MetaTileEntities.QUANTUM_TANK[5].getStackForm(), "CGC",
                "PHP", "CUC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.IV), 'P',
                new UnificationEntry(OrePrefix.plateDense, Materials.TungstenSteel), 'U',
                MetaItems.ELECTRIC_PUMP_IV.getStackForm(), 'G', MetaItems.FIELD_GENERATOR_EV.getStackForm(), 'H',
                MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_IV));
        ModHandler.addShapedRecipe(true, "quantum_tank_luv", MetaTileEntities.QUANTUM_TANK[6].getStackForm(), "CGC",
                "PHP", "CUC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.LuV), 'P',
                new UnificationEntry(OrePrefix.plateDense, Materials.RhodiumPlatedPalladium), 'U',
                MetaItems.ELECTRIC_PUMP_LuV.getStackForm(), 'G', MetaItems.FIELD_GENERATOR_IV.getStackForm(), 'H',
                MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_LUV));
        ModHandler.addShapedRecipe(true, "quantum_tank_zpm", MetaTileEntities.QUANTUM_TANK[7].getStackForm(), "CGC",
                "PHP", "CUC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.ZPM), 'P',
                new UnificationEntry(OrePrefix.plateDense, Materials.NaquadahAlloy), 'U',
                MetaItems.ELECTRIC_PUMP_ZPM.getStackForm(), 'G', MetaItems.FIELD_GENERATOR_LuV.getStackForm(), 'H',
                MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_ZPM));
        ModHandler.addShapedRecipe(true, "quantum_tank_uv", MetaTileEntities.QUANTUM_TANK[8].getStackForm(), "CGC",
                "PHP", "CUC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.UV), 'P',
                new UnificationEntry(OrePrefix.plateDense, Materials.Darmstadtium), 'U',
                MetaItems.ELECTRIC_PUMP_UV.getStackForm(), 'G', MetaItems.FIELD_GENERATOR_ZPM.getStackForm(), 'H',
                MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_UV));
        ModHandler.addShapedRecipe(true, "quantum_tank_uhv", MetaTileEntities.QUANTUM_TANK[9].getStackForm(), "CGC",
                "PHP", "CUC", 'C', new UnificationEntry(OrePrefix.circuit, Tier.UHV), 'P',
                new UnificationEntry(OrePrefix.plate, Materials.Neutronium), 'U',
                MetaItems.ELECTRIC_PUMP_UV.getStackForm(), 'G', MetaItems.FIELD_GENERATOR_UV.getStackForm(), 'H',
                MetaBlocks.HERMETIC_CASING.getItemVariant(HERMETIC_UHV));

        ModHandler.addShapedRecipe(true, "buffer_lv", MetaTileEntities.BUFFER[0].getStackForm(), "HP", "CV", 'H',
                MetaTileEntities.HULL[GTValues.LV].getStackForm(), 'P', MetaItems.ELECTRIC_PUMP_LV.getStackForm(), 'V',
                MetaItems.CONVEYOR_MODULE_LV.getStackForm(), 'C',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LV));
        ModHandler.addShapedRecipe(true, "buffer_mv", MetaTileEntities.BUFFER[1].getStackForm(), "HP", "CV", 'H',
                MetaTileEntities.HULL[GTValues.MV].getStackForm(), 'P', MetaItems.ELECTRIC_PUMP_MV.getStackForm(), 'V',
                MetaItems.CONVEYOR_MODULE_MV.getStackForm(), 'C',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LV));
        ModHandler.addShapedRecipe(true, "buffer_hv", MetaTileEntities.BUFFER[2].getStackForm(), "HP", "CV", 'H',
                MetaTileEntities.HULL[GTValues.HV].getStackForm(), 'P', MetaItems.ELECTRIC_PUMP_HV.getStackForm(), 'V',
                MetaItems.CONVEYOR_MODULE_HV.getStackForm(), 'C',
                new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LV));

        if (ConfigHolder.recipes.harderEBFControllerRecipe) {
            ModHandler.addShapedRecipe(true, "electric_blast_furnace",
                    MetaTileEntities.ELECTRIC_BLAST_FURNACE.getStackForm(),
                    "FFF", "CHC", "WCW",
                    'F', MetaTileEntities.ELECTRIC_FURNACE[1].getStackForm(),
                    'C', new UnificationEntry(OrePrefix.circuit, Tier.LV),
                    'H', MetaBlocks.METAL_CASING.getItemVariant(INVAR_HEATPROOF),
                    'W', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin));
        } else {
            ModHandler.addShapedRecipe(true, "electric_blast_furnace",
                    MetaTileEntities.ELECTRIC_BLAST_FURNACE.getStackForm(), "FFF", "CMC", "WCW", 'M',
                    MetaBlocks.METAL_CASING.getItemVariant(INVAR_HEATPROOF), 'F', OreDictNames.craftingFurnace, 'C',
                    new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LV), 'W',
                    new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin));
        }

        ModHandler.addShapedRecipe(true, "cleanroom", MetaTileEntities.CLEANROOM.getStackForm(), "FFF", "RHR", "MCM",
                'F', MetaItems.ITEM_FILTER.getStackForm(), 'R',
                new UnificationEntry(OrePrefix.rotor, Materials.StainlessSteel), 'H', HULL.getIngredient(GTValues.HV),
                'M', MetaItems.ELECTRIC_MOTOR_HV.getStackForm(), 'C', new UnificationEntry(OrePrefix.circuit, Tier.HV));

        if (ConfigHolder.compat.energy.enableFEConverters) {
            registerMachineRecipe(MetaTileEntities.ENERGY_CONVERTER[0], " WW", "RMC", " WW", 'C', CIRCUIT, 'M', HULL,
                    'W', CABLE, 'R', new UnificationEntry(OrePrefix.cableGtSingle, Materials.RedAlloy));
            registerMachineRecipe(MetaTileEntities.ENERGY_CONVERTER[1], " WW", "RMC", " WW", 'C', CIRCUIT, 'M', HULL,
                    'W', CABLE_QUAD, 'R', new UnificationEntry(OrePrefix.cableGtQuadruple, Materials.RedAlloy));
            registerMachineRecipe(MetaTileEntities.ENERGY_CONVERTER[2], " WW", "RMC", " WW", 'C', CIRCUIT, 'M', HULL,
                    'W', CABLE_OCT, 'R', new UnificationEntry(OrePrefix.cableGtOctal, Materials.RedAlloy));
            registerMachineRecipe(MetaTileEntities.ENERGY_CONVERTER[3], " WW", "RMC", " WW", 'C', CIRCUIT, 'M', HULL,
                    'W', CABLE_HEX, 'R', new UnificationEntry(OrePrefix.cableGtHex, Materials.RedAlloy));
        }

        ModHandler.addShapedRecipe(true, "fission_reactor", MetaTileEntities.FISSION_REACTOR.getStackForm(), "CSC",
                "RHR", "CWC",
                'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.EV),
                'H', MetaBlocks.FISSION_CASING.getItemVariant(BlockFissionCasing.FissionCasingType.REACTOR_VESSEL),
                'S', SENSOR.getIngredient(GTValues.EV),
                'R', new UnificationEntry(OrePrefix.rotor, Materials.Steel),
                'W', CABLE_QUAD.getIngredient(GTValues.EV));

        ModHandler.addShapedRecipe(true, "heat_exchanger", MetaTileEntities.HEAT_EXCHANGER.getStackForm(), "FFF",
                "PCP", "FFF",
                'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.HV),
                'P', new UnificationEntry(OrePrefix.pipeLargeFluid, Materials.Inconel),
                'F', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID));

        ModHandler.addShapedRecipe(true, "gas_centrifuge", MetaTileEntities.GAS_CENTRIFUGE.getStackForm(), "FFF",
                "WRW", "CCC",
                'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.EV),
                'R', new UnificationEntry(OrePrefix.rotor, Materials.Titanium),
                'F', MetaBlocks.METAL_CASING.getItemVariant(PTFE_INERT_CASING),
                'W', CABLE_QUAD.getIngredient(GTValues.EV));

        ModHandler.addShapedRecipe(true, "spent_fuel_pool", MetaTileEntities.SPENT_FUEL_POOL.getStackForm(), "PFP",
                "PCP", "PFP",
                'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LV),
                'F', MetaBlocks.METAL_CASING.getItemVariant(STAINLESS_CLEAN),
                'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel));
    }

    // Can only accept a subset of "Item" types:
    // - ItemStack
    // - Item
    // - Block
    // - MetaItem.MetaValueItem
    // - CraftingComponent.Component
    // - UnificationEntry
    // - String (intended as an OreDictionary entry)
    public static <
            T extends MetaTileEntity & ITieredMetaTileEntity> void registerMachineRecipe(boolean withUnificationData,
                                                                                         T[] metaTileEntities,
                                                                                         Object... recipe) {
        for (T metaTileEntity : metaTileEntities) {

            // Needed to skip certain tiers if not enabled.
            // Leaves UHV+ machine recipes to be implemented by addons.
            if (metaTileEntity != null) {
                Object[] prepRecipe = prepareRecipe(metaTileEntity.getTier(), Arrays.copyOf(recipe, recipe.length));
                if (prepRecipe == null) {
                    return;
                }
                ModHandler.addShapedRecipe(withUnificationData, metaTileEntity.getMetaName(),
                        metaTileEntity.getStackForm(), prepRecipe);
            }
        }
    }

    public static <T extends MetaTileEntity & ITieredMetaTileEntity> void registerMachineRecipe(T[] metaTileEntities,
                                                                                                Object... recipe) {
        registerMachineRecipe(true, metaTileEntities, recipe);
    }

    private static Object[] prepareRecipe(int tier, Object... recipe) {
        for (int i = 3; i < recipe.length; i++) {
            if (recipe[i] instanceof Component) {
                Object component = ((Component) recipe[i]).getIngredient(tier);
                if (component == null) {
                    return null;
                }
                recipe[i] = component;
            }
        }
        return recipe;
    }
}
