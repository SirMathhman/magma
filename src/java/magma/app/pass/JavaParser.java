package magma.app.pass;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;

import java.util.Collections;
import java.util.List;

import static magma.app.lang.CommonLang.LAMBDA_PARAMETERS;
import static magma.app.lang.CommonLang.SYMBOL_VALUE_TYPE;

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
        return new Ok<>(unit);
    }

    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        final var node = unit.value();
        if (node.is("lambda")) {
            final var parameter = node.findNode(LAMBDA_PARAMETERS)
                    .orElse(new MapNode())
                    .findString("value")
                    .orElse("");

            return new Ok<>(unit.enter().push(List.of(createDefinition(parameter))));
        }

        if (node.is("method")) {
            final var params = node.findNodeList("params").orElse(Collections.emptyList());
            return new Ok<>(unit.enter().push(params));
        }

        if (node.is("import")) {
            final var value = node.findNodeList("namespace")
                    .orElse(Collections.emptyList())
                    .getLast()
                    .findString("value")
                    .orElse("");

            return new Ok<>(unit.push(List.of(createDefinition(value))));
        }

        if (node.is("definition")) {
            return new Ok<>(unit.push(List.of(node)));
        }

        if (node.is(SYMBOL_VALUE_TYPE)) {
            final var value = node.findString("value").orElse("");
            if (!value.equals("this") && !unit.state().find(value).isPresent()) {
                return new Err<>(new CompileError("Symbol not defined", new NodeContext(node)));
            }
        }

        return new Ok<>(unit);
    }
}
