package magma.compile.pass;

import magma.api.Tuple;
import magma.compile.Node;
import magma.compile.State;

import java.util.ArrayList;
import java.util.List;

import static magma.compile.lang.CLang.FUNCTION_NAME;
import static magma.compile.lang.CLang.FUNCTION_PARAMS;
import static magma.compile.lang.CLang.FUNCTION_TYPE;
import static magma.compile.lang.CLang.FUNCTION_TYPE_PROPERTY;
import static magma.compile.lang.CLang.FUNCTION_VALUE;
import static magma.compile.lang.CommonLang.LAMBDA_PARAMETERS;
import static magma.compile.lang.CommonLang.LAMBDA_TYPE;
import static magma.compile.lang.CommonLang.LAMBDA_VALUE;
import static magma.compile.lang.CommonLang.SYMBOL_TYPE;
import static magma.compile.lang.CommonLang.SYMBOL_VALUE;

public record Flattener0(Generator generator) implements Passer<State> {
    @Override
    public Tuple<State, Node> beforePass(State state, Node node) {
        if (node.is(LAMBDA_TYPE)) {
            final var params = node.findNodeList(LAMBDA_PARAMETERS)
                    .orElse(new ArrayList<>())
                    .stream()
                    .map(param -> {
                        final var name = param.findString(SYMBOL_VALUE).orElse("");
                        return new Node("definition")
                                .withString("name", name)
                                .withNode("type", createAutoType());
                    })
                    .toList();

            final var value = node.findNode(LAMBDA_VALUE).orElse(new Node());

            return new Tuple<>(state, new Node(FUNCTION_TYPE)
                    .withString(FUNCTION_NAME, generator.generateUniqueName("function"))
                    .withNode(FUNCTION_TYPE_PROPERTY, createAutoType())
                    .withNodeList(FUNCTION_PARAMS, params)
                    .withNode(FUNCTION_VALUE, new Node("block")
                            .withNode("value", new Node("group")
                                    .withNodeList("children", List.of(
                                            new Node("return")
                                                    .withNode("value", value)
                                    )))));
        }
        return new Tuple<>(state, node);
    }

    private static Node createAutoType() {
        return new Node(SYMBOL_TYPE).withString(SYMBOL_VALUE, "auto");
    }
}
