package gregtech.core;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.GregTechAPIInternal;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.SimpleCapabilityManager;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverUIFactory;
import gregtech.api.fluids.GTFluidRegistration;
import gregtech.api.gui.UIFactory;
import gregtech.api.items.gui.PlayerInventoryUIFactory;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.metatileentity.registry.MTEManager;
import gregtech.api.metatileentity.registry.MTERegistry;
import gregtech.api.modules.GregTechModule;
import gregtech.api.modules.IGregTechModule;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.GTGuis;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.properties.impl.TemperatureProperty;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.MaterialRegistryEvent;
import gregtech.api.unification.material.event.PostMaterialEvent;
import gregtech.api.unification.material.registry.MarkerMaterialRegistry;
import gregtech.api.util.CapesRegistry;
import gregtech.api.util.Mods;
import gregtech.api.util.input.KeyBind;
import gregtech.api.util.oreglob.OreGlob;
import gregtech.api.util.virtualregistry.VirtualEnderRegistry;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinSaveData;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.common.CommonProxy;
import gregtech.common.ConfigHolder;
import gregtech.common.MetaEntities;
import gregtech.common.blocks.BlockBatteryPart;
import gregtech.common.blocks.BlockCleanroomCasing;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.command.CommandHand;
import gregtech.common.command.CommandRecipeCheck;
import gregtech.common.command.CommandShaders;
import gregtech.common.command.worldgen.CommandWorldgen;
import gregtech.common.covers.CoverBehaviors;
import gregtech.common.covers.filter.oreglob.impl.OreGlobParser;
import gregtech.common.items.ArmorItems;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.worldgen.LootTableHelper;
import gregtech.core.advancement.AdvancementTriggers;
import gregtech.core.advancement.internal.AdvancementManager;
import gregtech.core.command.internal.CommandManager;
import gregtech.core.network.internal.NetworkHandler;
import gregtech.core.network.packets.PacketBlockParticle;
import gregtech.core.network.packets.PacketClipboard;
import gregtech.core.network.packets.PacketClipboardNBTUpdate;
import gregtech.core.network.packets.PacketClipboardUIWidgetUpdate;
import gregtech.core.network.packets.PacketFluidVeinList;
import gregtech.core.network.packets.PacketKeyPressed;
import gregtech.core.network.packets.PacketKeysDown;
import gregtech.core.network.packets.PacketNotifyCapeChange;
import gregtech.core.network.packets.PacketPluginSynced;
import gregtech.core.network.packets.PacketRecoverMTE;
import gregtech.core.network.packets.PacketReloadShaders;
import gregtech.core.network.packets.PacketUIClientAction;
import gregtech.core.network.packets.PacketUIOpen;
import gregtech.core.network.packets.PacketUIWidgetUpdate;
import gregtech.core.sound.GTSoundEvents;
import gregtech.core.sound.internal.SoundManager;
import gregtech.core.unification.material.internal.MaterialRegistryManager;
import gregtech.datafix.command.CommandDataFix;
import gregtech.integration.bq.BQuDataFixer;
import gregtech.loaders.dungeon.DungeonLootLoader;
import gregtech.modules.GregTechModules;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoaderException;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.relauncher.Side;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static gregtech.api.GregTechAPI.*;

@GregTechModule(
                moduleID = GregTechModules.MODULE_CORE,
                containerID = GTValues.MODID,
                name = "GregTech Core",
                description = "Core GregTech content. Disabling this disables the entire mod and all its addons.",
                coreModule = true)
public class CoreModule implements IGregTechModule {

    public static final Logger logger = LogManager.getLogger("GregTech Core");

    @SidedProxy(modId = GTValues.MODID,
                clientSide = "gregtech.client.ClientProxy",
                serverSide = "gregtech.common.CommonProxy")
    public static CommonProxy proxy;

    public CoreModule() {
        GregTechAPI.networkHandler = NetworkHandler.getInstance();
        // must be set here because of GroovyScript compat
        // trying to read this before the pre-init stage
        GregTechAPI.materialManager = MaterialRegistryManager.getInstance();

        OreGlob.setCompiler((expr, ignoreCase) -> new OreGlobParser(expr, ignoreCase).compile());
    }

    @NotNull
    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        GregTechAPIInternal.preInit();
        GregTechAPI.advancementManager = AdvancementManager.getInstance();
        AdvancementTriggers.register();

        GregTechAPI.soundManager = SoundManager.getInstance();
        GTSoundEvents.register();

        /* MUI Initialization */
        GTGuis.registerFactories();
        GTGuiTextures.init();
        GTGuiTheme.registerThemes();

        /* Start UI Factory Registration */
        UI_FACTORY_REGISTRY.unfreeze();
        logger.info("Registering GTCEu UI Factories");
        MetaTileEntityUIFactory.INSTANCE.init();
        PlayerInventoryUIFactory.INSTANCE.init();
        CoverUIFactory.INSTANCE.init();
        logger.info("Registering addon UI Factories");
        MinecraftForge.EVENT_BUS.post(new GregTechAPI.RegisterEvent<>(UI_FACTORY_REGISTRY, UIFactory.class));
        UI_FACTORY_REGISTRY.freeze();
        /* End UI Factory Registration */

        SimpleCapabilityManager.init();

        /* Start Material Registration */

        GregTechAPI.markerMaterialRegistry = MarkerMaterialRegistry.getInstance();

        // First, register other mods' Registries
        MaterialRegistryManager managerInternal = (MaterialRegistryManager) GregTechAPI.materialManager;

        logger.info("Registering material registries");
        MinecraftForge.EVENT_BUS.post(new MaterialRegistryEvent());

        // First, register CEu Materials
        managerInternal.unfreezeRegistries();
        MaterialEvent materialEvent = new MaterialEvent();
        logger.info("Registering GTCEu Materials");
        Materials.register();
        MaterialRegistryManager.getInstance()
                .getRegistry(GTValues.MODID)
                .setFallbackMaterial(Materials.Aluminium);

        // Then, register addon Materials
        logger.info("Registering addon Materials");
        MinecraftForge.EVENT_BUS.post(materialEvent);

        // Fire Post-Material event, intended for when Materials need to be iterated over in-full before freezing
        // Block entirely new Materials from being added in the Post event
        managerInternal.closeRegistries();
        MinecraftForge.EVENT_BUS.post(new PostMaterialEvent());

        // Freeze Material Registry before processing Items, Blocks, and Fluids
        managerInternal.freezeRegistries();
        /* End Material Registration */

        // need to do this before MetaBlocks runs, to make sure all addons get their own BlockMachine
        /* Start MTE Registry Addition */
        GregTechAPI.mteManager = MTEManager.getInstance();
        MinecraftForge.EVENT_BUS.post(new MTEManager.MTERegistryEvent());
        /* End MTE Registry Addition */

        OreDictUnifier.init();

        MetaBlocks.init();
        MetaItems.init();
        ToolItems.init();
        ArmorItems.init();
        ArmorItems.initLegacyArmor();
        GTFluidRegistration.INSTANCE.register();

        /* Start CEu MetaTileEntity Registration */
        for (MTERegistry registry : mteManager.getRegistries()) {
            registry.unfreeze();
        }
        logger.info("Registering GTCEu Meta Tile Entities");
        MetaTileEntities.init();
        /* End CEu MetaTileEntity Registration */
        /* Addons not done via an Event due to how much must be initialized for MTEs to register */

        MetaEntities.init();

        /* Start API Block Registration */
        for (BlockWireCoil.CoilType type : BlockWireCoil.CoilType.values()) {
            HEATING_COILS.put(MetaBlocks.WIRE_COIL.getState(type), type);
        }
        for (BlockBatteryPart.BatteryPartType type : BlockBatteryPart.BatteryPartType.values()) {
            PSS_BATTERIES.put(MetaBlocks.BATTERY_BLOCK.getState(type), type);
        }
        for (BlockCleanroomCasing.CasingType type : BlockCleanroomCasing.CasingType.values()) {
            CLEANROOM_FILTERS.put(MetaBlocks.CLEANROOM_CASING.getState(type), type);
        }
        /* End API Block Registration */

        proxy.onPreLoad();
        KeyBind.init();
    }

    @Override
    public void registerPackets() {
        GregTechAPI.networkHandler.registerPacket(PacketUIOpen.class);
        GregTechAPI.networkHandler.registerPacket(PacketUIWidgetUpdate.class);
        GregTechAPI.networkHandler.registerPacket(PacketUIClientAction.class);
        GregTechAPI.networkHandler.registerPacket(PacketBlockParticle.class);
        GregTechAPI.networkHandler.registerPacket(PacketClipboard.class);
        GregTechAPI.networkHandler.registerPacket(PacketClipboardUIWidgetUpdate.class);
        GregTechAPI.networkHandler.registerPacket(PacketPluginSynced.class);
        GregTechAPI.networkHandler.registerPacket(PacketRecoverMTE.class);
        GregTechAPI.networkHandler.registerPacket(PacketKeysDown.class);
        GregTechAPI.networkHandler.registerPacket(PacketKeyPressed.class);
        GregTechAPI.networkHandler.registerPacket(PacketFluidVeinList.class);
        GregTechAPI.networkHandler.registerPacket(PacketNotifyCapeChange.class);
        GregTechAPI.networkHandler.registerPacket(PacketReloadShaders.class);
        GregTechAPI.networkHandler.registerPacket(PacketClipboardNBTUpdate.class);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        // freeze once addon preInit is finished
        for (MTERegistry registry : mteManager.getRegistries()) {
            registry.freeze();
        }
        proxy.onLoad();
        if (RecipeMap.isFoundInvalidRecipe()) {
            logger.fatal("Seems like invalid recipe was found.");
            // crash if config setting is set to false, or we are in deobfuscated environment
            if (!ConfigHolder.misc.ignoreErrorOrInvalidRecipes || !FMLForgePlugin.RUNTIME_DEOBF) {
                logger.fatal(
                        "Loading cannot continue. Either fix or report invalid recipes, or enable ignoreErrorOrInvalidRecipes in the config as a temporary solution");
                throw new LoaderException(
                        "Found at least one invalid recipe. Please read the log above for more details.");
            } else {
                logger.fatal("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                logger.fatal("Ignoring invalid recipes and continuing loading");
                logger.fatal("Some things may lack recipes or have invalid ones, proceed at your own risk");
                logger.fatal("Report to GTCEu GitHub to get more help and fix the problem");
                logger.fatal("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            }
        }

        WorldGenRegistry.INSTANCE.initializeRegistry();

        LootTableHelper.initialize();

        /* Start Cover Definition Registration */
        COVER_REGISTRY.unfreeze();
        CoverBehaviors.init();
        MinecraftForge.EVENT_BUS.post(new RegisterEvent<>(COVER_REGISTRY, CoverDefinition.class));
        COVER_REGISTRY.freeze();
        /* End Cover Definition Registration */

        DungeonLootLoader.init();
        MetaBlocks.registerWalkingSpeedBonus();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        proxy.onPostLoad();
        BedrockFluidVeinHandler.recalculateChances(true);
        // registers coil types for the BlastTemperatureProperty used in Blast Furnace Recipes
        // runs AFTER craftTweaker
        for (Map.Entry<IBlockState, IHeatingCoilBlockStats> entry : GregTechAPI.HEATING_COILS.entrySet()) {
            IHeatingCoilBlockStats value = entry.getValue();
            if (value != null) {
                String name = entry.getKey().getBlock().getTranslationKey();
                if (!name.endsWith(".name")) name = String.format("%s.name", name);
                TemperatureProperty.registerCoilType(value.getCoilTemperature(), value.getMaterial(), name);
            }
        }

        ModHandler.postInit();
    }

    @Override
    public void loadComplete(FMLLoadCompleteEvent event) {
        proxy.onLoadComplete();
    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        CommandManager commandManager = CommandManager.getInstance();
        GregTechAPI.commandManager = commandManager;
        commandManager.registerServerCommand(event);

        GregTechAPI.commandManager.addCommand(new CommandWorldgen());
        GregTechAPI.commandManager.addCommand(new CommandHand());
        GregTechAPI.commandManager.addCommand(new CommandRecipeCheck());
        GregTechAPI.commandManager.addCommand(new CommandShaders());
        GregTechAPI.commandManager.addCommand(new CommandDataFix());
        CapesRegistry.load();

        if (Mods.BetterQuestingUnofficial.isModLoaded()) {
            BQuDataFixer.onServerStarting(event.getServer());
        }
    }

    @Override
    public void serverStarted(FMLServerStartedEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
            if (!world.isRemote) {
                BedrockFluidVeinSaveData saveData = (BedrockFluidVeinSaveData) world
                        .loadData(BedrockFluidVeinSaveData.class, BedrockFluidVeinSaveData.dataName);
                if (saveData == null) {
                    saveData = new BedrockFluidVeinSaveData(BedrockFluidVeinSaveData.dataName);
                    world.setData(BedrockFluidVeinSaveData.dataName, saveData);
                    // the save data does not yet exist, use the latest version number
                    BedrockFluidVeinHandler.saveDataVersion = BedrockFluidVeinHandler.MAX_FLUID_SAVE_DATA_VERSION;
                }
                BedrockFluidVeinSaveData.setInstance(saveData);
            }
        }
    }

    @Override
    public void serverStopped(FMLServerStoppedEvent event) {
        VirtualEnderRegistry.clearMaps();
        CapesRegistry.clearMaps();
    }
}
