package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
        assertCompile(renderClass(className), renderFunction(className));
    }

    @Test
    void rootMemberInvalid() {
        assertThrows(CompileException.class, () -> compile("?"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void importStatic(String namespace) {
        assertCompile(renderStaticImport(namespace), renderInstanceImport(namespace));
    }
}