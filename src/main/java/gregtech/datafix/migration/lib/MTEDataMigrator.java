package gregtech.datafix.migration.lib;

import gregtech.api.GregTechAPI;
import gregtech.datafix.GTFixType;
import gregtech.datafix.migration.api.AbstractMTEMigrator;
import gregtech.datafix.migration.impl.MigrateMTEBlockTE;
import gregtech.datafix.migration.impl.MigrateMTEItems;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.util.ModFixs;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Performs arbitrary data migration for MTEs.
 * <p>
 * Has the following capabilities:
 * <ul>
 * <li>Migrating MTE registry names to new mod ids and name values</li>
 * <li>Migrating MTE metadata to new values</li>
 * <li>Migrating MTE NBT tags to new arbitrary tags</li>
 * </ul>
 * <p>
 * Usage:
 * <ol>
 * <li>Use a data versioning scheme like {@link gregtech.datafix.GTDataVersion}</li>
 * <li>In FMLPreInit, get the FML data fixer with
 * {@link net.minecraftforge.fml.common.FMLCommonHandler#getDataFixer()}</li>
 * <li>Call {@link net.minecraftforge.common.util.CompoundDataFixer#init(String, int)} to create a data fixer for your
 * mod</li>
 * <li>Create a new migrator with {@link #MTEDataMigrator(ModFixs, int)}</li>
 * <li>Once a migrator has been applied to a world, future migrations need to be done in a new migrator instance</li>
 * </ol>
 */
public final class MTEDataMigrator extends AbstractMTEMigrator {

    private final Object2ObjectMap<ResourceLocation, ResourceLocation> nameMap = new Object2ObjectOpenHashMap<>();
    private final Map<String, Short2ShortMap> itemBlockMeta = new Object2ObjectOpenHashMap<>();
    private final Map<ResourceLocation, Consumer<NBTTagCompound>> mteMigrators = new Object2ObjectOpenHashMap<>();

    /**
     * This data migrator will first attempt to migrate the registries over in case forge runs this before GT does the
     * dedicated migration.
     * <p>
     * DataFixer application order is non-deterministic across different mod ids, so we have to ensure other mods only
     * need to interact with the post mte registry migration metadata
     */
    private final MTERegistriesMigrator registriesMigrator = GregTechAPI.MIGRATIONS.registriesMigrator();

    /**
     * @param fixer      the fixer owning the migration
     * @param fixVersion the version this migration will bring the mod data to
     */
    public MTEDataMigrator(@NotNull ModFixs fixer, int fixVersion) {
        super(fixVersion);
        fixer.registerFix(GTFixType.ITEM_STACK_LIKE, new MigrateMTEItems(this));
        fixer.registerFix(FixTypes.CHUNK, new MigrateMTEBlockTE(this));
    }

    /**
     * Migrate an MTE's registry name to something else
     *
     * @param preRegistryName  the original registry name
     * @param postRegistryName the new registry name
     */
    public void migrateMTEName(@NotNull ResourceLocation preRegistryName, @NotNull ResourceLocation postRegistryName) {
        nameMap.put(preRegistryName, postRegistryName);
    }

    /**
     * Migrate an MTE's data to something else
     *
     * @param preRegistryName the original registry name
     * @param migrator        a data migrator operating on the MTE's NBT tag compound
     */
    public void migrateMTEData(@NotNull ResourceLocation preRegistryName,
                               @NotNull Consumer<@NotNull NBTTagCompound> migrator) {
        mteMigrators.put(preRegistryName, migrator);
    }

    /**
     * @param modid    the modid for the MTE's ItemBlock
     * @param preMeta  the original metadata for the MTE's ItemBlock
     * @param postMeta the new metadata for the MTE's ItemBlock
     */
    public void migrateMTEMeta(@NotNull String modid, int preMeta, int postMeta) {
        var mappings = itemBlockMeta.computeIfAbsent(modid, k -> {
            var map = new Short2ShortOpenHashMap();
            map.defaultReturnValue((short) -1);
            return map;
        });
        mappings.put((short) preMeta, (short) postMeta);
    }

    @Override
    public @Nullable ResourceLocation fixMTEid(@NotNull ResourceLocation original) {
        return nameMap.get(original);
    }

    @Override
    public void fixMTEData(@NotNull ResourceLocation original, @NotNull NBTTagCompound tag) {
        var migrator = mteMigrators.get(original);
        if (migrator != null) {
            migrator.accept(tag);
        }
    }

    @Override
    public short fixItemMeta(@NotNull ResourceLocation itemName, short meta) {
        meta = registriesMigrator.fixItemMeta(itemName, meta);
        Short2ShortMap map = itemBlockMeta.get(itemName.getNamespace());
        if (map != null) {
            short newMeta = map.get(meta);
            if (newMeta > 0) {
                return newMeta;
            }
        }
        return meta;
    }

    @Override
    public @Nullable ResourceLocation fixItemName(@NotNull ResourceLocation original, short originalMeta) {
        return registriesMigrator.fixItemName(original, originalMeta);
    }
}
