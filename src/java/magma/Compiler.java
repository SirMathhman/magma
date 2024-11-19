package magma;

import java.util.ArrayList;
import java.util.Optional;

public class Compiler {
    public static final String IMPORT_KEYWORD_WITH_SPACE = "import ";
    public static final String STATEMENT_END = ";";
    public static final String PACKAGE_KEYWORD_WITH_SPACE = "package ";
    public static final String STATIC_KEYWORD_WITH_SPACE = "static ";
    public static final String CLASS_KEYWORD_WITH_SPACE = "class ";
    public static final String BLOCK_EMPTY = " {}";

    static String compile(String input) throws CompileException {
        final var segments = split(input);
        var output = new StringBuilder();
        for (String segment : segments) {
            final var segmentOutput = compileRootSegment(segment);
            output.append(segmentOutput);
        }
        return output.toString();
    }

    static String compileRootSegment(String segment) throws CompileException {
        return compilePackage(segment)
                .or(() -> compileImport(segment))
                .or(() -> compileClass(segment))
                .orElseThrow(CompileException::new);
    }

    private static Optional<String> compileClass(String segment) {
        if (!segment.startsWith(CLASS_KEYWORD_WITH_SPACE)) return Optional.empty();
        final var substring = segment.substring(CLASS_KEYWORD_WITH_SPACE.length());

        if (!segment.endsWith(BLOCK_EMPTY)) return Optional.empty();
        final var slice = substring.substring(0, substring.length() - BLOCK_EMPTY.length());
        return Optional.of(renderFunction(slice));
    }

    private static Optional<String> compilePackage(String segment) {
        return segment.startsWith(PACKAGE_KEYWORD_WITH_SPACE) ? Optional.of("") : Optional.empty();
    }

    private static Optional<String> compileImport(String segment) {
        if (!segment.startsWith(IMPORT_KEYWORD_WITH_SPACE)) return Optional.empty();

        final var slice = segment.substring(IMPORT_KEYWORD_WITH_SPACE.length());
        var maybeStatic = slice.startsWith(STATIC_KEYWORD_WITH_SPACE)
                ? slice.substring(STATIC_KEYWORD_WITH_SPACE.length())
                : slice;

        if (!maybeStatic.endsWith(STATEMENT_END)) return Optional.empty();

        final var namespace = maybeStatic.substring(0, maybeStatic.length() - STATEMENT_END.length());
        return Optional.of(renderInstanceImport(namespace));
    }

    static ArrayList<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
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

    static String renderInstanceImport(String namespace) {
        return renderImport("", namespace);
    }

    static String renderStaticImport(String namespace) {
        return renderImport(STATIC_KEYWORD_WITH_SPACE, namespace);
    }

    static String renderImport(String infix, String namespace) {
        return IMPORT_KEYWORD_WITH_SPACE + infix + namespace + STATEMENT_END;
    }

    static String renderPackageStatement(String namespace) {
        return PACKAGE_KEYWORD_WITH_SPACE + namespace + STATEMENT_END;
    }

    static String renderClass(String className) {
        return CLASS_KEYWORD_WITH_SPACE + className + BLOCK_EMPTY;
    }

    static String renderFunction(String className) {
        return "class def " + className + "() => {}";
    }
}