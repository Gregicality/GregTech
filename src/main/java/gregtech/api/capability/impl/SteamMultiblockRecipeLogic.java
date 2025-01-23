package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.MultipleTankHandler;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SteamMultiblockRecipeLogic extends AbstractRecipeLogic {

    private MultipleTankHandler steamFluidTank;
    private IFluidTank steamFluidTankCombined;

    // EU per mB
    private final double conversionRate;

    public SteamMultiblockRecipeLogic(RecipeMapSteamMultiblockController tileEntity, RecipeMap<?> recipeMap,
                                      MultipleTankHandler steamFluidTank, double conversionRate) {
        super(tileEntity, recipeMap);
        this.steamFluidTank = steamFluidTank;
        this.conversionRate = conversionRate;
        setAllowOverclocking(false);
        combineSteamTanks();
    }

    public IFluidTank getSteamFluidTankCombined() {
        combineSteamTanks();
        return steamFluidTankCombined;
    }

    @Override
    protected IItemHandlerModifiable getInputInventory() {
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        return controller.getInputInventory();
    }

    @Override
    protected IItemHandlerModifiable getOutputInventory() {
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        return controller.getOutputInventory();
    }

    protected MultipleTankHandler getSteamFluidTank() {
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        return controller.getSteamFluidTank();
    }

    private void combineSteamTanks() {
        steamFluidTank = getSteamFluidTank();
        if (steamFluidTank == null)
            steamFluidTankCombined = new FluidTank(0);
        else {
            int capacity = steamFluidTank.size() * 64000;
            steamFluidTankCombined = new FluidTank(capacity);
            steamFluidTankCombined.fill(steamFluidTank.drain(capacity, false), true);
        }
    }

    @Override
    public void update() {
        // Fixes an annoying GTCE bug in AbstractRecipeLogic
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        if (isActive && !controller.isStructureFormed()) {
            progressTime = 0;
            wasActiveAndNeedsUpdate = true;
        }

        combineSteamTanks();
        super.update();
    }

    @Override
    protected long getEnergyInputPerSecond() {
        return 0;
    }

    @Override
    protected long getEnergyStored() {
        combineSteamTanks();
        return (long) Math.ceil(steamFluidTankCombined.getFluidAmount() * conversionRate);
    }

    @Override
    protected long getEnergyCapacity() {
        combineSteamTanks();
        return (long) Math.floor(steamFluidTankCombined.getCapacity() * conversionRate);
    }

    @Override
    protected boolean drawEnergy(long recipeEUt, boolean simulate) {
        combineSteamTanks();
        int resultDraw = GTUtility.safeCastLongToInt((long) Math.ceil(recipeEUt / conversionRate));
        return resultDraw >= 0 && steamFluidTankCombined.getFluidAmount() >= resultDraw &&
                steamFluidTank.drain(resultDraw, !simulate) != null;
    }

    @Override
    public long getMaxVoltage() {
        return GTValues.V[GTValues.LV];
    }

    @Override
    public boolean isAllowOverclocking() {
        return false;
    }

    @Override
    protected @Nullable Recipe setupAndConsumeRecipeInputs(@NotNull Recipe recipe,
                                                           @NotNull IItemHandlerModifiable importInventory) {
        RecipeMapSteamMultiblockController controller = (RecipeMapSteamMultiblockController) metaTileEntity;
        if (controller.checkRecipe(recipe, false)) {
            recipe = super.setupAndConsumeRecipeInputs(recipe, importInventory);
            if (recipe != null) {
                controller.checkRecipe(recipe, true);
                return recipe;
            }
        }
        return null;
    }

    @Override
    protected void completeRecipe() {
        super.completeRecipe();
        ventSteam();
    }

    private void ventSteam() {
        BlockPos machinePos = metaTileEntity.getPos();
        EnumFacing ventingSide = metaTileEntity.getFrontFacing();
        BlockPos ventingBlockPos = machinePos.offset(ventingSide);
        IBlockState blockOnPos = metaTileEntity.getWorld().getBlockState(ventingBlockPos);
        if (blockOnPos.getCollisionBoundingBox(metaTileEntity.getWorld(), ventingBlockPos) == Block.NULL_AABB) {
            performVentingAnimation(machinePos, ventingSide);
        } else if (blockOnPos.getBlock() == Blocks.SNOW_LAYER && blockOnPos.getValue(BlockSnow.LAYERS) == 1) {
            performVentingAnimation(machinePos, ventingSide);
            metaTileEntity.getWorld().destroyBlock(ventingBlockPos, false);
        }
    }

    private void performVentingAnimation(BlockPos machinePos, EnumFacing ventingSide) {
        WorldServer world = (WorldServer) metaTileEntity.getWorld();
        double posX = machinePos.getX() + 0.5 + ventingSide.getXOffset() * 0.6;
        double posY = machinePos.getY() + 0.5 + ventingSide.getYOffset() * 0.6;
        double posZ = machinePos.getZ() + 0.5 + ventingSide.getZOffset() * 0.6;

        world.spawnParticle(EnumParticleTypes.CLOUD, posX, posY, posZ,
                7 + GTValues.RNG.nextInt(3),
                ventingSide.getXOffset() / 2.0,
                ventingSide.getYOffset() / 2.0,
                ventingSide.getZOffset() / 2.0, 0.1);
        if (ConfigHolder.machines.machineSounds && !metaTileEntity.isMuffled()) {
            world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0f,
                    1.0f);
        }
    }

    @Override
    protected boolean hasEnoughPower(long eut, int duration) {
        long totalSteam = (long) (eut * duration / conversionRate);
        if (totalSteam > 0) {
            long steamStored = getEnergyStored();
            long steamCapacity = getEnergyCapacity();
            // if the required steam is larger than the full buffer, just require the full buffer
            if (steamCapacity < totalSteam) {
                return steamCapacity == steamStored;
            }
            // otherwise require the full amount of steam for the recipe
            return steamStored >= totalSteam;
        }
        // generation case unchanged
        return super.hasEnoughPower(eut, duration);
    }
}
