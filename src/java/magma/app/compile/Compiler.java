package magma.app.compile;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.StringContext;
import magma.app.compile.rule.*;
import magma.java.JavaLists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record Compiler(String input) {
    public static final String RETURN_PREFIX = "return ";
    public static final String STATEMENT_END = ";";
    public static final String VALUE = "value";
    public static final String RETURN_TYPE = "return";
    public static final String CHILDREN = "children";
    public static final String DECLARATION_TYPE = "declaration";
    public static final String DEFINITION_TYPE = "type";
    public static final String DEFINITION = "definition";
    public static final String DECLARATION_DEFINITION = DEFINITION;
    public static final String SYMBOL_TYPE = "symbol";
    public static final String SYMBOL_VALUE = "symbol-value";
    public static final String FUNCTION_TYPE = "function";

    private static Rule createReturnRule() {
        return new TypeRule(RETURN_TYPE, new PrefixRule(RETURN_PREFIX, new SuffixRule(new StringRule(VALUE), STATEMENT_END)));
    }

    private static Rule createCRootRule() {
        return new NodeListRule(CHILDREN, new OrRule(List.of(
                createFunctionRule()
        )));
    }

    private static TypeRule createFunctionRule() {
        return new TypeRule(FUNCTION_TYPE, new PrefixRule("int main(){", new SuffixRule(new NodeListRule(CHILDREN, createCStatementRule()), "}")));
    }

    private static Rule createCStatementRule() {
        return new OrRule(List.of(
                createDeclarationRule(createCDefinitionRule()),
                createReturnRule()
        ));
    }

    private static TypeRule createCDefinitionRule() {
        final var name = new StringRule("name");
        final var type = new NodeRule(DEFINITION_TYPE, createCTypeRule());
        return new TypeRule("definition-type", new FirstRule(type, " ", name));
    }

    private static Rule createCTypeRule() {
        return createSymbolTypeRule();
    }

    private static Rule createMagmaRootRule() {
        return new NodeListRule(CHILDREN, new StripRule(new OrRule(List.of(
                createDeclarationRule(createMagmaDefinitionRule()),
                createReturnRule()
        ))));
    }

    private static TypeRule createMagmaDefinitionRule() {
        final var name = new StripRule(new StringRule("name"));
        final var type = new NodeRule(DEFINITION_TYPE, createMagmaTypeRule());
        return new TypeRule("definition-type", new StripRule(new PrefixRule("let ", new FirstRule(name, ":", type))));
    }

    private static Rule createMagmaTypeRule() {
        return createSymbolTypeRule();
    }

    private static TypeRule createSymbolTypeRule() {
        return new TypeRule(SYMBOL_TYPE, new StripRule(new StringRule(SYMBOL_VALUE)));
    }

    private static TypeRule createDeclarationRule(TypeRule definition) {
        final var afterAssignment = new StripRule(new SuffixRule(new NodeRule("value", createValueRule()), ";"));
        return new TypeRule(DECLARATION_TYPE, new FirstRule(new NodeRule(DECLARATION_DEFINITION, definition), "=", afterAssignment));
    }

    private static Rule createValueRule() {
        return new OrRule(List.of(
                new TypeRule(SYMBOL_TYPE, new StringRule(SYMBOL_VALUE))
        ));
    }

    private static Result<Node, CompileError> pass(Node node) {
        return node.mapNodeList(CHILDREN, Compiler::passRootMembers)
                .orElse(new Ok<>(node))
                .mapValue(inner -> inner.retype(FUNCTION_TYPE).orElse(inner))
                .mapValue(inner -> new MapNode().withNodeList(CHILDREN, Collections.singletonList(inner)));
    }

    private static Result<List<Node>, CompileError> passRootMembers(List<Node> rootMembers) {
        return rootMembers.stream()
                .map(Compiler::passRootMember)
                .<Result<List<Node>, CompileError>>reduce(new Ok<>(new ArrayList<>()),
                        (current, element) -> current.and(() -> element).mapValue(JavaLists::add),
                        (_, value) -> value);
    }

    private static Result<Node, CompileError> passRootMember(Node child) {
        if (!child.is(DECLARATION_TYPE)) {
            return new Ok<>(child);
        }

        return child.mapNode(DECLARATION_DEFINITION, Compiler::passDefinition).orElse(new Ok<>(child));
    }

    private static Result<Node, CompileError> passDefinition(Node definition) {
        return definition
                .mapNode(DEFINITION_TYPE, Compiler::passSymbolType)
                .orElse(new Ok<>(definition));
    }

    private static Result<Node, CompileError> passSymbolType(Node type) {
        if (!type.is(SYMBOL_TYPE)) return new Ok<>(type);

        return type
                .mapString(SYMBOL_VALUE, Compiler::passSymbolValue)
                .orElse(new Ok<>(type));
    }

    private static Result<String, CompileError> passSymbolValue(String value) {
        if (value.equals("I32")) return new Ok<>("int");
        else return new Err<>(new CompileError("Unknown value", new StringContext(value)));
    }

    public Result<String, CompileError> compile() {
        final var sourceRule = createMagmaRootRule();
        final var targetRule = createCRootRule();

        return sourceRule.parse(this.input())
                .flatMapValue(Compiler::pass)
                .flatMapValue(targetRule::generate);
    }
}