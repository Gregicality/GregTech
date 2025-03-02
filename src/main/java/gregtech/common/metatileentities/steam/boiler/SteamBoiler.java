package gregtech.common.metatileentities.steam.boiler;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.FluidContainerSlotWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.ConfigHolder;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gregtech.api.capability.GregtechDataCodes.IS_WORKING;

public abstract class SteamBoiler extends MetaTileEntity implements IDataInfoProvider {

    private static final Pattern STRING_SUBSTITUTION_PATTERN = Pattern.compile("%s", Pattern.LITERAL);

    private static final EnumFacing[] STEAM_PUSH_DIRECTIONS = ArrayUtils.add(EnumFacing.HORIZONTALS, EnumFacing.UP);

    public final TextureArea bronzeSlotBackgroundTexture;

    public final TextureArea slotFurnaceBackground;

    protected final boolean isHighPressure;
    private final ICubeRenderer renderer;

    protected FluidTank waterFluidTank;
    protected FluidTank steamFluidTank;

    private int fuelBurnTimeLeft;
    private int fuelMaxBurnTime;
    private int currentTemperature;
    private boolean hasNoWater;
    private int timeBeforeCoolingDown;

    private boolean isBurning;
    private boolean wasBurningAndNeedsUpdate;
    private final ItemStackHandler containerInventory;

    public SteamBoiler(ResourceLocation metaTileEntityId, boolean isHighPressure, ICubeRenderer renderer) {
        super(metaTileEntityId);
        this.renderer = renderer;
        this.isHighPressure = isHighPressure;
        this.bronzeSlotBackgroundTexture = getGuiTexture("slot_%s");
        this.slotFurnaceBackground = getGuiTexture("slot_%s_furnace_background");
        this.containerInventory = new GTItemStackHandler(this, 2);
    }

    @Override
    public boolean isActive() {
        return isBurning;
    }

    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        if (isHighPressure) {
            return Textures.STEAM_BRICKED_CASING_STEEL;
        } else {
            return Textures.STEAM_BRICKED_CASING_BRONZE;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseRenderer().getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        getBaseRenderer().render(renderState, translation, colouredPipeline);
        renderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isBurning(), true);
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("FuelBurnTimeLeft", fuelBurnTimeLeft);
        data.setInteger("FuelMaxBurnTime", fuelMaxBurnTime);
        data.setInteger("CurrentTemperature", currentTemperature);
        data.setBoolean("HasNoWater", hasNoWater);
        data.setTag("ContainerInventory", containerInventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.fuelBurnTimeLeft = data.getInteger("FuelBurnTimeLeft");
        this.fuelMaxBurnTime = data.getInteger("FuelMaxBurnTime");
        this.currentTemperature = data.getInteger("CurrentTemperature");
        this.hasNoWater = data.getBoolean("HasNoWater");
        this.containerInventory.deserializeNBT(data.getCompoundTag("ContainerInventory"));
        this.isBurning = fuelBurnTimeLeft > 0;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isBurning);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isBurning = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == IS_WORKING) {
            this.isBurning = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    public void setFuelMaxBurnTime(int fuelMaxBurnTime) {
        this.fuelMaxBurnTime = fuelMaxBurnTime;
        this.fuelBurnTimeLeft = fuelMaxBurnTime;
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            updateCurrentTemperature();
            if (getOffsetTimer() % 10 == 0) {
                generateSteam();
            }

            GTTransferUtils.fillInternalTankFromFluidContainer(importFluids, containerInventory, 0, 1);

            if (getOffsetTimer() % 5 == 0) {
                pushFluidsIntoNearbyHandlers(STEAM_PUSH_DIRECTIONS);
            }

            if (fuelMaxBurnTime <= 0) {
                tryConsumeNewFuel();
                if (fuelBurnTimeLeft > 0) {
                    if (wasBurningAndNeedsUpdate) {
                        this.wasBurningAndNeedsUpdate = false;
                    } else setBurning(true);
                }
            }

            if (wasBurningAndNeedsUpdate) {
                this.wasBurningAndNeedsUpdate = false;
                setBurning(false);
            }
        }
    }

    private void updateCurrentTemperature() {
        if (fuelMaxBurnTime > 0) {
            if (getOffsetTimer() % 12 == 0) {
                if (currentTemperature < getMaxTemperate())
                    currentTemperature++;
                fuelBurnTimeLeft -= isHighPressure ? 2 : 1;
                if (fuelBurnTimeLeft <= 0) {
                    this.fuelMaxBurnTime = 0;
                    this.timeBeforeCoolingDown = getCooldownInterval();
                    // boiler has no fuel now, so queue burning state update
                    this.wasBurningAndNeedsUpdate = true;
                }
            }
        } else if (timeBeforeCoolingDown == 0) {
            if (currentTemperature > 0) {
                currentTemperature -= getCoolDownRate();
                timeBeforeCoolingDown = getCooldownInterval();
            }
        } else--timeBeforeCoolingDown;
    }

    protected abstract int getBaseSteamOutput();

    /** Returns the current total steam output every 10 ticks. */
    public int getTotalSteamOutput() {
        if (currentTemperature < 100) return 0;
        return (int) (getBaseSteamOutput() * (currentTemperature / (getMaxTemperate() * 1.0)) / 2);
    }

    public boolean hasWater() {
        return !hasNoWater;
    }

    private void generateSteam() {
        if (currentTemperature >= 100) {
            int fillAmount = getTotalSteamOutput();
            boolean hasDrainedWater = waterFluidTank.drain(1, true) != null;
            int filledSteam = 0;
            if (hasDrainedWater) {
                filledSteam = steamFluidTank.fill(Materials.Steam.getFluid(fillAmount), true);
            }
            if (this.hasNoWater && hasDrainedWater) {
                doExplosion(2.0f);
            } else this.hasNoWater = !hasDrainedWater;
            if (filledSteam == 0 && hasDrainedWater) {
                final float x = getPos().getX() + 0.5F;
                final float y = getPos().getY() + 0.5F;
                final float z = getPos().getZ() + 0.5F;

                ((WorldServer) getWorld()).spawnParticle(EnumParticleTypes.CLOUD,
                        x + getFrontFacing().getXOffset() * 0.6,
                        y + getFrontFacing().getYOffset() * 0.6,
                        z + getFrontFacing().getZOffset() * 0.6,
                        7 + GTValues.RNG.nextInt(3),
                        getFrontFacing().getXOffset() / 2.0,
                        getFrontFacing().getYOffset() / 2.0,
                        getFrontFacing().getZOffset() / 2.0, 0.1);

                if (ConfigHolder.machines.machineSounds && !this.isMuffled()) {
                    getWorld().playSound(null, x, y, z, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0f,
                            1.0f);
                }

                steamFluidTank.drain(4000, true);
            }
        } else {
            this.hasNoWater = waterFluidTank.getFluidAmount() == 0;
        }
    }

    public boolean isBurning() {
        return isBurning;
    }

    public void setBurning(boolean burning) {
        this.isBurning = burning;
        if (!getWorld().isRemote) {
            markDirty();
            writeCustomData(IS_WORKING, buf -> buf.writeBoolean(burning));
        }
    }

    protected abstract void tryConsumeNewFuel();

    protected abstract int getCooldownInterval();

    protected abstract int getCoolDownRate();

    public int getMaxTemperate() {
        return isHighPressure ? 1000 : 500;
    }

    public double getTemperaturePercent() {
        return currentTemperature / (getMaxTemperate() * 1.0);
    }

    public int getCurrentTemperature() {
        return currentTemperature;
    }

    public double getFuelLeftPercent() {
        return fuelMaxBurnTime == 0 ? 0.0 : fuelBurnTimeLeft / (fuelMaxBurnTime * 1.0);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        this.waterFluidTank = new FilteredFluidHandler(16000).setFilter(CommonFluidFilters.BOILER_FLUID);
        return new FluidTankList(false, waterFluidTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        this.steamFluidTank = new FluidTank(16000);
        return new FluidTankList(false, steamFluidTank);
    }

    protected TextureArea getGuiTexture(String pathTemplate) {
        String type = isHighPressure ? "steel" : "bronze";
        return TextureArea.fullImage(String.format("textures/gui/steam/%s/%s.png",
                type, STRING_SUBSTITUTION_PATTERN.matcher(pathTemplate).replaceAll(Matcher.quoteReplacement(type))));
    }

    public ModularUI.Builder createUITemplate(EntityPlayer player) {
        return ModularUI.builder(GuiTextures.BACKGROUND_STEAM.get(isHighPressure), 176, 166)
                .label(6, 6, getMetaFullName()).shouldColor(false)
                .widget(new ProgressWidget(this::getTemperaturePercent, 96, 26, 10, 54)
                        .setProgressBar(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(isHighPressure),
                                GuiTextures.PROGRESS_BAR_BOILER_HEAT,
                                MoveType.VERTICAL))

                .widget(new TankWidget(waterFluidTank, 83, 26, 10, 54)
                        .setBackgroundTexture(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(isHighPressure)))
                .widget(new TankWidget(steamFluidTank, 70, 26, 10, 54)
                        .setBackgroundTexture(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(isHighPressure)))

                .widget(new FluidContainerSlotWidget(containerInventory, 0, 43, 26, true)
                        .setBackgroundTexture(GuiTextures.SLOT_STEAM.get(isHighPressure),
                                GuiTextures.IN_SLOT_OVERLAY_STEAM.get(isHighPressure)))
                .slot(containerInventory, 1, 43, 62, true, false,
                        GuiTextures.SLOT_STEAM.get(isHighPressure),
                        GuiTextures.OUT_SLOT_OVERLAY_STEAM.get(isHighPressure))
                .image(43, 44, 18, 18, GuiTextures.CANISTER_OVERLAY_STEAM.get(isHighPressure))

                .bindPlayerInventory(player.inventory, GuiTextures.SLOT_STEAM.get(isHighPressure), 0);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(String.format("%s %s",
                I18n.format("gregtech.universal.tooltip.produces_fluid", getBaseSteamOutput() / 20),
                Materials.Steam.getLocalizedName()));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.BOILER;
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, containerInventory);
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        return Collections.singletonList(new TextComponentTranslation("gregtech.machine.steam_boiler.heat_amount",
                TextFormattingUtil.formatNumbers((int) (this.getTemperaturePercent() * 100))));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick() {
        if (this.isActive()) {
            EnumParticleTypes smokeParticle = isHighPressure ? EnumParticleTypes.SMOKE_LARGE :
                    EnumParticleTypes.SMOKE_NORMAL;
            VanillaParticleEffects.defaultFrontEffect(this, smokeParticle, EnumParticleTypes.FLAME);

            if (ConfigHolder.machines.machineSounds && GTValues.RNG.nextDouble() < 0.1) {
                BlockPos pos = getPos();
                getWorld().playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                        SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }
}
