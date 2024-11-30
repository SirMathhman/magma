package magma.app.compile.lang.casm;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.casm.assemble.Operator;
import magma.app.compile.pass.Passer;
import magma.java.JavaList;

import static magma.app.compile.lang.casm.CASMLang.instruct;
import static magma.app.compile.lang.magma.CommonLang.NUMERIC_VALUE_TYPE;
import static magma.app.compile.lang.magma.CommonLang.NUMERIC_VALUE;

public class LoadNumeric implements Passer {
    @Override
    public Option<Result<Node, CompileError>> afterNode(Node node) {
        if (!node.is(NUMERIC_VALUE_TYPE)) return new None<>();

        final var value = node.findInt(NUMERIC_VALUE).orElse(0);
        final var instructions = new JavaList<Node>()
                .add(instruct(Operator.LoadFromValue, value));

        final var loader = new MapNode("loader").withNodeList("instructions", instructions);
        return new Some<>(new Ok<>(loader));
    }
}
