package magma.app.compile.lang.common;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.HeadedStream;
import magma.api.stream.SingleHead;
import magma.api.stream.Stream;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.magma.Stateless;
import magma.java.JavaList;

import static magma.app.compile.lang.common.CommonLang.GROUP_CHILDREN;
import static magma.app.compile.lang.common.CommonLang.GROUP_TYPE;

public class FlattenGroup implements Stateless {
    @Override
    public Node afterPass(Node node) {
        final var children = node.findNodeList(GROUP_CHILDREN).orElse(new JavaList<>());
        return CommonLang.asGroup(children.stream()
                .flatMap(this::flatten)
                .foldLeft(new JavaList<Node>(), JavaList::add));
    }

    private Stream<Node> flatten(Node child) {
        if (!child.is(GROUP_TYPE)) return new HeadedStream<>(new SingleHead<>(child));

        return child.findNodeList(GROUP_CHILDREN)
                .orElse(new JavaList<>())
                .stream();
    }
}
