package magma.app.compile;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.CommonLang;
import magma.java.JavaLists;
import magma.java.JavaStreams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static magma.app.compile.lang.CLang.AFTER_STATEMENTS;
import static magma.app.compile.lang.CLang.BEFORE_STATEMENT;
import static magma.app.compile.lang.CommonLang.*;

public class RootPasser implements Passer {
    private static Node passFunction(Node inner) {
        final var node = inner.retype(FUNCTION_TYPE).orElse(inner);

        return node.withString(FUNCTION_NAME, "main")
                .withNode(FUNCTION_TYPE_PROPERTY, new MapNode(SYMBOL_TYPE).withString(SYMBOL_VALUE, "int"))
                .withString(AFTER_STATEMENTS, "\n");
    }

    private static Result<List<Node>, CompileError> passRootMembers(List<Node> rootMembers) {
        return JavaStreams.fromList(rootMembers)
                .map(RootPasser::passRootMember)
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

        return new Some<>(child.mapNode(RETURN_VALUE, RootPasser::passValue)
                .orElse(new Ok<>(child)));
    }

    private static Option<Result<Node, CompileError>> passDeclaration(Node child) {
        if (!child.is(CommonLang.DECLARATION_TYPE)) return new None<>();

        return new Some<>(child.mapNode(CommonLang.DECLARATION_DEFINITION, RootPasser::passDefinition).orElse(new Ok<>(child))
                .flatMapValue(declaration -> declaration.mapNode(DECLARATION_VALUE, RootPasser::passValue).orElse(new Ok<>(declaration)))
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
                .mapNode(CommonLang.DEFINITION_TYPE, new SymbolPasser()::pass)
                .orElse(new Ok<>(definition));
    }

    @Override
    public Result<Node, CompileError> pass(Node node) {
        return node.mapNodeList(CommonLang.CHILDREN, RootPasser::passRootMembers)
                .orElse(new Ok<>(node))
                .mapValue(RootPasser::passFunction)
                .mapValue(inner -> new MapNode().withNodeList(CommonLang.CHILDREN, Collections.singletonList(inner)));
    }
}