package gregtech.api.capability.impl;

import gregtech.api.capability.IDistillationTower;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.util.GTLog;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Allows hatchscan behavior to be used on fluid outputs. Not a child of {@link AbstractRecipeLogic}
 * for compatibility with other children.
 */
public class DistillationTowerLogicHandler {

    protected final IDistillationTower tower;

    private int layerCount;
    private List<IFluidHandler> orderedFluidOutputs;
    private IMultipleTankHandler fluidTanks;

    public DistillationTowerLogicHandler(IDistillationTower tower) {
        this.tower = tower;
    }

    /**
     * Applies fluids to outputs on a sorted one fluid -> one hatch basis
     * 
     * @param fluids the fluids to output. Will be automatically trimmed if there are not enough output hatches.
     * @param doFill whether the application should be simulated or not.
     * @return whether the fluids were successfully applied to the outputs or not.
     */
    public boolean applyFluidToOutputs(List<FluidStack> fluids, boolean doFill) {
        boolean valid = true;
        for (int i = 0; i < Math.min(fluids.size(), this.getOrderedFluidOutputs().size()); i++) {
            IFluidHandler handler = this.getOrderedFluidOutputs().get(i);
            int accepted = handler.fill(fluids.get(i), doFill);
            if (accepted != fluids.get(i).amount) valid = false;
            if (!doFill && !valid) break;
        }
        return valid;
    }

    /**
     * Called on structure formation to determine the number of layers in the distillation tower. <br>
     * <br>
     * Needs to be overriden for multiblocks that have different assemblies than the standard distillation tower.
     *
     * @param structurePattern the structure pattern
     */
    public void determineLayerCount(@NotNull BlockPattern structurePattern) {
        this.setLayerCount(structurePattern.formedRepetitionCount[1] + 1);
    }

    /**
     * Called on structure formation to determine the ordered list of fluid handlers in the distillation tower. <br>
     * <br>
     * Needs to be overriden for multiblocks that have different assemblies than the standard distillation tower.
     */
    public void determineOrderedFluidOutputs() {
        // noinspection SimplifyStreamApiCallChains
        List<MetaTileEntityMultiblockPart> fluidExportParts = tower.getMultiblockParts().stream()
                .filter(iMultiblockPart -> iMultiblockPart instanceof IMultiblockAbilityPart<?>abilityPart &&
                        abilityPart.getAbility() == MultiblockAbility.EXPORT_FLUIDS &&
                        abilityPart instanceof MetaTileEntityMultiblockPart)
                .map(iMultiblockPart -> (MetaTileEntityMultiblockPart) iMultiblockPart)
                .collect(Collectors.toList());
        // the fluidExportParts should come sorted in smallest Y first, largest Y last.
        List<IFluidHandler> orderedHandlerList = new ObjectArrayList<>();
        List<IFluidTank> tankList = new ObjectArrayList<>();
        int firstY = tower.getPos().getY() + 1;
        int exportIndex = 0;
        for (int y = firstY; y < firstY + this.getLayerCount(); y++) {
            if (fluidExportParts.size() <= exportIndex) {
                orderedHandlerList.add(FakeTank.INSTANCE);
                tankList.add(FakeTank.INSTANCE);
                continue;
            }
            MetaTileEntityMultiblockPart part = fluidExportParts.get(exportIndex);
            if (part.getPos().getY() == y) {
                // noinspection unchecked
                List<? extends IFluidTank> hatchTanks = ((IMultiblockAbilityPart<IFluidTank>) part)
                        .registerAbilities(MultiblockAbility.EXPORT_FLUIDS);
                orderedHandlerList.add(new FluidTankList(false, hatchTanks));
                tankList.addAll(hatchTanks);
                exportIndex++;
            } else if (part.getPos().getY() > y) {
                orderedHandlerList.add(FakeTank.INSTANCE);
                tankList.add(FakeTank.INSTANCE);
            } else {
                GTLog.logger.error(
                        "The Distillation Tower at {} had a fluid export hatch with an unexpected Y position.",
                        tower.getPos());
                tower.invalidateStructure();
                this.setOrderedFluidOutputs(new ObjectArrayList<>());
                this.setFluidTanks(new FluidTankList(false));
            }
        }
        this.setOrderedFluidOutputs(orderedHandlerList);
        this.setFluidTanks(new FluidTankList(tower.allowSameFluidFillForOutputs(), tankList));
    }

    /**
     * Should be called on structure invalidation.
     */
    public void invalidate() {
        this.setLayerCount(0);
        this.setOrderedFluidOutputs(null);
    }

    protected void setLayerCount(int layerCount) {
        this.layerCount = layerCount;
    }

    public int getLayerCount() {
        return layerCount;
    }

    protected void setOrderedFluidOutputs(List<IFluidHandler> orderedFluidOutputs) {
        this.orderedFluidOutputs = orderedFluidOutputs;
    }

    public List<IFluidHandler> getOrderedFluidOutputs() {
        return orderedFluidOutputs;
    }

    protected void setFluidTanks(IMultipleTankHandler fluidTanks) {
        this.fluidTanks = fluidTanks;
    }

    public IMultipleTankHandler getFluidTanks() {
        return fluidTanks;
    }

    // an endless void devouring any fluid sent to it
    protected static class FakeTank implements IFluidHandler, IFluidTank {

        protected static final FakeTank INSTANCE = new FakeTank();
        public static final FluidTankInfo FAKE_TANK_INFO = new FluidTankInfo(null, Integer.MAX_VALUE);
        public static final IFluidTankProperties FAKE_TANK_PROPERTIES = new FluidTankProperties(null, Integer.MAX_VALUE,
                true, false);
        public static final IFluidTankProperties[] FAKE_TANK_PROPERTIES_ARRAY = new IFluidTankProperties[] {
                FAKE_TANK_PROPERTIES };

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return FAKE_TANK_PROPERTIES_ARRAY;
        }

        @Override
        public FluidStack getFluid() {
            return null;
        }

        @Override
        public int getFluidAmount() {
            return 0;
        }

        @Override
        public int getCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public FluidTankInfo getInfo() {
            return FAKE_TANK_INFO;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return resource.amount;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return null;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }
    }
}
