package magma.app.compile.lang;

import magma.app.compile.rule.Filter;

public class NumberFilter implements Filter {
    @Override
    public boolean filter(String input) {
        final var length = input.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isDigit(input.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
