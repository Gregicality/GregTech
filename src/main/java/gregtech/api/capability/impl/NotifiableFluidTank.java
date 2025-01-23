package gregtech.api.capability.impl;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NotifiableFluidTank extends FilteredFluidHandler implements INotifiableHandler {

    List<MetaTileEntity> notifiableEntities = new ArrayList<>();
    private final boolean isExport;

    public NotifiableFluidTank(int capacity, MetaTileEntity entityToNotify, boolean isExport) {
        super(capacity);
        this.notifiableEntities.add(entityToNotify);
        this.isExport = isExport;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        for (MetaTileEntity metaTileEntity : notifiableEntities) {
            if (metaTileEntity != null && metaTileEntity.isValid()) {
                addToNotifiedList(metaTileEntity, this, isExport);
            }
        }
    }

    @Override
    public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.add(metaTileEntity);
    }

    @Override
    public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.remove(metaTileEntity);
    }

    @Override
    public @NotNull NotifiableFluidTank setFilter(@Nullable IFilter<FluidStack> filter) {
        super.setFilter(filter);
        return this;
    }
}
