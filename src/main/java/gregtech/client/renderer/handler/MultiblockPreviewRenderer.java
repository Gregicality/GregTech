package gregtech.client.renderer.handler;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.util.BlockInfo;
import gregtech.client.utils.TrackedDummyWorld;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class MultiblockPreviewRenderer {

    private static BlockPos mbpPos;
    private static long mbpEndTime;
    private static int opList = -1;
    private static int layer;
    private static int tier;

    public static void renderWorldLastEvent(RenderWorldLastEvent event) {
        if (mbpPos != null) {
            Minecraft mc = Minecraft.getMinecraft();
            long time = System.currentTimeMillis();
            if (opList == -1 || time > mbpEndTime || !(mc.world.getTileEntity(mbpPos) instanceof IGregTechTileEntity)) {
                resetMultiblockRender();
                layer = 0;
                tier = 0;
                return;
            }
            Entity entity = mc.getRenderViewEntity();
            if (entity == null) entity = mc.player;
            float partialTicks = event.getPartialTicks();
            double tx = entity.lastTickPosX + ((entity.posX - entity.lastTickPosX) * partialTicks);
            double ty = entity.lastTickPosY + ((entity.posY - entity.lastTickPosY) * partialTicks);
            double tz = entity.lastTickPosZ + ((entity.posZ - entity.lastTickPosZ) * partialTicks);

            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.pushMatrix();
            GlStateManager.translate(-tx, -ty, -tz);
            GlStateManager.enableBlend();

            GlStateManager.callList(opList);

            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
            GlStateManager.color(1F, 1F, 1F, 1F);

        }
    }

    public static void renderMultiBlockPreview(MultiblockControllerBase controller, long durTimeMillis) {
        if (!controller.getPos().equals(mbpPos)) {
            layer = 0;
        } else {
            if (mbpEndTime - System.currentTimeMillis() < 200) return;
            layer++;
        }
        resetMultiblockRender();
        mbpPos = controller.getPos();
        mbpEndTime = System.currentTimeMillis() + durTimeMillis;
        opList = GLAllocation.generateDisplayLists(1); // allocate op list
        GlStateManager.glNewList(opList, GL11.GL_COMPILE);
        if (tier != controller.getStructureTier()) {
            tier = controller.getStructureTier();
            controller.reinitializeStructurePattern();
        }
        List<MultiblockShapeInfo> shapes = controller.getMatchingShapes();
        if (!shapes.isEmpty()) {
            renderControllerInList(controller, shapes.get(0), layer);
        }
        GlStateManager.glEndList();
    }

    public static void resetMultiblockRender() {
        mbpPos = null;
        mbpEndTime = 0;
        if (opList != -1) {
            GlStateManager.glDeleteLists(opList, 1);
            opList = -1;
        }
    }

    public static void renderControllerInList(MultiblockControllerBase controllerBase, MultiblockShapeInfo shapeInfo,
                                              int layer) {
        BlockPos mbpPos = controllerBase.getPos();
        EnumFacing frontFacing, previewFacing;
        previewFacing = controllerBase.getFrontFacing();
        BlockPos controllerPos = BlockPos.ORIGIN;
        MultiblockControllerBase mte = null;
        BlockInfo[][][] blocks = shapeInfo.getBlocks();
        Map<BlockPos, BlockInfo> blockMap = new HashMap<>();
        int maxY = 0;
        for (int x = 0; x < blocks.length; x++) {
            BlockInfo[][] aisle = blocks[x];
            maxY = Math.max(maxY, aisle.length);
            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];
                for (int z = 0; z < column.length; z++) {
                    blockMap.put(new BlockPos(x, y, z), column[z]);
                    MetaTileEntity metaTE = column[z].getTileEntity() instanceof IGregTechTileEntity ?
                            ((IGregTechTileEntity) column[z].getTileEntity()).getMetaTileEntity() : null;
                    if (metaTE instanceof MultiblockControllerBase &&
                            metaTE.metaTileEntityId.equals(controllerBase.metaTileEntityId)) {
                        controllerPos = new BlockPos(x, y, z);
                        previewFacing = metaTE.getFrontFacing();
                        mte = (MultiblockControllerBase) metaTE;
                        break;
                    }
                }
            }
        }
        TrackedDummyWorld world = new TrackedDummyWorld();
        world.addBlocks(blockMap);
        int finalMaxY = layer % (maxY + 1);
        world.setRenderFilter(pos -> pos.getY() + 1 == finalMaxY || finalMaxY == 0);

        EnumFacing facing = controllerBase.getFrontFacing();
        EnumFacing upwardsFacing = controllerBase.getUpwardsFacing();

        frontFacing = facing.getYOffset() == 0 ? facing :
                facing.getYOffset() < 0 ? upwardsFacing : upwardsFacing.getOpposite();
        Rotation rotatePreviewBy = Rotation
                .values()[(4 + frontFacing.getHorizontalIndex() - previewFacing.getHorizontalIndex()) % 4];

        Minecraft mc = Minecraft.getMinecraft();
        BlockRendererDispatcher brd = mc.getBlockRendererDispatcher();
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buff = tes.getBuffer();
        GlStateManager.pushMatrix();
        GlStateManager.translate(mbpPos.getX(), mbpPos.getY(), mbpPos.getZ());
        GlStateManager.translate(0.5, 0, 0.5);
        GlStateManager.rotate(rotatePreviewBy.ordinal() * 90, 0, -1, 0);
        GlStateManager.translate(-0.5, 0, -0.5);

        if (facing == EnumFacing.UP) {
            GlStateManager.translate(0.5, 0.5, 0.5);
            GlStateManager.rotate(90, -previewFacing.getZOffset(), 0, previewFacing.getXOffset());
            GlStateManager.translate(-0.5, -0.5, -0.5);
        } else if (facing == EnumFacing.DOWN) {
            GlStateManager.translate(0.5, 0.5, 0.5);
            GlStateManager.rotate(90, previewFacing.getZOffset(), 0, -previewFacing.getXOffset());
            GlStateManager.translate(-0.5, -0.5, -0.5);
        } else {
            int degree = 90 * (upwardsFacing == EnumFacing.EAST ? -1 :
                    upwardsFacing == EnumFacing.SOUTH ? 2 : upwardsFacing == EnumFacing.WEST ? 1 : 0);
            GlStateManager.translate(0.5, 0.5, 0.5);
            GlStateManager.rotate(degree, previewFacing.getXOffset(), 0, previewFacing.getZOffset());
            GlStateManager.translate(-0.5, -0.5, -0.5);
        }

        if (mte != null) {
            mte.checkStructurePattern();
        }

        BlockRenderLayer oldLayer = MinecraftForgeClient.getRenderLayer();

        TargetBlockAccess targetBA = new TargetBlockAccess(world, BlockPos.ORIGIN);
        for (BlockPos pos : blockMap.keySet()) {
            targetBA.setPos(pos);
            GlStateManager.pushMatrix();
            BlockPos.MutableBlockPos tPos = new BlockPos.MutableBlockPos(pos.subtract(controllerPos));
            GlStateManager.translate(tPos.getX(), tPos.getY(), tPos.getZ());
            GlStateManager.translate(0.125, 0.125, 0.125);
            GlStateManager.scale(0.75, 0.75, 0.75);

            buff.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            IBlockState state = world.getBlockState(pos);
            for (BlockRenderLayer brl : BlockRenderLayer.values()) {
                if (state.getBlock().canRenderInLayer(state, brl)) {
                    ForgeHooksClient.setRenderLayer(brl);
                    brd.renderBlock(state, BlockPos.ORIGIN, targetBA, buff);
                }
            }
            tes.draw();
            GlStateManager.popMatrix();
        }
        ForgeHooksClient.setRenderLayer(oldLayer);

        GlStateManager.popMatrix();
    }

    @SideOnly(Side.CLIENT)
    private static class TargetBlockAccess implements IBlockAccess {

        private final IBlockAccess delegate;
        private BlockPos targetPos;

        public TargetBlockAccess(IBlockAccess delegate, BlockPos pos) {
            this.delegate = delegate;
            this.targetPos = pos;
        }

        public void setPos(BlockPos pos) {
            targetPos = pos;
        }

        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            return pos.equals(BlockPos.ORIGIN) ? delegate.getTileEntity(targetPos) : null;
        }

        @Override
        public int getCombinedLight(BlockPos pos, int lightValue) {
            return 15;
        }

        @Override
        public IBlockState getBlockState(BlockPos pos) {
            return pos.equals(BlockPos.ORIGIN) ? delegate.getBlockState(targetPos) : Blocks.AIR.getDefaultState();
        }

        @Override
        public boolean isAirBlock(BlockPos pos) {
            return !pos.equals(BlockPos.ORIGIN) || delegate.isAirBlock(targetPos);
        }

        @Override
        public Biome getBiome(BlockPos pos) {
            return delegate.getBiome(targetPos);
        }

        @Override
        public int getStrongPower(BlockPos pos, EnumFacing direction) {
            return 0;
        }

        @Override
        public WorldType getWorldType() {
            return delegate.getWorldType();
        }

        @Override
        public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
            return pos.equals(BlockPos.ORIGIN) && delegate.isSideSolid(targetPos, side, _default);
        }
    }
}
