package magma.app.compile.lang.common;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.HeadedStream;
import magma.api.stream.SingleHead;
import magma.api.stream.Stream;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.magma.Stateless;
import magma.app.compile.pass.Stateful;
import magma.java.JavaList;

import static magma.app.compile.lang.common.CommonLang.GROUP_CHILDREN;
import static magma.app.compile.lang.common.CommonLang.GROUP_TYPE;
import static magma.app.compile.lang.magma.MagmaLang.BLOCK_CHILDREN;
import static magma.app.compile.lang.magma.MagmaLang.BLOCK_TYPE;

public class FlattenBlock implements Stateless {
    @Override
    public Node afterPass(Node node) {
        final var children = node.findNodeList(BLOCK_CHILDREN).orElse(new JavaList<>());
        final var newChildren = children.stream()
                .flatMap(this::flatten)
                .foldLeft(new JavaList<Node>(), JavaList::add);
        return new MapNode("block").withNodeList(BLOCK_CHILDREN, newChildren);
    }

    private Stream<Node> flatten(Node child) {
        if (!child.is(GROUP_TYPE)) return new HeadedStream<>(new SingleHead<>(child));

        return child.findNodeList(GROUP_CHILDREN)
                .orElse(new JavaList<>())
                .stream();
    }
}
