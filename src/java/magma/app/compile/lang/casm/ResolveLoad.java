package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.lang.casm.assemble.Operator;
import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.pass.Stateful;
import magma.java.JavaList;

import static magma.app.compile.Compiler.SPILL0;
import static magma.app.compile.Compiler.STACK_POINTER;
import static magma.app.compile.lang.casm.CASMLang.instruct;

public class ResolveLoad implements Stateful {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node) {
        if (!node.is("load")) return new None<>();

        final var value = node.findNode("value").orElse(new MapNode());

        if (value.is("numeric-value")) {
            final var intValue = value.findInt("value").orElse(0);
            var result = instruct(Operator.LoadFromValue, intValue);
            return new Some<>(new Ok<>(new Tuple<>(state, result)));
        }

        if (value.is("address")) {
            final var offset = value.findInt("offset").orElse(0);
            var result = CommonLang.asGroup(new JavaList<Node>()
                    .add(new MapNode("move-stack-pointer").withInt("offset", offset))
                    .add(instruct(Operator.LoadIndirectly, STACK_POINTER))
                    .add(new MapNode("move-stack-pointer").withInt("offset", -offset)));
            return new Some<>(new Ok<>(new Tuple<>(state, result)));
        }

        if (value.is("add")) {
            final var left = value.findNode("left").orElse(new MapNode());
            final var right = value.findNode("right").orElse(new MapNode());

            var group = CommonLang.asGroup(new JavaList<Node>()
                    .add(new MapNode("load").withNode("value", left))
                    .add(instruct(Operator.StoreDirectly, SPILL0))
                    .add(new MapNode("load").withNode("value", right))
                    .add(instruct(Operator.AddFromAddress, SPILL0)));

            return new Some<>(new Ok<>(new Tuple<>(state, group)));
        }

        if (value.is("subtract")) {
            final var left = value.findNode("left").orElse(new MapNode());
            final var right = value.findNode("right").orElse(new MapNode());

            var group = CommonLang.asGroup(new JavaList<Node>()
                    .add(new MapNode("load").withNode("value", left))
                    .add(instruct(Operator.StoreDirectly, SPILL0))
                    .add(new MapNode("load").withNode("value", right))
                    .add(instruct(Operator.SubtractFromAddress, SPILL0)));

            return new Some<>(new Ok<>(new Tuple<>(state, group)));
        }

        if(value.is("reference")) {
            final var inner = value.findNode("value").orElse(new MapNode());

            if (inner.is("address")) {
                final var offset = inner.findInt("offset").orElse(0);
                var result = CommonLang.asGroup(new JavaList<Node>()
                        .add(new MapNode("move-stack-pointer").withInt("offset", offset))
                        .add(instruct(Operator.LoadDirectly, STACK_POINTER))
                        .add(new MapNode("move-stack-pointer").withInt("offset", -offset)));
                return new Some<>(new Ok<>(new Tuple<>(state, result)));
            }
        }

        if(value.is("dereference")) {
            final var inner = value.findNode("value").orElse(new MapNode());

            if (inner.is("address")) {
                final var offset = value.findInt("offset").orElse(0);
                var result = CommonLang.asGroup(new JavaList<Node>()
                        .add(new MapNode("move-stack-pointer").withInt("offset", offset))
                        .add(instruct(Operator.LoadIndirectly, STACK_POINTER))
                        .add(new MapNode("move-stack-pointer").withInt("offset", -offset)));
                return new Some<>(new Ok<>(new Tuple<>(state, result)));
            }
        }

        return new Some<>(new Err<>(new CompileError("Unknown value to load", new NodeContext(node))));
    }
}
