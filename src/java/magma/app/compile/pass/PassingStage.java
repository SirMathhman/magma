package magma.app.compile.pass;

import magma.app.compile.Node;

public interface PassingStage {
    Node pass(Node node);
}
