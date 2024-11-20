package magma;

import magma.result.Result;

import java.util.ArrayList;

public class Compiler {
    public static final String VALUE = "value";

    static Result<String, CompileException> compile(String input) {
        final var sourceRule = JavaLang.createRootJavaRule();
        final var targetRule = MagmaLang.createRootMagmaRule();

        return sourceRule.parse(input)
                .mapValue(Compiler::passRoot)
                .flatMapValue(targetRule::generate);
    }

    private static Node passRoot(Node node) {
        final var oldChildren = node.findNodeList(CommonLang.ROOT_CHILDREN).orElse(new ArrayList<>());
        final var newChildren = oldChildren.stream()
                .filter(node1 -> !node1.is(JavaLang.PACKAGE_TYPE))
                .map(Compiler::passRootMember)
                .toList();

        return new Node().withNodeList(CommonLang.ROOT_CHILDREN, newChildren);
    }

    private static Node passRootMember(Node node) {
        if (node.is(JavaLang.IMPORT_STATIC_TYPE)) return node.retype(CommonLang.IMPORT_TYPE);
        if (node.is(JavaLang.CLASS_TYPE)) return node.retype(MagmaLang.FUNCTION_TYPE);
        return node;
    }
}