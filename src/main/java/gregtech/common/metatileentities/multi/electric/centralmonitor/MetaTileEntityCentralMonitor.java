package gregtech.common.metatileentities.multi.electric.centralmonitor;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.cover.Cover;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.util.FacingPos;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.covers.CoverDigitalInterface;
import gregtech.common.gui.widget.monitor.WidgetScreenGrid;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.pipelike.cable.net.EnergyNet;
import gregtech.common.pipelike.cable.net.WorldENet;
import gregtech.common.pipelike.cable.tile.TileEntityCable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.lang.ref.WeakReference;
import java.util.*;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityCentralMonitor extends MultiblockWithDisplayBase implements IFastRenderMetaTileEntity {

    private final static long ENERGY_COST = -ConfigHolder.machines.centralMonitorEuCost;
    public final static int MAX_HEIGHT = 9;
    public final static int MAX_WIDTH = 14;
    // run-time data
    public int width;
    private long lastUpdate;
    private WeakReference<EnergyNet> currentEnergyNet;
    private List<BlockPos> activeNodes;
    private Set<FacingPos> netCovers;
    private Set<FacingPos> remoteCovers;
    @SideOnly(Side.CLIENT)
    public List<BlockPos> parts;
    public MetaTileEntityMonitorScreen[][] screens;
    private boolean isActive;
    private EnergyContainerList inputEnergy;
    // persistent data
    public int height = 3;

    public MetaTileEntityCentralMonitor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    private EnergyNet getEnergyNet() {
        if (!this.getWorld().isRemote) {
            TileEntity te = getNeighbor(frontFacing.getOpposite());
            if (te instanceof TileEntityCable) {
                TileEntityPipeBase<?, ?> tileEntityCable = (TileEntityCable) te;
                EnergyNet currentEnergyNet = this.currentEnergyNet.get();
                if (currentEnergyNet != null && currentEnergyNet.isValid() &&
                        currentEnergyNet.containsNode(tileEntityCable.getPipePos())) {
                    return currentEnergyNet; // return current net if it is still valid
                }
                WorldENet worldENet = (WorldENet) tileEntityCable.getPipeBlock()
                        .getWorldPipeNet(tileEntityCable.getPipeWorld());
                currentEnergyNet = worldENet.getNetFromPos(tileEntityCable.getPipePos());
                if (currentEnergyNet != null) {
                    this.currentEnergyNet = new WeakReference<>(currentEnergyNet);
                }
                return currentEnergyNet;
            }
        }
        return null;
    }

    private void updateNodes() {
        EnergyNet energyNet = getEnergyNet();
        if (energyNet == null) {
            activeNodes.clear();
            return;
        }
        if (energyNet.getLastUpdate() == lastUpdate) {
            return;
        }
        lastUpdate = energyNet.getLastUpdate();
        activeNodes.clear();
        energyNet.getAllNodes().forEach((pos, node) -> {
            if (node.isActive) {
                activeNodes.add(pos);
            }
        });
    }

    public void addRemoteCover(FacingPos cover) {
        if (remoteCovers != null) {
            if (remoteCovers.add(cover)) {
                writeCustomData(GregtechDataCodes.UPDATE_COVERS, this::writeCovers);
            }
        }
    }

    private boolean checkCovers() {
        boolean dirty = false;
        updateNodes();
        Set<FacingPos> checkCovers = new HashSet<>();
        World world = this.getWorld();
        for (BlockPos pos : activeNodes) {
            TileEntity tileEntityCable = world.getTileEntity(pos);
            if (!(tileEntityCable instanceof TileEntityPipeBase)) {
                continue;
            }
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (((IPipeTile<?, ?>) tileEntityCable).isConnected(facing)) {
                    TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
                    if (tileEntity instanceof IGregTechTileEntity) {
                        MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                        if (metaTileEntity != null) {
                            Cover cover = metaTileEntity.getCoverAtSide(facing.getOpposite());
                            if (cover instanceof CoverDigitalInterface digitalInterface && digitalInterface.isProxy()) {
                                checkCovers.add(new FacingPos(metaTileEntity.getPos(), cover.getAttachedSide()));
                            }
                        }
                    }
                }
            }
        }
        Iterator<FacingPos> iterator = remoteCovers.iterator();
        while (iterator.hasNext()) {
            FacingPos blockPosFace = iterator.next();
            TileEntity tileEntity = world.getTileEntity(blockPosFace.getPos());
            if (tileEntity instanceof IGregTechTileEntity) {
                MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                if (metaTileEntity != null) {
                    Cover cover = metaTileEntity.getCoverAtSide(blockPosFace.getFacing());
                    if (cover instanceof CoverDigitalInterface digitalInterface && digitalInterface.isProxy()) {
                        continue;
                    }
                }
            }
            iterator.remove();
            dirty = true;
        }
        if (checkCovers.size() != netCovers.size() || !netCovers.containsAll(checkCovers)) {
            netCovers = checkCovers;
            dirty = true;
        }
        return dirty;
    }

    private void writeCovers(PacketBuffer buf) {
        if (netCovers == null) {
            buf.writeInt(0);
        } else {
            buf.writeInt(netCovers.size());
            for (FacingPos cover : netCovers) {
                buf.writeBlockPos(cover.getPos());
                buf.writeByte(cover.getFacing().getIndex());
            }
        }
        if (remoteCovers == null) {
            buf.writeInt(0);
        } else {
            buf.writeInt(remoteCovers.size());
            for (FacingPos cover : remoteCovers) {
                buf.writeBlockPos(cover.getPos());
                buf.writeByte(cover.getFacing().getIndex());
            }
        }
    }

    private void readCovers(PacketBuffer buf) {
        netCovers = new HashSet<>();
        remoteCovers = new HashSet<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            netCovers.add(new FacingPos(buf.readBlockPos(), EnumFacing.byIndex(buf.readByte())));
        }
        size = buf.readInt();
        for (int i = 0; i < size; i++) {
            remoteCovers.add(new FacingPos(buf.readBlockPos(), EnumFacing.byIndex(buf.readByte())));
        }
    }

    private void writeParts(PacketBuffer buf) {
        buf.writeInt(
                (int) this.getMultiblockParts().stream().filter(MetaTileEntityMonitorScreen.class::isInstance).count());
        this.getMultiblockParts().forEach(part -> {
            if (part instanceof MetaTileEntityMonitorScreen) {
                buf.writeBlockPos(((MetaTileEntityMonitorScreen) part).getPos());
            }
        });
    }

    private void readParts(PacketBuffer buf) {
        parts = new ArrayList<>();
        clearScreens();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            parts.add(buf.readBlockPos());
        }
    }

    public void setHeight(int height) {
        if (this.height == height || height < 2 || height > MAX_HEIGHT) return;
        this.height = height;
        reinitializeStructurePattern();
        checkStructurePattern();
        writeCustomData(GregtechDataCodes.UPDATE_HEIGHT, buf -> buf.writeInt(height));
    }

    private void setActive(boolean isActive) {
        if (isActive == this.isActive) return;
        this.isActive = isActive;
        writeCustomData(GregtechDataCodes.UPDATE_ACTIVE, buf -> buf.writeBoolean(this.isActive));
    }

    public boolean isActive() {
        return isStructureFormed() && isActive;
    }

    private void clearScreens() {
        if (screens != null) {
            for (MetaTileEntityMonitorScreen[] screen : screens) {
                for (MetaTileEntityMonitorScreen s : screen) {
                    if (s != null) s.removeFromMultiBlock(this);
                }
            }
        }
        screens = new MetaTileEntityMonitorScreen[width][height];
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.SCREEN.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.COVER_INTERFACE_PROXY.renderSided(getFrontFacing().getOpposite(), renderState, translation, pipeline);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentTranslation("gregtech.multiblock.central_monitor.height", this.height));
        if (!isStructureFormed()) {
            ITextComponent buttonText = new TextComponentTranslation(
                    "gregtech.multiblock.central_monitor.height_modify", height);
            buttonText.appendText(" ");
            buttonText.appendSibling(AdvancedTextWidget.withButton(new TextComponentString("[-]"), "sub"));
            buttonText.appendText(" ");
            buttonText.appendSibling(AdvancedTextWidget.withButton(new TextComponentString("[+]"), "add"));
            textList.add(buttonText);
        } else {
            textList.add(new TextComponentTranslation("gregtech.multiblock.central_monitor.width", this.width));
            textList.add(new TextComponentTranslation("gregtech.multiblock.central_monitor.low_power"));
        }
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        super.handleDisplayClick(componentData, clickData);
        int modifier = componentData.equals("add") ? 1 : -1;
        setHeight(this.height + modifier);
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(width);
        buf.writeInt(height);
        buf.writeBoolean(isActive);
        writeCovers(buf);
        writeParts(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.width = buf.readInt();
        this.height = buf.readInt();
        this.isActive = buf.readBoolean();
        readCovers(buf);
        readParts(buf);
    }

    @Override
    public void receiveCustomData(int id, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(id, buf);
        if (id == GregtechDataCodes.UPDATE_ALL) {
            this.width = buf.readInt();
            this.height = buf.readInt();
            readCovers(buf);
            readParts(buf);
        } else if (id == GregtechDataCodes.UPDATE_COVERS) {
            readCovers(buf);
        } else if (id == GregtechDataCodes.UPDATE_HEIGHT) {
            this.height = buf.readInt();
            this.reinitializeStructurePattern();
        } else if (id == GregtechDataCodes.UPDATE_ACTIVE) {
            this.isActive = buf.readBoolean();
        } else if (id == GregtechDataCodes.STRUCTURE_FORMED) {
            if (!this.isStructureFormed()) {
                clearScreens();
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("screenH", this.height);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.height = data.hasKey("screenH") ? data.getInteger("screenH") : this.height;
        reinitializeStructurePattern();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityCentralMonitor(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        if (this.getOffsetTimer() % 20 == 0) {
            setActive(inputEnergy.changeEnergy(ENERGY_COST * this.getMultiblockParts().size()) ==
                    ENERGY_COST * this.getMultiblockParts().size());
            if (checkCovers()) {
                this.getMultiblockParts().forEach(part -> {
                    Set<FacingPos> covers = getAllCovers();
                    if (part instanceof MetaTileEntityMonitorScreen) {
                        ((MetaTileEntityMonitorScreen) part).updateCoverValid(covers);
                    }
                });
                writeCustomData(GregtechDataCodes.UPDATE_COVERS, this::writeCovers);
            }
        }
    }

    public Set<FacingPos> getAllCovers() {
        Set<FacingPos> allCovers = new HashSet<>();
        if (netCovers != null) {
            allCovers.addAll(netCovers);
        }
        if (remoteCovers != null) {
            allCovers.addAll(remoteCovers);
        }
        return allCovers;
    }

    @Override
    protected BlockPattern createStructurePattern() {
        StringBuilder start = new StringBuilder("AS");
        StringBuilder slice = new StringBuilder("BB");
        StringBuilder end = new StringBuilder("AA");
        for (int i = 0; i < height - 2; i++) {
            start.append('A');
            slice.append('B');
            end.append('A');
        }
        return FactoryBlockPattern.start(UP, BACK, RIGHT)
                .aisle(start.toString())
                .aisle(slice.toString()).setRepeatable(3, MAX_WIDTH)
                .aisle(end.toString())
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3)
                                .setPreviewCount(1)))
                .where('B', metaTileEntities(MetaTileEntities.MONITOR_SCREEN))
                .build();
    }

    @Override
    public String[] getDescription() {
        return new String[] { I18n.format("gregtech.multiblock.central_monitor.tooltip.1") };
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        lastUpdate = 0;
        currentEnergyNet = new WeakReference<>(null);
        activeNodes = new ArrayList<>();
        netCovers = new HashSet<>();
        remoteCovers = new HashSet<>();
        inputEnergy = new EnergyContainerList(this.getAbilities(MultiblockAbility.INPUT_ENERGY));
        width = 0;
        checkCovers();
        for (IMultiblockPart part : this.getMultiblockParts()) {
            if (part instanceof MetaTileEntityMonitorScreen) {
                width++;
            }
        }
        width = width / height;
        screens = new MetaTileEntityMonitorScreen[width][height];
        for (IMultiblockPart part : this.getMultiblockParts()) {
            if (part instanceof MetaTileEntityMonitorScreen) {
                MetaTileEntityMonitorScreen screen = (MetaTileEntityMonitorScreen) part;
                screens[screen.getX()][screen.getY()] = screen;
            }
        }
        writeCustomData(GregtechDataCodes.UPDATE_ALL, packetBuffer -> {
            packetBuffer.writeInt(width);
            packetBuffer.writeInt(height);
            writeCovers(packetBuffer);
            writeParts(packetBuffer);
        });
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (side == this.frontFacing.getOpposite() && capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(IEnergyContainer.DEFAULT);
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        if (this.isStructureFormed()) {
            return pass == 0;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isGlobalRenderer() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (!this.isStructureFormed()) return;
        RenderUtil.useStencil(() -> {
            GlStateManager.pushMatrix();
            RenderUtil.moveToFace(x, y, z, this.frontFacing);
            RenderUtil.rotateToFace(this.frontFacing, this.upwardsFacing);
            RenderUtil.renderRect(0.5f, -0.5f - (height - 2), width, height, 0.001f, 0xFF000000);
            GlStateManager.popMatrix();
        }, () -> {
            if (isActive) {
                GlStateManager.pushMatrix();
                /* hack the lightmap */
                GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
                net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
                float lastBrightnessX = OpenGlHelper.lastBrightnessX;
                float lastBrightnessY = OpenGlHelper.lastBrightnessY;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
                EntityPlayer player = Minecraft.getMinecraft().player;
                RayTraceResult rayTraceResult = player == null ? null : player
                        .rayTrace(Minecraft.getMinecraft().playerController.getBlockReachDistance(), partialTicks);
                int size = 0;
                for (int w = 0; w < width; w++) {
                    for (int h = 0; h < height; h++) {
                        MetaTileEntityMonitorScreen screen = screens[w][h];
                        if (screen != null) {
                            size++;
                            if (screen.isActive()) {
                                BlockPos pos = screen.getPos();
                                BlockPos pos2 = this.getPos();
                                GlStateManager.pushMatrix();
                                RenderUtil.moveToFace(x + pos.getX() - pos2.getX(), y + pos.getY() - pos2.getY(),
                                        z + pos.getZ() - pos2.getZ(), this.frontFacing);
                                RenderUtil.rotateToFace(this.frontFacing, this.upwardsFacing);
                                screen.renderScreen(partialTicks, rayTraceResult);
                                GlStateManager.popMatrix();
                            }
                        }
                    }
                }

                if (size != parts.size()) {
                    clearScreens();
                    for (BlockPos pos : parts) {
                        TileEntity tileEntity = getWorld().getTileEntity(pos);
                        if (tileEntity instanceof IGregTechTileEntity && ((IGregTechTileEntity) tileEntity)
                                .getMetaTileEntity() instanceof MetaTileEntityMonitorScreen) {
                            MetaTileEntityMonitorScreen screen = (MetaTileEntityMonitorScreen) ((IGregTechTileEntity) tileEntity)
                                    .getMetaTileEntity();
                            screen.addToMultiBlock(this);
                            int sx = screen.getX(), sy = screen.getY();
                            if (sx < 0 || sx >= width || sy < 0 || sy >= height) {
                                parts.clear();
                                clearScreens();
                                break;
                            }
                            screens[sx][sy] = screen;
                        }
                    }
                }

                /* restore the lightmap */
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
                net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
                GL11.glPopAttrib();
                GlStateManager.popMatrix();
            }
        }, true);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        BlockPos sp, ep;
        EnumFacing spin = this.upwardsFacing;
        if (frontFacing.getAxis() == EnumFacing.Axis.Y) {
            sp = this.getPos().offset(spin, -2);
            ep = sp.offset(spin, height + 1).offset(spin.rotateY(), (width + 2) * frontFacing.getYOffset());
        } else {
            if (spin == EnumFacing.NORTH) {
                sp = this.getPos().offset(EnumFacing.DOWN, 2);
                ep = sp.offset(EnumFacing.UP, height + 1).offset(this.frontFacing.rotateY(), -width - 2);
            } else if (spin == EnumFacing.SOUTH) {
                sp = this.getPos().offset(EnumFacing.UP, 2);
                ep = sp.offset(EnumFacing.DOWN, height + 1).offset(this.frontFacing.rotateY(), width + 2);
            } else if (spin == EnumFacing.WEST) {
                sp = this.getPos().offset(frontFacing.rotateY(), -2);
                ep = sp.offset(frontFacing.rotateY(), height + 1).offset(EnumFacing.UP, width + 2);
            } else {
                sp = this.getPos().offset(frontFacing.rotateY(), 2);
                ep = sp.offset(frontFacing.rotateY(), -height - 1).offset(EnumFacing.DOWN, width + 2);
            }
        }
        return new AxisAlignedBB(sp, ep);
    }

    // Disallow flip because otherwise a controller could be shared between multiple valid structures
    @Override
    public boolean allowsFlip() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        if (!isActive()) {
            return super.createUI(entityPlayer);
        } else {
            WidgetScreenGrid[][] screenGrids = new WidgetScreenGrid[width][height];
            WidgetGroup group = new WidgetGroup();
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    screenGrids[i][j] = new WidgetScreenGrid(4 * width, 4 * height, i, j);
                    group.addWidget(screenGrids[i][j]);
                }
            }
            if (!this.getWorld().isRemote) {
                this.getMultiblockParts().forEach(part -> {
                    if (part instanceof MetaTileEntityMonitorScreen) {
                        int x = ((MetaTileEntityMonitorScreen) part).getX();
                        int y = ((MetaTileEntityMonitorScreen) part).getY();
                        screenGrids[x][y].setScreen((MetaTileEntityMonitorScreen) part);
                    }
                });
            } else {
                parts.forEach(partPos -> {
                    TileEntity tileEntity = this.getWorld().getTileEntity(partPos);
                    if (tileEntity instanceof IGregTechTileEntity && ((IGregTechTileEntity) tileEntity)
                            .getMetaTileEntity() instanceof MetaTileEntityMonitorScreen) {
                        MetaTileEntityMonitorScreen part = (MetaTileEntityMonitorScreen) ((IGregTechTileEntity) tileEntity)
                                .getMetaTileEntity();
                        int x = part.getX();
                        int y = part.getY();
                        screenGrids[x][y].setScreen(part);
                    }
                });
            }
            return ModularUI.builder(GuiTextures.BOXED_BACKGROUND, 28 * width, 28 * height)
                    .widget(group)
                    .build(this.getHolder(), entityPlayer);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.multiblock.central_monitor.tooltip.1"));
        tooltip.add(I18n.format("gregtech.multiblock.central_monitor.tooltip.2", MAX_WIDTH, MAX_HEIGHT));
        tooltip.add(I18n.format("gregtech.multiblock.central_monitor.tooltip.3"));
        tooltip.add(I18n.format("gregtech.multiblock.central_monitor.tooltip.4", -ENERGY_COST));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }
}
