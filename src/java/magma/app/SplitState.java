package magma.app;

import magma.api.Tuple;
import magma.api.option.Option;

import java.util.List;

public interface SplitState {
    SplitState enter();

    SplitState next(char c);

    SplitState exit();

    SplitState advance();

    boolean isShallow();

    boolean isLevel();

    List<String> asList();

    Option<Tuple<Character, SplitState>> pop();
}
