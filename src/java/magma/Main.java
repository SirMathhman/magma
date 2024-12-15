package magma;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.result.Results;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "Main.java");
            final var input = Files.readString(source);
            final var target = source.resolveSibling("Main.mgs");
            Files.writeString(target, compile(input));
        } catch (IOException | CompileException e) {
            throw new RuntimeException(e);
        }
    }

    private static String compile(String input) throws CompileException {
        final var segments = split(input);

        var output = new StringBuilder();
        for (String segment : segments) {
            output.append(Results.unwrap(compileRootSegment(segment)));
        }

        return output.toString();
    }

    private static ArrayList<String> split(String input) {
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
        return compileNamespace("package ", stripped, "")
                .or(() -> compileNamespace("import ", stripped, stripped + "\n"))
                .or(() -> compileClass(stripped))
                .<Result<String, CompileException>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileException("Unknown root segment", stripped)));
    }

    private static Option<String> compileClass(String stripped) {
        if(stripped.contains("class ")) return new Some<>("class def Temp() => {}");
        return new None<>();
    }

    private static Option<String> compileNamespace(String prefix, String input, String output) {
        if (input.startsWith(prefix)) return new Some<>(output);
        else return new None<>();
    }
}
