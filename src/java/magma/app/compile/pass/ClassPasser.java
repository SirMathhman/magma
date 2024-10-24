package magma.app.compile.pass;

import magma.app.compile.Node;

import java.util.Optional;

import static magma.app.compile.lang.JavaLang.CLASS_TYPE;
import static magma.app.compile.lang.MagmaLang.FUNCTION;

public class ClassPasser {
    public static Optional<Node> pass(Node node) {
        if (node.is(CLASS_TYPE)) {
            return Optional.of(node.retype(FUNCTION));
        } else {
            return Optional.empty();
        }
    }
}
