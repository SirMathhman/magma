package magma.app.compile.rule;

import magma.api.Tuple;
import magma.api.collect.List;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.FormattedError;

import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BracketDivider implements Divider {
    static Option<Result<BracketState, FormattedError>> processChar(BracketState state) {
        return state.appendAndPop().map(tuple -> {
            final var appended = tuple.left();
            final var c = tuple.right();

            return processChar(c, appended).orElseGet(() -> {
                if (c == ';' && appended.isLevel()) {
                    return new Ok<>(appended.advance());
                } else if (c == '}' && appended.isShallow()) {
                    return appended.advance().exit();
                } else {
                    if (c == '{') return new Ok<>(appended.enter());
                    if (c == '}') return appended.exit();
                    return new Ok<>(appended);
                }
            });
        });
    }

    private static Option<Result<BracketState, FormattedError>> processChar(Character c, BracketState appended) {
        if (c != '\'') return new None<>();

        return appended.appendAndPop()
                .flatMap(BracketDivider::processEscapedChar)
                .flatMap(maybeEscape -> maybeEscape.appendAndPop().map(Tuple::left))
                .map(Ok::new);
    }

    private static Option<BracketState> processEscapedChar(Tuple<BracketState, Character> tuple0) {
        final var withMaybeEscape = tuple0.left();
        final var maybeEscape = tuple0.right();

        if (maybeEscape == '\\') {
            return withMaybeEscape.appendAndPop().map(Tuple::left);
        } else {
            return new Some<>(withMaybeEscape);
        }
    }

    private static boolean shouldContinue(Result<BracketState, FormattedError> stateResult) {
        return stateResult.mapValue(BracketState::hasMore)
                .findValue()
                .orElseGet(() -> false);
    }

    @Override
    public Result<List<Input>, FormattedError> divide(Input input) {
        final var unwrapped = input.getInput();
        final var queue = IntStream.range(0, unwrapped.length())
                .mapToObj(unwrapped::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        Result<BracketState, FormattedError> stateResult = new Ok<>(new BracketState(queue));
        while (shouldContinue(stateResult)) {
            final var option = stateResult
                    .mapValue(BracketDivider::processChar)
                    .<Option<Result<BracketState, FormattedError>>>match(value -> value, err -> new Some<>(new Err<>(err)));

            if (option.isPresent()) {
                stateResult = option.orElseNull();
            } else {
                break;
            }
        }

        return stateResult.flatMapValue(state -> state.complete(input));
    }

    @Override
    public StringBuilder concat(StringBuilder buffer, String slice) {
        return buffer.append(slice);
    }
}