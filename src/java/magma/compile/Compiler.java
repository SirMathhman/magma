package magma.compile;

import magma.compile.lang.JavaLang;
import magma.compile.lang.MagmaLang;

import java.util.List;

import static magma.compile.lang.CommonLang.CHILDREN;
import static magma.compile.lang.JavaLang.RECORD;
import static magma.compile.lang.MagmaLang.FUNCTION;

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
        final var node = JavaLang.JAVA_ROOT_RULE.parse(input).findValue()
                .orElseThrow(CompileException::new);

        final var passed = pass(node);

        return MagmaLang.MAGMA_ROOT_RULE.generate(passed).findValue()
                .orElseThrow(CompileException::new);
    }
}