package magma.app.compile.lang;

import magma.app.compile.rule.Filter;

public class NumberFilter implements Filter {
    private static boolean allDigits(String input) {
        final var length = input.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isDigit(input.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean filter(String input) {
        return input.startsWith("-")
                ? allDigits(input.substring(1))
                : allDigits(input);
    }
}
