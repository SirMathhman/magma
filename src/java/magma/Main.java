package magma;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.app.ApplicationError;
import magma.app.ThrowableError;
import magma.app.compile.Compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static final Path SOURCE = Executor.ROOT.resolve("main.mgs");

    public static void main(String[] args) {
        Executor.readSafe(Main.SOURCE)
                .mapErr(ThrowableError::new)
                .mapErr(ApplicationError::new)
                .mapValue(Main::compileAndWrite)
                .match(value -> value, Some::new)
                .ifPresent(err -> System.err.println(err.format(0, 0)));
    }

    private static Option<ApplicationError> compileAndWrite(String input) {
        return new Compiler(input)
                .compile()
                .mapErr(ApplicationError::new)
                .mapValue(Main::writeOutput)
                .match(value -> value, Some::new);
    }

    private static Option<ApplicationError> writeOutput(String output) {
        return writeSafe(Executor.TARGET, output)
                .map(ThrowableError::new)
                .map(ApplicationError::new);
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
