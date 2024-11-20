package magma;

import magma.result.Results;
import magma.rule.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static magma.Compiler.*;
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

    @ParameterizedTest
    @ValueSource(strings = {"First", "Second"})
    void classStatement(String className) {
        final var node = new Node(Optional.of(CLASS_TYPE)).withString(VALUE, className);
        Rule rule1 = createClassRule();
        final var input = rule1.generate(node).findValue().orElseThrow();
        Rule rule = createFunctionRule();
        Node node1 = node.retype(FUNCTION_TYPE);
        final var expected = rule.generate(node1).findValue().orElseThrow();
        assertCompile(input, expected);
    }

    @Test
    void rootMemberInvalid() {
        assertThrows(CompileException.class, () -> Results.unwrap(compile("?")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void importStatic(String namespace) {
        var rule = createInstanceImportRule();
        var rule1 = createStaticImportRule();

        var node = new Node(IMPORT_TYPE).withString(VALUE, namespace);
        var node1 = new Node(IMPORT_STATIC_TYPE).withString(VALUE, namespace);
        assertCompile(rule1.generate(node1).findValue().orElseThrow(), rule.generate(node).findValue().orElseThrow());
    }
}