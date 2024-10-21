package magma.app.compile.rule;

import java.util.List;

public record ListFilter(List<String> values) implements Filter {
    @Override
    public boolean filter(String input) {
        return values().contains(input);
    }
}