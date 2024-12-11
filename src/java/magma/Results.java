package magma;

import java.util.Optional;

public class Results {
    public static <T, X> Optional<Result<T, X>> invertOption(Result<Optional<T>, X> result) {
        return result.match(value -> value.map(Ok::new), err -> Optional.of(new Err<>(err)));
    }
}
