package magma.app.compile;

import magma.app.compile.lang.CommonLang;
import magma.app.compile.lang.JavaLang;
import magma.app.compile.lang.MagmaLang;
import magma.api.result.Results;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static magma.app.compile.lang.JavaLang.RECORD;
import static magma.app.compile.lang.MagmaLang.FUNCTION;
import static org.junit.jupiter.api.Assertions.*;

class CompilerTest {
    @Test
    void importStatic() {

    }

    @ParameterizedTest
    @ValueSource(strings = {"First", "Second"})
    void recordStatement(String name) {
        try {
            final var sourceNode = new MapNode()
                    .retype(RECORD)
                    .withString(CommonLang.NAME, name);
            final var targetNode = sourceNode.retype(FUNCTION);

            final var actual = new Compiler(Results.unwrap(JavaLang.RECORD_RULE.generate(sourceNode).first().orElseThrow())).compile();
            assertEquals(Results.unwrap(MagmaLang.FUNCTION_RULE.generate(targetNode).first().orElseThrow()), actual);
        } catch (CompileException e) {
            fail(e);
        }
    }

    @Test
    void invalid() {
        assertThrows(CompileException.class, () -> new Compiler("test").compile());
    }
}