package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.RotorHolder;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.materialitem.MetaTurbineItem;
import gregtech.api.items.metaitem.stats.TurbineRotor;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.electric.generator.turbine.RotorFit;
import gregtech.core.advancement.AdvancementTriggers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RotorHolder2 extends MetaTileEntityMultiblockNotifiablePart implements RotorHolder,
                          IMultiblockAbilityPart<RotorHolder> {

    private final InventoryRotorHolder inventory;
    /**
     * Only exists on the server
     */
    private TurbineRotor rotor;
    /**
     * Exists on the server and client
     */
    private boolean frontFaceFree;
    /**
     * Exists on the server and client
     */
    private int rotorColor = -1;
    /**
     * Exists on the server and client
     */
    private boolean isRotorSpinning;

    private RotorFit rotorFit = RotorFit.TIGHT;

    public RotorHolder2(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier, false);
        this.inventory = new InventoryRotorHolder(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new RotorHolder2(metaTileEntityId, getTier());
    }

    @Override
    public IItemHandlerModifiable getImportItems() {
        return inventory;
    }

    @Override
    protected ModularUI createUI(@NotNull EntityPlayer entityPlayer) {
        return ModularUI.defaultBuilder()
                .label(6, 6, getMetaFullName())
                .slot(inventory, 0, 79, 36, GuiTextures.SLOT, GuiTextures.TURBINE_OVERLAY)
                .bindPlayerInventory(entityPlayer.inventory)
                .build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.rotor_holder.tooltip1"));
        tooltip.add(I18n.format("gregtech.machine.rotor_holder.tooltip2"));
        tooltip.add(I18n.format("gregtech.universal.disabled"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            return;
        }

        if (getOffsetTimer() % 20 == 0) {
            boolean isFrontFree = checkTurbineFaceFree();
            if (isFrontFree != this.frontFaceFree) {
                this.frontFaceFree = isFrontFree;
                writeCustomData(GregtechDataCodes.FRONT_FACE_FREE, buf -> buf.writeBoolean(this.frontFaceFree));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public SoundEvent getSound() {
        MultiblockControllerBase controller = getController();
        if (controller != null) {
            RecipeMap<?> recipeMap = controller.getRecipeMap();
            if (recipeMap != null) {
                return recipeMap.getSound();
            }
        }

        return super.getSound();
    }

    @Override
    public MultiblockAbility<RotorHolder> getAbility() {
        return MultiblockAbility.ROTOR_HOLDER_2;
    }

    @Override
    public void registerAbilities(@NotNull List<RotorHolder> abilityList) {
        abilityList.add(this);
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    private void setRotorColor(int color) {
        if (rotorColor != color) {
            this.rotorColor = color;
            writeCustomData(GregtechDataCodes.UPDATE_ROTOR_COLOR, buf -> buf.writeInt(color));
        }
    }

    @Override
    public @Nullable TurbineRotor rotor() {
        return rotor;
    }

    private void setRotor(@Nullable TurbineRotor rotor) {
        this.rotor = rotor;
    }

    @Override
    public boolean isObstructed() {
        return !this.frontFaceFree;
    }

    @Override
    public void setSpinning(boolean spinning) {
        if (isRotorSpinning != spinning) {
            isRotorSpinning = spinning;
            writeCustomData(GregtechDataCodes.IS_ROTOR_LOOPING, buf -> buf.writeBoolean(isRotorSpinning));
        }
    }

    @Override
    public boolean isActive() {
        return isRotorSpinning;
    }

    @Override
    public @NotNull RotorFit rotorFitting() {
        return this.rotorFit;
    }

    @Override
    public boolean damageRotor(int amount) {
        MetaTurbineItem item = inventory.getTurbineItem();
        if (item == null) {
            return true;
        }

        if (item.damage(inventory.getRotorStack(), amount)) {
            inventory.setStackInSlot(0, ItemStack.EMPTY);
            setSpinning(false);
            getWorld().playSound(null, getPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        }
        return false;
    }

    private boolean checkTurbineFaceFree() {
        final EnumFacing front = getFrontFacing();
        // this can be anything really, as long as it is not up/down when on Y axis
        final EnumFacing upwards = front.getAxis() == EnumFacing.Axis.Y ? EnumFacing.NORTH : EnumFacing.UP;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(getPos());
        for (int left = -1; left <= 1; left++) {
            for (int up = -1; up <= 1; up++) {
                // flip doesn't affect anything here since we are checking a square anyway
                final BlockPos checkPos = RelativeDirection.offsetPos(pos, front, upwards, false, up, left, 1);
                final IBlockState state = getWorld().getBlockState(checkPos);
                if (!state.getBlock().isAir(state, getWorld(), checkPos)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        return onRotorHolderInteract(playerIn) || super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                 CuboidRayTraceResult hitResult) {
        return onRotorHolderInteract(playerIn) || super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        return onRotorHolderInteract(playerIn);
    }

    @Override
    public void onLeftClick(EntityPlayer player, EnumFacing facing, CuboidRayTraceResult hitResult) {
        onRotorHolderInteract(player);
    }

    private boolean onRotorHolderInteract(@NotNull EntityPlayer player) {
        if (player.isCreative()) return false;

        if (!getWorld().isRemote && isRotorSpinning) {
            float damageApplied = Math.min(1, rotor.overflowEfficiency() * 4);
            player.attackEntityFrom(DamageSources.getTurbineDamage(), damageApplied);
            AdvancementTriggers.ROTOR_HOLDER_DEATH.trigger((EntityPlayerMP) player);
            return true;
        }
        return isRotorSpinning;
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, inventory);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("inventory", inventory.serializeNBT());
        data.setBoolean("Spinning", isRotorSpinning);
        data.setBoolean("FrontFree", frontFaceFree);
        data.setByte("rotorFit", (byte) rotorFit.ordinal());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.inventory.deserializeNBT(data.getCompoundTag("inventory"));
        this.isRotorSpinning = data.getBoolean("Spinning");
        this.frontFaceFree = data.getBoolean("FrontFree");
        this.rotorFit = RotorFit.VALUES[data.getByte("rotorFit")];
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(rotorColor);
        buf.writeBoolean(isRotorSpinning);
        buf.writeBoolean(frontFaceFree);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.rotorColor = buf.readInt();
        this.isRotorSpinning = buf.readBoolean();
        this.frontFaceFree = buf.readBoolean();
        scheduleRenderUpdate();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.FRONT_FACE_FREE) {
            this.frontFaceFree = buf.readBoolean();
        } else if (dataId == GregtechDataCodes.IS_ROTOR_LOOPING) {
            this.isRotorSpinning = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_ROTOR_COLOR) {
            this.rotorColor = buf.readInt();
            scheduleRenderUpdate();
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ROTOR_HOLDER_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.LARGE_TURBINE_ROTOR_RENDERER.renderSided(renderState, translation, pipeline, getFrontFacing(),
                getController() != null, rotorColor != -1, isRotorSpinning, rotorColor);
    }

    private static class InventoryRotorHolder extends NotifiableItemStackHandler {

        private final RotorHolder2 rotorHolder;

        public InventoryRotorHolder(RotorHolder2 metaTileEntity) {
            super(metaTileEntity, 1, null, false);
            this.rotorHolder = metaTileEntity;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onLoad() {
            super.onLoad();
            rotorHolder.setRotor(getRotor());
            rotorHolder.setRotorColor(getRotorColor());
        }

        @Override
        public void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            rotorHolder.setRotor(getRotor());
            rotorHolder.setRotorColor(getRotorColor());
            rotorHolder.scheduleRenderUpdate();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return super.isItemValid(slot, stack) && stack.getItem() instanceof MetaTurbineItem;
        }

        private @NotNull ItemStack getRotorStack() {
            if (hasRotor()) {
                return getStackInSlot(0);
            }
            return ItemStack.EMPTY;
        }

        private boolean hasRotor() {
            return getRotor() != null;
        }

        private @Nullable TurbineRotor getRotor() {
            ItemStack stack = getStackInSlot(0);
            if (stack.getItem() instanceof MetaTurbineItem turbineItem) {
                return turbineItem.getRotorStats(stack);
            }
            return null;
        }

        public @Nullable MetaTurbineItem getTurbineItem() {
            if (getStackInSlot(0).getItem() instanceof MetaTurbineItem turbineItem) {
                return turbineItem;
            }
            return null;
        }

        private int getRotorColor() {
            TurbineRotor rotor = getRotor();
            if (rotor == null) {
                return -1;
            }

            return rotor.color();
        }
    }
}
