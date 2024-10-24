package magma.java;

import magma.api.list.ListHead;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.HeadedStream;
import magma.api.stream.Stream;

import java.util.List;

public record JavaList<T>(List<T> hidden) implements magma.api.list.List<T> {
    @Override
    public Stream<T> stream() {
        return new HeadedStream<T>(new ListHead<T>(this));
    }

    @Override
    public int size() {
        return hidden.size();
    }

    @Override
    public Option<T> get(int index) {
        if(index >= 0 && index < hidden.size()) {
            return new Some<>(hidden.get(index));
        } else {
            return new None<>();
        }
    }
}
