import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import java.util.function.Predicate;
struct Collectors {
	Option<String>> joining(String infix){return new Collector<>() {
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
	Boolean> allMatch(Predicate<T> predicate){return new Collector<>() {
            @Override
            public Boolean createInitial() {
                return true;
            }

            @Override
            public Boolean fold(Boolean current, T next) {
                return current && predicate.test(next);
            }
        };
	}
}