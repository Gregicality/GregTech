package gregtech.common.pipelike.fluidpipe;

import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.util.EntityDamageUtil;
import gregtech.client.renderer.pipe.FluidPipeRenderer;
import gregtech.client.renderer.pipe.PipeRenderer;
import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.common.pipelike.fluidpipe.net.WorldFluidPipeNet;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public class BlockFluidPipe extends BlockMaterialPipe<FluidPipeType, FluidPipeProperties, WorldFluidPipeNet> {

    private final SortedMap<Material, FluidPipeProperties> enabledMaterials = new TreeMap<>();

    public BlockFluidPipe(FluidPipeType pipeType, MaterialRegistry registry) {
        super(pipeType, registry);
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_PIPES);
        setHarvestLevel(ToolClasses.WRENCH, 1);
    }

    public void addPipeMaterial(Material material, FluidPipeProperties fluidPipeProperties) {
        Preconditions.checkNotNull(material, "material");
        Preconditions.checkNotNull(fluidPipeProperties, "material %s fluidPipeProperties was null", material);
        Preconditions.checkArgument(material.getRegistry().getNameForObject(material) != null,
                "material %s is not registered", material);
        this.enabledMaterials.put(material, fluidPipeProperties);
    }

    public Collection<Material> getEnabledMaterials() {
        return Collections.unmodifiableSet(enabledMaterials.keySet());
    }

    @Override
    public Class<FluidPipeType> getPipeTypeClass() {
        return FluidPipeType.class;
    }

    @Override
    public WorldFluidPipeNet getWorldPipeNet(World world) {
        return WorldFluidPipeNet.getWorldPipeNet(world);
    }

    @Override
    protected FluidPipeProperties createProperties(FluidPipeType fluidPipeType, Material material) {
        return fluidPipeType.modifyProperties(enabledMaterials.getOrDefault(material, getFallbackType()));
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    public PipeRenderer getPipeRenderer() {
        return FluidPipeRenderer.INSTANCE;
    }

    @Override
    protected FluidPipeProperties getFallbackType() {
        return enabledMaterials.values().iterator().next();
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs itemIn, @NotNull NonNullList<ItemStack> items) {
        for (Material material : enabledMaterials.keySet()) {
            items.add(getItem(material));
        }
    }

    @Override
    public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        // TODO insert to neighbours
    }

    @Override
    public boolean canPipesConnect(IPipeTile<FluidPipeType, FluidPipeProperties> selfTile, EnumFacing side,
                                   IPipeTile<FluidPipeType, FluidPipeProperties> sideTile) {
        return selfTile instanceof TileEntityFluidPipe && sideTile instanceof TileEntityFluidPipe;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<FluidPipeType, FluidPipeProperties> selfTile, EnumFacing side,
                                         TileEntity tile) {
        return tile != null &&
                tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()) != null;
    }

    @Override
    public boolean isHoldingPipe(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        ItemStack stack = player.getHeldItemMainhand();
        return stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlockFluidPipe;
    }

    @Override
    public void onEntityCollision(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                  @NotNull Entity entityIn) {
        super.onEntityCollision(worldIn, pos, state, entityIn);
        if (worldIn.isRemote) return;
        IPipeTile<FluidPipeType, FluidPipeProperties> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile == null) return;
        TileEntityFluidPipe pipe = (TileEntityFluidPipe) pipeTile;
        if (pipe instanceof TileEntityFluidPipeTickable && pipe.getFrameMaterial() == null &&
                ((TileEntityFluidPipeTickable) pipe).getOffsetTimer() % 10 == 0) {
            if (entityIn instanceof EntityLivingBase) {
                if (((TileEntityFluidPipeTickable) pipe).getFluidTanks().length > 1) {
                    // apply temperature damage for the hottest and coldest pipe (multi fluid pipes)
                    int maxTemperature = Integer.MIN_VALUE;
                    int minTemperature = Integer.MAX_VALUE;
                    for (FluidTank tank : ((TileEntityFluidPipeTickable) pipe).getFluidTanks()) {
                        if (tank.getFluid() != null && tank.getFluid().amount > 0) {
                            maxTemperature = Math.max(maxTemperature,
                                    tank.getFluid().getFluid().getTemperature(tank.getFluid()));
                            minTemperature = Math.min(minTemperature,
                                    tank.getFluid().getFluid().getTemperature(tank.getFluid()));
                        }
                    }
                    if (maxTemperature != Integer.MIN_VALUE) {
                        EntityDamageUtil.applyTemperatureDamage((EntityLivingBase) entityIn, maxTemperature, 1.0F, 5);
                    }
                    if (minTemperature != Integer.MAX_VALUE) {
                        EntityDamageUtil.applyTemperatureDamage((EntityLivingBase) entityIn, minTemperature, 1.0F, 5);
                    }
                } else {
                    FluidTank tank = ((TileEntityFluidPipeTickable) pipe).getFluidTanks()[0];
                    if (tank.getFluid() != null && tank.getFluid().amount > 0) {
                        // Apply temperature damage for the pipe (single fluid pipes)
                        EntityDamageUtil.applyTemperatureDamage((EntityLivingBase) entityIn,
                                tank.getFluid().getFluid().getTemperature(), 1.0F, 5);
                    }
                }
            }
        }
    }

    @Override
    public TileEntityPipeBase<FluidPipeType, FluidPipeProperties> createNewTileEntity(boolean supportsTicking) {
        return new TileEntityFluidPipeTickable(); // fluid pipes are always ticking
    }

    @Override
    @NotNull
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return FluidPipeRenderer.INSTANCE.getBlockRenderType();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return FluidPipeRenderer.INSTANCE.getParticleTexture((IPipeTile<?, ?>) world.getTileEntity(blockPos));
    }
}
