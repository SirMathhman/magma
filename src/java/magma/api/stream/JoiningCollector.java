package magma.api.stream;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

public class JoiningCollector implements Collector<String, Option<String>> {
    private final String slice;

    public JoiningCollector(String slice) {
        this.slice = slice;
    }

    @Override
    public Option<String> createInitial() {
        return new None<>();
    }

    @Override
    public Option<String> fold(Option<String> current, String element) {
        if (current.isEmpty()) return new Some<>(element);
        return current.map(inner -> inner + this.slice + element);
    }
}
