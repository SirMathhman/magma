package magma.stream;

import magma.option.Option;

public interface Head<T> {
    Option<T> next();
}
