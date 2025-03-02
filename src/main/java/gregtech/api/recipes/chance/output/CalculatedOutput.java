package gregtech.api.recipes.chance.output;

public class CalculatedOutput<I> {

    ChancedOutput<I> output;
    int amount;

    public CalculatedOutput(ChancedOutput<I> output, int amount) {
        this.output = output;
        this.amount = amount;
    }

    public CalculatedOutput(ChancedOutput<I> output) {
        this(output, 1);
    }

    public I createStack(ChanceConverter<I> converter) {
        return converter.convert(output.getIngredient(), amount);
    }

    public I getIngrediet() {
        return output.getIngredient();
    }

    @FunctionalInterface
    public interface ChanceConverter<I> {

        I convert(I output, int count);
    }
}
