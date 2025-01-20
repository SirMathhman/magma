package magma.app.rule.filter;

import java.util.function.Predicate;
import java.util.stream.IntStream;

public class SymbolFilter implements Predicate<String> {
    @Override
    public boolean test(String input) {
        return IntStream.range(0, input.length()).allMatch(index -> {
            final var c = input.charAt(index);
            return Character.isLetter(c) || (index != 0 && Character.isDigit(c));
        });
    }
}
