package gregtech.integration.jei.multiblock;

import gregtech.api.gui.GuiTextures;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTUtility;
import gregtech.api.util.GregFakePlayer;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.client.renderer.scene.ImmediateWorldSceneRenderer;
import gregtech.client.renderer.scene.WorldSceneRenderer;
import gregtech.client.utils.RenderUtil;
import gregtech.client.utils.TrackedDummyWorld;
import gregtech.common.ConfigHolder;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Translation;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.vecmath.Vector3f;

public class MultiblockInfoRecipeWrapper implements IRecipeWrapper {

    private static final int MAX_PARTS = 18;
    private static final int PARTS_HEIGHT = 36;
    private static final int SLOT_SIZE = 18;
    private static final int SLOTS_PER_ROW = 9;
    private static final int ICON_SIZE = 20;
    private static final int RIGHT_PADDING = 5;

    private static class MBPattern {

        final WorldSceneRenderer sceneRenderer;
        final List<ItemStack> parts;
        final Map<BlockPos, TraceabilityPredicate> predicateMap;

        public MBPattern(final WorldSceneRenderer sceneRenderer, final List<ItemStack> parts,
                         Map<BlockPos, TraceabilityPredicate> predicateMap) {
            this.sceneRenderer = sceneRenderer;
            this.parts = parts;
            this.predicateMap = predicateMap;
        }
    }

    private final MultiblockControllerBase controller;
    private MBPattern[] patterns;
    private final Map<GuiButton, Runnable> buttons = new HashMap<>();
    private RecipeLayout recipeLayout;
    private final List<ItemStack> allItemStackInputs = new ArrayList<>();

    private int layerIndex = -1;
    private int tierIndex = 0;
    private int currentRendererPage = 0;
    private int lastMouseX;
    private int lastMouseY;
    private Vector3f center;
    private float rotationYaw;
    private float rotationPitch;
    private float zoom;

    private final GuiButton buttonPreviousPattern;
    private final GuiButton buttonNextPattern;
    private final GuiButton nextLayerButton;
    private final GuiButton nextTierButton;

    private IDrawable slot;
    private IDrawable infoIcon;
    private boolean drawInfoIcon;
    private static ItemStack tooltipBlockStack;
    private static long lastRender;
    private List<String> predicateTips;

    private BlockPos selected;
    private final List<TraceabilityPredicate.SimplePredicate> predicates;
    private TraceabilityPredicate father;

    @SuppressWarnings("NewExpressionSideOnly")
    public MultiblockInfoRecipeWrapper(@NotNull MultiblockControllerBase controller) {
        this.controller = controller;
        Set<ItemStack> drops = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingAllButCount());
        this.patterns = controller.getMatchingShapes().stream()
                .map(it -> initializePattern(it, drops))
                .toArray(MBPattern[]::new);
        allItemStackInputs.addAll(drops);
        this.nextLayerButton = new GuiButton(0, 176 - (ICON_SIZE + RIGHT_PADDING), 70, ICON_SIZE, ICON_SIZE, "");
        this.buttonPreviousPattern = new GuiButton(0, 176 - ((2 * ICON_SIZE) + RIGHT_PADDING + 1), 90, ICON_SIZE,
                ICON_SIZE, "<");
        this.buttonNextPattern = new GuiButton(0, 176 - (ICON_SIZE + RIGHT_PADDING), 90, ICON_SIZE, ICON_SIZE, ">");
        this.nextTierButton = new GuiButton(0, 176 - (ICON_SIZE + RIGHT_PADDING), 110, ICON_SIZE, ICON_SIZE, "^");
        this.buttons.put(nextLayerButton, this::toggleNextLayer);
        this.buttons.put(buttonPreviousPattern, () -> switchRenderPage(-1));
        this.buttons.put(buttonNextPattern, () -> switchRenderPage(1));
        this.buttons.put(nextTierButton, this::toggleNextTier);
        boolean isPagesDisabled = patterns.length == 1;
        this.buttonPreviousPattern.visible = !isPagesDisabled;
        this.buttonNextPattern.visible = !isPagesDisabled;
        boolean isTieringEnabled = controller.getMaxStructureTier() > 0;
        this.nextTierButton.visible = isTieringEnabled;
        this.predicates = new ArrayList<>();
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM, allItemStackInputs);
        ingredients.setOutput(VanillaTypes.ITEM, controller.getStackForm());
    }

    private static MultiblockInfoRecipeWrapper lastWrapper;

    public void setRecipeLayout(RecipeLayout layout, IGuiHelper guiHelper) {
        this.recipeLayout = layout;

        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, SLOT_SIZE, SLOT_SIZE)
                .setTextureSize(SLOT_SIZE, SLOT_SIZE).build();
        this.infoIcon = guiHelper.drawableBuilder(GuiTextures.INFO_ICON.imageLocation, 0, 0, ICON_SIZE, ICON_SIZE)
                .setTextureSize(ICON_SIZE, ICON_SIZE).build();

        IDrawable border = layout.getRecipeCategory().getBackground();
        preparePlaceForParts(border.getHeight());
        if (Mouse.getEventDWheel() == 0 || lastWrapper != this) {
            selected = null;
            this.predicates.clear();
            this.father = null;
            lastWrapper = this;
            this.nextLayerButton.x = border.getWidth() - (ICON_SIZE + RIGHT_PADDING);
            this.buttonPreviousPattern.x = border.getWidth() - ((2 * ICON_SIZE) + RIGHT_PADDING + 1);
            this.buttonNextPattern.x = border.getWidth() - (ICON_SIZE + RIGHT_PADDING);
            this.buttonPreviousPattern.enabled = false;
            this.buttonNextPattern.enabled = patterns.length > 1;
            Vector3f size = ((TrackedDummyWorld) getCurrentRenderer().world).getSize();
            float max = Math.max(Math.max(Math.max(size.x, size.y), size.z), 1);
            this.zoom = (float) (3.5 * Math.sqrt(max));
            this.rotationYaw = 20.0f;
            this.rotationPitch = 50f;
            this.currentRendererPage = 0;
            setNextLayer(-1);
        } else {
            zoom = (float) MathHelper.clamp(zoom + (Mouse.getEventDWheel() < 0 ? 0.5 : -0.5), 3, 999);
            setNextLayer(getLayerIndex());
            if (predicates != null && predicates.size() > 0) {
                setItemStackGroup();
            }
        }
        if (getCurrentRenderer() != null) {
            TrackedDummyWorld world = (TrackedDummyWorld) getCurrentRenderer().world;
            resetCenter(world);
        }
        updateParts();
    }

    public WorldSceneRenderer getCurrentRenderer() {
        return patterns[currentRendererPage].sceneRenderer;
    }

    public int getLayerIndex() {
        return layerIndex;
    }

    private void toggleNextLayer() {
        WorldSceneRenderer renderer = getCurrentRenderer();
        int height = (int) ((TrackedDummyWorld) renderer.world).getSize().getY() - 1;
        if (++this.layerIndex > height) {
            // if current layer index is more than max height, reset it
            // to display all layers
            this.layerIndex = -1;
        }
        setNextLayer(layerIndex);
    }

    private void toggleNextTier() {
        if (++this.tierIndex > controller.getMaxStructureTier()) {
            this.tierIndex = 0;
        }
        this.controller.setStructureTier(tierIndex);
        controller.reinitializeStructurePattern();
        Set<ItemStack> drops = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingAllButCount());
        this.patterns = controller.getMatchingShapes().stream()
                .map(it -> initializePattern(it, drops))
                .toArray(MBPattern[]::new);
    }

    private void setNextLayer(int newLayer) {
        this.layerIndex = newLayer;
        this.nextLayerButton.displayString = "L:" + (layerIndex == -1 ? "A" : Integer.toString(layerIndex + 1));
        WorldSceneRenderer renderer = getCurrentRenderer();
        if (renderer != null) {
            TrackedDummyWorld world = ((TrackedDummyWorld) renderer.world);
            resetCenter(world);
            renderer.renderedBlocks.clear();
            int minY = (int) world.getMinPos().getY();
            Collection<BlockPos> renderBlocks;
            if (newLayer == -1) {
                renderBlocks = world.renderedBlocks;
            } else {
                renderBlocks = world.renderedBlocks.stream().filter(pos -> pos.getY() - minY == newLayer)
                        .collect(Collectors.toSet());
            }
            renderer.addRenderedBlocks(renderBlocks);
        }
    }

    private void resetCenter(TrackedDummyWorld world) {
        Vector3f size = world.getSize();
        Vector3f minPos = world.getMinPos();
        center = new Vector3f(minPos.x + size.x / 2, minPos.y + size.y / 2, minPos.z + size.z / 2);
        getCurrentRenderer().setCameraLookAt(center, zoom, Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
    }

    private void switchRenderPage(int amount) {
        int maxIndex = patterns.length - 1;
        int newIndex = Math.max(0, Math.min(currentRendererPage + amount, maxIndex));
        if (currentRendererPage != newIndex) {
            this.currentRendererPage = newIndex;
            this.buttonNextPattern.enabled = newIndex < maxIndex;
            this.buttonPreviousPattern.enabled = newIndex > 0;
            setNextLayer(-1);
            updateParts();
            getCurrentRenderer().setCameraLookAt(center, zoom, Math.toRadians(rotationPitch),
                    Math.toRadians(rotationYaw));
            if (this.selected != null) {
                this.selected = null;
                for (int i = 0; i < predicates.size(); i++) {
                    recipeLayout.getItemStacks().set(i + MAX_PARTS, ItemStack.EMPTY);
                }
                predicates.clear();
                this.father = null;
            }
        }
    }

    private void preparePlaceForParts(int recipeHeight) {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        for (int i = 0; i < MAX_PARTS; ++i)
            itemStackGroup.init(i, true,
                    SLOT_SIZE * i - (SLOT_SIZE * SLOTS_PER_ROW) * (i / SLOTS_PER_ROW) + (SLOT_SIZE / 2) - 2,
                    recipeHeight - PARTS_HEIGHT + SLOT_SIZE * (i / SLOTS_PER_ROW));
    }

    private void updateParts() {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        List<ItemStack> parts = this.patterns[currentRendererPage].parts;
        int limit = Math.min(parts.size(), MAX_PARTS);
        for (int i = 0; i < limit; ++i) {
            itemStackGroup.set(i, parts.get(i));
        }
        for (int i = parts.size(); i < MAX_PARTS; ++i) {
            itemStackGroup.set(i, (ItemStack) null);
        }
    }

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        WorldSceneRenderer renderer = getCurrentRenderer();
        int sceneHeight = recipeHeight - PARTS_HEIGHT;

        renderer.render(recipeLayout.getPosX(), recipeLayout.getPosY(), recipeWidth, sceneHeight,
                mouseX + recipeLayout.getPosX(), mouseY + recipeLayout.getPosY());
        drawMultiblockName(recipeWidth);

        // reset colors (so any elements render after this point are not dark)
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int iconX = recipeWidth - (ICON_SIZE + RIGHT_PADDING);
        int iconY = 49;
        this.infoIcon.draw(minecraft, iconX, iconY);

        this.drawInfoIcon = iconX <= mouseX && mouseX <= iconX + ICON_SIZE && iconY <= mouseY &&
                mouseY <= iconY + ICON_SIZE;

        // draw parts slots
        for (int i = 0; i < MAX_PARTS; ++i) {
            this.slot.draw(minecraft,
                    SLOT_SIZE * i - (SLOTS_PER_ROW * SLOT_SIZE) * (i / SLOTS_PER_ROW) + (SLOT_SIZE / 2) - 2,
                    sceneHeight + SLOT_SIZE * (i / SLOTS_PER_ROW));
        }

        // draw candidates slots
        for (int i = 0; i < predicates.size(); i++) {
            this.slot.draw(minecraft, 5 + (i / 6) * SLOT_SIZE, (i % 6) * SLOT_SIZE + 10);
        }

        // draw buttons
        for (GuiButton button : buttons.keySet()) {
            button.drawButton(minecraft, mouseX, mouseY, 0.0f);
        }

        tooltipBlockStack = null;
        this.predicateTips = null;
        RayTraceResult rayTraceResult = renderer.getLastTraceResult();
        boolean insideView = mouseX >= 0 && mouseY >= 0 &&
                mouseX < recipeWidth && mouseY < sceneHeight;
        boolean leftClickHeld = Mouse.isButtonDown(0);
        boolean rightClickHeld = Mouse.isButtonDown(1);
        if (insideView) {
            for (GuiButton button : buttons.keySet()) {
                if (button.isMouseOver()) {
                    insideView = false;
                    break;
                }
            }
        }
        if (insideView) {
            if (leftClickHeld) {
                rotationPitch += mouseX - lastMouseX + 360;
                rotationPitch = rotationPitch % 360;
                rotationYaw = (float) MathHelper.clamp(rotationYaw + (mouseY - lastMouseY), -89.9, 89.9);
            } else if (rightClickHeld) {
                int mouseDeltaY = mouseY - lastMouseY;
                if (Math.abs(mouseDeltaY) > 1) {
                    this.zoom = (float) MathHelper.clamp(zoom + (mouseDeltaY > 0 ? 0.5 : -0.5), 3, 999);
                }
            }
            renderer.setCameraLookAt(center, zoom, Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
        }

        if (!(leftClickHeld || rightClickHeld) && rayTraceResult != null &&
                !renderer.world.isAirBlock(rayTraceResult.getBlockPos())) {
            IBlockState blockState = renderer.world.getBlockState(rayTraceResult.getBlockPos());
            ItemStack itemStack = blockState.getBlock().getPickBlock(blockState, rayTraceResult, renderer.world,
                    rayTraceResult.getBlockPos(), minecraft.player);
            TraceabilityPredicate predicates = patterns[currentRendererPage].predicateMap
                    .get(rayTraceResult.getBlockPos());
            if (predicates != null) {
                BlockWorldState worldState = new BlockWorldState();
                worldState.update(renderer.world, rayTraceResult.getBlockPos(), new PatternMatchContext(),
                        new HashMap<>(), new HashMap<>(), predicates);
                for (TraceabilityPredicate.SimplePredicate common : predicates.common) {
                    if (common.test(worldState)) {
                        predicateTips = common.getToolTips(predicates);
                        break;
                    }
                }
                if (predicateTips == null) {
                    for (TraceabilityPredicate.SimplePredicate limit : predicates.limited) {
                        if (limit.test(worldState)) {
                            predicateTips = limit.getToolTips(predicates);
                            break;
                        }
                    }
                }
            }
            if (!itemStack.isEmpty()) {
                tooltipBlockStack = itemStack;
            }
        }

        lastRender = System.currentTimeMillis();
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        RenderHelper.disableStandardItemLighting();
    }

    private void drawMultiblockName(int recipeWidth) {
        String localizedName = I18n.format(controller.getMetaFullName());
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        List<String> lines = fontRenderer.listFormattedStringToWidth(localizedName, recipeWidth - 10);
        for (int i = 0; i < lines.size(); i++) {
            fontRenderer.drawString(lines.get(i), (recipeWidth - fontRenderer.getStringWidth(lines.get(i))) / 2,
                    fontRenderer.FONT_HEIGHT * i, ConfigHolder.client.multiblockPreviewFontColor);
        }
    }

    @Override
    public boolean handleClick(@NotNull Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        for (Entry<GuiButton, Runnable> button : buttons.entrySet()) {
            if (button.getKey().mousePressed(minecraft, mouseX, mouseY)) {
                button.getValue().run();
                selected = null;
                return true;
            }
        }
        if (mouseButton == 1) {
            if (getCurrentRenderer().getLastTraceResult() == null) {
                if (this.selected != null) {
                    this.selected = null;
                    for (int i = 0; i < predicates.size(); i++) {
                        recipeLayout.getItemStacks().set(i + MAX_PARTS, ItemStack.EMPTY);
                    }
                    predicates.clear();
                    this.father = null;
                    return true;
                }
                return false;
            }
            BlockPos selected = getCurrentRenderer().getLastTraceResult().getBlockPos();
            if (!Objects.equals(this.selected, selected)) {
                for (int i = 0; i < predicates.size(); i++) {
                    recipeLayout.getItemStacks().set(i + MAX_PARTS, ItemStack.EMPTY);
                }
                predicates.clear();
                this.father = null;
                this.selected = selected;
                TraceabilityPredicate predicate = patterns[currentRendererPage].predicateMap.get(this.selected);
                if (predicate != null) {
                    predicates.addAll(predicate.common);
                    predicates.addAll(predicate.limited);
                    predicates.removeIf(p -> p.candidates == null);
                    this.father = predicate;
                    setItemStackGroup();
                }
                return true;
            }
        }
        return false;
    }

    private void setItemStackGroup() {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        for (int i = 0; i < predicates.size(); i++) {
            itemStackGroup.init(i + MAX_PARTS, true, 5 + (i / 6) * SLOT_SIZE, (i % 6) * SLOT_SIZE + 10);
            itemStackGroup.set(i + MAX_PARTS, predicates.get(i).getCandidates());
        }

        itemStackGroup.addTooltipCallback((slotIndex, input, itemStack, tooltip) -> {
            if (slotIndex >= MAX_PARTS && slotIndex < MAX_PARTS + predicates.size()) {
                tooltip.addAll(predicates.get(slotIndex - MAX_PARTS).getToolTips(father));
            }
        });
    }

    @NotNull
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (drawInfoIcon) {
            return Arrays.asList(I18n.format("gregtech.multiblock.preview.zoom"),
                    I18n.format("gregtech.multiblock.preview.rotate"),
                    I18n.format("gregtech.multiblock.preview.select"));
        } else if (tooltipBlockStack != null && !tooltipBlockStack.isEmpty() && !Mouse.isButtonDown(0)) {
            Minecraft minecraft = Minecraft.getMinecraft();
            ITooltipFlag flag = minecraft.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED :
                    TooltipFlags.NORMAL;
            List<String> tooltip = tooltipBlockStack.getTooltip(minecraft.player, flag);
            EnumRarity rarity = tooltipBlockStack.getRarity();
            for (int k = 0; k < tooltip.size(); ++k) {
                if (k == 0) {
                    tooltip.set(k, rarity.color + tooltip.get(k));
                } else {
                    tooltip.set(k, TextFormatting.GRAY + tooltip.get(k));
                }
            }
            if (predicateTips != null) {
                tooltip.addAll(predicateTips);
            }
            return tooltip;
        }
        return Collections.emptyList();
    }

    private static class PartInfo {

        final ItemStack itemStack;
        boolean isController = false;
        boolean isTile = false;
        final int blockId;
        int amount = 0;

        PartInfo(final ItemStack itemStack, final BlockInfo blockInfo) {
            this.itemStack = itemStack;
            this.blockId = Block.getIdFromBlock(blockInfo.getBlockState().getBlock());
            TileEntity tileEntity = blockInfo.getTileEntity();
            if (tileEntity != null) {
                this.isTile = true;
                if (tileEntity instanceof IGregTechTileEntity iGregTechTileEntity) {
                    MetaTileEntity mte = iGregTechTileEntity.getMetaTileEntity();
                    this.isController = mte instanceof MultiblockControllerBase;
                }
            }
        }

        @NotNull
        ItemStack getItemStack() {
            ItemStack result = this.itemStack.copy();
            result.setCount(this.amount);
            return result;
        }
    }

    @NotNull
    private static Collection<PartInfo> gatherStructureBlocks(World world, @NotNull Map<BlockPos, BlockInfo> blocks,
                                                              Set<ItemStack> parts) {
        Map<ItemStack, PartInfo> partsMap = new Object2ObjectOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        for (Entry<BlockPos, BlockInfo> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            ItemStack stack = ItemStack.EMPTY;

            // first check if the block is a GT machine
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof IGregTechTileEntity) {
                stack = ((IGregTechTileEntity) tileEntity).getMetaTileEntity().getStackForm();
            }
            if (stack.isEmpty()) {
                // first, see what the block has to say for itself before forcing it to use a particular meta value
                stack = block.getPickBlock(state, new RayTraceResult(Vec3d.ZERO, EnumFacing.UP, pos), world, pos,
                        new GregFakePlayer(world));
            }
            if (stack.isEmpty()) {
                // try the default itemstack constructor if we're not a GT machine
                stack = GTUtility.toItem(state);
            }
            if (stack.isEmpty()) {
                // add the first of the block's drops if the others didn't work
                NonNullList<ItemStack> list = NonNullList.create();
                state.getBlock().getDrops(list, world, pos, state, 0);
                if (!list.isEmpty()) {
                    ItemStack is = list.get(0);
                    if (!is.isEmpty()) {
                        stack = is;
                    }
                }
            }

            // if we got a stack, add it to the set and map
            if (!stack.isEmpty()) {
                parts.add(stack);

                PartInfo partInfo = partsMap.get(stack);
                if (partInfo == null) {
                    partInfo = new PartInfo(stack, entry.getValue());
                    partsMap.put(stack, partInfo);
                }
                partInfo.amount++;
            }
        }
        return partsMap.values();
    }

    @SuppressWarnings("NewExpressionSideOnly")
    @NotNull
    private MBPattern initializePattern(@NotNull MultiblockShapeInfo shapeInfo, @NotNull Set<ItemStack> parts) {
        Map<BlockPos, BlockInfo> blockMap = new HashMap<>();
        MultiblockControllerBase controllerBase = null;
        BlockInfo[][][] blocks = shapeInfo.getBlocks();
        for (int x = 0; x < blocks.length; x++) {
            BlockInfo[][] aisle = blocks[x];
            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];
                for (int z = 0; z < column.length; z++) {
                    if (column[z].getTileEntity() instanceof IGregTechTileEntity &&
                            ((IGregTechTileEntity) column[z].getTileEntity())
                                    .getMetaTileEntity() instanceof MultiblockControllerBase) {
                        controllerBase = (MultiblockControllerBase) ((IGregTechTileEntity) column[z].getTileEntity())
                                .getMetaTileEntity();
                    }
                    blockMap.put(new BlockPos(x, y, z), column[z]);
                }
            }
        }

        TrackedDummyWorld world = new TrackedDummyWorld();
        ImmediateWorldSceneRenderer worldSceneRenderer = new ImmediateWorldSceneRenderer(world);
        worldSceneRenderer.setClearColor(ConfigHolder.client.multiblockPreviewColor);
        world.addBlocks(blockMap);

        Vector3f size = world.getSize();
        Vector3f minPos = world.getMinPos();
        center = new Vector3f(minPos.x + size.x / 2, minPos.y + size.y / 2, minPos.z + size.z / 2);

        worldSceneRenderer.addRenderedBlocks(world.renderedBlocks);
        worldSceneRenderer.setOnLookingAt(ray -> {});

        worldSceneRenderer.setAfterWorldRender(renderer -> {
            BlockPos look = worldSceneRenderer.getLastTraceResult() == null ? null :
                    worldSceneRenderer.getLastTraceResult().getBlockPos();
            if (look != null && look.equals(selected)) {
                renderBlockOverLay(selected, 200, 75, 75);
                return;
            }
            renderBlockOverLay(look, 150, 150, 150);
            renderBlockOverLay(selected, 255, 0, 0);
        });
        world.updateEntities();
        world.setRenderFilter(worldSceneRenderer.renderedBlocks::contains);

        Map<BlockPos, TraceabilityPredicate> predicateMap = new HashMap<>();
        if (controllerBase != null) {
            if (controllerBase.structurePattern == null) {
                controllerBase.reinitializeStructurePattern();
            }
            if (controllerBase.structurePattern != null) {
                controllerBase.structurePattern.cache.forEach((pos, blockInfo) -> predicateMap
                        .put(BlockPos.fromLong(pos), (TraceabilityPredicate) blockInfo.getInfo()));
            }
        }

        List<ItemStack> sortedParts = gatherStructureBlocks(worldSceneRenderer.world, blockMap, parts).stream()
                .sorted((one, two) -> {
                    if (one.isController) return -1;
                    if (two.isController) return +1;
                    if (one.isTile && !two.isTile) return -1;
                    if (two.isTile && !one.isTile) return +1;
                    if (one.blockId != two.blockId) return two.blockId - one.blockId;
                    return two.amount - one.amount;
                }).map(PartInfo::getItemStack).collect(Collectors.toList());

        return new MBPattern(worldSceneRenderer, sortedParts, predicateMap);
    }

    @SideOnly(Side.CLIENT)
    private static void renderBlockOverLay(BlockPos pos, int r, int g, int b) {
        if (pos == null) return;
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.translate((pos.getX() + 0.5), (pos.getY() + 0.5), (pos.getZ() + 0.5));
        GlStateManager.scale(1.01, 1.01, 1.01);

        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager.disableTexture2D();
        CCRenderState renderState = CCRenderState.instance();
        renderState.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR, tessellator.getBuffer());
        ColourMultiplier multiplier = new ColourMultiplier(0);
        renderState.setPipeline(new Translation(-0.5, -0.5, -0.5), multiplier);
        BlockRenderer.BlockFace blockFace = new BlockRenderer.BlockFace();
        renderState.setModel(blockFace);
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            multiplier.colour = RenderUtil.packColor(r, g, b, 255);
            blockFace.loadCuboidFace(Cuboid6.full, renderSide.getIndex());
            renderState.render();
        }
        renderState.draw();
        GlStateManager.scale(1 / 1.01, 1 / 1.01, 1 / 1.01);
        GlStateManager.translate(-(pos.getX() + 0.5), -(pos.getY() + 0.5), -(pos.getZ() + 0.5));
        GlStateManager.enableTexture2D();

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1, 1, 1, 1);
    }

    public static ItemStack getHoveredItemStack() {
        if (lastRender > System.currentTimeMillis() - 100) {
            return tooltipBlockStack;
        }
        return null;
    }
}
