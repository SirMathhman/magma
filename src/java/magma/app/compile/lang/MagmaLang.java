package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class MagmaLang {
    public static Rule createMagmaRootRule() {
        return new EmptyRule();
    }
}
