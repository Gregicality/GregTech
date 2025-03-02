package gregtech.common.gui.widget.prospector.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.*;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.api.worldgen.filler.FillerEntry;
import gregtech.common.gui.widget.prospector.ProspectingTexture;
import gregtech.common.gui.widget.prospector.ProspectorMode;
import gregtech.core.network.packets.PacketProspecting;
import gregtech.integration.xaero.ColorUtility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class WidgetProspectingMap extends Widget {

    private final int chunkRadius;
    private final WidgetOreList oreList;
    private final ProspectorMode mode;
    private final int scanTick;
    private boolean darkMode = false;
    private int chunkIndex = 0;

    @SideOnly(Side.CLIENT)
    private ProspectingTexture texture;
    @SideOnly(Side.CLIENT)
    private Consumer<PacketProspecting> onPacketReceived;
    private final Queue<PacketProspecting> packetQueue = new LinkedBlockingQueue<>();

    private long lastClicked;

    private final List<String> hoveredNames = new ArrayList<>();
    private int hoveredOreHeight = 0;
    private int color;

    public WidgetProspectingMap(int xPosition, int yPosition, int chunkRadius, WidgetOreList widgetOreList,
                                @NotNull ProspectorMode mode, int scanTick) {
        super(new Position(xPosition, yPosition), new Size(16 * (chunkRadius * 2 - 1), 16 * (chunkRadius * 2 - 1)));
        this.chunkRadius = chunkRadius;
        this.mode = mode;
        this.scanTick = scanTick;
        oreList = widgetOreList;
        if (oreList != null) {
            oreList.onSelected = name -> {
                if (texture != null) {
                    texture.loadTexture(null, name);
                }
            };
        }
    }

    @SideOnly(Side.CLIENT)
    public void setOnPacketReceived(Consumer<PacketProspecting> onPacketReceived) {
        this.onPacketReceived = onPacketReceived;
    }

    @SideOnly(Side.CLIENT)
    public void setDarkMode(boolean mode) {
        if (darkMode != mode) {
            darkMode = mode;
            if (texture != null) {
                texture.loadTexture(null, darkMode);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean getDarkMode() {
        return darkMode;
    }

    @Override
    public void detectAndSendChanges() {
        EntityPlayer player = gui.entityPlayer;
        World world = player.world;
        if (FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() % scanTick == 0 &&
                chunkIndex < (chunkRadius * 2 - 1) * (chunkRadius * 2 - 1)) {

            int playerChunkX = player.chunkCoordX;
            int playerChunkZ = player.chunkCoordZ;

            int row = chunkIndex / (chunkRadius * 2 - 1);
            int column = chunkIndex % (chunkRadius * 2 - 1);

            int ox = column - chunkRadius + 1;
            int oz = row - chunkRadius + 1;

            Chunk chunk = world.getChunk(playerChunkX + ox, playerChunkZ + oz);
            PacketProspecting packet = new PacketProspecting(playerChunkX + ox, playerChunkZ + oz, playerChunkX,
                    playerChunkZ, (int) player.posX, (int) player.posZ, this.mode);

            switch (mode) {
                case ORE:
                    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int ySize = chunk.getHeightValue(x, z);
                            for (int y = 1; y < ySize; y++) {
                                pos.setPos(x, y, z);
                                IBlockState state = chunk.getBlockState(pos);
                                ItemStack itemBlock = GTUtility.toItem(state);
                                if (GTUtility.isOre(itemBlock)) {
                                    boolean added = false;
                                    String oreDictString = OreDictUnifier.getOreDictionaryNames(itemBlock).stream()
                                            .findFirst()
                                            .orElse("");
                                    OrePrefix prefix = OreDictUnifier.getPrefix(itemBlock);
                                    if (prefix != null) {
                                        for (StoneType type : StoneType.STONE_TYPE_REGISTRY) {
                                            if (type.processingPrefix == prefix && type.shouldBeDroppedAsItem) {
                                                packet.addBlock(x, y, z, oreDictString);
                                                added = true;
                                                break;
                                            } else if (type.processingPrefix == prefix) {
                                                MaterialStack materialStack = OreDictUnifier.getMaterial(itemBlock);
                                                if (materialStack != null) {
                                                    String oreDict = "ore" +
                                                            oreDictString.replaceFirst(prefix.name(), "");
                                                    packet.addBlock(x, y, z, oreDict);
                                                    added = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    // Probably other mod's ores
                                    if (!added) {
                                        // Fallback
                                        packet.addBlock(x, y, z, oreDictString);
                                    }
                                }
                            }
                        }
                    }
                    break;
                case FLUID:
                    BedrockFluidVeinHandler.FluidVeinWorldEntry fStack = BedrockFluidVeinHandler
                            .getFluidVeinWorldEntry(world, chunk.x, chunk.z);
                    if (fStack != null && fStack.getDefinition() != null) {
                        packet.addBlock(0, 3, 0,
                                TextFormattingUtil.formatNumbers(100.0 *
                                        BedrockFluidVeinHandler.getOperationsRemaining(world, chunk.x, chunk.z) /
                                        BedrockFluidVeinHandler.MAXIMUM_VEIN_OPERATIONS));
                        packet.addBlock(0, 2, 0,
                                String.valueOf(BedrockFluidVeinHandler.getFluidYield(world, chunk.x, chunk.z)));
                        Fluid fluid = BedrockFluidVeinHandler.getFluidInChunk(world, chunk.x, chunk.z);
                        if (fluid != null) {
                            packet.addBlock(0, 1, 0, fluid.getName());
                        }
                    }
                    break;
                default:
                    break;
            }
            writeUpdateInfo(2, packet::writePacketData);
            chunkIndex++;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (texture != null) {
            GlStateManager.color(1, 1, 1, 1);
            texture.draw(this.getPosition().x, this.getPosition().y);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 2) {
            PacketProspecting packet = PacketProspecting.readPacketData(buffer);
            if (packet != null) {
                if (onPacketReceived != null) {
                    onPacketReceived.accept(packet);
                }
                addPacketToQueue(packet);
            }
        }
    }

    @Override
    public void updateScreen() {
        if (packetQueue != null) {
            int max = 10;
            while (max-- > 0 && !packetQueue.isEmpty()) {
                PacketProspecting packet = packetQueue.poll();
                if (texture == null) {
                    texture = new ProspectingTexture(packet.mode, chunkRadius, darkMode);
                }
                texture.updateTexture(packet);
                if (oreList != null) {
                    oreList.addOres(packet.ores, packet.mode);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void addPacketToQueue(PacketProspecting packet) {
        packetQueue.add(packet);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        // draw tooltips
        if (this.isMouseOverElement(mouseX, mouseY) && texture != null) {
            this.hoveredNames.clear();
            this.hoveredOreHeight = 0;
            List<String> tooltips = new ArrayList<>();
            int cX = (mouseX - this.getPosition().x) / 16;
            int cZ = (mouseY - this.getPosition().y) / 16;
            if (cX >= chunkRadius * 2 - 1 || cZ >= chunkRadius * 2 - 1)
                return;
            // draw hover layer
            Gui.drawRect(cX * 16 + this.getPosition().x,
                    cZ * 16 + this.getPosition().y,
                    (cX + 1) * 16 + this.getPosition().x,
                    (cZ + 1) * 16 + this.getPosition().y,
                    new Color(0x4B6C6C6C, true).getRGB());

            // pick the color of the highest element for the waypoint color
            final int[] maxAmount = { 0 };

            if (this.mode == ProspectorMode.ORE) { // draw ore
                tooltips.add(I18n.format("terminal.prospector.ore"));
                HashMap<String, Integer> oreInfo = new HashMap<>();
                HashMap<String, Float> oreHeight = new HashMap<>();
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        if (texture.map[cX * 16 + i][cZ * 16 + j] != null) {
                            texture.map[cX * 16 + i][cZ * 16 + j].forEach((height, dict) -> {
                                String name = OreDictUnifier.get(dict).getDisplayName();
                                if (ProspectingTexture.SELECTED_ALL.equals(texture.getSelected()) ||
                                        texture.getSelected().equals(dict)) {
                                    oreInfo.put(name, oreInfo.getOrDefault(name, 0) + 1);
                                    oreHeight.put(name, oreHeight.getOrDefault(name, 0.0f) + height.intValue());
                                    if (oreInfo.get(name) > maxAmount[0]) {
                                        maxAmount[0] = oreInfo.get(name);
                                        MaterialStack m = OreDictUnifier.getMaterial(OreDictUnifier.get(dict));
                                        if (m != null) {
                                            color = m.material.getMaterialRGB();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
                oreHeight.forEach((name, height) -> {
                    hoveredOreHeight += height;
                    int count = oreInfo.getOrDefault(name, 0);
                    float avgHeight = count != 0 ? height / count : 0.0f;
                    oreHeight.put(name, avgHeight);
                });
                int totalCount = oreInfo.values().stream().reduce(0, Integer::sum);
                if (totalCount != 0) {
                    hoveredOreHeight = Math.round(hoveredOreHeight / (float) totalCount);
                }
                oreInfo.forEach((name, count) -> {
                    float height = oreHeight.getOrDefault(name, 0.0f);
                    tooltips.add(name + ", y" + Math.round(height) + " --- " + count);
                    hoveredNames.add(name);
                });
            } else if (this.mode == ProspectorMode.FLUID) {
                tooltips.add(I18n.format("terminal.prospector.fluid"));
                if (texture.map[cX][cZ] != null && !texture.map[cX][cZ].isEmpty()) {
                    if (ProspectingTexture.SELECTED_ALL.equals(texture.getSelected()) ||
                            texture.getSelected().equals(texture.map[cX][cZ].get((byte) 1))) {
                        FluidStack fluidStack = FluidRegistry.getFluidStack(texture.map[cX][cZ].get((byte) 1), 1);
                        if (fluidStack != null) {
                            tooltips.add(I18n.format("terminal.prospector.fluid.info",
                                    fluidStack.getLocalizedName(),
                                    texture.map[cX][cZ].get((byte) 2),
                                    texture.map[cX][cZ].get((byte) 3)));
                            hoveredNames.add(fluidStack.getLocalizedName());
                            int amount = Integer.parseInt(texture.map[cX][cZ].get((byte) 2));
                            if (amount > maxAmount[0]) {
                                maxAmount[0] = amount;
                                color = fluidStack.getFluid().getColor(fluidStack);
                            }
                        }
                    }
                }
            }

            if (Mods.JourneyMap.isModLoaded() || Mods.VoxelMap.isModLoaded() || Mods.XaerosMinimap.isModLoaded()) {
                tooltips.add(I18n.format("terminal.prospector.waypoint.add"));
            }
            this.drawHoveringText(ItemStack.EMPTY, tooltips, 300, mouseX, mouseY);
            GlStateManager.color(1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        int cX = (mouseX - this.getPosition().x) / 16;
        int cZ = (mouseY - this.getPosition().y) / 16;

        if (cX >= chunkRadius * 2 - 1 || cZ >= chunkRadius * 2 - 1)
            return false;

        int xDiff = cX - (chunkRadius - 1);
        int zDiff = cZ - (chunkRadius - 1);

        int xPos = ((Minecraft.getMinecraft().player.chunkCoordX + xDiff) << 4) + 8;
        int zPos = ((Minecraft.getMinecraft().player.chunkCoordZ + zDiff) << 4) + 8;
        int yPos = hoveredOreHeight != 0 ? hoveredOreHeight :
                Minecraft.getMinecraft().world.getHeight(xPos, zPos);

        BlockPos b = new BlockPos(xPos, yPos, zPos);
        if (System.currentTimeMillis() - lastClicked < 400 && !hoveredNames.isEmpty()) {
            boolean added = false;
            trimHoveredNames();

            if (Mods.JourneyMap.isModLoaded()) {
                added = addJourneymapWaypoint(b);
            } else if (Mods.VoxelMap.isModLoaded()) {
                added = addVoxelMapWaypoint(b);
            } else if (Mods.XaerosMinimap.isModLoaded()) {
                added = addXaeroMapWaypoint(b);
            }
            if (added) {
                Minecraft.getMinecraft().player
                        .sendStatusMessage(new TextComponentTranslation("behavior.prospector.added_waypoint"), true);
            }
        }
        this.lastClicked = System.currentTimeMillis();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void trimHoveredNames() {
        List<OreDepositDefinition> oreVeins = WorldGenRegistry.getOreDeposits();
        for (OreDepositDefinition odd : oreVeins) {
            for (FillerEntry fillerEntry : odd.getBlockFiller().getAllPossibleStates()) {
                Collection<String> matches = new ArrayList<>();
                Collection<IBlockState> pr = fillerEntry.getPossibleResults();
                for (IBlockState bs : pr) {
                    Set<String> ores = OreDictUnifier.getOreDictionaryNames(new ItemStack(bs.getBlock()));
                    for (String dict : ores) {
                        String name = OreDictUnifier.get(dict).getDisplayName();
                        if (hoveredNames.contains(name)) {
                            matches.add(name);
                        }
                    }
                }
                if (matches.size() > pr.size() / 2) {
                    this.hoveredNames.removeAll(matches);
                    this.hoveredNames.add(FileUtility.trimFileName(odd.getDepositName()));
                }
            }
        }
    }

    @NotNull
    private String createVeinName() {
        // remove the [] surrounding the array
        String s = hoveredNames.toString();
        return s.substring(1, s.length() - 1);
    }

    @Optional.Method(modid = Mods.Names.JOURNEY_MAP)
    private boolean addJourneymapWaypoint(BlockPos b) {
        journeymap.client.model.Waypoint journeyMapWaypoint = new journeymap.client.model.Waypoint(createVeinName(),
                b,
                new Color(color),
                journeymap.client.model.Waypoint.Type.Normal,
                Minecraft.getMinecraft().world.provider.getDimension());
        if (!journeymap.client.waypoint.WaypointStore.INSTANCE.getAll().contains(journeyMapWaypoint)) {
            journeymap.client.waypoint.WaypointStore.INSTANCE.save(journeyMapWaypoint);
            return true;
        }
        return false;
    }

    @Optional.Method(modid = Mods.Names.VOXEL_MAP)
    private boolean addVoxelMapWaypoint(@NotNull BlockPos b) {
        Color c = new Color(color);
        TreeSet<Integer> world = new TreeSet<>();
        world.add(Minecraft.getMinecraft().world.provider.getDimension());

        com.mamiyaotaru.voxelmap.interfaces.IWaypointManager waypointManager = com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap
                .getInstance().getWaypointManager();
        com.mamiyaotaru.voxelmap.util.Waypoint voxelMapWaypoint = new com.mamiyaotaru.voxelmap.util.Waypoint(
                createVeinName(),
                b.getX(),
                b.getZ(),
                b.getY(),
                true,
                c.getRed() / 255F,
                c.getGreen() / 255F,
                c.getBlue() / 255F,
                Minecraft.getMinecraft().world.provider.getDimensionType().getSuffix(),
                Minecraft.getMinecraft().world.provider.getDimensionType().getName(),
                world);

        if (!waypointManager.getWaypoints().contains(voxelMapWaypoint)) {
            waypointManager.addWaypoint(voxelMapWaypoint);
            waypointManager.saveWaypoints();
            return true;
        }
        return false;
    }

    @Optional.Method(modid = Mods.Names.XAEROS_MINIMAP)
    private boolean addXaeroMapWaypoint(@NotNull BlockPos b) {
        int red = clampColor(color >> 16 & 0xFF);
        int green = clampColor(color >> 8 & 0xFF);
        int blue = clampColor(color & 0xFF);

        Color wpc = new Color(red, green, blue);
        double[] labWPC = ColorUtility.getLab(wpc);
        int bestColorIndex = 0;
        double closestDistance = Double.MAX_VALUE;

        for (int i = 0; i < xaerosColors.length; i++) {
            double[] c = xaerosColors[i];
            double diffLInner = Math.abs(c[0] - labWPC[0]);
            double diffAInner = Math.abs(c[1] - labWPC[1]);
            double diffBInner = Math.abs(c[2] - labWPC[2]);
            double distance = diffLInner * diffLInner + diffAInner * diffAInner + diffBInner * diffBInner;
            if (distance < closestDistance) {
                closestDistance = distance;
                bestColorIndex = i;
            }

        }

        xaero.common.XaeroMinimapSession minimapSession = xaero.common.XaeroMinimapSession.getCurrentSession();
        xaero.common.minimap.waypoints.WaypointSet wps = minimapSession.getWaypointsManager().getWaypoints();
        xaero.common.minimap.waypoints.WaypointWorld ww = minimapSession.getWaypointsManager().getCurrentWorld();
        xaero.common.minimap.waypoints.Waypoint xaeroWaypoint = new xaero.common.minimap.waypoints.Waypoint(
                b.getX(),
                b.getY(),
                b.getZ(),
                createVeinName(), hoveredNames.get(0).substring(0, 1), bestColorIndex);

        for (xaero.common.minimap.waypoints.Waypoint xwp : wps.getList()) {
            if (xwp.getX() == xaeroWaypoint.getX() &&
                    xwp.getY() == xaeroWaypoint.getY() &&
                    xwp.getZ() == xaeroWaypoint.getZ()) {
                return false;
            }
        }
        wps.getList().add(xaeroWaypoint);
        try {
            minimapSession.getModMain().getSettings().saveWaypoints(ww);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private static int clampColor(int color) {
        if (color < 32) {
            return 0;
        } else if (color < 128) {
            return 128;
        } else if (color < 192) {
            return 192;
        } else {
            return 255;
        }
    }

    private final double[][] xaerosColors = {
            ColorUtility.getLab(new Color(0, 0, 0)),
            ColorUtility.getLab(new Color(0, 0, 128)),
            ColorUtility.getLab(new Color(0, 128, 0)),
            ColorUtility.getLab(new Color(0, 128, 128)),
            ColorUtility.getLab(new Color(128, 0, 0)),
            ColorUtility.getLab(new Color(128, 0, 128)),
            ColorUtility.getLab(new Color(128, 128, 0)),
            ColorUtility.getLab(new Color(192, 192, 192)),
            ColorUtility.getLab(new Color(128, 128, 128)),
            ColorUtility.getLab(new Color(0, 0, 255)),
            ColorUtility.getLab(new Color(0, 255, 0)),
            ColorUtility.getLab(new Color(0, 255, 255)),
            ColorUtility.getLab(new Color(255, 0, 0)),
            ColorUtility.getLab(new Color(255, 0, 255)),
            ColorUtility.getLab(new Color(255, 255, 0)),
            ColorUtility.getLab(new Color(255, 255, 255)),
    };
}
