package magma;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.Stream;
import magma.app.compile.Node;
import magma.java.JavaList;

public record MultipleLoader(JavaList<Loader> loads) implements Loader {
    @Override
    public Option<Stream<Tuple<Integer, Loader>>> stream() {
        return new Some<>(loads.streamWithIndices());
    }

    @Override
    public Option<JavaList<Node>> findInstructions() {
        return new None<>();
    }
}
