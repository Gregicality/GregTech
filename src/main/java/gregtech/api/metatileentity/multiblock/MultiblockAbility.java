package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.*;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiblockAbility<T> {

    public static final Map<MultiblockAbility<?>, List<MetaTileEntity>> REGISTRY = new Object2ObjectOpenHashMap<>();

    public static final MultiblockAbility<IItemHandlerModifiable> EXPORT_ITEMS = ability(
            "export_items", IItemHandlerModifiable.class);
    public static final MultiblockAbility<IItemHandlerModifiable> IMPORT_ITEMS = ability(
            "import_items", IItemHandlerModifiable.class);

    public static final MultiblockAbility<IFluidTank> EXPORT_FLUIDS = ability(
            "export_fluids", IFluidTank.class);
    public static final MultiblockAbility<IFluidTank> IMPORT_FLUIDS = ability(
            "import_fluids", IFluidTank.class);

    public static final MultiblockAbility<IEnergyContainer> INPUT_ENERGY = ability(
            "input_energy", IEnergyContainer.class);
    public static final MultiblockAbility<IEnergyContainer> OUTPUT_ENERGY = ability(
            "output_energy", IEnergyContainer.class);

    public static final MultiblockAbility<IEnergyContainer> SUBSTATION_INPUT_ENERGY = ability(
            "substation_input_energy", IEnergyContainer.class);
    public static final MultiblockAbility<IEnergyContainer> SUBSTATION_OUTPUT_ENERGY = ability(
            "substation_output_energy", IEnergyContainer.class);

    public static final MultiblockAbility<IRotorHolder> ROTOR_HOLDER = ability(
            "rotor_holder", IRotorHolder.class);

    public static final MultiblockAbility<IFluidTank> PUMP_FLUID_HATCH = ability(
            "pump_fluid_hatch", IFluidTank.class);

    public static final MultiblockAbility<IFluidTank> STEAM = new MultiblockAbility<>(
            "steam", IFluidTank.class);
    public static final MultiblockAbility<IItemHandlerModifiable> STEAM_IMPORT_ITEMS = ability(
            "steam_import_items", IItemHandlerModifiable.class);
    public static final MultiblockAbility<IItemHandlerModifiable> STEAM_EXPORT_ITEMS = ability(
            "steam_export_items", IItemHandlerModifiable.class);

    public static final MultiblockAbility<IMaintenanceHatch> MAINTENANCE_HATCH = ability(
            "maintenance_hatch", IMaintenanceHatch.class);
    public static final MultiblockAbility<IMufflerHatch> MUFFLER_HATCH = ability(
            "muffler_hatch", IMufflerHatch.class);

    public static final MultiblockAbility<IItemHandlerModifiable> MACHINE_HATCH = ability(
            "machine_hatch", IItemHandlerModifiable.class);

    public static final MultiblockAbility<IFluidHandler> TANK_VALVE = ability(
            "tank_valve", IFluidHandler.class);

    public static final MultiblockAbility<IPassthroughHatch> PASSTHROUGH_HATCH = ability(
            "passthrough_hatch", IPassthroughHatch.class);

    public static final MultiblockAbility<IDataAccessHatch> DATA_ACCESS_HATCH = ability(
            "data_access_hatch", IDataAccessHatch.class);
    public static final MultiblockAbility<IOpticalDataAccessHatch> OPTICAL_DATA_RECEPTION = ability(
            "optical_data_reception", IOpticalDataAccessHatch.class);
    public static final MultiblockAbility<IOpticalDataAccessHatch> OPTICAL_DATA_TRANSMISSION = ability(
            "optical_data_transmission", IOpticalDataAccessHatch.class);
    public static final MultiblockAbility<ILaserContainer> INPUT_LASER = ability(
            "input_laser", ILaserContainer.class);
    public static final MultiblockAbility<ILaserContainer> OUTPUT_LASER = ability(
            "output_laser", ILaserContainer.class);

    public static final MultiblockAbility<IOpticalComputationHatch> COMPUTATION_DATA_RECEPTION = ability(
            "computation_data_reception", IOpticalComputationHatch.class);
    public static final MultiblockAbility<IOpticalComputationHatch> COMPUTATION_DATA_TRANSMISSION = ability(
            "computation_data_transmission", IOpticalComputationHatch.class);

    public static final MultiblockAbility<IHPCAComponentHatch> HPCA_COMPONENT = ability(
            "hpca_component", IHPCAComponentHatch.class);
    public static final MultiblockAbility<IObjectHolder> OBJECT_HOLDER = ability(
            "object_holder", IObjectHolder.class);

    public static void registerMultiblockAbility(MultiblockAbility<?> ability, MetaTileEntity part) {
        if (!REGISTRY.containsKey(ability)) {
            REGISTRY.put(ability, new ArrayList<>());
        }
        REGISTRY.get(ability).add(part);
    }

    public static <R> MultiblockAbility<R> ability(String name, Class<R> clazz) {
        return new MultiblockAbility<>(name, clazz);
    }

    private final String name;
    private final Class<T> clazz;

    public MultiblockAbility(String name, Class<T> clazz) {
        this.name = name.toLowerCase();
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean checkType(Object o) {
        return clazz.isAssignableFrom(o.getClass());
    }

    @SuppressWarnings("unchecked")
    public <R> List<R> castList(AbilityInstances instances) {
        if (instances.isKey(this)) {
            return (List<R>) instances;
        }
        return null;
    }
}
