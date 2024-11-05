package magma.app.compile.rule;

import magma.java.JavaStreams;

public class SymbolFilter implements Filter {
    @Override
    public boolean validate(String input) {
        return JavaStreams.fromString(input).allMatch(Character::isLetter);
    }

    @Override
    public String createMessage() {
        return "Not a symbol";
    }
}
