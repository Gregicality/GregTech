package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.IControllable;
import gregtech.api.capability.IFuelRodHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.BlockableSlotWidget;
import gregtech.api.items.itemhandlers.LockableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IFissionReactorHatch;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.nuclear.fission.FissionFuelRegistry;
import gregtech.api.nuclear.fission.IFissionFuelStats;
import gregtech.api.nuclear.fission.components.FuelRod;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockFissionCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.FISSION_LOCK_UPDATE;

public class MetaTileEntityFuelRodImportBus extends MetaTileEntityMultiblockNotifiablePart
                                            implements IMultiblockAbilityPart<IFuelRodHandler>, IFuelRodHandler,
                                            IControllable, IFissionReactorHatch {

    private boolean workingEnabled;
    private IFissionFuelStats fuelProperty;
    public MetaTileEntityFuelRodExportBus pairedHatch;
    private IFissionFuelStats partialFuel;
    private FuelRod internalFuelRod;

    public MetaTileEntityFuelRodImportBus(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 4, false);
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.workingEnabled = isWorkingAllowed;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFuelRodImportBus(metaTileEntityId);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new LockableItemStackHandler(this, false);
    }

    private ModularUI.Builder createUITemplate(EntityPlayer player) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 143).label(10, 5, getMetaFullName());

        builder.widget(new BlockableSlotWidget(importItems, 0, 79, 18, true, true)
                .setIsBlocked(this::isLocked).setBackgroundTexture(GuiTextures.SLOT));

        return builder.bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 60);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % 5 == 0) {
            pullItemsFromNearbyHandlers(getFrontFacing());
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer).build(getHolder(), entityPlayer);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.ITEM_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public MultiblockAbility<IFuelRodHandler> getAbility() {
        return MultiblockAbility.IMPORT_FUEL_ROD;
    }

    @Override
    public void registerAbilities(List<IFuelRodHandler> abilityList) {
        abilityList.add(this);
    }

    @Override
    public boolean checkValidity(int depth) {
        this.pairedHatch = getExportHatch(depth);
        return pairedHatch != null;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        getLockedImport().setLock(data.getBoolean("locked"));
        if (data.hasKey("partialFuel")) {
            this.partialFuel = FissionFuelRegistry.getFissionFuel(data.getInteger("partialFuel"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setBoolean("locked", getLockedImport().isLocked());
        if (partialFuel != null) data.setInteger("partialFuel", this.partialFuel.hashCode());
        return super.writeToNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeItemStack(getLockedImport().getStackInSlot(0));
        buf.writeBoolean(getLockedImport().isLocked());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        try {
            getLockedImport().setStackInSlot(0, buf.readItemStack());
        } catch (IOException e) { // ignored
        }
        getLockedImport().setLock(buf.readBoolean());
    }

    private LockableItemStackHandler getLockedImport() {
        return (LockableItemStackHandler) importItems;
    }

    @Override
    public void setLock(boolean isLocked) {
        getLockedImport().setLock(isLocked);
        writeCustomData(FISSION_LOCK_UPDATE, (packetBuffer -> {
            packetBuffer.writeBoolean(isLocked);
        }));
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == FISSION_LOCK_UPDATE) {
            getLockedImport().setLock(buf.readBoolean());
        }
    }

    @Override
    public boolean isLocked() {
        return getLockedImport().isLocked();
    }

    @Override
    public ItemStack getLockedObject() {
        return getLockedImport().getLockedObject();
    }

    @Override
    public IFissionFuelStats getFuel() {
        return this.fuelProperty;
    }

    @Override
    public void setFuel(IFissionFuelStats prop) {
        this.fuelProperty = prop;
    }

    @Override
    public IFissionFuelStats getPartialFuel() {
        return this.partialFuel;
    }

    @Override
    public boolean setPartialFuel(IFissionFuelStats prop) {
        if (prop == this.partialFuel) {
            return false;
        }
        this.partialFuel = prop;
        if (this.internalFuelRod != null) {
            this.internalFuelRod.setFuel(prop);
        }
        return true;
    }

    @Override
    public void setInternalFuelRod(FuelRod rod) {
        this.internalFuelRod = rod;
    }

    @Override
    public LockableItemStackHandler getStackHandler() {
        return this.getLockedImport();
    }

    public MetaTileEntityFuelRodExportBus getExportHatch(int depth) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        for (int i = 1; i < depth; i++) {
            if (getWorld().getBlockState(pos.move(this.frontFacing.getOpposite())) !=
                    MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.FUEL_CHANNEL)) {
                return null;
            }
        }
        if (getWorld().getTileEntity(pos.move(this.frontFacing.getOpposite())) instanceof IGregTechTileEntity gtTe) {
            MetaTileEntity mte = gtTe.getMetaTileEntity();
            if (mte instanceof MetaTileEntityFuelRodExportBus) {
                return (MetaTileEntityFuelRodExportBus) mte;
            }
        }
        return null;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.nuclear.locking.item"));
    }
}
