package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IMultipleRecipeMaps;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.network.AdvancedPacketBuffer;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.LocalizationUtils;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public abstract class MultiMapMultiblockController extends RecipeMapMultiblockController
                                                   implements IMultipleRecipeMaps {

    // array of possible recipes, specific to each multi - used when the multi has multiple RecipeMaps
    private final RecipeMap<?>[] recipeMaps;

    // index of the current selected recipe - used when the multi has multiple RecipeMaps
    private int recipeMapIndex = 0;

    public MultiMapMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?>[] recipeMaps) {
        super(metaTileEntityId, recipeMaps[0]);
        this.recipeMaps = recipeMaps;
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (recipeMaps.length == 1) return true;
        if (!getWorld().isRemote) {
            if (!this.recipeMapWorkable.isActive()) {
                int index;
                RecipeMap<?>[] recipeMaps = getAvailableRecipeMaps();
                if (playerIn.isSneaking()) // cycle recipemaps backwards
                    index = (recipeMapIndex - 1 < 0 ? recipeMaps.length - 1 : recipeMapIndex - 1) % recipeMaps.length;
                else // cycle recipemaps forwards
                    index = (recipeMapIndex + 1) % recipeMaps.length;

                setRecipeMapIndex(index);
            } else {
                playerIn.sendStatusMessage(
                        new TextComponentTranslation("gregtech.multiblock.multiple_recipemaps.switch_message"), true);
            }
        }

        return true; // return true here on the client to keep the GUI closed
    }

    @Override
    public RecipeMap<?>[] getAvailableRecipeMaps() {
        return recipeMaps;
    }

    @Override
    public void setRecipeMapIndex(int index) {
        this.recipeMapIndex = index;
        if (!getWorld().isRemote) {
            writeCustomData(GregtechDataCodes.RECIPE_MAP_INDEX, buf -> buf.writeByte(index));
            recipeMapWorkable.forceRecipeRecheck();
            markDirty();
        }
    }

    @Override
    public int getRecipeMapIndex() {
        return recipeMapIndex;
    }

    @Override
    public RecipeMap<?> getCurrentRecipeMap() {
        return getAvailableRecipeMaps()[recipeMapIndex];
    }

    @Override
    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn, boolean checkMaintenance, boolean checkItemIn,
                                               boolean checkItemOut, boolean checkFluidIn, boolean checkFluidOut,
                                               boolean checkMuffler) {
        boolean checkedItemIn = false, checkedItemOut = false, checkedFluidIn = false, checkedFluidOut = false;

        TraceabilityPredicate predicate = super.autoAbilities(checkMaintenance, checkMuffler)
                .or(checkEnergyIn ? abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                        .setMaxGlobalLimited(3).setPreviewCount(1) : new TraceabilityPredicate());

        for (RecipeMap<?> recipeMap : getAvailableRecipeMaps()) {
            if (!checkedItemIn && checkItemIn) {
                if (recipeMap.getMaxInputs() > 0) {
                    checkedItemIn = true;
                    predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));
                }
            }
            if (!checkedItemOut && checkItemOut) {
                if (recipeMap.getMaxOutputs() > 0) {
                    checkedItemOut = true;
                    predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1));
                }
            }
            if (!checkedFluidIn && checkFluidIn) {
                if (recipeMap.getMaxFluidInputs() > 0) {
                    checkedFluidIn = true;
                    predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1));
                }
            }
            if (!checkedFluidOut && checkFluidOut) {
                if (recipeMap.getMaxFluidOutputs() > 0) {
                    checkedFluidOut = true;
                    predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1));
                }
            }
        }
        return predicate;
    }

    @Override
    protected @NotNull Widget getFlexButton(int x, int y, int width, int height) {
        if (getAvailableRecipeMaps() != null && getAvailableRecipeMaps().length > 1) {
            return new ImageCycleButtonWidget(x, y, width, height, GuiTextures.BUTTON_MULTI_MAP,
                    getAvailableRecipeMaps().length, this::getRecipeMapIndex, this::setRecipeMapIndex)
                            .shouldUseBaseBackground().singleTexture()
                            .setTooltipHoverString(i -> LocalizationUtils
                                    .format("gregtech.multiblock.multiple_recipemaps.header") + " " +
                                    LocalizationUtils.format(
                                            "recipemap." + getAvailableRecipeMaps()[i].getUnlocalizedName() + ".name"));
        }
        return super.getFlexButton(x, y, width, height);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        if (recipeMaps.length == 1) return;
        tooltip.add(I18n.format("gregtech.multiblock.multiple_recipemaps_recipes.tooltip", this.recipeMapsToString()));
    }

    @SideOnly(Side.CLIENT)
    public String recipeMapsToString() {
        StringBuilder recipeMapsString = new StringBuilder();
        RecipeMap<?>[] recipeMaps = getAvailableRecipeMaps();
        for (int i = 0; i < recipeMaps.length; i++) {
            recipeMapsString.append(recipeMaps[i].getLocalizedName());
            if (recipeMaps.length - 1 != i)
                recipeMapsString.append(", "); // For delimiting
        }
        return recipeMapsString.toString();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("RecipeMapIndex", recipeMapIndex);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        recipeMapIndex = data.getInteger("RecipeMapIndex");
    }

    @Override
    public void writeInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(recipeMapIndex);
    }

    @Override
    public void receiveInitialSyncData(@NotNull AdvancedPacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        recipeMapIndex = buf.readByte();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull AdvancedPacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.RECIPE_MAP_INDEX) {
            recipeMapIndex = buf.readByte();
            scheduleRenderUpdate();
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        T capabilityResult = super.getCapability(capability, side);
        if (capabilityResult == null && capability == GregtechTileCapabilities.CAPABILITY_MULTIPLE_RECIPEMAPS) {
            return GregtechTileCapabilities.CAPABILITY_MULTIPLE_RECIPEMAPS.cast(this);
        }
        return capabilityResult;
    }
}
