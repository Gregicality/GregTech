package gregtech.api.recipes.chance.output.impl;

import gregtech.api.recipes.chance.output.BoostableChanceOutput;
import gregtech.api.util.GTStringUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.network.NetworkUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation for a chanced item output
 */
public class ChancedItemOutput extends BoostableChanceOutput<ItemStack> {

    public ChancedItemOutput(@NotNull ItemStack ingredient, int chance, int chanceBoost) {
        super(ingredient, chance, chanceBoost);
    }

    @Override
    public @NotNull ChancedItemOutput copy() {
        return new ChancedItemOutput(getIngredient().copy(), getChance(), getChanceBoost());
    }

    @Override
    public String toString() {
        return "ChancedItemOutput{" +
                "ingredient=" + GTStringUtils.prettyPrintItemStack(getIngredient()) +
                ", chance=" + getChance() +
                ", chanceBoost=" + getChanceBoost() +
                '}';
    }

    public static ChancedItemOutput fromBuffer(PacketBuffer buffer) {
        return new ChancedItemOutput(NetworkUtils.readItemStack(buffer), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void toBuffer(PacketBuffer buffer, ChancedItemOutput value) {
        NetworkUtils.writeItemStack(buffer, value.getIngredient());
        buffer.writeVarInt(value.getChance());
        buffer.writeVarInt(value.getChanceBoost());
    }
}
