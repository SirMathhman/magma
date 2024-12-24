package magma.compile.rule;

import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.compile.error.CompileError;
import magma.compile.error.StringContext;

public final class InfixSplitter implements Splitter {
    private final String infix;
    private final Locator locator;

    public InfixSplitter(String infix, Locator locator) {
        this.infix = infix;
        this.locator = locator;
    }

    CompileError createError(String input) {
        final var format = "Infix '%s' not present";
        final var message = format.formatted(infix);
        final var context = new StringContext(input);
        return new CompileError(message, context);
    }

    @Override
    public Result<Tuple<String, String>, CompileError> split(String input) {
        return locator.locate(input, infix)
                .map(index -> splitAtIndex(input, index))
                .<Result<Tuple<String, String>, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(createError(input)));
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