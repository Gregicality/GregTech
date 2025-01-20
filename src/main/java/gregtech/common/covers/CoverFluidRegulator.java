package gregtech.common.covers;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.covers.filter.FluidFilterContainer;
import gregtech.common.covers.filter.SimpleFluidFilter;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.logging.log4j.message.FormattedMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class CoverFluidRegulator extends CoverPump {

    protected TransferMode transferMode = TransferMode.TRANSFER_ANY;

    public CoverFluidRegulator(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                               @NotNull EnumFacing attachedSide, int tier, int mbPerTick) {
        super(definition, coverableView, attachedSide, tier, mbPerTick);
        this.fluidFilterContainer = new FluidFilterContainer(this);
    }

    @Override
    protected int doTransferFluidsInternal(IFluidHandler myFluidHandler, IFluidHandler fluidHandler,
                                           int transferLimit) {
        IFluidHandler sourceHandler;
        IFluidHandler destHandler;

        if (pumpMode == PumpMode.IMPORT) {
            sourceHandler = fluidHandler;
            destHandler = myFluidHandler;
        } else if (pumpMode == PumpMode.EXPORT) {
            sourceHandler = myFluidHandler;
            destHandler = fluidHandler;
        } else {
            return 0;
        }
        return switch (transferMode) {
            case TRANSFER_ANY -> GTTransferUtils.transferFluids(sourceHandler, destHandler, transferLimit,
                    fluidFilterContainer::test);
            case KEEP_EXACT -> doKeepExact(transferLimit, sourceHandler, destHandler,
                    fluidFilterContainer::test,
                    this.fluidFilterContainer.getTransferSize());
            case TRANSFER_EXACT -> doTransferExact(transferLimit, sourceHandler, destHandler,
                    fluidFilterContainer::test, this.fluidFilterContainer.getTransferSize());
        };
    }

    protected int doTransferExact(int transferLimit, IFluidHandler sourceHandler, IFluidHandler destHandler,
                                  Predicate<FluidStack> fluidFilter, int supplyAmount) {
        int fluidLeftToTransfer = transferLimit;
        for (IFluidTankProperties tankProperties : sourceHandler.getTankProperties()) {
            FluidStack sourceFluid = tankProperties.getContents();
            if (this.fluidFilterContainer.hasFilter()) {
                supplyAmount = this.fluidFilterContainer.getFilter().getTransferLimit(sourceFluid, supplyAmount);
            }
            if (fluidLeftToTransfer < supplyAmount)
                break;
            if (sourceFluid == null || sourceFluid.amount == 0 || !fluidFilter.test(sourceFluid)) continue;
            sourceFluid.amount = supplyAmount;
            if (GTTransferUtils.transferExactFluidStack(sourceHandler, destHandler, sourceFluid.copy())) {
                fluidLeftToTransfer -= sourceFluid.amount;
            }
            if (fluidLeftToTransfer == 0) break;
        }
        return transferLimit - fluidLeftToTransfer;
    }

    /**
     * Performs one tick worth of Keep Exact behavior.
     *
     * @param transferLimit the maximum amount in milliBuckets that may be transferred in one tick
     * @param sourceHandler source(s) to move fluids from
     * @param destHandler   destination(s) to move fluids to
     * @param fluidFilter   a predicate which determines what fluids may be moved
     * @param keepAmount    the desired amount in milliBuckets of a particular fluid in the destination
     * @return the total amount in milliBuckets of all fluids transferred from source to dest by this method
     */
    protected int doKeepExact(final int transferLimit,
                              final IFluidHandler sourceHandler,
                              final IFluidHandler destHandler,
                              final Predicate<FluidStack> fluidFilter,
                              int keepAmount) {
        if (sourceHandler == null || destHandler == null || fluidFilter == null)
            return 0;

        final Map<FluidStack, Integer> sourceFluids = collectDistinctFluids(sourceHandler,
                IFluidTankProperties::canDrain, fluidFilter);
        final Map<FluidStack, Integer> destFluids = collectDistinctFluids(destHandler, IFluidTankProperties::canFill,
                fluidFilter);

        int transferred = 0;
        for (FluidStack fluidStack : sourceFluids.keySet()) {
            if (transferred >= transferLimit)
                break;

            if (this.fluidFilterContainer.hasFilter()) {
                keepAmount = this.fluidFilterContainer.getFilter().getTransferLimit(fluidStack, keepAmount);
            }

            // if fluid needs to be moved to meet the Keep Exact value
            int amountInDest;
            if ((amountInDest = destFluids.getOrDefault(fluidStack, 0)) < keepAmount) {

                // move the lesser of the remaining transfer limit and the difference in actual vs keep exact amount
                int amountToMove = Math.min(transferLimit - transferred,
                        keepAmount - amountInDest);

                // Nothing to do here, try the next fluid.
                if (amountToMove <= 0)
                    continue;

                // Simulate a drain of this fluid from the source tanks
                FluidStack drainedResult = sourceHandler.drain(copyFluidStackWithAmount(fluidStack, amountToMove),
                        false);

                // Can't drain this fluid. Try the next one.
                if (drainedResult == null || drainedResult.amount <= 0 || !fluidStack.equals(drainedResult))
                    continue;

                // account for the possibility that the drain might give us less than requested
                final int drainable = Math.min(amountToMove, drainedResult.amount);

                // Simulate a fill of the drained amount
                int fillResult = destHandler.fill(copyFluidStackWithAmount(fluidStack, drainable), false);

                // Can't fill, try the next fluid.
                if (fillResult <= 0)
                    continue;

                // This Fluid can be drained and filled, so let's move the most that will actually work.
                int fluidToMove = Math.min(drainable, fillResult);
                FluidStack drainedActual = sourceHandler.drain(copyFluidStackWithAmount(fluidStack, fluidToMove), true);

                // Account for potential error states from the drain
                if (drainedActual == null)
                    throw new RuntimeException(
                            "Misbehaving fluid container: drain produced null after simulation succeeded");

                if (!fluidStack.equals(drainedActual))
                    throw new RuntimeException(
                            "Misbehaving fluid container: drain produced a different fluid than the simulation");

                if (drainedActual.amount != fluidToMove)
                    throw new RuntimeException(new FormattedMessage(
                            "Misbehaving fluid container: drain expected: {}, actual: {}",
                            fluidToMove,
                            drainedActual.amount).getFormattedMessage());

                // Perform Fill
                int filledActual = destHandler.fill(copyFluidStackWithAmount(fluidStack, fluidToMove), true);

                // Account for potential error states from the fill
                if (filledActual != fluidToMove)
                    throw new RuntimeException(new FormattedMessage(
                            "Misbehaving fluid container: fill expected: {}, actual: {}",
                            fluidToMove,
                            filledActual).getFormattedMessage());

                // update the transferred amount
                transferred += fluidToMove;
            }
        }

        return transferred;
    }

    /**
     * Copies a FluidStack and sets its amount to the specified value.
     *
     * @param fs     the original fluid stack to copy
     * @param amount the amount to set the copied FluidStack to
     * @return the copied FluidStack with the specified amount
     */
    private static FluidStack copyFluidStackWithAmount(FluidStack fs, int amount) {
        FluidStack fs2 = fs.copy();
        fs2.amount = amount;
        return fs2;
    }

    private static Map<FluidStack, Integer> collectDistinctFluids(IFluidHandler handler,
                                                                  Predicate<IFluidTankProperties> tankTypeFilter,
                                                                  Predicate<FluidStack> fluidTypeFilter) {
        final Map<FluidStack, Integer> summedFluids = new Object2IntOpenHashMap<>();
        Arrays.stream(handler.getTankProperties())
                .filter(tankTypeFilter)
                .map(IFluidTankProperties::getContents)
                .filter(Objects::nonNull)
                .filter(fluidTypeFilter)
                .forEach(fs -> {
                    summedFluids.putIfAbsent(fs, 0);
                    summedFluids.computeIfPresent(fs, (k, v) -> v + fs.amount);
                });

        return summedFluids;
    }

    public void setTransferMode(TransferMode transferMode) {
        if (this.transferMode != transferMode) {
            this.transferMode = transferMode;
            this.fluidFilterContainer.setMaxTransferSize(getMaxTransferRate());
            this.markDirty();
        }
    }

    public TransferMode getTransferMode() {
        return transferMode;
    }

    private boolean shouldDisplayAmountSlider() {
        if (transferMode == TransferMode.TRANSFER_ANY) {
            return false;
        }
        return fluidFilterContainer.showGlobalTransferLimitSlider();
    }

    @Override
    public ModularPanel confgurePanel(ModularPanel panel, boolean isSmallGui) {
        return panel.height(228);
    }

    @Override
    public @Nullable ParentWidget<?> createUI(GuiData data, PanelSyncManager syncManager) {
        var transferMode = new EnumSyncValue<>(TransferMode.class, this::getTransferMode, this::setTransferMode);
        transferMode.updateCacheFromSource(true);
        syncManager.syncValue("transfer_mode", transferMode);

        var bucketMode = new EnumSyncValue<>(BucketMode.class, this::getBucketMode, this::setBucketMode);
        bucketMode.updateCacheFromSource(true);
        syncManager.syncValue("bucket_mode", bucketMode);

        var filterTransferSize = new StringSyncValue(this::getStringTransferRate, this::setStringTransferRate);
        filterTransferSize.updateCacheFromSource(true);

        return super.createUI(data, syncManager)
                .child(new EnumRowBuilder<>(TransferMode.class)
                        .value(transferMode)
                        .lang("cover.generic.transfer_mode")
                        .overlay(GTGuiTextures.FLUID_TRANSFER_MODE_OVERLAY)
                        .build())
                .child(new EnumRowBuilder<>(BucketMode.class)
                        .value(bucketMode)
                        .overlay(IKey.str("kL"), IKey.str("L"))
                        .build()
                        .child(new TextFieldWidget().widthRel(0.5f).right(0)
                                .setEnabledIf(w -> shouldDisplayAmountSlider())
                                .setNumbers(0, Integer.MAX_VALUE)
                                .value(filterTransferSize)
                                .setTextColor(Color.WHITE.darker(1))));
    }

    @Override
    public int getMaxTransferRate() {
        return switch (this.transferMode) {
            case TRANSFER_ANY -> 1;
            case TRANSFER_EXACT -> maxFluidTransferRate;
            case KEEP_EXACT -> Integer.MAX_VALUE;
        };
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeByte(this.transferMode.ordinal());
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.transferMode = TransferMode.VALUES[packetBuffer.readByte()];
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferMode", transferMode.ordinal());
        tagCompound.setTag("filterv2", new NBTTagCompound());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        this.transferMode = TransferMode.VALUES[tagCompound.getInteger("TransferMode")];
        this.fluidFilterContainer.setMaxTransferSize(this.transferMode.maxStackSize);
        super.readFromNBT(tagCompound);
        // legacy NBT tag
        if (!tagCompound.hasKey("filterv2") && tagCompound.hasKey("TransferAmount")) {
            if (this.fluidFilterContainer.getFilter() instanceof SimpleFluidFilter simpleFluidFilter) {
                simpleFluidFilter
                        .configureFilterTanks(tagCompound.getInteger("TransferAmount"));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected @NotNull TextureAtlasSprite getPlateSprite() {
        return Textures.VOLTAGE_CASINGS[this.tier].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }
}
