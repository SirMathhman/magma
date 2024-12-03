package magma.app.compile.rule;

public class EqualsFilter implements Filter {
    private final String value;

    public EqualsFilter(String value) {
        this.value = value;
    }

    @Override
    public boolean test(String input) {
        return value.equals(input);
    }
}
