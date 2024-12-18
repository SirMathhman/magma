package magma.app.compile.rule;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.app.error.CompileError;

public interface Splitter {
    String merge(String leftValue, String rightValue);

    CompileError createError(Input input);

    Option<Tuple<Input, Input>> split(Input input);
}
