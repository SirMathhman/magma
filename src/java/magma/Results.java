package magma;

public class Results {
    public static <T, X extends Exception> T unwrap(Result<T, X> result) throws X {
        final var value = result.findValue();
        if (value.isPresent()) return value.get();

        final var error = result.findError();
        if (error.isPresent()) throw error.get();

        throw new RuntimeException();
    }
}
