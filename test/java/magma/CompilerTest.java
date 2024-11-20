package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static magma.Compiler.*;
import static org.junit.jupiter.api.Assertions.*;

class CompilerTest {
    private static void assertCompile(String input, String expected) {
        try {
            final var output = compile(input);
            assertEquals(expected, output);
        } catch (CompileException e) {
            fail(e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"First", "Second"})
    void classStatement(String className) {
        final var node = new Node(Optional.of(CLASS_TYPE)).withString(VALUE, className);
        final var input = createClassRule().generate(node).orElseThrow();
        final var expected = createFunctionRule().generate(node.retype(FUNCTION_TYPE)).orElseThrow();
        assertCompile(input, expected);
    }

    @Test
    void rootMemberInvalid() {
        assertThrows(CompileException.class, () -> compile("?"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void importStatic(String namespace) {
        var rule = createInstanceImportRule();
        var rule1 = createStaticImportRule();

        var node = new Node(IMPORT_TYPE).withString(VALUE, namespace);
        var node1 = new Node(IMPORT_STATIC_TYPE).withString(VALUE, namespace);
        assertCompile(rule1.generate(node1).orElseThrow(), rule.generate(node).orElseThrow());
    }
}