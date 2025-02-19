package gregtech.api.recipes.chance.output;

import gregtech.api.recipes.RecipeContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

/**
 * A list of rollable chanced outputs
 */
public class ChancedOutputList<I, T extends ChancedOutput<I>> {

    private final ChancedOutputLogic chancedOutputLogic;
    private final List<T> chancedEntries;

    public static <I, T extends ChancedOutput<I>> @NotNull ChancedOutputList<I, T> empty() {
        return new ChancedOutputList<>(ChancedOutputLogic.NONE, Collections.emptyList());
    }

    public ChancedOutputList(@NotNull ChancedOutputLogic chancedOutputLogic, @NotNull List<@NotNull T> chancedEntries) {
        this.chancedOutputLogic = chancedOutputLogic;
        this.chancedEntries = chancedEntries;
    }

    public @NotNull @Unmodifiable List<@NotNull T> getChancedEntries() {
        return chancedEntries;
    }

    /**
     * Roll the chances for this output list
     *
     * @param context Context containing machine and recipe tier, the boost function, and the chance cache
     * @return a list of the rolled outputs
     */
    public @Nullable @Unmodifiable List<CalculatedOutput<I>> roll(@NotNull RecipeContext<I> context) {
        return chancedOutputLogic.roll(getChancedEntries(), context);
    }

    public @NotNull ChancedOutputLogic getChancedOutputLogic() {
        return chancedOutputLogic;
    }

    @Override
    public String toString() {
        return chancedEntries.toString();
    }
}
