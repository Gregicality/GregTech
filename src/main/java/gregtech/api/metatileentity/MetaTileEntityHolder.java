package gregtech.api.metatileentity;

import com.google.common.base.Preconditions;
import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.gui.IUIHolder;
import gregtech.api.net.NetworkHandler;
import gregtech.api.net.packets.CPacketRecoverMTE;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

import static gregtech.api.capability.GregtechDataCodes.INITIALIZE_MTE;

public class MetaTileEntityHolder extends TickableTileEntityBase implements IUIHolder {

    MetaTileEntity metaTileEntity;
    private boolean needToUpdateLightning = false;

    private int[] timeStatistics = new int[25];
    private int timeStatisticsIndex = 0;
    private int lagWarningCount = 0;

    public MetaTileEntity getMetaTileEntity() {
        return metaTileEntity;
    }

    /**
     * Sets this holder's current meta tile entity to copy of given one
     * Note that this method copies given meta tile entity and returns actual instance
     * so it is safe to call it on sample meta tile entities
     * Also can use certain data to preinit the block before data is synced
     */
    public MetaTileEntity setMetaTileEntity(MetaTileEntity sampleMetaTileEntity, Object... data) {
        Preconditions.checkNotNull(sampleMetaTileEntity, "metaTileEntity");
        setRawMetaTileEntity(sampleMetaTileEntity.createMetaTileEntity(this));
        this.metaTileEntity.onAttached(data);
        if (hasWorld() && !getWorld().isRemote) {
            updateBlockOpacity();
            sendInitialSyncData();
            //just to update neighbours so cables and other things will work properly
            this.needToUpdateLightning = true;
            world.neighborChanged(getPos(), getBlockType(), getPos());
            markDirty();
        }
        return metaTileEntity;
    }

    protected void setRawMetaTileEntity(MetaTileEntity metaTileEntity){
        this.metaTileEntity = metaTileEntity;
        this.metaTileEntity.holder = this;
    }

    private void updateBlockOpacity() {
        IBlockState currentState = world.getBlockState(getPos());
        boolean isMetaTileEntityOpaque = metaTileEntity.isOpaqueCube();
        if (currentState.getValue(BlockMachine.OPAQUE) != isMetaTileEntityOpaque) {
            world.setBlockState(getPos(), currentState.withProperty(BlockMachine.OPAQUE, isMetaTileEntityOpaque));
        }
    }

    public void scheduleChunkForRenderUpdate() {
        BlockPos pos = getPos();
        getWorld().markBlockRangeForRenderUpdate(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    public void notifyBlockUpdate() {
        getWorld().notifyNeighborsOfStateChange(pos, getBlockType(), false);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("MetaId", NBT.TAG_STRING)) {
            String metaTileEntityIdRaw = compound.getString("MetaId");
            ResourceLocation metaTileEntityId = new ResourceLocation(metaTileEntityIdRaw);
            MetaTileEntity sampleMetaTileEntity = GregTechAPI.MTE_REGISTRY.getObject(metaTileEntityId);
            NBTTagCompound metaTileEntityData = compound.getCompoundTag("MetaTileEntity");
            if (sampleMetaTileEntity != null) {
                setRawMetaTileEntity(sampleMetaTileEntity.createMetaTileEntity(this));
                this.metaTileEntity.onAttached();
                this.metaTileEntity.readFromNBT(metaTileEntityData);
            } else {
                GTLog.logger.error("Failed to load MetaTileEntity with invalid ID " + metaTileEntityIdRaw);
            }
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (metaTileEntity != null) {
            compound.setString("MetaId", metaTileEntity.metaTileEntityId.toString());
            NBTTagCompound metaTileEntityData = new NBTTagCompound();
            metaTileEntity.writeToNBT(metaTileEntityData);
            compound.setTag("MetaTileEntity", metaTileEntityData);
        }
        return compound;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        Object metaTileEntityValue = metaTileEntity == null ? null : metaTileEntity.getCoverCapability(capability, facing);
        return metaTileEntityValue != null || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        T metaTileEntityValue = metaTileEntity == null ? null : metaTileEntity.getCoverCapability(capability, facing);
        return metaTileEntityValue != null ? metaTileEntityValue : super.getCapability(capability, facing);
    }

    @Override
    public void update() {
        long tickTime = System.nanoTime();
        if (metaTileEntity != null) {
            metaTileEntity.update();
        } else if (world.isRemote) { // recover the mte
            NetworkHandler.channel.sendToServer(new CPacketRecoverMTE(world.provider.getDimension(), getPos()).toFMLPacket());
        } else { // remove the block
            if (world.getBlockState(pos).getBlock() instanceof BlockMachine) {
                world.setBlockToAir(pos);
            }
        }
        
        if (this.needToUpdateLightning) {
            getWorld().checkLight(getPos());
            this.needToUpdateLightning = false;
        }

        if (world.isRemote && metaTileEntity != null && getMetaTileEntity().isValid()) {
            tickTime = System.nanoTime() - tickTime;
            if (timeStatistics.length > 0)
                timeStatistics[timeStatisticsIndex = (timeStatisticsIndex + 1) % timeStatistics.length] = (int) tickTime;
            if (tickTime > 100_000_000L && getMetaTileEntity().doTickProfileMessage() && lagWarningCount++ < 10)
                GTLog.logger.warn("WARNING: Possible Lag Source at [" + getPos().getX() + ", " + getPos().getY() + ", " + getPos().getZ() + "] in Dimension " + world.provider.getDimension() + " with " + tickTime + "ns caused by an instance of " + getMetaTileEntity().getClass());
        }

        //increment only after current tick, so meta tile entities will get first tick as timer == 0
        //and update their settings which depend on getTimer() % N properly
        super.update();
    }

    public ArrayList<ITextComponent> getDebugInfo(EntityPlayer player, int logLevel) {
        ArrayList<ITextComponent> list = new ArrayList<>();
        if (logLevel > 2) {
            list.add(new TextComponentTranslation(I18n.format("Meta-ID: " + TextFormatting.BLUE + getMetaTileEntity().metaTileEntityId +TextFormatting.RESET + (isValid() ? TextFormatting.GREEN + " valid" + TextFormatting.RESET : TextFormatting.RED + " invalid" + TextFormatting.RESET) + (metaTileEntity == null ? TextFormatting.RED + " MetaTileEntity == null!" + TextFormatting.RESET : " "))));
        }
        if (logLevel > 1) {
            if (timeStatistics.length > 0) {
                double averageTickTime = 0;
                double worstTickTime = 0;
                for (int tickTime : timeStatistics) {
                    averageTickTime += tickTime;
                    if (tickTime > worstTickTime) {
                        worstTickTime = tickTime;
                    }
                    // Uncomment this line to print out tick-by-tick times.
                    //list.add(new TextComponentTranslation("tickTime " + tickTime));
                }
                list.add(new TextComponentTranslation(I18n.format("Average CPU load of ~" + GTUtility.formatNumbers(averageTickTime / timeStatistics.length) + "ns over " + GTUtility.formatNumbers(timeStatistics.length) + " ticks with worst time of " + GTUtility.formatNumbers(worstTickTime) + "ns.")));
            }
            if (lagWarningCount > 0) {
                list.add(new TextComponentTranslation(I18n.format("Caused " + (lagWarningCount >= 10 ? "more than 10" : lagWarningCount) + " Lag Spike Warnings (anything taking longer than " + 100_000_000L + "ms) on the Server.")));
            }
        }
        if (logLevel > 0 && metaTileEntity != null) {
            IWorkable workable = metaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null);
            if (workable != null)
            list.add(new TextComponentTranslation(I18n.format("Machine is " + (workable.isActive() ? TextFormatting.GREEN + "active" + TextFormatting.RESET : TextFormatting.RED + "inactive" + TextFormatting.RESET))));
        }
        return list;
    }

    public void sendInitialSyncData() {
        writeCustomData(INITIALIZE_MTE, buffer -> {
            buffer.writeVarInt(GregTechAPI.MTE_REGISTRY.getIdByObjectName(metaTileEntity.metaTileEntityId));
            metaTileEntity.writeInitialSyncData(buffer);
        });
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        if (metaTileEntity != null) {
            buf.writeBoolean(true);
            buf.writeVarInt(GregTechAPI.MTE_REGISTRY.getIdByObjectName(metaTileEntity.metaTileEntityId));
            metaTileEntity.writeInitialSyncData(buf);
        } else buf.writeBoolean(false);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        if (buf.readBoolean()) {
            int metaTileEntityId = buf.readVarInt();
            setMetaTileEntity(GregTechAPI.MTE_REGISTRY.getObjectById(metaTileEntityId));
            this.metaTileEntity.receiveInitialSyncData(buf);
            scheduleChunkForRenderUpdate();
            this.needToUpdateLightning = true;
        }
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buffer) {
        if (discriminator == INITIALIZE_MTE) {
            int metaTileEntityId = buffer.readVarInt();
            setMetaTileEntity(GregTechAPI.MTE_REGISTRY.getObjectById(metaTileEntityId));
            this.metaTileEntity.receiveInitialSyncData(buffer);
            scheduleChunkForRenderUpdate();
            this.needToUpdateLightning = true;
        } else if (metaTileEntity != null) {
            metaTileEntity.receiveCustomData(discriminator, buffer);
        }
    }

    @Override
    public boolean isValid() {
        return !super.isInvalid() && metaTileEntity != null;
    }

    @Override
    public boolean isRemote() {
        return getWorld().isRemote;
    }

    @Override
    public void markAsDirty() {
        markDirty();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (metaTileEntity != null) {
            metaTileEntity.onLoad();
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (metaTileEntity != null) {
            metaTileEntity.onUnload();
        }
    }

    @Override
    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock(); //MetaTileEntityHolder should never refresh (until block changes)
    }

    @Override
    public void rotate(@Nonnull Rotation rotationIn) {
        if (metaTileEntity != null) {
            metaTileEntity.setFrontFacing(rotationIn.rotate(metaTileEntity.getFrontFacing()));
        }
    }

    @Override
    public void mirror(@Nonnull Mirror mirrorIn) {
        if (metaTileEntity != null) {
            rotate(mirrorIn.toRotation(metaTileEntity.getFrontFacing()));
        }
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        if (metaTileEntity == null) return false;
        for (EnumFacing side : EnumFacing.VALUES) {
            CoverBehavior cover = metaTileEntity.getCoverAtSide(side);
            if (cover instanceof IFastRenderMetaTileEntity && ((IFastRenderMetaTileEntity) cover).shouldRenderInPass(pass)) {
                return true;
            }
        }
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            return ((IFastRenderMetaTileEntity) metaTileEntity).shouldRenderInPass(pass);
        }
        return false;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            return ((IFastRenderMetaTileEntity) metaTileEntity).getRenderBoundingBox();
        }
        return new AxisAlignedBB(getPos());
    }

    @Override
    public boolean canRenderBreaking() {
        return false;
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    public boolean hasTESR() {
        if (metaTileEntity == null) return false;
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            return true;
        }
        for (EnumFacing side : EnumFacing.VALUES) {
            CoverBehavior cover = metaTileEntity.getCoverAtSide(side);
            if (cover instanceof IFastRenderMetaTileEntity) {
                return true;
            }
        }
        return false;
    }
}
