package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.block.BlockDragonEgg;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.dragon.phase.PhaseChargingPlayer;
import net.minecraft.entity.boss.dragon.phase.PhaseList;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.biome.BiomeEndDecorator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static gregtech.api.capability.GregtechDataCodes.IS_WORKING;

public class MetaTileEntityMagicEnergyAbsorber extends TieredMetaTileEntity {

    private final IntList connectedCrystalsIds = new IntArrayList();
    private boolean hasDragonEggAmplifier = false;
    private boolean isActive = false;

    public MetaTileEntityMagicEnergyAbsorber(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.EV);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMagicEnergyAbsorber(metaTileEntityId);
    }

    @SideOnly(Side.CLIENT)
    private ICubeRenderer getRenderer() {
        return isActive ? Textures.MAGIC_ENERGY_ABSORBER_ACTIVE : Textures.MAGIC_ENERGY_ABSORBER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getRenderer().getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        getRenderer().render(renderState, translation, colouredPipeline);
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote)
            return;
        if (!(getWorld().provider instanceof WorldProviderEnd)) {
            return; // don't try to do anything outside end dimension
        }
        if (getOffsetTimer() % 20 == 0 || isFirstTick()) {
            updateDragonEggStatus();
        }
        if (getOffsetTimer() % 200 == 0 || isFirstTick()) {
            updateConnectedCrystals();
        }

        int totalEnergyGeneration = 0;
        // enhanced for loops cause boxing and unboxing with FastUtil collections
        // noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < connectedCrystalsIds.size(); i++) {
            // since we don't check quite often, check twice before outputting energy
            if (getWorld().getEntityByID(connectedCrystalsIds.get(i)) instanceof EntityEnderCrystal) {
                totalEnergyGeneration += hasDragonEggAmplifier ? 128 : 32;
            }
        }
        if (totalEnergyGeneration > 0) {
            energyContainer.changeEnergy(totalEnergyGeneration);
        }
        setActive(totalEnergyGeneration > 0);
    }

    private void setActive(boolean isActive) {
        if (this.isActive != isActive) {
            this.isActive = isActive;
            if (!getWorld().isRemote) {
                writeCustomData(IS_WORKING, w -> w.writeBoolean(isActive));
            }
        }
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == IS_WORKING) {
            this.isActive = buf.readBoolean();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!getWorld().isRemote) {
            resetConnectedEnderCrystals();
        }
    }

    @Override
    protected boolean isEnergyEmitter() {
        return true;
    }

    private void updateConnectedCrystals() {
        this.connectedCrystalsIds.clear();
        final double maxDistance = 64 * 64;
        List<EntityEnderCrystal> enderCrystals = Arrays.stream(BiomeEndDecorator.getSpikesForWorld(getWorld()))
                .flatMap(endSpike -> getWorld()
                        .getEntitiesWithinAABB(EntityEnderCrystal.class, endSpike.getTopBoundingBox()).stream())
                .filter(crystal -> crystal.getDistanceSq(getPos()) < maxDistance)
                .collect(Collectors.toList());

        for (EntityEnderCrystal entityEnderCrystal : enderCrystals) {
            BlockPos beamTarget = entityEnderCrystal.getBeamTarget();
            if (beamTarget == null) {
                // if beam target is null, set ourselves as beam target
                entityEnderCrystal.setBeamTarget(getPos());
                this.connectedCrystalsIds.add(entityEnderCrystal.getEntityId());
            } else if (beamTarget.equals(getPos())) {
                // if beam target is ourselves, just add it to list
                this.connectedCrystalsIds.add(entityEnderCrystal.getEntityId());
            }
        }

        for (EntityDragon entityDragon : getWorld().getEntities(EntityDragon.class, EntitySelectors.IS_ALIVE)) {
            if (entityDragon.healingEnderCrystal != null &&
                    connectedCrystalsIds.contains(entityDragon.healingEnderCrystal.getEntityId())) {
                // if dragon is healing from crystal we draw energy from, reset it's healing crystal
                entityDragon.healingEnderCrystal = null;
                // if dragon is holding pattern, than deal damage and set it's phase to attack ourselves
                if (entityDragon.getPhaseManager().getCurrentPhase().getType() == PhaseList.HOLDING_PATTERN) {
                    entityDragon.attackEntityFrom(DamageSource.causeExplosionDamage((EntityLivingBase) null), 10.0f);
                    entityDragon.getPhaseManager().setPhase(PhaseList.CHARGING_PLAYER);
                    ((PhaseChargingPlayer) entityDragon.getPhaseManager().getCurrentPhase())
                            .setTarget(new Vec3d(getPos()));
                }
            }
        }
    }

    private void resetConnectedEnderCrystals() {
        // enhanced for loops cause boxing and unboxing with FastUtil collections
        // noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < connectedCrystalsIds.size(); i++) {
            EntityEnderCrystal entityEnderCrystal = (EntityEnderCrystal) getWorld()
                    .getEntityByID(connectedCrystalsIds.get(i));
            if (entityEnderCrystal != null && getPos().equals(entityEnderCrystal.getBeamTarget())) {
                // on removal, reset ender crystal beam location so somebody can use it
                entityEnderCrystal.setBeamTarget(null);
            }
        }
        connectedCrystalsIds.clear();
    }

    private void updateDragonEggStatus() {
        IBlockState blockState = getWorld().getBlockState(getPos().up());
        this.hasDragonEggAmplifier = blockState.getBlock() instanceof BlockDragonEgg;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick() {
        if (isActive() && !this.hasDragonEggAmplifier) {
            final BlockPos pos = getPos();
            for (int i = 0; i < 4; i++) {
                getWorld().spawnParticle(EnumParticleTypes.PORTAL,
                        pos.getX() + 0.5F,
                        pos.getY() + GTValues.RNG.nextFloat(),
                        pos.getZ() + 0.5F,
                        (GTValues.RNG.nextFloat() - 0.5F) * 0.5F,
                        (GTValues.RNG.nextFloat() - 0.5F) * 0.5F,
                        (GTValues.RNG.nextFloat() - 0.5F) * 0.5F);
            }
        }
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
