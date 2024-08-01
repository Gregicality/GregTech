package gregtech.api.recipes.chance.output;

import gregtech.api.GTValues;
import gregtech.api.recipes.chance.boost.BoostableChanceEntry;
import gregtech.api.recipes.chance.boost.ChanceBoostFunction;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Logic for determining which chanced outputs should be produced from a list
 */
public interface ChancedOutputLogic {

    /**
     * Chanced Output Logic where any ingredients succeeding their roll will be produced
     */
    ChancedOutputLogic OR = new ChancedOutputLogic() {

        @Override
        public @Nullable @Unmodifiable <I,
                T extends ChancedOutput<I>> List<@NotNull T> roll(@NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                  @NotNull ChanceBoostFunction boostFunction,
                                                                  int baseTier, int machineTier,
                                                                  Map<I, Integer> cache) {
            ImmutableList.Builder<T> builder = ImmutableList.builder();
            for (T entry : chancedEntries) {
                int numerator = getChance(entry, boostFunction, baseTier, machineTier);
                int denominator = entry.getDenominator();
                int cached = cache.get(entry.getIngredient());
                if (cached + numerator >= denominator) {
                    builder.add(entry);
                    cache.put(entry.getIngredient(), cached + numerator - denominator);
                } else {
                    cache.put(entry.getIngredient(), cached + numerator);
                }
                // if (passesChance(getChance(entry, boostFunction, baseTier, machineTier))) {
                // builder.add(entry);
                // }
            }

            List<T> list = builder.build();
            return list.size() == 0 ? null : list;
        }

        @Override
        public @NotNull String getTranslationKey() {
            return "gregtech.chance_logic.or";
        }

        @Override
        public String toString() {
            return "ChancedOutputLogic{OR}";
        }
    };

    /**
     * Chanced Output Logic where all ingredients must succeed their roll in order for any to be produced
     */
    ChancedOutputLogic AND = new ChancedOutputLogic() {

        @Override
        public @Nullable @Unmodifiable <I,
                T extends ChancedOutput<I>> List<@NotNull T> roll(@NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                  @NotNull ChanceBoostFunction boostFunction,
                                                                  int baseTier, int machineTier,
                                                                  Map<I, Integer> cache) {
            for (T entry : chancedEntries) {
                if (!passesChance(getChance(entry, boostFunction, baseTier, machineTier))) {
                    return null;
                }
            }
            return ImmutableList.copyOf(chancedEntries);
        }

        @Override
        public @NotNull String getTranslationKey() {
            return "gregtech.chance_logic.and";
        }

        @Override
        public String toString() {
            return "ChancedOutputLogic{AND}";
        }
    };

    /**
     * Chanced Output Logic where only the first ingredient succeeding its roll will be produced
     */
    ChancedOutputLogic XOR = new ChancedOutputLogic() {

        @Override
        public @Nullable @Unmodifiable <I,
                T extends ChancedOutput<I>> List<@NotNull T> roll(@NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                  @NotNull ChanceBoostFunction boostFunction,
                                                                  int baseTier, int machineTier,
                                                                  Map<I, Integer> cache) {
            for (T entry : chancedEntries) {
                if (passesChance(getChance(entry, boostFunction, baseTier, machineTier))) {
                    return Collections.singletonList(entry);
                }
            }
            return null;
        }

        @Override
        public @NotNull String getTranslationKey() {
            return "gregtech.chance_logic.xor";
        }

        @Override
        public String toString() {
            return "ChancedOutputLogic{XOR}";
        }
    };

    /**
     * Chanced Output Logic where nothing is produced
     */
    ChancedOutputLogic NONE = new ChancedOutputLogic() {

        @Override
        public @Nullable @Unmodifiable <I,
                T extends ChancedOutput<I>> List<@NotNull T> roll(@NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                  @NotNull ChanceBoostFunction boostFunction,
                                                                  int baseTier, int machineTier,
                                                                  Map<I, Integer> cache) {
            return null;
        }

        @Override
        public @NotNull String getTranslationKey() {
            return "gregtech.chance_logic.none";
        }

        @Override
        public String toString() {
            return "ChancedOutputLogic{NONE}";
        }
    };

    /**
     * @param entry         the entry to get the complete chance for
     * @param boostFunction the function boosting the entry's chance
     * @param baseTier      the base tier of the recipe
     * @param machineTier   the tier the recipe is run at
     * @return the total chance for the entry
     */
    static int getChance(@NotNull ChancedOutput<?> entry, @NotNull ChanceBoostFunction boostFunction, int baseTier,
                         int machineTier) {
        if (entry instanceof BoostableChanceEntry<?>boostableChanceEntry) {
            return boostFunction.getBoostedChance(boostableChanceEntry, baseTier, machineTier);
        }
        return entry.getChance();
    }

    /**
     * @param chance the chance to check
     * @return if the roll with the chance is successful
     */
    static boolean passesChance(int chance) {
        return chance > 0 && GTValues.RNG.nextInt(getMaxChancedValue()) <= chance;
    }

    /**
     * @return the upper bound for rolling chances
     */
    static int getMaxChancedValue() {
        return 10_000;
    }

    /**
     * Roll the chance and attempt to produce the output
     *
     * @param chancedEntries the list of entries to roll
     * @param boostFunction  the function to boost the entries' chances
     * @param baseTier       the base tier of the recipe
     * @param machineTier    the tier the recipe is run at
     * @param cache
     * @return a list of the produced outputs
     */
    <I, T extends ChancedOutput<I>> @Nullable @Unmodifiable List<@NotNull T> roll(
                                                                                  @NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                                  @NotNull ChanceBoostFunction boostFunction,
                                                                                  int baseTier, int machineTier,
                                                                                  Map<I, Integer> cache);

    @NotNull
    String getTranslationKey();
}
