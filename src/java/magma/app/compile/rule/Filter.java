package magma.app.compile.rule;

public interface Filter {
    boolean validate(String input);

    String createMessage();
}
