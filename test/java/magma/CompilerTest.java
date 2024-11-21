package magma;

import magma.result.Results;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static magma.Compiler.compile;
import static org.junit.jupiter.api.Assertions.*;

class CompilerTest {
    private static void assertCompile(String input, String expected) {
        try {
            final var output = Results.unwrap(compile(input));
            assertEquals(expected, output);
        } catch (CompileException e) {
            fail(e);
        }
    }

    private static void assertRootMember(Node inputNode, Node expectedNode) {
        final var input = JavaLang.createRootJavaRule().generate(inputNode).findValue().orElseThrow();
        final var expected = MagmaLang.createRootMagmaRule().generate(expectedNode).findValue().orElseThrow();
        assertCompile(input, expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"First", "Second"})
    void classStatement(String className) {
        final var classNode = new Node(JavaLang.CLASS_TYPE).withString(CommonLang.CLASS_NAME, className);
        final var functionNode = classNode.retype(MagmaLang.FUNCTION_TYPE);
        assertRootMember(classNode, functionNode);
    }

    @Test
    void rootMemberInvalid() {
        assertThrows(CompileException.class, () -> Results.unwrap(compile("?")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void importStatic(String namespace) {

        var node = new Node(CommonLang.IMPORT_TYPE).withString(CommonLang.CLASS_NAME, namespace);
        var node1 = new Node(JavaLang.IMPORT_STATIC_TYPE).withString(CommonLang.CLASS_NAME, namespace);
        assertRootMember(node1, node);
    }
}