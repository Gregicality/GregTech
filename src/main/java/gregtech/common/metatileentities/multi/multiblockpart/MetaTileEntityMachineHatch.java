package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.BlockableSlotWidget;
import gregtech.api.metatileentity.IMachineHatchMultiblock;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityMachineHatch extends MetaTileEntityMultiblockNotifiablePart
                                        implements IMultiblockAbilityPart<IItemHandlerModifiable> {

    private final IItemHandlerModifiable machineHandler;

    public MetaTileEntityMachineHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier, false);
        this.machineHandler = new LimitedImportHandler(this);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMachineHatch(metaTileEntityId, getTier());
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return MultiblockAbility.MACHINE_HATCH;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(machineHandler);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return machineHandler;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,
                18 + 18 + 94)
                .label(10, 5, getMetaFullName());

        builder.widget(new BlockableSlotWidget(machineHandler, 0, 81, 18, true, true)
                .setIsBlocked(this::isSlotBlocked)
                .setBackgroundTexture(GuiTextures.SLOT));

        return builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 + 12).build(getHolder(),
                entityPlayer);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    private int getMachineLimit() {
        if (getController() instanceof IMachineHatchMultiblock) {
            return ((IMachineHatchMultiblock) getController()).getMachineLimit();
        }
        return 64;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.machine_hatch.processing_array"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    private boolean isSlotBlocked() {
        if (getController() instanceof RecipeMapMultiblockController controller) {
            return controller.isActive();
        }
        return false;
    }

    private class LimitedImportHandler extends NotifiableItemStackHandler {

        public LimitedImportHandler(MetaTileEntity metaTileEntity) {
            super(metaTileEntity, 1, null, false);
        }

        @NotNull
        @Override
        // Insert item returns the remainder stack that was not inserted
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            // If the item was not valid, nothing from the stack can be inserted
            if (!isItemValid(slot, stack)) {
                return stack;
            }

            // Return Empty if passed Empty
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            // If the stacks do not match, nothing can be inserted
            if (!ItemStackHashStrategy.comparingAllButCount().equals(stack, this.getStackInSlot(slot)) &&
                    !this.getStackInSlot(slot).isEmpty()) {
                return stack;
            }

            int amountInSlot = this.getStackInSlot(slot).getCount();
            int slotLimit = getSlotLimit(slot);

            // If the current stack size in the slot is greater than the limit of the Multiblock, nothing can be
            // inserted
            if (amountInSlot >= slotLimit) {
                return stack;
            }

            // This will always be positive and greater than zero if reached
            int spaceAvailable = slotLimit - amountInSlot;

            // Insert the minimum amount between the amount of space available and the amount being inserted
            int amountToInsert = Math.min(spaceAvailable, stack.getCount());

            // The remainder that was not inserted
            int remainderAmount = stack.getCount() - amountToInsert;

            // Handle any remainder
            ItemStack remainder = ItemStack.EMPTY;

            if (remainderAmount > 0) {
                remainder = stack.copy();
                remainder.setCount(remainderAmount);
            }

            if (!simulate) {
                // Perform the actual insertion
                ItemStack temp = stack.copy();
                temp.setCount(amountInSlot + amountToInsert);
                this.setStackInSlot(slot, temp);
            }

            return remainder;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            boolean slotMatches = this.getStackInSlot(slot).isEmpty() ||
                    ItemStackHashStrategy.comparingAllButCount().equals(this.getStackInSlot(slot), stack);

            MultiblockControllerBase controller = getController();
            if (controller instanceof IMachineHatchMultiblock)
                return slotMatches && GTUtility.isMachineValidForMachineHatch(stack,
                        ((IMachineHatchMultiblock) controller).getBlacklist());

            // If the controller is null, this part is not attached to any Multiblock
            return slotMatches;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (isSlotBlocked()) {
                return ItemStack.EMPTY;
            }
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public <T> void addToNotifiedList(MetaTileEntity metaTileEntity, T handler, boolean isExport) {
            if (metaTileEntity instanceof IMachineHatchMultiblock && metaTileEntity.isValid()) {
                ((IMachineHatchMultiblock) metaTileEntity).notifyMachineChanged();
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return MetaTileEntityMachineHatch.this.getMachineLimit();
        }
    }
}
