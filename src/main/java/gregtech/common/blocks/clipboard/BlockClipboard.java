package gregtech.common.blocks.clipboard;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.RayTracer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.util.GTUtility;
import gregtech.common.render.clipboard.TileEntityClipboardRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockClipboard extends Block implements ITileEntityProvider {

    private static final AxisAlignedBB CLIPBOARD_AABB = new AxisAlignedBB(2.75 / 16.0, 0.0, 0.0, 13.25 / 16.0, 1.0, 0.4 / 16.0);

    public static final BlockClipboard INSTANCE = new BlockClipboard(); // Mainly to access the default state.

    protected ThreadLocal<TileEntityClipboard> tileEntities = new ThreadLocal<>();

    public BlockClipboard() {
        super(Material.WOOD);
        setHardness(0);
        setSoundType(SoundType.WOOD);
        setTranslationKey("clipboard");
        setLightOpacity(1);
    }

    @Override
    @Nonnull
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if(getTileEntity(source, pos) != null) {
            return GTUtility.rotateAroundYAxis(CLIPBOARD_AABB, EnumFacing.NORTH, getTileEntity(source, pos).getFrontFacing());
        } else {
            return CLIPBOARD_AABB;
        }
    }

    private ItemStack getDropStack(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState) {
        if(this.hasTileEntity(blockState)) {
            return this.tileEntities.get().getClipboard();
        }
        return ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return getDropStack(world, pos, state);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.add(this.getDropStack(world, pos, state));
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return TileEntityClipboardRenderer.BLOCK_RENDER_TYPE;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        EnumFacing directionHangedFrom = getTileEntity(worldIn, pos).getFrontFacing().getOpposite();
        if (pos.offset(directionHangedFrom).equals(fromPos)) {
            if (worldIn.getBlockState(fromPos).getBlockFaceShape(worldIn, fromPos, EnumFacing.UP) != BlockFaceShape.SOLID) {
                worldIn.destroyBlock(pos, true);
            }
        }
    }

    @Override
    @Nonnull
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntityClipboard tileEntity = getTileEntity(worldIn, pos);
        if (tileEntity != null) {
            tileEntities.set(tileEntity);
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        tileEntities.set(te == null ? tileEntities.get() : (TileEntityClipboard) te);
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        tileEntities.set(null);
    }

    public static TileEntityClipboard getTileEntity(IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityClipboard)
            return ((TileEntityClipboard)tileEntity);
        return null;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        TileEntityClipboard clipboard = new TileEntityClipboard();
        clipboard.setWorld(worldIn);
        tileEntities.set(new TileEntityClipboard());
        return tileEntities.get();
    }

    public TileEntity createNewTileEntity(World worldIn, int meta, BlockPos pos) {
        TileEntityClipboard clipboard = new TileEntityClipboard();
        clipboard.setWorld(worldIn);
        clipboard.setPos(pos);
        tileEntities.set(new TileEntityClipboard());
        return tileEntities.get();
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntityClipboard tileEntity = getTileEntity(worldIn, pos);
        RayTraceResult rayTraceResult = RayTracer.retraceBlock(worldIn, playerIn, pos);
        ItemStack itemStack = playerIn.getHeldItem(hand);
        if (tileEntity == null || rayTraceResult == null) {
            return false;
        }
        if(tileEntity.getWorld() != null && !tileEntity.getWorld().isRemote) {
            TileEntityClipboardUIFactory.INSTANCE.openUI(this.tileEntities.get(), (EntityPlayerMP) playerIn);
        }
        return true;
    }
}
