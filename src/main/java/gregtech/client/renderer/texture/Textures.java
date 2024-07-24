package gregtech.client.renderer.texture;

import gregtech.api.GTValues;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.cclop.UVMirror;
import gregtech.client.renderer.texture.cube.AlignedOrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.LDPipeOverlayRenderer;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SidedCubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleOrientedCubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.client.renderer.texture.custom.ClipboardRenderer;
import gregtech.client.renderer.texture.custom.CrateRenderer;
import gregtech.client.renderer.texture.custom.DrumRenderer;
import gregtech.client.renderer.texture.custom.FireboxActiveRenderer;
import gregtech.client.renderer.texture.custom.LargeTurbineRenderer;
import gregtech.client.renderer.texture.custom.QuantumStorageRenderer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.BlockRenderer.BlockFace;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.TransformationList;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.lib.vec.uv.UVTransformationList;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gregtech.api.util.GTUtility.gregtechId;

public class Textures {

    public static final Map<String, ICubeRenderer> CUBE_RENDERER_REGISTRY = new HashMap<>();

    private static final ThreadLocal<BlockFace> blockFaces = ThreadLocal.withInitial(BlockFace::new);
    public static final List<IIconRegister> iconRegisters = new ArrayList<>();

    // Custom Renderers
    public static final ClipboardRenderer CLIPBOARD_RENDERER = new ClipboardRenderer();
    public static final CrateRenderer WOODEN_CRATE = new CrateRenderer("storage/crates/wooden_crate");
    public static final CrateRenderer METAL_CRATE = new CrateRenderer("storage/crates/metal_crate");
    public static final DrumRenderer WOODEN_DRUM = new DrumRenderer("storage/drums/wooden_drum");
    public static final DrumRenderer DRUM = new DrumRenderer("storage/drums/drum");
    public static final LargeTurbineRenderer LARGE_TURBINE_ROTOR_RENDERER = new LargeTurbineRenderer();
    public static final QuantumStorageRenderer QUANTUM_STORAGE_RENDERER = new QuantumStorageRenderer();

    // Simple Cube Renderers
    public static final SimpleOverlayRenderer BRONZE_PLATED_BRICKS = new SimpleOverlayRenderer(
            "casings/solid/machine_bronze_plated_bricks");
    public static final SimpleOverlayRenderer PRIMITIVE_BRICKS = new SimpleOverlayRenderer(
            "casings/solid/machine_primitive_bricks");
    public static final SimpleOverlayRenderer COKE_BRICKS = new SimpleOverlayRenderer(
            "casings/solid/machine_coke_bricks");
    public static final SimpleOverlayRenderer HEAT_PROOF_CASING = new SimpleOverlayRenderer(
            "casings/solid/machine_casing_heatproof");
    public static final SimpleOverlayRenderer FROST_PROOF_CASING = new SimpleOverlayRenderer(
            "casings/solid/machine_casing_frost_proof");
    public static final SimpleOverlayRenderer SOLID_STEEL_CASING = new SimpleOverlayRenderer(
            "casings/solid/machine_casing_solid_steel");
    public static final SimpleOverlayRenderer CLEAN_STAINLESS_STEEL_CASING = new SimpleOverlayRenderer(
            "casings/solid/machine_casing_clean_stainless_steel");
    public static final SimpleOverlayRenderer STABLE_TITANIUM_CASING = new SimpleOverlayRenderer(
            "casings/solid/machine_casing_stable_titanium");
    public static final SimpleOverlayRenderer ROBUST_TUNGSTENSTEEL_CASING = new SimpleOverlayRenderer(
            "casings/solid/machine_casing_robust_tungstensteel");
    public static final SimpleOverlayRenderer STURDY_HSSE_CASING = new SimpleOverlayRenderer(
            "casings/solid/machine_casing_sturdy_hsse");
    public static final SimpleOverlayRenderer PALLADIUM_SUBSTATION_CASING = new SimpleOverlayRenderer(
            "casings/solid/machine_casing_palladium_substation");
    public static final SimpleOverlayRenderer INERT_PTFE_CASING = new SimpleOverlayRenderer(
            "casings/solid/machine_casing_inert_ptfe");
    public static final SimpleOverlayRenderer PLASCRETE = new SimpleOverlayRenderer("casings/cleanroom/plascrete");
    public static final SimpleOverlayRenderer FUSION_TEXTURE = new SimpleOverlayRenderer(
            "casings/fusion/machine_casing_fusion_hatch");
    public static final SimpleOverlayRenderer ACTIVE_FUSION_TEXTURE = new SimpleOverlayRenderer(
            "casings/fusion/machine_casing_fusion_hatch_yellow");
    public static final SimpleOverlayRenderer GRATE_CASING = new SimpleOverlayRenderer(
            "casings/pipe/machine_casing_grate");
    public static final SimpleOverlayRenderer HIGH_POWER_CASING = new SimpleOverlayRenderer(
            "casings/computer/high_power_casing");
    public static final SimpleOverlayRenderer FISSION_REACTOR_TEXTURE = new SimpleOverlayRenderer(
            "casings/fission/reactor_vessel");

    // Simple Sided Cube Renderers
    public static final SimpleSidedCubeRenderer STEAM_CASING_BRONZE = new SimpleSidedCubeRenderer(
            "casings/steam/bronze");
    public static final SimpleSidedCubeRenderer STEAM_CASING_STEEL = new SimpleSidedCubeRenderer("casings/steam/steel");
    public static final SimpleSidedCubeRenderer STEAM_BRICKED_CASING_BRONZE = new SimpleSidedCubeRenderer(
            "casings/steam/bricked_bronze");
    public static final SimpleSidedCubeRenderer STEAM_BRICKED_CASING_STEEL = new SimpleSidedCubeRenderer(
            "casings/steam/bricked_steel");
    public static final SimpleSidedCubeRenderer[] VOLTAGE_CASINGS = new SimpleSidedCubeRenderer[GTValues.V.length];
    public static final SimpleSidedCubeRenderer PRIMITIVE_PUMP = new SimpleSidedCubeRenderer("casings/pump_deck");
    public static final SimpleSidedCubeRenderer WOOD_WALL = new SimpleSidedCubeRenderer("casings/wood_wall");
    public static final SimpleSidedCubeRenderer MAGIC_ENERGY_ABSORBER = new SimpleSidedCubeRenderer(
            "casings/magic/absorber/normal");
    public static final SimpleSidedCubeRenderer MAGIC_ENERGY_ABSORBER_ACTIVE = new SimpleSidedCubeRenderer(
            "casings/magic/absorber/active");
    public static final SimpleSidedCubeRenderer DRUM_OVERLAY = new SimpleSidedCubeRenderer("storage/drums/drum_top");

    // Simple Oriented Cube Renderers
    public static final SimpleOrientedCubeRenderer CRAFTING_TABLE = new SimpleOrientedCubeRenderer(
            "casings/crafting_table");
    public static final SimpleOrientedCubeRenderer GRATE_CASING_STEEL_FRONT = new SimpleOrientedCubeRenderer(
            "casings/pipe/grate_steel_front");

    // Oriented Overlay Renderers
    public static final OrientedOverlayRenderer COAL_BOILER_OVERLAY = new OrientedOverlayRenderer(
            "generators/boiler/coal");
    public static final OrientedOverlayRenderer LAVA_BOILER_OVERLAY = new OrientedOverlayRenderer(
            "generators/boiler/lava");
    public static final OrientedOverlayRenderer SOLAR_BOILER_OVERLAY = new OrientedOverlayRenderer(
            "generators/boiler/solar");
    public static final OrientedOverlayRenderer PRIMITIVE_PUMP_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/primitive_pump");
    public static final OrientedOverlayRenderer PRIMITIVE_BLAST_FURNACE_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/primitive_blast_furnace");
    public static final OrientedOverlayRenderer COKE_OVEN_OVERLAY = new OrientedOverlayRenderer("multiblock/coke_oven");
    public static final OrientedOverlayRenderer MULTIBLOCK_WORKABLE_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/multiblock_workable");
    public static final OrientedOverlayRenderer BLAST_FURNACE_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/blast_furnace");
    public static final OrientedOverlayRenderer IMPLOSION_COMPRESSOR_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/implosion_compressor");
    public static final OrientedOverlayRenderer MULTI_FURNACE_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/multi_furnace");
    public static final OrientedOverlayRenderer PYROLYSE_OVEN_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/pyrolyse_oven");
    public static final OrientedOverlayRenderer VACUUM_FREEZER_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/vacuum_freezer");
    public static final OrientedOverlayRenderer DISTILLATION_TOWER_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/distillation_tower");
    public static final OrientedOverlayRenderer CRACKING_UNIT_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/cracking_unit");
    public static final OrientedOverlayRenderer LARGE_CHEMICAL_REACTOR_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/large_chemical_reactor");
    public static final OrientedOverlayRenderer LARGE_COMBUSTION_ENGINE_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/generator/large_combustion_engine");
    public static final OrientedOverlayRenderer EXTREME_COMBUSTION_ENGINE_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/generator/extreme_combustion_engine");
    public static final OrientedOverlayRenderer FLUID_RIG_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/fluid_drilling_rig");
    public static final OrientedOverlayRenderer LARGE_STEAM_TURBINE_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/generator/large_steam_turbine");
    public static final OrientedOverlayRenderer LARGE_GAS_TURBINE_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/generator/large_gas_turbine");
    public static final OrientedOverlayRenderer LARGE_PLASMA_TURBINE_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/generator/large_plasma_turbine");
    public static final OrientedOverlayRenderer LARGE_BRONZE_BOILER = new OrientedOverlayRenderer(
            "multiblock/generator/large_bronze_boiler");
    public static final OrientedOverlayRenderer LARGE_STEEL_BOILER = new OrientedOverlayRenderer(
            "multiblock/generator/large_steel_boiler");
    public static final OrientedOverlayRenderer LARGE_TITANIUM_BOILER = new OrientedOverlayRenderer(
            "multiblock/generator/large_titanium_boiler");
    public static final OrientedOverlayRenderer LARGE_TUNGSTENSTEEL_BOILER = new OrientedOverlayRenderer(
            "multiblock/generator/large_tungstensteel_boiler");
    public static final OrientedOverlayRenderer FUSION_REACTOR_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/fusion_reactor");
    public static final OrientedOverlayRenderer PROCESSING_ARRAY_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/processing_array");
    public static final OrientedOverlayRenderer ADVANCED_PROCESSING_ARRAY_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/advanced_processing_array");
    public static final OrientedOverlayRenderer LARGE_MINER_OVERLAY_BASIC = new OrientedOverlayRenderer(
            "multiblock/large_miner_basic");
    public static final OrientedOverlayRenderer LARGE_MINER_OVERLAY_ADVANCED = new OrientedOverlayRenderer(
            "multiblock/large_miner_advanced");
    public static final OrientedOverlayRenderer LARGE_MINER_OVERLAY_ADVANCED_2 = new OrientedOverlayRenderer(
            "multiblock/large_miner_advanced_2");
    public static final OrientedOverlayRenderer CLEANROOM_OVERLAY = new OrientedOverlayRenderer("multiblock/cleanroom");
    public static final OrientedOverlayRenderer MULTIBLOCK_TANK_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/multiblock_tank");
    public static final OrientedOverlayRenderer CHARCOAL_PILE_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/charcoal_pile_igniter");
    public static final OrientedOverlayRenderer DATA_BANK_OVERLAY = new OrientedOverlayRenderer("multiblock/data_bank");
    public static final OrientedOverlayRenderer RESEARCH_STATION_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/research_station");
    public static final OrientedOverlayRenderer HPCA_OVERLAY = new OrientedOverlayRenderer("multiblock/hpca");
    public static final OrientedOverlayRenderer NETWORK_SWITCH_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/network_switch");
    public static final OrientedOverlayRenderer POWER_SUBSTATION_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/power_substation");
    public static final OrientedOverlayRenderer FISSION_REACTOR_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/fission_reactor");
    public static final OrientedOverlayRenderer SPENT_FUEL_POOL_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/spent_fuel_pool");
    public static final OrientedOverlayRenderer HEAT_EXCHANGER_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/heat_exchanger");
    public static final OrientedOverlayRenderer GAS_CENTRIFUGE_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/gas_centrifuge");

    public static final OrientedOverlayRenderer ALLOY_SMELTER_OVERLAY = new OrientedOverlayRenderer(
            "machines/alloy_smelter");
    public static final OrientedOverlayRenderer FURNACE_OVERLAY = new OrientedOverlayRenderer("machines/furnace");
    public static final OrientedOverlayRenderer ELECTRIC_FURNACE_OVERLAY = new OrientedOverlayRenderer(
            "machines/electric_furnace");
    public static final OrientedOverlayRenderer EXTRACTOR_OVERLAY = new OrientedOverlayRenderer("machines/extractor");
    public static final OrientedOverlayRenderer COMPRESSOR_OVERLAY = new OrientedOverlayRenderer("machines/compressor");
    public static final OrientedOverlayRenderer MACERATOR_OVERLAY = new OrientedOverlayRenderer("machines/macerator");
    public static final OrientedOverlayRenderer PULVERIZER_OVERLAY = new OrientedOverlayRenderer("machines/pulverizer");
    public static final OrientedOverlayRenderer ARC_FURNACE_OVERLAY = new OrientedOverlayRenderer(
            "machines/arc_furnace");
    public static final OrientedOverlayRenderer ASSEMBLER_OVERLAY = new OrientedOverlayRenderer("machines/assembler");
    public static final OrientedOverlayRenderer CIRCUIT_ASSEMBLER_OVERLAY = new OrientedOverlayRenderer(
            "machines/circuit_assembler");
    public static final OrientedOverlayRenderer AUTOCLAVE_OVERLAY = new OrientedOverlayRenderer("machines/autoclave");
    public static final OrientedOverlayRenderer BENDER_OVERLAY = new OrientedOverlayRenderer("machines/bender");
    public static final OrientedOverlayRenderer BREWERY_OVERLAY = new OrientedOverlayRenderer("machines/brewery");
    public static final OrientedOverlayRenderer CANNER_OVERLAY = new OrientedOverlayRenderer("machines/canner");
    public static final OrientedOverlayRenderer CENTRIFUGE_OVERLAY = new OrientedOverlayRenderer("machines/centrifuge");
    public static final OrientedOverlayRenderer CHEMICAL_BATH_OVERLAY = new OrientedOverlayRenderer(
            "machines/chemical_bath");
    public static final OrientedOverlayRenderer CHEMICAL_REACTOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/chemical_reactor");
    public static final OrientedOverlayRenderer CUTTER_OVERLAY = new OrientedOverlayRenderer("machines/cutter");
    public static final OrientedOverlayRenderer DISTILLERY_OVERLAY = new OrientedOverlayRenderer("machines/distillery");
    public static final OrientedOverlayRenderer ELECTROLYZER_OVERLAY = new OrientedOverlayRenderer(
            "machines/electrolyzer");
    public static final OrientedOverlayRenderer ELECTROMAGNETIC_SEPARATOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/electromagnetic_separator");
    public static final OrientedOverlayRenderer EXTRUDER_OVERLAY = new OrientedOverlayRenderer("machines/extruder");
    public static final OrientedOverlayRenderer FERMENTER_OVERLAY = new OrientedOverlayRenderer("machines/fermenter");
    public static final OrientedOverlayRenderer FLUID_HEATER_OVERLAY = new OrientedOverlayRenderer(
            "machines/fluid_heater");
    public static final OrientedOverlayRenderer FLUID_SOLIDIFIER_OVERLAY = new OrientedOverlayRenderer(
            "machines/fluid_solidifier");
    public static final OrientedOverlayRenderer FORGE_HAMMER_OVERLAY = new OrientedOverlayRenderer(
            "machines/forge_hammer");
    public static final OrientedOverlayRenderer FORMING_PRESS_OVERLAY = new OrientedOverlayRenderer("machines/press");
    public static final OrientedOverlayRenderer GAS_COLLECTOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/gas_collector");
    public static final OrientedOverlayRenderer LATHE_OVERLAY = new OrientedOverlayRenderer("machines/lathe");
    public static final OrientedOverlayRenderer MIXER_OVERLAY = new OrientedOverlayRenderer("machines/mixer");
    public static final OrientedOverlayRenderer ORE_WASHER_OVERLAY = new OrientedOverlayRenderer("machines/ore_washer");
    public static final OrientedOverlayRenderer PACKER_OVERLAY = new OrientedOverlayRenderer("machines/packer");
    public static final OrientedOverlayRenderer POLARIZER_OVERLAY = new OrientedOverlayRenderer("machines/polarizer");
    public static final OrientedOverlayRenderer LASER_ENGRAVER_OVERLAY = new OrientedOverlayRenderer(
            "machines/laser_engraver");
    public static final OrientedOverlayRenderer ROCK_BREAKER_OVERLAY = new OrientedOverlayRenderer(
            "machines/rock_crusher");
    public static final OrientedOverlayRenderer SIFTER_OVERLAY = new OrientedOverlayRenderer("machines/sifter");
    public static final OrientedOverlayRenderer THERMAL_CENTRIFUGE_OVERLAY = new OrientedOverlayRenderer(
            "machines/thermal_centrifuge");
    public static final OrientedOverlayRenderer WIREMILL_OVERLAY = new OrientedOverlayRenderer("machines/wiremill");
    public static final OrientedOverlayRenderer MASS_FABRICATOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/mass_fabricator");
    public static final OrientedOverlayRenderer REPLICATOR_OVERLAY = new OrientedOverlayRenderer("machines/replicator");
    public static final OrientedOverlayRenderer SCANNER_OVERLAY = new OrientedOverlayRenderer("machines/scanner");
    public static final OrientedOverlayRenderer COMBUSTION_GENERATOR_OVERLAY = new OrientedOverlayRenderer(
            "generators/combustion");
    public static final OrientedOverlayRenderer GAS_TURBINE_OVERLAY = new OrientedOverlayRenderer(
            "generators/gas_turbine");
    public static final OrientedOverlayRenderer STEAM_TURBINE_OVERLAY = new OrientedOverlayRenderer(
            "generators/steam_turbine");
    public static final OrientedOverlayRenderer WORLD_ACCELERATOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/world_accelerator");
    public static final OrientedOverlayRenderer WORLD_ACCELERATOR_TE_OVERLAY = new OrientedOverlayRenderer(
            "machines/world_accelerator_te");

    // Simple Overlay Renderers
    public static final SimpleOverlayRenderer SCREEN = new SimpleOverlayRenderer("overlay/machine/overlay_screen");
    public static final SimpleOverlayRenderer DISPLAY = new SimpleOverlayRenderer("cover/overlay_display");
    public static final SimpleOverlayRenderer SHUTTER = new SimpleOverlayRenderer("cover/overlay_shutter");
    public static final SimpleOverlayRenderer DETECTOR_ENERGY = new SimpleOverlayRenderer(
            "cover/overlay_energy_detector");
    public static final SimpleOverlayRenderer DETECTOR_ENERGY_ADVANCED = new SimpleOverlayRenderer(
            "cover/overlay_energy_detector_advanced");
    public static final SimpleOverlayRenderer DETECTOR_FLUID = new SimpleOverlayRenderer(
            "cover/overlay_fluid_detector");
    public static final SimpleOverlayRenderer DETECTOR_FLUID_ADVANCED = new SimpleOverlayRenderer(
            "cover/overlay_fluid_detector_advanced");
    public static final SimpleOverlayRenderer DETECTOR_ITEM = new SimpleOverlayRenderer("cover/overlay_item_detector");
    public static final SimpleOverlayRenderer DETECTOR_ITEM_ADVANCED = new SimpleOverlayRenderer(
            "cover/overlay_item_detector_advanced");
    public static final SimpleOverlayRenderer DETECTOR_ACTIVITY = new SimpleOverlayRenderer(
            "cover/overlay_activity_detector");
    public static final SimpleOverlayRenderer DETECTOR_ACTIVITY_ADVANCED = new SimpleOverlayRenderer(
            "cover/overlay_activity_detector_advanced");
    public static final SimpleOverlayRenderer DETECTOR_MAINTENANCE = new SimpleOverlayRenderer(
            "cover/overlay_maintenance_detector");
    public static final SimpleOverlayRenderer SOLAR_PANEL = new SimpleOverlayRenderer("cover/overlay_solar_panel");
    public static final SimpleOverlayRenderer INFINITE_WATER = new SimpleOverlayRenderer(
            "cover/overlay_infinite_water");
    public static final SimpleOverlayRenderer FLUID_VOIDING = new SimpleOverlayRenderer("cover/overlay_fluid_voiding");
    public static final SimpleOverlayRenderer ITEM_VOIDING = new SimpleOverlayRenderer("cover/overlay_item_voiding");
    public static final SimpleOverlayRenderer FLUID_VOIDING_ADVANCED = new SimpleOverlayRenderer(
            "cover/overlay_fluid_voiding_advanced");
    public static final SimpleOverlayRenderer ITEM_VOIDING_ADVANCED = new SimpleOverlayRenderer(
            "cover/overlay_item_voiding_advanced");
    public static final SimpleOverlayRenderer ENDER_FLUID_LINK = new SimpleOverlayRenderer(
            "cover/overlay_ender_fluid_link");
    public static final SimpleOverlayRenderer STORAGE = new SimpleOverlayRenderer("cover/overlay_storage");
    public static final SimpleOverlayRenderer PIPE_OUT_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_pipe_out");
    public static final SimpleOverlayRenderer PIPE_IN_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_pipe_in");
    public static final SimpleOverlayRenderer PIPE_4X_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_pipe_4x");
    public static final SimpleOverlayRenderer PIPE_9X_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_pipe_9x");

    public static final SimpleOverlayRenderer FLUID_OUTPUT_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_fluid_output");
    public static final SimpleOverlayRenderer ITEM_OUTPUT_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_item_output");

    public static final SimpleOverlayRenderer FLUID_HATCH_OUTPUT_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_fluid_hatch_output");
    public static final SimpleOverlayRenderer FLUID_HATCH_INPUT_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_fluid_hatch_input");
    public static final SimpleOverlayRenderer ITEM_HATCH_OUTPUT_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_item_hatch_output");
    public static final SimpleOverlayRenderer ITEM_HATCH_INPUT_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_item_hatch_input");
    public static final SimpleOverlayRenderer WATER_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_water");

    public static final ICubeRenderer BRONZE_FIREBOX = new SidedCubeRenderer("casings/firebox/overlay/bronze");
    public static final ICubeRenderer BRONZE_FIREBOX_ACTIVE = new FireboxActiveRenderer(
            "casings/firebox/overlay/bronze/active");
    public static final ICubeRenderer STEEL_FIREBOX = new SidedCubeRenderer("casings/firebox/overlay/steel");
    public static final ICubeRenderer STEEL_FIREBOX_ACTIVE = new FireboxActiveRenderer(
            "casings/firebox/overlay/steel/active");
    public static final ICubeRenderer TITANIUM_FIREBOX = new SidedCubeRenderer("casings/firebox/overlay/titanium");
    public static final ICubeRenderer TITANIUM_FIREBOX_ACTIVE = new FireboxActiveRenderer(
            "casings/firebox/overlay/titanium/active");
    public static final ICubeRenderer TUNGSTENSTEEL_FIREBOX = new SidedCubeRenderer(
            "casings/firebox/overlay/tungstensteel");
    public static final ICubeRenderer TUNGSTENSTEEL_FIREBOX_ACTIVE = new FireboxActiveRenderer(
            "casings/firebox/overlay/tungstensteel/active");
    public static final ICubeRenderer COMPUTER_CASING = new SidedCubeRenderer("casings/computer/computer_casing");
    public static final ICubeRenderer ADVANCED_COMPUTER_CASING = new SidedCubeRenderer(
            "casings/computer/advanced_computer_casing");

    public static final AlignedOrientedOverlayRenderer LD_ITEM_PIPE = new LDPipeOverlayRenderer("pipe/ld_item_pipe");
    public static final AlignedOrientedOverlayRenderer LD_FLUID_PIPE = new LDPipeOverlayRenderer("pipe/ld_fluid_pipe");

    public static final SimpleOverlayRenderer ROTOR_HOLDER_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_rotor_holder");
    public static final SimpleOverlayRenderer ADV_PUMP_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_adv_pump");
    public static final SimpleOverlayRenderer FILTER_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_filter");
    public static final SimpleOverlayRenderer HATCH_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_hatch");
    public static final SimpleOverlayRenderer FLUID_FILTER_OVERLAY = new SimpleOverlayRenderer(
            "cover/overlay_fluid_filter");
    public static final SimpleOverlayRenderer ITEM_FILTER_FILTER_OVERLAY = new SimpleOverlayRenderer(
            "cover/overlay_item_filter");
    public static final SimpleOverlayRenderer ORE_DICTIONARY_FILTER_OVERLAY = new SimpleOverlayRenderer(
            "cover/overlay_ore_dictionary_filter");
    public static final SimpleOverlayRenderer SMART_FILTER_FILTER_OVERLAY = new SimpleOverlayRenderer(
            "cover/overlay_smart_item_filter");
    public static final SimpleOverlayRenderer MACHINE_CONTROLLER_OVERLAY = new SimpleOverlayRenderer(
            "cover/overlay_controller");
    public static final SimpleOverlayRenderer ENERGY_OUT = new SimpleOverlayRenderer(
            "overlay/machine/overlay_energy_out");
    public static final SimpleOverlayRenderer ENERGY_IN = new SimpleOverlayRenderer(
            "overlay/machine/overlay_energy_in");
    public static final SimpleOverlayRenderer ENERGY_OUT_MULTI = new SimpleOverlayRenderer(
            "overlay/machine/overlay_energy_out_multi");
    public static final SimpleOverlayRenderer ENERGY_IN_MULTI = new SimpleOverlayRenderer(
            "overlay/machine/overlay_energy_in_multi");
    public static final SimpleOverlayRenderer ENERGY_OUT_HI = new SimpleOverlayRenderer(
            "overlay/machine/overlay_energy_out_hi");
    public static final SimpleOverlayRenderer ENERGY_IN_HI = new SimpleOverlayRenderer(
            "overlay/machine/overlay_energy_in_hi");
    public static final SimpleOverlayRenderer ENERGY_OUT_ULTRA = new SimpleOverlayRenderer(
            "overlay/machine/overlay_energy_out_ultra");
    public static final SimpleOverlayRenderer ENERGY_IN_ULTRA = new SimpleOverlayRenderer(
            "overlay/machine/overlay_energy_in_ultra");
    public static final SimpleOverlayRenderer ENERGY_OUT_MAX = new SimpleOverlayRenderer(
            "overlay/machine/overlay_energy_out_max");
    public static final SimpleOverlayRenderer ENERGY_IN_MAX = new SimpleOverlayRenderer(
            "overlay/machine/overlay_energy_in_max");
    public static final SimpleOverlayRenderer CONVEYOR_OVERLAY = new SimpleOverlayRenderer("cover/overlay_conveyor");
    public static final SimpleOverlayRenderer CONVEYOR_OVERLAY_INVERTED = new SimpleOverlayRenderer(
            "cover/overlay_conveyor_inverted");
    public static final SimpleOverlayRenderer ARM_OVERLAY = new SimpleOverlayRenderer("cover/overlay_arm");
    public static final SimpleOverlayRenderer ARM_OVERLAY_INVERTED = new SimpleOverlayRenderer(
            "cover/overlay_arm_inverted");
    public static final SimpleOverlayRenderer PUMP_OVERLAY = new SimpleOverlayRenderer("cover/overlay_pump");
    public static final SimpleOverlayRenderer PUMP_OVERLAY_INVERTED = new SimpleOverlayRenderer(
            "cover/overlay_pump_inverted");
    public static final SimpleOverlayRenderer AIR_VENT_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_air_vent");
    public static final SimpleOverlayRenderer BLOWER_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_blower");
    public static final SimpleOverlayRenderer BLOWER_ACTIVE_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_blower_active");
    public static final SimpleOverlayRenderer INFINITE_EMITTER_FACE = new SimpleOverlayRenderer(
            "overlay/machine/overlay_energy_emitter");
    public static final SimpleOverlayRenderer STEAM_VENT_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_steam_vent");
    public static final SimpleOverlayRenderer QUANTUM_TANK_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_qtank");
    public static final SimpleOverlayRenderer QUANTUM_CHEST_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_qchest");
    public static final SimpleOverlayRenderer CREATIVE_CONTAINER_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_creativecontainer");
    public static final SimpleOverlayRenderer BUFFER_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_buffer");
    public static final SimpleOverlayRenderer MAINTENANCE_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_maintenance");
    public static final SimpleOverlayRenderer MAINTENANCE_OVERLAY_TAPED = new SimpleOverlayRenderer(
            "overlay/machine/overlay_maintenance_taped");
    public static final SimpleOverlayRenderer MAINTENANCE_OVERLAY_CONFIGURABLE = new SimpleOverlayRenderer(
            "overlay/machine/overlay_maintenance_configurable");
    public static final SimpleOverlayRenderer MAINTENANCE_OVERLAY_FULL_AUTO = new SimpleOverlayRenderer(
            "overlay/machine/overlay_maintenance_full_auto");
    public static final SimpleOverlayRenderer MAINTENANCE_OVERLAY_CLEANING = new SimpleOverlayRenderer(
            "overlay/machine/overlay_maintenance_cleaning");
    public static final SimpleOverlayRenderer MUFFLER_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_muffler");
    public static final SimpleOverlayRenderer STEAM_MINER_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_steam_miner");
    public static final SimpleOverlayRenderer CHUNK_MINER_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_chunk_miner");
    public static final SimpleOverlayRenderer BLANK_SCREEN = new SimpleOverlayRenderer(
            "overlay/machine/overlay_blank_screen");
    public static final SimpleOverlayRenderer DATA_ACCESS_HATCH = new SimpleOverlayRenderer(
            "overlay/machine/overlay_data_hatch");
    public static final SimpleOverlayRenderer CREATIVE_DATA_ACCESS_HATCH = new SimpleOverlayRenderer(
            "overlay/machine/overlay_data_hatch_creative");
    public static final SimpleOverlayRenderer OPTICAL_DATA_ACCESS_HATCH = new SimpleOverlayRenderer(
            "overlay/machine/overlay_data_hatch_optical");
    public static final SimpleOverlayRenderer LASER_SOURCE = new SimpleOverlayRenderer(
            "overlay/machine/overlay_laser_source");
    public static final SimpleOverlayRenderer LASER_TARGET = new SimpleOverlayRenderer(
            "overlay/machine/overlay_laser_target");
    public static final SimpleOverlayRenderer OBJECT_HOLDER_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_object_holder");
    public static final SimpleOverlayRenderer OBJECT_HOLDER_ACTIVE_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_object_holder_active");
    public static final SimpleOverlayRenderer HPCA_ACTIVE_COOLER_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/hpca/active_cooler");
    public static final SimpleOverlayRenderer HPCA_ACTIVE_COOLER_ACTIVE_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/hpca/active_cooler_active");
    public static final SimpleOverlayRenderer HPCA_BRIDGE_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/hpca/bridge");
    public static final SimpleOverlayRenderer HPCA_BRIDGE_ACTIVE_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/hpca/bridge_active");
    public static final SimpleOverlayRenderer HPCA_COMPUTATION_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/hpca/computation");
    public static final SimpleOverlayRenderer HPCA_COMPUTATION_ACTIVE_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/hpca/computation_active");
    public static final SimpleOverlayRenderer HPCA_ADVANCED_COMPUTATION_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/hpca/computation_advanced");
    public static final SimpleOverlayRenderer HPCA_ADVANCED_COMPUTATION_ACTIVE_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/hpca/computation_advanced_active");
    public static final SimpleOverlayRenderer HPCA_DAMAGED_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/hpca/damaged");
    public static final SimpleOverlayRenderer HPCA_DAMAGED_ACTIVE_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/hpca/damaged_active");
    public static final SimpleOverlayRenderer HPCA_ADVANCED_DAMAGED_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/hpca/damaged_advanced");
    public static final SimpleOverlayRenderer HPCA_ADVANCED_DAMAGED_ACTIVE_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/hpca/damaged_advanced_active");
    public static final SimpleOverlayRenderer HPCA_EMPTY_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/hpca/empty");
    public static final SimpleOverlayRenderer HPCA_HEAT_SINK_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/hpca/heat_sink");
    public static final SimpleOverlayRenderer ALARM_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_alarm");
    public static final SimpleOverlayRenderer ALARM_OVERLAY_ACTIVE = new SimpleOverlayRenderer(
            "overlay/machine/overlay_alarm_active");
    public static final SimpleOverlayRenderer TAPED_OVERLAY = new SimpleOverlayRenderer(
            "overlay/machine/overlay_ducttape");

    public static final SimpleOverlayRenderer COVER_INTERFACE_FLUID = new SimpleOverlayRenderer(
            "cover/cover_interface_fluid");
    public static final SimpleOverlayRenderer COVER_INTERFACE_FLUID_GLASS = new SimpleOverlayRenderer(
            "cover/cover_interface_fluid_glass");
    public static final SimpleOverlayRenderer COVER_INTERFACE_ITEM = new SimpleOverlayRenderer(
            "cover/cover_interface_item");
    public static final SimpleOverlayRenderer COVER_INTERFACE_ENERGY = new SimpleOverlayRenderer(
            "cover/cover_interface_energy");
    public static final SimpleOverlayRenderer COVER_INTERFACE_MACHINE_ON = new SimpleOverlayRenderer(
            "cover/cover_interface_machine_on");
    public static final SimpleOverlayRenderer COVER_INTERFACE_MACHINE_OFF = new SimpleOverlayRenderer(
            "cover/cover_interface_machine_off");
    public static final SimpleOverlayRenderer COVER_INTERFACE_PROXY = new SimpleOverlayRenderer(
            "cover/cover_interface_proxy");
    public static final SimpleOverlayRenderer COVER_INTERFACE_WIRELESS = new SimpleOverlayRenderer(
            "cover/cover_interface_wireless");

    public static final SimpleOverlayRenderer CONVERTER_FE_OUT = new SimpleOverlayRenderer(
            "overlay/converter/converter_fe_out");
    public static final SimpleOverlayRenderer CONVERTER_FE_IN = new SimpleOverlayRenderer(
            "overlay/converter/converter_fe_in");

    public static final SimpleOverlayRenderer ME_OUTPUT_HATCH = new SimpleOverlayRenderer(
            "overlay/appeng/me_output_hatch");
    public static final SimpleOverlayRenderer ME_OUTPUT_HATCH_ACTIVE = new SimpleOverlayRenderer(
            "overlay/appeng/me_output_hatch_active");
    public static final SimpleOverlayRenderer ME_INPUT_HATCH = new SimpleOverlayRenderer(
            "overlay/appeng/me_input_hatch");
    public static final SimpleOverlayRenderer ME_INPUT_HATCH_ACTIVE = new SimpleOverlayRenderer(
            "overlay/appeng/me_input_hatch_active");
    public static final SimpleOverlayRenderer ME_OUTPUT_BUS = new SimpleOverlayRenderer("overlay/appeng/me_output_bus");
    public static final SimpleOverlayRenderer ME_OUTPUT_BUS_ACTIVE = new SimpleOverlayRenderer(
            "overlay/appeng/me_output_bus_active");
    public static final SimpleOverlayRenderer ME_INPUT_BUS = new SimpleOverlayRenderer("overlay/appeng/me_input_bus");
    public static final SimpleOverlayRenderer ME_INPUT_BUS_ACTIVE = new SimpleOverlayRenderer(
            "overlay/appeng/me_input_bus_active");

    public static final SimpleOverlayRenderer CONTROL_ROD = new SimpleOverlayRenderer(
            "overlay/machine/overlay_control_rod");
    public static final SimpleOverlayRenderer CONTROL_ROD_MODERATED = new SimpleOverlayRenderer(
            "overlay/machine/overlay_control_rod_moderated");

    public static final ResourceLocation ACE_CAPE_TEXTURE = gregtechId("textures/capes/acecape.png");
    public static final ResourceLocation AGENDER_CAPE_TEXTURE = gregtechId("textures/capes/agendercape.png");
    public static final ResourceLocation AROMANTIC_CAPE_TEXTURE = gregtechId("textures/capes/aromanticcape.png");
    public static final ResourceLocation BI_CAPE_TEXTURE = gregtechId("textures/capes/bicape.png");
    public static final ResourceLocation GENDERFLUID_CAPE_TEXTURE = gregtechId("textures/capes/genderfluidcape.png");
    public static final ResourceLocation GENDERQUEER_CAPE_TEXTURE = gregtechId("textures/capes/genderqueercape.png");
    public static final ResourceLocation GREEN_CAPE_TEXTURE = gregtechId("textures/capes/greencape.png");
    public static final ResourceLocation GREGTECH_CAPE_TEXTURE = gregtechId("textures/capes/gregtechcape.png");
    public static final ResourceLocation INTERSEX_CAPE_TEXTURE = gregtechId("textures/capes/intersexcape.png");
    public static final ResourceLocation LESBIAN_CAPE_TEXTURE = gregtechId("textures/capes/lesbiancape.png");
    public static final ResourceLocation NONBINARY_CAPE_TEXTURE = gregtechId("textures/capes/nonbinarycape.png");
    public static final ResourceLocation PAN_CAPE_TEXTURE = gregtechId("textures/capes/pancape.png");
    public static final ResourceLocation RAINBOW_CAPE_TEXTURE = gregtechId("textures/capes/rainbowcape.png");
    public static final ResourceLocation RED_CAPE_TEXTURE = gregtechId("textures/capes/redcape.png");
    public static final ResourceLocation TRANS_CAPE_TEXTURE = gregtechId("textures/capes/transcape.png");
    public static final ResourceLocation YELLOW_CAPE_TEXTURE = gregtechId("textures/capes/yellowcape.png");

    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite RESTRICTIVE_OVERLAY;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_TINY;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_SMALL;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_NORMAL;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_LARGE;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_HUGE;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_QUADRUPLE;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_NONUPLE;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_SIDE;

    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_SMALL_WOOD;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_NORMAL_WOOD;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_LARGE_WOOD;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_SIDE_WOOD;

    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite OPTICAL_PIPE_IN;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite OPTICAL_PIPE_SIDE;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite OPTICAL_PIPE_SIDE_OVERLAY;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite OPTICAL_PIPE_SIDE_OVERLAY_ACTIVE;

    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite LASER_PIPE_IN;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite LASER_PIPE_SIDE;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite LASER_PIPE_OVERLAY;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite LASER_PIPE_OVERLAY_EMISSIVE;

    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY_UP;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY_DOWN;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY_LEFT;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY_RIGHT;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY_NU;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY_ND;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY_NL;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY_NR;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY_UD;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY_UL;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY_UR;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY_DL;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY_DR;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite PIPE_BLOCKED_OVERLAY_LR;

    @SideOnly(Side.CLIENT)
    public static ThreadLocal<CubeRendererState> RENDER_STATE;

    static {
        for (int i = 0; i < VOLTAGE_CASINGS.length; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            VOLTAGE_CASINGS[i] = new SimpleSidedCubeRenderer("casings/voltage/" + voltageName);
        }
        if (GTValues.isClientSide()) {
            RENDER_STATE = new ThreadLocal<>();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void register(TextureMap textureMap) {
        GTLog.logger.info("Loading meta tile entity texture sprites...");
        for (IIconRegister iconRegister : iconRegisters) {
            iconRegister.registerIcons(textureMap);
        }

        RESTRICTIVE_OVERLAY = textureMap.registerSprite(gregtechId("blocks/pipe/pipe_restrictive"));
        PIPE_TINY = textureMap.registerSprite(gregtechId("blocks/pipe/pipe_tiny_in"));
        PIPE_SMALL = textureMap.registerSprite(gregtechId("blocks/pipe/pipe_small_in"));
        PIPE_NORMAL = textureMap.registerSprite(gregtechId("blocks/pipe/pipe_normal_in"));
        PIPE_LARGE = textureMap.registerSprite(gregtechId("blocks/pipe/pipe_large_in"));
        PIPE_HUGE = textureMap.registerSprite(gregtechId("blocks/pipe/pipe_huge_in"));
        PIPE_QUADRUPLE = textureMap.registerSprite(gregtechId("blocks/pipe/pipe_quadruple_in"));
        PIPE_NONUPLE = textureMap.registerSprite(gregtechId("blocks/pipe/pipe_nonuple_in"));
        PIPE_SIDE = textureMap.registerSprite(gregtechId("blocks/pipe/pipe_side"));
        PIPE_SMALL_WOOD = textureMap.registerSprite(gregtechId("blocks/pipe/pipe_small_in_wood"));
        PIPE_NORMAL_WOOD = textureMap.registerSprite(gregtechId("blocks/pipe/pipe_normal_in_wood"));
        PIPE_LARGE_WOOD = textureMap.registerSprite(gregtechId("blocks/pipe/pipe_large_in_wood"));
        PIPE_SIDE_WOOD = textureMap.registerSprite(gregtechId("blocks/pipe/pipe_side_wood"));

        // Fluid Pipe Blocked overlay textures
        PIPE_BLOCKED_OVERLAY = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked"));
        PIPE_BLOCKED_OVERLAY_UP = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked_up"));
        PIPE_BLOCKED_OVERLAY_DOWN = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked_down"));
        PIPE_BLOCKED_OVERLAY_LEFT = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked_left"));
        PIPE_BLOCKED_OVERLAY_RIGHT = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked_right"));
        PIPE_BLOCKED_OVERLAY_NU = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked_nu"));
        PIPE_BLOCKED_OVERLAY_ND = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked_nd"));
        PIPE_BLOCKED_OVERLAY_NL = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked_nl"));
        PIPE_BLOCKED_OVERLAY_NR = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked_nr"));
        PIPE_BLOCKED_OVERLAY_UD = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked_ud"));
        PIPE_BLOCKED_OVERLAY_UL = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked_ul"));
        PIPE_BLOCKED_OVERLAY_UR = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked_ur"));
        PIPE_BLOCKED_OVERLAY_DL = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked_dl"));
        PIPE_BLOCKED_OVERLAY_DR = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked_dr"));
        PIPE_BLOCKED_OVERLAY_LR = textureMap.registerSprite(gregtechId("blocks/pipe/blocked/pipe_blocked_lr"));

        OPTICAL_PIPE_IN = textureMap
                .registerSprite(new ResourceLocation(GTValues.MODID, "blocks/pipe/pipe_optical_in"));
        OPTICAL_PIPE_SIDE = textureMap
                .registerSprite(new ResourceLocation(GTValues.MODID, "blocks/pipe/pipe_optical_side"));
        OPTICAL_PIPE_SIDE_OVERLAY = textureMap
                .registerSprite(new ResourceLocation(GTValues.MODID, "blocks/pipe/pipe_optical_side_overlay"));
        OPTICAL_PIPE_SIDE_OVERLAY_ACTIVE = textureMap
                .registerSprite(new ResourceLocation(GTValues.MODID, "blocks/pipe/pipe_optical_side_overlay_active"));

        LASER_PIPE_SIDE = textureMap
                .registerSprite(new ResourceLocation(GTValues.MODID, "blocks/pipe/pipe_laser_side"));
        LASER_PIPE_IN = textureMap.registerSprite(new ResourceLocation(GTValues.MODID, "blocks/pipe/pipe_laser_in"));
        LASER_PIPE_OVERLAY = textureMap
                .registerSprite(new ResourceLocation(GTValues.MODID, "blocks/pipe/pipe_laser_side_overlay"));
        LASER_PIPE_OVERLAY_EMISSIVE = textureMap
                .registerSprite(new ResourceLocation(GTValues.MODID, "blocks/pipe/pipe_laser_side_overlay_emissive"));

        for (MaterialIconSet iconSet : MaterialIconSet.ICON_SETS.values()) {
            textureMap.registerSprite(MaterialIconType.frameGt.getBlockTexturePath(iconSet));
        }
    }

    private static int mask(EnumFacing... facings) {
        int mask = 0;
        for (EnumFacing facing : facings) {
            mask |= (1 << facing.ordinal());
        }
        return mask;
    }

    @SideOnly(Side.CLIENT)
    public static void renderFace(CCRenderState renderState, Matrix4 translation, IVertexOperation[] ops,
                                  EnumFacing face, Cuboid6 bounds, TextureAtlasSprite sprite, BlockRenderLayer layer) {
        CubeRendererState op = RENDER_STATE.get();
        if (layer != null && op != null && op.layer != null &&
                (op.layer != layer || !op.shouldSideBeRendered(face, bounds))) {
            return;
        }
        BlockFace blockFace = blockFaces.get();
        blockFace.loadCuboidFace(bounds, face.getIndex());
        UVTransformationList uvList = new UVTransformationList(new IconTransformation(sprite));
        if (face.getIndex() == 0) {
            uvList.prepend(new UVMirror(0, 0, bounds.min.z, bounds.max.z));
        }
        renderState.setPipeline(blockFace, 0, blockFace.verts.length,
                ArrayUtils.addAll(ops, new TransformationList(translation), uvList));
        renderState.render();
    }

    // TODO Could maybe be cleaned up?
    public static ICubeRenderer getInactiveTexture(ICubeRenderer renderer) {
        if (renderer == BRONZE_FIREBOX_ACTIVE) return BRONZE_FIREBOX;
        if (renderer == STEEL_FIREBOX_ACTIVE) return STEEL_FIREBOX;
        if (renderer == TITANIUM_FIREBOX_ACTIVE) return TITANIUM_FIREBOX;
        if (renderer == TUNGSTENSTEEL_FIREBOX_ACTIVE) return TUNGSTENSTEEL_FIREBOX;
        return renderer;
    }
}
