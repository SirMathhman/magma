package magma;

import magma.result.Result;

import java.util.ArrayList;

public class Compiler {
    static Result<String, CompileException> compile(String input) {
        final var sourceRule = JavaLang.createRootJavaRule();
        final var targetRule = MagmaLang.createRootMagmaRule();

        return sourceRule.parse(input)
                .mapValue(Compiler::passRoot)
                .flatMapValue(targetRule::generate);
    }

    private static Node passRoot(Node root) {
        final var oldChildren = root.findNodeList(CommonLang.ROOT_CHILDREN).orElse(new ArrayList<>());
        final var newChildren = oldChildren.stream()
                .filter(rootMember -> !rootMember.is(JavaLang.PACKAGE_TYPE))
                .map(Compiler::passRootMember)
                .toList();

        return new Node().withNodeList(CommonLang.ROOT_CHILDREN, newChildren);
    }

    private static Node passRootMember(Node rootMember) {
        if (rootMember.is(JavaLang.IMPORT_STATIC_TYPE)) return rootMember.retype(CommonLang.IMPORT_TYPE);
        if (rootMember.is(JavaLang.CLASS_TYPE)) return rootMember.retype(MagmaLang.FUNCTION_TYPE);
        return rootMember;
    }
}