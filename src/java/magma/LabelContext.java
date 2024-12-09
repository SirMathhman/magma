package magma;

import java.util.List;

public record LabelContext(String labelName, State state) {
    public LabelContext define(String variableName, List<Loader> loaders) {
        return new LabelContext(labelName, state.define(labelName, variableName, loaders));
    }

    public LabelContext instruct(List<Instruction> instructions) {
        final var newState = state.instruct(labelName, instructions);
        return new LabelContext(labelName, newState);
    }

    public LabelContext assign(String variableName, int variableOffset, List<Loader> loaders) {
        return new LabelContext(labelName, state.assign(labelName, variableName, variableOffset, loaders));
    }

    public LabelContext jump(String labelName) {
        return new LabelContext(this.labelName, state.jump(this.labelName, labelName));
    }
}
