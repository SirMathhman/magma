package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        if (input.startsWith(DEF_KEYWORD_WITH_SPACE)) {
            final var slice = input.substring(DEF_KEYWORD_WITH_SPACE.length());

            final var index = slice.indexOf(BEFORE_TYPE);
            if (index != -1) {
                final var name = slice.substring(0, index);
                final var after = slice.substring(index + BEFORE_TYPE.length());

                final var beforeIndex = after.indexOf(BEFORE_CONTENT);
                if (beforeIndex != -1) {
                    final var oldType = after.substring(0, beforeIndex);
                    final var contentWithEnd = after.substring(beforeIndex + BEFORE_CONTENT.length());
                    if (!contentWithEnd.endsWith(AFTER_CONTENT)) {
                        var content = contentWithEnd.substring(0, contentWithEnd.length() - AFTER_CONTENT.length());
                        String outputContent = content.equals("\n" +
                                "    return 0;\n" +
                                "") ? "{return 0;}" : "";

                        final var type = switch (oldType) {
                            case "I32" -> "int";
                            case "Void" -> "Void";
                            default -> "";
                        };

                        return type + " " + name + "(){}";
                    }
                }
            }
        }

        return input;
    }
}
