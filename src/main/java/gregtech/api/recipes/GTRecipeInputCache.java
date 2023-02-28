package gregtech.api.recipes;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.GTLog;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GTRecipeInputCache {

    public static int EXPECTED_CACHE_SIZE = 15072;
    public static ObjectOpenHashSet<GTRecipeInput> INSTANCES;

    public static boolean isCacheEnabled() {
        return INSTANCES != null;
    }

    public static void enableCache() {
        if (INSTANCES == null) {
            INSTANCES = new ObjectOpenHashSet<>(EXPECTED_CACHE_SIZE);
            GTLog.logger.info("GTRecipeInput cache enabled");
        }
    }

    public static void disableCache() {
        if (INSTANCES != null) {
            GTLog.logger.info("GTRecipeInput cache disabled; releasing {} unique instances", INSTANCES.size());
            INSTANCES = null;
        }
    }

    public static GTRecipeInput deduplicate(GTRecipeInput recipeInput) {
        if (INSTANCES == null || recipeInput.isCached()) {
            return recipeInput;
        }
        GTRecipeInput cached = INSTANCES.addOrGet(recipeInput);
        if (cached == recipeInput) {
            cached.setCached();
        }
        return cached;
    }

    public static List<GTRecipeInput> deduplicateInputs(List<GTRecipeInput> inputs) {
        if (inputs.isEmpty()) {
            return Collections.emptyList();
        }
        if (INSTANCES == null) {
            return new ArrayList<>(inputs);
        }
        List<GTRecipeInput> list = new ArrayList<>(inputs.size());
        for (GTRecipeInput input : inputs) {
            list.add(deduplicate(input));
        }
        list.sort((ing1, ing2) -> Boolean.compare(ing1.isNonConsumable(), ing2.isNonConsumable()));
        return list;
    }
}
