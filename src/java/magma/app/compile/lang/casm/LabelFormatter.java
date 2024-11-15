package magma.app.compile.lang.casm;

import magma.app.compile.Node;

import static magma.app.compile.lang.CASMLang.GROUP_AFTER;
import static magma.app.compile.lang.CASMLang.GROUP_AFTER_NAME;

public class LabelFormatter implements Modifier {
    @Override
    public Node modify(Node node) {
        return node.withString(GROUP_AFTER_NAME, " ");
    }
}
