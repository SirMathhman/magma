package magma.app.compile.lang.c;

import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.rule.*;

public class CLang {
    public static Rule createRootRule() {
        return new TypeRule("root", new SplitRule(new BracketSplitter(), "children", createRootMemberRule()));
    }

    private static TypeRule createRootMemberRule() {
        return createFunctionRule();
    }

    private static TypeRule createFunctionRule() {
        final var name = new StringRule("name");
        final var value = new NodeRule("value",  CommonLang.createBlockRule(createStatementRule()));
        return new TypeRule("function", new PrefixRule("void ", new FirstRule(name, "()", value)));
    }

    private static TypeRule createStatementRule() {
        return new TypeRule("test", new EmptyRule());
    }
}
