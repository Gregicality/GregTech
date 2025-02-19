package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOrientedCubeRenderer;
import gregtech.client.renderer.texture.custom.FireboxActiveRenderer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.capability.GregtechDataCodes.SYNC_CONTROLLER;

public abstract class MetaTileEntityMultiblockPart extends MetaTileEntity
                                                   implements IMultiblockPart, ITieredMetaTileEntity {

    private final int tier;
    private BlockPos controllerPos;
    private MultiblockControllerBase controllerTile;
    protected ICubeRenderer hatchTexture = null;

    public MetaTileEntityMultiblockPart(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId);
        this.tier = tier;
        initializeInventory();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseTexture().getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        ICubeRenderer baseTexture = getBaseTexture();
        pipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));

        if (baseTexture instanceof FireboxActiveRenderer || baseTexture instanceof SimpleOrientedCubeRenderer) {
            baseTexture.renderOriented(renderState, translation, pipeline, getFrontFacing());
        } else {
            baseTexture.render(renderState, translation, pipeline);
        }
    }

    public int getTier() {
        return tier;
    }

    public MultiblockControllerBase getController() {
        if (getWorld() != null && getWorld().isRemote) { // check this only clientside
            if (controllerTile == null && controllerPos != null) {
                this.controllerTile = (MultiblockControllerBase) GTUtility.getMetaTileEntity(getWorld(), controllerPos);
            }
        }
        if (controllerTile != null && (controllerTile.getHolder() == null ||
                !controllerTile.isValid() ||
                !(getWorld().isRemote || controllerTile.getMultiblockParts().contains(this)))) {
            // tile can become invalid for many reasons, and can also forgot to remove us once we aren't in structure
            // anymore
            // so check it here to prevent bugs with dangling controller reference and wrong texture
            this.controllerTile = null;
        }
        return controllerTile;
    }

    public ICubeRenderer getBaseTexture() {
        MultiblockControllerBase controller = getController();
        if (controller != null) {
            return this.hatchTexture = controller.getBaseTexture(this);
        } else if (this.hatchTexture != null) {
            if (hatchTexture != Textures.getInactiveTexture(hatchTexture)) {
                return this.hatchTexture = Textures.getInactiveTexture(hatchTexture);
            }
            return this.hatchTexture;
        } else {
            return Textures.VOLTAGE_CASINGS[tier];
        }
    }

    public boolean shouldRenderOverlay() {
        MultiblockControllerBase controller = getController();
        return controller == null || controller.shouldRenderOverlay(this);
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.writeInitialSyncData(buf);
        MultiblockControllerBase controller = getController();
        buf.writeBoolean(controller != null);
        if (controller != null) {
            buf.writeBlockPos(controller.getPos());
        }
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            this.controllerPos = buf.readBlockPos();
            this.controllerTile = null;
        }
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == SYNC_CONTROLLER) {
            if (buf.readBoolean()) {
                this.controllerPos = buf.readBlockPos();
                this.controllerTile = null;
            } else {
                this.controllerPos = null;
                this.controllerTile = null;
            }
            scheduleRenderUpdate();
        }
    }

    private void setController(MultiblockControllerBase controller1) {
        this.controllerTile = controller1;
        if (!getWorld().isRemote) {
            writeCustomData(SYNC_CONTROLLER, writer -> {
                writer.writeBoolean(controllerTile != null);
                if (controllerTile != null) {
                    writer.writeBlockPos(controllerTile.getPos());
                }
            });
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        MultiblockControllerBase controller = getController();
        if (!getWorld().isRemote && controller != null) {
            controller.invalidateStructure();
        }
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        setController(controllerBase);
        scheduleRenderUpdate();
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        setController(null);
        scheduleRenderUpdate();
    }

    @Override
    public boolean isAttachedToMultiBlock() {
        return getController() != null;
    }

    @Override
    public int getDefaultPaintingColor() {
        return !isAttachedToMultiBlock() && hatchTexture == null ? super.getDefaultPaintingColor() : 0xFFFFFF;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        MultiblockControllerBase controllerBase = getController();
        if (controllerBase == null) return super.getIsWeatherOrTerrainResistant();
        return controllerBase.isMultiblockPartWeatherResistant(this);
    }
}
