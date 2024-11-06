package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class MagmaLang {
    public static Rule createMagmaRootRule() {
        return new TypeRule(CommonLang.ROOT_TYPE, new OrRule(List.of(
                new NodeRule(CommonLang.BLOCK_CHILDREN, CommonLang.createReturnRule()),
                new EmptyRule()
        )));
    }
}
