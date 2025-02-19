package gregtech;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.modules.ModuleContainerRegistryEvent;
import gregtech.api.persistence.PersistentData;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.modules.GregTechModules;
import gregtech.modules.ModuleManager;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = GTValues.MODID,
     name = GTValues.MOD_NAME,
     acceptedMinecraftVersions = "[1.12.2,1.13)",
     version = GTInternalTags.VERSION,
     dependencies = "required:forge@[14.23.5.2847,);" + "required-after:codechickenlib@[3.2.3,);" +
             "required-after:modularui@[2.5.0-rc,);" + "required-after:mixinbooter@[8.0,);" +
             "after:appliedenergistics2;" +
             "after:forestry;" + "after:extrabees;" + "after:extratrees;" + "after:genetics;" + "after:magicbees;" +
             "after:jei@[4.15.0,);" + "after:crafttweaker@[4.1.20,);" + "after:groovyscript@[1.2.0,);" +
             "after:theoneprobe;" + "after:hwyla;" + "after:exnihilocreatio;")
public class GregTechMod {

    // Hold this so that we can reference non-interface methods without
    // letting the GregTechAPI object see them as immediately.
    private ModuleManager moduleManager;

    public GregTechMod() {
        GregTechAPI.instance = this;
        FluidRegistry.enableUniversalBucket();
        if (FMLCommonHandler.instance().getSide().isClient()) {
            BloomEffectUtil.init();
        }
    }

    @EventHandler
    public void onConstruction(FMLConstructionEvent event) {
        PersistentData.instance().init();
        moduleManager = ModuleManager.getInstance();
        GregTechAPI.moduleManager = moduleManager;
        moduleManager.registerContainer(new GregTechModules());
        MinecraftForge.EVENT_BUS.post(new ModuleContainerRegistryEvent());
        moduleManager.setup(event.getASMHarvestedData(), Loader.instance().getConfigDir());
        moduleManager.onConstruction(event);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        moduleManager.onPreInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        moduleManager.onInit(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        moduleManager.onPostInit(event);
        moduleManager.processIMC(FMLInterModComms.fetchRuntimeMessages(GregTechAPI.instance));
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        moduleManager.onLoadComplete(event);
    }

    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        moduleManager.onServerAboutToStart(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        moduleManager.onServerStarting(event);
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        moduleManager.onServerStarted(event);
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        moduleManager.onServerStopping(event);
    }

    @EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        moduleManager.onServerStopped(event);
    }

    @EventHandler
    public void respondIMC(FMLInterModComms.IMCEvent event) {
        moduleManager.processIMC(event.getMessages());
    }
}
