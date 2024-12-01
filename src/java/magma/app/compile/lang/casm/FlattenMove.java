package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Compiler;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.casm.assemble.Operator;
import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.pass.Passer;
import magma.java.JavaList;

import java.util.Map;

public class FlattenMove implements Passer {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node) {
        if(!node.is("move")) return new None<>();

        final var length = node.findInt("length").orElse(0);
        if(length == 1) {
            final var source = node.findNode("source").orElse(new MapNode());
            final var destination = node.findNode("destination")
                    .orElse(new MapNode())
                    .findString("value")
                    .orElse("");

            final var value = source.findInt("value").orElse(0);
            final var load = CASMLang.instruct(Operator.LoadFromValue, value);
            final var move = new MapNode("goto").withString("location", destination)
                    .withNodeList("children", new JavaList<Node>()
                            .add(CASMLang.instruct(Operator.StoreDirectly)));

            final var group = CommonLang.toGroup(new JavaList<Node>()
                    .add(load)
                    .add(move));

            return new Some<>(new Ok<>(new Tuple<>(state, group)));
        } else {
            throw new UnsupportedOperationException("TODO");
        }
    }
}
