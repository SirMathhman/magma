package magma.app.compile;

import magma.api.result.Result;
import magma.app.compile.lang.MagmaLang;
import magma.app.compile.pass.TreePassingStage;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static magma.app.compile.lang.JavaLang.createRootRule;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void invalid() {
        assertThrows(CompileException.class, () -> {
            Compiler compiler = new Compiler("test", createRootRule(), new TreePassingStage(Collections.emptyList()), MagmaLang.createRootRule());
            unwrap(compiler.compile()).output();
        });
    }
}