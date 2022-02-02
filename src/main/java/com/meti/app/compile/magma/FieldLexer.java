package com.meti.app.compile.magma;

import com.meti.api.collect.IndexException;
import com.meti.api.collect.java.List;
import com.meti.api.collect.stream.StreamException;
import com.meti.api.collect.stream.Streams;
import com.meti.api.option.None;
import com.meti.api.option.Option;
import com.meti.api.option.Some;
import com.meti.app.compile.common.Definition;
import com.meti.app.compile.common.Initialization;
import com.meti.app.compile.feature.scope.Declaration;
import com.meti.app.compile.node.InputNode;
import com.meti.app.compile.node.Node;
import com.meti.app.compile.process.Processor;
import com.meti.app.compile.stage.CompileException;
import com.meti.app.compile.text.Input;

import static com.meti.app.compile.magma.ImplicitType.ImplicitType_;

public record FieldLexer(Input input) implements Processor<Node> {
    @Override
    public Option<Node> process() throws CompileException {
        var typeSeparatorOptional = input.firstIndexOfChar(':');
        var valueSeparator = findValueSeparator(input);

        var keys = input.slice(0, typeSeparatorOptional.or(valueSeparator).orElse(input.size()));
        var separator = keys.lastIndexOfChar(' ');
        var flags = lexFlags(keys, separator);
        try {
            if (flags.stream()
                    .map(flags::count)
                    .anyMatch(value -> value > 1)) {
                throw new CompileException("Duplicate flags are not allowed.");
            }
            if (flags.contains(Definition.Flag.Const) && flags.contains(Definition.Flag.Let)) {
                throw new CompileException("Definition cannot be mutable and immutable at the same time.");
            }
        } catch (StreamException e) {
            throw new CompileException(e);
        }

        var nameText = separator.map(space -> keys.slice(space + 1)).orElse(keys);
        Option<Node> field;
        field = typeSeparatorOptional.<Option<Node>, RuntimeException>map(typeSeparator -> new Some<>(valueSeparator.<Node, RuntimeException>map(s -> {
            var typeText = input.slice(typeSeparator + 1, s);
            var type = new InputNode(typeText);

            var valueText = input.slice(s + 1);
            var value = new InputNode(valueText);

            return new Initialization(nameText, type, value, flags);
        }).orElseGet(() -> {
            var typeText = input.slice(typeSeparator + 1);
            var type = new InputNode(typeText);

            return new Declaration(nameText, type, flags);
        }))).orElseGet(() -> valueSeparator.map(integer -> {
            var valueText = input.slice(integer + 1);
            var value = new InputNode(valueText);

            return new Initialization(nameText, ImplicitType_, value, flags);
        }));
        return field;
    }

    private static Option<Integer> findValueSeparator(Input input) {
        try {
            if (input.size() < 2) return new None<>();

            for (int i = 0; i < input.size(); i++) {
                var c = input.apply(i);
                var offset = Math.min(i + 2, input.size());
                if (c == '=' && !input.slice(i, offset)
                        .toOutput()
                        .computeRaw()
                        .equals("=>")) {
                    return new Some<>(i);
                }
            }
            return new None<>();
        } catch (IndexException e) {
            return new None<>();
        }
    }

    private static List<Definition.Flag> lexFlags(Input keys, Option<Integer> separator) throws CompileException {
        try {
            return separator.map(space -> Streams.apply(keys.slice(0, space)
                    .toOutput()
                    .compute()
                    .split(" "))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .flatMap(value1 -> Streams.apply(Definition.Flag.values())
                            .filter(value -> value.name().equalsIgnoreCase(value1)))
                    .foldRight(List.<Definition.Flag>createList(), List::add))
                    .orElse(List.createList());
        } catch (StreamException e) {
            throw new CompileException(e);
        }
    }
}
