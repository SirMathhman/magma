package magma.app.compile;

import magma.api.result.Result;
import org.junit.jupiter.api.Test;

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
            Compiler compiler = new Compiler("test");
            unwrap(compiler.compile()).output();
        });
    }
}