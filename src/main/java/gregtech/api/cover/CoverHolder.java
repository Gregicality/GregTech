package gregtech.api.cover;

import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.util.GTUtility;
import gregtech.client.utils.RenderUtil;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface CoverHolder extends CoverableView {

    Transformation REVERSE_HORIZONTAL_ROTATION = new Rotation(Math.PI, new Vector3(0.0, 1.0, 0.0)).at(Vector3.center);
    Transformation REVERSE_VERTICAL_ROTATION = new Rotation(Math.PI, new Vector3(1.0, 0.0, 0.0)).at(Vector3.center);

    /**
     * Add a cover to the CoverHolder
     *
     * @param side  the side to add the cover to
     * @param cover the cover to add
     */
    void addCover(@NotNull EnumFacing side, @NotNull Cover cover);

    /**
     * @param side the side to check
     * @return if the cover can be added at the side
     */
    boolean canPlaceCoverOnSide(@NotNull EnumFacing side);

    /**
     * @return if it is possible to attach any cover at all
     */
    boolean acceptsCovers();

    /**
     * Should call {@link #dropCover(EnumFacing)}.
     *
     * @param side the side to remove a cover from
     */
    void removeCover(@NotNull EnumFacing side);

    /**
     * Drop all attached covers on the ground
     */
    default void dropAllCovers() {
        for (EnumFacing side : EnumFacing.VALUES) {
            dropCover(side);
        }
    }

    /**
     * Drop a cover on the ground
     *
     * @param side the side the cover is attached to
     */
    default void dropCover(@NotNull EnumFacing side) {
        Cover cover = getCoverAtSide(side);
        if (cover == null) return;
        List<ItemStack> drops = cover.getDrops();
        cover.onRemoval();
        for (ItemStack dropStack : drops) {
            Block.spawnAsEntity(getWorld(), getPos(), dropStack);
        }
    }

    /**
     * Updates all covers. Should be called every tick.
     */
    default void updateCovers() {
        for (EnumFacing facing : EnumFacing.VALUES) {
            Cover cover = getCoverAtSide(facing);
            if (cover instanceof ITickable tickable) {
                tickable.update();
            }
        }
    }

    void writeCustomData(int discriminator, @NotNull Consumer<@NotNull AdvancedPacketBuffer> buf);

    /**
     * It is used to render cover's baseplate if this CoverHolder is not full block length.
     * Also used to check whether cover placement is possible on a side, because a cover cannot be placed if the
     * collision boxes of the Holder and its plate overlap.
     * If zero, it is expected that machine is full block and plate doesn't need to be rendered.
     * 
     * @return the cover plate thickness.
     */
    double getCoverPlateThickness();

    /**
     * @return if the back side of covers should be rendered
     */
    boolean shouldRenderCoverBackSides();

    /**
     * @return the painting color used for rendering
     */
    @SideOnly(Side.CLIENT)
    int getPaintingColorForRendering();

    @SideOnly(Side.CLIENT)
    default void renderCovers(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                              @NotNull BlockRenderLayer layer) {
        renderState.lightMatrix.locate(getWorld(), getPos());
        double coverPlateThickness = getCoverPlateThickness();
        IVertexOperation[] platePipeline = { renderState.lightMatrix,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())) };
        IVertexOperation[] coverPipeline = { renderState.lightMatrix };

        for (EnumFacing sideFacing : EnumFacing.values()) {
            Cover cover = getCoverAtSide(sideFacing);
            if (cover == null) continue;
            Cuboid6 plateBox = CoverUtil.getCoverPlateBox(sideFacing, coverPlateThickness);

            if (cover.canRenderInLayer(layer) && coverPlateThickness > 0) {
                renderState.preRenderWorld(getWorld(), getPos());
                cover.renderCoverPlate(renderState, translation, platePipeline, plateBox, layer);
            }

            if (cover.canRenderInLayer(layer)) {
                cover.renderCover(renderState, RenderUtil.adjustTrans(translation, sideFacing, 2), coverPipeline,
                        plateBox, layer);
                if (coverPlateThickness == 0.0 && shouldRenderCoverBackSides() && cover.canRenderBackside()) {
                    // machine is full block, but still not opaque - render cover on the back side too
                    Matrix4 backTranslation = translation.copy();
                    if (sideFacing.getAxis().isVertical()) {
                        REVERSE_VERTICAL_ROTATION.apply(backTranslation);
                    } else {
                        REVERSE_HORIZONTAL_ROTATION.apply(backTranslation);
                    }
                    backTranslation.translate(-sideFacing.getXOffset(), -sideFacing.getYOffset(),
                            -sideFacing.getZOffset());
                    cover.renderCover(renderState, backTranslation, coverPipeline, plateBox, layer); // may need to
                                                                                                     // translate the
                                                                                                     // layer here as
                                                                                                     // well
                }
            }
        }
    }

    default void addCoverCollisionBoundingBox(@NotNull List<? super IndexedCuboid6> collisionList) {
        double plateThickness = getCoverPlateThickness();
        if (plateThickness > 0.0) {
            for (EnumFacing side : EnumFacing.VALUES) {
                if (getCoverAtSide(side) != null) {
                    Cuboid6 coverBox = CoverUtil.getCoverPlateBox(side, plateThickness);
                    CoverRayTracer.CoverSideData coverSideData = new CoverRayTracer.CoverSideData(side);
                    collisionList.add(new IndexedCuboid6(coverSideData, coverBox));
                }
            }
        }
    }

    @Override
    default boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }
}
