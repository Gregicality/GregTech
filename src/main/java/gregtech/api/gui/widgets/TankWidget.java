package gregtech.api.gui.widgets;

import gregtech.api.GTValues;
import gregtech.api.fluids.GTFluid;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.ingredient.IIngredientSlot;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.util.*;
import gregtech.client.utils.RenderUtil;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TankWidget extends Widget implements IIngredientSlot {

    public final IFluidTank fluidTank;

    public int fluidRenderOffset = 1;
    private boolean hideTooltip;
    private boolean alwaysShowFull;
    private boolean drawHoveringText;

    private boolean allowClickFilling;
    private boolean allowClickEmptying;

    private IGuiTexture[] backgroundTexture;
    private IGuiTexture overlayTexture;

    protected FluidStack lastFluidInTank;
    private int lastTankCapacity;
    protected boolean isClient;

    public TankWidget(IFluidTank fluidTank, int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        this.fluidTank = fluidTank;
        this.drawHoveringText = true;
    }

    public TankWidget setClient() {
        this.isClient = true;
        this.lastFluidInTank = fluidTank != null ? fluidTank.getFluid() != null ? fluidTank.getFluid().copy() : null :
                null;
        this.lastTankCapacity = fluidTank != null ? fluidTank.getCapacity() : 0;
        return this;
    }

    public TankWidget setHideTooltip(boolean hideTooltip) {
        this.hideTooltip = hideTooltip;
        return this;
    }

    public TankWidget setDrawHoveringText(boolean drawHoveringText) {
        this.drawHoveringText = drawHoveringText;
        return this;
    }

    public TankWidget setAlwaysShowFull(boolean alwaysShowFull) {
        this.alwaysShowFull = alwaysShowFull;
        return this;
    }

    public TankWidget setBackgroundTexture(IGuiTexture... backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    public TankWidget setOverlayTexture(IGuiTexture overlayTexture) {
        this.overlayTexture = overlayTexture;
        return this;
    }

    public TankWidget setFluidRenderOffset(int fluidRenderOffset) {
        this.fluidRenderOffset = fluidRenderOffset;
        return this;
    }

    public TankWidget setContainerClicking(boolean allowClickContainerFilling, boolean allowClickContainerEmptying) {
        this.allowClickFilling = allowClickContainerFilling;
        this.allowClickEmptying = allowClickContainerEmptying;
        return this;
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            return lastFluidInTank;
        }
        return null;
    }

    public String getFormattedFluidAmount() {
        return String.format("%,d", lastFluidInTank == null ? 0 : lastFluidInTank.amount);
    }

    public String getFluidLocalizedName() {
        return lastFluidInTank == null ? "" : lastFluidInTank.getLocalizedName();
    }

    /**
     * @deprecated use {@link #getFluidTextComponent()}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    @Deprecated
    public String getFluidUnlocalizedName() {
        return lastFluidInTank == null ? "" : lastFluidInTank.getUnlocalizedName();
    }

    @Nullable
    public TextComponentTranslation getFluidTextComponent() {
        if (lastFluidInTank == null) return null;
        if (lastFluidInTank.getFluid() instanceof GTFluid.GTMaterialFluid materialFluid) {
            return materialFluid.toTextComponentTranslation();
        }
        return new TextComponentTranslation(lastFluidInTank.getUnlocalizedName());
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundTexture != null) {
            for (IGuiTexture textureArea : backgroundTexture) {
                textureArea.draw(pos.x, pos.y, size.width, size.height);
            }
        }
        // do not draw fluids if they are handled by JEI - it draws them itself
        if (lastFluidInTank != null && !gui.isJEIHandled) {
            GlStateManager.disableBlend();
            FluidStack stackToDraw = lastFluidInTank;
            int drawAmount = alwaysShowFull ? lastFluidInTank.amount : lastTankCapacity;
            if (alwaysShowFull && lastFluidInTank.amount == 0) {
                stackToDraw = lastFluidInTank.copy();
                stackToDraw.amount = 1;
                drawAmount = 1;
            }
            RenderUtil.drawFluidForGui(stackToDraw, drawAmount,
                    pos.x + fluidRenderOffset, pos.y + fluidRenderOffset,
                    size.width - fluidRenderOffset, size.height - fluidRenderOffset);

            if (alwaysShowFull && !hideTooltip && drawHoveringText) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5, 0.5, 1);

                String s = TextFormattingUtil.formatLongToCompactString(lastFluidInTank.amount, 4) + "L";

                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
                fontRenderer.drawStringWithShadow(s,
                        (pos.x + (size.width / 3F)) * 2 - fontRenderer.getStringWidth(s) + 21,
                        (pos.y + (size.height / 3F) + 6) * 2, 0xFFFFFF);
                GlStateManager.popMatrix();
            }
            GlStateManager.enableBlend();
        }
        if (overlayTexture != null) {
            overlayTexture.draw(pos.x, pos.y, size.width, size.height);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (!hideTooltip && !gui.isJEIHandled && isMouseOverElement(mouseX, mouseY)) {
            List<String> tooltips = new ArrayList<>();
            if (lastFluidInTank != null) {
                Fluid fluid = lastFluidInTank.getFluid();
                tooltips.add(fluid.getLocalizedName(lastFluidInTank));

                // Amount Tooltip
                tooltips.add(
                        LocalizationUtils.format("gregtech.fluid.amount", lastFluidInTank.amount, lastTankCapacity));

                // Add various tooltips from the material
                List<String> formula = FluidTooltipUtil.getFluidTooltip(lastFluidInTank);
                if (formula != null) {
                    for (String s : formula) {
                        if (s.isEmpty()) continue;
                        tooltips.add(s);
                    }
                }

                // Add tooltip showing how many "ingot moles" (increments of 144) this fluid is if shift is held
                addIngotMolFluidTooltip(lastFluidInTank, tooltips);

            } else {
                tooltips.add(LocalizationUtils.format("gregtech.fluid.empty"));
                tooltips.add(LocalizationUtils.format("gregtech.fluid.amount", 0, lastTankCapacity));
            }
            if (allowClickEmptying && allowClickFilling) {
                tooltips.add(""); // Add an empty line to separate from the bottom material tooltips
                tooltips.add(LocalizationUtils.format("gregtech.fluid.click_combined"));
            } else if (allowClickFilling) {
                tooltips.add(""); // Add an empty line to separate from the bottom material tooltips
                tooltips.add(LocalizationUtils.format("gregtech.fluid.click_to_fill"));
            } else if (allowClickEmptying) {
                tooltips.add(""); // Add an empty line to separate from the bottom material tooltips
                tooltips.add(LocalizationUtils.format("gregtech.fluid.click_to_empty"));
            }
            drawHoveringText(ItemStack.EMPTY, tooltips, 300, mouseX, mouseY);
            GlStateManager.color(1.0f, 1.0f, 1.0f);
        }
    }

    @Override
    public void updateScreen() {
        if (isClient) {
            FluidStack fluidStack = fluidTank.getFluid();
            if (fluidTank.getCapacity() != lastTankCapacity) {
                this.lastTankCapacity = fluidTank.getCapacity();
            }
            if (fluidStack == null && lastFluidInTank != null) {
                this.lastFluidInTank = null;

            } else if (fluidStack != null) {
                if (!fluidStack.isFluidEqual(lastFluidInTank)) {
                    this.lastFluidInTank = fluidStack.copy();
                } else if (fluidStack.amount != lastFluidInTank.amount) {
                    this.lastFluidInTank.amount = fluidStack.amount;
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        FluidStack fluidStack = fluidTank.getFluid();
        if (fluidTank.getCapacity() != lastTankCapacity) {
            this.lastTankCapacity = fluidTank.getCapacity();
            writeUpdateInfo(0, buffer -> buffer.writeVarInt(lastTankCapacity));
        }
        if (fluidStack == null && lastFluidInTank != null) {
            this.lastFluidInTank = null;
            writeUpdateInfo(1, buffer -> {});
        } else if (fluidStack != null) {
            if (!fluidStack.isFluidEqual(lastFluidInTank)) {
                this.lastFluidInTank = fluidStack.copy();
                NBTTagCompound fluidStackTag = fluidStack.writeToNBT(new NBTTagCompound());
                writeUpdateInfo(2, buffer -> buffer.writeCompoundTag(fluidStackTag));
            } else if (fluidStack.amount != lastFluidInTank.amount) {
                this.lastFluidInTank.amount = fluidStack.amount;
                writeUpdateInfo(3, buffer -> buffer.writeVarInt(lastFluidInTank.amount));
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 0) {
            this.lastTankCapacity = buffer.readVarInt();
        } else if (id == 1) {
            this.lastFluidInTank = null;
        } else if (id == 2) {
            NBTTagCompound fluidStackTag;
            try {
                fluidStackTag = buffer.readCompoundTag();
            } catch (IOException ignored) {
                return;
            }
            this.lastFluidInTank = FluidStack.loadFluidStackFromNBT(fluidStackTag);
        } else if (id == 3 && lastFluidInTank != null) {
            this.lastFluidInTank.amount = buffer.readVarInt();
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            ItemStack clickResult = tryClickContainer(buffer.readBoolean());
            if (clickResult != ItemStack.EMPTY) {
                ((EntityPlayerMP) gui.entityPlayer).updateHeldItem();
            }
        }
    }

    private ItemStack tryClickContainer(boolean tryFillAll) {
        EntityPlayer player = gui.entityPlayer;
        ItemStack currentStack = player.inventory.getItemStack();
        if (currentStack == ItemStack.EMPTY || currentStack.getCount() == 0) return ItemStack.EMPTY;

        ItemStack heldItemSizedOne = currentStack.copy();
        heldItemSizedOne.setCount(1);
        IFluidHandlerItem fluidHandlerItem = heldItemSizedOne
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandlerItem == null) return ItemStack.EMPTY;

        FluidStack tankFluid = fluidTank.getFluid();
        FluidStack heldFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
        if (heldFluid != null && heldFluid.amount <= 0) {
            heldFluid = null;
        }

        if (tankFluid == null) {
            // Tank is empty, only try to drain the held item
            if (!allowClickEmptying) {
                // tank does not allow emptying cells into it, return
                return ItemStack.EMPTY;
            }
            if (heldFluid == null) {
                // held item has no fluid, return
                return ItemStack.EMPTY;
            }

            // empty held item into the tank
            return fillTankFromStack(heldFluid, tryFillAll);
        }

        if (heldFluid != null && fluidTank.getFluidAmount() < fluidTank.getCapacity()) {
            // held item has a fluid, and so does the tank. tank still has some room left in it
            // either action is possible here
            if (allowClickEmptying) {
                // try to empty the item into the tank
                return fillTankFromStack(heldFluid, tryFillAll);
            }
            if (!allowClickFilling) {
                // cannot fill the item from the tank, return
                return ItemStack.EMPTY;
            }
            // slot does not allow filling, so try to take from the slot
            return drainTankFromStack(tryFillAll);
        } else {
            // tank is full, and there is some fluid available to take
            if (!allowClickFilling) {
                // slot does not allow taking, return
                return ItemStack.EMPTY;
            }
            // try to take from the slot
            return drainTankFromStack(tryFillAll);
        }
    }

    private ItemStack fillTankFromStack(@NotNull FluidStack heldFluid, boolean tryFillAll) {
        EntityPlayer player = gui.entityPlayer;
        ItemStack heldItem = player.inventory.getItemStack();
        if (heldItem == ItemStack.EMPTY || heldItem.getCount() == 0) return ItemStack.EMPTY;

        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.setCount(1);
        FluidStack currentFluid = fluidTank.getFluid();
        // Fluid type does not match
        if (currentFluid != null && !currentFluid.isFluidEqual(heldFluid)) return ItemStack.EMPTY;

        int freeSpace = fluidTank.getCapacity() - fluidTank.getFluidAmount();
        if (freeSpace <= 0) return ItemStack.EMPTY;

        ItemStack itemStackEmptied = ItemStack.EMPTY;
        int fluidAmountTaken = 0;

        IFluidHandlerItem fluidHandler = heldItemSizedOne
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler == null) return ItemStack.EMPTY;

        FluidStack drained = fluidHandler.drain(freeSpace, true);
        if (drained != null && drained.amount > 0) {
            itemStackEmptied = fluidHandler.getContainer();
            fluidAmountTaken = drained.amount;
        }
        if (itemStackEmptied == ItemStack.EMPTY) {
            return ItemStack.EMPTY;
        }

        // find out how many fills we can do
        // same round down behavior as drain
        int additional = tryFillAll ? Math.min(freeSpace / fluidAmountTaken, heldItem.getCount()) : 1;
        FluidStack copiedFluidStack = heldFluid.copy();
        copiedFluidStack.amount = fluidAmountTaken * additional;
        fluidTank.fill(copiedFluidStack, true);

        itemStackEmptied.setCount(additional);
        replaceCursorItemStack(itemStackEmptied);
        playSound(heldFluid, true);
        return itemStackEmptied;
    }

    private ItemStack drainTankFromStack(boolean tryFillAll) {
        EntityPlayer player = gui.entityPlayer;
        ItemStack heldItem = player.inventory.getItemStack();
        if (heldItem == ItemStack.EMPTY || heldItem.getCount() == 0) return ItemStack.EMPTY;

        ItemStack heldItemSizedOne = heldItem.copy();
        heldItemSizedOne.setCount(1);
        FluidStack currentFluid = fluidTank.getFluid();
        if (currentFluid == null) return ItemStack.EMPTY;
        currentFluid = currentFluid.copy();

        int originalFluidAmount = fluidTank.getFluidAmount();
        IFluidHandlerItem handler = heldItemSizedOne.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,
                null);
        if (handler == null) return ItemStack.EMPTY;
        ItemStack filledContainer = fillFluidContainer(currentFluid, heldItemSizedOne);
        if (filledContainer != ItemStack.EMPTY) {
            int filledAmount = originalFluidAmount - currentFluid.amount;
            if (filledAmount <= 0) {
                return ItemStack.EMPTY;
            }
            fluidTank.drain(filledAmount, true);
            if (tryFillAll) {
                // Determine how many more items we can fill. One item is already filled. Integer division means
                // it will round down, so it will only fill equivalent fluid amounts. For example:
                // Click with 3 cells, with 2500L of fluid in the tank. 2 cells will be filled, and 500L will
                // be left behind in the tank.
                int additional = Math.min(heldItem.getCount() - 1, currentFluid.amount / filledAmount);
                fluidTank.drain(filledAmount * additional, true);
                filledContainer.grow(additional);
            }
            replaceCursorItemStack(filledContainer);
            playSound(currentFluid, false);
        }
        return filledContainer;
    }

    private ItemStack fillFluidContainer(FluidStack fluidStack, ItemStack itemStack) {
        IFluidHandlerItem fluidHandlerItem = itemStack
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandlerItem == null) return ItemStack.EMPTY;
        int filledAmount = fluidHandlerItem.fill(fluidStack, true);
        if (filledAmount > 0) {
            fluidStack.amount -= filledAmount;
            return fluidHandlerItem.getContainer();
        }
        return ItemStack.EMPTY;
    }

    /**
     * Replace the ItemStack on the player's cursor with the passed stack.
     * Use to replace empty cells with filled, or filled cells with empty.
     * If it is not fully emptied/filled, it will place the new items into the player inventory instead,
     * and shrink the held stack by the appropriate amount.
     */
    private void replaceCursorItemStack(ItemStack resultStack) {
        EntityPlayer player = gui.entityPlayer;
        int resultStackSize = resultStack.getMaxStackSize();
        while (resultStack.getCount() > resultStackSize) {
            player.inventory.getItemStack().shrink(resultStackSize);
            addItemToPlayerInventory(player, resultStack.splitStack(resultStackSize));
        }
        if (player.inventory.getItemStack().getCount() == resultStack.getCount()) {
            // every item on the cursor is mutated, so leave it there
            player.inventory.setItemStack(resultStack);
        } else {
            // some items not mutated. Mutated items go into the inventory/world.
            ItemStack heldStack = player.inventory.getItemStack();
            heldStack.shrink(resultStack.getCount());
            player.inventory.setItemStack(heldStack);
            addItemToPlayerInventory(player, resultStack);
        }
    }

    /** Place an item into the player's inventory, or drop it in-world as an item entity if it cannot fit. */
    private static void addItemToPlayerInventory(EntityPlayer player, ItemStack stack) {
        if (stack == null) return;
        if (!player.inventory.addItemStackToInventory(stack) && !player.world.isRemote) {
            EntityItem dropItem = player.entityDropItem(stack, 0);
            if (dropItem != null) dropItem.setPickupDelay(0);
        }
    }

    /** Play the appropriate fluid interaction sound for the fluid. */
    private void playSound(FluidStack fluid, boolean fill) {
        if (fluid == null) return;
        SoundEvent soundEvent;
        if (fill) {
            soundEvent = fluid.getFluid().getFillSound(fluid);
        } else {
            soundEvent = fluid.getFluid().getEmptySound(fluid);
        }
        EntityPlayer player = gui.entityPlayer;
        player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ,
                soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            ItemStack currentStack = gui.entityPlayer.inventory.getItemStack();
            if ((allowClickEmptying || allowClickFilling) &&
                    currentStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                writeClientAction(1, writer -> writer.writeBoolean(button == 0));
                playButtonClickSound();
                return true;
            }
        }
        return false;
    }

    public static void addIngotMolFluidTooltip(FluidStack fluidStack, List<String> tooltip) {
        // Add tooltip showing how many "ingot moles" (increments of 144) this fluid is if shift is held
        if (TooltipHelper.isShiftDown() && fluidStack.amount > GTValues.L) {
            int numIngots = fluidStack.amount / GTValues.L;
            int extra = fluidStack.amount % GTValues.L;
            String fluidAmount = String.format(" %,d L = %,d * %d L", fluidStack.amount, numIngots, GTValues.L);
            if (extra != 0) {
                fluidAmount += String.format(" + %d L", extra);
            }
            tooltip.add(TextFormatting.GRAY + LocalizationUtils.format("gregtech.gui.amount_raw") + fluidAmount);
        }
    }
}
