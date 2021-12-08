package gregtech.api.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.gui.resources.ResourceHelper;
import gregtech.common.ConfigHolder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import java.util.EnumMap;
import java.util.Map;

public class SimpleSidedCubeRenderer implements ICubeRenderer, IIconRegister {

    public enum RenderSide {
        TOP, BOTTOM, SIDE;

        public static RenderSide bySide(EnumFacing side) {
            if (side == EnumFacing.UP) {
                return TOP;
            } else if (side == EnumFacing.DOWN) {
                return BOTTOM;
            } else return SIDE;
        }
    }

    protected final String basePath;

    @SideOnly(Side.CLIENT)
    protected Map<RenderSide, TextureAtlasSprite> sprites;

    @SideOnly(Side.CLIENT)
    protected Map<RenderSide, TextureAtlasSprite> spritesEmissive;

    public SimpleSidedCubeRenderer(String basePath) {
        this.basePath = basePath;
        Textures.iconRegisters.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        this.sprites = new EnumMap<>(RenderSide.class);
        this.spritesEmissive = new EnumMap<>(RenderSide.class);
        for (RenderSide overlayFace : RenderSide.values()) {
            String faceName = overlayFace.name().toLowerCase();
            ResourceLocation resourceLocation = new ResourceLocation(GTValues.MODID, String.format("blocks/%s/%s", basePath, faceName));
            sprites.put(overlayFace, textureMap.registerSprite(resourceLocation));
            ResourceLocation emissiveLocation = new ResourceLocation(GTValues.MODID, String.format("blocks/%s/%s_emissive", basePath, faceName));
            if (ResourceHelper.isTextureExist(emissiveLocation)) {
                spritesEmissive.put(overlayFace, textureMap.registerSprite(emissiveLocation));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getSpriteOnSide(RenderSide renderSide) {
        return sprites.get(renderSide);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleSprite() {
        return getSpriteOnSide(RenderSide.TOP);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds) {
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            RenderSide overlayFace = RenderSide.bySide(renderSide);
            TextureAtlasSprite renderSprite = sprites.get(overlayFace);
            Textures.renderFace(renderState, translation, pipeline, renderSide, bounds, renderSprite);
            TextureAtlasSprite spriteEmissive = spritesEmissive.get(overlayFace);
            if (spriteEmissive != null) {
                if (ConfigHolder.U.clientConfig.machinesEmissiveTextures) {
                    IVertexOperation[] lightPipeline = ArrayUtils.add(pipeline, new LightMapOperation(240, 240));
                    Textures.renderFaceBloom(renderState, translation, lightPipeline, renderSide, bounds, spriteEmissive);
                } else Textures.renderFace(renderState, translation, pipeline, renderSide, bounds, spriteEmissive);
            }
        }
    }
}
