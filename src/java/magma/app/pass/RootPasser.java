package magma.app.pass;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.CompileError;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.IntStream;

public class RootPasser implements Passer {
    private static PassUnit<Node> resolveImport(PassUnit<Node> unit) {
        final var fileNamespace = unit.findNamespace();
        return unit.withValue(unit.value().retype("include").mapNodeList("namespace", namespaceNodes -> {
            var oldNamespace = namespaceNodes.stream()
                    .map(node -> node.findString("value"))
                    .flatMap(Optional::stream)
                    .toList();
            final var newNamespace = new ArrayList<String>();
            IntStream.range(0, fileNamespace.size()).forEach(_ -> {
                newNamespace.add("..");
            });
            newNamespace.addAll(oldNamespace);
            return newNamespace.stream()
                    .map(value -> new MapNode("segment").withString("value", value))
                    .toList();
        }));
    }

    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")).or(Passer.by("interface")), node -> node.retype("struct"))
                .or(() -> unit.filter(Passer.by("import")).map(RootPasser::resolveImport))
                .orElse(unit));
    }
}