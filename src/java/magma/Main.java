package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static final Path SOURCE = Paths.get(".", "src", "magma", "main.mgs");

    public static void main(String[] args) {
        readSafe().mapValue(input -> {
            final var value = input.startsWith("return ") && input.endsWith(";")
                    ? input.substring("return ".length(), input.length() - 1)
                    : "0";

            final var output = "int main(){\n\treturn " + value + ";\n}";

            final var target = SOURCE.resolveSibling("main.c");
            return writeSafe(target, output);
        }).match(value -> value, Some::new).ifPresent(Throwable::printStackTrace);
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
