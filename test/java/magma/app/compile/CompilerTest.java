package magma.app.compile;

import magma.api.result.Result;
import magma.app.compile.lang.CommonLang;
import magma.app.compile.lang.JavaLang;
import magma.app.compile.lang.MagmaLang;
import magma.app.compile.rule.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static magma.app.compile.lang.JavaLang.RECORD_TYPE;
import static magma.app.compile.lang.MagmaLang.FUNCTION;
import static org.junit.jupiter.api.Assertions.*;

class CompilerTest {
    @Deprecated
    public static <T, E extends Exception> T unwrap(Result<T, E> result) throws E {
        final var value = result.findValue();
        if (value.isPresent()) return value.get();

        final var error = result.findError();
        if (error.isPresent()) throw error.get();

        throw new RuntimeException();
    }

    @Test
    void importStatic() {

    }

    @ParameterizedTest
    @ValueSource(strings = {"First", "Second"})
    void recordStatement(String name) {
        try {
            final var sourceNode = new MapNode()
                    .retype(RECORD_TYPE)
                    .withString(CommonLang.NAME, name);
            final var targetNode = sourceNode.retype(FUNCTION);

            Compiler compiler = new Compiler(unwrap(((Rule) JavaLang.createRecordRule()).generate(sourceNode).unwrap()));
            final var actual = unwrap(compiler.compile()).output();
            assertEquals(unwrap(((Rule) MagmaLang.createFunctionRule()).generate(targetNode).unwrap()), actual);
        } catch (CompileException e) {
            fail(e);
        }
    }

    @Test
    void invalid() {
        assertThrows(CompileException.class, () -> {
            Compiler compiler = new Compiler("test");
            unwrap(compiler.compile()).output();
        });
    }
}