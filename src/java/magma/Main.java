package magma;

import magma.app.ApplicationError;
import magma.app.ThrowableError;
import magma.app.compile.Compiler;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static final Path SOURCE = Paths.get(".", "src", "magma", "main.mgs");

    public static void main(String[] args) {
        readSafe()
                .mapErr(ThrowableError::new)
                .mapErr(ApplicationError::new)
                .mapValue(Main::compileAndWrite)
                .match(value -> value, Some::new)
                .ifPresent(err -> System.err.println(err.format(0)));
    }

    private static Option<ApplicationError> compileAndWrite(String input) {
        return new Compiler(input)
                .compile()
                .mapErr(ApplicationError::new)
                .mapValue(Main::writeOutput)
                .match(value -> value, Some::new);
    }

    private static Option<ApplicationError> writeOutput(String output) {
        final var target = SOURCE.resolveSibling("main.c");
        return writeSafe(target, output)
                .map(ThrowableError::new)
                .map(ApplicationError::new);
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
