package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException, CompileException {
        final var source = Paths.get(".", "src", "java", "magma", "Main.java");
        final var root = Files.readString(source);
        final var segments = split(root);
        for (String segment : segments) {
            compileRootSegment(segment);
        }
    }

    private static ArrayList<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';') {
                if (!buffer.isEmpty()) segments.add(buffer.toString());
                buffer = new StringBuilder();
            }
        }
        if (!buffer.isEmpty()) segments.add(buffer.toString());
        return segments;
    }

    private static void compileRootSegment(String root) throws CompileException {
        throw new CompileException("Invalid root", root);
    }
}
