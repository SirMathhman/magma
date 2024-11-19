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
    public static final String CLASS_TYPE = "class";

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
                .or(() -> compile(createInstanceImportRule(), segment))
                .or(() -> compile(createStaticImportRule(), segment))
                .or(() -> compileClass(segment))
                .orElseThrow(CompileException::new);
    }

    public static Rule createStaticImportRule() {
        return createImportRule("import-static", new PrefixRule(STATIC_KEYWORD_WITH_SPACE, createNamespaceRule()));
    }

    public static Rule createInstanceImportRule() {
        return createImportRule("import", createNamespaceRule());
    }

    private static Optional<String> compileClass(String segment) {
        Rule rule = createClassRule();
        return rule.parse(segment).map(Node::value).map(className -> render(className, createFunctionRule()));
    }

    public static Rule createClassRule() {
        return new TypeRule(CLASS_TYPE, new PrefixRule(CLASS_KEYWORD_WITH_SPACE, new SuffixRule(new StringRule(), BLOCK_EMPTY)));
    }

    private static Optional<String> compilePackage(String segment) {
        return segment.startsWith(PACKAGE_KEYWORD_WITH_SPACE) ? Optional.of("") : Optional.empty();
    }

    private static Optional<String> compile(Rule rule, String input) {
        return rule.parse(input).map(Node::value).map(namespace -> render(namespace, createInstanceImportRule()));
    }

    private static SuffixRule createNamespaceRule() {
        return new SuffixRule(new StringRule(), STATEMENT_END);
    }

    private static Rule createImportRule(String type, Rule suffixRule) {
        return new TypeRule(type, new PrefixRule(IMPORT_KEYWORD_WITH_SPACE, suffixRule));
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

    public static String render(String namespace, Rule rule) {
        return rule
                .generate(new Node(Optional.empty(), namespace))
                .orElse("");
    }

    public static PrefixRule createPackageRule() {
        return new PrefixRule(PACKAGE_KEYWORD_WITH_SPACE, createNamespaceRule());
    }

    public static PrefixRule createFunctionRule() {
        return new PrefixRule("class def ", new SuffixRule(new StringRule(), "() => {}"));
    }
}