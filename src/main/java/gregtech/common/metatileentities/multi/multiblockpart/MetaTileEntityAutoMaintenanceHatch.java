package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityAutoMaintenanceHatch extends MetaTileEntityMultiblockPart implements
                                                IMultiblockAbilityPart<IMaintenanceHatch>, IMaintenanceHatch {

    public MetaTileEntityAutoMaintenanceHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 3);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityAutoMaintenanceHatch(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            Textures.MAINTENANCE_OVERLAY_FULL_AUTO.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.disabled"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public void setTaped(boolean ignored) {}

    @Override
    public void storeMaintenanceData(byte ignored1, int ignored2) {}

    @Override
    public boolean hasMaintenanceData() {
        return true;
    }

    @Override
    public Tuple<Byte, Integer> readMaintenanceData() {
        return new Tuple<>((byte) 0b111111, 0);
    }

    @Override
    public boolean isFullAuto() {
        return true;
    }

    @Override
    public double getDurationMultiplier() {
        return 1.0;
    }

    @Override
    public double getTimeMultiplier() {
        return 1.0;
    }

    @Override
    public boolean startWithoutProblems() {
        return true;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public MultiblockAbility<IMaintenanceHatch> getAbility() {
        return MultiblockAbility.MAINTENANCE_HATCH;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        if (ConfigHolder.machines.enableMaintenance) {
            super.getSubItems(creativeTab, subItems);
            // keeps things in order despite IDs being out of order, due to the Cleaning Hatch being added later
            subItems.add(MetaTileEntities.CLEANING_MAINTENANCE_HATCH.getStackForm());
        }
    }
}
