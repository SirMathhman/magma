package com.meti.app.compile.block;

import com.meti.app.compile.Node;
import com.meti.core.None;
import com.meti.core.Option;
import com.meti.core.Some;
import com.meti.java.List;
import com.meti.java.String_;

public record Block(List<? extends Node> values) implements Node {
    @Override
    public Option<List<? extends Node>> lines() {
        return Some.apply(values);
    }

    @Override
    public Option<Node> withLines(List<? extends Node> lines) {
        return Some.apply(new Block(lines));
    }

    @Override
    public Option<Node> type() {
        return None.apply();
    }

    @Override
    public Option<String_> value() {
        return None.apply();
    }
}
