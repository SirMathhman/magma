package com.meti.java;

import com.meti.rule.ListRule;
import com.meti.rule.RangeRule;
import com.meti.rule.TextRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntegerLexerTest {
    @Test
    void valid(){
        assertTrue(((Lexer) new RuleLexer("int", "123", new TextRule("value", new ListRule(new RangeRule('0', '9')))))
                .lex()
                .isPresent());
    }

    @Test
    void invalid() {
        assertFalse(((Lexer) new RuleLexer("int", "test", new TextRule("value", new ListRule(new RangeRule('0', '9')))))
                .lex()
                .isPresent());
    }
}