package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMaintenance;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.gui.widget.among_us.FixWiringTaskWidget;
import gregtech.common.inventory.handlers.TapeItemStackHandler;
import gregtech.common.items.MetaItems;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static gregtech.api.capability.GregtechDataCodes.*;

public class MetaTileEntityMaintenanceHatch extends MetaTileEntityMultiblockPart
                                            implements IMultiblockAbilityPart<IMaintenanceHatch>, IMaintenanceHatch {

    private final boolean isConfigurable;
    private GTItemStackHandler itemStackHandler;
    private boolean isTaped;

    // Used to store state temporarily if the Controller is broken
    private byte maintenanceProblems = -1;
    private int timeActive = -1;

    private BigDecimal durationMultiplier = BigDecimal.ONE;

    // Some stats used for the Configurable Maintenance Hatch
    private static final BigDecimal MAX_DURATION_MULTIPLIER = BigDecimal.valueOf(1.1);
    private static final BigDecimal MIN_DURATION_MULTIPLIER = BigDecimal.valueOf(0.9);
    private static final BigDecimal DURATION_ACTION_AMOUNT = BigDecimal.valueOf(0.01);
    private static final Function<Double, Double> TIME_ACTION = (d) -> {
        if (d < 1.0)
            return -20.0 * d + 21;
        else
            return -8.0 * d + 9;
    };

    public MetaTileEntityMaintenanceHatch(ResourceLocation metaTileEntityId, boolean isConfigurable) {
        super(metaTileEntityId, isConfigurable ? 3 : 1);
        this.isConfigurable = isConfigurable;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityMaintenanceHatch(metaTileEntityId, isConfigurable);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        if (shouldRenderOverlay()) {
            (isConfigurable ? Textures.MAINTENANCE_OVERLAY_CONFIGURABLE :
                    isTaped ? Textures.MAINTENANCE_OVERLAY_TAPED : Textures.MAINTENANCE_OVERLAY)
                            .renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.itemStackHandler = new TapeItemStackHandler(this, 1);
        this.itemInventory = itemStackHandler;
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, itemStackHandler);
    }

    /**
     * Sets this Maintenance Hatch as being duct taped
     * 
     * @param isTaped is the state of the hatch being taped or not
     */
    @Override
    public void setTaped(boolean isTaped) {
        this.isTaped = isTaped;
        if (!getWorld().isRemote) {
            writeCustomData(IS_TAPED, buf -> buf.writeBoolean(isTaped));
            markDirty();
        }
    }

    /**
     * Stores maintenance data to this MetaTileEntity
     * 
     * @param maintenanceProblems is the byte value representing the problems
     * @param timeActive          is the int value representing the total time the parent multiblock has been active
     */
    @Override
    public void storeMaintenanceData(byte maintenanceProblems, int timeActive) {
        this.maintenanceProblems = maintenanceProblems;
        this.timeActive = timeActive;
        if (!getWorld().isRemote) {
            writeCustomData(STORE_MAINTENANCE, buf -> {
                buf.writeByte(maintenanceProblems);
                buf.writeInt(timeActive);
            });
        }
    }

    /**
     *
     * @return whether this maintenance hatch has maintenance data
     */
    @Override
    public boolean hasMaintenanceData() {
        return this.maintenanceProblems != -1;
    }

    /**
     * reads this MetaTileEntity's maintenance data
     * 
     * @return Tuple of Byte, Integer corresponding to the maintenance problems, and total time active
     */
    @Override
    public Tuple<Byte, Integer> readMaintenanceData() {
        Tuple<Byte, Integer> data = new Tuple<>(this.maintenanceProblems, this.timeActive);
        storeMaintenanceData((byte) -1, -1);
        return data;
    }

    @Override
    public boolean startWithoutProblems() {
        return isConfigurable;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % 20 == 0) {
            MultiblockControllerBase controller = getController();
            if (controller instanceof IMaintenance) {
                if (((IMaintenance) controller).hasMaintenanceProblems()) {
                    if (consumeDuctTape(this.itemInventory, 0)) {
                        fixAllMaintenanceProblems();
                        setTaped(true);
                    }
                }
            }
        }
    }

    /**
     * Fixes the maintenance problems of this hatch's Multiblock Controller
     * 
     * @param entityPlayer the player performing the fixing
     */
    private void fixMaintenanceProblems(@Nullable EntityPlayer entityPlayer) {
        if (!(this.getController() instanceof IMaintenance))
            return;

        if (!((IMaintenance) this.getController()).hasMaintenanceProblems())
            return;

        if (entityPlayer != null) {
            // Fix automatically on slot click by player in Creative Mode
            if (entityPlayer.capabilities.isCreativeMode) {
                fixAllMaintenanceProblems();
                return;
            }
            // Then for every slot in the player's main inventory, try to duct tape fix
            for (int i = 0; i < entityPlayer.inventory.mainInventory.size(); i++) {
                if (consumeDuctTape(new ItemStackHandler(entityPlayer.inventory.mainInventory), i)) {
                    fixAllMaintenanceProblems();
                    setTaped(true);
                    return;
                }
            }
            // Lastly for each problem the multi has, try to fix with tools
            fixProblemsWithTools(((IMaintenance) this.getController()).getMaintenanceProblems(), entityPlayer);
        }
    }

    /**
     *
     * Handles duct taping for manual and auto-taping use
     *
     * @param handler is the handler to get duct tape from
     * @param slot    is the inventory slot to check for tape
     * @return true if tape was consumed, else false
     */
    private boolean consumeDuctTape(@Nullable IItemHandler handler, int slot) {
        if (handler == null)
            return false;
        return consumeDuctTape(null, handler.getStackInSlot(slot));
    }

    private boolean consumeDuctTape(@Nullable EntityPlayer player, ItemStack itemStack) {
        if (!itemStack.isEmpty() && itemStack.isItemEqual(MetaItems.DUCT_TAPE.getStackForm())) {
            if (player == null || !player.capabilities.isCreativeMode) {
                itemStack.shrink(1);
            }
            return true;
        }
        return false;
    }

    /**
     * Attempts to fix a provided maintenance problem with a tool in the player's
     * inventory, if the tool exists.
     *
     * @param problems     Problem Flags
     * @param entityPlayer Target Player which their inventory would be scanned for tools to fix
     */
    private void fixProblemsWithTools(byte problems, EntityPlayer entityPlayer) {
        List<String> toolsToMatch = Arrays.asList(new String[6]);
        boolean proceed = false;
        for (byte index = 0; index < 6; index++) {
            if (((problems >> index) & 1) == 0) {
                proceed = true;
                switch (index) {
                    case 0 -> toolsToMatch.set(0, ToolClasses.WRENCH);
                    case 1 -> toolsToMatch.set(1, ToolClasses.SCREWDRIVER);
                    case 2 -> toolsToMatch.set(2, ToolClasses.SOFT_MALLET);
                    case 3 -> toolsToMatch.set(3, ToolClasses.HARD_HAMMER);
                    case 4 -> toolsToMatch.set(4, ToolClasses.WIRE_CUTTER);
                    case 5 -> toolsToMatch.set(5, ToolClasses.CROWBAR);
                }
            }
        }
        if (!proceed) {
            return;
        }

        mainfor:
        for (int i = 0; i < toolsToMatch.size(); i++) {
            String toolToMatch = toolsToMatch.get(i);
            if (toolToMatch != null) {
                // Try to use the item in the player's "hand" (under the cursor)
                ItemStack heldItem = entityPlayer.inventory.getItemStack();
                if (heldItem.getItem() instanceof ItemGTToolbelt toolbelt) {
                    if (toolbelt.damageAgainstMaintenanceProblem(heldItem, toolToMatch, entityPlayer)) {
                        ((IMaintenance) getController()).setMaintenanceFixed(i);
                        setTaped(false);
                        continue;
                    }
                } else if (ToolHelper.isTool(heldItem, toolToMatch)) {
                    fixProblemWithTool(i, heldItem, entityPlayer);

                    if (toolsToMatch.stream().allMatch(Objects::isNull)) {
                        return;
                    }
                    continue;
                }

                // Then try all the remaining inventory slots
                for (ItemStack itemStack : entityPlayer.inventory.mainInventory) {
                    if (itemStack.getItem() instanceof ItemGTToolbelt toolbelt) {
                        if (toolbelt.damageAgainstMaintenanceProblem(itemStack, toolToMatch, entityPlayer)) {
                            ((IMaintenance) getController()).setMaintenanceFixed(i);
                            setTaped(false);
                            continue mainfor;
                        }
                    } else if (ToolHelper.isTool(itemStack, toolToMatch)) {
                        fixProblemWithTool(i, itemStack, entityPlayer);

                        if (toolsToMatch.stream().allMatch(Objects::isNull)) {
                            return;
                        }
                        continue mainfor;
                    }
                }
            }
        }
    }

    @ApiStatus.Internal
    public void fixMaintenanceProblemsWithToolbelt(@NotNull EntityPlayer entityPlayer, ItemGTToolbelt toolbelt,
                                                   ItemStack toolbeltStack) {
        byte problems = ((IMaintenance) this.getController()).getMaintenanceProblems();
        for (byte index = 0; index < 6; index++) {
            if (((problems >> index) & 1) == 0) {
                String toolToMatch = switch (index) {
                    case 0 -> ToolClasses.WRENCH;
                    case 1 -> ToolClasses.SCREWDRIVER;
                    case 2 -> ToolClasses.SOFT_MALLET;
                    case 3 -> ToolClasses.HARD_HAMMER;
                    case 4 -> ToolClasses.WIRE_CUTTER;
                    case 5 -> ToolClasses.CROWBAR;
                    default -> null;
                };
                if (toolbelt.damageAgainstMaintenanceProblem(toolbeltStack, toolToMatch, entityPlayer)) {
                    ((IMaintenance) getController()).setMaintenanceFixed(index);
                    setTaped(false);
                }
            }
        }
    }

    private void fixProblemWithTool(int problemIndex, ItemStack stack, EntityPlayer player) {
        ((IMaintenance) getController()).setMaintenanceFixed(problemIndex);
        ToolHelper.damageItemWhenCrafting(stack, player);
        setTaped(false);
    }

    /**
     * Fixes every maintenance problem of the controller
     */
    public void fixAllMaintenanceProblems() {
        if (this.getController() instanceof IMaintenance)
            for (int i = 0; i < 6; i++) ((IMaintenance) this.getController()).setMaintenanceFixed(i);
    }

    @Override
    public boolean isFullAuto() {
        return false;
    }

    @Override
    public double getDurationMultiplier() {
        return durationMultiplier.doubleValue();
    }

    @Override
    public double getTimeMultiplier() {
        return BigDecimal.valueOf(TIME_ACTION.apply(durationMultiplier.doubleValue()))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private void incInternalMultiplier(Widget.ClickData data) {
        if (durationMultiplier.compareTo(MAX_DURATION_MULTIPLIER) == 0) return;
        durationMultiplier = durationMultiplier.add(DURATION_ACTION_AMOUNT);
        writeCustomData(MAINTENANCE_MULTIPLIER, b -> b.writeDouble(durationMultiplier.doubleValue()));
    }

    private void decInternalMultiplier(Widget.ClickData data) {
        if (durationMultiplier.compareTo(MIN_DURATION_MULTIPLIER) == 0) return;
        durationMultiplier = durationMultiplier.subtract(DURATION_ACTION_AMOUNT);
        writeCustomData(MAINTENANCE_MULTIPLIER, b -> b.writeDouble(durationMultiplier.doubleValue()));
    }

    @Override
    public void onRemoval() {
        if (getController() instanceof IMaintenance) {
            IMaintenance controller = (IMaintenance) getController();
            if (!getWorld().isRemote && controller != null)
                controller.storeTaped(isTaped);
        }
        super.onRemoval();
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (getController() instanceof IMaintenance && ((IMaintenance) getController()).hasMaintenanceProblems()) {
            if (consumeDuctTape(playerIn, playerIn.getHeldItem(hand))) {
                fixAllMaintenanceProblems();
                setTaped(true);
                return true;
            }
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 18 * 3 + 98)
                .label(5, 5, getMetaFullName())
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 * 3 + 16);

        if (!isConfigurable && GTValues.FOOLS.get()) {
            builder.widget(new FixWiringTaskWidget(48, 15, 80, 50)
                    .setOnFinished(this::fixAllMaintenanceProblems)
                    .setCanInteractPredicate(this::isAttachedToMultiBlock));
        } else {
            builder.widget(new SlotWidget(itemStackHandler, 0, 89 - 10, 18 - 1)
                    .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.DUCT_TAPE_OVERLAY)
                    .setTooltipText("gregtech.machine.maintenance_hatch_tape_slot.tooltip"))
                    .widget(new ClickButtonWidget(89 - 10 - 1, 18 * 2 + 3, 20, 20, "",
                            data -> fixMaintenanceProblems(entityPlayer))
                                    .setButtonTexture(GuiTextures.MAINTENANCE_ICON)
                                    .setTooltipText("gregtech.machine.maintenance_hatch_tool_slot.tooltip"));
        }
        if (isConfigurable) {
            builder.widget(
                    new AdvancedTextWidget(5, 25, getTextWidgetText("duration", this::getDurationMultiplier), 0x404040))
                    .widget(new AdvancedTextWidget(5, 39, getTextWidgetText("time", this::getTimeMultiplier), 0x404040))
                    .widget(new ClickButtonWidget(9, 18 * 3 + 16 - 18, 12, 12, "-", this::decInternalMultiplier))
                    .widget(new ClickButtonWidget(9 + 18 * 2, 18 * 3 + 16 - 18, 12, 12, "+",
                            this::incInternalMultiplier));
        }
        return builder.build(getHolder(), entityPlayer);
    }

    private static Consumer<List<ITextComponent>> getTextWidgetText(String type, Supplier<Double> multiplier) {
        return (list) -> {
            ITextComponent tooltip;
            if (multiplier.get() == 1.0) {
                tooltip = new TextComponentTranslation(
                        "gregtech.maintenance.configurable_" + type + ".unchanged_description");
            } else {
                tooltip = new TextComponentTranslation(
                        "gregtech.maintenance.configurable_" + type + ".changed_description", multiplier.get());
            }
            list.add(new TextComponentTranslation("gregtech.maintenance.configurable_" + type, multiplier.get())
                    .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
        };
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("IsTaped", isTaped);
        data.setTag("tapeInventory", itemStackHandler.serializeNBT());
        if (isConfigurable) {
            data.setDouble("DurationMultiplier", durationMultiplier.doubleValue());
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isTaped = data.getBoolean("IsTaped");
        if (data.hasKey("tapeInventory", Constants.NBT.TAG_COMPOUND)) {
            this.itemStackHandler.deserializeNBT(data.getCompoundTag("tapeInventory"));
        }
        if (isConfigurable) {
            durationMultiplier = BigDecimal.valueOf(data.getDouble("DurationMultiplier"));
        }
        // Legacy Inventory Handler Support
        if (data.hasKey("ImportInventory", Constants.NBT.TAG_COMPOUND)) {
            GTUtility.readItems(itemStackHandler, "ImportInventory", data);
            data.removeTag("ImportInventory");
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isTaped);
        if (isConfigurable) buf.writeDouble(durationMultiplier.doubleValue());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isTaped = buf.readBoolean();
        if (isConfigurable) durationMultiplier = BigDecimal.valueOf(buf.readDouble());
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == STORE_MAINTENANCE) {
            this.maintenanceProblems = buf.readByte();
            this.timeActive = buf.readInt();
            markDirty();
        } else if (dataId == IS_TAPED) {
            this.isTaped = buf.readBoolean();
            scheduleRenderUpdate();
            markDirty();
        } else if (dataId == MAINTENANCE_MULTIPLIER) {
            this.durationMultiplier = BigDecimal.valueOf(buf.readDouble());
            markDirty();
        }
    }

    @Override
    public MultiblockAbility<IMaintenanceHatch> getAbility() {
        return MultiblockAbility.MAINTENANCE_HATCH;
    }

    @Override
    public @NotNull List<? extends IMaintenanceHatch> registerAbilities(
                                                                        @NotNull MultiblockAbility<? extends IMaintenanceHatch> multiblockAbility) {
        return Collections.singletonList(this);
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        if (ConfigHolder.machines.enableMaintenance) {
            super.getSubItems(creativeTab, subItems);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.disabled"));
        if (isConfigurable) {
            tooltip.add(I18n.format("gregtech.maintenance.configurable.tooltip_basic"));
            if (!TooltipHelper.isShiftDown()) {
                tooltip.add(I18n.format("gregtech.maintenance.configurable.tooltip_more_info"));
            } else {
                tooltip.add(I18n.format("gregtech.maintenance.configurable.tooltip_pss_header"));
                tooltip.add(I18n.format("gregtech.maintenance.configurable.tooltip_pss_info"));
            }
        }
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.tool_action.tape"));
    }
}
