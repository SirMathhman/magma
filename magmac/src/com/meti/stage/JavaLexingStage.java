package com.meti.stage;

import com.meti.ImportLexer;
import com.meti.java.*;
import com.meti.node.Content;
import com.meti.rule.RequireRule;
import com.meti.rule.Rules;
import com.meti.rule.TextRule;

import java.util.List;

import static com.meti.rule.AndRule.And;

public class JavaLexingStage extends LexingStage {
    @Override
    protected Lexer createLexer(Content value) {
        var innerValue = value.value();
        return switch (value.name()) {
            case "top" -> new CompoundLexer(List.of(
                    () -> new ClassLexer(value.value(), value.indent()),
                    () -> new ImportLexer(value.value())
            ));
            case "class" -> new ClassLexer(innerValue, value.indent());

            /*
            TODO: statements
             */
            case "class-member" -> new CompoundLexer(List.of(
                    () -> new MethodLexer(value.indent(), value.value()),
                    () -> new DefinitionLexer(value.value(), value.indent()),
                    () -> new ClassLexer(value.value(), value.indent())
            ));

            case "method-statement" -> new DefinitionLexer(innerValue, value.indent());
            case "value" -> new CompoundLexer(List.of(
                    () -> new RuleLexer(And(new RequireRule("\""),
                            new TextRule("value", Rules.Any),
                            new RequireRule("\"")), innerValue, "string"),
                    () -> new FieldLexer(innerValue),
                    () -> new InvokeLexer(innerValue),
                    () -> new IntegerLexer(innerValue)));
            default -> throw new UnsupportedOperationException("Unknown node name: " + value.name());
        };
    }
}
