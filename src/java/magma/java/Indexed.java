package magma.java;

import magma.core.option.Option;

public interface Indexed<T> {
    int size();

    Option<T> get(int index);
}
