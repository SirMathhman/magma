package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Main {

    public static final String CLASS_KEYWORD_WITH_SPACE = "class ";

    public static void main(String[] args) {
        try {
            var source = Paths.get(".", "magmac", "src", "magma", "Main.java");
            var input = Files.readString(source);
            var target = source.resolveSibling("Main.mgs");
            Files.writeString(target, compile(input));
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compile(String input) {
        var inputTokens = split(input);

        var outputTokens = new ArrayList<String>();
        for (var inputToken : inputTokens) {
            outputTokens.add(compileRootStatement(inputToken.strip()));
        }

        return String.join("", outputTokens);
    }

    private static String compileRootStatement(String input) {
        return compileEmpty(input)
                .or(() -> compilePackage(input))
                .or(() -> compileImport(input))
                .or(() -> compileClass(input))
                .orElse(input);
    }

    private static Optional<String> compileClass(String input) {
        return lexClass(input).map(Main::parse).flatMap(Main::renderClass);
    }

    private static Optional<Node> lexClass(String input) {
        var modifiers = new ExtractStringListRule("modifiers", " ");
        var name = new StripRule(new ExtractStringRule("name"));
        var content = new ExtractStringRule("content");

        return new SplitAtSliceRule(new StripRule(modifiers), CLASS_KEYWORD_WITH_SPACE, new StripRule(new SplitAtSliceRule(name, "{", new StripRule(new RightRule(content, "}"))))).toNode(input);
    }

    private static Node parse(Node node) {
        var oldModifiers = node.apply("modifiers")
                .flatMap(Attribute::asStringList)
                .orElseThrow();

        var newModifiers = new ArrayList<String>();
        for (var oldModifier : oldModifiers) {
            if (oldModifier.equals("public")) {
                newModifiers.add("export");
            }
        }

        newModifiers.add("class");
        newModifiers.add("def");

        return node.with("modifiers", new StringListAttribute(newModifiers));
    }

    private static Optional<String> renderClass(Node node) {
        var modifiers = new ExtractStringListRule("modifiers", " ");
        var name = new ExtractStringRule("name");
        var content = new ExtractStringRule("content");

        return new SplitAtSliceRule(modifiers, " ", new SplitAtSliceRule(name, "() => {\n\t", new RightRule(content, "}"))).fromNode(node);
    }

    private static Optional<String> compileImport(String input) {
        if (input.startsWith("import ")) {
            return Optional.of(input + "\n");
        }
        return Optional.empty();
    }

    private static Optional<String> compilePackage(String input) {
        if (input.startsWith("package ")) {
            return Optional.of("");
        }
        return Optional.empty();
    }

    private static Optional<String> compileEmpty(String input) {
        if (input.isEmpty()) {
            return Optional.of("");
        }
        return Optional.empty();
    }

    private static List<String> split(String input) {
        var current = new State();
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            current = processChar(current.append(c), c);
        }

        return current.advance().tokens;
    }

    private static State processChar(State state, char c) {
        if (c == ';' && state.isLevel()) return state.advance();
        if (c == '{') return state.enter();
        if (c == '}') return state.exit();
        return state;
    }

    record State(List<String> tokens, StringBuilder buffer, int depth) {
        public State() {
            this(Collections.emptyList(), new StringBuilder(), 0);
        }

        private State advance() {
            var copy = new ArrayList<>(this.tokens);
            copy.add(this.buffer.toString());
            return new State(copy, new StringBuilder(), depth);
        }

        private State append(char c) {
            return new State(tokens, this.buffer.append(c), depth);
        }

        public boolean isLevel() {
            return this.depth == 0;
        }

        public State enter() {
            return new State(tokens, buffer, depth + 1);
        }

        public State exit() {
            return new State(tokens, buffer, depth - 1);
        }
    }
}
