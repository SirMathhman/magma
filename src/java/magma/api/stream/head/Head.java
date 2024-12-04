package magma.api.stream.head;

import magma.api.option.Option;

public interface Head<T> {
    Option<T> next();
}
