package magma.app.compile.pass;

import magma.app.compile.Node;
import magma.app.compile.lang.CommonLang;

import java.util.Optional;

import static magma.app.compile.lang.CommonLang.IMPORT_TYPE;
import static magma.app.compile.lang.JavaLang.IMPORT_STATIC_TYPE;

public class ImportPasser {
    public static Optional<Node> pass(Node node) {
        if (node.is(IMPORT_TYPE)) {
            return Optional.of(attachPadding(node));
        }
        if (node.is(IMPORT_STATIC_TYPE)) {
            return Optional.of(attachPadding(node.retype(IMPORT_TYPE)));
        }
        return Optional.empty();

    }

    private static Node attachPadding(Node node) {
        return node.withString(CommonLang.AFTER_IMPORT, "\n");
    }
}