package magma.compile.rule.split;

import magma.api.Tuple;
import magma.compile.error.CompileError;

import java.util.List;
import java.util.stream.Stream;

public interface Splitter {
    CompileError createError(String input, List<CompileError> errors);

    Stream<Tuple<String, String>> split(String input);

    String merge(String left, String right);
}
