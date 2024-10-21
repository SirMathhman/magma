package magma.app.compile.rule;

public class SymbolFilter implements Filter {
    @Override
    public boolean filter(String input) {
        for (int i = 0; i < input.length(); i++) {
            final var c = input.charAt(i);
            if (Character.isLetter(c) || (i != 0 && Character.isDigit(c))) {
                continue;
            }
            return false;
        }

        return true;
    }
}
