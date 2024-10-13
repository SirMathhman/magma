package magma;

import magma.result.Results;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static magma.JavaLang.RECORD;
import static org.junit.jupiter.api.Assertions.*;

class CompilerTest {

    @ParameterizedTest
    @ValueSource(strings = {"First", "Second"})
    void recordStatement(String name) {
        try {
            final var node = new MapNode()
                    .retype(RECORD)
                    .strings()
                    .with(CommonLang.NAME, name);

            final var actual = new Compiler(Results.unwrap(JavaLang.RECORD_RULE.generate(node))).compile();
            assertEquals(MagmaLang.FUNCTION_RULE.generate(node).findValue().orElse(""), actual);
        } catch (CompileException e) {
            fail(e);
        }
    }
    @Test
    void invalid() {
        assertThrows(CompileException.class, () -> new Compiler("test").compile());
    }
}