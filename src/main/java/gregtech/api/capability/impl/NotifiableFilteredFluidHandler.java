package gregtech.api.capability.impl;

import gregtech.api.metatileentity.MetaTileEntity;

@Deprecated
public class NotifiableFilteredFluidHandler extends NotifiableFluidTank {

    public NotifiableFilteredFluidHandler(int capacity, MetaTileEntity entityToNotify, boolean isExport) {
        super(capacity, entityToNotify, isExport);
    }
}
