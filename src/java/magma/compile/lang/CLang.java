package magma.compile.lang;

import magma.NodeRule;
import magma.compile.rule.OrRule;
import magma.compile.rule.Rule;
import magma.compile.rule.TypeRule;
import magma.compile.rule.split.LocatingSplitter;
import magma.compile.rule.split.SplitRule;
import magma.compile.rule.split.locate.FirstLocator;
import magma.compile.rule.string.PrefixRule;
import magma.compile.rule.string.StringListRule;
import magma.compile.rule.string.StringRule;
import magma.compile.rule.string.SuffixRule;

import java.util.List;

public class CLang {
    public static Rule createCRootRule() {
        return CommonLang.createGroupRule(createCRootMemberRule());
    }

    private static OrRule createCRootMemberRule() {
        return new OrRule(List.of(
                createIncludesRule(),
                createStructRule(),
                CommonLang.createWhitespaceRule()
        ));
    }

    private static Rule createStructRule() {
        final var name = new StringRule("name");
        final var wrapped = CommonLang.wrapInBlock(name, createStructMemberRule());
        return new TypeRule("struct", new PrefixRule("struct ", wrapped));
    }

    private static Rule createStructMemberRule() {
        return new OrRule(List.of(
                createFunctionRule(),
                CommonLang.createWhitespaceRule()
        ));
    }

    private static TypeRule createFunctionRule() {
        final var type = new NodeRule("type", CommonLang.createTypeRule());
        return new TypeRule("function", new SplitRule(type, new LocatingSplitter(" ", new FirstLocator()), new SuffixRule(new StringRule("name"), "(){}")));
    }

    private static Rule createIncludesRule() {
        final var namespace = new StringListRule("namespace", "/");
        return new TypeRule("include", new PrefixRule("#include \"", new SuffixRule(namespace, ".h\"")));
    }
}
