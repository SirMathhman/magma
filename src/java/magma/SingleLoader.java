package magma;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.Stream;
import magma.app.compile.Node;
import magma.java.JavaList;

public record SingleLoader(JavaList<Node> instructions) implements Loader {
    @Override
    public Option<Stream<Tuple<Integer, Loader>>> stream() {
        return new None<>();
    }

    @Override
    public Option<JavaList<Node>> findInstructions() {
        return new Some<>(instructions);
    }
}
