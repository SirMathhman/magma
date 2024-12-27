package magma.compile.rule.string.filter;

public interface Filter {
    String createErrorMessage();

    boolean test(String input);
}
