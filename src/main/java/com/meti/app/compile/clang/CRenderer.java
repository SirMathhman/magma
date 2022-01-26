package com.meti.app.compile.clang;

import com.meti.api.collect.java.List;
import com.meti.api.collect.stream.StreamException;
import com.meti.api.option.None;
import com.meti.api.option.Option;
import com.meti.app.compile.common.EmptyField;
import com.meti.app.compile.common.LineProcessor;
import com.meti.app.compile.common.alternate.ElseProcessor;
import com.meti.app.compile.common.binary.BinaryProcessor;
import com.meti.app.compile.common.block.BlockProcessor;
import com.meti.app.compile.common.condition.ConditionProcessor;
import com.meti.app.compile.common.integer.IntegerProcessor;
import com.meti.app.compile.common.invoke.InvocationProcessor;
import com.meti.app.compile.common.returns.ReturnProcessor;
import com.meti.app.compile.common.string.StringProcessor;
import com.meti.app.compile.common.variable.VariableProcessor;
import com.meti.app.compile.node.Node;
import com.meti.app.compile.node.OutputNode;
import com.meti.app.compile.node.attribute.Attribute;
import com.meti.app.compile.node.attribute.AttributeException;
import com.meti.app.compile.process.Processor;
import com.meti.app.compile.render.EmptyProcessor;
import com.meti.app.compile.render.RenderException;
import com.meti.app.compile.stage.CompileException;
import com.meti.app.compile.text.Output;
import com.meti.app.compile.text.RootText;

import java.util.ArrayList;
import java.util.stream.Collectors;

public record CRenderer(Node root) {
    private static Output renderField(Node node) throws CompileException {
        var name = node.apply(Attribute.Type.Name).asInput();
        var type = node.apply(Attribute.Type.Type).asNode();

        if (type.is(Node.Type.Structure)) {
            return type.apply(Attribute.Type.Name).asInput().toOutput().prepend("struct ").appendSlice(" ").appendSlice(name.toOutput().computeRaw());
        }
        if (type.is(Node.Type.Function)) {
            var returns = type.apply(Attribute.Type.Type).asNode();
            var returnType = renderType(returns);
            var oldParameters = type.apply(Attribute.Type.Parameters).asStreamOfNodes().collect(Collectors.toList());
            var newParameters = new ArrayList<String>();
            for (Node oldParameter : oldParameters) {
                var renderedParameter = renderType(oldParameter);
                newParameters.add(renderedParameter.compute());
            }
            return returnType.appendSlice(" (*").appendOutput(name.toOutput()).appendSlice(")").appendSlice("(").appendSlice(String.join(",", newParameters)).appendSlice(")");
        } else if (type.is(Node.Type.Reference)) {
            var child = type.apply(Attribute.Type.Value).asNode();
            return renderFieldWithType(new EmptyField(new RootText(name.toOutput().prepend("*").compute()), child, List.createList()));
        } else if (type.is(Node.Type.Primitive)) {
            var rendered = type.apply(Attribute.Type.Name).asInput().toOutput().compute().toLowerCase();
            return name.toOutput().prepend(rendered + " ");
        } else if (type.is(Node.Type.Integer)) {
            var isSigned = type.apply(Attribute.Type.Sign).asBoolean();
            var bits = type.apply(Attribute.Type.Bits).asInteger();
            var suffix = switch (bits) {
                case 8 -> "char";
                case 16 -> "int";
                default -> throw new RenderException("Unknown bit quantity: " + bits);
            };
            var value = (isSigned ? "" : "unsigned ") + suffix + " " + name.toOutput().compute();
            return new RootText(value).toOutput();
        } else {
            throw new RenderException("Cannot render type: " + type);
        }
    }

    private static Output renderFieldWithType(Node node) throws CompileException {
        var common = renderField(node);
        if (node.is(Node.Type.Declaration)) {
            return common;
        } else {
            var value = node.apply(Attribute.Type.Value).asNode();
            var valueText = renderNode(value);
            return common.appendSlice("=").appendOutput(valueText);
        }
    }

    static Output renderNode(Node node) throws CompileException {
        if (node.is(Node.Type.Input)) {
            return node.apply(Attribute.Type.Value).asInput().toOutput();
        }

        var renderers = java.util.List.of(new BinaryProcessor(node), new BlockProcessor(node), new ConditionProcessor(node), new DeclarationProcessor(node), new ElseProcessor(node), new EmptyProcessor(node), new ExternRenderer(node), new FunctionProcessor(node), new ImportProcessor(node), new IntegerProcessor(node), new InvocationProcessor(node), new LineProcessor(node), new ReturnProcessor(node), new StringProcessor(node), new StructureRenderer(node), new UnaryProcessor(node), new VariableProcessor(node));

        Option<Output> current = new None<>();
        for (Processor<Output> renderer : renderers) {
            var result = renderer.process();
            if (result.isPresent()) {
                current = result;
            }
        }

        return current.orElseThrow(() -> new CompileException("Unable to render oldNode: " + node));
    }

    public static Node renderDefinitionGroup(Node root) throws CompileException {
        try {
            return root.apply(Attribute.Group.Definition).foldRight(root, (current, type) -> current.mapAsNode(type, node -> {
                var renderedNode = renderFieldWithType(node);
                return new OutputNode(renderedNode);
            }));
        } catch (StreamException e) {
            throw new CompileException(e);
        }
    }

    private static Output renderType(Node oldParameter) throws CompileException {
        return renderField(new EmptyField(new RootText(""), oldParameter, List.createList()));
    }

    public Node renderNodeGroup(Node root) throws CompileException {
        try {
            return root.apply(Attribute.Group.Node).foldRight(root, (current, type) -> current.mapAsNode(type, input -> new OutputNode(renderAST(input))));
        } catch (StreamException e) {
            throw new CompileException(e);
        }
    }

    public Output render() throws CompileException {
        return renderAST(root);
    }

    Output renderAST(Node root) throws CompileException {
        try {
            var withNodes = renderNodeGroup(root);
            var withNodeCollections = renderNodesGroup(withNodes);
            var withFields = renderDefinitionGroup(withNodeCollections);
            var current = renderDefinitionsGroup(withFields);
            return renderNode(current);
        } catch (CompileException e) {
            throw new CompileException("Failed to render AST of node: " + root, e);
        }
    }

    private Node renderNodesGroup(Node root) throws CompileException {
        try {
            return root.apply(Attribute.Group.Nodes).foldRight(root, (current, type) -> {
                try {
                    return root.mapAsNodeStream(type, stream -> stream.map(this::renderAST).map(OutputNode::new));
                } catch (StreamException e) {
                    throw new AttributeException(e);
                }
            });
        } catch (StreamException e) {
            throw new CompileException(e);
        }
    }

    private Node renderDefinitionsGroup(Node root) throws CompileException {
        try {
            return root.apply(Attribute.Group.Definitions).foldRight(root, (current, type) -> {
                try {
                    return current.mapAsNodeStream(type, input -> input.map(CRenderer::renderFieldWithType)
                            .map(OutputNode::new));
                } catch (StreamException e) {
                    throw new AttributeException(e);
                }
            });
        } catch (StreamException e) {
            throw new CompileException(e);
        }
    }
}
