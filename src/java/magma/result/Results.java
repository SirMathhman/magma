package magma.result;

public class Results {
    public static <T, E extends Exception> T unwrap(Result<T, E> result) throws E {
        final var error = result.findError();
        if (error.isPresent()) throw error.orElseGet(() -> {
            throw new RuntimeException("Error was marked as present, but no error was provided!");
        });

        final var value = result.findValue();
        if (value.isPresent()) return value.orElseGet(() -> {
            throw new RuntimeException("Value was marked as present, but no value was provided.");
        });

        throw new RuntimeException("Neither a value nor an error was present.");
    }
}
