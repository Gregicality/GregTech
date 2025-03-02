package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.properties.RecipeProperty;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class CleanroomProperty extends RecipeProperty<CleanroomType> {

    public static final String KEY = "cleanroom";

    private static CleanroomProperty INSTANCE;

    private CleanroomProperty() {
        super(KEY, CleanroomType.class);
    }

    public static CleanroomProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CleanroomProperty();
            GregTechAPI.RECIPE_PROPERTIES.register(KEY, INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public @NotNull NBTBase serialize(@NotNull Object value) {
        return new NBTTagString(castValue(value).getName());
    }

    @Override
    public @NotNull Object deserialize(@NotNull NBTBase nbt) {
        return Objects.requireNonNull(CleanroomType.getByName(((NBTTagString) nbt).getString()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(@NotNull Minecraft minecraft, int x, int y, int color, Object value) {
        CleanroomType type = castValue(value);

        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.cleanroom", getName(type)), x, y, color);
    }

    @Override
    public int getInfoHeight(Object value) {
        CleanroomType type = castValue(value);
        if (type == null) return 0;
        return super.getInfoHeight(value);
    }

    @NotNull
    private static String getName(@NotNull CleanroomType value) {
        String name = I18n.format(value.getTranslationKey());
        if (name.length() >= 20) return name.substring(0, 20) + "..";
        return name;
    }
}
