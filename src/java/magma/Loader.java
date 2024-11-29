package magma;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.stream.Stream;
import magma.app.compile.Node;
import magma.java.JavaList;

public interface Loader {
    Option<Stream<Tuple<Integer, Loader>>> stream();

    Option<JavaList<Node>> findInstructions();
}
