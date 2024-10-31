package magma.core.stream;

import magma.core.option.Option;

public interface Head<T> {
    Option<T> next();
}
