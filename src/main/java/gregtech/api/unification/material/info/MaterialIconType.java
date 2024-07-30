package gregtech.api.unification.material.info;

import gregtech.api.gui.resources.ResourceHelper;
import gregtech.api.util.GTUtility;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MaterialIconType {

    public static final Map<String, MaterialIconType> ICON_TYPES = new HashMap<>();

    static int idCounter = 0;

    public static final MaterialIconType dustTiny = new MaterialIconType("dustTiny");
    public static final MaterialIconType dustSmall = new MaterialIconType("dustSmall");
    public static final MaterialIconType dust = new MaterialIconType("dust");
    public static final MaterialIconType dustImpure = new MaterialIconType("dustImpure");
    public static final MaterialIconType dustPure = new MaterialIconType("dustPure");

    public static final MaterialIconType crushed = new MaterialIconType("crushed");
    public static final MaterialIconType crushedPurified = new MaterialIconType("crushedPurified");
    public static final MaterialIconType crushedCentrifuged = new MaterialIconType("crushedCentrifuged");

    public static final MaterialIconType gem = new MaterialIconType("gem");
    public static final MaterialIconType gemChipped = new MaterialIconType("gemChipped");
    public static final MaterialIconType gemFlawed = new MaterialIconType("gemFlawed");
    public static final MaterialIconType gemFlawless = new MaterialIconType("gemFlawless");
    public static final MaterialIconType gemExquisite = new MaterialIconType("gemExquisite");

    public static final MaterialIconType nugget = new MaterialIconType("nugget");

    public static final MaterialIconType ingot = new MaterialIconType("ingot");
    public static final MaterialIconType ingotHot = new MaterialIconType("ingotHot");
    public static final MaterialIconType ingotDouble = new MaterialIconType("ingotDouble");
    public static final MaterialIconType ingotTriple = new MaterialIconType("ingotTriple");
    public static final MaterialIconType ingotQuadruple = new MaterialIconType("ingotQuadruple");
    public static final MaterialIconType ingotQuintuple = new MaterialIconType("ingotQuintuple");

    public static final MaterialIconType plate = new MaterialIconType("plate");
    public static final MaterialIconType plateDouble = new MaterialIconType("plateDouble");
    public static final MaterialIconType plateTriple = new MaterialIconType("plateTriple");
    public static final MaterialIconType plateQuadruple = new MaterialIconType("plateQuadruple");
    public static final MaterialIconType plateQuintuple = new MaterialIconType("plateQuintuple");
    public static final MaterialIconType plateDense = new MaterialIconType("plateDense");

    public static final MaterialIconType stick = new MaterialIconType("stick");
    public static final MaterialIconType lens = new MaterialIconType("lens");
    public static final MaterialIconType round = new MaterialIconType("round");
    public static final MaterialIconType bolt = new MaterialIconType("bolt");
    public static final MaterialIconType screw = new MaterialIconType("screw");
    public static final MaterialIconType ring = new MaterialIconType("ring");
    public static final MaterialIconType wireFine = new MaterialIconType("wireFine");
    public static final MaterialIconType gearSmall = new MaterialIconType("gearSmall");
    public static final MaterialIconType rotor = new MaterialIconType("rotor");
    public static final MaterialIconType stickLong = new MaterialIconType("stickLong");
    public static final MaterialIconType springSmall = new MaterialIconType("springSmall");
    public static final MaterialIconType spring = new MaterialIconType("spring");
    public static final MaterialIconType gear = new MaterialIconType("gear");
    public static final MaterialIconType foil = new MaterialIconType("foil");

    public static final MaterialIconType toolHeadSword = new MaterialIconType("toolHeadSword");
    public static final MaterialIconType toolHeadPickaxe = new MaterialIconType("toolHeadPickaxe");
    public static final MaterialIconType toolHeadShovel = new MaterialIconType("toolHeadShovel");
    public static final MaterialIconType toolHeadAxe = new MaterialIconType("toolHeadAxe");
    public static final MaterialIconType toolHeadHoe = new MaterialIconType("toolHeadHoe");
    public static final MaterialIconType toolHeadHammer = new MaterialIconType("toolHeadHammer");
    public static final MaterialIconType toolHeadFile = new MaterialIconType("toolHeadFile");
    public static final MaterialIconType toolHeadSaw = new MaterialIconType("toolHeadSaw");
    public static final MaterialIconType toolHeadBuzzSaw = new MaterialIconType("toolHeadBuzzSaw");
    public static final MaterialIconType toolHeadDrill = new MaterialIconType("toolHeadDrill");
    public static final MaterialIconType toolHeadChainsaw = new MaterialIconType("toolHeadChainsaw");
    public static final MaterialIconType toolHeadScythe = new MaterialIconType("toolHeadScythe");
    public static final MaterialIconType toolHeadScrewdriver = new MaterialIconType("toolHeadScrewdriver");
    public static final MaterialIconType toolHeadWrench = new MaterialIconType("toolHeadWrench");

    public static final MaterialIconType turbineBlade = new MaterialIconType("turbineBlade");

    public static final MaterialIconType turbineSmall = new MaterialIconType("turbineSmall");
    public static final MaterialIconType turbineNormal = new MaterialIconType("turbineNormal");
    public static final MaterialIconType turbineLarge = new MaterialIconType("turbineLarge");

    // BLOCK TEXTURES
    public static final MaterialIconType liquid = new MaterialIconType("liquid");
    public static final MaterialIconType gas = new MaterialIconType("gas");
    public static final MaterialIconType plasma = new MaterialIconType("plasma");
    public static final MaterialIconType ore = new MaterialIconType("ore");
    public static final MaterialIconType oreSmall = new MaterialIconType("oreSmall");

    // BLOCK MODELS
    public static final MaterialIconType block = new MaterialIconType("block");
    public static final MaterialIconType frameGt = new MaterialIconType("frameGt");

    // USED FOR GREGIFICATION ADDON
    public static final MaterialIconType seed = new MaterialIconType("seed");
    public static final MaterialIconType crop = new MaterialIconType("crop");
    public static final MaterialIconType essence = new MaterialIconType("essence");

    private static final Table<MaterialIconType, MaterialIconSet, ResourceLocation> ITEM_MODEL_CACHE = HashBasedTable
            .create();
    private static final Table<MaterialIconType, MaterialIconSet, ResourceLocation> BLOCK_TEXTURE_CACHE = HashBasedTable
            .create();
    private static final Table<MaterialIconType, MaterialIconSet, ResourceLocation> BLOCK_MODEL_CACHE = HashBasedTable
            .create();

    private static final String BLOCK_TEXTURE_PATH_FULL = "textures/blocks/material_sets/%s/%s.png";
    private static final String BLOCK_TEXTURE_PATH = "blocks/material_sets/%s/%s";

    private static final String ITEM_MODEL_PATH_FULL = "models/item/material_sets/%s/%s.json";
    private static final String ITEM_MODEL_PATH = "material_sets/%s/%s";

    private static final String BLOCK_MODEL_PATH_FULL = "models/block/material_sets/%s/%s.json";
    private static final String BLOCK_MODEL_PATH = "block/material_sets/%s/%s";

    public final String name;
    public final int id;

    public MaterialIconType(String name) {
        this.name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
        Preconditions.checkArgument(!ICON_TYPES.containsKey(this.name),
                "MaterialIconType " + this.name + " already registered!");
        this.id = idCounter++;
        ICON_TYPES.put(this.name, this);
    }

    @NotNull
    public ResourceLocation getBlockTexturePath(@NotNull MaterialIconSet materialIconSet) {
        return recurseIconsetPath(materialIconSet, BLOCK_TEXTURE_CACHE, BLOCK_TEXTURE_PATH_FULL, BLOCK_TEXTURE_PATH);
    }

    @NotNull
    public ResourceLocation getItemModelPath(@NotNull MaterialIconSet materialIconSet) {
        return recurseIconsetPath(materialIconSet, ITEM_MODEL_CACHE, ITEM_MODEL_PATH_FULL, ITEM_MODEL_PATH);
    }

    @NotNull
    public ResourceLocation getBlockModelPath(@NotNull MaterialIconSet materialIconSet) {
        return recurseIconsetPath(materialIconSet, BLOCK_MODEL_CACHE, BLOCK_MODEL_PATH_FULL, BLOCK_MODEL_PATH);
    }

    /**
     * Find the location of the asset associated with the iconset or its parents as a fallback
     *
     * @param iconSet  the starting IconSet to get the location for
     * @param cache    the cache to store the value in
     * @param fullPath the full path to the asset with formatting (%s) for IconSet and IconType names
     * @param path     the abbreviated path to the asset with formatting (%s) for IconSet and IconType names
     * @return the location of the asset
     */
    @NotNull
    public ResourceLocation recurseIconsetPath(@NotNull MaterialIconSet iconSet,
                                               @NotNull Table<MaterialIconType, MaterialIconSet, ResourceLocation> cache,
                                               @NotNull String fullPath, @NotNull String path) {
        if (cache.contains(this, iconSet)) {
            return cache.get(this, iconSet);
        }

        if (!iconSet.isRootIconset && FMLCommonHandler.instance().getSide().isClient()) {
            ResourceLocation fullLocation = GTUtility.gregtechId(String.format(fullPath, iconSet.name, this.name));
            if (!ResourceHelper.doResourcepacksHaveResource(fullLocation)) {
                ResourceLocation iconSetPath = recurseIconsetPath(iconSet.parentIconset, cache, fullPath, path);
                cache.put(this, iconSet, iconSetPath);
                return iconSetPath;
            }
        }

        ResourceLocation iconSetPath = GTUtility.gregtechId(String.format(path, iconSet.name, this.name));
        cache.put(this, iconSet, iconSetPath);
        return iconSetPath;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @SuppressWarnings("unused") // Called from ASM-injected code
    public static void clearCache() {
        ITEM_MODEL_CACHE.clear();
        BLOCK_TEXTURE_CACHE.clear();
        BLOCK_MODEL_CACHE.clear();
    }
}
