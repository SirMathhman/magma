package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CompilerTest {

    @ParameterizedTest
    @ValueSource(strings = {"First", "Second"})
    void recordStatement(String name) {
        try {
            final var node = new MapNode().withString(CommonLang.NAME, name);
            final var actual = new Compiler(JavaLang.RECORD_RULE.generate(node).orElseThrow()).compile();
            assertEquals(MagmaLang.FUNCTION_RULE.generate(node).orElse(""), actual);
        } catch (CompileException e) {
            fail(e);
        }
    }

    @Test
    void invalid() {
        assertThrows(CompileException.class, () -> new Compiler("test").compile());
    }
}