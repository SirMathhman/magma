package magma.app.compile.format;

import magma.app.compile.Node;
import magma.app.compile.lang.casm.Modifier;

import static magma.app.compile.lang.CASMLang.GROUP_AFTER;
import static magma.app.compile.lang.CASMLang.GROUP_AFTER_NAME;

public class SectionFormatter implements Modifier {
    @Override
    public Node modify(Node node) {
        return node.withString(GROUP_AFTER_NAME, " ")
                .withString(GROUP_AFTER, "\n");
    }
}
