package gregtech.api.capability.impl;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import java.util.ArrayList;
import java.util.List;

public class NotifiableItemStackHandler extends GTItemStackHandler implements INotifiableHandler {

    List<MetaTileEntity> notifiableEntities = new ArrayList<>();
    private final boolean isExport;

    public NotifiableItemStackHandler(MetaTileEntity metaTileEntity, int slots, MetaTileEntity entityToNotify,
                                      boolean isExport) {
        super(metaTileEntity, slots);
        if (entityToNotify != null) {
            this.notifiableEntities.add(entityToNotify);
        }
        this.isExport = isExport;
    }

    @Override
    public void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        for (MetaTileEntity metaTileEntity : notifiableEntities) {
            if (metaTileEntity != null && metaTileEntity.isValid()) {
                addToNotifiedList(metaTileEntity, this, isExport);
            }
        }
    }

    @Override
    public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        if (metaTileEntity == null) return;
        this.notifiableEntities.add(metaTileEntity);
    }

    @Override
    public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.remove(metaTileEntity);
    }
}
