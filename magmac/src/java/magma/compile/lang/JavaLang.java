package magma.compile.lang;

import magma.compile.rule.EmptyRule;
import magma.compile.rule.LazyRule;
import magma.compile.rule.OrRule;
import magma.compile.rule.Rule;
import magma.compile.rule.SymbolRule;
import magma.compile.rule.TypeRule;
import magma.compile.rule.split.FirstRule;
import magma.compile.rule.split.LastRule;
import magma.compile.rule.split.ParamSplitter;
import magma.compile.rule.split.SplitMultipleRule;
import magma.compile.rule.text.LeftRule;
import magma.compile.rule.text.RightRule;
import magma.compile.rule.text.StripRule;
import magma.compile.rule.text.extract.ExtractNodeRule;
import magma.compile.rule.text.extract.ExtractStringRule;

import java.util.ArrayList;
import java.util.List;

public class JavaLang {
    public static Rule createRootRule() {
        return Lang.createBlock(createRootMemberRule());
    }

    private static OrRule createRootMemberRule() {
        var classRule = createClassRule();
        var namespace = Lang.createNamespaceRule();
        var importRule = Lang.createImportRule(namespace);

        return new OrRule(List.of(
                new TypeRule("package", new LeftRule("package ", new RightRule(new ExtractNodeRule("internal", namespace), ";"))),
                importRule,
                classRule
        ));
    }

    private static TypeRule createClassRule() {
        var definition = createDefinitionHeaderRule();
        var value = createValueRule();

        var declaration = Lang.createDeclarationRule(definition, value);
        var statement = new LazyRule();

        var rules = List.of(
                Lang.createCommentRule(),
                Lang.createTryRule(statement),
                declaration,
                Lang.createAssignmentRule(value),
                new TypeRule("invocation", new RightRule(Lang.createInvocationRule(value), ";")),
                Lang.createCatchRule(definition, statement),
                Lang.createIfRule("if", value, statement),
                Lang.createIfRule("while", value, statement),
                Lang.createReturnRule(value),
                Lang.createForRule(definition, value, statement, ":"),
                Lang.createElseRule(statement),
                new TypeRule("empty", new RightRule(new StripRule(new EmptyRule()), ";"))
        );

        var copy = new ArrayList<>(rules);
        copy.add(new TypeRule("constructor", new RightRule(createConstructorRule(value), ";")));

        statement.setRule(new OrRule(copy));

        var params = Lang.createParamsRule(definition);
        var content = new StripRule(new LeftRule("{", new RightRule(new ExtractNodeRule("child", Lang.createBlock(statement)), "}")));
        var paramsAndValue = new FirstRule(params, ")", content);

        var leftRule = new ExtractNodeRule("definition", new TypeRule("definition", definition));
        var methodRule = new TypeRule("method", new FirstRule(leftRule, "(", paramsAndValue));

        var classMember = new OrRule(List.of(
                declaration,
                methodRule
        ));

        var modifiers = Lang.createModifiersRule();

        var name =  new TypeRule("symbol", new StripRule(new ExtractStringRule("value")));
        var prototype = new OrRule(List.of(
                new TypeRule("generic", new FirstRule(new StripRule(new ExtractStringRule("value")), "<", new RightRule(new ExtractStringRule("child"), ">"))),
                name
        ));

        var leftRule1 = new ExtractNodeRule("name", prototype);
        var beforeContent = new OrRule(List.of(
                new FirstRule(leftRule1, " implements", new ExtractNodeRule("interface", prototype)),
                leftRule1
        ));

        return new TypeRule("class", new FirstRule(modifiers, "class ", new FirstRule(new StripRule(beforeContent), "{", new RightRule(new ExtractNodeRule("child", Lang.createBlock(classMember)), "}"))));
    }

    private static LazyRule createValueRule() {
        var value = new LazyRule();
        value.setRule(new OrRule(List.of(
                Lang.createStringRule(),
                Lang.createCharRule(),
                createLambdaRule(value),
                Lang.createTernaryRule(value),
                createConstructorRule(value),
                Lang.createInvocationRule(value),
                Lang.createAccessRule(value),
                Lang.createSymbolRule(),
                Lang.createNumberRule(),
                Lang.createOperator("equals", "==", value),
                Lang.createOperator("add", "+", value),
                Lang.createOperator("greater-than", ">", value)
        )));
        return value;
    }

    private static TypeRule createLambdaRule(LazyRule value) {
        var left = new StripRule(new ExtractStringRule("param-name"));
        var right = new StripRule(new ExtractNodeRule("child", value));
        return new TypeRule("lambda", new FirstRule(left, "->", right));
    }

    private static TypeRule createConstructorRule(LazyRule value) {
        var arguments = new OrRule(List.of(
                new SplitMultipleRule(new ParamSplitter(), ", ", "arguments", new StripRule(value))
        ));

        var caller = new ExtractNodeRule("caller", value);
        var withGenerics = new OrRule(List.of(
                new LastRule(caller, "<", new ExtractStringRule("temp")),
                caller
        ));
        var before = new RightRule(new InvocationStart(withGenerics, arguments), ")");
        var child = new OrRule(List.of(
                new FirstRule(new StripRule(before), "{", new ExtractStringRule("after")),
                before
        ));
        return new TypeRule("constructor", new LeftRule("new ", child));
    }

    private static Rule createDefinitionHeaderRule() {
        var type = Lang.createTypeRule();
        var modifiers = Lang.createModifiersRule();
        var withoutModifiers = new ExtractNodeRule("type", type);
        var withModifiers = new LastRule(modifiers, " ", withoutModifiers);
        var anyModifiers = new OrRule(List.of(withModifiers, withoutModifiers));
        return new LastRule(anyModifiers, " ", new StripRule(new SymbolRule(new ExtractStringRule("name"))));
    }

}
