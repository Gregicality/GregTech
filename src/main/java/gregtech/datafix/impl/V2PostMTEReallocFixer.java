package gregtech.datafix.impl;

import gregtech.api.GTValues;
import gregtech.datafix.migration.lib.MTEDataMigrator;

import org.jetbrains.annotations.NotNull;

import static gregtech.api.GTValues.*;
import static gregtech.api.util.GTUtility.gregtechId;
import static gregtech.common.metatileentities.MetaTileEntities.*;

public final class V2PostMTEReallocFixer {

    private V2PostMTEReallocFixer() {}

    public static void apply(@NotNull MTEDataMigrator dataMigrator) {
        // item/fluid input/output buses
        for (int i = 0; i < UEV; i++) {
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1150 + i, 11000 + i); // item in
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1165 + i, 11015 + i); // item out
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1180 + i, 11030 + i); // fluid in
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1195 + i, 11045 + i); // fluid out
        }
        for (int i = 0; i < ITEM_IMPORT_BUS.length - GTValues.UEV; i++) {
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1860 + i, 11000 + UEV + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1865 + i, 11015 + UEV + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1870 + i, 11030 + UEV + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1875 + i, 11045 + UEV + i);
        }
        // energy hatches
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1399, 11150 + EV); // 4A EV IN
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1400, 11165 + EV); // 4A EV OUT
        for (int i = 0; i < IV; i++) {
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1210 + i, 11120 + i); // 2A IN
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1225 + i, 11135 + i); // 2A OUT
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1240 + i, 11150 + IV + i); // 4A IN
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1245 + i, 11180 + IV + i); // 16A IN
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1250 + i, 11165 + IV + i); // 4A OUT
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1255 + i, 11195 + IV + i); // 16A OUT
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1260 + i, 11210 + IV + i); // 64A IN
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1265 + i, 11225 + IV + i); // 64A OUT
        }
        for (int i = 0; i < (MAX - UEV); i++) {
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1210 + i, 11120 + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1225 + i, 11135 + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1820 + i, 11150 + UEV + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1825 + i, 11180 + UEV + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1830 + i, 11165 + UEV + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1835 + i, 11195 + UEV + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1840 + i, 11210 + UEV + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1845 + i, 11225 + UEV + i);
        }
        // lasers
        for (int i = 0; i < UV; i++) {
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1420 + i, 11240 + IV + i); // 256A IN
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1429 + i, 11255 + IV + i); // 256A OUT
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1438 + i, 11270 + IV + i); // 1024A IN
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1447 + i, 11285 + IV + i); // 1024A OUT
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1456 + i, 11300 + IV + i); // 4096A IN
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1465 + i, 11315 + IV + i); // 4096A OUT
        }
        // rotor holders
        for (int i = 0; i < 6; i++) {
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1640 + i, 11450 + HV + i);
        }
        // quad/nonuple hatches
        for (int i = 0; i < UEV; i++) {
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1780 + i, 11060 + IV + i); // 4x in
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1785 + i, 11075 + IV + i); // 9x in
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1790 + i, 11090 + IV + i); // 4x out
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1795 + i, 11105 + IV + i); // 9x out
        }
        for (int i = 0; i < (QUADRUPLE_IMPORT_HATCH.length - UEV); i++) {
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1800 + i, 11060 + UEV + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1805 + i, 11075 + UEV + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1810 + i, 11090 + UEV + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1815 + i, 11105 + UEV + i);
        }
        // EV quad and nonuple import/export hatches
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1190, 11060 + EV);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1191, 11075 + EV);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1205, 11090 + EV);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1206, 11105 + EV);
        dataMigrator.migrateMTEName(gregtechId("fluid_hatch.import_4x"),
                gregtechId("fluid_hatch.import_4x.ev"));
        dataMigrator.migrateMTEName(gregtechId("fluid_hatch.import_9x"),
                gregtechId("fluid_hatch.import_9x.ev"));
        dataMigrator.migrateMTEName(gregtechId("fluid_hatch.export_4x"),
                gregtechId("fluid_hatch.export_4x.ev"));
        dataMigrator.migrateMTEName(gregtechId("fluid_hatch.export_9x"),
                gregtechId("fluid_hatch.export_9x.ev"));
        // mufflers
        for (int i = 0; i < GTValues.UHV; i++) {
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1657 + i, 11465 + LV + i);
        }
        // maintenance
        for (int i = 0; i < 3; i++) {
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1654 + i, 11480 + i);
        }
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1401, 11483);
        // PA hatch
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1398, 11495);
        // Passthrough hatches
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1402, 11496);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1403, 11497);
        // data hatches
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1404, 11498);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1405, 11499);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1406, 11500);
        // computation hatches
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1407, 11501);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1408, 11502);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1409, 11503);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1410, 11504);
        // reservoir hatch
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1418, 11505);
        // primitive pump hatch
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1649, 11506);
        // steam hatches
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1651, 11507);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1652, 11508);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1653, 11509);
        // object holder
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1411, 11510);
        // hpca components
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1412, 11511);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1413, 11512);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1414, 11513);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1415, 11514);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1416, 11515);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1417, 11516);
        // tank valves
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1596, 11523);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1598, 11524);
        // AE2 hatches
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1745, 11511);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1746, 11512);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1747, 11513);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1748, 11514);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1752, 11515);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1753, 11516);
        // transformers
        for (int i = 0; i < TRANSFORMER.length; i++) {
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1270 + i, 1000 + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1730 + i, 1015 + i);
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1285 + i, 1030 + i);
        }
        // magic energy absorber
        dataMigrator.migrateMTEMeta(GTValues.MODID, 984, 1752);
        // multiblocks
        for (int i = 0; i <= 42; i++) {
            dataMigrator.migrateMTEMeta(GTValues.MODID, 1000 + i, 10000 + i);
        }
        // multiblock tanks
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1597, 10043);
        dataMigrator.migrateMTEMeta(GTValues.MODID, 1599, 10044);
    }
}
