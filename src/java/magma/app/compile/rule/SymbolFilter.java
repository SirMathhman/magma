package magma.app.compile.rule;

public class SymbolFilter implements Filter {
    private static boolean isValid(char c, int index) {
        return Character.isLetter(c) || c == '_' || c == '*' || (index != 0 && Character.isDigit(c));
    }

    @Override
    public boolean filter(String input) {
        int i = 0;
        while (i < input.length()) {
            final var c = input.charAt(i);
            if (!isValid(c, i)) return false;

            i++;
        }

        return true;
    }
}
