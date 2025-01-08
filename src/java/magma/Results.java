package magma;

public class Results {
    public static <T, X extends Exception> T unwrap(Result<T, X> result) throws X {
        final var maybeValue = result.findValue();
        if (maybeValue.isPresent()) return maybeValue.get();

        final var maybeError = result.findError();
        if (maybeError.isPresent()) throw maybeError.get();

        throw new RuntimeException("Neither a value nor an error is present.");
    }
}