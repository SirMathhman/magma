package magma.core.stream;

import magma.core.option.None;
import magma.core.option.Option;

public class EmptyHead<T> implements Head<T> {
    @Override
    public Option<T> next() {
        return new None<>();
    }
}
