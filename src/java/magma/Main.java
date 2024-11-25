package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        extracted().ifPresent(error -> System.err.println(error.display()));
    }

    private static Option<ApplicationError> extracted() {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            final var files = stream.filter(file -> file.getFileName().toString().endsWith(".java"))
                    .toList();

            return files.stream()
                    .map(Main::compile)
                    .flatMap(Options::stream)
                    .findFirst()
                    .<Option<ApplicationError>>map(Some::new)
                    .orElseGet(None::new);
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError<>(e)));
        }
    }

    private static Option<ApplicationError> compile(Path path) {
        return readString(path).mapValue(input -> {
            final var relativized = Main.SOURCE_DIRECTORY.relativize(path);
            final var parent = relativized.getParent();

            final var targetParent = TARGET_DIRECTORY.resolve(parent);
            if (!Files.exists(targetParent)) {
                try {
                    Files.createDirectories(targetParent);
                } catch (IOException e) {
                    return new Some<>(new ApplicationError(new JavaError<>(e)));
                }
            }

            final var fileName = path.getFileName().toString();
            final var separator = fileName.indexOf('.');
            var name = fileName.substring(0, separator);
            final var target = targetParent.resolve(name + ".mgs");

            return compileRoot(input).mapErr(ApplicationError::new).<Option<ApplicationError>>mapValue(output -> {
                try {
                    Files.writeString(target, output);
                    return new None<>();
                } catch (IOException e) {
                    return new Some<>(new ApplicationError(new JavaError<>(e)));
                }
            }).match(value -> value, Some::new);
        }).match(value -> value, Some::new);
    }

    private static Result<String, ApplicationError> readString(Path path) {
        try {
            return new Ok<>(Files.readString(path));
        } catch (IOException e) {
            return new Err<>(new ApplicationError(new JavaError<>(e)));
        }
    }

    private static Result<String, CompileError> compileRoot(String root) {
        return new Err<>(new CompileError("Invalid root", root));
    }
}
