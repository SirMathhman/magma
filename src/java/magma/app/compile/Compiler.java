package magma.app.compile;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
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
import static magma.app.compile.lang.CLang.BEFORE_STATEMENT;
import static magma.app.compile.lang.CommonLang.*;

public record Compiler(String input) {

    private static Result<Node, CompileError> pass(Node node) {
        return node.mapNodeList(CommonLang.CHILDREN, Compiler::passRootMembers)
                .orElse(new Ok<>(node))
                .mapValue(Compiler::passFunction)
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
        return passDeclaration(child)
                .or(() -> passReturn(child))
                .orElse(new Ok<>(child))
                .mapValue(rootMember -> rootMember.withString(BEFORE_STATEMENT, "\n\t"));
    }

    private static Option<Result<Node, CompileError>> passReturn(Node child) {
        if (!child.is(RETURN_TYPE)) return new None<>();

        return new Some<>(child.mapNode(RETURN_VALUE, Compiler::passValue)
                .orElse(new Ok<>(child)));
    }

    private static Option<Result<Node, CompileError>> passDeclaration(Node child) {
        if (!child.is(CommonLang.DECLARATION_TYPE)) return new None<>();

        return new Some<>(child.mapNode(CommonLang.DECLARATION_DEFINITION, Compiler::passDefinition).orElse(new Ok<>(child))
                .flatMapValue(declaration -> declaration.mapNode(DECLARATION_VALUE, Compiler::passValue).orElse(new Ok<>(declaration)))
                .mapValue(value -> value.withString(DECLARATION_AFTER_DEFINITION, " "))
                .mapValue(value -> value.withString(DECLARATION_BEFORE_VALUE, " ")));
    }

    private static Ok<Node, CompileError> passValue(Node value) {
        if (value.is(ADD_TYPE)) {
            return new Ok<>(value.withString(OPERATION_AFTER_LEFT, " ")
                    .withString(OPERATION_BEFORE_RIGHT, " "));
        } else {
            return new Ok<>(value);
        }
    }

    private static Result<Node, CompileError> passDefinition(Node definition) {
        return definition
                .mapNode(CommonLang.DEFINITION_TYPE, Compiler::passSymbolType)
                .orElse(new Ok<>(definition));
    }

    private static Result<Node, CompileError> passSymbolType(Node type) {
        if (!type.is(CommonLang.SYMBOL_TYPE)) return new Ok<>(type);

        return type
                .mapString(CommonLang.SYMBOL_VALUE, Compiler::passSymbolTypeValue)
                .orElse(new Ok<>(type));
    }

    private static Result<String, CompileError> passSymbolTypeValue(String value) {
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