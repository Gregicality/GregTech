package gregtech.common.metatileentities.steam;

import gregtech.api.capability.*;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.capability.impl.miner.MinerLogic;
import gregtech.api.capability.impl.miner.SteamMinerLogic;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.ConfigHolder;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

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

public class SteamMiner extends MetaTileEntity implements IMiner, IControllable, IVentable, IDataInfoProvider {

    private boolean needsVenting = false;
    private boolean ventingStuck = false;

    private final int inventorySize;
    private final int energyPerTick;
    private boolean isInventoryFull = false;

    private final MinerLogic minerLogic;

    public SteamMiner(ResourceLocation metaTileEntityId, int speed, int maximumRadius, int fortune) {
        super(metaTileEntityId);
        this.inventorySize = 4;
        this.energyPerTick = 16;
        this.minerLogic = new SteamMinerLogic(this, fortune, speed, maximumRadius, Textures.BRONZE_PLATED_BRICKS);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SteamMiner(metaTileEntityId, this.minerLogic.getSpeed(), this.minerLogic.getMaximumRadius(),
                this.minerLogic.getFortune());
    }

    @Override
    public FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, new FilteredFluidHandler(16000).setFilter(CommonFluidFilters.STEAM));
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 0, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, inventorySize, this, true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        ColourMultiplier multiplier = new ColourMultiplier(
                GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
        IVertexOperation[] coloredPipeline = ArrayUtils.add(pipeline, multiplier);
        Textures.STEAM_CASING_BRONZE.render(renderState, translation, coloredPipeline);
        for (EnumFacing renderSide : EnumFacing.HORIZONTALS) {
            if (renderSide == getFrontFacing()) {
                Textures.PIPE_OUT_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
            } else
                Textures.STEAM_MINER_OVERLAY.renderSided(renderSide, renderState, translation, coloredPipeline);
        }
        Textures.STEAM_VENT_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        Textures.PIPE_IN_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        minerLogic.renderPipe(renderState, translation, pipeline);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND_STEAM.get(false), 175, 176);
        builder.bindPlayerInventory(entityPlayer.inventory, 94);

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(exportItems, index, 142 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false)));
            }
        }
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT_STEAM.get(false), 10);

        builder.image(7, 16, 105, 75, GuiTextures.DISPLAY_STEAM.get(false))
                .label(6, 6, getMetaFullName());
        builder.widget(new AdvancedTextWidget(10, 19, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(84));
        builder.widget(new AdvancedTextWidget(70, 19, this::addDisplayText2, 0xFFFFFF)
                .setMaxWidthLimit(84));

        return builder.build(getHolder(), entityPlayer);
    }

    void addDisplayText(List<ITextComponent> textList) {
        int workingArea = getWorkingArea(minerLogic.getCurrentRadius());
        textList.add(new TextComponentTranslation("gregtech.machine.miner.startx", this.minerLogic.getX().get()));
        textList.add(new TextComponentTranslation("gregtech.machine.miner.starty", this.minerLogic.getY().get()));
        textList.add(new TextComponentTranslation("gregtech.machine.miner.startz", this.minerLogic.getZ().get()));
        textList.add(new TextComponentTranslation("gregtech.machine.miner.working_area", workingArea, workingArea));
        if (this.minerLogic.isDone())
            textList.add(new TextComponentTranslation("gregtech.machine.miner.done")
                    .setStyle(new Style().setColor(TextFormatting.GREEN)));
        else if (this.minerLogic.isWorking())
            textList.add(new TextComponentTranslation("gregtech.machine.miner.working")
                    .setStyle(new Style().setColor(TextFormatting.GOLD)));
        else if (!this.isWorkingEnabled())
            textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));
        if (this.isInventoryFull)
            textList.add(new TextComponentTranslation("gregtech.machine.miner.invfull")
                    .setStyle(new Style().setColor(TextFormatting.RED)));
        if (ventingStuck)
            textList.add(new TextComponentTranslation("gregtech.machine.steam_miner.vent")
                    .setStyle(new Style().setColor(TextFormatting.RED)));
        else if (!drainEnergy(true))
            textList.add(new TextComponentTranslation("gregtech.machine.steam_miner.steam")
                    .setStyle(new Style().setColor(TextFormatting.RED)));
    }

    void addDisplayText2(List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation("gregtech.machine.miner.minex", this.minerLogic.getMineX().get()));
        textList.add(new TextComponentTranslation("gregtech.machine.miner.miney", this.minerLogic.getMineY().get()));
        textList.add(new TextComponentTranslation("gregtech.machine.miner.minez", this.minerLogic.getMineZ().get()));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.uses_per_tick_steam", energyPerTick) + TextFormatting.GRAY +
                ", " + I18n.format("gregtech.machine.miner.per_block", this.minerLogic.getSpeed() / 20));
        int maxArea = getWorkingArea(minerLogic.getMaximumRadius());
        tooltip.add(I18n.format("gregtech.universal.tooltip.working_area", maxArea, maxArea));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.reset"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    public boolean drainEnergy(boolean simulate) {
        int resultSteam = importFluids.getTankAt(0).getFluidAmount() - energyPerTick;
        if (!ventingStuck && resultSteam >= 0L && resultSteam <= importFluids.getTankAt(0).getCapacity()) {
            if (!simulate)
                importFluids.getTankAt(0).drain(energyPerTick, true);
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        super.update();
        this.minerLogic.performMining();
        if (!getWorld().isRemote) {
            if (getOffsetTimer() % 5 == 0)
                pushItemsIntoNearbyHandlers(getFrontFacing());

            if (this.minerLogic.wasActiveAndNeedsUpdate()) {
                this.minerLogic.setWasActiveAndNeedsUpdate(false);
                this.minerLogic.setActive(false);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("needsVenting", this.needsVenting);
        data.setBoolean("ventingStuck", this.ventingStuck);
        return this.minerLogic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.needsVenting = data.getBoolean("needsVenting");
        this.ventingStuck = data.getBoolean("ventingStuck");
        this.minerLogic.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.needsVenting);
        buf.writeBoolean(this.ventingStuck);
        this.minerLogic.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.needsVenting = buf.readBoolean();
        this.ventingStuck = buf.readBoolean();
        this.minerLogic.receiveInitialSyncData(buf);
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.NEEDS_VENTING) {
            this.needsVenting = buf.readBoolean();
        } else if (dataId == GregtechDataCodes.VENTING_STUCK) {
            this.ventingStuck = buf.readBoolean();
        }
        this.minerLogic.receiveCustomData(dataId, buf);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.STEAM_CASING_BRONZE.getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.TOP),
                getPaintingColorForRendering());
    }

    public void setVentingStuck(boolean ventingStuck) {
        this.ventingStuck = ventingStuck;
        if (!this.getWorld().isRemote) {
            this.markDirty();
            this.writeCustomData(GregtechDataCodes.VENTING_STUCK, (buf) -> buf.writeBoolean(ventingStuck));
        }
    }

    @Override
    public void setNeedsVenting(boolean needsVenting) {
        this.needsVenting = needsVenting;
        if (!needsVenting && this.ventingStuck)
            this.setVentingStuck(false);

        if (!this.getWorld().isRemote) {
            this.markDirty();
            this.writeCustomData(GregtechDataCodes.NEEDS_VENTING, (buf) -> buf.writeBoolean(needsVenting));
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isInventoryFull() {
        return isInventoryFull;
    }

    @Override
    public void setInventoryFull(boolean isFull) {
        this.isInventoryFull = isFull;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.minerLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.minerLogic.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public boolean isNeedsVenting() {
        return this.needsVenting;
    }

    @Override
    public void tryDoVenting() {
        BlockPos machinePos = this.getPos();
        EnumFacing ventingSide = EnumFacing.UP;
        BlockPos ventingBlockPos = machinePos.offset(ventingSide);
        IBlockState blockOnPos = this.getWorld().getBlockState(ventingBlockPos);
        if (blockOnPos.getCollisionBoundingBox(this.getWorld(), ventingBlockPos) == Block.NULL_AABB) {
            this.getWorld()
                    .getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(ventingBlockPos),
                            EntitySelectors.CAN_AI_TARGET)
                    .forEach((entity) -> entity.attackEntityFrom(DamageSources.getHeatDamage(), 6.0F));
            WorldServer world = (WorldServer) this.getWorld();
            double posX = (double) machinePos.getX() + 0.5D + (double) ventingSide.getXOffset() * 0.6D;
            double posY = (double) machinePos.getY() + 0.5D + (double) ventingSide.getYOffset() * 0.6D;
            double posZ = (double) machinePos.getZ() + 0.5D + (double) ventingSide.getZOffset() * 0.6D;
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ, 7 + world.rand.nextInt(3),
                    (double) ventingSide.getXOffset() / 2.0D, (double) ventingSide.getYOffset() / 2.0D,
                    (double) ventingSide.getZOffset() / 2.0D, 0.1D);
            if (ConfigHolder.machines.machineSounds && !this.isMuffled()) {
                world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F,
                        1.0F);
            }
            this.setNeedsVenting(false);
        } else if (!this.ventingStuck) {
            this.setVentingStuck(true);
        }
    }

    @Override
    public boolean isVentingStuck() {
        return ventingStuck;
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        int workingArea = getWorkingArea(this.minerLogic.getCurrentRadius());
        return Collections.singletonList(
                new TextComponentTranslation("gregtech.machine.miner.working_area", workingArea, workingArea));
    }
}
