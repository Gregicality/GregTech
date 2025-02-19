package gregtech.api.metatileentity;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IDataStickIntractable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerProxy;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverHolder;
import gregtech.api.cover.CoverRayTracer;
import gregtech.api.cover.CoverSaveHandler;
import gregtech.api.cover.CoverUtil;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.interfaces.ISyncedTileEntity;
import gregtech.api.metatileentity.registry.MTERegistry;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.GregTechGuiScreen;
import gregtech.api.mui.factory.MetaTileEntityGuiFactory;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.common.items.MetaItems;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.me.helpers.AENetworkProxy;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static gregtech.api.capability.GregtechDataCodes.*;

public abstract class MetaTileEntity implements ISyncedTileEntity, CoverHolder, IVoidable, IGuiHolder<PosGuiData> {

    public static final IndexedCuboid6 FULL_CUBE_COLLISION = new IndexedCuboid6(null, Cuboid6.full);

    public static final String TAG_KEY_PAINTING_COLOR = "PaintingColor";
    public static final String TAG_KEY_MUFFLED = "Muffled";

    private final MTERegistry registry;

    public final ResourceLocation metaTileEntityId;
    IGregTechTileEntity holder;

    protected IItemHandlerModifiable importItems;
    protected IItemHandlerModifiable exportItems;

    protected IItemHandler itemInventory;

    protected FluidTankList importFluids;
    protected FluidTankList exportFluids;

    protected IFluidHandler fluidInventory;

    private final Map<String, MTETrait> mteTraits = new Object2ObjectOpenHashMap<>();
    private final Int2ObjectMap<MTETrait> mteTraitByNetworkId = new Int2ObjectOpenHashMap<>();

    protected EnumFacing frontFacing = EnumFacing.NORTH;
    private int paintingColor = -1;

    private final int[] sidedRedstoneOutput = new int[6];
    private final int[] sidedRedstoneInput = new int[6];
    private int cachedLightValue;

    private boolean wasExploded = false;

    private final EnumMap<EnumFacing, Cover> covers = new EnumMap<>(EnumFacing.class);

    protected List<IItemHandlerModifiable> notifiedItemOutputList = new ArrayList<>();
    protected List<IItemHandlerModifiable> notifiedItemInputList = new ArrayList<>();
    protected List<IFluidHandler> notifiedFluidInputList = new ArrayList<>();
    protected List<IFluidHandler> notifiedFluidOutputList = new ArrayList<>();

    protected boolean muffled = false;

    private int playSoundCooldown = 0;
    private int lastTick = 0;

    @Nullable
    private UUID owner = null;

    private final Set<CreativeTabs> creativeTabs = new ObjectArraySet<>();

    {
        creativeTabs.add(CreativeTabs.SEARCH);
        creativeTabs.add(GTCreativeTabs.TAB_GREGTECH_MACHINES);
    }

    protected MetaTileEntity(@NotNull ResourceLocation metaTileEntityId) {
        this.metaTileEntityId = metaTileEntityId;
        this.registry = GregTechAPI.mteManager.getRegistry(metaTileEntityId.getNamespace());
        initializeInventory();
    }

    protected void initializeInventory() {
        this.importItems = createImportItemHandler();
        this.exportItems = createExportItemHandler();
        this.itemInventory = new ItemHandlerProxy(importItems, exportItems);

        this.importFluids = createImportFluidHandler();
        this.exportFluids = createExportFluidHandler();
        this.fluidInventory = new FluidHandlerProxy(importFluids, exportFluids);
    }

    public IGregTechTileEntity getHolder() {
        return holder;
    }

    public abstract MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity);

    @Override
    public World getWorld() {
        return holder == null ? null : holder.world();
    }

    @Override
    public BlockPos getPos() {
        return holder == null ? null : holder.pos();
    }

    @Override
    public void markDirty() {
        if (holder != null) {
            holder.markAsDirty();
        }
    }

    public boolean isFirstTick() {
        return holder != null && holder.isFirstTick();
    }

    /**
     * Replacement for former getTimer() call.
     *
     * @return Timer value, starting at zero, with a random offset [0, 20).
     */
    @Override
    public long getOffsetTimer() {
        return holder == null ? 0L : holder.getOffsetTimer();
    }

    @Override
    public @Nullable TileEntity getNeighbor(@NotNull EnumFacing facing) {
        return holder != null ? holder.getNeighbor(facing) : null;
    }

    @Override
    public final void writeCustomData(int discriminator, @NotNull Consumer<@NotNull PacketBuffer> dataWriter) {
        if (holder != null) {
            holder.writeCustomData(discriminator, dataWriter);
        }
    }

    public void addDebugInfo(List<String> list) {}

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        if (ConfigHolder.machines.doTerrainExplosion && getIsWeatherOrTerrainResistant())
            tooltip.add(I18n.format("gregtech.universal.tooltip.terrain_resist"));
    }

    /**
     * Override this to add extended tool information to the "Hold SHIFT to show Tool Info" tooltip section.
     * ALWAYS CALL SUPER LAST!
     * Intended ordering:
     * - Screwdriver
     * - Wrench
     * - Wire Cutter
     * - Soft Hammer
     * - Hammer
     * - Crowbar
     * - Others
     * <br>
     * The super method automatically handles Hammer muffling and Crowbar cover removal.
     * If you have extended usages of these tools in your addon, let us know and we can amend
     * this default appended tooltip information.
     */
    @SideOnly(Side.CLIENT)
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        if (getSound() != null) {
            tooltip.add(I18n.format("gregtech.tool_action.hammer"));
        }
        tooltip.add(I18n.format("gregtech.tool_action.crowbar"));
    }

    /**
     * Override this to completely remove the "Tool Info" tooltip section
     */
    public boolean showToolUsages() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(RenderUtil.getMissingSprite(), 0xFFFFFF);
    }

    /**
     * ItemStack currently being rendered by this meta tile entity
     * Use this to obtain itemstack-specific data like contained fluid, painting color
     * Generally useful in combination with {@link #writeItemStackData(net.minecraft.nbt.NBTTagCompound)}
     */
    @SideOnly(Side.CLIENT)
    protected ItemStack renderContextStack;

    @SideOnly(Side.CLIENT)
    public void setRenderContextStack(ItemStack itemStack) {
        this.renderContextStack = itemStack;
    }

    /**
     * Renders this meta tile entity
     * Note that you shouldn't refer to world-related information in this method, because it
     * will be called on ItemStacks too
     *
     * @param renderState render state (either chunk batched or item)
     * @param pipeline    default set of pipeline transformations
     */
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        TextureAtlasSprite atlasSprite = RenderUtil.getMissingSprite();
        IVertexOperation[] renderPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        for (EnumFacing face : EnumFacing.VALUES) {
            Textures.renderFace(renderState, translation, renderPipeline, face, Cuboid6.full, atlasSprite,
                    BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(BlockRenderLayer renderLayer) {
        return renderLayer == BlockRenderLayer.CUTOUT_MIPPED ||
                renderLayer == BloomEffectUtil.getEffectiveBloomLayer() ||
                (renderLayer == BlockRenderLayer.TRANSLUCENT &&
                        !getWorld().getBlockState(getPos()).getValue(BlockMachine.OPAQUE));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getPaintingColorForRendering() {
        if (getWorld() == null && renderContextStack != null) {
            NBTTagCompound tagCompound = renderContextStack.getTagCompound();
            if (tagCompound != null && tagCompound.hasKey(TAG_KEY_PAINTING_COLOR, NBT.TAG_INT)) {
                return tagCompound.getInteger(TAG_KEY_PAINTING_COLOR);
            }
        }
        return isPainted() ? paintingColor : getDefaultPaintingColor();
    }

    /**
     * Used to display things like particles on random display ticks
     * This method is typically used by torches or nether portals, as an example use-case
     */
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick() {}

    /**
     * Called from ItemBlock to initialize this MTE with data contained in ItemStack
     *
     * @param itemStack itemstack of itemblock
     */
    public void initFromItemStackData(NBTTagCompound itemStack) {}

    /**
     * Called to write MTE specific data when it is destroyed to save it's state
     * into itemblock, which can be placed later to get {@link #initFromItemStackData} called
     *
     * @param itemStack itemstack from which this MTE is being placed
     */
    public void writeItemStackData(NBTTagCompound itemStack) {}

    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        subItems.add(getStackForm());
    }

    /**
     * Check if this MTE belongs in certain creative tab. To add machines in custom creative tab, the creative tab
     * should be registered via {@link gregtech.api.block.machines.MachineItemBlock#addCreativeTab(CreativeTabs)
     * MachineItemBlock#addCreativeTab(CreativeTabs)} beforehand.
     *
     * @param creativeTab The creative tab to check
     * @return Whether this MTE belongs in the creative tab or not
     * @see gregtech.api.block.machines.MachineItemBlock#addCreativeTab(CreativeTabs)
     *      MachineItemBlock#addCreativeTab(CreativeTabs)
     */
    public boolean isInCreativeTab(CreativeTabs creativeTab) {
        return creativeTabs.contains(creativeTab);
    }

    public String getItemSubTypeId(ItemStack itemStack) {
        return "";
    }

    public ICapabilityProvider initItemStackCapabilities(ItemStack itemStack) {
        return null;
    }

    public String getMetaName() {
        return String.format("%s.machine.%s", metaTileEntityId.getNamespace(), metaTileEntityId.getPath());
    }

    public final String getMetaFullName() {
        return getMetaName() + ".name";
    }

    public <T> void addNotifiedInput(T input) {
        if (input instanceof IItemHandlerModifiable) {
            if (!notifiedItemInputList.contains(input)) {
                this.notifiedItemInputList.add((IItemHandlerModifiable) input);
            }
        } else if (input instanceof IFluidHandler) {
            if (!notifiedFluidInputList.contains(input)) {
                this.notifiedFluidInputList.add((IFluidHandler) input);
            }
        }
    }

    public <T> void addNotifiedOutput(T output) {
        if (output instanceof IItemHandlerModifiable) {
            if (!notifiedItemOutputList.contains(output)) {
                this.notifiedItemOutputList.add((IItemHandlerModifiable) output);
            }
        } else if (output instanceof NotifiableFluidTank) {
            if (!notifiedFluidOutputList.contains(output)) {
                this.notifiedFluidOutputList.add((NotifiableFluidTank) output);
            }
        }
    }

    /**
     * Adds a trait to this meta tile entity.
     * Traits are objects linked with meta tile entity and performing certain actions.
     * Usually traits implement capabilities there can be only one trait for a given name
     *
     * @param trait trait object to add
     */
    void addMetaTileEntityTrait(@NotNull MTETrait trait) {
        this.mteTraits.put(trait.getName(), trait);
        this.mteTraitByNetworkId.put(trait.getNetworkID(), trait);
    }

    /**
     * Get a trait by name
     *
     * @param name the name of the trait
     * @return the trait associated with the name
     */
    @Nullable
    public final MTETrait getMTETrait(@NotNull String name) {
        return this.mteTraits.get(name);
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new GTItemStackHandler(this, 0);
    }

    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, 0);
    }

    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false);
    }

    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false);
    }

    protected boolean openGUIOnRightClick() {
        return true;
    }

    /**
     * Creates a UI instance for player opening inventory of this meta tile entity
     *
     * @param entityPlayer player opening inventory
     * @return freshly created UI instance
     */
    @Deprecated
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Deprecated
    public ModularUI getModularUI(EntityPlayer entityPlayer) {
        return createUI(entityPlayer);
    }

    @ApiStatus.Experimental
    public boolean usesMui2() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public final ModularScreen createScreen(PosGuiData posGuiData, ModularPanel mainPanel) {
        return new GregTechGuiScreen(mainPanel, getUITheme());
    }

    public GTGuiTheme getUITheme() {
        return GTGuiTheme.STANDARD;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        return null;
    }

    public final void onCoverLeftClick(EntityPlayer playerIn, CuboidRayTraceResult result) {
        Cover cover = getCoverAtSide(result.sideHit);
        if (cover == null || !cover.onLeftClick(playerIn, result)) {
            onLeftClick(playerIn, result.sideHit, result);
        }
    }

    /**
     * Called when player clicks on specific side of this meta tile entity
     *
     * @return true if something happened, so animation will be played
     */
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        ItemStack heldStack = playerIn.getHeldItem(hand);
        if (this instanceof IDataStickIntractable dsi) {
            if (MetaItems.TOOL_DATA_STICK.isItemEqual(heldStack) && dsi.onDataStickRightClick(playerIn, heldStack)) {
                return true;
            }
        }

        if (!playerIn.isSneaking() && openGUIOnRightClick()) {
            if (getWorld() != null && !getWorld().isRemote) {
                if (usesMui2()) {
                    MetaTileEntityGuiFactory.open(playerIn, this);
                } else {
                    MetaTileEntityUIFactory.INSTANCE.openUI(getHolder(), (EntityPlayerMP) playerIn);
                }

                if (getOwner() == null) {
                    this.owner = playerIn.getUniqueID();
                }
            }
            return true;
        } else {
            // Attempt to rename the MTE first
            if (heldStack.getItem() == Items.NAME_TAG) {
                if (playerIn.isSneaking() && heldStack.getTagCompound() != null &&
                        heldStack.getTagCompound().hasKey("display")) {
                    MetaTileEntityHolder mteHolder = (MetaTileEntityHolder) getHolder();

                    mteHolder.setCustomName(heldStack.getTagCompound().getCompoundTag("display").getString("Name"));
                    if (!playerIn.isCreative()) {
                        heldStack.shrink(1);
                    }
                    return true;
                }
            }

            // Try to do cover right-click behavior on this specific side first
            EnumFacing hitFacing = hitResult.sideHit;
            Cover cover = hitFacing == null ? null : getCoverAtSide(hitFacing);
            if (cover != null) {
                if (cover.onRightClick(playerIn, hand, hitResult) == EnumActionResult.SUCCESS) {
                    return true;
                }
            }

            // Then try to do cover screwdriver-click behavior on the cover grid side next
            EnumFacing gridSideHit = CoverRayTracer.determineGridSideHit(hitResult);
            cover = gridSideHit == null ? null : getCoverAtSide(gridSideHit);
            if (cover != null && playerIn.isSneaking() && playerIn.getHeldItemMainhand().isEmpty()) {
                return cover.onScrewdriverClick(playerIn, hand, hitResult) == EnumActionResult.SUCCESS;
            }
        }

        return false;
    }

    /**
     * Called when a player clicks this meta tile entity with a tool
     *
     * @return true if something happened, so tools will get damaged and animations will be played
     */
    public final boolean onToolClick(EntityPlayer playerIn, @NotNull Set<String> toolClasses, EnumHand hand,
                                     CuboidRayTraceResult hitResult) {
        // the side hit from the machine grid
        EnumFacing gridSideHit = CoverRayTracer.determineGridSideHit(hitResult);
        Cover cover = gridSideHit == null ? null : getCoverAtSide(gridSideHit);

        // Prioritize covers where they apply (Screwdriver, Soft Mallet)
        if (toolClasses.contains(ToolClasses.SCREWDRIVER)) {
            if (cover != null && cover.onScrewdriverClick(playerIn, hand, hitResult) == EnumActionResult.SUCCESS) {
                return true;
            } else return onScrewdriverClick(playerIn, hand, gridSideHit, hitResult);
        }
        if (toolClasses.contains(ToolClasses.SOFT_MALLET)) {
            if (cover != null && cover.onSoftMalletClick(playerIn, hand, hitResult) == EnumActionResult.SUCCESS) {
                return true;
            } else return onSoftMalletClick(playerIn, hand, gridSideHit, hitResult);
        }
        if (toolClasses.contains(ToolClasses.WRENCH)) {
            return onWrenchClick(playerIn, hand, gridSideHit, hitResult);
        }
        if (toolClasses.contains(ToolClasses.CROWBAR)) {
            return onCrowbarClick(playerIn, hand, gridSideHit, hitResult);
        }
        if (toolClasses.contains(ToolClasses.HARD_HAMMER)) {
            return onHardHammerClick(playerIn, hand, gridSideHit, hitResult);
        }
        if (toolClasses.contains(ToolClasses.WIRE_CUTTER)) {
            return onWireCutterClick(playerIn, hand, gridSideHit, hitResult);
        }
        return false;
    }

    /**
     * Called when player clicks a wrench on specific side of this meta tile entity
     *
     * @return true if something happened, so the tool will get damaged and animation will be played
     */
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide,
                                 CuboidRayTraceResult hitResult) {
        if (!needsSneakToRotate() || playerIn.isSneaking()) {
            if (wrenchSide == getFrontFacing() || !isValidFrontFacing(wrenchSide) || !hasFrontFacing()) {
                return false;
            }
            if (wrenchSide != null && !getWorld().isRemote) {
                setFrontFacing(wrenchSide);
            }
            return true;
        }
        return false;
    }

    /**
     * Called when player clicks a screwdriver on specific side of this meta tile entity
     *
     * @return true if something happened, so the tool will get damaged and animation will be played
     */
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        return false;
    }

    /**
     * Called when player clicks a crowbar on specific side of this meta tile entity
     *
     * @return true if something happened, so the tool will get damaged and animation will be played
     */
    public boolean onCrowbarClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                  CuboidRayTraceResult hitResult) {
        if (getCoverAtSide(facing) != null) {
            removeCover(facing);
            return true;
        }
        return false;
    }

    /**
     * Called when player clicks a soft mallet on specific side of this meta tile entity
     *
     * @return true if something happened, so the tool will get damaged and animation will be played
     */
    public boolean onSoftMalletClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                     CuboidRayTraceResult hitResult) {
        IControllable controllable = getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        if (controllable != null) {
            controllable.setWorkingEnabled(!controllable.isWorkingEnabled());
            if (!getWorld().isRemote) {
                playerIn.sendStatusMessage(new TextComponentTranslation(controllable.isWorkingEnabled() ?
                        "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled"), true);
            }
            return true;
        }
        return false;
    }

    /**
     * Called when player clicks a hard hammer on specific side of this meta tile entity
     *
     * @return true if something happened, so the tool will get damaged and animation will be played
     */
    public boolean onHardHammerClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                     CuboidRayTraceResult hitResult) {
        toggleMuffled();
        if (!getWorld().isRemote) {
            playerIn.sendStatusMessage(new TextComponentTranslation(isMuffled() ?
                    "gregtech.machine.muffle.on" : "gregtech.machine.muffle.off"), true);
        }
        return true;
    }

    /**
     * Called when player clicks a wire cutter on specific side of this meta tile entity
     *
     * @return true if something happened, so the tool will get damaged and animation will be played
     */
    public boolean onWireCutterClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                     CuboidRayTraceResult hitResult) {
        return false;
    }

    public void onLeftClick(EntityPlayer player, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (this instanceof IDataStickIntractable dsi) {
            ItemStack stack = player.getHeldItemMainhand();
            if (MetaItems.TOOL_DATA_STICK.isItemEqual(stack)) {
                dsi.onDataStickLeftClick(player, stack);
            }
        }
    }

    /**
     * @return true if the player must sneak to rotate this metatileentity, otherwise false
     */
    public boolean needsSneakToRotate() {
        return false;
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
    public void addCover(@NotNull EnumFacing side, @NotNull Cover cover) {
        // we checked before if the side already has a cover
        covers.put(side, cover);
        if (!getWorld().isRemote) {
            CoverSaveHandler.writeCoverPlacement(this, COVER_ATTACHED_MTE, side, cover);
        }
        notifyBlockUpdate();
        markDirty();
        onCoverPlacementUpdate();
    }

    @Override
    public boolean canPlaceCoverOnSide(@NotNull EnumFacing side) {
        List<IndexedCuboid6> collisionList = new ArrayList<>();
        addCollisionBoundingBox(collisionList);
        // cover collision box overlaps with meta tile entity collision box
        return !CoverUtil.doesCoverCollide(side, collisionList, getCoverPlateThickness());
    }

    @Override
    public boolean acceptsCovers() {
        return covers.size() < EnumFacing.VALUES.length;
    }

    /**
     * @param side the side to remove a cover from
     */
    public final void removeCover(@NotNull EnumFacing side) {
        Cover cover = getCoverAtSide(side);
        if (cover == null) return;

        dropCover(side);
        covers.remove(side);
        writeCustomData(COVER_REMOVED_MTE, buffer -> buffer.writeByte(side.getIndex()));
        notifyBlockUpdate();
        markDirty();
        onCoverPlacementUpdate();
    }

    protected void onCoverPlacementUpdate() {}

    @Override
    public double getCoverPlateThickness() {
        return 0;
    }

    @Override
    public boolean shouldRenderCoverBackSides() {
        return !isOpaqueCube();
    }

    public void onLoad() {
        for (EnumFacing side : EnumFacing.VALUES) {
            this.sidedRedstoneInput[side.getIndex()] = GTUtility.getRedstonePower(getWorld(), getPos(), side);
        }
    }

    public void onUnload() {}

    public final boolean canConnectRedstone(@Nullable EnumFacing side) {
        // so far null side means either upwards or downwards redstone wire connection
        // so check both top cover and bottom cover
        if (side == null) {
            return canConnectRedstone(EnumFacing.UP) ||
                    canConnectRedstone(EnumFacing.DOWN);
        }
        Cover cover = getCoverAtSide(side);
        if (cover == null) {
            return canMachineConnectRedstone(side);
        }
        return cover.canConnectRedstone();
    }

    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return false;
    }

    @Override
    public final int getInputRedstoneSignal(@NotNull EnumFacing side, boolean ignoreCover) {
        if (!ignoreCover && getCoverAtSide(side) != null) {
            return 0; // covers block input redstone signal for machine
        }
        return sidedRedstoneInput[side.getIndex()];
    }

    public final boolean isBlockRedstonePowered() {
        for (EnumFacing side : EnumFacing.VALUES) {
            if (getInputRedstoneSignal(side, false) > 0) {
                return true;
            }
        }
        return false;
    }

    public void onNeighborChanged() {}

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

    /**
     * @deprecated Will be removed in 2.9. Comparators no longer supported for MetaTileEntities, as cover are
     *             interactions favored.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    @Deprecated
    public int getActualComparatorValue() {
        return 0;
    }

    public int getActualLightValue() {
        return 0;
    }

    /**
     * @deprecated Will be removed in 2.9.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    @Deprecated
    public final int getComparatorValue() {
        return 0;
    }

    public final int getLightValue() {
        return cachedLightValue;
    }

    private void updateLightValue() {
        int newLightValue = getActualLightValue();
        if (cachedLightValue != newLightValue) {
            this.cachedLightValue = newLightValue;
            if (getWorld() != null) {
                getWorld().checkLight(getPos());
            }
        }
    }

    public void update() {
        if (!getWorld().isRemote && !allowTickAcceleration()) {
            int currentTick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
            if (currentTick == lastTick) {
                return;
            }
            lastTick = currentTick;
        }

        for (MTETrait mteTrait : this.mteTraits.values()) {
            if (shouldUpdate(mteTrait)) {
                mteTrait.update();
            }
        }

        if (!getWorld().isRemote) {
            updateCovers();
        } else {
            updateSound();
        }
        if (getOffsetTimer() % 5 == 0L) {
            updateLightValue();
        }
    }

    /**
     * @return Whether this machine is allowed to be tick accelerated by external means. This does NOT
     *         apply to World Accelerators from GT, those will never work on machines. This refers to effects
     *         like Time in a Bottle, or Torcherino, or similar.
     */
    public boolean allowTickAcceleration() {
        return ConfigHolder.machines.allowTickAcceleration;
    }

    protected boolean shouldUpdate(MTETrait trait) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    private void updateSound() {
        if (!ConfigHolder.machines.machineSounds || isMuffled()) {
            return;
        }
        SoundEvent sound = getSound();
        if (sound == null) {
            return;
        }
        if (isValid() && isActive()) {
            if (--playSoundCooldown > 0) {
                return;
            }
            GregTechAPI.soundManager.startTileSound(sound.getSoundName(), getVolume(), getPos());
            playSoundCooldown = 20;
        } else {
            GregTechAPI.soundManager.stopTileSound(getPos());
            playSoundCooldown = 0;
        }
    }

    public final @NotNull ItemStack getStackForm(int amount) {
        int metaTileEntityIntId = registry.getIdByObjectName(metaTileEntityId);
        return new ItemStack(registry.getBlock(), amount, metaTileEntityIntId);
    }

    @Override
    public final @NotNull ItemStack getStackForm() {
        return getStackForm(1);
    }

    public final @NotNull MTERegistry getRegistry() {
        return registry;
    }

    public final @NotNull BlockMachine getBlock() {
        return registry.getBlock();
    }

    /**
     * Add special drops which this meta tile entity contains here
     * Meta tile entity item is ALREADY added into this list
     * Do NOT add inventory contents in this list - it will be dropped automatically when breakBlock is called
     * This will only be called if meta tile entity is broken with proper tool (i.e wrench)
     *
     * @param dropsList list of meta tile entity drops
     * @param harvester harvester of this meta tile entity, or null
     */
    public void getDrops(@NotNull List<@NotNull ItemStack> dropsList, @Nullable EntityPlayer harvester) {}

    public final ItemStack getPickItem(CuboidRayTraceResult result, EntityPlayer player) {
        IndexedCuboid6 hitCuboid = result.cuboid6;
        final boolean isCreativePickBlock = player.isCreative() && TooltipHelper.isCtrlDown();
        if (hitCuboid.data instanceof CoverRayTracer.CoverSideData coverSideData) {
            Cover cover = getCoverAtSide(coverSideData.side);
            return cover == null || isCreativePickBlock ? ItemStack.EMPTY : cover.getPickItem();
        } else if (hitCuboid.data == null || hitCuboid.data instanceof CoverRayTracer.PrimaryBoxData) {
            // data is null -> MetaTileEntity hull hit
            Cover cover = getCoverAtSide(result.sideHit);
            if (cover != null && !isCreativePickBlock) {
                return cover.getPickItem();
            }
            return getPickItem(player);
        } else {
            return ItemStack.EMPTY;
        }
    }

    public ItemStack getPickItem(EntityPlayer player) {
        return getStackForm();
    }

    /**
     * Whether this tile entity represents completely opaque cube
     *
     * @return true if machine is opaque
     */
    public boolean isOpaqueCube() {
        return true;
    }

    public int getLightOpacity() {
        return 255;
    }

    /**
     * Called to obtain list of AxisAlignedBB used for collision testing, highlight rendering
     * and ray tracing this meta tile entity's block in world
     */
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        collisionList.add(FULL_CUBE_COLLISION);
    }

    /**
     * Retrieves face shape on the current side of this meta tile entity
     */
    public BlockFaceShape getFaceShape(EnumFacing side) {
        return isOpaqueCube() ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    /**
     * @return tool required to dismantle this meta tile entity properly
     */
    public String getHarvestTool() {
        return ToolClasses.WRENCH;
    }

    /**
     * @return minimal level of tool required to dismantle this meta tile entity properly
     */
    public int getHarvestLevel() {
        return 1;
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        buf.writeByte(this.frontFacing.getIndex());
        buf.writeInt(this.paintingColor);
        buf.writeShort(this.mteTraitByNetworkId.size());
        for (Int2ObjectMap.Entry<MTETrait> entry : mteTraitByNetworkId.int2ObjectEntrySet()) {
            buf.writeVarInt(entry.getIntKey());
            entry.getValue().writeInitialData(buf);
        }
        CoverSaveHandler.writeInitialSyncData(buf, this);
        buf.writeBoolean(muffled);
    }

    public boolean isPainted() {
        return this.paintingColor != -1;
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        this.frontFacing = EnumFacing.VALUES[buf.readByte()];
        this.paintingColor = buf.readInt();
        int amountOfTraits = buf.readShort();
        for (int i = 0; i < amountOfTraits; i++) {
            int traitNetworkId = buf.readVarInt();
            MTETrait trait = mteTraitByNetworkId.get(traitNetworkId);
            if (trait == null) {
                GTLog.logger.warn("Could not find MTETrait for id: {} at position {}.", traitNetworkId, getPos());
            } else {
                trait.receiveInitialSyncData(buf);
            }
        }
        CoverSaveHandler.receiveInitialSyncData(buf, this);
        this.muffled = buf.readBoolean();
    }

    public void writeTraitData(MTETrait trait, int internalId, Consumer<PacketBuffer> dataWriter) {
        writeCustomData(SYNC_MTE_TRAITS, buffer -> {
            buffer.writeVarInt(trait.getNetworkID());
            buffer.writeVarInt(internalId);
            dataWriter.accept(buffer);
        });
    }

    @Override
    public void writeCoverData(@NotNull Cover cover, int discriminator, @NotNull Consumer<@NotNull PacketBuffer> buf) {
        writeCustomData(UPDATE_COVER_DATA_MTE, buffer -> {
            buffer.writeByte(cover.getAttachedSide().getIndex());
            buffer.writeVarInt(discriminator);
            buf.accept(buffer);
        });
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        if (dataId == UPDATE_FRONT_FACING) {
            this.frontFacing = EnumFacing.VALUES[buf.readByte()];
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_PAINTING_COLOR) {
            this.paintingColor = buf.readInt();
            scheduleRenderUpdate();
        } else if (dataId == SYNC_MTE_TRAITS) {
            int traitNetworkId = buf.readVarInt();
            int internalId = buf.readVarInt();
            MTETrait trait = mteTraitByNetworkId.get(traitNetworkId);
            if (trait == null) {
                GTLog.logger.warn("Could not find MTETrait for id: {} at position {}.", traitNetworkId, getPos());
            } else {
                ISyncedTileEntity.addCode(internalId, trait);
                trait.receiveCustomData(internalId, buf);
            }
        } else if (dataId == COVER_ATTACHED_MTE) {
            CoverSaveHandler.readCoverPlacement(buf, this);
        } else if (dataId == COVER_REMOVED_MTE) {
            // cover removed event
            EnumFacing placementSide = EnumFacing.VALUES[buf.readByte()];
            this.covers.remove(placementSide);
            onCoverPlacementUpdate();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_COVER_DATA_MTE) {
            // cover custom data received
            EnumFacing coverSide = EnumFacing.VALUES[buf.readByte()];
            Cover cover = getCoverAtSide(coverSide);
            int internalId = buf.readVarInt();
            if (cover != null) {
                ISyncedTileEntity.addCode(internalId, cover);
                cover.readCustomData(internalId, buf);
            }
        } else if (dataId == UPDATE_SOUND_MUFFLED) {
            this.muffled = buf.readBoolean();
            if (muffled) {
                GregTechAPI.soundManager.stopTileSound(getPos());
            }
        }
    }

    public BlockFaceShape getCoverFaceShape(EnumFacing side) {
        if (getCoverAtSide(side) != null) {
            return BlockFaceShape.SOLID; // covers are always solid
        }
        return getFaceShape(side);
    }

    public final <T> T getCoverCapability(Capability<T> capability, EnumFacing side) {
        boolean isCoverable = capability == GregtechTileCapabilities.CAPABILITY_COVER_HOLDER;
        Cover cover = side == null ? null : getCoverAtSide(side);
        T originalCapability = getCapability(capability, side);
        if (cover != null && !isCoverable) {
            return cover.getCapability(capability, originalCapability);
        }
        return originalCapability;
    }

    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_COVER_HOLDER) {
            return GregtechTileCapabilities.CAPABILITY_COVER_HOLDER.cast(this);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY &&
                getFluidInventory().getTankProperties().length > 0) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(getFluidInventory());
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY &&
                getItemInventory().getSlots() > 0) {
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getItemInventory());
                }
        T capabilityResult = null;
        for (MTETrait mteTrait : this.mteTraits.values()) {
            capabilityResult = mteTrait.getCapability(capability);
            if (capabilityResult != null) {
                break;
            }
        }
        if (side != null && capabilityResult instanceof IEnergyContainer) {
            IEnergyContainer energyContainer = (IEnergyContainer) capabilityResult;
            if (!energyContainer.inputsEnergy(side) && !energyContainer.outputsEnergy(side)) {
                return null; // do not provide energy container if it can't input or output energy at all
            }
        }
        return capabilityResult;
    }

    public void fillInternalTankFromFluidContainer() {
        fillInternalTankFromFluidContainer(importFluids);
    }

    public void fillInternalTankFromFluidContainer(IFluidHandler fluidHandler) {
        for (int i = 0; i < importItems.getSlots(); i++) {
            ItemStack inputContainerStack = importItems.extractItem(i, 1, true);
            FluidActionResult result = FluidUtil.tryEmptyContainer(inputContainerStack, fluidHandler, Integer.MAX_VALUE,
                    null, false);
            if (result.isSuccess()) {
                ItemStack remainingItem = result.getResult();
                if (ItemStack.areItemStacksEqual(inputContainerStack, remainingItem))
                    continue; // do not fill if item stacks match
                if (!remainingItem.isEmpty() && !GTTransferUtils.insertItem(exportItems, remainingItem, true).isEmpty())
                    continue; // do not fill if can't put remaining item
                FluidUtil.tryEmptyContainer(inputContainerStack, fluidHandler, Integer.MAX_VALUE, null, true);
                importItems.extractItem(i, 1, false);
                GTTransferUtils.insertItem(exportItems, remainingItem, false);
            }
        }
    }

    public void fillContainerFromInternalTank() {
        fillContainerFromInternalTank(exportFluids);
    }

    public void fillContainerFromInternalTank(IFluidHandler fluidHandler) {
        for (int i = 0; i < importItems.getSlots(); i++) {
            ItemStack emptyContainer = importItems.extractItem(i, 1, true);
            FluidActionResult result = FluidUtil.tryFillContainer(emptyContainer, fluidHandler, Integer.MAX_VALUE, null,
                    false);
            if (result.isSuccess()) {
                ItemStack remainingItem = result.getResult();
                if (!remainingItem.isEmpty() && !GTTransferUtils.insertItem(exportItems, remainingItem, true).isEmpty())
                    continue;
                FluidUtil.tryFillContainer(emptyContainer, fluidHandler, Integer.MAX_VALUE, null, true);
                importItems.extractItem(i, 1, false);
                GTTransferUtils.insertItem(exportItems, remainingItem, false);
            }
        }
    }

    public void pushFluidsIntoNearbyHandlers(EnumFacing... allowedFaces) {
        transferToNearby(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, GTTransferUtils::transferFluids,
                allowedFaces);
    }

    public void pullFluidsFromNearbyHandlers(EnumFacing... allowedFaces) {
        transferToNearby(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                (thisCap, otherCap) -> GTTransferUtils.transferFluids(otherCap, thisCap), allowedFaces);
    }

    public void pushItemsIntoNearbyHandlers(EnumFacing... allowedFaces) {
        transferToNearby(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, GTTransferUtils::moveInventoryItems,
                allowedFaces);
    }

    public void pullItemsFromNearbyHandlers(EnumFacing... allowedFaces) {
        transferToNearby(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                (thisCap, otherCap) -> GTTransferUtils.moveInventoryItems(otherCap, thisCap), allowedFaces);
    }

    private <T> void transferToNearby(Capability<T> capability, BiConsumer<T, T> transfer, EnumFacing... allowedFaces) {
        for (EnumFacing nearbyFacing : allowedFaces) {
            TileEntity tileEntity = getNeighbor(nearbyFacing);
            if (tileEntity == null) {
                continue;
            }
            T otherCap = tileEntity.getCapability(capability, nearbyFacing.getOpposite());
            // use getCoverCapability so item/ore dictionary filter covers will work properly
            T thisCap = getCoverCapability(capability, nearbyFacing);
            if (otherCap == null || thisCap == null) {
                continue;
            }
            transfer.accept(thisCap, otherCap);
        }
    }

    public final int getOutputRedstoneSignal(@Nullable EnumFacing side) {
        if (side == null) {
            return getHighestOutputRedstoneSignal();
        }
        Cover cover = getCoverAtSide(side);
        int sidedOutput = sidedRedstoneOutput[side.getIndex()];
        return cover == null ? sidedOutput : cover.getRedstoneSignalOutput();
    }

    public final int getHighestOutputRedstoneSignal() {
        int highestSignal = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            Cover cover = getCoverAtSide(side);
            int sidedOutput = sidedRedstoneOutput[side.getIndex()];
            int sideResult = cover == null ? sidedOutput : cover.getRedstoneSignalOutput();
            highestSignal = Math.max(highestSignal, sideResult);
        }
        return highestSignal;
    }

    public final void setOutputRedstoneSignal(EnumFacing side, int strength) {
        Preconditions.checkNotNull(side, "side");
        this.sidedRedstoneOutput[side.getIndex()] = strength;
        if (getWorld() != null && !getWorld().isRemote && getCoverAtSide(side) == null) {
            notifyBlockUpdate();
        }
    }

    @Override
    public void notifyBlockUpdate() {
        if (holder != null) holder.notifyBlockUpdate();
    }

    @Override
    public void scheduleRenderUpdate() {
        if (holder != null) holder.scheduleRenderUpdate();
    }

    public void setFrontFacing(EnumFacing frontFacing) {
        Preconditions.checkNotNull(frontFacing, "frontFacing");
        this.frontFacing = frontFacing;
        if (getWorld() != null && !getWorld().isRemote) {
            notifyBlockUpdate();
            markDirty();
            writeCustomData(UPDATE_FRONT_FACING, buf -> buf.writeByte(frontFacing.getIndex()));
            for (MTETrait mteTrait : this.mteTraits.values()) {
                mteTrait.onFrontFacingSet(frontFacing);
            }
        }
    }

    public void setPaintingColor(int paintingColor) {
        this.paintingColor = paintingColor;
        if (getWorld() != null && !getWorld().isRemote) {
            notifyBlockUpdate();
            markDirty();
            writeCustomData(UPDATE_PAINTING_COLOR, buf -> buf.writeInt(paintingColor));
        }
    }

    public int getDefaultPaintingColor() {
        return ConfigHolder.client.defaultPaintingColor;
    }

    public boolean isValidFrontFacing(EnumFacing facing) {
        if (this.hasFrontFacing() && getFrontFacing() == facing) return false;
        return facing != EnumFacing.UP && facing != EnumFacing.DOWN;
    }

    public boolean hasFrontFacing() {
        return true;
    }

    /**
     * @return true if this meta tile entity should serialize it's export and import inventories
     *         Useful when you use your own unified inventory and don't need these dummies to be saved
     */
    protected boolean shouldSerializeInventories() {
        return true;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("FrontFacing", frontFacing.getIndex());
        if (isPainted()) {
            data.setInteger(TAG_KEY_PAINTING_COLOR, paintingColor);
        }
        data.setInteger("CachedLightValue", cachedLightValue);

        if (shouldSerializeInventories()) {
            GTUtility.writeItems(importItems, "ImportInventory", data);
            GTUtility.writeItems(exportItems, "ExportInventory", data);

            data.setTag("ImportFluidInventory", importFluids.serializeNBT());
            data.setTag("ExportFluidInventory", exportFluids.serializeNBT());
        }

        for (MTETrait mteTrait : this.mteTraits.values()) {
            data.setTag(mteTrait.getName(), mteTrait.serializeNBT());
        }

        CoverSaveHandler.writeCoverNBT(data, this);

        data.setBoolean(TAG_KEY_MUFFLED, muffled);

        if (owner != null)
            data.setUniqueId("Owner", owner);

        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        this.frontFacing = EnumFacing.VALUES[data.getInteger("FrontFacing")];
        if (data.hasKey(TAG_KEY_PAINTING_COLOR)) {
            this.paintingColor = data.getInteger(TAG_KEY_PAINTING_COLOR);
        }
        this.cachedLightValue = data.getInteger("CachedLightValue");

        if (shouldSerializeInventories()) {
            GTUtility.readItems(importItems, "ImportInventory", data);
            GTUtility.readItems(exportItems, "ExportInventory", data);

            importFluids.deserializeNBT(data.getCompoundTag("ImportFluidInventory"));
            exportFluids.deserializeNBT(data.getCompoundTag("ExportFluidInventory"));
        }

        for (MTETrait mteTrait : this.mteTraits.values()) {
            NBTTagCompound traitCompound = data.getCompoundTag(mteTrait.getName());
            mteTrait.deserializeNBT(traitCompound);
        }

        CoverSaveHandler.readCoverNBT(data, this, covers::put);
        this.muffled = data.getBoolean(TAG_KEY_MUFFLED);

        if (data.hasKey("Owner"))
            this.owner = data.getUniqueId("Owner");
    }

    @Override
    public boolean isValid() {
        return getHolder() != null && getHolder().isValid();
    }

    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        clearInventory(itemBuffer, importItems);
        clearInventory(itemBuffer, exportItems);
    }

    public static void clearInventory(@NotNull List<@NotNull ItemStack> itemBuffer,
                                      @NotNull IItemHandlerModifiable inventory) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                itemBuffer.add(stackInSlot);
            }
        }
    }

    public int getItemStackLimit(ItemStack stack) {
        return 64;
    }

    /**
     * Called whenever a MetaTileEntity is placed in world by {@link Block#onBlockPlacedBy},
     * gives the MetaTileEntity an Owner by UUID
     * <p>
     * If placing an MTE with methods such as {@link World#setBlockState(BlockPos, IBlockState)},
     * this should be manually called immediately afterwards
     */
    public void onPlacement(@Nullable EntityLivingBase placer) {
        if (placer instanceof EntityPlayer player) {
            this.owner = player.getUniqueID();
        }
    }

    /**
     * Called whenever a MetaTileEntity is placed in world by {@link Block#onBlockPlacedBy},
     * gives the MetaTileEntity an Owner of Null
     * <p>
     * If placing an MTE with methods such as {@link World#setBlockState(BlockPos, IBlockState)},
     * this should be manually called immediately afterwards
     */
    public final void onPlacement() {
        onPlacement(null);
    }

    /**
     * Called from breakBlock right before meta tile entity destruction
     * at this stage tile entity inventory is already dropped on ground, but drops aren't fetched yet
     * tile entity will still get getDrops called after this, if player broke block
     */
    public void onRemoval() {}

    public void invalidate() {
        if (getWorld() != null && getWorld().isRemote) {
            GregTechAPI.soundManager.stopTileSound(getPos());
        }
    }

    @SideOnly(Side.CLIENT)
    public SoundEvent getSound() {
        return null;
    }

    public boolean isActive() {
        return false;
    }

    public @NotNull EnumFacing getFrontFacing() {
        return frontFacing;
    }

    public int getPaintingColor() {
        return paintingColor;
    }

    public IItemHandler getItemInventory() {
        return itemInventory;
    }

    public IFluidHandler getFluidInventory() {
        return fluidInventory;
    }

    public IItemHandlerModifiable getImportItems() {
        return importItems;
    }

    public IItemHandlerModifiable getExportItems() {
        return exportItems;
    }

    public FluidTankList getImportFluids() {
        return importFluids;
    }

    public FluidTankList getExportFluids() {
        return exportFluids;
    }

    public List<IItemHandlerModifiable> getNotifiedItemOutputList() {
        return notifiedItemOutputList;
    }

    public List<IItemHandlerModifiable> getNotifiedItemInputList() {
        return notifiedItemInputList;
    }

    public List<IFluidHandler> getNotifiedFluidInputList() {
        return notifiedFluidInputList;
    }

    public List<IFluidHandler> getNotifiedFluidOutputList() {
        return notifiedFluidOutputList;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean shouldDropWhenDestroyed() {
        return !wasExploded();
    }

    public float getBlockHardness() {
        return 6.0f;
    }

    public float getBlockResistance() {
        return 6.0f;
    }

    /**
     * Override this if the MTE will keep its Item inventory on-break.
     * If this is overridden to return True, you MUST take care to handle
     * the ItemStacks in the MTE's inventory otherwise they will be voided on break.
     *
     * @return True if MTE inventory is kept as an ItemStack, false otherwise
     */
    public boolean keepsInventory() {
        return false;
    }

    public boolean getWitherProof() {
        return false;
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    public final void toggleMuffled() {
        muffled = !muffled;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_SOUND_MUFFLED, buf -> buf.writeBoolean(muffled));
        }
    }

    public boolean isMuffled() {
        return muffled;
    }

    public boolean canRenderFrontFaceX() {
        return false;
    }

    public boolean isSideUsed(EnumFacing face) {
        if (getCoverAtSide(face) != null) return true;
        return face == this.getFrontFacing() && this.canRenderFrontFaceX();
    }

    /**
     * @return the MTE's {@link AbstractRecipeLogic}
     */
    @Nullable
    public final AbstractRecipeLogic getRecipeLogic() {
        MTETrait trait = getMTETrait(GregtechDataCodes.ABSTRACT_WORKABLE_TRAIT);
        if (trait instanceof AbstractRecipeLogic) {
            return ((AbstractRecipeLogic) trait);
        } else if (trait != null) {
            throw new IllegalStateException(
                    "MTE Trait " + trait.getName() + " has name " + GregtechDataCodes.ABSTRACT_WORKABLE_TRAIT +
                            " but is not instanceof AbstractRecipeLogic");
        }
        return null;
    }

    /**
     * @return the RecipeMap from the MTE's {@link AbstractRecipeLogic}
     */
    @Nullable
    public final RecipeMap<?> getRecipeMap() {
        AbstractRecipeLogic recipeLogic = getRecipeLogic();
        return recipeLogic == null ? null : recipeLogic.getRecipeMap();
    }

    public void checkWeatherOrTerrainExplosion(float explosionPower, double additionalFireChance,
                                               IEnergyContainer energyContainer) {
        World world = getWorld();
        if (!world.isRemote && ConfigHolder.machines.doTerrainExplosion && !getIsWeatherOrTerrainResistant() &&
                energyContainer.getEnergyStored() != 0) {
            if (GTValues.RNG.nextInt(1000) == 0) {
                for (EnumFacing side : EnumFacing.VALUES) {
                    Block block = getWorld().getBlockState(getPos().offset(side)).getBlock();
                    if (block == Blocks.FIRE || block == Blocks.WATER || block == Blocks.FLOWING_WATER ||
                            block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
                        doExplosion(explosionPower);
                        return;
                    }
                }
            }
            if (GTValues.RNG.nextInt(1000) == 0) {
                if (world.isRainingAt(getPos()) || world.isRainingAt(getPos().east()) ||
                        world.isRainingAt(getPos().west()) || world.isRainingAt(getPos().north()) ||
                        world.isRainingAt(getPos().south())) {
                    if (world.isThundering() && GTValues.RNG.nextInt(3) == 0) {
                        doExplosion(explosionPower);
                    } else if (GTValues.RNG.nextInt(10) == 0) {
                        doExplosion(explosionPower);
                    } else setOnFire(additionalFireChance);
                }
            }
        }
    }

    public void doExplosion(float explosionPower) {
        setExploded();
        getWorld().setBlockToAir(getPos());
        getWorld().createExplosion(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                explosionPower, ConfigHolder.machines.doesExplosionDamagesTerrain);
    }

    /**
     * Mark the MTE as having been blown up by an explosion
     */
    protected final void setExploded() {
        this.wasExploded = true;
    }

    /**
     * @return if the MTE was blown up by an explosion
     */
    protected final boolean wasExploded() {
        return this.wasExploded;
    }

    public void setOnFire(double additionalFireChance) {
        boolean isFirstFireSpawned = false;
        for (EnumFacing side : EnumFacing.VALUES) {
            if (getWorld().isAirBlock(getPos().offset(side))) {
                if (!isFirstFireSpawned) {
                    getWorld().setBlockState(getPos().offset(side), Blocks.FIRE.getDefaultState(), 11);
                    if (!getWorld().isAirBlock(getPos().offset(side))) {
                        isFirstFireSpawned = true;
                    }
                } else if (additionalFireChance >= GTValues.RNG.nextDouble() * 100) {
                    getWorld().setBlockState(getPos().offset(side), Blocks.FIRE.getDefaultState(), 11);
                }
            }
        }
    }

    /**
     * Whether this tile entity not explode in rain, fire, water or lava
     *
     * @return true if tile entity should not explode in these sources
     */
    public boolean getIsWeatherOrTerrainResistant() {
        return false;
    }

    public boolean doTickProfileMessage() {
        return true;
    }

    public boolean canRenderMachineGrid(@NotNull ItemStack mainHandStack, @NotNull ItemStack offHandStack) {
        final String[] tools = { ToolClasses.WRENCH, ToolClasses.SCREWDRIVER };
        return ToolHelper.isTool(mainHandStack, tools) ||
                ToolHelper.isTool(offHandStack, tools);
    }

    public float getVolume() {
        return 1.0f;
    }

    @Override
    public boolean canVoidRecipeItemOutputs() {
        return false;
    }

    @Override
    public boolean canVoidRecipeFluidOutputs() {
        return false;
    }

    @NotNull
    @Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public AECableType getCableConnectionType(@NotNull AEPartLocation part) {
        return AECableType.NONE;
    }

    @Nullable
    @Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public AENetworkProxy getProxy() {
        return null;
    }

    @Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public void gridChanged() {}

    /**
     * Add MTE to a creative tab. Ensure that the creative tab has been registered via
     * {@link gregtech.api.block.machines.MachineItemBlock#addCreativeTab(CreativeTabs)
     * MachineItemBlock#addCreativeTab(CreativeTabs)} beforehand.
     */
    public void addAdditionalCreativeTabs(CreativeTabs creativeTab) {
        Preconditions.checkNotNull(creativeTab, "creativeTab");
        if (creativeTabs.contains(creativeTab)) {
            GTLog.logger.error("{} is already in the creative tab {}.", this, creativeTab.tabLabel,
                    new IllegalArgumentException());
            return;
        }

        creativeTabs.add(creativeTab);
    }

    public void removeFromCreativeTab(CreativeTabs creativeTab) {
        Preconditions.checkNotNull(creativeTab, "creativeTab");
        if (creativeTab == CreativeTabs.SEARCH) {
            GTLog.logger.error("Cannot remove MTEs from the creative search tab.",
                    new IllegalArgumentException());
            return;
        }
        if (creativeTab == GTCreativeTabs.TAB_GREGTECH_MACHINES &&
                metaTileEntityId.getNamespace().equals(GTValues.MODID)) {
            GTLog.logger.error("Cannot remove GT MTEs from the GT machines tab.", new IllegalArgumentException());
            return;
        }
        if (!creativeTabs.contains(creativeTab)) {
            GTLog.logger.error("{} is not in the creative tab {}.", this, creativeTab.tabLabel,
                    new IllegalArgumentException());
            return;
        }

        creativeTabs.remove(creativeTab);
    }

    public Set<CreativeTabs> getCreativeTabs() {
        return Collections.unmodifiableSet(creativeTabs);
    }
}
