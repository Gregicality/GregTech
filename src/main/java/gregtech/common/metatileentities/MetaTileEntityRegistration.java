package gregtech.common.metatileentities;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.FeCompat;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.converter.MetaTileEntityConverter;
import gregtech.common.metatileentities.electric.MetaTileEntityAlarm;
import gregtech.common.metatileentities.electric.MetaTileEntityBatteryBuffer;
import gregtech.common.metatileentities.electric.MetaTileEntityBlockBreaker;
import gregtech.common.metatileentities.electric.MetaTileEntityCharger;
import gregtech.common.metatileentities.electric.MetaTileEntityDiode;
import gregtech.common.metatileentities.electric.MetaTileEntityFisher;
import gregtech.common.metatileentities.electric.MetaTileEntityGasCollector;
import gregtech.common.metatileentities.electric.MetaTileEntityHull;
import gregtech.common.metatileentities.electric.MetaTileEntityItemCollector;
import gregtech.common.metatileentities.electric.MetaTileEntityMagicEnergyAbsorber;
import gregtech.common.metatileentities.electric.MetaTileEntityMiner;
import gregtech.common.metatileentities.electric.MetaTileEntityPump;
import gregtech.common.metatileentities.electric.MetaTileEntityRockBreaker;
import gregtech.common.metatileentities.electric.MetaTileEntitySingleCombustion;
import gregtech.common.metatileentities.electric.MetaTileEntitySingleTurbine;
import gregtech.common.metatileentities.electric.MetaTileEntityTransformer;
import gregtech.common.metatileentities.electric.MetaTileEntityWorldAccelerator;
import gregtech.common.metatileentities.electric.SimpleMachineMetaTileEntityResizable;
import gregtech.common.metatileentities.multi.BoilerType;
import gregtech.common.metatileentities.multi.MetaTileEntityCokeOven;
import gregtech.common.metatileentities.multi.MetaTileEntityCokeOvenHatch;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;
import gregtech.common.metatileentities.multi.MetaTileEntityMultiblockTank;
import gregtech.common.metatileentities.multi.MetaTileEntityPrimitiveBlastFurnace;
import gregtech.common.metatileentities.multi.MetaTileEntityPrimitiveWaterPump;
import gregtech.common.metatileentities.multi.MetaTileEntityPumpHatch;
import gregtech.common.metatileentities.multi.MetaTileEntityTankValve;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityActiveTransformer;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityAssemblyLine;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityCleanroom;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityCrackingUnit;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDataBank;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDistillationTower;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityElectricBlastFurnace;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityFluidDrill;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityFusionReactor;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityHPCA;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityImplosionCompressor;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityLargeChemicalReactor;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityLargeMiner;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityMultiSmelter;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityNetworkSwitch;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityPowerSubstation;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityProcessingArray;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityPyrolyseOven;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityResearchStation;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityVacuumFreezer;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityCentralMonitor;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeCombustionEngine;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityAutoMaintenanceHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityCleaningMaintenanceHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityComputationHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityDataAccessHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityEnergyHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityLaserHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMachineHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMaintenanceHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMufflerHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityObjectHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityOpticalDataHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityPassthroughHatchFluid;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityPassthroughHatchItem;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityReservoirHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityRotorHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntitySubstationEnergyHatch;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEInputBus;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEInputHatch;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEOutputBus;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEOutputHatch;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEStockingBus;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEStockingHatch;
import gregtech.common.metatileentities.multi.multiblockpart.hpca.MetaTileEntityHPCABridge;
import gregtech.common.metatileentities.multi.multiblockpart.hpca.MetaTileEntityHPCAComputation;
import gregtech.common.metatileentities.multi.multiblockpart.hpca.MetaTileEntityHPCACooler;
import gregtech.common.metatileentities.multi.multiblockpart.hpca.MetaTileEntityHPCAEmpty;
import gregtech.common.metatileentities.multi.steam.MetaTileEntitySteamGrinder;
import gregtech.common.metatileentities.multi.steam.MetaTileEntitySteamOven;
import gregtech.common.metatileentities.primitive.MetaTileEntityCharcoalPileIgniter;
import gregtech.common.metatileentities.steam.SteamAlloySmelter;
import gregtech.common.metatileentities.steam.SteamCompressor;
import gregtech.common.metatileentities.steam.SteamExtractor;
import gregtech.common.metatileentities.steam.SteamFurnace;
import gregtech.common.metatileentities.steam.SteamHammer;
import gregtech.common.metatileentities.steam.SteamMacerator;
import gregtech.common.metatileentities.steam.SteamMiner;
import gregtech.common.metatileentities.steam.SteamRockBreaker;
import gregtech.common.metatileentities.steam.boiler.SteamCoalBoiler;
import gregtech.common.metatileentities.steam.boiler.SteamLavaBoiler;
import gregtech.common.metatileentities.steam.boiler.SteamSolarBoiler;
import gregtech.common.metatileentities.steam.multiblockpart.MetaTileEntitySteamHatch;
import gregtech.common.metatileentities.steam.multiblockpart.MetaTileEntitySteamItemBus;
import gregtech.common.metatileentities.storage.MetaTileEntityBuffer;
import gregtech.common.metatileentities.storage.MetaTileEntityCrate;
import gregtech.common.metatileentities.storage.MetaTileEntityCreativeChest;
import gregtech.common.metatileentities.storage.MetaTileEntityCreativeEnergy;
import gregtech.common.metatileentities.storage.MetaTileEntityCreativeTank;
import gregtech.common.metatileentities.storage.MetaTileEntityDrum;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumExtender;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumProxy;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumStorageController;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;
import gregtech.common.metatileentities.storage.MetaTileEntityWorkbench;
import gregtech.common.pipelike.fluidpipe.longdistance.MetaTileEntityLDFluidEndpoint;
import gregtech.common.pipelike.itempipe.longdistance.MetaTileEntityLDItemEndpoint;

import static gregtech.api.util.GTUtility.gregtechId;

final class MetaTileEntityRegistration {

    private MetaTileEntityRegistration() {}

    static void register() {
        // Singleblocks: 1-9999
        singleblocks();

        // Multiblocks: 10000-10999
        multiblocks();

        // Multiblock parts: 11000-12999
        multiblockParts();
    }

    /**
     * 1-9999 (incl)
     */
    private static void singleblocks() {
        // Steam Machines: 1-49
        MetaTileEntities.STEAM_BOILER_COAL_BRONZE = MetaTileEntities.registerMetaTileEntity(1,
                new SteamCoalBoiler(gregtechId("steam_boiler_coal_bronze"), false));
        MetaTileEntities.STEAM_BOILER_COAL_STEEL = MetaTileEntities.registerMetaTileEntity(2,
                new SteamCoalBoiler(gregtechId("steam_boiler_coal_steel"), true));

        MetaTileEntities.STEAM_BOILER_SOLAR_BRONZE = MetaTileEntities.registerMetaTileEntity(3,
                new SteamSolarBoiler(gregtechId("steam_boiler_solar_bronze"), false));
        MetaTileEntities.STEAM_BOILER_SOLAR_STEEL = MetaTileEntities.registerMetaTileEntity(4,
                new SteamSolarBoiler(gregtechId("steam_boiler_solar_steel"), true));

        MetaTileEntities.STEAM_BOILER_LAVA_BRONZE = MetaTileEntities.registerMetaTileEntity(5,
                new SteamLavaBoiler(gregtechId("steam_boiler_lava_bronze"), false));
        MetaTileEntities.STEAM_BOILER_LAVA_STEEL = MetaTileEntities.registerMetaTileEntity(6,
                new SteamLavaBoiler(gregtechId("steam_boiler_lava_steel"), true));

        MetaTileEntities.STEAM_EXTRACTOR_BRONZE = MetaTileEntities.registerMetaTileEntity(7,
                new SteamExtractor(gregtechId("steam_extractor_bronze"), false));
        MetaTileEntities.STEAM_EXTRACTOR_STEEL = MetaTileEntities.registerMetaTileEntity(8,
                new SteamExtractor(gregtechId("steam_extractor_steel"), true));

        MetaTileEntities.STEAM_MACERATOR_BRONZE = MetaTileEntities.registerMetaTileEntity(9,
                new SteamMacerator(gregtechId("steam_macerator_bronze"), false));
        MetaTileEntities.STEAM_MACERATOR_STEEL = MetaTileEntities.registerMetaTileEntity(10,
                new SteamMacerator(gregtechId("steam_macerator_steel"), true));

        MetaTileEntities.STEAM_COMPRESSOR_BRONZE = MetaTileEntities.registerMetaTileEntity(11,
                new SteamCompressor(gregtechId("steam_compressor_bronze"), false));
        MetaTileEntities.STEAM_COMPRESSOR_STEEL = MetaTileEntities.registerMetaTileEntity(12,
                new SteamCompressor(gregtechId("steam_compressor_steel"), true));

        MetaTileEntities.STEAM_HAMMER_BRONZE = MetaTileEntities.registerMetaTileEntity(13,
                new SteamHammer(gregtechId("steam_hammer_bronze"), false));
        MetaTileEntities.STEAM_HAMMER_STEEL = MetaTileEntities.registerMetaTileEntity(14,
                new SteamHammer(gregtechId("steam_hammer_steel"), true));

        MetaTileEntities.STEAM_FURNACE_BRONZE = MetaTileEntities.registerMetaTileEntity(15,
                new SteamFurnace(gregtechId("steam_furnace_bronze"), false));
        MetaTileEntities.STEAM_FURNACE_STEEL = MetaTileEntities.registerMetaTileEntity(16,
                new SteamFurnace(gregtechId("steam_furnace_steel"), true));

        MetaTileEntities.STEAM_ALLOY_SMELTER_BRONZE = MetaTileEntities.registerMetaTileEntity(17,
                new SteamAlloySmelter(gregtechId("steam_alloy_smelter_bronze"), false));
        MetaTileEntities.STEAM_ALLOY_SMELTER_STEEL = MetaTileEntities.registerMetaTileEntity(18,
                new SteamAlloySmelter(gregtechId("steam_alloy_smelter_steel"), true));

        MetaTileEntities.STEAM_ROCK_BREAKER_BRONZE = MetaTileEntities.registerMetaTileEntity(19,
                new SteamRockBreaker(gregtechId("steam_rock_breaker_bronze"), false));
        MetaTileEntities.STEAM_ROCK_BREAKER_STEEL = MetaTileEntities.registerMetaTileEntity(20,
                new SteamRockBreaker(gregtechId("steam_rock_breaker_steel"), true));

        MetaTileEntities.STEAM_MINER = MetaTileEntities.registerMetaTileEntity(21,
                new SteamMiner(gregtechId("steam_miner"), 320, 4, 0));

        // Basic single block machines: 50-919

        // Electric Furnace, IDs 50-64
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.ELECTRIC_FURNACE, 50, "electric_furnace",
                RecipeMaps.FURNACE_RECIPES,
                Textures.ELECTRIC_FURNACE_OVERLAY, true);

        // Macerator, IDs 65-79
        MetaTileEntities.registerMetaTileEntities(MetaTileEntities.MACERATOR, 65, "macerator",
                (tier, voltageName) -> new SimpleMachineMetaTileEntityResizable(
                        gregtechId(String.format("%s.%s", "macerator", voltageName)),
                        RecipeMaps.MACERATOR_RECIPES,
                        -1,
                        switch (tier) {
                        case 1, 2 -> 1;
                        case 3 -> 3;
                        default -> 4;
                        },
                        tier <= GTValues.MV ? Textures.MACERATOR_OVERLAY : Textures.PULVERIZER_OVERLAY,
                        tier,
                        true,
                        GTUtility.defaultTankSizeFunction,
                        VanillaParticleEffects.TOP_SMOKE_SMALL, null));

        // Alloy Smelter, IDs 80-94
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.ALLOY_SMELTER, 80, "alloy_smelter",
                RecipeMaps.ALLOY_SMELTER_RECIPES,
                Textures.ALLOY_SMELTER_OVERLAY, true);

        // Arc Furnace, IDs 95-109
        MetaTileEntities.registerMetaTileEntities(MetaTileEntities.ARC_FURNACE, 95, "arc_furnace",
                (tier, voltageName) -> new SimpleMachineMetaTileEntityResizable(
                        gregtechId(String.format("%s.%s", "arc_furnace", voltageName)),
                        RecipeMaps.ARC_FURNACE_RECIPES,
                        -1,
                        tier >= GTValues.EV ? 9 : 4,
                        Textures.ARC_FURNACE_OVERLAY,
                        tier,
                        false,
                        GTUtility.hvCappedTankSizeFunction));

        // Assembler, IDs 110-124
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.ASSEMBLER, 110, "assembler",
                RecipeMaps.ASSEMBLER_RECIPES,
                Textures.ASSEMBLER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Autoclave, IDs 125-139
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.AUTOCLAVE, 125, "autoclave",
                RecipeMaps.AUTOCLAVE_RECIPES,
                Textures.AUTOCLAVE_OVERLAY, false, GTUtility.hvCappedTankSizeFunction);

        // Bender, IDs 140-154
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.BENDER, 140, "bender", RecipeMaps.BENDER_RECIPES,
                Textures.BENDER_OVERLAY, true);

        // Brewery, IDs 155-169
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.BREWERY, 155, "brewery",
                RecipeMaps.BREWING_RECIPES, Textures.BREWERY_OVERLAY,
                true, GTUtility.hvCappedTankSizeFunction);

        // Canner, IDs 170-184
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.CANNER, 170, "canner", RecipeMaps.CANNER_RECIPES,
                Textures.CANNER_OVERLAY, true);

        // Centrifuge, IDs 185-199
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.CENTRIFUGE, 185, "centrifuge",
                RecipeMaps.CENTRIFUGE_RECIPES,
                Textures.CENTRIFUGE_OVERLAY, false, GTUtility.largeTankSizeFunction);

        // Chemical Bath, IDs 200-214
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.CHEMICAL_BATH, 200, "chemical_bath",
                RecipeMaps.CHEMICAL_BATH_RECIPES,
                Textures.CHEMICAL_BATH_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Chemical Reactor, IDs 215-229
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.CHEMICAL_REACTOR, 215, "chemical_reactor",
                RecipeMaps.CHEMICAL_RECIPES,
                Textures.CHEMICAL_REACTOR_OVERLAY, true, tier -> 16000);

        // Compressor, IDs 230-244
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.COMPRESSOR, 230, "compressor",
                RecipeMaps.COMPRESSOR_RECIPES,
                Textures.COMPRESSOR_OVERLAY, true);

        // Cutter, IDs 245-259
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.CUTTER, 245, "cutter", RecipeMaps.CUTTER_RECIPES,
                Textures.CUTTER_OVERLAY, true);

        // Distillery, IDs 260-274
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.DISTILLERY, 260, "distillery",
                RecipeMaps.DISTILLERY_RECIPES,
                Textures.DISTILLERY_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Electrolyzer, IDs 275-289
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.ELECTROLYZER, 275, "electrolyzer",
                RecipeMaps.ELECTROLYZER_RECIPES,
                Textures.ELECTROLYZER_OVERLAY, false, GTUtility.largeTankSizeFunction);

        // Electromagnetic Separator, IDs 290-304
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.ELECTROMAGNETIC_SEPARATOR, 290,
                "electromagnetic_separator",
                RecipeMaps.ELECTROMAGNETIC_SEPARATOR_RECIPES, Textures.ELECTROMAGNETIC_SEPARATOR_OVERLAY, true);

        // Extractor, IDs 305-319
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.EXTRACTOR, 305, "extractor",
                RecipeMaps.EXTRACTOR_RECIPES,
                Textures.EXTRACTOR_OVERLAY, true);

        // Extruder, IDs 320-334
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.EXTRUDER, 320, "extruder",
                RecipeMaps.EXTRUDER_RECIPES, Textures.EXTRUDER_OVERLAY,
                true);

        // Fermenter, IDs 335-349
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.FERMENTER, 335, "fermenter",
                RecipeMaps.FERMENTING_RECIPES,
                Textures.FERMENTER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // TODO Replication system
        // Mass Fabricator, IDs 350-364
        // registerSimpleMetaTileEntity(MASS_FABRICATOR, 350, "mass_fabricator", RecipeMaps.MASS_FABRICATOR_RECIPES,
        // Textures.MASS_FABRICATOR_OVERLAY, true);

        // TODO Should anonymously override SimpleMachineMetaTileEntity#getCircuitSlotOverlay() to display the data
        // stick overlay
        // Replicator, IDs 365-379
        // registerSimpleMetaTileEntity(REPLICATOR, 365, "replicator", RecipeMaps.REPLICATOR_RECIPES,
        // Textures.REPLICATOR_OVERLAY, true);

        // Fluid Heater, IDs 380-394
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.FLUID_HEATER, 380, "fluid_heater",
                RecipeMaps.FLUID_HEATER_RECIPES,
                Textures.FLUID_HEATER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Fluid Solidifier, IDs 395-409
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.FLUID_SOLIDIFIER, 395, "fluid_solidifier",
                RecipeMaps.FLUID_SOLIDFICATION_RECIPES,
                Textures.FLUID_SOLIDIFIER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Forge Hammer, IDs 410-424
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.FORGE_HAMMER, 410, "forge_hammer",
                RecipeMaps.FORGE_HAMMER_RECIPES,
                Textures.FORGE_HAMMER_OVERLAY, true);

        // Forming Press, IDs 425-439
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.FORMING_PRESS, 425, "forming_press",
                RecipeMaps.FORMING_PRESS_RECIPES,
                Textures.FORMING_PRESS_OVERLAY, true);

        // Lathe, IDs 440-454
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.LATHE, 440, "lathe", RecipeMaps.LATHE_RECIPES,
                Textures.LATHE_OVERLAY, true);

        // Scanner, IDs 455-469
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.SCANNER, 455, "scanner",
                RecipeMaps.SCANNER_RECIPES, Textures.SCANNER_OVERLAY,
                true);

        // Mixer, IDs 470-484
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.MIXER, 470, "mixer", RecipeMaps.MIXER_RECIPES,
                Textures.MIXER_OVERLAY, false,
                GTUtility.hvCappedTankSizeFunction);

        // Ore Washer, IDs 485-499
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.ORE_WASHER, 485, "ore_washer",
                RecipeMaps.ORE_WASHER_RECIPES,
                Textures.ORE_WASHER_OVERLAY, true);

        // Packer, IDs 500-514
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.PACKER, 500, "packer", RecipeMaps.PACKER_RECIPES,
                Textures.PACKER_OVERLAY, true);

        // FREE, IDs 515-529

        // Gas Collectors, IDs 530-544
        MetaTileEntities.registerMetaTileEntities(MetaTileEntities.GAS_COLLECTOR, 530, "gas_collector",
                (tier, voltageName) -> new MetaTileEntityGasCollector(
                        gregtechId(String.format("%s.%s", "gas_collector", voltageName)),
                        RecipeMaps.GAS_COLLECTOR_RECIPES, Textures.GAS_COLLECTOR_OVERLAY, tier, false,
                        GTUtility.largeTankSizeFunction));
        // Polarizer, IDs 545-559
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.POLARIZER, 545, "polarizer",
                RecipeMaps.POLARIZER_RECIPES,
                Textures.POLARIZER_OVERLAY, true);

        // Laser Engraver, IDs 560-574
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.LASER_ENGRAVER, 560, "laser_engraver",
                RecipeMaps.LASER_ENGRAVER_RECIPES,
                Textures.LASER_ENGRAVER_OVERLAY, true);

        // Sifter, IDs 575-589
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.SIFTER, 575, "sifter", RecipeMaps.SIFTER_RECIPES,
                Textures.SIFTER_OVERLAY, true);

        // FREE, IDs 590-604

        // Thermal Centrifuge, IDs 605-619
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.THERMAL_CENTRIFUGE, 605, "thermal_centrifuge",
                RecipeMaps.THERMAL_CENTRIFUGE_RECIPES, Textures.THERMAL_CENTRIFUGE_OVERLAY, true);

        // Wire Mill, IDs 620-634
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.WIREMILL, 620, "wiremill",
                RecipeMaps.WIREMILL_RECIPES, Textures.WIREMILL_OVERLAY,
                true);

        // Circuit Assembler, IDs 650-664
        MetaTileEntities.registerSimpleMetaTileEntity(MetaTileEntities.CIRCUIT_ASSEMBLER, 635, "circuit_assembler",
                RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES,
                Textures.CIRCUIT_ASSEMBLER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Rock Breaker, IDs 665-679
        MetaTileEntities.registerMetaTileEntities(MetaTileEntities.ROCK_BREAKER, 665, "rock_breaker",
                (tier, voltageName) -> new MetaTileEntityRockBreaker(
                        gregtechId(String.format("%s.%s", "rock_breaker", voltageName)),
                        RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, tier));

        // Other single block machines

        // Chunk Miner, IDs 920-934

        MetaTileEntities.MINER[0] = MetaTileEntities.registerMetaTileEntity(920,
                new MetaTileEntityMiner(gregtechId("miner.lv"), 1, 160, 8, 1));
        MetaTileEntities.MINER[1] = MetaTileEntities.registerMetaTileEntity(921,
                new MetaTileEntityMiner(gregtechId("miner.mv"), 2, 80, 16, 2));
        MetaTileEntities.MINER[2] = MetaTileEntities.registerMetaTileEntity(922,
                new MetaTileEntityMiner(gregtechId("miner.hv"), 3, 40, 24, 3));

        // Diesel Generator, IDs 935-949
        MetaTileEntities.COMBUSTION_GENERATOR[0] = MetaTileEntities.registerMetaTileEntity(935,
                new MetaTileEntitySingleCombustion(gregtechId("combustion_generator.lv"),
                        RecipeMaps.COMBUSTION_GENERATOR_FUELS, Textures.COMBUSTION_GENERATOR_OVERLAY, 1,
                        GTUtility.genericGeneratorTankSizeFunction));
        MetaTileEntities.COMBUSTION_GENERATOR[1] = MetaTileEntities.registerMetaTileEntity(936,
                new MetaTileEntitySingleCombustion(gregtechId("combustion_generator.mv"),
                        RecipeMaps.COMBUSTION_GENERATOR_FUELS, Textures.COMBUSTION_GENERATOR_OVERLAY, 2,
                        GTUtility.genericGeneratorTankSizeFunction));
        MetaTileEntities.COMBUSTION_GENERATOR[2] = MetaTileEntities.registerMetaTileEntity(937,
                new MetaTileEntitySingleCombustion(gregtechId("combustion_generator.hv"),
                        RecipeMaps.COMBUSTION_GENERATOR_FUELS, Textures.COMBUSTION_GENERATOR_OVERLAY, 3,
                        GTUtility.genericGeneratorTankSizeFunction));

        // Steam Turbine, IDs 950-964
        MetaTileEntities.STEAM_TURBINE[0] = MetaTileEntities.registerMetaTileEntity(950,
                new MetaTileEntitySingleTurbine(gregtechId("steam_turbine.lv"), RecipeMaps.STEAM_TURBINE_FUELS,
                        Textures.STEAM_TURBINE_OVERLAY, 1, GTUtility.steamGeneratorTankSizeFunction));
        MetaTileEntities.STEAM_TURBINE[1] = MetaTileEntities.registerMetaTileEntity(951,
                new MetaTileEntitySingleTurbine(gregtechId("steam_turbine.mv"), RecipeMaps.STEAM_TURBINE_FUELS,
                        Textures.STEAM_TURBINE_OVERLAY, 2, GTUtility.steamGeneratorTankSizeFunction));
        MetaTileEntities.STEAM_TURBINE[2] = MetaTileEntities.registerMetaTileEntity(952,
                new MetaTileEntitySingleTurbine(gregtechId("steam_turbine.hv"), RecipeMaps.STEAM_TURBINE_FUELS,
                        Textures.STEAM_TURBINE_OVERLAY, 3, GTUtility.steamGeneratorTankSizeFunction));

        // Gas Turbine, IDs 965-979
        MetaTileEntities.GAS_TURBINE[0] = MetaTileEntities.registerMetaTileEntity(965,
                new MetaTileEntitySingleTurbine(gregtechId("gas_turbine.lv"), RecipeMaps.GAS_TURBINE_FUELS,
                        Textures.GAS_TURBINE_OVERLAY, 1, GTUtility.genericGeneratorTankSizeFunction));
        MetaTileEntities.GAS_TURBINE[1] = MetaTileEntities.registerMetaTileEntity(966,
                new MetaTileEntitySingleTurbine(gregtechId("gas_turbine.mv"), RecipeMaps.GAS_TURBINE_FUELS,
                        Textures.GAS_TURBINE_OVERLAY, 2, GTUtility.genericGeneratorTankSizeFunction));
        MetaTileEntities.GAS_TURBINE[2] = MetaTileEntities.registerMetaTileEntity(967,
                new MetaTileEntitySingleTurbine(gregtechId("gas_turbine.hv"), RecipeMaps.GAS_TURBINE_FUELS,
                        Textures.GAS_TURBINE_OVERLAY, 3, GTUtility.genericGeneratorTankSizeFunction));

        // Item Collector, IDs 980-984
        MetaTileEntities.ITEM_COLLECTOR[0] = MetaTileEntities.registerMetaTileEntity(980,
                new MetaTileEntityItemCollector(gregtechId("item_collector.lv"), 1, 8));
        MetaTileEntities.ITEM_COLLECTOR[1] = MetaTileEntities.registerMetaTileEntity(981,
                new MetaTileEntityItemCollector(gregtechId("item_collector.mv"), 2, 16));
        MetaTileEntities.ITEM_COLLECTOR[2] = MetaTileEntities.registerMetaTileEntity(982,
                new MetaTileEntityItemCollector(gregtechId("item_collector.hv"), 3, 32));
        MetaTileEntities.ITEM_COLLECTOR[3] = MetaTileEntities.registerMetaTileEntity(983,
                new MetaTileEntityItemCollector(gregtechId("item_collector.ev"), 4, 64));

        // Hulls, IDs 985-999
        int endPos = GregTechAPI.isHighTier() ? MetaTileEntities.HULL.length : GTValues.UHV + 1;
        for (int i = 0; i < endPos; i++) {
            MetaTileEntities.HULL[i] = new MetaTileEntityHull(gregtechId("hull." + GTValues.VN[i].toLowerCase()), i);
            MetaTileEntities.registerMetaTileEntity(985 + i, MetaTileEntities.HULL[i]);
        }

        // Misc Machines: 1000-2000

        // Transformer, IDs 1000-1014
        // Hi-Amp Transformer, IDs 1015-1029
        // Power Transformer, IDs 1030-1044
        endPos = GregTechAPI.isHighTier() ? MetaTileEntities.TRANSFORMER.length - 1 : GTValues.UV;
        for (int i = 0; i <= endPos; i++) {
            // 1A <-> 4A
            MetaTileEntityTransformer transformer = new MetaTileEntityTransformer(
                    gregtechId("transformer." + GTValues.VN[i].toLowerCase()), i);
            MetaTileEntities.TRANSFORMER[i] = MetaTileEntities.registerMetaTileEntity(1000 + (i), transformer);
            // 2A <-> 8A and 4A <-> 16A
            MetaTileEntityTransformer adjustableTransformer = new MetaTileEntityTransformer(
                    gregtechId("transformer.hi_amp." + GTValues.VN[i].toLowerCase()), i, 2, 4);
            MetaTileEntities.HI_AMP_TRANSFORMER[i] = MetaTileEntities.registerMetaTileEntity(1015 + i,
                    adjustableTransformer);
            // 16A <-> 64A (can do other amperages because of legacy compat)
            adjustableTransformer = new MetaTileEntityTransformer(
                    gregtechId("transformer.adjustable." + GTValues.VN[i].toLowerCase()), i, 1, 2, 4, 16);
            MetaTileEntities.POWER_TRANSFORMER[i] = MetaTileEntities.registerMetaTileEntity(1030 + (i),
                    adjustableTransformer);
        }

        // Free range: 1045-1074

        // Quantum Storage Network 1075-1089
        MetaTileEntities.QUANTUM_STORAGE_CONTROLLER = MetaTileEntities.registerMetaTileEntity(1075,
                new MetaTileEntityQuantumStorageController(gregtechId("quantum_storage_controller")));
        MetaTileEntities.QUANTUM_STORAGE_PROXY = MetaTileEntities.registerMetaTileEntity(1076,
                new MetaTileEntityQuantumProxy(gregtechId("quantum_storage_proxy")));
        MetaTileEntities.QUANTUM_STORAGE_EXTENDER = MetaTileEntities.registerMetaTileEntity(1077,
                new MetaTileEntityQuantumExtender(gregtechId("quantum_storage_extender")));

        // Free range: 1090-1299

        // Diode, IDs 1300-1314
        endPos = GregTechAPI.isHighTier() ? MetaTileEntities.DIODES.length - 1 : GTValues.UHV + 1;
        for (int i = 0; i < endPos; i++) {
            String diodeId = "diode." + GTValues.VN[i].toLowerCase();
            MetaTileEntityDiode diode = new MetaTileEntityDiode(gregtechId(diodeId), i, 16);
            MetaTileEntities.DIODES[i] = MetaTileEntities.registerMetaTileEntity(1300 + i, diode);
        }

        // Battery Buffer, IDs 1315-1369
        endPos = GregTechAPI.isHighTier() ? MetaTileEntities.BATTERY_BUFFER[0].length : GTValues.UHV + 1;
        MetaTileEntities.BATTERY_BUFFER[0] = new MetaTileEntityBatteryBuffer[endPos];
        MetaTileEntities.BATTERY_BUFFER[1] = new MetaTileEntityBatteryBuffer[endPos];
        MetaTileEntities.BATTERY_BUFFER[2] = new MetaTileEntityBatteryBuffer[endPos];
        for (int i = 0; i < endPos; i++) {
            String bufferIdBase = "battery_buffer." + GTValues.VN[i].toLowerCase() + ".";
            MetaTileEntities.BATTERY_BUFFER[0][i] = MetaTileEntities.registerMetaTileEntity(1315 + i,
                    new MetaTileEntityBatteryBuffer(gregtechId(bufferIdBase + 4), i, 4));
            MetaTileEntities.BATTERY_BUFFER[1][i] = MetaTileEntities.registerMetaTileEntity(1330 + i,
                    new MetaTileEntityBatteryBuffer(gregtechId(bufferIdBase + 8), i, 8));
            MetaTileEntities.BATTERY_BUFFER[2][i] = MetaTileEntities.registerMetaTileEntity(1345 + i,
                    new MetaTileEntityBatteryBuffer(gregtechId(bufferIdBase + 16), i, 16));
        }

        // Free range: 1370-1374

        // Charger, IDs 1375-1089
        endPos = GregTechAPI.isHighTier() ? MetaTileEntities.CHARGER.length : GTValues.UHV + 1;
        for (int i = 0; i < endPos; i++) {
            String chargerId = "charger." + GTValues.VN[i].toLowerCase();
            MetaTileEntityCharger charger = new MetaTileEntityCharger(gregtechId(chargerId), i, 4);
            MetaTileEntities.CHARGER[i] = MetaTileEntities.registerMetaTileEntity(1375 + i, charger);
        }

        // World Accelerators, IDs 1390-1404
        if (ConfigHolder.machines.enableWorldAccelerators) {
            endPos = GTValues.UV + 1;
            for (int i = 1; i < endPos; i++) {
                MetaTileEntities.WORLD_ACCELERATOR[i] = MetaTileEntities.registerMetaTileEntity(1390 + i,
                        new MetaTileEntityWorldAccelerator(
                                gregtechId("world_accelerator." + GTValues.VN[i].toLowerCase()), i));
            }
        }

        // Free range: 1405-1509

        // Buffers, IDs 1510-1512
        MetaTileEntities.BUFFER[0] = MetaTileEntities.registerMetaTileEntity(1510,
                new MetaTileEntityBuffer(gregtechId("buffer.lv"), 1));
        MetaTileEntities.BUFFER[1] = MetaTileEntities.registerMetaTileEntity(1511,
                new MetaTileEntityBuffer(gregtechId("buffer.mv"), 2));
        MetaTileEntities.BUFFER[2] = MetaTileEntities.registerMetaTileEntity(1512,
                new MetaTileEntityBuffer(gregtechId("buffer.hv"), 3));

        // Free Range: 1513-1514

        // Fishers, IDs 1515-1529
        MetaTileEntities.FISHER[0] = MetaTileEntities.registerMetaTileEntity(1515,
                new MetaTileEntityFisher(gregtechId("fisher.lv"), 1));
        MetaTileEntities.FISHER[1] = MetaTileEntities.registerMetaTileEntity(1516,
                new MetaTileEntityFisher(gregtechId("fisher.mv"), 2));
        MetaTileEntities.FISHER[2] = MetaTileEntities.registerMetaTileEntity(1517,
                new MetaTileEntityFisher(gregtechId("fisher.hv"), 3));
        MetaTileEntities.FISHER[3] = MetaTileEntities.registerMetaTileEntity(1518,
                new MetaTileEntityFisher(gregtechId("fisher.ev"), 4));

        // Pumps, IDs 1530-1544
        MetaTileEntities.PUMP[0] = MetaTileEntities.registerMetaTileEntity(1530,
                new MetaTileEntityPump(gregtechId("pump.lv"), 1));
        MetaTileEntities.PUMP[1] = MetaTileEntities.registerMetaTileEntity(1531,
                new MetaTileEntityPump(gregtechId("pump.mv"), 2));
        MetaTileEntities.PUMP[2] = MetaTileEntities.registerMetaTileEntity(1532,
                new MetaTileEntityPump(gregtechId("pump.hv"), 3));
        MetaTileEntities.PUMP[3] = MetaTileEntities.registerMetaTileEntity(1533,
                new MetaTileEntityPump(gregtechId("pump.ev"), 4));

        // Super / Quantum Chests, IDs 1560-1574
        for (int i = 0; i < GTValues.IV; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            MetaTileEntities.QUANTUM_CHEST[i] = new MetaTileEntityQuantumChest(gregtechId("super_chest." + voltageName),
                    i + 1,
                    4000000L * (int) Math.pow(2, i));
            MetaTileEntities.registerMetaTileEntity(1560 + i, MetaTileEntities.QUANTUM_CHEST[i]);
        }

        for (int i = GTValues.IV; i < MetaTileEntities.QUANTUM_CHEST.length; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            long capacity = i == GTValues.UHV ? Integer.MAX_VALUE : 4000000L * (int) Math.pow(2, i);
            MetaTileEntities.QUANTUM_CHEST[i] = new MetaTileEntityQuantumChest(
                    gregtechId("quantum_chest." + voltageName), i, capacity);
            MetaTileEntities.registerMetaTileEntity(1565 + i, MetaTileEntities.QUANTUM_CHEST[i]);
        }

        // Super / Quantum Tanks, IDs 1575-1589
        for (int i = 0; i < GTValues.IV; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            MetaTileEntities.QUANTUM_TANK[i] = new MetaTileEntityQuantumTank(gregtechId("super_tank." + voltageName),
                    i + 1,
                    4000000 * (int) Math.pow(2, i));
            MetaTileEntities.registerMetaTileEntity(1575 + i, MetaTileEntities.QUANTUM_TANK[i]);
        }

        for (int i = GTValues.IV; i < MetaTileEntities.QUANTUM_TANK.length; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            int capacity = i == GTValues.UHV ? Integer.MAX_VALUE : 4000000 * (int) Math.pow(2, i);
            MetaTileEntities.QUANTUM_TANK[i] = new MetaTileEntityQuantumTank(gregtechId("quantum_tank." + voltageName),
                    i, capacity);
            MetaTileEntities.registerMetaTileEntity(1580 + i, MetaTileEntities.QUANTUM_TANK[i]);
        }

        // Block Breakers, IDs 1590-1594
        for (int i = 0; i < MetaTileEntities.BLOCK_BREAKER.length; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            MetaTileEntities.BLOCK_BREAKER[i] = new MetaTileEntityBlockBreaker(
                    gregtechId("block_breaker." + voltageName), i + 1);
            MetaTileEntities.registerMetaTileEntity(1590 + i, MetaTileEntities.BLOCK_BREAKER[i]);
        }

        // Drums, IDs 1610-1624
        MetaTileEntities.WOODEN_DRUM = MetaTileEntities.registerMetaTileEntity(1610,
                new MetaTileEntityDrum(gregtechId("drum.wood"), Materials.Wood, 16000));
        MetaTileEntities.BRONZE_DRUM = MetaTileEntities.registerMetaTileEntity(1611,
                new MetaTileEntityDrum(gregtechId("drum.bronze"), Materials.Bronze, 32000));
        MetaTileEntities.STEEL_DRUM = MetaTileEntities.registerMetaTileEntity(1612,
                new MetaTileEntityDrum(gregtechId("drum.steel"), Materials.Steel, 64000));
        MetaTileEntities.ALUMINIUM_DRUM = MetaTileEntities.registerMetaTileEntity(1613,
                new MetaTileEntityDrum(gregtechId("drum.aluminium"), Materials.Aluminium, 128000));
        MetaTileEntities.STAINLESS_STEEL_DRUM = MetaTileEntities.registerMetaTileEntity(1614,
                new MetaTileEntityDrum(gregtechId("drum.stainless_steel"), Materials.StainlessSteel, 256000));
        MetaTileEntities.TITANIUM_DRUM = MetaTileEntities.registerMetaTileEntity(1615,
                new MetaTileEntityDrum(gregtechId("drum.titanium"), Materials.Titanium, 512000));
        MetaTileEntities.TUNGSTENSTEEL_DRUM = MetaTileEntities.registerMetaTileEntity(1616,
                new MetaTileEntityDrum(gregtechId("drum.tungstensteel"), Materials.TungstenSteel, 1024000));
        MetaTileEntities.GOLD_DRUM = MetaTileEntities.registerMetaTileEntity(1617,
                new MetaTileEntityDrum(gregtechId("drum.gold"), Materials.Gold, 32000));

        // Crates, IDs 1625-1639
        MetaTileEntities.WOODEN_CRATE = MetaTileEntities.registerMetaTileEntity(1625,
                new MetaTileEntityCrate(gregtechId("crate.wood"), Materials.Wood, 27, 9));
        MetaTileEntities.BRONZE_CRATE = MetaTileEntities.registerMetaTileEntity(1626,
                new MetaTileEntityCrate(gregtechId("crate.bronze"), Materials.Bronze, 54, 9));
        MetaTileEntities.STEEL_CRATE = MetaTileEntities.registerMetaTileEntity(1627,
                new MetaTileEntityCrate(gregtechId("crate.steel"), Materials.Steel, 72, 9));
        MetaTileEntities.ALUMINIUM_CRATE = MetaTileEntities.registerMetaTileEntity(1628,
                new MetaTileEntityCrate(gregtechId("crate.aluminium"), Materials.Aluminium, 90, 10));
        MetaTileEntities.STAINLESS_STEEL_CRATE = MetaTileEntities.registerMetaTileEntity(1629,
                new MetaTileEntityCrate(gregtechId("crate.stainless_steel"), Materials.StainlessSteel, 108, 12));
        MetaTileEntities.TITANIUM_CRATE = MetaTileEntities.registerMetaTileEntity(1630,
                new MetaTileEntityCrate(gregtechId("crate.titanium"), Materials.Titanium, 126, 14));
        MetaTileEntities.TUNGSTENSTEEL_CRATE = MetaTileEntities.registerMetaTileEntity(1631,
                new MetaTileEntityCrate(gregtechId("crate.tungstensteel"), Materials.TungstenSteel, 144, 16));

        // Misc, IDs 1646-1999
        MetaTileEntities.WORKBENCH = MetaTileEntities.registerMetaTileEntity(1647,
                new MetaTileEntityWorkbench(gregtechId("workbench")));
        MetaTileEntities.PRIMITIVE_WATER_PUMP = MetaTileEntities.registerMetaTileEntity(1648,
                new MetaTileEntityPrimitiveWaterPump(gregtechId("primitive_water_pump")));

        MetaTileEntities.CREATIVE_ENERGY = MetaTileEntities.registerMetaTileEntity(1650,
                new MetaTileEntityCreativeEnergy());
        MetaTileEntities.CLIPBOARD_TILE = MetaTileEntities.registerMetaTileEntity(1666,
                new MetaTileEntityClipboard(gregtechId("clipboard")));

        MetaTileEntities.MONITOR_SCREEN = MetaTileEntities.registerMetaTileEntity(1667,
                new MetaTileEntityMonitorScreen(gregtechId("monitor_screen")));

        // Creative Chest and Tank, IDs 1668-1669
        MetaTileEntities.CREATIVE_CHEST = MetaTileEntities.registerMetaTileEntity(1668,
                new MetaTileEntityCreativeChest(gregtechId("creative_chest")));
        MetaTileEntities.CREATIVE_TANK = MetaTileEntities.registerMetaTileEntity(1669,
                new MetaTileEntityCreativeTank(gregtechId("creative_tank")));

        // Energy Converter, IDs 1670-1729
        endPos = GregTechAPI.isHighTier() ? MetaTileEntities.ENERGY_CONVERTER[0].length : GTValues.UHV + 1;
        int[] amps = { 1, 4, 8, 16 };
        for (int i = 0; i < endPos; i++) {
            for (int j = 0; j < amps.length; j++) {
                // Check to make sure this is a valid amount of power to be able to convert.
                // Tests if both:
                // - The maximum amount of EU/t of this converter can turn into FE without overflowing
                // - Max int FE/t can convert to the full amount of EU/t without being short
                // This is done because these ratios are configured separately.
                long eu = amps[j] * GTValues.V[i];
                long euToFe = FeCompat.toFeLong(eu, FeCompat.ratio(false));
                long feToEu = FeCompat.toEu(Integer.MAX_VALUE, FeCompat.ratio(true));
                if (euToFe > Integer.MAX_VALUE || feToEu < eu) continue;

                String id = "energy_converter." + GTValues.VN[i].toLowerCase() + "." + amps[j];
                MetaTileEntityConverter converter = new MetaTileEntityConverter(gregtechId(id), i, amps[j]);
                MetaTileEntities.ENERGY_CONVERTER[j][i] = MetaTileEntities.registerMetaTileEntity(1670 + j + i * 4,
                        converter);
            }
        }

        // Free Range: 1730-1748

        MetaTileEntities.LONG_DIST_ITEM_ENDPOINT = MetaTileEntities.registerMetaTileEntity(1749,
                new MetaTileEntityLDItemEndpoint(gregtechId("ld_item_endpoint")));
        MetaTileEntities.LONG_DIST_FLUID_ENDPOINT = MetaTileEntities.registerMetaTileEntity(1750,
                new MetaTileEntityLDFluidEndpoint(gregtechId("ld_fluid_endpoint")));

        // Alarm, ID 1751
        MetaTileEntities.ALARM = MetaTileEntities.registerMetaTileEntity(1751,
                new MetaTileEntityAlarm(gregtechId("alarm")));

        MetaTileEntities.MAGIC_ENERGY_ABSORBER = MetaTileEntities.registerMetaTileEntity(1752,
                new MetaTileEntityMagicEnergyAbsorber(gregtechId("magic_energy_absorber")));
    }

    /**
     * 10000-10999 (incl)
     */
    private static void multiblocks() {
        MetaTileEntities.PRIMITIVE_BLAST_FURNACE = MetaTileEntities.registerMetaTileEntity(10000,
                new MetaTileEntityPrimitiveBlastFurnace(gregtechId("primitive_blast_furnace.bronze")));
        MetaTileEntities.ELECTRIC_BLAST_FURNACE = MetaTileEntities.registerMetaTileEntity(10001,
                new MetaTileEntityElectricBlastFurnace(gregtechId("electric_blast_furnace")));
        MetaTileEntities.VACUUM_FREEZER = MetaTileEntities.registerMetaTileEntity(10002,
                new MetaTileEntityVacuumFreezer(gregtechId("vacuum_freezer")));
        MetaTileEntities.IMPLOSION_COMPRESSOR = MetaTileEntities.registerMetaTileEntity(10003,
                new MetaTileEntityImplosionCompressor(gregtechId("implosion_compressor")));
        MetaTileEntities.PYROLYSE_OVEN = MetaTileEntities.registerMetaTileEntity(10004,
                new MetaTileEntityPyrolyseOven(gregtechId("pyrolyse_oven")));
        MetaTileEntities.DISTILLATION_TOWER = MetaTileEntities.registerMetaTileEntity(10005,
                new MetaTileEntityDistillationTower(gregtechId("distillation_tower"), true));
        MetaTileEntities.MULTI_FURNACE = MetaTileEntities.registerMetaTileEntity(10006,
                new MetaTileEntityMultiSmelter(gregtechId("multi_furnace")));
        MetaTileEntities.LARGE_COMBUSTION_ENGINE = MetaTileEntities.registerMetaTileEntity(10007,
                new MetaTileEntityLargeCombustionEngine(gregtechId("large_combustion_engine"), GTValues.EV));
        MetaTileEntities.EXTREME_COMBUSTION_ENGINE = MetaTileEntities.registerMetaTileEntity(10008,
                new MetaTileEntityLargeCombustionEngine(gregtechId("extreme_combustion_engine"), GTValues.IV));
        MetaTileEntities.CRACKER = MetaTileEntities.registerMetaTileEntity(10009,
                new MetaTileEntityCrackingUnit(gregtechId("cracker")));

        MetaTileEntities.LARGE_STEAM_TURBINE = MetaTileEntities.registerMetaTileEntity(10010,
                new MetaTileEntityLargeTurbine(gregtechId("large_turbine.steam"), RecipeMaps.STEAM_TURBINE_FUELS, 3,
                        MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_TURBINE_CASING),
                        MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX),
                        Textures.SOLID_STEEL_CASING, false, Textures.LARGE_STEAM_TURBINE_OVERLAY));
        MetaTileEntities.LARGE_GAS_TURBINE = MetaTileEntities.registerMetaTileEntity(10011,
                new MetaTileEntityLargeTurbine(gregtechId("large_turbine.gas"),
                        RecipeMaps.GAS_TURBINE_FUELS, 4,
                        MetaBlocks.TURBINE_CASING
                                .getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_TURBINE_CASING),
                        MetaBlocks.TURBINE_CASING
                                .getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_STEEL_GEARBOX),
                        Textures.CLEAN_STAINLESS_STEEL_CASING, true, Textures.LARGE_GAS_TURBINE_OVERLAY));
        MetaTileEntities.LARGE_PLASMA_TURBINE = MetaTileEntities.registerMetaTileEntity(10012,
                new MetaTileEntityLargeTurbine(gregtechId("large_turbine.plasma"), RecipeMaps.PLASMA_GENERATOR_FUELS, 5,
                        MetaBlocks.TURBINE_CASING
                                .getState(BlockTurbineCasing.TurbineCasingType.TUNGSTENSTEEL_TURBINE_CASING),
                        MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TUNGSTENSTEEL_GEARBOX),
                        Textures.ROBUST_TUNGSTENSTEEL_CASING, false, Textures.LARGE_PLASMA_TURBINE_OVERLAY));

        MetaTileEntities.LARGE_BRONZE_BOILER = MetaTileEntities.registerMetaTileEntity(10013,
                new MetaTileEntityLargeBoiler(gregtechId("large_boiler.bronze"), BoilerType.BRONZE));
        MetaTileEntities.LARGE_STEEL_BOILER = MetaTileEntities.registerMetaTileEntity(10014,
                new MetaTileEntityLargeBoiler(gregtechId("large_boiler.steel"), BoilerType.STEEL));
        MetaTileEntities.LARGE_TITANIUM_BOILER = MetaTileEntities.registerMetaTileEntity(10015,
                new MetaTileEntityLargeBoiler(gregtechId("large_boiler.titanium"), BoilerType.TITANIUM));
        MetaTileEntities.LARGE_TUNGSTENSTEEL_BOILER = MetaTileEntities.registerMetaTileEntity(10016,
                new MetaTileEntityLargeBoiler(gregtechId("large_boiler.tungstensteel"), BoilerType.TUNGSTENSTEEL));

        MetaTileEntities.COKE_OVEN = MetaTileEntities.registerMetaTileEntity(10017,
                new MetaTileEntityCokeOven(gregtechId("coke_oven")));
        MetaTileEntities.COKE_OVEN_HATCH = MetaTileEntities.registerMetaTileEntity(10018,
                new MetaTileEntityCokeOvenHatch(gregtechId("coke_oven_hatch")));

        MetaTileEntities.ASSEMBLY_LINE = MetaTileEntities.registerMetaTileEntity(10019,
                new MetaTileEntityAssemblyLine(gregtechId("assembly_line")));
        MetaTileEntities.FUSION_REACTOR[0] = MetaTileEntities.registerMetaTileEntity(10020,
                new MetaTileEntityFusionReactor(gregtechId("fusion_reactor.luv"), GTValues.LuV));
        MetaTileEntities.FUSION_REACTOR[1] = MetaTileEntities.registerMetaTileEntity(10021,
                new MetaTileEntityFusionReactor(gregtechId("fusion_reactor.zpm"), GTValues.ZPM));
        MetaTileEntities.FUSION_REACTOR[2] = MetaTileEntities.registerMetaTileEntity(10022,
                new MetaTileEntityFusionReactor(gregtechId("fusion_reactor.uv"), GTValues.UV));

        MetaTileEntities.LARGE_CHEMICAL_REACTOR = MetaTileEntities.registerMetaTileEntity(10023,
                new MetaTileEntityLargeChemicalReactor(gregtechId("large_chemical_reactor")));

        MetaTileEntities.STEAM_OVEN = MetaTileEntities.registerMetaTileEntity(10024,
                new MetaTileEntitySteamOven(gregtechId("steam_oven")));
        MetaTileEntities.STEAM_GRINDER = MetaTileEntities.registerMetaTileEntity(10025,
                new MetaTileEntitySteamGrinder(gregtechId("steam_grinder")));

        MetaTileEntities.BASIC_LARGE_MINER = MetaTileEntities.registerMetaTileEntity(10026,
                new MetaTileEntityLargeMiner(gregtechId("large_miner.ev"), GTValues.EV, 16, 3, 4, Materials.Steel, 8));
        MetaTileEntities.LARGE_MINER = MetaTileEntities.registerMetaTileEntity(10027,
                new MetaTileEntityLargeMiner(gregtechId("large_miner.iv"),
                        GTValues.IV, 4, 5, 5, Materials.Titanium, 16));
        MetaTileEntities.ADVANCED_LARGE_MINER = MetaTileEntities.registerMetaTileEntity(10028,
                new MetaTileEntityLargeMiner(gregtechId("large_miner.luv"),
                        GTValues.LuV, 1, 7, 6, Materials.TungstenSteel, 32));

        MetaTileEntities.CENTRAL_MONITOR = MetaTileEntities.registerMetaTileEntity(10029,
                new MetaTileEntityCentralMonitor(gregtechId("central_monitor")));

        MetaTileEntities.PROCESSING_ARRAY = MetaTileEntities.registerMetaTileEntity(10030,
                new MetaTileEntityProcessingArray(gregtechId("processing_array"), 0));
        MetaTileEntities.ADVANCED_PROCESSING_ARRAY = MetaTileEntities.registerMetaTileEntity(10031,
                new MetaTileEntityProcessingArray(gregtechId("advanced_processing_array"), 1));

        MetaTileEntities.BASIC_FLUID_DRILLING_RIG = MetaTileEntities.registerMetaTileEntity(10032,
                new MetaTileEntityFluidDrill(gregtechId("fluid_drilling_rig.mv"), 2));
        MetaTileEntities.FLUID_DRILLING_RIG = MetaTileEntities.registerMetaTileEntity(10033,
                new MetaTileEntityFluidDrill(gregtechId("fluid_drilling_rig.hv"), 3));
        MetaTileEntities.ADVANCED_FLUID_DRILLING_RIG = MetaTileEntities.registerMetaTileEntity(10034,
                new MetaTileEntityFluidDrill(gregtechId("fluid_drilling_rig.ev"), 4));

        MetaTileEntities.CLEANROOM = MetaTileEntities.registerMetaTileEntity(10035,
                new MetaTileEntityCleanroom(gregtechId("cleanroom")));

        MetaTileEntities.CHARCOAL_PILE_IGNITER = MetaTileEntities.registerMetaTileEntity(10036,
                new MetaTileEntityCharcoalPileIgniter(gregtechId("charcoal_pile")));

        MetaTileEntities.DATA_BANK = MetaTileEntities.registerMetaTileEntity(10037,
                new MetaTileEntityDataBank(gregtechId("data_bank")));
        MetaTileEntities.RESEARCH_STATION = MetaTileEntities.registerMetaTileEntity(10038,
                new MetaTileEntityResearchStation(gregtechId("research_station")));
        MetaTileEntities.HIGH_PERFORMANCE_COMPUTING_ARRAY = MetaTileEntities.registerMetaTileEntity(10039,
                new MetaTileEntityHPCA(gregtechId("high_performance_computing_array")));
        MetaTileEntities.NETWORK_SWITCH = MetaTileEntities.registerMetaTileEntity(10040,
                new MetaTileEntityNetworkSwitch(gregtechId("network_switch")));

        MetaTileEntities.POWER_SUBSTATION = MetaTileEntities.registerMetaTileEntity(10041,
                new MetaTileEntityPowerSubstation(gregtechId("power_substation")));
        MetaTileEntities.ACTIVE_TRANSFORMER = MetaTileEntities.registerMetaTileEntity(10042,
                new MetaTileEntityActiveTransformer(gregtechId("active_transformer")));

        MetaTileEntities.WOODEN_TANK = MetaTileEntities.registerMetaTileEntity(10043,
                new MetaTileEntityMultiblockTank(gregtechId("tank.wood"), false, 250 * 1000));
        MetaTileEntities.STEEL_TANK = MetaTileEntities.registerMetaTileEntity(10044,
                new MetaTileEntityMultiblockTank(gregtechId("tank.steel"), true, 1000 * 1000));
    }

    /**
     * 11000-12999 (incl)
     */
    private static void multiblockParts() {
        // Import Buses, IDs 11000-11014
        // Export Buses, IDs 11015-11029
        // Import Hatches, IDs 11030-11044
        // Export Hatches, IDs 11045-11059
        int endPos = GregTechAPI.isHighTier() ? MetaTileEntities.ITEM_IMPORT_BUS.length : GTValues.UHV + 1;
        for (int i = 0; i < endPos; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            MetaTileEntities.ITEM_IMPORT_BUS[i] = new MetaTileEntityItemBus(
                    gregtechId("item_bus.import." + voltageName), i, false);
            MetaTileEntities.ITEM_EXPORT_BUS[i] = new MetaTileEntityItemBus(
                    gregtechId("item_bus.export." + voltageName), i, true);
            MetaTileEntities.FLUID_IMPORT_HATCH[i] = new MetaTileEntityFluidHatch(
                    gregtechId("fluid_hatch.import." + voltageName), i,
                    false);
            MetaTileEntities.FLUID_EXPORT_HATCH[i] = new MetaTileEntityFluidHatch(
                    gregtechId("fluid_hatch.export." + voltageName), i,
                    true);

            MetaTileEntities.registerMetaTileEntity(11000 + i, MetaTileEntities.ITEM_IMPORT_BUS[i]);
            MetaTileEntities.registerMetaTileEntity(11015 + i, MetaTileEntities.ITEM_EXPORT_BUS[i]);
            MetaTileEntities.registerMetaTileEntity(11030 + i, MetaTileEntities.FLUID_IMPORT_HATCH[i]);
            MetaTileEntities.registerMetaTileEntity(11045 + i, MetaTileEntities.FLUID_EXPORT_HATCH[i]);
        }

        // quad and nonuple hatches, Ids 11060-11219
        // 4x Import hatch 11060-11074
        // 9x Import hatch 11075-11089
        // 4x Export hatch 11090-11204
        // 9x Export hatch 11205-11219
        endPos = GregTechAPI.isHighTier() ? MetaTileEntities.QUADRUPLE_IMPORT_HATCH.length : GTValues.UHV + 1;
        for (int i = GTValues.EV; i < endPos; i++) {
            String tierName = GTValues.VN[i].toLowerCase();
            MetaTileEntities.QUADRUPLE_IMPORT_HATCH[i] = MetaTileEntities.registerMetaTileEntity(11060 + i,
                    new MetaTileEntityMultiFluidHatch(gregtechId("fluid_hatch.import_4x." + tierName), i, 4, false));
            MetaTileEntities.NONUPLE_IMPORT_HATCH[i] = MetaTileEntities.registerMetaTileEntity(11075 + i,
                    new MetaTileEntityMultiFluidHatch(gregtechId("fluid_hatch.import_9x." + tierName), i, 9, false));
            MetaTileEntities.QUADRUPLE_EXPORT_HATCH[i] = MetaTileEntities.registerMetaTileEntity(11090 + i,
                    new MetaTileEntityMultiFluidHatch(gregtechId("fluid_hatch.export_4x." + tierName), i, 4, true));
            MetaTileEntities.NONUPLE_EXPORT_HATCH[i] = MetaTileEntities.registerMetaTileEntity(11105 + i,
                    new MetaTileEntityMultiFluidHatch(gregtechId("fluid_hatch.export_9x." + tierName), i, 9, true));
        }

        // Energy Hatches, IDs 11120-11239
        // 2A Energy Input, IDs 11120-11134
        // 2A Energy Output, IDs 11135-11149
        // 4A Energy Input, IDs 11150-11164
        // 4A Energy Output, IDs 11165-11179
        // 16A Energy Input, IDs 11180-11194
        // 16A Energy Output, IDs 11195-11209
        // 64A Energy Input, IDs 11210-11224
        // 64A Energy Output, IDs 11225-11239
        endPos = GregTechAPI.isHighTier() ? MetaTileEntities.ENERGY_INPUT_HATCH.length : GTValues.UHV + 1;
        for (int i = 0; i < endPos; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            MetaTileEntities.ENERGY_INPUT_HATCH[i] = MetaTileEntities.registerMetaTileEntity(11120 + i,
                    new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.input." + voltageName), i, 2, false));
            MetaTileEntities.ENERGY_OUTPUT_HATCH[i] = MetaTileEntities.registerMetaTileEntity(11135 + i,
                    new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.output." + voltageName), i, 2, true));

            MetaTileEntities.ENERGY_INPUT_HATCH_4A[i] = MetaTileEntities.registerMetaTileEntity(11150 + i,
                    new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.input_4a." + voltageName), i, 4, false));
            MetaTileEntities.ENERGY_OUTPUT_HATCH_4A[i] = MetaTileEntities.registerMetaTileEntity(11165 + i,
                    new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.output_4a." + voltageName), i, 4, true));
            MetaTileEntities.ENERGY_INPUT_HATCH_16A[i] = MetaTileEntities.registerMetaTileEntity(11180 + i,
                    new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.input_16a." + voltageName), i, 16,
                            false));
            MetaTileEntities.ENERGY_OUTPUT_HATCH_16A[i] = MetaTileEntities.registerMetaTileEntity(11195 + i,
                    new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.output_16a." + voltageName), i, 16,
                            true));
            MetaTileEntities.SUBSTATION_ENERGY_INPUT_HATCH[i] = MetaTileEntities.registerMetaTileEntity(11210 + i,
                    new MetaTileEntitySubstationEnergyHatch(gregtechId("substation_hatch.input_64a." + voltageName),
                            i, 64, false));
            MetaTileEntities.SUBSTATION_ENERGY_OUTPUT_HATCH[i] = MetaTileEntities.registerMetaTileEntity(11225 + i,
                    new MetaTileEntitySubstationEnergyHatch(
                            gregtechId("substation_hatch.output_64a." + voltageName), i, 64, true));
        }

        // Laser Hatches, IDs 11240-11449
        // 256A IN, IDs 11240-11254
        // 256A OUT, IDs 11255-11269
        // 1024A IN, IDs 11270-11284
        // 1024A OUT, IDs 11285-11299
        // 4096A IN, IDs 11300-11314
        // 4096A OUT, IDs 11315-11329
        endPos = GregTechAPI.isHighTier() ? MetaTileEntities.LASER_INPUT_HATCH_256.length : GTValues.UHV + 1;
        for (int i = GTValues.IV; i < endPos; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            MetaTileEntities.LASER_INPUT_HATCH_256[i] = MetaTileEntities.registerMetaTileEntity(11240 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.target_256a." + voltageName), false, i, 256));
            MetaTileEntities.LASER_OUTPUT_HATCH_256[i] = MetaTileEntities.registerMetaTileEntity(11255 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.source_256a." + voltageName), true, i, 256));
            MetaTileEntities.LASER_INPUT_HATCH_1024[i] = MetaTileEntities.registerMetaTileEntity(11270 + i,
                    new MetaTileEntityLaserHatch(
                            gregtechId("laser_hatch.target_1024a." + voltageName), false, i, 1024));
            MetaTileEntities.LASER_OUTPUT_HATCH_1024[i] = MetaTileEntities.registerMetaTileEntity(11285 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.source_1024a." + voltageName), true, i, 1024));
            MetaTileEntities.LASER_INPUT_HATCH_4096[i] = MetaTileEntities.registerMetaTileEntity(11300 + i,
                    new MetaTileEntityLaserHatch(
                            gregtechId("laser_hatch.target_4096a." + voltageName), false, i, 4096));
            MetaTileEntities.LASER_OUTPUT_HATCH_4096[i] = MetaTileEntities.registerMetaTileEntity(11315 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.source_4096a." + voltageName), true, i, 4096));
        }

        // Rotor Holder, IDs 11450-11464
        for (int i = GTValues.HV; i <= GTValues.UV; i++) {
            MetaTileEntities.ROTOR_HOLDER[i - GTValues.HV] = MetaTileEntities.registerMetaTileEntity(11450 + i,
                    new MetaTileEntityRotorHolder(gregtechId("rotor_holder." + GTValues.VN[i].toLowerCase()), i));
        }

        // Muffler Hatches, IDs 11465-11479
        endPos = GregTechAPI.isHighTier() ? MetaTileEntities.MUFFLER_HATCH.length : GTValues.UV + 1;
        for (int i = 1; i < endPos; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            MetaTileEntities.MUFFLER_HATCH[i] = new MetaTileEntityMufflerHatch(
                    gregtechId("muffler_hatch." + voltageName), i);

            MetaTileEntities.registerMetaTileEntity(11465 + i, MetaTileEntities.MUFFLER_HATCH[i]);
        }

        // Maintenance Hatches, IDs 11480-11494
        MetaTileEntities.MAINTENANCE_HATCH = MetaTileEntities.registerMetaTileEntity(11480,
                new MetaTileEntityMaintenanceHatch(gregtechId("maintenance_hatch"), false));
        MetaTileEntities.CONFIGURABLE_MAINTENANCE_HATCH = MetaTileEntities.registerMetaTileEntity(11481,
                new MetaTileEntityMaintenanceHatch(gregtechId("maintenance_hatch_configurable"), true));
        MetaTileEntities.AUTO_MAINTENANCE_HATCH = MetaTileEntities.registerMetaTileEntity(11482,
                new MetaTileEntityAutoMaintenanceHatch(gregtechId("maintenance_hatch_full_auto")));
        MetaTileEntities.CLEANING_MAINTENANCE_HATCH = MetaTileEntities.registerMetaTileEntity(11483,
                new MetaTileEntityCleaningMaintenanceHatch(gregtechId("maintenance_hatch_cleanroom_auto")));

        // Misc Hatches: ID 11495-11524
        MetaTileEntities.MACHINE_HATCH = MetaTileEntities.registerMetaTileEntity(11495,
                new MetaTileEntityMachineHatch(gregtechId("machine_hatch"), 5));

        MetaTileEntities.PASSTHROUGH_HATCH_ITEM = MetaTileEntities.registerMetaTileEntity(11496,
                new MetaTileEntityPassthroughHatchItem(gregtechId("passthrough_hatch_item"), 3));
        MetaTileEntities.PASSTHROUGH_HATCH_FLUID = MetaTileEntities.registerMetaTileEntity(11497,
                new MetaTileEntityPassthroughHatchFluid(gregtechId("passthrough_hatch_fluid"), 3));

        MetaTileEntities.DATA_ACCESS_HATCH = MetaTileEntities.registerMetaTileEntity(11498,
                new MetaTileEntityDataAccessHatch(gregtechId("data_access_hatch"), GTValues.EV, false));
        MetaTileEntities.ADVANCED_DATA_ACCESS_HATCH = MetaTileEntities.registerMetaTileEntity(11499,
                new MetaTileEntityDataAccessHatch(gregtechId("data_access_hatch.advanced"), GTValues.LuV, false));
        MetaTileEntities.CREATIVE_DATA_HATCH = MetaTileEntities.registerMetaTileEntity(11500,
                new MetaTileEntityDataAccessHatch(gregtechId("data_access_hatch.creative"), GTValues.MAX, true));

        MetaTileEntities.OPTICAL_DATA_HATCH_RECEIVER = MetaTileEntities.registerMetaTileEntity(11501,
                new MetaTileEntityOpticalDataHatch(gregtechId("data_access_hatch.optical.receiver"), false));
        MetaTileEntities.OPTICAL_DATA_HATCH_TRANSMITTER = MetaTileEntities.registerMetaTileEntity(11502,
                new MetaTileEntityOpticalDataHatch(gregtechId("data_access_hatch.optical.transmitter"), true));
        MetaTileEntities.COMPUTATION_HATCH_RECEIVER = MetaTileEntities.registerMetaTileEntity(11503,
                new MetaTileEntityComputationHatch(gregtechId("computation_hatch.receiver"), false));
        MetaTileEntities.COMPUTATION_HATCH_TRANSMITTER = MetaTileEntities.registerMetaTileEntity(11504,
                new MetaTileEntityComputationHatch(gregtechId("computation_hatch.transmitter"), true));

        MetaTileEntities.RESERVOIR_HATCH = MetaTileEntities.registerMetaTileEntity(11505,
                new MetaTileEntityReservoirHatch(gregtechId("reservoir_hatch")));
        MetaTileEntities.PUMP_OUTPUT_HATCH = MetaTileEntities.registerMetaTileEntity(11506,
                new MetaTileEntityPumpHatch(gregtechId("pump_hatch")));

        MetaTileEntities.STEAM_EXPORT_BUS = MetaTileEntities.registerMetaTileEntity(11507,
                new MetaTileEntitySteamItemBus(gregtechId("steam_export_bus"), true));
        MetaTileEntities.STEAM_IMPORT_BUS = MetaTileEntities.registerMetaTileEntity(11508,
                new MetaTileEntitySteamItemBus(gregtechId("steam_import_bus"), false));
        MetaTileEntities.STEAM_HATCH = MetaTileEntities.registerMetaTileEntity(11509,
                new MetaTileEntitySteamHatch(gregtechId("steam_hatch")));

        MetaTileEntities.OBJECT_HOLDER = MetaTileEntities.registerMetaTileEntity(11510,
                new MetaTileEntityObjectHolder(gregtechId("research_station.object_holder")));

        MetaTileEntities.HPCA_EMPTY_COMPONENT = MetaTileEntities.registerMetaTileEntity(11511,
                new MetaTileEntityHPCAEmpty(gregtechId("hpca.empty_component")));
        MetaTileEntities.HPCA_COMPUTATION_COMPONENT = MetaTileEntities.registerMetaTileEntity(11512,
                new MetaTileEntityHPCAComputation(gregtechId("hpca.computation_component"), false));
        MetaTileEntities.HPCA_ADVANCED_COMPUTATION_COMPONENT = MetaTileEntities.registerMetaTileEntity(11513,
                new MetaTileEntityHPCAComputation(gregtechId("hpca.advanced_computation_component"), true));
        MetaTileEntities.HPCA_HEAT_SINK_COMPONENT = MetaTileEntities.registerMetaTileEntity(11514,
                new MetaTileEntityHPCACooler(gregtechId("hpca.heat_sink_component"), false));
        MetaTileEntities.HPCA_ACTIVE_COOLER_COMPONENT = MetaTileEntities.registerMetaTileEntity(11515,
                new MetaTileEntityHPCACooler(gregtechId("hpca.active_cooler_component"), true));
        MetaTileEntities.HPCA_BRIDGE_COMPONENT = MetaTileEntities.registerMetaTileEntity(11516,
                new MetaTileEntityHPCABridge(gregtechId("hpca.bridge_component")));

        MetaTileEntities.WOODEN_TANK_VALVE = MetaTileEntities.registerMetaTileEntity(11523,
                new MetaTileEntityTankValve(gregtechId("tank_valve.wood"), false));
        MetaTileEntities.STEEL_TANK_VALVE = MetaTileEntities.registerMetaTileEntity(11524,
                new MetaTileEntityTankValve(gregtechId("tank_valve.steel"), true));

        // ME Hatches, IDs 11525-11539
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            MetaTileEntities.FLUID_EXPORT_HATCH_ME = MetaTileEntities.registerMetaTileEntity(11525,
                    new MetaTileEntityMEOutputHatch(gregtechId("me_export_fluid_hatch")));
            MetaTileEntities.ITEM_EXPORT_BUS_ME = MetaTileEntities.registerMetaTileEntity(11526,
                    new MetaTileEntityMEOutputBus(gregtechId("me_export_item_bus")));
            MetaTileEntities.FLUID_IMPORT_HATCH_ME = MetaTileEntities.registerMetaTileEntity(11527,
                    new MetaTileEntityMEInputHatch(gregtechId("me_import_fluid_hatch")));
            MetaTileEntities.ITEM_IMPORT_BUS_ME = MetaTileEntities.registerMetaTileEntity(11528,
                    new MetaTileEntityMEInputBus(gregtechId("me_import_item_bus")));
            MetaTileEntities.STOCKING_BUS_ME = MetaTileEntities.registerMetaTileEntity(11529,
                    new MetaTileEntityMEStockingBus(gregtechId("me_stocking_item_bus")));
            MetaTileEntities.STOCKING_HATCH_ME = MetaTileEntities.registerMetaTileEntity(11530,
                    new MetaTileEntityMEStockingHatch(gregtechId("me_stocking_fluid_hatch")));
        }
    }
}
