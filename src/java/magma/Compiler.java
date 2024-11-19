package magma;

import java.util.ArrayList;
import java.util.List;
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
        Rule rule = createClassRule();
        return rule.parse(segment).map(Node::value).map(Compiler::renderFunction);
    }

    private static Rule createClassRule() {
        return new TypeRule("class", new PrefixRule(CLASS_KEYWORD_WITH_SPACE, new SuffixRule(new StringRule(), BLOCK_EMPTY)));
    }

    private static Optional<String> compilePackage(String segment) {
        return segment.startsWith(PACKAGE_KEYWORD_WITH_SPACE) ? Optional.of("") : Optional.empty();
    }

    private static Optional<String> compileImport(String segment) {
        Rule rule = createImportRule();
        return rule.parse(segment).map(Node::value).map(Compiler::renderInstanceImport);
    }

    private static Rule createImportRule() {
        final var suffixRule = new SuffixRule(new StringRule(), STATEMENT_END);
        final var maybeStatic = new OrRule(List.of(
                new PrefixRule(STATIC_KEYWORD_WITH_SPACE, suffixRule),
                suffixRule
        ));

        return new TypeRule("import", new PrefixRule(IMPORT_KEYWORD_WITH_SPACE, maybeStatic));
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