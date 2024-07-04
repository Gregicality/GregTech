package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.util.AssemblyLineManager;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.api.util.LocalizationUtils;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDataBank;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MetaTileEntityDataAccessHatch extends MetaTileEntityMultiblockNotifiablePart
                                           implements IMultiblockAbilityPart<IDataAccessHatch>, IDataAccessHatch,
                                           IDataInfoProvider {

    private final Set<Recipe> recipes;
    private final boolean isCreative;

    public MetaTileEntityDataAccessHatch(ResourceLocation metaTileEntityId, int tier, boolean isCreative) {
        super(metaTileEntityId, tier, false);
        this.isCreative = isCreative;
        this.recipes = isCreative ? Collections.emptySet() : new ObjectOpenHashSet<>();
        rebuildData(getController() instanceof MetaTileEntityDataBank);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDataAccessHatch(metaTileEntityId, getTier(), isCreative());
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        if (isCreative) return super.createImportItemHandler();
        return new NotifiableItemStackHandler(this, getInventorySize(), getController(), false) {

            @Override
            public void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                rebuildData(getController() instanceof MetaTileEntityDataBank);
            }

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                var controller = MetaTileEntityDataAccessHatch.this.getController();
                boolean isDataBank = controller instanceof MetaTileEntityDataBank;
                if (AssemblyLineManager.isStackDataItem(stack, isDataBank) &&
                        AssemblyLineManager.hasResearchTag(stack)) {
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }
        };
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            if (isCreative) {
                Textures.CREATIVE_DATA_ACCESS_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
            } else {
                Textures.DATA_ACCESS_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        if (isCreative) return null;
        int rowSize = (int) Math.sqrt(getInventorySize());
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 18 + 18 * rowSize + 94)
                .label(6, 6, getMetaFullName());

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(isExportHatch ? exportItems : importItems, index,
                        88 - rowSize * 9 + x * 18, 18 + y * 18, true, !isExportHatch)
                                .setBackgroundTexture(GuiTextures.SLOT));
            }
        }
        return builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * rowSize + 12)
                .build(getHolder(), entityPlayer);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return !this.isCreative;
    }

    protected int getInventorySize() {
        return getTier() == GTValues.LuV ? 16 : 9;
    }

    private void rebuildData(boolean isDataBank) {
        if (isCreative || getWorld() == null || getWorld().isRemote) return;
        recipes.clear();
        for (int i = 0; i < this.importItems.getSlots(); i++) {
            ItemStack stack = this.importItems.getStackInSlot(i);
            String researchId = AssemblyLineManager.readResearchId(stack);
            boolean isValid = AssemblyLineManager.isStackDataItem(stack, isDataBank);
            if (researchId != null && isValid) {
                Collection<Recipe> collection = ((IResearchRecipeMap) RecipeMaps.ASSEMBLY_LINE_RECIPES)
                        .getDataStickEntry(researchId);
                if (collection != null) {
                    recipes.addAll(collection);
                }
            }
        }
    }

    @Override
    public boolean isRecipeAvailable(@NotNull Recipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
        seen.add(this);
        return recipes.contains(recipe);
    }

    @Override
    public boolean isCreative() {
        return this.isCreative;
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        if (ConfigHolder.machines.enableResearch) {
            super.getSubItems(creativeTab, subItems);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.data_access_hatch.tooltip.1"));
        if (isCreative) {
            tooltip.add(I18n.format("gregtech.creative_tooltip.1") + TooltipHelper.RAINBOW +
                    I18n.format("gregtech.creative_tooltip.2") + I18n.format("gregtech.creative_tooltip.3"));
        } else {
            tooltip.add(I18n.format("gregtech.machine.data_access_hatch.tooltip.2", getInventorySize()));
        }
        if (canPartShare()) {
            tooltip.add(I18n.format("gregtech.universal.enabled"));
        } else {
            tooltip.add(I18n.format("gregtech.universal.disabled"));
        }
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        if (recipes.isEmpty()) return Collections.emptyList();
        List<ITextComponent> list = new ArrayList<>();

        list.add(new TextComponentTranslation("behavior.data_item.assemblyline.title"));
        list.add(new TextComponentString(""));
        Collection<ItemStack> itemsAdded = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingAll());
        for (Recipe recipe : recipes) {
            ItemStack stack = recipe.getOutputs().get(0);
            if (!itemsAdded.contains(stack)) {
                itemsAdded.add(stack);
                list.add(new TextComponentTranslation("behavior.data_item.assemblyline.data",
                        LocalizationUtils.format(stack.getTranslationKey())));
            }
        }
        return list;
    }

    @Override
    public boolean canPartShare() {
        return isCreative;
    }

    @Override
    public MultiblockAbility<IDataAccessHatch> getAbility() {
        return MultiblockAbility.DATA_ACCESS_HATCH;
    }

    @Override
    public @NotNull List<? extends IDataAccessHatch> registerAbilities(
            @NotNull MultiblockAbility<? extends IDataAccessHatch> multiblockAbility) {
        return Collections.singletonList(this);
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        rebuildData(controllerBase instanceof MetaTileEntityDataBank);
        super.addToMultiBlock(controllerBase);
    }
}
