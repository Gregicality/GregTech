package gregtech.api.recipes.ui;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.RecipeProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;

import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.function.DoubleSupplier;

@ApiStatus.Experimental
public class RecipeMapUI<R extends RecipeMap<?>> {

    private final Byte2ObjectMap<TextureArea> slotOverlays = new Byte2ObjectOpenHashMap<>();

    private final R recipeMap;
    private final boolean modifyItemInputs;
    private final boolean modifyItemOutputs;
    private final boolean modifyFluidInputs;
    private final boolean modifyFluidOutputs;

    private final boolean isGenerator;

    private TextureArea progressBarTexture = GuiTextures.PROGRESS_BAR_ARROW;
    private ProgressWidget.MoveType moveType = ProgressWidget.MoveType.HORIZONTAL;
    private @Nullable TextureArea specialTexture;
    private int @Nullable [] specialTexturePosition;

    private boolean isJEIVisible = true;

    /**
     * @param recipeMap          the recipemap corresponding to this ui
     * @param modifyItemInputs   if item input amounts can be modified
     * @param modifyItemOutputs  if item output amounts can be modified
     * @param modifyFluidInputs  if fluid input amounts can be modified
     * @param modifyFluidOutputs if fluid output amounts can be modified
     */
    public RecipeMapUI(@NotNull R recipeMap, boolean modifyItemInputs, boolean modifyItemOutputs,
                       boolean modifyFluidInputs, boolean modifyFluidOutputs, boolean isGenerator) {
        this.recipeMap = recipeMap;
        this.modifyItemInputs = modifyItemInputs;
        this.modifyItemOutputs = modifyItemOutputs;
        this.modifyFluidInputs = modifyFluidInputs;
        this.modifyFluidOutputs = modifyFluidOutputs;
        this.isGenerator = isGenerator;
    }

    /**
     * Compute the storage key for slot overlays.
     *
     * @param isOutput if the slot is an output slot
     * @param isFluid  if the slot is a fluid slot
     * @param isLast   if the slot is the last slot of its type
     * @return the key
     */
    @ApiStatus.Internal
    public static byte computeOverlayKey(boolean isOutput, boolean isFluid, boolean isLast) {
        return (byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0));
    }

    /**
     * Determines the slot grid for an item input amount
     *
     * @param itemInputsCount the item input amount
     * @return [slots to the left, slots downwards]
     */
    @Contract("_ -> new")
    public static int @NotNull [] determineSlotsGrid(int itemInputsCount) {
        int itemSlotsToLeft;
        int itemSlotsToDown;
        double sqrt = Math.sqrt(itemInputsCount);
        // if the number of input has an integer root
        // return it.
        if (sqrt % 1 == 0) {
            itemSlotsToLeft = (int) sqrt;
            itemSlotsToDown = itemSlotsToLeft;
        } else if (itemInputsCount == 3) {
            itemSlotsToLeft = 3;
            itemSlotsToDown = 1;
        } else {
            // if we couldn't fit all into a perfect square,
            // increase the amount of slots to the left
            itemSlotsToLeft = (int) Math.ceil(sqrt);
            itemSlotsToDown = itemSlotsToLeft - 1;
            // if we still can't fit all the slots in a grid,
            // increase the amount of slots on the bottom
            if (itemInputsCount > itemSlotsToLeft * itemSlotsToDown) {
                itemSlotsToDown = itemSlotsToLeft;
            }
        }
        return new int[] { itemSlotsToLeft, itemSlotsToDown };
    }

    /**
     * Create a JEI UI Template
     *
     * @param importItems  the input item inventory
     * @param exportItems  the output item inventory
     * @param importFluids the input fluid inventory
     * @param exportFluids the output fluid inventory
     * @param yOffset      the y offset for the gui
     * @return the populated builder
     */
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems,
                                                 FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(200, 78, 23 + yOffset, 20, 20, progressBarTexture,
                moveType, recipeMap));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (specialTexture != null && specialTexturePosition != null) {
            addSpecialTexture(builder);
        }
        return builder;
    }

    /**
     * This DOES NOT include machine control widgets or binds player inventory
     *
     * @param progressSupplier a supplier for the progress bar
     * @param importItems      the input item inventory
     * @param exportItems      the output item inventory
     * @param importFluids     the input fluid inventory
     * @param exportFluids     the output fluid inventory
     * @param yOffset          the y offset for the gui
     * @return the populated builder
     */
    public ModularUI.Builder createUITemplate(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems,
                                              IItemHandlerModifiable exportItems, FluidTankList importFluids,
                                              FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(
                new RecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20, progressBarTexture,
                        moveType, recipeMap));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (specialTexture != null && specialTexturePosition != null) {
            addSpecialTexture(builder);
        }
        return builder;
    }

    /**
     * This DOES NOT include machine control widgets or binds player inventory
     *
     * @param progressSupplier a supplier for the progress bar
     * @param importItems      the input item inventory
     * @param exportItems      the output item inventory
     * @param importFluids     the input fluid inventory
     * @param exportFluids     the output fluid inventory
     * @param yOffset          the y offset for the gui
     * @return the populated builder
     */
    public ModularUI.Builder createUITemplateNoOutputs(DoubleSupplier progressSupplier,
                                                       IItemHandlerModifiable importItems,
                                                       IItemHandlerModifiable exportItems, FluidTankList importFluids,
                                                       FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(
                new RecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20, progressBarTexture,
                        moveType, recipeMap));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        if (specialTexture != null && specialTexturePosition != null) {
            addSpecialTexture(builder);
        }
        return builder;
    }

    /**
     * @param builder      the builder to add to
     * @param itemHandler  the item handler to use
     * @param fluidHandler the fluid handler to use
     * @param isOutputs    if slots should be output slots
     * @param yOffset      the y offset for the gui
     */
    protected void addInventorySlotGroup(@NotNull ModularUI.Builder builder,
                                         @NotNull IItemHandlerModifiable itemHandler,
                                         @NotNull FluidTankList fluidHandler, boolean isOutputs, int yOffset) {
        int itemInputsCount = itemHandler.getSlots();
        int fluidInputsCount = fluidHandler.size();
        boolean invertFluids = false;
        if (itemInputsCount == 0) {
            int tmp = itemInputsCount;
            itemInputsCount = fluidInputsCount;
            fluidInputsCount = tmp;
            invertFluids = true;
        }
        int[] inputSlotGrid = determineSlotsGrid(itemInputsCount);
        int itemSlotsToLeft = inputSlotGrid[0];
        int itemSlotsToDown = inputSlotGrid[1];
        int startInputsX = isOutputs ? 106 : 70 - itemSlotsToLeft * 18;
        int startInputsY = 33 - (int) (itemSlotsToDown / 2.0 * 18) + yOffset;
        boolean wasGroup = itemHandler.getSlots() + fluidHandler.size() == 12;
        if (wasGroup) startInputsY -= 9;
        else if (itemHandler.getSlots() >= 6 && fluidHandler.size() >= 2 && !isOutputs) startInputsY -= 9;
        for (int i = 0; i < itemSlotsToDown; i++) {
            for (int j = 0; j < itemSlotsToLeft; j++) {
                int slotIndex = i * itemSlotsToLeft + j;
                if (slotIndex >= itemInputsCount) break;
                int x = startInputsX + 18 * j;
                int y = startInputsY + 18 * i;
                addSlot(builder, x, y, slotIndex, itemHandler, fluidHandler, invertFluids, isOutputs);
            }
        }
        if (wasGroup) startInputsY += 2;
        if (fluidInputsCount > 0 || invertFluids) {
            if (itemSlotsToDown >= fluidInputsCount && itemSlotsToLeft < 3) {
                int startSpecX = isOutputs ? startInputsX + itemSlotsToLeft * 18 : startInputsX - 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int y = startInputsY + 18 * i;
                    addSlot(builder, startSpecX, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
                }
            } else {
                int startSpecY = startInputsY + itemSlotsToDown * 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int x = isOutputs ? startInputsX + 18 * (i % 3) :
                            startInputsX + itemSlotsToLeft * 18 - 18 - 18 * (i % 3);
                    int y = startSpecY + (i / 3) * 18;
                    addSlot(builder, x, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
                }
            }
        }
    }

    /**
     * Add a slot to this ui
     *
     * @param builder      the builder to add to
     * @param x            the x coordinate of the slot
     * @param y            the y coordinate of the slot
     * @param slotIndex    the slot index of the slot
     * @param itemHandler  the item handler to use
     * @param fluidHandler the fluid handler to use
     * @param isFluid      if the slot is a fluid slot
     * @param isOutputs    if slots should be output slots
     */
    protected void addSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler,
                           FluidTankList fluidHandler, boolean isFluid, boolean isOutputs) {
        if (!isFluid) {
            builder.widget(new SlotWidget(itemHandler, slotIndex, x, y, true, !isOutputs).setBackgroundTexture(
                    getOverlaysForSlot(isOutputs, false, slotIndex == itemHandler.getSlots() - 1)));
        } else {
            builder.widget(new TankWidget(fluidHandler.getTankAt(slotIndex), x, y, 18, 18).setAlwaysShowFull(true)
                    .setBackgroundTexture(getOverlaysForSlot(isOutputs, true, slotIndex == fluidHandler.size() - 1))
                    .setContainerClicking(true, !isOutputs));
        }
    }

    /**
     * @param isOutput if the slot is an output slot
     * @param isFluid  if the slot is a fluid slot
     * @param isLast   if the slot is the last slot of its type
     * @return the overlays for a slot
     */
    protected TextureArea[] getOverlaysForSlot(boolean isOutput, boolean isFluid, boolean isLast) {
        TextureArea base = isFluid ? GuiTextures.FLUID_SLOT : GuiTextures.SLOT;
        byte overlayKey = computeOverlayKey(isOutput, isFluid, isLast);
        if (slotOverlays.containsKey(overlayKey)) {
            return new TextureArea[] { base, slotOverlays.get(overlayKey) };
        }
        return new TextureArea[] { base };
    }

    /**
     * @return the height used to determine size of background texture in JEI
     */
    public int getPropertyHeightShift() {
        int maxPropertyCount = 0;
        if (shouldShiftWidgets()) {
            for (Recipe recipe : recipeMap.getRecipeList()) {
                int count = recipe.propertyStorage().size();
                if (count > maxPropertyCount) {
                    maxPropertyCount = count;
                }
            }
        }
        return maxPropertyCount * 10; // GTRecipeWrapper#LINE_HEIGHT
    }

    /**
     * @return widgets should be shifted
     */
    private boolean shouldShiftWidgets() {
        return recipeMap.getMaxInputs() + recipeMap.getMaxOutputs() >= 6 ||
                recipeMap.getMaxFluidInputs() + recipeMap.getMaxFluidOutputs() >= 6;
    }

    /**
     * @return the progress bar's move type
     */
    public @NotNull ProgressWidget.MoveType progressBarMoveType() {
        return moveType;
    }

    /**
     * @param moveType the new progress bar move type
     */
    public void setProgressBarMoveType(@NotNull ProgressWidget.MoveType moveType) {
        this.moveType = moveType;
    }

    /**
     * @return the texture of the progress bar
     */
    public @NotNull TextureArea progressBarTexture() {
        return progressBarTexture;
    }

    /**
     * @param progressBarTexture the new progress bar texture
     */
    public void setProgressBarTexture(@NotNull TextureArea progressBarTexture) {
        this.progressBarTexture = progressBarTexture;
    }

    /**
     * @param progressBarTexture the new progress bar texture
     * @param moveType           the new progress bar move type
     */
    public void setProgressBar(@NotNull TextureArea progressBarTexture, @NotNull ProgressWidget.MoveType moveType) {
        this.progressBarTexture = progressBarTexture;
        this.moveType = moveType;
    }

    /**
     * @param specialTexture the special texture to set
     * @param x              the x coordinate of the texture
     * @param y              the y coordinate of the texture
     * @param width          the width of the texture
     * @param height         the height of the texture
     */
    public void setSpecialTexture(@NotNull TextureArea specialTexture, int x, int y, int width, int height) {
        setSpecialTexture(specialTexture, new int[] { x, y, width, height });
    }

    /**
     * @param specialTexture the special texture to set
     * @param position       the position of the texture: [x, y, width, height]
     */
    public void setSpecialTexture(@NotNull TextureArea specialTexture, int @NotNull [] position) {
        this.specialTexture = specialTexture;
        this.specialTexturePosition = position;
    }

    /**
     * @return the special texture
     */
    public @Nullable TextureArea specialTexture() {
        return this.specialTexture;
    }

    /**
     * @return the special texture's position
     */
    public int @Nullable @UnmodifiableView [] specialTexturePosition() {
        return this.specialTexturePosition;
    }

    /**
     * Add a special texture to a builder
     *
     * @param builder the builder to add to
     * @return the updated builder
     */
    public @NotNull ModularUI.Builder addSpecialTexture(@NotNull ModularUI.Builder builder) {
        if (specialTexturePosition != null) {
            builder.image(specialTexturePosition[0], specialTexturePosition[1],
                    specialTexturePosition[2],
                    specialTexturePosition[3], specialTexture);
        }
        return builder;
    }

    /**
     * @return if this ui should be visible in JEI
     */
    public boolean isJEIVisible() {
        return isJEIVisible;
    }

    /**
     * @param isJEIVisible if the ui should be visible in JEI
     */
    public void setJEIVisible(boolean isJEIVisible) {
        this.isJEIVisible = isJEIVisible;
    }

    /**
     * @return if item input slot amounts can be modified
     */
    public boolean canModifyItemInputs() {
        return modifyItemInputs;
    }

    /**
     * @return if item output slot amounts can be modified
     */
    public boolean canModifyItemOutputs() {
        return modifyItemOutputs;
    }

    /**
     * @return if fluid input slot amounts can be modified
     */
    public boolean canModifyFluidInputs() {
        return modifyFluidInputs;
    }

    /**
     * @return if fluid output slot amounts can be modified
     */
    public boolean canModifyFluidOutputs() {
        return modifyFluidOutputs;
    }

    /**
     * @return if this UI represents an energy generating recipemap
     */
    public boolean isGenerator() {
        return isGenerator;
    }

    /**
     * @param texture  the texture to set
     * @param isOutput if the slot is an output slot
     */
    public void setItemSlotOverlay(@NotNull TextureArea texture, boolean isOutput) {
        this.slotOverlays.put(computeOverlayKey(isOutput, false, false), texture);
        this.slotOverlays.put(computeOverlayKey(isOutput, false, true), texture);
    }

    /**
     * @param texture    the texture to set
     * @param isOutput   if the slot is an output slot
     * @param isLastSlot if the slot is the last slot
     */
    public void setItemSlotOverlay(@NotNull TextureArea texture, boolean isOutput, boolean isLastSlot) {
        this.slotOverlays.put(computeOverlayKey(isOutput, false, isLastSlot), texture);
    }

    /**
     * @param texture  the texture to set
     * @param isOutput if the slot is an output slot
     */
    public void setFluidSlotOverlay(@NotNull TextureArea texture, boolean isOutput) {
        this.slotOverlays.put(computeOverlayKey(isOutput, true, false), texture);
        this.slotOverlays.put(computeOverlayKey(isOutput, true, true), texture);
    }

    /**
     * @param texture    the texture to set
     * @param isOutput   if the slot is an output slot
     * @param isLastSlot if the slot is the last slot
     */
    public void setFluidSlotOverlay(@NotNull TextureArea texture, boolean isOutput, boolean isLastSlot) {
        this.slotOverlays.put(computeOverlayKey(isOutput, true, isLastSlot), texture);
    }

    /**
     * @param key     the key to store the slot's texture with
     * @param texture the texture to store
     */
    @ApiStatus.Internal
    public void setSlotOverlay(byte key, @NotNull TextureArea texture) {
        this.slotOverlays.put(key, texture);
    }

    /**
     * @return the UI's recipemap
     */
    public @NotNull R recipeMap() {
        return recipeMap;
    }
}
