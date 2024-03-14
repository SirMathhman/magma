package com.meti;

import static com.meti.Some.Some;

public record StringNode(String value) implements Node {
    @Override
    public Option<String> render() {
        return Some("\"" + value() + "\"");
    }
}