package gregtech.common.metatileentities;

import gregtech.api.GregTechAPI;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.FakeModularGui;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.itemhandlers.InaccessibleItemStackHandler;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.util.GTLog;
import gregtech.api.util.GregFakePlayer;
import gregtech.client.renderer.texture.custom.ClipboardRenderer;
import gregtech.common.gui.impl.FakeModularUIContainerClipboard;
import gregtech.common.items.behaviors.ClipboardBehavior;
import gregtech.core.network.packets.PacketClipboardNBTUpdate;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static codechicken.lib.raytracer.RayTracer.*;
import static gregtech.api.capability.GregtechDataCodes.*;
import static gregtech.client.renderer.texture.Textures.CLIPBOARD_RENDERER;
import static gregtech.common.items.MetaItems.CLIPBOARD;

public class MetaTileEntityClipboard extends MetaTileEntity implements IFastRenderMetaTileEntity {

    private static final AxisAlignedBB CLIPBOARD_AABB_NORTH = new AxisAlignedBB(2.75 / 16.0, 0, 0, 13.25 / 16.0,
            16 / 16.0, 0.4 / 16.0);
    private static final AxisAlignedBB CLIPBOARD_AABB_SOUTH = new AxisAlignedBB(13.25 / 16.0, 0, 16 / 16.0, 2.75 / 16.0,
            16 / 16.0, 15.6 / 16.0);
    private static final AxisAlignedBB CLIPBOARD_AABB_WEST = new AxisAlignedBB(0, 0, 13.25 / 16.0, 0.4 / 16.0,
            16 / 16.0, 2.75 / 16.0);
    private static final AxisAlignedBB CLIPBOARD_AABB_EAST = new AxisAlignedBB(16 / 16.0, 0, 2.75 / 16.0, 15.6 / 16.0,
            16 / 16.0, 13.25 / 16.0);

    private static final AxisAlignedBB PAGE_AABB_NORTH = new AxisAlignedBB(3 / 16.0, 0.25 / 16.0, 0.25 / 16.0,
            13 / 16.0, 14.25 / 16.0, 0.3 / 16.0);
    private static final AxisAlignedBB PAGE_AABB_SOUTH = new AxisAlignedBB(13 / 16.0, 0.25 / 16.0, 15.75 / 16.0,
            3 / 16.0, 14.25 / 16.0, 15.7 / 16.0);
    private static final AxisAlignedBB PAGE_AABB_WEST = new AxisAlignedBB(0.25 / 16.0, 0.25 / 16.0, 13 / 16.0,
            0.3 / 16.0, 14.25 / 16.0, 3 / 16.0);
    private static final AxisAlignedBB PAGE_AABB_EAST = new AxisAlignedBB(15.75 / 16.0, 0.25 / 16.0, 3 / 16.0,
            15.7 / 16.0, 14.25 / 16.0, 13 / 16.0);

    public static final float scale = 1;
    public FakeModularGui guiCache;
    public FakeModularUIContainerClipboard guiContainerCache;
    private static final NBTBase NO_CLIPBOARD_SIG = new NBTTagInt(0);
    private boolean didSetFacing = false;

    public MetaTileEntityClipboard(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public void update() {
        super.update();
        if (guiContainerCache == null) {
            createFakeGui();
            scheduleRenderUpdate();
        }
        if (this.getWorld().isRemote) {
            if (guiCache != null)
                guiCache.updateScreen();
        }
        if (guiContainerCache != null)
            guiContainerCache.detectAndSendChanges();
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }

    @Override
    public void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {
        ClipboardRenderer.renderBoard(renderState, translation.copy(), new IVertexOperation[] {}, getFrontFacing(),
                this, partialTicks);
    }

    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (this.getClipboard() != null)
            ClipboardRenderer.renderGUI(x, y, z, this.getFrontFacing(), this, partialTicks);
    }

    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(2, 2, 2));
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        return null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityClipboard(metaTileEntityId);
    }

    @Override
    public ModularUI createUI(EntityPlayer entityPlayer) {
        if (getClipboard().isItemEqual(CLIPBOARD.getStackForm())) {
            List<IItemBehaviour> behaviours = ((MetaItem<?>) getClipboard().getItem()).getBehaviours(getClipboard());
            Optional<IItemBehaviour> clipboardBehaviour = behaviours.stream()
                    .filter((x) -> x instanceof ClipboardBehavior).findFirst();
            if (!clipboardBehaviour.isPresent())
                return null;
            if (clipboardBehaviour.get() instanceof ClipboardBehavior) {
                PlayerInventoryHolder holder = new PlayerInventoryHolder(new GregFakePlayer(entityPlayer.world),
                        EnumHand.MAIN_HAND); // We can't have this actually set the player's hand
                holder.setCustomValidityCheck(this::isValid).setCurrentItem(this.getClipboard());
                if (entityPlayer instanceof GregFakePlayer) { // This is how to tell if this is being called in-world or
                                                              // not
                    return ClipboardBehavior.createMTEUI(holder, entityPlayer);
                } else {
                    return ((ClipboardBehavior) clipboardBehaviour.get()).createUI(holder, entityPlayer);
                }
            }
        }
        return null;
    }

    public void createFakeGui() {
        // Basically just the original function from the PluginBehavior, but with a lot of now useless stuff stripped
        // out.
        try {
            GregFakePlayer fakePlayer = new GregFakePlayer(this.getWorld());
            fakePlayer.setHeldItem(EnumHand.MAIN_HAND, this.getClipboard());
            ModularUI ui = this.createUI(fakePlayer);

            ModularUI.Builder builder = new ModularUI.Builder(ui.backgroundPath, ui.getWidth(), ui.getHeight());
            builder.shouldColor(false);

            List<Widget> widgets = new ArrayList<>(ui.guiWidgets.values());

            for (Widget widget : widgets) {
                builder.widget(widget);
            }
            ui = builder.build(ui.holder, ui.entityPlayer);
            FakeModularUIContainerClipboard fakeModularUIContainer = new FakeModularUIContainerClipboard(ui, this);
            this.guiContainerCache = fakeModularUIContainer;
            if (getWorld().isRemote)
                this.guiCache = new FakeModularGui(ui, fakeModularUIContainer);
            this.writeCustomData(CREATE_FAKE_UI);
        } catch (Exception e) {
            GTLog.logger.error(e);
        }
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.itemInventory = new InaccessibleItemStackHandler(this);
    }

    public ItemStack getClipboard() {
        if (this.itemInventory.getStackInSlot(0) == ItemStack.EMPTY) {
            ((InaccessibleItemStackHandler) this.itemInventory).setStackInSlot(0, CLIPBOARD.getStackForm());
        }
        return this.itemInventory.getStackInSlot(0);
    }

    public void initializeClipboard(ItemStack stack) {
        ((InaccessibleItemStackHandler) this.itemInventory).setStackInSlot(0, stack.copy());
        writeCustomData(INIT_CLIPBOARD_NBT, buf -> {
            buf.writeCompoundTag(stack.getTagCompound());
        });
    }

    public void setClipboard(ItemStack stack) {
        ((InaccessibleItemStackHandler) this.itemInventory).setStackInSlot(0, stack.copy());
    }

    @Override
    public void getDrops(@NotNull List<@NotNull ItemStack> dropsList, @Nullable EntityPlayer harvester) {
        dropsList.clear();
        dropsList.add(this.getClipboard());
    }

    @Override
    public float getBlockHardness() {
        return 100;
    }

    @Override
    public int getHarvestLevel() {
        return 4;
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getWorld() != null && !getWorld().isRemote) {
                MetaTileEntityUIFactory.INSTANCE.openUI(getHolder(), (EntityPlayerMP) playerIn);
            }
        } else {
            breakClipboard(playerIn);
        }
        return true;
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide,
                                 CuboidRayTraceResult hitResult) {
        return false;
    }

    private void breakClipboard(@Nullable EntityPlayer player) {
        if (!getWorld().isRemote) {
            BlockPos pos = this.getPos(); // Saving this for later so it doesn't get mangled
            World world = this.getWorld(); // Same here

            List<ItemStack> drops = new ArrayList<>();
            getDrops(drops, player);

            Block.spawnAsEntity(getWorld(), pos, drops.get(0));
            this.dropAllCovers();
            this.onRemoval();

            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
    }

    @Override
    public void onNeighborChanged() {
        if (!getWorld().isRemote && didSetFacing) {
            BlockPos pos = getPos().offset(getFrontFacing());
            IBlockState state = getWorld().getBlockState(pos);
            if (state.getBlock().isAir(state, getWorld(), pos) ||
                    !state.isSideSolid(getWorld(), pos, getFrontFacing())) {
                breakClipboard(null);
            }
        }
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        this.didSetFacing = true;
    }

    @Override
    public String getHarvestTool() {
        return ToolClasses.AXE;
    }

    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        AxisAlignedBB aabb;
        switch (this.getFrontFacing()) {
            case SOUTH:
                aabb = CLIPBOARD_AABB_SOUTH;
                break;
            case WEST:
                aabb = CLIPBOARD_AABB_WEST;
                break;
            case EAST:
                aabb = CLIPBOARD_AABB_EAST;
                break;
            default:
                aabb = CLIPBOARD_AABB_NORTH;
        }
        collisionList.add(new IndexedCuboid6(null, aabb));
    }

    public IndexedCuboid6 getPageCuboid() {
        AxisAlignedBB aabb;
        switch (this.getFrontFacing()) {
            case SOUTH:
                aabb = PAGE_AABB_SOUTH;
                break;
            case WEST:
                aabb = PAGE_AABB_WEST;
                break;
            case EAST:
                aabb = PAGE_AABB_EAST;
                break;
            default:
                aabb = PAGE_AABB_NORTH;
        }
        return new IndexedCuboid6(null, aabb);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(CLIPBOARD_RENDERER.getParticleTexture(), 0xFFFFFF);
    }

    public Pair<Double, Double> checkLookingAt(EntityPlayer player) {
        if (this.getWorld() != null && player != null) {
            Vec3d startVec = getStartVec(player);
            Vec3d endVec = getEndVec(player);
            CuboidRayTraceResult rayTraceResult = rayTrace(this.getPos(), new Vector3(startVec), new Vector3(endVec),
                    getPageCuboid());
            if (rayTraceResult != null && rayTraceResult.sideHit == this.getFrontFacing().getOpposite()) {
                TileEntity tileEntity = this.getWorld().getTileEntity(rayTraceResult.getBlockPos());
                if (tileEntity instanceof IGregTechTileEntity &&
                        ((IGregTechTileEntity) tileEntity).getMetaTileEntity() instanceof MetaTileEntityClipboard) {
                    double[] pos = handleRayTraceResult(rayTraceResult, this.getFrontFacing().getOpposite());
                    if (pos[0] >= 0 && pos[0] <= 1 && pos[1] >= 0 && pos[1] <= 1)
                        return Pair.of(pos[0], pos[1]);
                }
            }
        }
        return null;
    }

    private static double[] handleRayTraceResult(CuboidRayTraceResult rayTraceResult, EnumFacing spin) {
        double x, y;
        double dX = rayTraceResult.sideHit.getAxis() == EnumFacing.Axis.X ?
                rayTraceResult.hitVec.z - rayTraceResult.getBlockPos().getZ() :
                rayTraceResult.hitVec.x - rayTraceResult.getBlockPos().getX();
        double dY = rayTraceResult.sideHit.getAxis() == EnumFacing.Axis.Y ?
                rayTraceResult.hitVec.z - rayTraceResult.getBlockPos().getZ() :
                rayTraceResult.hitVec.y - rayTraceResult.getBlockPos().getY();
        if (spin == EnumFacing.NORTH) {
            x = 1 - dX;
        } else if (spin == EnumFacing.SOUTH) {
            x = dX;
        } else if (spin == EnumFacing.EAST) {
            x = 1 - dX;
            if (rayTraceResult.sideHit.getXOffset() < 0 || rayTraceResult.sideHit.getZOffset() > 0) {
                x = 1 - x;
            }
        } else {
            x = 1 - dX;
            if (rayTraceResult.sideHit.getXOffset() < 0 || rayTraceResult.sideHit.getZOffset() > 0) {
                x = 1 - x;
            }
        }

        y = 1 - dY; // Since y values are quite weird here

        // Scale these to be 0 - 1
        x -= 3.0 / 16;
        y -= 1.75 / 16;
        x /= 14.0 / 16;
        y /= 14.0 / 16;

        return new double[] { x, y };
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if (this.getClipboard() != null && this.getClipboard().getTagCompound() != null)
            data.setTag("clipboardNBT", this.getClipboard().getTagCompound());
        else
            data.setTag("clipboardNBT", NO_CLIPBOARD_SIG);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        NBTBase clipboardNBT = data.getTag("clipboardNBT");
        if (clipboardNBT != NO_CLIPBOARD_SIG && clipboardNBT instanceof NBTTagCompound) {
            ItemStack clipboard = this.getClipboard();
            clipboard.setTagCompound((NBTTagCompound) clipboardNBT);
            this.setClipboard(clipboard);
        }
    }

    public void setClipboardNBT(NBTTagCompound data) {
        ItemStack clipboard = this.getClipboard();
        clipboard.setTagCompound(data);
        this.setClipboard(clipboard);
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.writeInitialSyncData(buf);
        if (this.getClipboard() != null && this.getClipboard().getTagCompound() != null)
            buf.writeCompoundTag(this.getClipboard().getTagCompound());
        else {
            buf.writeCompoundTag(new NBTTagCompound());
        }
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        try {
            NBTTagCompound clipboardNBT = buf.readCompoundTag();
            if (clipboardNBT != null && !clipboardNBT.equals(new NBTTagCompound())) {
                ItemStack clipboard = this.getClipboard();
                clipboard.setTagCompound(clipboardNBT);
                this.setClipboard(clipboard);
            }
        } catch (Exception e) {
            GTLog.logger.error("Could not initialize Clipboard from InitialSyncData buffer", e);
        }
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId >= UPDATE_UI) {
            int windowID = buf.readVarInt();
            int widgetID = buf.readVarInt();
            if (guiCache != null)
                guiCache.handleWidgetUpdate(windowID, widgetID, buf);
            this.scheduleRenderUpdate();
            this.sendNBTToServer();
        } else if (dataId == CREATE_FAKE_UI) {
            createFakeGui();
            this.scheduleRenderUpdate();
        } else if (dataId == MOUSE_POSITION) {
            int mouseX = buf.readVarInt();
            int mouseY = buf.readVarInt();
            if (guiCache != null && guiContainerCache != null) {
                guiCache.mouseClicked(mouseX, mouseY, 0); // Left mouse button
            }
            this.scheduleRenderUpdate();
            this.sendNBTToServer();
        } else if (dataId == INIT_CLIPBOARD_NBT) {
            try {
                NBTTagCompound clipboardNBT = buf.readCompoundTag();
                if (clipboardNBT != NO_CLIPBOARD_SIG) {
                    ItemStack clipboard = this.getClipboard();
                    clipboard.setTagCompound(clipboardNBT);
                    this.setClipboard(clipboard);
                }
            } catch (Exception e) {
                GTLog.logger.error("Could not read Clipboard Init NBT from CustomData buffer", e);
            }
        }
    }

    private void sendNBTToServer() {
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
        packetBuffer.writeCompoundTag(this.getClipboard().getTagCompound());
        GregTechAPI.networkHandler.sendToServer(new PacketClipboardNBTUpdate(
                this.getWorld().provider.getDimension(),
                this.getPos(),
                1, packetBuffer));
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) { // JEI shouldn't show this
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {}

    public void readUIAction(EntityPlayerMP player, int id, PacketBuffer buf) {
        if (id == 1) {
            if (this.guiContainerCache != null) {
                guiContainerCache.handleClientAction(buf);
            }
        }
    }

    @Override
    public void onLeftClick(EntityPlayer player, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (this.getWorld().isRemote) return;
        Pair<Double, Double> clickCoords = this.checkLookingAt(player);
        int width = 178; // These should always be correct.
        int height = 230;
        double scale = 1.0 / Math.max(width, height);
        int mouseX = (int) ((clickCoords.getLeft() / scale));
        int mouseY = (int) ((clickCoords.getRight() / scale));
        if (0 <= mouseX && mouseX <= width && 0 <= mouseY && mouseY <= height) {
            this.writeCustomData(MOUSE_POSITION, buf -> {
                buf.writeVarInt(mouseX);
                buf.writeVarInt(mouseY);
            });
        }
    }

    @Override
    public boolean canPlaceCoverOnSide(@NotNull EnumFacing side) {
        return false;
    }

    @Override
    public boolean acceptsCovers() {
        return false;
    }

    @Override
    public boolean canRenderMachineGrid(@NotNull ItemStack mainHandStack, @NotNull ItemStack offHandStack) {
        return false;
    }

    @Override
    public boolean showToolUsages() {
        return false;
    }

    @Override
    public ItemStack getPickItem(EntityPlayer player) {
        return this.getClipboard();
    }
}
