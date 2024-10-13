package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static magma.Compiler.FUNCTION;
import static org.junit.jupiter.api.Assertions.*;

class CompilerTest {
    private static String renderRecord(String name) {
        return Compiler.RECORD_KEYWORD_WITH_SPACE + name + Compiler.RECORD_SUFFIX;
    }

    @ParameterizedTest
    @ValueSource(strings = {"First", "Second"})
    void recordStatement(String name) {
        try {
            final var actual = new Compiler(renderRecord(name)).compile();
            assertEquals(FUNCTION.generate(new Node(name)).orElse(""), actual);
        } catch (CompileException e) {
            fail(e);
        }
    }

    @Test
    void invalid() {
        assertThrows(CompileException.class, () -> new Compiler("test").compile());
    }
}