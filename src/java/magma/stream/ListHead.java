package magma.stream;

import java.util.List;
import java.util.Optional;

public class ListHead<T> implements Head<T> {
    private final List<T> list;
    private int counter = 0;

    public ListHead(List<T> list) {
        this.list = list;
    }

    @Override
    public Optional<T> next() {
        if (counter >= list.size()) return Optional.empty();

        var element = list.get(counter);
        counter++;
        return Optional.of(element);
    }
}
