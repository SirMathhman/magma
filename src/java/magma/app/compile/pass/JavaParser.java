package magma.app.compile.pass;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.node.Input;
import magma.app.compile.node.MapNode;
import magma.app.compile.node.Node;
import magma.app.compile.node.NodeProperties;
import magma.app.compile.node.StringInput;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
import magma.java.JavaList;
import magma.java.JavaOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static magma.app.lang.CommonLang.LAMBDA_PARAMETERS;
import static magma.app.lang.CommonLang.SYMBOL_VALUE_TYPE;
import static magma.java.JavaLang.isDefaultJavaValue;

public class JavaParser implements Passer {
    private static Node createDefinition(String parameter) {
        Node node = new MapNode("definition");
        NodeProperties<Input> inputNodeProperties = node.inputs();
        return inputNodeProperties.with("name", new StringInput("name")).orElse(new MapNode());
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
            final Optional<Node> parameterNode = JavaOptions.toNative(node.nodes().find(LAMBDA_PARAMETERS));
            final var parameterNodeLists = JavaOptions.toNative(node.nodeLists().find(LAMBDA_PARAMETERS).map(JavaList::list));
            if (parameterNode.isPresent()) {
                Node node1 = parameterNode
                        .orElse(new MapNode());
                final var parameter = JavaOptions.toNative(node1.inputs().find("value").map(Input::unwrap))
                        .orElse("");

                definitions = List.of(createDefinition(parameter));
            } else if (parameterNodeLists.isPresent()) {
                definitions = parameterNodeLists.orElse(new ArrayList<>())
                        .stream()
                        .map(child -> JavaOptions.toNative(child.inputs().find("value").map(Input::unwrap)))
                        .flatMap(Optional::stream)
                        .map(JavaParser::createDefinition)
                        .toList();
            } else {
                definitions = Collections.emptyList();
            }

            return new Ok<>(unit.enter().define(definitions));
        }

        if (node.is("class") || node.is("record") || node.is("interface")) {
            final var name = JavaOptions.toNative(node.inputs().find("name").map(Input::unwrap)).orElse("");
            final var value = JavaOptions.toNative(node.nodes().find("value")).orElse(new MapNode());
            final var children = JavaOptions.toNative(value.nodeLists().find("children").map(JavaList::list)).orElse(new ArrayList<>());

            final var methodDefinitions = children.stream()
                    .filter(child -> child.is("method"))
                    .map(method -> JavaOptions.toNative(method.nodes().find("definition")))
                    .flatMap(Optional::stream)
                    .toList();

            return new Ok<>(unit.enter()
                    .define(List.of(createDefinition(name)))
                    .define(methodDefinitions));
        }

        if (node.is("method")) {
            final var params = JavaOptions.toNative(node.nodeLists().find("params").map(JavaList::list)).orElse(Collections.emptyList());
            return new Ok<>(unit.enter().define(params));
        }

        if (node.is("import")) {
            Node node1 = JavaOptions.toNative(node.nodeLists().find("namespace").map(JavaList::list))
                    .orElse(Collections.emptyList())
                    .getLast();
            final var value = JavaOptions.toNative(node1.inputs().find("value").map(Input::unwrap))
                    .orElse("");

            return new Ok<>(unit.define(List.of(createDefinition(value))));
        }

        if (node.is("definition")) {
            return new Ok<>(unit.define(List.of(node)));
        }

        if (node.is(SYMBOL_VALUE_TYPE)) {
            final var value = JavaOptions.toNative(node.inputs().find("value").map(Input::unwrap)).orElse("");
            if (!value.equals("this") && !unit.state().find(value).isPresent() && !isDefaultJavaValue(value)) {

                final var state = unit.state();
                return new Err<>(new CompileError("Symbol not defined - " + state, new NodeContext(node)));
            }
        }

        return new Ok<>(unit);
    }
}
