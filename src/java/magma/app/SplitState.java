package magma.app;

import magma.api.Tuple;
import magma.api.option.Option;

import java.util.List;

public interface SplitState {
    Tuple<SplitState, Character> appendNext(Character c);

    SplitState enter();

    SplitState append(char c);

    SplitState exit();

    SplitState advance();

    boolean isShallow();

    boolean isLevel();

    List<String> asList();

    Option<Tuple<SplitState, Character>> pop();
}
