package magma.compile.pass;

import magma.api.Tuple;
import magma.compile.Node;
import magma.compile.State;

import java.util.List;

import static magma.compile.lang.CommonLang.DATA_ACCESS;
import static magma.compile.lang.CommonLang.FUNCTION_ACCESS;
import static magma.compile.lang.CommonLang.INVOCATION_ARGUMENTS;
import static magma.compile.lang.CommonLang.INVOCATION_CALLER;
import static magma.compile.lang.CommonLang.INVOCATION_VALUE;
import static magma.compile.lang.CommonLang.LAMBDA_PARAMETERS;
import static magma.compile.lang.CommonLang.LAMBDA_TYPE;
import static magma.compile.lang.CommonLang.LAMBDA_VALUE;
import static magma.compile.lang.CommonLang.SYMBOL_TYPE;
import static magma.compile.lang.CommonLang.SYMBOL_VALUE;

public class Flattener implements Passer<State> {
    private int counter = -1;

    private String generateUniqueName(String category) {
        counter++;
        return "__" + category + counter + "__";
    }

    @Override
    public Tuple<State, Node> afterPass(State state, Node node) {
        if (node.is(FUNCTION_ACCESS)) {
            final var symbol = new Node(SYMBOL_TYPE).withString(SYMBOL_VALUE, generateUniqueName("lambda"));
            final var lambda = new Node(LAMBDA_TYPE)
                    .withNodeList(LAMBDA_PARAMETERS, List.of(symbol))
                    .withNode(LAMBDA_VALUE, new Node(INVOCATION_VALUE)
                            .withNode(INVOCATION_CALLER, node.retype(DATA_ACCESS))
                            .withNodeList(INVOCATION_ARGUMENTS, List.of(symbol)));

            return new Tuple<>(state, lambda);
        }

        return new Tuple<>(state, node);
    }
}
