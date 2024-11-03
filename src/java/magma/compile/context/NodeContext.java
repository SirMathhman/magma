package magma.compile.context;

import magma.compile.Context;
import magma.compile.Node;

public record NodeContext(Node node) implements Context {
}
