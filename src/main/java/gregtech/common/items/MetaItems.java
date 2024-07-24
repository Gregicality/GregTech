package gregtech.common.items;

import gregtech.api.GregTechAPI;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.handler.FacadeRenderer;
import gregtech.common.items.armor.MetaArmor;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.CaseFormat;

import java.util.*;

public final class MetaItems {

    private MetaItems() {}

    public static final List<MetaItem<?>> ITEMS = MetaItem.getMetaItems();

    public static MetaItem<?>.MetaValueItem CREDIT_COPPER;
    public static MetaItem<?>.MetaValueItem CREDIT_CUPRONICKEL;
    public static MetaItem<?>.MetaValueItem CREDIT_SILVER;
    public static MetaItem<?>.MetaValueItem CREDIT_GOLD;
    public static MetaItem<?>.MetaValueItem CREDIT_PLATINUM;
    public static MetaItem<?>.MetaValueItem CREDIT_OSMIUM;
    public static MetaItem<?>.MetaValueItem CREDIT_NAQUADAH;
    public static MetaItem<?>.MetaValueItem CREDIT_NEUTRONIUM;

    public static MetaItem<?>.MetaValueItem COIN_GOLD_ANCIENT;
    public static MetaItem<?>.MetaValueItem COIN_DOGE;
    public static MetaItem<?>.MetaValueItem COIN_CHOCOLATE;

    public static MetaItem<?>.MetaValueItem COMPRESSED_CLAY;
    public static MetaItem<?>.MetaValueItem COMPRESSED_COKE_CLAY;
    public static MetaItem<?>.MetaValueItem COMPRESSED_FIRECLAY;
    public static MetaItem<?>.MetaValueItem FIRECLAY_BRICK;
    public static MetaItem<?>.MetaValueItem COKE_OVEN_BRICK;

    public static MetaItem<?>.MetaValueItem WOODEN_FORM_EMPTY;
    public static MetaItem<?>.MetaValueItem WOODEN_FORM_BRICK;

    public static MetaItem<?>.MetaValueItem SHAPE_EMPTY;

    public static final MetaItem<?>.MetaValueItem[] SHAPE_MOLDS = new MetaValueItem[13];
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_PLATE;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_GEAR;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_CREDIT;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_BOTTLE;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_INGOT;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_BALL;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_BLOCK;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_NUGGET;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_CYLINDER;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_ANVIL;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_NAME;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_GEAR_SMALL;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_ROTOR;

    public static final MetaItem<?>.MetaValueItem[] SHAPE_EXTRUDERS = new MetaValueItem[27];
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_PLATE;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_ROD;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_BOLT;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_RING;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_CELL;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_INGOT;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_WIRE;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_PIPE_TINY;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_PIPE_SMALL;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_PIPE_NORMAL;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_PIPE_LARGE;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_PIPE_HUGE;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_BLOCK;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_GEAR;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_BOTTLE;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_FOIL;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_GEAR_SMALL;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_ROD_LONG;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_ROTOR;

    public static MetaItem<?>.MetaValueItem SPRAY_SOLVENT;
    public static MetaItem<?>.MetaValueItem SPRAY_EMPTY;

    public static MetaItem<?>.MetaValueItem FLUID_CELL;
    public static MetaItem<?>.MetaValueItem FLUID_CELL_UNIVERSAL;
    public static MetaItem<?>.MetaValueItem FLUID_CELL_LARGE_STEEL;
    public static MetaItem<?>.MetaValueItem FLUID_CELL_LARGE_ALUMINIUM;
    public static MetaItem<?>.MetaValueItem FLUID_CELL_LARGE_STAINLESS_STEEL;
    public static MetaItem<?>.MetaValueItem FLUID_CELL_LARGE_TITANIUM;
    public static MetaItem<?>.MetaValueItem FLUID_CELL_LARGE_TUNGSTEN_STEEL;
    public static MetaItem<?>.MetaValueItem FLUID_CELL_GLASS_VIAL;

    public static MetaItem<?>.MetaValueItem TOOL_MATCHES;
    public static MetaItem<?>.MetaValueItem TOOL_MATCHBOX;
    public static MetaItem<?>.MetaValueItem TOOL_LIGHTER_INVAR;
    public static MetaItem<?>.MetaValueItem TOOL_LIGHTER_PLATINUM;

    public static MetaItem<?>.MetaValueItem CARBON_FIBERS;
    public static MetaItem<?>.MetaValueItem CARBON_MESH;
    public static MetaItem<?>.MetaValueItem CARBON_FIBER_PLATE;
    public static MetaItem<?>.MetaValueItem DUCT_TAPE;
    public static MetaItem<?>.MetaValueItem BASIC_TAPE;

    public static MetaItem<?>.MetaValueItem NEUTRON_REFLECTOR;

    public static MetaItem<?>.MetaValueItem BATTERY_HULL_LV;
    public static MetaItem<?>.MetaValueItem BATTERY_HULL_MV;
    public static MetaItem<?>.MetaValueItem BATTERY_HULL_HV;
    public static MetaItem<?>.MetaValueItem BATTERY_HULL_SMALL_VANADIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_HULL_MEDIUM_VANADIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_HULL_LARGE_VANADIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_HULL_MEDIUM_NAQUADRIA;
    public static MetaItem<?>.MetaValueItem BATTERY_HULL_LARGE_NAQUADRIA;

    public static MetaItem<?>.MetaValueItem BATTERY_ULV_TANTALUM;
    public static MetaItem<?>.MetaValueItem BATTERY_LV_CADMIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_LV_LITHIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_LV_SODIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_MV_CADMIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_MV_LITHIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_MV_SODIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_HV_CADMIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_HV_LITHIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_HV_SODIUM;
    public static MetaItem<?>.MetaValueItem ENERGIUM_CRYSTAL;
    public static MetaItem<?>.MetaValueItem LAPOTRON_CRYSTAL;

    public static MetaItem<?>.MetaValueItem BATTERY_EV_VANADIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_IV_VANADIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_LUV_VANADIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_ZPM_NAQUADRIA;
    public static MetaItem<?>.MetaValueItem BATTERY_UV_NAQUADRIA;

    public static MetaItem<?>.MetaValueItem ENERGY_LAPOTRONIC_ORB;
    public static MetaItem<?>.MetaValueItem ENERGY_LAPOTRONIC_ORB_CLUSTER;
    public static MetaItem<?>.MetaValueItem ZERO_POINT_MODULE;
    public static MetaItem<?>.MetaValueItem ULTIMATE_BATTERY;

    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_LV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_MV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_HV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_EV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_IV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_LuV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_ZPM;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_UV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_UHV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_UEV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_UIV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_UXV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_OpV;

    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_LV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_MV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_HV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_EV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_IV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_LuV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_ZPM;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_UV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_UHV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_UEV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_UIV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_UXV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_OpV;

    public static MetaItem<?>.MetaValueItem FLUID_REGULATOR_LV;
    public static MetaItem<?>.MetaValueItem FLUID_REGULATOR_MV;
    public static MetaItem<?>.MetaValueItem FLUID_REGULATOR_HV;
    public static MetaItem<?>.MetaValueItem FLUID_REGULATOR_EV;
    public static MetaItem<?>.MetaValueItem FLUID_REGULATOR_IV;
    public static MetaItem<?>.MetaValueItem FLUID_REGULATOR_LUV;
    public static MetaItem<?>.MetaValueItem FLUID_REGULATOR_ZPM;
    public static MetaItem<?>.MetaValueItem FLUID_REGULATOR_UV;

    public static MetaItem<?>.MetaValueItem FLUID_FILTER;

    public static MetaItem<?>.MetaValueItem DYNAMITE;

    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_LV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_MV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_HV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_EV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_IV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_LuV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_ZPM;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_UV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_UHV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_UEV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_UIV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_UXV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_OpV;

    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_LV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_MV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_HV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_EV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_IV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_LUV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_ZPM;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_UV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_UHV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_UEV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_UIV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_UXV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_OpV;

    public static MetaItem<?>.MetaValueItem ROBOT_ARM_LV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_MV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_HV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_EV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_IV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_LuV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_ZPM;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_UV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_UHV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_UEV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_UIV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_UXV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_OpV;

    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_LV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_MV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_HV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_EV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_IV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_LuV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_ZPM;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_UV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_UHV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_UEV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_UIV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_UXV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_OpV;

    public static MetaItem<?>.MetaValueItem EMITTER_LV;
    public static MetaItem<?>.MetaValueItem EMITTER_MV;
    public static MetaItem<?>.MetaValueItem EMITTER_HV;
    public static MetaItem<?>.MetaValueItem EMITTER_EV;
    public static MetaItem<?>.MetaValueItem EMITTER_IV;
    public static MetaItem<?>.MetaValueItem EMITTER_LuV;
    public static MetaItem<?>.MetaValueItem EMITTER_ZPM;
    public static MetaItem<?>.MetaValueItem EMITTER_UV;
    public static MetaItem<?>.MetaValueItem EMITTER_UHV;
    public static MetaItem<?>.MetaValueItem EMITTER_UEV;
    public static MetaItem<?>.MetaValueItem EMITTER_UIV;
    public static MetaItem<?>.MetaValueItem EMITTER_UXV;
    public static MetaItem<?>.MetaValueItem EMITTER_OpV;

    public static MetaItem<?>.MetaValueItem SENSOR_LV;
    public static MetaItem<?>.MetaValueItem SENSOR_MV;
    public static MetaItem<?>.MetaValueItem SENSOR_HV;
    public static MetaItem<?>.MetaValueItem SENSOR_EV;
    public static MetaItem<?>.MetaValueItem SENSOR_IV;
    public static MetaItem<?>.MetaValueItem SENSOR_LuV;
    public static MetaItem<?>.MetaValueItem SENSOR_ZPM;
    public static MetaItem<?>.MetaValueItem SENSOR_UV;
    public static MetaItem<?>.MetaValueItem SENSOR_UHV;
    public static MetaItem<?>.MetaValueItem SENSOR_UEV;
    public static MetaItem<?>.MetaValueItem SENSOR_UIV;
    public static MetaItem<?>.MetaValueItem SENSOR_UXV;
    public static MetaItem<?>.MetaValueItem SENSOR_OpV;

    public static MetaItem<?>.MetaValueItem TOOL_DATA_STICK;
    public static MetaItem<?>.MetaValueItem TOOL_DATA_ORB;
    public static MetaItem<?>.MetaValueItem TOOL_DATA_MODULE;

    public static final Map<MarkerMaterial, MetaValueItem> GLASS_LENSES = new HashMap<>();

    public static MetaItem<?>.MetaValueItem SILICON_BOULE;
    public static MetaItem<?>.MetaValueItem PHOSPHORUS_BOULE;
    public static MetaItem<?>.MetaValueItem NAQUADAH_BOULE;
    public static MetaItem<?>.MetaValueItem NEUTRONIUM_BOULE;
    public static MetaItem<?>.MetaValueItem SILICON_WAFER;
    public static MetaItem<?>.MetaValueItem PHOSPHORUS_WAFER;
    public static MetaItem<?>.MetaValueItem NAQUADAH_WAFER;
    public static MetaItem<?>.MetaValueItem NEUTRONIUM_WAFER;

    public static MetaItem<?>.MetaValueItem HIGHLY_ADVANCED_SOC_WAFER;
    public static MetaItem<?>.MetaValueItem ADVANCED_SYSTEM_ON_CHIP_WAFER;
    public static MetaItem<?>.MetaValueItem INTEGRATED_LOGIC_CIRCUIT_WAFER;
    public static MetaItem<?>.MetaValueItem CENTRAL_PROCESSING_UNIT_WAFER;
    public static MetaItem<?>.MetaValueItem ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER;
    public static MetaItem<?>.MetaValueItem LOW_POWER_INTEGRATED_CIRCUIT_WAFER;
    public static MetaItem<?>.MetaValueItem POWER_INTEGRATED_CIRCUIT_WAFER;
    public static MetaItem<?>.MetaValueItem HIGH_POWER_INTEGRATED_CIRCUIT_WAFER;
    public static MetaItem<?>.MetaValueItem ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER;
    public static MetaItem<?>.MetaValueItem NAND_MEMORY_CHIP_WAFER;
    public static MetaItem<?>.MetaValueItem NANO_CENTRAL_PROCESSING_UNIT_WAFER;
    public static MetaItem<?>.MetaValueItem NOR_MEMORY_CHIP_WAFER;
    public static MetaItem<?>.MetaValueItem QUBIT_CENTRAL_PROCESSING_UNIT_WAFER;
    public static MetaItem<?>.MetaValueItem RANDOM_ACCESS_MEMORY_WAFER;
    public static MetaItem<?>.MetaValueItem SYSTEM_ON_CHIP_WAFER;
    public static MetaItem<?>.MetaValueItem SIMPLE_SYSTEM_ON_CHIP_WAFER;

    public static MetaItem<?>.MetaValueItem ENGRAVED_CRYSTAL_CHIP;
    public static MetaItem<?>.MetaValueItem ENGRAVED_LAPOTRON_CHIP;

    public static MetaItem<?>.MetaValueItem HIGHLY_ADVANCED_SOC;
    public static MetaItem<?>.MetaValueItem ADVANCED_SYSTEM_ON_CHIP;
    public static MetaItem<?>.MetaValueItem INTEGRATED_LOGIC_CIRCUIT;
    public static MetaItem<?>.MetaValueItem CENTRAL_PROCESSING_UNIT;
    public static MetaItem<?>.MetaValueItem ULTRA_LOW_POWER_INTEGRATED_CIRCUIT;
    public static MetaItem<?>.MetaValueItem LOW_POWER_INTEGRATED_CIRCUIT;
    public static MetaItem<?>.MetaValueItem POWER_INTEGRATED_CIRCUIT;
    public static MetaItem<?>.MetaValueItem HIGH_POWER_INTEGRATED_CIRCUIT;
    public static MetaItem<?>.MetaValueItem ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT;
    public static MetaItem<?>.MetaValueItem NAND_MEMORY_CHIP;
    public static MetaItem<?>.MetaValueItem NANO_CENTRAL_PROCESSING_UNIT;
    public static MetaItem<?>.MetaValueItem NOR_MEMORY_CHIP;
    public static MetaItem<?>.MetaValueItem QUBIT_CENTRAL_PROCESSING_UNIT;
    public static MetaItem<?>.MetaValueItem RANDOM_ACCESS_MEMORY;
    public static MetaItem<?>.MetaValueItem SYSTEM_ON_CHIP;
    public static MetaItem<?>.MetaValueItem SIMPLE_SYSTEM_ON_CHIP;

    public static MetaItem<?>.MetaValueItem RAW_CRYSTAL_CHIP;
    public static MetaItem<?>.MetaValueItem RAW_CRYSTAL_CHIP_PART;
    public static MetaItem<?>.MetaValueItem CRYSTAL_CENTRAL_PROCESSING_UNIT;
    public static MetaItem<?>.MetaValueItem CRYSTAL_SYSTEM_ON_CHIP;

    public static MetaItem<?>.MetaValueItem COATED_BOARD;
    public static MetaItem<?>.MetaValueItem PHENOLIC_BOARD;
    public static MetaItem<?>.MetaValueItem PLASTIC_BOARD;
    public static MetaItem<?>.MetaValueItem EPOXY_BOARD;
    public static MetaItem<?>.MetaValueItem FIBER_BOARD;
    public static MetaItem<?>.MetaValueItem MULTILAYER_FIBER_BOARD;
    public static MetaItem<?>.MetaValueItem WETWARE_BOARD;

    public static MetaItem<?>.MetaValueItem BASIC_CIRCUIT_BOARD;
    public static MetaItem<?>.MetaValueItem GOOD_CIRCUIT_BOARD;
    public static MetaItem<?>.MetaValueItem PLASTIC_CIRCUIT_BOARD;
    public static MetaItem<?>.MetaValueItem ADVANCED_CIRCUIT_BOARD;
    public static MetaItem<?>.MetaValueItem EXTREME_CIRCUIT_BOARD;
    public static MetaItem<?>.MetaValueItem ELITE_CIRCUIT_BOARD;
    public static MetaItem<?>.MetaValueItem WETWARE_CIRCUIT_BOARD;

    public static MetaItem<?>.MetaValueItem VACUUM_TUBE;
    public static MetaItem<?>.MetaValueItem GLASS_TUBE;
    public static MetaItem<?>.MetaValueItem RESISTOR;
    public static MetaItem<?>.MetaValueItem DIODE;
    public static MetaItem<?>.MetaValueItem CAPACITOR;
    public static MetaItem<?>.MetaValueItem TRANSISTOR;
    public static MetaItem<?>.MetaValueItem INDUCTOR;
    public static MetaItem<?>.MetaValueItem SMD_CAPACITOR;
    public static MetaItem<?>.MetaValueItem SMD_DIODE;
    public static MetaItem<?>.MetaValueItem SMD_RESISTOR;
    public static MetaItem<?>.MetaValueItem SMD_TRANSISTOR;
    public static MetaItem<?>.MetaValueItem SMD_INDUCTOR;
    public static MetaItem<?>.MetaValueItem ADVANCED_SMD_CAPACITOR;
    public static MetaItem<?>.MetaValueItem ADVANCED_SMD_DIODE;
    public static MetaItem<?>.MetaValueItem ADVANCED_SMD_RESISTOR;
    public static MetaItem<?>.MetaValueItem ADVANCED_SMD_TRANSISTOR;
    public static MetaItem<?>.MetaValueItem ADVANCED_SMD_INDUCTOR;

    // T1: Electronic
    public static MetaItem<?>.MetaValueItem ELECTRONIC_CIRCUIT_LV;
    public static MetaItem<?>.MetaValueItem ELECTRONIC_CIRCUIT_MV;

    // T2: Integrated
    public static MetaItem<?>.MetaValueItem INTEGRATED_CIRCUIT_LV;
    public static MetaItem<?>.MetaValueItem INTEGRATED_CIRCUIT_MV;
    public static MetaItem<?>.MetaValueItem INTEGRATED_CIRCUIT_HV;

    // ULV/LV easier circuits
    public static MetaItem<?>.MetaValueItem NAND_CHIP_ULV;
    public static MetaItem<?>.MetaValueItem MICROPROCESSOR_LV;

    // T3: Processor
    public static MetaItem<?>.MetaValueItem PROCESSOR_MV;
    public static MetaItem<?>.MetaValueItem PROCESSOR_ASSEMBLY_HV;
    public static MetaItem<?>.MetaValueItem WORKSTATION_EV;
    public static MetaItem<?>.MetaValueItem MAINFRAME_IV;

    // T4: Nano
    public static MetaItem<?>.MetaValueItem NANO_PROCESSOR_HV;
    public static MetaItem<?>.MetaValueItem NANO_PROCESSOR_ASSEMBLY_EV;
    public static MetaItem<?>.MetaValueItem NANO_COMPUTER_IV;
    public static MetaItem<?>.MetaValueItem NANO_MAINFRAME_LUV;

    // T5: Quantum
    public static MetaItem<?>.MetaValueItem QUANTUM_PROCESSOR_EV;
    public static MetaItem<?>.MetaValueItem QUANTUM_ASSEMBLY_IV;
    public static MetaItem<?>.MetaValueItem QUANTUM_COMPUTER_LUV;
    public static MetaItem<?>.MetaValueItem QUANTUM_MAINFRAME_ZPM;

    // T6: Crystal
    public static MetaItem<?>.MetaValueItem CRYSTAL_PROCESSOR_IV;
    public static MetaItem<?>.MetaValueItem CRYSTAL_ASSEMBLY_LUV;
    public static MetaItem<?>.MetaValueItem CRYSTAL_COMPUTER_ZPM;
    public static MetaItem<?>.MetaValueItem CRYSTAL_MAINFRAME_UV;

    // T7: Wetware
    public static MetaItem<?>.MetaValueItem WETWARE_PROCESSOR_LUV;
    public static MetaItem<?>.MetaValueItem WETWARE_PROCESSOR_ASSEMBLY_ZPM;
    public static MetaItem<?>.MetaValueItem WETWARE_SUPER_COMPUTER_UV;
    public static MetaItem<?>.MetaValueItem WETWARE_MAINFRAME_UHV;

    public static MetaItem<?>.MetaValueItem COMPONENT_GRINDER_DIAMOND;
    public static MetaItem<?>.MetaValueItem COMPONENT_GRINDER_TUNGSTEN;

    public static MetaItem<?>.MetaValueItem IRON_MINECART_WHEELS;
    public static MetaItem<?>.MetaValueItem STEEL_MINECART_WHEELS;

    public static MetaItem<?>.MetaValueItem QUANTUM_EYE;
    public static MetaItem<?>.MetaValueItem QUANTUM_STAR;
    public static MetaItem<?>.MetaValueItem GRAVI_STAR;

    public static MetaItem<?>.MetaValueItem ITEM_FILTER;
    public static MetaItem<?>.MetaValueItem ORE_DICTIONARY_FILTER;
    public static MetaItem<?>.MetaValueItem SMART_FILTER;

    public static MetaItem<?>.MetaValueItem COVER_SHUTTER;
    public static MetaItem<?>.MetaValueItem COVER_MACHINE_CONTROLLER;
    public static MetaItem<?>.MetaValueItem COVER_FACADE;

    public static MetaItem<?>.MetaValueItem COVER_ACTIVITY_DETECTOR;
    public static MetaItem<?>.MetaValueItem COVER_ACTIVITY_DETECTOR_ADVANCED;
    public static MetaItem<?>.MetaValueItem COVER_FLUID_DETECTOR;
    public static MetaItem<?>.MetaValueItem COVER_FLUID_DETECTOR_ADVANCED;
    public static MetaItem<?>.MetaValueItem COVER_ITEM_DETECTOR;
    public static MetaItem<?>.MetaValueItem COVER_ITEM_DETECTOR_ADVANCED;
    public static MetaItem<?>.MetaValueItem COVER_ENERGY_DETECTOR;
    public static MetaItem<?>.MetaValueItem COVER_ENERGY_DETECTOR_ADVANCED;
    public static MetaItem<?>.MetaValueItem COVER_MAINTENANCE_DETECTOR;

    public static MetaItem<?>.MetaValueItem COVER_SCREEN;
    public static MetaItem<?>.MetaValueItem COVER_INFINITE_WATER;
    public static MetaItem<?>.MetaValueItem COVER_ENDER_FLUID_LINK;
    public static MetaItem<?>.MetaValueItem COVER_DIGITAL_INTERFACE;
    public static MetaItem<?>.MetaValueItem COVER_DIGITAL_INTERFACE_WIRELESS;
    public static MetaItem<?>.MetaValueItem COVER_FLUID_VOIDING;
    public static MetaItem<?>.MetaValueItem COVER_FLUID_VOIDING_ADVANCED;
    public static MetaItem<?>.MetaValueItem COVER_ITEM_VOIDING;
    public static MetaItem<?>.MetaValueItem COVER_ITEM_VOIDING_ADVANCED;
    public static MetaItem<?>.MetaValueItem COVER_STORAGE;

    public static MetaItem<?>.MetaValueItem COVER_SOLAR_PANEL;
    public static MetaItem<?>.MetaValueItem COVER_SOLAR_PANEL_ULV;
    public static MetaItem<?>.MetaValueItem COVER_SOLAR_PANEL_LV;
    public static MetaItem<?>.MetaValueItem COVER_SOLAR_PANEL_MV;
    public static MetaItem<?>.MetaValueItem COVER_SOLAR_PANEL_HV;
    public static MetaItem<?>.MetaValueItem COVER_SOLAR_PANEL_EV;
    public static MetaItem<?>.MetaValueItem COVER_SOLAR_PANEL_IV;
    public static MetaItem<?>.MetaValueItem COVER_SOLAR_PANEL_LUV;
    public static MetaItem<?>.MetaValueItem COVER_SOLAR_PANEL_ZPM;
    public static MetaItem<?>.MetaValueItem COVER_SOLAR_PANEL_UV;

    public static MetaItem<?>.MetaValueItem PLUGIN_TEXT;
    public static MetaItem<?>.MetaValueItem PLUGIN_ONLINE_PIC;
    public static MetaItem<?>.MetaValueItem PLUGIN_FAKE_GUI;
    public static MetaItem<?>.MetaValueItem PLUGIN_ADVANCED_MONITOR;

    public static MetaItem<?>.MetaValueItem INTEGRATED_CIRCUIT;

    public static MetaItem<?>.MetaValueItem FOAM_SPRAYER;

    public static MetaItem<?>.MetaValueItem GELLED_TOLUENE;

    public static MetaItem<?>.MetaValueItem BOTTLE_PURPLE_DRINK;

    public static MetaItem<?>.MetaValueItem PLANT_BALL;
    public static MetaItem<?>.MetaValueItem STICKY_RESIN;
    public static MetaItem<?>.MetaValueItem ENERGIUM_DUST;

    public static MetaItem<?>.MetaValueItem POWER_UNIT_LV;
    public static MetaItem<?>.MetaValueItem POWER_UNIT_MV;
    public static MetaItem<?>.MetaValueItem POWER_UNIT_HV;
    public static MetaItem<?>.MetaValueItem POWER_UNIT_EV;
    public static MetaItem<?>.MetaValueItem POWER_UNIT_IV;

    public static MetaItem<?>.MetaValueItem NANO_SABER;
    public static MetaItem<?>.MetaValueItem PROSPECTOR_LV;
    public static MetaItem<?>.MetaValueItem PROSPECTOR_HV;
    public static MetaItem<?>.MetaValueItem PROSPECTOR_LUV;

    public static MetaItem<?>.MetaValueItem TRICORDER_SCANNER;
    public static MetaItem<?>.MetaValueItem DEBUG_SCANNER;

    public static MetaItem<?>.MetaValueItem ITEM_MAGNET_LV;
    public static MetaItem<?>.MetaValueItem ITEM_MAGNET_HV;

    public static MetaItem<?>.MetaValueItem RUBBER_WOOD_BOAT;
    public static MetaItem<?>.MetaValueItem TREATED_WOOD_BOAT;

    public static MetaItem<?>.MetaValueItem RUBBER_WOOD_DOOR;
    public static MetaItem<?>.MetaValueItem TREATED_WOOD_DOOR;

    public static MetaItem<?>.MetaValueItem WIRELESS;
    public static MetaItem<?>.MetaValueItem CAMERA;
    public static MetaItem<?>.MetaValueItem TERMINAL;

    public static final MetaItem<?>.MetaValueItem[] DYE_ONLY_ITEMS = new MetaItem.MetaValueItem[EnumDyeColor
            .values().length];
    public static final MetaItem<?>.MetaValueItem[] SPRAY_CAN_DYES = new MetaItem.MetaValueItem[EnumDyeColor
            .values().length];

    public static MetaItem<?>.MetaValueItem TURBINE_ROTOR;

    public static MetaItem<?>.MetaValueItem ENERGY_MODULE;
    public static MetaItem<?>.MetaValueItem ENERGY_CLUSTER;
    public static MetaItem<?>.MetaValueItem NEURO_PROCESSOR;
    public static MetaItem<?>.MetaValueItem STEM_CELLS;
    public static MetaItem<?>.MetaValueItem PETRI_DISH;

    public static MetaItem<?>.MetaValueItem BIO_CHAFF;

    public static MetaItem<?>.MetaValueItem VOLTAGE_COIL_ULV;
    public static MetaItem<?>.MetaValueItem VOLTAGE_COIL_LV;
    public static MetaItem<?>.MetaValueItem VOLTAGE_COIL_MV;
    public static MetaItem<?>.MetaValueItem VOLTAGE_COIL_HV;
    public static MetaItem<?>.MetaValueItem VOLTAGE_COIL_EV;
    public static MetaItem<?>.MetaValueItem VOLTAGE_COIL_IV;
    public static MetaItem<?>.MetaValueItem VOLTAGE_COIL_LuV;
    public static MetaItem<?>.MetaValueItem VOLTAGE_COIL_ZPM;
    public static MetaItem<?>.MetaValueItem VOLTAGE_COIL_UV;

    public static MetaItem<?>.MetaValueItem CLIPBOARD;

    public static ArmorMetaItem<?>.ArmorMetaValueItem NIGHTVISION_GOGGLES;

    public static ArmorMetaItem<?>.ArmorMetaValueItem NANO_CHESTPLATE;
    public static ArmorMetaItem<?>.ArmorMetaValueItem NANO_LEGGINGS;
    public static ArmorMetaItem<?>.ArmorMetaValueItem NANO_BOOTS;
    public static ArmorMetaItem<?>.ArmorMetaValueItem NANO_HELMET;

    public static ArmorMetaItem<?>.ArmorMetaValueItem QUANTUM_CHESTPLATE;
    public static ArmorMetaItem<?>.ArmorMetaValueItem QUANTUM_LEGGINGS;
    public static ArmorMetaItem<?>.ArmorMetaValueItem QUANTUM_BOOTS;
    public static ArmorMetaItem<?>.ArmorMetaValueItem QUANTUM_HELMET;

    public static ArmorMetaItem<?>.ArmorMetaValueItem SEMIFLUID_JETPACK;
    public static ArmorMetaItem<?>.ArmorMetaValueItem ELECTRIC_JETPACK;

    public static ArmorMetaItem<?>.ArmorMetaValueItem ELECTRIC_JETPACK_ADVANCED;
    public static ArmorMetaItem<?>.ArmorMetaValueItem NANO_CHESTPLATE_ADVANCED;
    public static ArmorMetaItem<?>.ArmorMetaValueItem QUANTUM_CHESTPLATE_ADVANCED;

    public static MetaItem<?>.MetaValueItem POWER_THRUSTER;
    public static MetaItem<?>.MetaValueItem POWER_THRUSTER_ADVANCED;
    public static MetaItem<?>.MetaValueItem GRAVITATION_ENGINE;

    public static MetaItem<?>.MetaValueItem SUS_RECORD;
    public static MetaItem<?>.MetaValueItem NAN_CERTIFICATE;

    public static MetaItem<?>.MetaValueItem FERTILIZER;
    public static MetaItem<?>.MetaValueItem BLACKLIGHT;

    public static MetaItem<?>.MetaValueItem LOGO;

    public static MetaItem<?>.MetaValueItem MULTIBLOCK_BUILDER;

    // Nuclear
    public static MetaItem<?>.MetaValueItem ANODE_BASKET;
    public static MetaItem<?>.MetaValueItem FUEL_CLADDING;

    private static final List<OrePrefix> orePrefixes = new ArrayList<>();

    static {
        orePrefixes.add(OrePrefix.dust);
        orePrefixes.add(OrePrefix.dustSmall);
        orePrefixes.add(OrePrefix.dustTiny);
        orePrefixes.add(OrePrefix.dustImpure);
        orePrefixes.add(OrePrefix.dustPure);
        orePrefixes.add(OrePrefix.crushed);
        orePrefixes.add(OrePrefix.crushedPurified);
        orePrefixes.add(OrePrefix.crushedCentrifuged);
        orePrefixes.add(OrePrefix.gem);
        orePrefixes.add(OrePrefix.gemChipped);
        orePrefixes.add(OrePrefix.gemFlawed);
        orePrefixes.add(OrePrefix.gemFlawless);
        orePrefixes.add(OrePrefix.gemExquisite);
        orePrefixes.add(OrePrefix.ingot);
        orePrefixes.add(OrePrefix.ingotHot);
        orePrefixes.add(OrePrefix.plate);
        orePrefixes.add(OrePrefix.plateDouble);
        orePrefixes.add(OrePrefix.plateDense);
        orePrefixes.add(OrePrefix.foil);
        orePrefixes.add(OrePrefix.stick);
        orePrefixes.add(OrePrefix.stickLong);
        orePrefixes.add(OrePrefix.bolt);
        orePrefixes.add(OrePrefix.screw);
        orePrefixes.add(OrePrefix.ring);
        orePrefixes.add(OrePrefix.nugget);
        orePrefixes.add(OrePrefix.round);
        orePrefixes.add(OrePrefix.spring);
        orePrefixes.add(OrePrefix.springSmall);
        orePrefixes.add(OrePrefix.gear);
        orePrefixes.add(OrePrefix.gearSmall);
        orePrefixes.add(OrePrefix.wireFine);
        orePrefixes.add(OrePrefix.rotor);
        orePrefixes.add(OrePrefix.lens);
        orePrefixes.add(OrePrefix.turbineBlade);
        orePrefixes.add(OrePrefix.toolHeadDrill);
        orePrefixes.add(OrePrefix.toolHeadChainsaw);
        orePrefixes.add(OrePrefix.toolHeadWrench);
        orePrefixes.add(OrePrefix.toolHeadBuzzSaw);
        orePrefixes.add(OrePrefix.toolHeadScrewdriver);
        orePrefixes.add(OrePrefix.fuelRod);
        orePrefixes.add(OrePrefix.fuelRodDepleted);
        orePrefixes.add(OrePrefix.fuelRodHotDepleted);
        orePrefixes.add(OrePrefix.fuelPellet);
        orePrefixes.add(OrePrefix.fuelPelletDepleted);
        orePrefixes.add(OrePrefix.dustSpentFuel);
        orePrefixes.add(OrePrefix.dustBredFuel);
        orePrefixes.add(OrePrefix.dustFissionByproduct);
    }

    public static void init() {
        MetaItem1 first = new MetaItem1();
        first.setRegistryName("meta_item_1");
        MetaArmor armor = new MetaArmor();
        armor.setRegistryName("gt_armor");
        for (OrePrefix prefix : orePrefixes) {
            for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
                String regName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, prefix.name());
                MetaPrefixItem metaOrePrefix = new MetaPrefixItem(registry, prefix);
                metaOrePrefix.setRegistryName(registry.getModid(), String.format("meta_%s", regName));
            }
        }
    }

    public static void registerOreDict() {
        for (MetaItem<?> item : ITEMS) {
            if (item instanceof MetaPrefixItem) {
                ((MetaPrefixItem) item).registerOreDict();
            }
        }
        for (Map.Entry<MarkerMaterial, MetaValueItem> entry : GLASS_LENSES.entrySet()) {
            // Register "craftingLensWhite" for example
            OreDictUnifier.registerOre(entry.getValue().getStackForm(), OrePrefix.craftingLens, entry.getKey());
            // Register "craftingLensGlass", intended only for recipes to dye lenses and not in the Engraver
            OreDictUnifier.registerOre(entry.getValue().getStackForm(),
                    String.format("%s%s", OrePrefix.craftingLens.name(), "Glass"));
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        MinecraftForge.EVENT_BUS.register(MetaItems.class);
        for (MetaItem<?> item : ITEMS) {
            item.registerModels();
            item.registerTextureMesh();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerColors() {
        for (MetaItem<?> item : ITEMS) {
            item.registerColor();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerBakedModels(ModelBakeEvent event) {
        GTLog.logger.info("Registering special item models");
        registerSpecialItemModel(event, COVER_FACADE, new FacadeRenderer());
    }

    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void registerSpecialItemModel(ModelBakeEvent event, MetaValueItem metaValueItem,
                                                 IBakedModel bakedModel) {
        // noinspection RedundantCast
        ResourceLocation modelPath = ((MetaItem) metaValueItem.getMetaItem()).createItemModelPath(metaValueItem, "");
        ModelResourceLocation modelResourceLocation = new ModelResourceLocation(modelPath, "inventory");
        event.getModelRegistry().putObject(modelResourceLocation, bakedModel);
    }

    @SuppressWarnings("unused")
    public static void addOrePrefix(OrePrefix... prefixes) {
        orePrefixes.addAll(Arrays.asList(prefixes));
    }
}
