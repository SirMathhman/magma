package magma.app.compile;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.lang.JavaLang;
import magma.app.compile.lang.MagmaLang;
import magma.app.compile.rule.RuleResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static magma.app.compile.lang.CommonLang.CHILDREN;
import static magma.app.compile.lang.JavaLang.*;
import static magma.app.compile.lang.MagmaLang.FUNCTION;

public record Compiler(String input) {
    private static Node pass(Node node) {
        return node.mapNodeList(CHILDREN, Compiler::passChildren).orElse(node);
    }

    private static List<Node> passChildren(List<Node> children) {
        return children.stream()
                .filter(child -> !child.is(JavaLang.PACKAGE))
                .map(Compiler::passRootChild)
                .toList();
    }

    private static Node passRootChild(Node node) {
        return passRecord(node)
                .or(() -> passInterface(node))
                .or(() -> passClass(node))
                .orElse(node);
    }

    private static Optional<Node> passClass(Node node) {
        if (node.is(CLASS)) {
            return Optional.of(node.retype(FUNCTION));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Node> passRecord(Node node) {
        return node.is(RECORD) ? Optional.of(node.retype(FUNCTION)) : Optional.empty();
    }

    private static Optional<Node> passInterface(Node node) {
        if (!node.is(INTERFACE)) return Optional.empty();

        final var retype = node.retype(MagmaLang.TRAIT);

        final var withModifiers = retype.mapStringList(MODIFIERS, modifiers -> {
            var newList = new ArrayList<String>();
            if (modifiers.contains("public")) newList.add("export");
            return newList;
        }).orElse(retype);

        final var withChildren = withModifiers.mapNodeList(CHILDREN, children -> children.stream()
                .map(Compiler::passClassMember)
                .toList()).orElse(withModifiers);

        return Optional.of(withChildren);
    }

    private static Node passClassMember(Node child) {
        if (child.is(METHOD)) return child.retype(FUNCTION);
        return child;
    }

    public Result<CompileResult, CompileException> compile() {
        final var parsed = createRootRule().parse(input);
        return write(parsed).flatMapValue(beforePass -> {
            final var afterPass = pass(beforePass);
            final var generated = MagmaLang.createRootRule().generate(afterPass);
            return write(generated).mapValue(output -> new CompileResult(beforePass, afterPass, output));
        });
    }

    private <T, E extends Exception> Result<T, CompileException> write(RuleResult<T, E> result) {
        if (result.isValid()) {
            return new Ok<>(result.result().findValue().orElseThrow());
        } else {
            writeResult(result, 0, 0);
            return new Err<>(new CompileException());
        }
    }

    private <T, E extends Exception> void writeResult(RuleResult<T, E> result, int depth, int index) {
        final var error = result.result().findError();
        if (error.isPresent()) {
            final var repeat = " ".repeat(depth);
            final var s = (index + 1) + ") ";
            final var rawMessage = error.get().getMessage();
            final var message = rawMessage.replaceAll("\r\n", "\r\n" + repeat + " ".repeat(s.length()));
            System.out.println(repeat + s + message);
        }

        List<RuleResult<T, E>> children = result.sortedChildren();
        for (int i = 0; i < children.size(); i++) {
            RuleResult<T, E> child = children.get(i);
            writeResult(child, depth + 1, i);
        }
    }
}