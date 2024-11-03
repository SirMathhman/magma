package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static final Path SOURCE = Paths.get(".", "src", "magma", "main.mgs");
    public static final String RETURN_PREFIX = "return ";
    public static final String STATEMENT_END = ";";

    public static void main(String[] args) {
        readSafe()
                .mapValue(Main::compileAndWrite)
                .match(value -> value, Some::new)
                .ifPresent(Throwable::printStackTrace);
    }

    private static Option<IOException> compileAndWrite(String input) {
        final var output = compile(input);
        final var target = SOURCE.resolveSibling("main.c");
        return writeSafe(target, output);
    }

    private static String compile(String input) {
        final var value = findReturnValue(input).orElse("0");
        return "int main(){\n\t" + generateReturnStatement(value) + "\n}";
    }

    /*
    TODO: extract return rule
     */
    private static Option<String> findReturnValue(String input) {
        if (!input.startsWith(RETURN_PREFIX)) return new None<>();

        final var slice = input.substring(RETURN_PREFIX.length());
        if (!input.endsWith(STATEMENT_END)) return new None<>();

        final var value = slice.substring(0, slice.length() - 1);
        return findString(value);
    }

    private static Option<String> findString(String value) {
        return new Some<>(value);
    }

    private static String generateReturnStatement(String value) {
        return RETURN_PREFIX + value + STATEMENT_END;
    }

    private static Result<String, IOException> readSafe() {
        try {
            return new Ok<>(Files.readString(Main.SOURCE));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static Option<IOException> writeSafe(Path target, String output) {
        try {
            Files.writeString(target, output);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(e);
        }
    }
}
