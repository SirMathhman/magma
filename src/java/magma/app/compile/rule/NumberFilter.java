package magma.app.compile.rule;

import magma.java.JavaStreams;

public class NumberFilter implements Filter {
    @Override
    public boolean validate(String input) {
        return JavaStreams.fromString(input).allMatch(Character::isDigit);
    }

    @Override
    public String createMessage() {
        return "Not a number";
    }
}
