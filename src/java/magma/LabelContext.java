package magma;

import java.util.List;

public record LabelContext(String name, State state) {
    public LabelContext define(String variableName, List<Loader> loaders) {
        return new LabelContext(name, state.define(name, variableName, loaders));
    }

    public LabelContext assign(String variableName, int variableOffset, List<Loader> loaders) {
        return new LabelContext(name, state.assign(name, variableName, variableOffset, loaders));
    }
}
