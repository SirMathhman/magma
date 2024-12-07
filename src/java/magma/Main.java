package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class Main {
    public static final String DEF_KEYWORD_WITH_SPACE = "def ";
    public static final String BEFORE_TYPE = "(): ";
    public static final String BEFORE_CONTENT = " => {";
    public static final String AFTER_CONTENT = "}";

    private static String getString(String content) {
        return BEFORE_CONTENT +
                content +
                AFTER_CONTENT;
    }

    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "magma", "main.mgs");
            final var input = Files.readString(source);
            Files.writeString(source.resolveSibling("main.c"), compile(input));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String compile(String input) {
        return compileFunction(input).orElse(input);
    }

    private static Optional<String> compileFunction(String input) {
        if (!input.startsWith(DEF_KEYWORD_WITH_SPACE)) return Optional.empty();
        final var slice = input.substring(DEF_KEYWORD_WITH_SPACE.length());

        final var index = slice.indexOf(BEFORE_TYPE);
        if (index == -1) return Optional.empty();

        final var name = slice.substring(0, index);
        final var after = slice.substring(index + BEFORE_TYPE.length());

        final var beforeIndex = after.indexOf(BEFORE_CONTENT);
        if (beforeIndex == -1) return Optional.empty();

        final var oldType = after.substring(0, beforeIndex);
        final var contentWithEnd = after.substring(beforeIndex + BEFORE_CONTENT.length()).strip();
        if (!contentWithEnd.endsWith(AFTER_CONTENT)) return Optional.empty();

        var inputContent = contentWithEnd.substring(0, contentWithEnd.length() - AFTER_CONTENT.length()).strip();
        var outputContent = inputContent.equals("return 0;") ? "\n\treturn 0;" : "";

        final var type = switch (oldType) {
            case "I32" -> "int";
            case "Void" -> "Void";
            default -> "";
        };

        return Optional.of(type + " " + name + "(){" + outputContent + "\n}");
    }
}
