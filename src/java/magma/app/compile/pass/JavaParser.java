package magma.app.compile.pass;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static magma.app.lang.CommonLang.LAMBDA_PARAMETERS;
import static magma.app.lang.CommonLang.SYMBOL_VALUE_TYPE;
import static magma.java.JavaLang.isDefaultJavaValue;

public class JavaParser implements Passer {
    private static Node createDefinition(String parameter) {
        return new MapNode("definition").withString("name", parameter);
    }

    @Override
    public Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        final var node = unit.value();
        if (node.is("method")) {
            return new Ok<>(unit.exit());
        }

        if (node.is("class") || node.is("record") || node.is("interface")) {
            return new Ok<>(unit.exit());
        }

        return new Ok<>(unit);
    }

    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        final var node = unit.value();
        if (node.is("lambda")) {
            final List<Node> definitions;
            final var parameterNode = node.findNode(LAMBDA_PARAMETERS);
            final var parameterNodeLists = node.findNodeList(LAMBDA_PARAMETERS);
            if (parameterNode.isPresent()) {
                final var parameter = parameterNode
                        .orElse(new MapNode())
                        .findString("value")
                        .orElse("");

                definitions = List.of(createDefinition(parameter));
            } else if (parameterNodeLists.isPresent()) {
                definitions = parameterNodeLists.orElse(new ArrayList<>())
                        .stream()
                        .map(child -> child.findString("value"))
                        .flatMap(Optional::stream)
                        .map(JavaParser::createDefinition)
                        .toList();
            } else {
                definitions = Collections.emptyList();
            }

            return new Ok<>(unit.enter().define(definitions));
        }

        if (node.is("class") || node.is("record") || node.is("interface")) {
            final var name = node.findString("name").orElse("");
            final var value = node.findNode("value").orElse(new MapNode());
            final var children = value.findNodeList("children").orElse(new ArrayList<>());

            final var methodDefinitions = children.stream()
                    .filter(child -> child.is("method"))
                    .map(method -> method.findNode("definition"))
                    .flatMap(Optional::stream)
                    .toList();

            return new Ok<>(unit.enter()
                    .define(List.of(createDefinition(name)))
                    .define(methodDefinitions));
        }

        if (node.is("method")) {
            final var params = node.findNodeList("params").orElse(Collections.emptyList());
            return new Ok<>(unit.enter().define(params));
        }

        if (node.is("import")) {
            final var value = node.findNodeList("namespace")
                    .orElse(Collections.emptyList())
                    .getLast()
                    .findString("value")
                    .orElse("");

            return new Ok<>(unit.define(List.of(createDefinition(value))));
        }

        if (node.is("definition")) {
            return new Ok<>(unit.define(List.of(node)));
        }

        if (node.is(SYMBOL_VALUE_TYPE)) {
            final var value = node.findString("value").orElse("");
            if (!value.equals("this") && !unit.state().find(value).isPresent() && !isDefaultJavaValue(value)) {

                final var state = unit.state();
                return new Err<>(new CompileError("Symbol not defined - " + state, new NodeContext(node)));
            }
        }

        return new Ok<>(unit);
    }
}
