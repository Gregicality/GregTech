package gregtech.api.recipes;

import gregtech.api.recipes.chance.boost.BoostableChanceEntry;
import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import gregtech.api.recipes.chance.output.ChancedOutput;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RecipeContext<I> {

    private final Map<I, Integer> cache;
    ChanceBoostFunction boostFunction;
    public int baseTier, machineTier;

    public RecipeContext(@Nullable Hash.Strategy<I> strategy) {
        this.cache = strategy == null ? null : new Object2IntOpenCustomHashMap<>(strategy);
    }

    public RecipeContext() {
        this(null);
    }

    public RecipeContext<I> update(ChanceBoostFunction boostFunction,
                                   int baseTier, int machineTier) {
        this.boostFunction = boostFunction;
        this.baseTier = baseTier;
        this.machineTier = machineTier;
        return this;
    }

    public void updateCachedChance(ChancedOutput<I> entry, int chance) {
        updateCachedChance(entry.getIngredient(), chance % entry.getMaxChance());
    }

    public void updateCachedChance(I ingredient, int chance) {
        if (cache == null) return;
        cache.put(ingredient, chance);
    }

    public int getCachedChance(ChancedOutput<I> entry) {
        if (cache == null || !cache.containsKey(entry.getIngredient()))
            return -1;

        return cache.get(entry.getIngredient());
    }

    public int getChance(ChancedOutput<I> entry) {
        int cache = getCachedChance(entry);
        if (cache == -1) cache = 0;
        if (entry instanceof BoostableChanceEntry<?>boostableChanceEntry) {
            return boostChance(boostableChanceEntry) + cache;
        }
        return entry.getChance() + cache;
    }

    public int boostChance(BoostableChanceEntry<?> entry) {
        return boostFunction.getBoostedChance(entry, baseTier, machineTier);
    }

    public Map<I, Integer> getCache() {
        return cache;
    }
}
