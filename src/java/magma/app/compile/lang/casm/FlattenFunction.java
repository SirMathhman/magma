package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.pass.Stateful;
import magma.java.JavaList;

public class FlattenFunction implements Stateful {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node) {
        if (!node.is("function")) return new None<>();

        final var name = node.findString("name").orElse("");
        final var value = node.findNode("value").orElse(new MapNode());
        final var children = value.findNodeList("children").orElse(new JavaList<Node>());
        return new Some<>(new Ok<>(new Tuple<>(state, CASMLang.label(name, children.list()))));
    }
}
