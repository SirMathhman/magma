package magma.app.compile.pass;

import magma.app.compile.MapNode;
import magma.app.compile.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static magma.app.compile.lang.CommonLang.*;
import static magma.app.compile.lang.MagmaLang.FUNCTION_TYPE;

public class AccessModifier implements Passer {
    @Override
    public Optional<Node> beforePass(Node node) {
        if (!node.is(FUNCTION_TYPE)) return Optional.empty();

        final var oldModifiers = node.findNodeList(MODIFIERS)
                .orElse(Collections.emptyList())
                .stream()
                .map(modifier -> modifier.findString(MODIFIER_VALUE))
                .flatMap(Optional::stream)
                .toList();

        var newModifiers = new ArrayList<String>();
        if (oldModifiers.contains("public")) {
            newModifiers.add("export");
        }

        if(newModifiers.isEmpty()) return Optional.of(node);

        final var wrapped = newModifiers.stream()
                .map(modifier -> new MapNode().retype(MODIFIER_TYPE).withString(MODIFIER_VALUE, modifier))
                .toList();

        return Optional.of(node.withNodeList(MODIFIERS, wrapped));
    }
}
