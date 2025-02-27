package gregtech.integration.exnihilo;

import gregtech.api.GTValues;
import gregtech.api.gui.resources.SteamTexture;
import gregtech.api.modules.GregTechModule;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMapBuilder;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.FileUtility;
import gregtech.api.util.Mods;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.IntegrationModule;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.exnihilo.items.ExNihiloPebble;
import gregtech.integration.exnihilo.metatileentities.MetaTileEntitySieve;
import gregtech.integration.exnihilo.metatileentities.MetaTileEntitySteamSieve;
import gregtech.integration.exnihilo.recipes.ExNihiloRecipes;
import gregtech.integration.exnihilo.recipes.SieveDrops;
import gregtech.integration.exnihilo.recipes.ui.SieveUI;
import gregtech.modules.GregTechModules;

import net.minecraft.init.SoundEvents;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static gregtech.api.unification.ore.OrePrefix.Conditions.hasOreProperty;
import static gregtech.api.unification.ore.OrePrefix.Flags.ENABLE_UNIFICATION;
import static gregtech.common.metatileentities.MetaTileEntities.*;

@GregTechModule(
                moduleID = GregTechModules.MODULE_EN,
                containerID = GTValues.MODID,
                modDependencies = Mods.Names.EX_NIHILO_CREATIO,
                name = "GregTech Ex Nihilo Creatio Integration",
                description = "Ex Nihilo Integration Module")
public class ExNihiloModule extends IntegrationSubmodule {

    // Items
    public static ExNihiloPebble GTPebbles;

    // Recipe maps
    public static final RecipeMap<SimpleRecipeBuilder> SIEVE_RECIPES = new RecipeMapBuilder<>("electric_sieve",
            new SimpleRecipeBuilder().duration(100).EUt(4))
                    .itemInputs(2)
                    .itemOutputs(36)
                    .ui(SieveUI::new)
                    .sound(SoundEvents.BLOCK_SAND_PLACE)
                    .build();

    // Machines
    public static MetaTileEntitySteamSieve STEAM_SIEVE_BRONZE;
    public static MetaTileEntitySteamSieve STEAM_SIEVE_STEEL;
    public static MetaTileEntitySieve[] SIEVES = new MetaTileEntitySieve[GTValues.V.length - 1];

    // Textures
    public static final SteamTexture PROGRESS_BAR_SIFTER_STEAM = SteamTexture
            .fullImage("textures/gui/progress_bar/progress_bar_sift_%s.png");

    // Ore prefixes
    public static OrePrefix oreChunk;
    public static OrePrefix oreEnderChunk;
    public static OrePrefix oreNetherChunk;

    // Icon Types
    public static MaterialIconType oreChunkIcon;
    public static MaterialIconType oreEnderChunkIcon;
    public static MaterialIconType oreNetherChunkIcon;

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(ExNihiloModule.class);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        getLogger().info("Registering Ex Nihilo Compat Items, Blocks, and Machines");
        GTPebbles = new ExNihiloPebble();
        registerMetaTileEntities();
        FileUtility.extractJarFiles(String.format("/assets/%s/%s/%s", GTValues.MODID, "integration", "exnihilo"),
                new File(Loader.instance().getConfigDir(), "gregtech"), false);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        SieveDrops.registerRecipes();
        ExNihiloRecipes.registerExNihiloRecipes();
    }

    @SubscribeEvent()
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        IntegrationModule.logger.info("Registering Ex Nihilo Compat Recipes");
        ExNihiloRecipes.registerHandlers();
        ExNihiloRecipes.registerGTRecipes();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerMaterials(MaterialEvent event) {
        oreChunkIcon = new MaterialIconType("oreChunk");
        oreEnderChunkIcon = new MaterialIconType("oreEnderChunk");
        oreNetherChunkIcon = new MaterialIconType("oreNetherChunk");

        oreChunk = new OrePrefix("oreChunk", -1, null, oreChunkIcon, ENABLE_UNIFICATION, hasOreProperty);
        oreEnderChunk = new OrePrefix("oreEnderChunk", -1, null, oreEnderChunkIcon, ENABLE_UNIFICATION, hasOreProperty);
        oreNetherChunk = new OrePrefix("oreNetherChunk", -1, null, oreNetherChunkIcon, ENABLE_UNIFICATION,
                hasOreProperty);

        oreChunk.setAlternativeOreName(OrePrefix.ore.name());
        oreEnderChunk.setAlternativeOreName(OrePrefix.oreEndstone.name());
        oreNetherChunk.setAlternativeOreName(OrePrefix.oreNetherrack.name());

        MetaItems.addOrePrefix(oreChunk, oreEnderChunk, oreNetherChunk);
    }

    private void registerMetaTileEntities() {
        STEAM_SIEVE_BRONZE = MetaTileEntities.registerMetaTileEntity(4000,
                new MetaTileEntitySteamSieve(new ResourceLocation(GTValues.MODID, "sieve.steam"), false));
        STEAM_SIEVE_STEEL = MetaTileEntities.registerMetaTileEntity(4001,
                new MetaTileEntitySteamSieve(new ResourceLocation(GTValues.MODID, "steam_sieve_steel"), true));

        SIEVES[0] = MetaTileEntities.registerMetaTileEntity(4002,
                new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.lv"), 1));
        SIEVES[1] = MetaTileEntities.registerMetaTileEntity(4003,
                new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.mv"), 2));
        SIEVES[2] = MetaTileEntities.registerMetaTileEntity(4004,
                new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.hv"), 3));
        SIEVES[3] = MetaTileEntities.registerMetaTileEntity(4005,
                new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.ev"), 4));
        SIEVES[4] = MetaTileEntities.registerMetaTileEntity(4006,
                new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.iv"), 5));
        if (getMidTier("sieve")) {
            SIEVES[5] = MetaTileEntities.registerMetaTileEntity(4007,
                    new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.luv"), 6));
            SIEVES[6] = MetaTileEntities.registerMetaTileEntity(4008,
                    new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.zpm"), 7));
            SIEVES[7] = MetaTileEntities.registerMetaTileEntity(4009,
                    new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.uv"), 8));
        }
        if (getHighTier("sieve")) {
            SIEVES[8] = MetaTileEntities.registerMetaTileEntity(4010,
                    new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.uhv"), 9));
            SIEVES[9] = MetaTileEntities.registerMetaTileEntity(4011,
                    new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.uev"), 10));
            SIEVES[10] = MetaTileEntities.registerMetaTileEntity(4012,
                    new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.uiv"), 11));
            SIEVES[11] = MetaTileEntities.registerMetaTileEntity(4013,
                    new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.uxv"), 12));
            SIEVES[12] = MetaTileEntities.registerMetaTileEntity(4014,
                    new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.opv"), 13));
        }
    }
}
