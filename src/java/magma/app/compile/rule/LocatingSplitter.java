package magma.app.compile.rule;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.app.error.CompileError;
import magma.app.error.InputContext;

import static magma.app.compile.rule.FirstLocator.First;
import static magma.app.compile.rule.LastLocator.Last;

public final class LocatingSplitter implements Splitter {
    private final String slice;
    private final Locator locator;

    private LocatingSplitter(Locator locator, String slice) {
        this.slice = slice;
        this.locator = locator;
    }

    public static Splitter LocateFirst(String slice) {
        return Locate(First, slice);
    }

    public static Splitter Locate(Locator locator, String slice) {
        return new LocatingSplitter(locator, slice);
    }

    public static Splitter LocateLast(String slice) {
        return Locate(Last, slice);
    }

    @Override
    public String merge(String leftValue, String rightValue) {
        return leftValue + slice + rightValue;
    }

    @Override
    public CompileError createError(Input input) {
        final var format = "Infix '%s' not present";
        final var message = format.formatted(slice);
        final var context = new InputContext(new Input(input.getInput()));
        return new CompileError(message, context);
    }

    private Option<Tuple<String, String>> split0(Input input) {
        return locator.locate(input.getInput(), slice).map(index -> {
            final var left = input.getInput().substring(0, index);
            final var right = input.getInput().substring(index + slice.length());
            return new Tuple<>(left, right);
        });
    }

    @Override
    public Option<Tuple<Input, Input>> split(Input input) {
        return split0(input).map(tuple -> tuple.mapLeft(Input::new).mapRight(Input::new));
    }
}