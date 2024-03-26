package com.meti.stage;

import com.meti.TypeCompiler;
import com.meti.node.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TransformStage extends Stage<Node, Node> {
    private static StringAttribute compileType(Attribute type) {
        return type.asString()
                .map(TypeCompiler::new)
                .map(TypeCompiler::compile)
                .map(StringAttribute::new)
                .orElseThrow();
    }

    @Override
    public Optional<Node> onEnter(Node value) {
        return Optional.of(value);
    }

    private Node createOutput(Node o) {
        return o;
    }

    private Node createInput(Node node) {
        return node;
    }

    @Override
    protected Optional<Node> onExit(Node node) {
        if(node.is("type")) {
            var value = node.apply("value").flatMap(Attribute::asString).orElseThrow();
            var result = new TypeCompiler(value).compile();
            return Optional.of(node.replace("value", new StringAttribute(result)));
        }

        if (node.is("class")) {
            var indent = node.apply("indent").flatMap(Attribute::asInt).orElseThrow();
            var body = node.apply("body").flatMap(Attribute::asListOfNodes).orElse(Collections.emptyList());

            var newBody = body.stream()
                    .map(element -> cleanDefinition(element, indent).orElse(element))
                    .collect(Collectors.toList());

            return Optional.ofNullable(node.replace("body", new NodeListAttribute(newBody)));
        }

        if (node.is("method")) {
            /*
            TODO: implement stack logic
             */
            var indent = node.apply("indent").flatMap(Attribute::asInt).orElse(0);

            var body = node.apply("body").flatMap(Attribute::asListOfNodes).orElse(Collections.emptyList());

            var newBody = body.stream()
                    .map(element -> cleanDefinition(element, indent).orElse(element))
                    .collect(Collectors.toList());

            return Optional.ofNullable(node.replace("body", new NodeListAttribute(newBody)));
        }

        return Optional.of(node);
    }

    private Optional<Node> cleanDefinition(Node element, int indent) {
        if (element.is("definition")) {
            var previousFlags = element.apply("flags").flatMap(Attribute::asListOfStrings).orElse(Collections.emptyList());
            List<String> newFlags = new ArrayList<>();
            if (previousFlags.contains("public")) {
                newFlags.add("pub");
            }

            if (previousFlags.contains("final")) {
                newFlags.add("const");
            } else {
                newFlags.add("let");
            }

            return Optional.ofNullable(element
                    .map("type", TransformStage::compileType)
                    .replace("indent", new IntAttribute(indent + 1))
                    .replace("flags", new StringListAttribute(newFlags)));
        }

        return Optional.empty();
    }

    @Override
    public Optional<Node> applyToNode(Node node) {
        return apply(createInput(node)).map(this::createOutput);
    }
}
