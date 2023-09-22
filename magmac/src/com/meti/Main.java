package com.meti;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.meti.Options.$Option;

public class Main {
    public static void main(String[] args) {
        var root = Paths.get(".", "magmac");
        var source = root.resolve("src");
        var dist = root.resolve("dist");

        if (!Files.exists(dist)) {
            try {
                Files.createDirectories(dist);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (var list = Files.walk(source)) {
            list.filter(Files::isRegularFile).forEach(file -> compileFile(source, dist, file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void compileFile(Path source, Path dist, Path file) {
        try {
            var input = Files.readString(file);
            var lines = split(input);

            var output = lines.stream()
                    .map(Main::compileLine)
                    .collect(Collectors.joining());

            var relative = source.relativize(file);
            var parent = relative.getParent();

            var fileName = relative.getFileName().toString();
            var separator = fileName.indexOf('.');
            var withoutSeparator = fileName.substring(0, separator);

            var resolvedParent = dist.resolve(parent);
            if (!Files.exists(resolvedParent)) {
                Files.createDirectories(resolvedParent);
            }

            var outputFile = resolvedParent.resolve(withoutSeparator + ".mgs");
            Files.writeString(outputFile, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> split(String input) {
        var lines = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;

        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            if (c == ';' && depth == 0) {
                lines.add(buffer.toString());
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
                buffer.append(c);
            }
        }
        lines.add(buffer.toString());
        return lines;
    }

    private static String compileLine(String input) {
        var stripped = new JavaString(input.strip());
        return stripped.indexOfSlice("import ")
                .filter(index -> index.value() != 0)
                .map(index -> compileImport(stripped, index))
                .orElseGet(() -> compileClass(stripped))
                .unwrapOrElseGet(() -> input + ";");
    }

    private static Option<String> compileClass(JavaString stripped) {
        return $Option(() -> {
            var bodyStart = stripped.indexOfChar('{').$();
            var bodyEnd = stripped
                    .lastIndexOf('}').$()
                    .next().$();

            var body = stripped.substring(bodyStart.to(bodyEnd).$());
            var keys = stripped.sliceTo(bodyStart).strip();
            var separator = keys.lastIndexOf(' ');
            var name = keys.substring(separator + 1);

            var slicedBody = body.substring(1, body.length() - 1).strip();
            var outputBody = split(slicedBody)
                    .stream()
                    .map(Main::compileLine)
                    .collect(Collectors.joining());

            return "export class def " + name + " => " + outputBody;
        });
    }

    private static String compileImport(JavaString stripped, Index index) {
        var name = stripped.substring(index);
        var separator = name.lastIndexOf('.');
        var parent = name.substring(0, separator);
        var child = name.substring(separator + 1);
        return "import { " + child + " } from " + parent + ";\n";
    }
}