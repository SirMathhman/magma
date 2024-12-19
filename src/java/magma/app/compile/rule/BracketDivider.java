package magma.app.compile.rule;

import magma.api.collect.List;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.FormattedError;

public class BracketDivider implements Divider {
    static Result<BracketState, FormattedError> processChar(BracketState state, char c) {
        final var appended = state.append(c);
        if (c == ';' && appended.isLevel()) {
            return new Ok<>(appended.advance());
        } else if (c == '}' && appended.isShallow()) {
            return appended.advance().exit();
        } else {
            if (c == '{') return new Ok<>(appended.enter());
            if (c == '}') return appended.exit();
            return new Ok<>(appended);
        }
    }

    @Override
    public Result<List<Input>, FormattedError> divide(Input input) {
        Result<BracketState, FormattedError> stateResult = new Ok<>(new BracketState());
        for (int i = 0; i < input.getInput().length(); i++) {
            var c = input.getInput().charAt(i);
            stateResult = stateResult.flatMapValue(inner -> processChar(inner, c));
        }

        return stateResult.flatMapValue(state -> state.complete(input));
    }

    @Override
    public StringBuilder concat(StringBuilder buffer, String slice) {
        return buffer.append(slice);
    }
}