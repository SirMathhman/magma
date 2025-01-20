package magma.api.stream;

import java.util.Optional;

public class EmptyHead<T> implements Head<T> {
    @Override
    public Optional<T> next() {
        return Optional.empty();
    }
}
