package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.GradientUtil;
import gregtech.api.util.Mods;
import gregtech.common.pipelike.PipeCollectorWalker;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.util.AEColor;
import appeng.tile.networking.TileCableBus;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class ColorSprayBehavior extends AbstractUsableBehaviour implements IItemDurabilityManager {

    private final ItemStack empty;
    private final EnumDyeColor color;
    private final Pair<Color, Color> durabilityBarColors;

    public ColorSprayBehavior(ItemStack empty, int totalUses, int color) {
        super(totalUses);
        this.empty = empty;
        EnumDyeColor[] colors = EnumDyeColor.values();
        this.color = color >= colors.length || color < 0 ? null : colors[color];
        // default to a gray color if this.color is null (like for solvent spray)
        int colorValue = this.color == null ? 0x969696 : this.color.colorValue;
        this.durabilityBarColors = GradientUtil.getGradient(colorValue, 10);
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos, facing, stack)) {
            return ActionResult.newResult(EnumActionResult.FAIL, player.getHeldItem(hand));
        }
        if (!tryPaintBlock(player, world, pos, facing, hand)) {
            return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
        }
        useItemDurability(player, hand, stack, empty.copy());
        world.playSound(null, player.posX, player.posY, player.posZ, GTSoundEvents.SPRAY_CAN_TOOL,
                SoundCategory.PLAYERS, 1.0f, 1.0f);
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    private boolean tryPaintBlock(EntityPlayer player, World world, BlockPos pos, EnumFacing side, EnumHand hand) {
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (color == null) {
            return tryStripBlockColor(player, world, pos, block, side, hand);
        }
        return tryPaintSpecialBlock(player, world, pos, block, hand) ||
                block.recolorBlock(world, pos, side, this.color);
    }

    private boolean tryPaintSpecialBlock(EntityPlayer player, World world, BlockPos pos, Block block, EnumHand hand) {
        if (block == Blocks.GLASS) {
            IBlockState newBlockState = Blocks.STAINED_GLASS.getDefaultState()
                    .withProperty(BlockStainedGlass.COLOR, this.color);
            world.setBlockState(pos, newBlockState);
            return true;
        }
        if (block == Blocks.GLASS_PANE) {
            IBlockState newBlockState = Blocks.STAINED_GLASS_PANE.getDefaultState()
                    .withProperty(BlockStainedGlassPane.COLOR, this.color);
            world.setBlockState(pos, newBlockState);
            return true;
        }
        if (block == Blocks.HARDENED_CLAY) {
            IBlockState newBlockState = Blocks.STAINED_HARDENED_CLAY.getDefaultState()
                    .withProperty(BlockColored.COLOR, this.color);
            world.setBlockState(pos, newBlockState);
            return true;
        }
        TileEntity te = world.getTileEntity(pos);
        if (player.isSneaking() && te instanceof IPipeTile<?, ?>) {
            return traversePipes(player, world, hand, pos);
        }
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            if (te instanceof TileCableBus cable) {
                // do not try to recolor if it already is this color
                if (cable.getColor().ordinal() != color.ordinal()) {
                    cable.recolourBlock(null, AEColor.values()[color.ordinal()], player);
                    return true;
                }
            }
        }
        return false;
    }

    // note: automatically uses durability from recolouring pipes, apart from the last use, as this allows for proper
    // animation of the item's use
    private boolean traversePipes(EntityPlayer player, World world, EnumHand hand, BlockPos startPos) {
        TileEntity connectedTe = world.getTileEntity(startPos);
        // I LOVE LAMBDAS
        final boolean[] painted = { false };
        ItemStack heldItem = player.getHeldItem(hand);
        int colour = this.color == null ? -1 : this.color.colorValue;
        if (connectedTe instanceof IPipeTile<?, ?>startPipe) {
            PipeCollectorWalker.collectPipeNet(world, startPos, startPipe, pipe -> {
                if (getUsesLeft(heldItem) == 0) {
                    return false;
                }
                if (pipe.getPaintingColor() != colour) {
                    pipe.setPaintingColor(colour);
                    pipe.scheduleRenderUpdate();
                    if (getUsesLeft(heldItem) == 1) {
                        if (painted[0]) {
                            useItemDurability(player, hand, heldItem, empty.copy());
                        }
                        painted[0] = true;
                        return false;
                    }
                    // Off by one durability as the final use is handled by onItemUse, along with the use animation
                    if (painted[0]) {
                        useItemDurability(player, hand, heldItem, empty.copy());
                    }
                    painted[0] = true;
                }
                return true;
            });

        }
        return painted[0];
    }

    @SuppressWarnings("unchecked, rawtypes")
    private boolean tryStripBlockColor(EntityPlayer player, World world, BlockPos pos, Block block,
                                       EnumFacing side, EnumHand hand) {
        // MC special cases
        if (block == Blocks.STAINED_GLASS) {
            world.setBlockState(pos, Blocks.GLASS.getDefaultState());
            return true;
        }
        if (block == Blocks.STAINED_GLASS_PANE) {
            world.setBlockState(pos, Blocks.GLASS_PANE.getDefaultState());
            return true;
        }
        if (block == Blocks.STAINED_HARDENED_CLAY) {
            world.setBlockState(pos, Blocks.HARDENED_CLAY.getDefaultState());
            return true;
        }

        // MTE special case
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IGregTechTileEntity) {
            MetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
            if (mte != null) {
                if (mte.isPainted()) {
                    mte.setPaintingColor(-1);
                    return true;
                } else return false;
            }
        }

        // TileEntityPipeBase special case
        if (player.isSneaking() && te instanceof IPipeTile<?, ?>) {
            return traversePipes(player, world, hand, pos);
        }

        // AE2 cable special case
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            if (te instanceof TileCableBus cable) {
                // do not try to strip color if it is already colorless
                if (cable.getColor() != AEColor.TRANSPARENT) {
                    cable.recolourBlock(null, AEColor.TRANSPARENT, player);
                    return true;
                } else return false;
            }
        }

        // General case
        IBlockState state = world.getBlockState(pos);
        for (IProperty prop : state.getProperties().keySet()) {
            if (prop.getName().equals("color") && prop.getValueClass() == EnumDyeColor.class) {
                IBlockState defaultState = block.getDefaultState();
                EnumDyeColor defaultColor = EnumDyeColor.WHITE;
                try {
                    // try to read the default color value from the default state instead of just
                    // blindly setting it to default state, and potentially resetting other values
                    defaultColor = (EnumDyeColor) defaultState.getValue(prop);
                } catch (IllegalArgumentException ignored) {
                    // no default color, we may have to fallback to WHITE here
                    // other mods that have custom behavior can be done as
                    // special cases above on a case-by-case basis
                }
                block.recolorBlock(world, pos, side, defaultColor);
                return true;
            }
        }

        return false;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        int remainingUses = getUsesLeft(itemStack);
        if (color != null) {
            lines.add(I18n.format("behaviour.paintspray." + this.color.getTranslationKey() + ".tooltip"));
        } else {
            lines.add(I18n.format("behaviour.paintspray.solvent.tooltip"));
        }
        lines.add(I18n.format("behaviour.paintspray.uses", remainingUses));
        lines.add(I18n.format("behaviour.paintspray.offhand"));
        lines.add(I18n.format("behaviour.paintspray.crouch"));
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        return (double) getUsesLeft(itemStack) / totalUses;
    }

    @Nullable
    @Override
    public Pair<Color, Color> getDurabilityColorsForDisplay(ItemStack itemStack) {
        return durabilityBarColors;
    }

    @Override
    public boolean doDamagedStateColors(ItemStack itemStack) {
        return false;
    }
}
