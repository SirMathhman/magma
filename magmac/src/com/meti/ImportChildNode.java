package com.meti;

import static com.meti.Some.Some;

public record ImportChildNode(String child, String parent) implements Node {
    @Override
    public Option<String> render() {
        return Some("import { " + child() + " } from " + parent() + ";\n");
    }
}