package magma.app.compile.rule;

import java.util.ArrayList;
import java.util.List;

public class State {
    private final List<String> list;
    private StringBuilder buffer;

    public State(List<String> list, StringBuilder buffer) {
        this.list = list;
        this.buffer = buffer;
    }

    public State() {
        this(new ArrayList<>(), new StringBuilder());
    }

    State advance() {
        if (!getBuffer().isEmpty()) list.add(getBuffer().toString());
        setBuffer(new StringBuilder());
        return this;
    }

    State append(char c) {
        getBuffer().append(c);
        return this;
    }

    public List<String> segments() {
        return list;
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }
}
