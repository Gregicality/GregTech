package gregtech.common.items;

import gregtech.api.GTValues;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armoritem.ArmorBuilder;
import gregtech.api.items.armoritem.IGTArmor;
import gregtech.api.items.armoritem.ItemGTArmor;
import gregtech.api.items.armoritem.ItemGTElectricArmor;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.common.ConfigHolder;
import gregtech.common.items.armor.AdvancedNanoMuscleSuite;
import gregtech.common.items.armor.AdvancedQuarkTechSuite;
import gregtech.common.items.armor.ArmorSet;
import gregtech.common.items.armor.JetpackStats;
import gregtech.common.items.armor.QuarkTechSuite;
import gregtech.common.items.armor.behavior.ElectricJetpackBehavior;
import gregtech.common.items.armor.behavior.FallDamageCancelBehavior;
import gregtech.common.items.armor.behavior.FueledJetpackBehavior;
import gregtech.common.items.armor.behavior.NightvisionBehavior;
import gregtech.common.items.armor.behavior.StepAssistBehavior;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.client.model.ModelLoader;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ArmorItems {

    private static final List<IGTArmor> ARMORS = new ArrayList<>();

    public static ItemGTElectricArmor NIGHTVISION_GOGGLES;

    public static ItemGTArmor FUELED_JETPACK;
    public static ItemGTElectricArmor ELECTRIC_JETPACK;
    public static ItemGTElectricArmor ADVANCED_ELECTRIC_JETPACK;

    public static ItemGTElectricArmor NANO_HELMET;
    public static ItemGTElectricArmor NANO_CHESTPLATE;
    public static ItemGTElectricArmor NANO_LEGGINGS;
    public static ItemGTElectricArmor NANO_BOOTS;
    public static ItemGTElectricArmor ADVANCED_NANO_CHESTPLATE;

    public static ItemGTElectricArmor QUANTUM_HELMET;
    public static ItemGTElectricArmor QUANTUM_CHESTPLATE;
    public static ItemGTElectricArmor QUANTUM_LEGGINGS;
    public static ItemGTElectricArmor QUANTUM_BOOTS;
    public static ItemGTElectricArmor ADVANCED_QUANTUM_CHESTPLATE;

    private ArmorItems() {/**/}

    public static List<IGTArmor> getAllArmors() {
        return ARMORS;
    }

    // TODO ArmorProperties values not set here (and their original code was already deleted):
    // Electric Jetpack: 0, 0, 0
    // Advanced Electric Jetpack: 0, 0, 0
    // Fueled Jetpack: 0, 0, 0

    // TODO Other information from deleted files:
    // Fueled Jetpack:
    // - Default subitem is filled with Diesel
    // - Durability bar colors: GradientUtil.getGradient(0xB7AF08, 10)

    public static void init() {
        // todo get rid of this config... "stuff"
        NIGHTVISION_GOGGLES = register(ItemGTElectricArmor.Builder
                .of(GTValues.MODID, "nightvision_goggles", EntityEquipmentSlot.HEAD)
                .electric(ConfigHolder.tools.voltageTierNightVision,
                        80_000L * (long) Math.max(1, Math.pow(1, ConfigHolder.tools.voltageTierNightVision - 1)))
                .electricCost(2)
                .behaviors(NightvisionBehavior.INSTANCE));

        FUELED_JETPACK = register(ItemGTArmor.Builder.of(GTValues.MODID, "fueled_jetpack", EntityEquipmentSlot.CHEST)
                .behaviors(new FueledJetpackBehavior(JetpackStats.FUELED, 16000,
                        FueledJetpackBehavior.COMBUSTION_FUEL_BURN_TIME)));

        ELECTRIC_JETPACK = register(ItemGTElectricArmor.Builder
                .of(GTValues.MODID, "electric_jetpack", EntityEquipmentSlot.CHEST)
                .electric(ConfigHolder.tools.voltageTierImpeller,
                        1_000_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.tools.voltageTierImpeller - 2)))
                .behaviors(new ElectricJetpackBehavior(JetpackStats.ELECTRIC, 30))
                .rarity(EnumRarity.UNCOMMON));

        ADVANCED_ELECTRIC_JETPACK = register(ItemGTElectricArmor.Builder
                .of(GTValues.MODID, "advanced_electric_jetpack", EntityEquipmentSlot.CHEST)
                .electric(ConfigHolder.tools.voltageTierAdvImpeller,
                        6_400_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.tools.voltageTierAdvImpeller - 4)))
                .behaviors(new ElectricJetpackBehavior(JetpackStats.ADVANCED, 480))
                .rarity(EnumRarity.RARE));

        int energyPerUse = 512;
        int tier = ConfigHolder.tools.voltageTierNanoSuit;
        long maxCapacity = 6_400_000L * (long) Math.max(1, Math.pow(4, tier - 3));

        NANO_HELMET = register(ItemGTElectricArmor.Builder.of(GTValues.MODID, "nano_helmet", EntityEquipmentSlot.HEAD)
                .electric(tier, maxCapacity)
                .electricCost(energyPerUse)
                .behaviors(NightvisionBehavior.INSTANCE)
                .rarity(EnumRarity.UNCOMMON)
                .armorSet(ArmorSet.NANO));

        NANO_CHESTPLATE = register(
                ItemGTElectricArmor.Builder.of(GTValues.MODID, "nano_chestplate", EntityEquipmentSlot.CHEST)
                        .electric(tier, maxCapacity)
                        .electricCost(energyPerUse)
                        .rarity(EnumRarity.UNCOMMON)
                        .armorSet(ArmorSet.NANO));

        NANO_LEGGINGS = register(
                ItemGTElectricArmor.Builder.of(GTValues.MODID, "nano_leggings", EntityEquipmentSlot.LEGS)
                        .electric(tier, maxCapacity)
                        .electricCost(energyPerUse)
                        .allowBlocking(DamageSource.FALL)
                        .rarity(EnumRarity.UNCOMMON)
                        .armorSet(ArmorSet.NANO));

        NANO_BOOTS = register(ItemGTElectricArmor.Builder.of(GTValues.MODID, "nano_boots", EntityEquipmentSlot.FEET)
                .electric(tier, maxCapacity)
                .electricCost(energyPerUse)
                .behaviors(StepAssistBehavior.INSTANCE, FallDamageCancelBehavior.INSTANCE)
                .allowBlocking(DamageSource.FALL)
                .rarity(EnumRarity.UNCOMMON)
                .armorSet(ArmorSet.NANO));
    }

    public static <T extends IGTArmor, U extends ArmorBuilder<T, U>> T register(@NotNull ArmorBuilder<T, U> builder) {
        T armor = builder.build();
        ARMORS.add(armor);
        return armor;
    }

    public static <T extends IGTArmor> T register(@NotNull T armor) {
        ARMORS.add(armor);
        return armor;
    }

    public static void registerModels() {
        ARMORS.forEach(armor -> ModelLoader.setCustomModelResourceLocation(armor.get(), 0, armor.getModelLocation()));
    }

    public static void registerColors() {
        ARMORS.forEach(armor -> Minecraft.getMinecraft().getItemColors().registerItemColorHandler(armor::getColor,
                armor.get()));
    }

    // TODO Remove everything below here in v...

    @Deprecated
    private static ArmorMetaItem<?>.ArmorMetaValueItem OLD_NIGHTVISION_GOGGLES;
    @Deprecated
    private static ArmorMetaItem<?>.ArmorMetaValueItem OLD_NANO_CHESTPLATE;
    @Deprecated
    private static ArmorMetaItem<?>.ArmorMetaValueItem OLD_NANO_LEGGINGS;
    @Deprecated
    private static ArmorMetaItem<?>.ArmorMetaValueItem OLD_NANO_BOOTS;
    @Deprecated
    private static ArmorMetaItem<?>.ArmorMetaValueItem OLD_NANO_HELMET;
    @Deprecated
    private static ArmorMetaItem<?>.ArmorMetaValueItem OLD_QUANTUM_CHESTPLATE;
    @Deprecated
    private static ArmorMetaItem<?>.ArmorMetaValueItem OLD_QUANTUM_LEGGINGS;
    @Deprecated
    private static ArmorMetaItem<?>.ArmorMetaValueItem OLD_QUANTUM_BOOTS;
    @Deprecated
    private static ArmorMetaItem<?>.ArmorMetaValueItem OLD_QUANTUM_HELMET;
    @Deprecated
    private static ArmorMetaItem<?>.ArmorMetaValueItem OLD_SEMIFLUID_JETPACK;
    @Deprecated
    private static ArmorMetaItem<?>.ArmorMetaValueItem OLD_ELECTRIC_JETPACK;
    @Deprecated
    private static ArmorMetaItem<?>.ArmorMetaValueItem OLD_ELECTRIC_JETPACK_ADVANCED;
    @Deprecated
    private static ArmorMetaItem<?>.ArmorMetaValueItem OLD_NANO_CHESTPLATE_ADVANCED;
    @Deprecated
    private static ArmorMetaItem<?>.ArmorMetaValueItem OLD_QUANTUM_CHESTPLATE_ADVANCED;

    // TODO Remove all old armor texture files, change old models to point to new texture location until full removal.
    // TODO Rename old armor lang keys (other than names), and move names to a separated section for easy removal.

    @Deprecated
    public static void initLegacyArmor() {
        ArmorMetaItem<ArmorMetaItem<?>.ArmorMetaValueItem> legacyArmor = new ArmorMetaItem<>() {

            @Override
            public void registerSubItems() {
                // todo call setInvisible() on all of these
                OLD_NIGHTVISION_GOGGLES = addItem(1, "nightvision_goggles").setArmorLogic(EntityEquipmentSlot.HEAD,
                        "nightvision_goggles");

                OLD_SEMIFLUID_JETPACK = addItem(2, "liquid_fuel_jetpack").setArmorLogic(EntityEquipmentSlot.CHEST,
                        "fueled_jetpack");
                OLD_ELECTRIC_JETPACK = addItem(3, "electric_jetpack").setArmorLogic(EntityEquipmentSlot.CHEST,
                        "electric_jetpack");
                OLD_ELECTRIC_JETPACK_ADVANCED = addItem(4, "advanced_electric_jetpack")
                        .setArmorLogic(EntityEquipmentSlot.CHEST, "advanced_electric_jetpack");

                OLD_NANO_HELMET = addItem(20, "nms.helmet").setArmorLogic(EntityEquipmentSlot.HEAD, "nano_helmet");
                OLD_NANO_CHESTPLATE = addItem(21, "nms.chestplate").setArmorLogic(EntityEquipmentSlot.CHEST,
                        "nano_chestplate");
                OLD_NANO_LEGGINGS = addItem(22, "nms.leggings").setArmorLogic(EntityEquipmentSlot.LEGS,
                        "nano_leggings");
                OLD_NANO_BOOTS = addItem(23, "nms.boots").setArmorLogic(EntityEquipmentSlot.FEET, "nano_boots");
                MetaItems.NANO_CHESTPLATE_ADVANCED = addItem(30, "nms.advanced_chestplate")
                        .setArmorLogic(new AdvancedNanoMuscleSuite(512,
                                12_800_000L *
                                        (long) Math.max(1, Math.pow(4, ConfigHolder.tools.voltageTierAdvNanoSuit - 3)),
                                ConfigHolder.tools.voltageTierAdvNanoSuit))
                        .setRarity(EnumRarity.RARE);

                int energyPerUse = 8192;
                int tier = ConfigHolder.tools.voltageTierQuarkTech;
                long maxCapacity = 100_000_000L * (long) Math.max(1, Math.pow(4, tier - 5));
                MetaItems.QUANTUM_HELMET = addItem(40, "qts.helmet")
                        .setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.HEAD, energyPerUse, maxCapacity, tier))
                        .setRarity(EnumRarity.RARE);
                MetaItems.QUANTUM_CHESTPLATE = addItem(41, "qts.chestplate")
                        .setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.CHEST, energyPerUse, maxCapacity, tier))
                        .setRarity(EnumRarity.RARE);
                MetaItems.QUANTUM_LEGGINGS = addItem(42, "qts.leggings")
                        .setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.LEGS, energyPerUse, maxCapacity, tier))
                        .setRarity(EnumRarity.RARE);
                MetaItems.QUANTUM_BOOTS = addItem(43, "qts.boots")
                        .setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.FEET, energyPerUse, maxCapacity, tier))
                        .setRarity(EnumRarity.RARE);
                MetaItems.QUANTUM_CHESTPLATE_ADVANCED = addItem(50, "qts.advanced_chestplate")
                        .setArmorLogic(new AdvancedQuarkTechSuite(energyPerUse,
                                1_000_000_000L *
                                        (long) Math.max(1, Math.pow(4, ConfigHolder.tools.voltageTierAdvQuarkTech - 6)),
                                ConfigHolder.tools.voltageTierAdvQuarkTech))
                        .setRarity(EnumRarity.EPIC);
            }
        };
        legacyArmor.setRegistryName("gt_armor");
    }

    // TODO Remove in v...
    @Deprecated
    public static void registerLegacyConversionRecipes() {
        // todo
        addConversion(OLD_NIGHTVISION_GOGGLES, NIGHTVISION_GOGGLES.getStack());
        addConversion(OLD_NANO_HELMET, NANO_HELMET.getStack());
        addConversion(OLD_NANO_CHESTPLATE, NANO_CHESTPLATE.getStack());
        addConversion(OLD_NANO_LEGGINGS, NANO_LEGGINGS.getStack());
        addConversion(OLD_NANO_BOOTS, NANO_BOOTS.getStack());
        // addConversion(OLD_QUANTUM_HELMET, QUANTUM_HELMET.getStack());
        // addConversion(OLD_QUANTUM_CHESTPLATE, QUANTUM_CHESTPLATE.getStack());
        // addConversion(OLD_QUANTUM_LEGGINGS, QUANTUM_LEGGINGS.getStack());
        // addConversion(OLD_QUANTUM_BOOTS, QUANTUM_BOOTS.getStack());
        addConversion(OLD_SEMIFLUID_JETPACK, FUELED_JETPACK.getStack());
        addConversion(OLD_ELECTRIC_JETPACK, ELECTRIC_JETPACK.getStack());
        // addConversion(OLD_ELECTRIC_JETPACK_ADVANCED, ADVANCED_ELECTRIC_JETPACK.getStack());
        // addConversion(OLD_NANO_CHESTPLATE_ADVANCED, ADVANCED_NANO_CHESTPLATE.getStack());
        // addConversion(OLD_QUANTUM_CHESTPLATE_ADVANCED, ADVANCED_QUANTUM_CHESTPLATE.getStack());
    }

    @Deprecated
    private static void addConversion(MetaItem<?>.MetaValueItem oldArmor, ItemStack newArmor) {
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputNBT(oldArmor, NBTMatcher.ANY, NBTCondition.ANY)
                .circuitMeta(20)
                .outputs(newArmor)
                .duration(100).EUt(7).buildAndRegister();
    }
}
