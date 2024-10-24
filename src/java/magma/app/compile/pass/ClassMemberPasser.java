package magma.app.compile.pass;

import magma.app.compile.Node;

import static magma.app.compile.lang.JavaLang.METHOD;
import static magma.app.compile.lang.MagmaLang.FUNCTION;

public class ClassMemberPasser {
    public static Node passClassMember(Node child) {
        if (child.is(METHOD)) return child.retype(FUNCTION);
        return child;
    }
}
