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
        final var node = new Node(Optional.of(JavaLang.CLASS_TYPE)).withString(VALUE, className);
        Rule rule1 = JavaLang.createClassRule();
        final var input = rule1.generate(node).findValue().orElseThrow();
        Rule rule = MagmaLang.createFunctionRule();
        Node node1 = node.retype(MagmaLang.FUNCTION_TYPE);
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
        var rule = CommonLang.createInstanceImportRule();
        var rule1 = JavaLang.createStaticImportRule();

        var node = new Node(CommonLang.IMPORT_TYPE).withString(VALUE, namespace);
        var node1 = new Node(JavaLang.IMPORT_STATIC_TYPE).withString(VALUE, namespace);
        assertCompile(rule1.generate(node1).findValue().orElseThrow(), rule.generate(node).findValue().orElseThrow());
    }
}