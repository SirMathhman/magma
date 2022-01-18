package com.meti.app.compile.common.binary;

import com.meti.api.option.Option;
import com.meti.app.compile.lex.Lexer;
import com.meti.app.compile.node.Content;
import com.meti.app.compile.node.Node;
import com.meti.app.compile.node.Text;

public record BinaryLexer(Text text) implements Lexer {
    @Override
    public Option<Node> lex() {
        return text.firstIndexOfChar(' ')
                .flatMap(firstSeparator -> text.firstIndexOfCharWithOffset(' ', firstSeparator + 1)
                        .map(secondSeparator -> extract(firstSeparator, secondSeparator)));
    }

    private BinaryOperation extract(Integer firstSeparator, Integer secondSeparator) {
        var firstText = text.slice(0, firstSeparator);
        var operatorText = text.slice(firstSeparator + 1, secondSeparator);
        var secondText = text.slice(secondSeparator + 1);

        var first = new Content(firstText);
        var operator = new Content(operatorText);
        var second = new Content(secondText);

        return new BinaryOperation(operator, first, second);
    }
}