package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class MagmaLang {
    public static Rule createMagmaRootRule() {
        return new TypeRule(CommonLang.ROOT_TYPE, new NodeRule("value", CommonLang.createBlockRule(List.of(
                new TypeRule(CommonLang.DECLARATION_TYPE, new PrefixRule("let x : I32 = 420;", new EmptyRule())),
                CommonLang.createReturnRule()
        ))));
    }
}
