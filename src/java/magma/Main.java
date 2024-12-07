package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static final String DEF_KEYWORD_WITH_SPACE = "def ";
    public static final String BEFORE_TYPE = "(): ";
    public static final String AFTER_TYPE = " => {}";

    private static String getString(String returnType) {
        return BEFORE_TYPE + returnType + AFTER_TYPE;
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
                if (after.endsWith(AFTER_TYPE)) {
                    final var oldType = after.substring(0, after.length() - AFTER_TYPE.length());
                    final var type = switch (oldType) {
                        case "I32" -> "int";
                        case "Void" -> "Void";
                        default -> "";
                    };

                    return type + " " + name + "(){}";
                }
            }
        }

        return input;
    }
}
