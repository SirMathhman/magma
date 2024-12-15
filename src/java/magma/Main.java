package magma;

import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.result.Results;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "Main.java");
            final var input = Files.readString(source);
            final var target = source.resolveSibling("Main.mgs");
            Files.writeString(target, Results.unwrap(compileRoot(input)));
        } catch (IOException | CompileException e) {
            throw new RuntimeException(e);
        }
    }

    private static Result<String, CompileException> compileRoot(String input) {
        return splitAndCompile(input, Main::compileRootSegment);
    }

    private static Result<String, CompileException> splitAndCompile(String input, Function<String, Result<String, CompileException>> compiler) {
        final var segments = split(input);

        Result<StringBuilder, CompileException> current = new Ok<>(new StringBuilder());
        for (String segment : segments) {
            current = current.flatMapValue(output -> compiler.apply(segment).mapValue(output::append));
        }

        return current.mapValue(StringBuilder::toString);
    }

    private static List<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
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

    private static Result<String, CompileException> compileRootSegment(String input) {
        final var stripped = input.strip();
        final var packageResult = compileNamespace("package ", stripped, "");
        if (packageResult.isOk()) return packageResult;

        final var importResult = compileNamespace("import ", stripped, stripped + "\n");
        if (importResult.isOk()) return importResult;

        final var classResult = compileClass(stripped);
        if (classResult.isOk()) return classResult;

        return new Err<>(new CompileException("Unknown root segment", stripped));
    }

    private static Result<String, CompileException> compileClass(String stripped) {
        final var classIndex = stripped.indexOf("class ");
        if (classIndex == -1) return new Err<>(new CompileException("Infix 'class ' not present", stripped));

        final var right = stripped.substring(classIndex + "class ".length()).strip();
        final var contentStart = right.indexOf('{');
        if (contentStart == -1) return new Err<>(new CompileException("Infix '{' not present", right));

        final var name = right.substring(0, contentStart).strip();

        if (!right.endsWith("}")) return new Err<>(new CompileException("Suffix '}' not present", right));
        final var content = right.substring(0, right.length() - 1);
        return splitAndCompile(content, Main::compileClassMember)
                .mapValue(outputContent -> "class def " + name + "() => {" + outputContent + "}");
    }

    private static Result<String, CompileException> compileClassMember(String classMember) {
        return new Err<>(new CompileException("Unknown class member", classMember));
    }

    private static Result<String, CompileException> compileNamespace(String prefix, String input, String output) {
        if (input.startsWith(prefix)) return new Ok<>(output);
        else return new Err<>(new CompileException("Prefix '%s' not present".formatted(prefix), input));
    }
}
