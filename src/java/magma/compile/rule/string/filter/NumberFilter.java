package magma.compile.rule.string.filter;

public class NumberFilter implements Filter {
    @Override
    public String createErrorMessage() {
        return "Not a number";
    }

    @Override
    public boolean test(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
