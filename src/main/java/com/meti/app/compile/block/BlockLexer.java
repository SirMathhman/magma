package com.meti.app.compile.block;

import com.meti.app.compile.*;
import com.meti.app.compile.attribute.Attribute;
import com.meti.app.compile.attribute.NodeListAttribute;
import com.meti.core.Ok;
import com.meti.core.Option;
import com.meti.core.Result;
import com.meti.java.JavaList;
import com.meti.java.JavaMap;
import com.meti.java.JavaString;
import com.meti.java.String_;

import static com.meti.core.Options.$$;
import static com.meti.core.Options.$Option;
import static com.meti.java.JavaString.fromSlice;

public record BlockLexer(String_ line) implements Lexer {
    private Option<Node> lex1() {
        return $Option(() -> {
            var stripped = line().strip();
            var bodyStart = stripped.firstIndexOfChar('{').$();
            var bodyEnd = stripped.lastIndexOfChar('}').$();
            if (!bodyStart.isStart() || !bodyEnd.isEnd()) {
                return $$();
            }

            var range = bodyStart.nextExclusive().$().to(bodyEnd).$();
            var content = stripped.sliceBetween(range);
            var map = new Splitter(content).split()
                    .map(String_::strip)
                    .filter(value -> !value.isEmpty())
                    .map(value1 -> new Content(fromSlice(""), value1))
                    .collect(JavaList.intoList());
            return new MapNode(fromSlice("block"), JavaMap.<String_, Attribute>empty()
                    .insert(fromSlice("lines"), new NodeListAttribute(JavaString.fromSlice("any"), map)));
        });
    }

    @Override
    public Option<Result<Node, CompileException>> lex() {
        return lex1().map(Ok::apply);
    }
}