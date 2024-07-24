package gregtech.common.items;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.metaitem.FilteredFluidStats;
import gregtech.api.items.metaitem.FoodStats;
import gregtech.api.items.metaitem.MusicDiscStats;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.api.items.metaitem.stats.IItemContainerItemProvider;
import gregtech.api.items.metaitem.stats.ItemFluidContainer;
import gregtech.api.terminal.hardware.HardwareProvider;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.MarkerMaterials.Component;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTUtility;
import gregtech.api.util.RandomPotionEffect;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.covers.filter.IFilter;
import gregtech.common.covers.filter.OreDictionaryItemFilter;
import gregtech.common.covers.filter.SimpleFluidFilter;
import gregtech.common.covers.filter.SimpleItemFilter;
import gregtech.common.covers.filter.SmartItemFilter;
import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.common.entities.GTBoatEntity.GTBoatType;
import gregtech.common.items.behaviors.ClipboardBehavior;
import gregtech.common.items.behaviors.ColorSprayBehaviour;
import gregtech.common.items.behaviors.DataItemBehavior;
import gregtech.common.items.behaviors.DoorBehavior;
import gregtech.common.items.behaviors.DynamiteBehaviour;
import gregtech.common.items.behaviors.FacadeItem;
import gregtech.common.items.behaviors.FertilizerBehavior;
import gregtech.common.items.behaviors.FoamSprayerBehavior;
import gregtech.common.items.behaviors.GTBoatBehavior;
import gregtech.common.items.behaviors.IntCircuitBehaviour;
import gregtech.common.items.behaviors.ItemMagnetBehavior;
import gregtech.common.items.behaviors.LighterBehaviour;
import gregtech.common.items.behaviors.MultiblockBuilderBehavior;
import gregtech.common.items.behaviors.NanoSaberBehavior;
import gregtech.common.items.behaviors.ProspectorScannerBehavior;
import gregtech.common.items.behaviors.TerminalBehaviour;
import gregtech.common.items.behaviors.TooltipBehavior;
import gregtech.common.items.behaviors.TricorderBehavior;
import gregtech.common.items.behaviors.TurbineRotorBehavior;
import gregtech.common.items.behaviors.filter.OreDictFilterUIManager;
import gregtech.common.items.behaviors.filter.SimpleFilterUIManager;
import gregtech.common.items.behaviors.filter.SimpleFluidFilterUIManager;
import gregtech.common.items.behaviors.filter.SmartFilterUIManager;
import gregtech.common.items.behaviors.monitorplugin.AdvancedMonitorPluginBehavior;
import gregtech.common.items.behaviors.monitorplugin.FakeGuiPluginBehavior;
import gregtech.common.items.behaviors.monitorplugin.OnlinePicPluginBehavior;
import gregtech.common.items.behaviors.monitorplugin.TextPluginBehavior;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static gregtech.api.GTValues.M;
import static gregtech.api.util.DyeUtil.getOredictColorName;
import static gregtech.common.items.MetaItems.*;

public class MetaItem1 extends StandardMetaItem {

    public MetaItem1() {
        super();
    }

    @Override
    public void registerSubItems() {
        // Credits: ID 0-10
        CREDIT_COPPER = addItem(0, "credit.copper");
        CREDIT_CUPRONICKEL = addItem(1, "credit.cupronickel");
        CREDIT_SILVER = addItem(2, "credit.silver").setRarity(EnumRarity.UNCOMMON);
        CREDIT_GOLD = addItem(3, "credit.gold").setRarity(EnumRarity.UNCOMMON);
        CREDIT_PLATINUM = addItem(4, "credit.platinum").setRarity(EnumRarity.RARE);
        CREDIT_OSMIUM = addItem(5, "credit.osmium").setRarity(EnumRarity.RARE);
        CREDIT_NAQUADAH = addItem(6, "credit.naquadah").setRarity(EnumRarity.EPIC);
        CREDIT_NEUTRONIUM = addItem(7, "credit.neutronium").setRarity(EnumRarity.EPIC);

        COIN_GOLD_ANCIENT = addItem(8, "coin.gold.ancient")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Gold, M / 4)))
                .setRarity(EnumRarity.RARE);
        COIN_DOGE = addItem(9, "coin.doge")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Brass, M / 4)))
                .setRarity(EnumRarity.EPIC);
        COIN_CHOCOLATE = addItem(10, "coin.chocolate")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Gold, M / 4)))
                .addComponents(new FoodStats(1, 0.1F, false, true, OreDictUnifier.get(OrePrefix.foil, Materials.Gold),
                        new RandomPotionEffect(MobEffects.SPEED, 200, 1, 10)));

        // Solidifier Shapes: ID 11-30
        SHAPE_EMPTY = addItem(11, "shape.empty")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));

        SHAPE_MOLDS[0] = SHAPE_MOLD_PLATE = addItem(12, "shape.mold.plate")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_MOLDS[1] = SHAPE_MOLD_GEAR = addItem(13, "shape.mold.gear")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_MOLDS[2] = SHAPE_MOLD_CREDIT = addItem(14, "shape.mold.credit")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_MOLDS[3] = SHAPE_MOLD_BOTTLE = addItem(15, "shape.mold.bottle")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_MOLDS[4] = SHAPE_MOLD_INGOT = addItem(16, "shape.mold.ingot")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_MOLDS[5] = SHAPE_MOLD_BALL = addItem(17, "shape.mold.ball")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_MOLDS[6] = SHAPE_MOLD_BLOCK = addItem(18, "shape.mold.block")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_MOLDS[7] = SHAPE_MOLD_NUGGET = addItem(19, "shape.mold.nugget")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_MOLDS[8] = SHAPE_MOLD_CYLINDER = addItem(20, "shape.mold.cylinder")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_MOLDS[9] = SHAPE_MOLD_ANVIL = addItem(21, "shape.mold.anvil")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_MOLDS[10] = SHAPE_MOLD_NAME = addItem(22, "shape.mold.name")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_MOLDS[11] = SHAPE_MOLD_GEAR_SMALL = addItem(23, "shape.mold.gear.small")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_MOLDS[12] = SHAPE_MOLD_ROTOR = addItem(24, "shape.mold.rotor")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));

        // Extruder Shapes: ID 31-59
        SHAPE_EXTRUDERS[0] = SHAPE_EXTRUDER_PLATE = addItem(31, "shape.extruder.plate")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[1] = SHAPE_EXTRUDER_ROD = addItem(32, "shape.extruder.rod")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[2] = SHAPE_EXTRUDER_BOLT = addItem(33, "shape.extruder.bolt")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[3] = SHAPE_EXTRUDER_RING = addItem(34, "shape.extruder.ring")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[4] = SHAPE_EXTRUDER_CELL = addItem(35, "shape.extruder.cell")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[5] = SHAPE_EXTRUDER_INGOT = addItem(36, "shape.extruder.ingot")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[6] = SHAPE_EXTRUDER_WIRE = addItem(37, "shape.extruder.wire")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[7] = SHAPE_EXTRUDER_PIPE_TINY = addItem(38, "shape.extruder.pipe.tiny")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[8] = SHAPE_EXTRUDER_PIPE_SMALL = addItem(39, "shape.extruder.pipe.small")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[9] = SHAPE_EXTRUDER_PIPE_NORMAL = addItem(40, "shape.extruder.pipe.normal")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[10] = SHAPE_EXTRUDER_PIPE_LARGE = addItem(41, "shape.extruder.pipe.large")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[11] = SHAPE_EXTRUDER_PIPE_HUGE = addItem(42, "shape.extruder.pipe.huge")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[12] = SHAPE_EXTRUDER_BLOCK = addItem(43, "shape.extruder.block")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        // Extruder Shapes index 13-20 (inclusive), id 44-51 (inclusive) are unused
        SHAPE_EXTRUDERS[21] = SHAPE_EXTRUDER_GEAR = addItem(52, "shape.extruder.gear")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[22] = SHAPE_EXTRUDER_BOTTLE = addItem(53, "shape.extruder.bottle")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[23] = SHAPE_EXTRUDER_FOIL = addItem(54, "shape.extruder.foil")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[24] = SHAPE_EXTRUDER_GEAR_SMALL = addItem(55, "shape.extruder.gear_small")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[25] = SHAPE_EXTRUDER_ROD_LONG = addItem(56, "shape.extruder.rod_long")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));
        SHAPE_EXTRUDERS[26] = SHAPE_EXTRUDER_ROTOR = addItem(57, "shape.extruder.rotor")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4)));

        // Spray Cans: ID 60-77
        SPRAY_EMPTY = addItem(61, "spray.empty");

        // out of registry order so it can reference the Empty Spray Can
        SPRAY_SOLVENT = addItem(60, "spray.solvent").setMaxStackSize(1)
                .addComponents(new ColorSprayBehaviour(SPRAY_EMPTY.getStackForm(), 1024, -1))
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        for (int i = 0; i < EnumDyeColor.values().length; i++) {
            SPRAY_CAN_DYES[i] = addItem(62 + i, "spray.can.dyes." + EnumDyeColor.values()[i].getName())
                    .setMaxStackSize(1)
                    .addComponents(new ColorSprayBehaviour(SPRAY_EMPTY.getStackForm(), 512, i))
                    .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        }

        // Fluid Cells: ID 78-88
        FLUID_CELL = addItem(78, "fluid_cell")
                .addComponents(new FilteredFluidStats(1000, 1800, true, false, false, false, false),
                        new ItemFluidContainer())
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        FLUID_CELL_UNIVERSAL = addItem(79, "fluid_cell.universal")
                .addComponents(new FilteredFluidStats(1000, 1800, true, false, false, false, true),
                        new ItemFluidContainer())
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        FLUID_CELL_LARGE_STEEL = addItem(80, "large_fluid_cell.steel")
                .addComponents(new FilteredFluidStats(8000,
                        Materials.Steel.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true, false,
                        false, false, true), new ItemFluidContainer())
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 4))) // ingot * 4
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        FLUID_CELL_LARGE_ALUMINIUM = addItem(81, "large_fluid_cell.aluminium")
                .addComponents(new FilteredFluidStats(32000,
                        Materials.Aluminium.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true, false,
                        false, false, true), new ItemFluidContainer())
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Aluminium, M * 4))) // ingot * 4
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        FLUID_CELL_LARGE_STAINLESS_STEEL = addItem(82, "large_fluid_cell.stainless_steel")
                .addComponents(new FilteredFluidStats(64000,
                        Materials.StainlessSteel.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true,
                        true, true, false, true), new ItemFluidContainer())
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.StainlessSteel, M * 6))) // ingot * 6
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        FLUID_CELL_LARGE_TITANIUM = addItem(83, "large_fluid_cell.titanium")
                .addComponents(new FilteredFluidStats(128000,
                        Materials.Titanium.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true, true,
                        false, false, true), new ItemFluidContainer())
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Titanium, M * 6))) // ingot * 6
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        FLUID_CELL_LARGE_TUNGSTEN_STEEL = addItem(84, "large_fluid_cell.tungstensteel")
                .addComponents(new FilteredFluidStats(512000,
                        Materials.TungstenSteel.getProperty(PropertyKey.FLUID_PIPE).getMaxFluidTemperature(), true,
                        true, false, false, true), new ItemFluidContainer())
                .setMaxStackSize(32)
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.TungstenSteel, M * 8))) // ingot * 8
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        FLUID_CELL_GLASS_VIAL = addItem(85, "fluid_cell.glass_vial")
                .addComponents(new FilteredFluidStats(1000, 1200, false, true, false, false, true),
                        new ItemFluidContainer())
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Glass, M / 4))) // small dust
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        // Limited-Use Items: ID 89-95

        TOOL_MATCHES = addItem(89, "tool.matches")
                .addComponents(new LighterBehaviour(false, false, false))
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        TOOL_MATCHBOX = addItem(90, "tool.matchbox")
                .addComponents(new LighterBehaviour(false, true, false, Items.PAPER, 16))
                .setMaxStackSize(1)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        TOOL_LIGHTER_INVAR = addItem(91, "tool.lighter.invar")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Invar, M * 2)))
                .addComponents(new LighterBehaviour(GTUtility.gregtechId("lighter_open"), true, true, true))
                .addComponents(new FilteredFluidStats(100, true, CommonFluidFilters.LIGHTER_FUEL))
                .setMaxStackSize(1)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        TOOL_LIGHTER_PLATINUM = addItem(92, "tool.lighter.platinum")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Platinum, M * 2)))
                .addComponents(new LighterBehaviour(GTUtility.gregtechId("lighter_open"), true, true, true))
                .addComponents(new FilteredFluidStats(1000, true, CommonFluidFilters.LIGHTER_FUEL))
                .setMaxStackSize(1)
                .setRarity(EnumRarity.UNCOMMON)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        BOTTLE_PURPLE_DRINK = addItem(93, "bottle.purple.drink").addComponents(new FoodStats(8, 0.2F, true, true,
                new ItemStack(Items.GLASS_BOTTLE), new RandomPotionEffect(MobEffects.HASTE, 800, 1, 90)));

        // Voltage Coils: ID 96-110
        VOLTAGE_COIL_ULV = addItem(96, "voltage_coil.ulv").setMaterialInfo(new ItemMaterialInfo(
                new MaterialStack(Materials.Lead, M * 2), new MaterialStack(Materials.IronMagnetic, M / 2)));
        VOLTAGE_COIL_LV = addItem(97, "voltage_coil.lv").setMaterialInfo(new ItemMaterialInfo(
                new MaterialStack(Materials.Steel, M * 2), new MaterialStack(Materials.IronMagnetic, M / 2)));
        VOLTAGE_COIL_MV = addItem(98, "voltage_coil.mv").setMaterialInfo(new ItemMaterialInfo(
                new MaterialStack(Materials.Aluminium, M * 2), new MaterialStack(Materials.SteelMagnetic, M / 2)));
        VOLTAGE_COIL_HV = addItem(99, "voltage_coil.hv").setMaterialInfo(new ItemMaterialInfo(
                new MaterialStack(Materials.BlackSteel, M * 2), new MaterialStack(Materials.SteelMagnetic, M / 2)));
        VOLTAGE_COIL_EV = addItem(100, "voltage_coil.ev")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Platinum, M * 2),
                        new MaterialStack(Materials.NeodymiumMagnetic, M / 2)));
        VOLTAGE_COIL_IV = addItem(101, "voltage_coil.iv").setMaterialInfo(new ItemMaterialInfo(
                new MaterialStack(Materials.Iridium, M * 2), new MaterialStack(Materials.NeodymiumMagnetic, M / 2)));
        VOLTAGE_COIL_LuV = addItem(102, "voltage_coil.luv").setMaterialInfo(new ItemMaterialInfo(
                new MaterialStack(Materials.Osmiridium, M * 2), new MaterialStack(Materials.SamariumMagnetic, M / 2)));
        VOLTAGE_COIL_ZPM = addItem(103, "voltage_coil.zpm").setMaterialInfo(new ItemMaterialInfo(
                new MaterialStack(Materials.Europium, M * 2), new MaterialStack(Materials.SamariumMagnetic, M / 2)));
        VOLTAGE_COIL_UV = addItem(104, "voltage_coil.uv").setMaterialInfo(new ItemMaterialInfo(
                new MaterialStack(Materials.Tritanium, M * 2), new MaterialStack(Materials.SamariumMagnetic, M / 2)));

        // ???: ID 111-125

        // Motors: ID 126-140
        ELECTRIC_MOTOR_LV = addItem(127, "electric.motor.lv");
        ELECTRIC_MOTOR_MV = addItem(128, "electric.motor.mv");
        ELECTRIC_MOTOR_HV = addItem(129, "electric.motor.hv");
        ELECTRIC_MOTOR_EV = addItem(130, "electric.motor.ev");
        ELECTRIC_MOTOR_IV = addItem(131, "electric.motor.iv");
        ELECTRIC_MOTOR_LuV = addItem(132, "electric.motor.luv");
        ELECTRIC_MOTOR_ZPM = addItem(133, "electric.motor.zpm");
        ELECTRIC_MOTOR_UV = addItem(134, "electric.motor.uv");
        ELECTRIC_MOTOR_UHV = addItem(135, "electric.motor.uhv").setInvisibleIf(!GregTechAPI.isHighTier());
        ELECTRIC_MOTOR_UEV = addItem(136, "electric.motor.uev").setInvisibleIf(!GregTechAPI.isHighTier());
        ELECTRIC_MOTOR_UIV = addItem(137, "electric.motor.uiv").setInvisibleIf(!GregTechAPI.isHighTier());
        ELECTRIC_MOTOR_UXV = addItem(138, "electric.motor.uxv").setInvisibleIf(!GregTechAPI.isHighTier());
        ELECTRIC_MOTOR_OpV = addItem(139, "electric.motor.opv").setInvisibleIf(!GregTechAPI.isHighTier());

        // Pumps: ID 141-155
        ELECTRIC_PUMP_LV = addItem(142, "electric.pump.lv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 / 20));
        }));
        ELECTRIC_PUMP_MV = addItem(143, "electric.pump.mv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 4 / 20));
        }));
        ELECTRIC_PUMP_HV = addItem(144, "electric.pump.hv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 16 / 20));
        }));
        ELECTRIC_PUMP_EV = addItem(145, "electric.pump.ev").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 / 20));
        }));
        ELECTRIC_PUMP_IV = addItem(146, "electric.pump.iv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 4 / 20));
        }));
        ELECTRIC_PUMP_LuV = addItem(147, "electric.pump.luv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 16 / 20));
        }));
        ELECTRIC_PUMP_ZPM = addItem(148, "electric.pump.zpm").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 / 20));
        }));
        ELECTRIC_PUMP_UV = addItem(149, "electric.pump.uv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
        }));
        ELECTRIC_PUMP_UHV = addItem(150, "electric.pump.uhv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
        })).setInvisibleIf(!GregTechAPI.isHighTier());
        ELECTRIC_PUMP_UEV = addItem(151, "electric.pump.uev").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
        })).setInvisibleIf(!GregTechAPI.isHighTier());
        ELECTRIC_PUMP_UIV = addItem(152, "electric.pump.uiv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
        })).setInvisibleIf(!GregTechAPI.isHighTier());
        ELECTRIC_PUMP_UXV = addItem(153, "electric.pump.uxv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
        })).setInvisibleIf(!GregTechAPI.isHighTier());
        ELECTRIC_PUMP_OpV = addItem(154, "electric.pump.opv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
        })).setInvisibleIf(!GregTechAPI.isHighTier());

        // Conveyors: ID 156-170
        CONVEYOR_MODULE_LV = addItem(157, "conveyor.module.lv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate", 8));
        }));
        CONVEYOR_MODULE_MV = addItem(158, "conveyor.module.mv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate", 32));
        }));
        CONVEYOR_MODULE_HV = addItem(159, "conveyor.module.hv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate", 64));
        }));
        CONVEYOR_MODULE_EV = addItem(160, "conveyor.module.ev").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 3));
        }));
        CONVEYOR_MODULE_IV = addItem(161, "conveyor.module.iv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 8));
        }));
        CONVEYOR_MODULE_LuV = addItem(162, "conveyor.module.luv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        }));
        CONVEYOR_MODULE_ZPM = addItem(163, "conveyor.module.zpm").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        }));
        CONVEYOR_MODULE_UV = addItem(164, "conveyor.module.uv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        }));
        CONVEYOR_MODULE_UHV = addItem(165, "conveyor.module.uhv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        })).setInvisibleIf(!GregTechAPI.isHighTier());
        CONVEYOR_MODULE_UEV = addItem(166, "conveyor.module.uev").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        })).setInvisibleIf(!GregTechAPI.isHighTier());
        CONVEYOR_MODULE_UIV = addItem(167, "conveyor.module.uiv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        })).setInvisibleIf(!GregTechAPI.isHighTier());
        CONVEYOR_MODULE_UXV = addItem(168, "conveyor.module.uxv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        })).setInvisibleIf(!GregTechAPI.isHighTier());
        CONVEYOR_MODULE_OpV = addItem(169, "conveyor.module.opv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        })).setInvisibleIf(!GregTechAPI.isHighTier());

        // Pistons: ID 171-185
        ELECTRIC_PISTON_LV = addItem(172, "electric.piston.lv");
        ELECTRIC_PISTON_MV = addItem(173, "electric.piston.mv");
        ELECTRIC_PISTON_HV = addItem(174, "electric.piston.hv");
        ELECTRIC_PISTON_EV = addItem(175, "electric.piston.ev");
        ELECTRIC_PISTON_IV = addItem(176, "electric.piston.iv");
        ELECTRIC_PISTON_LUV = addItem(177, "electric.piston.luv");
        ELECTRIC_PISTON_ZPM = addItem(178, "electric.piston.zpm");
        ELECTRIC_PISTON_UV = addItem(179, "electric.piston.uv");
        ELECTRIC_PISTON_UHV = addItem(180, "electric.piston.uhv").setInvisibleIf(!GregTechAPI.isHighTier());
        ELECTRIC_PISTON_UEV = addItem(181, "electric.piston.uev").setInvisibleIf(!GregTechAPI.isHighTier());
        ELECTRIC_PISTON_UIV = addItem(182, "electric.piston.uiv").setInvisibleIf(!GregTechAPI.isHighTier());
        ELECTRIC_PISTON_UXV = addItem(183, "electric.piston.uxv").setInvisibleIf(!GregTechAPI.isHighTier());
        ELECTRIC_PISTON_OpV = addItem(184, "electric.piston.opv").setInvisibleIf(!GregTechAPI.isHighTier());

        // Robot Arms: ID 186-200
        ROBOT_ARM_LV = addItem(187, "robot.arm.lv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.robot.arm.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate", 8));
        }));
        ROBOT_ARM_MV = addItem(188, "robot.arm.mv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.robot.arm.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate", 32));
        }));
        ROBOT_ARM_HV = addItem(189, "robot.arm.hv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.robot.arm.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate", 64));
        }));
        ROBOT_ARM_EV = addItem(190, "robot.arm.ev").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.robot.arm.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 3));
        }));
        ROBOT_ARM_IV = addItem(191, "robot.arm.iv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.robot.arm.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 8));
        }));
        ROBOT_ARM_LuV = addItem(192, "robot.arm.luv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.robot.arm.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        }));
        ROBOT_ARM_ZPM = addItem(193, "robot.arm.zpm").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.robot.arm.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        }));
        ROBOT_ARM_UV = addItem(194, "robot.arm.uv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.robot.arm.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        }));
        ROBOT_ARM_UHV = addItem(195, "robot.arm.uhv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.robot.arm.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        })).setInvisibleIf(!GregTechAPI.isHighTier());
        ROBOT_ARM_UEV = addItem(196, "robot.arm.uev").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.robot.arm.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        })).setInvisibleIf(!GregTechAPI.isHighTier());
        ROBOT_ARM_UIV = addItem(197, "robot.arm.uiv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.robot.arm.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        })).setInvisibleIf(!GregTechAPI.isHighTier());
        ROBOT_ARM_UXV = addItem(198, "robot.arm.uxv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.robot.arm.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        })).setInvisibleIf(!GregTechAPI.isHighTier());
        ROBOT_ARM_OpV = addItem(199, "robot.arm.opv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.robot.arm.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", 16));
        })).setInvisibleIf(!GregTechAPI.isHighTier());

        // Field Generators: ID 201-215
        FIELD_GENERATOR_LV = addItem(202, "field.generator.lv");
        FIELD_GENERATOR_MV = addItem(203, "field.generator.mv");
        FIELD_GENERATOR_HV = addItem(204, "field.generator.hv");
        FIELD_GENERATOR_EV = addItem(205, "field.generator.ev");
        FIELD_GENERATOR_IV = addItem(206, "field.generator.iv");
        FIELD_GENERATOR_LuV = addItem(207, "field.generator.luv");
        FIELD_GENERATOR_ZPM = addItem(208, "field.generator.zpm");
        FIELD_GENERATOR_UV = addItem(209, "field.generator.uv");
        FIELD_GENERATOR_UHV = addItem(210, "field.generator.uhv").setInvisibleIf(!GregTechAPI.isHighTier());
        FIELD_GENERATOR_UEV = addItem(211, "field.generator.uev").setInvisibleIf(!GregTechAPI.isHighTier());
        FIELD_GENERATOR_UIV = addItem(212, "field.generator.uiv").setInvisibleIf(!GregTechAPI.isHighTier());
        FIELD_GENERATOR_UXV = addItem(213, "field.generator.uxv").setInvisibleIf(!GregTechAPI.isHighTier());
        FIELD_GENERATOR_OpV = addItem(214, "field.generator.opv").setInvisibleIf(!GregTechAPI.isHighTier());

        // Emitters: ID 216-230
        EMITTER_LV = addItem(217, "emitter.lv");
        EMITTER_MV = addItem(218, "emitter.mv");
        EMITTER_HV = addItem(219, "emitter.hv");
        EMITTER_EV = addItem(220, "emitter.ev");
        EMITTER_IV = addItem(221, "emitter.iv");
        EMITTER_LuV = addItem(222, "emitter.luv");
        EMITTER_ZPM = addItem(223, "emitter.zpm");
        EMITTER_UV = addItem(224, "emitter.uv");
        EMITTER_UHV = addItem(225, "emitter.uhv").setInvisibleIf(!GregTechAPI.isHighTier());
        EMITTER_UEV = addItem(226, "emitter.uev").setInvisibleIf(!GregTechAPI.isHighTier());
        EMITTER_UIV = addItem(227, "emitter.uiv").setInvisibleIf(!GregTechAPI.isHighTier());
        EMITTER_UXV = addItem(228, "emitter.uxv").setInvisibleIf(!GregTechAPI.isHighTier());
        EMITTER_OpV = addItem(229, "emitter.opv").setInvisibleIf(!GregTechAPI.isHighTier());

        // Sensors: ID 231-245
        SENSOR_LV = addItem(232, "sensor.lv");
        SENSOR_MV = addItem(233, "sensor.mv");
        SENSOR_HV = addItem(234, "sensor.hv");
        SENSOR_EV = addItem(235, "sensor.ev");
        SENSOR_IV = addItem(236, "sensor.iv");
        SENSOR_LuV = addItem(237, "sensor.luv");
        SENSOR_ZPM = addItem(238, "sensor.zpm");
        SENSOR_UV = addItem(239, "sensor.uv");
        SENSOR_UHV = addItem(240, "sensor.uhv").setInvisibleIf(!GregTechAPI.isHighTier());
        SENSOR_UEV = addItem(241, "sensor.uev").setInvisibleIf(!GregTechAPI.isHighTier());
        SENSOR_UIV = addItem(242, "sensor.uiv").setInvisibleIf(!GregTechAPI.isHighTier());
        SENSOR_UXV = addItem(243, "sensor.uxv").setInvisibleIf(!GregTechAPI.isHighTier());
        SENSOR_OpV = addItem(244, "sensor.opv").setInvisibleIf(!GregTechAPI.isHighTier());

        // Fluid Regulators: ID 246-260
        FLUID_REGULATOR_LV = addItem(247, "fluid.regulator.lv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.fluid.regulator.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 / 20));
        }));
        FLUID_REGULATOR_MV = addItem(248, "fluid.regulator.mv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.fluid.regulator.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 4 / 20));
        }));
        FLUID_REGULATOR_HV = addItem(249, "fluid.regulator.hv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.fluid.regulator.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 16 / 20));
        }));
        FLUID_REGULATOR_EV = addItem(250, "fluid.regulator.ev").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.fluid.regulator.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 / 20));
        }));
        FLUID_REGULATOR_IV = addItem(251, "fluid.regulator.iv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.fluid.regulator.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 4 / 20));
        }));
        FLUID_REGULATOR_LUV = addItem(252, "fluid.regulator.luv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.fluid.regulator.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 16 / 20));
        }));
        FLUID_REGULATOR_ZPM = addItem(253, "fluid.regulator.zpm").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.fluid.regulator.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 / 20));
        }));
        FLUID_REGULATOR_UV = addItem(254, "fluid.regulator.uv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.fluid.regulator.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 * 64 * 64 * 4 / 20));
        }));

        // Data Items: ID 261-265
        TOOL_DATA_STICK = addItem(261, "tool.datastick").addComponents(new DataItemBehavior());
        TOOL_DATA_ORB = addItem(262, "tool.dataorb").addComponents(new DataItemBehavior());
        TOOL_DATA_MODULE = addItem(263, "tool.datamodule").addComponents(new DataItemBehavior(true));

        // Special Machine Components: ID 266-280
        COMPONENT_GRINDER_DIAMOND = addItem(266, "component.grinder.diamond")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M * 8),
                        new MaterialStack(Materials.Diamond, M * 5)));
        COMPONENT_GRINDER_TUNGSTEN = addItem(267, "component.grinder.tungsten")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Tungsten, M * 4),
                        new MaterialStack(Materials.VanadiumSteel, M * 8), new MaterialStack(Materials.Diamond, M)));

        IRON_MINECART_WHEELS = addItem(268, "minecart_wheels.iron")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Iron, M)));
        STEEL_MINECART_WHEELS = addItem(269, "minecart_wheels.steel")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, M)));

        // Special Eyes/Stars: ID 281-289
        QUANTUM_EYE = addItem(281, "quantumeye");
        QUANTUM_STAR = addItem(282, "quantumstar");
        GRAVI_STAR = addItem(283, "gravistar");

        // Filters: ID 290-300
        FLUID_FILTER = addItem(290, "fluid_filter")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Zinc, M * 2)))
                .addComponents(new SimpleFluidFilterUIManager(), IFilter.factory(SimpleFluidFilter::new));
        ITEM_FILTER = addItem(291, "item_filter")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Zinc, M * 2),
                        new MaterialStack(Materials.Steel, M)))
                .addComponents(new SimpleFilterUIManager(), IFilter.factory(SimpleItemFilter::new));
        ORE_DICTIONARY_FILTER = addItem(292, "ore_dictionary_filter")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Zinc, M * 2)))
                .addComponents(new OreDictFilterUIManager(), IFilter.factory(OreDictionaryItemFilter::new));
        SMART_FILTER = addItem(293, "smart_item_filter")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Zinc, M * 3 / 2)))
                .addComponents(new SmartFilterUIManager(), IFilter.factory(SmartItemFilter::new));

        // Functional Covers: ID 301-330
        COVER_MACHINE_CONTROLLER = addItem(301, "cover.controller");
        COVER_ACTIVITY_DETECTOR = addItem(302, "cover.activity.detector");
        COVER_ACTIVITY_DETECTOR_ADVANCED = addItem(303, "cover.activity.detector_advanced");
        COVER_FLUID_DETECTOR = addItem(304, "cover.fluid.detector");
        COVER_FLUID_DETECTOR_ADVANCED = addItem(319, "cover.fluid.detector.advanced");
        COVER_ITEM_DETECTOR = addItem(305, "cover.item.detector");
        COVER_ITEM_DETECTOR_ADVANCED = addItem(320, "cover.item.detector.advanced");
        COVER_ENERGY_DETECTOR = addItem(306, "cover.energy.detector");
        COVER_ENERGY_DETECTOR_ADVANCED = addItem(318, "cover.energy.detector.advanced");
        COVER_SCREEN = addItem(307, "cover.screen");
        // FREE ID 308
        COVER_SHUTTER = addItem(309, "cover.shutter");
        COVER_INFINITE_WATER = addItem(310, "cover.infinite_water").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.cover.infinite_water.tooltip.1"));
            lines.add(I18n.format("gregtech.universal.tooltip.produces_fluid", 16_000 / 20));
        }));
        COVER_ENDER_FLUID_LINK = addItem(311, "cover.ender_fluid_link");
        COVER_DIGITAL_INTERFACE = addItem(312, "cover.digital");
        COVER_DIGITAL_INTERFACE_WIRELESS = addItem(313, "cover.digital.wireless");
        COVER_FLUID_VOIDING = addItem(314, "cover.fluid.voiding");
        COVER_FLUID_VOIDING_ADVANCED = addItem(315, "cover.fluid.voiding.advanced");
        COVER_ITEM_VOIDING = addItem(316, "cover.item.voiding");
        COVER_ITEM_VOIDING_ADVANCED = addItem(317, "cover.item.voiding.advanced");
        COVER_STORAGE = addItem(321, "cover.storage");
        COVER_MAINTENANCE_DETECTOR = addItem(322, "cover.maintenance.detector");

        COVER_FACADE = addItem(330, "cover.facade").addComponents(new FacadeItem()).disableModelLoading();

        // Solar Panels: ID 331-346
        COVER_SOLAR_PANEL = addItem(331, "cover.solar.panel").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.1"));
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.2"));
            lines.add(I18n.format("gregtech.universal.tooltip.voltage_out", 1, GTValues.VNF[GTValues.ULV]));
        }));
        COVER_SOLAR_PANEL_ULV = addItem(332, "cover.solar.panel.ulv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.1"));
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.2"));
            lines.add(I18n.format("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.ULV],
                    GTValues.VNF[GTValues.ULV]));
        }));
        COVER_SOLAR_PANEL_LV = addItem(333, "cover.solar.panel.lv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.1"));
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.2"));
            lines.add(I18n.format("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.LV],
                    GTValues.VNF[GTValues.LV]));
        }));
        COVER_SOLAR_PANEL_MV = addItem(334, "cover.solar.panel.mv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.1"));
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.2"));
            lines.add(I18n.format("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.MV],
                    GTValues.VNF[GTValues.MV]));
        }));
        COVER_SOLAR_PANEL_HV = addItem(335, "cover.solar.panel.hv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.1"));
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.2"));
            lines.add(I18n.format("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.HV],
                    GTValues.VNF[GTValues.HV]));
        }));
        COVER_SOLAR_PANEL_EV = addItem(336, "cover.solar.panel.ev").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.1"));
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.2"));
            lines.add(I18n.format("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.EV],
                    GTValues.VNF[GTValues.EV]));
        }));
        COVER_SOLAR_PANEL_IV = addItem(337, "cover.solar.panel.iv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.1"));
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.2"));
            lines.add(I18n.format("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.IV],
                    GTValues.VNF[GTValues.IV]));
        }));
        COVER_SOLAR_PANEL_LUV = addItem(338, "cover.solar.panel.luv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.1"));
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.2"));
            lines.add(I18n.format("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.LuV],
                    GTValues.VNF[GTValues.LuV]));
        }));
        COVER_SOLAR_PANEL_ZPM = addItem(339, "cover.solar.panel.zpm").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.1"));
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.2"));
            lines.add(I18n.format("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.ZPM],
                    GTValues.VNF[GTValues.ZPM]));
        }));
        COVER_SOLAR_PANEL_UV = addItem(340, "cover.solar.panel.uv").addComponents(new TooltipBehavior(lines -> {
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.1"));
            lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.2"));
            lines.add(I18n.format("gregtech.universal.tooltip.voltage_out", GTValues.V[GTValues.UV],
                    GTValues.VNF[GTValues.UV]));
        }));
        // MAX-tier solar panel?

        if (!ConfigHolder.machines.enableHighTierSolars) {
            COVER_SOLAR_PANEL_IV.setInvisible();
            COVER_SOLAR_PANEL_LUV.setInvisible();
            COVER_SOLAR_PANEL_ZPM.setInvisible();
            COVER_SOLAR_PANEL_UV.setInvisible();
        }

        // Early Game Brick Related: ID 347-360
        IItemContainerItemProvider selfContainerItemProvider = itemStack -> itemStack;
        WOODEN_FORM_EMPTY = addItem(347, "wooden_form.empty");
        WOODEN_FORM_BRICK = addItem(348, "wooden_form.brick").addComponents(selfContainerItemProvider);
        COMPRESSED_CLAY = addItem(349, "compressed.clay")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Clay, M)));
        COMPRESSED_COKE_CLAY = addItem(350, "compressed.coke_clay")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Clay, M)));
        COMPRESSED_FIRECLAY = addItem(351, "compressed.fireclay")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Fireclay, M)));
        FIRECLAY_BRICK = addItem(352, "brick.fireclay")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Fireclay, M)));
        COKE_OVEN_BRICK = addItem(353, "brick.coke")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Clay, M)));

        if (!ConfigHolder.recipes.harderBrickRecipes)
            COMPRESSED_CLAY.setInvisible();

        // Boules: ID 361-370
        SILICON_BOULE = addItem(361, "boule.silicon");
        PHOSPHORUS_BOULE = addItem(362, "boule.phosphorus");
        NAQUADAH_BOULE = addItem(363, "boule.naquadah");
        NEUTRONIUM_BOULE = addItem(364, "boule.neutronium");

        // Boule-Direct Wafers: ID 371-380
        SILICON_WAFER = addItem(371, "wafer.silicon");
        PHOSPHORUS_WAFER = addItem(372, "wafer.phosphorus");
        NAQUADAH_WAFER = addItem(373, "wafer.naquadah");
        NEUTRONIUM_WAFER = addItem(374, "wafer.neutronium");

        // Unfinished Circuit Boards: ID 381-400
        COATED_BOARD = addItem(381, "board.coated");
        PHENOLIC_BOARD = addItem(382, "board.phenolic");
        PLASTIC_BOARD = addItem(383, "board.plastic");
        EPOXY_BOARD = addItem(384, "board.epoxy");
        FIBER_BOARD = addItem(385, "board.fiber_reinforced");
        MULTILAYER_FIBER_BOARD = addItem(386, "board.multilayer.fiber_reinforced");
        WETWARE_BOARD = addItem(387, "board.wetware");

        // Finished Circuit Boards: ID 401-420
        BASIC_CIRCUIT_BOARD = addItem(401, "circuit_board.basic");
        GOOD_CIRCUIT_BOARD = addItem(402, "circuit_board.good");
        PLASTIC_CIRCUIT_BOARD = addItem(403, "circuit_board.plastic");
        ADVANCED_CIRCUIT_BOARD = addItem(404, "circuit_board.advanced");
        EXTREME_CIRCUIT_BOARD = addItem(405, "circuit_board.extreme");
        ELITE_CIRCUIT_BOARD = addItem(406, "circuit_board.elite");
        WETWARE_CIRCUIT_BOARD = addItem(407, "circuit_board.wetware");

        // Dyes: ID 421-436
        for (int i = 0; i < EnumDyeColor.values().length; i++) {
            EnumDyeColor dyeColor = EnumDyeColor.values()[i];
            DYE_ONLY_ITEMS[i] = addItem(421 + i, "dye." + dyeColor.getName()).addOreDict(getOredictColorName(dyeColor));
        }

        // Plant/Rubber Related: ID 438-445
        STICKY_RESIN = addItem(438, "rubber_drop").setBurnValue(200);
        PLANT_BALL = addItem(439, "plant_ball").setBurnValue(75);
        BIO_CHAFF = addItem(440, "bio_chaff").setBurnValue(200);

        // Power Units: ID 446-459
        POWER_UNIT_LV = addItem(446, "power_unit.lv")
                .addComponents(ElectricStats.createElectricItem(100000L, GTValues.LV)).setMaxStackSize(8);
        POWER_UNIT_MV = addItem(447, "power_unit.mv")
                .addComponents(ElectricStats.createElectricItem(400000L, GTValues.MV)).setMaxStackSize(8);
        POWER_UNIT_HV = addItem(448, "power_unit.hv")
                .addComponents(ElectricStats.createElectricItem(1600000L, GTValues.HV)).setMaxStackSize(8);
        POWER_UNIT_EV = addItem(449, "power_unit.ev")
                .addComponents(ElectricStats.createElectricItem(6400000L, GTValues.EV)).setMaxStackSize(8);
        POWER_UNIT_IV = addItem(450, "power_unit.iv")
                .addComponents(ElectricStats.createElectricItem(25600000L, GTValues.IV)).setMaxStackSize(8);

        // Usable Items: ID 460-490
        DYNAMITE = addItem(460, "dynamite")
                .addComponents(new DynamiteBehaviour())
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        INTEGRATED_CIRCUIT = addItem(461, "circuit.integrated").addComponents(new IntCircuitBehaviour())
                .setModelAmount(33);
        FOAM_SPRAYER = addItem(462, "foam_sprayer").addComponents(new FoamSprayerBehavior())
                .setMaxStackSize(1)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        NANO_SABER = addItem(463, "nano_saber").addComponents(ElectricStats.createElectricItem(4_000_000L, GTValues.HV))
                .addComponents(new NanoSaberBehavior())
                .setMaxStackSize(1)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        NANO_SABER.getMetaItem().addPropertyOverride(NanoSaberBehavior.OVERRIDE_KEY_LOCATION,
                (stack, worldIn, entityIn) -> NanoSaberBehavior.isItemActive(stack) ? 1.0f : 0.0f);

        CLIPBOARD = addItem(464, "clipboard")
                .addComponents(new ClipboardBehavior())
                .setMaxStackSize(1)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        TERMINAL = addItem(465, "terminal")
                .addComponents(new HardwareProvider(), new TerminalBehaviour())
                .setMaxStackSize(1)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        PROSPECTOR_LV = addItem(466, "prospector.lv")
                .addComponents(ElectricStats.createElectricItem(100_000L, GTValues.LV),
                        new ProspectorScannerBehavior(2, GTValues.LV))
                .setMaxStackSize(1)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        PROSPECTOR_HV = addItem(467, "prospector.hv")
                .addComponents(ElectricStats.createElectricItem(1_600_000L, GTValues.HV),
                        new ProspectorScannerBehavior(3, GTValues.HV))
                .setMaxStackSize(1)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        PROSPECTOR_LUV = addItem(468, "prospector.luv")
                .addComponents(ElectricStats.createElectricItem(1_000_000_000L, GTValues.LuV),
                        new ProspectorScannerBehavior(5, GTValues.LuV))
                .setMaxStackSize(1)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        TRICORDER_SCANNER = addItem(469, "tricorder_scanner")
                .addComponents(ElectricStats.createElectricItem(100_000L, GTValues.MV), new TricorderBehavior(2))
                .setMaxStackSize(1)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        DEBUG_SCANNER = addItem(470, "debug_scanner")
                .addComponents(new TricorderBehavior(3))
                .setMaxStackSize(1)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        ITEM_MAGNET_LV = addItem(471, "item_magnet.lv")
                .addComponents(ElectricStats.createElectricItem(100_000L, GTValues.LV), new ItemMagnetBehavior(8))
                .setMaxStackSize(1)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        ITEM_MAGNET_HV = addItem(472, "item_magnet.hv")
                .addComponents(ElectricStats.createElectricItem(1_600_000L, GTValues.HV), new ItemMagnetBehavior(32))
                .setMaxStackSize(1)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        RUBBER_WOOD_BOAT = addItem(473, "rubber_wood_boat")
                .addComponents(new GTBoatBehavior(GTBoatType.RUBBER_WOOD_BOAT)).setMaxStackSize(1).setBurnValue(400);
        TREATED_WOOD_BOAT = addItem(474, "treated_wood_boat")
                .addComponents(new GTBoatBehavior(GTBoatType.TREATED_WOOD_BOAT)).setMaxStackSize(1).setBurnValue(400);
        RUBBER_WOOD_DOOR = addItem(475, "rubber_wood_door").addComponents(new DoorBehavior(MetaBlocks.RUBBER_WOOD_DOOR))
                .setBurnValue(200).setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_DECORATIONS);
        TREATED_WOOD_DOOR = addItem(476, "treated_wood_door")
                .addComponents(new DoorBehavior(MetaBlocks.TREATED_WOOD_DOOR)).setBurnValue(200)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_DECORATIONS);

        // Misc Crafting Items: ID 491-515
        ENERGIUM_DUST = addItem(491, "energium_dust");
        ENGRAVED_LAPOTRON_CHIP = addItem(492, "engraved.lapotron_chip");
        // Free ID: 493, 494, 495, 496
        NEUTRON_REFLECTOR = addItem(497, "neutron_reflector");
        GELLED_TOLUENE = addItem(498, "gelled_toluene");
        CARBON_FIBERS = addItem(499, "carbon.fibers");
        CARBON_MESH = addItem(500, "carbon.mesh");
        CARBON_FIBER_PLATE = addItem(501, "carbon.plate");
        DUCT_TAPE = addItem(502, "duct_tape");
        WIRELESS = addItem(503, "wireless");
        CAMERA = addItem(504, "camera");
        BASIC_TAPE = addItem(505, "basic_tape");

        // Circuit Components: ID 516-565
        VACUUM_TUBE = addItem(516, "circuit.vacuum_tube").setUnificationData(OrePrefix.circuit, Tier.ULV);
        GLASS_TUBE = addItem(517, "component.glass.tube")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Glass, M)));
        TRANSISTOR = addItem(518, "component.transistor").setUnificationData(OrePrefix.component, Component.Transistor);
        RESISTOR = addItem(519, "component.resistor").setUnificationData(OrePrefix.component, Component.Resistor);
        CAPACITOR = addItem(520, "component.capacitor").setUnificationData(OrePrefix.component, Component.Capacitor);
        DIODE = addItem(521, "component.diode").setUnificationData(OrePrefix.component, Component.Diode);
        INDUCTOR = addItem(522, "component.inductor").setUnificationData(OrePrefix.component, Component.Inductor);
        SMD_TRANSISTOR = addItem(523, "component.smd.transistor").setUnificationData(OrePrefix.component,
                Component.Transistor);
        SMD_RESISTOR = addItem(524, "component.smd.resistor").setUnificationData(OrePrefix.component,
                Component.Resistor);
        SMD_CAPACITOR = addItem(525, "component.smd.capacitor").setUnificationData(OrePrefix.component,
                Component.Capacitor);
        SMD_DIODE = addItem(526, "component.smd.diode").setUnificationData(OrePrefix.component, Component.Diode);
        SMD_INDUCTOR = addItem(527, "component.smd.inductor").setUnificationData(OrePrefix.component,
                Component.Inductor);
        ADVANCED_SMD_TRANSISTOR = addItem(528, "component.advanced_smd.transistor");
        ADVANCED_SMD_RESISTOR = addItem(529, "component.advanced_smd.resistor");
        ADVANCED_SMD_CAPACITOR = addItem(530, "component.advanced_smd.capacitor");
        ADVANCED_SMD_DIODE = addItem(531, "component.advanced_smd.diode");
        ADVANCED_SMD_INDUCTOR = addItem(532, "component.advanced_smd.inductor");

        // Engraved and Complex Wafers: ID 566-590
        CENTRAL_PROCESSING_UNIT_WAFER = addItem(566, "wafer.central_processing_unit");
        RANDOM_ACCESS_MEMORY_WAFER = addItem(567, "wafer.random_access_memory");
        INTEGRATED_LOGIC_CIRCUIT_WAFER = addItem(568, "wafer.integrated_logic_circuit");
        NANO_CENTRAL_PROCESSING_UNIT_WAFER = addItem(569, "wafer.nano_central_processing_unit");
        QUBIT_CENTRAL_PROCESSING_UNIT_WAFER = addItem(570, "wafer.qbit_central_processing_unit");
        SIMPLE_SYSTEM_ON_CHIP_WAFER = addItem(571, "wafer.simple_system_on_chip");
        SYSTEM_ON_CHIP_WAFER = addItem(572, "wafer.system_on_chip");
        ADVANCED_SYSTEM_ON_CHIP_WAFER = addItem(573, "wafer.advanced_system_on_chip");
        HIGHLY_ADVANCED_SOC_WAFER = addItem(574, "wafer.highly_advanced_system_on_chip");
        NAND_MEMORY_CHIP_WAFER = addItem(575, "wafer.nand_memory_chip");
        NOR_MEMORY_CHIP_WAFER = addItem(576, "wafer.nor_memory_chip");
        ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER = addItem(577, "wafer.ultra_low_power_integrated_circuit");
        LOW_POWER_INTEGRATED_CIRCUIT_WAFER = addItem(578, "wafer.low_power_integrated_circuit");
        POWER_INTEGRATED_CIRCUIT_WAFER = addItem(579, "wafer.power_integrated_circuit");
        HIGH_POWER_INTEGRATED_CIRCUIT_WAFER = addItem(580, "wafer.high_power_integrated_circuit");
        ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER = addItem(581, "wafer.ultra_high_power_integrated_circuit");

        // Engraved and Complex Cut Wafers: ID 591-615
        CENTRAL_PROCESSING_UNIT = addItem(591, "plate.central_processing_unit");
        RANDOM_ACCESS_MEMORY = addItem(592, "plate.random_access_memory");
        INTEGRATED_LOGIC_CIRCUIT = addItem(593, "plate.integrated_logic_circuit");
        NANO_CENTRAL_PROCESSING_UNIT = addItem(594, "plate.nano_central_processing_unit");
        QUBIT_CENTRAL_PROCESSING_UNIT = addItem(595, "plate.qbit_central_processing_unit");
        SIMPLE_SYSTEM_ON_CHIP = addItem(596, "plate.simple_system_on_chip");
        SYSTEM_ON_CHIP = addItem(597, "plate.system_on_chip");
        ADVANCED_SYSTEM_ON_CHIP = addItem(598, "plate.advanced_system_on_chip");
        HIGHLY_ADVANCED_SOC = addItem(599, "plate.highly_advanced_system_on_chip");
        NAND_MEMORY_CHIP = addItem(600, "plate.nand_memory_chip");
        NOR_MEMORY_CHIP = addItem(601, "plate.nor_memory_chip");
        ULTRA_LOW_POWER_INTEGRATED_CIRCUIT = addItem(602, "plate.ultra_low_power_integrated_circuit");
        LOW_POWER_INTEGRATED_CIRCUIT = addItem(603, "plate.low_power_integrated_circuit");
        POWER_INTEGRATED_CIRCUIT = addItem(604, "plate.power_integrated_circuit");
        HIGH_POWER_INTEGRATED_CIRCUIT = addItem(605, "plate.high_power_integrated_circuit");
        ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT = addItem(606, "plate.ultra_high_power_integrated_circuit");

        // ???: ID 616-620

        // Circuits: ID 621-700

        // T1: Electronic
        ELECTRONIC_CIRCUIT_LV = addItem(621, "circuit.electronic").setUnificationData(OrePrefix.circuit, Tier.LV);
        ELECTRONIC_CIRCUIT_MV = addItem(622, "circuit.good_electronic").setUnificationData(OrePrefix.circuit, Tier.MV);

        // T2: Integrated
        INTEGRATED_CIRCUIT_LV = addItem(623, "circuit.basic_integrated").setUnificationData(OrePrefix.circuit, Tier.LV);
        INTEGRATED_CIRCUIT_MV = addItem(624, "circuit.good_integrated").setUnificationData(OrePrefix.circuit, Tier.MV);
        INTEGRATED_CIRCUIT_HV = addItem(625, "circuit.advanced_integrated").setUnificationData(OrePrefix.circuit,
                Tier.HV);

        // Misc Unlocks
        NAND_CHIP_ULV = addItem(626, "circuit.nand_chip").setUnificationData(OrePrefix.circuit, Tier.ULV);
        MICROPROCESSOR_LV = addItem(627, "circuit.microprocessor").setUnificationData(OrePrefix.circuit, Tier.LV);

        // T3: Processor
        PROCESSOR_MV = addItem(628, "circuit.processor").setUnificationData(OrePrefix.circuit, Tier.MV);
        PROCESSOR_ASSEMBLY_HV = addItem(629, "circuit.assembly").setUnificationData(OrePrefix.circuit, Tier.HV);
        WORKSTATION_EV = addItem(630, "circuit.workstation").setUnificationData(OrePrefix.circuit, Tier.EV);
        MAINFRAME_IV = addItem(631, "circuit.mainframe").setUnificationData(OrePrefix.circuit, Tier.IV);

        // T4: Nano
        NANO_PROCESSOR_HV = addItem(632, "circuit.nano_processor").setUnificationData(OrePrefix.circuit, Tier.HV);
        NANO_PROCESSOR_ASSEMBLY_EV = addItem(633, "circuit.nano_assembly").setUnificationData(OrePrefix.circuit,
                Tier.EV);
        NANO_COMPUTER_IV = addItem(634, "circuit.nano_computer").setUnificationData(OrePrefix.circuit, Tier.IV);
        NANO_MAINFRAME_LUV = addItem(635, "circuit.nano_mainframe").setUnificationData(OrePrefix.circuit, Tier.LuV);

        // T5: Quantum
        QUANTUM_PROCESSOR_EV = addItem(636, "circuit.quantum_processor").setUnificationData(OrePrefix.circuit, Tier.EV);
        QUANTUM_ASSEMBLY_IV = addItem(637, "circuit.quantum_assembly").setUnificationData(OrePrefix.circuit, Tier.IV);
        QUANTUM_COMPUTER_LUV = addItem(638, "circuit.quantum_computer").setUnificationData(OrePrefix.circuit, Tier.LuV);
        QUANTUM_MAINFRAME_ZPM = addItem(639, "circuit.quantum_mainframe").setUnificationData(OrePrefix.circuit,
                Tier.ZPM);

        // T6: Crystal
        CRYSTAL_PROCESSOR_IV = addItem(640, "circuit.crystal_processor").setUnificationData(OrePrefix.circuit, Tier.IV);
        CRYSTAL_ASSEMBLY_LUV = addItem(641, "circuit.crystal_assembly").setUnificationData(OrePrefix.circuit, Tier.LuV);
        CRYSTAL_COMPUTER_ZPM = addItem(642, "circuit.crystal_computer").setUnificationData(OrePrefix.circuit, Tier.ZPM);
        CRYSTAL_MAINFRAME_UV = addItem(643, "circuit.crystal_mainframe").setUnificationData(OrePrefix.circuit, Tier.UV);

        // T7: Wetware
        WETWARE_PROCESSOR_LUV = addItem(644, "circuit.wetware_processor").setUnificationData(OrePrefix.circuit,
                Tier.LuV);
        WETWARE_PROCESSOR_ASSEMBLY_ZPM = addItem(645, "circuit.wetware_assembly").setUnificationData(OrePrefix.circuit,
                Tier.ZPM);
        WETWARE_SUPER_COMPUTER_UV = addItem(646, "circuit.wetware_computer").setUnificationData(OrePrefix.circuit,
                Tier.UV);
        WETWARE_MAINFRAME_UHV = addItem(647, "circuit.wetware_mainframe").setUnificationData(OrePrefix.circuit,
                Tier.UHV);

        // T8: Bioware

        // T9: Optical

        // T10: Exotic

        // T11: Cosmic

        // T12: Supra-Causal

        // T13: ???

        // Crystal Circuit Components: ID 701-705
        RAW_CRYSTAL_CHIP = addItem(701, "crystal.raw");
        RAW_CRYSTAL_CHIP_PART = addItem(702, "crystal.raw_chip");
        ENGRAVED_CRYSTAL_CHIP = addItem(703, "engraved.crystal_chip");
        CRYSTAL_CENTRAL_PROCESSING_UNIT = addItem(704, "crystal.central_processing_unit");
        CRYSTAL_SYSTEM_ON_CHIP = addItem(705, "crystal.system_on_chip");

        // Wetware Circuit Components: ID 706-710
        NEURO_PROCESSOR = addItem(708, "processor.neuro");
        STEM_CELLS = addItem(709, "stem_cells");
        PETRI_DISH = addItem(710, "petri_dish");

        // Turbine Rotors: ID 711-715
        TURBINE_ROTOR = addItem(711, "turbine_rotor").addComponents(new TurbineRotorBehavior());

        // Battery Hulls: ID 716-730
        BATTERY_HULL_LV = addItem(717, "battery.hull.lv")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.BatteryAlloy, M))); // plate
        BATTERY_HULL_MV = addItem(718, "battery.hull.mv")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.BatteryAlloy, M * 3))); // plate * 3
        BATTERY_HULL_HV = addItem(719, "battery.hull.hv")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.BatteryAlloy, M * 9))); // plate * 9
        BATTERY_HULL_SMALL_VANADIUM = addItem(720, "battery.hull.ev")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.BlueSteel, M * 2)));
        BATTERY_HULL_MEDIUM_VANADIUM = addItem(721, "battery.hull.iv")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.RoseGold, M * 6)));
        BATTERY_HULL_LARGE_VANADIUM = addItem(722, "battery.hull.luv")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.RedSteel, M * 18)));
        BATTERY_HULL_MEDIUM_NAQUADRIA = addItem(723, "battery.hull.zpm")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Europium, M * 6)));
        BATTERY_HULL_LARGE_NAQUADRIA = addItem(724, "battery.hull.uv")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Americium, M * 18)));

        // Batteries: 731-775
        BATTERY_ULV_TANTALUM = addItem(731, "battery.re.ulv.tantalum")
                .addComponents(ElectricStats.createRechargeableBattery(1000, GTValues.ULV))
                .setUnificationData(OrePrefix.battery, Tier.ULV).setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        BATTERY_LV_SODIUM = addItem(732, "battery.re.lv.sodium")
                .addComponents(ElectricStats.createRechargeableBattery(80000, GTValues.LV))
                .setUnificationData(OrePrefix.battery, Tier.LV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        BATTERY_MV_SODIUM = addItem(733, "battery.re.mv.sodium")
                .addComponents(ElectricStats.createRechargeableBattery(360000, GTValues.MV))
                .setUnificationData(OrePrefix.battery, Tier.MV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        BATTERY_HV_SODIUM = addItem(734, "battery.re.hv.sodium")
                .addComponents(ElectricStats.createRechargeableBattery(1200000, GTValues.HV))
                .setUnificationData(OrePrefix.battery, Tier.HV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        BATTERY_LV_LITHIUM = addItem(735, "battery.re.lv.lithium")
                .addComponents(ElectricStats.createRechargeableBattery(120000, GTValues.LV))
                .setUnificationData(OrePrefix.battery, Tier.LV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        BATTERY_MV_LITHIUM = addItem(736, "battery.re.mv.lithium")
                .addComponents(ElectricStats.createRechargeableBattery(420000, GTValues.MV))
                .setUnificationData(OrePrefix.battery, Tier.MV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        BATTERY_HV_LITHIUM = addItem(737, "battery.re.hv.lithium")
                .addComponents(ElectricStats.createRechargeableBattery(1800000, GTValues.HV))
                .setUnificationData(OrePrefix.battery, Tier.HV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        BATTERY_LV_CADMIUM = addItem(738, "battery.re.lv.cadmium")
                .addComponents(ElectricStats.createRechargeableBattery(100000, GTValues.LV))
                .setUnificationData(OrePrefix.battery, Tier.LV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        BATTERY_MV_CADMIUM = addItem(739, "battery.re.mv.cadmium")
                .addComponents(ElectricStats.createRechargeableBattery(400000, GTValues.MV))
                .setUnificationData(OrePrefix.battery, Tier.MV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        BATTERY_HV_CADMIUM = addItem(740, "battery.re.hv.cadmium")
                .addComponents(ElectricStats.createRechargeableBattery(1600000, GTValues.HV))
                .setUnificationData(OrePrefix.battery, Tier.HV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        ENERGIUM_CRYSTAL = addItem(741, "energy_crystal")
                .addComponents(ElectricStats.createRechargeableBattery(6_400_000L, GTValues.HV))
                .setUnificationData(OrePrefix.battery, Tier.HV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        LAPOTRON_CRYSTAL = addItem(742, "lapotron_crystal")
                .addComponents(ElectricStats.createRechargeableBattery(25_000_000L, GTValues.EV))
                .setUnificationData(OrePrefix.battery, Tier.EV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        BATTERY_EV_VANADIUM = addItem(743, "battery.ev.vanadium")
                .addComponents(ElectricStats.createRechargeableBattery(10_240_000L, GTValues.EV))
                .setUnificationData(OrePrefix.battery, Tier.EV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        BATTERY_IV_VANADIUM = addItem(744, "battery.iv.vanadium")
                .addComponents(ElectricStats.createRechargeableBattery(40_960_000L, GTValues.IV))
                .setUnificationData(OrePrefix.battery, Tier.IV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        BATTERY_LUV_VANADIUM = addItem(745, "battery.luv.vanadium")
                .addComponents(ElectricStats.createRechargeableBattery(163_840_000L, GTValues.LuV))
                .setUnificationData(OrePrefix.battery, Tier.LuV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        BATTERY_ZPM_NAQUADRIA = addItem(746, "battery.zpm.naquadria")
                .addComponents(ElectricStats.createRechargeableBattery(655_360_000L, GTValues.ZPM))
                .setUnificationData(OrePrefix.battery, Tier.ZPM).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        BATTERY_UV_NAQUADRIA = addItem(747, "battery.uv.naquadria")
                .addComponents(ElectricStats.createRechargeableBattery(2_621_440_000L, GTValues.UV))
                .setUnificationData(OrePrefix.battery, Tier.UV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        ENERGY_LAPOTRONIC_ORB = addItem(748, "energy.lapotronic_orb")
                .addComponents(ElectricStats.createRechargeableBattery(250_000_000L, GTValues.IV))
                .setUnificationData(OrePrefix.battery, Tier.IV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        ENERGY_LAPOTRONIC_ORB_CLUSTER = addItem(749, "energy.lapotronic_orb_cluster")
                .addComponents(ElectricStats.createRechargeableBattery(1_000_000_000L, GTValues.LuV))
                .setUnificationData(OrePrefix.battery, Tier.LuV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        ENERGY_MODULE = addItem(750, "energy.module")
                .addComponents(
                        new IItemComponent[] { ElectricStats.createRechargeableBattery(4_000_000_000L, GTValues.ZPM) })
                .setUnificationData(OrePrefix.battery, Tier.ZPM).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        ENERGY_CLUSTER = addItem(751, "energy.cluster")
                .addComponents(
                        new IItemComponent[] { ElectricStats.createRechargeableBattery(20_000_000_000L, GTValues.UV) })
                .setUnificationData(OrePrefix.battery, Tier.UV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        ZERO_POINT_MODULE = addItem(752, "zpm")
                .addComponents(ElectricStats.createBattery(2000000000000L, GTValues.ZPM, true)).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);
        ULTIMATE_BATTERY = addItem(753, "max.battery")
                .addComponents(ElectricStats.createRechargeableBattery(Long.MAX_VALUE, GTValues.UHV))
                .setUnificationData(OrePrefix.battery, Tier.UHV).setModelAmount(8)
                .setCreativeTabs(GTCreativeTabs.TAB_GREGTECH_TOOLS);

        POWER_THRUSTER = addItem(776, "power_thruster").setRarity(EnumRarity.UNCOMMON);
        POWER_THRUSTER_ADVANCED = addItem(777, "power_thruster_advanced").setRarity(EnumRarity.RARE);
        GRAVITATION_ENGINE = addItem(778, "gravitation_engine").setRarity(EnumRarity.EPIC);

        // Plugins: 780-799
        PLUGIN_ADVANCED_MONITOR = addItem(780, "plugin.advanced_monitor")
                .addComponents(new AdvancedMonitorPluginBehavior());
        PLUGIN_FAKE_GUI = addItem(781, "plugin.fake_gui").addComponents(new FakeGuiPluginBehavior());
        PLUGIN_ONLINE_PIC = addItem(782, "plugin.online_pic").addComponents(new OnlinePicPluginBehavior());
        PLUGIN_TEXT = addItem(783, "plugin.text").addComponents(new TextPluginBehavior());

        // Records: 800-819
        SUS_RECORD = addItem(800, "record.sus").addComponents(new MusicDiscStats(GTSoundEvents.SUS_RECORD))
                .setRarity(EnumRarity.RARE).setMaxStackSize(1).setInvisible();

        // Dyed Glass Lenses: 820-840
        for (int i = 0; i < MarkerMaterials.Color.VALUES.length; i++) {
            MarkerMaterial color = MarkerMaterials.Color.VALUES[i];
            if (color != MarkerMaterials.Color.White) {
                GLASS_LENSES.put(color, addItem(820 + i, String.format("glass_lens.%s", color.toString())));
            }
        }

        // Misc 1000+
        NAN_CERTIFICATE = addItem(1000, "nan.certificate").setRarity(EnumRarity.EPIC);
        FERTILIZER = addItem(1001, "fertilizer").addComponents(new FertilizerBehavior());
        BLACKLIGHT = addItem(1002, "blacklight");

        LOGO = addItem(1003, "logo").setInvisible();
        LOGO.getMetaItem().addPropertyOverride(new ResourceLocation("xmas"), (s, w, e) -> GTValues.XMAS.get() ? 1 : 0);

        MULTIBLOCK_BUILDER = addItem(1004, "tool.multiblock_builder").addComponents(new MultiblockBuilderBehavior())
                .setMaxStackSize(1);

        ANODE_BASKET = addItem(1005, "basket.anode");
        FUEL_CLADDING = addItem(1006, "cladding.fuel");
    }
}
