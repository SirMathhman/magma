package magma.app.compile.lang;

import magma.app.compile.rule.EmptyRule;
import magma.app.compile.rule.PrefixRule;
import magma.app.compile.rule.Rule;

public class CLang {
    public static Rule createCRootRule() {
        return new PrefixRule("int main(){\n\treturn 0;\n}", new EmptyRule());
    }
}
