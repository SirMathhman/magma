package magma.app.compile;

import magma.app.compile.lang.CommonLang;
import magma.app.compile.lang.JavaLang;
import magma.app.compile.lang.MagmaLang;
import magma.api.result.Results;
import magma.app.compile.rule.Rule;
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

            Compiler compiler = new Compiler(Results.unwrap(((Rule) JavaLang.createRecordRule()).generate(sourceNode).unwrap()));
            final var actual = compiler.compile().output();
            assertEquals(Results.unwrap(((Rule) MagmaLang.createFunctionRule()).generate(targetNode).unwrap()), actual);
        } catch (CompileException e) {
            fail(e);
        }
    }

    @Test
    void invalid() {
        assertThrows(CompileException.class, () -> new Compiler("test").compile().output());
    }
}