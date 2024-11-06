package magma.app.compile.lang;

import magma.app.compile.rule.*;

import static magma.app.compile.lang.CommonLang.createReturnRule;

public class CLang {
    public static Rule createCRootRule() {
        final var children = new NodeRule(CommonLang.BLOCK_CHILDREN, createReturnRule());
        return new TypeRule(CommonLang.FUNCTION_TYPE, new PrefixRule("int main(){\n\t", new SuffixRule(children, "\n}")));
    }
}
