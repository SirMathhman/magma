package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "main.mgs");
            final var input = Files.readString(source);
            final var target = source.resolveSibling("main.c");
            Files.writeString(target, compile(input));

            final var process = new ProcessBuilder("clang", "main.c", "-o", "main.exe")
                    .directory(Paths.get(".", "src", "java", "magma").toFile())
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compile(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < input.length(); i++) {
            final var c = input.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else if (c == '}' && depth == 1) {
                depth--;
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else if (c == '{') depth++;
            else if (c == '}') depth--;
        }
        advance(buffer, segments);

        final var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compileRootSegment(segment.strip()));
        }

        return output.toString();
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static String compileRootSegment(String input) {
        if (input.startsWith("import ") && input.endsWith(";")) {
            return "#include <" + input.substring("import ".length(), input.length() - 1) + ".h>\n";
        }

        if (input.startsWith("namespace ")) {
            return "";
        }

        return input;
    }
}
