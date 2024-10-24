package magma.app.compile.pass;

import magma.app.compile.Node;
import magma.app.compile.lang.CommonLang;

import java.util.Optional;

public class ImportPasser {
    public static Optional<Node> pass(Node node) {
        if (!node.is(CommonLang.IMPORT)) return Optional.empty();

        return Optional.of(node.withString(CommonLang.AFTER_IMPORT, "\n"));
    }
}