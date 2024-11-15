package magma.app.compile.lang.casm;

import magma.app.compile.Node;

public interface Modifier {
    Node modify(Node node);
}
