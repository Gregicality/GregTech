package gregtech.api.cover;

import gregtech.api.network.AdvancedPacketBuffer;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.Consumer;

public interface CoverableView extends ICapabilityProvider {

    /**
     * @return the world containing the CoverableView
     */
    @UnknownNullability
    World getWorld();

    /**
     * @return the pos of the block containing the CoverableView
     */
    @UnknownNullability
    BlockPos getPos();

    /**
     * @param facing the side to get the neighbor at
     * @return the neighbor tile entity at the side
     */
    @Nullable
    TileEntity getNeighbor(@NotNull EnumFacing facing);

    /**
     * Mark the CoverableView as needing to be saved to the chunk
     */
    void markDirty();

    /**
     * Notify block updates for the CoverableView
     */
    void notifyBlockUpdate();

    /**
     * Schedule the CoverableView to update rendering
     */
    void scheduleRenderUpdate();

    /**
     * @return tick timer value with a random offset of [0,20]
     */
    long getOffsetTimer();

    /**
     * @return if the CoverableView is a valid TileEntity
     */
    boolean isValid();

    /**
     * @param side the side to retrieve a cover from
     * @return the cover at the side
     */
    @Nullable
    Cover getCoverAtSide(@NotNull EnumFacing side);

    /**
     * @param side the side to check
     * @return if there is a cover at the side
     */
    default boolean hasCover(@NotNull EnumFacing side) {
        return getCoverAtSide(side) != null;
    }

    /**
     * @return if there is any cover attached
     */
    boolean hasAnyCover();

    /**
     * @param side        the side to get the redstone from
     * @param ignoreCover if the cover is being ignored
     * @return the redstone signal being input at the side
     */
    int getInputRedstoneSignal(@NotNull EnumFacing side, boolean ignoreCover);

    void writeCoverData(@NotNull Cover cover, int discriminator, @NotNull Consumer<@NotNull AdvancedPacketBuffer> buf);

    /**
     * @return an ItemStack representation of the CoverableView, or {@link ItemStack#EMPTY} if not possible.
     */
    @NotNull
    ItemStack getStackForm();
}
