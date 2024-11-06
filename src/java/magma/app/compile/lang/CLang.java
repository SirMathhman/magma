package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.createReturnRule;

public class CLang {
    public static Rule createCRootRule() {
        final var list = List.of(
                new TypeRule(CommonLang.DECLARATION_TYPE, new PrefixRule("int x = 420;", new EmptyRule())),
                createReturnRule()
        );

        final var children = new NodeRule("value", CommonLang.createBlockRule(list));
        return new TypeRule(CommonLang.FUNCTION_TYPE, new PrefixRule("int main(){", new SuffixRule(children, "\n}")));
    }
}
