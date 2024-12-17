package magma.app.compile.rule;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.app.error.CompileError;

public interface Splitter {
    String merge(String leftValue, String rightValue);

    CompileError createError(String input);

    Option<Tuple<String, String>> split(String input);
}
