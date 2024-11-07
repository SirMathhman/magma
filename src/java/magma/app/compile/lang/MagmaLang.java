package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.BLOCK_CHILDREN;
import static magma.app.compile.lang.CommonLang.ROOT_TYPE;

public class MagmaLang {
    public static Rule createMagmaRootRule() {
        return new TypeRule(ROOT_TYPE, new NodeListRule(BLOCK_CHILDREN, new OrRule(List.of(
                createStructRule()
        ))));
    }

    private static PrefixRule createStructRule() {
        final var name = new StringRule("name");
        final var typeParams = new StringRule("type-params");
        final var leftRule = new StripRule(new FirstRule(name, "<", new SuffixRule(typeParams, ">")));
        final var children = new NodeListRule(BLOCK_CHILDREN, new OrRule(List.of(new StripRule(new EmptyRule()))));
        return new PrefixRule("struct ", new FirstRule(leftRule, "{", new SuffixRule(children, "}")));
    }
}
