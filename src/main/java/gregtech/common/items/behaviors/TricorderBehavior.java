package gregtech.common.items.behaviors;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IQuantumController;
import gregtech.api.capability.IQuantumStorage;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.TextFormattingUtil;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.common.ConfigHolder;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TricorderBehavior implements IItemBehaviour {

    private final int debugLevel;
    private int energyCost = Integer.MAX_VALUE;

    public TricorderBehavior(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
                                           float hitY, float hitZ, EnumHand hand) {
        if (!world.isRemote && !world.isAirBlock(pos)) {

            List<ITextComponent> info = getScannerInfo(player, world, pos);
            if (player.isCreative() || drainEnergy(player.getHeldItem(hand), energyCost, true)) {
                if (!player.isCreative()) {
                    drainEnergy(player.getHeldItem(hand), energyCost, false);
                }
                for (ITextComponent line : info) {
                    player.sendMessage(line);
                }
                if (ConfigHolder.client.toolUseSounds)
                    world.playSound(null, pos, GTSoundEvents.TRICORDER_TOOL, SoundCategory.PLAYERS, 1, 1);
            } else {
                player.sendMessage(new TextComponentTranslation("behavior.prospector.not_enough_energy"));
            }
        }
        return EnumActionResult.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    public List<ITextComponent> getScannerInfo(EntityPlayer player, World world, BlockPos pos) {
        int energyCost = 100;

        List<ITextComponent> list = new ArrayList<>();

        TileEntity tileEntity = world.getTileEntity(pos);

        IBlockState state = world.getBlockState(pos);
        state = state.getBlock().getActualState(state, world, pos);
        Block block = state.getBlock();

        // coordinates of the block

        list.add(new TextComponentTranslation("behavior.tricorder.position",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(pos.getX()))
                        .setStyle(new Style().setColor(TextFormatting.AQUA)),
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(pos.getY()))
                        .setStyle(new Style().setColor(TextFormatting.AQUA)),
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(pos.getZ()))
                        .setStyle(new Style().setColor(TextFormatting.AQUA)),
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(world.provider.getDimension()))
                        .setStyle(new Style().setColor(TextFormatting.AQUA))));

        // hardness and blast resistance
        list.add(new TextComponentTranslation("behavior.tricorder.block_hardness",
                new TextComponentTranslation(
                        TextFormattingUtil.formatNumbers(block.getBlockHardness(state, world, pos)))
                                .setStyle(new Style().setColor(TextFormatting.YELLOW)),
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(block.getExplosionResistance(player)))
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))));

        if (debugLevel > 2) {
            for (Map.Entry<IProperty<?>, Comparable<?>> prop : state.getProperties().entrySet()) {
                list.add(new TextComponentTranslation("behavior.tricorder.state",
                        new TextComponentTranslation(prop.getKey().getName()),
                        new TextComponentTranslation(prop.getValue().toString())
                                .setStyle(new Style().setColor(TextFormatting.AQUA))));
            }
            if (state instanceof IExtendedBlockState) {
                IExtendedBlockState extState = (IExtendedBlockState) state;
                for (Map.Entry<IUnlistedProperty<?>, Optional<?>> prop : extState.getUnlistedProperties().entrySet()) {
                    if (prop.getValue().isPresent()) {
                        list.add(new TextComponentTranslation("behavior.tricorder.state",
                                new TextComponentTranslation(prop.getKey().getName()),
                                new TextComponentTranslation(prop.getValue().get().toString())
                                        .setStyle(new Style().setColor(TextFormatting.AQUA))));
                    }
                }
            }
        }

        MetaTileEntity metaTileEntity;
        if (tileEntity instanceof IGregTechTileEntity) {
            metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
            if (metaTileEntity == null)
                return list;

            // name of the machine
            list.add(new TextComponentTranslation("behavior.tricorder.block_name",
                    new TextComponentTranslation(metaTileEntity.getMetaFullName())
                            .setStyle(new Style().setColor(TextFormatting.BLUE)),
                    new TextComponentTranslation(TextFormattingUtil
                            .formatNumbers(
                                    metaTileEntity.getRegistry().getIdByObjectName(metaTileEntity.metaTileEntityId)))
                                            .setStyle(new Style().setColor(TextFormatting.BLUE))));

            UUID owner = metaTileEntity.getOwner();
            if (owner != null) {
                EntityPlayer ownerEntity = metaTileEntity.getWorld().getPlayerEntityByUUID(owner);
                if (ownerEntity != null) {
                    list.add(new TextComponentTranslation("behavior.tricorder.mte_owner",
                            new TextComponentTranslation(ownerEntity.getName())
                                    .setStyle(new Style().setColor(TextFormatting.AQUA))));
                } else {
                    list.add(new TextComponentTranslation("behavior.tricorder.mte_owner_offline")
                            .setStyle(new Style().setColor(TextFormatting.YELLOW)));
                }
            } else {
                list.add(new TextComponentTranslation("behavior.tricorder.mte_owner_null")
                        .setStyle(new Style().setColor(TextFormatting.RED)));
            }

            list.add(new TextComponentTranslation("behavior.tricorder.divider"));

            // fluid tanks
            FluidTankList tanks = metaTileEntity.getImportFluids();
            int tankIndex = 0;
            boolean allTanksEmpty = true;
            if (tanks != null && !tanks.getFluidTanks().isEmpty()) {
                energyCost += 500;
                for (int i = 0; i < tanks.getFluidTanks().size(); i++) {
                    IFluidTank tank = tanks.getTankAt(i);
                    if (tank.getFluid() == null)
                        continue;

                    allTanksEmpty = false;
                    list.add(new TextComponentTranslation("behavior.tricorder.tank", i,
                            new TextComponentTranslation(TextFormattingUtil.formatNumbers(tank.getFluid().amount))
                                    .setStyle(new Style().setColor(TextFormatting.GREEN)),
                            new TextComponentTranslation(TextFormattingUtil.formatNumbers(tank.getCapacity()))
                                    .setStyle(new Style().setColor(TextFormatting.YELLOW)),
                            new TextComponentTranslation(tank.getFluid().getLocalizedName())
                                    .setStyle(new Style().setColor(TextFormatting.GOLD))));
                }
                tankIndex += tanks.getFluidTanks().size();
            }
            tanks = metaTileEntity.getExportFluids();
            if (tanks != null && !tanks.getFluidTanks().isEmpty()) {
                energyCost += 500;
                for (int i = 0; i < tanks.getFluidTanks().size(); i++) {
                    IFluidTank tank = tanks.getTankAt(i);
                    if (tank.getFluid() == null)
                        continue;

                    allTanksEmpty = false;
                    list.add(new TextComponentTranslation("behavior.tricorder.tank", tankIndex + i,
                            new TextComponentTranslation(TextFormattingUtil.formatNumbers(tank.getFluid().amount))
                                    .setStyle(new Style().setColor(TextFormatting.GREEN)),
                            new TextComponentTranslation(TextFormattingUtil.formatNumbers(tank.getCapacity()))
                                    .setStyle(new Style().setColor(TextFormatting.YELLOW)),
                            new TextComponentTranslation(tank.getFluid().getLocalizedName())
                                    .setStyle(new Style().setColor(TextFormatting.GOLD))));
                }
            }

            if (allTanksEmpty && (metaTileEntity.getImportFluids() != null || metaTileEntity.getExportFluids() != null))
                list.add(new TextComponentTranslation("behavior.tricorder.tanks_empty"));

            // sound muffling
            energyCost += 500;
            if (metaTileEntity.isMuffled())
                list.add(new TextComponentTranslation("behavior.tricorder.muffled")
                        .setStyle(new Style().setColor(TextFormatting.GREEN)));

            // workable progress info
            IWorkable workable = metaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null);
            if (workable != null) {
                if (!workable.isWorkingEnabled()) {
                    list.add(new TextComponentTranslation("behavior.tricorder.machine_disabled")
                            .setStyle(new Style().setColor(TextFormatting.RED)));
                }
                // if (workable.wasShutdown()) { //todo
                // list.add(new TextComponentTranslation("behavior.tricorder.machine_power_loss").setStyle(new
                // Style().setColor(TextFormatting.RED)));
                // }
                energyCost += 400;
                if (workable.getMaxProgress() > 0) {
                    list.add(new TextComponentTranslation("behavior.tricorder.machine_progress",
                            new TextComponentTranslation(TextFormattingUtil.formatNumbers(workable.getProgress()))
                                    .setStyle(new Style().setColor(TextFormatting.GREEN)),
                            new TextComponentTranslation(TextFormattingUtil.formatNumbers(workable.getMaxProgress()))
                                    .setStyle(new Style().setColor(TextFormatting.YELLOW))));
                }
            }

            // energy container
            IEnergyContainer container = metaTileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER,
                    null);
            if (container != null && container.getEnergyCapacity() > 0) {
                list.add(new TextComponentTranslation("behavior.tricorder.divider"));
                if (container.getInputVoltage() > 0) {
                    list.add(new TextComponentTranslation("behavior.tricorder.energy_container_in",
                            new TextComponentTranslation(TextFormattingUtil.formatNumbers(container.getInputVoltage()))
                                    .setStyle(new Style().setColor(TextFormatting.RED)),
                            new TextComponentTranslation(
                                    GTValues.VN[GTUtility.getTierByVoltage(container.getInputVoltage())])
                                            .setStyle(new Style().setColor(TextFormatting.RED)),
                            new TextComponentTranslation(TextFormattingUtil.formatNumbers(container.getInputAmperage()))
                                    .setStyle(new Style().setColor(TextFormatting.RED))));
                }
                if (container.getOutputVoltage() > 0) {
                    list.add(new TextComponentTranslation("behavior.tricorder.energy_container_out",
                            new TextComponentTranslation(TextFormattingUtil.formatNumbers(container.getOutputVoltage()))
                                    .setStyle(new Style().setColor(TextFormatting.RED)),
                            new TextComponentTranslation(
                                    GTValues.VN[GTUtility.getTierByVoltage(container.getOutputVoltage())])
                                            .setStyle(new Style().setColor(TextFormatting.RED)),
                            new TextComponentTranslation(
                                    TextFormattingUtil.formatNumbers(container.getOutputAmperage()))
                                            .setStyle(new Style().setColor(TextFormatting.RED))));
                }
                list.add(new TextComponentTranslation("behavior.tricorder.energy_container_storage",
                        new TextComponentTranslation(TextFormattingUtil.formatNumbers(container.getEnergyStored()))
                                .setStyle(new Style().setColor(TextFormatting.GREEN)),
                        new TextComponentTranslation(TextFormattingUtil.formatNumbers(container.getEnergyCapacity()))
                                .setStyle(new Style().setColor(TextFormatting.YELLOW))));
            }

            // machine-specific info
            IDataInfoProvider provider = null;
            if (tileEntity instanceof IDataInfoProvider)
                provider = (IDataInfoProvider) tileEntity;
            else if (metaTileEntity instanceof IDataInfoProvider)
                provider = (IDataInfoProvider) metaTileEntity;

            if (provider != null) {
                list.add(new TextComponentTranslation("behavior.tricorder.divider"));

                list.addAll(provider.getDataInfo());
            }

            // quantum storage
            if (metaTileEntity instanceof IQuantumController quantumController) {
                list.add(new TextComponentTranslation("behavior.tricorder.divider"));
                long eut = quantumController.getEnergyUsage(); // eu per 10 ticks
                int tier = GTUtility.getTierByVoltage(eut / 10);
                list.add(new TextComponentTranslation("behavior.tricorder.quantum_controller.usage",
                        TextFormatting.RED + String.format("%.1f", eut / 10d) + TextFormatting.RESET,
                        GTValues.VNF[tier]));
                var handler = quantumController.getHandler();
                list.add(new TextComponentTranslation("behavior.tricorder.quantum_controller.connected_items",
                        TextFormatting.RED.toString() + handler.getItemDelegate().getSlots()));
                list.add(new TextComponentTranslation("behavior.tricorder.quantum_controller.connected_fluids",
                        TextFormatting.RED.toString() + handler.getFluidDelegate().getTanks()));
            } else if (metaTileEntity instanceof IQuantumStorage<?>storage) {
                var qcontrollor = storage.getQuantumController();
                if (qcontrollor != null) {
                    long eut = qcontrollor.getTypeEnergy(storage);

                    list.add(new TextComponentTranslation("behavior.tricorder.divider"));
                    list.add(new TextComponentTranslation("behavior.tricorder.quantum_storage.usage",
                            TextFormatting.RED + String.format("%.1f", eut / 10d)));
                }
            }

        } else if (tileEntity instanceof IPipeTile) {
            // pipes need special name handling
            IPipeTile<?, ?> pipeTile = (IPipeTile<?, ?>) tileEntity;

            if (pipeTile.getPipeBlock().getRegistryName() != null) {
                list.add(new TextComponentTranslation("behavior.tricorder.block_name",
                        new TextComponentTranslation(
                                LocalizationUtils.format(pipeTile.getPipeBlock().getTranslationKey()))
                                        .setStyle(new Style().setColor(TextFormatting.BLUE)),
                        new TextComponentTranslation(
                                TextFormattingUtil.formatNumbers(block.getMetaFromState(world.getBlockState(pos))))
                                        .setStyle(new Style().setColor(TextFormatting.BLUE))));
            }

            // pipe-specific info
            if (tileEntity instanceof IDataInfoProvider) {
                IDataInfoProvider provider = (IDataInfoProvider) tileEntity;

                list.add(new TextComponentTranslation("behavior.tricorder.divider"));

                list.addAll(provider.getDataInfo());
            }

            if (tileEntity instanceof TileEntityFluidPipe) {
                // getting fluid info always costs 500
                energyCost += 500;
            }
        } else if (tileEntity instanceof IDataInfoProvider) {
            IDataInfoProvider provider = (IDataInfoProvider) tileEntity;

            list.add(new TextComponentTranslation("behavior.tricorder.divider"));

            list.addAll(provider.getDataInfo());
        } else {
            list.add(new TextComponentTranslation("behavior.tricorder.block_name",
                    new TextComponentTranslation(LocalizationUtils.format(block.getLocalizedName()))
                            .setStyle(new Style().setColor(TextFormatting.BLUE)),
                    new TextComponentTranslation(
                            TextFormattingUtil.formatNumbers(block.getMetaFromState(world.getBlockState(pos))))
                                    .setStyle(new Style().setColor(TextFormatting.BLUE))));
        }

        // crops (adds 1000EU)

        // bedrock fluids
        list.add(new TextComponentTranslation("behavior.tricorder.divider"));
        Fluid fluid = BedrockFluidVeinHandler.getFluidInChunk(world, pos.getX() / 16, pos.getZ() / 16);// -# to only
                                                                                                       // read
        if (fluid != null) {
            FluidStack stack = new FluidStack(fluid,
                    BedrockFluidVeinHandler.getOperationsRemaining(world, pos.getX() / 16, pos.getZ() / 16));
            double fluidPercent = stack.amount * 100.0 / BedrockFluidVeinHandler.MAXIMUM_VEIN_OPERATIONS;

            if (player.isCreative()) {
                list.add(new TextComponentTranslation("behavior.tricorder.bedrock_fluid.amount",
                        new TextComponentTranslation(fluid.getLocalizedName(stack))
                                .setStyle(new Style().setColor(TextFormatting.GOLD)),
                        new TextComponentTranslation(String.valueOf(
                                BedrockFluidVeinHandler.getFluidYield(world, pos.getX() / 16, pos.getZ() / 16)))
                                        .setStyle(new Style().setColor(TextFormatting.GOLD)),
                        new TextComponentTranslation(String.valueOf(fluidPercent))
                                .setStyle(new Style().setColor(TextFormatting.YELLOW))));
            } else {
                list.add(new TextComponentTranslation("behavior.tricorder.bedrock_fluid.amount_unknown",
                        new TextComponentTranslation(String.valueOf(fluidPercent))
                                .setStyle(new Style().setColor(TextFormatting.YELLOW))));
            }
        } else {
            list.add(new TextComponentTranslation("behavior.tricorder.bedrock_fluid.nothing"));
        }

        // pollution
        // if (GT_Pollution.hasPollution(currentChunk)) {
        // list.add("Pollution in Chunk: " + TextFormatting.RED +
        // GTUtility.formatNumbers(GT_Pollution.getPollution(currentChunk)) + TextFormatting.RESET + " gibbl");
        // } else {
        // list.add(TextFormatting.GREEN + "No Pollution in Chunk! HAYO!" + TextFormatting.RESET);
        // }

        // debug TODO
        if (tileEntity instanceof MetaTileEntityHolder) {
            list.addAll(((MetaTileEntityHolder) tileEntity).getDebugInfo(player, debugLevel));
        }

        this.energyCost = energyCost;
        return list;
    }

    private boolean drainEnergy(@NotNull ItemStack stack, long amount, boolean simulate) {
        if (debugLevel > 2)
            return true;

        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null)
            return false;

        return electricItem.discharge(amount, Integer.MAX_VALUE, true, false, simulate) >= amount;
    }
}
