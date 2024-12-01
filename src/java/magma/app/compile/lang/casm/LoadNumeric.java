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
import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.pass.Passer;
import magma.java.JavaList;

import static magma.app.compile.lang.casm.CASMLang.instruct;
import static magma.app.compile.lang.common.CommonLang.NUMERIC_VALUE;

public class LoadNumeric implements Passer {
    @Override
    public Option<Result<Node, CompileError>> afterNode(Node node) {
        if (!node.is("load")) return new None<>();

        final var value0 = node.findNode("value").orElse(new MapNode());
        final var value = value0.findInt(NUMERIC_VALUE).orElse(0);
        final var instructions = new JavaList<Node>()
                .add(instruct(Operator.LoadFromValue, value));

        final var loader = CommonLang.toGroup(instructions);
        return new Some<>(new Ok<>(loader));
    }
}
