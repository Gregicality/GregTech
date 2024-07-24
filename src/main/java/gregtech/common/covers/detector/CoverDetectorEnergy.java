package gregtech.common.covers.detector;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.cover.ICustomEnergyCover;
import gregtech.api.util.RedstoneUtil;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

public class CoverDetectorEnergy extends CoverDetectorBase implements ITickable {

    public CoverDetectorEnergy(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                               @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null) != null ||
                coverable instanceof ICustomEnergyCover; // todo check this
    }

    public long getCoverHolderCapacity() {
        if (getCoverableView() instanceof ICustomEnergyCover custom) {
            return custom.getCoverCapacity();
        } else {
            IEnergyContainer energyContainer = getCoverableView()
                    .getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
            if (energyContainer != null) return energyContainer.getEnergyCapacity();
        }
        return 0;
    }

    public long getCoverHolderStored() {
        if (getCoverableView() instanceof ICustomEnergyCover custom) {
            return custom.getCoverStored();
        } else {
            IEnergyContainer energyContainer = getCoverableView()
                    .getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
            if (energyContainer != null) return energyContainer.getEnergyStored();
        }
        return 0;
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        Textures.DETECTOR_ENERGY.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public void update() {
        if (getOffsetTimer() % 20 != 0) return;

        long storedEnergy = getCoverHolderStored();
        long energyCapacity = getCoverHolderCapacity();

        if (energyCapacity == 0) return;

        setRedstoneSignalOutput(RedstoneUtil.computeRedstoneValue(storedEnergy, energyCapacity, isInverted()));
    }
}
