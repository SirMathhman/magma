package magma.app.filter;

import java.util.function.Predicate;

public class SymbolFilter implements Predicate<String> {
    @Override
    public boolean test(String input1) {
        for (int i = 0; i < input1.length(); i++) {
            final var c = input1.charAt(i);
            if (Character.isLetter(c)) continue;
            return false;
        }
        return true;
    }
}
