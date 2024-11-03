package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static final Path SOURCE = Paths.get(".", "src", "magma", "main.mgs");
    public static final String RETURN_PREFIX = "return ";
    public static final String STATEMENT_END = ";";
    public static final String VALUE = "value";

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
        final var result = createReturnRule().parse(input)
                .flatMap(node -> createReturnRule().generate(node))
                .orElse("");

        return "int main(){\n\t" + result + "\n}";
    }

    private static PrefixRule createReturnRule() {
        return new PrefixRule(RETURN_PREFIX, new SuffixRule(new ExtractRule(VALUE), STATEMENT_END));
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
