package magma.app.compile;

import magma.api.result.Results;
import magma.app.compile.lang.JavaLang;
import magma.app.compile.lang.MagmaLang;

import java.util.List;

import static magma.app.compile.lang.CommonLang.CHILDREN;
import static magma.app.compile.lang.JavaLang.RECORD;
import static magma.app.compile.lang.MagmaLang.FUNCTION;

public record Compiler(String input) {
    private static Node pass(Node node) {
        return node.mapNodeList(CHILDREN, Compiler::passChildren).orElse(node);
    }

    private static List<Node> passChildren(List<Node> children) {
        return children.stream()
                .filter(child -> !child.is(JavaLang.PACKAGE))
                .map(child -> child.is(RECORD) ? child.retype(FUNCTION) : child)
                .toList();
    }

    public String compile() throws CompileException {
        final var node = Results.unwrap(JavaLang.JAVA_ROOT_RULE.parse(input).unwrap());
        final var passed = pass(node);
        return Results.unwrap(MagmaLang.MAGMA_ROOT_RULE.generate(passed).unwrap());
    }
}