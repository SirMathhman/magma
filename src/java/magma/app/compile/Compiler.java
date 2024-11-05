package magma.app.compile;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.StringContext;
import magma.app.compile.lang.CLang;
import magma.app.compile.lang.CommonLang;
import magma.app.compile.lang.MagmaLang;
import magma.java.JavaLists;
import magma.java.JavaStreams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static magma.app.compile.lang.CLang.AFTER_STATEMENTS;

public record Compiler(String input) {

    private static Result<Node, CompileError> pass(Node node) {
        return node.mapNodeList(CommonLang.CHILDREN, Compiler::passRootMembers)
                .orElse(new Ok<>(node))
                .mapValue(inner -> passFunction(inner))
                .mapValue(inner -> new MapNode().withNodeList(CommonLang.CHILDREN, Collections.singletonList(inner)));
    }

    private static Node passFunction(Node inner) {
        return inner.retype(CLang.FUNCTION_TYPE).orElse(inner).withString(AFTER_STATEMENTS, "\n");
    }

    private static Result<List<Node>, CompileError> passRootMembers(List<Node> rootMembers) {
        return JavaStreams.stream(rootMembers)
                .map(Compiler::passRootMember)
                .into(ResultStream::new)
                .foldResultsLeft(new ArrayList<>(), JavaLists::add);
    }

    private static Result<Node, CompileError> passRootMember(Node child) {
        if (!child.is(CommonLang.DECLARATION_TYPE)) {
            return new Ok<>(child);
        }

        return child.mapNode(CommonLang.DECLARATION_DEFINITION, Compiler::passDefinition).orElse(new Ok<>(child));
    }

    private static Result<Node, CompileError> passDefinition(Node definition) {
        return definition
                .mapNode(CommonLang.DEFINITION_TYPE, Compiler::passSymbolType)
                .orElse(new Ok<>(definition));
    }

    private static Result<Node, CompileError> passSymbolType(Node type) {
        if (!type.is(CommonLang.SYMBOL_TYPE)) return new Ok<>(type);

        return type
                .mapString(CommonLang.SYMBOL_VALUE, Compiler::passSymbolValue)
                .orElse(new Ok<>(type));
    }

    private static Result<String, CompileError> passSymbolValue(String value) {
        if (value.equals("I32")) return new Ok<>("int");
        else return new Err<>(new CompileError("Unknown value", new StringContext(value)));
    }

    public Result<String, CompileError> compile() {
        final var sourceRule = MagmaLang.createMagmaRootRule();
        final var targetRule = CLang.createCRootRule();

        return sourceRule.parse(this.input())
                .flatMapValue(Compiler::pass)
                .flatMapValue(targetRule::generate);
    }
}