package gregtech.common.metatileentities.multi.multiblockpart.hpca;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IHPCAComponentHatch;
import gregtech.api.capability.IHPCAComputationProvider;
import gregtech.api.capability.IHPCACoolantProvider;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityHPCA;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MetaTileEntityHPCAComponent extends MetaTileEntityMultiblockPart implements
                                                  IMultiblockAbilityPart<IHPCAComponentHatch>, IHPCAComponentHatch {

    private boolean damaged;

    public MetaTileEntityHPCAComponent(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.ZPM);
    }

    public abstract boolean isAdvanced();

    public boolean doesAllowBridging() {
        return false;
    }

    public abstract SimpleOverlayRenderer getFrontOverlay();

    public SimpleOverlayRenderer getFrontActiveOverlay() {
        return getFrontOverlay();
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public MultiblockAbility<IHPCAComponentHatch> getAbility() {
        return MultiblockAbility.HPCA_COMPONENT;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer;
            var controller = getController();
            if (controller != null && controller.isActive()) {
                renderer = getFrontActiveOverlay();
            } else {
                renderer = getFrontOverlay();
            }
            if (renderer != null) {
                EnumFacing facing = getFrontFacing();
                // Always render this outwards in the HPCA, in case it is not placed outwards in structure.
                // Check for HPCA specifically since these components could potentially be used in other multiblocks.
                if (controller instanceof MetaTileEntityHPCA) {
                    facing = RelativeDirection.RIGHT.getRelativeFacing(
                            controller.getFrontFacing(),
                            controller.getUpwardsFacing(),
                            controller.isFlipped());
                }
                renderer.renderSided(facing, renderState, translation, pipeline);
            }
        }
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        return isAdvanced() ? Textures.ADVANCED_COMPUTER_CASING : Textures.COMPUTER_CASING;
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        if (isBridge()) {
            tooltip.add(I18n.format("gregtech.machine.hpca.component_type.bridge"));
        }

        final int upkeepEUt = getUpkeepEUt();
        final int maxEUt = getMaxEUt();
        if (upkeepEUt != 0 && upkeepEUt != maxEUt) {
            tooltip.add(I18n.format("gregtech.machine.hpca.component_general.upkeep_eut", upkeepEUt));
        }
        if (maxEUt != 0) {
            tooltip.add(I18n.format("gregtech.machine.hpca.component_general.max_eut", maxEUt));
        }

        if (this instanceof IHPCACoolantProvider provider) {
            if (provider.isActiveCooler()) {
                tooltip.add(I18n.format("gregtech.machine.hpca.component_type.cooler_active"));
                tooltip.add(I18n.format("gregtech.machine.hpca.component_type.cooler_active_coolant",
                        provider.getMaxCoolantPerTick(), I18n.format(Materials.PCBCoolant.getUnlocalizedName())));
            } else {
                tooltip.add(I18n.format("gregtech.machine.hpca.component_type.cooler_passive"));
            }
            tooltip.add(
                    I18n.format("gregtech.machine.hpca.component_type.cooler_cooling", provider.getCoolingAmount()));
        }

        if (this instanceof IHPCAComputationProvider provider) {
            tooltip.add(I18n.format("gregtech.machine.hpca.component_type.computation_cwut", provider.getCWUPerTick()));
            tooltip.add(I18n.format("gregtech.machine.hpca.component_type.computation_cooling",
                    provider.getCoolingPerTick()));
        }

        if (canBeDamaged()) {
            tooltip.add(TooltipHelper.BLINKING_ORANGE + I18n.format("gregtech.machine.hpca.component_type.damaged"));
        }
    }

    // Unnecessary to show this since it is just Wrench rotation
    @Override
    public boolean showToolUsages() {
        return false;
    }

    // Handle damaged state

    @Override
    public final boolean isBridge() {
        return doesAllowBridging() && !(canBeDamaged() && isDamaged());
    }

    @Override
    public boolean isDamaged() {
        return canBeDamaged() && damaged;
    }

    @Override
    public void setDamaged(boolean damaged) {
        if (!canBeDamaged()) return;
        if (this.damaged != damaged) {
            this.damaged = damaged;
            markDirty();
            if (getWorld() != null && !getWorld().isRemote) {
                writeCustomData(GregtechDataCodes.DAMAGE_STATE, buf -> buf.writeBoolean(damaged));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if (canBeDamaged()) {
            data.setBoolean("Damaged", damaged);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (canBeDamaged()) {
            this.damaged = data.getBoolean("Damaged");
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        if (canBeDamaged()) {
            buf.writeBoolean(damaged);
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (canBeDamaged()) {
            this.damaged = buf.readBoolean();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (canBeDamaged() && dataId == GregtechDataCodes.DAMAGE_STATE) {
            this.damaged = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public boolean shouldDropWhenDestroyed() {
        return super.shouldDropWhenDestroyed() && !(canBeDamaged() && isDamaged());
    }

    @Override
    public void getDrops(@NotNull List<@NotNull ItemStack> dropsList, @Nullable EntityPlayer harvester) {
        if (canBeDamaged() && isDamaged()) {
            if (isAdvanced()) {
                dropsList.add(MetaBlocks.COMPUTER_CASING
                        .getItemVariant(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING));
            } else {
                dropsList
                        .add(MetaBlocks.COMPUTER_CASING.getItemVariant(BlockComputerCasing.CasingType.COMPUTER_CASING));
            }
        }
    }

    @Override
    public String getMetaName() {
        if (canBeDamaged() && isDamaged()) {
            return super.getMetaName() + ".damaged";
        }
        return super.getMetaName();
    }
}
