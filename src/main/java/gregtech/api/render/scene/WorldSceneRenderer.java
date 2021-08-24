package gregtech.api.render.scene;

import gregtech.api.gui.resources.RenderUtil;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.world.DummyWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.annotation.Nonnull;
import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/23/22:22
 * @Description: Abstract class, and extend a lot of features compared with the original one.
 */
public abstract class WorldSceneRenderer {
    protected static final FloatBuffer MODELVIEW_MATRIX_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final FloatBuffer PROJECTION_MATRIX_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final IntBuffer VIEWPORT_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
    protected static final FloatBuffer PIXEL_DEPTH_BUFFER = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final FloatBuffer OBJECT_POS_BUFFER = ByteBuffer.allocateDirect(3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

    public final TrackedDummyWorld world = new TrackedDummyWorld();
    public final Set<BlockPos> renderedBlocks = new HashSet<>();
    private Runnable beforeRender;
    private Runnable afterRender;
    private Predicate<BlockPos> renderFilter;
    private Consumer<BlockPosFace> onLookingAt;
    private BlockPosFace lastHitBlock;

    public WorldSceneRenderer addBlocks(Map<BlockPos, BlockInfo> renderedBlocks) {
        for (Map.Entry<BlockPos, BlockInfo> renderEntry : renderedBlocks.entrySet()) {
            BlockPos pos = renderEntry.getKey();
            BlockInfo blockInfo = renderEntry.getValue();
            if (blockInfo.getBlockState().getBlock() == Blocks.AIR)
                continue; //do not render air blocks
            this.renderedBlocks.add(pos);
            blockInfo.apply(world, pos);
        }
        return this;
    }

    public WorldSceneRenderer addBlock(BlockPos pos, BlockInfo blockInfo) {
        if (blockInfo.getBlockState().getBlock() == Blocks.AIR)
            return this;
        this.renderedBlocks.add(pos);
        blockInfo.apply(world, pos);
        return this;
    }

    public WorldSceneRenderer setBeforeWorldRender(Runnable callback) {
        this.beforeRender = callback;
        return this;
    }

    public WorldSceneRenderer setAfterWorldRender(Runnable callback) {
        this.afterRender = callback;
        return this;
    }

    public WorldSceneRenderer setRenderFilter(Predicate<BlockPos> filter) {
        this.renderFilter = filter;
        return this;
    }

    public WorldSceneRenderer setOnLookingAt(Consumer<BlockPosFace> onLookingAt) {
        this.onLookingAt = onLookingAt;
        return this;
    }

    public Vector3f getSceneSize() {
        return world.getSize();
    }

    public BlockPosFace getLastHitBlock() {
        return lastHitBlock;
    }

    public void render(float x, float y, float width, float height, int mouseX, int mouseY) {
        // setupCamera
        setupCamera((int)x, (int)y, (int)width, (int)height);
        // render TrackedDummyWorld
        drawWorld();
        // render lookingAt
        lastHitBlock = unProject(mouseX, mouseY);
        if (lastHitBlock != null) {
            if(onLookingAt != null) {
                onLookingAt.accept(lastHitBlock);
            }
        }
        // resetCamera
        resetCamera();
    }

    protected void setupCamera(int x, int y, int width, int height) {
        GlStateManager.pushAttrib();

        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();

        //setup viewport and clear GL buffers
        GlStateManager.viewport(x, y, width, height);

        clearView(x, y, width, height);

        //setup projection matrix to perspective
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        float aspectRatio = width / (height * 1.0f);
        GLU.gluPerspective(60.0f, aspectRatio, 0.1f, 10000.0f);

        //setup modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GLU.gluLookAt(0.0f, 0.0f, -10.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    protected void clearView(int x, int y, int width, int height) {
        RenderUtil.setGlClearColorFromInt(0, 0);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    protected void resetCamera() {
        //reset viewport
        Minecraft minecraft = Minecraft.getMinecraft();
        GlStateManager.viewport(0, 0, minecraft.displayWidth, minecraft.displayHeight);

        //reset projection matrix
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();

        //reset modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();

        Minecraft.getMinecraft().entityRenderer.enableLightmap();

        //reset attributes
        GlStateManager.popAttrib();
    }

    protected void drawWorld() {
        if (beforeRender != null) {
            beforeRender.run();
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        BlockRendererDispatcher dispatcher = minecraft.getBlockRendererDispatcher();
        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        for (BlockPos pos : renderedBlocks) {
            if (renderFilter != null && !renderFilter.test(pos))
                continue; //do not render if position is skipped
            IBlockState blockState = world.getBlockState(pos);
            for(BlockRenderLayer renderLayer : BlockRenderLayer.values()) {
                if (!blockState.getBlock().canRenderInLayer(blockState, renderLayer)) continue;
                ForgeHooksClient.setRenderLayer(renderLayer);
                dispatcher.renderBlock(blockState, pos, world, bufferBuilder);
            }
        }

        tessellator.draw();
        ForgeHooksClient.setRenderLayer(oldRenderLayer);

        if (afterRender != null) {
            afterRender.run();
        }
    }

    public Vector3f project(BlockPos pos, boolean depth) {
        //read current rendering parameters
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW_MATRIX_BUFFER);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION_MATRIX_BUFFER);
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT_BUFFER);

        //rewind buffers after write by OpenGL glGet calls
        MODELVIEW_MATRIX_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        VIEWPORT_BUFFER.rewind();

        //call gluProject with retrieved parameters
        GLU.gluProject(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, MODELVIEW_MATRIX_BUFFER, PROJECTION_MATRIX_BUFFER, VIEWPORT_BUFFER, OBJECT_POS_BUFFER);

        //rewind buffers after read by gluProject
        VIEWPORT_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        MODELVIEW_MATRIX_BUFFER.rewind();

        //rewind buffer after write by gluProject
        OBJECT_POS_BUFFER.rewind();

        //obtain position in Screen
        float winX = OBJECT_POS_BUFFER.get();
        float winY = OBJECT_POS_BUFFER.get();
        float winZ = OBJECT_POS_BUFFER.get();

        //rewind buffer after read
        OBJECT_POS_BUFFER.rewind();

        //check whether pass depth test
        if (!depth || Objects.equals(unProject((int) winX, (int) winY), pos)) {
            return new Vector3f(winX, winY, winZ);
        }

        return null;
    }

    public BlockPosFace unProject(int mouseX, int mouseY) {
        //read depth of pixel under mouse
        GL11.glReadPixels(mouseX, mouseY, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, PIXEL_DEPTH_BUFFER);

        //rewind buffer after write by glReadPixels
        PIXEL_DEPTH_BUFFER.rewind();

        //retrieve depth from buffer (0.0-1.0f)
        float pixelDepth = PIXEL_DEPTH_BUFFER.get();

        //rewind buffer after read
        PIXEL_DEPTH_BUFFER.rewind();

        //read current rendering parameters
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW_MATRIX_BUFFER);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION_MATRIX_BUFFER);
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT_BUFFER);

        //rewind buffers after write by OpenGL glGet calls
        MODELVIEW_MATRIX_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        VIEWPORT_BUFFER.rewind();

        //call gluUnProject with retrieved parameters
        GLU.gluUnProject(mouseX, mouseY, pixelDepth, MODELVIEW_MATRIX_BUFFER, PROJECTION_MATRIX_BUFFER, VIEWPORT_BUFFER, OBJECT_POS_BUFFER);

        //rewind buffers after read by gluUnProject
        VIEWPORT_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        MODELVIEW_MATRIX_BUFFER.rewind();

        //rewind buffer after write by gluUnProject
        OBJECT_POS_BUFFER.rewind();

        //obtain absolute position in world
        float posX = OBJECT_POS_BUFFER.get();
        float posY = OBJECT_POS_BUFFER.get();
        float posZ = OBJECT_POS_BUFFER.get();

        //rewind buffer after read
        OBJECT_POS_BUFFER.rewind();

        //if we didn't hit anything, just return null. also return null if hit is too far from us
        if (posY < -100.0f) {
            return null; //stop execution at that point
        }

        BlockPos pos = new BlockPos(posX, posY, posZ);
        if (world.isAirBlock(pos)) {
            //if block is air, then search for nearest adjacent block
            //this can happen under extreme rotation angles
            for (EnumFacing offset : EnumFacing.VALUES) {
                BlockPos relative = pos.offset(offset);
                if (world.isAirBlock(relative)) continue;
                pos = relative;
                break;
            }
        }
        if (world.isAirBlock(pos)) {
            //if we didn't found any other block, return null
            return null;
        }
        EnumFacing.Axis axis = EnumFacing.Axis.X;
        double min = Math.abs(Math.round(posX) - posX);
        double tmp = Math.abs(Math.round(posY) - posY);
        if (min > tmp) {
            min = tmp;
            axis = EnumFacing.Axis.Y;
        }
        tmp = Math.abs(Math.round(posZ) - posZ);
        if (min > tmp) {
            axis = EnumFacing.Axis.Z;
        }
        EnumFacing facing = EnumFacing.UP;
        if (axis == EnumFacing.Axis.Y && (posY - pos.getY()) < 0.5) {
            facing = EnumFacing.DOWN;
        } else if (axis == EnumFacing.Axis.X) {
            if ((posX - pos.getX()) < 0.5)
                facing = EnumFacing.WEST;
            else
                facing = EnumFacing.EAST;
        } else if (axis == EnumFacing.Axis.Z) {
            if ((posZ - pos.getZ()) < 0.5)
                facing = EnumFacing.NORTH;
            else
                facing = EnumFacing.SOUTH;
        }
        return new BlockPosFace(pos, facing);
    }

    public static class BlockPosFace extends BlockPos {
        public final EnumFacing facing;

        public BlockPosFace(BlockPos pos, EnumFacing facing) {
            super(pos);
            this.facing = facing;
        }

    }

    public class TrackedDummyWorld extends DummyWorld {

        private final Vector3f minPos = new Vector3f(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        private final Vector3f maxPos = new Vector3f(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

        @Override
        public boolean setBlockState(@Nonnull BlockPos pos, IBlockState newState, int flags) {
            if (newState.getBlock() == Blocks.AIR) {
                renderedBlocks.remove(pos);
            } else {
                renderedBlocks.add(pos);
            }
            minPos.setX(Math.min(minPos.getX(), pos.getX()));
            minPos.setY(Math.min(minPos.getY(), pos.getY()));
            minPos.setZ(Math.min(minPos.getZ(), pos.getZ()));
            maxPos.setX(Math.max(maxPos.getX(), pos.getX()));
            maxPos.setY(Math.max(maxPos.getY(), pos.getY()));
            maxPos.setZ(Math.max(maxPos.getZ(), pos.getZ()));
            return super.setBlockState(pos, newState, flags);
        }

        @Nonnull
        @Override
        public IBlockState getBlockState(@Nonnull BlockPos pos) {
            if (renderFilter != null && !renderFilter.test(pos))
                return Blocks.AIR.getDefaultState(); //return air if not rendering this block
            return super.getBlockState(pos);
        }

        public Vector3f getSize() {
            Vector3f result = new Vector3f();
            result.setX(maxPos.getX() - minPos.getX() + 1);
            result.setY(maxPos.getY() - minPos.getY() + 1);
            result.setZ(maxPos.getZ() - minPos.getZ() + 1);
            return result;
        }

        public Vector3f getMinPos() {
            return minPos;
        }

        public Vector3f getMaxPos() {
            return maxPos;
        }
    }
}
