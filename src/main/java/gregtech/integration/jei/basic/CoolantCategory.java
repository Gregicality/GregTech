package gregtech.integration.jei.basic;

import gregtech.api.gui.GuiTextures;
import gregtech.api.nuclear.fission.CoolantRegistry;
import gregtech.api.nuclear.fission.ICoolantStats;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import org.jetbrains.annotations.Nullable;

public class CoolantCategory extends BasicRecipeCategory<CoolantInfo, CoolantInfo> {

    private final IDrawable icon;
    protected final IDrawable slot;
    private final IDrawable arrow;
    private String temps;
    private String heatCapacity;
    private String heatTransfer;
    private String moderation;
    private String hydrogen;

    public CoolantCategory(IGuiHelper guiHelper) {
        super("coolant", "fission.coolant.name", guiHelper.createBlankDrawable(176, 90), guiHelper);

        this.icon = guiHelper.createDrawableIngredient(MetaTileEntities.FISSION_REACTOR.getStackForm());
        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18)
                .build();
        this.arrow = guiHelper.drawableBuilder(GuiTextures.PROGRESS_BAR_ARROW.imageLocation, 0, 20, 20, 20)
                .setTextureSize(20, 40).build();
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, CoolantInfo recipeWrapper, IIngredients ingredients) {
        IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();

        fluidStackGroup.init(0, true, 55, 9);
        fluidStackGroup.set(0, recipeWrapper.coolant);
        fluidStackGroup.init(1, true, 105, 9);
        fluidStackGroup.set(1, recipeWrapper.hotCoolant);

        ICoolantStats coolant = CoolantRegistry.getCoolant(recipeWrapper.coolant.getFluid());

        temps = I18n.format("gregtech.coolant.exit_temp",
                coolant.getHotCoolant().getTemperature());
        heatCapacity = I18n.format("gregtech.coolant.heat_capacity",
                coolant.getSpecificHeatCapacity());
        heatTransfer = I18n.format(I18n.format("gregtech.coolant.cooling_factor",
                coolant.getCoolingFactor()));
        moderation = I18n.format("gregtech.coolant.moderation_factor",
                coolant.getModeratorFactor());

        if (coolant.accumulatesHydrogen()) {
            hydrogen = I18n.format("gregtech.coolant.accumulates_hydrogen");
        } else {
            hydrogen = "";
        }
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        slot.draw(minecraft, 54, 8);
        slot.draw(minecraft, 104, 8);
        arrow.draw(minecraft, 77, 6);

        int start = 40;
        int linesDrawn = 0;
        minecraft.fontRenderer.drawString(temps, 0, FONT_HEIGHT * linesDrawn + start, 0x111111);
        linesDrawn++;
        minecraft.fontRenderer.drawString(heatCapacity, 0, FONT_HEIGHT * linesDrawn + start, 0x111111);
        linesDrawn++;
        minecraft.fontRenderer.drawString(heatTransfer, 0, FONT_HEIGHT * linesDrawn + start, 0x111111);
        linesDrawn++;
        minecraft.fontRenderer.drawString(moderation, 0, FONT_HEIGHT * linesDrawn + start, 0x111111);
        linesDrawn++;
        if (hydrogen.length() > 0) {
            minecraft.fontRenderer.drawString(hydrogen, 0, FONT_HEIGHT * linesDrawn + start, 0x111111);
        }
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(CoolantInfo recipe) {
        return recipe;
    }
}
