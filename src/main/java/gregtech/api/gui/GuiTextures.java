package gregtech.api.gui;

import gregtech.api.gui.resources.AdoptableTextureArea;
import gregtech.api.gui.resources.SizedTextureArea;
import gregtech.api.gui.resources.SteamTexture;
import gregtech.api.gui.resources.TextureArea;

public class GuiTextures {

    // GREGTECH
    public static final TextureArea GREGTECH_LOGO = TextureArea.fullImage("textures/gui/icon/gregtech_logo.png");
    public static final TextureArea GREGTECH_LOGO_XMAS = TextureArea
            .fullImage("textures/gui/icon/gregtech_logo_xmas.png");
    public static final TextureArea GREGTECH_LOGO_DARK = TextureArea
            .fullImage("textures/gui/icon/gregtech_logo_dark.png");
    public static final TextureArea GREGTECH_LOGO_BLINKING_YELLOW = TextureArea
            .fullImage("textures/gui/icon/gregtech_logo_blinking_yellow.png");
    public static final TextureArea GREGTECH_LOGO_BLINKING_RED = TextureArea
            .fullImage("textures/gui/icon/gregtech_logo_blinking_red.png");

    // BASE TEXTURES
    public static final TextureArea BACKGROUND = AdoptableTextureArea.fullImage("textures/gui/base/background.png", 176,
            166, 3, 3);
    // todo try to remove
    public static final TextureArea BORDERED_BACKGROUND = AdoptableTextureArea
            .fullImage("textures/gui/base/bordered_background.png", 195, 136, 4, 4);
    // todo try to remove
    public static final TextureArea BOXED_BACKGROUND = AdoptableTextureArea
            .fullImage("textures/gui/base/boxed_background.png", 256, 174, 11, 11);
    public static final SteamTexture BACKGROUND_STEAM = SteamTexture.fullImage("textures/gui/base/background_%s.png",
            176, 166, 3, 3);
    public static final TextureArea CLIPBOARD_BACKGROUND = TextureArea
            .fullImage("textures/gui/base/clipboard_background.png");
    public static final TextureArea CLIPBOARD_PAPER_BACKGROUND = TextureArea
            .fullImage("textures/gui/base/clipboard_paper_background.png");

    public static final TextureArea BLANK = AdoptableTextureArea.fullImage("textures/gui/base/blank.png", 1, 1, 0, 0);
    public static final TextureArea BLANK_TRANSPARENT = AdoptableTextureArea
            .fullImage("textures/gui/base/blank_transparent.png", 1, 1, 0, 0);
    public static final TextureArea DISPLAY = AdoptableTextureArea.fullImage("textures/gui/base/display.png", 143, 75,
            2, 2);
    public static final SteamTexture DISPLAY_STEAM = SteamTexture.fullImage("textures/gui/base/display_%s.png", 143, 75,
            2, 2);
    public static final TextureArea FLUID_SLOT = AdoptableTextureArea.fullImage("textures/gui/base/fluid_slot.png", 18,
            18, 1, 1);
    public static final TextureArea FLUID_TANK_BACKGROUND = TextureArea
            .fullImage("textures/gui/base/fluid_tank_background.png");
    public static final TextureArea FLUID_TANK_OVERLAY = TextureArea
            .fullImage("textures/gui/base/fluid_tank_overlay.png");
    public static final TextureArea SLOT = AdoptableTextureArea.fullImage("textures/gui/base/slot.png", 18, 18, 1, 1);
    public static final TextureArea SLOT_DARK = AdoptableTextureArea.fullImage("textures/gui/base/slot_dark.png", 18,
            18, 1, 1);
    @Deprecated // idek what this texture is
    public static final TextureArea SLOT_DARKENED = TextureArea.fullImage("textures/gui/base/darkened_slot.png");
    public static final SteamTexture SLOT_STEAM = SteamTexture.fullImage("textures/gui/base/slot_%s.png");
    public static final TextureArea TOGGLE_BUTTON_BACK = TextureArea
            .fullImage("textures/gui/widget/toggle_button_background.png");

    // FLUID & ITEM OUTPUT BUTTONS
    public static final TextureArea BLOCKS_INPUT = TextureArea.fullImage("textures/gui/widget/button_blocks_input.png");
    public static final TextureArea BUTTON = TextureArea.fullImage("textures/gui/widget/button.png");
    public static final TextureArea BUTTON_ALLOW_IMPORT_EXPORT = TextureArea
            .fullImage("textures/gui/widget/button_allow_import_export.png");
    public static final TextureArea BUTTON_BLACKLIST = TextureArea
            .fullImage("textures/gui/widget/button_blacklist.png");
    public static final TextureArea BUTTON_CLEAR_GRID = TextureArea
            .fullImage("textures/gui/widget/button_clear_grid.png");
    public static final TextureArea BUTTON_FILTER_DAMAGE = TextureArea
            .fullImage("textures/gui/widget/button_filter_damage.png");
    public static final TextureArea BUTTON_FILTER_NBT = TextureArea
            .fullImage("textures/gui/widget/button_filter_nbt.png");
    public static final TextureArea BUTTON_FLUID_OUTPUT = TextureArea
            .fullImage("textures/gui/widget/button_fluid_output_overlay.png");
    public static final TextureArea BUTTON_ITEM_OUTPUT = TextureArea
            .fullImage("textures/gui/widget/button_item_output_overlay.png");
    public static final TextureArea BUTTON_LOCK = TextureArea.fullImage("textures/gui/widget/button_lock.png");
    public static final TextureArea BUTTON_FLUID_VOID = TextureArea
            .fullImage("textures/gui/widget/button_fluid_void.png");
    public static final TextureArea BUTTON_ITEM_VOID = TextureArea
            .fullImage("textures/gui/widget/button_item_void.png");
    public static final TextureArea BUTTON_VOID_NONE = TextureArea
            .fullImage("textures/gui/widget/button_void_none.png");
    public static final TextureArea BUTTON_VOID_MULTIBLOCK = TextureArea
            .fullImage("textures/gui/widget/button_void_multiblock.png");
    public static final TextureArea BUTTON_LEFT = TextureArea.fullImage("textures/gui/widget/left.png");
    public static final TextureArea BUTTON_OVERCLOCK = TextureArea
            .fullImage("textures/gui/widget/button_overclock.png");
    public static final TextureArea BUTTON_PUBLIC_PRIVATE = TextureArea
            .fullImage("textures/gui/widget/button_public_private.png");
    public static final TextureArea BUTTON_RIGHT = TextureArea.fullImage("textures/gui/widget/right.png");
    public static final TextureArea BUTTON_SWITCH_VIEW = TextureArea
            .fullImage("textures/gui/widget/button_switch_view.png");
    public static final TextureArea BUTTON_WORKING_ENABLE = TextureArea
            .fullImage("textures/gui/widget/button_working_enable.png");
    public static final TextureArea BUTTON_INT_CIRCUIT_PLUS = TextureArea
            .fullImage("textures/gui/widget/button_circuit_plus.png");
    public static final TextureArea BUTTON_INT_CIRCUIT_MINUS = TextureArea
            .fullImage("textures/gui/widget/button_circuit_minus.png");
    public static final TextureArea CLIPBOARD_BUTTON = TextureArea
            .fullImage("textures/gui/widget/clipboard_button.png");
    public static final SizedTextureArea CLIPBOARD_TEXT_BOX = AdoptableTextureArea
            .fullImage("textures/gui/widget/clipboard_text_box.png", 9, 18, 1, 1);
    public static final TextureArea DISTRIBUTION_MODE = TextureArea
            .fullImage("textures/gui/widget/button_distribution_mode.png");
    public static final TextureArea LOCK = TextureArea.fullImage("textures/gui/widget/lock.png");
    public static final TextureArea LOCK_WHITE = TextureArea.fullImage("textures/gui/widget/lock_white.png");
    public static final TextureArea SWITCH = TextureArea.fullImage("textures/gui/widget/switch.png");
    public static final TextureArea SWITCH_HORIZONTAL = TextureArea
            .fullImage("textures/gui/widget/switch_horizontal.png");
    public static final SizedTextureArea VANILLA_BUTTON = SizedTextureArea
            .fullImage("textures/gui/widget/vanilla_button.png", 200, 40);
    public static final TextureArea BUTTON_POWER = TextureArea.fullImage("textures/gui/widget/button_power.png");
    public static final TextureArea BUTTON_POWER_DETAIL = TextureArea
            .fullImage("textures/gui/widget/button_power_detail.png");
    public static final TextureArea BUTTON_DISTINCT_BUSES = TextureArea
            .fullImage("textures/gui/widget/button_distinct_buses.png");
    public static final TextureArea BUTTON_NO_DISTINCT_BUSES = TextureArea
            .fullImage("textures/gui/widget/button_no_distinct_buses.png");
    public static final TextureArea BUTTON_NO_FLEX = TextureArea.fullImage("textures/gui/widget/button_no_flex.png");
    public static final TextureArea BUTTON_MULTI_MAP = TextureArea
            .fullImage("textures/gui/widget/button_multi_map.png");
    public static final TextureArea BUTTON_MINER_MODES = TextureArea
            .fullImage("textures/gui/widget/button_miner_modes.png");
    public static final TextureArea BUTTON_THROTTLE_MINUS = TextureArea
            .fullImage("textures/gui/widget/button_throttle_minus.png");
    public static final TextureArea BUTTON_THROTTLE_PLUS = TextureArea
            .fullImage("textures/gui/widget/button_throttle_plus.png");
    public static final TextureArea BUTTON_CONTROL_ROD_HELPER = TextureArea
            .fullImage("textures/gui/widget/button_control_rod_helper.png");

    // INDICATORS & ICONS
    public static final TextureArea INDICATOR_NO_ENERGY = TextureArea
            .fullImage("textures/gui/base/indicator_no_energy.png");
    public static final SteamTexture INDICATOR_NO_STEAM = SteamTexture
            .fullImage("textures/gui/base/indicator_no_steam_%s.png");
    public static final TextureArea TANK_ICON = TextureArea.fullImage("textures/gui/base/tank_icon.png");

    // WIDGET UI RELATED
    public static final TextureArea SLIDER_BACKGROUND = TextureArea
            .fullImage("textures/gui/widget/slider_background.png");
    public static final TextureArea SLIDER_BACKGROUND_VERTICAL = TextureArea
            .fullImage("textures/gui/widget/slider_background_vertical.png");
    public static final TextureArea SLIDER_ICON = TextureArea.fullImage("textures/gui/widget/slider.png");
    public static final TextureArea DARK_SLIDER_BACKGROUND = TextureArea
            .fullImage("textures/gui/widget/dark_slider_background.png");
    public static final TextureArea DARK_SLIDER_BACKGROUND_VERTICAL = TextureArea
            .fullImage("textures/gui/widget/dark_slider_background_vertical.png");
    public static final TextureArea DARK_SLIDER_ICON = TextureArea.fullImage("textures/gui/widget/dark_slider.png");
    public static final TextureArea MAINTENANCE_ICON = TextureArea
            .fullImage("textures/gui/widget/button_maintenance.png");

    // PRIMITIVE
    public static final TextureArea PRIMITIVE_BACKGROUND = AdoptableTextureArea
            .fullImage("textures/gui/primitive/primitive_background.png", 176, 166, 3, 3);
    public static final TextureArea PRIMITIVE_SLOT = AdoptableTextureArea
            .fullImage("textures/gui/primitive/primitive_slot.png", 18, 18, 1, 1);
    public static final TextureArea PRIMITIVE_FURNACE_OVERLAY = TextureArea
            .fullImage("textures/gui/primitive/overlay_primitive_furnace.png");
    public static final TextureArea PRIMITIVE_DUST_OVERLAY = TextureArea
            .fullImage("textures/gui/primitive/overlay_primitive_dust.png");
    public static final TextureArea PRIMITIVE_INGOT_OVERLAY = TextureArea
            .fullImage("textures/gui/primitive/overlay_primitive_ingot.png");
    public static final TextureArea PRIMITIVE_LARGE_FLUID_TANK = TextureArea
            .fullImage("textures/gui/primitive/primitive_large_fluid_tank.png");
    public static final TextureArea PRIMITIVE_LARGE_FLUID_TANK_OVERLAY = TextureArea
            .fullImage("textures/gui/primitive/primitive_large_fluid_tank_overlay.png");
    public static final TextureArea PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR = TextureArea
            .fullImage("textures/gui/primitive/progress_bar_primitive_blast_furnace.png");

    // SLOT OVERLAYS
    public static final TextureArea ATOMIC_OVERLAY_1 = TextureArea
            .fullImage("textures/gui/overlay/atomic_overlay_1.png");
    public static final TextureArea ATOMIC_OVERLAY_2 = TextureArea
            .fullImage("textures/gui/overlay/atomic_overlay_2.png");
    public static final TextureArea ARROW_INPUT_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/arrow_input_overlay.png");
    public static final TextureArea ARROW_OUTPUT_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/arrow_output_overlay.png");
    public static final TextureArea BATTERY_OVERLAY = TextureArea.fullImage("textures/gui/overlay/battery_overlay.png");
    public static final TextureArea BEAKER_OVERLAY_1 = TextureArea
            .fullImage("textures/gui/overlay/beaker_overlay_1.png");
    public static final TextureArea BEAKER_OVERLAY_2 = TextureArea
            .fullImage("textures/gui/overlay/beaker_overlay_2.png");
    public static final TextureArea BEAKER_OVERLAY_3 = TextureArea
            .fullImage("textures/gui/overlay/beaker_overlay_3.png");
    public static final TextureArea BEAKER_OVERLAY_4 = TextureArea
            .fullImage("textures/gui/overlay/beaker_overlay_4.png");
    public static final TextureArea BENDER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/bender_overlay.png");
    public static final TextureArea BOX_OVERLAY = TextureArea.fullImage("textures/gui/overlay/box_overlay.png");
    public static final TextureArea BOXED_OVERLAY = TextureArea.fullImage("textures/gui/overlay/boxed_overlay.png");
    public static final TextureArea BREWER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/brewer_overlay.png");
    public static final TextureArea CANNER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/canner_overlay.png");
    public static final TextureArea CHARGER_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/charger_slot_overlay.png");
    public static final TextureArea CANISTER_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/canister_overlay.png");
    public static final SteamTexture CANISTER_OVERLAY_STEAM = SteamTexture
            .fullImage("textures/gui/overlay/canister_overlay_%s.png");
    public static final TextureArea CENTRIFUGE_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/centrifuge_overlay.png");
    public static final TextureArea CIRCUIT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/circuit_overlay.png");
    public static final SteamTexture COAL_OVERLAY_STEAM = SteamTexture
            .fullImage("textures/gui/overlay/coal_overlay_%s.png");
    public static final TextureArea COMPRESSOR_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/compressor_overlay.png");
    public static final SteamTexture COMPRESSOR_OVERLAY_STEAM = SteamTexture
            .fullImage("textures/gui/overlay/compressor_overlay_%s.png");
    public static final TextureArea CRACKING_OVERLAY_1 = TextureArea
            .fullImage("textures/gui/overlay/cracking_overlay_1.png");
    public static final TextureArea CRACKING_OVERLAY_2 = TextureArea
            .fullImage("textures/gui/overlay/cracking_overlay_2.png");
    public static final TextureArea CRUSHED_ORE_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/crushed_ore_overlay.png");
    public static final SteamTexture CRUSHED_ORE_OVERLAY_STEAM = SteamTexture
            .fullImage("textures/gui/overlay/crushed_ore_overlay_%s.png");
    public static final TextureArea CRYSTAL_OVERLAY = TextureArea.fullImage("textures/gui/overlay/crystal_overlay.png");
    public static final TextureArea CUTTER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/cutter_overlay.png");
    public static final TextureArea DARK_CANISTER_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/dark_canister_overlay.png");
    public static final TextureArea DUST_OVERLAY = TextureArea.fullImage("textures/gui/overlay/dust_overlay.png");
    public static final SteamTexture DUST_OVERLAY_STEAM = SteamTexture
            .fullImage("textures/gui/overlay/dust_overlay_%s.png");
    public static final TextureArea EXTRACTOR_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/extractor_overlay.png");
    public static final SteamTexture EXTRACTOR_OVERLAY_STEAM = SteamTexture
            .fullImage("textures/gui/overlay/extractor_overlay_%s.png");
    public static final TextureArea FILTER_SLOT_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/filter_slot_overlay.png");
    public static final TextureArea FURNACE_OVERLAY_1 = TextureArea
            .fullImage("textures/gui/overlay/furnace_overlay_1.png");
    public static final TextureArea FURNACE_OVERLAY_2 = TextureArea
            .fullImage("textures/gui/overlay/furnace_overlay_2.png");
    public static final SteamTexture FURNACE_OVERLAY_STEAM = SteamTexture
            .fullImage("textures/gui/overlay/furnace_overlay_%s.png");
    public static final TextureArea HAMMER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/hammer_overlay.png");
    public static final SteamTexture HAMMER_OVERLAY_STEAM = SteamTexture
            .fullImage("textures/gui/overlay/hammer_overlay_%s.png");
    public static final TextureArea HEATING_OVERLAY_1 = TextureArea
            .fullImage("textures/gui/overlay/heating_overlay_1.png");
    public static final TextureArea HEATING_OVERLAY_2 = TextureArea
            .fullImage("textures/gui/overlay/heating_overlay_2.png");
    public static final TextureArea IMPLOSION_OVERLAY_1 = TextureArea
            .fullImage("textures/gui/overlay/implosion_overlay_1.png");
    public static final TextureArea IMPLOSION_OVERLAY_2 = TextureArea
            .fullImage("textures/gui/overlay/implosion_overlay_2.png");
    public static final TextureArea IN_SLOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/in_slot_overlay.png");
    public static final SteamTexture IN_SLOT_OVERLAY_STEAM = SteamTexture
            .fullImage("textures/gui/overlay/in_slot_overlay_%s.png");
    public static final TextureArea INGOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/ingot_overlay.png");
    public static final TextureArea INT_CIRCUIT_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/int_circuit_overlay.png");
    public static final TextureArea LENS_OVERLAY = TextureArea.fullImage("textures/gui/overlay/lens_overlay.png");
    public static final TextureArea LIGHTNING_OVERLAY_1 = TextureArea
            .fullImage("textures/gui/overlay/lightning_overlay_1.png");
    public static final TextureArea LIGHTNING_OVERLAY_2 = TextureArea
            .fullImage("textures/gui/overlay/lightning_overlay_2.png");
    public static final TextureArea MOLD_OVERLAY = TextureArea.fullImage("textures/gui/overlay/mold_overlay.png");
    public static final TextureArea MOLECULAR_OVERLAY_1 = TextureArea
            .fullImage("textures/gui/overlay/molecular_overlay_1.png");
    public static final TextureArea MOLECULAR_OVERLAY_2 = TextureArea
            .fullImage("textures/gui/overlay/molecular_overlay_2.png");
    public static final TextureArea MOLECULAR_OVERLAY_3 = TextureArea
            .fullImage("textures/gui/overlay/molecular_overlay_3.png");
    public static final TextureArea MOLECULAR_OVERLAY_4 = TextureArea
            .fullImage("textures/gui/overlay/molecular_overlay_4.png");
    public static final TextureArea OUT_SLOT_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/out_slot_overlay.png");
    public static final SteamTexture OUT_SLOT_OVERLAY_STEAM = SteamTexture
            .fullImage("textures/gui/overlay/out_slot_overlay_%s.png");
    public static final TextureArea PAPER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/paper_overlay.png");
    public static final TextureArea PRINTED_PAPER_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/printed_paper_overlay.png");
    public static final TextureArea PIPE_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/pipe_overlay_2.png");
    public static final TextureArea PIPE_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/pipe_overlay_1.png");
    public static final TextureArea PRESS_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/press_overlay_1.png");
    public static final TextureArea PRESS_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/press_overlay_2.png");
    public static final TextureArea PRESS_OVERLAY_3 = TextureArea.fullImage("textures/gui/overlay/press_overlay_3.png");
    public static final TextureArea PRESS_OVERLAY_4 = TextureArea.fullImage("textures/gui/overlay/press_overlay_4.png");
    public static final TextureArea SAWBLADE_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/sawblade_overlay.png");
    public static final TextureArea SOLIDIFIER_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/solidifier_overlay.png");
    public static final TextureArea STRING_SLOT_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/string_slot_overlay.png");
    public static final TextureArea TOOL_SLOT_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/tool_slot_overlay.png");
    public static final TextureArea TURBINE_OVERLAY = TextureArea.fullImage("textures/gui/overlay/turbine_overlay.png");
    public static final TextureArea VIAL_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/vial_overlay_1.png");
    public static final TextureArea VIAL_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/vial_overlay_2.png");
    public static final TextureArea WIREMILL_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/wiremill_overlay.png");
    public static final TextureArea POSITIVE_MATTER_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/positive_matter_overlay.png");
    public static final TextureArea NEUTRAL_MATTER_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/neutral_matter_overlay.png");
    public static final TextureArea DATA_ORB_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/data_orb_overlay.png");
    public static final TextureArea SCANNER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/scanner_overlay.png");
    public static final TextureArea DUCT_TAPE_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/duct_tape_overlay.png");
    public static final TextureArea RESEARCH_STATION_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/research_station_overlay.png");

    // PROGRESS BARS
    public static final TextureArea PROGRESS_BAR_ARC_FURNACE = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_arc_furnace.png");
    public static final TextureArea PROGRESS_BAR_ARROW = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_arrow.png");
    public static final SteamTexture PROGRESS_BAR_ARROW_STEAM = SteamTexture
            .fullImage("textures/gui/progress_bar/progress_bar_arrow_%s.png");
    public static final TextureArea PROGRESS_BAR_ARROW_MULTIPLE = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_arrow_multiple.png");
    public static final TextureArea PROGRESS_BAR_ASSEMBLY_LINE = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_assembly_line.png");
    public static final TextureArea PROGRESS_BAR_ASSEMBLY_LINE_ARROW = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_assembly_line_arrow.png");
    public static final TextureArea PROGRESS_BAR_BATH = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_bath.png");
    public static final TextureArea PROGRESS_BAR_BENDING = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_bending.png");
    public static final SteamTexture PROGRESS_BAR_BOILER_EMPTY = SteamTexture
            .fullImage("textures/gui/progress_bar/progress_bar_boiler_empty_%s.png");
    public static final SteamTexture PROGRESS_BAR_BOILER_FUEL = SteamTexture
            .fullImage("textures/gui/progress_bar/progress_bar_boiler_fuel_%s.png");
    public static final TextureArea PROGRESS_BAR_BOILER_HEAT = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_boiler_heat.png");
    public static final TextureArea PROGRESS_BAR_CANNER = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_canner.png");
    public static final TextureArea PROGRESS_BAR_CIRCUIT = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_circuit.png");
    public static final TextureArea PROGRESS_BAR_CIRCUIT_ASSEMBLER = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_circuit_assembler.png");
    public static final TextureArea PROGRESS_BAR_COKE_OVEN = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_coke_oven.png");
    public static final TextureArea PROGRESS_BAR_COMPRESS = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_compress.png");
    public static final SteamTexture PROGRESS_BAR_COMPRESS_STEAM = SteamTexture
            .fullImage("textures/gui/progress_bar/progress_bar_compress_%s.png");
    public static final TextureArea PROGRESS_BAR_CRACKING = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_cracking.png");
    public static final TextureArea PROGRESS_BAR_CRACKING_INPUT = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_cracking_2.png");
    public static final TextureArea PROGRESS_BAR_CRYSTALLIZATION = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_crystallization.png");
    public static final TextureArea PROGRESS_BAR_DISTILLATION_TOWER = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_distillation_tower.png");
    public static final TextureArea PROGRESS_BAR_EXTRACT = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_extract.png");
    public static final SteamTexture PROGRESS_BAR_EXTRACT_STEAM = SteamTexture
            .fullImage("textures/gui/progress_bar/progress_bar_extract_%s.png");
    public static final TextureArea PROGRESS_BAR_EXTRUDER = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_extruder.png");
    public static final TextureArea PROGRESS_BAR_FUSION = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_fusion.png");
    public static final TextureArea PROGRESS_BAR_GAS_COLLECTOR = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_gas_collector.png");
    public static final TextureArea PROGRESS_BAR_HAMMER = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_hammer.png");
    public static final SteamTexture PROGRESS_BAR_HAMMER_STEAM = SteamTexture
            .fullImage("textures/gui/progress_bar/progress_bar_hammer_%s.png");
    public static final TextureArea PROGRESS_BAR_HAMMER_BASE = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_hammer_base.png");
    public static final SteamTexture PROGRESS_BAR_HAMMER_BASE_STEAM = SteamTexture
            .fullImage("textures/gui/progress_bar/progress_bar_hammer_base_%s.png");
    public static final TextureArea PROGRESS_BAR_LATHE = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_lathe.png");
    public static final TextureArea PROGRESS_BAR_LATHE_BASE = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_lathe_base.png");
    public static final TextureArea PROGRESS_BAR_MACERATE = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_macerate.png");
    public static final SteamTexture PROGRESS_BAR_MACERATE_STEAM = SteamTexture
            .fullImage("textures/gui/progress_bar/progress_bar_macerate_%s.png");
    public static final TextureArea PROGRESS_BAR_MAGNET = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_magnet.png");
    public static final TextureArea PROGRESS_BAR_MASS_FAB = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_mass_fab.png");
    public static final TextureArea PROGRESS_BAR_MIXER = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_mixer.png");
    public static final TextureArea PROGRESS_BAR_PACKER = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_packer.png");
    public static final TextureArea PROGRESS_BAR_RECYCLER = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_recycler.png");
    public static final TextureArea PROGRESS_BAR_REPLICATOR = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_replicator.png");
    public static final TextureArea PROGRESS_BAR_SIFT = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_sift.png");
    public static final TextureArea PROGRESS_BAR_SLICE = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_slice.png");
    public static final SteamTexture PROGRESS_BAR_SOLAR_STEAM = SteamTexture
            .fullImage("textures/gui/progress_bar/progress_bar_solar_%s.png");
    public static final TextureArea PROGRESS_BAR_UNPACKER = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_unpacker.png");
    public static final TextureArea PROGRESS_BAR_WIREMILL = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_wiremill.png");
    public static final TextureArea PROGRESS_BAR_RESEARCH_STATION_1 = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_research_station_1.png");
    public static final TextureArea PROGRESS_BAR_RESEARCH_STATION_2 = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_research_station_2.png");
    public static final TextureArea PROGRESS_BAR_RESEARCH_STATION_BASE = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_research_station_base.png");
    public static final TextureArea PROGRESS_BAR_FUSION_ENERGY = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_fusion_energy.png");
    public static final TextureArea PROGRESS_BAR_FUSION_HEAT = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_fusion_heat.png");
    public static final TextureArea PROGRESS_BAR_MULTI_ENERGY_YELLOW = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_multi_energy_yellow.png");
    public static final TextureArea PROGRESS_BAR_HPCA_COMPUTATION = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_hpca_computation.png");
    public static final TextureArea PROGRESS_BAR_LCE_FUEL = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_lce_fuel.png");
    public static final TextureArea PROGRESS_BAR_LCE_LUBRICANT = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_lce_lubricant.png");
    public static final TextureArea PROGRESS_BAR_LCE_OXYGEN = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_lce_oxygen.png");
    public static final TextureArea PROGRESS_BAR_TURBINE_ROTOR_SPEED = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_turbine_rotor_speed.png");
    public static final TextureArea PROGRESS_BAR_TURBINE_ROTOR_DURABILITY = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_turbine_rotor_durability.png");
    public static final TextureArea PROGRESS_BAR_FLUID_RIG_DEPLETION = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_fluid_rig_depletion.png");
    public static final TextureArea PROGRESS_BAR_FISSION_HEAT = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_fission_heat.png");
    public static final TextureArea PROGRESS_BAR_FISSION_PRESSURE = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_fission_pressure.png");
    public static final TextureArea PROGRESS_BAR_FISSION_ENERGY = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_fission_energy.png");

    // Fusion reactor diagram progress bar parts
    public static final TextureArea PROGRESS_BAR_FUSION_REACTOR_DIAGRAM_BL = TextureArea
            .fullImage("textures/gui/progress_bar/fusion_diagram/bottom_left.png");
    public static final TextureArea PROGRESS_BAR_FUSION_REACTOR_DIAGRAM_BR = TextureArea
            .fullImage("textures/gui/progress_bar/fusion_diagram/bottom_right.png");
    public static final TextureArea PROGRESS_BAR_FUSION_REACTOR_DIAGRAM_TL = TextureArea
            .fullImage("textures/gui/progress_bar/fusion_diagram/top_left.png");
    public static final TextureArea PROGRESS_BAR_FUSION_REACTOR_DIAGRAM_TR = TextureArea
            .fullImage("textures/gui/progress_bar/fusion_diagram/top_right.png");

    // JEI
    public static final TextureArea INFO_ICON = TextureArea.fullImage("textures/gui/widget/information.png");
    public static final TextureArea MULTIBLOCK_CATEGORY = TextureArea
            .fullImage("textures/gui/icon/multiblock_category.png");
    public static final TextureArea ARC_FURNACE_RECYLCING_CATEGORY = TextureArea
            .fullImage("textures/gui/icon/arc_furnace_recycling.png");
    public static final TextureArea MACERATOR_RECYLCING_CATEGORY = TextureArea
            .fullImage("textures/gui/icon/macerator_recycling.png");
    public static final TextureArea EXTRACTOR_RECYLCING_CATEGORY = TextureArea
            .fullImage("textures/gui/icon/extractor_recycling.png");

    // Covers
    public static final TextureArea COVER_MACHINE_CONTROLLER = TextureArea
            .fullImage("textures/items/metaitems/cover.controller.png");

    // Ore Filter
    public static final TextureArea ORE_FILTER_BUTTON_CASE_SENSITIVE = TextureArea
            .fullImage("textures/gui/widget/ore_filter/button_case_sensitive.png");
    public static final TextureArea ORE_FILTER_BUTTON_MATCH_ALL = TextureArea
            .fullImage("textures/gui/widget/ore_filter/button_match_all.png");
    public static final TextureArea ORE_FILTER_INFO = TextureArea.fullImage("textures/gui/widget/ore_filter/info.png");
    public static final TextureArea ORE_FILTER_SUCCESS = TextureArea
            .fullImage("textures/gui/widget/ore_filter/success.png");
    public static final TextureArea ORE_FILTER_ERROR = TextureArea
            .fullImage("textures/gui/widget/ore_filter/error.png");
    public static final TextureArea ORE_FILTER_WARN = TextureArea.fullImage("textures/gui/widget/ore_filter/warn.png");
    public static final TextureArea ORE_FILTER_WAITING = TextureArea
            .fullImage("textures/gui/widget/ore_filter/waiting.png");

    public static final TextureArea ORE_FILTER_MATCH = TextureArea
            .fullImage("textures/gui/widget/ore_filter/match.png");
    public static final TextureArea ORE_FILTER_NO_MATCH = TextureArea
            .fullImage("textures/gui/widget/ore_filter/no_match.png");

    // Terminal
    public static final TextureArea ICON_REMOVE = TextureArea.fullImage("textures/gui/terminal/icon/remove_hover.png");
    public static final TextureArea ICON_UP = TextureArea.fullImage("textures/gui/terminal/icon/up_hover.png");
    public static final TextureArea ICON_DOWN = TextureArea.fullImage("textures/gui/terminal/icon/down_hover.png");
    public static final TextureArea ICON_RIGHT = TextureArea.fullImage("textures/gui/terminal/icon/right_hover.png");
    public static final TextureArea ICON_LEFT = TextureArea.fullImage("textures/gui/terminal/icon/left_hover.png");
    public static final TextureArea ICON_ADD = TextureArea.fullImage("textures/gui/terminal/icon/add_hover.png");

    public final static TextureArea ICON_NEW_PAGE = TextureArea
            .fullImage("textures/gui/terminal/icon/system/memory_card_hover.png");
    public final static TextureArea ICON_LOAD = TextureArea.fullImage("textures/gui/terminal/icon/folder_hover.png");
    public final static TextureArea ICON_SAVE = TextureArea
            .fullImage("textures/gui/terminal/icon/system/save_hover.png");
    public final static TextureArea ICON_LOCATION = TextureArea.fullImage("textures/gui/terminal/icon/guide_hover.png");
    public final static TextureArea ICON_VISIBLE = TextureArea
            .fullImage("textures/gui/terminal/icon/appearance_hover.png");
    public final static TextureArea ICON_CALCULATOR = TextureArea
            .fullImage("textures/gui/terminal/icon/calculator_hover.png");
    public final static TextureArea UI_FRAME_SIDE_UP = TextureArea.fullImage("textures/gui/terminal/frame_side_up.png");
    public final static TextureArea UI_FRAME_SIDE_DOWN = TextureArea
            .fullImage("textures/gui/terminal/frame_side_down.png");

    // Texture Areas
    public static final TextureArea BUTTON_FLUID = TextureArea
            .fullImage("textures/blocks/cover/cover_interface_fluid_button.png");
    public static final TextureArea BUTTON_ITEM = TextureArea
            .fullImage("textures/blocks/cover/cover_interface_item_button.png");
    public static final TextureArea BUTTON_ENERGY = TextureArea
            .fullImage("textures/blocks/cover/cover_interface_energy_button.png");
    public static final TextureArea BUTTON_MACHINE = TextureArea
            .fullImage("textures/blocks/cover/cover_interface_machine_button.png");
    public static final TextureArea BUTTON_INTERFACE = TextureArea
            .fullImage("textures/blocks/cover/cover_interface_computer_button.png");
    public static final TextureArea COVER_INTERFACE_MACHINE_ON_PROXY = TextureArea
            .fullImage("textures/blocks/cover/cover_interface_machine_on_proxy.png");
    public static final TextureArea COVER_INTERFACE_MACHINE_OFF_PROXY = TextureArea
            .fullImage("textures/blocks/cover/cover_interface_machine_off_proxy.png");

    // Lamp item overlay
    public static final TextureArea LAMP_NO_BLOOM = TextureArea
            .fullImage("textures/gui/item_overlay/lamp_no_bloom.png");
    public static final TextureArea LAMP_NO_LIGHT = TextureArea
            .fullImage("textures/gui/item_overlay/lamp_no_light.png");

    // ME hatch/bus
    public static final TextureArea NUMBER_BACKGROUND = TextureArea
            .fullImage("textures/gui/widget/number_background.png");
    public static final TextureArea CONFIG_ARROW = TextureArea.fullImage("textures/gui/widget/config_arrow.png");
    public static final TextureArea CONFIG_ARROW_DARK = TextureArea
            .fullImage("textures/gui/widget/config_arrow_dark.png");
    public static final TextureArea SELECT_BOX = TextureArea.fullImage("textures/gui/widget/select_box.png");
    public static final TextureArea BUTTON_AUTO_PULL = TextureArea
            .fullImage("textures/gui/widget/button_me_auto_pull.png");
    public static final TextureArea ARROW_DOUBLE = TextureArea.fullImage("textures/gui/widget/arrow_double.png");

    // Fusion Reactor custom images
    public static final TextureArea FUSION_REACTOR_MK1_TITLE = TextureArea
            .fullImage("textures/gui/widget/fusion_reactor_mk1_title.png");
    public static final TextureArea FUSION_REACTOR_MK2_TITLE = TextureArea
            .fullImage("textures/gui/widget/fusion_reactor_mk2_title.png");
    public static final TextureArea FUSION_REACTOR_MK3_TITLE = TextureArea
            .fullImage("textures/gui/widget/fusion_reactor_mk3_title.png");
    public static final TextureArea FUSION_REACTOR_DIAGRAM = TextureArea
            .fullImage("textures/gui/widget/fusion_reactor_diagram.png");
    public static final TextureArea FUSION_REACTOR_LEGEND = TextureArea
            .fullImage("textures/gui/widget/fusion_reactor_legend.png");

    // HPCA Component icons
    public static final TextureArea HPCA_COMPONENT_OUTLINE = TextureArea
            .fullImage("textures/gui/widget/hpca/component_outline.png");
    public static final TextureArea HPCA_ICON_EMPTY_COMPONENT = TextureArea
            .fullImage("textures/gui/widget/hpca/empty_component.png");
    public static final TextureArea HPCA_ICON_ADVANCED_COMPUTATION_COMPONENT = TextureArea
            .fullImage("textures/gui/widget/hpca/advanced_computation_component.png");
    public static final TextureArea HPCA_ICON_BRIDGE_COMPONENT = TextureArea
            .fullImage("textures/gui/widget/hpca/bridge_component.png");
    public static final TextureArea HPCA_ICON_COMPUTATION_COMPONENT = TextureArea
            .fullImage("textures/gui/widget/hpca/computation_component.png");
    public static final TextureArea HPCA_ICON_ACTIVE_COOLER_COMPONENT = TextureArea
            .fullImage("textures/gui/widget/hpca/active_cooler_component.png");
    public static final TextureArea HPCA_ICON_HEAT_SINK_COMPONENT = TextureArea
            .fullImage("textures/gui/widget/hpca/heat_sink_component.png");
    public static final TextureArea HPCA_ICON_DAMAGED_ADVANCED_COMPUTATION_COMPONENT = TextureArea
            .fullImage("textures/gui/widget/hpca/damaged_advanced_computation_component.png");
    public static final TextureArea HPCA_ICON_DAMAGED_COMPUTATION_COMPONENT = TextureArea
            .fullImage("textures/gui/widget/hpca/damaged_computation_component.png");
}
