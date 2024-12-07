package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static final String DEF_KEYWORD_WITH_SPACE = "def ";
    public static final String FUNCTION_SUFFIX = "(): Void => {}";

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
        if (input.startsWith(DEF_KEYWORD_WITH_SPACE) && input.endsWith(FUNCTION_SUFFIX)) {
            final var name = input.substring(DEF_KEYWORD_WITH_SPACE.length(), input.length() - FUNCTION_SUFFIX.length());
            return "void " + name + "(){}";
        }

        return input;
    }

    private static String renderFunction(String name) {
        return DEF_KEYWORD_WITH_SPACE + name + FUNCTION_SUFFIX;
    }
}
