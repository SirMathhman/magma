package magma.app.compile.pass;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.node.Input;
import magma.app.compile.node.MapNode;
import magma.app.compile.node.Node;
import magma.app.compile.node.NodeProperties;
import magma.app.compile.node.StringInput;
import magma.app.error.CompileError;
import magma.java.JavaList;
import magma.java.JavaOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RootPasser implements Passer {
    private static PassUnit<Node> resolveImport(PassUnit<Node> unit) {
        final var fileNamespace = unit.findNamespace();
        Node node1 = unit.value().retype("include");
        return unit.withValue(node1.nodeLists().map("namespace", list -> new JavaList<>(((Function<List<Node>, List<Node>>) namespaceNodes -> {
            var oldNamespace = namespaceNodes.stream()
                    .map(node -> JavaOptions.toNative(node.inputs().find("value").map(Input::unwrap)))
                    .flatMap(Optional::stream)
                    .toList();
            final var newNamespace = new ArrayList<String>();
            IntStream.range(0, fileNamespace.size()).forEach(_ -> {
                newNamespace.add("..");
            });
            newNamespace.addAll(oldNamespace);
            return newNamespace.stream()
                    .map(value -> {
                        Node node = new MapNode("segment");
                        NodeProperties<Input> inputNodeProperties = node.inputs();
                        return inputNodeProperties.with("value", new StringInput("value")).orElse(new MapNode());
                    })
                    .toList();
        }).apply(list.unwrap()))).orElse(node1));
    }

    private static List<Node> removeFunctionalImports(List<Node> children) {
        return children.stream()
                .flatMap(RootPasser::removeFunctionalImport)
                .toList();
    }

    private static Stream<Node> removeFunctionalImport(Node child) {
        if (!child.is("import")) return Stream.of(child);
        final var namespace = JavaOptions.toNative(child.nodeLists().find("namespace").map(JavaList::list))
                .orElse(Collections.emptyList())
                .stream()
                .map(node -> JavaOptions.toNative(node.inputs().find("value").map(Input::unwrap)))
                .flatMap(Optional::stream)
                .toList();

        if (!namespace.equals(List.of("java", "util", "function", "Function"))) return Stream.of(child);
        return Stream.empty();
    }

    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")).or(Passer.by("interface")), node -> node.retype("struct"))
                .or(() -> unit.filter(Passer.by("import")).map(RootPasser::resolveImport))
                .or(() -> unit.filterAndMapToValue(Passer.by("root"), node -> {
                    return node.nodeLists().map("children", list -> new JavaList<>(((Function<List<Node>, List<Node>>) RootPasser::removeFunctionalImports).apply(list.unwrap()))).orElse(node);
                }))
                .orElse(unit));
    }
}