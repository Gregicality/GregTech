package gregtech.api.recipes.chance.output;

import gregtech.api.recipes.RecipeContext;
import gregtech.api.recipes.chance.ChanceEntry;
import gregtech.api.recipes.chance.boost.ChanceBoostFunction;

import com.google.common.collect.ImmutableList;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ChancedOutputLogicTest {

    private static class TestChancedOutput extends ChancedOutput<String> {

        public TestChancedOutput(@NotNull String ingredient, int chance) {
            super(ingredient, chance);
        }

        @Override
        public @NotNull ChanceEntry<String> copy() {
            return new TestChancedOutput(getIngredient(), getChance());
        }
    }

    private static final RecipeContext<String> context = new RecipeContext<String>().update(ChanceBoostFunction.NONE, 0,
            0);

    private static <I, T extends ChancedOutput<I>> void listsMatch(@NotNull List<T> original,
                                                                   @Nullable List<CalculatedOutput<I>> rolled) {
        MatcherAssert.assertThat(rolled, CoreMatchers.notNullValue());
        MatcherAssert.assertThat(rolled.size(), CoreMatchers.is(original.size()));
        for (int i = 0; i < original.size(); i++) {
            MatcherAssert.assertThat(rolled.get(i).getIngrediet(), CoreMatchers.is(original.get(i).getIngredient()));
        }
    }

    @Test
    public void testORLogic() {
        List<TestChancedOutput> chanceEntries = ImmutableList.of(
                new TestChancedOutput("a", ChancedOutputLogic.getMaxChancedValue()),
                new TestChancedOutput("b", ChancedOutputLogic.getMaxChancedValue()),
                new TestChancedOutput("c", ChancedOutputLogic.getMaxChancedValue()));

        var list = ChancedOutputLogic.OR.roll(chanceEntries, context);
        listsMatch(chanceEntries, list);
    }

    @Test
    public void testANDLogic() {
        List<TestChancedOutput> chanceEntries = ImmutableList.of(
                new TestChancedOutput("a", ChancedOutputLogic.getMaxChancedValue()),
                new TestChancedOutput("b", ChancedOutputLogic.getMaxChancedValue()),
                new TestChancedOutput("c", 0));

        var list = ChancedOutputLogic.AND.roll(chanceEntries, context);
        MatcherAssert.assertThat(list, CoreMatchers.nullValue());

        chanceEntries = ImmutableList.of(
                new TestChancedOutput("a", ChancedOutputLogic.getMaxChancedValue()),
                new TestChancedOutput("b", ChancedOutputLogic.getMaxChancedValue()),
                new TestChancedOutput("c", ChancedOutputLogic.getMaxChancedValue()));

        list = ChancedOutputLogic.AND.roll(chanceEntries, context);
        listsMatch(chanceEntries, list);
    }

    @Test
    public void testXORLogic() {
        List<TestChancedOutput> chanceEntries = ImmutableList.of(
                new TestChancedOutput("a", ChancedOutputLogic.getMaxChancedValue()),
                new TestChancedOutput("b", ChancedOutputLogic.getMaxChancedValue()),
                new TestChancedOutput("c", ChancedOutputLogic.getMaxChancedValue()));

        var list = ChancedOutputLogic.XOR.roll(chanceEntries, context);
        MatcherAssert.assertThat(list, CoreMatchers.notNullValue());
        MatcherAssert.assertThat(list.size(), CoreMatchers.is(1));
        boolean exists = false;
        for (var e : chanceEntries) {
            if (e.getIngredient().equals(list.get(0).getIngrediet()))
                exists = true;
        }
        MatcherAssert.assertThat(exists, CoreMatchers.is(true));

        // XOR does not always produce the first entry from the list
        // MatcherAssert.assertThat(list.get(0).output.getIngredient(),
        // CoreMatchers.is(chanceEntries.get(0).getIngredient()));
    }

    @Test
    public void testNONELogic() {
        List<TestChancedOutput> chanceEntries = ImmutableList.of(
                new TestChancedOutput("a", ChancedOutputLogic.getMaxChancedValue()),
                new TestChancedOutput("b", ChancedOutputLogic.getMaxChancedValue()),
                new TestChancedOutput("c", ChancedOutputLogic.getMaxChancedValue()));

        var list = ChancedOutputLogic.NONE.roll(chanceEntries, context);
        MatcherAssert.assertThat(list, CoreMatchers.nullValue());
    }
}
