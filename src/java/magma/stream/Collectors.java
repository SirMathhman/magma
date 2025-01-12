package magma.stream;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

public class Collectors {
    public static Collector<String, Option<String>> joining(String infix) {
        return new Collector<>() {
            @Override
            public Option<String> createInitial() {
                return new None<>();
            }

            @Override
            public Option<String> fold(Option<String> current, String next) {
                if (current.isEmpty()) return new Some<>(next);
                return current.map(inner -> inner + infix + next);
            }
        };
    }
}
