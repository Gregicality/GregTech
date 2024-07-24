package gregtech.integration.forestry;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ItemGTTool;
import gregtech.api.modules.GregTechModule;
import gregtech.api.recipes.machines.RecipeMapScanner;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.Mods;
import gregtech.common.items.ToolItems;
import gregtech.integration.IntegrationModule;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.forestry.bees.*;
import gregtech.integration.forestry.frames.GTFrameType;
import gregtech.integration.forestry.frames.GTItemFrame;
import gregtech.integration.forestry.recipes.*;
import gregtech.integration.forestry.tools.ScoopBehavior;
import gregtech.modules.GregTechModules;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import forestry.api.core.ForestryAPI;
import forestry.core.items.IColoredItem;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

@GregTechModule(
                moduleID = GregTechModules.MODULE_FR,
                containerID = GTValues.MODID,
                modDependencies = Mods.Names.FORESTRY,
                name = "GregTech Forestry Integration",
                description = "Forestry Integration Module")
public class ForestryModule extends IntegrationSubmodule {

    private static MetaItem<?> forestryMetaItem;

    public static GTItemFrame FRAME_ACCELERATED;
    public static GTItemFrame FRAME_MUTAGENIC;
    public static GTItemFrame FRAME_WORKING;
    public static GTItemFrame FRAME_DECAYING;
    public static GTItemFrame FRAME_SLOWING;
    public static GTItemFrame FRAME_STABILIZING;
    public static GTItemFrame FRAME_ARBORIST;

    public static MetaItem<?>.MetaValueItem ELECTRODE_APATITE;
    public static MetaItem<?>.MetaValueItem ELECTRODE_BLAZE;
    public static MetaItem<?>.MetaValueItem ELECTRODE_BRONZE;
    public static MetaItem<?>.MetaValueItem ELECTRODE_COPPER;
    public static MetaItem<?>.MetaValueItem ELECTRODE_DIAMOND;
    public static MetaItem<?>.MetaValueItem ELECTRODE_EMERALD;
    public static MetaItem<?>.MetaValueItem ELECTRODE_ENDER;
    public static MetaItem<?>.MetaValueItem ELECTRODE_GOLD;
    public static MetaItem<?>.MetaValueItem ELECTRODE_IRON;
    public static MetaItem<?>.MetaValueItem ELECTRODE_LAPIS;
    public static MetaItem<?>.MetaValueItem ELECTRODE_OBSIDIAN;
    public static MetaItem<?>.MetaValueItem ELECTRODE_ORCHID;
    public static MetaItem<?>.MetaValueItem ELECTRODE_RUBBER;
    public static MetaItem<?>.MetaValueItem ELECTRODE_TIN;

    public static IGTTool SCOOP;

    public static GTDropItem DROPS;
    public static GTCombItem COMBS;

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(ForestryModule.class);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        forestryMetaItem = new StandardMetaItem();
        forestryMetaItem.setRegistryName("forestry_meta_item");

        // GT Frames
        if (ForestryConfig.enableGTFrames) {
            if (Mods.ForestryApiculture.isModLoaded()) {
                FRAME_ACCELERATED = new GTItemFrame(GTFrameType.ACCELERATED);
                FRAME_MUTAGENIC = new GTItemFrame(GTFrameType.MUTAGENIC);
                FRAME_WORKING = new GTItemFrame(GTFrameType.WORKING);
                FRAME_DECAYING = new GTItemFrame(GTFrameType.DECAYING);
                FRAME_SLOWING = new GTItemFrame(GTFrameType.SLOWING);
                FRAME_STABILIZING = new GTItemFrame(GTFrameType.STABILIZING);
                FRAME_ARBORIST = new GTItemFrame(GTFrameType.ARBORIST);
            } else {
                getLogger()
                        .warn("GregTech Frames are enabled, but Forestry Apiculture module is disabled. Skipping...");
            }
        }

        // GT Scoop
        if (ForestryConfig.enableGTScoop) {
            SCOOP = ToolItems.register(ItemGTTool.Builder.of(GTValues.MODID, "scoop")
                    .toolStats(b -> b
                            .cannotAttack().attackSpeed(-2.4F)
                            .behaviors(ScoopBehavior.INSTANCE))
                    .toolClasses("scoop")
                    .oreDict("toolScoop"));
        }

        // GT Bees
        if (ForestryConfig.enableGTBees) {
            if (Mods.ForestryApiculture.isModLoaded()) {
                DROPS = new GTDropItem();
                COMBS = new GTCombItem();
            } else {
                getLogger().warn("GregTech Bees are enabled, but Forestry Apiculture module is disabled. Skipping...");
            }
        }

        // Remove duplicate/conflicting bees from other Forestry addons.
        // Done in init to have our changes applied before their registration,
        // since we load after other Forestry addons purposefully.
        if (ForestryConfig.disableConflictingBees && Mods.ForestryApiculture.isModLoaded()) {
            BeeRemovals.init();
        }

        // Custom scanner logic for scanning Forestry bees, saplings, etc
        RecipeMapScanner.registerCustomScannerLogic(new ForestryScannerLogic());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        // Yes, this recipe stuff has to be done in init. Because Forestry refuses to move their recipes
        // to the event, causing removals to need to be done in init instead of registry event.
        // See https://github.com/ForestryMC/ForestryMC/issues/2599
        if (ForestryConfig.enableGTElectronTubes) {
            ForestryElectrodeRecipes.onInit();
        }

        if (Mods.ForestryApiculture.isModLoaded()) {
            if (ForestryConfig.harderForestryRecipes) {
                ForestryMiscRecipes.initRemoval();
            }

            if (ForestryConfig.enableGTBees) {
                GTAlleleBeeSpecies.setupAlleles();
                GTBeeDefinition.initBees();
            }
        }

        if (Mods.ExtraBees.isModLoaded()) {
            registerAlvearyMutators();
        }

        if (event.getSide() == Side.CLIENT) {
            if (Mods.ForestryApiculture.isModLoaded()) {
                if (ForestryConfig.enableGTBees) {
                    Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack, tintIndex) -> {
                        if (stack.getItem() instanceof IColoredItem coloredItem) {
                            return coloredItem.getColorFromItemstack(stack, tintIndex);
                        }
                        return 0xFFFFFF;
                    }, DROPS, COMBS);
                }
            }
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        if (Mods.ForestryApiculture.isModLoaded()) {
            getLogger().info("Copying Forestry Centrifuge recipes to GT Centrifuge");
            CombRecipes.initForestryCombs();
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        // GT Frames
        if (Mods.ForestryApiculture.isModLoaded()) {
            if (ForestryConfig.enableGTFrames) {
                registry.register(FRAME_ACCELERATED);
                registry.register(FRAME_MUTAGENIC);
                registry.register(FRAME_WORKING);
                registry.register(FRAME_DECAYING);
                registry.register(FRAME_SLOWING);
                registry.register(FRAME_STABILIZING);
                registry.register(FRAME_ARBORIST);
            }
        }

        // GT Electrodes
        if (ForestryConfig.enableGTElectronTubes) {
            ELECTRODE_APATITE = forestryMetaItem.addItem(1, "electrode.apatite");
            ELECTRODE_BLAZE = forestryMetaItem.addItem(2, "electrode.blaze");
            ELECTRODE_BRONZE = forestryMetaItem.addItem(3, "electrode.bronze");
            ELECTRODE_COPPER = forestryMetaItem.addItem(4, "electrode.copper");
            ELECTRODE_DIAMOND = forestryMetaItem.addItem(5, "electrode.diamond");
            ELECTRODE_EMERALD = forestryMetaItem.addItem(6, "electrode.emerald");
            ELECTRODE_ENDER = forestryMetaItem.addItem(7, "electrode.ender");
            ELECTRODE_GOLD = forestryMetaItem.addItem(8, "electrode.gold");
            ELECTRODE_LAPIS = forestryMetaItem.addItem(9, "electrode.lapis");
            ELECTRODE_OBSIDIAN = forestryMetaItem.addItem(10, "electrode.obsidian");
            ELECTRODE_TIN = forestryMetaItem.addItem(11, "electrode.tin");

            if (Mods.IndustrialCraft2.isModLoaded() || Mods.BinnieCore.isModLoaded()) {
                ELECTRODE_IRON = forestryMetaItem.addItem(12, "electrode.iron");
            }
            if (Mods.ExtraUtilities2.isModLoaded()) {
                ELECTRODE_ORCHID = forestryMetaItem.addItem(13, "electrode.orchid");
            }
            if (Mods.IndustrialCraft2.isModLoaded() || Mods.TechReborn.isModLoaded() || Mods.BinnieCore.isModLoaded()) {
                ELECTRODE_RUBBER = forestryMetaItem.addItem(14, "electrode.rubber");
            }
        }

        // GT Drops
        if (Mods.ForestryApiculture.isModLoaded()) {
            if (ForestryConfig.enableGTBees) {
                registry.register(DROPS);
                registry.register(COMBS);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        if (Mods.ForestryApiculture.isModLoaded()) {
            if (ForestryConfig.enableGTFrames) {
                FRAME_ACCELERATED.registerModel(FRAME_ACCELERATED, ForestryAPI.modelManager);
                FRAME_MUTAGENIC.registerModel(FRAME_MUTAGENIC, ForestryAPI.modelManager);
                FRAME_WORKING.registerModel(FRAME_WORKING, ForestryAPI.modelManager);
                FRAME_DECAYING.registerModel(FRAME_DECAYING, ForestryAPI.modelManager);
                FRAME_SLOWING.registerModel(FRAME_SLOWING, ForestryAPI.modelManager);
                FRAME_STABILIZING.registerModel(FRAME_STABILIZING, ForestryAPI.modelManager);
                FRAME_ARBORIST.registerModel(FRAME_ARBORIST, ForestryAPI.modelManager);
            }
            if (ForestryConfig.enableGTBees) {
                DROPS.registerModel(DROPS, ForestryAPI.modelManager);
                COMBS.registerModel(COMBS, ForestryAPI.modelManager);
            }
        }
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        if (Mods.ForestryApiculture.isModLoaded()) {
            // GT Frames
            if (ForestryConfig.enableGTFrames) {
                ForestryFrameRecipes.init();
            }

            // GT Combs
            if (ForestryConfig.enableGTBees) {
                CombRecipes.initGTCombs();
            }
        }

        // GT Electrodes
        if (ForestryConfig.enableGTElectronTubes) {
            ForestryElectrodeRecipes.onRecipeEvent();
        }

        // GT Scoop
        if (ForestryConfig.enableGTScoop) {
            ForestryToolRecipes.registerHandlers();
        }

        // Random other recipes
        ForestryMiscRecipes.init();
        ForestryExtractorRecipes.init();
    }

    @SubscribeEvent
    public static void registerMaterials(MaterialEvent event) {
        if (Mods.ForestryApiculture.isModLoaded()) {
            if (ForestryConfig.enableGTFrames) {
                Materials.TreatedWood.addFlags(MaterialFlags.GENERATE_LONG_ROD);
                Materials.Uranium235.addFlags(MaterialFlags.GENERATE_LONG_ROD);
                Materials.Plutonium241.addFlags(MaterialFlags.GENERATE_LONG_ROD, MaterialFlags.GENERATE_FOIL);
                Materials.BlueSteel.addFlags(MaterialFlags.GENERATE_LONG_ROD);
            }
            if (ForestryConfig.enableGTElectronTubes) {
                Materials.Copper.addFlags(MaterialFlags.GENERATE_BOLT_SCREW);
                Materials.Emerald.addFlags(MaterialFlags.GENERATE_BOLT_SCREW);
                Materials.Lapis.addFlags(MaterialFlags.GENERATE_BOLT_SCREW);
            }
            if (ForestryConfig.enableGTBees) {
                // Blocks for Bee Breeding
                Materials.Arsenic.addFlags(MaterialFlags.FORCE_GENERATE_BLOCK);
                Materials.Lithium.addFlags(MaterialFlags.FORCE_GENERATE_BLOCK);
                Materials.Electrotine.addFlags(MaterialFlags.FORCE_GENERATE_BLOCK);
                Materials.Lutetium.addFlags(MaterialFlags.FORCE_GENERATE_BLOCK);
                Materials.TricalciumPhosphate.addFlags(MaterialFlags.FORCE_GENERATE_BLOCK);

                // Ores for Comb Processing, does not generate Ore Blocks
                createOreProperty(Materials.Chrome, Materials.Iron, Materials.Magnesium);
                createOreProperty(Materials.Manganese, Materials.Chrome, Materials.Iron);
                createOreProperty(Materials.Magnesium, Materials.Olivine);
                createOreProperty(Materials.Silicon, Materials.SiliconDioxide);
                createOreProperty(Materials.Tungsten, Materials.Manganese, Materials.Molybdenum);
                createOreProperty(Materials.Titanium, Materials.Almandine);
                createOreProperty(Materials.Osmium, Materials.Iridium);
                createOreProperty(Materials.Iridium, Materials.Platinum, Materials.Osmium);
                createOreProperty(Materials.Electrum, Materials.Gold, Materials.Silver);
                createOreProperty(Materials.Uranium, Materials.Lead, Materials.Uranium235, Materials.Thorium);
                createOreProperty(Materials.NaquadahEnriched, Materials.Naquadah, Materials.Naquadria);
                createOreProperty(Materials.Uranium235);
                createOreProperty(Materials.Neutronium);
                createOreProperty(Materials.Gallium);
                createOreProperty(Materials.Niobium);
                createOreProperty(Materials.Rutile);
                createOreProperty(Materials.Naquadria);
                createOreProperty(Materials.Lutetium);
                createOreProperty(Materials.Americium);
                createOreProperty(Materials.NetherStar);
                createOreProperty(Materials.Trinium);
            }
        }
    }

    private static void createOreProperty(Material material, Material... byproducts) {
        if (material.hasProperty(PropertyKey.ORE)) {
            IntegrationModule.logger.debug("Material {} already has an ore property, skipping...", material);
            return;
        }

        OreProperty property = new OreProperty();
        if (byproducts != null && byproducts.length != 0) {
            property.setOreByProducts(byproducts);
        }
        material.setProperty(PropertyKey.ORE, property);
        material.addFlags(MaterialFlags.DISABLE_ORE_BLOCK);
    }

    private static void registerAlvearyMutators() {
        try {
            Class<?> mutationHandler = Class.forName("binnie.extrabees.utils.AlvearyMutationHandler");
            Method method = mutationHandler.getDeclaredMethod("addMutationItem", ItemStack.class, float.class);

            registerAlvearyMutator(method, Materials.Uranium238, 2.0f);
            registerAlvearyMutator(method, Materials.Uranium235, 4.0f);
            registerAlvearyMutator(method, Materials.Plutonium241, 6.0f);
            registerAlvearyMutator(method, Materials.Plutonium239, 8.0f);
            registerAlvearyMutator(method, Materials.NaquadahEnriched, 10.0f);
            registerAlvearyMutator(method, Materials.Naquadria, 15.0f);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
            IntegrationModule.logger.error("Could not register GT Alveary mutators!");
        }
    }

    private static void registerAlvearyMutator(Method method, Material material, float chance) {
        try {
            ItemStack stack = OreDictUnifier.get(OrePrefix.dust, material);
            if (stack != ItemStack.EMPTY) {
                method.invoke(null, stack, chance);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            IntegrationModule.logger.error("Could not register GT Alveary mutators!");
        }
    }
}
