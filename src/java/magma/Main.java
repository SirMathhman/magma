package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static final Path SOURCE = Paths.get(".", "src", "magma", "main.mgs");

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
        final var value = input.startsWith("return ") && input.endsWith(";")
                ? input.substring("return ".length(), input.length() - 1)
                : "0";

        return "int main(){\n\treturn " + value + ";\n}";
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
