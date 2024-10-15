package magma.compile;

import magma.compile.lang.CommonLang;
import magma.compile.lang.JavaLang;
import magma.compile.lang.MagmaLang;
import magma.result.Results;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static magma.compile.lang.JavaLang.RECORD;
import static magma.compile.lang.MagmaLang.FUNCTION;
import static org.junit.jupiter.api.Assertions.*;

class CompilerTest {

    @ParameterizedTest
    @ValueSource(strings = {"First", "Second"})
    void recordStatement(String name) {
        try {
            final var sourceNode = new MapNode()
                    .retype(RECORD)
                    .withString(CommonLang.NAME, name);
            final var targetNode = sourceNode.retype(FUNCTION);

            final var actual = new Compiler(Results.unwrap(JavaLang.RECORD_RULE.generate(sourceNode))).compile();
            assertEquals(Results.unwrap(MagmaLang.FUNCTION_RULE.generate(targetNode)), actual);
        } catch (CompileException e) {
            fail(e);
        }
    }

    @Test
    void invalid() {
        assertThrows(CompileException.class, () -> new Compiler("test").compile());
    }
}