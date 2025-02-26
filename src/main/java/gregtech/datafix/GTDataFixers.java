package gregtech.datafix;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.datafix.impl.V2PostMTEReallocFixer;
import gregtech.datafix.migration.impl.MigrateMTEBlockTE;
import gregtech.datafix.migration.impl.MigrateMTEItems;
import gregtech.datafix.migration.lib.MTEDataMigrator;
import gregtech.datafix.migration.lib.MTERegistriesMigrator;
import gregtech.datafix.walker.WalkItemStackLike;

import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

import static gregtech.api.GTValues.*;
import static gregtech.common.metatileentities.MetaTileEntities.*;

public final class GTDataFixers {

    public static final Logger LOGGER = LogManager.getLogger("GregTech DataFixers");
    private static final IDataWalker ITEM_STACK_WALKER = new WalkItemStackLike();

    private GTDataFixers() {}

    public static void init() {
        final CompoundDataFixer forgeFixer = FMLCommonHandler.instance().getDataFixer();
        registerWalkers(forgeFixer);
        registerFixes(forgeFixer);
        migrateMTERegistries();
    }

    private static void registerWalkers(@NotNull CompoundDataFixer fixer) {
        fixer.registerVanillaWalker(FixTypes.BLOCK_ENTITY, ITEM_STACK_WALKER);
        fixer.registerVanillaWalker(FixTypes.ENTITY, ITEM_STACK_WALKER);
        fixer.registerVanillaWalker(FixTypes.PLAYER, ITEM_STACK_WALKER);
    }

    private static void registerFixes(@NotNull CompoundDataFixer forgeFixer) {
        LOGGER.info("GT data version is: {}", GTDataVersion.currentVersion());
        ModFixs fixer = forgeFixer.init(GTValues.MODID, GTDataVersion.currentVersion().ordinal());

        for (GTDataVersion version : GTDataVersion.VALUES) {
            registerFixes(version, fixer);
        }
    }

    private static void registerFixes(@NotNull GTDataVersion version, @NotNull ModFixs fixer) {
        if (version != GTDataVersion.V0_PRE_MTE) {
            LOGGER.info("Registering fixer for data version {}", version);
        }
        switch (version) {
            case V1_POST_MTE -> {
                MTERegistriesMigrator migrator = GregTechAPI.MIGRATIONS.registriesMigrator();
                fixer.registerFix(GTFixType.ITEM_STACK_LIKE, new MigrateMTEItems(migrator));
                fixer.registerFix(FixTypes.CHUNK, new MigrateMTEBlockTE(migrator));
            }
            case V2_POST_ID_REALLOC -> {
                MTEDataMigrator dataMigrator = new MTEDataMigrator(fixer, version.ordinal());
                V2PostMTEReallocFixer.apply(dataMigrator);
            }
            default -> {}
        }
    }

    /**
     * Migrate GT's own MTEs to the new blocks automatically
     */
    private static void migrateMTERegistries() {
        MTERegistriesMigrator migrator = GregTechAPI.MIGRATIONS.registriesMigrator();
        migrator.migrate(GTValues.MODID, IntStream.range(0, 2000));
    }
}
