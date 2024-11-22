package magma;

public class Results {
    public static <T, E extends Exception> T unwrap(Result<T, E> result) throws E {
        final var value = result.findValue();
        if (value.isPresent()) return value.get();

        final var error = result.findError();
        if (error.isPresent()) throw error.get();

        throw new RuntimeException();
    }
}
