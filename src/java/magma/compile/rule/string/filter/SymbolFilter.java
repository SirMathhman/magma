package magma.compile.rule.string.filter;

public class SymbolFilter implements Filter {
    public SymbolFilter() {
    }

    @Override
    public String createErrorMessage() {
        return "Not a symbol";
    }

    @Override
    public boolean test(String input) {
        if (input.isEmpty()) return false;

        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            if (Character.isLetter(c) || ((i != 0) && Character.isDigit(c))) {
                continue;
            }
            return false;
        }

        return true;
    }
}