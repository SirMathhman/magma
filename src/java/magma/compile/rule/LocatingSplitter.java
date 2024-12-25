package magma.compile.rule;

import magma.api.Tuple;
import magma.compile.error.CompileError;
import magma.compile.error.StringContext;
import magma.compile.rule.locate.Locator;

import java.util.List;
import java.util.stream.Stream;

public final class LocatingSplitter implements Splitter {
    private final String infix;
    private final Locator locator;

    public LocatingSplitter(String infix, Locator locator) {
        this.infix = infix;
        this.locator = locator;
    }

    @Override
    public CompileError createError(String input, List<CompileError> errors) {
        final var format = "Infix '%s' not present";
        final var message = format.formatted(infix);
        final var context = new StringContext(input);
        return new CompileError(message, context, errors);
    }

    @Override
    public Stream<Tuple<String, String>> split(String input) {
        return locator.locate(input, infix)
                .map(index -> splitAtIndex(input, index));
    }

    private Tuple<String, String> splitAtIndex(String input, int index) {
        final var leftSlice = input.substring(0, index);
        final var rightSlice = input.substring(index + infix.length());
        return new Tuple<>(leftSlice, rightSlice);
    }

    @Override
    public String merge(String left, String right) {
        return left + infix + right;
    }
}