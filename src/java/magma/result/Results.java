package magma.result;

public class Results {
    public static <T, X extends Exception> T unwrap(Result<T, X> result) throws X {
        final var value = result.findValue();
        if (value.isPresent()) return value.orElseNull();

        final var error = result.findError();
        if (error.isPresent()) throw error.orElseNull();

        throw new RuntimeException("Neither a value nor an value is present.");
    }
}
