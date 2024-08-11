package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IPassthroughHatch;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Grid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetaTileEntityPassthroughHatchFluid extends MetaTileEntityMultiblockPart implements IPassthroughHatch,
                                                 IMultiblockAbilityPart<IPassthroughHatch>,
                                                 IControllable {

    private static final int TANK_SIZE = 16_000;

    private FluidTankList fluidTankList;

    private IFluidHandler importHandler;
    private IFluidHandler exportHandler;

    private boolean workingEnabled;

    public MetaTileEntityPassthroughHatchFluid(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.workingEnabled = true;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPassthroughHatchFluid(metaTileEntityId, getTier());
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        FilteredFluidHandler[] fluidHandlers = new FilteredFluidHandler[getTier() + 1];
        for (int i = 0; i < fluidHandlers.length; i++) {
            fluidHandlers[i] = new FilteredFluidHandler(TANK_SIZE);
        }
        fluidInventory = fluidTankList = new FluidTankList(false, fluidHandlers);
        importHandler = new FluidHandlerProxy(fluidTankList, new FluidTankList(false));
        exportHandler = new FluidHandlerProxy(new FluidTankList(false), fluidTankList);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % 5 == 0) {
            if (workingEnabled) {
                pushFluidsIntoNearbyHandlers(getFrontFacing().getOpposite()); // outputs to back
                pullFluidsFromNearbyHandlers(getFrontFacing()); // inputs from front
            }
        }
    }

    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }

    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            // front side input
            Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.FLUID_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);

            // back side output
            Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing().getOpposite(), renderState, translation, pipeline);
            Textures.FLUID_HATCH_OUTPUT_OVERLAY.renderSided(getFrontFacing().getOpposite(), renderState, translation,
                    pipeline);
        }
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, TANK_SIZE, getController(), true);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, TANK_SIZE, getController(), false);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        int rowSize = (int) Math.sqrt(getTier() + 1);

        int backgroundWidth = 9 * 18 + 14;
        int backgroundHeight = 18 + 18 * rowSize + 94;

        List<List<IWidget>> widgets = new ArrayList<>();
        for (int i = 0; i < rowSize; i++) {
            widgets.add(new ArrayList<>());
            for (int j = 0; j < rowSize; j++) {
                widgets.get(i).add(new GTFluidSlot().syncHandler(fluidTankList.getTankAt(i * rowSize + j))
                        .background(GTGuiTextures.FLUID_SLOT));
            }
        }

        BooleanSyncValue workingStateValue = new BooleanSyncValue(() -> workingEnabled, val -> workingEnabled = val);
        return GTGuis.createPanel(this, backgroundWidth, backgroundHeight)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory().left(7).bottom(7))
                .child(new Grid()
                        .top(18).height(rowSize * 18)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .alignX(0.5f)
                        .matrix(widgets))
                .child(new ToggleButton()
                        .top(18 * 2).left(18 * 8 + 7)
                        .value(workingStateValue)
                        .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                        .tooltip(t -> t.setAutoUpdate(true))
                        .tooltipBuilder(t -> t.addLine(workingStateValue.getBoolValue() ?
                                IKey.lang("gregtech.gui.fluid_passthrough.enabled") :
                                IKey.lang("gregtech.gui.fluid_passthrough.disabled"))));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("FluidInventory", fluidTankList.serializeNBT());
        tag.setBoolean("WorkingEnabled", workingEnabled);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.fluidTankList.deserializeNBT(tag.getCompoundTag("FluidInventory"));
        // Passthrough hatches before this change won't have WorkingEnabled at all, so we need to check if it exists
        if (tag.hasKey("WorkingEnabled")) {
            this.workingEnabled = tag.getBoolean("WorkingEnabled");
        }
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity_mult", getTier() + 1, TANK_SIZE));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public MultiblockAbility<IPassthroughHatch> getAbility() {
        return MultiblockAbility.PASSTHROUGH_HATCH;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    @NotNull
    @Override
    public Class<IFluidHandler> getPassthroughType() {
        return IFluidHandler.class;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        // enforce strict sided-ness for fluid IO
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (side == getFrontFacing()) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(importHandler);
            } else if (side == getFrontFacing().getOpposite()) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(exportHandler);
            } else return null;
        } else if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);

        buf.writeBoolean(workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);

        this.workingEnabled = buf.readBoolean();
    }
}
