package magma.app.compile;

import magma.api.Tuple;
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

public class PassingStage {
    static Result<Node, CompileError> pass(Node node) {
        return passNodes(node)
                .flatMapValue(PassingStage::passNodeLists)
                .flatMapValue(PassingStage::getNodeCompileErrorResult);
    }

    private static Result<Node, CompileError> getNodeCompileErrorResult(Node node) {
        return node.mapNodeList(CommonLang.CHILDREN, PassingStage::passRootMembers)
                .orElse(new Ok<>(node))
                .mapValue(PassingStage::passFunction)
                .mapValue(inner -> new MapNode().withNodeList(CommonLang.CHILDREN, Collections.singletonList(inner)));
    }

    private static Result<Node, CompileError> passNodeLists(Node node) {
        return node.streamNodeLists().foldLeftToResult(node, PassingStage::foldNodeListsIntoNode);
    }

    private static Result<Node, CompileError> foldNodeListsIntoNode(Node node, Tuple<String, List<Node>> entry) {
        return JavaStreams.fromList(entry.right())
                .foldLeftToResult(new ArrayList<>(), PassingStage::passAndAnd)
                .mapValue(list -> node.withNodeList(entry.left(), list));
    }

    private static Result<List<Node>, CompileError> passAndAnd(List<Node> values, Node value) {
        return pass(value).mapValue(passed -> JavaLists.add(values, passed));
    }

    private static Result<Node, CompileError> passNodes(Node node) {
        return node.streamNodes()
                .foldLeftToResult(node, (current, tuple) -> pass(tuple.right())
                        .mapValue(value -> current.withNode(tuple.left(), value)));
    }

    private static Node passFunction(Node inner) {
        final var node = inner.retype(FUNCTION_TYPE).orElse(inner);

        return node.withString(FUNCTION_NAME, "main")
                .withNode(FUNCTION_TYPE_PROPERTY, new MapNode(SYMBOL_TYPE).withString(SYMBOL_VALUE, "int"))
                .withString(AFTER_STATEMENTS, "\n");
    }

    private static Result<List<Node>, CompileError> passRootMembers(List<Node> rootMembers) {
        return JavaStreams.fromList(rootMembers)
                .map(PassingStage::passRootMember)
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

        return new Some<>(child.mapNode(RETURN_VALUE, PassingStage::passValue)
                .orElse(new Ok<>(child)));
    }

    private static Option<Result<Node, CompileError>> passDeclaration(Node child) {
        if (!child.is(CommonLang.DECLARATION_TYPE)) return new None<>();

        return new Some<>(child.mapNode(CommonLang.DECLARATION_DEFINITION, PassingStage::passDefinition).orElse(new Ok<>(child))
                .flatMapValue(declaration -> declaration.mapNode(DECLARATION_VALUE, PassingStage::passValue).orElse(new Ok<>(declaration)))
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
}
