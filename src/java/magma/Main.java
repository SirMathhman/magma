package magma;

import magma.error.ApplicationError;
import magma.error.CompileError;
import magma.error.Error;
import magma.error.JavaError;
import magma.option.None;
import magma.option.Option;
import magma.option.Options;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        run().map(Error::display).ifPresent(System.err::println);
    }

    private static Option<ApplicationError> run() {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            return stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .map(Main::runWithSource)
                    .flatMap(Options::asStream)
                    .findFirst()
                    .<Option<ApplicationError>>map(Some::new)
                    .orElseGet(None::new);
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError(e)));
        }
    }

    private static Option<ApplicationError> runWithSource(Path source) {
        final var relativeSourceParent = Main.SOURCE_DIRECTORY.relativize(source.getParent());
        final var targetParent = TARGET_DIRECTORY.resolve(relativeSourceParent);
        if (!Files.exists(targetParent)) {
            return createDirectoriesSafe(targetParent).or(() -> compileAndRead(source, targetParent));
        }

        return compileAndRead(source, targetParent);
    }

    private static Option<ApplicationError> compileAndRead(Path source, Path targetParent) {
        final var name = source.getFileName().toString();
        final var separator = name.indexOf('.');
        final var nameWithoutExt = name.substring(0, separator);
        final var target = targetParent.resolve(nameWithoutExt + ".mgs");
        return readSafe(source)
                .mapValue(input -> compileAndWrite(input, target))
                .match(value -> value, Some::new);
    }

    private static Option<ApplicationError> compileAndWrite(String input, Path target) {
        return compile(input)
                .mapErr(ApplicationError::new)
                .mapValue(output -> writeSafe(target, output))
                .match(value -> value, Some::new);
    }

    private static Option<ApplicationError> writeSafe(Path target, String output) {
        try {
            Files.writeString(target, output);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError(e)));
        }
    }

    private static Result<String, ApplicationError> readSafe(Path source) {
        try {
            return new Ok<>(Files.readString(source));
        } catch (IOException e) {
            return new Err<>(new ApplicationError(new JavaError(e)));
        }
    }

    private static Option<ApplicationError> createDirectoriesSafe(Path targetParent) {
        try {
            Files.createDirectories(targetParent);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError(e)));
        }
    }

    private static Result<String, CompileError> compile(String root) {
        final var segments = split(root);

        Result<StringBuilder, CompileError> result = new Ok<>(new StringBuilder());
        for (String segment : segments) {
            result = result.flatMap(builder -> compileRootSegment(segment.strip()).mapValue(builder::append));
        }

        return result.mapValue(StringBuilder::toString);
    }

    private static ArrayList<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static Result<String, CompileError> compileRootSegment(String input) {
        return createJavaRootMemberRule().parse(input).flatMap(javaRootMemberRule -> {
            return createMagmaRootMemberRule().generate(javaRootMemberRule);
        });
    }

    private static OrRule createMagmaRootMemberRule() {
        return new OrRule(List.of(
                new TypeRule("import", new ExactRule("import temp;")),
                new TypeRule("function", new ExactRule("def temp() => {}")),
                new TypeRule("trait", new ExactRule("trait Temp {}"))
        ));
    }

    private static OrRule createJavaRootMemberRule() {
        return new OrRule(List.of(
                new TypeRule("package", new PrefixRule("package ", new DiscardRule())),
                new TypeRule("import", new PrefixRule("import ", new DiscardRule())),
                new TypeRule("record ", new InfixRule(new DiscardRule(), "record ", new DiscardRule())),
                new TypeRule("class ", new InfixRule(new DiscardRule(), "class ", new DiscardRule())),
                new TypeRule("interface ", new InfixRule(new DiscardRule(), "interface ", new DiscardRule()))
        ));
    }
}
