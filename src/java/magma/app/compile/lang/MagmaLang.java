package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.BLOCK_CHILDREN;

public class MagmaLang {
    public static Rule createMagmaRootRule() {
        return new TypeRule(CommonLang.ROOT_TYPE, new NodeListRule(BLOCK_CHILDREN, new OrRule(List.of(
                new TypeRule(CommonLang.DECLARATION_TYPE, new PrefixRule("let x : I32 = 420;", new EmptyRule())),
                CommonLang.createReturnRule()
        ))));
    }
}
