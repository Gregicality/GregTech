package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;

import java.util.ArrayList;
import java.util.List;

public abstract class MetaTileEntityMultiblockNotifiablePart extends MetaTileEntityMultiblockPart {

    protected final boolean isExportHatch;

    public MetaTileEntityMultiblockNotifiablePart(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier);
        this.isExportHatch = isExportHatch;
    }

    private INotifiableHandler getItemHandler() {
        INotifiableHandler handler = null;
        if (isExportHatch && getExportItems() instanceof INotifiableHandler) {
            handler = (INotifiableHandler) getExportItems();
        } else if (!isExportHatch && getImportItems() instanceof INotifiableHandler) {
            handler = (INotifiableHandler) getImportItems();
        } else if (getItemInventory() instanceof INotifiableHandler) {
            handler = (INotifiableHandler) getItemInventory();
        }
        return handler;
    }

    private FluidTankList getFluidHandlers() {
        FluidTankList handler = null;
        if (isExportHatch && getExportFluids().getFluidTanks().size() > 0) {
            handler = getExportFluids();
        } else if (!isExportHatch && getImportFluids().getFluidTanks().size() > 0) {
            handler = getImportFluids();
        }
        return handler;
    }

    private List<INotifiableHandler> getPartHandlers() {
        List<INotifiableHandler> handlerList = new ArrayList<>();

        INotifiableHandler itemHandler = getItemHandler();
        if (itemHandler != null && itemHandler.size() > 0) {
            handlerList.add(itemHandler);
        }

        if (this.fluidInventory.getTankProperties().length > 0) {
            FluidTankList fluidTankList = getFluidHandlers();
            if (fluidTankList != null) {
                for (IFluidTank fluidTank : fluidTankList) {
                    if (fluidTank instanceof IMultipleTankHandler.MultiFluidTankEntry entry) {
                        fluidTank = entry.getDelegate();
                    }
                    if (fluidTank instanceof INotifiableHandler) {
                        handlerList.add((INotifiableHandler) fluidTank);
                    }
                }
            }
        }
        return handlerList;
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        List<INotifiableHandler> handlerList = getPartHandlers();
        for (INotifiableHandler handler : handlerList) {
            handler.addNotifiableMetaTileEntity(controllerBase);
            handler.addToNotifiedList(this, handler, isExportHatch);
        }
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        List<INotifiableHandler> handlerList = getPartHandlers();
        for (INotifiableHandler handler : handlerList) {
            handler.removeNotifiableMetaTileEntity(controllerBase);
        }
    }
}
