package magma;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.ApplicationError;
import magma.app.ThrowableError;
import magma.app.compile.Compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static final Path SOURCE = Paths.get(".", "src", "magma", "main.mgs");
    public static final Path TARGET = SOURCE.resolveSibling("main.asm");

    public static void main(String[] args) {
        readSafe(Main.SOURCE)
                .mapErr(ThrowableError::new)
                .mapErr(ApplicationError::new)
                .mapValue(Main::compileAndWrite)
                .match(value -> value, Some::new)
                .or(Main::readAndExecute)
                .ifPresent(err -> System.err.println(err.format(0, 0)));
    }

    private static Option<ApplicationError> readAndExecute() {
        return readSafe(Main.SOURCE)
                .mapErr(ThrowableError::new)
                .mapErr(ApplicationError::new)
                .match(input -> {
                    execute(input);
                    return new None<>();
                }, Some::new);
    }

    private static void execute(String input) {
        final var lines = input.split("\\R");

        for (String line : lines) {
        }
    }

    private static Option<ApplicationError> compileAndWrite(String input) {
        return new Compiler(input)
                .compile()
                .mapErr(ApplicationError::new)
                .mapValue(Main::writeOutput)
                .match(value -> value, Some::new);
    }

    private static Option<ApplicationError> writeOutput(String output) {
        return writeSafe(TARGET, output)
                .map(ThrowableError::new)
                .map(ApplicationError::new);
    }

    private static Result<String, IOException> readSafe(Path path) {
        try {
            return new Ok<>(Files.readString(path));
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
