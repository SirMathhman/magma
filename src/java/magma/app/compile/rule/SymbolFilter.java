package magma.app.compile.rule;

public class SymbolFilter implements Filter {
    @Override
    public boolean test(String input) {
        for (int i = 0; i < input.length(); i++) {
            if(!Character.isLetter(input.charAt(i))) return false;
        }

        return true;
    }
}
