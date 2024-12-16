package magma.api.stream;

import magma.api.option.Option;

public record ConcatHead<T>(Head<T> before, Head<T> after) implements Head<T> {
    @Override
    public Option<T> next() {
        return before.next().or(after::next);
    }
}
