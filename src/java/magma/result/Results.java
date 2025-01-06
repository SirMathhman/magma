package magma.result;

import java.util.Optional;

public class Results {
    public static <T, X extends Exception> T unwrap(Result<T, X> result) throws X {
        final var maybeValue = result.findValue()
                .map(Optional::of)
                .orElseGet(Optional::empty);
        if (maybeValue.isPresent()) return maybeValue.get();

        final var maybeError = result.findError().map(Optional::of).orElseGet(Optional::empty);
        if (maybeError.isPresent()) throw maybeError.get();

        throw new RuntimeException("Neither a value nor an error is present.");
    }
}
