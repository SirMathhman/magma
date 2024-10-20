package magma.app.compile;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.result.Results;
import magma.app.compile.lang.JavaLang;
import magma.app.compile.lang.MagmaLang;
import magma.app.compile.rule.RuleResult;

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
        final var node = Results.unwrap(write(JavaLang.JAVA_ROOT_RULE.parse(input)));
        final var passed = pass(node);
        return Results.unwrap(write(MagmaLang.MAGMA_ROOT_RULE.generate(passed)));
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

        List<RuleResult<T, E>> children = result.children();
        for (int i = 0; i < children.size(); i++) {
            RuleResult<T, E> child = children.get(i);
            writeResult(child, depth + 1, i);
        }
    }
}