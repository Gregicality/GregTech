package gregtech.api.capability;

import gregtech.common.covers.ender.CoverAbstractEnderLink;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFluidHatch;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class GregtechDataCodes {

    private static int nextId = 0;

    public static int assignId() {
        return nextId++;
    }

    // MTE implementation update codes
    public static final int INITIALIZE_MTE = assignId();
    public static final int UPDATE_FRONT_FACING = assignId();
    public static final int UPDATE_PAINTING_COLOR = assignId();
    public static final int SYNC_MTE_TRAITS = assignId();
    public static final int COVER_ATTACHED_MTE = assignId();
    public static final int COVER_REMOVED_MTE = assignId();
    public static final int UPDATE_COVER_DATA_MTE = assignId();
    public static final int UPDATE_SOUND_MUFFLED = assignId();

    public static final int UPDATE_OUTPUT_FACING = assignId();
    public static final int UPDATE_AUTO_OUTPUT_ITEMS = assignId();
    public static final int UPDATE_AUTO_OUTPUT_FLUIDS = assignId();
    public static final int UPDATE_IS_VOIDING = assignId();

    // Robotic Arm
    public static final int UPDATE_TRANSFER_MODE = assignId();

    // Drum
    public static final int UPDATE_AUTO_OUTPUT = assignId();

    // Steam Machines
    public static final int NEEDS_VENTING = assignId();
    public static final int VENTING_SIDE = assignId();
    public static final int VENTING_STUCK = assignId();
    public static final int BOILER_HEAT = assignId();
    public static final int BOILER_LAST_TICK_STEAM = assignId();

    // Misc TEs (Transformer, World Accelerator)
    public static final int SYNC_TILE_MODE = assignId();

    // Clipboard
    public static final int CREATE_FAKE_UI = assignId();
    public static final int MOUSE_POSITION = assignId();
    public static final int INIT_CLIPBOARD_NBT = assignId();

    public static final int UPDATE_UI = assignId(); // 10-36
    // Pump
    public static final int PUMP_HEAD_LEVEL = assignId();

    // Item Collector, Magic Energy Absorber, Large Boiler, Steam Oven
    public static final int IS_WORKING = assignId();

    // Adjustable Transformer, Adjustable Energy Hatch, Diode
    public static final int AMP_INDEX = assignId();

    // Facade Cover
    public static final int UPDATE_FACADE_STACK = assignId();

    // Pipe implementation update codes
    public static final int UPDATE_INSULATION_COLOR = assignId();
    public static final int UPDATE_CONNECTIONS = assignId();
    public static final int SYNC_COVER_IMPLEMENTATION = assignId();
    public static final int UPDATE_PIPE_TYPE = assignId();
    public static final int UPDATE_PIPE_MATERIAL = assignId();
    public static final int UPDATE_BLOCKED_CONNECTIONS = assignId();
    public static final int UPDATE_FRAME_MATERIAL = assignId();
    public static final int UPDATE_COVER_DATA_PIPE = assignId();
    public static final int COVER_ATTACHED_PIPE = assignId();
    public static final int COVER_REMOVED_PIPE = assignId();
    public static final int PIPE_OPTICAL_ACTIVE = assignId();
    public static final int PIPE_LASER_ACTIVE = assignId();
    public static final int CABLE_TEMPERATURE = assignId();

    // Multiblock implementation update codes
    public static final int SYNC_CONTROLLER = assignId();
    public static final int IS_ROTOR_LOOPING = assignId();
    public static final int FRONT_FACE_FREE = assignId();
    public static final int UPDATE_ROTOR_COLOR = assignId();
    public static final int UPDATE_STRUCTURE_SIZE = assignId();
    public static final int STRUCTURE_FORMED = assignId();
    public static final int VARIANT_RENDER_UPDATE = assignId();
    public static final int IS_TAPED = assignId();
    public static final int STORE_MAINTENANCE = assignId();
    public static final int STORE_TAPED = assignId();
    public static final int RECIPE_MAP_INDEX = assignId();
    public static final int IS_FRONT_FACE_FREE = assignId();
    public static final int MAINTENANCE_MULTIPLIER = assignId();
    public static final int UPDATE_UPWARDS_FACING = assignId();
    public static final int UPDATE_FLIP = assignId();

    // Item Bus Item Stack Auto Collapsing
    public static final int TOGGLE_COLLAPSE_ITEMS = assignId();

    // Fusion Reactor
    public static final int UPDATE_COLOR = assignId();

    // Assembly Line
    public static final int UPDATE_PARTICLE = assignId();

    // Central Monitor
    public static final int UPDATE_ALL = assignId();
    public static final int UPDATE_COVERS = assignId();
    public static final int UPDATE_HEIGHT = assignId();
    public static final int UPDATE_ACTIVE = assignId();
    public static final int UPDATE_PLUGIN_ITEM = assignId();

    // Central Monitor Plugin
    public static final int UPDATE_PLUGIN_DATA = assignId();
    public static final int UPDATE_PLUGIN_CONFIG = assignId();
    public static final int ACTION_PLUGIN_CONFIG = assignId();
    public static final int UPDATE_PLUGIN_CLICK = assignId();
    public static final int UPDATE_ADVANCED_VALID_POS = assignId();
    public static final int UPDATE_FAKE_GUI = assignId();
    public static final int ACTION_FAKE_GUI = assignId();
    public static final int UPDATE_FAKE_GUI_DETECT = assignId();

    // Digital Interface
    public static final int UPDATE_COVER_MODE = assignId();
    public static final int UPDATE_FLUID = assignId();
    public static final int UPDATE_ITEM = assignId();
    public static final int UPDATE_ENERGY = assignId();
    public static final int UPDATE_ENERGY_PER = assignId();
    public static final int UPDATE_MACHINE = assignId();

    // Phantom Tanks
    public static final int REMOVE_PHANTOM_FLUID_TYPE = assignId();
    public static final int CHANGE_PHANTOM_FLUID = assignId();
    public static final int SET_PHANTOM_FLUID = assignId();
    public static final int LOAD_PHANTOM_FLUID_STACK_FROM_NBT = assignId();

    // Recipe Logic
    public static final int WORKABLE_ACTIVE = assignId();
    public static final int WORKING_ENABLED = assignId();

    // Creative Energy
    public static final int UPDATE_IO_SPEED = assignId();

    // Quantum Chest/Tank
    public static final int UPDATE_ITEM_COUNT = assignId();
    public static final int UPDATE_FLUID_AMOUNT = assignId();

    // Quantum Storage Controller
    public static final int UPDATE_CONTROLLER_POS = assignId();
    public static final int REMOVE_CONTROLLER = assignId();
    public static final int LOCATE_CONTROLLER = assignId();

    // Detector Covers
    public static final int UPDATE_INVERTED = assignId();

    // HPCA / Research Station
    public static final int DAMAGE_STATE = assignId();
    public static final int LOCK_OBJECT_HOLDER = assignId();
    public static final int CACHED_CWU = assignId();

    // NBT Keys

    // From MetaTileEntityHolder
    public static final String CUSTOM_NAME = "CustomName";
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    public static final String TAG_KEY_MTE = "MetaTileEntity";

    // From MetaTileEntity
    public static final String TAG_KEY_PAINTING_COLOR = "PaintingColor";
    public static final String TAG_KEY_FRAGILE = "Fragile";
    public static final String TAG_KEY_MUFFLED = "Muffled";

    // MTE Trait Names

    public static final String ABSTRACT_WORKABLE_TRAIT = "RecipeMapWorkable";
    public static final String ENERGY_CONTAINER_TRAIT = "EnergyContainer";
    public static final String ENERGY_CONVERTER_TRAIT = "EnergyConvertTrait";
    public static final String FUSION_REACTOR_ENERGY_CONTAINER_TRAIT = "EnergyContainerInternal";
    public static final String BATTERY_BUFFER_ENERGY_CONTAINER_TRAIT = "BatteryEnergyContainer";

    // Alarm
    public static final int UPDATE_SOUND = assignId();
    public static final int UPDATE_RADIUS = assignId();

    // ME Parts
    public static final int UPDATE_AUTO_PULL = assignId();
    public static final int UPDATE_ONLINE_STATUS = assignId();

    // Everything below MUST be last in the class!
    public static final Int2ObjectMap<String> NAMES = new Int2ObjectArrayMap<>();

    static {
        registerFields(GregtechDataCodes.class);
        // todo these really should be moved to this class
        registerFields(CoverAbstractEnderLink.class, CoverAbstractEnderLink.UPDATE_PRIVATE);
        registerFields(MetaTileEntityFluidHatch.class, MetaTileEntityFluidHatch.LOCK_FILL);
    }

    public static String getNameFor(int id) {
        return NAMES.getOrDefault(id, "Unknown_DataCode:" + id);
    }

    /**
     * Registers all fields from the passed in class to the name registry.
     * Optionally, you can pass in a list of valid ids to check against so that errant ids are not added
     * 
     * @param clazz    Class to iterate fields
     * @param validIds optional array of valid ids to check against class fields
     */
    public static void registerFields(Class<?> clazz, int... validIds) {
        try {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType() != Integer.TYPE) continue;
                if (!Modifier.isStatic(field.getModifiers())) continue;
                if (!Modifier.isFinal(field.getModifiers())) continue;
                int id = field.getInt(null);
                if (!ArrayUtils.isEmpty(validIds) && !ArrayUtils.contains(validIds, id))
                    continue;
                NAMES.put(id, field.getName() + ":" + id);
            }
        } catch (IllegalAccessException ignored) {}
    }
}
