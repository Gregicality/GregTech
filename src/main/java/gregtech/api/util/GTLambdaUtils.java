package gregtech.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class GTLambdaUtils {

    public static @NotNull <T> Consumer<T> mergeConsumers(@Nullable Consumer<T> first, @NotNull Consumer<T> andThen) {
        return first == null ? andThen : first.andThen(andThen);
    }
}
