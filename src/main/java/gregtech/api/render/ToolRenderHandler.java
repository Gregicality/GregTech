package gregtech.api.render;

import gregtech.api.items.toolitem.IAOEItem;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ToolRenderHandler {

    public static final ToolRenderHandler INSTANCE = new ToolRenderHandler();

    private static final MethodHandle destroyBlockIconsHandle;

    static {
        MethodHandle destroyBlockIcons = null;
        try {
            Field destroyBlockIconsField = ObfuscationReflectionHelper.findField(RenderGlobal.class, "field_94141_F");
            destroyBlockIcons = MethodHandles.lookup().unreflectGetter(destroyBlockIconsField);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        destroyBlockIconsHandle = destroyBlockIcons;
    }

    private ToolRenderHandler() { }

    @SubscribeEvent
    public void onDrawBlockOutline(DrawBlockHighlightEvent event) throws Throwable {
        EntityPlayer player = event.getPlayer();
        ItemStack mainStack = player.getHeldItemMainhand();
        List<BlockPos> aoeBlocksToRender = null;
        if (mainStack.getItem() instanceof IAOEItem) {
            aoeBlocksToRender = ((IAOEItem) mainStack.getItem()).getAOEBlocks(mainStack, player, event.getTarget());
            for (BlockPos extraPos : aoeBlocksToRender) {
                event.getContext().drawSelectionBox(player, new RayTraceResult(Vec3d.ZERO, null, extraPos), 0, event.getPartialTicks());
            }
        }
        DestroyBlockProgress progress = event.getContext().damagedBlocks.get(player.getEntityId());
        if (aoeBlocksToRender != null && progress != null) {
            preRenderDamagedBlocks();
            drawBlockDamageTexture(event, aoeBlocksToRender, progress.getPartialBlockDamage());
            postRenderDamagedBlocks();
        }
    }

    // Copied from RenderGlobal
    private void preRenderDamagedBlocks() {
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.doPolygonOffset(-3.0F, -3.0F);
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();
    }

    // Copied from RenderGlobal
    private void postRenderDamagedBlocks() {
        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    public void drawBlockDamageTexture(DrawBlockHighlightEvent event, List<BlockPos> blocksToRender, int partialBlockDamage) throws Throwable {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = event.getPlayer();
        float partialTicks = event.getPartialTicks();
        double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        BlockRendererDispatcher rendererDispatcher = mc.getBlockRendererDispatcher();

        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        preRenderDamagedBlocks();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        bufferBuilder.setTranslation(-d3, -d4, -d5);
        bufferBuilder.noColor();

        TextureAtlasSprite sprite = ((TextureAtlasSprite[]) destroyBlockIconsHandle.invokeExact(event.getContext()))[partialBlockDamage];

        for (BlockPos blockPos : blocksToRender) {
            IBlockState blockState = mc.world.getBlockState(blockPos);
            if (blockState.getMaterial() == Material.AIR) {
                return;
            }
            TileEntity tileEntity = mc.world.getTileEntity(blockPos);
            boolean hasBreak = tileEntity != null && tileEntity.canRenderBreaking();
            if (!hasBreak) {
                rendererDispatcher.renderBlockDamage(blockState, blockPos, sprite, mc.world);
            }
        }

        Tessellator.getInstance().draw();
        bufferBuilder.setTranslation(0.0D, 0.0D, 0.0D);
        postRenderDamagedBlocks();
    }
}
