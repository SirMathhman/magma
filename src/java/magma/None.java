package magma;

import java.util.function.Consumer;

public class None<T> implements Option<T> {
    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public void ifPresent(Consumer<T> consumer) {
    }
}
