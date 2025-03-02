package gregtech.api.recipes.chance.output.impl;

import gregtech.api.recipes.chance.output.BoostableChanceOutput;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.network.NetworkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Implementation for a chanced fluid output
 */
public class ChancedFluidOutput extends BoostableChanceOutput<FluidStack> {

    public ChancedFluidOutput(@NotNull FluidStack ingredient, int chance, int chanceBoost) {
        super(ingredient, chance, chanceBoost);
    }

    @Override
    public @NotNull ChancedFluidOutput copy() {
        return new ChancedFluidOutput(getIngredient().copy(), getChance(), getChanceBoost());
    }

    @Override
    public String toString() {
        return "ChancedFluidOutput{" +
                "ingredient=FluidStack{" + getIngredient().getUnlocalizedName() +
                ", amount=" + getIngredient().amount +
                "}, chance=" + getChance() +
                ", chanceBoost=" + getChanceBoost() +
                '}';
    }

    public static ChancedFluidOutput fromBuffer(PacketBuffer buffer) {
        return new ChancedFluidOutput(Objects.requireNonNull(NetworkUtils.readFluidStack(buffer)), buffer.readVarInt(),
                buffer.readVarInt());
    }

    public static void toBuffer(PacketBuffer buffer, ChancedFluidOutput value) {
        NetworkUtils.writeFluidStack(buffer, value.getIngredient());
        buffer.writeVarInt(value.getChance());
        buffer.writeVarInt(value.getChanceBoost());
    }
}
