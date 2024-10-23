package magma.app.compile.lang;

import magma.app.compile.rule.Filter;

import java.util.stream.IntStream;

public class NumberFilter implements Filter {
    private static boolean allDigits(String input) {
        return IntStream.range(0, input.length())
                .allMatch(i -> Character.isDigit(input.charAt(i)));
    }

    @Override
    public boolean filter(String input) {
        return input.startsWith("-")
                ? allDigits(input.substring(1))
                : allDigits(input);
    }
}
