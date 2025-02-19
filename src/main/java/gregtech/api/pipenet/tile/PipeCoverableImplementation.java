package gregtech.api.pipenet.tile;

import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverHolder;
import gregtech.api.cover.CoverSaveHandler;
import gregtech.api.metatileentity.interfaces.ISyncedTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.function.Consumer;

import static gregtech.api.capability.GregtechDataCodes.*;

public class PipeCoverableImplementation implements CoverHolder {

    private IPipeTile<?, ?> holder;
    private final EnumMap<EnumFacing, Cover> covers = new EnumMap<>(EnumFacing.class);
    private final int[] sidedRedstoneInput = new int[6];

    public PipeCoverableImplementation(IPipeTile<?, ?> holder) {
        this.holder = holder;
    }

    public void transferDataTo(PipeCoverableImplementation destImpl) {
        for (EnumFacing side : EnumFacing.VALUES) {
            Cover cover = covers.get(side);
            if (cover == null) continue;
            NBTTagCompound tagCompound = new NBTTagCompound();
            cover.writeToNBT(tagCompound);
            Cover newCover = cover.getDefinition().createCover(destImpl, side);
            newCover.readFromNBT(tagCompound);
            destImpl.covers.put(side, newCover);
        }
    }

    @Override
    public final void addCover(@NotNull EnumFacing side, @NotNull Cover cover) {
        if (cover instanceof ITickable && !holder.supportsTicking()) {
            IPipeTile<?, ?> newHolderTile = holder.setSupportsTicking();
            newHolderTile.getCoverableImplementation().addCover(side, cover);
            holder = newHolderTile;
            return;
        }
        // we checked before if the side already has a cover
        this.covers.put(side, cover);
        if (!getWorld().isRemote) {
            // do not sync or handle logic on client side
            CoverSaveHandler.writeCoverPlacement(this, COVER_ATTACHED_PIPE, side, cover);
            if (cover.shouldAutoConnectToPipes()) {
                holder.setConnection(side, true, false);
            }
        }

        holder.notifyBlockUpdate();
        holder.markAsDirty();
    }

    @Override
    public final void removeCover(@NotNull EnumFacing side) {
        Cover cover = getCoverAtSide(side);
        if (cover == null) return;

        dropCover(side);
        covers.remove(side);
        writeCustomData(COVER_REMOVED_PIPE, buffer -> buffer.writeByte(side.getIndex()));
        if (cover.shouldAutoConnectToPipes()) {
            holder.setConnection(side, false, false);
        }
        holder.notifyBlockUpdate();
        holder.markAsDirty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ItemStack getStackForm() {
        BlockPipe pipeBlock = holder.getPipeBlock();
        return pipeBlock.getDropItem(holder);
    }

    public void onLoad() {
        for (EnumFacing side : EnumFacing.VALUES) {
            this.sidedRedstoneInput[side.getIndex()] = GTUtility.getRedstonePower(getWorld(), getPos(), side);
        }
    }

    @Override
    public final int getInputRedstoneSignal(@NotNull EnumFacing side, boolean ignoreCover) {
        if (!ignoreCover && getCoverAtSide(side) != null) {
            return 0; // covers block input redstone signal for machine
        }
        return sidedRedstoneInput[side.getIndex()];
    }

    public void updateInputRedstoneSignals() {
        for (EnumFacing side : EnumFacing.VALUES) {
            int redstoneValue = GTUtility.getRedstonePower(getWorld(), getPos(), side);
            int currentValue = sidedRedstoneInput[side.getIndex()];
            if (redstoneValue != currentValue) {
                this.sidedRedstoneInput[side.getIndex()] = redstoneValue;
                Cover cover = getCoverAtSide(side);
                if (cover != null) {
                    cover.onRedstoneInputSignalChange(redstoneValue);
                }
            }
        }
    }

    @Override
    public void notifyBlockUpdate() {
        holder.notifyBlockUpdate();
    }

    @Override
    public void scheduleRenderUpdate() {
        BlockPos pos = getPos();
        getWorld().markBlockRangeForRenderUpdate(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    @Override
    public double getCoverPlateThickness() {
        float thickness = holder.getPipeType().getThickness();
        // no cover plate for pipes >= 1 block thick
        if (thickness >= 1) return 0;

        // If the available space for the cover is less than the regular cover plate thickness, use that

        // need to divide by 2 because thickness is centered on the block, so the space is half on each side of the pipe
        return Math.min(1.0 / 16.0, (1.0 - thickness) / 2);
    }

    @Override
    public boolean shouldRenderCoverBackSides() {
        return false;
    }

    @Override
    public int getPaintingColorForRendering() {
        return ConfigHolder.client.defaultPaintingColor;
    }

    @Override
    public boolean canPlaceCoverOnSide(@NotNull EnumFacing side) {
        return holder.canPlaceCoverOnSide(side);
    }

    @Override
    public final boolean acceptsCovers() {
        return covers.size() < EnumFacing.VALUES.length;
    }

    public boolean canConnectRedstone(@Nullable EnumFacing side) {
        // so far null side means either upwards or downwards redstone wire connection
        // so check both top cover and bottom cover
        if (side == null) {
            return canConnectRedstone(EnumFacing.UP) ||
                    canConnectRedstone(EnumFacing.DOWN);
        }
        Cover cover = getCoverAtSide(side);
        return cover != null && cover.canConnectRedstone();
    }

    public int getOutputRedstoneSignal(@Nullable EnumFacing side) {
        if (side == null) {
            return getHighestOutputRedstoneSignal();
        }
        Cover cover = getCoverAtSide(side);
        return cover == null ? 0 : cover.getRedstoneSignalOutput();
    }

    public int getHighestOutputRedstoneSignal() {
        int highestSignal = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            Cover cover = getCoverAtSide(side);
            if (cover == null) continue;
            highestSignal = Math.max(highestSignal, cover.getRedstoneSignalOutput());
        }
        return highestSignal;
    }

    public void update() {
        if (!getWorld().isRemote) {
            updateCovers();
        }
    }

    @Override
    public void writeCoverData(@NotNull Cover cover, int discriminator,
                               @NotNull Consumer<@NotNull AdvancedPacketBuffer> buf) {
        writeCustomData(UPDATE_COVER_DATA_PIPE, buffer -> {
            buffer.writeByte(cover.getAttachedSide().getIndex());
            buffer.writeVarInt(discriminator);
            buf.accept(buffer.openSubBuffer());
            buffer.writeSubBuffer();
        });
    }

    public void writeInitialSyncData(AdvancedPacketBuffer buf) {
        CoverSaveHandler.writeInitialSyncData(buf, this);
    }

    public void readInitialSyncData(AdvancedPacketBuffer buf) {
        CoverSaveHandler.receiveInitialSyncData(buf, this);
    }

    @Override
    public void writeCustomData(int dataId, @NotNull Consumer<AdvancedPacketBuffer> writer) {
        holder.writeCoverCustomData(dataId, writer);
    }

    public void readCustomData(int dataId, AdvancedPacketBuffer buf) {
        if (dataId == COVER_ATTACHED_PIPE) {
            CoverSaveHandler.readCoverPlacement(buf, this);
        } else if (dataId == COVER_REMOVED_PIPE) {
            // cover removed event
            EnumFacing placementSide = EnumFacing.VALUES[buf.readByte()];
            this.covers.remove(placementSide);
            holder.scheduleChunkForRenderUpdate();
        } else if (dataId == UPDATE_COVER_DATA_PIPE) {
            // cover custom data received
            EnumFacing coverSide = EnumFacing.VALUES[buf.readByte()];
            Cover cover = getCoverAtSide(coverSide);
            int internalId = buf.readVarInt();
            AdvancedPacketBuffer b = buf.readSubBuffer(internalId);
            if (cover == null) {
                GTLog.logger.warn("Unable to find cover for side {} at position {}", coverSide,
                        this.getPos());
            } else {
                cover.readCustomData(internalId, b);
                ISyncedTileEntity.checkData(b, cover);
            }
            b.closeSubBuffer();
        }
    }

    public void writeToNBT(NBTTagCompound data) {
        CoverSaveHandler.writeCoverNBT(data, this);
    }

    public void readFromNBT(NBTTagCompound data) {
        CoverSaveHandler.readCoverNBT(data, this, covers::put);
    }

    @Override
    public World getWorld() {
        return holder.getPipeWorld();
    }

    @Override
    public BlockPos getPos() {
        return holder.getPipePos();
    }

    public TileEntity getTileEntityHere() {
        return holder instanceof TileEntity te ? te : getWorld().getTileEntity(getPos());
    }

    @Override
    public @Nullable TileEntity getNeighbor(@NotNull EnumFacing facing) {
        return holder.getNeighbor(facing);
    }

    @Override
    public long getOffsetTimer() {
        return holder.getTickTimer();
    }

    @Nullable
    @Override
    public Cover getCoverAtSide(@NotNull EnumFacing side) {
        return covers.get(side);
    }

    @Override
    public boolean hasAnyCover() {
        return !covers.isEmpty();
    }

    @Override
    public void markDirty() {
        holder.markAsDirty();
    }

    @Override
    public boolean isValid() {
        return holder.isValidTile();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        return holder.getCapabilityInternal(capability, side);
    }
}
