package gregtech.integration.opencomputers;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.modules.GregTechModule;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.opencomputers.drivers.*;
import gregtech.integration.opencomputers.drivers.specific.DriverConverter;
import gregtech.integration.opencomputers.drivers.specific.DriverFissionReactor;
import gregtech.integration.opencomputers.drivers.specific.DriverFusionReactor;
import gregtech.integration.opencomputers.drivers.specific.DriverPowerSubstation;
import gregtech.integration.opencomputers.drivers.specific.DriverWorldAccelerator;
import gregtech.modules.GregTechModules;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverBlock;

@GregTechModule(
                moduleID = GregTechModules.MODULE_OC,
                containerID = GTValues.MODID,
                modDependencies = Mods.Names.OPEN_COMPUTERS,
                name = "GregTech OpenComputers Integration",
                description = "OpenComputers Integration Module")
public class OpenComputersModule extends IntegrationSubmodule {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        Mods.GTCE2OC.throwIncompatibilityIfLoaded("All functionality from this mod has been implemented here.");
    }

    @Override
    public void init(FMLInitializationEvent event) {
        getLogger().info("Registering OpenComputers Drivers...");
        registerDriver(new DriverEnergyContainer());
        registerDriver(new DriverWorkable());
        registerDriver(new DriverAbstractRecipeLogic());
        registerDriver(new DriverRecipeMapMultiblockController());
        registerDriver(new DriverCoverHolder());
        registerDriver(new DriverSimpleMachine());
        registerDriver(new DriverFusionReactor());
        registerDriver(new DriverWorldAccelerator());
        registerDriver(new DriverConverter());
        registerDriver(new DriverPowerSubstation());
        registerDriver(new DriverFissionReactor());
    }

    public static void registerDriver(DriverBlock driver) {
        if (GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_OC)) {
            Driver.add(driver);
        }
    }
}
