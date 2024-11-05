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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record Compiler(String input) {

    private static Result<Node, CompileError> pass(Node node) {
        return node.mapNodeList(CommonLang.CHILDREN, Compiler::passRootMembers)
                .orElse(new Ok<>(node))
                .mapValue(inner -> inner.retype(CLang.FUNCTION_TYPE).orElse(inner))
                .mapValue(inner -> new MapNode().withNodeList(CommonLang.CHILDREN, Collections.singletonList(inner)));
    }

    private static Result<List<Node>, CompileError> passRootMembers(List<Node> rootMembers) {
        return rootMembers.stream()
                .map(Compiler::passRootMember)
                .<Result<List<Node>, CompileError>>reduce(new Ok<>(new ArrayList<>()),
                        (current, element) -> current.and(() -> element).mapValue(JavaLists::add),
                        (_, value) -> value);
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