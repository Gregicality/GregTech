package gregtech.integration.opencomputers.drivers.specific;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.metatileentities.multi.MetaTileEntityFissionReactor;
import gregtech.integration.opencomputers.drivers.EnvironmentMetaTileEntity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;

public class DriverFissionReactor extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return MetaTileEntityFissionReactor.class;
    }

    @Override
    public boolean worksWith(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return ((IGregTechTileEntity) tileEntity).getMetaTileEntity() instanceof MetaTileEntityFissionReactor;
        }
        return false;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return new EnvironmentFissionReactor((IGregTechTileEntity) tileEntity,
                    (MetaTileEntityFissionReactor) ((IGregTechTileEntity) tileEntity).getMetaTileEntity());
        }
        return null;
    }

    public final static class EnvironmentFissionReactor extends
                                                        EnvironmentMetaTileEntity<MetaTileEntityFissionReactor> {

        public EnvironmentFissionReactor(IGregTechTileEntity holder, MetaTileEntityFissionReactor tileEntity) {
            super(holder, tileEntity, "gt_fissionReactor");
        }

        @Callback(doc = "function():number --  Returns the max power of the reactor, in MW.")
        public Object[] getMaxPower(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getMaxPower() };
        }

        @Callback(doc = "function():number --  Returns the power of the reactor, in MW.")
        public Object[] getPower(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getPower() };
        }

        @Callback(doc = "function():number --  Returns the max temperature of the reactor.")
        public Object[] getMaxTemperature(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getMaxTemperature() };
        }

        @Callback(doc = "function():number --  Returns the temperature of the reactor.")
        public Object[] getTemperature(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getTemperature() };
        }

        @Callback(doc = "function():number --  Returns the max pressure of the reactor, in pascals.")
        public Object[] getMaxPressure(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getMaxPressure() };
        }

        @Callback(doc = "function():number --  Returns the pressure of the reactor, in pascals.")
        public Object[] getPressure(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getPressure() };
        }

        @Callback(doc = "function():number --  Returns how much control rods are inserted, in [0, 1]")
        public Object[] getControlRodInsertion(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getControlRodInsertion() };
        }

        @Callback(doc = "function(insertion:boolean) --  Sets how much control rods are inserted, in [0, 1]")
        public Object[] setControlRodInsertion(final Context context, final Arguments args) {
            tileEntity.setControlRodInsertionValue((float) args.checkDouble(0));
            return new Object[] {};
        }

        @Callback(doc = "function():boolean --  Returns whether smiley is regulating control rods.")
        public Object[] getSmiley(final Context context, final Arguments args) {
            return new Object[] { tileEntity.areControlRodsRegulated() };
        }

        @Callback(doc = "function(smiley:boolean) --  Pass in true to revive smiley, pass in false to kill smiley.")
        public Object[] setSmiley(final Context context, final Arguments args) {
            tileEntity.toggleControlRodRegulation(args.checkBoolean(0));
            return new Object[] {};
        }
    }
}
