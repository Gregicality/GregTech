package gregtech.common.metatileentities.storage;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.PhantomFluidWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.custom.QuantumStorageRenderer;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankPropertiesWrapper;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityCreativeTank extends MetaTileEntityQuantumTank {

    private int mBPerCycle = 1;
    private int ticksPerCycle = 1;
    private boolean active = false;

    public MetaTileEntityCreativeTank(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.MAX, -1);
        this.fluidTank = new CreativeFluidTank(1);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.QUANTUM_STORAGE_RENDERER.renderMachine(renderState, translation,
                ArrayUtils.add(pipeline,
                        new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))),
                this);
        Textures.CREATIVE_CONTAINER_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        if (this.getOutputFacing() != null) {
            Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
            if (!isConnected() && active) {
                Textures.FLUID_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
            }
        }
        QuantumStorageRenderer.renderTankFluid(renderState, translation, pipeline, this.fluidTank, getWorld(), getPos(),
                getFrontFacing());
        renderIndicatorOverlay(renderState, translation, pipeline);
    }

    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (this.getWorld() != null && this.fluidTank.getFluid() != null && this.fluidTank.getFluid().amount > 0)
            QuantumStorageRenderer.renderTankAmount(x, y, z, frontFacing, 69);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCreativeTank(this.metaTileEntityId);
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        return GTGuis.createPanel(this, 176, 166)
                .child(IKey.lang("gregtech.creative.tank.fluid").asWidget()
                        .pos(7, 9))
                // todo replace with our fluid slot
                .child(new GTFluidSlot()
                        .syncHandler(GTFluidSlot.sync(this.fluidTank)
                                .phantom(true)
                                .showAmount(true))
                        .pos(36, 6))
                .child(Flow.column()
                        .pos(7, 28)
                        .crossAxisAlignment(Alignment.CrossAxis.START)
                        .coverChildren()
                        .child(IKey.lang("gregtech.creative.tank.mbpc").asWidget()
                                .marginBottom(2))
                        .child(new TextFieldWidget()
                                .left(2)
                                .marginBottom(15)
                                .size(154, 14)
                                .setNumbers(1, Integer.MAX_VALUE)
                                .setMaxLength(11)
                                .value(new IntSyncValue(() -> mBPerCycle, value -> mBPerCycle = value)))
                        .child(IKey.lang("gregtech.creative.tank.tpc").asWidget()
                                .marginBottom(2))
                        .child(new TextFieldWidget()
                                .left(2)
                                .size(154, 14)
                                .setNumbers(1, Integer.MAX_VALUE)
                                .setMaxLength(11)
                                .value(new IntSyncValue(() -> ticksPerCycle, value -> ticksPerCycle = value))))
                .child(new ToggleButton()
                        .disableHoverBackground()
                        .pos(7, 101)
                        .size(162, 20)
                        .overlay(IKey.dynamic(() -> LocalizationUtils.format(
                                active ? "gregtech.creative.activity.on" : "gregtech.creative.activity.off")))
                        .value(new BooleanSyncValue(() -> active, value -> {
                            active = value;
                            scheduleRenderUpdate();
                            var c = getQuantumController();
                            if (c != null) c.updateHandler();
                        })))
                .child(createConnectionButton()
                        .top(6));
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 209)
                .bindPlayerInventory(entityPlayer.inventory, 126);
        builder.widget(new PhantomFluidWidget(36, 6, 18, 18,
                () -> this.fluidTank.getFluid(), data -> {
                    this.fluidTank.setFluid(data);
                }).showTip(false));
        builder.label(7, 9, "gregtech.creative.tank.fluid");
        builder.widget(new ImageWidget(7, 45, 154, 14, GuiTextures.DISPLAY));
        builder.widget(new TextFieldWidget2(9, 47, 152, 10, () -> String.valueOf(mBPerCycle), value -> {
            if (!value.isEmpty()) {
                mBPerCycle = Integer.parseInt(value);
            }
        }).setMaxLength(11).setNumbersOnly(1, Integer.MAX_VALUE));
        builder.label(7, 28, "gregtech.creative.tank.mbpc");

        builder.widget(new ImageWidget(7, 82, 154, 14, GuiTextures.DISPLAY));
        builder.widget(new TextFieldWidget2(9, 84, 152, 10, () -> String.valueOf(ticksPerCycle), value -> {
            if (!value.isEmpty()) {
                ticksPerCycle = Integer.parseInt(value);
            }
        }).setMaxLength(11).setNumbersOnly(1, Integer.MAX_VALUE));
        builder.label(7, 65, "gregtech.creative.tank.tpc");

        builder.widget(new CycleButtonWidget(7, 101, 162, 20, () -> active, value -> {
            active = value;
            scheduleRenderUpdate();
            var c = getQuantumController();
            if (c != null) c.updateHandler();
        }, "gregtech.creative.activity.off", "gregtech.creative.activity.on"));

        builder.widget(createConnectedGui(6));

        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void update() {
        super.update();
        if (ticksPerCycle == 0 || getOffsetTimer() % ticksPerCycle != 0 || fluidTank.getFluid() == null ||
                getWorld().isRemote || !active || isConnected())
            return;

        TileEntity tile = getNeighbor(getOutputFacing());
        if (tile != null) {
            IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                    getOutputFacing().getOpposite());
            if (fluidHandler == null || fluidHandler.getTankProperties().length == 0)
                return;

            fluidHandler.fill(fluidTank.drain(mBPerCycle, false), true);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("mBPerCycle", mBPerCycle);
        data.setInteger("TicksPerCycle", ticksPerCycle);
        data.setBoolean("Active", active);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        mBPerCycle = data.getInteger("mBPerCycle");
        ticksPerCycle = data.getInteger("TicksPerCycle");
        active = data.getBoolean("Active");
        super.readFromNBT(data);
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        mBPerCycle = itemStack.getInteger("mBPerCycle");
        ticksPerCycle = itemStack.getInteger("ticksPerCycle");
    }

    @Override
    public void writeItemStackData(NBTTagCompound itemStack) {
        super.writeItemStackData(itemStack);
        itemStack.setInteger("mBPerCycle", mBPerCycle);
        itemStack.setInteger("ticksPerCycle", ticksPerCycle);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.creative_tooltip.1") + TooltipHelper.RAINBOW +
                I18n.format("gregtech.creative_tooltip.2") + I18n.format("gregtech.creative_tooltip.3"));
        // do not append the normal tooltips
    }

    private class CreativeFluidTank extends FluidTank {

        public CreativeFluidTank(int capacity) {
            super(capacity);
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            if (this.tankProperties == null) {
                this.tankProperties = new IFluidTankProperties[] {
                        new FluidTankPropertiesWrapper(fluidTank) {

                            @Override
                            public int getCapacity() {
                                return mBPerCycle;
                            }

                            @Override
                            public FluidStack getContents() {
                                if (!active) return null;
                                var f = super.getContents();
                                if (f != null) f.amount = mBPerCycle;
                                return f;
                            }

                            @Override
                            public boolean canDrainFluidType(FluidStack fluidStack) {
                                if (!active) return false;
                                return super.canDrainFluidType(fluidStack);
                            }
                        }
                };
            }
            return this.tankProperties;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return drain(resource == null ? 0 : resource.amount, doDrain);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (!active) return null;

            var f = super.drain(maxDrain, false);
            if (f != null) {
                f = f.copy();
                f.amount = Math.min(mBPerCycle, maxDrain);
            }
            return f;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }
    }
}
