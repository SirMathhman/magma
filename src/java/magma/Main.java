package magma;

import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.result.Results;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        try {
            run();
        } catch (ApplicationException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void run() throws ApplicationException {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .toList();

            for (Path source : sources) {
                runWithSource(source);
            }
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private static void runWithSource(Path source) throws ApplicationException {
        final var relativeSourceParent = Main.SOURCE_DIRECTORY.relativize(source.getParent());
        final var targetParent = TARGET_DIRECTORY.resolve(relativeSourceParent);
        if (!Files.exists(targetParent)) createDirectoriesSafe(targetParent);

        final var name = source.getFileName().toString();
        final var separator = name.indexOf('.');
        final var nameWithoutExt = name.substring(0, separator);
        final var target = targetParent.resolve(nameWithoutExt + ".mgs");
        final var input = readSafe(source);
        final var output = Results.unwrap(compile(input));
        writeSafe(target, output);
    }

    private static void writeSafe(Path target, String output) throws ApplicationException {
        try {
            Files.writeString(target, output);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private static String readSafe(Path source) throws ApplicationException {
        try {
            return Files.readString(source);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private static void createDirectoriesSafe(Path targetParent) throws ApplicationException {
        try {
            Files.createDirectories(targetParent);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private static Result<String, ApplicationException> compile(String root) {
        final var segments = split(root);

        Result<StringBuilder, ApplicationException> result = new Ok<>(new StringBuilder());
        for (String segment : segments) {
            result = result.flatMap(builder -> compileRootSegment(segment).mapValue(builder::append));
        }

        return result.mapValue(StringBuilder::toString);
    }

    private static ArrayList<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';') {
                advance(buffer, segments);
                buffer = new StringBuilder();
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static Result<String, ApplicationException> compileRootSegment(String rootSegment) {
        if (rootSegment.startsWith("package ")) return new Ok<>("");
        return new Err<>(new CompileException("Invalid root segment", rootSegment));
    }
}
