package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.ICleanroomFilter;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMufflerHatch;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.CleanroomLogic;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleGeneratorMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.PatternStringError;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.Mods;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockCleanroomCasing;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.MetaTileEntityCokeOven;
import gregtech.common.metatileentities.multi.MetaTileEntityPrimitiveBlastFurnace;
import gregtech.common.metatileentities.multi.MetaTileEntityPrimitiveWaterPump;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityCentralMonitor;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class MetaTileEntityCleanroom extends MultiblockWithDisplayBase
                                     implements ICleanroomProvider, IWorkable, IDataInfoProvider {

    public static final int CLEAN_AMOUNT_THRESHOLD = 90;
    public static final int MIN_CLEAN_AMOUNT = 0;

    public static final int MIN_RADIUS = 2;
    public static final int MIN_DEPTH = 4;

    private int lDist = 0;
    private int rDist = 0;
    private int bDist = 0;
    private int fDist = 0;
    private int hDist = 0;

    private CleanroomType cleanroomType = null;
    private int cleanAmount;

    private IEnergyContainer energyContainer;

    private ICleanroomFilter cleanroomFilter;
    private final CleanroomLogic cleanroomLogic;
    private final Collection<ICleanroomReceiver> cleanroomReceivers = new HashSet<>();

    public MetaTileEntityCleanroom(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.cleanroomLogic = new CleanroomLogic(this, GTValues.LV);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCleanroom(metaTileEntityId);
    }

    protected void initializeAbilities() {
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
    }

    private void resetTileAbilities() {
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
        this.cleanroomFilter = context.get("FilterType");
        this.cleanroomType = cleanroomFilter.getCleanroomType();

        // max progress is based on the dimensions of the structure: (x^3)-(x^2)
        // taller cleanrooms take longer than wider ones
        // minimum of 100 is a 5x5x5 cleanroom: 125-25=100 ticks
        this.cleanroomLogic.setMaxProgress(Math.max(100,
                ((lDist + rDist + 1) * (bDist + fDist + 1) * hDist) - ((lDist + rDist + 1) * (bDist + fDist + 1))));
        this.cleanroomLogic.setMinEnergyTier(cleanroomFilter.getMinTier());
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        this.cleanroomLogic.invalidate();
        this.cleanAmount = MIN_CLEAN_AMOUNT;
        cleanroomReceivers.forEach(receiver -> {
            if (receiver.getCleanroom() == this) {
                receiver.unsetCleanroom();
            }
        });
        cleanroomReceivers.clear();
    }

    @Override
    protected void updateFormedValid() {
        if (!getWorld().isRemote) {
            this.cleanroomLogic.updateLogic();
            if (this.cleanroomLogic.wasActiveAndNeedsUpdate()) {
                this.cleanroomLogic.setWasActiveAndNeedsUpdate(false);
                this.cleanroomLogic.setActive(false);
            }
        }
    }

    @Override
    public void checkStructurePattern() {
        if (!this.isStructureFormed()) {
            reinitializeStructurePattern();
        }
        super.checkStructurePattern();
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    public boolean allowsFlip() {
        return false;
    }

    /**
     * Scans for blocks around the controller to update the dimensions
     */
    public boolean updateStructureDimensions() {
        World world = getWorld();
        EnumFacing front = getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing left = front.rotateYCCW();
        EnumFacing right = left.getOpposite();

        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos fPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos bPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos hPos = new BlockPos.MutableBlockPos(getPos());

        // find the distances from the controller to the plascrete blocks on one horizontal axis and the Y axis
        // repeatable aisles take care of the second horizontal axis
        int lDist = 0;
        int rDist = 0;
        int bDist = 0;
        int fDist = 0;
        int hDist = 0;

        // find the left, right, back, and front distances for the structure pattern
        // maximum size is 15x15x15 including walls, so check 7 block radius around the controller for blocks
        for (int i = 1; i < 8; i++) {
            if (lDist == 0 && isBlockEdge(world, lPos, left)) lDist = i;
            if (rDist == 0 && isBlockEdge(world, rPos, right)) rDist = i;
            if (bDist == 0 && isBlockEdge(world, bPos, back)) bDist = i;
            if (fDist == 0 && isBlockEdge(world, fPos, front)) fDist = i;
            if (lDist != 0 && rDist != 0 && bDist != 0 && fDist != 0) break;
        }

        // height is diameter instead of radius, so it needs to be done separately
        for (int i = 1; i < 15; i++) {
            if (isBlockFloor(world, hPos, EnumFacing.DOWN)) hDist = i;
            if (hDist != 0) break;
        }

        if (lDist < MIN_RADIUS || rDist < MIN_RADIUS || bDist < MIN_RADIUS || fDist < MIN_RADIUS || hDist < MIN_DEPTH) {
            invalidateStructure();
            return false;
        }

        this.lDist = lDist;
        this.rDist = rDist;
        this.bDist = bDist;
        this.fDist = fDist;
        this.hDist = hDist;

        writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, buf -> {
            buf.writeInt(this.lDist);
            buf.writeInt(this.rDist);
            buf.writeInt(this.bDist);
            buf.writeInt(this.fDist);
            buf.writeInt(this.hDist);
        });
        return true;
    }

    /**
     * @param world     the world to check
     * @param pos       the pos to check and move
     * @param direction the direction to move
     * @return if a block is a valid wall block at pos moved in direction
     */
    public boolean isBlockEdge(@NotNull World world, @NotNull BlockPos.MutableBlockPos pos,
                               @NotNull EnumFacing direction) {
        return world.getBlockState(pos.move(direction)) ==
                MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE);
    }

    /**
     * @param world     the world to check
     * @param pos       the pos to check and move
     * @param direction the direction to move
     * @return if a block is a valid floor block at pos moved in direction
     */
    public boolean isBlockFloor(@NotNull World world, @NotNull BlockPos.MutableBlockPos pos,
                                @NotNull EnumFacing direction) {
        return isBlockEdge(world, pos, direction) || world.getBlockState(pos) ==
                MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.CLEANROOM_GLASS);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        // return the default structure, even if there is no valid size found
        // this means auto-build will still work, and prevents terminal crashes.
        if (getWorld() != null) updateStructureDimensions();

        // these can sometimes get set to 0 when loading the game, breaking JEI
        if (lDist < MIN_RADIUS) lDist = MIN_RADIUS;
        if (rDist < MIN_RADIUS) rDist = MIN_RADIUS;
        if (bDist < MIN_RADIUS) bDist = MIN_RADIUS;
        if (fDist < MIN_RADIUS) fDist = MIN_RADIUS;
        if (hDist < MIN_DEPTH) hDist = MIN_DEPTH;

        if (this.frontFacing == EnumFacing.EAST || this.frontFacing == EnumFacing.WEST) {
            int tmp = lDist;
            lDist = rDist;
            rDist = tmp;
        }

        // build each row of the structure
        StringBuilder borderBuilder = new StringBuilder();     // BBBBB
        StringBuilder wallBuilder = new StringBuilder();       // BXXXB
        StringBuilder insideBuilder = new StringBuilder();     // X X
        StringBuilder roofBuilder = new StringBuilder();       // BFFFB
        StringBuilder controllerBuilder = new StringBuilder(); // BFSFB
        StringBuilder centerBuilder = new StringBuilder();     // BXKXB

        // everything to the left of the controller
        for (int i = 0; i < lDist; i++) {
            borderBuilder.append("B");
            if (i == 0) {
                wallBuilder.append("B");
                insideBuilder.append("X");
                roofBuilder.append("B");
                controllerBuilder.append("B");
                centerBuilder.append("B");
            } else {
                insideBuilder.append(" ");
                wallBuilder.append("X");
                roofBuilder.append("F");
                controllerBuilder.append("F");
                centerBuilder.append("X");
            }
        }

        // everything in-line with the controller
        borderBuilder.append("B");
        wallBuilder.append("X");
        insideBuilder.append(" ");
        roofBuilder.append("F");
        controllerBuilder.append("S");
        centerBuilder.append("K");

        // everything to the right of the controller
        for (int i = 0; i < rDist; i++) {
            borderBuilder.append("B");
            if (i == rDist - 1) {
                wallBuilder.append("B");
                insideBuilder.append("X");
                roofBuilder.append("B");
                controllerBuilder.append("B");
                centerBuilder.append("B");
            } else {
                insideBuilder.append(" ");
                wallBuilder.append("X");
                roofBuilder.append("F");
                controllerBuilder.append("F");
                centerBuilder.append("X");
            }
        }

        // build each slice of the structure
        String[] wall = new String[hDist + 1]; // "BBBBB", "BXXXB", "BXXXB", "BXXXB", "BBBBB"
        Arrays.fill(wall, wallBuilder.toString());
        wall[0] = borderBuilder.toString();
        wall[wall.length - 1] = borderBuilder.toString();

        String[] slice = new String[hDist + 1]; // "BXXXB", "X X", "X X", "X X", "BFFFB"
        Arrays.fill(slice, insideBuilder.toString());
        slice[0] = wallBuilder.toString();
        slice[slice.length - 1] = roofBuilder.toString();

        String[] center = Arrays.copyOf(slice, slice.length); // "BXKXB", "X X", "X X", "X X", "BFSFB"
        if (this.frontFacing == EnumFacing.NORTH || this.frontFacing == EnumFacing.SOUTH) {
            center[0] = centerBuilder.reverse().toString();
            center[center.length - 1] = controllerBuilder.reverse().toString();
        } else {
            center[0] = centerBuilder.toString();
            center[center.length - 1] = controllerBuilder.toString();
        }

        TraceabilityPredicate wallPredicate = states(getCasingState(), getGlassState());
        TraceabilityPredicate basePredicate = autoAbilities().or(abilities(MultiblockAbility.INPUT_ENERGY)
                .setMinGlobalLimited(1).setMaxGlobalLimited(3));

        // layer the slices one behind the next
        return FactoryBlockPattern.start()
                .aisle(wall)
                .aisle(slice).setRepeatable(bDist - 1)
                .aisle(center)
                .aisle(slice).setRepeatable(fDist - 1)
                .aisle(wall)
                .where('S', selfPredicate())
                .where('B', states(getCasingState()).or(basePredicate))
                .where('X', wallPredicate.or(basePredicate)
                        .or(doorPredicate().setMaxGlobalLimited(8))
                        .or(abilities(MultiblockAbility.PASSTHROUGH_HATCH).setMaxGlobalLimited(30)))
                .where('K', wallPredicate) // the block beneath the controller must only be a casing for structure
                                           // dimension checks
                .where('F', filterPredicate())
                .where(' ', innerPredicate())
                .build();
    }

    @NotNull
    protected TraceabilityPredicate filterPredicate() {
        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState blockState = blockWorldState.getBlockState();
            if (GregTechAPI.CLEANROOM_FILTERS.containsKey(blockState)) {
                ICleanroomFilter cleanroomFilter = GregTechAPI.CLEANROOM_FILTERS.get(blockState);
                if (cleanroomFilter.getCleanroomType() == null) return false;

                ICleanroomFilter currentFilter = blockWorldState.getMatchContext().getOrPut("FilterType",
                        cleanroomFilter);
                if (!currentFilter.getCleanroomType().equals(cleanroomFilter.getCleanroomType())) {
                    blockWorldState.setError(new PatternStringError("gregtech.multiblock.pattern.error.filters"));
                    return false;
                }
                blockWorldState.getMatchContext().getOrPut("VABlock", new LinkedList<>()).add(blockWorldState.getPos());
                return true;
            }
            return false;
        }, () -> GregTechAPI.CLEANROOM_FILTERS.entrySet().stream()
                .filter(entry -> entry.getValue().getCleanroomType() != null)
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
                .map(entry -> new BlockInfo(entry.getKey(), null))
                .toArray(BlockInfo[]::new))
                        .addTooltips("gregtech.multiblock.pattern.error.filters");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.PLASCRETE;
    }

    // protected to allow easy addition of addon "cleanrooms"
    @NotNull
    protected IBlockState getCasingState() {
        return MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE);
    }

    @NotNull
    protected IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.CLEANROOM_GLASS);
    }

    @NotNull
    protected static TraceabilityPredicate doorPredicate() {
        return new TraceabilityPredicate(
                blockWorldState -> blockWorldState.getBlockState().getBlock() instanceof BlockDoor);
    }

    @NotNull
    protected TraceabilityPredicate innerPredicate() {
        return new TraceabilityPredicate(blockWorldState -> {
            // all non-MetaTileEntities are allowed inside by default
            TileEntity tileEntity = blockWorldState.getTileEntity();
            if (!(tileEntity instanceof IGregTechTileEntity)) return true;

            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();

            // always ban other cleanrooms, can cause problems otherwise
            if (metaTileEntity instanceof ICleanroomProvider)
                return false;

            if (isMachineBanned(metaTileEntity))
                return false;

            // the machine does not need a cleanroom, so do nothing more
            if (!(metaTileEntity instanceof ICleanroomReceiver cleanroomReceiver)) return true;

            // give the machine this cleanroom if it doesn't have this one
            if (cleanroomReceiver.getCleanroom() != this) {
                cleanroomReceiver.setCleanroom(this);
                cleanroomReceivers.add(cleanroomReceiver);
            }
            return true;
        });
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_MECHANICAL;
    }

    protected boolean isMachineBanned(MetaTileEntity metaTileEntity) {
        // blacklisted machines: mufflers and all generators, miners/drills, primitives
        if (metaTileEntity instanceof IMufflerHatch) return true;
        if (metaTileEntity instanceof SimpleGeneratorMetaTileEntity) return true;
        if (metaTileEntity instanceof FuelMultiblockController) return true;
        if (metaTileEntity instanceof MetaTileEntityLargeMiner) return true;
        if (metaTileEntity instanceof MetaTileEntityFluidDrill) return true;
        if (metaTileEntity instanceof MetaTileEntityCentralMonitor) return true;
        if (metaTileEntity instanceof MetaTileEntityCleanroom) return true;
        if (metaTileEntity instanceof MetaTileEntityCokeOven) return true;
        if (metaTileEntity instanceof MetaTileEntityPrimitiveBlastFurnace) return true;
        return metaTileEntity instanceof MetaTileEntityPrimitiveWaterPump;
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.setWorkingStatus(cleanroomLogic.isWorkingEnabled(), cleanroomLogic.isActive())
                .addEnergyUsageLine(energyContainer)
                .addEnergyUsageExactLine(isClean() ? 4 : GTValues.VA[getEnergyTier()])
                .addCustom((list, syncer) -> {
                    // Cleanliness status line
                    if (isStructureFormed()) {
                        IKey cleanState;
                        if (syncer.syncBoolean(isClean())) { // is this method actually synced?
                            cleanState = KeyUtil.lang(TextFormatting.GREEN, "gregtech.multiblock.cleanroom.clean_state",
                                    cleanAmount);
                        } else {
                            cleanState = KeyUtil.lang(TextFormatting.DARK_RED,
                                    "gregtech.multiblock.cleanroom.dirty_state", cleanAmount);
                        }

                        list.add(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.cleanroom.clean_status",
                                cleanState));
                    }
                })
                .addProgressLine(getProgress(), getMaxProgress())
                .addWorkingStatusLine();
    }

    @Override
    protected void configureWarningText(MultiblockUIBuilder builder) {
        builder.addLowPowerLine(!drainEnergy(true))
                .addCustom((list, syncer) -> {
                    if (isStructureFormed() && !syncer.syncBoolean(isClean())) {
                        list.add(KeyUtil.lang(TextFormatting.YELLOW,
                                "gregtech.multiblock.cleanroom.warning_contaminated"));
                    }

                    if (!syncer.syncBoolean(cleanroomLogic.isVoltageHighEnough())) {
                        IKey energyNeeded = IKey.str(GTValues.VNF[cleanroomFilter.getMinTier()]);
                        list.add(KeyUtil.lang(TextFormatting.YELLOW, "gregtech.multiblock.cleanroom.low_tier",
                                energyNeeded));
                    }
                })
                .addMaintenanceProblemLines(getMaintenanceProblems());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.3"));
        tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.4"));

        if (TooltipHelper.isCtrlDown()) {
            tooltip.add("");
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.5"));
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.6"));
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.7"));
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.8"));
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.9"));
            if (Mods.AppliedEnergistics2.isModLoaded()) {
                tooltip.add(I18n.format(AEConfig.instance().isFeatureEnabled(AEFeature.CHANNELS) ?
                        "gregtech.machine.cleanroom.tooltip.ae2.channels" :
                        "gregtech.machine.cleanroom.tooltip.ae2.no_channels"));
            }
            tooltip.add("");
        } else {
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.hold_ctrl"));
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(),
                isWorkingEnabled());
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.CLEANROOM_OVERLAY;
    }

    @Override
    public boolean checkCleanroomType(@NotNull CleanroomType type) {
        return type == this.cleanroomType;
    }

    @Override
    public void setCleanAmount(int amount) {
        this.cleanAmount = amount;
    }

    @Override
    public void adjustCleanAmount(int amount) {
        // do not allow negative cleanliness nor cleanliness above 100
        this.cleanAmount = MathHelper.clamp(this.cleanAmount + amount, 0, 100);
    }

    @Override
    public boolean isClean() {
        return this.cleanAmount >= CLEAN_AMOUNT_THRESHOLD;
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        return Collections.singletonList(new TextComponentTranslation(
                isClean() ? "gregtech.multiblock.cleanroom.clean_state" : "gregtech.multiblock.cleanroom.dirty_state",
                this.cleanAmount));
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.cleanroomLogic.isActive();
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.cleanroomLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        if (!isActivationAllowed) // pausing sets not clean
            setCleanAmount(MIN_CLEAN_AMOUNT);
        this.cleanroomLogic.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public int getProgress() {
        return cleanroomLogic.getProgressTime();
    }

    @Override
    public int getMaxProgress() {
        return cleanroomLogic.getMaxProgress();
    }

    public int getProgressPercent() {
        return cleanroomLogic.getProgressPercent();
    }

    @Override
    public int getEnergyTier() {
        if (energyContainer == null) return GTValues.LV;
        return Math.min(GTValues.MAX,
                Math.max(GTValues.LV, GTUtility.getFloorTierByVoltage(energyContainer.getInputVoltage())));
    }

    @Override
    public long getEnergyInputPerSecond() {
        return energyContainer.getInputPerSec();
    }

    public boolean drainEnergy(boolean simulate) {
        long energyToDrain = isClean() ? 4 :
                GTValues.VA[getEnergyTier()];
        long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.changeEnergy(-energyToDrain);
            return true;
        }
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_STRUCTURE_SIZE) {
            this.lDist = buf.readInt();
            this.rDist = buf.readInt();
            this.bDist = buf.readInt();
            this.fDist = buf.readInt();
            this.hDist = buf.readInt();
        } else if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.cleanroomLogic.setActive(buf.readBoolean());
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.cleanroomLogic.setWorkingEnabled(buf.readBoolean());
            scheduleRenderUpdate();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("lDist", this.lDist);
        data.setInteger("rDist", this.rDist);
        data.setInteger("bDist", this.fDist);
        data.setInteger("fDist", this.bDist);
        data.setInteger("hDist", this.hDist);
        data.setInteger("cleanAmount", this.cleanAmount);
        return this.cleanroomLogic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.lDist = data.hasKey("lDist") ? data.getInteger("lDist") : this.lDist;
        this.rDist = data.hasKey("rDist") ? data.getInteger("rDist") : this.rDist;
        this.hDist = data.hasKey("hDist") ? data.getInteger("hDist") : this.hDist;
        this.bDist = data.hasKey("bDist") ? data.getInteger("bDist") : this.bDist;
        this.fDist = data.hasKey("fDist") ? data.getInteger("fDist") : this.fDist;
        reinitializeStructurePattern();
        this.cleanAmount = data.getInteger("cleanAmount");
        this.cleanroomLogic.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.lDist);
        buf.writeInt(this.rDist);
        buf.writeInt(this.bDist);
        buf.writeInt(this.fDist);
        buf.writeInt(this.hDist);
        buf.writeInt(this.cleanAmount);
        this.cleanroomLogic.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.lDist = buf.readInt();
        this.rDist = buf.readInt();
        this.bDist = buf.readInt();
        this.fDist = buf.readInt();
        this.hDist = buf.readInt();
        this.cleanAmount = buf.readInt();
        this.cleanroomLogic.receiveInitialSyncData(buf);
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        if (ConfigHolder.machines.enableCleanroom) {
            super.getSubItems(creativeTab, subItems);
        }
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("XXXXX", "XIHLX", "XXDXX", "XXXXX", "XXXXX")
                .aisle("XXXXX", "X   X", "G   G", "X   X", "XFFFX")
                .aisle("XXXXX", "X   X", "G   G", "X   X", "XFSFX")
                .aisle("XXXXX", "X   X", "G   G", "X   X", "XFFFX")
                .aisle("XMXEX", "XXOXX", "XXRXX", "XXXXX", "XXXXX")
                .where('X', MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE))
                .where('G', MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.CLEANROOM_GLASS))
                .where('S', MetaTileEntities.CLEANROOM, EnumFacing.SOUTH)
                .where(' ', Blocks.AIR.getDefaultState())
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LV], EnumFacing.SOUTH)
                .where('I', MetaTileEntities.PASSTHROUGH_HATCH_ITEM, EnumFacing.NORTH)
                .where('L', MetaTileEntities.PASSTHROUGH_HATCH_FLUID, EnumFacing.NORTH)
                .where('H', MetaTileEntities.HULL[GTValues.HV], EnumFacing.NORTH)
                .where('D', MetaTileEntities.DIODES[GTValues.HV], EnumFacing.NORTH)
                .where('M',
                        () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                                MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE),
                        EnumFacing.SOUTH)
                .where('O',
                        Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.FACING, EnumFacing.NORTH)
                                .withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER))
                .where('R', Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.FACING, EnumFacing.NORTH)
                        .withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER));

        GregTechAPI.CLEANROOM_FILTERS.entrySet().stream()
                .filter(entry -> entry.getValue().getCleanroomType() != null)
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
                .forEach(entry -> shapeInfo.add(builder.where('F', entry.getKey()).build()));

        return shapeInfo;
    }

    @Override
    public boolean shouldShowVoidingModeButton() {
        return false;
    }
}
