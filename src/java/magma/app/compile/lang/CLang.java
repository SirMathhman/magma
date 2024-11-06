package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.createReturnRule;

public class CLang {
    public static Rule createCRootRule() {
        final var children = new NodeListRule(CommonLang.BLOCK_CHILDREN, new OrRule(List.of(
                new TypeRule(CommonLang.DECLARATION_TYPE, new PrefixRule("int x = 420;", new EmptyRule())),
                createReturnRule()
        )));

        return new TypeRule(CommonLang.FUNCTION_TYPE, new PrefixRule("int main(){\n\t", new SuffixRule(children, "\n}")));
    }
}
